package com.gooddata.qa.graphene.dlui;

import static org.testng.Assert.*;

import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.dto.Processes;
import com.gooddata.qa.graphene.entity.ADSInstance;
import com.gooddata.qa.graphene.entity.disc.ExecutionTask;
import com.gooddata.qa.utils.http.RestUtils;

public class DataloadProcessTest extends AbstractDLUITest{

    private static final String CONCURRENT_DATA_LOAD_MESSAGE = "The schedule did not run"
            + " because one or more of the datasets in this schedule is already synchronizing.";
    private String projectId;

    @BeforeClass
    public void initProjectTitle() {
        projectTitle = "Dataload process test";
    }

    @Test(dependsOnMethods = {"prepareLDMAndADSInstance"}, groups = {"initialDataForDLUI"})
    public void initialProperties() {
        projectId = testParams.getProjectId();
    }

    @Test(dependsOnGroups = {"initialDataForDLUI"})
    public void autoCreateDataloadProcess() throws IOException, JSONException {
        createUpdateADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);
        assertTrue(dataloadProcessIsCreated(), "DATALOAD process is not created!");
        Processes dataloadProcessList = RestUtils.getProcessesList(getRestApiClient(), projectId);
        assertEquals(dataloadProcessList.getDataloadProcess().getName(), DEFAULT_DATAlOAD_PROCESS_NAME);
        assertEquals(dataloadProcessList.getDataloadProcessCount(), 1);
        assertEquals(createDataLoadProcess(), HttpStatus.CONFLICT.value());
        ExecutionTask execution = RestUtils.createDataloadProcessExecution(getRestApiClient(),
                getDataloadProcessUri());
        assertEquals(execution.getStatusCode(), HttpStatus.CREATED.value());
        assertEquals(RestUtils.getLastExecutionPollingState(restApiClient, execution.getDetailLink()), "OK");
    }

    @Test(dependsOnMethods = {"autoCreateDataloadProcess"})
    public void changeAdsInstanceWhenHavingDataloadProcess() throws IOException, JSONException {
        createDataLoadProcess();
        String dataloadProcessId = getDataloadProcessId();
        adsInstance = new ADSInstance().withName("ADS Instance for DLUI test 2")
                .withAuthorizationToken(testParams.loadProperty("dss.authorizationToken"));
        createADSInstance(adsInstance);

        setDefaultSchemaForOutputStage(getRestApiClient(), adsInstance.getId());
        assertEquals(RestUtils.getProcessesList(getRestApiClient(), projectId).getDataloadProcessCount(), 1);
        assertEquals(getDataloadProcessId(), dataloadProcessId);
    }

    @Test(dependsOnMethods = {"autoCreateDataloadProcess"})
    public void changeAdsInstanceAfterDeleteDataloadProcess() throws IOException, JSONException{
        createDataLoadProcess();
        RestUtils.deleteDataloadProcess(getRestApiClient(), getDataloadProcessUri(), projectId);
        assertEquals(RestUtils.getProcessesList(getRestApiClient(), projectId).getDataloadProcessCount(), 0);
        setDefaultSchemaForOutputStage(getRestApiClient(), adsInstance.getId());
        Processes dataloadProcessList = RestUtils.getProcessesList(getRestApiClient(), projectId);
        assertEquals(dataloadProcessList.getDataloadProcessCount(), 1);
        assertEquals(dataloadProcessList.getDataloadProcess().getName(), DEFAULT_DATAlOAD_PROCESS_NAME);
    }

    @Test(dependsOnMethods = {"autoCreateDataloadProcess"}, alwaysRun = true)
    public void checkConcurrentDataLoadViaRestAPI() throws ParseException, JSONException, IOException {
        createUpdateADSTable(ADSTables.WITH_ADDITIONAL_FIELDS_LARGE_DATA);
        if (!getDataloadProcessId().isEmpty()) {
            RestUtils.deleteDataloadProcess(getRestApiClient(), getDataloadProcessUri(), projectId);
        }
        createDataLoadProcess();
        assertFalse(getDataloadProcessId().isEmpty());
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

    private void waitForAddingDataTask(String executionDetailLink) 
            throws ParseException, IOException, JSONException {
        String state = "";
        do {
            state = RestUtils.getCurrentExecutionPollingState(restApiClient, executionDetailLink);
            if ("RUNNING".equals(state))
                break;
        } while (!"ERROR".equals(state) && !"OK".equals(state));
    }
}
