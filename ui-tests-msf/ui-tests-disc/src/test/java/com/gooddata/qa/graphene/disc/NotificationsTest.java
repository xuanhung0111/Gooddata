package com.gooddata.qa.graphene.disc;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.commons.lang.SystemUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.disc.ExecutionDetails;
import com.gooddata.qa.graphene.entity.disc.NotificationBuilder;
import com.gooddata.qa.graphene.entity.disc.NotificationParameters;
import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.enums.disc.DeployPackages;
import com.gooddata.qa.graphene.enums.disc.DeployPackages.Executables;
import com.gooddata.qa.graphene.enums.disc.NotificationEvents;
import com.gooddata.qa.graphene.enums.disc.ScheduleCronTimes;
import com.gooddata.qa.graphene.enums.disc.ScheduleStatus;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.disc.NotificationRule;
import com.gooddata.qa.graphene.fragments.greypages.md.obj.ObjectFragment;
import com.gooddata.qa.utils.mail.ImapClient;

public class NotificationsTest extends AbstractDISCTest {

    private static final String NOTIFICATION_TEST_PROCESS = "Notification Test Process";
    private static final String SUCCESS_NOTIFICATION_TEST_PROCESS = "Success Notification Test";
    private static final String FAILURE_NOTIFICATION_TEST_PROCESS = "Failure Notification Test";
    private static final String CUSTOM_NOTIFICATION_TEST_PROCESS = "Custom Event Notification Test";
    private static final String REPEATED_FAILURES_NOTIFICATION_SUBJECT =
            "Repeated data loading failure: \"%s\" process";
    private static final String REPEATED_FAILURES_NOTIFICATION_BODY =
            "Hello, the %s schedule within the \"%s\" process of your \"%s\" GoodData project (id: %s) has failed for the ${numberOfFailures}th time."
                    + " We highly recommend disabling failing schedules until the issues are addressed: Go to"
                    + " the schedule page Click \"Disable\" button If you require assistance with troubleshooting"
                    + " data uploading, please visit the GoodData Support Portal. At your service, The GoodData Team";
    private static final String REPEATED_FAILURES_NOTIFICATION_MESSAGE_1 =
            "<p>the <a href=\"%s\">%s schedule</a> within the &quot;%s&quot; process of your &quot;%s&quot;"
                    + " GoodData project (id: %s) has failed for the ${numberOfFailures}th time.</p>";
    private static final String REPEATED_FAILURES_NOTIFICATION_MESSAGE_2 =
            "<li>Go to the <a href=\"%s\">schedule page</a></li>";
    private static final String SCHEDULE_DISABLED_NOTIFICATION_SUBJECT = "Schedule disabled: \"%s\" process";
    private static final String SCHEDULE_DISABLED_NOTIFICATION_BODY =
            "Hello, the %s schedule within the \"%s\" process of your \"%s\" GoodData project "
                    + "(id: %s) has been automatically disabled following its ${numberOfFailures}th consecutive failure. "
                    + "To resume scheduled uploads from this process: Go to the schedule page Click "
                    + "\"Enable\" If you require assistance with troubleshooting data uploading, "
                    + "please visit the GoodData Support Portal. At your service, The GoodData Team";
    private static final String SCHEDULE_DISABLED_NOTIFICATION_MESSAGE_1 =
            "<p>the <a href=\"%s\">%s schedule</a> within the &quot;%s&quot; process of your &quot;%s&quot;"
                    + " GoodData project (id: %s) has been <strong>automatically disabled</strong> following its "
                    + "${numberOfFailures}th consecutive failure.</p>";
    private static final String SCHEDULE_DISABLED_NOTIFICATION_MESSAGE_2 =
            "<li>Go to the <a href=\"%s\">schedule page</a></li>";
    private static final String NOTIFICATION_SUPPORT_MESSAGE =
            "<p>If you require assistance with troubleshooting data uploading, please visit the "
                    + "<a href=\"https://help.gooddata.com/display/doc/Troubleshooting+Failed+Schedules\">GoodData Support Portal</a>.</p>";
    private static final String NOTIFICATION_RULES_EMPTY_STATE_MESSAGE =
            "No event (eg. schedule start, finish, fail, etc.) will trigger a notification email.";

    private String successNotificationSubject = "Success Notification_" + Calendar.getInstance().getTime();
    private String successNotificationMessage = "params.PROJECT=${params.PROJECT}" + "*"
            + "params.USER=${params.USER}" + "*" + "params.USER_EMAIL=${params.USER_EMAIL}" + "*"
            + "params.PROCESS_URI=${params.PROCESS_URI}" + "*" + "params.PROCESS_ID=${params.PROCESS_ID}" + "*"
            + "params.PROCESS_NAME=${params.PROCESS_NAME}" + "*" + "params.EXECUTABLE=${params.EXECUTABLE}" + "*"
            + "params.SCHEDULE_ID=${params.SCHEDULE_ID}" + "*" + "params.SCHEDULE_NAME=${params.SCHEDULE_NAME}"
            + "*" + "params.LOG=${params.LOG}" + "*" + "params.START_TIME=${params.START_TIME}" + "*"
            + "params.FINISH_TIME=${params.FINISH_TIME}";
    private String successNotificationParams = "${params.PROJECT}${params.USER}${params.USER_EMAIL}"
            + "${params.PROCESS_URI}${params.PROCESS_ID}${params.PROCESS_NAME}${params.EXECUTABLE}"
            + "${params.SCHEDULE_ID}${params.SCHEDULE_NAME}${params.LOG}${params.START_TIME}"
            + "${params.FINISH_TIME}";
    private String failureNotificationSubject = "Failure Notification_" + Calendar.getInstance().getTime();
    private String failureNotificationMessage = "params.PROJECT=${params.PROJECT}" + "*"
            + "params.USER=${params.USER}" + "*" + "params.USER_EMAIL=${params.USER_EMAIL}" + "*"
            + "params.PROCESS_URI=${params.PROCESS_URI}" + "*" + "params.PROCESS_ID=${params.PROCESS_ID}" + "*"
            + "params.PROCESS_NAME=${params.PROCESS_NAME}" + "*" + "params.EXECUTABLE=${params.EXECUTABLE}" + "*"
            + "params.SCHEDULE_ID=${params.SCHEDULE_ID}" + "*" + "params.SCHEDULE_NAME=${params.SCHEDULE_NAME}"
            + "*" + "params.LOG=${params.LOG}" + "*" + "params.START_TIME=${params.START_TIME}" + "*"
            + "params.FINISH_TIME=${params.FINISH_TIME}" + "*" + "params.ERROR_MESSAGE=${params.ERROR_MESSAGE}";
    private String failureNotificationParams = "${params.PROJECT}${params.USER}${params.USER_EMAIL}"
            + "${params.PROCESS_URI}${params.PROCESS_ID}${params.PROCESS_NAME}${params.EXECUTABLE}"
            + "${params.SCHEDULE_ID}${params.SCHEDULE_NAME}${params.LOG}${params.START_TIME}"
            + "${params.FINISH_TIME}${params.ERROR_MESSAGE}";
    private String processStartedNotificationSubject = "Process Started Notification_"
            + Calendar.getInstance().getTime();
    private String processStartedNotificationMessage = "params.PROJECT=${params.PROJECT}" + "*"
            + "params.USER=${params.USER}" + "*" + "params.USER_EMAIL=${params.USER_EMAIL}" + "*"
            + "params.PROCESS_URI=${params.PROCESS_URI}" + "*" + "params.PROCESS_ID=${params.PROCESS_ID}" + "*"
            + "params.PROCESS_NAME=${params.PROCESS_NAME}" + "*" + "params.EXECUTABLE=${params.EXECUTABLE}" + "*"
            + "params.SCHEDULE_ID=${params.SCHEDULE_ID}" + "*" + "params.SCHEDULE_NAME=${params.SCHEDULE_NAME}"
            + "*" + "params.LOG=${params.LOG}" + "*" + "params.START_TIME=${params.START_TIME}";
    private String processStartedNotificationParams = "${params.PROJECT}${params.USER}${params.USER_EMAIL}"
            + "${params.PROCESS_URI}${params.PROCESS_ID}${params.PROCESS_NAME}${params.EXECUTABLE}"
            + "${params.SCHEDULE_ID}${params.SCHEDULE_NAME}${params.LOG}${params.START_TIME}";
    private String processScheduledNotificationSubject = "Process Scheduled Notification_"
            + Calendar.getInstance().getTime();
    private String processScheduledNotificationMessage = "params.PROJECT=${params.PROJECT}" + "*"
            + "params.USER=${params.USER}" + "*" + "params.USER_EMAIL=${params.USER_EMAIL}" + "*"
            + "params.PROCESS_URI=${params.PROCESS_URI}" + "*" + "params.PROCESS_ID=${params.PROCESS_ID}" + "*"
            + "params.EXECUTABLE=${params.EXECUTABLE}" + "*" + "params.SCHEDULE_ID=${params.SCHEDULE_ID}" + "*"
            + "params.SCHEDULE_NAME=${params.SCHEDULE_NAME}" + "*"
            + "params.SCHEDULED_TIME=${params.SCHEDULED_TIME}";
    private String processScheduledNotificationParams = "${params.PROJECT}${params.USER}${params.USER_EMAIL}"
            + "${params.PROCESS_URI}${params.PROCESS_ID}${params.EXECUTABLE}"
            + "${params.SCHEDULE_ID}${params.SCHEDULE_NAME}${params.SCHEDULED_TIME}";
    private String customEventNotificationSubject = "Custom Event_" + Calendar.getInstance().getTime();
    private String customEventNotificationMessage = "params.CUSTOM=${params.hello}";
    private String notificationSubject = "Notification Subject_";
    private String notificationMessage = "params.PROJECT=${params.PROJECT}";
    private String userProfileId = null;
    private String successProcessUri;
    private String failureProcessUri;
    private String successfulScheduleId;
    private String failedScheduleId;

    @FindBy(tagName = "pre")
    private ObjectFragment objectFragment;

    private ExecutionDetails successfulExecutionDetails = new ExecutionDetails().setStatus(ScheduleStatus.OK);
    private ExecutionDetails failedExecutionDetails = new ExecutionDetails().setStatus(ScheduleStatus.ERROR);

    @BeforeClass(alwaysRun = true)
    public void initProperties() {
        projectTitle = "Disc-test-notification";
        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"notification"})
    public void prepareDataForSucessEvent() {
        openProjectDetailPage(testParams.getProjectId());
        deployInProjectDetailPage(DeployPackages.BASIC, SUCCESS_NOTIFICATION_TEST_PROCESS);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"notification"})
    public void prepareDataForFailureEvent() {
        openProjectDetailPage(testParams.getProjectId());
        deployInProjectDetailPage(DeployPackages.BASIC, FAILURE_NOTIFICATION_TEST_PROCESS);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"notification"})
    public void prepareDataForNotificationFormChecking() {
        openProjectDetailPage(testParams.getProjectId());
        deployInProjectDetailPage(DeployPackages.BASIC, NOTIFICATION_TEST_PROCESS);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"notification"})
    public void prepareDataForCustomEvent() {
        openProjectDetailPage(testParams.getProjectId());
        deployInProjectDetailPage(DeployPackages.CTL_EVENT, CUSTOM_NOTIFICATION_TEST_PROCESS);
    }

    @Test(dependsOnMethods = {"prepareDataForSucessEvent"}, groups = {"notification"})
    public void createNotificationForSuccessEvent() {
        NotificationBuilder notificationInfo =
                new NotificationBuilder().setProcessName(SUCCESS_NOTIFICATION_TEST_PROCESS).setEmail(imapUser)
                        .setSubject(successNotificationSubject).setMessage(successNotificationMessage)
                        .setEvent(NotificationEvents.SUCCESS);
        createAndAssertNotification(notificationInfo);
    }

    @Test(dependsOnMethods = {"prepareDataForFailureEvent"}, groups = {"notification"})
    public void createNotificationForFailureEvent() {
        NotificationBuilder notificationInfo =
                new NotificationBuilder().setProcessName(FAILURE_NOTIFICATION_TEST_PROCESS).setEmail(imapUser)
                        .setSubject(failureNotificationSubject).setMessage(failureNotificationMessage)
                        .setEvent(NotificationEvents.FAILURE);
        createAndAssertNotification(notificationInfo);
    }

    @Test(dependsOnMethods = {"prepareDataForSucessEvent"}, groups = {"notification"})
    public void createNotificationForProcessStartedEvent() {
        NotificationBuilder notificationInfo =
                new NotificationBuilder().setProcessName(SUCCESS_NOTIFICATION_TEST_PROCESS).setEmail(imapUser)
                        .setSubject(processStartedNotificationSubject)
                        .setMessage(processStartedNotificationMessage)
                        .setEvent(NotificationEvents.PROCESS_STARTED);
        createAndAssertNotification(notificationInfo);
    }

    @Test(dependsOnMethods = {"prepareDataForSucessEvent"}, groups = {"notification"})
    public void createNotificationForProcessScheduledEvent() {
        NotificationBuilder notificationInfo =
                new NotificationBuilder().setProcessName(SUCCESS_NOTIFICATION_TEST_PROCESS).setEmail(imapUser)
                        .setSubject(processScheduledNotificationSubject)
                        .setMessage(processScheduledNotificationMessage)
                        .setEvent(NotificationEvents.PROCESS_SCHEDULED);
        createAndAssertNotification(notificationInfo);
    }

    @Test(dependsOnMethods = {"prepareDataForCustomEvent"}, groups = {"notification"})
    public void createNotificationForCustomEvent() {
        NotificationBuilder notificationInfo =
                new NotificationBuilder().setProcessName(CUSTOM_NOTIFICATION_TEST_PROCESS).setEmail(imapUser)
                        .setSubject(customEventNotificationSubject).setMessage(customEventNotificationMessage)
                        .setEvent(NotificationEvents.CUSTOM_EVENT).setCustomEventName("welcomeEvent");
        createAndAssertNotification(notificationInfo);
    }

    @Test(dependsOnMethods = {"createNotificationForSuccessEvent"}, groups = {"notification"})
    public void successEventTrigger() {
        openProjectDetailPage(testParams.getProjectId());
        ScheduleBuilder scheduleBuilder =
                new ScheduleBuilder().setProcessName(SUCCESS_NOTIFICATION_TEST_PROCESS)
                        .setExecutable(Executables.SUCCESSFUL_GRAPH).setCronTime(ScheduleCronTimes.CRON_EVERYDAY)
                        .setHourInDay("23").setMinuteInHour("59");
        createSchedule(scheduleBuilder);
        String scheduleUrl = browser.getCurrentUrl();

        scheduleDetail.manualRun();
        assertSuccessfulExecution();
        successProcessUri = getProcessUri(scheduleUrl);
        successfulScheduleId = scheduleUrl.substring(scheduleUrl.lastIndexOf("/") + 1);
        getExecutionInfoFromGreyPage(successfulExecutionDetails, scheduleDetail.getLastExecutionLogLink());
    }

    @Test(dependsOnMethods = {"createNotificationForFailureEvent"}, groups = {"notification"})
    public void failureEventTrigger() {
        openProjectDetailPage(testParams.getProjectId());
        ScheduleBuilder scheduleBuilder =
                new ScheduleBuilder().setProcessName(FAILURE_NOTIFICATION_TEST_PROCESS)
                        .setExecutable(Executables.FAILED_GRAPH).setCronTime(ScheduleCronTimes.CRON_EVERYDAY)
                        .setHourInDay("23").setMinuteInHour("59");
        createSchedule(scheduleBuilder);

        scheduleDetail.manualRun();
        assertFailedExecution(scheduleBuilder.getExecutable());

        String scheduleUrl = browser.getCurrentUrl();
        failureProcessUri = getProcessUri(scheduleUrl);
        failedScheduleId = scheduleUrl.substring(scheduleUrl.lastIndexOf("/") + 1);
        getExecutionInfoFromGreyPage(failedExecutionDetails, scheduleDetail.getLastExecutionLogLink());
    }

    @Test(dependsOnMethods = {"createNotificationForCustomEvent"}, groups = {"notification"})
    public void customEventTrigger() {
        openProjectDetailPage(testParams.getProjectId());
        ScheduleBuilder scheduleBuilder =
                new ScheduleBuilder().setProcessName(CUSTOM_NOTIFICATION_TEST_PROCESS)
                        .setExecutable(Executables.CTL_GRAPH).setCronTime(ScheduleCronTimes.CRON_EVERYDAY)
                        .setHourInDay("23").setMinuteInHour("59");
        createSchedule(scheduleBuilder);

        scheduleDetail.manualRun();
        assertSuccessfulExecution();
    }

    @Test(dependsOnMethods = {"successEventTrigger"}, groups = {"notification"})
    public void checkSuccessMessage() throws MessagingException {
        NotificationParameters expectedParams =
                new NotificationParameters()
                        .setProjectId(testParams.getProjectId())
                        .setUser(getUserProfileId())
                        .setUserEmail(testParams.getUser())
                        .setProcessUri(successProcessUri)
                        .setProcessName(SUCCESS_NOTIFICATION_TEST_PROCESS)
                        .setExecutable(
                                DeployPackages.BASIC.getPackageRootFolder()
                                        + Executables.SUCCESSFUL_GRAPH.getExecutablePath())
                        .setScheduleName(Executables.SUCCESSFUL_GRAPH.getExecutableName())
                        .setScheduleId(successfulScheduleId).setExecutionDetails(successfulExecutionDetails);
        checkNotification(NotificationEvents.SUCCESS, expectedParams);
    }

    @Test(dependsOnMethods = {"successEventTrigger", "createNotificationForProcessStartedEvent"},
            groups = {"notification"})
    public void checkProcessStartedSuccessMessage() throws MessagingException {
        NotificationParameters expectedParams =
                new NotificationParameters()
                        .setProjectId(testParams.getProjectId())
                        .setUser(getUserProfileId())
                        .setUserEmail(testParams.getUser())
                        .setProcessUri(successProcessUri)
                        .setProcessName(SUCCESS_NOTIFICATION_TEST_PROCESS)
                        .setExecutable(
                                DeployPackages.BASIC.getPackageRootFolder()
                                        + Executables.SUCCESSFUL_GRAPH.getExecutablePath())
                        .setScheduleName(Executables.SUCCESSFUL_GRAPH.getExecutableName())
                        .setScheduleId(successfulScheduleId).setExecutionDetails(successfulExecutionDetails);
        checkNotification(NotificationEvents.PROCESS_STARTED, expectedParams);
    }

    @Test(dependsOnMethods = {"successEventTrigger", "createNotificationForProcessScheduledEvent"},
            groups = {"notification"})
    public void checkProcessScheduledSuccessMessage() throws MessagingException {
        NotificationParameters expectedParams =
                new NotificationParameters()
                        .setProjectId(testParams.getProjectId())
                        .setUser(getUserProfileId())
                        .setUserEmail(testParams.getUser())
                        .setProcessUri(successProcessUri)
                        .setExecutable(
                                DeployPackages.BASIC.getPackageRootFolder()
                                        + Executables.SUCCESSFUL_GRAPH.getExecutablePath())
                        .setScheduleName(Executables.SUCCESSFUL_GRAPH.getExecutableName())
                        .setScheduleId(successfulScheduleId).setExecutionDetails(successfulExecutionDetails);
        checkNotification(NotificationEvents.PROCESS_SCHEDULED, expectedParams);
    }

    @Test(dependsOnMethods = {"failureEventTrigger"}, groups = {"notification"})
    public void checkFailureMessage() throws MessagingException {
        NotificationParameters expectedParams =
                new NotificationParameters()
                        .setProjectId(testParams.getProjectId())
                        .setUser(getUserProfileId())
                        .setUserEmail(testParams.getUser())
                        .setProcessUri(failureProcessUri)
                        .setProcessName(FAILURE_NOTIFICATION_TEST_PROCESS)
                        .setExecutable(
                                DeployPackages.BASIC.getPackageRootFolder()
                                        + Executables.FAILED_GRAPH.getExecutablePath())
                        .setScheduleName(Executables.FAILED_GRAPH.getExecutableName())
                        .setScheduleId(failedScheduleId).setExecutionDetails(failedExecutionDetails);
        checkNotification(NotificationEvents.FAILURE, expectedParams);
    }

    @Test(dependsOnMethods = {"customEventTrigger"}, groups = {"notification"})
    public void checkCustomEventMessage() throws MessagingException {
        NotificationParameters expectedParams = new NotificationParameters().setCustomParam("World");
        checkNotification(NotificationEvents.CUSTOM_EVENT, expectedParams);
    }

    @Test(dependsOnMethods = {"prepareDataForNotificationFormChecking"}, groups = {"notification"})
    public void checkEmptyNotificationFieldsError() {
        openProjectDetailPage(testParams.getProjectId());
        projectDetailPage.activeProcess(NOTIFICATION_TEST_PROCESS).clickOnNotificationRuleButton();
        waitForElementVisible(discNotificationRules.getRoot());
        discNotificationRules.clickOnAddNotificationButton();
        int notificationIndex = discNotificationRules.getNotificationNumber() - 1;
        NotificationRule notificationRuleItem = discNotificationRules.getNotificationRule(notificationIndex);

        notificationRuleItem.fillInEmail("");
        assertTrue(notificationRuleItem.isCorrectEmailValidationError(), "Incorrect email validation error!");

        notificationRuleItem.fillInSubject("");
        assertTrue(notificationRuleItem.isCorrectSubjectValidationError(), "Incorrect subject validation error!");

        notificationRuleItem.fillInMessage("");
        assertTrue(notificationRuleItem.isCorrectMessageValidationError(), "Incorrect message validation error!");

        notificationRuleItem.setNotificationEvent(NotificationEvents.CUSTOM_EVENT);
        notificationRuleItem.fillInNotificationCustomEventName("");
        assertTrue(notificationRuleItem.isCorrectCustomEventNameValidationError());
    }

    @Test(dependsOnMethods = {"prepareDataForNotificationFormChecking"}, groups = {"notification"})
    public void checkEmailFieldError() {
        openProjectDetailPage(testParams.getProjectId());
        projectDetailPage.activeProcess(NOTIFICATION_TEST_PROCESS).clickOnNotificationRuleButton();
        waitForElementVisible(discNotificationRules.getRoot());
        discNotificationRules.clickOnAddNotificationButton();
        int notificationIndex = discNotificationRules.getNotificationNumber() - 1;
        NotificationRule notificationRuleItem = discNotificationRules.getNotificationRule(notificationIndex);
        notificationRuleItem.fillInEmail("abc@gmail.com,xyz@gmail.com");
        assertTrue(notificationRuleItem.isCorrectEmailValidationError(), "Incorrect email validation error!");
    }

    @Test(dependsOnMethods = {"prepareDataForNotificationFormChecking"}, groups = {"notification"})
    public void checkAvailableParams() {
        openProjectDetailPage(testParams.getProjectId());
        projectDetailPage.activeProcess(NOTIFICATION_TEST_PROCESS).clickOnNotificationRuleButton();
        waitForElementVisible(discNotificationRules.getRoot());
        discNotificationRules.clickOnAddNotificationButton();
        int notificationIndex = discNotificationRules.getNotificationNumber() - 1;
        NotificationRule notificationRule = discNotificationRules.getNotificationRule(notificationIndex);

        System.out.println("Available params for Success Notification: " + notificationRule.getAvailableParams());
        assertEquals(notificationRule.getAvailableParams(), successNotificationParams);

        notificationRule.setNotificationEvent(NotificationEvents.PROCESS_SCHEDULED);
        System.out.println("Available params for Process Scheduled Notification: "
                + notificationRule.getAvailableParams());
        assertEquals(notificationRule.getAvailableParams(), processScheduledNotificationParams);

        notificationRule.setNotificationEvent(NotificationEvents.PROCESS_STARTED);
        System.out.println("Available params for Process Started Notification: "
                + notificationRule.getAvailableParams());
        assertEquals(notificationRule.getAvailableParams(), processStartedNotificationParams);

        notificationRule.setNotificationEvent(NotificationEvents.FAILURE);
        System.out.println("Available params for Failure Notification: " + notificationRule.getAvailableParams());
        assertEquals(notificationRule.getAvailableParams(), failureNotificationParams);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"notification"})
    public void checkNotificationNumber() {
        String processName = "Check Notification Number";
        try {
            openProjectDetailPage(testParams.getProjectId());
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
        openProjectDetailPage(testParams.getProjectId());

        String notificationNumber =
                projectDetailPage.activeProcess(NOTIFICATION_TEST_PROCESS).getNotificationRuleNumber();

        NotificationBuilder notificationBuilder =
                new NotificationBuilder().setProcessName(NOTIFICATION_TEST_PROCESS).setEmail(imapUser)
                        .setSubject(notificationSubject).setMessage(notificationMessage).setSaved(false);
        createNotification(notificationBuilder);

        openProjectDetailPage(testParams.getProjectId());
        assertEquals(projectDetailPage.activeProcess(NOTIFICATION_TEST_PROCESS).getNotificationRuleNumber(),
                notificationNumber);
    }

    @Test(dependsOnMethods = {"prepareDataForNotificationFormChecking"}, groups = {"notification"})
    public void checkDeleteNotification() {
        openProjectDetailPage(testParams.getProjectId());
        projectDetailPage.activeProcess(NOTIFICATION_TEST_PROCESS).clickOnNotificationRuleButton();
        waitForElementVisible(discNotificationRules.getRoot());
        int notificationNumber = discNotificationRules.getNotificationNumber();

        NotificationBuilder notificationInfo =
                new NotificationBuilder().setProcessName(NOTIFICATION_TEST_PROCESS).setEmail(imapUser)
                        .setSubject(notificationSubject).setMessage(notificationMessage)
                        .setEvent(NotificationEvents.FAILURE).setSaved(true);
        createAndAssertNotification(notificationInfo);

        deleteNotification(notificationInfo);
        checkNotificationNumber(notificationNumber, NOTIFICATION_TEST_PROCESS);
    }

    @Test(dependsOnMethods = {"prepareDataForNotificationFormChecking"}, groups = {"notification"})
    public void checkCancelDeleteNotification() {
        openProjectDetailPage(testParams.getProjectId());
        projectDetailPage.activeProcess(NOTIFICATION_TEST_PROCESS).clickOnNotificationRuleButton();
        waitForElementVisible(discNotificationRules.getRoot());
        int notificationNumber = discNotificationRules.getNotificationNumber();

        NotificationBuilder notificationInfo =
                new NotificationBuilder().setProcessName(NOTIFICATION_TEST_PROCESS).setEmail(imapUser)
                        .setSubject(notificationSubject).setMessage(notificationMessage)
                        .setEvent(NotificationEvents.FAILURE).setSaved(true);
        createAndAssertNotification(notificationInfo);
        notificationNumber++;

        deleteNotification(notificationInfo.setSaved(false));
        checkNotificationNumber(notificationNumber - 1, NOTIFICATION_TEST_PROCESS);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"notification"})
    public void checkEditNotification() throws MessagingException {
        openProjectDetailPage(testParams.getProjectId());
        String processName = "Check Edit Notification";
        try {
            deployInProjectDetailPage(DeployPackages.BASIC, processName);

            String subject = notificationSubject + Calendar.getInstance().getTime();
            String message = "params.PROJECT=${params.PROJECT}" + "*" + "params.FINISH_TIME=${params.FINISH_TIME}";

            NotificationBuilder notificationInfo =
                    new NotificationBuilder().setProcessName(processName).setEmail(testParams.getUser())
                            .setSubject(subject).setMessage(message).setEvent(NotificationEvents.SUCCESS);
            createAndAssertNotification(notificationInfo);

            editNotification(notificationInfo.setEmail(imapUser)
                    .setSubject(subject + Calendar.getInstance().getTime())
                    .setEvent(NotificationEvents.PROCESS_STARTED));

            openProjectDetailPage(testParams.getProjectId());
            createSchedule(new ScheduleBuilder().setProcessName(processName)
                    .setExecutable(Executables.SUCCESSFUL_GRAPH).setCronTime(ScheduleCronTimes.CRON_EVERYDAY)
                    .setHourInDay("23").setMinuteInHour("59"));
            scheduleDetail.manualRun();
            assertSuccessfulExecution();

            NotificationParameters expectedParams =
                    new NotificationParameters().setProjectId(testParams.getProjectId()).setExecutionDetails(
                            new ExecutionDetails());
            waitForNotification(notificationInfo.getSubject(), expectedParams);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"notification"})
    public void checkCancelEditNotification() throws MessagingException {
        openProjectDetailPage(testParams.getProjectId());
        String processName = "Check Cancel Edit Notification";
        try {
            deployInProjectDetailPage(DeployPackages.BASIC, processName);

            String subject = notificationSubject + Calendar.getInstance().getTime();
            String message = "params.PROJECT=${params.PROJECT}" + "*" + "params.FINISH_TIME=${params.FINISH_TIME}";

            NotificationBuilder notificationInfo =
                    new NotificationBuilder().setProcessName(processName).setEmail(imapUser).setSubject(subject)
                            .setMessage(message).setEvent(NotificationEvents.SUCCESS);
            createAndAssertNotification(notificationInfo);

            NotificationBuilder editedNotificationInfo =
                    new NotificationBuilder().setProcessName(processName).setEmail(imapUser)
                            .setSubject(notificationSubject + Calendar.getInstance().getTime())
                            .setMessage(message).setEvent(NotificationEvents.PROCESS_STARTED)
                            .setEmail(testParams.getUser()).setSaved(false);
            editNotification(editedNotificationInfo);
            assertNotification(notificationInfo);

            openProjectDetailPage(testParams.getProjectId());
            createSchedule(new ScheduleBuilder().setProcessName(processName)
                    .setExecutable(Executables.SUCCESSFUL_GRAPH).setCronTime(ScheduleCronTimes.CRON_EVERYDAY)
                    .setHourInDay("23").setMinuteInHour("59"));

            scheduleDetail.manualRun();
            assertSuccessfulExecution();

            ExecutionDetails executionDetails = new ExecutionDetails().setStatus(ScheduleStatus.OK);
            getExecutionInfoFromGreyPage(executionDetails, scheduleDetail.getLastExecutionLogLink());

            NotificationParameters expectedParams =
                    new NotificationParameters().setProjectId(testParams.getProjectId()).setExecutionDetails(
                            executionDetails);
            waitForNotification(subject, expectedParams);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"notification"})
    public void checkRepeatedDataLoadingFailureNotification()
            throws IOException, JSONException, MessagingException {
        try {
            addUserToProject(imapUser, UserRoles.ADMIN);
            logout();

            signInAtUI(imapUser, imapPassword);
            openProjectDetailPage(testParams.getProjectId());
            String processName = "Check Repeated Failures Notification" + Calendar.getInstance().getTimeInMillis();
            deployInProjectDetailPage(DeployPackages.BASIC, processName);
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName)
                            .setExecutable(Executables.SHORT_TIME_FAILED_GRAPH)
                            .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setHourInDay("23").setMinuteInHour("59");
            createSchedule(scheduleBuilder);
            scheduleBuilder.setScheduleUrl(browser.getCurrentUrl());

            repeatManualRunFailedSchedule(getNumberOfFailuresToSendMail(), scheduleBuilder.getExecutable());
            waitForRepeatedFailuresEmail(scheduleBuilder);

            repeatManualRunFailedSchedule(
                    getNumberOfFailuresToDisableSchedule() - getNumberOfFailuresToSendMail(),
                    scheduleBuilder.getExecutable());
            scheduleBuilder.setEnabled(false);
            waitForRepeatedFailuresEmail(scheduleBuilder);
        } finally {
            cleanProcessesInWorkingProject();
            logout();
            signInAtUI(testParams.getUser(), testParams.getPassword());
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"notification"})
    public void checkEmptyStateNotificationList() {
        openProjectDetailPage(testParams.getProjectId());
        String processName = "Check Empty State Notification List";
        try {
            deployInProjectDetailPage(DeployPackages.BASIC, processName);
            openProjectDetailPage(testParams.getProjectId());
            projectDetailPage.activeProcess(processName).clickOnNotificationRuleButton();
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

    private int getNumberOfFailuresToSendMail() {
        return RepeatedDataLoadingFailureNumber.TO_SEND_MAIL.getNumber(testParams.getHost());
    }

    private int getNumberOfFailuresToDisableSchedule() {
        return RepeatedDataLoadingFailureNumber.TO_DISABLE_SCHEDULE.getNumber(testParams.getHost());
    }

    private void editNotification(NotificationBuilder newNotificationBuilder) {
        openProjectDetailPage(testParams.getProjectId());
        projectDetailPage.activeProcess(newNotificationBuilder.getProcessName()).clickOnNotificationRuleButton();
        waitForFragmentVisible(discNotificationRules);
        int notificationIndex = newNotificationBuilder.getIndex();
        NotificationRule notificationRule = discNotificationRules.getNotificationRule(notificationIndex);
        notificationRule.expandNotificationRule();
        notificationRule.clearNotificationEmail();
        notificationRule.clearNotificationSubject();
        notificationRule.clearNotificationMessage();
        notificationRule.setNotificationFields(newNotificationBuilder);
        if (newNotificationBuilder.isSaved())
            notificationRule.saveNotification();
        else
            notificationRule.cancelSaveNotification();
    }

    private void waitForNotification(String subject, NotificationParameters expectedParams) throws MessagingException {
        try (ImapClient imapClient = new ImapClient(imapHost, imapUser, imapPassword)) {
            System.out.println("Waiting for notification...");
            checkScheduleEventNotification(imapClient, subject, expectedParams);
        }
    }

    private void waitForRepeatedFailuresEmail(ScheduleBuilder scheduleBuilder) throws MessagingException {
        try (ImapClient imapClient = new ImapClient(imapHost, imapUser, imapPassword)) {
            System.out.println("Waiting for notification...");
            checkRepeatFailureEmail(imapClient, scheduleBuilder);
        }
    }

    private void checkNotification(NotificationEvents event, NotificationParameters expectedParams)
            throws MessagingException {
        String notificationSubject = null;
        switch (event) {
            case SUCCESS:
                notificationSubject = successNotificationSubject;
                break;
            case FAILURE:
                notificationSubject = failureNotificationSubject;
                break;
            case PROCESS_STARTED:
                notificationSubject = processStartedNotificationSubject;
                break;
            case PROCESS_SCHEDULED:
                notificationSubject = processScheduledNotificationSubject;
                break;
            case CUSTOM_EVENT:
                notificationSubject = customEventNotificationSubject;
                break;
            default:
                break;
        }
        waitForNotification(notificationSubject, expectedParams);
    }

    private void checkNotificationNumber(int expectedNotificationNumber, String processName) {
        openProjectDetailPage(testParams.getProjectId());
        waitForFragmentVisible(projectDetailPage);
        projectDetailPage.activeProcess(processName);
        if (expectedNotificationNumber == 0) {
            assertEquals(projectDetailPage.getNotificationRuleNumber(), "No notification rules");
        } else {
            String notificationNumber =
                    String.valueOf(expectedNotificationNumber) + " notification rule"
                            + (expectedNotificationNumber > 1 ? "s" : "");
            assertEquals(projectDetailPage.getNotificationRuleNumber(), notificationNumber,
                    "Incorrect notification number!");
        }
        projectDetailPage.clickOnNotificationRuleButton();
        waitForFragmentVisible(discNotificationRules);
        assertEquals(expectedNotificationNumber, discNotificationRules.getNotificationNumber(),
                "Incorrect notification number!");
        discNotificationRules.closeNotificationRulesDialog();
    }

    private void deleteNotification(NotificationBuilder notificationInfo) {
        openProjectDetailPage(testParams.getProjectId());
        projectDetailPage.activeProcess(notificationInfo.getProcessName()).clickOnNotificationRuleButton();
        waitForFragmentVisible(discNotificationRules);
        discNotificationRules.getNotificationRule(notificationInfo.getIndex()).deleteNotification(true);
    }

    private String getProcessUri(String url) {
        String processUri =
                "/gdc/projects/" + testParams.getProjectId() + "/dataload/"
                        + url.substring(url.indexOf("processes"), url.lastIndexOf("schedules") - 1);
        return processUri;
    }

    private void getExecutionInfoFromGreyPage(ExecutionDetails executionDetails, String executionLogLink) {
        executionDetails.setScheduleLogLink(executionLogLink);
        browser.get(executionLogLink.replace("ea.", "").replace("/log", "/detail"));
        waitForFragmentVisible(objectFragment);
        JSONObject jsonObject = null;
        JSONObject executionDetailJSONObject;
        try {
            jsonObject = objectFragment.getObject();
            executionDetailJSONObject = jsonObject.getJSONObject("executionDetail");
            executionDetails.setStartTime(executionDetailJSONObject.getString("started"));
            executionDetails.setEndTime(executionDetailJSONObject.getString("finished"));
            executionDetails.setScheduledTime(executionDetailJSONObject.getString("created"));
            if (executionDetails.getStatus().equals(ScheduleStatus.ERROR))
                executionDetails.setErrorMessage(executionDetailJSONObject.getJSONObject("error")
                        .getString("message").replaceAll("\n(\\s*)", ""));
        } catch (JSONException e) {
            fail("There is problem with jsonObject: " + e);
        }
    }

    private void checkRepeatFailureEmail(ImapClient imapClient, ScheduleBuilder scheduleBuilder)
            throws MessagingException {
        boolean isEnabled = scheduleBuilder.isEnabled();
        String subjectFormat =
                isEnabled ? REPEATED_FAILURES_NOTIFICATION_SUBJECT : SCHEDULE_DISABLED_NOTIFICATION_SUBJECT;
        String processName = scheduleBuilder.getProcessName();
        String notificationSubject = String.format(subjectFormat, processName);
        String notificationMessage1 =
                isEnabled ? REPEATED_FAILURES_NOTIFICATION_MESSAGE_1.replace("${numberOfFailures}",
                        Integer.toString(getNumberOfFailuresToSendMail()))
                        : SCHEDULE_DISABLED_NOTIFICATION_MESSAGE_1.replace("${numberOfFailures}",
                                Integer.toString(getNumberOfFailuresToDisableSchedule()));
        String notificationMessage2 =
                isEnabled ? REPEATED_FAILURES_NOTIFICATION_MESSAGE_2 : SCHEDULE_DISABLED_NOTIFICATION_MESSAGE_2;
        String notificationBody =
                isEnabled ? REPEATED_FAILURES_NOTIFICATION_BODY.replace("${numberOfFailures}",
                        Integer.toString(getNumberOfFailuresToSendMail())) : SCHEDULE_DISABLED_NOTIFICATION_BODY
                        .replace("${numberOfFailures}", Integer.toString(getNumberOfFailuresToDisableSchedule()));

        Message notification = getNotification(imapClient, notificationSubject);
        Document message = null;
        try {
            message = Jsoup.parse(notification.getContent().toString());
        } catch (IOException e) {
            fail("There is problem during parsing message: " + e);
        } catch (MessagingException e) {
            fail("There is problem during getting repeated failures email: " + e);
        }
        System.out.println("Notification message: " + message.getElementsByTag("body").text());

        String scheduleName = scheduleBuilder.getScheduleName();
        assertEquals(message.getElementsByTag("body").text(), String.format(notificationBody, scheduleName,
                processName, projectTitle, testParams.getProjectId()), "Incorrect message in notification!");
        String scheduleUrl = scheduleBuilder.getScheduleUrl();
        assertEquals(message.getElementsByTag("p").get(1).toString().replace("https://ea.", "https://"),
                String.format(notificationMessage1, scheduleUrl, scheduleName, processName, projectTitle,
                        testParams.getProjectId()), "Incorrect message in notification!");
        assertEquals(message.getElementsByTag("li").get(0).toString().replace("https://ea.", "https://"),
                String.format(notificationMessage2, scheduleUrl), "Incorrect message in notification!");
        assertEquals(message.getElementsByTag("p").get(3).toString(), NOTIFICATION_SUPPORT_MESSAGE,
                "Incorrect message in notification!");
    }

    private void checkScheduleEventNotification(ImapClient imapClient, String subject,
            NotificationParameters expectedParams) throws MessagingException {
        Message notification = getNotification(imapClient, subject);
        ArrayList<String> paramValues = new ArrayList<String>();
        String notificationContent = "";
        try {
            notificationContent = notification.getContent().toString() + "*";
        } catch (IOException e) {
            fail("There is problem when checking schedule event notification: " + e);
        } catch (MessagingException e) {
            fail("There is problem when checking schedule event notification: " + e);
        }
        while (!notificationContent.isEmpty()) {
            if (notificationContent.substring(0, notificationContent.indexOf("*")).contains("params.LOG"))
                paramValues.add(notificationContent.substring(0, notificationContent.indexOf("*")).replace(
                        "https://ea.", "https://"));
            else
                paramValues.add(notificationContent.substring(0, notificationContent.indexOf("*")));
            System.out.println("Param value: " + paramValues.get(paramValues.size() - 1));
            if (notificationContent.substring(notificationContent.indexOf("*")).equals("*"))
                notificationContent = "";
            else
                notificationContent = notificationContent.substring(notificationContent.indexOf("*") + 1);
        }

        for (String paramItem : paramValues) {
            String paramName = paramItem.substring(0, paramItem.indexOf("="));
            String paramValue = paramItem.substring(paramItem.indexOf("=") + 1);
            if (paramValue.contains(SystemUtils.LINE_SEPARATOR)) {
                StringBuilder builder = new StringBuilder();
                for (String s : paramValue.split(SystemUtils.LINE_SEPARATOR)) {
                    builder.append(s.trim());
                }
                paramValue = builder.toString();
            }
            if (paramName.contains("TIME") && !paramValue.isEmpty()) {
                long timeDifferenceInSeconds =
                        timeDifferenceInSecond(expectedParams.getParamValue(paramName), paramValue);
                assertTrue(timeDifferenceInSeconds >= -2 && timeDifferenceInSeconds <= 2,
                        "Not allowed time difference in seconds: " + timeDifferenceInSeconds);
            } else
                assertEquals(paramValue, expectedParams.getParamValue(paramName), "Parameter " + paramName
                        + " has incorrect value: " + paramValue);
        }
    }

    private long timeDifferenceInSecond(String expectedTime, String actualTime) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        try {
            long compareResult = format2.parse(actualTime).getTime() - format.parse(expectedTime).getTime();
            return TimeUnit.MILLISECONDS.toSeconds(compareResult);
        } catch (ParseException e) {
            fail("Exeception in parsing time: " + e);
        }
        return 0;
    }

    private String getUserProfileId() {
        if (userProfileId == null) {
            userProfileId = getGoodDataClient().getAccountService().getCurrent().getUri();
            userProfileId = userProfileId.substring(userProfileId.lastIndexOf("/") + 1);
        }

        return userProfileId;
    }

    private enum RepeatedDataLoadingFailureNumber {
        TO_SEND_MAIL(2, 5, 5),
        TO_DISABLE_SCHEDULE(3, 30, 30);

        private int piNumber;
        private int stagingNumber;
        private int euNumber;

        private RepeatedDataLoadingFailureNumber(int pi, int staging, int eu) {
            this.piNumber = pi;
            this.stagingNumber = staging;
            this.euNumber = eu;
        }

        public int getNumber(String host) {
            if ("gdctest.eu.gooddata.com".equals(host))
                return euNumber;
            else if (host.contains("staging"))
                return stagingNumber;
            else
                return piNumber;
        }
    }
}
