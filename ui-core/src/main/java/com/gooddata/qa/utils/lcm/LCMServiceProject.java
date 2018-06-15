package com.gooddata.qa.utils.lcm;

import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.ProcessExecution;
import com.gooddata.dataload.processes.ProcessExecutionDetail;
import com.gooddata.dataload.processes.ProcessService;
import com.gooddata.dataload.processes.ProcessType;
import com.gooddata.project.Project;
import com.gooddata.project.ProjectService;
import com.gooddata.qa.graphene.common.TestParameters;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile.Column;
import com.gooddata.qa.graphene.entity.disc.Parameters;
import com.gooddata.qa.graphene.enums.process.Parameter;
import com.gooddata.qa.utils.ads.AdsHelper;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestClient.RestProfile;
import com.gooddata.warehouse.Warehouse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.logging.Logger;
import static java.util.stream.Collectors.joining;

/**
 * This class present a lcm service project, it means a project contains ruby processes to run bricks
 * This project has 4 dataload process, first process to load data to ads instance associated to project and
 * 3 process for running release brick, provision brick and rollout brick
 */
public final class LCMServiceProject {
    private static final Logger log = Logger.getLogger(LCMServiceProject.class.getName());
    private static final String UPDATE_ADS_TABLE_EXECUTABLE = "DLUI/graph/CreateAndCopyDataToADS.grf";
    //List of segments which used to be released by this service project, this intend for cleanup
    private final Set<String> associatedSegments = new HashSet<>();
    //id of this service project which contains 4 dataload process
    private String projectId;
    //this lcm project contains 3 ruby process which execute brick to release, provision and rollout
    private RubyProcess releaseProcess;
    private RubyProcess provisionProcess;
    private RubyProcess rolloutProcess;
    //ads associated to this project, used as lcm repository
    private Warehouse ads;
    //process to load data to associated ads
    private DataloadProcess updateAdsTableProcess;
    //default params to run ads data load process
    private Supplier<Parameters> defaultAdsParameters;

    private RestClient restClient;

    /**
     * Create a service project that contains 3 ruby-run process: release, provision, rollout and one ads dataload process
     *
     * @return LCMServiceProject
     */
    public static LCMServiceProject newWorkFlow(final TestParameters testParameters) {
        return new LCMServiceProject(testParameters);
    }

    private LCMServiceProject() {
        //prevent default constructor
    }

    private LCMServiceProject(final TestParameters testParameters) {
        try {
            this.restClient = createDomainRestClient(testParameters);
            this.projectId = createNewEmptyProject(testParameters, "ATT Service Project");
            log.info("--->Created service project:" + this.projectId);

            initAdsInstance(testParameters);
            log.info("--->Created ads instance has uri:" + ads.getUri());
            createRubyProcesses(testParameters);
        } catch (Exception e) {
            throw new RuntimeException("Cannot init lcm project", e);
        }
    }

    /**
     * Delete devs/masters project, clientIds, segments which have involved in this lcm model
     */
    public void cleanUp() {
        try {
            log.info("---Removing ads instance");
            AdsHelper adsHelper = new AdsHelper(restClient, this.projectId);
            adsHelper.removeAds(ads);
            log.info("---Deleting associated segments");
            LcmRestUtils.deleteSegments(this.restClient, this.associatedSegments);
            log.info("---Deleting service project");
            deleteProject(this.projectId);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getProjectId() {
        return projectId;
    }

    /**
     * Run release process
     *
     * @param segments
     */
    public void release(final JSONArray segments) {
        addReleasedSegmentsToAssociatedList(segments);
        Parameters releaseTemplate = getReleaseParamsTemplate();
        JSONObject encodedParamObj = new JSONObject(releaseTemplate.getParameters().get("gd_encoded_params"));
        encodedParamObj.put("segments", segments);
        String encodesString = encodedParamObj.toString();
        log.info(encodesString);
        releaseTemplate.getParameters().put("gd_encoded_params", encodesString);
        releaseProcess.execute(releaseTemplate);
    }

    /**
     * Run provision project
     * @param inputSource
     */
    public void provision(final JSONObject inputSource) {
        Parameters provisionTemplate = getProvisionParamsTemplate();
        JSONObject encodedParamObj = new JSONObject(provisionTemplate.getParameters().get("gd_encoded_params"));
        encodedParamObj.put("input_source", inputSource);
        String encodesString = encodedParamObj.toString();
        log.info(encodesString);
        provisionTemplate.getParameters().put("gd_encoded_params", encodesString);

        provisionProcess.execute(provisionTemplate);
    }

    /**
     * Run rollout process
     *
     * @param segments
     */
    public void rollout(final JSONArray segments) {
        Parameters rolloutTemplate = getRolloutParamsTemplate();
        JSONObject encodedParamObj = new JSONObject(rolloutTemplate.getParameters().get("gd_encoded_params"));
        encodedParamObj.put("segments_filter", segments);
        String encodesString = encodedParamObj.toString();
        log.info(encodesString);
        rolloutTemplate.getParameters().put("gd_encoded_params", encodesString);

        rolloutProcess.execute(rolloutTemplate);
    }

    /**
     * @param segmentId
     * @param clientId
     * @param clientProjectIds
     * @return
     */
    public JSONObject createProvisionDatasource(final String segmentId,
                                                final String clientId, String... clientProjectIds) {
        CsvFile csvFile = new CsvFile("clients")
                .columns(new Column("segment_id"), new Column("client_id"), new Column("project_id"));
        Arrays.stream(clientProjectIds).forEach(
                clientProjectId -> csvFile.rows(segmentId, clientId, String.format("/gdc/projects/%s", clientProjectId)));

        createAdsTableFromFile(csvFile);

        return new JSONObject() {{
            put("type", "ads");
            put("query", "SELECT segment_id, client_id, project_id FROM clients;");
        }};
    }

    public Parameters getReleaseParamsTemplate() {
        return releaseProcess.getDefaultParameters();
    }

    public Parameters getProvisionParamsTemplate() {
        return provisionProcess.getDefaultParameters();
    }

    public Parameters getRolloutParamsTemplate() {
        return rolloutProcess.getDefaultParameters();
    }

    /**
     * Create an ads table which associacated with LCM service project, ruby process use this table as input to run
     * provision
     *
     * @param file
     * @return this
     */
    private LCMServiceProject createAdsTableFromFile(final CsvFile file) {
        final String sql = buildSql(file);
        log.info(sql);
        executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                defaultAdsParameters.get().addParameter(Parameter.SQL_QUERY, sql));
        return this;
    }

    /**
     * Create ads instance where become a repository for ruby process
     *
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    private LCMServiceProject initAdsInstance(final TestParameters testParameters)
            throws IOException {
        AdsHelper adsHelper = new AdsHelper(createDomainRestClient(testParameters), this.projectId);
        ads = adsHelper.createAds("ads-lcm-" + generateHashString(),
                testParameters.loadProperty("dss.authorizationToken"));

        adsHelper.associateAdsWithProject(ads, projectId, "", "");
        updateAdsTableProcess = getProcessService().createProcess(getProject(),
                new DataloadProcess(generateProcessName(), ProcessType.GRAPH),
                getResourceAsFile("/zip-file/adsTable.zip"));

        defaultAdsParameters = () -> new Parameters()
                .addParameter(Parameter.ADS_URL, ads.getConnectionUrl())
                .addParameter(Parameter.ADS_USER, testParameters.getDomainUser())
                .addSecureParameter(Parameter.ADS_PASSWORD, testParameters.getPassword());
        return this;
    }

    private LCMServiceProject createRubyProcesses(final TestParameters testParameters) {
        this.releaseProcess = RubyProcess.ofRelease(testParameters, ads.getConnectionUrl(), this.projectId);
        this.provisionProcess = RubyProcess.ofProvision(testParameters, ads.getConnectionUrl(), this.projectId);
        this.rolloutProcess = RubyProcess.ofRollout(testParameters, ads.getConnectionUrl(), this.projectId);
        return this;
    }

    /**
     * Get file instance of resource, use this to create a input File of
     * a dataload process to use for creating ads instance
     *
     * @param resourcePath path of adsTable
     * @return file
     */
    private File getResourceAsFile(final String resourcePath) {
        try {
            InputStream in = getClass().getResourceAsStream(resourcePath);
            if (in == null) {
                throw new RuntimeException("Cannot read resource: " + resourcePath);
            }

            File tempFile = File.createTempFile(String.valueOf(in.hashCode()), ".tmp");
            tempFile.deleteOnExit();

            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildSql(final CsvFile file) {
        return new StringBuilder()
                .append(createTable(file))
                .append(insertData(file))
                .toString();
    }

    private String createTable(final CsvFile file) {
        return new StringBuilder()
                .append("DROP TABLE IF EXISTS ${table};")
                .append("CREATE TABLE ${table}(")
                .append(file.getColumnNames().stream().map(name -> name.replace(name, name + " VARCHAR(128)"))
                        .collect(joining(", ")))
                .append(");")
                .toString()
                .replace("${table}", file.getName());
    }

    private String insertData(final CsvFile file) {
        return file.getDataRows().stream()
                .map(row -> "INSERT into ${table} values (" + row.stream().map(value -> "'" + value + "'")
                        .collect(joining(", ")) + ");")
                .collect(joining())
                .replace("${table}", file.getName());
    }

    /**
     * execute dataload process to put data to associated ads
     *
     * @param process
     * @param executable
     * @param parameters
     * @return
     */
    private ProcessExecutionDetail executeProcess(final DataloadProcess process, final String executable,
                                                  final Parameters parameters) {
        return restClient.getProcessService()
                .executeProcess(new ProcessExecution(process, executable,
                        parameters.getParameters(), parameters.getSecureParameters()))
                .get();
    }

    private String generateHashString() {
        return UUID.randomUUID().toString().substring(0, 5);
    }

    private ProcessService getProcessService() {
        return restClient.getProcessService();
    }

    private Project getProject() {
        return restClient.getProjectService().getProjectById(projectId);
    }

    private String generateProcessName() {
        return "Ads dataload process-" + generateHashString();
    }

    /**
     * Add segments input of release process to associated list, later used this list to clean up
     *
     * @param segments
     */
    private void addReleasedSegmentsToAssociatedList(final JSONArray segments) {
        segments.forEach(segment -> {
            JSONObject obj = (JSONObject) segment;
            this.associatedSegments.add(obj.getString("segment_id"));
        });
    }

    private String createNewEmptyProject(final TestParameters testParameters, final String projectTitle) {
        final Project project = new Project(projectTitle, testParameters.getAuthorizationToken());
        project.setDriver(testParameters.getProjectDriver());
        project.setEnvironment(testParameters.getProjectEnvironment());

        return restClient.getProjectService().createProject(project).get().getId();
    }

    private RestClient createDomainRestClient(final TestParameters testParameters) {
        return new RestClient(
                new RestProfile(testParameters.getHost(), testParameters.getDomainUser(),
                        testParameters.getPassword(), true));
    }

    private void deleteProject(final String projectId) {
        final ProjectService service = restClient.getProjectService();
        service.removeProject(service.getProjectById(projectId));
    }
}
