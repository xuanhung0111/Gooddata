package com.gooddata.qa.graphene;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.MAQL_FILES;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.SQL_FILES;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.ZIP_FILES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.http.RestUtils.executeRequest;
import static com.gooddata.qa.utils.http.RestUtils.getJsonObject;
import static com.gooddata.qa.utils.http.RestUtils.getResource;
import static com.gooddata.qa.utils.http.model.ModelRestUtils.getDatasetElementFromModelView;
import static com.gooddata.qa.utils.http.rolap.RolapRestUtils.executeMAQL;
import static com.gooddata.qa.utils.http.rolap.RolapRestUtils.getAsyncTaskStatus;
import static com.gooddata.qa.utils.http.rolap.RolapRestUtils.waitingForAsyncTask;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsFile;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.isNull;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.http.ParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
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
import com.gooddata.md.report.GridElement;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.Report;
import com.gooddata.md.report.ReportDefinition;
import com.gooddata.qa.graphene.entity.DataSource;
import com.gooddata.qa.graphene.entity.Dataset;
import com.gooddata.qa.graphene.entity.Field;
import com.gooddata.qa.graphene.entity.Field.FieldTypes;
import com.gooddata.qa.graphene.entity.disc.ProjectInfo;
import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.enums.DatasetElements;
import com.gooddata.qa.graphene.utils.AdsHelper;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.report.ReportExportFormat;
import com.gooddata.warehouse.Warehouse;
import com.google.common.collect.Lists;

public class AbstractMSFTest extends AbstractProjectTest {

    protected static final String DATALOAD = "DATALOAD";

    protected static final String OUTPUTSTAGE_URI = "/gdc/dataload/projects/%s/outputStage/";
    protected static final String OUTPUT_STAGE_METADATA_URI = OUTPUTSTAGE_URI + "metadata";
    protected static final String INTERNAL_OUTPUT_STAGE_URI = "/gdc/dataload/internal/projects/%s/outputStage/";
    protected static final String MAPPING_RESOURCE = INTERNAL_OUTPUT_STAGE_URI + "mapping";
    protected static final String OUTPUT_STATE_MODEL_RESOURCE = INTERNAL_OUTPUT_STAGE_URI + "model";
    protected static final String PROJECT_MODEL_VIEW = "/gdc/projects/%s/model/view";

    protected static final String DEFAULT_DATAlOAD_PROCESS_NAME = "ADS to LDM synchronization";
    protected static final String CLOUDCONNECT_PROCESS_PACKAGE = "dlui.zip";
    protected static final String DLUI_GRAPH_CREATE_AND_COPY_DATA_TO_ADS =
            "DLUI/graph/CreateAndCopyDataToADS.grf";
    protected static final String ADS_URL =
            "jdbc:gdc:datawarehouse://${host}/gdc/datawarehouse/instances/${adsId}";
    protected static final String ACCEPT_APPLICATION_JSON_WITH_VERSION = "application/json; version=1";
    protected static final String ACCEPT_TEXT_PLAIN_WITH_VERSION = "text/plain; version=1";

    @SuppressWarnings("serial")
    protected static final Map<String, String> SYNCHRONIZE_ALL_PARAM = unmodifiableMap(new HashMap<String, String>() {{
        put("GDC_DE_SYNCHRONIZE_ALL", Boolean.TRUE.toString());
    }});

    private String dssAuthorizationToken;
    private AdsHelper adsHelper;

    protected String technicalUser;
    protected String technicalUserPassword;
    protected String initialLdmMaqlFile = "create-ldm.txt";

    protected ProjectInfo workingProject;
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

    protected AdsHelper getAdsHelper() {
        if (isNull(adsHelper))
            adsHelper = new AdsHelper(getGoodDataClient(), getRestApiClient());
        return adsHelper;
    }

    protected Warehouse createAds(final String name) {
        return getAdsHelper().createAds(name, dssAuthorizationToken);
    }

    protected ProcessService getProcessService() {
        return getGoodDataClient().getProcessService();
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

    protected void customOutputStageMetadata(final DataSource... dataSources)
            throws ParseException, IOException, JSONException {
        final String putBody = prepareOutputStageMetadata(dataSources);
        final String putUri = format(OUTPUT_STAGE_METADATA_URI, getWorkingProject().getProjectId());
        getResource(getRestApiClient(),
                getRestApiClient().newPutMethod(putUri, putBody),
                req -> req.setHeader("Accept", ACCEPT_APPLICATION_JSON_WITH_VERSION),
                HttpStatus.OK);
    }

    protected void createUpdateADSTableBySQLFiles(final String createTableFile, final String copyTableFile,
            final Warehouse ads) {
        final Map<String, String> params = prepareParamsToUpdateADS(createTableFile, copyTableFile, ads.getId());
        assertTrue(executeProcess(cloudconnectProcess, DLUI_GRAPH_CREATE_AND_COPY_DATA_TO_ADS, params).isSuccess());
    }

    protected DataSource prepareADSTable(final ADSTables adsTable) {
        createUpdateADSTable(adsTable);
        return new DataSource().withName(adsTable.datasourceName).withDatasets(adsTable.getDatasets());
    }

    protected void createUpdateADSTable(final ADSTables adsTable) {
        createUpdateADSTableBySQLFiles(adsTable.createTableSqlFile, adsTable.copyTableSqlFile, ads);
    }

    protected Map<String, String> prepareParamsToUpdateADS(final String createTableSqlFile,
            final String copyTableSqlFile, final String adsId) {
        final Map<String, String> params = new HashMap<>();
        final String createTableSql = getResourceAsString("/" + SQL_FILES + "/" + createTableSqlFile);
        final String copyTableSql = getResourceAsString("/" + SQL_FILES + "/" + copyTableSqlFile);
        final String adsUrl = ADS_URL.replace("${host}", testParams.getHost()).replace("${adsId}", adsId);

        params.put("CREATE_TABLE", createTableSql);
        params.put("COPY_TABLE", copyTableSql);
        params.put("ADS_URL", adsUrl);
        params.put("ADS_USER", testParams.getUser());
        params.put("ADS_PASSWORD", testParams.getPassword());
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
        if (DATALOAD.equals(type)) {
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
        return getProcessService().executeProcess(new ProcessExecution(process, executable, params)).get();
    }

    protected void createSchedule(ScheduleBuilder scheduleBuilder) {
        projectDetailPage.clickOnNewScheduleButton();
        waitForFragmentVisible(scheduleForm);
        scheduleForm.createNewSchedule(scheduleBuilder);
        if (scheduleBuilder.isConfirmed())
        waitForFragmentVisible(scheduleDetail);
    }

    protected DataloadProcess deleteDataloadProcessAndCreateNewOne() {
        final Optional<DataloadProcess> dataloadProcess = getDataloadProcess();
        if (dataloadProcess.isPresent()) {
            getProcessService().removeProcess(dataloadProcess.get());
        }
        return createProcess(DEFAULT_DATAlOAD_PROCESS_NAME, DATALOAD);
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
            .filter(process -> DATALOAD.equals(process.getType()))
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
            put("type", DATALOAD);
            put("name", DEFAULT_DATAlOAD_PROCESS_NAME);
        }}).toString();

        return executeRequest(getRestApiClient(), restApiClient.newPutMethod(getDataloadProcessUri(), body));
    }

    protected void verifyValidLink(final RestApiClient restApiClient, final String link) {
        executeRequest(restApiClient, restApiClient.newGetMethod(link), HttpStatus.OK);
    }

    protected String getDiffResourceContent(final RestApiClient restApiClient, final HttpStatus status)
            throws ParseException, IOException {
        return getResource(restApiClient,
                restApiClient.newGetMethod(format(OUTPUTSTAGE_URI, testParams.getProjectId()) + "diff"),
                req -> req.setHeader("Accept", ACCEPT_TEXT_PLAIN_WITH_VERSION),
                status);
    }

    protected String getMappingResourceContent(final RestApiClient restApiClient, final HttpStatus status)
            throws ParseException, IOException {
        return getResource(restApiClient,
                restApiClient.newGetMethod(format(MAPPING_RESOURCE, testParams.getProjectId())),
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
                asList("metricGroup"),
                asList(new AttributeInGrid(attr.getDefaultDisplayForm().getUri())),
                asList(new GridElement(m.getUri(), metric)));
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
            while ((result = reader.read()) != null) {
                attributes.add(result.get(0).trim());
                metrics.add(result.get(1).trim());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        log.info("Attributes: " + attributes.toString());
        assertTrue(isEqualCollection(attributes, attributeValues), "Incorrect attribute values!");

        log.info("Metric: " + metrics.toString());
        assertTrue(isEqualCollection(metrics, metricValues), "Incorrect metric values!");
    }

    protected void openProjectDetailPage(ProjectInfo project) {
        openUrl(DISC_PROJECTS_PAGE_URL);
        waitForElementVisible(discProjectsList.getRoot());
        discProjectsList.clickOnProjectTitle(project);
        waitForElementVisible(projectDetailPage.getRoot());
    }

    protected void checkReportAfterAddReferenceToDataset() {
        prepareMetricToCheckNewAddedFields("number");
        createAndCheckReport("Report to check reference", "artistname", "number [Sum]",
            asList("OOP1", "OOP2", "OOP3", "OOP4", "OOP5", "OOP6", "OOP7", "OOP8"),
            asList("1000", "1200", "1400", "1600", "1800", "2000", "700", "800"));
    }

    protected ProjectInfo getWorkingProject() {
        if (workingProject == null)
            workingProject = new ProjectInfo(projectTitle, testParams.getProjectId());
        return workingProject;
    }

    private String prepareOutputStageMetadata(final DataSource... dataSources) throws JSONException {
        final JSONObject metaObject = new JSONObject();

        final Collection<JSONObject> metadataObjects = Lists.newArrayList();
        for (DataSource dataSource : dataSources) {
            for (Dataset dataset : dataSource.getAvailableDatasets(FieldTypes.ALL)) {
                metadataObjects.add(prepareMetadataObject(dataset.getName(), dataSource.getName()));
            }
        }
        metaObject.put("outputStageMetadata", new JSONObject().put("tableMeta", metadataObjects));

        return metaObject.toString();
    }

    private JSONObject prepareMetadataObject(final String tableName, final String dataSourceName) throws JSONException {
        return new JSONObject() {{
            put("tableMetadata", new JSONObject() {{
                put("table", tableName);
                put("defaultSource", dataSourceName);
                put("columnMeta", new JSONArray());
            }});
        }};
    }

    private String sendRequestToUpdateModel(final String maql) throws ParseException, JSONException, IOException {
        return executeMAQL(getRestApiClient(), getWorkingProject().getProjectId(), maql);
    }

    protected enum AdditionalDatasets {

        PERSON_WITH_NEW_FIELDS("person", new Field("Position", FieldTypes.ATTRIBUTE)),
        PERSON_WITH_NEW_DATE_FIELD(
                "person",
                new Field("Position", FieldTypes.ATTRIBUTE),
                new Field("Date", FieldTypes.DATE)),
        OPPORTUNITY_WITH_NEW_FIELDS(
                "opportunity",
                new Field("Title2", FieldTypes.ATTRIBUTE),
                new Field("Label", FieldTypes.LABEL_HYPERLINK),
                new Field("Totalprice2", FieldTypes.FACT)),
        OPPORTUNITY_WITH_NEW_DATE_FIELD(
                "opportunity",
                new Field("Title2", FieldTypes.ATTRIBUTE),
                new Field("Label", FieldTypes.LABEL_HYPERLINK),
                new Field("Totalprice2", FieldTypes.FACT),
                new Field("Date", FieldTypes.DATE)),
        ARTIST_WITH_NEW_FIELD("artist", new Field("Artisttitle", FieldTypes.ATTRIBUTE)),
        AUTHOR_WITH_NEW_FIELD("author", new Field("Authorname", FieldTypes.ATTRIBUTE)),
        TRACK_WITH_NEW_FIELD("track", new Field("Trackname", FieldTypes.ATTRIBUTE));

        private String name;
        private List<Field> additionalFields;

        private AdditionalDatasets(String name, Field... additionalFields) {
            this.name = name;
            this.additionalFields = Lists.newArrayList(additionalFields);
        }

        public Dataset getDataset() {
            List<Field> fields = Lists.newArrayList();
            for (Field additionalField : additionalFields) {
                fields.add(additionalField.clone());
            }
            return new Dataset().withName(name).withFields(fields);
        }
    }

    protected enum ADSTables {

        WITHOUT_ADDITIONAL_FIELDS("createTable.txt", "copyTable.txt", "Unknown data source"),
        WITH_ADDITIONAL_FIELDS(
                "createTableWithAdditionalFields.txt",
                "copyTableWithAdditionalFields.txt",
                "Unknown data source",
                AdditionalDatasets.PERSON_WITH_NEW_FIELDS,
                AdditionalDatasets.OPPORTUNITY_WITH_NEW_FIELDS),
        WITH_ADDITIONAL_DATE(
                "createTableWithAdditionalDate.txt",
                "copyTableWithAdditionalDate.txt",
                "Unknown data source",
                AdditionalDatasets.PERSON_WITH_NEW_DATE_FIELD,
                AdditionalDatasets.OPPORTUNITY_WITH_NEW_FIELDS),
        WITH_ERROR_MAPPING("createTableWithErrorMapping.txt", "copyTableWithErrorMapping.txt"),
        WITH_ADDITIONAL_FIELDS_LARGE_DATA(
                "createTableWithAdditionalFields.txt",
                "copyTableWithAdditionalFieldsLargeData.txt",
                "Unknown data source",
                AdditionalDatasets.PERSON_WITH_NEW_FIELDS,
                AdditionalDatasets.OPPORTUNITY_WITH_NEW_FIELDS),
        WITH_ADDITIONAL_FIELDS_AND_REFERECES(
                "createTableWithReferences.txt",
                "copyTableWithReferences.txt",
                "Unknown data source",
                AdditionalDatasets.ARTIST_WITH_NEW_FIELD,
                AdditionalDatasets.TRACK_WITH_NEW_FIELD),
        WITH_ADDITIONAL_FIELDS_AND_MULTI_REFERECES(
                "createTableWithMultiReferences.txt",
                "copyTableWithMultiReferences.txt",
                "Unknown data source",
                AdditionalDatasets.TRACK_WITH_NEW_FIELD,
                AdditionalDatasets.ARTIST_WITH_NEW_FIELD,
                AdditionalDatasets.AUTHOR_WITH_NEW_FIELD),
        WITH_ADDITIONAL_CONNECTION_POINT(
                "createTableWithAdditionalConnectionPoint.txt",
                "copyTableWithAdditionalConnectionPoint.txt",
                "Unknown data source",
                AdditionalDatasets.PERSON_WITH_NEW_FIELDS,
                AdditionalDatasets.OPPORTUNITY_WITH_NEW_FIELDS),
        WITH_ADDITIONAL_LABEL_OF_NEW_FIELD(
                "createTableWithAdditionalLabelOfNewField.txt",
                "copyTableWithAdditionalLabelOfNewField.txt",
                "Unknown data source",
                AdditionalDatasets.PERSON_WITH_NEW_FIELDS,
                AdditionalDatasets.OPPORTUNITY_WITH_NEW_FIELDS);

        private String createTableSqlFile;
        private String copyTableSqlFile;
        private String datasourceName;
        private List<AdditionalDatasets> additionalDatasets = Lists.newArrayList();

        private ADSTables(String createTableSqlFile, String copyTableSqlFile) {
            this(createTableSqlFile, copyTableSqlFile, "");
        }

        private ADSTables(String createTableSqlFile, String copyTableSqlFile,
                String datasourceName, AdditionalDatasets... datasets) {
            this.createTableSqlFile = createTableSqlFile;
            this.copyTableSqlFile = copyTableSqlFile;
            this.datasourceName = datasourceName;
            this.additionalDatasets = Arrays.asList(datasets);
        }

        public List<Dataset> getDatasets() {
            List<Dataset> datasets = Lists.newArrayList();
            for (AdditionalDatasets additionalDataset : this.additionalDatasets) {
                datasets.add(additionalDataset.getDataset());
            }
            return datasets;
        }
    }
}
