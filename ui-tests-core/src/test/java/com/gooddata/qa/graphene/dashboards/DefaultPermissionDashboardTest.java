package com.gooddata.qa.graphene.dashboards;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.fragments.manage.MetricDetailsPage;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class DefaultPermissionDashboardTest extends GoodSalesAbstractTest {

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createAmountMetric();
    }

    @Test(dependsOnGroups = "createProject")
    public void hiddenDashboardByDefault() {
        initDashboardsPage().openEditExportEmbedMenu().select("Add Dashboard");
        assertEquals(dashboardsPage.getBubbleBlueTooltip(), "This dashboard is hidden. Only you can see it.");
        assertTrue(dashboardsPage.isUnlisted(), "Eye icon should display");
        dashboardsPage.saveDashboard();
        assertEquals(dashboardsPage.getBubbleBlueTooltip(),
                "This dashboard is hidden. Only you can see it. Share it with others");
    }

    @Test(dependsOnGroups = "createProject")
    public void displayBlueBubbleOnDashboardPage() {
        int times = 1;
        initDashboardsPage();
        while(times < 6) {
            dashboardsPage.openEditExportEmbedMenu().select("Add Dashboard");
            assertTrue(dashboardsPage.isBlueBubbleTooltipDisplayed(),
                    format("Blue bubble doesn't display at %d times ",  times++));
            dashboardsPage.saveDashboard();
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void displayBlueBubbleOnNewReport() {
        int times = 1;
        while(times < 6) {
            assertTrue(initReportCreation().isBlueBubbleTooltipDisplayed(),
                    format("Blue bubble doesn't display at %d times ",  times++));
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void limitTimesDisplayBlueBubbleOnNewMetric() {
        final int limitedTimes = 5;
        String finalMetric = "Metric " + limitedTimes;
        int times = 1;
        while(times < limitedTimes) {
            String metricName = "Metric " + times;
            MetricDetailsPage metricDetailsPage = initMetricPage()
                    .createShareMetric(metricName, METRIC_AMOUNT, ATTR_YEAR_SNAPSHOT)
                    .openMetricDetailPage(metricName);
            assertTrue(metricDetailsPage.isBlueBubbleTooltipDisplayed(),
                    format("Blue bubble doesn't display at %d times ",  times++));
            metricDetailsPage.closeBubbleBlueTooltip();
        }
        assertFalse(initMetricPage().createShareMetric(finalMetric, METRIC_AMOUNT, ATTR_YEAR_SNAPSHOT)
                .openMetricDetailPage(finalMetric)
                .isBlueBubbleTooltipDisplayed(),
                format("Blue bubble shouldn't display after closing at %d times", limitedTimes - 1));
    }
}
