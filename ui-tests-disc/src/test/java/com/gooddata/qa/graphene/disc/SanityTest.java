package com.gooddata.qa.graphene.disc;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import javax.mail.MessagingException;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.disc.ExecutionDetails;
import com.gooddata.qa.graphene.entity.disc.NotificationBuilder;
import com.gooddata.qa.graphene.entity.disc.NotificationParameters;
import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder.Parameter;
import com.gooddata.qa.graphene.enums.disc.DeployPackages;
import com.gooddata.qa.graphene.enums.disc.NotificationEvents;
import com.gooddata.qa.graphene.enums.disc.OverviewProjectStates;
import com.gooddata.qa.graphene.enums.disc.ProjectStateFilters;
import com.gooddata.qa.graphene.enums.disc.ScheduleCronTimes;
import com.gooddata.qa.graphene.enums.disc.ScheduleStatus;
import com.gooddata.qa.graphene.enums.disc.DeployPackages.Executables;

public class SanityTest extends AbstractDISC {

    @BeforeClass
    public void initProperties() {
        zipFilePath = testParams.loadProperty("zipFilePath") + testParams.getFolderSeparator();
        projectTitle = "Disc-test-sanity";
        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");
        userProfileId =
                testParams.loadProperty("userProfileUri").substring(
                        testParams.loadProperty("userProfileUri").lastIndexOf("/") + 1);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"deploy"})
    public void deployCloudConnectInProjectsPage() {
        try {
            deployInProjectsPage(getProjects(), DeployPackages.CLOUDCONNECT,
                    "CloudConnect - Projects List Page");
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"deploy"})
    public void deployCloudConnectInProjectDetailPage() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT,
                    "CloudConnect - Project Detail Page");
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"deploy"})
    public void redeployProcessWithDifferentPackage() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());
            String processName = "Redeploy process with different package";
            deployInProjectDetailPage(DeployPackages.EXECUTABLES_GRAPH, processName);
            redeployProcess(processName, DeployPackages.CLOUDCONNECT, processName);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void createScheduleWithCustomInput() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Create Schedule with Custom Input";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);

            List<Parameter> paramList =
                    Arrays.asList(
                            new Parameter().setParamName("param").setParamValue("value"),
                            new Parameter().setParamName("secure param")
                                    .setParamValue("secure value").setSecureParam());
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.DWHS2)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES)
                            .setParameters(paramList).isConfirm();
            createAndAssertSchedule(scheduleBuilder);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void checkManualExecution() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Check Manual Execution";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.manualRun();
            scheduleDetail.assertSuccessfulExecution();
            scheduleDetail.assertManualRunExecution();
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void checkScheduleAutoRun() {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

            String processName = "Check Auto Run Schedule";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_EVERYHOUR)
                            .setMinuteInHour("${minute}");
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.waitForAutoRunSchedule(scheduleBuilder.getCronTimeBuilder());
            scheduleDetail.assertSuccessfulExecution();
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"notification"})
    public void checkNotification() throws InterruptedException, ParseException,
            MessagingException, IOException, JSONException {
        openProjectDetailByUrl(getWorkingProject().getProjectId());
        String processName = "Check Notification";
        try {
            deployInProjectDetailPage(DeployPackages.BASIC, processName);

            String subject = notificationSubject + Calendar.getInstance().getTime();
            String message =
                    "params.PROJECT=${params.PROJECT}" + "*"
                            + "params.FINISH_TIME=${params.FINISH_TIME}";

            NotificationBuilder notificationInfo =
                    new NotificationBuilder().setProcessName(processName).setEmail(imapUser)
                            .setSubject(subject).setMessage(message)
                            .setEvent(NotificationEvents.SUCCESS);
            createAndAssertNotification(notificationInfo);

            openProjectDetailByUrl(getWorkingProject().getProjectId());
            createAndAssertSchedule(new ScheduleBuilder().setProcessName(processName)
                    .setExecutable(Executables.SUCCESSFUL_GRAPH)
                    .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setHourInDay("23")
                    .setMinuteInHour("59"));
            scheduleDetail.manualRun();
            scheduleDetail.assertSuccessfulExecution();

            ExecutionDetails executionDetails = new ExecutionDetails().setStatus(ScheduleStatus.OK);
            getExecutionInfoFromGreyPage(executionDetails, scheduleDetail.getLastExecutionLogLink());

            NotificationParameters expectedParams =
                    new NotificationParameters().setProjectId(testParams.getProjectId())
                            .setExecutionDetails(executionDetails);
            waitForNotification(subject, expectedParams);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"project-detail"})
    public void checkGoToDashboardsLink() {
        openProjectDetailPage(getWorkingProject());
        waitForElementVisible(projectDetailPage.getRoot());
        projectDetailPage.goToDashboards();
        waitForDashboardPageLoaded(browser);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"project-overview"})
    public void checkFailedOverviewNumber() {
        checkOverviewStateNumber(OverviewProjectStates.FAILED);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"project-overview"})
    public void checkRunningOverviewNumber() {
        checkOverviewStateNumber(OverviewProjectStates.RUNNING);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"project-overview"})
    public void checkSuccessfulOverviewNumber() {
        checkOverviewStateNumber(OverviewProjectStates.SUCCESSFUL);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"projects-page"})
    public void checkProjectFilterOptions() {
        openUrl(DISC_PROJECTS_PAGE_URL);
        waitForElementVisible(discProjectsPage.getRoot());
        discProjectsPage.checkProjectFilterOptions();
        assertEquals(ProjectStateFilters.ALL.getOption(), discProjectsPage
                .getSelectedFilterOption().getText());
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"projects-page"})
    public void checkFailedProjectsFilterOption() {
        try {
            String processName = "Check Failed Projects Filter Option";
            prepareDataForProjectsPageTest(ProjectStateFilters.FAILED, getWorkingProject(),
                    processName, Executables.FAILED_GRAPH);

            openUrl(DISC_PROJECTS_PAGE_URL);
            waitForElementVisible(discProjectsPage.getRoot());
            discProjectsPage.checkProjectFilter(ProjectStateFilters.FAILED, getProjects());
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"projects-page"})
    public void checkSuccessfulProjectsFilterOptions() {
        try {
            String processName = "Check Successful Projects Filter Option";
            prepareDataForProjectsPageTest(ProjectStateFilters.SUCCESSFUL, getWorkingProject(),
                    processName, Executables.SUCCESSFUL_GRAPH);

            openUrl(DISC_PROJECTS_PAGE_URL);
            waitForElementVisible(discProjectsPage.getRoot());
            discProjectsPage.checkProjectFilter(ProjectStateFilters.SUCCESSFUL, getProjects());
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"projects-page"})
    public void checkRunningProjectsFilterOptions() {
        try {
            String processName = "Check Running Projects Filter Option";
            prepareDataForProjectsPageTest(ProjectStateFilters.RUNNING, getWorkingProject(),
                    processName, Executables.LONG_TIME_RUNNING_GRAPH);

            openUrl(DISC_PROJECTS_PAGE_URL);
            waitForElementVisible(discProjectsPage.getRoot());
            discProjectsPage.checkProjectFilter(ProjectStateFilters.RUNNING, getProjects());
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"projects-page"})
    public void checkSearchProjectByName() {
        openUrl(DISC_PROJECTS_PAGE_URL);
        waitForElementVisible(discProjectsPage.getRoot());
        discProjectsPage.searchProjectByName(projectTitle);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"projects-page"})
    public void checkSearchProjectById() {
        openUrl(DISC_PROJECTS_PAGE_URL);
        waitForElementVisible(discProjectsPage.getRoot());
        discProjectsPage.searchProjectById(getWorkingProject());
    }
}
