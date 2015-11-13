package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.GoodData;
import com.gooddata.md.MetadataService;
import com.gooddata.md.Metric;
import com.gooddata.project.Project;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.enums.GDEmails;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import static com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog.TRIGGERED_WHEN_GOES_ABOVE;
import static com.gooddata.qa.graphene.indigo.dashboards.common.DashboardsTest.DATE_FILTER_ALL_TIME;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import com.gooddata.qa.utils.http.RestUtils;
import static com.gooddata.qa.utils.http.RestUtils.getNoReplyEmail;
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
import org.testng.annotations.Test;

/**
 * Need to run on empty project because neither csv upload (upload.html) nor
 * webdav upload work for GoodSales demo project (old template, dli/sli)
 */
public class KpiAlertEvaluateTest extends AbstractProjectTest {

    private static final String KPI_METRIC = "M" + UUID.randomUUID().toString().substring(0, 6);
    private static final String KPI_DATE_DIMENSION = "Date dimension (templ:Minimalistic)";
    private static final String KPI_LINK_CLASS = "s-kpi-link";

    private static final String MAQL_PATH = "/minimalistic/minimalistic-maql.txt";

    private static final String CSV_PATH = "/minimalistic/minimalistic.csv";
    private static final String UPLOADINFO_PATH = "/minimalistic/upload_info.json";

    private static final String CSV_INCREASED_PATH = "/minimalistic-increased/minimalistic-increased.csv";
    private static final String UPLOADINFO_INCREASED_PATH = "/minimalistic-increased/upload_info.json";

    private static final String INCREASED_VALUE = "2";

    private static final KpiConfiguration kpi = new KpiConfiguration.Builder()
        .metric(KPI_METRIC)
        .dateDimension(KPI_DATE_DIMENSION)
        .build();

    @BeforeClass(alwaysRun = true)
    public void setUpImap() throws Exception {
        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"desktop"})
    public void checkKpiAlertEvaluation() throws URISyntaxException, JSONException, IOException, MessagingException {
        long testStartTime = new Date().getTime();
        String imapUniqueUser = generateImapUniqueUserEmail(imapUser);
        String userUri = addImapUserToProject(imapUniqueUser, imapPassword);
        String from = getNoReplyEmail(restApiClient);

        String metricUri = null;

        try {
            switchToAdmin();
            setupMaql(MAQL_PATH);
            setupData(CSV_PATH, UPLOADINFO_PATH);
            switchToUser(imapUniqueUser, imapPassword);

            metricUri = createMetric(KPI_METRIC, "Fact");

            initIndigoDashboardsPage()
                    .getSplashScreen()
                    .startEditingWidgets();

            indigoDashboardsPage
                    .waitForDashboardLoad()
                    .addWidget(kpi)
                    .saveEditModeWithKpis()
                    .selectDateFilterByName(DATE_FILTER_ALL_TIME)
                    .getFirstKpi()
                    .openAlertDialog()
                    .selectTriggeredWhen(TRIGGERED_WHEN_GOES_ABOVE)
                    .setThreshold(INCREASED_VALUE)
                    .setAlert();

            switchToAdmin();
            setupData(CSV_INCREASED_PATH, UPLOADINFO_INCREASED_PATH);
            switchToUser(imapUniqueUser, imapPassword);

            // metric name is in mail subject and is unique
            String link = getDashboardLink(getLastMailContent(from, KPI_METRIC, testStartTime));

            assertNotNull(link);
            browser.get(link);

            Kpi firstKpi = indigoDashboardsPage
                    .waitForDashboardLoad()
                    .waitForAllKpiWidgetContentLoaded()
                    .getFirstKpi();

            takeScreenshot(browser, "checkKpiAlertEvaluation-alert-triggered", getClass());

            // check that alert is triggered and date filter is reset accordingly
            assertTrue(firstKpi.isAlertTriggered());
            assertEquals(indigoDashboardsPage.getDateFilterSelection(), DATE_FILTER_ALL_TIME);

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

    private String getLastMailContent(String from, String subject, long messagesArrivedAfter) throws IOException, MessagingException {
        Collection<Message> messages = ImapUtils.waitForMessageWithExpectedReceivedTime(
                getImapClient(), new GDEmails(from, 5), subject, messagesArrivedAfter);

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

    private String createMetric(String metricName, String factName) throws JSONException, IOException {
        GoodData gdClient = getGoodDataClient();
        MetadataService mdService = gdClient.getMetadataService();
        Project project = gdClient.getProjectService().getProjectById(testParams.getProjectId());
        String factUri = RestUtils.getFactUriByName(restApiClient, testParams.getProjectId(), factName);
        Metric customMetric = mdService.createObj(project,
                new Metric(metricName, "select sum(["+factUri+"])", "#,##0.00"));

        return customMetric.getUri();
    }

    private void switchToAdmin() throws JSONException {
        logout();
        signIn(true, UserRoles.ADMIN);
    };

    private void switchToUser(String username, String password) throws JSONException {
        logout();
        signInAtGreyPages(username, password);
    }
}
