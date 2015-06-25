package com.gooddata.qa.graphene.disc;

import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.enums.UserRoles;
import com.gooddata.qa.graphene.enums.disc.ScheduleCronTimes;
import com.gooddata.qa.graphene.utils.ProcessUtils;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestUtils;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.MAQL_FILES;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;
import static org.testng.Assert.*;
import static java.util.Arrays.asList;

public class DataloadSchedulesTest extends AbstractSchedulesTest {

    private static final String CONCURRENT_DATA_LOAD_MESSAGE = "The schedule did not run"
            + " because one or more of the datasets in this schedule is already synchronizing.";
    private static final String NO_LOG_AVAILABLE_TITLE =
            "No log available. There was an error while executing the schedule.";
    private static final String OPPORTUNITY_DATASET = "opportunity";
    private static final String PERSON_DATASET = "person";

    private static final String SCHEDULE_DETAIL_URI = "/gdc/projects/%s/schedules/%s";

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
        createUpdateADSTable(ADSTables.WITH_ADDITIONAL_FIELDS_LARGE_DATA);

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

    @Test(dependsOnMethods = {"setUp"}, groups = {"dataloadSchedulesTest"})
    public void checkScheduleOwnerAfterRedeployDataloadProcess()
            throws ParseException, IOException, JSONException {
        createUpdateADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);

        ScheduleBuilder schedule = new ScheduleBuilder().setProcessName(DEFAULT_DATAlOAD_PROCESS_NAME)
                .setCronTime(ScheduleCronTimes.CRON_15_MINUTES).setHasDataloadProcess(true)
                .setDatasetsToSynchronize(asList(OPPORTUNITY_DATASET))
                .setScheduleName("Check Schedule Owner");

        openProjectDetailPage(getWorkingProject());
        createSchedule(schedule);
        try {
            String scheduleOwner = String.format("\"ownerLogin\":\"%s\"", testParams.getUser());
            schedule.setScheduleUrl(browser.getCurrentUrl());

            assertTrue(getScheduleDetail(schedule.getScheduleUrl()).contains(scheduleOwner),
                    "Schedule owner is not admin user");

            RestUtils.addUserToProject(testParams.getHost(), testParams.getProjectId(),
                    testParams.getUser(), testParams.getPassword(), technicalUserUri, UserRoles.ADMIN);
            addUserToAdsInstance(adsInstance, technicalUserUri, technicalUser, "dataAdmin");

            logout();
            signInAtGreyPages(technicalUser, technicalUserPassword);

            redeployDataLoadProcess(getRestApiClient(technicalUser, technicalUserPassword));

            assertTrue(getScheduleDetail(schedule.getScheduleUrl()).contains(scheduleOwner),
                    "Schedule owner is changed after re-deployed dataload process.");

            openScheduleViaUrl(schedule.getScheduleUrl());
            runManualAndCheckExecutionSuccessful("checkScheduleOwnerAfterRedeployDataloadProcess");
        } finally {
            logout();
            signIn(true, UserRoles.ADMIN);
            openScheduleViaUrl(schedule.getScheduleUrl());
            scheduleDetail.disableSchedule();
        }
    }

    @Test(dependsOnGroups = {"dataloadSchedulesTest"}, groups = {"reference"}, alwaysRun = true)
    public void autoCreationConnectingDatasets() throws InterruptedException, ParseException,
            JSONException, IOException {
        updateModelOfGDProject(getResourceAsString("/" + MAQL_FILES + "/dropTablesOpportunityPerson.txt"));
        createUpdateADSTable(ADSTables.WITH_ADDITIONAL_FIELDS_AND_REFERECES);
        updateModelOfGDProject(getResourceAsString("/" + MAQL_FILES + "/create-ldm-references.txt"));

        ScheduleBuilder schedule = new ScheduleBuilder().setProcessName(DEFAULT_DATAlOAD_PROCESS_NAME)
                .setCronTime(ScheduleCronTimes.CRON_15_MINUTES).setHasDataloadProcess(true)
                .setSynchronizeAllDatasets(true)
                .setScheduleName("Check Auto Creation Connecting");

        openProjectDetailPage(getWorkingProject());
        createSchedule(schedule);
        try {
            schedule.setScheduleUrl(browser.getCurrentUrl());
            runManualAndCheckExecutionSuccessful("autoCreationConnectingDatasets");

            Thread.sleep(3000); // Wait for project model updating
            List<String> references = getReferencesOfDataset("track");
            System.out.println("References: " + references);
            assertTrue(references.contains("dataset.artist"), "Reference was not added automatically!");

            checkReportAfterAddReferenceToDataset();
        } finally {
            openScheduleViaUrl(schedule.getScheduleUrl());
            scheduleDetail.disableSchedule();
        }
    }

    @Test(dependsOnGroups = {"dataloadSchedulesTest", "reference"}, alwaysRun = true)
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

    private String getScheduleDetail(String scheduleUrl) {
        Pattern myPattern = Pattern.compile("/schedules/(.*)");
        Matcher m = myPattern.matcher(scheduleUrl);
        String scheduleId = "";
        if (m.find()) {
            scheduleId = m.group(1);
            return RestUtils.getResource(getRestApiClient(),
                    String.format(SCHEDULE_DETAIL_URI, testParams.getProjectId(), scheduleId), HttpStatus.OK);
        } 

        throw new IllegalStateException("Schedule ID wasn't found, we could not get schedule detail content!"); 
    }

    private void runManualAndCheckExecutionSuccessful(String screenShot) {
        scheduleDetail.manualRun();
        browser.navigate().refresh();
        waitForElementVisible(scheduleDetail.getRoot());
        scheduleDetail.waitForExecutionFinish();
        Screenshots.takeScreenshot(browser, screenShot, getClass());
        scheduleDetail.assertManualRunExecution();
    }
}
