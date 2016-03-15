package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.md.Restriction.identifier;
import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.browser.BrowserUtils.canAccessGreyPage;
import static com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog.TRIGGERED_WHEN_DROPS_BELOW;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createAnalyticalDashboard;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createKpiWidget;
import static java.lang.String.format;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import com.gooddata.md.Dimension;
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
import com.gooddata.qa.utils.mail.ImapUtils;
import com.google.common.collect.Iterables;

public class KpiValueFormatInAlertEmailTest extends AbstractProjectTest {

    private static final String DATASET_NAME = "User";

    private List<String> kpiNames = new ArrayList<>();

    private CsvFile csvFile;

    @BeforeClass(alwaysRun = true)
    public void initImapUser() throws Exception {
        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");
    }

    @Test(dependsOnMethods = "createProject", groups = "precondition")
    public void initIndigoDashboardWithKpi() throws JSONException, IOException {
        csvFile = new CsvFile(DATASET_NAME)
                .columns(new CsvFile.Column("firstname"), new CsvFile.Column("number"), new CsvFile.Column("Date"))
                .rows("Khoa", "10000", "2015-09-01");
        csvFile.saveToDisc(testParams.getCsvFolder());

        uploadCSV(csvFile.getFilePath());

        String numberFactUri = getMdService().getObjUri(getProject(), Fact.class, title("Number"));
        String firstNameAttributeUri = getMdService().getObjUri(getProject(), Attribute.class, title("Firstname"));
        String firstNameValueUri = firstNameAttributeUri + "/elements?id=2";

        String dateDimensionUri = getMdService().getObjUri(getProject(), Dimension.class,
                identifier("date.dim_date"));

        String maqlExpression = format("SELECT SUM([%s]) WHERE [%s] = [%s]",
                numberFactUri, firstNameAttributeUri, firstNameValueUri);

        List<Metric> metrics = Arrays.asList(
                new Metric(generateUniqueMetricName(), maqlExpression, "[<15000]#,##0.00 $"),
                new Metric(generateUniqueMetricName(), maqlExpression, "[=10000]#,##0 USD"),
                new Metric(generateUniqueMetricName(), maqlExpression, "[>5000]#,##0.0 VND"));

        List<String> kpiUris = new ArrayList<>();

        for (Metric metric : metrics) {
            kpiNames.add(metric.getTitle());

            kpiUris.add(createKpiWidget(getRestApiClient(), testParams.getProjectId(),
                    new KpiMDConfiguration.Builder()
                            .title(metric.getTitle())
                            .metric(getMdService().createObj(getProject(), metric).getUri())
                            .dateDataSet(dateDimensionUri)
                            .comparisonType(ComparisonType.PREVIOUS_PERIOD)
                            .comparisonDirection(ComparisonDirection.GOOD)
                            .build()));
        }

        createAnalyticalDashboard(getRestApiClient(), testParams.getProjectId(), kpiUris);
    }

    @Test(dependsOnMethods = "initIndigoDashboardWithKpi", groups = "precondition")
    public void setAlertForAllKpis()
            throws ParseException, IOException, JSONException {
        addUserToProject(imapUser, UserRoles.ADMIN);

        logout();
        signInAtGreyPages(imapUser, imapPassword);

        for (String kpiName : kpiNames) {
            initIndigoDashboardsPageWithWidgets()
                    .getKpiByHeadline(kpiName)
                    .openAlertDialog()
                    .selectTriggeredWhen(TRIGGERED_WHEN_DROPS_BELOW)
                    .setThreshold("15000")
                    .setAlert();
        }
    }

    @DataProvider(name = "kpiProvider")
    public Object[][] kpiProvider() {
        return new Object[][] {
            {kpiNames.get(0), "10,000.00 $", true},
            {kpiNames.get(1), "10,000 USD", false},
            {kpiNames.get(2), "10,000.0 VND", false}
        };
    }

    @Test(dependsOnGroups = "precondition", groups = "desktop", dataProvider = "kpiProvider")
    public void checkKpiValueFormatInEmail(String kpiName, String kpiValue, boolean updateCsv)
            throws JSONException, IOException, MessagingException {
        if (updateCsv) {
            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.ADMIN);

            updateCsvDataset(DATASET_NAME, csvFile.getFilePath());

            logout();
            signInAtGreyPages(imapUser, imapPassword);
        }

        Kpi kpi = initIndigoDashboardsPageWithWidgets().getKpiByHeadline(kpiName);

        takeScreenshot(browser, "Kpi-with-name-" + kpiName + "-is-triggered", getClass());

        assertTrue(kpi.isAlertTriggered(), "Kpi with name: " + kpiName + " is not triggered");
        assertEquals(kpi.getValue(), kpiValue);

        if (!testParams.isClusterEnvironment()) return;

        Document alertEmail = doActionWithImapClient(imapClient ->
                getAlertEmail(imapClient, GDEmails.NOREPLY, kpiName + " is " + kpiValue));

        assertTrue(doesEmailHaveContent(alertEmail, kpiValue),
                "Kpi value does not appear in alert email or has invalid format");
    }

    private String generateUniqueMetricName() {
        return "sumOfNumber-" + UUID.randomUUID().toString().substring(0, 6);
    }

    private Document getAlertEmail(ImapClient imapClient, GDEmails from, String subject)
            throws MessagingException, IOException {
        Collection<Message> messages = ImapUtils.waitForMessage(imapClient, from, subject);
        return Jsoup.parse(ImapClient.getEmailBody(Iterables.getLast(messages)));
    }

    private boolean doesEmailHaveContent(Document email, final String content) {
        return email.getElementsByTag("td")
                .stream()
                .filter(e -> e.text().contains(content))
                .findFirst()
                .isPresent();
    }
}
