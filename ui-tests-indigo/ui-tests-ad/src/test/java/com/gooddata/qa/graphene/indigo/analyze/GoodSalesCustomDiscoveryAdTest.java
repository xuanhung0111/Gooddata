package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.function.Supplier;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.indigo.ShortcutPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport;

public class GoodSalesCustomDiscoveryAdTest extends AnalyticalDesignerAbstractTest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Indigo-GoodSales-Demo-Custom-Discovery-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void testCustomDiscovery() {
        initAnalysePage();

        ChartReport report = analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .getChartReport();
        assertThat(report.getTrackersCount(), equalTo(1));
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        analysisPage.changeReportType(ReportType.BAR_CHART);
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() == 0);

        analysisPage.addAttribute(ACTIVITY_TYPE);
        assertThat(report.getTrackersCount(), equalTo(4));

        analysisPage.resetToBlankState();
    }

    @Test(dependsOnGroups = {"init"})
    public void testWithAttribute() {
        initAnalysePage();

        assertEquals(analysisPage.addAttribute(ACTIVITY_TYPE)
                .getExplorerMessage(), "Now select a measure to display");

        assertEquals(analysisPage.changeReportType(ReportType.BAR_CHART)
                .getExplorerMessage(), "Now select a measure to display");

        assertEquals(analysisPage.changeReportType(ReportType.LINE_CHART)
                .getExplorerMessage(), "Now select a measure to display");

        TableReport report = analysisPage.changeReportType(ReportType.TABLE).getTableReport();
        assertThat(report.getHeaders().stream().map(String::toLowerCase).collect(toList()),
                equalTo(asList(ACTIVITY_TYPE.toLowerCase())));
        checkingOpenAsReport("testWithAttribute");
    }

    @Test(dependsOnGroups = {"init"})
    public void dragMetricToColumnChartShortcutPanel() {
        initAnalysePage();

        WebElement metric = analysisPage.getCataloguePanel()
                .searchAndGet(NUMBER_OF_ACTIVITIES, FieldType.METRIC);

        Supplier<WebElement> recommendation = () ->
            waitForElementPresent(ShortcutPanel.AS_A_COLUMN_CHART.getLocator(), browser);

            ChartReport report = analysisPage.drag(metric, recommendation)
                    .getChartReport();

        assertThat(report.getTrackersCount(), equalTo(1));
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        analysisPage.changeReportType(ReportType.BAR_CHART);
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() == 0);

        analysisPage.addAttribute(ACTIVITY_TYPE);
        assertThat(report.getTrackersCount(), equalTo(4));
        checkingOpenAsReport("dragMetricToColumnChartShortcutPanel");
    }

    @Test(dependsOnGroups = {"init"})
    public void dragMetricToTrendShortcutPanel() {
        initAnalysePage();

        WebElement metric = analysisPage.getCataloguePanel()
                .searchAndGet(NUMBER_OF_ACTIVITIES, FieldType.METRIC);

        Supplier<WebElement> trendRecommendation = () ->
            waitForElementPresent(ShortcutPanel.TRENDED_OVER_TIME.getLocator(), browser);

        ChartReport report = analysisPage.drag(metric, trendRecommendation)
                .getChartReport();

        assertTrue(report.getTrackersCount() >= 1);
        assertTrue(analysisPage.getFilterBuckets().isFilterVisible("Activity"));
        assertThat(analysisPage.getFilterBuckets().getFilterText("Activity"), equalTo("Activity: Last 4 quarters"));
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
        checkingOpenAsReport("dragMetricToTrendShortcutPanel");
    }
}
