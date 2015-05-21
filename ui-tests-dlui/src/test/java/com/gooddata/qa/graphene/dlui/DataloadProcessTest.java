package com.gooddata.qa.graphene.dlui;

import static org.testng.Assert.*;

import java.io.IOException;
import java.util.Collection;

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
import com.gooddata.qa.graphene.entity.ExecutionParameter;
import com.gooddata.qa.graphene.enums.UserRoles;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.qa.utils.http.RestUtils;
import com.gooddata.qa.graphene.utils.ProcessUtils;
import com.google.common.collect.Lists;

public class DataloadProcessTest extends AbstractDLUITest {

    private static final String CONCURRENT_DATA_LOAD_MESSAGE = "The schedule did not run"
            + " because one or more of the datasets in this schedule is already synchronizing.";
    private static final String INTERNAL_OUTPUT_STAGE_URI =
            "/gdc/dataload/internal/projects/%s/outputStage/";
    private static final String MAPPING_RESOURCE = INTERNAL_OUTPUT_STAGE_URI + "mapping";
    private static final String OUTPUT_STATE_MODEL_RESOURCE = INTERNAL_OUTPUT_STAGE_URI + "model";
    private static final String OUTPUT_STATE_METADATA_RESOURCE = OUTPUTSTAGE_URI + "metadata";


    @BeforeClass
    public void initProjectTitle() {
        projectTitle = "Dataload process test";
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"},
            groups = {"dataloadProcessTest"}, priority = 0)
    public void autoCreateDataloadProcess() throws IOException, JSONException, InterruptedException {
        createUpdateADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);
        assertTrue(isDataloadProcessCreated(), "DATALOAD process is not created!");
        Processes dataloadProcessList =
                ProcessUtils.getProcessesList(getRestApiClient(), testParams.getProjectId());
        assertEquals(dataloadProcessList.getDataloadProcess().getName(),
                DEFAULT_DATAlOAD_PROCESS_NAME);
        assertEquals(dataloadProcessList.getDataloadProcessCount(), 1);
        assertEquals(createDataLoadProcess(), HttpStatus.CONFLICT.value());

        executeDataloadProcessSuccessfully(getRestApiClient());
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"},
            groups = {"dataloadProcessTest"}, priority = 1)
    public void changeAdsInstanceWhenHavingDataloadProcess() throws IOException, JSONException,
            InterruptedException {
        createDataLoadProcess();
        String dataloadProcessId = getDataloadProcessId();
        ADSInstance newAdsInstance =
                new ADSInstance().withName("ADS Instance for DLUI test 2").withAuthorizationToken(
                        testParams.loadProperty("dss.authorizationToken"));
        createADSInstance(newAdsInstance);
        setDefaultSchemaForOutputStage(getRestApiClient(), newAdsInstance.getId());

        try {
            assertEquals(
                    ProcessUtils.getProcessesList(getRestApiClient(), testParams.getProjectId())
                            .getDataloadProcessCount(), 1);
            assertEquals(getDataloadProcessId(), dataloadProcessId);
        } finally {
            setDefaultSchemaForOutputStage(getRestApiClient(), adsInstance.getId());
            deleteADSInstance(newAdsInstance);
        }
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"},
            groups = {"dataloadProcessTest"}, priority = 1)
    public void changeAdsInstanceAfterDeleteDataloadProcess() throws IOException, JSONException {
        createDataLoadProcess();
        ProcessUtils.deleteDataloadProcess(getRestApiClient(), getDataloadProcessUri(),
                testParams.getProjectId());
        assertEquals(ProcessUtils.getProcessesList(getRestApiClient(), testParams.getProjectId())
                .getDataloadProcessCount(), 0);
        setDefaultSchemaForOutputStage(getRestApiClient(), adsInstance.getId());
        Processes dataloadProcessList =
                ProcessUtils.getProcessesList(getRestApiClient(), testParams.getProjectId());
        assertEquals(dataloadProcessList.getDataloadProcessCount(), 1);
        assertEquals(dataloadProcessList.getDataloadProcess().getName(),
                DEFAULT_DATAlOAD_PROCESS_NAME);
    }

    @Test(dependsOnMethods = {"addUsersToProjects"}, groups = {"dataloadProcessTest"}, priority = 2)
    public void checkProcessOwner() throws ParseException, JSONException, IOException {
        createUpdateADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);
        // Delete old dataload process and create new one to ensure that
        // execution result isn't influenced by others
        deleteDataloadProcessAndCreateNewOne();

        assertEquals(getOwnerLogin(), testParams.getUser(),
                "Process owner of dataload is incorrect!");
        String executionUri = executeDataloadProcessSuccessfully(getRestApiClient());
        assertTrue(getLogContent(getRestApiClient(), executionUri).contains(String.format("user: %s", testParams.getUser())));

        logout();
        signInAtGreyPages(technicalUser, technicalUserPassword);
        RestApiClient restApi = getRestApiClient(technicalUser, technicalUserPassword);
        assertEquals(redeployDataLoadProcess(restApi), HttpStatus.OK.value());
        assertEquals(getOwnerLogin(), technicalUser,
                "Process owner of dataload after redeployed is not changed!");
        String executionByTechnicalUser = executeDataloadProcessSuccessfully(restApi);
        assertTrue(getLogContent(restApi, executionByTechnicalUser).contains(
                String.format("user: %s", technicalUser)));

        logout();
        signIn(true, UserRoles.EDITOR);
        RestApiClient editorRestApi =
                getRestApiClient(testParams.getEditorUser(), testParams.getEditorPassword());
        String executionByEditor = executeDataloadProcessSuccessfully(editorRestApi);
        assertTrue(getLogContent(editorRestApi, executionByEditor).contains(
                String.format("user: %s", technicalUser)));

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
            RestApiClient editorRestApi =
                    getRestApiClient(testParams.getEditorUser(), testParams.getEditorPassword());
            RestUtils.getResourceWithCustomAcceptHeader(editorRestApi,
                    String.format(MAPPING_RESOURCE, testParams.getProjectId()), HttpStatus.OK,
                    ACCEPT_HEADER_VALUE_WITH_VERSION);
            RestUtils.getResourceWithCustomAcceptHeader(editorRestApi,
                    String.format(OUTPUT_STATE_MODEL_RESOURCE, testParams.getProjectId()),
                    HttpStatus.OK, ACCEPT_HEADER_VALUE_WITH_VERSION);
            RestUtils.getResourceWithCustomAcceptHeader(editorRestApi,
                    String.format(OUTPUTSTAGE_URI, testParams.getProjectId()), HttpStatus.OK,
                    ACCEPT_HEADER_VALUE_WITH_VERSION);
            RestUtils.getResourceWithCustomAcceptHeader(editorRestApi,
                    String.format(OUTPUT_STATE_METADATA_RESOURCE, testParams.getProjectId()),
                    HttpStatus.OK, ACCEPT_HEADER_VALUE_WITH_VERSION);
            RestUtils.getResource(editorRestApi, executeDataloadProcessSuccessfully(editorRestApi),
                    HttpStatus.NO_CONTENT);
        } finally {
            logout();
            signIn(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = {"dataloadProcessTest"}, priority = 3)
    public void checkConcurrentDataLoadViaRestAPI() throws ParseException, JSONException,
            IOException, InterruptedException {
        createUpdateADSTable(ADSTables.WITH_ADDITIONAL_FIELDS_LARGE_DATA);
        deleteDataloadProcessAndCreateNewOne();

        Collection<ExecutionParameter> dataloadExecutionParams =
                Lists.newArrayList(new ExecutionParameter(GDC_DE_SYNCHRONIZE_ALL, true));
        String executionUri = executeDataloadProcess(dataloadExecutionParams);
        waitForExecutionInRunningState(executionUri);
        // Check that the second execution will be failed because the first execution is running
        String errorMessage =
                failedToCreateDataloadExecution(HttpStatus.CONFLICT, dataloadExecutionParams);
        assertEquals(errorMessage, CONCURRENT_DATA_LOAD_MESSAGE);
        assertTrue(ProcessUtils.isExecutionSuccessful(getRestApiClient(), executionUri));
    }

    @Test(dependsOnGroups = "dataloadProcessTest", alwaysRun = true)
    public void cleanUp() {
        deleteADSInstance(adsInstance);
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"}, groups = {"dataloadProcessTest"}, priority = 2)
    private void addUsersToProjects() throws ParseException, IOException, JSONException {
        RestUtils.addUserToProject(testParams.getHost(), testParams.getProjectId(),
                testParams.getUser(), testParams.getPassword(), technicalUserUri, UserRoles.ADMIN);
        RestUtils.addUserToProject(testParams.getHost(), testParams.getProjectId(),
                testParams.getUser(), testParams.getPassword(), testParams.getEditorProfileUri(),
                UserRoles.EDITOR);
        addUserToAdsInstance(adsInstance, technicalUserUri, technicalUser, "dataAdmin");
        addUserToAdsInstance(adsInstance, testParams.getEditorProfileUri(),
                testParams.getEditorUser(), "dataAdmin");
    }

    private void waitForExecutionInRunningState(String executionUri) throws ParseException, IOException,
            JSONException {
        String state = "";
        do {
            state = ProcessUtils.getExecutionStatus(getRestApiClient(), executionUri);
            if ("RUNNING".equals(state))
                break;
        } while (!"ERROR".equals(state) && !"OK".equals(state));
    }

    private String getOwnerLogin() throws IOException, JSONException {
        return ProcessUtils.getProcessesList(getRestApiClient(), testParams.getProjectId())
                .getDataloadProcess().getOwnerLogin();
    }

    private int redeployDataLoadProcess(RestApiClient restApiClient) throws IOException,
            JSONException {
        HttpRequestBase putRequest =
                restApiClient.newPutMethod(getDataloadProcessUri(), ProcessUtils
                        .prepareProcessCreationBody("DATALOAD", DEFAULT_DATAlOAD_PROCESS_NAME)
                        .toString());
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

    private String executeDataloadProcessSuccessfully(RestApiClient restApiClient)
            throws IOException, JSONException {
        String executionUri =
                executeDataloadProcess(Lists.newArrayList(new ExecutionParameter(
                        GDC_DE_SYNCHRONIZE_ALL, true)));

        assertTrue(ProcessUtils.isExecutionSuccessful(restApiClient, executionUri),
                "Process execution is not successful!");

        return executionUri;
    }

    private void deleteDataloadProcessAndCreateNewOne() throws IOException, JSONException {
        if (!getDataloadProcessId().isEmpty()) {
            ProcessUtils.deleteDataloadProcess(getRestApiClient(), getDataloadProcessUri(),
                    testParams.getProjectId());
        }
        createDataLoadProcess();
        assertFalse(getDataloadProcessId().isEmpty());
    }

    private String getLogContent(RestApiClient restApiClient, String executionUri) {
        return RestUtils.getResource(getRestApiClient(), executionUri + "/log", HttpStatus.OK);
    }
}
