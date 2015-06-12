package com.gooddata.qa.graphene.dlui;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.API_RESOURCES;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsFile;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractMSFTest;
import com.gooddata.qa.graphene.dto.Processes;
import com.gooddata.qa.graphene.entity.ADSInstance;
import com.gooddata.qa.graphene.entity.ExecutionParameter;
import com.gooddata.qa.graphene.enums.UserRoles;
import com.gooddata.qa.graphene.utils.ProcessUtils;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.qa.utils.http.RestUtils;
import com.gooddata.qa.utils.webdav.WebDavClient;
import com.google.common.collect.Lists;

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
    public void initialData() throws JSONException {
        prepareLDMAndADSInstance();
        setUpOutputStageAndCreateCloudConnectProcess();
    }

    @Test(dependsOnGroups = {"initialData"},
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

    @Test(dependsOnGroups = {"initialData"},
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

    @Test(dependsOnGroups = {"initialData"},
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

        assertEquals(getOwnerLogin(), testParams.getUser(), "Process owner of dataload is incorrect!");
        String executionUri = executeDataloadProcessSuccessfully(getRestApiClient());
        assertTrue(getLogContent(getRestApiClient(), executionUri)
                .contains(String.format("user: %s", testParams.getUser())));

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

    @Test(dependsOnGroups = {"initialData"}, groups = {"dataloadProcessTest"}, priority = 3)
    public void checkSuccessfulExecutionLog() throws ParseException, JSONException, IOException {
        createUpdateADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);
        deleteDataloadProcessAndCreateNewOne();
        String executionUri = executeDataloadProcessSuccessfully(getRestApiClient());
        assertContentLogFile(getLogContent(getRestApiClient(), executionUri), "execution_log_successful.txt");
    }

    @Test(dependsOnGroups = {"initialData"}, groups = {"dataloadProcessTest"}, priority = 3)
    public void checkExecutionLogWithErrorMapping() throws ParseException, JSONException, IOException,
            InterruptedException {
        createUpdateADSTable(ADSTables.WITH_ERROR_MAPPING);
        deleteDataloadProcessAndCreateNewOne();

        String executionUri = executeDataloadProcess(getRestApiClient(), Lists.newArrayList(
                new ExecutionParameter(GDC_DE_SYNCHRONIZE_ALL, true)));
        waitForExecutionInRunningState(executionUri);
        assertEquals(ProcessUtils.waitForRunningExecutionByStatus(getRestApiClient(), executionUri), ERROR_STATE);

        assertContentLogFile(getLogContent(getRestApiClient(), executionUri), "execution_log_error_mapping.txt");
    }

    @Test(dependsOnGroups = {"initialData"}, groups = {"dataloadProcessTest"}, priority = 3)
    public void checkExecutionLogWithETLFailure() throws ParseException, JSONException, IOException,
            InterruptedException {
        List<String> deleteFiles = new ArrayList<String>(Arrays.asList("f_opportunity.csv",
                "dataset.opportunity.csv", "f_person.csv", "dataset.person.csv"));

        createUpdateADSTable(ADSTables.WITH_ADDITIONAL_FIELDS_LARGE_DATA);
        deleteDataloadProcessAndCreateNewOne();

        String webDavUrl = getWebDavUrl();
        String executionUri = executeDataloadProcess(getRestApiClient(), Lists.newArrayList(
                new ExecutionParameter(GDC_DE_SYNCHRONIZE_ALL, true)));
        waitForExecutionInRunningState(executionUri);
        String etlTrigerParams = waitForTriggerEtlPullAndGetParams(executionUri);

        deleteFilesFromWebdav(webDavUrl + "/" + etlTrigerParams, deleteFiles);
        assertEquals(ProcessUtils.waitForRunningExecutionByStatus(getRestApiClient(), executionUri), ERROR_STATE,
                "Execution is not failed because of ETL pull error!");

        assertContentLogFile(getLogContent(getRestApiClient(), executionUri), "execution_log_error_etl.txt");
    }

    @Test(dependsOnGroups = {"initialData"}, groups = {"dataloadProcessTest"}, priority = 3)
    public void checkConcurrentDataLoadViaRestAPI() throws ParseException, JSONException,
            IOException, InterruptedException {
        createUpdateADSTable(ADSTables.WITH_ADDITIONAL_FIELDS_LARGE_DATA);
        deleteDataloadProcessAndCreateNewOne();

        Collection<ExecutionParameter> dataloadExecutionParams =
                Lists.newArrayList(new ExecutionParameter(GDC_DE_SYNCHRONIZE_ALL, true));
        String executionUri = executeDataloadProcess(getRestApiClient(), dataloadExecutionParams);
        waitForExecutionInRunningState(executionUri);
        // Check that the second execution will be failed because the first execution is running
        String errorMessage =
                failedToCreateDataloadExecution(HttpStatus.CONFLICT, dataloadExecutionParams);
        assertEquals(errorMessage, CONCURRENT_DATA_LOAD_MESSAGE);
        assertTrue(ProcessUtils.isExecutionSuccessful(getRestApiClient(), executionUri));
    }

    @Test(dependsOnGroups = {"initialData"}, groups = {"dataloadProcessTest"}, priority = 2)
    public void addUsersToProjects() throws ParseException, IOException, JSONException {
        RestUtils.addUserToProject(testParams.getHost(), testParams.getProjectId(),
                testParams.getUser(), testParams.getPassword(), technicalUserUri, UserRoles.ADMIN);
        RestUtils.addUserToProject(testParams.getHost(), testParams.getProjectId(),
                testParams.getUser(), testParams.getPassword(), testParams.getEditorProfileUri(),
                UserRoles.EDITOR);
        addUserToAdsInstance(adsInstance, technicalUserUri, technicalUser, "dataAdmin");
        addUserToAdsInstance(adsInstance, testParams.getEditorProfileUri(),
                testParams.getEditorUser(), "dataAdmin");
    }

    @Test(dependsOnGroups = "dataloadProcessTest", alwaysRun = true)
    public void cleanUp() throws JSONException {
        logout();
        signIn(true, UserRoles.ADMIN);
        deleteADSInstance(adsInstance);
    }

    private void waitForExecutionInRunningState(String executionUri) throws ParseException, IOException,
            JSONException {
        String state = "";
        do {
            state = ProcessUtils.getExecutionStatus(getRestApiClient(), executionUri);
            if (RUNNING_STATE.equals(state))
                break;
        } while (!ERROR_STATE.equals(state) && !OK_STATE.equals(state));
    }

    private String waitForTriggerEtlPullAndGetParams(String executionUri) throws IOException, JSONException {
        String logContent;
        String state;

        long timeOut = System.currentTimeMillis() + 5 * 60 * 1000; // 5 minutes
        while (true) {
            logContent = getLogContent(getRestApiClient(), executionUri);
            Pattern myPattern = Pattern.compile("ETL pull triggered, params: (.*)");
            Matcher m = myPattern.matcher(logContent);

            if (m.find()) {
                state = ProcessUtils.getExecutionStatus(getRestApiClient(), executionUri);
                assertEquals(state, RUNNING_STATE);
                return m.group(1);
            }

            if (System.currentTimeMillis() >= timeOut) { 
                break;
            }
        }
        throw new IllegalStateException("ETL pull triggered params not found!");
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

    private String getLogContent(RestApiClient restApiClient, String executionUri) {
        return RestUtils.getResource(restApiClient, executionUri + "/log", HttpStatus.OK);
    }

    private String getWebDavUrl() {
        openUrl(PAGE_GDC);
        waitForElementPresent(gdcFragment.getRoot());
        return gdcFragment.getUserUploadsURL();
    }

    private void deleteFilesFromWebdav(String webDavStructure, List<String> deleteUrls) throws IOException,
            InterruptedException {
        WebDavClient webDav = WebDavClient.getInstance(testParams.getUser(), testParams.getPassword());

        final int timeout = 2 * 60 * 1000; // 2 mins
        int totalWaitingTime = 0;
        final int sleepTime = 200;
        String fileUrl;
        while (deleteUrls.size() > 0 && totalWaitingTime < timeout) {
            for (String file : deleteUrls) {
                System.out.println(String.format("Waiting for deleting file %s from WebDav...", file));

                fileUrl = webDavStructure + "/" + file;
                if (webDav.isFilePresent(fileUrl)) {
                    webDav.deleteFile(fileUrl);
                    deleteUrls.remove(file);
                    break;
                }

                if (totalWaitingTime >= timeout) {
                    System.out.println("Time out for deleting files from webdav!");
                    break;
                }
                Thread.sleep(sleepTime);
                totalWaitingTime += sleepTime;
            }
        }
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
}
