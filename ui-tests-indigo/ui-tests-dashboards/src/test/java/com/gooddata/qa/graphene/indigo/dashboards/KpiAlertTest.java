package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog.TRIGGERED_WHEN_DROPS_BELOW;
import static com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog.TRIGGERED_WHEN_GOES_ABOVE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AVG_AMOUNT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createAnalyticalDashboard;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.deleteWidgetsUsingCascase;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.getKpiUri;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.UUID;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog;
import com.gooddata.qa.graphene.indigo.dashboards.common.GoodSalesAbstractDashboardTest;

public class KpiAlertTest extends GoodSalesAbstractDashboardTest {

    private static final String METRIC_IN_PERCENT = "M" + UUID.randomUUID().toString().substring(0, 6);

    private static final String KPI_ALERT_DIALOG_HEADER = "Email me when this KPI is";
    private static final String KPI_ALERT_THRESHOLD = "100"; // TODO: consider parsing value from KPI to avoid immediate alert trigger

    @Override
    protected void prepareSetupProject() throws ParseException, JSONException, IOException {
        createAnalyticalDashboard(getRestApiClient(), testParams.getProjectId(), singletonList(createAmountKpi()));
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop"})
    public void checkNewKpiDoesNotHaveAlertSet() throws JSONException, IOException {
        // creating kpi using REST is not recommended for this use case
        startEditMode().addKpi(
                new KpiConfiguration.Builder()
                    .metric(METRIC_AVG_AMOUNT)
                    .dataSet(DATE_CREATED)
                    .build())
                .saveEditModeWithWidgets();

        try {
            takeScreenshot(browser, "checkNewKpiDoesNotHaveAlertSet", getClass());
            assertFalse(getLastKpiAfterAlertsLoaded().hasSetAlert());
        } finally {
            deleteWidgetsUsingCascase(getRestApiClient(), testParams.getProjectId(),
                    getKpiUri(METRIC_AVG_AMOUNT, getRestApiClient(), testParams.getProjectId()));
        }
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop"})
    public void checkKpiAlertButtonVisibility() {
        Kpi kpi = initIndigoDashboardsPageWithWidgets().getFirstWidget(Kpi.class);

        takeScreenshot(browser, "checkKpiAlertButtonVisibility-shouldNotBeVisible", getClass());
        kpi.waitForAlertButtonNotVisible()
            .hoverAndClickKpiAlertButton()
            .waitForAlertButtonVisible();
        takeScreenshot(browser, "checkKpiAlertButtonVisibility-shouldBeVisible", getClass());
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop"})
    public void checkKpiAlertDialog() {
        Kpi kpi = initIndigoDashboardsPageWithWidgets().getFirstWidget(Kpi.class);

        kpi.hoverAndClickKpiAlertButton();

        takeScreenshot(browser, "checkKpiAlertDialog", getClass());
        assertTrue(kpi.hasAlertDialogOpen());
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop"})
    public void checkKpiAlertDialogHeader() {
        KpiAlertDialog kpiAlertDialog =
                initIndigoDashboardsPageWithWidgets().getFirstWidget(Kpi.class).openAlertDialog();

        takeScreenshot(browser, "checkKpiAlertDialogHeader", getClass());
        assertEquals(kpiAlertDialog.getDialogHeader(), KPI_ALERT_DIALOG_HEADER);
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop"})
    public void checkAddKpiAlert() throws JSONException, IOException {
        String kpiUri = addWidgetToWorkingDashboard(createAmountKpi());

        try {
            initIndigoDashboardsPageWithWidgets();
            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, KPI_ALERT_THRESHOLD);

            takeScreenshot(browser, "checkAddKpiAlert", getClass());
            assertTrue(getLastKpiAfterAlertsLoaded().hasSetAlert());
        } finally {
            deleteWidgetsUsingCascase(getRestApiClient(), testParams.getProjectId(), kpiUri);
        }
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop"})
    public void checkKpiAlertUpdate() throws JSONException, IOException {
        String kpiUri = addWidgetToWorkingDashboard(createAmountKpi());

        try {
            String updatedThreshold = "200";
            KpiAlertDialog kpiAlertDialog;

            initIndigoDashboardsPageWithWidgets();
            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, KPI_ALERT_THRESHOLD);

            assertTrue(getLastKpiAfterAlertsLoaded().hasSetAlert());

            kpiAlertDialog = waitForFragmentVisible(indigoDashboardsPage)
                .getLastWidget(Kpi.class)
                .openAlertDialog();

            takeScreenshot(browser, "checkKpiAlertUpdate_before", getClass());
            assertEquals(kpiAlertDialog.getTriggeredWhen(), TRIGGERED_WHEN_GOES_ABOVE);
            assertEquals(kpiAlertDialog.getThreshold(), KPI_ALERT_THRESHOLD);

            setAlertForLastKpi(TRIGGERED_WHEN_DROPS_BELOW, updatedThreshold);

            assertTrue(getLastKpiAfterAlertsLoaded().hasSetAlert());

            kpiAlertDialog = waitForFragmentVisible(indigoDashboardsPage)
                .getLastWidget(Kpi.class)
                .openAlertDialog();

            takeScreenshot(browser, "checkKpiAlertUpdate_after", getClass());
            assertEquals(kpiAlertDialog.getTriggeredWhen(), TRIGGERED_WHEN_DROPS_BELOW);
            assertEquals(kpiAlertDialog.getThreshold(), updatedThreshold);
        } finally {
            deleteWidgetsUsingCascase(getRestApiClient(), testParams.getProjectId(), kpiUri);
        }
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop"})
    public void checkKpiAlertDialogWithPercentMetric() throws JSONException, IOException {
        Metric percentMetric = createMetric(METRIC_IN_PERCENT, "SELECT 1", "#,##0%");
        addWidgetToWorkingDashboard(
                createKpiUsingRest(createDefaultKpiConfiguration(percentMetric, DATE_CREATED)));

        try {
            boolean hasPercentSymbol = initIndigoDashboardsPageWithWidgets()
                .getLastWidget(Kpi.class)
                .openAlertDialog()
                .hasInputSuffix();

            takeScreenshot(browser, "checkKpiAlertDialogWithPercentMetric", getClass());
            assertTrue(hasPercentSymbol);
        } finally {
            deleteWidgetsUsingCascase(getRestApiClient(), testParams.getProjectId(), percentMetric.getUri());
        }
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop"})
    public void checkKpiAlertWithDateFilter() throws JSONException, IOException {
        String kpiUri = addWidgetToWorkingDashboard(createAmountKpi());

        try {
            KpiAlertDialog kpiAlertDialog = initIndigoDashboardsPageWithWidgets()
                .selectDateFilterByName(DATE_FILTER_LAST_MONTH)
                .getLastWidget(Kpi.class)
                .openAlertDialog();

            assertFalse(kpiAlertDialog.hasAlertMessage());

            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, KPI_ALERT_THRESHOLD);

            assertTrue(getLastKpiAfterAlertsLoaded().hasSetAlert());

            boolean isAlertMessageDisplayed = waitForFragmentVisible(indigoDashboardsPage)
                .selectDateFilterByName(DATE_FILTER_ALL_TIME)
                .getLastWidget(Kpi.class)
                .openAlertDialog()
                .hasAlertMessage();

            assertTrue(isAlertMessageDisplayed);

        } finally {
            deleteWidgetsUsingCascase(getRestApiClient(), testParams.getProjectId(), kpiUri);
        }
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop"})
    public void checkKpiAlertResetFilters() throws JSONException, IOException {
        String kpiUri = addWidgetToWorkingDashboard(createAmountKpi());

        try {
            initIndigoDashboardsPageWithWidgets().selectDateFilterByName(DATE_FILTER_THIS_MONTH);

            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, KPI_ALERT_THRESHOLD);

            waitForFragmentVisible(indigoDashboardsPage)
                .selectDateFilterByName(DATE_FILTER_THIS_QUARTER)
                .getLastWidget(Kpi.class)
                .openAlertDialog()
                .applyAlertFilters();

            String dateFilterSelection = indigoDashboardsPage
                .waitForDateFilter()
                .getSelection();
            assertEquals(dateFilterSelection, DATE_FILTER_THIS_MONTH);

        } finally {
            deleteWidgetsUsingCascase(getRestApiClient(), testParams.getProjectId(), kpiUri);
        }
    }

    @DataProvider(name = "dateFilterProvider")
    public Object[][] dateFilterProvider() {
        return new Object[][] {
            {DATE_FILTER_ALL_TIME, ""},
            {"Last 7 days", "in trailing 7 days"},
            {"Last 30 days", "in trailing 30 days"},
            {"Last 90 days", "in trailing 90 days"},
            {DATE_FILTER_THIS_MONTH, "in current month"},
            {DATE_FILTER_LAST_MONTH, "in previous month"},
            {"Last 12 months", "in previous 12 months"},
            {DATE_FILTER_THIS_QUARTER, "in current quarter"},
            {DATE_FILTER_LAST_QUARTER, "in previous quarter"},
            {"Last 4 quarters", "in previous 4 quarters"},
            {"This year", "in current year"},
            {"Last year", "in previous year"}
        };
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop"}, dataProvider = "dateFilterProvider")
    public void checkKpiAlertMessageWithDateFilter(String dateFilter, String alertDialogInfoText)
            throws JSONException, IOException {
        String kpiUri = addWidgetToWorkingDashboard(createAmountKpi());

        try {
            initIndigoDashboardsPageWithWidgets().selectDateFilterByName(dateFilter);

            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, KPI_ALERT_THRESHOLD);

            Kpi kpi = getLastKpiAfterAlertsLoaded();
            assertTrue(kpi.hasSetAlert());

            String kpiAlertDialogTextBefore = kpi
                .openAlertDialog()
                .getAlertDialogText();

            takeScreenshot(browser, "checkKpiAlertMessageWithDateFilter-" + dateFilter, getClass());
            assertEquals(kpiAlertDialogTextBefore, alertDialogInfoText);

            String anotherDateFilter =
                    dateFilter.equals(DATE_FILTER_LAST_MONTH) ? DATE_FILTER_THIS_MONTH : DATE_FILTER_LAST_MONTH;
            String kpiAlertDialogTextAfter = waitForFragmentVisible(indigoDashboardsPage)
                .selectDateFilterByName(anotherDateFilter)
                .getLastWidget(Kpi.class)
                .openAlertDialog()
                .getAlertDialogText();

            assertEquals(kpiAlertDialogTextBefore, kpiAlertDialogTextAfter);

        } finally {
            deleteWidgetsUsingCascase(getRestApiClient(), testParams.getProjectId(), kpiUri);
        }
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop"})
    public void checkKpiAlertDelete() throws JSONException, IOException {
        String kpiUri = addWidgetToWorkingDashboard(createAmountKpi());

        try {
            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, KPI_ALERT_THRESHOLD);

            takeScreenshot(browser, "checkKpiAlertDelete_before", getClass());
            assertTrue(getLastKpiAfterAlertsLoaded().hasSetAlert());

            deleteAlertForLastKpi();

            takeScreenshot(browser, "checkKpiAlertDelete_after", getClass());
            assertFalse(getLastKpiAfterAlertsLoaded().hasSetAlert());
        } finally {
            // working dashboard should not be empty after delete the kpi
            // because following method will delete it in that case
            deleteWidgetsUsingCascase(getRestApiClient(), testParams.getProjectId(), kpiUri);
        }
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop"})
    public void checkKpiAlertValidationNumber() throws JSONException, IOException {
        String kpiUri = addWidgetToWorkingDashboard(createAmountKpi());

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
            deleteWidgetsUsingCascase(getRestApiClient(), testParams.getProjectId(), kpiUri);
        }
    }

    private Kpi getLastKpiAfterAlertsLoaded() {
        return waitForFragmentVisible(indigoDashboardsPage)
            .waitForWidgetsLoading()
            .waitForAlertsLoaded()
            .getLastWidget(Kpi.class);
    }
}
