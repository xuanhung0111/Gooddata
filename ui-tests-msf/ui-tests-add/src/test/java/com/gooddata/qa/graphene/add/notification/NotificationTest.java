package com.gooddata.qa.graphene.add.notification;

import com.gooddata.dataload.processes.Schedule;
import com.gooddata.qa.graphene.common.AbstractDataloadProcessTest;
import com.gooddata.qa.graphene.entity.add.SyncDatasets;
import com.gooddata.qa.graphene.entity.ads.SqlBuilder;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.entity.disc.NotificationRule;
import com.gooddata.qa.graphene.entity.model.Dataset;
import com.gooddata.qa.graphene.entity.model.LdmModel;
import com.gooddata.qa.graphene.enums.GDEmails;
import com.gooddata.qa.graphene.enums.disc.notification.Variable;
import com.gooddata.qa.graphene.enums.process.Parameter;
import com.gooddata.qa.graphene.fragments.disc.notification.NotificationRuleItem;
import com.gooddata.qa.graphene.fragments.disc.notification.NotificationRuleItem.NotificationEvent;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.mail.Message;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import static com.gooddata.qa.graphene.entity.disc.NotificationRule.buildMessage;
import static com.gooddata.qa.graphene.entity.disc.NotificationRule.getVariablesFromMessage;
import static com.gooddata.qa.utils.mail.ImapUtils.getEmailBody;
import static com.gooddata.qa.utils.mail.ImapUtils.waitForMessages;
import static java.lang.String.join;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class NotificationTest extends AbstractDataloadProcessTest {

    private static final String ATTR_NAME = "name";
    private static final String ERROR_MESSAGE = "Mapping validation failed, see log for more details.";

    private final String SUCCESS_EMAIL_SUBJECT = "Notification for success event " + generateHashString();
    private final String PROCESS_STARTED_EMAIL_SUBJECT = "Notification for process started event " + generateHashString();
    private final String FAILURE_EMAIL_SUBJECT = "Notification for failure event " + generateHashString();

    private CsvFile opportunity;
    private CsvFile person;
    private Schedule schedule;

    @BeforeClass(alwaysRun = true)
    public void initImapUser() {
        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");
    }

    @Test(dependsOnGroups = {"initDataload"})
    public void checkParamExecutableReplacedByDataset() {
        NotificationRuleItem item = initDiscProjectDetailPage()
                .getDataloadProcess()
                .openNotificationRuleDialog()
                .clickAddNotificationRule();

        item.selectEvent(NotificationEvent.SUCCESS);
        assertThat(item.getVariables(), hasItem(Variable.DATASETS));
        assertThat(item.getVariables(), not(hasItem(Variable.EXECUTABLE)));

        item.selectEvent(NotificationEvent.PROCESS_STARTED);
        assertThat(item.getVariables(), hasItem(Variable.DATASETS));
        assertThat(item.getVariables(), not(hasItem(Variable.EXECUTABLE)));

        item.selectEvent(NotificationEvent.FAILURE);
        assertThat(item.getVariables(), hasItem(Variable.DATASETS));
        assertThat(item.getVariables(), not(hasItem(Variable.EXECUTABLE)));
    }

    @Test(dependsOnGroups = {"initDataload"})
    public void notShowCustomEventForDataloadProcess() {
        Collection<NotificationEvent> events = initDiscProjectDetailPage()
                .getDataloadProcess()
                .openNotificationRuleDialog()
                .clickAddNotificationRule()
                .getAvailableEvents();
        assertThat(events, not(hasItem(NotificationEvent.CUSTOM_EVENT)));
    }

    @Test(dependsOnGroups = {"initDataload"}, groups = {"precondition"})
    public void initData() throws JSONException {
        setupMaql(new LdmModel()
                .withDataset(new Dataset(DATASET_OPPORTUNITY)
                        .withAttributes(ATTR_NAME)
                        .withFacts(FACT_PRICE))
                .withDataset(new Dataset(DATASET_PERSON)
                        .withAttributes(ATTR_NAME)
                        .withFacts(FACT_AGE))
                .buildMaql());

        opportunity = new CsvFile(DATASET_OPPORTUNITY)
                .columns(new CsvFile.Column(ATTR_NAME), new CsvFile.Column(FACT_PRICE),
                        new CsvFile.Column(X_TIMESTAMP_COLUMN))
                .rows("Op1", "100", getCurrentDate());

        person = new CsvFile(DATASET_PERSON)
                .columns(new CsvFile.Column(ATTR_NAME), new CsvFile.Column(FACT_AGE),
                        new CsvFile.Column(X_TIMESTAMP_COLUMN))
                .rows("P1", "18", getCurrentDate());
    }

    @Test(dependsOnMethods = {"initData"}, groups = {"precondition"})
    public void createNotification() {
        NotificationRule successRule = new NotificationRule()
                .withEmail(imapUser)
                .withEvent(NotificationEvent.SUCCESS)
                .withSubject(SUCCESS_EMAIL_SUBJECT)
                .withMessage(Variable.DATASETS.getValue());

        NotificationRule processStartedRule = new NotificationRule()
                .withEmail(imapUser)
                .withEvent(NotificationEvent.PROCESS_STARTED)
                .withSubject(PROCESS_STARTED_EMAIL_SUBJECT)
                .withMessage(Variable.DATASETS.getValue());

        NotificationRule failureRule = new NotificationRule()
                .withEmail(imapUser)
                .withEvent(NotificationEvent.FAILURE)
                .withSubject(FAILURE_EMAIL_SUBJECT)
                .withMessage(buildMessage(Variable.DATASETS, Variable.ERROR_MESSAGE));

        initDiscProjectDetailPage()
                .getDataloadProcess()
                .openNotificationRuleDialog()
                .createNotificationRule(successRule)
                .createNotificationRule(processStartedRule)
                .createNotificationRule(failureRule)
                .closeDialog();
    }

    @Test(dependsOnGroups = {"precondition"})
    public void checkNotificationForFullLoadDataset() {
        Date timeReceiveEmail = getTimeReceiveEmail();

        executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                defaultParameters.get().addParameter(Parameter.SQL_QUERY, SqlBuilder.build(opportunity, person)));

        schedule = createScheduleForManualTrigger(generateScheduleName(), SyncDatasets.ALL);
        initScheduleDetail(schedule).executeSchedule().waitForExecutionFinish();

        Document processStartedNotification = getNotificationEmailContent(PROCESS_STARTED_EMAIL_SUBJECT, timeReceiveEmail);
        assertEquals(processStartedNotification.text(), "PERSON (full load), OPPORTUNITY (full load)");

        Document successNotification = getNotificationEmailContent(SUCCESS_EMAIL_SUBJECT, timeReceiveEmail);
        assertEquals(successNotification.text(), "PERSON (full load), OPPORTUNITY (full load)");
    }

    @Test(dependsOnMethods = {"checkNotificationForFullLoadDataset"})
    public void checkNotificationForIncrementalLoadDataset() {
        Date timeReceiveEmail = getTimeReceiveEmail();

        person.rows("P2", "20", getCurrentDate());
        executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                defaultParameters.get().addParameter(Parameter.SQL_QUERY, SqlBuilder.build(person)));

        Schedule schedule = createScheduleForManualTrigger(generateScheduleName(), SyncDatasets.custom(DATASET_PERSON));
        initScheduleDetail(schedule).executeSchedule().waitForExecutionFinish();

        Document processStartedNotification = getNotificationEmailContent(PROCESS_STARTED_EMAIL_SUBJECT, timeReceiveEmail);
        assertTrue(processStartedNotification.text().matches("PERSON \\(incremental load from .*\\)"),
                "Dataset notification not show as expected");

        Document successNotification = getNotificationEmailContent(SUCCESS_EMAIL_SUBJECT, timeReceiveEmail);
        assertTrue(successNotification.text().matches("PERSON \\(incremental load from .*\\)"),
                "Dataset notification not show as expected");
    }

    @Test(dependsOnMethods = {"checkNotificationForFullLoadDataset"})
    public void checkNotificationForErrorDatasetLoaded() {
        Date timeReceiveEmail = getTimeReceiveEmail();

        CsvFile personError = new CsvFile("person_error")
                .columns(new CsvFile.Column(ATTR_NAME), new CsvFile.Column(FACT_AGE),
                        new CsvFile.Column(X_TIMESTAMP_COLUMN))
                .rows("P1", "18", getCurrentDate());

        executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                defaultParameters.get().addParameter(Parameter.SQL_QUERY,
                        join(";", SqlBuilder.dropTables(DATASET_PERSON, DATASET_OPPORTUNITY), SqlBuilder.build(personError))));

        try {
            String errorMessage = initScheduleDetail(schedule)
                    .executeSchedule()
                    .waitForExecutionFinish()
                    .getLastExecutionHistoryItem()
                    .getErrorMessage();
            assertEquals(errorMessage, ERROR_MESSAGE);

            Document notification = getNotificationEmailContent(FAILURE_EMAIL_SUBJECT, timeReceiveEmail);
            Map<String, String> variables = getVariablesFromMessage(notification.text());
            assertEquals(variables.get(Variable.DATASETS.getName()), "");
            assertEquals(variables.get(Variable.ERROR_MESSAGE.getName()), errorMessage);
        } finally {
            executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                    defaultParameters.get().addParameter(Parameter.SQL_QUERY,
                            join(";", SqlBuilder.dropTables("person_error"), SqlBuilder.build(opportunity, person))));
        }
    }

    private Document getNotificationEmailContent(String emailSubject, Date timeReceiveAfter) {
        return doActionWithImapClient(imapClient -> {
            Message message = waitForMessages(imapClient, GDEmails.NO_REPLY, emailSubject, timeReceiveAfter, 1).get(0);
            return Jsoup.parse(getEmailBody(message));
        });
    }

    private String getCurrentDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"));
    }

    private Date getTimeReceiveEmail() {
        return new Date();
    }
}
