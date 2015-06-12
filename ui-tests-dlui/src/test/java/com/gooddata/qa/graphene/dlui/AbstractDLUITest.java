package com.gooddata.qa.graphene.dlui;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static java.lang.String.format;
import static org.testng.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractMSFTest;
import com.gooddata.qa.graphene.entity.ADSInstance;
import com.gooddata.qa.graphene.entity.DataSource;
import com.gooddata.qa.graphene.entity.Dataset;
import com.gooddata.qa.graphene.entity.ExecutionParameter;
import com.gooddata.qa.graphene.entity.Field;
import com.gooddata.qa.graphene.entity.ProcessInfo;
import com.gooddata.qa.graphene.entity.Field.FieldTypes;
import com.gooddata.qa.graphene.enums.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.AnnieUIDialogFragment;
import com.gooddata.qa.graphene.utils.ProcessUtils;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.qa.utils.http.RestUtils;
import com.google.common.collect.Lists;

public abstract class AbstractDLUITest extends AbstractMSFTest {

    protected static final String CLOUDCONNECT_PROCESS_PACKAGE = "dlui.zip";
    protected static final String DLUI_GRAPH_CREATE_AND_COPY_DATA_TO_ADS =
            "DLUI/graph/CreateAndCopyDataToADS.grf";

    protected static final String DATALOAD_PROCESS_URI = "/gdc/projects/%s/dataload/processes/";
    private static final String OUTPUT_STAGE_METADATA_URI =
            "/gdc/dataload/projects/%s/outputStage/metadata";
    protected static final String ACCEPT_APPLICATION_JSON_WITH_VERSION = "application/json; version=1";
    protected static final String ACCEPT_TEXT_PLAIN_WITH_VERSION = "text/plain; version=1";
    protected static final String OUTPUTSTAGE_URI = "/gdc/dataload/projects/%s/outputStage/";

    protected static final String DEFAULT_DATAlOAD_PROCESS_NAME = "ADS to LDM synchronization";

    protected static final String ADS_USER_PARAM = "ADS_USER";
    protected static final String ADS_PASSWORD_PARAM = "ADS_PASSWORD";
    protected static final String ADS_URL_PARAM = "ADS_URL";
    protected static final String CREATE_ADS_TABLE_PARAM = "CREATE_TABLE";
    protected static final String COPY_ADS_TABLE_PARAM = "COPY_TABLE";
    protected static final String EXECUTABLE = "executable";
    protected static final String GDC_DE_SYNCHRONIZE_ALL = "GDC_DE_SYNCHRONIZE_ALL";

    @FindBy(css = ".s-btn-add_data")
    private WebElement addDataButton;

    @FindBy(css = ".annie-dialog-main")
    protected AnnieUIDialogFragment annieUIDialog;

    protected String technicalUser;
    protected String technicalUserPassword;
    protected String technicalUserUri;
    protected String dssAuthorizationToken;
    protected String initialLdmMaqlFile = "create-ldm.txt";

    protected static final String ADS_URL =
            "jdbc:gdc:datawarehouse://${host}/gdc/datawarehouse/instances/${adsId}";

    protected ProcessInfo cloudconnectProcess;
    protected ProcessInfo dataloadProcess;
    protected ADSInstance adsInstance;

    @BeforeClass
    public void initialProperties() {
        maqlFilePath = testParams.loadProperty("maqlFilePath") + testParams.getFolderSeparator();
        sqlFilePath = testParams.loadProperty("sqlFilePath") + testParams.getFolderSeparator();
        zipFilePath = testParams.loadProperty("zipFilePath") + testParams.getFolderSeparator();
        technicalUser = testParams.loadProperty("technicalUser");
        technicalUserPassword = testParams.loadProperty("technicalUserPassword");
        technicalUserUri = testParams.loadProperty("technicalUserUri");
        dssAuthorizationToken = testParams.loadProperty("dss.authorizationToken");
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"initialDataForDLUI", "setup"})
    public void prepareLDMAndADSInstance() throws JSONException, ParseException, IOException,
            InterruptedException {
        // Override this method in test class if it's necessary to add more users to project
        addUsersToProject();

        RestUtils.enableFeatureFlagInProject(getRestApiClient(), testParams.getProjectId(),
                ProjectFeatureFlags.ENABLE_DATA_EXPLORER);
        updateModelOfGDProject(maqlFilePath + initialLdmMaqlFile);

        adsInstance = new ADSInstance().withName("ADS Instance for DLUI test")
                .withAuthorizationToken(dssAuthorizationToken);
        createADSInstance(adsInstance);
        // Override this method in test class if it's necessary to add more users to ads instance
        addUsersToAdsInstance();
    }

    @Test(dependsOnMethods = {"prepareLDMAndADSInstance"}, groups = {"initialDataForDLUI", "setup"},
            alwaysRun = true, priority = 1)
    public void setUpOutputStageAndCreateCloudConnectProcess() {
        // Override this method in test class if using other user to set default schema for output stage
        setDefaultSchemaForOutputStage();
        assertTrue(isDataloadProcessCreated(), "DATALOAD process is not created!");

        cloudconnectProcess = new ProcessInfo().withProjectId(testParams.getProjectId())
                        .withProcessName("Initial Data for ADS Instance").withProcessType("GRAPH");
        assertEquals(createCloudConnectProcess(cloudconnectProcess), HttpStatus.CREATED.value());
    }

    protected void addUsersToProject() {}

    protected void addUsersToAdsInstance() {}

    protected void setDefaultSchemaForOutputStage() {
        setDefaultSchemaForOutputStage(getRestApiClient(), adsInstance.getId());
    }

    protected String getAdsUrl(ADSInstance adsInstance) {
        return ADS_URL.replace("${host}", testParams.getHost()).replace("${adsId}",
                adsInstance.getId());
    }

    protected int createCloudConnectProcess(ProcessInfo processInfo) {
        String uploadFilePath =
                uploadZipFileToWebDavWithoutWebContainer(zipFilePath + CLOUDCONNECT_PROCESS_PACKAGE);
        return ProcessUtils.createCloudConnectProcess(getRestApiClient(), processInfo,
                uploadFilePath.substring(uploadFilePath.indexOf("/uploads")) + "/"
                        + CLOUDCONNECT_PROCESS_PACKAGE);
    }

    protected int createDataLoadProcess() {
        return createDataLoadProcess(new ProcessInfo().withProcessName(
                DEFAULT_DATAlOAD_PROCESS_NAME).withProjectId(getWorkingProject().getProjectId()));
    }

    protected int createDataLoadProcess(ProcessInfo processInfo) {
        return ProcessUtils.createDataloadProcess(getRestApiClient(), processInfo);
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

    protected String failedToCreateDataloadExecution(HttpStatus expectedStatusCode,
            Collection<ExecutionParameter> params) throws IOException, JSONException {
        return ProcessUtils.failedToCreateDataloadProcessExecution(getRestApiClient(),
                expectedStatusCode, getDataloadProcessInfo(), params);
    }

    protected String executeDataloadProcess(Collection<ExecutionParameter> params)
            throws IOException, JSONException {
        return ProcessUtils
                .executeProcess(getRestApiClient(), getDataloadProcessInfo(), "", params);
    }

    protected String executeCloudConnectProcess(ProcessInfo processInfo, String executable,
            List<ExecutionParameter> params) {
        String executionUri =
                ProcessUtils.executeProcess(getRestApiClient(), processInfo, executable, params);

        return executionUri;
    }

    protected void openAnnieDialog() {
        initManagePage();
        waitForElementVisible(addDataButton).click();
        browser.switchTo().frame(
                waitForElementVisible(By.xpath("//iframe[contains(@src,'dlui-annie')]"), browser));
        waitForElementVisible(annieUIDialog.getRoot());
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
        createUpdateADSTableBySQLFiles(adsTable.createTableSqlFile, adsTable.copyTableSqlFile, adsInstance);
    }

    protected void createUpdateADSTableBySQLFiles(String createTableFile, String copyTableFile, 
            ADSInstance instance){
        List<ExecutionParameter> params =
                prepareParamsToUpdateADS(createTableFile, copyTableFile, instance);

        String executionUri =
                executeCloudConnectProcess(cloudconnectProcess,
                        DLUI_GRAPH_CREATE_AND_COPY_DATA_TO_ADS, params);
        assertTrue(ProcessUtils.isExecutionSuccessful(getRestApiClient(), executionUri));
    }

    protected List<ExecutionParameter> prepareParamsToUpdateADS(String createTableSqlFile,
            String copyTableSqlFile, ADSInstance instance) {
        List<ExecutionParameter> params = Lists.newArrayList();

        params.add(new ExecutionParameter(ADS_USER_PARAM, testParams.getUser()));
        params.add(new ExecutionParameter(ADS_PASSWORD_PARAM, testParams.getPassword()));
        params.add(new ExecutionParameter(ADS_URL_PARAM, ADS_URL.replace("${host}",
                testParams.getHost()).replace("${adsId}", instance.getId())));
        try {
            String createTableSql =
                    FileUtils.readFileToString(new File(sqlFilePath + createTableSqlFile),
                            StandardCharsets.UTF_8);
            String copyTableSql =
                    FileUtils.readFileToString(new File(sqlFilePath + copyTableSqlFile),
                            StandardCharsets.UTF_8);
            params.add(new ExecutionParameter(CREATE_ADS_TABLE_PARAM, createTableSql));
            params.add(new ExecutionParameter(COPY_ADS_TABLE_PARAM, copyTableSql));
        } catch (IOException e) {
            throw new IllegalStateException(
                    "There is an exception during reading file to string! ", e);
        }
        return params;
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
                AdditionalDatasets.AUTHOR_WITH_NEW_FIELD);

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
