package com.gooddata.qa.graphene.dlui;

import static org.testng.Assert.*;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.dto.Processes;
import com.gooddata.qa.graphene.entity.ADSInstance;
import com.gooddata.qa.graphene.entity.disc.ExecutionTask;
import com.gooddata.qa.graphene.enums.UserRoles;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.qa.utils.http.RestUtils;

public class DataloadProcessTest extends AbstractDLUITest {

    private static final String CONCURRENT_DATA_LOAD_MESSAGE = "The schedule did not run"
            + " because one or more of the datasets in this schedule is already synchronizing.";
    private static final String INTERNAL_OUTPUT_STAGE_URI = "/gdc/dataload/internal/projects/%s/outputStage/";
    private static final String MAPPING_RESOURCE = INTERNAL_OUTPUT_STAGE_URI + "mapping";
    private static final String OUTPUT_STATE_MODEL_RESOURCE = INTERNAL_OUTPUT_STAGE_URI + "model";
    private static final String OUTPUT_STATE_METADATA_RESOURCE = OUTPUTSTAGE_URI + "metadata";


    @BeforeClass
    public void initProjectTitle() {
        projectTitle = "Dataload process test";
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = {"dataloadProcessTest"}, priority = 0)
    public void autoCreateDataloadProcess() throws IOException, JSONException {
        createUpdateADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);
        assertTrue(dataloadProcessIsCreated(), "DATALOAD process is not created!");
        Processes dataloadProcessList = RestUtils.getProcessesList(getRestApiClient(), testParams.getProjectId());
        assertEquals(dataloadProcessList.getDataloadProcess().getName(), DEFAULT_DATAlOAD_PROCESS_NAME);
        assertEquals(dataloadProcessList.getDataloadProcessCount(), 1);
        assertEquals(createDataLoadProcess(), HttpStatus.CONFLICT.value());
        ExecutionTask execution = RestUtils.createDataloadProcessExecution(getRestApiClient(),
                getDataloadProcessUri());
        assertEquals(execution.getStatusCode(), HttpStatus.CREATED.value());
        assertEquals(RestUtils.getLastExecutionPollingState(restApiClient, execution.getDetailLink()), "OK");
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = {"dataloadProcessTest"}, priority = 1)
    public void changeAdsInstanceWhenHavingDataloadProcess() throws IOException, JSONException {
        createDataLoadProcess();
        String dataloadProcessId = getDataloadProcessId();
        ADSInstance newAdsInstance = new ADSInstance().withName("ADS Instance for DLUI test 2")
                .withAuthorizationToken(testParams.loadProperty("dss.authorizationToken"));
        createADSInstance(newAdsInstance);
        setDefaultSchemaForOutputStage(getRestApiClient(), newAdsInstance.getId());

        try {
            assertEquals(RestUtils.getProcessesList(getRestApiClient(), testParams.getProjectId()).
                    getDataloadProcessCount(), 1);
            assertEquals(getDataloadProcessId(), dataloadProcessId);
        } finally {
            setDefaultSchemaForOutputStage(getRestApiClient(), adsInstance.getId());
            deleteADSInstance(newAdsInstance);
        }
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = {"dataloadProcessTest"}, priority = 1)
    public void changeAdsInstanceAfterDeleteDataloadProcess() throws IOException, JSONException{
        createDataLoadProcess();
        RestUtils.deleteDataloadProcess(getRestApiClient(), getDataloadProcessUri(), testParams.getProjectId());
        assertEquals(RestUtils.getProcessesList(getRestApiClient(), testParams.getProjectId())
                .getDataloadProcessCount(), 0);
        setDefaultSchemaForOutputStage(getRestApiClient(), adsInstance.getId());
        Processes dataloadProcessList = RestUtils.getProcessesList(getRestApiClient(), testParams.getProjectId());
        assertEquals(dataloadProcessList.getDataloadProcessCount(), 1);
        assertEquals(dataloadProcessList.getDataloadProcess().getName(), DEFAULT_DATAlOAD_PROCESS_NAME);
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = {"dataloadProcessTest"}, priority = 2)
    private void addUsersToProjects() throws ParseException, IOException, JSONException {
        RestUtils.addUserToProject(testParams.getHost(), testParams.getProjectId(), testParams.getUser(),
                testParams.getPassword(), technicalUserUri, UserRoles.ADMIN);
        RestUtils.addUserToProject(testParams.getHost(), testParams.getProjectId(), testParams.getUser(),
                testParams.getPassword(), testParams.getEditorProfileUri(), UserRoles.EDITOR);
        addUserToAdsInstance(adsInstance, technicalUserUri, technicalUser, "dataAdmin");
        addUserToAdsInstance(adsInstance, testParams.getEditorProfileUri(),
                testParams.getEditorUser(), "dataAdmin");
    }

    @Test(dependsOnMethods = {"addUsersToProjects"}, groups = {"dataloadProcessTest"}, priority = 2)
    public void checkProcessOwner() throws ParseException, JSONException, IOException {
        createUpdateADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);
        // Delete old dataload process and create new one to ensure that
        // execution result isn't influenced by others
        deleteDataloadProcessAndCreateNewOne();

        assertEquals(getOwnerLogin(), testParams.getUser(), "Process owner of dataload is incorrect!");
        ExecutionTask execution = executeDataloadProcess(getRestApiClient());
        assertTrue(getLogFileContent(getRestApiClient(), execution.getDetailLink())
                .contains(String.format("user: %s", testParams.getUser())));

        logout();
        signInAtGreyPages(technicalUser, technicalUserPassword);
        RestApiClient restApi = getRestApiClient(technicalUser, technicalUserPassword);
        assertEquals(redeployDataLoadProcess(restApi), HttpStatus.OK.value());
        assertEquals(getOwnerLogin(), technicalUser, 
                "Process owner of dataload after redeployed is not changed!");
        ExecutionTask technicalExecution = executeDataloadProcess(restApi);
        assertTrue(getLogFileContent(getRestApiClient(), technicalExecution.getDetailLink())
                .contains(String.format("user: %s", technicalUser)));

        logout();
        signIn(true, UserRoles.EDITOR);
        RestApiClient editorRestApi = getRestApiClient(testParams.getEditorUser(), testParams.getEditorPassword());
        ExecutionTask editorExecution = executeDataloadProcess(editorRestApi);
        assertTrue(getLogFileContent(editorRestApi, editorExecution.getDetailLink())
                .contains(String.format("user: %s", technicalUser)));

        assertEquals(getOwnerLogin(), technicalUser, 
                "Process owner of dataload is changed after executed process!");
    }

    @Test(dependsOnMethods = {"addUsersToProjects"}, groups = {"dataloadProcessTest"}, priority = 2)
    public void editorAccessToDataloadResources() throws ParseException, JSONException, IOException {
        createUpdateADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);
        deleteDataloadProcessAndCreateNewOne();
        logout();
        signIn(true, UserRoles.EDITOR);
        try {
            RestApiClient editorRestApi = getRestApiClient(testParams.getEditorUser(),
                    testParams.getEditorPassword());
            executeGetRequest(editorRestApi, String.format(MAPPING_RESOURCE, testParams.getProjectId()),
                    HttpStatus.OK);
            executeGetRequest(editorRestApi, String.format(OUTPUT_STATE_MODEL_RESOURCE, testParams.getProjectId()),
                    HttpStatus.OK);
            executeGetRequest(editorRestApi, String.format(OUTPUTSTAGE_URI, testParams.getProjectId()),
                    HttpStatus.OK);
            executeGetRequest(editorRestApi, 
                    String.format(OUTPUT_STATE_METADATA_RESOURCE, testParams.getProjectId()), HttpStatus.OK);
            ExecutionTask execution = executeDataloadProcess(editorRestApi);
            executeGetRequest(editorRestApi, execution.getPollLink(), HttpStatus.NO_CONTENT);
        } finally {
            logout();
            signIn(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = {"dataloadProcessTest"}, priority = 3)
    public void checkConcurrentDataLoadViaRestAPI() throws ParseException, JSONException, IOException {
        createUpdateADSTable(ADSTables.WITH_ADDITIONAL_FIELDS_LARGE_DATA);
        deleteDataloadProcessAndCreateNewOne();
        ExecutionTask execution1 = 
                RestUtils.createDataloadProcessExecution(getRestApiClient(), getDataloadProcessUri());
        String execution1DetailLink = execution1.getDetailLink();
        assertEquals(execution1.getStatusCode(), HttpStatus.CREATED.value());
        waitForAddingDataTask(execution1DetailLink);
        // Check that the second execution will be failed because the first execution is running
        ExecutionTask execution2 = 
                RestUtils.createDataloadProcessExecution(getRestApiClient(), getDataloadProcessUri()); 
        assertEquals(execution2.getStatusCode(), HttpStatus.CONFLICT.value());
        assertEquals(execution2.getError(), CONCURRENT_DATA_LOAD_MESSAGE);

        assertEquals(RestUtils.getLastExecutionPollingState(restApiClient, execution1DetailLink), "OK");
    }

    @Test(dependsOnGroups = "dataloadProcessTest", alwaysRun = true)
    public void cleanUp() {
        deleteADSInstance(adsInstance);
    }

    private void executeGetRequest(final RestApiClient restApiClient, final String uri,
            HttpStatus expectedStatusCode)
            throws IOException, JSONException {
        HttpRequestBase getRequest = restApiClient.newGetMethod(uri);
        getRequest.setHeader("Accept", ACCEPT_HEADER_VALUE_WITH_VERSION);
        HttpResponse getResponse;
        try {
            getResponse = restApiClient.execute(getRequest, expectedStatusCode, "Invalid status code");
            EntityUtils.consumeQuietly(getResponse.getEntity());
        } finally {
            getRequest.releaseConnection();
        }
    }

    private void waitForAddingDataTask(String executionDetailLink) 
            throws ParseException, IOException, JSONException {
        String state = "";
        do {
            state = RestUtils.getCurrentExecutionPollingState(restApiClient, executionDetailLink);
            if ("RUNNING".equals(state))
                break;
        } while (!"ERROR".equals(state) && !"OK".equals(state));
    }

    private String getOwnerLogin() throws IOException, JSONException {
        return RestUtils.getProcessesList(getRestApiClient(), testParams.getProjectId())
                .getDataloadProcess().getOwnerLogin();
    }

    private int redeployDataLoadProcess(RestApiClient restApiClient) throws IOException, JSONException {
        HttpRequestBase postRequest = restApiClient.newPutMethod(getDataloadProcessUri(), 
                createJSONObjectForDataloadProcess().toString());
        int responseStatusCode;
        try {
            HttpResponse postResponse = restApiClient.execute(postRequest);
            responseStatusCode = postResponse.getStatusLine().getStatusCode();
            EntityUtils.consumeQuietly(postResponse.getEntity());
            System.out.println("Response status: " + responseStatusCode);
        } finally {
            postRequest.releaseConnection();
        }

        return responseStatusCode;
    }

    private ExecutionTask executeDataloadProcess(RestApiClient restApiClient)
            throws ParseException, JSONException, IOException {
        ExecutionTask execution = RestUtils.createDataloadProcessExecution(restApiClient,
                getDataloadProcessUri());

        assertEquals(execution.getStatusCode(), HttpStatus.CREATED.value(), "Cannot execute dataload process");
        assertEquals(RestUtils.getLastExecutionPollingState(restApiClient, execution.getDetailLink()), "OK",
                "Dataload process execution is failed!");
        return execution;
    }

    private String getLogFileContent(RestApiClient restApiClient, final String executionUri)
            throws IOException, JSONException {
        String logFileUri = RestUtils.getJSONObjectFrom(restApiClient, executionUri)
                .getJSONObject("executionDetail").getJSONObject("links").getString("log");
        HttpRequestBase getRequest = restApiClient.newGetMethod(logFileUri);
        HttpResponse getResponse;
        String logContent = "";
        try {
            getResponse = restApiClient.execute(getRequest, HttpStatus.OK, "Invalid status code");
            logContent = EntityUtils.toString(getResponse.getEntity());
            EntityUtils.consumeQuietly(getResponse.getEntity());
        } finally {
            getRequest.releaseConnection();
        }
        return logContent;
    }

    private void deleteDataloadProcessAndCreateNewOne() throws IOException, JSONException { 
        if (!getDataloadProcessId().isEmpty()) {
            RestUtils.deleteDataloadProcess(getRestApiClient(), getDataloadProcessUri(),
                    testParams.getProjectId());
        }
        createDataLoadProcess();
        assertFalse(getDataloadProcessId().isEmpty());
    }
}
