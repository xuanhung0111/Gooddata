package com.gooddata.qa.graphene.dlui;

import static com.gooddata.qa.graphene.enums.ResourceDirectory.API_RESOURCES;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsFile;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.GoodDataException;
import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.ProcessExecutionDetail;
import com.gooddata.qa.graphene.AbstractMSFTest;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.utils.ads.AdsHelper.AdsRole;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.qa.utils.http.RestUtils;
import com.gooddata.qa.utils.http.process.ProcessRestUtils;
import com.gooddata.qa.utils.http.rolap.RolapRestUtils;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils;
import com.gooddata.warehouse.Warehouse;

public class DataloadProcessTest extends AbstractMSFTest {

    private static final String CONCURRENT_DATA_LOAD_MESSAGE = "The schedule did not run"
            + " because one or more of the datasets in this schedule is already synchronizing.";
    private static final String RUNNING_STATE = "RUNNING";
    private static final String OK_STATE = "OK";
    private static final String ERROR_STATE = "ERROR";

    @BeforeClass
    public void initProjectTitle() {
        projectTitle = "Dataload process test";
    }

    @Test(dependsOnMethods = {"createProject"}, groups = { "initialData" })
    public void initialData() throws JSONException, IOException {
        prepareLDMAndADSInstance();
        setUpOutputStageAndCreateCloudConnectProcess();
        enableDataExplorer();
    }

    @Test(dependsOnGroups = {"initialData"}, priority = 0)
    public void autoCreateDataloadProcess() throws IOException, JSONException {
        createUpdateADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);
        assertTrue(getDataloadProcess().isPresent(), "DATALOAD process is not created!");
        assertTrue(getProcessService().listProcesses(getProject())
                .stream()
                .anyMatch(process -> DEFAULT_DATAlOAD_PROCESS_NAME.equals(process.getName())));

        try {
            getProcessService().createProcess(getProject(),
                new DataloadProcess(DEFAULT_DATAlOAD_PROCESS_NAME, DATALOAD));
            fail("Still create another dataload process.");
        } catch (GoodDataException e) {}

        assertTrue(executeProcess(createProcess(DEFAULT_DATAlOAD_PROCESS_NAME, "DATALOAD"), "",SYNCHRONIZE_ALL_PARAM).isSuccess());
    }

    @Test(dependsOnGroups = {"initialData"}, priority = 1)
    public void changeAdsInstanceWhenHavingDataloadProcess() throws IOException, JSONException {
        final String dataloadProcessId = createProcess(DEFAULT_DATAlOAD_PROCESS_NAME, DATALOAD).getId();
        final Warehouse newAds = createAds("ADS Instance for DLUI test 2");
        setDefaultSchemaForOutputStage(newAds);

        try {
            assertTrue(getProcessService().listProcesses(getProject())
                    .stream()
                    .anyMatch(process -> dataloadProcessId.equals(process.getId())));
        } finally {
            setDefaultSchemaForOutputStage(ads);
            getAdsHelper().removeAds(newAds);
        }
    }

    @Test(dependsOnGroups = {"initialData"}, priority = 1)
    public void changeAdsInstanceAfterDeleteDataloadProcess() throws IOException, JSONException {
        final DataloadProcess dataloadProcess = createProcess(DEFAULT_DATAlOAD_PROCESS_NAME, DATALOAD);
        getProcessService().removeProcess(dataloadProcess);
        assertFalse(getDataloadProcess().isPresent());

        setDefaultSchemaForOutputStage(ads);
        assertTrue(getProcessService().listProcesses(getProject())
                .stream()
                .anyMatch(process -> DEFAULT_DATAlOAD_PROCESS_NAME.equals(process.getName())));
    }

    @Test(dependsOnMethods = {"addUsersToProjects"}, priority = 2)
    public void checkProcessOwner() throws ParseException, JSONException, IOException {
        createUpdateADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);

        // Delete old dataload process and create new one to ensure that
        // execution result isn't influenced by others
        final DataloadProcess dataloadProcess = deleteDataloadProcessAndCreateNewOne();

        assertTrue(getProcessService().listUserProcesses()
              .stream()
              .anyMatch(process -> DEFAULT_DATAlOAD_PROCESS_NAME.equals(process.getName())));
        final ProcessExecutionDetail executionDetail = executeProcess(dataloadProcess, "", SYNCHRONIZE_ALL_PARAM);
        assertTrue(ProcessRestUtils.getExecutionLog(getGoodDataClient(), executionDetail)
            .contains(format("user=%s", testParams.getUser())));

        logout();
        signInAtGreyPages(technicalUser, technicalUserPassword);
        final RestApiClient restApi = getRestApiClient(technicalUser, technicalUserPassword);
        assertEquals(redeployDataLoadProcess(restApi), HttpStatus.OK.value());
        assertEquals(getOwnerLogin(), technicalUser,
                "Process owner of dataload after redeployed is not changed!");

        final String executionByTechnicalUser = executeDataloadProcessSuccessfully(restApi);
        assertTrue(getLogContent(restApi, executionByTechnicalUser).contains(
                String.format("user=%s", technicalUser)));

        logout();
        signIn(true, UserRoles.EDITOR);
        final RestApiClient editorRestApi = getRestApiClient(testParams.getEditorUser(), testParams.getEditorPassword());
        final String executionByEditor = executeDataloadProcessSuccessfully(editorRestApi);
        assertTrue(getLogContent(editorRestApi, executionByEditor).contains(
                String.format("user=%s", technicalUser)));
        assertEquals(getOwnerLogin(), technicalUser,
                "Process owner of dataload is changed after executed process!");
    }

    @Test(dependsOnGroups = {"initialData"}, priority = 3)
    public void checkSuccessfulExecutionLog() throws ParseException, JSONException, IOException {
        createUpdateADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);
        final DataloadProcess dataloadProcess = deleteDataloadProcessAndCreateNewOne();
        final ProcessExecutionDetail executionDetail = executeProcess(dataloadProcess, "", SYNCHRONIZE_ALL_PARAM);
        assertContentLogFile(ProcessRestUtils.getExecutionLog(getGoodDataClient(), executionDetail),
                "execution_log_successful.txt");
    }

    @Test(dependsOnGroups = {"initialData"}, priority = 3)
    public void checkExecutionLogWithErrorMapping() throws ParseException, JSONException, IOException {
        createUpdateADSTable(ADSTables.WITH_ERROR_MAPPING);
        deleteDataloadProcessAndCreateNewOne();

        final String executionUri = executeDataloadProcess(getRestApiClient(), SYNCHRONIZE_ALL_PARAM);
        waitForExecutionInRunningState(executionUri);
        assertEquals(waitForRunningExecutionByStatus(getRestApiClient(), executionUri), ERROR_STATE);

        assertContentLogFile(getLogContent(getRestApiClient(), executionUri), "execution_log_error_mapping.txt");
    }

    @Test(dependsOnGroups = {"initialData"}, priority = 3)
    public void checkConcurrentDataLoadViaRestAPI() throws ParseException, JSONException, IOException {
        createUpdateADSTable(ADSTables.WITH_ADDITIONAL_FIELDS_LARGE_DATA);
        deleteDataloadProcessAndCreateNewOne();

        final String executionUri = executeDataloadProcess(getRestApiClient(), SYNCHRONIZE_ALL_PARAM);
        waitForExecutionInRunningState(executionUri);
        // Check that the second execution will be failed because the first execution is running
        final String errorMessage = failedToCreateDataloadExecution(HttpStatus.CONFLICT, SYNCHRONIZE_ALL_PARAM);
        assertEquals(errorMessage, CONCURRENT_DATA_LOAD_MESSAGE);
        assertTrue(isExecutionSuccessful(getRestApiClient(), executionUri));
    }

    @Test(dependsOnGroups = {"initialData"}, priority = 2)
    public void addUsersToProjects() throws ParseException, IOException, JSONException {
        UserManagementRestUtils.addUserToProject(getRestApiClient(), testParams.getProjectId(), technicalUser, UserRoles.ADMIN);
        UserManagementRestUtils.addUserToProject(getRestApiClient(), testParams.getProjectId(), testParams.getEditorUser(), 
                UserRoles.EDITOR);
        getAdsHelper().addUserToAdsInstance(ads, technicalUser, AdsRole.DATA_ADMIN);
        getAdsHelper().addUserToAdsInstance(ads, testParams.getEditorUser(), AdsRole.DATA_ADMIN);
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws JSONException, ParseException, IOException {
        logout();
        signIn(true, UserRoles.ADMIN);
        getAdsHelper().removeAds(ads);
    }

    private void waitForExecutionInRunningState(String executionUri) throws ParseException, IOException,
            JSONException {
        String state = "";
        do {
            state = getExecutionStatus(getRestApiClient(), executionUri);
            if (RUNNING_STATE.equals(state))
                break;
        } while (!ERROR_STATE.equals(state) && !OK_STATE.equals(state));
    }

    private String getOwnerLogin() throws IOException, JSONException {
        return ProcessRestUtils.getDataloadProcessOwner(getRestApiClient(), testParams.getProjectId());
    }

    private String getLogContent(RestApiClient restApiClient, String executionUri)
            throws ParseException, IOException {
        return RestUtils.getResource(restApiClient, executionUri + "/log", HttpStatus.OK);
    }

    private void assertContentLogFile(String logContent, String expectedLogFile) throws IOException {
        File logFile = getResourceAsFile("/" + API_RESOURCES + "/" + expectedLogFile);
        List<String> allLines = FileUtils.readLines(logFile);
        for (String line : allLines) {
            boolean containsLine = logContent.contains(line.trim()); 
            if (!containsLine) {
                System.out.println("Different line: " + line);
            }
            assertTrue(containsLine, "Log content is not correct!");
        }

        Pattern myPattern = Pattern.compile("Selected datasets: dataset.opportunity,\\s?dataset.person");
        Matcher m = myPattern.matcher(logContent);
        assertTrue(m.find(), "Log content is not correct!");
    }

    private String failedToCreateDataloadExecution(HttpStatus expectedStatusCode, Map<String, String> params)
            throws IOException, JSONException {
        final String executionUri = DataloadProcess.TEMPLATE
                .expand(testParams.getProjectId(), getDataloadProcessId()).toString() + "/executions";
        return ProcessRestUtils.createProcessExecution(expectedStatusCode, getRestApiClient(), executionUri, "", params);
    }

    private String executeDataloadProcessSuccessfully(RestApiClient restApiClient)
            throws IOException, JSONException {
        final String executionUri = executeDataloadProcess(restApiClient, SYNCHRONIZE_ALL_PARAM);
        RolapRestUtils.waitingForAsyncTask(restApiClient, executionUri);
        assertTrue("OK".equals(getExecutionStatus(restApiClient, executionUri)));
        return executionUri;
    }

    private String executeDataloadProcess(RestApiClient restApiClient, Map<String, String> params)
            throws IOException, JSONException {
        final String executionUri = DataloadProcess.TEMPLATE
                .expand(testParams.getProjectId(), getDataloadProcessId()).toString() + "/executions";
        return ProcessRestUtils.executeProcess(restApiClient, executionUri, "", params);
    }

    private String waitForRunningExecutionByStatus(RestApiClient restApiClient,
            String executionUri) throws JSONException, IOException {
        String status;
        do {
            status = getExecutionStatus(restApiClient, executionUri);
            System.out.println("Current execution status is: " + status);
            sleepTightInSeconds(2);
        } while("QUEUED".equals(status) || "RUNNING".equals(status));

        return status;
    }
}
