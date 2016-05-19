package com.gooddata.qa.graphene.disc;

import static com.gooddata.qa.utils.http.RestUtils.getResource;
import static org.testng.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.IOException;
import java.util.Calendar;

import org.apache.http.ParseException;
import org.springframework.http.HttpStatus;

import javax.mail.MessagingException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.disc.NotificationBuilder;
import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.enums.disc.DeployPackages;
import com.gooddata.qa.graphene.enums.disc.DeployPackages.Executables;
import com.gooddata.qa.graphene.enums.disc.NotificationEvents;
import com.gooddata.qa.graphene.enums.disc.OverviewProjectStates;
import com.gooddata.qa.graphene.enums.disc.ProjectStateFilters;
import com.gooddata.qa.graphene.enums.disc.ScheduleCronTimes;
import com.gooddata.qa.utils.mail.ImapClient;

public class SanityTest extends AbstractOverviewProjectsTest {

    @BeforeClass
    public void initProperties() {
        projectTitle = "Disc-test-sanity";
        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"deploy"})
    public void deployCloudConnectInProjectsPage() {
        try {
            deployInProjectsPage(DeployPackages.CLOUDCONNECT, "CloudConnect - Projects List Page",
                    testParams.getProjectId());
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"deploy"})
    public void deployCloudConnectInProjectDetailPage() {
        try {
            openProjectDetailPage(testParams.getProjectId());
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, "CloudConnect - Project Detail Page");
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"deploy"})
    public void redeployProcessWithDifferentPackage() {
        try {
            openProjectDetailPage(testParams.getProjectId());
            String processName = "Redeploy process with different package";
            deployInProjectDetailPage(DeployPackages.EXECUTABLES_GRAPH, processName);
            redeployProcess(processName, DeployPackages.CLOUDCONNECT, processName);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void createAndAssertSchedule() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Create Schedule with Custom Input";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.DWHS2)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES);
            createAndAssertSchedule(scheduleBuilder);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void checkManualExecution() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Check Manual Execution";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.manualRun();
            assertSuccessfulExecution();
            assertLastExecutionDetail();
            assertTrue(scheduleDetail.isLastExecutionManualIconDisplay());
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    /**
     * this testcase is used to check that a ruby process could be executed well in DISC.
     * @throws IOException 
     * @throws ParseException 
     */
    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void checkRubyExecution() throws ParseException, IOException {
        try {
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Check Ruby Execution";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.RUBY3)
                            .setCronTime(ScheduleCronTimes.CRON_15_MINUTES);
            prepareScheduleWithBasicPackage(scheduleBuilder);

            scheduleDetail.manualRun();
            assertSuccessfulExecution();
            assertTrue(scheduleDetail.isLastExecutionManualIconDisplay());
            assertThat(getResource(getRestApiClient(), scheduleDetail.getLastExecutionLogLink(), HttpStatus.OK),
                    containsString("Hello World"));
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedule"})
    public void checkScheduleAutoRun() {
        try {
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Check Auto Run Schedule";
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_EVERYHOUR).setMinuteInHour("${minute}");
            prepareScheduleWithBasicPackage(scheduleBuilder);

            assertTrue(scheduleDetail.waitForAutoRunSchedule(scheduleBuilder.getCronTimeBuilder()),
                    "Schedule is not run automatically well!");
            assertSuccessfulExecution();
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"notification"})
    public void createAndAssertNotification() throws MessagingException {
        openProjectDetailPage(testParams.getProjectId());
        String processName = "Check Notification";
        try {
            deployInProjectDetailPage(DeployPackages.BASIC, processName);

            final String subject = "Notification Subject_" + Calendar.getInstance().getTime();
            String message = "Notification message.";

            NotificationBuilder notificationInfo =
                    new NotificationBuilder().setProcessName(processName).setEmail(imapUser).setSubject(subject)
                            .setMessage(message).setEvent(NotificationEvents.SUCCESS);
            createAndAssertNotification(notificationInfo);

            openProjectDetailPage(testParams.getProjectId());
            browser.navigate().refresh();
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.SUCCESSFUL_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_EVERYHOUR);
            createSchedule(scheduleBuilder);
            scheduleDetail.manualRun();
            assertSuccessfulExecution();

            final ImapClient imapClient = new ImapClient(imapHost, imapUser, imapPassword);
            getNotification(imapClient, subject);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"project-overview"})
    public void checkFailedOverviewNumber() {
        try {
            checkOverviewStateNumber(OverviewProjectStates.FAILED);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"project-overview"})
    public void checkRunningOverviewNumber() {
        try {
            checkOverviewStateNumber(OverviewProjectStates.RUNNING);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"project-overview"})
    public void checkSuccessfulOverviewNumber() {
        try {
            checkOverviewStateNumber(OverviewProjectStates.SUCCESSFUL);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"projects-page"})
    public void checkFailedProjectsFilterOption() {
        try {
            checkProjectsFilter(ProjectStateFilters.FAILED);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"projects-page"})
    public void checkSuccessfulProjectsFilterOptions() {
        try {
            checkProjectsFilter(ProjectStateFilters.SUCCESSFUL);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"projects-page"})
    public void checkRunningProjectsFilterOptions() {
        try {
            checkProjectsFilter(ProjectStateFilters.RUNNING);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"projects-page"})
    public void checkSearchProjectByName() {
        checkSearchWorkingProjectByName();
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"projects-page"})
    public void checkSearchProjectById() {
        checkSearchWorkingProjectById();
    }
}
