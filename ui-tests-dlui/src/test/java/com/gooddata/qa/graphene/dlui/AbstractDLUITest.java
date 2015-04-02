package com.gooddata.qa.graphene.dlui;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static java.lang.String.format;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.jboss.arquillian.graphene.Graphene.createPageFragment;
import static org.testng.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
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

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.ADSInstance;
import com.gooddata.qa.graphene.entity.DataSource;
import com.gooddata.qa.graphene.entity.Dataset;
import com.gooddata.qa.graphene.entity.Field;
import com.gooddata.qa.graphene.entity.ProcessInfo;
import com.gooddata.qa.graphene.entity.Field.FieldTypes;
import com.gooddata.qa.graphene.entity.disc.ProjectInfo;
import com.gooddata.qa.graphene.enums.DLUIProcessParameters;
import com.gooddata.qa.graphene.enums.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.AnnieUIDialogFragment;
import com.gooddata.qa.graphene.fragments.greypages.datawarehouse.InstanceFragment;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.qa.utils.http.RestUtils;
import com.gooddata.qa.utils.webdav.WebDavClient;
import com.google.common.collect.Lists;

public abstract class AbstractDLUITest extends AbstractProjectTest {

    private static final String CLOUDCONNECT_PROCESS_PACKAGE = "dlui.zip";
    private static final String DLUI_GRAPH_CREATE_AND_COPY_DATA_TO_ADS =
            "DLUI/graph/CreateAndCopyDataToADS.grf";

    protected static final String DATALOAD_PROCESS_URI = "/gdc/projects/%s/dataload/processes/";
    private static final String PROCESS_EXECUTION_URI = DATALOAD_PROCESS_URI + "%s/executions";
    private static final String ADS_INSTANCES_URI = "gdc/datawarehouse/instances/";
    private static final String ADS_INSTANCE_SCHEMA_URI = "/" + ADS_INSTANCES_URI
            + "%s/schemas/default";
    private static final String OUTPUTSTAGE_URI = "/gdc/dataload/projects/%s/outputStage/";
    private static final String OUTPUT_STAGE_METADATA_URI =
            "/gdc/dataload/projects/%s/outputStage/metadata";
    private static final String ACCEPT_HEADER_VALUE_WITH_VERSION = "application/json; version=1";

    protected static final String DEFAULT_DATAlOAD_PROCESS_NAME = "ADS to LDM synchronization";
    protected static final String FROM = "no-reply@gooddata.com";

    private JSONObject cloudConnectProcess = new JSONObject();
    private JSONObject processExecution = new JSONObject();

    @FindBy(css = ".s-btn-add_data")
    private WebElement addDataButton;

    @FindBy(css = ".annie-dialog-main")
    protected AnnieUIDialogFragment annieUIDialog;

    private ProjectInfo workingProject;

    protected String maqlFilePath;
    protected String sqlFilePath;
    protected String zipFilePath;
    protected String INITIAL_LDM_MAQL_FILE = "create-ldm.txt";
    protected String technicalUser;
    protected String technicalUserPassword;
    protected String technicalUserUri;

    protected static final String ADS_URL =
            "jdbc:gdc:datawarehouse://${host}/gdc/datawarehouse/instances/${adsId}";

    protected ProcessInfo cloudconnectProcess;
    protected ADSInstance adsInstance;

    @BeforeClass
    public void initialProperties() {
        maqlFilePath = testParams.loadProperty("maqlFilePath") + testParams.getFolderSeparator();
        sqlFilePath = testParams.loadProperty("sqlFilePath") + testParams.getFolderSeparator();
        zipFilePath = testParams.loadProperty("zipFilePath") + testParams.getFolderSeparator();
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"initialDataForDLUI"})
    public void prepareLDMAndADSInstance() throws JSONException {
        RestUtils.enableFeatureFlagInProject(getRestApiClient(), testParams.getProjectId(),
                ProjectFeatureFlags.ENABLE_DATA_EXPLORER);
        updateModelOfGDProject(maqlFilePath + INITIAL_LDM_MAQL_FILE);

        adsInstance =
                new ADSInstance().withName("ADS Instance for DLUI test").withAuthorizationToken(
                        testParams.loadProperty("dss.authorizationToken"));
        createADSInstance(adsInstance);

        setDefaultSchemaForOutputStage(getRestApiClient(), adsInstance.getId());
        assertTrue(dataloadProcessIsCreated(), "DATALOAD process is not created!");

        cloudconnectProcess =
                new ProcessInfo().withProjectId(testParams.getProjectId())
                        .withProcessName("Initial Data for ADS Instance").withProcessType("GRAPH");
        createCloudConnectProcess(cloudconnectProcess);
    }

    protected ProjectInfo getWorkingProject() {
        if (workingProject == null)
            workingProject = new ProjectInfo(projectTitle, testParams.getProjectId());
        return workingProject;
    }

    protected int createDataLoadProcess() {
        String processUri = String.format(DATALOAD_PROCESS_URI, getWorkingProject().getProjectId());
        LinkedHashMap<String, String> objMap = new LinkedHashMap<String, String>();
        objMap.put("type", "DATALOAD");
        objMap.put("name", DEFAULT_DATAlOAD_PROCESS_NAME);
        JSONObject dataloadProcessObj = new JSONObject();
        try {
            dataloadProcessObj.put("process", objMap);
        } catch (JSONException e) {
            throw new IllegalStateException(
                    "There is a problem with JSON object when creating dataload process! ", e);
        }
        HttpRequestBase postRequest =
                getRestApiClient().newPostMethod(processUri, dataloadProcessObj.toString());
        HttpResponse postResponse = getRestApiClient().execute(postRequest);

        int responseStatusCode = postResponse.getStatusLine().getStatusCode();

        EntityUtils.consumeQuietly(postResponse.getEntity());
        System.out.println("Response status: " + responseStatusCode);

        return responseStatusCode;
    }

    protected boolean dataloadProcessIsCreated() {
        return createDataLoadProcess() == HttpStatus.CONFLICT.value();
    }

    protected void updateModelOfGDProject(String maqlFile) {
        assertEquals(
                RestUtils.getPollingState(getRestApiClient(), sendRequestToUpdateModel(maqlFile)),
                "OK", "Model is not updated successfully!");
    }

    protected void dropAddedFieldsInLDM(String maqlFile) {
        String pollingUri = sendRequestToUpdateModel(maqlFile);
        String pollingState = RestUtils.getPollingState(getRestApiClient(), pollingUri);
        if (!"OK".equals(pollingState)) {
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

    protected void createADSInstance(ADSInstance adsInstance) {
        openUrl(ADS_INSTANCES_URI);
        waitForElementVisible(storageForm.getRoot());
        assertTrue(storageForm.verifyValidCreateStorageForm(), "Create form is invalid");
        String adsUrl;
        try {
            adsUrl =
                    storageForm.createStorage(adsInstance.getName(), adsInstance.getDescription(),
                            adsInstance.getAuthorizationToken());
        } catch (Exception e) {
            throw new IllegalStateException(
                    "There is an exception during creating new ads instance! ", e);
        }

        adsInstance.withId(adsUrl.substring(adsUrl.lastIndexOf("/") + 1));
        System.out.println("adsId: " + adsInstance.getId());
    }

    protected void addUserToAdsInstance(ADSInstance adsInstance, String userUri, String user,
            String userRole) {
        openUrl(ADS_INSTANCES_URI + adsInstance.getId() + "/users");
        storageUsersForm.verifyValidAddUserForm();
        try {
            storageUsersForm.fillAddUserToStorageForm(userRole, null, user, true);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "There is an exeception when adding user to ads instance!", e);
        }
        takeScreenshot(browser, "datawarehouse-add-user-filled-form", this.getClass());
        assertTrue(browser.getCurrentUrl().contains(userUri.replace("/gdc/account/profile/", "")),
                "The user is not added to ads instance successfully!");
    }

    protected void deleteADSInstance(ADSInstance adsInstance) {
        openUrl(ADS_INSTANCES_URI + adsInstance.getId());
        InstanceFragment storage =
                createPageFragment(InstanceFragment.class,
                        waitForElementVisible(BY_GP_FORM_SECOND, browser));
        assertTrue(storage.verifyValidDeleteStorageForm(), "Delete form is invalid");
        storage.deleteStorageSuccess();
    }

    protected int createCloudConnectProcess(ProcessInfo processInfo) {
        String uploadFilePath =
                uploadZipFileToWebDav(zipFilePath + CLOUDCONNECT_PROCESS_PACKAGE, null);
        String processesUri =
                String.format(DATALOAD_PROCESS_URI, getWorkingProject().getProjectId());
        prepareCCProcessCreationBody(processInfo, uploadFilePath);
        String postBody = cloudConnectProcess.toString();
        System.out.println("postBody: " + postBody);

        HttpRequestBase postRequest = getRestApiClient().newPostMethod(processesUri, postBody);
        HttpResponse postResponse = getRestApiClient().execute(postRequest);
        int responseStatusCode = postResponse.getStatusLine().getStatusCode();

        System.out.println(postResponse.getFirstHeader("Location"));
        String processUri = postResponse.getFirstHeader("Location").getValue();
        processInfo.withProcessId(processUri.substring(processUri.lastIndexOf("/") + 1));
        System.out.println("Process id: " + processInfo.getProcessId());

        EntityUtils.consumeQuietly(postResponse.getEntity());
        System.out.println("Response status: " + responseStatusCode);

        return responseStatusCode;
    }

    protected void executeProcess(String processId, String adsUrl, String createTableSqlFile,
            String copyTableSqlFile) {
        String processExecutionUri =
                String.format(PROCESS_EXECUTION_URI, testParams.getProjectId(), processId);
        try {
            String createTableSql =
                    FileUtils
                            .readFileToString(new File(createTableSqlFile), StandardCharsets.UTF_8);
            String copyTableSql =
                    FileUtils.readFileToString(new File(copyTableSqlFile), StandardCharsets.UTF_8);
            prepareProcessExecutionBody(adsUrl, createTableSql, copyTableSql);
            String postBody = processExecution.toString();

            String pollingUri = executeProcessRequest(processExecutionUri, postBody);

            pollingExecutionStatus(pollingUri);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "There is an exception during reading file to string! ", e);
        }
    }

    protected void setDefaultSchemaForOutputStage(RestApiClient restApiClient, String adsId) {
        String schemaUri = String.format(ADS_INSTANCE_SCHEMA_URI, adsId);
        JSONObject outputStageObj = new JSONObject();
        try {
            outputStageObj.put("outputStage", new JSONObject().put("schema", schemaUri));
        } catch (JSONException e) {
            throw new IllegalStateException(
                    "There is a problem with JSON object when set default schema for outputStage! ",
                    e);
        }

        String putUri = String.format(OUTPUTSTAGE_URI, getWorkingProject().getProjectId());
        String putBody = outputStageObj.toString();
        HttpRequestBase putRequest = restApiClient.newPutMethod(putUri, putBody);
        putRequest.setHeader("Accept", ACCEPT_HEADER_VALUE_WITH_VERSION);

        HttpResponse putResponse = restApiClient.execute(putRequest);
        int responseStatusCode = putResponse.getStatusLine().getStatusCode();

        System.out.println(putResponse.toString());
        EntityUtils.consumeQuietly(putResponse.getEntity());
        System.out.println("Response status: " + responseStatusCode);
        assertEquals(responseStatusCode, HttpStatus.OK.value(),
                "Default schema is not set successfully!");
    }

    protected String uploadZipFileToWebDav(String zipFile, String webContainer) {
        WebDavClient webDav =
                WebDavClient.getInstance(testParams.getUser(), testParams.getPassword());
        File resourceFile = new File(zipFile);
        if (webContainer == null) {
            openUrl(PAGE_GDC);
            waitForElementPresent(gdcFragment.getRoot());
            assertTrue(webDav.createStructure(gdcFragment.getUserUploadsURL()),
                    " Create WebDav storage structure");
        } else
            webDav.setWebDavStructure(webContainer);

        webDav.uploadFile(resourceFile);

        return webDav.getWebDavStructure();
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
        executeProcess(
                cloudconnectProcess.getProcessId(),
                ADS_URL.replace("${host}", testParams.getHost()).replace("${adsId}",
                        adsInstance.getId()), sqlFilePath + adsTable.createTableSqlFile,
                sqlFilePath + adsTable.copyTableSqlFile);
    }

    protected void deleteOutputStageMetadata() {
        customOutputStageMetadata();
    }

    protected void customOutputStageMetadata(DataSource... dataSources) {
        String putBody = prepareOutputStageMetadata(dataSources);

        String putUri =
                String.format(OUTPUT_STAGE_METADATA_URI, getWorkingProject().getProjectId());

        HttpRequestBase putRequest = getRestApiClient().newPutMethod(putUri, putBody);
        putRequest.setHeader("Accept", ACCEPT_HEADER_VALUE_WITH_VERSION);

        HttpResponse putResponse =
                getRestApiClient().execute(putRequest, HttpStatus.OK,
                        "Metadata is not updated successfully! Put body: " + putBody);

        EntityUtils.consumeQuietly(putResponse.getEntity());
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

    private String sendRequestToUpdateModel(String maqlFile) {
        String maql = "";
        String pollingUri = "";
        try {
            maql = FileUtils.readFileToString(new File(maqlFile));
            pollingUri =
                    RestUtils.updateLDM(getRestApiClient(), getWorkingProject().getProjectId(),
                            maql);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "There is an exeception during reading file to string!", e);
        }

        return pollingUri;
    }

    private void prepareProcessExecutionBody(String adsUrl, String createTableSql,
            String copyTableSql) {
        LinkedHashMap<String, Object> objMap = new LinkedHashMap<String, Object>();
        LinkedHashMap<String, String> paramMap = new LinkedHashMap<String, String>();
        paramMap.put(DLUIProcessParameters.ADSUSER.getJsonObjectKey(), testParams.getUser());
        paramMap.put(DLUIProcessParameters.ADSPASSWORD.getJsonObjectKey(), testParams.getPassword());
        paramMap.put(DLUIProcessParameters.ADSURL.getJsonObjectKey(), adsUrl);
        paramMap.put(DLUIProcessParameters.CREATE_TABLE_SQL.getJsonObjectKey(), createTableSql);
        paramMap.put(DLUIProcessParameters.COPY_TABLE_SQL.getJsonObjectKey(), copyTableSql);
        objMap.put(DLUIProcessParameters.EXECUTABLE.getJsonObjectKey(),
                DLUI_GRAPH_CREATE_AND_COPY_DATA_TO_ADS);
        objMap.put("params", paramMap);
        try {
            processExecution.put("execution", objMap);
        } catch (JSONException e) {
            throw new IllegalStateException(
                    "There is a problem with JSON object when executing an process! ", e);
        }
    }

    private String executeProcessRequest(String processExecutionUri, String postBody) {

        HttpRequestBase postRequest =
                getRestApiClient().newPostMethod(processExecutionUri, postBody);
        HttpResponse postResponse =
                getRestApiClient().execute(postRequest, HttpStatus.CREATED,
                        "Execution is not created!");
        String pollingUri = "";
        try {
            pollingUri =
                    new JSONObject(EntityUtils.toString(postResponse.getEntity()))
                            .getJSONObject("executionTask").getJSONObject("links")
                            .getString("detail");

            EntityUtils.consumeQuietly(postResponse.getEntity());
        } catch (Exception e) {
            throw new IllegalStateException("There is an exeception during running process! ", e);
        } finally {
            postRequest.releaseConnection();
        }

        return pollingUri;
    }

    private void pollingExecutionStatus(String pollingUri) {
        HttpRequestBase getRequest = getRestApiClient().newGetMethod(pollingUri);
        HttpResponse getResponse;
        String state = "";
        try {
            do {
                getResponse = getRestApiClient().execute(getRequest);
                state =
                        new JSONObject(EntityUtils.toString(getResponse.getEntity()))
                                .getJSONObject("executionDetail").get("status").toString();
                System.out.println("Current execution state is: " + state);
                Thread.sleep(5000);
            } while ("QUEUED".equals(state) || "RUNNING".equals(state));

            assertEquals(state, "OK", "Invalid execution status: " + state);

            EntityUtils.consumeQuietly(getResponse.getEntity());
        } catch (Exception e) {
            throw new IllegalStateException(
                    "There is an exception during polling execution status!", e);
        } finally {
            getRequest.releaseConnection();
        }
    }

    private void prepareCCProcessCreationBody(ProcessInfo processInfo, String uploadFilePath) {
        try {
            LinkedHashMap<String, String> objMap = new LinkedHashMap<String, String>();
            objMap.put("type", "GRAPH");
            objMap.put("name", processInfo.getProcessName());
            objMap.put("path", uploadFilePath.substring(uploadFilePath.indexOf("/uploads")) + "/"
                    + CLOUDCONNECT_PROCESS_PACKAGE);
            cloudConnectProcess.put("process", objMap);
        } catch (JSONException e) {
            throw new IllegalStateException(
                    "There is a problem when create JSON object for creating CloudConnect process! ",
                    e);
        }
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
                new Field("Date", FieldTypes.DATE));

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
        WITH_ERROR_MAPPING("createTableWithErrorMapping.txt", "copyTableWithErrorMapping.txt");

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

    protected String getDataloadProcessUri() throws IOException, JSONException {
        return format(DATALOAD_PROCESS_URI, testParams.getProjectId()) + getDataloadProcessId();
    }

    protected String getDataloadProcessId() throws IOException, JSONException {
        return RestUtils.getProcessesList(getRestApiClient(), testParams.getProjectId())
                .getDataloadProcess().getProcessId();
    }
}
