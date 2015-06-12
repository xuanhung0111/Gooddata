package com.gooddata.qa.graphene.disc;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.lang.reflect.Method;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.enums.disc.DeployPackages;
import com.gooddata.qa.graphene.enums.disc.DeployPackages.Executables;
import com.gooddata.qa.graphene.enums.disc.ScheduleStatus;

public class ProjectDetailTest extends AbstractSchedulesTest {

    private String PROJECT_EMPTY_STATE_TITLE =
            "You don’t have any deployed data loading processes.";
    private String PROJECT_EMPTY_STATE_MESSAGE =
            "How to deploy a process? Read Preparing a Data Loading Process article";
    private static final long expectedDownloadedProcessSize = 64000L;
    private String downloadFolder;

    @BeforeClass
    public void initProperties() {
        downloadFolder =
                testParams.loadProperty("browserDownloadFolder") + testParams.getFolderSeparator();
        projectTitle = "Disc-test-project-detail";
    }

    @AfterMethod
    public void afterTest(Method m) {
        cleanWorkingProjectAfterTest(m);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkProjectInfo() {
        openProjectDetailPage(getWorkingProject());
        String processName = "Check Project Info";
        int processNumber = projectDetailPage.getNumberOfProcesses();
        deployInProjectDetailPage(DeployPackages.BASIC, processName);

        assertEquals(processNumber + 1, projectDetailPage.getNumberOfProcesses(),
                "The number of processes is incorrect!");
        openProjectDetailPage(getWorkingProject());
        waitForElementVisible(projectDetailPage.getRoot());
        assertEquals(projectTitle, projectDetailPage.getDisplayedProjectTitle());
        assertEquals(testParams.getProjectId(), projectDetailPage.getProjectMetadata("Project ID"));
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkProcessInfo() {
        openProjectDetailPage(getWorkingProject());
        String processName = "Check Process Info";
        deployInProjectDetailPage(DeployPackages.BASIC, processName);

        projectDetailPage.selectScheduleTab(processName);
        waitForElementVisible(projectDetailPage.checkEmptySchedulesList(processName));

        projectDetailPage.selectExecutableTab(processName);
        projectDetailPage.assertExecutableList(DeployPackages.BASIC.getExecutables());

        projectDetailPage.selectMetadataTab(processName);
        String processID =
                browser.getCurrentUrl()
                        .substring(browser.getCurrentUrl().indexOf("processes/"),
                                browser.getCurrentUrl().lastIndexOf("/")).replace("processes/", "");
        System.out.println("processID: " + processID);
        assertEquals(processID, projectDetailPage.getProcessMetadata("Process ID"));
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkGoToDashboardsLinkInProjectDetailPage() {
        openProjectDetailPage(getWorkingProject());
        waitForElementVisible(projectDetailPage.getRoot());
        projectDetailPage.goToDashboards();
        waitForDashboardPageLoaded(browser);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkEmptyProjectState() {
        openProjectDetailPage(getWorkingProject());
        waitForElementVisible(projectDetailPage.getRoot());
        assertEquals(PROJECT_EMPTY_STATE_TITLE, projectDetailPage.getProjectEmptyStateTitle());
        assertEquals(PROJECT_EMPTY_STATE_MESSAGE, projectDetailPage.getProjectEmptyStateMessage());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void downloadProcess() {
        openProjectDetailPage(getWorkingProject());
        String processName = "Download Process Test";
        deployInProjectDetailPage(DeployPackages.BASIC, processName);

        System.out.println("Download folder: " + downloadFolder);
        projectDetailPage.checkDownloadProcess(processName, downloadFolder,
                expectedDownloadedProcessSize);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkSortedProcesses() {
        openProjectDetailPage(getWorkingProject());
        deployInProjectDetailPage(DeployPackages.BASIC, "Process-A");
        deployInProjectDetailPage(DeployPackages.BASIC, "Process-Z");
        deployInProjectDetailPage(DeployPackages.BASIC, "Process-B");
        deployInProjectDetailPage(DeployPackages.BASIC, "Process-P");

        openProjectDetailPage(getWorkingProject());
        projectDetailPage.checkSortedProcesses();
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkSortedProcessesAfterRedeploy() {
        openProjectDetailPage(getWorkingProject());
        deployInProjectDetailPage(DeployPackages.BASIC, "Process-A");
        deployInProjectDetailPage(DeployPackages.BASIC, "Process-P");
        deployInProjectDetailPage(DeployPackages.BASIC, "Process-R");

        openProjectDetailPage(getWorkingProject());
        redeployProcess("Process-R", DeployPackages.EXECUTABLES_GRAPH, "Process-B");

        openProjectDetailPage(getWorkingProject());
        projectDetailPage.checkSortedProcesses();
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkDeleteProcess() {
        openProjectDetailPage(getWorkingProject());
        deployInProjectDetailPage(DeployPackages.BASIC, "Process-A");
        deployInProjectDetailPage(DeployPackages.BASIC, "Process-Z");
        deployInProjectDetailPage(DeployPackages.BASIC, "Process-B");
        deployInProjectDetailPage(DeployPackages.BASIC, "Process-P");

        openProjectDetailPage(getWorkingProject());
        projectDetailPage.deleteProcess("Process-P");
        assertFalse(projectDetailPage.assertIsExistingProcess("Process-P"));
        projectDetailPage.checkSortedProcesses();
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkProcessDeleteDialog() {
        openProjectDetailPage(getWorkingProject());
        deployInProjectDetailPage(DeployPackages.BASIC, "Process-B");
        String processName = "Process-A";
        deployInProjectDetailPage(DeployPackages.BASIC, processName);

        openProjectDetailPage(getWorkingProject());
        projectDetailPage.checkDeleteProcessDialog(processName);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkCancelProcessDeleteDialog() {
        openProjectDetailPage(getWorkingProject());
        String processName = "Process-A";
        deployInProjectDetailPage(DeployPackages.BASIC, processName);

        projectDetailPage.checkCancelDeleteProcess(processName);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkExecutableScheduleNumber() {
        openProjectDetailPage(getWorkingProject());
        String processName = "Process-A";
        deployInProjectDetailPage(DeployPackages.BASIC, processName);
        createSchedule(new ScheduleBuilder().setProcessName(processName).setExecutable(
                Executables.SUCCESSFUL_GRAPH));
        scheduleDetail.clickOnCloseScheduleButton();

        projectDetailPage.checkExecutableScheduleNumber(processName,
                Executables.FAILED_GRAPH.getExecutableName(), 0);
        projectDetailPage.checkExecutableScheduleNumber(processName,
                Executables.LONG_TIME_RUNNING_GRAPH.getExecutableName(), 0);
        projectDetailPage.checkExecutableScheduleNumber(processName,
                Executables.SUCCESSFUL_GRAPH.getExecutableName(), 1);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkProcessScheduleList() {
        openProjectDetailPage(getWorkingProject());
        String processName = "Check Process Schedule List";
        deployInProjectDetailPage(DeployPackages.BASIC, processName);
        ScheduleBuilder successfulScheduleBuilder =
                new ScheduleBuilder().setProcessName(processName).setExecutable(
                        Executables.SUCCESSFUL_GRAPH);
        createAndAssertSchedule(successfulScheduleBuilder);

        projectDetailPage.assertScheduleStatus(successfulScheduleBuilder.getScheduleName(),
                ScheduleStatus.UNSCHEDULED);
        scheduleDetail.manualRun();

        scheduleDetail.isInRunningState();
        projectDetailPage.assertScheduleStatus(successfulScheduleBuilder.getScheduleName(),
                ScheduleStatus.RUNNING);

        scheduleDetail.assertSuccessfulExecution();
        projectDetailPage.assertScheduleStatus(successfulScheduleBuilder.getScheduleName(),
                ScheduleStatus.OK);

        ScheduleBuilder failedScheduleBuilder =
                new ScheduleBuilder().setProcessName(processName).setExecutable(
                        Executables.FAILED_GRAPH);
        createAndAssertSchedule(failedScheduleBuilder);

        scheduleDetail.manualRun();
        scheduleDetail.assertFailedExecution(failedScheduleBuilder.getExecutable());
        projectDetailPage.assertScheduleStatus(failedScheduleBuilder.getScheduleName(),
                ScheduleStatus.ERROR);

        scheduleDetail.disableSchedule();
        projectDetailPage.assertScheduleStatus(failedScheduleBuilder.getScheduleName(),
                ScheduleStatus.DISABLED);
    }

}
