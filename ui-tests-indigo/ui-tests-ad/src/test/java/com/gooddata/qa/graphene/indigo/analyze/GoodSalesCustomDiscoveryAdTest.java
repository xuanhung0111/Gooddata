package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.jboss.arquillian.graphene.Graphene;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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

        analysisPage.addCategory(ACTIVITY_TYPE);
        assertThat(report.getTrackersCount(), equalTo(4));

        analysisPage.resetToBlankState();
    }

    @Test(dependsOnGroups = {"init"})
    public void testWithAttribute() {
        initAnalysePage();

        assertEquals(analysisPage.addCategory(ACTIVITY_TYPE)
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

        ChartReport report = analysisPage.dragAndDropMetricToShortcutPanel(NUMBER_OF_ACTIVITIES,
                ShortcutPanel.AS_A_COLUMN_CHART).getChartReport();
        assertThat(report.getTrackersCount(), equalTo(1));
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        analysisPage.changeReportType(ReportType.BAR_CHART);
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() == 0);

        analysisPage.addCategory(ACTIVITY_TYPE);
        assertThat(report.getTrackersCount(), equalTo(4));
        checkingOpenAsReport("dragMetricToColumnChartShortcutPanel");
    }

    @Test(dependsOnGroups = {"init"})
    public void dragMetricToTrendShortcutPanel() {
        initAnalysePage();

        ChartReport report = analysisPage.dragAndDropMetricToShortcutPanel(NUMBER_OF_ACTIVITIES,
                ShortcutPanel.TRENDED_OVER_TIME).getChartReport();
        assertTrue(report.getTrackersCount() >= 1);
        assertTrue(analysisPage.isFilterVisible("Activity"));
        assertThat(analysisPage.getFilterText("Activity"), equalTo("Activity: Last 4 quarters"));
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
        checkingOpenAsReport("dragMetricToTrendShortcutPanel");
    }

    @Test(dependsOnGroups = {"init"}, enabled = false, description = "https://jira.intgdc.com/browse/CL-7670")
    public void testAccessibilityGuidanceForAttributesMetrics() {
        initAnalysePage();

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .addInapplicableCategory(STAGE_NAME);
        assertThat(analysisPage.getExplorerMessage(), equalTo("Visualization cannot be displayed"));
        takeScreenshot(browser,
                "testAccessibilityGuidanceForAttributesMetrics - inapplicableCategory", getClass());

        assertTrue(analysisPage.searchBucketItem(STAGE_NAME));
        takeScreenshot(browser, 
                "testAccessibilityGuidanceForAttributesMetrics - searchInapplicableCategory", getClass());
        assertTrue(analysisPage.getAllCatalogFieldNamesInViewPort().contains(STAGE_NAME));
        assertFalse(analysisPage.searchBucketItem(STAGE_NAME + "not found"));
        takeScreenshot(browser,
                "testAccessibilityGuidanceForAttributesMetrics - searchNotFound", getClass());
    }
}
