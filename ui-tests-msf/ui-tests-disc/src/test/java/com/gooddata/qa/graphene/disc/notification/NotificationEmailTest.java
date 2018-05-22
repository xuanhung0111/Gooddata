package com.gooddata.qa.graphene.disc.notification;

import static com.gooddata.qa.graphene.entity.disc.NotificationRule.getVariablesFromMessage;
import static com.gooddata.qa.utils.mail.ImapUtils.getEmailBody;
import static com.gooddata.qa.utils.mail.ImapUtils.waitForMessages;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import javax.mail.Message;
import javax.mail.MessagingException;

import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestClient.RestProfile;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestRequest;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.Schedule;
import com.gooddata.qa.graphene.common.AbstractProcessTest;
import com.gooddata.qa.graphene.entity.disc.NotificationRule;
import com.gooddata.qa.graphene.enums.GDEmails;
import com.gooddata.qa.graphene.enums.disc.notification.Variable;
import com.gooddata.qa.graphene.enums.disc.notification.VariableList;
import com.gooddata.qa.graphene.enums.disc.schedule.Executable;
import com.gooddata.qa.graphene.enums.disc.schedule.ScheduleCronTime;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.disc.notification.NotificationRuleItem.NotificationEvent;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.PackageFile;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.ProcessType;
import com.gooddata.qa.graphene.fragments.disc.process.ProcessDetail;
import com.gooddata.qa.graphene.fragments.disc.schedule.ScheduleDetail;

public class NotificationEmailTest extends AbstractProcessTest {

    private static final String DATE_FORMAT_PATTERN_1 = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String DATE_FORMAT_PATTERN_2 = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    private static final String REPEATED_DATA_LOADING_FAILURE_SUBJECT = "Repeated data loading failure: \"%s\" process";

    private static final String REPEATED_DATA_LOADING_FAILURE_MESSAGE = "Hello, the %s schedule within "
            + "the \"%s\" process of your \"%s\" GoodData project (id: %s) has failed for the 5th time. We highly "
            + "recommend disabling failing schedules until the issues are addressed: Go to the schedule page "
            + "Click \"Disable\" button If you require assistance with troubleshooting data uploading, please "
            + "visit the GoodData Support Portal. At your service, The GoodData Team";

    private static final String SCHEDULE_DISABLED_SUBJECT = "Schedule disabled: \"%s\" process";

    private static final String SCHEDULE_DISABLED_MESSAGE = "Hello, the %s schedule within the \"%s\" process of "
            + "your \"%s\" GoodData project (id: %s) has been automatically disabled following its 30th "
            + "consecutive failure. To resume scheduled uploads from this process: Go to the schedule page "
            + "Click \"Enable\" If you require assistance with troubleshooting data uploading, please visit the "
            + "GoodData Support Portal. At your service, The GoodData Team";

    @BeforeClass(alwaysRun = true)
    public void initImapUser() {
        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void receiveEmailByProcessScheduledEvent() throws MessagingException, IOException, JSONException {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, Executable.SUCCESSFUL_GRAPH,
                    ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            NotificationRule notificationRule = new NotificationRule()
                    .withEmail(imapUser)
                    .withEvent(NotificationEvent.PROCESS_SCHEDULED)
                    .withSubject("Notification for process scheduled event " + generateHashString())
                    .withMessage(VariableList.PROCESS_SCHEDULED.buildMessage());

            ProcessDetail processDetail = initDiscProjectDetailPage().getProcess(process.getName());

            processDetail.openNotificationRuleDialog()
                    .createNotificationRule(notificationRule)
                    .closeDialog();

            processDetail.openSchedule(schedule.getName()).executeSchedule().waitForExecutionFinish();

            JSONObject lastExecutionDetail = getLastExecutionDetail(process.getId());
            Document emailContent = getNotificationEmailContent(notificationRule.getSubject());
            Map<String, String> variables = getVariablesFromMessage(emailContent.text());

            assertEquals(variables.get(Variable.PROJECT.getName()), testParams.getProjectId());
            assertEquals(variables.get(Variable.USER.getName()), getUserProfileId());
            assertEquals(variables.get(Variable.USER_EMAIL.getName()), testParams.getUser());
            assertEquals(variables.get(Variable.PROCESS_URI.getName()), process.getUri());
            assertEquals(variables.get(Variable.PROCESS_ID.getName()), process.getId());
            assertThat(variables.get(Variable.EXECUTABLE.getName()), containsString(Executable.SUCCESSFUL_GRAPH.getPath()));
            assertEquals(variables.get(Variable.SCHEDULE_ID.getName()), schedule.getId());
            assertEquals(variables.get(Variable.SCHEDULE_NAME.getName()), schedule.getName());
            assertTrue(isDateEqualWithinAcceptedThreshold(
                    parseDateFromPattern(variables.get(Variable.SCHEDULE_TIME.getName()), DATE_FORMAT_PATTERN_1),
                    parseDateFromPattern(lastExecutionDetail.getString("created"), DATE_FORMAT_PATTERN_2)));

        } finally {
            getProcessService().removeProcess(process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void receiveEmailByProcessStartedEvent() throws JSONException, IOException, MessagingException {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, Executable.SUCCESSFUL_GRAPH,
                    ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            NotificationRule notificationRule = new NotificationRule()
                    .withEmail(imapUser)
                    .withEvent(NotificationEvent.PROCESS_STARTED)
                    .withSubject("Notification for process started event " + generateHashString())
                    .withMessage(NotificationRule.buildMessage(Variable.PROCESS_ID, Variable.SCHEDULE_ID,
                            Variable.LOG, Variable.START_TIME));

            ProcessDetail processDetail = initDiscProjectDetailPage().getProcess(process.getName());

            processDetail.openNotificationRuleDialog()
                    .createNotificationRule(notificationRule)
                    .closeDialog();

            processDetail.openSchedule(schedule.getName()).executeSchedule().waitForExecutionFinish();

            JSONObject lastExecutionDetail = getLastExecutionDetail(process.getId());
            Document emailContent = getNotificationEmailContent(notificationRule.getSubject());
            Map<String, String> variables = getVariablesFromMessage(emailContent.text());

            assertEquals(variables.get(Variable.PROCESS_ID.getName()), process.getId());
            assertEquals(variables.get(Variable.SCHEDULE_ID.getName()), schedule.getId());
            assertEquals(variables.get(Variable.LOG.getName()), lastExecutionDetail.getString("logFileName"));
            assertTrue(isDateEqualWithinAcceptedThreshold(
                    parseDateFromPattern(variables.get(Variable.START_TIME.getName()), DATE_FORMAT_PATTERN_1),
                    parseDateFromPattern(lastExecutionDetail.getString("started"), DATE_FORMAT_PATTERN_2)));

        } finally {
            getProcessService().removeProcess(process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void receiveEmailBySuccessEvent() throws JSONException, IOException, MessagingException {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, Executable.SUCCESSFUL_GRAPH,
                    ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            NotificationRule notificationRule = new NotificationRule()
                    .withEmail(imapUser)
                    .withEvent(NotificationEvent.SUCCESS)
                    .withSubject("Notification for success event " + generateHashString())
                    .withMessage(NotificationRule.buildMessage(Variable.PROCESS_ID, Variable.SCHEDULE_ID,
                            Variable.FINISH_TIME));

            ProcessDetail processDetail = initDiscProjectDetailPage().getProcess(process.getName());

            processDetail.openNotificationRuleDialog()
                    .createNotificationRule(notificationRule)
                    .closeDialog();

            processDetail.openSchedule(schedule.getName()).executeSchedule().waitForExecutionFinish();

            JSONObject lastExecutionDetail = getLastExecutionDetail(process.getId());
            Document emailContent = getNotificationEmailContent(notificationRule.getSubject());
            Map<String, String> variables = getVariablesFromMessage(emailContent.text());

            assertEquals(variables.get(Variable.PROCESS_ID.getName()), process.getId());
            assertEquals(variables.get(Variable.SCHEDULE_ID.getName()), schedule.getId());
            assertTrue(isDateEqualWithinAcceptedThreshold(
                    parseDateFromPattern(variables.get(Variable.FINISH_TIME.getName()), DATE_FORMAT_PATTERN_1),
                    parseDateFromPattern(lastExecutionDetail.getString("finished"), DATE_FORMAT_PATTERN_2)));

        } finally {
            getProcessService().removeProcess(process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void receiveEmailByFailureEvent() throws JSONException, IOException, MessagingException {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, Executable.SHORT_TIME_ERROR_GRAPH,
                    ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            NotificationRule notificationRule = new NotificationRule()
                    .withEmail(imapUser)
                    .withEvent(NotificationEvent.FAILURE)
                    .withSubject("Notification for failure event " + generateHashString())
                    .withMessage(NotificationRule.buildMessage(Variable.PROCESS_ID, Variable.SCHEDULE_ID,
                            Variable.ERROR_MESSAGE));

            ProcessDetail processDetail = initDiscProjectDetailPage().getProcess(process.getName());

            processDetail.openNotificationRuleDialog()
                    .createNotificationRule(notificationRule)
                    .closeDialog();

            processDetail.openSchedule(schedule.getName()).executeSchedule().waitForExecutionFinish();

            JSONObject lastExecutionDetail = getLastExecutionDetail(process.getId());
            Document emailContent = getNotificationEmailContent(notificationRule.getSubject());
            Map<String, String> variables = getVariablesFromMessage(emailContent.text());

            assertEquals(variables.get(Variable.PROCESS_ID.getName()), process.getId());
            assertEquals(variables.get(Variable.SCHEDULE_ID.getName()), schedule.getId());
            assertEquals(variables.get(Variable.ERROR_MESSAGE.getName()),
                    lastExecutionDetail.getJSONObject("error").getString("message").replaceAll("\n\\s+", " "));

        } finally {
            getProcessService().removeProcess(process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void receiveEmailByCustomEvent() throws MessagingException, IOException {
        DataloadProcess process = createProcess(generateProcessName(), PackageFile.CTL_EVENT, ProcessType.CLOUD_CONNECT);

        try {
            Schedule schedule = createSchedule(process, Executable.CTL_GRAPH,
                    ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            NotificationRule notificationRule = new NotificationRule()
                    .withEmail(imapUser)
                    .withEvent(NotificationEvent.CUSTOM_EVENT)
                    .withCustomEventName("welcomeEvent")
                    .withSubject("Notification for custom event " + generateHashString())
                    .withMessage("params.CUSTOM==${params.hello}");

            ProcessDetail processDetail = initDiscProjectDetailPage().getProcess(process.getName());

            processDetail.openNotificationRuleDialog()
                    .createNotificationRule(notificationRule)
                    .closeDialog();

            processDetail.openSchedule(schedule.getName()).executeSchedule().waitForExecutionFinish();

            Document emailContent = getNotificationEmailContent(notificationRule.getSubject());
            assertEquals(emailContent.text(), "params.CUSTOM==World");

        } finally {
            getProcessService().removeProcess(process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkRepeatedDataLoadingFailureNotification()
            throws MessagingException, IOException, ParseException, JSONException {
        addUserToProject(imapUser, UserRoles.ADMIN);
        RestClient restClient = new RestClient(new RestProfile(testParams.getHost(), imapUser, imapPassword, true));

        logout();
        signInAtGreyPages(imapUser, imapPassword);

        DataloadProcess process = createProcess(restClient, generateProcessName(), PackageFile.BASIC,
                ProcessType.CLOUD_CONNECT);

        try {
            String cronExpression = parseTimeToCronExpression(LocalTime.now().minusMinutes(1));
            Schedule schedule = createSchedule(restClient, process, Executable.SHORT_TIME_ERROR_GRAPH, cronExpression);

            ScheduleDetail scheduleDetail = initScheduleDetail(schedule);
            executeScheduleWithSpecificTimes(scheduleDetail, 5);

            Document repeatedFailureEmailContent = getNotificationEmailContent(
                    format(REPEATED_DATA_LOADING_FAILURE_SUBJECT, process.getName()));

            assertEquals(repeatedFailureEmailContent.text(),
                    format(REPEATED_DATA_LOADING_FAILURE_MESSAGE, Executable.SHORT_TIME_ERROR_GRAPH.getName(),
                            process.getName(), projectTitle, testParams.getProjectId()));

            executeScheduleWithSpecificTimes(scheduleDetail, 25);
            Document scheduleDisabledEmailContent = getNotificationEmailContent(
                    format(SCHEDULE_DISABLED_SUBJECT, process.getName()));

            assertEquals(scheduleDisabledEmailContent.text(),
                    format(SCHEDULE_DISABLED_MESSAGE, Executable.SHORT_TIME_ERROR_GRAPH.getName(),
                            process.getName(), projectTitle, testParams.getProjectId()));

        } finally {
            getProcessService().removeProcess(process);
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    private Document getNotificationEmailContent(String emailSubject) throws MessagingException, IOException {
        return doActionWithImapClient(imapClient -> {
            Message message = waitForMessages(imapClient, GDEmails.NO_REPLY, emailSubject, 1).get(0);
            return Jsoup.parse(getEmailBody(message));
        });
    }

    private String getUserProfileId() throws ParseException, JSONException, IOException {
        UserManagementRestRequest userManagementRestRequest = new UserManagementRestRequest(
                new RestClient(getProfile(Profile.DOMAIN)), testParams.getProjectId());
        String userProfileUri = userManagementRestRequest.getUserProfileUri(testParams.getUserDomain(),
                testParams.getUser());
        return userProfileUri.split("/profile/")[1];
    }

    private boolean isDateEqualWithinAcceptedThreshold(LocalDateTime date1, LocalDateTime date2) {
        return Duration.between(date1, date2).getSeconds() <= 1;
    }

    private LocalDateTime parseDateFromPattern(String date, String pattern) {
        return LocalDateTime.parse(date, DateTimeFormatter.ofPattern(pattern));
    }

    private JSONObject getLastExecutionDetail(String processId)
            throws JSONException, IOException {
        return new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .getJsonObject(
                        format("/gdc/projects/%s/dataload/processes/%s/executions", testParams.getProjectId(), processId))
                .getJSONObject("executions")
                .getJSONArray("items")
                .getJSONObject(0)
                .getJSONObject("executionDetail");
    }
}
