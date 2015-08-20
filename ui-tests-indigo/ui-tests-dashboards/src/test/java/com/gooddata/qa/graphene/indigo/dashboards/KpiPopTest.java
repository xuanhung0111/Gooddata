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

        Assert.assertTrue(justAddedKpi.hasMetric());

        String metricText = justAddedKpi.getMetric();
        Assert.assertTrue(metricText.contains("change"));
        Assert.assertTrue(metricText.contains("last month"));

        indigoDashboardsPage
            .saveEditMode();

        Kpi lastKpi = initIndigoDashboardsPage()
            .getLastKpi();

        Assert.assertTrue(lastKpi.hasMetric());

        metricText = lastKpi.getMetric();
        Assert.assertTrue(metricText.contains("change"));
        Assert.assertTrue(metricText.contains("last month"));
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkKpiWithoutComparison() {
        Kpi kpi = initIndigoDashboardsPage()
            .switchToEditMode()
            .addWidget(AMOUNT, DATE_CREATED, COMPARISON_NO_COMPARISON)
            .selectLastKpi();

        Assert.assertFalse(kpi.hasMetric());

        indigoDashboardsPage
            .saveEditMode();

        Kpi lastKpi = initIndigoDashboardsPage()
            .getLastKpi();

        Assert.assertFalse(lastKpi.hasMetric());
    }
}
