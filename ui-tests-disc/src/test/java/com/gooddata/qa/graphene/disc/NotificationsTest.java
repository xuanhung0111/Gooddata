package com.gooddata.qa.graphene.disc;

import java.io.IOException;
import java.util.Calendar;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.disc.ExecutionDetails;
import com.gooddata.qa.graphene.entity.disc.NotificationBuilder;
import com.gooddata.qa.graphene.entity.disc.NotificationParameters;
import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.enums.UserRoles;
import com.gooddata.qa.graphene.enums.disc.NotificationEvents;
import com.gooddata.qa.graphene.enums.disc.DeployPackages;
import com.gooddata.qa.graphene.enums.disc.ScheduleStatus;
import com.gooddata.qa.graphene.enums.disc.ScheduleCronTimes;
import com.gooddata.qa.graphene.enums.disc.DeployPackages.Executables;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

public class NotificationsTest extends AbstractNotificationTest {

    @BeforeClass
    public void initProperties() {
        zipFilePath = testParams.loadProperty("zipFilePath") + testParams.getFolderSeparator();
        projectTitle = "Disc-test-notification";
        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");
        imapUserUri = testParams.loadProperty("imap.userUri");
        userProfileId =
                testParams.loadProperty("userProfileUri").substring(
                        testParams.loadProperty("userProfileUri").lastIndexOf("/") + 1);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"notification"})
    public void prepareDataForSucessEvent() {
        openProjectDetailByUrl(getWorkingProject().getProjectId());
        deployInProjectDetailPage(DeployPackages.BASIC, SUCCESS_NOTIFICATION_TEST_PROCESS);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"notification"})
    public void prepareDataForFailureEvent() {
        openProjectDetailByUrl(getWorkingProject().getProjectId());
        deployInProjectDetailPage(DeployPackages.BASIC, FAILURE_NOTIFICATION_TEST_PROCESS);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"notification"})
    public void prepareDataForNotificationFormChecking() {
        openProjectDetailByUrl(getWorkingProject().getProjectId());
        deployInProjectDetailPage(DeployPackages.BASIC, NOTIFICATION_TEST_PROCESS);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"notification"})
    public void prepareDataForCustomEvent() {
        openProjectDetailByUrl(getWorkingProject().getProjectId());
        deployInProjectDetailPage(DeployPackages.CTL_EVENT, CUSTOM_NOTIFICATION_TEST_PROCESS);
    }

    @Test(dependsOnMethods = {"prepareDataForSucessEvent"}, groups = {"notification"})
    public void createNotificationForSuccessEvent() {
        NotificationBuilder notificationInfo =
                new NotificationBuilder().setProcessName(SUCCESS_NOTIFICATION_TEST_PROCESS)
                        .setEmail(imapUser).setSubject(successNotificationSubject)
                        .setMessage(successNotificationMessage)
                        .setEvent(NotificationEvents.SUCCESS);
        createAndAssertNotification(notificationInfo);
    }

    @Test(dependsOnMethods = {"prepareDataForFailureEvent"}, groups = {"notification"})
    public void createNotificationForFailureEvent() {
        NotificationBuilder notificationInfo =
                new NotificationBuilder().setProcessName(FAILURE_NOTIFICATION_TEST_PROCESS)
                        .setEmail(imapUser).setSubject(failureNotificationSubject)
                        .setMessage(failureNotificationMessage)
                        .setEvent(NotificationEvents.FAILURE);
        createAndAssertNotification(notificationInfo);
    }

    @Test(dependsOnMethods = {"prepareDataForSucessEvent"}, groups = {"notification"})
    public void createNotificationForProcessStartedEvent() {
        NotificationBuilder notificationInfo =
                new NotificationBuilder().setProcessName(SUCCESS_NOTIFICATION_TEST_PROCESS)
                        .setEmail(imapUser).setSubject(processStartedNotificationSubject)
                        .setMessage(processStartedNotificationMessage)
                        .setEvent(NotificationEvents.PROCESS_STARTED);
        createAndAssertNotification(notificationInfo);
    }

    @Test(dependsOnMethods = {"prepareDataForSucessEvent"}, groups = {"notification"})
    public void createNotificationForProcessScheduledEvent() {
        NotificationBuilder notificationInfo =
                new NotificationBuilder().setProcessName(SUCCESS_NOTIFICATION_TEST_PROCESS)
                        .setEmail(imapUser).setSubject(processScheduledNotificationSubject)
                        .setMessage(processScheduledNotificationMessage)
                        .setEvent(NotificationEvents.PROCESS_SCHEDULED);
        createAndAssertNotification(notificationInfo);
    }

    @Test(dependsOnMethods = {"prepareDataForCustomEvent"}, groups = {"notification"})
    public void createNotificationForCustomEvent() {
        NotificationBuilder notificationInfo =
                new NotificationBuilder().setProcessName(CUSTOM_NOTIFICATION_TEST_PROCESS)
                        .setEmail(imapUser).setSubject(customEventNotificationSubject)
                        .setMessage(customEventNotificationMessage)
                        .setEvent(NotificationEvents.CUSTOM_EVENT)
                        .setCustomEventName("welcomeEvent");
        createAndAssertNotification(notificationInfo);
    }

    @Test(dependsOnMethods = {"createNotificationForSuccessEvent"}, groups = {"notification"})
    public void successEventTrigger() {
        openProjectDetailByUrl(getWorkingProject().getProjectId());
        ScheduleBuilder scheduleBuilder =
                new ScheduleBuilder().setProcessName(SUCCESS_NOTIFICATION_TEST_PROCESS)
                        .setExecutable(Executables.SUCCESSFUL_GRAPH)
                        .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setHourInDay("23")
                        .setMinuteInHour("59");
        createSchedule(scheduleBuilder);
        String scheduleUrl = browser.getCurrentUrl();

        scheduleDetail.manualRun();
        scheduleDetail.assertSuccessfulExecution();
        successProcessUri = getProcessUri(scheduleUrl);
        successfulScheduleId = scheduleUrl.substring(scheduleUrl.lastIndexOf("/") + 1);
        getExecutionInfoFromGreyPage(successfulExecutionDetails,
                scheduleDetail.getLastExecutionLogLink());
    }

    @Test(dependsOnMethods = {"createNotificationForFailureEvent"}, groups = {"notification"})
    public void failureEventTrigger() {
        openProjectDetailByUrl(getWorkingProject().getProjectId());
        ScheduleBuilder scheduleBuilder =
                new ScheduleBuilder().setProcessName(FAILURE_NOTIFICATION_TEST_PROCESS)
                        .setExecutable(Executables.FAILED_GRAPH)
                        .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setHourInDay("23")
                        .setMinuteInHour("59");
        createSchedule(scheduleBuilder);

        scheduleDetail.manualRun();
        scheduleDetail.assertFailedExecution(scheduleBuilder.getExecutable());

        String scheduleUrl = browser.getCurrentUrl();
        failureProcessUri = getProcessUri(scheduleUrl);
        failedScheduleId = scheduleUrl.substring(scheduleUrl.lastIndexOf("/") + 1);
        getExecutionInfoFromGreyPage(failedExecutionDetails,
                scheduleDetail.getLastExecutionLogLink());
    }

    @Test(dependsOnMethods = {"createNotificationForCustomEvent"}, groups = {"notification"})
    public void customEventTrigger() {
        openProjectDetailByUrl(getWorkingProject().getProjectId());
        ScheduleBuilder scheduleBuilder =
                new ScheduleBuilder().setProcessName(CUSTOM_NOTIFICATION_TEST_PROCESS)
                        .setExecutable(Executables.CTL_GRAPH)
                        .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setHourInDay("23")
                        .setMinuteInHour("59");
        createSchedule(scheduleBuilder);

        scheduleDetail.manualRun();
        scheduleDetail.assertSuccessfulExecution();
    }

    @Test(dependsOnMethods = {"successEventTrigger"}, groups = {"notification"})
    public void checkSuccessMessage() {
        NotificationParameters expectedParams =
                new NotificationParameters()
                        .setProjectId(testParams.getProjectId())
                        .setUser(userProfileId)
                        .setUserEmail(testParams.getUser())
                        .setProcessUri(successProcessUri)
                        .setProcessName(SUCCESS_NOTIFICATION_TEST_PROCESS)
                        .setExecutable(
                                DeployPackages.BASIC.getPackageRootFolder()
                                        + Executables.SUCCESSFUL_GRAPH.getExecutablePath())
                        .setScheduleName(Executables.SUCCESSFUL_GRAPH.getExecutableName())
                        .setScheduleId(successfulScheduleId)
                        .setExecutionDetails(successfulExecutionDetails);
        checkNotification(NotificationEvents.SUCCESS, expectedParams);
    }

    @Test(dependsOnMethods = {"successEventTrigger", "createNotificationForProcessStartedEvent"},
            groups = {"notification"})
    public void checkProcessStartedSuccessMessage() {
        NotificationParameters expectedParams =
                new NotificationParameters()
                        .setProjectId(testParams.getProjectId())
                        .setUser(userProfileId)
                        .setUserEmail(testParams.getUser())
                        .setProcessUri(successProcessUri)
                        .setProcessName(SUCCESS_NOTIFICATION_TEST_PROCESS)
                        .setExecutable(
                                DeployPackages.BASIC.getPackageRootFolder()
                                        + Executables.SUCCESSFUL_GRAPH.getExecutablePath())
                        .setScheduleName(Executables.SUCCESSFUL_GRAPH.getExecutableName())
                        .setScheduleId(successfulScheduleId)
                        .setExecutionDetails(successfulExecutionDetails);
        checkNotification(NotificationEvents.PROCESS_STARTED, expectedParams);
    }

    @Test(dependsOnMethods = {"successEventTrigger", "createNotificationForProcessScheduledEvent"},
            groups = {"notification"})
    public void checkProcessScheduledSuccessMessage() {
        NotificationParameters expectedParams =
                new NotificationParameters()
                        .setProjectId(testParams.getProjectId())
                        .setUser(userProfileId)
                        .setUserEmail(testParams.getUser())
                        .setProcessUri(successProcessUri)
                        .setExecutable(
                                DeployPackages.BASIC.getPackageRootFolder()
                                        + Executables.SUCCESSFUL_GRAPH.getExecutablePath())
                        .setScheduleName(Executables.SUCCESSFUL_GRAPH.getExecutableName())
                        .setScheduleId(successfulScheduleId)
                        .setExecutionDetails(successfulExecutionDetails);
        checkNotification(NotificationEvents.PROCESS_SCHEDULED, expectedParams);
    }

    @Test(dependsOnMethods = {"failureEventTrigger"}, groups = {"notification"})
    public void checkFailureMessage() {
        NotificationParameters expectedParams =
                new NotificationParameters()
                        .setProjectId(testParams.getProjectId())
                        .setUser(userProfileId)
                        .setUserEmail(testParams.getUser())
                        .setProcessUri(failureProcessUri)
                        .setProcessName(FAILURE_NOTIFICATION_TEST_PROCESS)
                        .setExecutable(
                                DeployPackages.BASIC.getPackageRootFolder()
                                        + Executables.FAILED_GRAPH.getExecutablePath())
                        .setScheduleName(Executables.FAILED_GRAPH.getExecutableName())
                        .setScheduleId(failedScheduleId)
                        .setExecutionDetails(failedExecutionDetails);
        checkNotification(NotificationEvents.FAILURE, expectedParams);
    }

    @Test(dependsOnMethods = {"customEventTrigger"}, groups = {"notification"})
    public void checkCustomEventMessage() {
        NotificationParameters expectedParams =
                new NotificationParameters().setCustomParam("World");
        checkNotification(NotificationEvents.CUSTOM_EVENT, expectedParams);
    }

    @Test(dependsOnMethods = {"prepareDataForNotificationFormChecking"}, groups = {"notification"})
    public void checkEmptyNotificationFieldsError() {
        openProjectDetailByUrl(getWorkingProject().getProjectId());
        projectDetailPage.getNotificationButton(NOTIFICATION_TEST_PROCESS).click();
        waitForElementVisible(discNotificationRules.getRoot());
        discNotificationRules.clickOnAddNotificationButton();
        int notificationIndex = discNotificationRules.getNotificationNumber() - 1;
        discNotificationRules.checkEmptyNotificationFields(notificationIndex);
    }

    @Test(dependsOnMethods = {"prepareDataForNotificationFormChecking"}, groups = {"notification"})
    public void checkEmailFieldError() {
        openProjectDetailByUrl(getWorkingProject().getProjectId());
        projectDetailPage.getNotificationButton(NOTIFICATION_TEST_PROCESS).click();
        waitForElementVisible(discNotificationRules.getRoot());
        discNotificationRules.clickOnAddNotificationButton();
        int notificationIndex = discNotificationRules.getNotificationNumber() - 1;
        discNotificationRules.checkOnlyOneEmailError(notificationIndex);
    }

    @Test(dependsOnMethods = {"prepareDataForNotificationFormChecking"}, groups = {"notification"})
    public void checkAvailableParams() {
        openProjectDetailByUrl(getWorkingProject().getProjectId());
        projectDetailPage.getNotificationButton(NOTIFICATION_TEST_PROCESS).click();
        waitForElementVisible(discNotificationRules.getRoot());
        discNotificationRules.clickOnAddNotificationButton();
        int notificationIndex = discNotificationRules.getNotificationNumber() - 1;

        System.out.println("Available params for Success Notification: "
                + discNotificationRules.getAvailableParams());
        assertEquals(discNotificationRules.getAvailableParams(), successNotificationParams);

        discNotificationRules.setNotificationEvent(notificationIndex,
                NotificationEvents.PROCESS_SCHEDULED);
        System.out.println("Available params for Process Scheduled Notification: "
                + discNotificationRules.getAvailableParams());
        assertEquals(discNotificationRules.getAvailableParams(), processScheduledNotificationParams);

        discNotificationRules.setNotificationEvent(notificationIndex,
                NotificationEvents.PROCESS_STARTED);
        System.out.println("Available params for Process Started Notification: "
                + discNotificationRules.getAvailableParams());
        assertEquals(discNotificationRules.getAvailableParams(), processStartedNotificationParams);

        discNotificationRules.setNotificationEvent(notificationIndex, NotificationEvents.FAILURE);
        System.out.println("Available params for Failure Notification: "
                + discNotificationRules.getAvailableParams());
        assertEquals(discNotificationRules.getAvailableParams(), failureNotificationParams);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"notification"})
    public void checkNotificationNumber() {
        String processName = "Check Notification Number";
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());
            deployInProjectDetailPage(DeployPackages.BASIC, processName);

            NotificationBuilder notificationInfo =
                    new NotificationBuilder().setProcessName(processName).setEmail(imapUser)
                            .setSubject(notificationSubject).setMessage(notificationMessage)
                            .setEvent(NotificationEvents.SUCCESS);
            checkNotificationNumber(0, processName);
            createAndAssertNotification(notificationInfo);
            checkNotificationNumber(1, processName);
            createAndAssertNotification(notificationInfo);
            checkNotificationNumber(2, processName);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"prepareDataForNotificationFormChecking"}, groups = {"notification"})
    public void checkCancelCreateNotification() {
        openProjectDetailByUrl(getWorkingProject().getProjectId());
        String notificationNumber =
                projectDetailPage.getNotificationButton(NOTIFICATION_TEST_PROCESS).getText();

        NotificationBuilder notificationBuilder =
                new NotificationBuilder().setProcessName(NOTIFICATION_TEST_PROCESS)
                        .setEmail(imapUser).setSubject(notificationSubject)
                        .setMessage(notificationMessage).setSaved(false);
        createNotification(notificationBuilder);

        openProjectDetailPage(getWorkingProject());
        assertEquals(projectDetailPage.getNotificationButton(NOTIFICATION_TEST_PROCESS).getText(),
                notificationNumber);
    }

    @Test(dependsOnMethods = {"prepareDataForNotificationFormChecking"}, groups = {"notification"})
    public void checkDeleteNotification() {
        openProjectDetailByUrl(getWorkingProject().getProjectId());
        projectDetailPage.getNotificationButton(NOTIFICATION_TEST_PROCESS).click();
        waitForElementVisible(discNotificationRules.getRoot());
        int notificationNumber = discNotificationRules.getNotificationNumber();

        NotificationBuilder notificationInfo =
                new NotificationBuilder().setProcessName(NOTIFICATION_TEST_PROCESS)
                        .setEmail(imapUser).setSubject(notificationSubject)
                        .setMessage(notificationMessage).setEvent(NotificationEvents.FAILURE)
                        .setSaved(true);
        createAndAssertNotification(notificationInfo);

        deleteNotification(notificationInfo);
        checkNotificationNumber(notificationNumber, NOTIFICATION_TEST_PROCESS);
    }

    @Test(dependsOnMethods = {"prepareDataForNotificationFormChecking"}, groups = {"notification"})
    public void checkCancelDeleteNotification() {
        openProjectDetailByUrl(getWorkingProject().getProjectId());
        projectDetailPage.getNotificationButton(NOTIFICATION_TEST_PROCESS).click();
        waitForElementVisible(discNotificationRules.getRoot());
        int notificationNumber = discNotificationRules.getNotificationNumber();

        NotificationBuilder notificationInfo =
                new NotificationBuilder().setProcessName(NOTIFICATION_TEST_PROCESS)
                        .setEmail(imapUser).setSubject(notificationSubject)
                        .setMessage(notificationMessage).setEvent(NotificationEvents.FAILURE)
                        .setSaved(true);
        createAndAssertNotification(notificationInfo);
        notificationNumber++;

        deleteNotification(notificationInfo.setSaved(false));
        checkNotificationNumber(notificationNumber, NOTIFICATION_TEST_PROCESS);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"notification"})
    public void checkEditNotification() {
        openProjectDetailByUrl(getWorkingProject().getProjectId());
        String processName = "Check Edit Notification";
        try {
            deployInProjectDetailPage(DeployPackages.BASIC, processName);

            String subject = notificationSubject + Calendar.getInstance().getTime();
            String message =
                    "params.PROJECT=${params.PROJECT}" + "*"
                            + "params.FINISH_TIME=${params.FINISH_TIME}";

            NotificationBuilder notificationInfo =
                    new NotificationBuilder().setProcessName(processName)
                            .setEmail(testParams.getUser()).setSubject(subject).setMessage(message)
                            .setEvent(NotificationEvents.SUCCESS);
            createAndAssertNotification(notificationInfo);

            editNotification(notificationInfo.setEmail(imapUser)
                    .setSubject(subject + Calendar.getInstance().getTime())
                    .setEvent(NotificationEvents.PROCESS_STARTED));

            openProjectDetailPage(getWorkingProject());
            createSchedule(new ScheduleBuilder().setProcessName(processName)
                    .setExecutable(Executables.SUCCESSFUL_GRAPH)
                    .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setHourInDay("23")
                    .setMinuteInHour("59"));
            scheduleDetail.manualRun();
            scheduleDetail.assertSuccessfulExecution();

            NotificationParameters expectedParams =
                    new NotificationParameters().setProjectId(testParams.getProjectId())
                            .setExecutionDetails(new ExecutionDetails());
            waitForNotification(notificationInfo.getSubject(), expectedParams);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"notification"})
    public void checkCancelEditNotification() {
        openProjectDetailByUrl(getWorkingProject().getProjectId());
        String processName = "Check Cancel Edit Notification";
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

            NotificationBuilder editedNotificationInfo =
                    new NotificationBuilder().setProcessName(processName).setEmail(imapUser)
                            .setSubject(notificationSubject + Calendar.getInstance().getTime())
                            .setMessage(message).setEvent(NotificationEvents.PROCESS_STARTED)
                            .setEmail(testParams.getUser()).setSaved(false);
            editNotification(editedNotificationInfo);
            assertNotification(notificationInfo);

            openProjectDetailPage(getWorkingProject());
            createSchedule(new ScheduleBuilder().setProcessName(processName)
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
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"notification"})
    public void checkRepeatedDataLoadingFailureNotification() throws ParseException, IOException, JSONException {
        try {
            addUserToProject(imapUserUri, UserRoles.ADMIN);
            logout();

            signInAtUI(imapUser, imapPassword);
            openProjectDetailByUrl(getWorkingProject().getProjectId());
            String processName =
                    "Check Repeated Failures Notification"
                            + Calendar.getInstance().getTimeInMillis();
            deployInProjectDetailPage(DeployPackages.BASIC, processName);
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.SHORT_TIME_FAILED_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setHourInDay("23")
                            .setMinuteInHour("59");
            createSchedule(scheduleBuilder);
            scheduleBuilder.setScheduleUrl(browser.getCurrentUrl());

            scheduleDetail.repeatManualRunFailedSchedule(getNumberOfFailuresToSendMail(),
                    scheduleBuilder.getExecutable());
            waitForRepeatedFailuresEmail(scheduleBuilder);

            scheduleDetail.repeatManualRunFailedSchedule(getNumberOfFailuresToDisableSchedule()
                    - getNumberOfFailuresToSendMail(), scheduleBuilder.getExecutable());
            scheduleBuilder.setEnabled(false);
            waitForRepeatedFailuresEmail(scheduleBuilder);
        } finally {
            cleanProcessesInWorkingProject();
            logout();
            signInAtUI(testParams.getUser(), testParams.getPassword());
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"notification"})
    public void checkEmptyStateNotificationList() {
        openProjectDetailByUrl(getWorkingProject().getProjectId());
        String processName = "Check Empty State Notification List";
        try {
            deployInProjectDetailPage(DeployPackages.BASIC, processName);
            openProjectDetailPage(getWorkingProject());
            projectDetailPage.getNotificationButton(processName).click();
            waitForElementVisible(discNotificationRules.getRoot());
            assertTrue(discNotificationRules.getEmptyStateMessage().contains(
                    NOTIFICATION_RULES_EMPTY_STATE_MESSAGE));
            System.out.println("Notification Rules Empty State Message: "
                    + discNotificationRules.getEmptyStateMessage());
            discNotificationRules.closeNotificationRulesDialog();
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnGroups = {"notification"}, alwaysRun = true)
    public void deleteProcesses() {
        cleanProcessesInWorkingProject();
    }
}
