package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog;
import static com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog.TRIGGERED_WHEN_GOES_ABOVE;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardWithWidgetsTest;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import org.testng.Assert;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

public class KpiAlertTest extends DashboardWithWidgetsTest {

    private static final String KPI_ALERT_DIALOG_HEADER = "Email me when this KPI…";

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkNewKpiDoesNotHaveAlertSet() {
        initIndigoDashboardsPage()
            .switchToEditMode()
            .addWidget(AMOUNT, DATE_CREATED)
            .saveEditMode();

        Kpi kpi = initIndigoDashboardsPage()
            .waitForAlertsLoaded()
            .getLastKpi();

        takeScreenshot(browser, "checkNewKpiDoesNotHaveAlertSet", getClass());
        Assert.assertFalse(kpi.hasSetAlert());

        indigoDashboardsPage
            .switchToEditMode()
            .clickLastKpiDeleteButton()
            .waitForDialog()
            .submitClick();

        indigoDashboardsPage
            .saveEditMode();
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
        String threshold = "100"; // TODO: consider parsing value from KPI to avoid immediate alert trigger

        initIndigoDashboardsPage()
            .switchToEditMode()
            .addWidget(AMOUNT, DATE_CREATED)
            .saveEditMode();

        initIndigoDashboardsPage()
            .getLastKpi()
            .openAlertDialog()
            .selectTriggeredWhen(TRIGGERED_WHEN_GOES_ABOVE)
            .setThreshold(threshold)
            .setAlert();

        Kpi kpi = initIndigoDashboardsPage()
            .waitForAlertsLoaded()
            .getLastKpi();

        takeScreenshot(browser, "checkAddKpiAlert", getClass());
        assertTrue(kpi.hasSetAlert());

        indigoDashboardsPage
            .switchToEditMode()
            .clickLastKpiDeleteButton()
            .waitForDialog()
            .submitClick();

        indigoDashboardsPage
            .saveEditMode();
    }
}
