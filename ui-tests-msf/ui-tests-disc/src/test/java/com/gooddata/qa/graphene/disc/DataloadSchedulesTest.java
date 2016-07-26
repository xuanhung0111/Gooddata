package com.gooddata.qa.graphene.disc;

import static com.gooddata.qa.graphene.enums.ResourceDirectory.MAQL_FILES;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTight;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.enums.disc.OverviewProjectStates;
import com.gooddata.qa.graphene.enums.disc.ScheduleCronTimes;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.disc.ScheduleDetail.Confirmation;
import com.gooddata.qa.utils.ads.AdsHelper.AdsRole;
import com.gooddata.qa.utils.http.RestUtils;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils;
import com.google.common.collect.Lists;

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
        getProcessService().removeProcess(getDataloadProcess().get());
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"dataloadSchedulesTest"})
    public void setUp() throws IOException, JSONException {
        prepareLDMAndADSInstance();
        setUpOutputStageAndCreateCloudConnectProcess();
        enableDataExplorer();
    }

    @Test(dependsOnMethods = {"setUp"}, groups = {"schedule", "tests", "dataloadSchedulesTest"})
    public void createDataloadScheduleWithAllDatasets() throws JSONException {
        try {
            openProjectDetailPage(testParams.getProjectId());

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

    @Test(dependsOnMethods = {"setUp"}, groups = {"schedule", "tests", "dataloadSchedulesTest"})
    public void createDataloadScheduleWithCustomDatasets() throws JSONException {
        try {
            openProjectDetailPage(testParams.getProjectId());

            ScheduleBuilder scheduleBuilder = new ScheduleBuilder().setProcessName(DEFAULT_DATAlOAD_PROCESS_NAME)
                    .setCronTime(ScheduleCronTimes.CRON_15_MINUTES)
                    .setConfirmed(true).setHasDataloadProcess(true)
                    .setDatasetsToSynchronize(asList(OPPORTUNITY_DATASET))
                    .setAllDatasets(asList(OPPORTUNITY_DATASET, PERSON_DATASET))
                    .setScheduleName("1 dataset");
            createAndAssertSchedule(scheduleBuilder);
        } finally {
            scheduleDetail.disableSchedule();
        }
    }

    @Test(dependsOnMethods = {"createDataloadScheduleWithCustomDatasets"}, groups = {"schedule", "tests",
            "dataloadSchedulesTest"})
    public void editDataloadScheduleWithCustomDatasets() throws JSONException {
        try {
            openProjectDetailPage(testParams.getProjectId());

            ScheduleBuilder scheduleBuilder = new ScheduleBuilder().setProcessName(DEFAULT_DATAlOAD_PROCESS_NAME)
                    .setCronTime(ScheduleCronTimes.CRON_15_MINUTES)
                    .setConfirmed(true).setHasDataloadProcess(true)
                    .setDatasetsToSynchronize(asList(OPPORTUNITY_DATASET))
                    .setAllDatasets(asList(OPPORTUNITY_DATASET, PERSON_DATASET))
                    .setScheduleName("Custom Schedule Name").setDataloadDatasetsOverlap(false);
            createSchedule(scheduleBuilder);

            scheduleBuilder.setScheduleUrl(browser.getCurrentUrl());

            scheduleDetail.openDatasetDialog();
            scheduleDetail.clickOnNoneDatasetButton();
            assertTrue(scheduleDetail.isSelectedDatasetsChecked(Collections.<String>emptyList()));

            scheduleDetail.cancelSelectSynchronizeDatasets();
            assertSchedule(scheduleBuilder);
            assertTrue(scheduleDetail.isCorrectDatasetsSelected(scheduleBuilder), "Incorrect selected datasets!");
            
            scheduleDetail.openDatasetDialog();
            scheduleDetail.clickOnAllDatasetsButton();
            assertTrue(scheduleDetail.isSelectedDatasetsChecked(scheduleBuilder.getAllDatasets()), "All datasets are not checked");

            scheduleDetail.clickOnNoneDatasetButton();
            assertTrue(scheduleDetail.isSelectedDatasetsChecked(Collections.<String>emptyList()),
                    "There are some checked datasets when none dataset option is selected");

            scheduleDetail.saveSelectedSynchronizeDatasets();

            scheduleBuilder.setDatasetsToSynchronize(Collections.<String>emptyList());
            assertSchedule(scheduleBuilder);
            
        } finally {
            scheduleDetail.disableSchedule();
        }
    }

    @Test(dependsOnMethods = { "setUp" }, groups = { "schedule", "tests", "dataloadSchedulesTest" })
    public void testSearchDataset() throws JSONException {
        String fullKey = "opportunity";
        String partKey = "portu";
        String invalidKey = "!@#$%%";
        String multiResultsKey = "p";
        try {
            openProjectDetailPage(testParams.getProjectId());

            ScheduleBuilder scheduleBuilder = new ScheduleBuilder().setProcessName(DEFAULT_DATAlOAD_PROCESS_NAME)
                    .setCronTime(ScheduleCronTimes.CRON_15_MINUTES).setConfirmed(true).setHasDataloadProcess(true)
                    .setDatasetsToSynchronize(asList(OPPORTUNITY_DATASET))
                    .setAllDatasets(asList(OPPORTUNITY_DATASET, PERSON_DATASET))
                    .setScheduleName("Search Dataset Schedule").setDataloadDatasetsOverlap(false);
            createSchedule(scheduleBuilder);
            scheduleBuilder.setScheduleUrl(browser.getCurrentUrl());

            scheduleDetail.openDatasetDialog();
            searchDatasetAndCheckResult(fullKey, asList(OPPORTUNITY_DATASET));
            searchDatasetAndCheckResult(partKey, asList(OPPORTUNITY_DATASET));
            searchDatasetAndCheckResult(invalidKey, Collections.<String>emptyList());
            searchDatasetAndCheckResult(multiResultsKey, 
                    asList(PERSON_DATASET, OPPORTUNITY_DATASET));
        } finally {
            scheduleDetail.disableSchedule();
        }
    }

    @Test(dependsOnMethods = {"setUp"}, groups = {"dataloadSchedulesTest"})
    public void checkConcurrentDataLoadSchedule() {
        createUpdateADSTable(ADSTables.WITH_ADDITIONAL_FIELDS_LARGE_DATA);

        ScheduleBuilder schedule1 =
                new ScheduleBuilder().setProcessName(DEFAULT_DATAlOAD_PROCESS_NAME)
                        .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setHasDataloadProcess(true)
                        .setDatasetsToSynchronize(asList(OPPORTUNITY_DATASET))
                        .setScheduleName(OPPORTUNITY_DATASET + " (1)");
        ScheduleBuilder schedule2 =
                new ScheduleBuilder().setProcessName(DEFAULT_DATAlOAD_PROCESS_NAME)
                        .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setHasDataloadProcess(true)
                        .setDatasetsToSynchronize(asList(OPPORTUNITY_DATASET))
                        .setScheduleName(OPPORTUNITY_DATASET + " (2)");
        try {
            openProjectDetailPage(testParams.getProjectId());
            createSchedule(schedule1);
            schedule1.setScheduleUrl(browser.getCurrentUrl());

            openProjectDetailPage(testParams.getProjectId());
            createSchedule(schedule2);
            browser.navigate().refresh();
            waitForFragmentVisible(scheduleDetail);
            schedule2.setScheduleUrl(browser.getCurrentUrl());

            openScheduleViaUrl(schedule1.getScheduleUrl());
            scheduleDetail.manualRun();
            scheduleDetail.clickOnCloseScheduleButton();

            openScheduleViaUrl(schedule2.getScheduleUrl());
            scheduleDetail.tryToRun();
            takeScreenshot(browser, "Concurrent-Dataload-Schedule-Schedule2-Failed", getClass());
            assertConcurrentDataloadScheduleFailed();

            openScheduleViaUrl(schedule1.getScheduleUrl());
            takeScreenshot(browser, "Concurrent-Dataload-Schedule-Schedule1-Successful", getClass());
            assertSuccessfulExecution();
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

        openProjectDetailPage(testParams.getProjectId());
        createSchedule(schedule);
        try {
            String scheduleOwner = String.format("\"ownerLogin\":\"%s\"", testParams.getUser());
            schedule.setScheduleUrl(browser.getCurrentUrl());

            assertTrue(getScheduleDetail(schedule.getScheduleUrl()).contains(scheduleOwner),
                    "Schedule owner is not admin user");

            UserManagementRestUtils.addUserToProject(getRestApiClient(), testParams.getProjectId(), technicalUser,
                    UserRoles.ADMIN);
            getAdsHelper().addUserToAdsInstance(ads, technicalUser, AdsRole.DATA_ADMIN);

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

    @Test(dependsOnMethods = {"setUp"}, groups = {"schedule", "tests", "dataloadSchedulesTest"})
    public void checkDataloadDatasetsOverlap() throws JSONException {
        ScheduleBuilder allDatasetsLoad = new ScheduleBuilder().setProcessName(DEFAULT_DATAlOAD_PROCESS_NAME)
                .setCronTime(ScheduleCronTimes.CRON_15_MINUTES)
                .setConfirmed(true).setHasDataloadProcess(true)
                .setSynchronizeAllDatasets(true)
                .setScheduleName("Not Overlap");
        ScheduleBuilder oneDatasetLoad = new ScheduleBuilder().setProcessName(DEFAULT_DATAlOAD_PROCESS_NAME)
                .setCronTime(ScheduleCronTimes.CRON_15_MINUTES)
                .setConfirmed(true).setHasDataloadProcess(true)
                .setDatasetsToSynchronize(asList(OPPORTUNITY_DATASET))
                .setAllDatasets(asList(OPPORTUNITY_DATASET, PERSON_DATASET))
                .setScheduleName("Overlap")
                .setDataloadDatasetsOverlap(true);
        try {
            openProjectDetailPage(testParams.getProjectId());
            createSchedule(allDatasetsLoad);
            allDatasetsLoad.setScheduleUrl(browser.getCurrentUrl());
            assertSchedule(allDatasetsLoad);
            
            createSchedule(oneDatasetLoad);
            oneDatasetLoad.setScheduleUrl(browser.getCurrentUrl());
            assertSchedule(oneDatasetLoad);
        } finally {
            openScheduleViaUrl(allDatasetsLoad.getScheduleUrl());
            scheduleDetail.deleteSchedule(Confirmation.SAVE_CHANGES);
            openScheduleViaUrl(oneDatasetLoad.getScheduleUrl());
            scheduleDetail.deleteSchedule(Confirmation.SAVE_CHANGES);
        }
    }

    @Test(dependsOnMethods = {"setUp"}, groups = {"dataloadScheduleReportTest"}, priority = 1)
    public void checkManualDataloadOfAllDatasets() throws IOException, JSONException {
        ScheduleBuilder scheduleBuilder =
                new ScheduleBuilder().setProcessName(DEFAULT_DATAlOAD_PROCESS_NAME)
                        .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setHasDataloadProcess(true)
                        .setSynchronizeAllDatasets(true).setScheduleName("Manual dataload of all datasets");
        try {
            deleteAndCreateDefaultModel();

            openProjectDetailPage(testParams.getProjectId());
            createSchedule(scheduleBuilder);
            scheduleBuilder.setScheduleUrl(browser.getCurrentUrl());

            scheduleDetail.manualRun();
            assertSuccessfulExecution();

            checkReportOfAllDatasets();
        } finally {
            openScheduleViaUrl(scheduleBuilder.getScheduleUrl());
            scheduleDetail.disableSchedule();
        }
    }

    @Test(dependsOnMethods = {"setUp"}, groups = {"dataloadScheduleReportTest"}, priority = 1)
    public void checkManualDataloadOfOneDataset() throws IOException, JSONException {
        ScheduleBuilder scheduleBuilder =
                new ScheduleBuilder().setProcessName(DEFAULT_DATAlOAD_PROCESS_NAME)
                        .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setHasDataloadProcess(true)
                        .setDatasetsToSynchronize(asList(OPPORTUNITY_DATASET))
                        .setAllDatasets(asList(OPPORTUNITY_DATASET, PERSON_DATASET))
                        .setScheduleName("Manual dataload of opportunity");
        try {
            deleteAndCreateDefaultModel();

            openProjectDetailPage(testParams.getProjectId());
            createSchedule(scheduleBuilder);
            scheduleBuilder.setScheduleUrl(browser.getCurrentUrl());

            scheduleDetail.manualRun();
            assertSuccessfulExecution();

            prepareMetricToCheckNewAddedFields("age", "price");
            createAndCheckReport("Opportunity dataset", "name", "price [Sum]",
                    Lists.newArrayList("A", "B", "C", "D", "E", "F"),
                    Collections.nCopies(6, "100"));
            createAndCheckReport("Person dataset", "person", "age [Sum]",
                    Lists.newArrayList("(empty value)"), Lists.newArrayList(""));
        } finally {
            openScheduleViaUrl(scheduleBuilder.getScheduleUrl());
            scheduleDetail.disableSchedule();
        }
    }

    @Test(dependsOnMethods = {"setUp"}, groups = {"dataloadScheduleReportTest"}, priority = 1)
    public void checkAutoDataloadOfAllDatasets() throws IOException, JSONException {
        ScheduleBuilder scheduleBuilder =
                new ScheduleBuilder().setProcessName(DEFAULT_DATAlOAD_PROCESS_NAME)
                        .setCronTime(ScheduleCronTimes.CRON_EVERYHOUR).setMinuteInHour("${minute}")
                        .setHasDataloadProcess(true).setSynchronizeAllDatasets(true)
                        .setScheduleName("Auto dataload of all datasets");
        try {
            deleteAndCreateDefaultModel();

            openProjectDetailPage(testParams.getProjectId());
            createSchedule(scheduleBuilder);
            scheduleBuilder.setScheduleUrl(browser.getCurrentUrl());

            assertTrue(scheduleDetail.waitForAutoRunSchedule(scheduleBuilder.getCronTimeBuilder()),
                    "Schedule is not run automatically well!");
            assertSuccessfulExecution();

            checkReportOfAllDatasets();
        } finally {
            openScheduleViaUrl(scheduleBuilder.getScheduleUrl());
            scheduleDetail.disableSchedule();
        }
    }

    @Test(dependsOnMethods = {"setUp"}, groups = {"dataloadScheduleReportTest"}, priority = 1)
    public void checkAutoDataloadOfOneDataset() throws IOException, JSONException {
        ScheduleBuilder scheduleBuilder =
                new ScheduleBuilder().setProcessName(DEFAULT_DATAlOAD_PROCESS_NAME)
                        .setCronTime(ScheduleCronTimes.CRON_EVERYHOUR).setMinuteInHour("${minute}")
                        .setHasDataloadProcess(true).setDatasetsToSynchronize(asList(PERSON_DATASET))
                        .setAllDatasets(asList(OPPORTUNITY_DATASET, PERSON_DATASET))
                        .setScheduleName("Auto dataload of opportunity");
        try {
            deleteAndCreateDefaultModel();

            openProjectDetailPage(testParams.getProjectId());
            createSchedule(scheduleBuilder);
            scheduleBuilder.setScheduleUrl(browser.getCurrentUrl());

            assertTrue(scheduleDetail.waitForAutoRunSchedule(scheduleBuilder.getCronTimeBuilder()),
                    "Schedule is not run automatically well!");
            assertSuccessfulExecution();

            prepareMetricToCheckNewAddedFields("age", "price");
            createAndCheckReport("Opportunity dataset", "name", "price [Sum]",
                    Lists.newArrayList("(empty value)"), Lists.newArrayList(""));
            createAndCheckReport("Person dataset", "person", "age [Sum]",
                    Lists.newArrayList("A", "B", "C", "D", "E", "F", "J"),
                    Lists.newArrayList("36", "34", "10", "8", "2", "40", "13"));
        } finally {
            openScheduleViaUrl(scheduleBuilder.getScheduleUrl());
            scheduleDetail.disableSchedule();
        }
    }

    @Test(dependsOnGroups = {"dataloadSchedulesTest", "dataloadScheduleReportTest"}, groups = {"reference"}, alwaysRun = true)
    public void autoCreationConnectingDatasets() throws ParseException,
            JSONException, IOException {
        updateModelOfGDProject(getResourceAsString("/" + MAQL_FILES + "/dropDefaultModel.txt"));
        createUpdateADSTable(ADSTables.WITH_ADDITIONAL_FIELDS_AND_REFERECES);
        updateModelOfGDProject(getResourceAsString("/" + MAQL_FILES + "/create-ldm-references.txt"));

        ScheduleBuilder schedule =
                new ScheduleBuilder().setProcessName(DEFAULT_DATAlOAD_PROCESS_NAME)
                        .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setHasDataloadProcess(true)
                        .setSynchronizeAllDatasets(true).setScheduleName("Check Auto Creation Connecting");

        openProjectDetailPage(testParams.getProjectId());
        createSchedule(schedule);
        try {
            schedule.setScheduleUrl(browser.getCurrentUrl());
            runManualAndCheckExecutionSuccessful("autoCreationConnectingDatasets");

            sleepTight(3000); // Wait for project model updating
            List<String> references = getReferencesOfDataset("track");
            log.info("References: " + references);
            assertTrue(references.contains("dataset.artist"), "Reference was not added automatically!");

            checkReportAfterAddReferenceToDataset();
        } finally {
            openScheduleViaUrl(schedule.getScheduleUrl());
            scheduleDetail.disableSchedule();
        }
    }

    @Test(dependsOnMethods = {"setUp"}, groups = {"dataloadSchedulesTest"}, priority = 1)
    public void autoRunDataloadSchedule() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(DEFAULT_DATAlOAD_PROCESS_NAME)
                            .setCronTime(ScheduleCronTimes.CRON_EVERYHOUR).setMinuteInHour("${minute}")
                            .setConfirmed(true).setHasDataloadProcess(true).setSynchronizeAllDatasets(true)
                            .setScheduleName("Auto Run Dataload Schedule");
            createAndAssertSchedule(scheduleBuilder);
            assertTrue(scheduleDetail.waitForAutoRunSchedule(scheduleBuilder.getCronTimeBuilder()),
                    "Schedule is not run automatically well!");
            scheduleDetail.waitForExecutionFinish();
            assertSuccessfulExecution();
        } finally {
            scheduleDetail.disableSchedule();
        }
    }

    @Test(dependsOnMethods = {"setUp"}, groups = {"dataloadSchedulesTest"})
    public void disableDataloadSchedule() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(DEFAULT_DATAlOAD_PROCESS_NAME)
                            .setCronTime(ScheduleCronTimes.CRON_EVERYHOUR).setMinuteInHour("${minute}")
                            .setConfirmed(true).setHasDataloadProcess(true).setSynchronizeAllDatasets(true)
                            .setScheduleName("Disabled Dataload Schedule");
            createAndAssertSchedule(scheduleBuilder);

            scheduleDetail.disableSchedule();
            assertTrue(scheduleDetail.isDisabledSchedule(scheduleBuilder.getCronTimeBuilder()));
            scheduleDetail.manualRun();
            assertSuccessfulExecution();

            scheduleBuilder.setCronTime(ScheduleCronTimes.CRON_15_MINUTES);
            scheduleDetail.changeCronTime(scheduleBuilder.getCronTimeBuilder(), Confirmation.SAVE_CHANGES);

            scheduleDetail.enableSchedule();
            assertTrue(scheduleDetail.waitForAutoRunSchedule(scheduleBuilder.getCronTimeBuilder()),
                    "Schedule is not run automatically well!");
            assertSuccessfulExecution();
        } finally {
            scheduleDetail.disableSchedule();
        }
    }


    @Test(dependsOnMethods = {"setUp"}, groups = {"dataloadSchedulesTest"})
    public void dependentDataloadSchedule() {
        String processName = DEFAULT_DATAlOAD_PROCESS_NAME;

        ScheduleBuilder triggerScheduleBuilder =
                new ScheduleBuilder().setProcessName(processName).setHasDataloadProcess(true)
                        .setSynchronizeAllDatasets(true).setScheduleName("Trigger Schedule")
                        .setCronTime(ScheduleCronTimes.CRON_EVERYDAY);
        ScheduleBuilder dependentScheduleBuilder =
                new ScheduleBuilder().setProcessName(processName).setHasDataloadProcess(true)
                        .setSynchronizeAllDatasets(true).setScheduleName("Dependent Schedule")
                        .setCronTime(ScheduleCronTimes.AFTER).setTriggerScheduleGroup(processName)
                        .setTriggerScheduleOption(triggerScheduleBuilder.getScheduleName());
        try {
            openProjectDetailPage(testParams.getProjectId());
            createAndAssertSchedule(triggerScheduleBuilder);
            triggerScheduleBuilder.setScheduleUrl(browser.getCurrentUrl());
            createAndAssertSchedule(dependentScheduleBuilder);
            dependentScheduleBuilder.setScheduleUrl(browser.getCurrentUrl());

            runSuccessfulTriggerSchedule(triggerScheduleBuilder.getScheduleUrl());
            waitForAutoRunDependentSchedule(dependentScheduleBuilder);
            assertSuccessfulExecution();
        } finally {
            openScheduleViaUrl(triggerScheduleBuilder.getScheduleUrl());
            scheduleDetail.disableSchedule();
            openScheduleViaUrl(dependentScheduleBuilder.getScheduleUrl());
            scheduleDetail.disableSchedule();
        }
    }

    @Test(dependsOnMethods = {"setUp"}, groups = {"dataloadSchedulesTest"})
    public void addRetryToDataloadSchedule() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(DEFAULT_DATAlOAD_PROCESS_NAME)
                            .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setConfirmed(true)
                            .setHasDataloadProcess(true).setSynchronizeAllDatasets(true)
                            .setScheduleName("Dataload Schedule with Retry");
            createAndAssertSchedule(scheduleBuilder);
            scheduleDetail.addValidRetry("15", Confirmation.SAVE_CHANGES);
            assertEquals(scheduleDetail.getRescheduleTime(), "15");
        } finally {
            scheduleDetail.disableSchedule();
        }
    }

    @Test(dependsOnMethods = {"setUp"}, groups = {"dataloadSchedulesTest"})
    public void checkDataloadScheduleAtOverviewPage() {
        openProjectDetailPage(testParams.getProjectId());

        ScheduleBuilder scheduleBuilder =
                new ScheduleBuilder().setProcessName(DEFAULT_DATAlOAD_PROCESS_NAME)
                        .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setConfirmed(true)
                        .setHasDataloadProcess(true).setSynchronizeAllDatasets(true)
                        .setScheduleName("Dataload Schedule at Overview page");
        try {
            createAndAssertSchedule(scheduleBuilder);
            scheduleBuilder.setScheduleUrl(browser.getCurrentUrl());
            scheduleDetail.manualRun();
            assertSuccessfulExecution();

            initDISCOverviewPage();
            discOverview.selectOverviewState(OverviewProjectStates.SUCCESSFUL);
            waitForElementVisible(discOverviewProjects.getRoot());
            assertOverviewCustomScheduleName(OverviewProjectStates.SUCCESSFUL,
                    testParams.getProjectId(), scheduleBuilder);
        } finally {
            openScheduleViaUrl(scheduleBuilder.getScheduleUrl());
            scheduleDetail.disableSchedule();
        }
    }

    @Test(dependsOnMethods = {"setUp"}, groups = {"dataloadSchedulesTest"})
    public void deleteDataloadSchedule() {
        openProjectDetailPage(testParams.getProjectId());

        ScheduleBuilder scheduleBuilder =
                new ScheduleBuilder().setProcessName(DEFAULT_DATAlOAD_PROCESS_NAME)
                        .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setConfirmed(true)
                        .setHasDataloadProcess(true).setSynchronizeAllDatasets(true)
                        .setScheduleName("Delete Dataload Schedule");
        createAndAssertSchedule(scheduleBuilder);
        scheduleDetail.deleteSchedule(Confirmation.SAVE_CHANGES);
        assertNull(waitForFragmentVisible(schedulesTable).getSchedule(scheduleBuilder.getScheduleName()),
                "Schedule is not deleted well!");
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws ParseException, JSONException, IOException {
        getAdsHelper().removeAds(ads);
    }

    private void searchDatasetAndCheckResult(String searchKey, List<String> expectedResult) {
        scheduleDetail.searchDatasets(searchKey);
        assertTrue(CollectionUtils.isEqualCollection(scheduleDetail.getSearchedDatasets(), expectedResult), 
                "Search results with keyword" + searchKey + " is not correct!");
        if (expectedResult.isEmpty()) {
            assertEquals(scheduleDetail.getDatasetListCount(), expectedResult.size(), 
                    "Number of search results with keyword" + searchKey + " is not correct!");
        }
    }

    private void assertConcurrentDataloadScheduleFailed() {
        browser.navigate().refresh();
        waitForElementVisible(scheduleDetail.getRoot());
        scheduleDetail.waitForExecutionFinish();
        assertTrue(scheduleDetail.isSchedulerErrorIconVisible(), "Scheduler error icon is not shown!");
        assertEquals(scheduleDetail.getExecutionErrorDescription(), CONCURRENT_DATA_LOAD_MESSAGE, "Incorrect concurrent data load message!");
        assertEquals(scheduleDetail.getLastExecutionLogTitle(), NO_LOG_AVAILABLE_TITLE, "Incorrect no log available!");
        assertTrue(scheduleDetail.getLastExecutionRuntime().isEmpty(), "Execution runtime is not empty");
        assertFalse(scheduleDetail.getLastExecutionTime().isEmpty(), "Execution time is not shown!");
        assertNull(scheduleDetail.getLastExecutionLogLink(), "Log link is not null!");
    }

    private String getScheduleDetail(String scheduleUrl) throws ParseException, IOException {
        Pattern myPattern = Pattern.compile("/schedules/(.*)");
        Matcher m = myPattern.matcher(scheduleUrl);
        String scheduleId = "";
        if (m.find()) {
            scheduleId = m.group(1);
            return RestUtils.getResource(getRestApiClient(),
                    format(SCHEDULE_DETAIL_URI, testParams.getProjectId(), scheduleId), HttpStatus.OK);
        }

        throw new IllegalStateException("Schedule ID wasn't found, we could not get schedule detail content!");
    }

    private void runManualAndCheckExecutionSuccessful(String screenShot) {
        scheduleDetail.manualRun();
        assertSuccessfulExecution();
        takeScreenshot(browser, screenShot, getClass());
    }
    
    private void deleteAndCreateDefaultModel() throws IOException, JSONException {
        updateModelOfGDProject(getResourceAsString("/" + MAQL_FILES + "/dropDefaultModel.txt"));
        updateModelOfGDProject(getResourceAsString("/" + MAQL_FILES + "/" + initialLdmMaqlFile));
    }
    
    private void checkReportOfAllDatasets() {
        prepareMetricToCheckNewAddedFields("age", "price");
        createAndCheckReport("Opportunity dataset", "name", "price [Sum]",
                asList("A", "B", "C", "D", "E", "F"),
                Collections.nCopies(6, "100"));
        createAndCheckReport("Person dataset", "person", "age [Sum]",
                asList("A", "B", "C", "D", "E", "F", "J"),
                asList("36", "34", "10", "8", "2", "40", "13"));
    }
}
