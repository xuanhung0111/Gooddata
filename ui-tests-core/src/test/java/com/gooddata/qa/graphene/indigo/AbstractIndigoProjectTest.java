package com.gooddata.qa.graphene.indigo;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.indigo.ReportDefinition;
import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.indigo.ShortcutPanel;
import com.gooddata.qa.graphene.fragments.indigo.pages.internals.AttributeFilterPickerPanel;
import com.gooddata.qa.graphene.fragments.indigo.recommendation.ComparisonRecommendation;
import com.gooddata.qa.graphene.fragments.indigo.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.fragments.indigo.recommendation.TrendingRecommendation;
import com.gooddata.qa.graphene.fragments.indigo.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.reports.TableReport;

public abstract class AbstractIndigoProjectTest extends AbstractProjectTest {

    protected static final String DATE = "Date";

    protected static final String CUSTOM_DISCOVERY_GROUP = "custom_discovery";
    protected static final String CONTRIBUTION_GROUP = "contribution";
    protected static final String COMPARISON_GROUP = "comparison";
    protected static final String TRENDING_GROUP = "trending";
    protected static final String EXPLORE_PROJECT_DATA_GROUP = "explore_project_data";
    protected static final String EXPORT_GROUP = "export";
    protected static final String FILTER_GROUP = "filter";
    protected static final String PERIOD_OVER_PERIOD_GROUP = "PoP";
    protected static final String RESET_GROUP = "reset";
    protected static final String CHART_REPORT_GROUP = "chart_report";
    protected static final String TABLE_REPORT_GROUP = "table_report";

    protected String metric1;
    protected String metric2;
    protected String metric3;
    protected String attribute1;
    protected String attribute2;
    protected String attribute3;

    @BeforeClass
    public void initStartPage() {
        startPage = "projects.html";
        projectCreateCheckIterations = 60; // 5 minutes
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {CUSTOM_DISCOVERY_GROUP})
    public void testCustomDiscovery() {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withMetrics(metric1));
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        analysisPage.changeReportType(ReportType.BAR_CHART);
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() == 0);

        analysisPage.addCategory(attribute1);
        assertEquals(report.getTrackersCount(), 3);

        analysisPage.resetToBlankState();
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {CUSTOM_DISCOVERY_GROUP})
    public void testWithAttribute() {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withCategories(attribute1));
        assertEquals(analysisPage.getExplorerMessage(), "Now select a metric to display");

        assertEquals(analysisPage.changeReportType(ReportType.BAR_CHART)
                                 .getExplorerMessage(), "Now select a metric to display");

        assertEquals(analysisPage.changeReportType(ReportType.LINE_CHART)
                .getExplorerMessage(), "Now select a metric to display");

        TableReport report = analysisPage.changeReportType(ReportType.TABLE).getTableReport();
        assertEquals(report.getHeaders(), Arrays.asList(attribute1.toUpperCase()));
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {CUSTOM_DISCOVERY_GROUP})
    public void dragMetricToColumnChartShortcutPanel() {
        initAnalysePage();

        analysisPage.dragAndDropMetricToShortcutPanel(metric1, ShortcutPanel.AS_A_COLUMN_CHART);
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        analysisPage.changeReportType(ReportType.BAR_CHART);
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() == 0);

        analysisPage.addCategory(attribute1);
        assertEquals(report.getTrackersCount(), 3);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {CUSTOM_DISCOVERY_GROUP})
    public void dragMetricToTrendShortcutPanel() {
        initAnalysePage();

        analysisPage.dragAndDropMetricToShortcutPanel(metric1, ShortcutPanel.TRENDED_OVER_TIME);
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 4);
        assertTrue(analysisPage.isFilterVisible(DATE));
        assertEquals(analysisPage.getFilterText(DATE), DATE + ": Last 4 quarters");
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {CONTRIBUTION_GROUP})
    public void testSimpleContribution() {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withMetrics(metric1)
                .withCategories(attribute2));
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 6);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer
                .isRecommendationVisible(RecommendationStep.SEE_PERCENTS));
        recommendationContainer.getRecommendation(RecommendationStep.SEE_PERCENTS).apply();
        assertTrue(analysisPage.isReportTypeSelected(ReportType.BAR_CHART));
        assertEquals(report.getTrackersCount(), 6);
        assertTrue(analysisPage.isShowPercentConfigEnabled());
        assertTrue(analysisPage.isShowPercentConfigSelected());

        analysisPage.addCategory(attribute1);
        assertTrue(analysisPage.isReportTypeSelected(ReportType.BAR_CHART));
        assertEquals(report.getTrackersCount(), 3);
        assertTrue(analysisPage.isShowPercentConfigEnabled());
        assertTrue(analysisPage.isShowPercentConfigSelected());
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {CONTRIBUTION_GROUP})
    public void testAnotherApproachToShowContribution() {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withMetrics(metric1));
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
        ComparisonRecommendation comparisonRecommendation =
                recommendationContainer.getRecommendation(RecommendationStep.COMPARE);
        comparisonRecommendation.select(attribute1).apply();
        assertTrue(recommendationContainer
                .isRecommendationVisible(RecommendationStep.SEE_PERCENTS));
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {COMPARISON_GROUP})
    public void testSimpleComparison() {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withMetrics(metric1));
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        ComparisonRecommendation comparisonRecommendation =
                recommendationContainer.getRecommendation(RecommendationStep.COMPARE);
        comparisonRecommendation.select(attribute1).apply();
        assertTrue(analysisPage.getAllCategoryNames().contains(attribute1));
        assertEquals(analysisPage.getFilterText(attribute1), attribute1 + ": All");
        assertEquals(report.getTrackersCount(), 3);
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        analysisPage.addCategory(attribute2);
        assertTrue(analysisPage.getAllCategoryNames().contains(attribute2));
        assertEquals(analysisPage.getFilterText(attribute2), attribute2 + ": All");
        assertEquals(report.getTrackersCount(), 6);
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {TRENDING_GROUP})
    public void supportParameter() {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withMetrics(metric1));
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        TrendingRecommendation trendingRecommendation =
                recommendationContainer.getRecommendation(RecommendationStep.SEE_TREND);
        trendingRecommendation.select("Month").apply();
        assertTrue(analysisPage.getAllCategoryNames().contains(DATE));
        assertTrue(analysisPage.isFilterVisible(DATE));
        assertEquals(analysisPage.getFilterText(DATE), DATE + ": Last 4 quarters");
        assertTrue(analysisPage.isShowPercentConfigEnabled());
        assertTrue(analysisPage.isCompareSamePeriodConfigEnabled());
        assertFalse(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
        assertEquals(report.getTrackersCount(), 12);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {TRENDING_GROUP})
    public void displayInColumnChartWithOnlyMetric() {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withMetrics(metric1));
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));

        analysisPage.addFilter(attribute2);
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));

        analysisPage.changeReportType(ReportType.BAR_CHART);
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() == 0);

        analysisPage.changeReportType(ReportType.COLUMN_CHART);
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));

        analysisPage.addCategory(attribute2);
        assertFalse(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {TRENDING_GROUP})
    public void displayWhenDraggingFirstMetric() {
        initAnalysePage();

        analysisPage.dragAndDropMetricToShortcutPanel(metric1, ShortcutPanel.TRENDED_OVER_TIME);
        assertTrue(analysisPage.getAllCategoryNames().contains(DATE));
        assertTrue(analysisPage.isFilterVisible(DATE));
        assertEquals(analysisPage.getFilterText(DATE), DATE + ": Last 4 quarters");
        assertEquals(analysisPage.getChartReport().getTrackersCount(), 4);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {EXPLORE_PROJECT_DATA_GROUP})
    public void exploreDate() {
        initAnalysePage();
        StringBuilder expected = new StringBuilder(DATE).append("\n")
                .append("Represents all of your date fields in project. Can group by Day, Week, Month, Quarter & Year.\n")
                .append("Field Type\n")
                .append("Date\n");
        assertEquals(analysisPage.getTimeDescription(DATE), expected.toString());
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {EXPORT_GROUP})
    public void exportCustomDiscovery() throws InterruptedException {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withType(ReportType.TABLE)
                .withMetrics(metric3).withCategories(attribute1));
        assertTrue(analysisPage.isExportToReportButtonEnabled());
        TableReport analysisReport = analysisPage.getTableReport();
        List<List<String>> analysisContent = analysisReport.getContent();
        Iterator<String> analysisHeaders = analysisReport.getHeaders().iterator();

        analysisPage.exportReport();
        String currentWindowHandel = browser.getWindowHandle();
        for (String handel : browser.getWindowHandles()) {
            if (!handel.equals(currentWindowHandel))
                browser.switchTo().window(handel);
        }

        com.gooddata.qa.graphene.fragments.reports.TableReport tableReport =
                Graphene.createPageFragment(
                        com.gooddata.qa.graphene.fragments.reports.TableReport.class,
                        waitForElementVisible(By.id("gridContainerTab"), browser));

        Iterator<String> attributes = tableReport.getAttributeElements().iterator();

        Thread.sleep(2000); // wait for metric values is calculated and loaded
        Iterator<Float> metrics = tableReport.getMetricElements().iterator();

        List<List<String>> content = new ArrayList<List<String>>();
        while (attributes.hasNext() && metrics.hasNext()) {
            content.add(Arrays.asList(attributes.next(), String.valueOf(metrics.next())));
        }

        assertEquals(content, analysisContent, "Content is not correct");

        List<String> headers = tableReport.getAttributesHeader();
        headers.addAll(tableReport.getMetricsHeader());
        Iterator<String> reportheaders = headers.iterator();

        while (analysisHeaders.hasNext() && reportheaders.hasNext()) {
            assertEquals(reportheaders.next().toLowerCase(), analysisHeaders.next().toLowerCase(), "Headers are not correct");
        }

        browser.close();
        browser.switchTo().window(currentWindowHandel);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {EXPORT_GROUP})
    public void exportVisualizationWithOneAttributeInChart() {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withCategories(attribute1));
        assertEquals(analysisPage.getExplorerMessage(), "Now select a metric to display");
        assertFalse(analysisPage.isExportToReportButtonEnabled());
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {FILTER_GROUP})
    public void filterOnDateAttribute() {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withMetrics(metric1)
                .withCategories(attribute2).withFilters(DATE));
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 6);
        assertEquals(analysisPage.getFilterText(DATE), DATE + ": All time");

        analysisPage.configTimeFilter("This year");
        assertEquals(analysisPage.getFilterText(DATE), DATE + ": This year");
        assertEquals(report.getTrackersCount(), 6);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {FILTER_GROUP})
    public void testDateInCategoryAndDateInFilter() {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withMetrics(metric1).withCategories(DATE));
        assertEquals(analysisPage.getChartReport().getTrackersCount(), 3);
        assertEquals(analysisPage.getFilterText(DATE), DATE + ": All time");
        assertEquals(analysisPage.getAllGranularities(),
                Arrays.asList("Day", "Week (Sun-Sat)", "Month", "Quarter", "Year"));
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {FILTER_GROUP})
    public void trendingRecommendationOverrideDateFilter() {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withMetrics(metric1).withFilters(DATE));
        assertEquals(analysisPage.getFilterText(DATE), DATE + ": All time");
        analysisPage.configTimeFilter("This month");
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 1);

        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        recommendationContainer.getRecommendation(RecommendationStep.SEE_TREND).apply();;
        assertEquals(analysisPage.getFilterText(DATE), DATE + ": Last 4 quarters");
        assertEquals(report.getTrackersCount(), 4);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {FILTER_GROUP})
    public void dragAndDropAttributeToFilterBucket() {
        initAnalysePage();

        analysisPage
                .createReport(new ReportDefinition().withMetrics(metric1).withCategories(attribute1));
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 3);
        assertEquals(analysisPage.getFilterText(attribute1), attribute1 + ": All");

        assertEquals(analysisPage.addFilter(attribute2).getFilterText(attribute2), attribute2 + ": All");
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {FILTER_GROUP})
    public void addFilterDoesNotHideRecommendation() {
        initAnalysePage();

        analysisPage
                .createReport(new ReportDefinition().withMetrics(metric1).withCategories(attribute1));
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 3);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer
                .isRecommendationVisible(RecommendationStep.SEE_PERCENTS));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        analysisPage.addFilter(attribute2);
        assertTrue(recommendationContainer
                .isRecommendationVisible(RecommendationStep.SEE_PERCENTS));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {PERIOD_OVER_PERIOD_GROUP})
    public void testSimplePoP() {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withMetrics(metric1).withCategories(DATE));
        assertTrue(analysisPage.isFilterVisible(DATE));
        assertEquals(analysisPage.getFilterText(DATE), DATE + ": All time");
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 3);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
        recommendationContainer.getRecommendation(RecommendationStep.COMPARE).apply();
        assertEquals(report.getTrackersCount(), 6);
        List<String> legends = report.getLegends();
        assertEquals(legends.size(), 2);
        assertEquals(legends, Arrays.asList(metric1 + " - previous year", metric1));

        analysisPage.addMetric(metric2);
        assertEquals(report.getTrackersCount(), 3);
        legends = report.getLegends();
        assertEquals(legends.size(), 1);
        assertEquals(legends, Arrays.asList("Series 1"));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {PERIOD_OVER_PERIOD_GROUP})
    public void testAnotherApproachToShowPoP() {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withMetrics(metric1));
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        recommendationContainer.getRecommendation(RecommendationStep.SEE_TREND).apply();

        assertTrue(analysisPage.getAllCategoryNames().contains(DATE));
        assertTrue(analysisPage.isFilterVisible(DATE));
        assertEquals(analysisPage.getFilterText(DATE), DATE + ": Last 4 quarters");
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {RESET_GROUP})
    public void testResetFunction() {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withMetrics(metric1));
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        analysisPage.changeReportType(ReportType.BAR_CHART);
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() == 0);

        analysisPage.addCategory(attribute1);
        assertEquals(report.getTrackersCount(), 3);

        analysisPage.resetToBlankState();
    }

    protected void testComparisonAndPoPAttribute(int trackersCountInLastState) {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withMetrics(metric1));
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        ComparisonRecommendation comparisonRecommendation =
                recommendationContainer.getRecommendation(RecommendationStep.COMPARE);
        comparisonRecommendation.select(attribute1).apply();
        assertTrue(analysisPage.getAllCategoryNames().contains(attribute1));
        assertEquals(analysisPage.getFilterText(attribute1), attribute1 + ": All");
        assertEquals(report.getTrackersCount(), 3);
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        comparisonRecommendation =
                recommendationContainer.getRecommendation(RecommendationStep.COMPARE);
        comparisonRecommendation.select("This month").apply();
        assertEquals(analysisPage.getFilterText(DATE), DATE + ": This month");
        assertEquals(report.getTrackersCount(), 6);
        List<String> legends = report.getLegends();
        assertEquals(legends.size(), 2);
        assertEquals(legends, Arrays.asList(metric1 + " - previous year", metric1));

        analysisPage.addCategory(attribute2);
        assertEquals(analysisPage.getFilterText(attribute2), attribute2 + ": All");
        assertEquals(report.getTrackersCount(), trackersCountInLastState);
        legends = report.getLegends();
        assertEquals(legends.size(), 2);
        assertEquals(legends, Arrays.asList(metric1 + " - previous year", metric1));
    }

    protected void compararisonRecommendationOverrideDateFilter(int trackersCountInLastState) {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withMetrics(metric1)
                .withCategories(attribute2).withFilters(DATE));
        analysisPage.configTimeFilter("Last year");
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 6);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        ComparisonRecommendation comparisonRecommendation =
                recommendationContainer.getRecommendation(RecommendationStep.COMPARE);
        comparisonRecommendation.select("This month").apply();
        assertEquals(analysisPage.getFilterText(DATE), DATE + ": This month");
        assertEquals(report.getTrackersCount(), trackersCountInLastState);
        List<String> legends = report.getLegends();
        assertEquals(legends.size(), 2);
        assertEquals(legends, Arrays.asList(metric1 + " - previous year", metric1));
    }

    protected void filterOnAttribute(String filterText, String... filterValues) {
        initAnalysePage();

        analysisPage
                .createReport(new ReportDefinition().withMetrics(metric1).withCategories(attribute1));
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 3);
        assertEquals(analysisPage.getFilterText(attribute1), attribute1 + ": All");

        WebElement filter = analysisPage.getFilter(attribute1);
        filter.click();
        AttributeFilterPickerPanel attributePanel =
                Graphene.createPageFragment(AttributeFilterPickerPanel.class,
                        waitForElementVisible(AttributeFilterPickerPanel.LOCATOR, browser));
        attributePanel.assertPanel();
        attributePanel.discard();

        analysisPage.configAttributeFilter(attribute1, filterValues);
        assertEquals(report.getTrackersCount(), 2);
        assertEquals(analysisPage.getFilterText(attribute1), filterText);
    }

    protected void attributeFilterIsRemovedWhenRemoveAttributeInCatalogue(String filterText, String... filterValues) {
        initAnalysePage();

        analysisPage
                .createReport(new ReportDefinition().withMetrics(metric1).withCategories(attribute1));
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 3);
        assertEquals(analysisPage.getFilterText(attribute1), attribute1 + ": All");

        analysisPage.configAttributeFilter(attribute1, filterValues);
        assertEquals(report.getTrackersCount(), 2);
        assertEquals(analysisPage.getFilterText(attribute1), filterText);

        analysisPage.configAttributeFilter(attribute1, "All");
        assertEquals(report.getTrackersCount(), 3);
        assertEquals(analysisPage.getFilterText(attribute1), attribute1 + ": All");

        analysisPage.addCategory(attribute2);
        assertFalse(analysisPage.isFilterVisible(attribute1));
    }

    protected void verifyChartReport(ReportDefinition reportDefinition, List<List<String>> tooltip) {
        initAnalysePage();

        analysisPage.createReport(reportDefinition);

        ChartReport chartReport = analysisPage.getChartReport();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0), tooltip);
        assertEquals(chartReport.getLegends(), Arrays.asList("Series 1"));
        assertEquals(chartReport.getLegendColors(), Arrays.asList("rgb(109, 118, 128)"));
        assertEquals(chartReport.getLegendColorByName("Series 1"), "rgb(109, 118, 128)");
//        assertTrue(chartReport.clickOnTrackerByIndex(0).isTrackerInSelectedStateByIndex(0));
//        assertFalse(chartReport.isTrackerInSelectedStateByIndex(1));
//        assertTrue(chartReport.clickOnLegendByName("Series 1").isTrackerInNormalStateByIndex(0));
//        assertTrue(chartReport.isTrackerInNormalStateByIndex(1));
    }

    protected void verifyTableReportContent(ReportDefinition reportDefinition, List<String> headers, List<List<String>> content) {
        initAnalysePage();

        analysisPage.createReport(reportDefinition);

        TableReport tableReport = analysisPage.getTableReport();
        assertEquals(tableReport.getHeaders(), headers);
        assertEquals(tableReport.getContent(), content);
    }
}
