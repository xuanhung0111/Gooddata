package com.gooddata.qa.graphene.indigo.dashboards.common;

import java.io.IOException;
import java.util.UUID;

import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.utils.http.indigo.IndigoRestUtils;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;

public abstract class DashboardWithWidgetsTest extends DashboardsTest {

    protected void setupKpi(KpiConfiguration config) {
        initIndigoDashboardsPageWithWidgets()
            .switchToEditMode()
            .addWidget(config)
            .saveEditModeWithWidgets();
    }

    protected void teardownKpi() {
        if (initIndigoDashboardsPageWithWidgets().isEditButtonVisible()) {
            waitForFragmentVisible(indigoDashboardsPage).switchToEditMode();
        }

        waitForFragmentVisible(indigoDashboardsPage)
            .clickLastKpiDeleteButton()
            .waitForDialog()
            .submitClick();

        indigoDashboardsPage
            .saveEditModeWithWidgets();
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"dashboardWidgetsInit"})
    public void initDashboardWithWidgets() throws JSONException, IOException {
        IndigoRestUtils.prepareAnalyticalDashboardTemplate(getRestApiClient(), getGoodDataClient(),
                testParams.getProjectId());
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"dashboardWidgetsInit"})
    public void captureScreenshotForCurrentDashboardWithLoadedWidgets() throws JSONException, IOException {
        initIndigoDashboardsPageWithWidgets()
            .waitForAllKpiWidgetContentLoaded();
        takeScreenshot(browser, "captureScreenshotForCurrentDashboardWithLoadedWidgets", getClass());
    }

    protected String generateUniqueHeadlineTitle() {
        // create unique headline title which fits into headline title (has limited size)
        return UUID.randomUUID().toString().substring(0, 18);
    }

    protected void setAlertForLastKpi(String triggeredWhen, String threshold) {
        waitForFragmentVisible(indigoDashboardsPage)
            .waitForDashboardLoad()
            .waitForAllKpiWidgetsLoaded()
            .getLastKpi()
            .openAlertDialog()
            .selectTriggeredWhen(triggeredWhen)
            .setThreshold(threshold)
            .setAlert();
    }

    protected void deleteAlertForLastKpi() {
        waitForFragmentVisible(indigoDashboardsPage)
            .getLastKpi()
            .openAlertDialog()
            .deleteAlert();
    }
}
