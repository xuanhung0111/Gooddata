package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.tagName;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractGoodSalesE2ETest;

public class ChartLegendTest extends AbstractGoodSalesE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Chart-Legend-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void test_chart_legend_should_not_be_clickable() {
        String visibleSeriesSelector = ".highcharts-series[visibility=visible]";
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(lostOppsMetric, METRICS_BUCKET);

        expectElementCount(visibleSeriesSelector, 2);

        // click legend for Salary and check no impact on visible series
        browser.findElements(className("highcharts-legend-item"))
            .stream()
            .filter(e -> "# of Activities".equals(e.findElement(tagName("tspan")).getText()))
            .findFirst()
            .get()
            .click();
        expectElementCount(visibleSeriesSelector, 2);
    }
}
