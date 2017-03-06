package com.gooddata.qa.graphene;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.MAQL_FILES;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.SQL_FILES;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.ZIP_FILES;
import static com.gooddata.qa.utils.http.RestUtils.ACCEPT_TEXT_PLAIN_WITH_VERSION;
import static com.gooddata.qa.utils.http.RestUtils.executeRequest;
import static com.gooddata.qa.utils.http.RestUtils.getJsonObject;
import static com.gooddata.qa.utils.http.RestUtils.getResource;
import static com.gooddata.qa.utils.http.model.ModelRestUtils.getDatasetElementFromModelView;
import static com.gooddata.qa.utils.http.process.ProcessRestUtils.DATALOAD_PROCESS_TYPE;
import static com.gooddata.qa.utils.http.rolap.RolapRestUtils.executeMAQL;
import static com.gooddata.qa.utils.http.rolap.RolapRestUtils.getAsyncTaskStatus;
import static com.gooddata.qa.utils.http.rolap.RolapRestUtils.waitingForAsyncTask;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsFile;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;
import static com.gooddata.qa.utils.ads.AdsHelper.ADS_DB_CONNECTION_URL;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableMap;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.http.ParseException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;
import org.testng.annotations.BeforeClass;

import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.ProcessExecution;
import com.gooddata.dataload.processes.ProcessExecutionDetail;
import com.gooddata.dataload.processes.ProcessService;
import com.gooddata.md.Attribute;
import com.gooddata.md.Fact;
import com.gooddata.md.Metric;
import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.md.report.Report;
import com.gooddata.md.report.ReportDefinition;
import com.gooddata.qa.graphene.entity.dlui.DataSource;
import com.gooddata.qa.graphene.enums.DatasetElements;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.disc.projects.ProjectDetailPage;
import com.gooddata.qa.utils.ads.AdsHelper;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;
import com.gooddata.report.ReportExportFormat;
import com.gooddata.warehouse.Warehouse;
import com.google.common.collect.Lists;

public class AbstractMSFTest extends AbstractProjectTest {

    protected static final String DEFAULT_DATAlOAD_PROCESS_NAME = "Automated Data Distribution";
    protected static final String CLOUDCONNECT_PROCESS_PACKAGE = "dlui.zip";
    protected static final String DLUI_GRAPH_CREATE_AND_COPY_DATA_TO_ADS =
            "DLUI/graph/CreateAndCopyDataToADS.grf";

    @SuppressWarnings("serial")
    protected static final Map<String, String> SYNCHRONIZE_ALL_PARAM = unmodifiableMap(new HashMap<String, String>() {{
        put("GDC_DE_SYNCHRONIZE_ALL", Boolean.TRUE.toString());
    }});

    private String dssAuthorizationToken;

    protected String technicalUser;
    protected String technicalUserPassword;
    protected String initialLdmMaqlFile = "create-ldm.txt";

    protected DataloadProcess cloudconnectProcess;
    protected Warehouse ads;

    @BeforeClass(alwaysRun = true)
    public void initialProperties() {
        dssAuthorizationToken = testParams.loadProperty("dss.authorizationToken");

        technicalUser = testParams.loadProperty("technicalUser");
        technicalUserPassword = testParams.loadProperty("technicalUserPassword");
    }

    /*
     * Hook methods for prepareLDMAndADSInstance
     */
    protected void addUsersToProject() {}
    protected void addUsersToAdsInstance() {}

    protected Warehouse createAds(final String name) {
        return getAdsHelper().createAds(name, dssAuthorizationToken);
    }

    protected ProcessService getProcessService() {
        return getGoodDataClient().getProcessService();
    }

    protected void enableDataExplorer() {
        ProjectRestUtils.setFeatureFlagInProject(getGoodDataClient(), testParams.getProjectId(),
                ProjectFeatureFlags.ENABLE_DATA_EXPLORER, true);
    }

    protected void prepareLDMAndADSInstance() throws IOException, JSONException {
        // Override this method in test class if it's necessary to add more users to project
        addUsersToProject();
        updateModelOfGDProject(getResourceAsString("/" + MAQL_FILES + "/" + initialLdmMaqlFile));

        ads = createAds("ADS Instance for DLUI test");

        // Override this method in test class if it's necessary to add more users to ads instance
        addUsersToAdsInstance();
    }

    protected void setUpOutputStageAndCreateCloudConnectProcess()
            throws ParseException, JSONException, IOException {
        // Override this method in test class if using other user to set default schema for output stage
        setDefaultSchemaForOutputStage(ads);
        assertTrue(getDataloadProcess().isPresent(), "DATALOAD process is not created!");

        cloudconnectProcess = createProcess("Initial Data for ADS Instance", "GRAPH",
                getResourceAsFile("/" + ZIP_FILES + "/" + CLOUDCONNECT_PROCESS_PACKAGE));
    }

    protected void setDefaultSchemaForOutputStage(final Warehouse ads)
            throws ParseException, JSONException, IOException {
        getAdsHelper().associateAdsWithProject(ads, testParams.getProjectId());
    }

    protected void createUpdateADSTableBySQLFiles(final String createTableFile, final String copyTableFile,
            final Warehouse ads) {
        final Map<String, String> params = prepareParamsToUpdateADS(createTableFile, copyTableFile, ads.getId());
        final Map<String, String> hiddenParams = prepareHiddenParamsToUpdateADS();
        assertTrue(executeProcess(cloudconnectProcess, DLUI_GRAPH_CREATE_AND_COPY_DATA_TO_ADS, params, 
                hiddenParams).isSuccess());
    }

    protected DataSource prepareADSTable(final ADSTables adsTable) {
        createUpdateADSTable(adsTable);
        return new DataSource().withName(adsTable.getDatasourceName()).withDatasets(adsTable.getDatasets());
    }

    protected void createUpdateADSTable(final ADSTables adsTable) {
        createUpdateADSTableBySQLFiles(adsTable.getCreateTableSqlFile(), adsTable.getCopyTableSqlFile(), ads);
    }

    protected Map<String, String> prepareHiddenParamsToUpdateADS() {
        final Map<String, String> hiddenParams = new HashMap<>();
        hiddenParams.put("ADS_PASSWORD", testParams.getPassword());
        return hiddenParams;
    }

    protected Map<String, String> prepareParamsToUpdateADS(final String createTableSqlFile,
            final String copyTableSqlFile, final String adsId) {
        final Map<String, String> params = new HashMap<>();
        final String createTableSql = getResourceAsString("/" + SQL_FILES + "/" + createTableSqlFile);
        final String copyTableSql = getResourceAsString("/" + SQL_FILES + "/" + copyTableSqlFile);
        final String adsUrl = format(ADS_DB_CONNECTION_URL, testParams.getHost(), adsId);

        params.put("CREATE_TABLE", createTableSql);
        params.put("COPY_TABLE", copyTableSql);
        params.put("ADS_URL", adsUrl);
        params.put("ADS_USER", testParams.getUser());
        return params;
    }

    protected void updateModelOfGDProject(final String maql) throws IOException, JSONException {
        log.info("Update model of GD project!");
        String pollingUri = sendRequestToUpdateModel(maql);
        waitingForAsyncTask(getRestApiClient(), pollingUri);
        assertEquals(getAsyncTaskStatus(getRestApiClient(), pollingUri), "OK",
                "Model is not updated successfully!");
    }

    protected void dropAddedFieldsInLDM(final String maql) throws IOException, JSONException {
        final String pollingUri = sendRequestToUpdateModel(maql);
        waitingForAsyncTask(getRestApiClient(), pollingUri);
        if ("OK".equals(getAsyncTaskStatus(getRestApiClient(), pollingUri)))
            return;

        final String errorMessage = getJsonObject(getRestApiClient(), pollingUri)
            .getJSONObject("wTaskStatus")
            .getJSONArray("messages")
            .getJSONObject(0)
            .getJSONObject("error")
            .getString("message");
        log.info("LDM update is failed with error message: " + errorMessage);
        assertEquals(errorMessage, "The object (%s) doesn't exist.");
    }

    protected DataloadProcess createProcess(final String name, final String type, File processData) {
        return getProcessService().createProcess(getProject(), new DataloadProcess(name, type), processData);
    }

    protected DataloadProcess createProcess(final String name, final String type) {
        if (DATALOAD_PROCESS_TYPE.equals(type)) {
            final Optional<DataloadProcess> dataloadProcess = getProcessService()
                .listProcesses(getProject())
                .stream()
                .filter(process -> type.equals(process.getType()))
                .findFirst();
            if (dataloadProcess.isPresent()) return dataloadProcess.get();
        }
        return getProcessService().createProcess(getProject(), new DataloadProcess(name, type));
    }

    protected ProcessExecutionDetail executeProcess(final DataloadProcess process, final String executable,
            final Map<String, String> params) {
        return getProcessService()
                .executeProcess(new ProcessExecution(process, executable, params))
                .get();
    }

    protected ProcessExecutionDetail executeProcess(final DataloadProcess process, final String executable,
            final Map<String, String> params, final Map<String, String> hiddenParams) {
        return getProcessService()
                .executeProcess(new ProcessExecution(process, executable, params, hiddenParams))
                .get();
    }

    protected DataloadProcess deleteDataloadProcessAndCreateNewOne() {
        final Optional<DataloadProcess> dataloadProcess = getDataloadProcess();
        if (dataloadProcess.isPresent()) {
            getProcessService().removeProcess(dataloadProcess.get());
        }
        return createProcess(DEFAULT_DATAlOAD_PROCESS_NAME, DATALOAD_PROCESS_TYPE);
    }

    protected String getDataloadProcessUri() {
        return getDataloadProcess().get().getUri();
    }

    protected String getDataloadProcessId() {
        return getDataloadProcess().get().getId();
    }

    protected Optional<DataloadProcess> getDataloadProcess() {
        return getProcessService()
            .listProcesses(getProject())
            .stream()
            .filter(process -> DATALOAD_PROCESS_TYPE.equals(process.getType()))
            .findFirst();
    }

    protected String getExecutionStatus(final RestApiClient restApiClient, final String executionUri)
            throws JSONException, IOException {
        return getJsonObject(restApiClient, executionUri + "/detail")
                .getJSONObject("executionDetail")
                .getString("status");
    }

    protected boolean isExecutionSuccessful(final RestApiClient restApiClient, final String executionUri)
            throws JSONException, IOException {
        waitingForAsyncTask(restApiClient, executionUri);
        return "OK".equals(getExecutionStatus(restApiClient, executionUri));
    }

    protected int redeployDataLoadProcess(final RestApiClient restApiClient) throws IOException, JSONException {
        final String body = new JSONObject().put("process", new JSONObject() {{
            put("type", DATALOAD_PROCESS_TYPE);
            put("name", DEFAULT_DATAlOAD_PROCESS_NAME);
        }}).toString();

        return executeRequest(restApiClient, restApiClient.newPutMethod(getDataloadProcessUri(), body));
    }

    protected void verifyValidLink(final RestApiClient restApiClient, final String link) {
        executeRequest(restApiClient, restApiClient.newGetMethod(link), HttpStatus.OK);
    }

    protected String getDiffResourceContent(final RestApiClient restApiClient, final HttpStatus status)
            throws ParseException, IOException {
        return getResource(restApiClient,
                restApiClient.newGetMethod(format(AdsHelper.OUTPUT_STAGE_URI, testParams.getProjectId()) + "diff"),
                req -> req.setHeader("Accept", ACCEPT_TEXT_PLAIN_WITH_VERSION),
                status);
    }

    protected List<String> getReferencesOfDataset(final String dataset) throws ParseException, JSONException,
            IOException {
        JSONArray array = getDatasetElementFromModelView(getRestApiClient(), testParams.getProjectId(),
                dataset, DatasetElements.REFERENCES, JSONArray.class);
        return new ObjectMapper().readValue(array.toString(), new TypeReference<List<String>>() {});
    }

    protected void prepareMetricToCheckNewAddedFields(final String... facts) {
        Stream.of(facts).forEach(fact -> {
            createMetric(fact + " [Sum]", format("SELECT SUM([%s])",
                    getMdService().getObjUri(getProject(), Fact.class, title(fact))), "#,##0.00");
        });
    }

    protected void createAndCheckReport(final String reportName, final String attribute, final String metric,
            final Collection<String> attributeValues, final Collection<String> metricValues) {
        final Metric m = getMdService().getObj(getProject(), Metric.class, title(metric));
        final Attribute attr = getMdService().getObj(getProject(), Attribute.class, title(attribute));
        ReportDefinition definition = GridReportDefinitionContent.create(
                reportName,
                asList(METRIC_GROUP),
                asList(new AttributeInGrid(attr.getDefaultDisplayForm().getUri(), attr.getTitle())),
                asList(new MetricElement(m)));
        definition = getMdService().createObj(getProject(), definition);
        final Report report = getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        getGoodDataClient().getReportService().exportReport(report, ReportExportFormat.CSV, output).get();

        final List<String> attributes = Lists.newArrayList();
        final List<String> metrics = Lists.newArrayList();
        List<String> result;
        try (CsvListReader reader = new CsvListReader(new InputStreamReader(
                new ByteArrayInputStream(output.toByteArray())), CsvPreference.STANDARD_PREFERENCE)) {
            reader.getHeader(true);
            while ((result = reader.read()) != null && !result.isEmpty()) {
                String attributeValue = (result.get(0) != null) ? result.get(0).trim() : "(empty value)";
                attributes.add(attributeValue);
                String metricValue = (result.get(1) != null) ? result.get(1).trim() : "";
                metrics.add(metricValue);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        log.info("Attributes: " + attributes.toString());
        assertTrue(isEqualCollection(attributes, attributeValues), "Incorrect attribute values!");

        log.info("Metric: " + metrics.toString());
        assertTrue(isEqualCollection(metrics, metricValues), "Incorrect metric values!");
    }

    protected ProjectDetailPage openProjectDetailPage() {
        openUrl(format(ProjectDetailPage.URI, testParams.getProjectId()));
        return ProjectDetailPage.getInstance(browser);
    }

    protected void checkReportAfterAddReferenceToDataset() {
        prepareMetricToCheckNewAddedFields("number");
        createAndCheckReport("Report to check reference", "artistname", "number [Sum]",
            asList("OOP1", "OOP2", "OOP3", "OOP4", "OOP5", "OOP6", "OOP7", "OOP8"),
            asList("1000", "1200", "1400", "1600", "1800", "2000", "700", "800"));
    }

    private String sendRequestToUpdateModel(final String maql) throws ParseException, JSONException, IOException {
        return executeMAQL(getRestApiClient(), testParams.getProjectId(), maql);
    }
}
