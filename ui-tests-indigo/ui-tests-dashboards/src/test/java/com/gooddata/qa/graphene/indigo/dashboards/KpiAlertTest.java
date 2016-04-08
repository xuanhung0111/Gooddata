package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog.TRIGGERED_WHEN_DROPS_BELOW;
import static com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog.TRIGGERED_WHEN_GOES_ABOVE;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.UUID;

import org.json.JSONException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardWithWidgetsTest;
import com.gooddata.qa.utils.http.RestUtils;

public class KpiAlertTest extends DashboardWithWidgetsTest {

    private static final KpiConfiguration kpiConfig = new KpiConfiguration.Builder()
        .metric(AMOUNT)
        .dataSet(DATE_CREATED)
        .build();

    private static final String METRIC_IN_PERCENT = "M" + UUID.randomUUID().toString().substring(0, 6);

    private static final KpiConfiguration kpiConfigWithMetricInPercent = new KpiConfiguration.Builder()
        .metric(METRIC_IN_PERCENT)
        .dataSet(DATE_CREATED)
        .build();

    private static final String KPI_ALERT_DIALOG_HEADER = "Email me when this KPI is";
    private static final String KPI_ALERT_THRESHOLD = "100"; // TODO: consider parsing value from KPI to avoid immediate alert trigger

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkNewKpiDoesNotHaveAlertSet() {
        setupKpi(kpiConfig);

        try {
            takeScreenshot(browser, "checkNewKpiDoesNotHaveAlertSet", getClass());
            assertFalse(getLastKpiAfterAlertsLoaded().hasSetAlert());
        } finally {
            teardownKpi();
        }
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkKpiAlertButtonVisibility() {
        Kpi kpi = initIndigoDashboardsPageWithWidgets()
            .getFirstKpi();

        takeScreenshot(browser, "checkKpiAlertButtonVisibility-shouldNotBeVisible", getClass());
        kpi.waitForAlertButtonNotVisible()
            .hoverAndClickKpiAlertButton()
            .waitForAlertButtonVisible();
        takeScreenshot(browser, "checkKpiAlertButtonVisibility-shouldBeVisible", getClass());
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkKpiAlertDialog() {
        Kpi kpi = initIndigoDashboardsPageWithWidgets()
            .getFirstKpi();

        kpi.hoverAndClickKpiAlertButton();

        takeScreenshot(browser, "checkKpiAlertDialog", getClass());
        assertTrue(kpi.hasAlertDialogOpen());
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkKpiAlertDialogHeader() {
        KpiAlertDialog kpiAlertDialog = initIndigoDashboardsPageWithWidgets()
            .getFirstKpi()
            .openAlertDialog();

        takeScreenshot(browser, "checkKpiAlertDialogHeader", getClass());
        assertEquals(kpiAlertDialog.getDialogHeader(), KPI_ALERT_DIALOG_HEADER);
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkAddKpiAlert() {
        setupKpi(kpiConfig);

        try {
            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, KPI_ALERT_THRESHOLD);

            takeScreenshot(browser, "checkAddKpiAlert", getClass());
            assertTrue(getLastKpiAfterAlertsLoaded().hasSetAlert());
        } finally {
            teardownKpi();
        }
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkKpiAlertUpdate() {
        setupKpi(kpiConfig);

        try {
            String updatedThreshold = "200";
            KpiAlertDialog kpiAlertDialog;

            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, KPI_ALERT_THRESHOLD);

            assertTrue(getLastKpiAfterAlertsLoaded().hasSetAlert());

            kpiAlertDialog = waitForFragmentVisible(indigoDashboardsPage)
                .getLastKpi()
                .openAlertDialog();

            takeScreenshot(browser, "checkKpiAlertUpdate_before", getClass());
            assertEquals(kpiAlertDialog.getTriggeredWhen(), TRIGGERED_WHEN_GOES_ABOVE);
            assertEquals(kpiAlertDialog.getThreshold(), KPI_ALERT_THRESHOLD);

            setAlertForLastKpi(TRIGGERED_WHEN_DROPS_BELOW, updatedThreshold);

            assertTrue(getLastKpiAfterAlertsLoaded().hasSetAlert());

            kpiAlertDialog = waitForFragmentVisible(indigoDashboardsPage)
                .getLastKpi()
                .openAlertDialog();

            takeScreenshot(browser, "checkKpiAlertUpdate_after", getClass());
            assertEquals(kpiAlertDialog.getTriggeredWhen(), TRIGGERED_WHEN_DROPS_BELOW);
            assertEquals(kpiAlertDialog.getThreshold(), updatedThreshold);
        } finally {
            teardownKpi();
        }
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkKpiAlertDialogWithPercentMetric() throws JSONException {
        String metricUri = createMetric(METRIC_IN_PERCENT, "SELECT 1", "#,##0%").getUri();
        setupKpi(kpiConfigWithMetricInPercent);

        try {
            boolean hasPercentSymbol = waitForFragmentVisible(indigoDashboardsPage)
                .getLastKpi()
                .openAlertDialog()
                .hasInputSuffix();

            takeScreenshot(browser, "checkKpiAlertDialogWithPercentMetric", getClass());
            assertTrue(hasPercentSymbol);
        } finally {
            teardownKpi();
            if (metricUri != null) {
                RestUtils.deleteObject(restApiClient, metricUri);
            }
        }
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkKpiAlertWithDateFilter() {
        setupKpi(kpiConfig);

        try {
            KpiAlertDialog kpiAlertDialog = waitForFragmentVisible(indigoDashboardsPage)
                .selectDateFilterByName(DATE_FILTER_LAST_MONTH)
                .getLastKpi()
                .openAlertDialog();

            assertFalse(kpiAlertDialog.hasAlertMessage());

            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, KPI_ALERT_THRESHOLD);

            assertTrue(getLastKpiAfterAlertsLoaded().hasSetAlert());

            boolean isAlertMessageDisplayed = waitForFragmentVisible(indigoDashboardsPage)
                .selectDateFilterByName(DATE_FILTER_ALL_TIME)
                .getLastKpi()
                .openAlertDialog()
                .hasAlertMessage();

            assertTrue(isAlertMessageDisplayed);

        } finally {
            teardownKpi();
        }
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkKpiAlertResetFilters() {
        setupKpi(kpiConfig);

        try {
            waitForFragmentVisible(indigoDashboardsPage)
                .selectDateFilterByName(DATE_FILTER_THIS_MONTH);

            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, KPI_ALERT_THRESHOLD);

            waitForFragmentVisible(indigoDashboardsPage)
                .selectDateFilterByName(DATE_FILTER_THIS_QUARTER)
                .getLastKpi()
                .openAlertDialog()
                .applyAlertFilters();

            String dateFilterSelection = indigoDashboardsPage
                .waitForDateFilter()
                .getSelection();
            assertEquals(dateFilterSelection, DATE_FILTER_THIS_MONTH);

        } finally {
            teardownKpi();
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

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"},
            dataProvider = "dateFilterProvider")
    public void checkKpiAlertMessageWithDateFilter(String dateFilter, String alertDialogInfoText) {
        setupKpi(kpiConfig);

        try {
            waitForFragmentVisible(indigoDashboardsPage)
                .selectDateFilterByName(dateFilter);

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
                .getLastKpi()
                .openAlertDialog()
                .getAlertDialogText();

            assertEquals(kpiAlertDialogTextBefore, kpiAlertDialogTextAfter);

        } finally {
            teardownKpi();
        }
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkKpiAlertDelete() {
        setupKpi(kpiConfig);

        try {
            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, KPI_ALERT_THRESHOLD);

            takeScreenshot(browser, "checkKpiAlertDelete_before", getClass());
            assertTrue(getLastKpiAfterAlertsLoaded().hasSetAlert());

            deleteAlertForLastKpi();

            takeScreenshot(browser, "checkKpiAlertDelete_after", getClass());
            assertFalse(getLastKpiAfterAlertsLoaded().hasSetAlert());
        } finally {
            teardownKpi();
        }
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkKpiAlertValidationNumber() {
        setupKpi(kpiConfig);

        try {
            KpiAlertDialog dialog = waitForFragmentVisible(indigoDashboardsPage)
                .getLastKpi()
                .openAlertDialog();

            String message = dialog.selectTriggeredWhen(TRIGGERED_WHEN_GOES_ABOVE)
                .setThreshold("1-")
                .getAlertMessageText();
            assertEquals(message, "This number is not valid.\nDouble-check it and try again.");

            dialog.discardAlert();
        } finally {
            teardownKpi();
        }
    }

    private Kpi getLastKpiAfterAlertsLoaded() {
        return waitForFragmentVisible(indigoDashboardsPage)
            .waitForAllKpiWidgetContentLoaded()
            .waitForAlertsLoaded()
            .getLastKpi();
    }
}
