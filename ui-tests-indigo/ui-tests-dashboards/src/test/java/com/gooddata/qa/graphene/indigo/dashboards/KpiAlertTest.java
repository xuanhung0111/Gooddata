package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog;
import static com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog.TRIGGERED_WHEN_GOES_ABOVE;
import static com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog.TRIGGERED_WHEN_DROPS_BELOW;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardWithWidgetsTest;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

public class KpiAlertTest extends DashboardWithWidgetsTest {

    private static final String KPI_ALERT_DIALOG_HEADER = "Email me when this KPI…";

    private Kpi getLastKpiAfterAlertsLoaded() {
        return initIndigoDashboardsPage()
            .waitForAlertsLoaded()
            .getLastKpi();
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkNewKpiDoesNotHaveAlertSet() {
        setupKpi(AMOUNT, DATE_CREATED);

        try {
            Kpi kpi = getLastKpiAfterAlertsLoaded();

            takeScreenshot(browser, "checkNewKpiDoesNotHaveAlertSet", getClass());
            Assert.assertFalse(kpi.hasSetAlert());
        } finally {
            teardownKpi();
        }
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkKpiAlertButtonVisibility() {
        Kpi kpi = initIndigoDashboardsPage()
            .getFirstKpi();

        takeScreenshot(browser, "checkKpiAlertButtonVisibility-shouldNotBeVisible", getClass());
        kpi.waitForAlertButtonNotVisible()
            .hoverAndClickKpiAlertButton()
            .waitForAlertButtonVisible();
        takeScreenshot(browser, "checkKpiAlertButtonVisibility-shouldBeVisible", getClass());
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkKpiAlertDialog() {
        Kpi kpi = initIndigoDashboardsPage()
            .getFirstKpi();

        kpi.hoverAndClickKpiAlertButton();

        takeScreenshot(browser, "checkKpiAlertDialog", getClass());
        Assert.assertTrue(kpi.hasAlertDialogOpen());
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkKpiAlertDialogHeader() {
        KpiAlertDialog kpiAlertDialog = initIndigoDashboardsPage()
            .getFirstKpi()
            .openAlertDialog();

        takeScreenshot(browser, "checkKpiAlertDialogHeader", getClass());
        Assert.assertEquals(kpiAlertDialog.getDialogHeader(), KPI_ALERT_DIALOG_HEADER);
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkAddKpiAlert() {
        setupKpi(AMOUNT, DATE_CREATED);

        try {
            String threshold = "100"; // TODO: consider parsing value from KPI to avoid immediate alert trigger

            initIndigoDashboardsPage()
                .getLastKpi()
                .openAlertDialog()
                .selectTriggeredWhen(TRIGGERED_WHEN_GOES_ABOVE)
                .setThreshold(threshold)
                .setAlert();

            Kpi kpi = getLastKpiAfterAlertsLoaded();

            takeScreenshot(browser, "checkAddKpiAlert", getClass());
            assertTrue(kpi.hasSetAlert());
        } finally {
            teardownKpi();
        }
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkAddKpiAlertUpdate() {
        setupKpi(AMOUNT, DATE_CREATED);

        try {
            String threshold = "100";
            String updatedThreshold = "200";
            Kpi kpi;
            KpiAlertDialog kpiAlertDialog;


            initIndigoDashboardsPage()
                .getLastKpi()
                .openAlertDialog()
                .selectTriggeredWhen(TRIGGERED_WHEN_GOES_ABOVE)
                .setThreshold(threshold)
                .setAlert();

            kpi = getLastKpiAfterAlertsLoaded();

            assertTrue(kpi.hasSetAlert());

            kpiAlertDialog = initIndigoDashboardsPage()
                .getLastKpi()
                .openAlertDialog();

            takeScreenshot(browser, "checkKpiAlertUpdate_before", getClass());
            assertEquals(kpiAlertDialog.getTriggeredWhen(), TRIGGERED_WHEN_GOES_ABOVE);
            assertEquals(kpiAlertDialog.getThreshold(), threshold);

            initIndigoDashboardsPage()
                .getLastKpi()
                .openAlertDialog()
                .selectTriggeredWhen(TRIGGERED_WHEN_DROPS_BELOW)
                .setThreshold(updatedThreshold)
                .setAlert();

            kpi = getLastKpiAfterAlertsLoaded();

            assertTrue(kpi.hasSetAlert());

            kpiAlertDialog = initIndigoDashboardsPage()
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
    public void checkKpiAlertDelete() {
        setupKpi(AMOUNT, DATE_CREATED);

        try {
            String threshold = "100";
            Kpi kpi;

            initIndigoDashboardsPage()
                .getLastKpi()
                .openAlertDialog()
                .selectTriggeredWhen(TRIGGERED_WHEN_GOES_ABOVE)
                .setThreshold(threshold)
                .setAlert();

            kpi = getLastKpiAfterAlertsLoaded();

            takeScreenshot(browser, "checkKpiAlertDelete_before", getClass());
            assertTrue(kpi.hasSetAlert());

            initIndigoDashboardsPage()
                .getLastKpi()
                .openAlertDialog()
                .deleteAlert();

            kpi = getLastKpiAfterAlertsLoaded();

            takeScreenshot(browser, "checkKpiAlertDelete_after", getClass());
            assertFalse(kpi.hasSetAlert());
        } finally {
            teardownKpi();
        }
    }
}
