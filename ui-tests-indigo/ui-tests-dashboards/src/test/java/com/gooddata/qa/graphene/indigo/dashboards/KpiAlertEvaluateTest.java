package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog.TRIGGERED_WHEN_GOES_ABOVE;
import static com.gooddata.qa.graphene.indigo.dashboards.common.DashboardsTest.DATE_FILTER_ALL_TIME;
import static com.gooddata.qa.graphene.utils.UrlParserUtils.replaceInUrl;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.utils.mail.ImapUtils.waitForMessages;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.md.Fact;
import com.gooddata.md.Restriction;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.enums.GDEmails;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.utils.http.RestUtils;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

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

    private void setupProjectMaql() throws JSONException, IOException {
        switchToAdmin();
        setupMaql(MAQL_PATH);
    }

    private void setupFeatureFlag() throws JSONException {
        ProjectRestUtils.setFeatureFlagInProject(getGoodDataClient(), testParams.getProjectId(),
                ProjectFeatureFlags.ENABLE_ANALYTICAL_DASHBOARDS, true);
    }

    @Test(dependsOnMethods = "createProject", groups = "desktop")
    public void setupProject() throws JSONException, IOException {
        setupProjectMaql();
        setupFeatureFlag();
    }

    @DataProvider(name = "alertsProvider")
    public Object[][] alertsProvider() {
        return new Object[][] {
            {"Fact", "select sum([%s])", "#,##0.00", "2"},
            {"Fact", "select sum([%s]) / 100", "#,##0.00%", "50"}
        };
    }

    @Test(dependsOnMethods = "setupProject", dataProvider = "alertsProvider", groups = "desktop")
    public void checkKpiAlertEvaluation(String factName, String metricTemplate, String format, String threshold)
            throws URISyntaxException, JSONException, IOException, MessagingException {

        Date testStartTime = new Date();
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

            waitForFragmentVisible(indigoDashboardsPage)
                    .waitForDashboardLoad()
                    .addWidget(kpi)
                    .selectDateFilterByName(DATE_FILTER_ALL_TIME)
                    .saveEditModeWithWidgets()
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

            waitForFragmentVisible(indigoDashboardsPage)
                    .switchToEditMode()
                    .deleteDashboard(true);

        } finally {
            switchToAdmin();
            if (metricUri != null) {
                RestUtils.deleteObject(restApiClient, metricUri);
            }
            UserManagementRestUtils.deleteUserByUri(restApiClient, userUri);
        }
    }

    private String generateImapUniqueUserEmail(String email) {
        String append = UUID.randomUUID().toString().substring(0, 6);
        return email.replace("@", "+dashboards_" + append + "@");
    }

    private String getDashboardLink(String emailContent) {
        Pattern pattern = Pattern.compile(KPI_LINK_CLASS + ".*?href=\"(.*?)\"");
        Matcher matcher = pattern.matcher(emailContent);

        return matcher.find() ? matcher.group(1) : null;
    }

    private String addImapUserToProject(String email, String password) throws ParseException, IOException, JSONException {
        String userUri = UserManagementRestUtils.createUser(getRestApiClient(), testParams.getUserDomain(), email,
                password);
        UserManagementRestUtils.addUserToProject(getRestApiClient(), testParams.getProjectId(),
                email, UserRoles.ADMIN);

        return userUri;
    }

    private String getLastMailContent(String subject, Date arrivedDate) throws IOException, MessagingException {
        return doActionWithImapClient(imapClient -> {
                    List<Message> messages =
                            waitForMessages(
                                    imapClient, GDEmails.NOREPLY, subject, arrivedDate, 1);
                    return messages.get(0).getContent().toString().trim();
                });
    }

    private String createMetricFromFact(String metricName, String factName, String template, String format)
            throws JSONException, IOException {

        String factUri = getMdService().getObjUri(getProject(), Fact.class, Restriction.title(factName));
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

    private void checkKpiAlertTriggered(String metricName, String dateFilter, Date testStartTime)
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
            .dataSet(dateDimension)
            .build();
    }

    private Kpi initDashboardsPageAndGetKpi(String metricName) {
        return initIndigoDashboardsPageWithWidgets()
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

    private void checkAlertViaEmail(String metricName, String dateFilter, Date testStartTime)
            throws IOException, MessagingException {
        log.info("Checking the alert via email notification...");
        String link = getDashboardLink(getLastMailContent(metricName, testStartTime));

        assertNotNull(link);
        browser.get(link);

        if (testParams.isHostProxy()) {
            replaceInUrl(browser, testParams.getHostProxy(), testParams.getHost());
        }
        Kpi checkKpi = waitForFragmentVisible(indigoDashboardsPage)
                .waitForDashboardLoad()
                .waitForAllKpiWidgetContentLoaded()
                .getKpiByHeadline(metricName);

        // check that alert is triggered and date filter is reset accordingly
        assertTrue(checkKpi.isAlertTriggered());
        takeScreenshot(browser, "checkKpiAlertEvaluation-alert-triggered-via-email-"+metricName, getClass());
        assertEquals(indigoDashboardsPage.getDateFilterSelection(), dateFilter);
    }
}
