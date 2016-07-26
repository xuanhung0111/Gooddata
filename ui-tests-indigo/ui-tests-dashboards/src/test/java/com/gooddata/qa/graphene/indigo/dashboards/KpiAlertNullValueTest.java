package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.md.Restriction.identifier;
import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog.TRIGGERED_WHEN_GOES_ABOVE;
import static com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog.TRIGGERED_WHEN_DROPS_BELOW;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createAnalyticalDashboard;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createKpiWidget;
import static java.lang.String.format;
import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.browser.BrowserUtils.canAccessGreyPage;
import static com.gooddata.qa.utils.mail.ImapUtils.waitForMessages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.Dataset;
import com.gooddata.md.Fact;
import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.entity.kpi.KpiMDConfiguration;
import com.gooddata.qa.graphene.enums.GDEmails;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonDirection;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonType;
import com.gooddata.qa.utils.mail.ImapClient;
import com.google.common.collect.Iterables;

public class KpiAlertNullValueTest extends AbstractProjectTest {

    private static final String DATASET_NAME = "User";

    private static final String DROP_BELOW_LIMIT = "dropped below the set limit";
    private static final String GONE_ABOVE_LIMIT = "has gone above the set limit";

    private List<String> kpiNames = new ArrayList<>();

    @BeforeClass(alwaysRun = true)
    public void initImapUser() throws Exception {
        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"precondition"})
    public void initIndigoDashboardWithKpi() throws JSONException, IOException {
        String csvFilePath = new CsvFile(DATASET_NAME)
                .columns(new CsvFile.Column("firstname"), new CsvFile.Column("number"), new CsvFile.Column("Date"))
                .rows("Chi", "10100000", "2015-09-01")
                .saveToDisc(testParams.getCsvFolder());

        uploadCSV(csvFilePath);

        String numberFactUri = getMdService().getObjUri(getProject(), Fact.class, title("Number"));
        String firstNameAttributeUri = getMdService().getObjUri(getProject(), Attribute.class, title("Firstname"));
        String firstNameValueUri = firstNameAttributeUri + "/elements?id=2";

        String dateDatasetUri = getMdService().getObjUri(getProject(), Dataset.class,
                identifier("date.dataset.dt"));

        String maqlExpression = format("SELECT SUM([%s]) WHERE [%s] = [%s]",
                numberFactUri, firstNameAttributeUri, firstNameValueUri);

        List<Metric> metrics = Arrays.asList(
                new Metric(generateUniqueMetricName(), maqlExpression, "#,##0"),
                new Metric(generateUniqueMetricName(), maqlExpression, "#,##0"),
                new Metric(generateUniqueMetricName(), maqlExpression, "[=NULL]empty;#,##0"),
                new Metric(generateUniqueMetricName(), maqlExpression, "#,##0%"),
                new Metric(generateUniqueMetricName(), maqlExpression, "#,##0%"));

        List<String> kpiUris = new ArrayList<>();

        for (Metric metric : metrics) {
            kpiNames.add(metric.getTitle());

            kpiUris.add(createKpiWidget(getRestApiClient(), testParams.getProjectId(),
                    new KpiMDConfiguration.Builder()
                    .title(metric.getTitle())
                    .metric(getMdService().createObj(getProject(), metric).getUri())
                    .dateDataSet(dateDatasetUri)
                    .comparisonType(ComparisonType.PREVIOUS_PERIOD)
                    .comparisonDirection(ComparisonDirection.GOOD)
                    .build()));
        }

        createAnalyticalDashboard(getRestApiClient(), testParams.getProjectId(), kpiUris);
    }

    @DataProvider(name = "kpiAlertProvider")
    public Object[][] kpiAlertProvider() {
        return new Object[][] {
            {kpiNames.get(0), TRIGGERED_WHEN_DROPS_BELOW, "2", true},
            {kpiNames.get(1), TRIGGERED_WHEN_GOES_ABOVE, "-2", false},
            {kpiNames.get(2), TRIGGERED_WHEN_GOES_ABOVE, "-2", false},
            {kpiNames.get(3), TRIGGERED_WHEN_DROPS_BELOW, "10", false},
            {kpiNames.get(4), TRIGGERED_WHEN_GOES_ABOVE, "-90", false}
        };
    }

    @Test(dependsOnMethods = "initIndigoDashboardWithKpi", dataProvider = "kpiAlertProvider",
            groups = "precondition")
    public void setAlertForAllKpis(String name, String triggerCondition, String threshold, boolean inviteUser)
            throws ParseException, IOException, JSONException {
        if (inviteUser) {
            addUserToProject(imapUser, UserRoles.ADMIN);

            logout();
            signInAtGreyPages(imapUser, imapPassword);
        }

        initIndigoDashboardsPageWithWidgets()
                .getKpiByHeadline(name)
                .openAlertDialog()
                .selectTriggeredWhen(triggerCondition)
                .setThreshold(threshold)
                .setAlert();
    }

    @DataProvider(name = "alertEmailProvider")
    public Object[][] alertEmailProvider() {
        return new Object[][] {
            {kpiNames.get(0), " is -", DROP_BELOW_LIMIT + " 2", true},
            {kpiNames.get(1), " is -", GONE_ABOVE_LIMIT + " -2", false},
            {kpiNames.get(2), " is - empty", GONE_ABOVE_LIMIT + " 2", false},
            {kpiNames.get(3), " is -", DROP_BELOW_LIMIT + " 10%", false},
            {kpiNames.get(4), " is -", GONE_ABOVE_LIMIT + " -90%", false}
        };
    }

    @Test(dependsOnGroups = "precondition", groups = "desktop", dataProvider = "alertEmailProvider")
    public void checkKpiAlertOnNullValue(String name, String emailSubject, String emailContent, boolean updateCsv)
            throws JSONException, MessagingException, IOException {
        if (updateCsv) {
            String updateCsvFilePath = new CsvFile("User-update")
                    .columns(new CsvFile.Column("firstname"), new CsvFile.Column("number"),
                            new CsvFile.Column("Date"))
                    .rows("Chi2", "10100000", "2015-09-01")
                    .saveToDisc(testParams.getCsvFolder());

            logout();
            signIn(canAccessGreyPage(browser), UserRoles.ADMIN);

            updateCsvDataset(DATASET_NAME, updateCsvFilePath);

            logout();
            signInAtGreyPages(imapUser, imapPassword);
        }

        Kpi kpi = initIndigoDashboardsPageWithWidgets().getKpiByHeadline(name);

        takeScreenshot(browser, "Kpi-with-name-" + name + "-is-triggered", getClass());
        assertTrue(kpi.isAlertTriggered(), "Kpi with name: " + name + " is not triggered");

        if (!testParams.isClusterEnvironment()) return;

        try (ImapClient imapClient = new ImapClient(imapHost, imapUser, imapPassword)) {
            Document alertEmail = getAlertEmail(imapClient, GDEmails.NOREPLY, name + emailSubject);
            assertTrue(doesEmailHaveContent(alertEmail, name + " " + emailContent),
                    "Alert email contains invalid content");
        }
    }

    private String generateUniqueMetricName() {
        return "sumOfNumber-" + UUID.randomUUID().toString().substring(0, 6);
    }

    private Document getAlertEmail(ImapClient imapClient, GDEmails from, String subject)
            throws MessagingException, IOException {
        List<Message> messages  = waitForMessages(imapClient, from, subject, 1);
        return Jsoup.parse(ImapClient.getEmailBody(Iterables.getLast(messages)));
    }

    private boolean doesEmailHaveContent(Document email, String content) {
        return email.getElementsByTag("td")
                .stream()
                .filter(e -> e.text().contains(content))
                .findFirst()
                .isPresent();
    }
}
