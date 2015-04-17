package com.gooddata.qa.graphene.disc;

import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.enums.disc.ScheduleCronTimes;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestUtils;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;

import static java.lang.String.format;
import static org.testng.Assert.*;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForFragmentVisible;

public class DataloadSchedulesTest extends AbstractSchedulesTest {

    private static final String PROCESS_NAME = "Dataload process";
    private static final int STATUS_POLLING_CHECK_ITERATIONS = 60;
    private static final String CONCURRENT_DATA_LOAD_MESSAGE = "The schedule did not run" 
            +" because one or more of the datasets in this schedule is already synchronizing.";
    private static final String NO_LOG_AVAILABLE_TITLE = 
            "No log available. There was an error while executing the schedule.";

    @BeforeClass
    public void initProperties() {
        projectTitle = "Disc-test-dataload-schedule";
    }

    @AfterClass
    public void tearDown() throws IOException, JSONException {
        RestUtils.deleteDataloadProcess(getRestApiClient() ,getDataloadProcessUri(), getWorkingProject().getProjectId());
    }
    
    @Test(dependsOnMethods = {"createProject"})
    public void setUp() throws IOException, JSONException {
        createDataloadProcessIfDoesntExist();
        createDatasets();
    }

    @Test(dependsOnMethods = {"setUp"}, groups = {"schedule", "tests"})
    public void createDataloadScheduleWithAllDatasets() throws JSONException, InterruptedException {
        openProjectDetailPage(getWorkingProject());

        ScheduleBuilder scheduleBuilder = new ScheduleBuilder().setProcessName(PROCESS_NAME)
                .setCronTime(ScheduleCronTimes.CRON_15_MINUTES)
                .setConfirmed(true)
                .setHasDataloadProcess(true)
                .setSynchronizeAllDatasets(true)
                .setScheduleName("All datasets");
        createAndAssertSchedule(scheduleBuilder);
    }

    @Test(dependsOnMethods = {"setUp", "createDataloadScheduleWithAllDatasets"}, groups = {"schedule", "tests"})
    public void createDataloadScheduleWithCustomDatasets() throws JSONException, InterruptedException {
        openProjectDetailPage(getWorkingProject());

        ScheduleBuilder scheduleBuilder = new ScheduleBuilder().setProcessName(PROCESS_NAME)
                .setCronTime(ScheduleCronTimes.CRON_15_MINUTES)
                .setConfirmed(true)
                .setHasDataloadProcess(true)
                .setDatasetsToSynchronize(Arrays.asList("Salesforce"))
                .setAllDatasets(Arrays.asList("Salesforce", "test"))
                .setScheduleName("1 dataset")
                .setDataloadDatasetsOverlap(true);
        createAndAssertSchedule(scheduleBuilder);
    }

    @Test(dependsOnMethods = {"createDataloadScheduleWithCustomDatasets"}, groups = {"schedule", "tests"})
    public void editDataloadScheduleWithCustomDatasets() throws JSONException, InterruptedException {

        openProjectDetailPage(getWorkingProject());

        ScheduleBuilder scheduleBuilder = new ScheduleBuilder().setProcessName(PROCESS_NAME)
                .setCronTime(ScheduleCronTimes.CRON_15_MINUTES)
                .setConfirmed(true)
                .setHasDataloadProcess(true)
                .setDatasetsToSynchronize(Arrays.asList("Salesforce"))
                .setAllDatasets(Arrays.asList("Salesforce", "test"))
                .setScheduleName("1 dataset (1)")
                .setDataloadDatasetsOverlap(true);
        createAndAssertSchedule(scheduleBuilder);

        scheduleBuilder.setScheduleUrl(browser.getCurrentUrl());

        scheduleDetail.changeAndCheckDatasetDialog(scheduleBuilder);
    }

    @Test(dependsOnMethods = {"setUp"})
    public void checkConcurrentDataLoadSchedule() {
        openProjectDetailPage(getWorkingProject());

        ScheduleBuilder schedule1 = new ScheduleBuilder().setProcessName(PROCESS_NAME)
                .setCronTime(ScheduleCronTimes.CRON_15_MINUTES)
                .setHasDataloadProcess(true)
                .setDatasetsToSynchronize(Arrays.asList("Salesforce"))
                .setAllDatasets(Arrays.asList("Salesforce", "test"))
                .setScheduleName("Salesforce data load 1");
        createSchedule(schedule1);
        schedule1.setScheduleUrl(browser.getCurrentUrl());

        ScheduleBuilder schedule2 = new ScheduleBuilder().setProcessName(PROCESS_NAME)
                .setCronTime(ScheduleCronTimes.CRON_15_MINUTES)
                .setHasDataloadProcess(true)
                .setDatasetsToSynchronize(Arrays.asList("Salesforce"))
                .setAllDatasets(Arrays.asList("Salesforce", "test"))
                .setScheduleName("Salesforce data load 2");
        openProjectDetailByUrl(testParams.getProjectId());
        createSchedule(schedule2);
        browser.navigate().refresh();
        waitForFragmentVisible(scheduleDetail);
        schedule2.setScheduleUrl(browser.getCurrentUrl());

        openScheduleViaUrl(schedule1.getScheduleUrl());
        scheduleDetail.manualRun();
        assertTrue(scheduleDetail.isInRunningState());
        scheduleDetail.clickOnCloseScheduleButton();

        openScheduleViaUrl(schedule2.getScheduleUrl());
        scheduleDetail.tryToRun();
        assertConcurrentDataloadScheduleFailed();
        Screenshots.takeScreenshot(browser, "Concurrent-Dataload-Schedule-DISC", getClass());
    }

    private void assertConcurrentDataloadScheduleFailed() {
        scheduleDetail.waitForExecutionFinish();
        assertTrue(scheduleDetail.isLastSchedulerErrorIconVisible());
        assertEquals(scheduleDetail.getExecutionErrorDescription(), CONCURRENT_DATA_LOAD_MESSAGE);
        assertEquals(scheduleDetail.getLastExecutionLogTitle(), NO_LOG_AVAILABLE_TITLE); 
        assertTrue(scheduleDetail.getExecutionRuntime().isEmpty());
        assertFalse(scheduleDetail.getLastExecutionTime().isEmpty());
        assertEquals(scheduleDetail.getLastExecutionLogTitle(), NO_LOG_AVAILABLE_TITLE);
        assertNull(scheduleDetail.getLastExecutionLogLink());
    }

    private void createDatasets() {
        HttpRequestBase getRequest = getRestApiClient().newGetMethod(format("/gdc/md/%s/ldm/singleloadinterface/dataset.salesforce", getWorkingProject().getProjectId()));
        HttpResponse getResponse = getRestApiClient().execute(getRequest);

        // dataset already exists
        if (getResponse.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
            EntityUtils.consumeQuietly(getResponse.getEntity());
            return;
        }

        try {
            String maql = IOUtils.toString(getClass().getResource("create-datasets.txt"));
            postMAQL(maql, STATUS_POLLING_CHECK_ITERATIONS);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create datasets", e);
        }
    }


    private void createDataloadProcessIfDoesntExist() throws IOException, JSONException {
        if (createDataloadProcess() == HttpStatus.CONFLICT.value()) {
            String dataloadProcessUri = getDataloadProcessUri();
            RestUtils.deleteDataloadProcess(getRestApiClient(),
                    dataloadProcessUri, getWorkingProject().getProjectId());
        }

        createDataloadProcess();
    }

    private int createDataloadProcess() {
        System.out.println("Creating dataload process for project: " + getWorkingProject().getProjectId());

        HttpRequestBase postRequest = getRestApiClient().newPostMethod(getProcessesUri(), "{\"process\": {\"type\":\"DATALOAD\",\"name\":\"" + PROCESS_NAME + "\"}}");
        HttpResponse postResponse = getRestApiClient().execute(postRequest);

        int responseStatusCode = postResponse.getStatusLine().getStatusCode();

        EntityUtils.consumeQuietly(postResponse.getEntity());
        System.out.println(" - status: " + responseStatusCode);

        return responseStatusCode;
    }

    private String getDataloadProcessUri() throws IOException, JSONException {
        return getProcessesUri() + "/" + 
                RestUtils.getProcessesList(getRestApiClient(), getWorkingProject().getProjectId())
                .getDataloadProcess().getProcessId();
    }

    private String getProcessesUri() {
        return format("/gdc/projects/%s/dataload/processes", getWorkingProject().getProjectId());
    }

}
