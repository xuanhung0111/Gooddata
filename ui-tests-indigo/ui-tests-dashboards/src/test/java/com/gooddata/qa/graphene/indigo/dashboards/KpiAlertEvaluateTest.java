package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.enums.GDEmails;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import static com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog.TRIGGERED_WHEN_GOES_ABOVE;
import static com.gooddata.qa.graphene.indigo.dashboards.common.DashboardsTest.DATE_FILTER_ALL_TIME;
import static com.gooddata.qa.graphene.utils.NavigateUtils.replaceInUrl;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import com.gooddata.qa.utils.http.RestUtils;
import com.gooddata.qa.utils.mail.ImapClient;
import com.gooddata.qa.utils.mail.ImapUtils;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Message;
import javax.mail.MessagingException;
import org.apache.commons.io.IOUtils;
import org.apache.http.ParseException;
import org.json.JSONException;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Need to run on empty project because neither csv upload (upload.html) nor
 * webdav upload work for GoodSales demo project (old template, dli/sli)
 */
public class KpiAlertEvaluateTest extends AbstractProjectTest {

    private static final String KPI_DATE_DIMENSION = "templ:Minimalistic";
    private static final String KPI_LINK_CLASS = "s-kpi-link";

    private static final String MAQL_PATH = "/minimalistic/minimalistic-maql.txt";

    private static final String CSV_PATH = "/minimalistic/minimalistic.csv";
    private static final String UPLOADINFO_PATH = "/minimalistic/upload_info.json";

    private static final String CSV_INCREASED_PATH = "/minimalistic-increased/minimalistic-increased.csv";
    private static final String UPLOADINFO_INCREASED_PATH = "/minimalistic-increased/upload_info.json";

    @BeforeClass(alwaysRun = true)
    public void setUpImap() throws Exception {
        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");
    }

    @Test(dependsOnMethods = "createProject", groups = "desktop")
    public void setupProjectMaql() throws JSONException, IOException {
        switchToAdmin();
        setupMaql(MAQL_PATH);
    }

    @DataProvider(name = "alertsProvider")
    public Object[][] alertsProvider() {
        return new Object[][] {
            {"Fact", "select sum([%s])", "#,##0.00", "2"},
            {"Fact", "select sum([%s]) / 100", "#,##0.00%", "50"}
        };
    }

    @Test(dependsOnMethods = "setupProjectMaql", dataProvider = "alertsProvider", groups = "desktop")
    public void checkKpiAlertEvaluation(String factName, String metricTemplate, String format, String threshold)
            throws URISyntaxException, JSONException, IOException, MessagingException {

        long testStartTime = new Date().getTime();
        String imapUniqueUser = generateImapUniqueUserEmail(imapUser);
        String userUri = addImapUserToProject(imapUniqueUser, imapPassword);

        String metricUri = null;

        String metricName = "M" + UUID.randomUUID().toString().substring(0, 6);
        KpiConfiguration kpi = getKpiConfiguration(metricName, KPI_DATE_DIMENSION);

        try {
            switchToAdmin();
            setupData(CSV_PATH, UPLOADINFO_PATH);
            switchToUser(imapUniqueUser, imapPassword);

            metricUri = createMetricFromFact(metricName, factName, metricTemplate, format);

            initIndigoDashboardsPage()
                    .getSplashScreen()
                    .startEditingWidgets();

            indigoDashboardsPage
                    .waitForDashboardLoad()
                    .addWidget(kpi)
                    .saveEditModeWithKpis()
                    .selectDateFilterByName(DATE_FILTER_ALL_TIME)
                    .getKpiByHeadline(metricName)
                    .openAlertDialog()
                    .selectTriggeredWhen(TRIGGERED_WHEN_GOES_ABOVE)
                    .setThreshold(threshold)
                    .setAlert();

            switchToAdmin();
            setupData(CSV_INCREASED_PATH, UPLOADINFO_INCREASED_PATH);
            switchToUser(imapUniqueUser, imapPassword);

            // metric name is in mail subject and is unique
            checkKpiAlertTriggered(metricName, DATE_FILTER_ALL_TIME, testStartTime);

            indigoDashboardsPage
                    .switchToEditMode()
                    .deleteDashboard(true);

        } finally {
            switchToAdmin();
            if (metricUri != null) {
                RestUtils.deleteObject(restApiClient, metricUri);
            }
            RestUtils.deleteUser(restApiClient, userUri);
        }
    }

    private String generateImapUniqueUserEmail(String email) {
        String append = UUID.randomUUID().toString().substring(0, 6);
        return email.replace("@", "+dashboards_" + append + "@");
    }

    private ImapClient getImapClient() {
        return new ImapClient(imapHost, imapUser, imapPassword);
    }

    private String getDashboardLink(String emailContent) {
        Pattern pattern = Pattern.compile(KPI_LINK_CLASS + ".*?href=\"(.*?)\"");
        Matcher matcher = pattern.matcher(emailContent);

        return matcher.find() ? matcher.group(1) : null;
    }

    private String addImapUserToProject(String email, String password) throws ParseException, IOException, JSONException {
        String userUri = RestUtils.createNewUser(getRestApiClient(), email, password);
        RestUtils.addUserToProject(getRestApiClient(), testParams.getProjectId(),
                email, UserRoles.ADMIN);

        return userUri;
    }

    private String getLastMailContent(String subject, long messagesArrivedAfter) throws IOException, MessagingException {
        Collection<Message> messages = ImapUtils.waitForMessageWithExpectedReceivedTime(
                getImapClient(), GDEmails.NOREPLY, subject, messagesArrivedAfter);

        return Iterables.getLast(messages).getContent().toString().trim();
    }

    private void setupMaql(String maqlPath) throws JSONException, IOException {
        URL maqlResource = getClass().getResource(maqlPath);
        postMAQL(IOUtils.toString(maqlResource), 60);
    }

    private void setupData(String csvPath, String uploadInfoPath) throws JSONException, IOException, URISyntaxException {
        URL csvResource = getClass().getResource(csvPath);
        String webdavURL = uploadFileToWebDav(csvResource, null);

        URL uploadInfoResource = getClass().getResource(uploadInfoPath);
        uploadFileToWebDav(uploadInfoResource, webdavURL);

        postPullIntegration(webdavURL.substring(webdavURL.lastIndexOf("/") + 1, webdavURL.length()), 60);
    }

    private String createMetricFromFact(String metricName, String factName, String template, String format)
            throws JSONException, IOException {

        String factUri = RestUtils.getFactUriByName(restApiClient, testParams.getProjectId(), factName);
        return createMetric(metricName, template.replace("%s", factUri), format).getUri();
    }

    private void switchToAdmin() throws JSONException {
        logout();
        signIn(true, UserRoles.ADMIN);
    };

    private void switchToUser(String username, String password) throws JSONException {
        logout();
        signInAtGreyPages(username, password);
    }

    private void checkKpiAlertTriggered(String metricName, String dateFilter, long testStartTime)
            throws IOException, MessagingException {
        // check that dashboard alert worker triggered the alert in UI
        checkAlertInUI(metricName);

        if (!testParams.isClusterEnvironment()) return;

        // check via email notification link
        checkAlertViaEmail(metricName, dateFilter, testStartTime);
    }

    private KpiConfiguration getKpiConfiguration(String metricName, String dateDimension) {
        return new KpiConfiguration.Builder()
            .metric(metricName)
            .dateDimension(dateDimension)
            .build();
    }

    private Kpi initDashboardsPageAndGetKpi(String metricName) {
        return initIndigoDashboardsPage()
                .waitForDashboardLoad()
                .waitForAllKpiWidgetContentLoaded()
                .getKpiByHeadline(metricName);
    }

    private void checkAlertInUI(String metricName) {
        log.info("Checking the alert via UI...");
        for (int attempt = 0;; attempt++) {
            assertTrue(attempt < 10, "Maximum attempts to get triggered alert reached. Exiting.");

            if (initDashboardsPageAndGetKpi(metricName)
                    .isAlertTriggered()) {
                break;
            }

            sleepTightInSeconds(30);
        }
        takeScreenshot(browser, "checkKpiAlertEvaluation-alert-triggered-via-UI-"+metricName, getClass());
    }

    private void checkAlertViaEmail(String metricName, String dateFilter, long testStartTime)
            throws IOException, MessagingException {
        log.info("Checking the alert via email notification...");
        String link = getDashboardLink(getLastMailContent(metricName, testStartTime));

        assertNotNull(link);
        browser.get(link);

        if (testParams.isHostProxy()) {
            replaceInUrl(browser, testParams.getHostProxy(), testParams.getHost());
        }
        Kpi checkKpi = indigoDashboardsPage
                .waitForDashboardLoad()
                .waitForAllKpiWidgetContentLoaded()
                .getKpiByHeadline(metricName);

        // check that alert is triggered and date filter is reset accordingly
        assertTrue(checkKpi.isAlertTriggered());
        takeScreenshot(browser, "checkKpiAlertEvaluation-alert-triggered-via-email-"+metricName, getClass());
        assertEquals(indigoDashboardsPage.getDateFilterSelection(), dateFilter);
    }
}
