package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardWithWidgetsTest;
import org.testng.Assert;
import org.testng.annotations.Test;

public class KpiPopTest extends DashboardWithWidgetsTest {

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkNewlyAddedKpiHasMetrics() {
        Kpi justAddedKpi = initIndigoDashboardsPage()
            .switchToEditMode()
            .addWidget(AMOUNT, DATE_CREATED)
            .selectLastKpi();

        Assert.assertTrue(justAddedKpi.hasPopSection());

        indigoDashboardsPage
            .saveEditMode();

        Kpi lastKpi = initIndigoDashboardsPage()
            .getLastKpi();

        Assert.assertTrue(lastKpi.hasPopSection());
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkKpiWithoutComparison() {
        Kpi kpi = initIndigoDashboardsPage()
            .switchToEditMode()
            .addWidget(AMOUNT, DATE_CREATED, COMPARISON_NO_COMPARISON)
            .selectLastKpi();

        Assert.assertFalse(kpi.hasPopSection());

        indigoDashboardsPage
            .saveEditMode();

        Kpi lastKpi = initIndigoDashboardsPage()
            .getLastKpi();

        Assert.assertFalse(lastKpi.hasPopSection());
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkKpiPopSection() {
        initIndigoDashboardsPage()
            .switchToEditMode()
            .addWidget(AMOUNT, DATE_CREATED, COMPARISON_LAST_YEAR)
            .saveEditMode();

        Kpi kpi = initIndigoDashboardsPage()
            .getLastKpi();

        Assert.assertEquals(kpi.getPopSection().getChangeTitle(), "change");
        Assert.assertEquals(kpi.getPopSection().getPeriodTitle(), "YoY");

        indigoDashboardsPage.selectDateFilterByName(DATE_FILTER_ALL_TIME);

        Assert.assertEquals(kpi.getPopSection().getChangeTitle(), "change");
        Assert.assertEquals(kpi.getPopSection().getPeriodTitle(), "N/A");
    }
}
