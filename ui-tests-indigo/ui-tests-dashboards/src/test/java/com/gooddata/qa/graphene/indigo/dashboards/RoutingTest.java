package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.indigo.dashboards.common.GoodSalesAbstractDashboardTest;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DASH_TAB_OUTLOOK;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.deleteAnalyticalDashboard;
import java.io.IOException;
import org.json.JSONException;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

public class RoutingTest extends GoodSalesAbstractDashboardTest {
    private static final KpiConfiguration kpi = new KpiConfiguration.Builder()
        .metric(METRIC_AMOUNT)
        .dataSet(DATE_CREATED)
        .comparison(Kpi.ComparisonType.NO_COMPARISON.toString())
        .drillTo(DASH_TAB_OUTLOOK)
        .build();

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop"})
    public void checkRedirectedToProjectRoute() throws JSONException {
        String url = PAGE_INDIGO_DASHBOARDS + "#/p/" + testParams.getProjectId();
        openUrl(url);

        waitForOpeningIndigoDashboard();
        IndigoDashboardsPage.getInstance(browser).getSplashScreen();

        assertTrue(browser.getCurrentUrl().contains(getIndigoDashboardsPageUri()),
                "Expecting /p/ to be redirected to /project/");
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop"})
    public void checkDashboardIdentifierAppended() throws JSONException, IOException {
        initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .addKpi(kpi)
            .saveEditModeWithWidgets();

        try {
            initIndigoDashboardsPageWithWidgets();
            assertTrue(browser.getCurrentUrl().contains("/dashboard/"));
        } finally {
            deleteAnalyticalDashboard(getRestApiClient(), getWorkingDashboardUri());
        }
    }
}