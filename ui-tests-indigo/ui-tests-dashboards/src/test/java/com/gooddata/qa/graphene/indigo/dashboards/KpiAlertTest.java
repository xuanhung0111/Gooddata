package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog.TRIGGERED_WHEN_DROPS_BELOW;
import static com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog.TRIGGERED_WHEN_GOES_ABOVE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AVG_AMOUNT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.mail.ImapUtils.areMessagesArrived;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.joda.time.DateTime.now;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.UUID;

import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.enums.GDEmails;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;

public class KpiAlertTest extends AbstractDashboardTest {

    private static final String METRIC_IN_PERCENT = "M" + UUID.randomUUID().toString().substring(0, 6);

    private static final String KPI_ALERT_DIALOG_HEADER = "Email me when this KPI is";
    private static final String KPI_ALERT_THRESHOLD = "100"; // TODO: consider parsing value from KPI to avoid immediate alert trigger
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
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)),
                testParams.getProjectId());
        getMetricCreator().createAvgAmountMetric();
        indigoRestRequest.createAnalyticalDashboard(singletonList(createAmountKpi()));
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        addUserToProject(imapUser, UserRoles.ADMIN);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkNewKpiDoesNotHaveAlertSet() throws JSONException, IOException {
        // creating kpi using REST is not recommended for this use case
        initIndigoDashboardsPageWithWidgets().switchToEditMode()
                .addKpi(new KpiConfiguration.Builder().metric(METRIC_AVG_AMOUNT).dataSet(DATE_DATASET_CREATED).build())
                .saveEditModeWithWidgets();

        try {
            takeScreenshot(browser, "checkNewKpiDoesNotHaveAlertSet", getClass());
            assertFalse(getLastKpiAfterAlertsLoaded().hasSetAlert(), "Kpi shouldn't be set alert");
        } finally {
            indigoRestRequest.deleteWidgetsUsingCascade(indigoRestRequest.getKpiUri(METRIC_AVG_AMOUNT));
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkKpiAlertButtonVisibility() {
        Kpi kpi = initIndigoDashboardsPageWithWidgets().getFirstWidget(Kpi.class);

        takeScreenshot(browser, "checkKpiAlertButtonVisibility-shouldNotBeVisible", getClass());
        kpi.waitForAlertButtonNotVisible()
            .hoverAndClickKpiAlertButton()
            .waitForAlertButtonVisible();
        takeScreenshot(browser, "checkKpiAlertButtonVisibility-shouldBeVisible", getClass());
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkKpiAlertDialog() {
        Kpi kpi = initIndigoDashboardsPageWithWidgets().getFirstWidget(Kpi.class);

        kpi.hoverAndClickKpiAlertButton();

        takeScreenshot(browser, "checkKpiAlertDialog", getClass());
        assertTrue(kpi.hasAlertDialogOpen(), "Alert dialog should open");
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkKpiAlertDialogHeader() {
        KpiAlertDialog kpiAlertDialog =
                initIndigoDashboardsPageWithWidgets().getFirstWidget(Kpi.class).openAlertDialog();

        takeScreenshot(browser, "checkKpiAlertDialogHeader", getClass());
        assertEquals(kpiAlertDialog.getDialogHeader(), KPI_ALERT_DIALOG_HEADER);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkAddKpiAlert() throws JSONException, IOException {
        String kpiUri = addWidgetToWorkingDashboardFluidLayout(createAmountKpi(), 0);

        try {
            initIndigoDashboardsPageWithWidgets();
            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, KPI_ALERT_THRESHOLD);

            takeScreenshot(browser, "checkAddKpiAlert", getClass());
            assertTrue(getLastKpiAfterAlertsLoaded().hasSetAlert(), "Kpi should be set alert");
        } finally {
            indigoRestRequest.deleteWidgetsUsingCascade(kpiUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkKpiAlertUpdate() throws JSONException, IOException {
        String kpiUri = addWidgetToWorkingDashboardFluidLayout(createAmountKpi(), 0);

        try {
            String updatedThreshold = "200";
            KpiAlertDialog kpiAlertDialog;

            initIndigoDashboardsPageWithWidgets();
            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, KPI_ALERT_THRESHOLD);

            assertTrue(getLastKpiAfterAlertsLoaded().hasSetAlert(), "Kpi should be set alert");

            kpiAlertDialog = waitForFragmentVisible(indigoDashboardsPage)
                .getLastWidget(Kpi.class)
                .openAlertDialog();

            takeScreenshot(browser, "checkKpiAlertUpdate_before", getClass());
            assertEquals(kpiAlertDialog.getTriggeredWhen(), TRIGGERED_WHEN_GOES_ABOVE);
            assertEquals(kpiAlertDialog.getThreshold(), KPI_ALERT_THRESHOLD);

            setAlertForLastKpi(TRIGGERED_WHEN_DROPS_BELOW, updatedThreshold);

            assertTrue(getLastKpiAfterAlertsLoaded().hasSetAlert(), "Kpi should be set alert");

            kpiAlertDialog = waitForFragmentVisible(indigoDashboardsPage)
                .getLastWidget(Kpi.class)
                .openAlertDialog();

            takeScreenshot(browser, "checkKpiAlertUpdate_after", getClass());
            assertEquals(kpiAlertDialog.getTriggeredWhen(), TRIGGERED_WHEN_DROPS_BELOW);
            assertEquals(kpiAlertDialog.getThreshold(), updatedThreshold);
        } finally {
            indigoRestRequest.deleteWidgetsUsingCascade(kpiUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkKpiAlertDialogWithPercentMetric() throws JSONException, IOException {
        Metric percentMetric = createMetric(METRIC_IN_PERCENT, "SELECT 1", "#,##0%");
        addWidgetToWorkingDashboardFluidLayout(
                createKpiUsingRest(createDefaultKpiConfiguration(percentMetric, DATE_DATASET_CREATED)), 0);

        try {
            boolean hasPercentSymbol = initIndigoDashboardsPageWithWidgets()
                .getLastWidget(Kpi.class)
                .openAlertDialog()
                .hasInputSuffix();

            takeScreenshot(browser, "checkKpiAlertDialogWithPercentMetric", getClass());
            assertTrue(hasPercentSymbol, "Alert dialog should have input suffix");
        } finally {
            indigoRestRequest.deleteWidgetsUsingCascade(percentMetric.getUri());
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkKpiAlertWithDateFilter() throws JSONException, IOException {
        String kpiUri = addWidgetToWorkingDashboardFluidLayout(createAmountKpi(), 0);

        try {
            initIndigoDashboardsPageWithWidgets().openExtendedDateFilterPanel()
                .selectPeriod(DateRange.LAST_MONTH).apply();
            KpiAlertDialog kpiAlertDialog = indigoDashboardsPage.getLastWidget(Kpi.class)
                .openAlertDialog();

            assertFalse(kpiAlertDialog.hasAlertMessage(), "Alert dialog shouldn't have alert message");

            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, KPI_ALERT_THRESHOLD);

            assertTrue(getLastKpiAfterAlertsLoaded().hasSetAlert(), "Kpi should be set alert");

            waitForFragmentVisible(indigoDashboardsPage).openExtendedDateFilterPanel()
                .selectPeriod(DateRange.ALL_TIME).apply();

            boolean isAlertMessageDisplayed = indigoDashboardsPage.getLastWidget(Kpi.class)
                .openAlertDialog()
                .hasAlertMessage();

            assertTrue(isAlertMessageDisplayed, "Alert message should display");

        } finally {
            indigoRestRequest.deleteWidgetsUsingCascade(kpiUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkKpiAlertResetFilters() throws JSONException, IOException {
        String kpiUri = addWidgetToWorkingDashboardFluidLayout(createAmountKpi(), 0);

        try {
            initIndigoDashboardsPageWithWidgets().openExtendedDateFilterPanel().selectPeriod(DateRange.THIS_MONTH)
                .apply();

            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, KPI_ALERT_THRESHOLD);

            waitForFragmentVisible(indigoDashboardsPage).openExtendedDateFilterPanel()
                .selectPeriod(DateRange.THIS_QUARTER).apply();
            indigoDashboardsPage.getLastWidget(Kpi.class).openAlertDialog().applyAlertFilters();

            DateRange dateFilterSelection = indigoDashboardsPage.openExtendedDateFilterPanel().getSelectedDateFilter();
            assertEquals(dateFilterSelection, DateRange.THIS_MONTH);

        } finally {
            indigoRestRequest.deleteWidgetsUsingCascade(kpiUri);
        }
    }

    @DataProvider(name = "dateFilterProvider")
    public Object[][] dateFilterProvider() {
        return new Object[][] {
            {DateRange.ALL_TIME, ""},
            {DateRange.LAST_7_DAYS, "in last 7 days"},
            {DateRange.LAST_30_DAYS, "in last 30 days"},
            {DateRange.LAST_90_DAYS, "in last 90 days"},
            {DateRange.THIS_MONTH, "in this month"},
            {DateRange.LAST_MONTH, "in last month"},
            {DateRange.LAST_12_MONTHS, "in last 12 months"},
            {DateRange.THIS_QUARTER, "in this quarter"},
            {DateRange.LAST_QUARTER, "in last quarter"},
            {DateRange.LAST_4_QUARTERS, "in last 4 quarters"},
            {DateRange.THIS_YEAR, "in this year"},
            {DateRange.LAST_YEAR, "in last year"}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"}, dataProvider = "dateFilterProvider")
    public void checkKpiAlertMessageWithDateFilter(DateRange dateFilter, String alertDialogInfoText)
            throws JSONException, IOException {
        String kpiUri = addWidgetToWorkingDashboardFluidLayout(createAmountKpi(), 0);

        try {
            initIndigoDashboardsPageWithWidgets().openExtendedDateFilterPanel().selectPeriod(dateFilter).apply();

            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, KPI_ALERT_THRESHOLD);

            Kpi kpi = getLastKpiAfterAlertsLoaded();
            assertTrue(kpi.hasSetAlert(), "Kpi should be set alert");

            String kpiAlertDialogTextBefore = kpi
                .openAlertDialog()
                .getAlertDialogText();

            takeScreenshot(browser, "checkKpiAlertMessageWithDateFilter-" + dateFilter, getClass());
            assertEquals(kpiAlertDialogTextBefore, alertDialogInfoText);

            DateRange anotherDateFilter =
                    dateFilter.equals(DateRange.LAST_MONTH) ? DateRange.THIS_MONTH : DateRange.LAST_MONTH;
            waitForFragmentVisible(indigoDashboardsPage)
                .openExtendedDateFilterPanel().selectPeriod(anotherDateFilter).apply();
            String kpiAlertDialogTextAfter = indigoDashboardsPage.getLastWidget(Kpi.class)
                .openAlertDialog()
                .getAlertDialogText();

            assertEquals(kpiAlertDialogTextBefore, kpiAlertDialogTextAfter);

        } finally {
            indigoRestRequest.deleteWidgetsUsingCascade(kpiUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkKpiAlertDelete() throws JSONException, IOException {
        String kpiUri = addWidgetToWorkingDashboardFluidLayout(createAmountKpi(), 0);

        try {
            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, KPI_ALERT_THRESHOLD);

            takeScreenshot(browser, "checkKpiAlertDelete_before", getClass());
            assertTrue(getLastKpiAfterAlertsLoaded().hasSetAlert(), "Kpi should be set alert");

            deleteAlertForLastKpi();

            takeScreenshot(browser, "checkKpiAlertDelete_after", getClass());
            assertFalse(getLastKpiAfterAlertsLoaded().hasSetAlert(), "Kpi shouldn't be set alert");
        } finally {
            // working dashboard should not be empty after delete the kpi
            // because following method will delete it in that case
            indigoRestRequest.deleteWidgetsUsingCascade(kpiUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkKpiAlertValidationNumber() throws JSONException, IOException {
        String kpiUri = addWidgetToWorkingDashboardFluidLayout(createAmountKpi(), 0);

        try {
            KpiAlertDialog dialog = waitForFragmentVisible(indigoDashboardsPage)
                .getLastWidget(Kpi.class)
                .openAlertDialog();

            String message = dialog.selectTriggeredWhen(TRIGGERED_WHEN_GOES_ABOVE)
                .setThreshold("1-")
                .getAlertMessageText();
            assertEquals(message, "This number is not valid.\nDouble-check it and try again.");

            dialog.discardAlert();
        } finally {
            indigoRestRequest.deleteWidgetsUsingCascade(kpiUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void triggerAlertImmediatelyWhenHitThreshold() throws JSONException, IOException {
        final Metric numberMetric = createMetric("Metric-" + generateHashString(), "SELECT 100", "#,##0");
        final String numberKpiUri = createKpiUsingRest(createDefaultKpiConfiguration(numberMetric, DATE_DATASET_CREATED));

        addWidgetToWorkingDashboardFluidLayout(numberKpiUri, 0);
        logout();
        signInAtGreyPages(imapUser, imapPassword);

        try {
            Kpi numberKpi = initIndigoDashboardsPageWithWidgets().getLastWidget(Kpi.class);
            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, "50");
            takeScreenshot(browser, "Alert-triggered-immediately-right-after-setting", getClass());
            assertTrue(numberKpi.isAlertTriggered(), "Alert is not triggered");

            if (testParams.isClusterEnvironment()) {
                assertFalse(doActionWithImapClient(imapClient ->
                        areMessagesArrived(imapClient, GDEmails.NOREPLY, numberMetric.getTitle(), 1)),
                        "Alert email is sent to mailbox");
            }

            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, "70");
            takeScreenshot(browser, "Current-alert-still-trigger-if-new-setup-hit-threshold-again", getClass());
            assertTrue(numberKpi.isAlertTriggered(), "Alert is not triggered");

            if (testParams.isClusterEnvironment()) {
                assertFalse(doActionWithImapClient(imapClient ->
                        areMessagesArrived(imapClient, GDEmails.NOREPLY, numberMetric.getTitle(), 1)),
                        "Alert email is sent to mailbox");
            }

            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, "150");
            takeScreenshot(browser, "Alert-not-trigger-if-new-setup-not-hit-threshold", getClass());
            assertFalse(numberKpi.isAlertTriggered(), "Alert still triggered although condition is not full-filled");

        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
            getMdService().removeObjByUri(numberKpiUri);
            getMdService().removeObj(numberMetric);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void updateAlertWhenKpiValueIsEmpty() throws JSONException, IOException {
        String amountMetricUri = getMdService().getObjUri(getProject(), Metric.class, title(METRIC_AMOUNT));
        Attribute createdYearAttribute = getMdService().getObj(getProject(), Attribute.class, title(ATTR_YEAR_CREATED));
        String createdYearValueUri = getMdService()
                .getAttributeElements(createdYearAttribute)
                .stream()
                .filter(e -> String.valueOf(now().getYear()).equals(e.getTitle()))
                .findFirst()
                .get()
                .getUri();

        String expression = format("SELECT SUM([%s]) WHERE [%s] = [%s]",
                amountMetricUri, createdYearAttribute.getUri(), createdYearValueUri);

        final Metric nullValueMetric = createMetric("Metric-" + generateHashString(), expression, "#,##0");
        final String nullValueKpiUri = createKpiUsingRest(createDefaultKpiConfiguration(nullValueMetric, DATE_DATASET_CREATED));

        addWidgetToWorkingDashboardFluidLayout(nullValueKpiUri, 0);

        try {
            Kpi nullValueKpi = initIndigoDashboardsPageWithWidgets().getLastWidget(Kpi.class);
            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, "-1");

            takeScreenshot(browser, "Alert-triggered-for-empty-value-kpi", getClass());
            assertTrue(nullValueKpi.isAlertTriggered(), "Alert is not triggered");

            setAlertForLastKpi(TRIGGERED_WHEN_DROPS_BELOW, "1");
            assertTrue(nullValueKpi.isAlertTriggered(), "Alert is not triggered");

        } finally {
            getMdService().removeObjByUri(nullValueKpiUri);
            getMdService().removeObj(nullValueMetric);
        }
    }

    private Kpi getLastKpiAfterAlertsLoaded() {
        return waitForFragmentVisible(indigoDashboardsPage)
            .waitForWidgetsLoading()
            .waitForAlertsLoaded()
            .getLastWidget(Kpi.class);
    }
}
