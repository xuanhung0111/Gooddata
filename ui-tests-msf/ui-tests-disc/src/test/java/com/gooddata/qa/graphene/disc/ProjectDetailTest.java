package com.gooddata.qa.graphene.disc;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.enums.disc.DeployPackages;
import com.gooddata.qa.graphene.enums.disc.DeployPackages.Executables;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.enums.disc.ScheduleStatus;
import com.gooddata.qa.graphene.fragments.disc.ExecutablesTable;
import com.google.common.base.Predicate;

public class ProjectDetailTest extends AbstractSchedulesTest {

    private String PROJECT_EMPTY_STATE_TITLE = "You don’t have any deployed data loading processes.";
    private String PROJECT_EMPTY_STATE_MESSAGE =
            "How to deploy a process? Read Preparing a Data Loading Process article";
    private static final String DELETE_PROCESS_DIALOG_MESSAGE = "Are you sure you want to delete process \"%s\"?";
    private static final String DELETE_PROCESS_DIALOG_TITLE = "Delete \"%s\" process";
    private static final String EXECUTABLE_NO_SCHEDULES = "No schedules";
    private static final String EXECUTABLE_SCHEDULE_NUMBER = "Scheduled %d time%s";

    private static final long expectedDownloadedProcessSize = 64000L;
    private String downloadFolder;

    @BeforeClass(alwaysRun = true)
    public void initProperties() {
        downloadFolder = testParams.loadProperty("browserDownloadFolder") + testParams.getFolderSeparator();
        projectTitle = "Disc-test-project-detail";
    }

    @AfterMethod
    public void afterTest(Method m) {
        cleanWorkingProjectAfterTest(m);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkProjectInfo() {
        openProjectDetailPage(testParams.getProjectId());
        String processName = "Check Project Info";
        int processNumber = projectDetailPage.getNumberOfProcesses();
        deployInProjectDetailPage(DeployPackages.BASIC, processName);

        assertEquals(processNumber + 1, projectDetailPage.getNumberOfProcesses(),
                "The number of processes is incorrect!");
        openProjectDetailPage(testParams.getProjectId());
        waitForElementVisible(projectDetailPage.getRoot());
        assertEquals(projectTitle, projectDetailPage.getDisplayedProjectTitle());
        assertEquals(testParams.getProjectId(), projectDetailPage.getProjectMetadata("Project ID"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkProcessInfo() {
        openProjectDetailPage(testParams.getProjectId());
        String processName = "Check Process Info";
        deployInProjectDetailPage(DeployPackages.BASIC, processName);

        projectDetailPage.activeProcess(processName);
        assertTrue(projectDetailPage.isTabActive(projectDetailPage.getScheduleTab()), 
                "Schedule tab is not active!");
        assertTrue(projectDetailPage.isEmptyScheduleList(), "Schedule list is not empty!");

        projectDetailPage.clickOnExecutableTab();
        assertTrue(projectDetailPage.isTabActive(projectDetailPage.getExecutableTab()), 
                "Executable tab is not active!");
        assertTrue(projectDetailPage.isCorrectExecutableList(DeployPackages.BASIC.getExecutables()),
                "Incorrect executable list");

        projectDetailPage.clickOnMetadataTab();
        String processID =
                browser.getCurrentUrl()
                        .substring(browser.getCurrentUrl().indexOf("processes/"),
                                browser.getCurrentUrl().lastIndexOf("/")).replace("processes/", "");
        System.out.println("processID: " + processID);
        assertEquals(processID, projectDetailPage.getMetadata(), "Incorrect process metadata!");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkGoToDashboardsLinkInProjectDetailPage() {
        openProjectDetailPage(testParams.getProjectId());
        waitForElementVisible(projectDetailPage.getRoot());
        projectDetailPage.goToDashboards();
        waitForDashboardPageLoaded(browser);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkEmptyProjectState() {
        openProjectDetailPage(testParams.getProjectId());
        waitForElementVisible(projectDetailPage.getRoot());
        assertEquals(PROJECT_EMPTY_STATE_TITLE, projectDetailPage.getProjectEmptyStateTitle());
        assertEquals(PROJECT_EMPTY_STATE_MESSAGE, projectDetailPage.getProjectEmptyStateMessage());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void downloadProcess() {
        openProjectDetailPage(testParams.getProjectId());
        String processName = "Download Process Test";
        deployInProjectDetailPage(DeployPackages.BASIC, processName);

        System.out.println("Download folder: " + downloadFolder);
        projectDetailPage.clickOnMetadataTab();
        String processID = projectDetailPage.getMetadata();
        projectDetailPage.clickOnDownloadButton();

        final File zipDownload = new File(downloadFolder + processID + "-decrypted.zip");
        Predicate<WebDriver> downloadProcessFinished = browser -> {
            System.out.println("Wait for downloading process!");
            return zipDownload.length() > expectedDownloadedProcessSize;
        };
        
        Graphene.waitGui().withTimeout(3, TimeUnit.MINUTES).pollingEvery(10, TimeUnit.SECONDS)
            .until(downloadProcessFinished);
        System.out.println("Download file size: " + zipDownload.length());
        System.out.println("Download file path: " + zipDownload.getPath());
        System.out.println("Download file name: " + zipDownload.getName());
        assertTrue(zipDownload.length() > expectedDownloadedProcessSize, "Process \"" + processName
                + "\" is downloaded sucessfully!");
        zipDownload.delete();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkSortedProcesses() {
        openProjectDetailPage(testParams.getProjectId());
        deployInProjectDetailPage(DeployPackages.BASIC, "Process-A");
        deployInProjectDetailPage(DeployPackages.BASIC, "Process-Z");
        deployInProjectDetailPage(DeployPackages.BASIC, "Process-B");
        deployInProjectDetailPage(DeployPackages.BASIC, "Process-P");

        openProjectDetailPage(testParams.getProjectId());
        checkSortedProcessList();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkSortedProcessesAfterRedeploy() {
        openProjectDetailPage(testParams.getProjectId());
        deployInProjectDetailPage(DeployPackages.BASIC, "Process-A");
        deployInProjectDetailPage(DeployPackages.BASIC, "Process-P");
        deployInProjectDetailPage(DeployPackages.BASIC, "Process-R");

        openProjectDetailPage(testParams.getProjectId());
        redeployProcess("Process-R", DeployPackages.EXECUTABLES_GRAPH, "Process-B");

        openProjectDetailPage(testParams.getProjectId());
        checkSortedProcessList();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkDeleteProcess() {
        openProjectDetailPage(testParams.getProjectId());
        deployInProjectDetailPage(DeployPackages.BASIC, "Process-A");
        deployInProjectDetailPage(DeployPackages.BASIC, "Process-Z");
        deployInProjectDetailPage(DeployPackages.BASIC, "Process-B");
        deployInProjectDetailPage(DeployPackages.BASIC, "Process-P");

        openProjectDetailPage(testParams.getProjectId());
        projectDetailPage.activeProcess("Process-P").deleteProcess();
        assertFalse(projectDetailPage.isExistingProcess("Process-P"));
        checkSortedProcessList();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkProcessDeleteDialog() {
        openProjectDetailPage(testParams.getProjectId());
        deployInProjectDetailPage(DeployPackages.BASIC, "Process-B");
        String processName = "Process-A";
        deployInProjectDetailPage(DeployPackages.BASIC, processName);

        openProjectDetailPage(testParams.getProjectId());
        String deleteProcessTitle = String.format(DELETE_PROCESS_DIALOG_TITLE, processName);
        String deleteProcessMessage = String.format(DELETE_PROCESS_DIALOG_MESSAGE, processName);
        projectDetailPage.activeProcess(processName).clickOnDeleteButton();
        assertEquals(projectDetailPage.getDeleteDialogTitle(), deleteProcessTitle);
        assertEquals(projectDetailPage.getDeleteDialogMessage(), deleteProcessMessage);
        projectDetailPage.clickOnProcessDeleteCancelButton();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkCancelProcessDeleteDialog() {
        openProjectDetailPage(testParams.getProjectId());
        String processName = "Process-A";
        deployInProjectDetailPage(DeployPackages.BASIC, processName);

        projectDetailPage.activeProcess(processName).clickOnDeleteButton();
        projectDetailPage.clickOnProcessDeleteCancelButton();
        assertTrue(projectDetailPage.isExistingProcess(processName), "Process is not deleted well!");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkExecutableScheduleNumber() {
        openProjectDetailPage(testParams.getProjectId());
        String processName = "Process-A";
        deployInProjectDetailPage(DeployPackages.BASIC, processName);
        createSchedule(new ScheduleBuilder().setProcessName(processName).setExecutable(
                Executables.SUCCESSFUL_GRAPH));
        scheduleDetail.clickOnCloseScheduleButton();

        checkExecutableScheduleNumber(processName, Executables.FAILED_GRAPH.getExecutableName(), 0);
        checkExecutableScheduleNumber(processName, Executables.LONG_TIME_RUNNING_GRAPH.getExecutableName(), 0);
        checkExecutableScheduleNumber(processName, Executables.SUCCESSFUL_GRAPH.getExecutableName(), 1);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkProcessScheduleList() {
        openProjectDetailPage(testParams.getProjectId());
        String processName = "Check Process Schedule List";
        deployInProjectDetailPage(DeployPackages.BASIC, processName);
        ScheduleBuilder successfulScheduleBuilder =
                new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.SUCCESSFUL_GRAPH);
        createAndAssertSchedule(successfulScheduleBuilder);


        WebElement successfulSchedule = projectDetailPage.getSchedule(successfulScheduleBuilder.getScheduleName());

        assertScheduleStatus(successfulSchedule, ScheduleStatus.UNSCHEDULED);
        scheduleDetail.manualRun();

        scheduleDetail.isInRunningState();
        assertScheduleStatus(successfulSchedule, ScheduleStatus.RUNNING);

        assertSuccessfulExecution();
        assertScheduleStatus(successfulSchedule, ScheduleStatus.OK);

        ScheduleBuilder failedScheduleBuilder =
                new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.FAILED_GRAPH);
        createAndAssertSchedule(failedScheduleBuilder);

        scheduleDetail.manualRun();
        assertFailedExecution(failedScheduleBuilder.getExecutable());

        WebElement failedSchedule = projectDetailPage.getSchedule(failedScheduleBuilder.getScheduleName());
        assertScheduleStatus(failedSchedule, ScheduleStatus.ERROR);

        scheduleDetail.disableSchedule();
        assertScheduleStatus(failedSchedule, ScheduleStatus.DISABLED);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkAccessProjectDetailWithNonAdminRole() throws ParseException, IOException, JSONException {
        try {
            accessProjectDetailWithNonAdminRole(testParams.getEditorUser(), testParams.getPassword());
            logout();
            accessProjectDetailWithNonAdminRole(testParams.getViewerUser(), testParams.getPassword());
        } finally {
            logout();
            signInAtGreyPages(testParams.getUser(), testParams.getPassword());
        }
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
        createAndAddUserToProject(UserRoles.VIEWER);
    }

    private void assertScheduleStatus(WebElement schedule, ScheduleStatus scheduleStatus) {
        assertNotNull(schedule, "Schedule is not shown on project detail page!");
        if (scheduleStatus == ScheduleStatus.ERROR)
            assertTrue(schedule.getAttribute("class").contains("is-error"), "Error is not shown!");
        assertNotNull(schedule.findElement(scheduleStatus.getIconByCss()));
        if (scheduleStatus == ScheduleStatus.UNSCHEDULED)
            assertFalse(schedule.getAttribute("class").contains("is-error"), "Error is shown!");
    }

    private void accessProjectDetailWithNonAdminRole(String user, String password) throws JSONException {
        signInAtGreyPages(user, password);
        openUrl(DISC_PROJECTS_PAGE_URL + "/" + testParams.getProjectId());
        waitForFragmentVisible(discOverviewProjects);
    }

    private void checkSortedProcessList() {
        List<WebElement> processes = projectDetailPage.getProcesses();
        for (int i = 0; i < processes.size(); i++) {
            projectDetailPage.activeProcess(processes.get(i));
            System.out.println("Title of Process[" + i + "] : " + projectDetailPage.getProcessTitle());
            if (i > 0) {
                assertTrue(projectDetailPage.getProcessTitle().compareTo(
                        projectDetailPage.activeProcess(processes.get(i - 1)).getProcessTitle()) >= 0);
            }
        }
    }

    private void checkExecutableScheduleNumber(String processName, String executableName, int scheduleNumber) {
        String executableScheduleNumber =
                String.format(EXECUTABLE_SCHEDULE_NUMBER, scheduleNumber, (scheduleNumber > 1 ? "s" : ""));
        projectDetailPage.activeProcess(processName);
        projectDetailPage.clickOnExecutableTab();
        ExecutablesTable executablesTable = projectDetailPage.getExecutableTable();
        waitForElementVisible(executablesTable.getRoot());
        if (scheduleNumber > 0) {
            assertEquals(executablesTable.getExecutableScheduleNumber(executableName), executableScheduleNumber,
                    "Incorrect schedule number is shown for executable " + executableName);
        } else
            assertEquals(executablesTable.getExecutableScheduleNumber(executableName), EXECUTABLE_NO_SCHEDULES,
                    "Incorrect schedule number is shown for executable " + executableName);
    }
}
