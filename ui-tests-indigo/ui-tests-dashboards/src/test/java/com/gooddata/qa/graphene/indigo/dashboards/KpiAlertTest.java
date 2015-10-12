package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog;
import static com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog.TRIGGERED_WHEN_GOES_ABOVE;
import static com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog.TRIGGERED_WHEN_DROPS_BELOW;
import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardWithWidgetsTest;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

public class KpiAlertTest extends DashboardWithWidgetsTest {

    private static final KpiConfiguration kpiConfig = new KpiConfiguration.Builder()
        .metric(AMOUNT)
        .dateDimension(DATE_CREATED)
        .build();

    private static final String KPI_ALERT_DIALOG_HEADER = "Email me when this KPI…";
    private static final String KPI_ALERT_FILTERS_DIFFER_MESSAGE = "Alert and current dashboard filters differ. Apply alert filters";
    private static final String KPI_ALERT_THRESHOLD = "100"; // TODO: consider parsing value from KPI to avoid immediate alert trigger

    private Kpi getLastKpiAfterAlertsLoaded() {
        return initIndigoDashboardsPageWithWidgets()
            .waitForAllKpiWidgetContentLoaded()
            .waitForAlertsLoaded()
            .getLastKpi();
    }

    private void setAlertForLastKpi(String triggeredWhen, String threshold) {
        initIndigoDashboardsPageWithWidgets()
            .getLastKpi()
            .openAlertDialog()
            .selectTriggeredWhen(triggeredWhen)
            .setThreshold(threshold)
            .setAlert();
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkNewKpiDoesNotHaveAlertSet() {
        setupKpi(kpiConfig);

        try {
            takeScreenshot(browser, "checkNewKpiDoesNotHaveAlertSet", getClass());
            Assert.assertFalse(getLastKpiAfterAlertsLoaded().hasSetAlert());
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
        Assert.assertEquals(kpiAlertDialog.getDialogHeader(), KPI_ALERT_DIALOG_HEADER);
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

            kpiAlertDialog = initIndigoDashboardsPageWithWidgets()
                .getLastKpi()
                .openAlertDialog();

            takeScreenshot(browser, "checkKpiAlertUpdate_before", getClass());
            assertEquals(kpiAlertDialog.getTriggeredWhen(), TRIGGERED_WHEN_GOES_ABOVE);
            assertEquals(kpiAlertDialog.getThreshold(), KPI_ALERT_THRESHOLD);

            setAlertForLastKpi(TRIGGERED_WHEN_DROPS_BELOW, updatedThreshold);

            assertTrue(getLastKpiAfterAlertsLoaded().hasSetAlert());

            kpiAlertDialog = initIndigoDashboardsPageWithWidgets()
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
    public void checkKpiAlertWithDateFilter() {
        setupKpi(kpiConfig);

        try {
            KpiAlertDialog kpiAlertDialog = initIndigoDashboardsPageWithWidgets()
                .selectDateFilterByName(DATE_FILTER_LAST_MONTH)
                .getLastKpi()
                .openAlertDialog();

            assertFalse(kpiAlertDialog.hasAlertMessage());

            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, KPI_ALERT_THRESHOLD);

            assertTrue(getLastKpiAfterAlertsLoaded().hasSetAlert());

            String alertMessageText = initIndigoDashboardsPageWithWidgets()
                .selectDateFilterByName(DATE_FILTER_ALL_TIME)
                .getLastKpi()
                .openAlertDialog()
                .getAlertMessageText();

            assertEquals(alertMessageText, KPI_ALERT_FILTERS_DIFFER_MESSAGE);

        } finally {
            teardownKpi();
        }
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkKpiAlertResetFilters() {
        setupKpi(kpiConfig);

        try {
            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, KPI_ALERT_THRESHOLD);

            initIndigoDashboardsPageWithWidgets()
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

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkKpiAlertMessageWithDateFilter() {
        setupKpi(kpiConfig);

        try {
            String alertDialogInfoText = "in current month";

            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, KPI_ALERT_THRESHOLD);

            Kpi kpi = getLastKpiAfterAlertsLoaded();
            assertTrue(kpi.hasSetAlert());

            String kpiAlertDialogTextBefore = kpi
                .openAlertDialog()
                .getAlertDialogText();

            assertEquals(kpiAlertDialogTextBefore, alertDialogInfoText);

            String kpiAlertDialogTextAfter = initIndigoDashboardsPageWithWidgets()
                .selectDateFilterByName(DATE_FILTER_LAST_MONTH)
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

            initIndigoDashboardsPageWithWidgets()
                .getLastKpi()
                .openAlertDialog()
                .deleteAlert();

            takeScreenshot(browser, "checkKpiAlertDelete_after", getClass());
            assertFalse(getLastKpiAfterAlertsLoaded().hasSetAlert());
        } finally {
            teardownKpi();
        }
    }
}
