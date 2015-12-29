package com.gooddata.qa.graphene;

import static com.gooddata.qa.graphene.enums.ResourceDirectory.MAQL_FILES;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.SQL_FILES;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.ZIP_FILES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsFile;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;

import com.gooddata.qa.graphene.entity.DataSource;
import com.gooddata.qa.graphene.entity.Dataset;
import com.gooddata.qa.graphene.entity.ExecutionParameter;
import com.gooddata.qa.graphene.entity.Field;
import com.gooddata.qa.graphene.entity.Field.FieldTypes;
import com.gooddata.qa.graphene.entity.ProcessInfo;
import com.gooddata.qa.graphene.entity.disc.ProjectInfo;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.DatasetElements;
import com.gooddata.qa.graphene.enums.metrics.SimpleMetricTypes;
import com.gooddata.qa.graphene.utils.AdsHelper;
import com.gooddata.qa.graphene.utils.ProcessUtils;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.qa.utils.http.RestUtils;
import com.gooddata.qa.utils.webdav.WebDavClient;
import com.gooddata.warehouse.Warehouse;
import com.google.common.collect.Lists;

public class AbstractMSFTest extends AbstractProjectTest {

    private static final String DATALOAD = "DATALOAD";

    protected static final String DATALOAD_PROCESS_URI = "/gdc/projects/%s/dataload/processes/";
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

    protected static final String EXECUTABLE = "executable";
    protected static final String GDC_DE_SYNCHRONIZE_ALL = "GDC_DE_SYNCHRONIZE_ALL";
    protected static final String ADS_USER_PARAM = "ADS_USER";
    protected static final String ADS_PASSWORD_PARAM = "ADS_PASSWORD";
    protected static final String ADS_URL_PARAM = "ADS_URL";
    protected static final String CREATE_ADS_TABLE_PARAM = "CREATE_TABLE";
    protected static final String COPY_ADS_TABLE_PARAM = "COPY_TABLE";
    

    protected String dssAuthorizationToken;
    protected String technicalUser;
    protected String technicalUserPassword;
    protected String initialLdmMaqlFile = "create-ldm.txt";

    protected ProjectInfo workingProject;
    protected ProcessInfo cloudconnectProcess;
    protected ProcessInfo dataloadProcess;
    protected Warehouse ads;
    protected AdsHelper adsHelper;

    @BeforeClass(alwaysRun = true)
    public void initialProperties() {
        dssAuthorizationToken = testParams.loadProperty("dss.authorizationToken");

        technicalUser = testParams.loadProperty("technicalUser");
        technicalUserPassword = testParams.loadProperty("technicalUserPassword");
    }

    protected ProjectInfo getWorkingProject() {
        if (workingProject == null)
            workingProject = new ProjectInfo(projectTitle, testParams.getProjectId());
        return workingProject;
    }

    protected void prepareLDMAndADSInstance() throws JSONException {
        // Override this method in test class if it's necessary to add more users to project
        addUsersToProject();
        updateModelOfGDProject(getResourceAsString("/" + MAQL_FILES + "/" + initialLdmMaqlFile));
        
        adsHelper = new AdsHelper(getGoodDataClient(), getRestApiClient());
        ads = adsHelper.createAds("ADS Instance for DLUI test", dssAuthorizationToken);
        
        // Override this method in test class if it's necessary to add more users to ads instance
        addUsersToAdsInstance();
    }

    protected void setUpOutputStageAndCreateCloudConnectProcess() {
        // Override this method in test class if using other user to set default schema for output stage
        setDefaultSchemaForOutputStage(ads);
        assertTrue(isDataloadProcessCreated(), "DATALOAD process is not created!");

        cloudconnectProcess = new ProcessInfo().withProjectId(testParams.getProjectId())
                        .withProcessName("Initial Data for ADS Instance").withProcessType("GRAPH");
        assertEquals(createCloudConnectProcess(cloudconnectProcess), HttpStatus.CREATED.value());
    }

    protected boolean isDataloadProcessCreated() {
        int dataloadProcessNumber = 0;
        try {
            dataloadProcessNumber =
                    ProcessUtils.getProcessesList(getRestApiClient(),
                            getWorkingProject().getProjectId()).getDataloadProcessCount();
        } catch (Exception e) {
            throw new IllegalStateException("There is an exeception when getting process list!", e);
        }

        return dataloadProcessNumber > 0;
    }

    protected void addUsersToProject() {}

    protected void addUsersToAdsInstance() {}

    protected void setDefaultSchemaForOutputStage(Warehouse ads) {
        adsHelper.associateAdsWithProject(ads, testParams.getProjectId());
    }

    protected void createUpdateADSTableBySQLFiles(String createTableFile, String copyTableFile, Warehouse ads) {
        Collection<ExecutionParameter> params =
                prepareParamsToUpdateADS(createTableFile, copyTableFile, ads.getId());

        String executionUri =
                executeCloudConnectProcess(cloudconnectProcess, DLUI_GRAPH_CREATE_AND_COPY_DATA_TO_ADS, params);
        assertTrue(ProcessUtils.isExecutionSuccessful(getRestApiClient(), executionUri));
    }

    protected Collection<ExecutionParameter> prepareParamsToUpdateADS(String createTableSqlFile,
            String copyTableSqlFile, String adsId) {
        List<ExecutionParameter> params = Lists.newArrayList();

        params.add(new ExecutionParameter(ADS_USER_PARAM, testParams.getUser()));
        params.add(new ExecutionParameter(ADS_PASSWORD_PARAM, testParams.getPassword()));
        params.add(new ExecutionParameter(ADS_URL_PARAM, ADS_URL.replace("${host}", testParams.getHost()).replace(
                "${adsId}", adsId)));
        String createTableSql = getResourceAsString("/" + SQL_FILES + "/" + createTableSqlFile);
        String copyTableSql = getResourceAsString("/" + SQL_FILES + "/" + copyTableSqlFile);
        params.add(new ExecutionParameter(CREATE_ADS_TABLE_PARAM, createTableSql));
        params.add(new ExecutionParameter(COPY_ADS_TABLE_PARAM, copyTableSql));
        return params;
    }

    protected String executeCloudConnectProcess(ProcessInfo processInfo, String executable,
            Collection<ExecutionParameter> params) {
        String executionUri =
                ProcessUtils.executeProcess(getRestApiClient(), processInfo, executable, params);

        return executionUri;
    }

    protected int createCloudConnectProcess(ProcessInfo processInfo) {
        String uploadFilePath =
                uploadZipFileToWebDavWithoutWebContainer(CLOUDCONNECT_PROCESS_PACKAGE);
        return ProcessUtils.createCloudConnectProcess(getRestApiClient(), processInfo,
                uploadFilePath.substring(uploadFilePath.indexOf("/uploads")) + "/"
                        + CLOUDCONNECT_PROCESS_PACKAGE);
    }

    protected void updateModelOfGDProject(String maql) {
        System.out.println("Update model of GD project!");
        String pollingUri = sendRequestToUpdateModel(maql);
        RestUtils.waitingForAsyncTask(getRestApiClient(), pollingUri);
        assertEquals(RestUtils.getAsyncTaskStatus(getRestApiClient(), pollingUri), "OK",
                "Model is not updated successfully!");
    }

    protected void dropAddedFieldsInLDM(String maql) {
        String pollingUri = sendRequestToUpdateModel(maql);
        RestUtils.waitingForAsyncTask(getRestApiClient(), pollingUri);
        if (!"OK".equals(RestUtils.getAsyncTaskStatus(getRestApiClient(), pollingUri))) {
            HttpRequestBase getRequest = getRestApiClient().newGetMethod(pollingUri);
            HttpResponse getResponse = getRestApiClient().execute(getRequest);
            String errorMessage = "";
            try {
                errorMessage =
                        new JSONObject(EntityUtils.toString(getResponse.getEntity()))
                                .getJSONObject("wTaskStatus").getJSONArray("messages")
                                .getJSONObject(0).getJSONObject("error").get("message").toString();
            } catch (Exception e) {
                throw new IllegalStateException("There is an exeption when getting error message!",
                        e);
            }

            EntityUtils.consumeQuietly(getResponse.getEntity());

            System.out.println("LDM update is failed with error message: " + errorMessage);
            assertEquals(errorMessage, "The object (%s) doesn't exist.");
        }
    }
    
    protected void createADS(Warehouse ads) {
        adsHelper.createAds(ads.getTitle(), ads.getAuthorizationToken());
    }

    protected void deleteADSInstance(Warehouse ads) {
        adsHelper.removeAds(ads);
    }

    protected String uploadZipFileToWebDavWithoutWebContainer(String zipFileName) {
        WebDavClient webDav =
                WebDavClient.getInstance(testParams.getUser(), testParams.getPassword());
        File resourceFile = getResourceAsFile("/" + ZIP_FILES + "/" + zipFileName);
        System.out.println("Resource file: " + resourceFile.getAbsolutePath());
        openUrl(PAGE_GDC);
        waitForElementPresent(gdcFragment.getRoot());
        assertTrue(webDav.createStructure(gdcFragment.getUserUploadsURL()),
                " Create WebDav storage structure");

        webDav.uploadFile(resourceFile);

        return webDav.getWebDavStructure();
    }

    protected int createDataLoadProcess() {
        return createDataLoadProcess(new ProcessInfo().withProcessName(
                DEFAULT_DATAlOAD_PROCESS_NAME).withProjectId(getWorkingProject().getProjectId()));
    }

    protected int createDataLoadProcess(ProcessInfo processInfo) {
        return ProcessUtils.createDataloadProcess(getRestApiClient(), processInfo);
    }

    protected String failedToCreateDataloadExecution(HttpStatus expectedStatusCode,
            Collection<ExecutionParameter> params) throws IOException, JSONException {
        return ProcessUtils.failedToCreateDataloadProcessExecution(getRestApiClient(),
                expectedStatusCode, getDataloadProcessInfo(), params);
    }

    protected String executeDataloadProcess(RestApiClient restApiClient, Collection<ExecutionParameter> params)
            throws IOException, JSONException {
        return ProcessUtils
                .executeProcess(restApiClient, getDataloadProcessInfo(), "", params);
    }

    protected String executeDataloadProcessSuccessfully(RestApiClient restApiClient)
            throws IOException, JSONException {
        String executionUri =
                executeDataloadProcess(restApiClient, Lists.newArrayList(new ExecutionParameter(
                        GDC_DE_SYNCHRONIZE_ALL, true)));

        assertTrue(ProcessUtils.isExecutionSuccessful(restApiClient, executionUri),
                "Process execution is not successful!");
        return executionUri;
    }

    protected void deleteDataloadProcessAndCreateNewOne() throws IOException, JSONException {
        if (!getDataloadProcessId().isEmpty()) {
            ProcessUtils.deleteDataloadProcess(getRestApiClient(), getDataloadProcessUri(),
                    testParams.getProjectId());
        }
        createDataLoadProcess();
        assertFalse(getDataloadProcessId().isEmpty());
    }

    protected DataSource prepareADSTable(ADSTables adsTable) {
        createUpdateADSTable(adsTable);
        DataSource dataSource =
                new DataSource().withName(adsTable.datasourceName).withDatasets(
                        adsTable.getDatasets());

        return dataSource;
    }

    protected Dataset prepareDataset(AdditionalDatasets additionalDataset) {
        return additionalDataset.getDataset();
    }

    protected void createUpdateADSTable(ADSTables adsTable) {
        createUpdateADSTableBySQLFiles(adsTable.createTableSqlFile, adsTable.copyTableSqlFile, ads);
    }

    protected void deleteOutputStageMetadata() {
        customOutputStageMetadata();
    }

    protected void customOutputStageMetadata(DataSource... dataSources) {
        String putBody = prepareOutputStageMetadata(dataSources);

        String putUri =
                String.format(OUTPUT_STAGE_METADATA_URI, getWorkingProject().getProjectId());

        HttpRequestBase putRequest = getRestApiClient().newPutMethod(putUri, putBody);
        putRequest.setHeader("Accept", ACCEPT_APPLICATION_JSON_WITH_VERSION);

        HttpResponse putResponse =
                getRestApiClient().execute(putRequest, HttpStatus.OK,
                        "Metadata is not updated successfully! Put body: " + putBody);

        EntityUtils.consumeQuietly(putResponse.getEntity());
    }

    protected String getDataloadProcessUri() throws IOException, JSONException {
        return format(DATALOAD_PROCESS_URI, testParams.getProjectId()) + getDataloadProcessId();
    }

    protected String getDataloadProcessId() throws IOException, JSONException {
        return ProcessUtils.getProcessesList(getRestApiClient(), testParams.getProjectId())
                .getDataloadProcess().getProcessId();
    }

    protected String getDiffResourceContent(RestApiClient restApiClient, HttpStatus status) {
        return RestUtils.getResourceWithCustomAcceptHeader(restApiClient, 
                format(OUTPUTSTAGE_URI, testParams.getProjectId()) + "diff",
                status, ACCEPT_TEXT_PLAIN_WITH_VERSION);
    }

    protected String getMappingResourceContent(RestApiClient restApiClient, HttpStatus status) {
        return RestUtils.getResourceWithCustomAcceptHeader(restApiClient,
                format(MAPPING_RESOURCE, testParams.getProjectId()), status, ACCEPT_TEXT_PLAIN_WITH_VERSION);
    }

    protected List<String> getReferencesOfDataset(String dataset) throws ParseException, JSONException,
            IOException {
        JSONArray array = RestUtils.getDatasetElementFromModelView(getRestApiClient(), testParams.getProjectId(),
                dataset, DatasetElements.REFERENCES, JSONArray.class);
        return new ObjectMapper().readValue(array.toString(), new TypeReference<List<String>>() {
        });
    }

    protected int redeployDataLoadProcess(RestApiClient restApiClient) throws IOException, JSONException {
        HttpRequestBase putRequest = restApiClient.newPutMethod(getDataloadProcessUri(), ProcessUtils
                .prepareProcessCreationBody(DATALOAD, DEFAULT_DATAlOAD_PROCESS_NAME).toString());
        int responseStatusCode;
        try {
            HttpResponse postResponse = restApiClient.execute(putRequest);
            responseStatusCode = postResponse.getStatusLine().getStatusCode();
            EntityUtils.consumeQuietly(postResponse.getEntity());
            System.out.println("Response status: " + responseStatusCode);
        } finally {
            putRequest.releaseConnection();
        }

        return responseStatusCode;
    }

    protected void prepareMetricToCheckNewAddedFields(String... facts) {
        for (String fact : facts) {
            initFactPage();
            factsTable.selectObject(fact);
            waitForFragmentVisible(factDetailPage).createSimpleMetric(SimpleMetricTypes.SUM, fact);
            initMetricPage();
            waitForFragmentVisible(metricsTable);
            metricsTable.selectObject(fact + " [Sum]");
            waitForFragmentVisible(metricDetailPage).setMetricVisibleToAllUser();
        }
    }

    protected void createAndCheckReport(UiReportDefinition reportDefinition, Collection<String> attributeValues,
            Collection<String> metricValues) {
        createReport(reportDefinition, reportDefinition.getName());

        List<String> attributes = reportPage.getTableReport().getAttributeElements();
        System.out.println("Attributes: " + attributes.toString());
        assertTrue(CollectionUtils.isEqualCollection(attributes, attributeValues),
                "Incorrect attribute values!");

        List<String> metrics = reportPage.getTableReport().getRawMetricElements();
        System.out.println("Metric: " + metrics.toString());
        assertTrue(CollectionUtils.isEqualCollection(metrics, metricValues),
                "Incorrect metric values!");
    }

    protected void checkReportAfterAddReferenceToDataset() {
        prepareMetricToCheckNewAddedFields("number");
        UiReportDefinition reportDefinition =
                new UiReportDefinition().withName("Report to check reference")
                        .withHows("artistname").withWhats("number [Sum]");
        createAndCheckReport(reportDefinition, Lists.newArrayList("OOP1", "OOP2",
                "OOP3", "OOP4", "OOP5", "OOP6", "OOP7", "OOP8"), Lists.newArrayList("1,000.00",
                "1,200.00", "1,400.00", "1,600.00", "1,800.00", "2,000.00", "700.00", "800.00"));
    }

    private ProcessInfo getDataloadProcessInfo() throws IOException, JSONException {
        return new ProcessInfo().withProjectId(getWorkingProject().getProjectId()).withProcessId(
                getDataloadProcessId());
    }

    private String prepareOutputStageMetadata(DataSource... dataSources) {
        JSONObject metaObject = new JSONObject();

        try {
            Collection<JSONObject> metadataObjects = Lists.newArrayList();
            for (DataSource dataSource : dataSources) {
                for (Dataset dataset : dataSource.getAvailableDatasets(FieldTypes.ALL)) {
                    metadataObjects.add(prepareMetadataObject(dataset.getName(),
                            dataSource.getName(), new JSONArray()));
                }
            }
            metaObject.put("outputStageMetadata",
                    new JSONObject().put("tableMeta", metadataObjects));
        } catch (JSONException e) {
            throw new IllegalStateException(
                    "There is JSONExcetion during prepareOutputStageMetadata!", e);
        }

        return metaObject.toString();
    }

    private JSONObject prepareMetadataObject(String tableName, String dataSourceName,
            JSONArray metaColumns) {
        JSONObject metadataObject = new JSONObject();
        try {
            metadataObject.put("tableMetadata",
                    new JSONObject().put("table", tableName).put("defaultSource", dataSourceName)
                            .put("columnMeta", metaColumns));
        } catch (JSONException e) {
            throw new IllegalStateException("JSONExeception", e);
        }

        return metadataObject;
    }

    private String sendRequestToUpdateModel(String maql) {
        String pollingUri = "";
        try {
            pollingUri = RestUtils.executeMAQL(getRestApiClient(), getWorkingProject().getProjectId(), maql);
        } catch (Exception e) {
            throw new IllegalStateException("There is an exeception during LDM update!", e);
        }

        return pollingUri;
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
