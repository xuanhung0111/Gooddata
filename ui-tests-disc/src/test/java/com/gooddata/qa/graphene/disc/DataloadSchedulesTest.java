package com.gooddata.qa.graphene.disc;

import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.enums.disc.ScheduleCronTimes;
import com.gooddata.qa.graphene.utils.ProcessUtils;
import com.gooddata.qa.utils.graphene.Screenshots;

import org.json.JSONException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collections;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static java.lang.String.format;
import static org.testng.Assert.*;
import static java.util.Arrays.asList;

public class DataloadSchedulesTest extends AbstractSchedulesTest {

    private static final String CONCURRENT_DATA_LOAD_MESSAGE = "The schedule did not run"
            + " because one or more of the datasets in this schedule is already synchronizing.";
    private static final String NO_LOG_AVAILABLE_TITLE =
            "No log available. There was an error while executing the schedule.";
    private static final String OPPORTUNITY_DATASET = "opportunity";
    private static final String PERSON_DATASET = "person";

    @BeforeClass
    public void initProperties() {
        projectTitle = "Disc-test-dataload-schedule";
    }

    @AfterClass
    public void tearDown() throws IOException, JSONException {
        ProcessUtils.deleteDataloadProcess(getRestApiClient(), getDataloadProcessUri(),
                getWorkingProject().getProjectId());
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"dataloadSchedulesTest"})
    public void setUp() throws IOException, JSONException {
        prepareLDMAndADSInstance();
        setUpOutputStageAndCreateCloudConnectProcess();
    }

    @Test(dependsOnMethods = {"setUp"}, groups = {"schedule", "tests", "dataloadSchedulesTest"})
    public void createDataloadScheduleWithAllDatasets() throws JSONException, InterruptedException {
        try {
            openProjectDetailByUrl(testParams.getProjectId());

            ScheduleBuilder scheduleBuilder = new ScheduleBuilder().setProcessName(DEFAULT_DATAlOAD_PROCESS_NAME)
                    .setCronTime(ScheduleCronTimes.CRON_15_MINUTES)
                    .setConfirmed(true).setHasDataloadProcess(true)
                    .setSynchronizeAllDatasets(true)
                    .setScheduleName("All datasets");
            createAndAssertSchedule(scheduleBuilder);
        } finally {
            scheduleDetail.disableSchedule();
        }
    }

    @Test(dependsOnMethods = {"setUp", "createDataloadScheduleWithAllDatasets"}, groups = {
        "schedule", "tests", "dataloadSchedulesTest"})
    public void createDataloadScheduleWithCustomDatasets() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());

            ScheduleBuilder scheduleBuilder = new ScheduleBuilder().setProcessName(DEFAULT_DATAlOAD_PROCESS_NAME)
                    .setCronTime(ScheduleCronTimes.CRON_15_MINUTES)
                    .setConfirmed(true).setHasDataloadProcess(true)
                    .setDatasetsToSynchronize(asList(OPPORTUNITY_DATASET))
                    .setAllDatasets(asList(OPPORTUNITY_DATASET, PERSON_DATASET))
                    .setScheduleName("1 dataset")
                    .setDataloadDatasetsOverlap(true);
            createAndAssertSchedule(scheduleBuilder);
        } finally {
            scheduleDetail.disableSchedule();
        }
    }

    @Test(dependsOnMethods = {"createDataloadScheduleWithCustomDatasets"}, groups = {"schedule", "tests",
            "dataloadSchedulesTest"})
    public void editDataloadScheduleWithCustomDatasets() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(getWorkingProject());

            ScheduleBuilder scheduleBuilder = new ScheduleBuilder().setProcessName(DEFAULT_DATAlOAD_PROCESS_NAME)
                    .setCronTime(ScheduleCronTimes.CRON_15_MINUTES)
                    .setConfirmed(true).setHasDataloadProcess(true)
                    .setDatasetsToSynchronize(asList(OPPORTUNITY_DATASET))
                    .setAllDatasets(asList(OPPORTUNITY_DATASET, PERSON_DATASET))
                    .setScheduleName("Custom Schedule Name").setDataloadDatasetsOverlap(false);
            createSchedule(scheduleBuilder);

            scheduleBuilder.setScheduleUrl(browser.getCurrentUrl());

            scheduleDetail.cancelChangeAndCheckDatasetDialog(scheduleBuilder);

            scheduleDetail.changeAndCheckDatasetDialog(scheduleBuilder);
        } finally {
            scheduleDetail.disableSchedule();
        }
    }

    @Test(dependsOnMethods = { "setUp" }, groups = { "schedule", "tests", "dataloadSchedulesTest" })
    public void testSearchDataset() throws JSONException, InterruptedException {
        String fullKey = "opportunity";
        String partKey = "portu";
        String invalidKey = "!@#$%%";
        String multiResultsKey = "p";
        try {
            openProjectDetailByUrl(testParams.getProjectId());

            ScheduleBuilder scheduleBuilder = new ScheduleBuilder().setProcessName(DEFAULT_DATAlOAD_PROCESS_NAME)
                    .setCronTime(ScheduleCronTimes.CRON_15_MINUTES).setConfirmed(true).setHasDataloadProcess(true)
                    .setDatasetsToSynchronize(asList(OPPORTUNITY_DATASET))
                    .setAllDatasets(asList(OPPORTUNITY_DATASET, PERSON_DATASET))
                    .setScheduleName("Search Dataset Schedule").setDataloadDatasetsOverlap(false);
            createSchedule(scheduleBuilder);
            scheduleBuilder.setScheduleUrl(browser.getCurrentUrl());

            scheduleDetail.openDatasetDialog();
            scheduleDetail.searchDatasetAndCheckResult(fullKey, asList(OPPORTUNITY_DATASET));
            scheduleDetail.searchDatasetAndCheckResult(partKey, asList(OPPORTUNITY_DATASET));
            scheduleDetail.searchDatasetAndCheckResult(invalidKey, Collections.<String>emptyList());
            scheduleDetail.searchDatasetAndCheckResult(multiResultsKey, 
                    asList(PERSON_DATASET, OPPORTUNITY_DATASET));
        } finally {
            scheduleDetail.disableSchedule();
        }
    }

    @Test(dependsOnMethods = {"setUp"}, groups = {"dataloadSchedulesTest"})
    public void checkConcurrentDataLoadSchedule() {
        createUpdateADSTableBySQLFiles("createTableWithAdditionalFields.txt",
                "copyTableWithAdditionalFieldsLargeData.txt", adsInstance);

        ScheduleBuilder schedule1 = new ScheduleBuilder().setProcessName(DEFAULT_DATAlOAD_PROCESS_NAME)
                        .setCronTime(ScheduleCronTimes.CRON_15_MINUTES).setHasDataloadProcess(true)
                        .setDatasetsToSynchronize(asList(OPPORTUNITY_DATASET))
                        .setScheduleName(OPPORTUNITY_DATASET + " (1)");
        ScheduleBuilder schedule2 = new ScheduleBuilder().setProcessName(DEFAULT_DATAlOAD_PROCESS_NAME)
                        .setCronTime(ScheduleCronTimes.CRON_15_MINUTES).setHasDataloadProcess(true)
                        .setDatasetsToSynchronize(asList(OPPORTUNITY_DATASET))
                        .setScheduleName(OPPORTUNITY_DATASET + " (2)");
        try {
            openProjectDetailByUrl(testParams.getProjectId());
            createSchedule(schedule1);
            schedule1.setScheduleUrl(browser.getCurrentUrl());

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
            Screenshots.takeScreenshot(browser, "Concurrent-Dataload-Schedule-Schedule2-Failed", getClass());
            assertConcurrentDataloadScheduleFailed();

            openScheduleViaUrl(schedule1.getScheduleUrl());
            Screenshots.takeScreenshot(browser, "Concurrent-Dataload-Schedule-Schedule1-Successful", getClass());
            scheduleDetail.assertManualRunExecution();
        } finally {
            openScheduleViaUrl(schedule1.getScheduleUrl());
            scheduleDetail.disableSchedule();
            openScheduleViaUrl(schedule2.getScheduleUrl());
            scheduleDetail.disableSchedule();
        }
    }

    @Test(dependsOnGroups = "dataloadSchedulesTest", alwaysRun = true)
    public void cleanUp() {
        deleteADSInstance(adsInstance);
    }

    private void assertConcurrentDataloadScheduleFailed() {
        browser.navigate().refresh();
        waitForElementVisible(scheduleDetail.getRoot());
        scheduleDetail.waitForExecutionFinish();
        assertTrue(scheduleDetail.isLastSchedulerErrorIconVisible());
        assertEquals(scheduleDetail.getExecutionErrorDescription(), CONCURRENT_DATA_LOAD_MESSAGE);
        assertEquals(scheduleDetail.getLastExecutionLogTitle(), NO_LOG_AVAILABLE_TITLE);
        assertTrue(scheduleDetail.getExecutionRuntime().isEmpty());
        assertFalse(scheduleDetail.getLastExecutionTime().isEmpty());
        assertEquals(scheduleDetail.getLastExecutionLogTitle(), NO_LOG_AVAILABLE_TITLE);
        assertNull(scheduleDetail.getLastExecutionLogLink());
    }

    private String getDataloadProcessUri() throws IOException, JSONException {
        return getProcessesUri()
                + ProcessUtils
                        .getProcessesList(getRestApiClient(), getWorkingProject().getProjectId())
                        .getDataloadProcess().getProcessId();
    }

    private String getProcessesUri() {
        return format(DATALOAD_PROCESS_URI, getWorkingProject().getProjectId());
    }
}
