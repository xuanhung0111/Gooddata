package com.gooddata.qa.graphene.disc;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.process.ProcessRestUtils.deteleProcess;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertEquals;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static java.util.stream.Collectors.toList;
import static com.google.common.collect.Iterables.getLast;
import static java.lang.String.format;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.GoodDataException;
import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.Schedule;
import com.gooddata.qa.graphene.disc.common.__AbstractDISCTest;
import com.gooddata.qa.graphene.enums.disc.ScheduleStatus;
import com.gooddata.qa.graphene.enums.disc.__Executable;
import com.gooddata.qa.graphene.enums.disc.__ScheduleCronTime;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.ProcessType;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.PackageFile;
import com.gooddata.qa.graphene.fragments.disc.ConfirmationDialog;
import com.gooddata.qa.graphene.fragments.disc.process.ProcessDetail;
import com.gooddata.qa.graphene.fragments.disc.process.ProcessDetail.Tab;
import com.gooddata.qa.graphene.fragments.disc.schedule.__ScheduleDetailFragment;

public class __ProjectsDetailTest extends __AbstractDISCTest {

    private static final String EMPTY_STATE_TITLE = "You don’t have any deployed data loading processes.";
    private static final String EMPTY_STATE_MESSAGE = "How to deploy a process? Read Preparing a Data Loading Process article";
    private static final String NO_SCHEDULE = "No schedules";

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
        createAndAddUserToProject(UserRoles.VIEWER);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkProjectInfo() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            __initDiscProjectDetailPage();
            assertEquals(projectDetailPage.getTitle(), projectTitle);
            assertEquals(projectDetailPage.getProjectIdMetadata(), testParams.getProjectId());
            assertTrue(projectDetailPage.hasProcess(process.getName()));

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkProcessInfo() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            ProcessDetail processDetail = __initDiscProjectDetailPage()
                    .getProcess(process.getName())
                    .openTab(Tab.SCHEDULE);
            assertTrue(processDetail.isTabActive(Tab.SCHEDULE), "Tab is not active");
            assertTrue(processDetail.hasNoSchedule(), "Clean process should have no schedule");

            processDetail.openTab(Tab.EXECUTABLE);
            assertTrue(processDetail.isTabActive(Tab.EXECUTABLE), "Tab is not active");
            assertTrue(isEqualCollection(processDetail.getExecutables(), getExecutablesOf(process)));

            processDetail.openTab(Tab.METADATA);
            assertTrue(processDetail.isTabActive(Tab.METADATA), "Tab is not active");
            assertEquals(processDetail.getMetadata("Process ID"), process.getId());

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void goToDashboardsInProjectDetailPage() {
        __initDiscProjectDetailPage().goToDashboards();
        waitForDashboardPageLoaded(browser);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkProjectEmptyState() {
        __initDiscProjectDetailPage();
        assertEquals(projectDetailPage.getEmptyStateTitle(), EMPTY_STATE_TITLE);
        assertEquals(projectDetailPage.getEmptyStateMessage(), EMPTY_STATE_MESSAGE);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void downloadProcess() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());
        File downloadedFile = null;

        try {
            __initDiscProjectDetailPage().downloadProcess(process.getName());
            downloadedFile = waitForProcessDownloaded(process.getId(), 64000L);

        } finally {
            deteleProcess(getGoodDataClient(), process);
            downloadedFile.deleteOnExit();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkSortedProcesses() {
        Collection<String> listProcessNameInOrder = generateListProcessNameInOrder();
        List<DataloadProcess> processes = createProcesses(listProcessNameInOrder);

        try {
            __initDiscProjectDetailPage();
            takeScreenshot(browser, "processes-sorted-with-alphabet-order", getClass());
            assertEquals(projectDetailPage.getProcessNames(), listProcessNameInOrder);

            String newProcessName = "Z-process";
            projectDetailPage.getProcess(processes.get(0).getName())
                    .redeployWithZipFile(newProcessName, ProcessType.CLOUD_CONNECT, PackageFile.BASIC.loadFile());

            takeScreenshot(browser, "processes-sorted-again-after-redeploy", getClass());
            assertEquals(getLast(projectDetailPage.getProcessNames()), newProcessName);

        } finally {
            processes.stream().forEach(p -> deteleProcess(getGoodDataClient(), p));
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void deleteProcess() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            ConfirmationDialog dialog = __initDiscProjectDetailPage()
                    .getProcess(process.getName())
                    .clickDeleteButton();

            takeScreenshot(browser, "Delete-confirmation-dialog-shows", getClass());
            assertEquals(dialog.getTitle(), format("Delete \"%s\" process", process.getName()));
            assertEquals(dialog.getMessage(), format("Are you sure you want to delete process \"%s\"?", process.getName()));

            dialog.discard();
            assertTrue(projectDetailPage.hasProcess(process.getName()));
            assertFalse(projectDetailPage.deleteProcess(process.getName()).hasProcess(process.getName()),
                    "Process is not deleted successfully");

        } finally {
            try {
                deteleProcess(getGoodDataClient(), process);
            } catch (GoodDataException ex) {
                // Process is already deleted in test and ignore this
            }
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkScheduleInfoFromExecutable() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            createSchedule(process, __Executable.SUCCESSFUL_GRAPH, __ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            ProcessDetail processDetail = __initDiscProjectDetailPage()
                    .getProcess(process.getName())
                    .openTab(Tab.EXECUTABLE);

            assertEquals(processDetail.getScheduleInfoFrom(__Executable.SUCCESSFUL_GRAPH), "Scheduled 1 time");
            assertEquals(processDetail.getScheduleInfoFrom(__Executable.ERROR_GRAPH), NO_SCHEDULE);
            assertEquals(processDetail.getScheduleInfoFrom(__Executable.LONG_TIME_RUNNING_GRAPH), NO_SCHEDULE);
            assertEquals(processDetail.getScheduleInfoFrom(__Executable.SHORT_TIME_ERROR_GRAPH), NO_SCHEDULE);

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkScheduleStatus() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, __Executable.SUCCESSFUL_GRAPH,
                    __ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            ProcessDetail processDetail = __initDiscProjectDetailPage()
                    .getProcess(process.getName())
                    .openTab(Tab.SCHEDULE);
            assertEquals(processDetail.getScheduleStatus(schedule.getName()), ScheduleStatus.UNSCHEDULED);

            __ScheduleDetailFragment scheduleDetail = processDetail.openSchedule(schedule.getName())
                    .executeSchedule()
                    .waitForStatus(ScheduleStatus.RUNNING);
            assertEquals(processDetail.getScheduleStatus(schedule.getName()), ScheduleStatus.RUNNING);

            scheduleDetail.waitForExecutionFinish();
            assertEquals(processDetail.getScheduleStatus(schedule.getName()), ScheduleStatus.OK);

            ((__ScheduleDetailFragment) scheduleDetail.selectExecutable(__Executable.ERROR_GRAPH))
                    .saveChanges().executeSchedule().waitForExecutionFinish();
            assertEquals(processDetail.getScheduleStatus(__Executable.ERROR_GRAPH.getName()),
                    ScheduleStatus.ERROR);

            scheduleDetail.disableSchedule();
            assertEquals(processDetail.getScheduleStatus(__Executable.ERROR_GRAPH.getName()),
                    ScheduleStatus.DISABLED);

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @DataProvider(name = "userProvider")
    public Object[][] getUserProvider() {
        return new Object[][] {
            {UserRoles.EDITOR},
            {UserRoles.VIEWER}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "userProvider")
    public void cannotAccessProjectDetailWithNonAdminRole(UserRoles role) throws JSONException {
        logoutAndLoginAs(true, role);

        try {
            openUrl(format(PROJECT_DETAIL_PAGE_URL, testParams.getProjectId()));
            waitForFragmentVisible(overviewPage).waitForPageLoaded();

        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    private File waitForProcessDownloaded(String processId, long expectedMinimumFileSize) {
        File downloadedFile = new File(testParams.getDownloadFolder() + "/" + processId + "-decrypted.zip");
        int timeoutInSecond = 90;
        int pollingTimeInSecond = 3;

        while (timeoutInSecond != 0) {
            sleepTightInSeconds(pollingTimeInSecond);
            timeoutInSecond = timeoutInSecond - pollingTimeInSecond;

            if (downloadedFile.length() > expectedMinimumFileSize) {
                return downloadedFile;
            }
        }

        throw new RuntimeException("File is not downloaded completely after timeout: " + timeoutInSecond + "seconds");
    }

    private Collection<String> generateListProcessNameInOrder() {
        return IntStream.rangeClosed(1, 5).mapToObj(i -> generateProcessName()).sorted().collect(toList());
    }

    private List<DataloadProcess> createProcesses(Collection<String> listProcessName) {
        return listProcessName.stream().map(p -> createProcessWithBasicPackage(p)).collect(toList());
    }

    private Collection<String> getExecutablesOf(DataloadProcess process) {
        return process.getExecutables()
                .stream().map(e -> e.substring(e.indexOf("/"), e.length())).collect(toList());
    }
}
