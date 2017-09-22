package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.md.Restriction.identifier;
import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog.TRIGGERED_WHEN_DROPS_BELOW;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils.changeMetricExpression;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createAnalyticalDashboard;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createKpiWidget;
import static com.gooddata.qa.utils.mail.ImapUtils.areMessagesArrived;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils.deleteUserByEmail;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import com.gooddata.qa.utils.http.fact.FactRestUtils;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.jsoup.nodes.Document;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.Dataset;
import com.gooddata.md.Fact;
import com.gooddata.md.Metric;
import com.gooddata.md.ObjNotFoundException;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.entity.kpi.KpiMDConfiguration;
import com.gooddata.qa.graphene.enums.GDEmails;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonDirection;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonType;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;

public class KpiAlertSpecialCaseTest extends AbstractDashboardTest {

    private static final String DATASET_NAME = "User";
    private static final String NUMBER_VALUE = "15";

    private String csvFilePath;
    private String otherCsvFilePath;

    private String numberFactUri;
    private String dateDatasetUri;

    private String firstNameAttributeUri;
    private String firstNameValueUri;

    private String sumOfNumberMetricUri;

    @Override
    public void initProperties() {
        super.initProperties();
        // init imap properties
        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"precondition"})
    public void inviteUserToProject() throws ParseException, IOException, JSONException {
        addUserToProject(imapUser, UserRoles.ADMIN);
        logoutAndLoginAs(imapUser, imapPassword);
    }

    @Test(dependsOnMethods = "inviteUserToProject", groups = "precondition")
    public void initData() throws IOException {
        csvFilePath = new CsvFile(DATASET_NAME)
                .columns(new CsvFile.Column("Firstname"), new CsvFile.Column("Number"), new CsvFile.Column("Date"))
                .rows("Khoa", NUMBER_VALUE, getCurrentDate())
                .saveToDisc(testParams.getCsvFolder());

        otherCsvFilePath = new CsvFile("Update")
                .columns(new CsvFile.Column("Firstname"), new CsvFile.Column("Number"), new CsvFile.Column("Date"))
                .rows("Khoa", "5", getCurrentDate())
                .saveToDisc(testParams.getCsvFolder());

        uploadCSV(csvFilePath);

        numberFactUri = getMdService().getObjUri(getProject(), Fact.class, title("Number"));

        dateDatasetUri = getMdService().getObjUri(getProject(), Dataset.class,
                identifier("date.dataset.dt"));

        final Attribute firstNameAttribute =
                getMdService().getObj(getProject(), Attribute.class, identifier("attr.csv_user.firstname"));

        firstNameAttributeUri = firstNameAttribute.getUri();

        firstNameValueUri = getMdService().getAttributeElements(firstNameAttribute)
                .stream()
                .filter(e -> "Khoa".equals(e.getTitle()))
                .findFirst()
                .get()
                .getUri();

        String maqlExpression = format("SELECT SUM([%s])", numberFactUri);

        sumOfNumberMetricUri = getMdService()
                .createObj(getProject(), new Metric("SumOfNumber", maqlExpression, "#,##0"))
                .getUri();
    }

    @DataProvider(name = "metricProvider")
    public Object[][] getMetricProvider() {
        final String uniqueString = UUID.randomUUID().toString().substring(0, 6);

        final String yearAttributeUri = getMdService().getObjUri(getProject(), Attribute.class, identifier("date.year"));

        final String timeMacroExpression = format("SELECT SUM([%s]) WHERE [%s] = this", numberFactUri, yearAttributeUri);

        final String shareExpression = format("SELECT [%s] / (SELECT [%s] BY [%s], ALL OTHER WITHOUT PF)",
                sumOfNumberMetricUri, sumOfNumberMetricUri, firstNameAttributeUri);

        final String differenceExpression = format("SELECT [%s] - (SELECT [%s] BY ALL [%s] WHERE [%s] IN ([%s]) WITHOUT PF)",
                sumOfNumberMetricUri, sumOfNumberMetricUri, firstNameAttributeUri, firstNameAttributeUri, firstNameValueUri);

        final String ratioExpression = format("SELECT [%s] / [%s]", sumOfNumberMetricUri, sumOfNumberMetricUri);

        return new Object[][] {
            {createMetric("time-" + uniqueString, timeMacroExpression, "#,##0"), true},
            {createMetric("share-" + uniqueString, shareExpression, "#,##0"), false},
            {createMetric("diff-" + uniqueString, differenceExpression, "#,##0"), false},
            {createMetric("ratio-" + uniqueString, ratioExpression, "#,##0"), false}
        };
    }

    @Test(dependsOnGroups = {"precondition"}, groups = "desktop",
            dataProvider = "metricProvider")
    public void checkAlertWithSpecialMetricExpression(Metric metric, boolean checkEmail) throws JSONException, IOException {
        final String kpiName = metric.getTitle();
        final String kpiUri = createKpi(kpiName, metric.getUri());

        final String indigoDashboardUri = createAnalyticalDashboard(
                getRestApiClient(), testParams.getProjectId(), singletonList(kpiUri));

        try {
            setAlertForKpi(kpiName, TRIGGERED_WHEN_DROPS_BELOW, NUMBER_VALUE);

            updateCsvDataset(DATASET_NAME, otherCsvFilePath);

            Kpi kpi = initIndigoDashboardsPageWithWidgets().getWidgetByHeadline(Kpi.class, kpiName);

            takeScreenshot(browser, "Kpi-" + kpiName + "-alert-triggered", getClass());
            assertTrue(kpi.isAlertTriggered(), "Kpi alert is not triggered");

            // Due to ONE-934, alert now is triggered immediately when the current value full-fill with condition.
            // In this case, email will not be sent to mailbox.
            // Email is sent only when user set alert first then update the value to full-fill with condition.
            // However, some kind of special metric such as Share Metric, Difference Metric, Ratio Metric will
            // not update value no matter how we update the data. So the alert triggered for these metrics always fall into case 1.
            // That means there have no email sent to mail box
            if (!checkEmail) return;

            assertTrue(doActionWithImapClient(imapClient -> areMessagesArrived(imapClient, GDEmails.NOREPLY, kpiName, 1)),
                    "Alert email is not sent to mailbox");

        } finally {
            getMdService().removeObjByUri(indigoDashboardUri);
            getMdService().removeObj(metric);

            updateCsvDataset(DATASET_NAME, csvFilePath);
        }
    }

    @Test(dependsOnGroups = {"precondition"}, groups = "desktop")
    public void checkAlertWhenChangeMetricExpression() throws JSONException, IOException {
        String kpiName = generateUniqueName();
        String kpiUri = createKpi(kpiName, sumOfNumberMetricUri);

        String indigoDashboardUri = createAnalyticalDashboard(
                getRestApiClient(), testParams.getProjectId(), singletonList(kpiUri));

        try {
            setAlertForKpi(kpiName, TRIGGERED_WHEN_DROPS_BELOW, NUMBER_VALUE);

            String newMaqlExpression = format("SELECT SUM([%s]) WHERE [%s] = [%s]",
                    numberFactUri, firstNameAttributeUri, firstNameValueUri);

            changeMetricExpression(getRestApiClient(), sumOfNumberMetricUri, newMaqlExpression);

            updateCsvDataset(DATASET_NAME, otherCsvFilePath);

            Kpi kpi = initIndigoDashboardsPageWithWidgets().getWidgetByHeadline(Kpi.class, kpiName);

            takeScreenshot(browser, "Kpi-" + kpiName + "-alert-triggered", getClass());
            assertTrue(kpi.isAlertTriggered(), "Kpi alert is not triggered");

            assertTrue(doActionWithImapClient(imapClient -> areMessagesArrived(imapClient, GDEmails.NOREPLY, kpiName, 1)),
                    "Alert email is not sent to mailbox");

        } finally {
            getMdService().removeObjByUri(indigoDashboardUri);
            updateCsvDataset(DATASET_NAME, csvFilePath);
        }
    }

    @Test(dependsOnGroups = {"precondition"}, groups = "desktop")
    public void checkAlertOnRestrictedData() throws JSONException, IOException {

        final String metricExpression = format("SELECT SUM([%s])", numberFactUri);
        final Metric metric = createMetric("Metric-" + generateHashString(), metricExpression, "#,##0.00");
        final String kpiName = generateUniqueName();
        final String kpiUri = createKpi(kpiName, metric.getUri());


        String indigoDashboardUri = createAnalyticalDashboard(
                getRestApiClient(), testParams.getProjectId(), singletonList(kpiUri));

        try {
            FactRestUtils.setFactRestricted(getRestApiClient(), getProject(), numberFactUri );

            setAlertForKpi(kpiName, TRIGGERED_WHEN_DROPS_BELOW, NUMBER_VALUE);

            updateCsvDataset(DATASET_NAME, otherCsvFilePath);

            Kpi kpi = initIndigoDashboardsPageWithWidgets().getWidgetByHeadline(Kpi.class, kpiName);

            takeScreenshot(browser, "Kpi-" + kpiName + "-alert-triggered", getClass());
            assertTrue(kpi.isAlertTriggered(), "Kpi alert is triggered");

            assertTrue(doActionWithImapClient(imapClient -> areMessagesArrived(imapClient, GDEmails.NOREPLY, kpiName, 1)),
                    "Alert email is sent to mailbox");
        } finally {
            getMdService().removeObjByUri(indigoDashboardUri);
            updateCsvDataset(DATASET_NAME, csvFilePath);
            FactRestUtils.unsetFactRestricted(getRestApiClient(), getProject(), numberFactUri );
        }
    }

    @Test(dependsOnGroups = {"precondition"}, groups = "desktop")
    public void checkPlainMailSendWithAlert() throws JSONException, IOException {
        String kpiName = generateUniqueName();
        String kpiUri = createKpi(kpiName, sumOfNumberMetricUri);

        String indigoDashboardUri = createAnalyticalDashboard(
                getRestApiClient(), testParams.getProjectId(), singletonList(kpiUri));

        try {
            setAlertForKpi(kpiName, TRIGGERED_WHEN_DROPS_BELOW, NUMBER_VALUE);

            updateCsvDataset(DATASET_NAME, otherCsvFilePath);

            Kpi kpi = initIndigoDashboardsPageWithWidgets().getWidgetByHeadline(Kpi.class, kpiName);

            takeScreenshot(browser, "Kpi-" + kpiName + "-alert-triggered", getClass());
            assertTrue(kpi.isAlertTriggered(), "Kpi alert is not triggered");

            assertTrue(doActionWithImapClient(imapClient -> areMessagesArrived(imapClient, GDEmails.NOREPLY, kpiName, 1)),
                    "Alert email is not sent to mailbox");

            updateCsvDataset(DATASET_NAME, csvFilePath);

            kpi = initIndigoDashboardsPageWithWidgets().getWidgetByHeadline(Kpi.class, kpiName);

            takeScreenshot(browser, "Kpi-" + kpiName + "-alert-not-triggered", getClass());
            assertFalse(kpi.isAlertTriggered(), "Kpi alert is triggered although the condition is not reached");

            assertFalse(doActionWithImapClient(imapClient -> areMessagesArrived(imapClient, GDEmails.NOREPLY, kpiName, 2)),
                    "Alert email is sent to mailbox again");

            updateCsvDataset(DATASET_NAME, otherCsvFilePath);

            kpi = initIndigoDashboardsPageWithWidgets().getWidgetByHeadline(Kpi.class, kpiName);

            takeScreenshot(browser, "Kpi-" + kpiName + "-alert-triggered-again", getClass());
            assertTrue(kpi.isAlertTriggered(), "Kpi alert is not triggered");

            assertTrue(doActionWithImapClient(imapClient -> areMessagesArrived(imapClient, GDEmails.NOREPLY, kpiName, 2)),
                    "Alert email is not sent to mailbox");

        } finally {
            getMdService().removeObjByUri(indigoDashboardUri);
            updateCsvDataset(DATASET_NAME, csvFilePath);
        }
    }

    @Test(dependsOnGroups = {"precondition"}, groups = "desktop")
    public void removeKpiAfterSettingAlert() throws JSONException, IOException {
        String kpiName = generateUniqueName();
        String kpiUri = createKpi(kpiName, sumOfNumberMetricUri);

        String indigoDashboardUri = createAnalyticalDashboard(
                getRestApiClient(), testParams.getProjectId(), singletonList(kpiUri));

        try {
            setAlertForKpi(kpiName, TRIGGERED_WHEN_DROPS_BELOW, NUMBER_VALUE);

            getMdService().removeObjByUri(kpiUri);

            updateCsvDataset(DATASET_NAME, otherCsvFilePath);

            assertFalse(doActionWithImapClient(imapClient -> areMessagesArrived(imapClient, GDEmails.NOREPLY, kpiName, 1)),
                    "Alert email is sent to mailbox");

        } finally {
            getMdService().removeObjByUri(indigoDashboardUri);
            updateCsvDataset(DATASET_NAME, csvFilePath);
        }
    }

    @Test(dependsOnGroups = {"precondition"}, groups = "desktop")
    public void removeDashboardAfterSettingAlert() throws JSONException, IOException {
        String kpiName = generateUniqueName();
        String kpiUri = createKpi(kpiName, sumOfNumberMetricUri);

        String indigoDashboardUri = createAnalyticalDashboard(
                getRestApiClient(), testParams.getProjectId(), singletonList(kpiUri));

        try {
            setAlertForKpi(kpiName, TRIGGERED_WHEN_DROPS_BELOW, NUMBER_VALUE);

            getMdService().removeObjByUri(indigoDashboardUri);

            updateCsvDataset(DATASET_NAME, otherCsvFilePath);

            assertFalse(doActionWithImapClient(imapClient -> areMessagesArrived(imapClient, GDEmails.NOREPLY, kpiName, 1)),
                    "Alert email is sent to mailbox");

        } finally {
            updateCsvDataset(DATASET_NAME, csvFilePath);

            try {
                getMdService().removeObjByUri(indigoDashboardUri);
            } catch (ObjNotFoundException e) {
                // Indigo dashboard is already deleted in test and ignore this
            }
        }
    }

    @Test(dependsOnGroups = {"precondition"}, groups = "desktop")
    public void removeUserAfterSettingAlert() throws JSONException, IOException {
        String newImapUser = createDynamicUserFrom(imapUser);
        addUserToProject(newImapUser, UserRoles.ADMIN);

        String kpiName = generateUniqueName();
        String kpiUri = createKpi(kpiName, sumOfNumberMetricUri);

        String indigoDashboardUri = createAnalyticalDashboard(
                getRestApiClient(), testParams.getProjectId(), singletonList(kpiUri));

        try {
            logoutAndLoginAs(newImapUser, testParams.getPassword());

            setAlertForKpi(kpiName, TRIGGERED_WHEN_DROPS_BELOW, NUMBER_VALUE);

            logoutAndLoginAs(imapUser, imapPassword);

            RestApiClient restApiClient = testParams.getDomainUser() == null ? getRestApiClient() : getDomainUserRestApiClient();
            deleteUserByEmail(restApiClient, testParams.getUserDomain(), newImapUser);

            updateCsvDataset(DATASET_NAME, otherCsvFilePath);

            assertFalse(doActionWithImapClient(imapClient -> areMessagesArrived(imapClient, GDEmails.NOREPLY, kpiName, 1)),
                    "Alert email is sent to mailbox");

        } finally {
            logoutAndLoginAs(imapUser, imapPassword);
            getMdService().removeObjByUri(indigoDashboardUri);
            updateCsvDataset(DATASET_NAME, csvFilePath);
        }
    }

    @DataProvider(name = "embeddedTypeProvider")
    public Object[][] getEmbeddedTypeProvider() {
        return new Object[][] {
            {EmbeddedType.IFRAME},
            {EmbeddedType.URL}
        };
    }

    @Test(dependsOnGroups = {"precondition"}, dataProvider = "embeddedTypeProvider", groups = "desktop")
    public void setAlertInEmbeddedDashboard(EmbeddedType type) throws JSONException, IOException {
        String kpiName = generateUniqueName();
        String kpiUri = createKpi(kpiName, sumOfNumberMetricUri);

        String indigoDashboardUri = createAnalyticalDashboard(
                getRestApiClient(), testParams.getProjectId(), singletonList(kpiUri));

        ProjectRestUtils.setFeatureFlagInProject(getGoodDataClient(), testParams.getProjectId(),
                ProjectFeatureFlags.HIDE_KPI_ALERT_LINK, true);

        try {
            initEmbeddedIndigoDashboardPageByType(type)
                    .waitForDashboardLoad()
                    .waitForWidgetsLoading();

            setAlertForLastKpi(TRIGGERED_WHEN_DROPS_BELOW, NUMBER_VALUE);
            updateCsvDataset(DATASET_NAME, otherCsvFilePath);

            IndigoDashboardsPage indigoDashboardsPage = initEmbeddedIndigoDashboardPageByType(type)
                    .waitForDashboardLoad()
                    .waitForWidgetsLoading();

            takeScreenshot(browser, "Kpi-alert-triggered-in-embedded-dashboard-with-" + type, getClass());
            assertTrue(indigoDashboardsPage.getLastWidget(Kpi.class).isAlertTriggered(), "Kpi alert is not triggered");

            Document alertEmail = getLastAlertEmailContent(GDEmails.NOREPLY, kpiName);
            assertFalse(doesAlertEmailHaveContent(alertEmail, "View on dashboard"),
                    "The link to Embedded Kpi dashboard is not disabled");

        } finally {
            getMdService().removeObjByUri(indigoDashboardUri);

            ProjectRestUtils.setFeatureFlagInProject(getGoodDataClient(), testParams.getProjectId(),
                    ProjectFeatureFlags.HIDE_KPI_ALERT_LINK, false);

            updateCsvDataset(DATASET_NAME, csvFilePath);
        }
    }

    private String generateUniqueName() {
        return "sumOfNumber-" + UUID.randomUUID().toString().substring(0, 6);
    }

    private String createKpi(String title, String metricUri) throws JSONException, IOException {
        return createKpiWidget(getRestApiClient(), testParams.getProjectId(),
                new KpiMDConfiguration.Builder()
                        .title(title)
                        .metric(metricUri)
                        .dateDataSet(dateDatasetUri)
                        .comparisonType(ComparisonType.PREVIOUS_PERIOD)
                        .comparisonDirection(ComparisonDirection.GOOD)
                        .build());
    }

    private void setAlertForKpi(String title, String triggerCondition, String threshold) {
        initIndigoDashboardsPageWithWidgets()
                .getWidgetByHeadline(Kpi.class, title)
                .openAlertDialog()
                .selectTriggeredWhen(triggerCondition)
                .setThreshold(threshold)
                .setAlert();
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    private void logoutAndLoginAs(String username, String password) throws JSONException {
        logout();
        signInAtGreyPages(username, password);
    }
}
