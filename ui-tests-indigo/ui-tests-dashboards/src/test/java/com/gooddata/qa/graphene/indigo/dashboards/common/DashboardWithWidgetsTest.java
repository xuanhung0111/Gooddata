package com.gooddata.qa.graphene.indigo.dashboards.common;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.utils.http.indigo.IndigoRestUtils;

import java.io.IOException;
import java.util.UUID;

import org.json.JSONException;
import org.testng.annotations.Test;

public abstract class DashboardWithWidgetsTest extends DashboardsTest {

    protected void setupKpi(KpiConfiguration config) {
        initIndigoDashboardsPageWithWidgets()
            .switchToEditMode()
            .addWidget(config)
            .saveEditModeWithKpis();
    }

    protected void teardownKpi() {
        if (initIndigoDashboardsPageWithWidgets().isEditButtonVisible()) {
            initIndigoDashboardsPageWithWidgets()
                .switchToEditMode();
        }

        initIndigoDashboardsPageWithWidgets()
            .clickLastKpiDeleteButton()
            .waitForDialog()
            .submitClick();

        indigoDashboardsPage
            .saveEditModeWithKpis();
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"dashboardWidgetsInit"})
    public void initDashboardWithWidgets() throws JSONException, IOException {
        IndigoRestUtils.prepareAnalyticalDashboardTemplate(getRestApiClient(), testParams.getProjectId());
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
}
