package com.gooddata.qa.graphene.disc;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.DISCNotificationEvents;
import com.gooddata.qa.graphene.enums.DISCProcessTypes;
import com.gooddata.qa.graphene.enums.ScheduleCronTimes;
import com.gooddata.qa.utils.mail.ImapClient;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

public class NotificationsTest extends AbstractSchedulesTests {

    private static final List<String> BASIC_GRAPH_LIST = Arrays.asList("errorGraph.grf",
            "longTimeRunningGraph.grf", "successfulGraph.grf");
    private static final String NOTIFICATION_TEST_PROCESS = "Notification Test Process";
    private static final String SUCCESS_NOTIFICATION_TEST_PROCESS = "Success Notification Test";
    private static final String FAILURE_NOTIFICATION_TEST_PROCESS = "Failure Notification Test";
    private static final String CUSTOM_NOTIFICATION_TEST_PROCESS = "Custom Event Notification Test";
    private static final String REPEATED_FAILURES_NOTIFICATION_SUBJECT =
            "Repeated data loading failure: \"%s\" process";
    private static final String REPEATED_FAILURES_NOTIFICATION_MESSAGE_1 =
            "<p>the <a href=\"%s\">%s schedule</a> within the \"%s\" process of your\"%s\" GoodData project (id: %s) has failed for the 5th time.</p>";
    private static final String REPEATED_FAILURES_NOTIFICATION_MESSAGE_2 =
            "<p>We highly recommend disabling failing schedules until the issues are addressed:</p>"
                    + System.lineSeparator()
                    + "<ol> <li>Go to the <a href=\"%s\">schedule page</a></li> <li>Click \"Disable\" button</li>";
    private static final String SCHEDULE_DISABLED_NOTIFICATION_SUBJECT =
            "Schedule disabled: \"%s\" process";
    private static final String SCHEDULE_DISABLED_NOTIFICATION_MESSAGE_1 =
            "<p>the <a href=\"%s\">%s schedule</a> within the \"%s\" process of your\"%s\" GoodData project (id: %s) "
                    + "has been <strong>automatically disabled</strong> following its30th consecutive failure.</p>";
    private static final String SCHEDULE_DISABLED_NOTIFICATION_MESSAGE_2 =
            "<p>To resume scheduled uploads from this process:</p>"
                    + System.lineSeparator()
                    + "<ol> <li>Go to the <a href=\"%s\">schedule page</a></li> <li>Click \"Enable\"</li>";
    private static final String NOTIFICATION_SUPPORT_MESSAGE =
            "<p>If you require assistance with troubleshooting data uploading, please visit the"
                    + System.lineSeparator()
                    + "<a href=\"http://support.gooddata.com/entries/23541617-Automatic-Disabling-of-Failed-Schedules\">GoodData Support Portal</a>.</p>";
    private static final String NOTIFICATION_RULES_EMPTY_STATE_MESSAGE =
            "No event (eg. schedule start, finish, fail, etc.) will trigger a notification email.";

    private static final String FROM = "no-reply@gooddata.com";

    private String successNotificationSubject = "Success Notification_"
            + Calendar.getInstance().getTime();
    private String successNotificationMessage = "${params.PROJECT}" + "*" + "${params.USER}" + "*"
            + "${params.USER_EMAIL}" + "*" + "${params.PROCESS_URI}" + "*" + "${params.PROCESS_ID}"
            + "*" + "${params.PROCESS_NAME}" + "*" + "${params.EXECUTABLE}" + "*"
            + "${params.SCHEDULE_ID}" + "*" + "${params.SCHEDULE_NAME}" + "*" + "${params.LOG}"
            + "*" + "${params.START_TIME}" + "*" + "${params.FINISH_TIME}";
    private String failureNotificationSubject = "Failure Notification_"
            + Calendar.getInstance().getTime();
    private String failureNotificationMessage = "${params.PROJECT}" + "*" + "${params.USER}" + "*"
            + "${params.USER_EMAIL}" + "*" + "${params.PROCESS_URI}" + "*" + "${params.PROCESS_ID}"
            + "*" + "${params.PROCESS_NAME}" + "*" + "${params.EXECUTABLE}" + "*"
            + "${params.SCHEDULE_ID}" + "*" + "${params.SCHEDULE_NAME}" + "*" + "${params.LOG}"
            + "*" + "${params.START_TIME}" + "*" + "${params.FINISH_TIME}" + "*"
            + "${params.ERROR_MESSAGE}";
    private String processStartedNotificationSubject = "Process Started Notification_"
            + Calendar.getInstance().getTime();
    private String processStartedNotificationMessage = "${params.PROJECT}" + "*" + "${params.USER}"
            + "*" + "${params.USER_EMAIL}" + "*" + "${params.PROCESS_URI}" + "*"
            + "${params.PROCESS_ID}" + "*" + "${params.PROCESS_NAME}" + "*"
            + "${params.EXECUTABLE}" + "*" + "${params.SCHEDULE_ID}" + "*"
            + "${params.SCHEDULE_NAME}" + "*" + "${params.LOG}" + "*" + "${params.START_TIME}";
    private String processScheduledNotificationSubject = "Process Scheduled Notification_"
            + Calendar.getInstance().getTime();
    private String processScheduledNotificationMessage = "${params.PROJECT}" + "*"
            + "${params.USER}" + "*" + "${params.USER_EMAIL}" + "*" + "${params.PROCESS_URI}" + "*"
            + "${params.PROCESS_ID}" + "*" + "${params.EXECUTABLE}" + "*" + "${params.SCHEDULE_ID}"
            + "*" + "${params.SCHEDULE_NAME}" + "*" + "${params.SCHEDULED_TIME}";
    private String customEventNotificationSubject = "Custom Event_"
            + Calendar.getInstance().getTime();
    private String customEventNotificationMessage = "${params.hello}";
    private String notificationSubject = "Notification Subject_";
    private String notificationMessage = "${params.PROJECT}";

    private String userProfileId;
    private String successProcessUri;
    private String failureProcessUri;
    private String successfulScheduleId;
    private String lastSuccessfulExecutionLogLink;
    private String successfulStartTime;
    private String successfulEndTime;
    private String failedScheduleId;
    private String lastFailedExecutionLogLink;
    private String failedStartTime;
    private String failedEndTime;
    private String errorMessage;
    private final SimpleDateFormat dateFormat1 = new SimpleDateFormat("MMM dd, yyyy");
    private final SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat timeFormat1 = new SimpleDateFormat("hh:mm:ss aaa");
    private final SimpleDateFormat timeFormat2 = new SimpleDateFormat("HH:mm:ss");

    @BeforeClass
    public void initProperties() {
        zipFilePath = testParams.loadProperty("zipFilePath") + testParams.getFolderSeparator();
        projectTitle = "Disc-test-notification";
        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");
        userProfileId =
                testParams.loadProperty("userProfileUri").substring(
                        testParams.loadProperty("userProfileUri").lastIndexOf("/") + 1);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"notification"})
    public void prepareDataForSucessEvent() throws JSONException, InterruptedException {
        openProjectDetailPage(projectTitle, testParams.getProjectId());
        deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
                DISCProcessTypes.GRAPH, SUCCESS_NOTIFICATION_TEST_PROCESS, BASIC_GRAPH_LIST, true);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"notification"})
    public void prepareDataForFailureEvent() throws JSONException, InterruptedException {
        openProjectDetailPage(projectTitle, testParams.getProjectId());
        deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
                DISCProcessTypes.GRAPH, FAILURE_NOTIFICATION_TEST_PROCESS, BASIC_GRAPH_LIST, true);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"notification"})
    public void prepareDataForNotificationFormChecking() throws JSONException, InterruptedException {
        openProjectDetailPage(projectTitle, testParams.getProjectId());
        deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
                DISCProcessTypes.GRAPH, NOTIFICATION_TEST_PROCESS, BASIC_GRAPH_LIST, true);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"notification"})
    public void prepareDataForCustomEvent() throws JSONException, InterruptedException {
        openProjectDetailPage(projectTitle, testParams.getProjectId());
        deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "CTL_event",
                DISCProcessTypes.GRAPH, CUSTOM_NOTIFICATION_TEST_PROCESS,
                Arrays.asList("CTL_Function.grf"), true);
    }

    @Test(dependsOnMethods = {"prepareDataForSucessEvent"}, groups = {"notification"})
    public void createNotificationForSuccessEvent() throws InterruptedException {
        int notificationIndex =
                createNotification(SUCCESS_NOTIFICATION_TEST_PROCESS, imapUser,
                        successNotificationSubject, successNotificationMessage,
                        DISCNotificationEvents.SUCCESS, null, true);
        assertNotification(SUCCESS_NOTIFICATION_TEST_PROCESS, notificationIndex, imapUser,
                successNotificationSubject, successNotificationMessage,
                DISCNotificationEvents.SUCCESS, null);
    }

    @Test(dependsOnMethods = {"prepareDataForFailureEvent"}, groups = {"notification"})
    public void createNotificationForFailureEvent() throws InterruptedException {
        int notificationIndex =
                createNotification(FAILURE_NOTIFICATION_TEST_PROCESS, imapUser,
                        failureNotificationSubject, failureNotificationMessage,
                        DISCNotificationEvents.FAILURE, null, true);
        assertNotification(FAILURE_NOTIFICATION_TEST_PROCESS, notificationIndex, imapUser,
                failureNotificationSubject, failureNotificationMessage,
                DISCNotificationEvents.FAILURE, null);
    }

    @Test(dependsOnMethods = {"prepareDataForSucessEvent"}, groups = {"notification"})
    public void createNotificationForProcessStartedEvent() throws InterruptedException {
        int notificationIndex =
                createNotification(SUCCESS_NOTIFICATION_TEST_PROCESS, imapUser,
                        processStartedNotificationSubject, processStartedNotificationMessage,
                        DISCNotificationEvents.PROCESS_STARTED, null, true);
        assertNotification(SUCCESS_NOTIFICATION_TEST_PROCESS, notificationIndex, imapUser,
                processStartedNotificationSubject, processStartedNotificationMessage,
                DISCNotificationEvents.PROCESS_STARTED, null);
    }

    @Test(dependsOnMethods = {"prepareDataForSucessEvent"}, groups = {"notification"})
    public void createNotificationForProcessScheduledEvent() throws InterruptedException {
        int notificationIndex =
                createNotification(SUCCESS_NOTIFICATION_TEST_PROCESS, imapUser,
                        processScheduledNotificationSubject, processScheduledNotificationMessage,
                        DISCNotificationEvents.PROCESS_SCHEDULED, null, true);
        assertNotification(SUCCESS_NOTIFICATION_TEST_PROCESS, notificationIndex, imapUser,
                processScheduledNotificationSubject, processScheduledNotificationMessage,
                DISCNotificationEvents.PROCESS_SCHEDULED, null);
    }

    @Test(dependsOnMethods = {"prepareDataForCustomEvent"}, groups = {"notification"})
    public void createNotificationForCustomEvent() throws InterruptedException {
        int notificationIndex =
                createNotification(CUSTOM_NOTIFICATION_TEST_PROCESS, imapUser,
                        customEventNotificationSubject, customEventNotificationMessage,
                        DISCNotificationEvents.CUSTOM_EVENT, "welcomeEvent", true);
        assertNotification(CUSTOM_NOTIFICATION_TEST_PROCESS, notificationIndex, imapUser,
                customEventNotificationSubject, customEventNotificationMessage,
                DISCNotificationEvents.CUSTOM_EVENT, "welcomeEvent");
    }

    @Test(dependsOnMethods = {"createNotificationForSuccessEvent"}, groups = {"notification"})
    public void successEventTrigger() throws InterruptedException, ParseException {
        Pair<String, List<String>> cronTime =
                Pair.of(ScheduleCronTimes.CRON_EVERYDAY.getCronTime(), Arrays.asList("59", "23"));
        openProjectDetailPage(projectTitle, testParams.getProjectId());
        createScheduleForProcess(projectTitle, testParams.getProjectId(),
                SUCCESS_NOTIFICATION_TEST_PROCESS, "/graph/successfulGraph.grf", cronTime, null);
        assertNewSchedule(SUCCESS_NOTIFICATION_TEST_PROCESS, "successfulGraph.grf", cronTime, null);
        scheduleDetail.manualRun();
        scheduleDetail.assertLastExecutionDetails(true, true, false,
                "Basic/graph/successfulGraph.grf", DISCProcessTypes.GRAPH, 5);
        successProcessUri = getProcessUri(browser.getCurrentUrl());
        successfulScheduleId =
                browser.getCurrentUrl().substring(browser.getCurrentUrl().lastIndexOf("/") + 1);
        lastSuccessfulExecutionLogLink =
                scheduleDetail.getLastExecutionLogLink().replace("ea.", "");
        List<String> formattedDateTime =
                formatExecutionDateTime(successfulStartTime, successfulEndTime,
                        scheduleDetail.getLastExecutionDate(),
                        scheduleDetail.getLastExecutionTime());
        successfulStartTime = formattedDateTime.get(0);
        successfulEndTime = formattedDateTime.get(1);
    }

    @Test(dependsOnMethods = {"createNotificationForFailureEvent"}, groups = {"notification"})
    public void failureEventTrigger() throws InterruptedException, ParseException {
        Pair<String, List<String>> cronTime =
                Pair.of(ScheduleCronTimes.CRON_EVERYDAY.getCronTime(), Arrays.asList("59", "23"));
        openProjectDetailPage(projectTitle, testParams.getProjectId());
        createScheduleForProcess(projectTitle, testParams.getProjectId(),
                FAILURE_NOTIFICATION_TEST_PROCESS, "/graph/errorGraph.grf", cronTime, null);
        assertNewSchedule(FAILURE_NOTIFICATION_TEST_PROCESS, "errorGraph.grf", cronTime, null);
        scheduleDetail.manualRun();
        scheduleDetail.assertLastExecutionDetails(false, true, false, "Basic/graph/errorGraph.grf",
                DISCProcessTypes.GRAPH, 5);
        failureProcessUri = getProcessUri(browser.getCurrentUrl());
        failedScheduleId =
                browser.getCurrentUrl().substring(browser.getCurrentUrl().lastIndexOf("/") + 1);
        lastFailedExecutionLogLink =
                scheduleDetail.getLastExecutionLogLink().replace("https://ea.", "https://");
        List<String> formattedDateTime =
                formatExecutionDateTime(failedStartTime, failedEndTime,
                        scheduleDetail.getLastExecutionDate(),
                        scheduleDetail.getLastExecutionTime());
        failedStartTime = formattedDateTime.get(0);
        failedEndTime = formattedDateTime.get(1);
        errorMessage = scheduleDetail.getLastExecutionDescription();
    }

    @Test(dependsOnMethods = {"createNotificationForCustomEvent"}, groups = {"notification"})
    public void customEventTrigger() throws InterruptedException, ParseException {
        Pair<String, List<String>> cronTime =
                Pair.of(ScheduleCronTimes.CRON_EVERYDAY.getCronTime(), Arrays.asList("59", "23"));
        openProjectDetailPage(projectTitle, testParams.getProjectId());
        createScheduleForProcess(projectTitle, testParams.getProjectId(),
                CUSTOM_NOTIFICATION_TEST_PROCESS, "/graph/CTL_Function.grf", cronTime, null);
        assertNewSchedule(CUSTOM_NOTIFICATION_TEST_PROCESS, "CTL_Function.grf", cronTime, null);
        scheduleDetail.manualRun();
        scheduleDetail.assertLastExecutionDetails(true, true, false,
                "CTL_event/graph/CTL_Function.grf", DISCProcessTypes.GRAPH, 5);
    }

    @Test(dependsOnMethods = {"successEventTrigger"}, groups = {"notification"})
    public void checkSuccessMessage() throws InterruptedException, MessagingException, IOException {
        List<String> expectedParamValues =
                getBasicEventParamValues(successProcessUri, SUCCESS_NOTIFICATION_TEST_PROCESS,
                        "Basic/graph/successfulGraph.grf", "successfulGraph.grf",
                        successfulScheduleId, lastSuccessfulExecutionLogLink, successfulStartTime,
                        successfulEndTime, null);
        checkNotification(DISCNotificationEvents.SUCCESS, expectedParamValues);
    }

    @Test(dependsOnMethods = {"successEventTrigger", "createNotificationForProcessStartedEvent"},
            groups = {"notification"})
    public void checkProcessStartedSuccessMessage() throws InterruptedException,
            MessagingException, IOException {
        List<String> expectedParamValues =
                getBasicEventParamValues(successProcessUri, SUCCESS_NOTIFICATION_TEST_PROCESS,
                        "Basic/graph/successfulGraph.grf", "successfulGraph.grf",
                        successfulScheduleId, lastSuccessfulExecutionLogLink, successfulStartTime,
                        null, null);
        checkNotification(DISCNotificationEvents.PROCESS_STARTED, expectedParamValues);
    }

    @Test(dependsOnMethods = {"successEventTrigger", "createNotificationForProcessScheduledEvent"},
            groups = {"notification"})
    public void checkProcessScheduledSuccessMessage() throws InterruptedException,
            MessagingException, IOException {
        List<String> expectedParamValues =
                getBasicEventParamValues(successProcessUri, null,
                        "Basic/graph/successfulGraph.grf", "successfulGraph.grf",
                        successfulScheduleId, null, successfulStartTime, null, null);
        checkNotification(DISCNotificationEvents.PROCESS_SCHEDULED, expectedParamValues);
    }

    @Test(dependsOnMethods = {"failureEventTrigger"}, groups = {"notification"})
    public void checkFailureMessage() throws InterruptedException, MessagingException, IOException {
        List<String> expectedParamValues =
                getBasicEventParamValues(failureProcessUri, FAILURE_NOTIFICATION_TEST_PROCESS,
                        "Basic/graph/errorGraph.grf", "errorGraph.grf", failedScheduleId,
                        lastFailedExecutionLogLink, failedStartTime, failedEndTime, errorMessage);
        checkNotification(DISCNotificationEvents.FAILURE, expectedParamValues);
    }

    @Test(dependsOnMethods = {"customEventTrigger"}, groups = {"notification"})
    public void checkCustomEventMessage() throws InterruptedException, MessagingException,
            IOException {
        ArrayList<String> expectedParamValues = new ArrayList<String>();
        expectedParamValues.add("World");
        checkNotification(DISCNotificationEvents.CUSTOM_EVENT, expectedParamValues);
    }

    @Test(dependsOnMethods = {"prepareDataForNotificationFormChecking"}, groups = {"notification"})
    public void checkEmptyNotificationFieldsError() throws JSONException, InterruptedException {
        openProjectDetailPage(projectTitle, testParams.getProjectId());
        projectDetailPage.getNotificationButton(NOTIFICATION_TEST_PROCESS).click();
        waitForElementVisible(discNotificationRules.getRoot());
        discNotificationRules.clickOnAddNotificationButton();
        discNotificationRules.checkInvalidNotificationFields(
                discNotificationRules.getNotificationNumber() - 1, "", "", "",
                DISCNotificationEvents.SUCCESS, "");
    }

    @Test(dependsOnMethods = {"prepareDataForNotificationFormChecking"}, groups = {"notification"})
    public void checkEmailFieldError() throws JSONException, InterruptedException {
        openProjectDetailPage(projectTitle, testParams.getProjectId());
        projectDetailPage.getNotificationButton(NOTIFICATION_TEST_PROCESS).click();
        waitForElementVisible(discNotificationRules.getRoot());
        discNotificationRules.clickOnAddNotificationButton();
        discNotificationRules.checkInvalidNotificationFields(
                discNotificationRules.getNotificationNumber() - 1, imapUser + "," + imapUser, null,
                null, DISCNotificationEvents.SUCCESS, null);
    }

    @Test(dependsOnMethods = {"prepareDataForNotificationFormChecking"}, groups = {"notification"})
    public void checkAvailableParams() throws InterruptedException {
        openProjectDetailPage(projectTitle, testParams.getProjectId());
        projectDetailPage.getNotificationButton(NOTIFICATION_TEST_PROCESS).click();
        waitForElementVisible(discNotificationRules.getRoot());
        discNotificationRules.clickOnAddNotificationButton();
        int notificationIndex = discNotificationRules.getNotificationNumber() - 1;
        System.out.println("Available params for Success Notification: "
                + discNotificationRules.getAvailableParams());
        String expectedParams = successNotificationMessage.replace("*", "");
        assertEquals(expectedParams, discNotificationRules.getAvailableParams());
        discNotificationRules.setNotificationEvent(notificationIndex,
                DISCNotificationEvents.PROCESS_SCHEDULED);
        expectedParams = processScheduledNotificationMessage.replace("*", "");
        System.out.println("Available params for Process Scheduled Notification: "
                + discNotificationRules.getAvailableParams());
        assertEquals(expectedParams, discNotificationRules.getAvailableParams());
        discNotificationRules.setNotificationEvent(notificationIndex,
                DISCNotificationEvents.PROCESS_STARTED);
        expectedParams = processStartedNotificationMessage.replace("*", "");
        System.out.println("Available params for Process Started Notification: "
                + discNotificationRules.getAvailableParams());
        assertEquals(expectedParams, discNotificationRules.getAvailableParams());
        discNotificationRules.setNotificationEvent(notificationIndex,
                DISCNotificationEvents.FAILURE);
        expectedParams = failureNotificationMessage.replace("*", "");
        System.out.println("Available params for Failure Notification: "
                + discNotificationRules.getAvailableParams());
        assertEquals(expectedParams, discNotificationRules.getAvailableParams());
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"notification"})
    public void checkNotificationNumber() throws JSONException, InterruptedException {
        try {
            openProjectDetailPage(projectTitle, testParams.getProjectId());
            deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
                    DISCProcessTypes.GRAPH, "Check Notification Number", BASIC_GRAPH_LIST, true);
            checkNotificationNumber(0, "Check Notification Number");
            createNotification("Check Notification Number", imapUser, notificationSubject,
                    notificationMessage, DISCNotificationEvents.SUCCESS, null, true);
            checkNotificationNumber(1, "Check Notification Number");
            createNotification("Check Notification Number", imapUser, notificationSubject,
                    notificationMessage, DISCNotificationEvents.FAILURE, null, true);
            checkNotificationNumber(2, "Check Notification Number");
        } finally {
            openProjectDetailByUrl(testParams.getProjectId());
            projectDetailPage.deleteProcess("Check Notification Number");
        }
    }

    @Test(dependsOnMethods = {"prepareDataForNotificationFormChecking"}, groups = {"notification"})
    public void checkCancelCreateNotification() throws InterruptedException {
        openProjectDetailPage(projectTitle, testParams.getProjectId());
        String notificationNumber =
                projectDetailPage.getNotificationButton(NOTIFICATION_TEST_PROCESS).getText();
        createNotification(NOTIFICATION_TEST_PROCESS, imapUser, notificationSubject,
                notificationMessage, DISCNotificationEvents.FAILURE, null, false);
        openProjectDetailPage(projectTitle, testParams.getProjectId());
        assertEquals(projectDetailPage.getNotificationButton(NOTIFICATION_TEST_PROCESS).getText(),
                notificationNumber);
    }

    @Test(dependsOnMethods = {"prepareDataForNotificationFormChecking"}, groups = {"notification"})
    public void checkDeleteNotification() throws InterruptedException {
        openProjectDetailPage(projectTitle, testParams.getProjectId());
        projectDetailPage.getNotificationButton(NOTIFICATION_TEST_PROCESS).click();
        waitForElementVisible(discNotificationRules.getRoot());
        int notificationNumber = discNotificationRules.getNotificationNumber();
        int notificationIndex =
                createNotification(NOTIFICATION_TEST_PROCESS, imapUser, notificationSubject,
                        notificationMessage, DISCNotificationEvents.FAILURE, null, true);
        deleteNotification(NOTIFICATION_TEST_PROCESS, notificationIndex, true);
        checkNotificationNumber(notificationNumber, NOTIFICATION_TEST_PROCESS);
    }

    @Test(dependsOnMethods = {"prepareDataForNotificationFormChecking"}, groups = {"notification"})
    public void checkCancelDeleteNotification() throws InterruptedException {
        openProjectDetailPage(projectTitle, testParams.getProjectId());
        projectDetailPage.getNotificationButton(NOTIFICATION_TEST_PROCESS).click();
        waitForElementVisible(discNotificationRules.getRoot());
        int notificationNumber = discNotificationRules.getNotificationNumber();
        int notificationIndex =
                createNotification(NOTIFICATION_TEST_PROCESS, imapUser, notificationSubject,
                        notificationMessage, DISCNotificationEvents.FAILURE, null, true);
        notificationNumber++;
        deleteNotification(NOTIFICATION_TEST_PROCESS, notificationIndex, false);
        checkNotificationNumber(notificationNumber, NOTIFICATION_TEST_PROCESS);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"notification"})
    public void checkEditNotification() throws InterruptedException, ParseException,
            MessagingException, IOException, JSONException {
        openProjectDetailPage(projectTitle, testParams.getProjectId());
        deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
                DISCProcessTypes.GRAPH, "Check Edit Notification", BASIC_GRAPH_LIST, true);
        String subject = notificationSubject + Calendar.getInstance().getTime();
        String message = "${params.PROJECT}" + "*" + "${params.FINISH_TIME}";
        int notificationIndex =
                createNotification("Check Edit Notification", imapUser, subject, message,
                        DISCNotificationEvents.SUCCESS, null, true);
        assertNotification("Check Edit Notification", notificationIndex, imapUser, subject,
                message, DISCNotificationEvents.SUCCESS, null);
        String editedSubject = notificationSubject + Calendar.getInstance().getTime();
        editNotification("Check Edit Notification", notificationIndex, testParams.getEditorUser(),
                editedSubject, message, DISCNotificationEvents.PROCESS_STARTED, null, true);
        assertNotification("Check Edit Notification", notificationIndex,
                testParams.getEditorUser(), editedSubject, message,
                DISCNotificationEvents.PROCESS_STARTED, null);
        checkNotificationNumber(notificationIndex + 1, "Check Edit Notification");
        Pair<String, List<String>> cronTime =
                Pair.of(ScheduleCronTimes.CRON_EVERYDAY.getCronTime(), Arrays.asList("59", "23"));
        openProjectDetailPage(projectTitle, testParams.getProjectId());
        createScheduleForProcess(projectTitle, testParams.getProjectId(),
                "Check Edit Notification", "/graph/successfulGraph.grf", cronTime, null);
        assertNewSchedule("Check Edit Notification", "successfulGraph.grf", cronTime, null);
        scheduleDetail.manualRun();
        scheduleDetail.assertLastExecutionDetails(true, true, false,
                "Basic/graph/successfulGraph.grf", DISCProcessTypes.GRAPH, 5);
        List<String> expectedParamValues = new ArrayList<String>();
        expectedParamValues.add(testParams.getProjectId());
        expectedParamValues.add("");
        waitForNotification(editedSubject, expectedParamValues);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"notification"})
    public void checkCancelEditNotification() throws InterruptedException, ParseException,
            MessagingException, IOException, JSONException {
        openProjectDetailPage(projectTitle, testParams.getProjectId());
        deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
                DISCProcessTypes.GRAPH, "Check Cancel Edit Notification", BASIC_GRAPH_LIST, true);
        String subject = notificationSubject + Calendar.getInstance().getTime();
        String message = "${params.PROJECT}" + "*" + "${params.FINISH_TIME}";
        int notificationIndex =
                createNotification("Check Cancel Edit Notification", imapUser, subject, message,
                        DISCNotificationEvents.SUCCESS, null, true);
        assertNotification("Check Cancel Edit Notification", notificationIndex, imapUser, subject,
                message, DISCNotificationEvents.SUCCESS, null);
        String editedSubject = notificationSubject + Calendar.getInstance().getTime();
        editNotification("Check Cancel Edit Notification", notificationIndex,
                testParams.getEditorUser(), editedSubject, message,
                DISCNotificationEvents.PROCESS_STARTED, null, false);
        assertNotification("Check Cancel Edit Notification", notificationIndex, imapUser, subject,
                message, DISCNotificationEvents.SUCCESS, null);
        checkNotificationNumber(notificationIndex + 1, "Check Cancel Edit Notification");
        Pair<String, List<String>> cronTime =
                Pair.of(ScheduleCronTimes.CRON_EVERYDAY.getCronTime(), Arrays.asList("59", "23"));
        openProjectDetailPage(projectTitle, testParams.getProjectId());
        createScheduleForProcess(projectTitle, testParams.getProjectId(),
                "Check Cancel Edit Notification", "/graph/successfulGraph.grf", cronTime, null);
        assertNewSchedule("Check Cancel Edit Notification", "successfulGraph.grf", cronTime, null);
        scheduleDetail.manualRun();
        scheduleDetail.assertLastExecutionDetails(true, true, false,
                "Basic/graph/successfulGraph.grf", DISCProcessTypes.GRAPH, 5);
        successfulEndTime =
                formatExecutionDateTime(successfulStartTime, successfulEndTime,
                        scheduleDetail.getLastExecutionDate(),
                        scheduleDetail.getLastExecutionTime()).get(1);

        List<String> expectedParamValues = new ArrayList<String>();
        expectedParamValues.add(testParams.getProjectId());
        expectedParamValues.add(successfulEndTime);
        waitForNotification(subject, expectedParamValues);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"notification"})
    public void checkRepeatedDataLoadingFailureNotification() throws InterruptedException,
            JSONException, MessagingException, IOException {
        openProjectDetailPage(projectTitle, testParams.getProjectId());
        String processName =
                "Check Repeated Failures Notification" + Calendar.getInstance().getTimeInMillis();
        deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
                DISCProcessTypes.GRAPH, processName, BASIC_GRAPH_LIST, true);
        Pair<String, List<String>> cronTime =
                Pair.of(ScheduleCronTimes.CRON_EVERYDAY.getCronTime(), Arrays.asList("59", "23"));
        createScheduleForProcess(projectTitle, testParams.getProjectId(), processName, null,
                cronTime, null);
        assertNewSchedule(processName, "errorGraph.grf", cronTime, null);
        String scheduleUrl = browser.getCurrentUrl();

        scheduleDetail.repeatManualRun(5, "Basic/graph/errorGraph.grf", DISCProcessTypes.GRAPH,
                false);
        String repeatedFailuresNotificationSubject =
                String.format(REPEATED_FAILURES_NOTIFICATION_SUBJECT, processName);
        String repeatedFailuresNotificationMessagePart1 =
                String.format(REPEATED_FAILURES_NOTIFICATION_MESSAGE_1, scheduleUrl,
                        "errorGraph.grf", processName, projectTitle, testParams.getProjectId());
        String repeatedFailuresNotificationMessagePart2 =
                String.format(REPEATED_FAILURES_NOTIFICATION_MESSAGE_2, scheduleUrl);
        System.out.println("Repeated Data Loading Failures Message: "
                + repeatedFailuresNotificationMessagePart1);
        waitForNotification(repeatedFailuresNotificationSubject,
                Arrays.asList(repeatedFailuresNotificationMessagePart1));
        waitForNotification(repeatedFailuresNotificationSubject,
                Arrays.asList(repeatedFailuresNotificationMessagePart2));
        waitForNotification(repeatedFailuresNotificationSubject,
                Arrays.asList(NOTIFICATION_SUPPORT_MESSAGE));

        scheduleDetail.repeatManualRun(25, "Basic/graph/errorGraph.grf", DISCProcessTypes.GRAPH,
                false);
        String scheduleDisabledNotificationSubject =
                String.format(SCHEDULE_DISABLED_NOTIFICATION_SUBJECT, processName);
        String scheduleDisabledNotificationMessagePart1 =
                String.format(SCHEDULE_DISABLED_NOTIFICATION_MESSAGE_1, scheduleUrl,
                        "errorGraph.grf", processName, projectTitle, testParams.getProjectId());
        String scheduleDisabledNotificationMessagePart2 =
                String.format(SCHEDULE_DISABLED_NOTIFICATION_MESSAGE_2, scheduleUrl);
        System.out.println("message: " + scheduleDisabledNotificationMessagePart1);
        System.out.println("message: " + scheduleDisabledNotificationMessagePart2);
        waitForNotification(scheduleDisabledNotificationSubject,
                Arrays.asList(scheduleDisabledNotificationMessagePart1));
        waitForNotification(scheduleDisabledNotificationSubject,
                Arrays.asList(scheduleDisabledNotificationMessagePart2));
        waitForNotification(scheduleDisabledNotificationSubject,
                Arrays.asList(NOTIFICATION_SUPPORT_MESSAGE));
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"notification"})
    public void checkEmptyStateNotificationList() throws InterruptedException, ParseException,
            MessagingException, IOException, JSONException {
        openProjectDetailPage(projectTitle, testParams.getProjectId());
        deployInProjectDetailPage(projectTitle, testParams.getProjectId(), "Basic",
                DISCProcessTypes.GRAPH, "Check Empty State Notification List", BASIC_GRAPH_LIST,
                true);
        openProjectDetailPage(projectTitle, testParams.getProjectId());
        projectDetailPage.getNotificationButton("Check Empty State Notification List").click();
        waitForElementVisible(discNotificationRules.getRoot());
        assertTrue(discNotificationRules.getEmptyStateMessage().contains(
                NOTIFICATION_RULES_EMPTY_STATE_MESSAGE));
        System.out.println("Notification Rules Empty State Message: "
                + discNotificationRules.getEmptyStateMessage());
        discNotificationRules.closeNotificationRulesDialog();
    }

    @Test(dependsOnGroups = {"notification"}, groups = {"tests"}, alwaysRun = true)
    public void deleteProcesses() throws InterruptedException {
        openProjectDetailByUrl(testParams.getProjectId());
        projectDetailPage.deleteAllProcesses();
    }

    @Test(dependsOnGroups = {"notification"}, groups = {"tests"})
    public void test() throws JSONException, InterruptedException {
        successfulTest = true;
    }

    private int createNotification(String processName, String email, String subject,
            String message, DISCNotificationEvents event, String customEventName, boolean isSaved)
            throws InterruptedException {
        openProjectDetailPage(projectTitle, testParams.getProjectId());
        projectDetailPage.getNotificationButton(processName).click();
        waitForElementVisible(discNotificationRules.getRoot());
        discNotificationRules.clickOnAddNotificationButton();
        int notificationIndex = discNotificationRules.getNotificationNumber() - 1;
        discNotificationRules.setNotificationFields(notificationIndex, email, subject, message,
                event, customEventName);
        if (isSaved)
            discNotificationRules.saveNotification(notificationIndex);
        else
            discNotificationRules.cancelSaveNotification(notificationIndex);
        Thread.sleep(2000);
        return notificationIndex;
    }

    private void assertNotification(String processName, int notificationIndex, String email,
            String subject, String message, DISCNotificationEvents event, String customEventName)
            throws InterruptedException {
        openProjectDetailPage(projectTitle, testParams.getProjectId());
        projectDetailPage.getNotificationButton(processName).click();
        waitForElementVisible(discNotificationRules.getRoot());
        if (notificationIndex >= 0) {
            assertTrue(discNotificationRules.isNotExpanded(notificationIndex));
            discNotificationRules.expandNotificationRule(notificationIndex);
            discNotificationRules.assertNotificationFields(processName, notificationIndex, email,
                    subject, message, event, customEventName);
        }
    }

    private void editNotification(String processName, int notificationIndex, String email,
            String subject, String message, DISCNotificationEvents event, String customEventName,
            boolean isSaved) throws InterruptedException {
        openProjectDetailPage(projectTitle, testParams.getProjectId());
        projectDetailPage.getNotificationButton(processName).click();
        waitForElementVisible(discNotificationRules.getRoot());
        discNotificationRules.expandNotificationRule(notificationIndex);
        if (!email.isEmpty())
            discNotificationRules.clearNotificationEmail(notificationIndex);
        if (!subject.isEmpty())
            discNotificationRules.clearNotificationSubject(notificationIndex);
        if (!message.isEmpty())
            discNotificationRules.clearNotificationMessage(notificationIndex);
        discNotificationRules.setNotificationFields(notificationIndex, email, subject, message,
                event, customEventName);
        if (isSaved)
            discNotificationRules.saveNotification(notificationIndex);
        else
            discNotificationRules.cancelSaveNotification(notificationIndex);
    }

    private void checkMailbox(ImapClient imapClient, String subject,
            List<String> expectedParamValues) throws InterruptedException, MessagingException,
            IOException {
        Message[] notification = new Message[0];
        for (int i = 0; i < 100 && notification.length <= 0; i++) {
            System.out.println("Wait for notification: " + subject);
            notification = imapClient.getMessagesFromInbox(FROM, subject);
            Thread.sleep(2000);
        }
        assertTrue(notification.length > 0, "Notification is not sent!");
        assertTrue(notification.length == 1, "More than 1 notification!");
        System.out.println("Notification message: " + notification.length);
        System.out.println("Notification subject: " + notification[0].getSubject());
        System.out.println("Notification content: " + notification[0].getContent().toString());
        ArrayList<String> paramValues = new ArrayList<String>();
        String notificationContent = notification[0].getContent().toString() + "*";
        while (!notificationContent.isEmpty()) {
            if (notificationContent.substring(0, notificationContent.indexOf("*")).contains(
                    "https://ea."))
                paramValues.add(notificationContent.substring(0, notificationContent.indexOf("*"))
                        .replace("https://ea.", "https://")
                        .replace(System.lineSeparator() + "   ", ""));
            else
                paramValues.add(notificationContent.substring(0, notificationContent.indexOf("*"))
                        .replace(System.lineSeparator() + "   ", ""));
            System.out.println("Param value: " + paramValues.get(paramValues.size() - 1));
            if (notificationContent.substring(notificationContent.indexOf("*")).equals("*"))
                notificationContent = "";
            else
                notificationContent =
                        notificationContent.substring(notificationContent.indexOf("*") + 1);
        }
        for (int i = 0; expectedParamValues != null && i < expectedParamValues.size(); i++) {
            assertTrue(paramValues.get(i).contains(expectedParamValues.get(i)),
                    "The expected param value is: " + expectedParamValues.get(i)
                            + ",but the displayed param value in message is: "
                            + paramValues.get(i));
            System.out.println("Param value is displayed well in notification: "
                    + paramValues.get(i));
        }
    }

    private void waitForNotification(String subject, List<String> expectedParamValues)
            throws InterruptedException, MessagingException, IOException {
        ImapClient imapClient = new ImapClient(imapHost, imapUser, imapPassword);
        try {
            System.out.println("Waiting for notification...");
            checkMailbox(imapClient, subject, expectedParamValues);
            successfulTest = true;
        } finally {
            imapClient.close();
        }
    }

    private void checkNotification(DISCNotificationEvents event, List<String> expectedParamValues)
            throws InterruptedException, MessagingException, IOException {
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
        }
        waitForNotification(notificationSubject, expectedParamValues);
    }

    private void checkNotificationNumber(int expectedNotificationNumber, String processName)
            throws InterruptedException {
        openProjectDetailPage(projectTitle, testParams.getProjectId());
        waitForElementVisible(projectDetailPage.getRoot());
        if (expectedNotificationNumber == 0) {
            assertEquals(projectDetailPage.getNotificationButton(processName).getText(),
                    "No notification rules");
        } else {
            String notificationNumber =
                    String.valueOf(expectedNotificationNumber) + " notification rule"
                            + (expectedNotificationNumber > 1 ? "s" : "");
            assertEquals(projectDetailPage.getNotificationButton(processName).getText(),
                    notificationNumber);
        }
        projectDetailPage.getNotificationButton(processName).click();
        waitForElementVisible(discNotificationRules.getRoot());
        assertEquals(expectedNotificationNumber, discNotificationRules.getNotificationNumber());
    }

    private void deleteNotification(String processName, int notificationIndex, boolean isConfirmed)
            throws InterruptedException {
        openProjectDetailPage(projectTitle, testParams.getProjectId());
        projectDetailPage.getNotificationButton(processName).click();
        waitForElementVisible(discNotificationRules.getRoot());
        discNotificationRules.deleteNotification(notificationIndex, isConfirmed);
    }

    private String getProcessUri(String url) {
        String processUri =
                "/gdc/projects/" + testParams.getProjectId() + "/dataload/"
                        + url.substring(url.indexOf("processes"), url.lastIndexOf("schedules") - 2);
        return processUri;
    }

    private List<String> formatExecutionDateTime(String startTime, String endTime,
            String executionDate, String executionTime) throws ParseException {
        List<String> formattedDateTime = new ArrayList<String>();
        startTime =
                dateFormat2.format(dateFormat1.parse(executionDate))
                        + "T"
                        + timeFormat2.format(timeFormat1.parse(executionTime.substring(0,
                                executionTime.indexOf("-") - 1)));
        formattedDateTime.add(startTime);
        endTime =
                dateFormat2.format(dateFormat1.parse(executionDate))
                        + "T"
                        + timeFormat2.format(timeFormat1.parse(executionTime
                                .substring(executionTime.indexOf("-") + 1)));
        formattedDateTime.add(endTime);
        System.out.println("Start time: " + startTime);
        System.out.println("End time: " + endTime);
        return formattedDateTime;
    }

    private List<String> getBasicEventParamValues(String processUri, String processName,
            String executable, String scheduleName, String scheduleId, String logLink,
            String startTime, String endTime, String errorMessage) {
        List<String> expectedParamValues = new ArrayList<String>();
        expectedParamValues.add(testParams.getProjectId());
        expectedParamValues.add(userProfileId);
        expectedParamValues.add(testParams.getUser());
        expectedParamValues.add(processUri);
        expectedParamValues.add(processUri.substring(processUri.lastIndexOf("/") + 1));
        if (processName != null)
            expectedParamValues.add(processName);
        expectedParamValues.add(executable);
        expectedParamValues.add(scheduleId);
        expectedParamValues.add(scheduleName);
        if (logLink != null)
            expectedParamValues.add(logLink);
        expectedParamValues.add(startTime.substring(0, startTime.lastIndexOf(":") - 1));
        if (endTime != null)
            expectedParamValues.add(endTime);
        if (errorMessage != null)
            expectedParamValues.add(errorMessage);
        return expectedParamValues;
    }
}
