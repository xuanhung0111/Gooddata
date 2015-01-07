package com.gooddata.qa.graphene.disc;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.enums.disc.DeployPackages;
import com.gooddata.qa.graphene.enums.disc.ScheduleStatus;
import com.gooddata.qa.graphene.enums.disc.DeployPackages.Executables;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

public class ProjectDetailTests extends AbstractSchedulesTests {

    private String PROJECT_EMPTY_STATE_TITLE =
            "You don’t have any deployed data loading processes.";
    private String PROJECT_EMPTY_STATE_MESSAGE =
            "How to deploy a process? Read Preparing a Data Loading Process article";
    private static final long expectedDownloadedProcessSize = 64000L;

    private String downloadFolder;

    @BeforeClass
    public void initProperties() {
        zipFilePath = testParams.loadProperty("zipFilePath") + testParams.getFolderSeparator();
        downloadFolder =
                testParams.loadProperty("browserDownloadFolder") + testParams.getFolderSeparator();
        projectTitle = "Disc-test-project-detail";
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void checkProjectInfo() {
        try {
            openProjectDetailPage(getWorkingProject());
            String processName = "Check Project Info";
            int processNumber = projectDetailPage.getNumberOfProcesses();
            deployInProjectDetailPage(DeployPackages.BASIC, processName);

            assertEquals(processNumber + 1, projectDetailPage.getNumberOfProcesses(),
                    "The number of processes is incorrect!");
            openProjectDetailPage(getWorkingProject());
            waitForElementVisible(projectDetailPage.getRoot());
            assertEquals(projectTitle, projectDetailPage.getDisplayedProjectTitle());
            assertEquals(testParams.getProjectId(),
                    projectDetailPage.getProjectMetadata("Project ID"));
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void checkProcessInfo() {
        try {
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
                                    browser.getCurrentUrl().lastIndexOf("/"))
                            .replace("processes/", "");
            System.out.println("processID: " + processID);
            assertEquals(processID, projectDetailPage.getProcessMetadata("Process ID"));
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void checkGoToDashboardsLink() {
        openProjectDetailPage(getWorkingProject());
        waitForElementVisible(projectDetailPage.getRoot());
        projectDetailPage.goToDashboards();
        waitForDashboardPageLoaded(browser);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void checkEmptyProjectState() {
        openProjectDetailPage(getWorkingProject());
        waitForElementVisible(projectDetailPage.getRoot());
        assertEquals(PROJECT_EMPTY_STATE_TITLE, projectDetailPage.getProjectEmptyStateTitle());
        assertEquals(PROJECT_EMPTY_STATE_MESSAGE, projectDetailPage.getProjectEmptyStateMessage());
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void downloadProcess() {
        try {
            openProjectDetailPage(getWorkingProject());
            String processName = "Download Process Test";
            deployInProjectDetailPage(DeployPackages.BASIC, processName);

            System.out.println("Download folder: " + downloadFolder);
            projectDetailPage.checkDownloadProcess(processName, downloadFolder,
                    testParams.getProjectId(), expectedDownloadedProcessSize);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void checkSortedProcesses() {
        try {
            openProjectDetailPage(getWorkingProject());
            deployInProjectDetailPage(DeployPackages.BASIC, "Process-A");
            deployInProjectDetailPage(DeployPackages.BASIC, "Process-Z");
            deployInProjectDetailPage(DeployPackages.BASIC, "Process-B");
            deployInProjectDetailPage(DeployPackages.BASIC, "Process-P");

            openProjectDetailPage(getWorkingProject());
            projectDetailPage.checkSortedProcesses();
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void checkSortedProcessesAfterRedeploy() {
        try {
            openProjectDetailPage(getWorkingProject());
            deployInProjectDetailPage(DeployPackages.BASIC, "Process-A");
            deployInProjectDetailPage(DeployPackages.BASIC, "Process-P");
            deployInProjectDetailPage(DeployPackages.BASIC, "Process-R");

            openProjectDetailPage(getWorkingProject());
            redeployProcess("Process-R", DeployPackages.BASIC, "Process-B");

            openProjectDetailPage(getWorkingProject());
            projectDetailPage.checkSortedProcesses();
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void checkDeleteProcess() {
        try {
            openProjectDetailPage(getWorkingProject());
            deployInProjectDetailPage(DeployPackages.BASIC, "Process-A");
            deployInProjectDetailPage(DeployPackages.BASIC, "Process-Z");
            deployInProjectDetailPage(DeployPackages.BASIC, "Process-B");
            deployInProjectDetailPage(DeployPackages.BASIC, "Process-P");

            openProjectDetailPage(getWorkingProject());
            projectDetailPage.deleteProcess("Process-P");
            assertFalse(projectDetailPage.assertIsExistingProcess("Process-P"));
            projectDetailPage.checkSortedProcesses();
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void checkProcessDeleteDialog() {
        try {
            openProjectDetailPage(getWorkingProject());
            deployInProjectDetailPage(DeployPackages.BASIC, "Process-B");
            String processName = "Process-A";
            deployInProjectDetailPage(DeployPackages.BASIC, processName);

            openProjectDetailPage(getWorkingProject());
            projectDetailPage.checkDeleteProcessDialog(processName);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void checkCancelProcessDeleteDialog() {
        try {
            openProjectDetailPage(getWorkingProject());
            String processName = "Process-A";
            deployInProjectDetailPage(DeployPackages.BASIC, processName);

            projectDetailPage.checkCancelDeleteProcess(processName);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void checkExecutableScheduleNumber() {
        try {
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
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void checkProcessScheduleList() {
        try {
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
            scheduleDetail.isInScheduledState();
            projectDetailPage.assertScheduleStatus(successfulScheduleBuilder.getScheduleName(),
                    ScheduleStatus.SCHEDULED);

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
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

}
