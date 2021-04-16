package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.ElementUtils.BY_ERROR_MESSAGE_BAR;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog.TRIGGERED_WHEN_GOES_ABOVE;
import static com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog.TRIGGERED_WHEN_DROPS_BELOW;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.mail.ImapUtils.areMessagesArrived;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static com.gooddata.sdk.model.md.Restriction.identifier;
import static com.gooddata.sdk.model.md.Restriction.title;

import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.UUID;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.AttributeFilter;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.apache.http.ParseException;
import org.hamcrest.Description;
import org.json.JSONException;
import org.testng.annotations.Test;
import org.jsoup.nodes.Document;

import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.sdk.model.md.Dataset;
import com.gooddata.sdk.model.md.Fact;
import com.gooddata.sdk.model.md.Metric;
import com.gooddata.qa.graphene.enums.GDEmails;
import com.gooddata.qa.graphene.entity.kpi.KpiMDConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonType;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonDirection;

public class KpiBrokenAlertAndFilterTest extends AbstractDashboardTest {

    private static final String KPI_ALERT_THRESHOLD = "100";
    private static final String DATASET_NAME = "User";
    private static final String ATTR_FISRTNAME = "Firstname";

    private String csvFilePath;
    private String otherCsvFilePath;

    private String numberFactUri;
    private String dateDatasetUri;

    private String sumOfNumberMetricUri;
    private IndigoRestRequest indigoRestRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");
    }

    @Override
    protected void customizeProject() throws Throwable {
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)),testParams.getProjectId());
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"precondition"})
    public void inviteUserToProject() throws ParseException, IOException, JSONException {
        addUserToProject(imapUser, UserRoles.ADMIN);
        logoutAndLoginAs(imapUser, imapPassword);
    }

    @Test(dependsOnMethods = {"inviteUserToProject"}, groups = "precondition")
    public void initData() throws IOException {
        csvFilePath = new CsvFile(DATASET_NAME)
            .columns(new CsvFile.Column("Firstname"), new CsvFile.Column("Lastname"), new CsvFile.Column("Number"), new CsvFile.Column("Date"))
            .rows("Khoa", "Nguyen", "15", getCurrentDate())
            .rows("Tung", "Duong", "10", getCurrentDate())
            .rows("Khanh", "Pham", "5", getCurrentDate())
            .saveToDisc(testParams.getCsvFolder());

        otherCsvFilePath = new CsvFile("Update")
            .columns(new CsvFile.Column("Firstname"), new CsvFile.Column("Lastname"), new CsvFile.Column("Number"), new CsvFile.Column("Date"))
            .rows("Khoa", "Nguyen", "5", getCurrentDate())
            .rows("Tung", "Duong", "2", getCurrentDate())
            .rows("Khanh", "Pham", "1", getCurrentDate())
            .saveToDisc(testParams.getCsvFolder());

        uploadCSV(csvFilePath);

        numberFactUri = getMdService().getObjUri(getProject(), Fact.class, title("Number"));
        dateDatasetUri = getMdService().getObjUri(getProject(), Dataset.class,identifier("date.dataset.dt"));

        String maqlExpression = format("SELECT SUM([%s])", numberFactUri);
        sumOfNumberMetricUri = getMdService().createObj(getProject(), new Metric("SumOfNumber", maqlExpression, "#,##0")).getUri();
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"}, description="This test covers RAIL-3067 and RAIL-3062")
    public void checkBrokenAlertRemoveAttributeFilter() throws JSONException, IOException {
        String kpiUri = addWidgetToWorkingDashboardFluidLayout(createNumOfActivitiesKpi(), 0);
        try {
            initIndigoDashboardsPage().switchToEditMode().addAttributeFilter(ATTR_ACCOUNT)
                .addAttributeFilter(ATTR_DEPARTMENT, "Direct Sales").saveEditModeWithWidgets();

            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, KPI_ALERT_THRESHOLD);

            indigoDashboardsPage.switchToEditMode().deleteAttributeFilter(ATTR_ACCOUNT)
                .deleteAttributeFilter(ATTR_DEPARTMENT).saveEditModeWithWidgets();

            KpiAlertDialog kpiAlertDialog = getLastKpiAfterAlertsLoaded().openAlertDialog();
            assertEquals(kpiAlertDialog.getFilterSectionBrokenDialogHeader(), "FILTERS REMOVED FROM DASHBOARD");

            browser.navigate().refresh();
            waitForFragmentVisible(indigoDashboardsPage);
            assertEquals(waitForElementVisible(BY_ERROR_MESSAGE_BAR, browser).getText(), "Someone disabled or removed filters" +
                " that you use to watch for changes to your KPIs. To see the correct KPI values, remove the broken alerts, or update" +
                " the KPIs individually. Alternatively, enter edit mode and add the removed filters back, or re-enable the disabled filters.");

            getLastKpiAfterAlertsLoaded().openAlertDialog().updateFiltersOnBrokenAlert();
            getLastKpiAfterAlertsLoaded().openAlertDialog().deleteAlert();
            
        } finally {
            indigoRestRequest.deleteWidgetsUsingCascade(kpiUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"}, description="This test covers RAIL-3067 and RAIL-3062")
    public void checkBrokenAlertUncheckAttributeAndDateFilter() throws JSONException, IOException {
        String kpiUri = addWidgetToWorkingDashboardFluidLayout(createNumOfActivitiesKpi(), 0);

        try {
            initIndigoDashboardsPage().switchToEditMode()
                .addAttributeFilter(ATTR_ACCOUNT)
                .addAttributeFilter(ATTR_DEPARTMENT, "Direct Sales")
                .selectDateFilterByName(DATE_FILTER_THIS_MONTH)
                .saveEditModeWithWidgets();

            waitForFragmentVisible(indigoDashboardsPage);
            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, KPI_ALERT_THRESHOLD);

            indigoDashboardsPage.switchToEditMode().getLastWidget(Kpi.class).clickOnContent();
            indigoDashboardsPage.getConfigurationPanel().getFilterByAttributeFilter(ATTR_ACCOUNT).setChecked(false);
            indigoDashboardsPage.getConfigurationPanel().getFilterByAttributeFilter(ATTR_DEPARTMENT).setChecked(false);
            indigoDashboardsPage.getConfigurationPanel().getFilterByDateFilter().setChecked(false);
            indigoDashboardsPage.saveEditModeWithWidgets();

            KpiAlertDialog kpiAlertDialog = getLastKpiAfterAlertsLoaded().openAlertDialog();
            assertEquals(kpiAlertDialog.getFilterSectionBrokenDialogHeader(), "FILTERS IGNORED FOR THIS KPI");

            getLastKpiAfterAlertsLoaded().openAlertDialog().updateFiltersOnBrokenAlert();
            getLastKpiAfterAlertsLoaded().openAlertDialog().setAlert();

        } finally {
            indigoRestRequest.deleteWidgetsUsingCascade(kpiUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"}, description = "This test covers RAIL-3071")
    public void checkKpiAlertResetFilters() throws JSONException, IOException {
        String kpiUri = addWidgetToWorkingDashboardFluidLayout(createAmountKpi(), 0);

        try {
            initIndigoDashboardsPage().switchToEditMode().addAttributeFilter(ATTR_DEPARTMENT)
                .selectDateFilterByName(DATE_FILTER_THIS_MONTH).saveEditModeWithWidgets();

            waitForFragmentVisible(indigoDashboardsPage);
            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, KPI_ALERT_THRESHOLD);

            indigoDashboardsPage.switchToEditMode()
                .getAttributeFiltersPanel().getAttributeFilter(ATTR_DEPARTMENT).clearAllCheckedValues().selectByName("Direct Sales");
            indigoDashboardsPage.selectDateFilterByName(DATE_FILTER_THIS_QUARTER).saveEditModeWithWidgets();

            indigoDashboardsPage.getLastWidget(Kpi.class).openAlertDialog().applyAlertFilters();
            indigoDashboardsPage.waitForWidgetsLoading();

            DateRange dateFilterSelection = indigoDashboardsPage.openExtendedDateFilterPanel().getSelectedDateFilter();
            assertEquals(dateFilterSelection, DateRange.THIS_MONTH);

            AttributeFilter departmentFilter = indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilter(ATTR_DEPARTMENT);
            assertEquals(departmentFilter.getSelectedItems(), "All");

        } finally {
            indigoRestRequest.deleteWidgetsUsingCascade(kpiUri);
        }
    }

    @Test(dependsOnGroups = {"precondition"}, groups = "desktop", description = "This test covers RAIL-3082")
    public void openAlertFromEmailAndCheckFilters() throws JSONException, IOException {
        String kpiName = generateUniqueName();
        String kpiUri = createKpi(kpiName, sumOfNumberMetricUri);

        String indigoDashboardUri = indigoRestRequest.createAnalyticalDashboard(singletonList(kpiUri));
        initIndigoDashboardsPage().switchToEditMode().addAttributeFilter(ATTR_FISRTNAME, "Khoa")
            .selectDateFilterByName(DATE_FILTER_THIS_QUARTER).saveEditModeWithWidgets();
        try {
            setAlertForKpi(kpiName, TRIGGERED_WHEN_DROPS_BELOW, "10");
            updateCsvDataset(DATASET_NAME, otherCsvFilePath);
            Kpi kpi = initIndigoDashboardsPageWithWidgets().getWidgetByHeadline(Kpi.class, kpiName);
            takeScreenshot(browser, "Kpi-" + kpiName + "-alert-triggered", getClass());
            assertTrue(kpi.isAlertTriggered(), "Kpi alert is not triggered");
            assertTrue(doActionWithImapClient(imapClient -> areMessagesArrived(imapClient, GDEmails.NOREPLY, kpiName, 1)),
                "Alert email is not sent to mailbox");

            // Open alert dashboard via "View on Dashboard" button
            Document email = getLastAlertEmailContent(GDEmails.NOREPLY, kpiName);
            kpi = openDashboardFromLink(getDashboardLinkFromEmail(email,0)).getLastWidget(Kpi.class);
            assertTrue(kpi.isAlertTriggered(), "Kpi " + kpiName + " alert is not triggered");
            DateRange dateFilterSelection = indigoDashboardsPage.openExtendedDateFilterPanel().getSelectedDateFilter();
            assertEquals(dateFilterSelection, DateRange.THIS_QUARTER);
            AttributeFilter firstNameFilter = indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilter(ATTR_FISRTNAME);
            assertEquals(firstNameFilter.getSelectedItems(), "Khoa");

            // Open alert dashboard via "Update your limit or remove alert" link
            kpi = openDashboardFromLink(getDashboardLinkFromEmail(email, 1)).getLastWidget(Kpi.class);
            assertTrue(kpi.isAlertTriggered(), "Kpi " + kpiName + " alert is not triggered");
            assertEquals(dateFilterSelection, DateRange.THIS_QUARTER);
            assertEquals(firstNameFilter.getSelectedItems(), "Khoa");

        } finally {
            getMdService().removeObjByUri(indigoDashboardUri);
            updateCsvDataset(DATASET_NAME, csvFilePath);
        }
    }

    private Kpi getLastKpiAfterAlertsLoaded() {
        return waitForFragmentVisible(indigoDashboardsPage)
            .waitForWidgetsLoading()
            .waitForAlertsLoaded()
            .getLastWidget(Kpi.class);
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    private String generateUniqueName() {
        return "sumOfNumber-" + UUID.randomUUID().toString().substring(0, 6);
    }

    private String createKpi(String title, String metricUri) throws JSONException, IOException {
        return indigoRestRequest.createKpiWidget(
            new KpiMDConfiguration.Builder().title(title).metric(metricUri).dateDataSet(dateDatasetUri)
                .comparisonType(ComparisonType.PREVIOUS_PERIOD).comparisonDirection(ComparisonDirection.GOOD).build());
    }

    private void setAlertForKpi(String title, String triggerCondition, String threshold) {
        initIndigoDashboardsPageWithWidgets().getWidgetByHeadline(Kpi.class, title)
            .openAlertDialog().selectTriggeredWhen(triggerCondition).setThreshold(threshold).setAlert();
    }

    private String getDashboardLinkFromEmail(Document email, Integer index) {
        String dashboardUrl = email.getElementsByAttributeValueMatching("class", "s-kpi-link").get(index).attr("href");
        Pattern pattern = Pattern.compile("https://.*\\.com/");
        Matcher matcher = pattern.matcher(dashboardUrl);
        if (!matcher.find()) {
            throw new RuntimeException("Dashboard link not contain test host domain!");
        }
        return dashboardUrl.replace(matcher.group(), "");
    }

    private IndigoDashboardsPage openDashboardFromLink(String link) {
        openUrl(link);
        waitForOpeningIndigoDashboard();
        return IndigoDashboardsPage.getInstance(browser).waitForDashboardLoad();
    }

    private void logoutAndLoginAs(String username, String password) throws JSONException {
        logout();
        signInAtGreyPages(username, password);
    }
}
