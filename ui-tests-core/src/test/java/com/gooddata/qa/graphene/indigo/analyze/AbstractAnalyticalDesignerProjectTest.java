package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForFragmentNotVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.indigo.ReportDefinition;
import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.indigo.ShortcutPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributeFilterPickerPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.DateFilterPickerPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.ComparisonRecommendation;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.TrendingRecommendation;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.google.common.collect.Lists;

public abstract class AbstractAnalyticalDesignerProjectTest extends AbstractProjectTest {

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
    protected static final String UNDO_REDO_GROUP = "undo_redo";
    protected static final String DATA_COMBINATION = "data_combination";

    protected String metric1;
    protected String metric2;
    protected String metric3;
    protected String metric4;
    protected String attribute1;
    protected String attribute2;
    protected String attribute3;

    protected String notAvailableAttribute;

    @BeforeClass
    public void initStartPage() {
        startPage = "projects.html";
        projectCreateCheckIterations = 60; // 5 minutes
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"init"})
    public void turnOffWalkme() {
        initAnalysePage();

        try {
            WebElement walkmeCloseElement = waitForElementVisible(By.className("walkme-action-close"), browser);
            walkmeCloseElement.click();
            waitForElementNotPresent(walkmeCloseElement);
        } catch (TimeoutException e) {
            System.out.println("Walkme dialog is not appeared!");
        }
    }

    @Test(dependsOnGroups = {"init"}, groups = {CUSTOM_DISCOVERY_GROUP})
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

    @Test(dependsOnGroups = {"init"}, groups = {CUSTOM_DISCOVERY_GROUP})
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

    @Test(dependsOnGroups = {"init"}, groups = {CUSTOM_DISCOVERY_GROUP})
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

    @Test(dependsOnGroups = {"init"}, groups = {CUSTOM_DISCOVERY_GROUP})
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

    @Test(dependsOnGroups = {"init"}, groups = {CUSTOM_DISCOVERY_GROUP})
    public void testAccessibilityGuidanceForAttributesMetrics() throws InterruptedException {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withMetrics(metric1));
        analysisPage.addInapplicableCategory(notAvailableAttribute);
        assertEquals(analysisPage.getExplorerMessage(), "Visualization cannot be displayed");
        Screenshots.takeScreenshot(browser,
                "testAccessibilityGuidanceForAttributesMetrics - inapplicableCategory", getClass());

        assertTrue(analysisPage.searchBucketItem(notAvailableAttribute));
        Screenshots.takeScreenshot(browser, 
                "testAccessibilityGuidanceForAttributesMetrics - searchInapplicableCategory", getClass());
        assertTrue(analysisPage.getAllCatalogueItemsInViewPort().contains(notAvailableAttribute));
        assertFalse(analysisPage.searchBucketItem(notAvailableAttribute + "not found"));
        Screenshots.takeScreenshot(browser,
                "testAccessibilityGuidanceForAttributesMetrics - searchNotFound", getClass());
    }

    @Test(dependsOnGroups = {"init"}, groups = {CONTRIBUTION_GROUP})
    public void testSimpleContribution() {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withMetrics(metric1)
                .withCategories(attribute2));
        analysisPage.waitForReportComputing();
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
        analysisPage.waitForReportComputing();
        assertTrue(analysisPage.isReportTypeSelected(ReportType.BAR_CHART));
        assertEquals(report.getTrackersCount(), 3);
        assertTrue(analysisPage.isShowPercentConfigEnabled());
        assertFalse(analysisPage.isShowPercentConfigSelected());
    }

    @Test(dependsOnGroups = {"init"}, groups = {CONTRIBUTION_GROUP})
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

    @Test(dependsOnGroups = {"init"}, groups = {COMPARISON_GROUP})
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
        assertTrue(analysisPage.getAllAddedCategoryNames().contains(attribute1));
        assertEquals(analysisPage.getFilterText(attribute1), attribute1 + ": All");
        assertEquals(report.getTrackersCount(), 3);
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        analysisPage.addCategory(attribute2);
        assertTrue(analysisPage.getAllAddedCategoryNames().contains(attribute2));
        assertEquals(analysisPage.getFilterText(attribute2), attribute2 + ": All");
        assertEquals(report.getTrackersCount(), 6);
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
    }

    @Test(dependsOnGroups = {"init"}, groups = {COMPARISON_GROUP})
    public void testComparisonAndPoPAttribute() {
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
        assertTrue(analysisPage.getAllAddedCategoryNames().contains(attribute1));
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
        assertEquals(report.getTrackersCount(), 12);
        legends = report.getLegends();
        assertEquals(legends.size(), 2);
        assertEquals(legends, Arrays.asList(metric1 + " - previous year", metric1));
    }

    @Test(dependsOnGroups = {"init"}, groups = {TRENDING_GROUP})
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
        assertTrue(analysisPage.getAllAddedCategoryNames().contains(DATE));
        assertTrue(analysisPage.isFilterVisible(DATE));
        assertEquals(analysisPage.getFilterText(DATE), DATE + ": Last 4 quarters");
        assertTrue(analysisPage.isShowPercentConfigEnabled());
        assertTrue(analysisPage.isCompareSamePeriodConfigEnabled());
        assertFalse(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
        assertEquals(report.getTrackersCount(), 12);
    }

    @Test(dependsOnGroups = {"init"}, groups = {TRENDING_GROUP})
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

    @Test(dependsOnGroups = {"init"}, groups = {TRENDING_GROUP})
    public void displayWhenDraggingFirstMetric() {
        initAnalysePage();

        analysisPage.dragAndDropMetricToShortcutPanel(metric1, ShortcutPanel.TRENDED_OVER_TIME);
        assertTrue(analysisPage.getAllAddedCategoryNames().contains(DATE));
        assertTrue(analysisPage.isFilterVisible(DATE));
        assertEquals(analysisPage.getFilterText(DATE), DATE + ": Last 4 quarters");
        assertEquals(analysisPage.getChartReport().getTrackersCount(), 4);
    }

    @Test(dependsOnGroups = {"init"}, groups = {EXPLORE_PROJECT_DATA_GROUP})
    public void exploreDate() {
        initAnalysePage();
        StringBuilder expected = new StringBuilder(DATE).append("\n")
                .append("Represents all of your date fields in project. Can group by Day, Week, Month, Quarter & Year.\n")
                .append("Field Type\n")
                .append("Date\n");
        assertEquals(analysisPage.getTimeDescription(DATE), expected.toString());
    }

    @Test(dependsOnGroups = {"init"}, groups = {EXPORT_GROUP})
    public void exportCustomDiscovery() throws InterruptedException {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withType(ReportType.TABLE)
                .withMetrics(metric1).withCategories(attribute3));
        assertTrue(analysisPage.isExportToReportButtonEnabled());
        TableReport analysisReport = analysisPage.getTableReport();
        List<List<String>> analysisContent = analysisReport.getContent();
        Iterator<String> analysisHeaders = analysisReport.getHeaders().iterator();

        analysisPage.exportReport();
        String currentWindowHandle = browser.getWindowHandle();
        for (String handle : browser.getWindowHandles()) {
            if (!handle.equals(currentWindowHandle))
                browser.switchTo().window(handle);
        }

        com.gooddata.qa.graphene.fragments.reports.TableReport tableReport =
                Graphene.createPageFragment(
                        com.gooddata.qa.graphene.fragments.reports.TableReport.class,
                        waitForElementVisible(By.id("gridContainerTab"), browser));

        Iterator<String> attributes = tableReport.getAttributeElements().iterator();

        Thread.sleep(2000); // wait for metric values is calculated and loaded
        Iterator<String> metrics = tableReport.getRawMetricElements().iterator();

        List<List<String>> content = new ArrayList<List<String>>();
        while (attributes.hasNext() && metrics.hasNext()) {
            content.add(Arrays.asList(attributes.next(), metrics.next()));
        }

        assertEquals(content, analysisContent, "Content is not correct");

        List<String> headers = tableReport.getAttributesHeader();
        headers.addAll(tableReport.getMetricsHeader());
        Iterator<String> reportheaders = headers.iterator();

        while (analysisHeaders.hasNext() && reportheaders.hasNext()) {
            assertEquals(reportheaders.next().toLowerCase(), analysisHeaders.next().toLowerCase(), "Headers are not correct");
        }

        browser.close();
        browser.switchTo().window(currentWindowHandle);
    }

    @Test(dependsOnGroups = {"init"}, groups = {EXPORT_GROUP})
    public void exportVisualizationWithOneAttributeInChart() {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withCategories(attribute1));
        assertEquals(analysisPage.getExplorerMessage(), "Now select a metric to display");
        assertFalse(analysisPage.isExportToReportButtonEnabled());
    }

    @Test(dependsOnGroups = {"init"}, groups = {FILTER_GROUP})
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

    @Test(dependsOnGroups = {"init"}, groups = {FILTER_GROUP})
    public void testDateInCategoryAndDateInFilter() {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withMetrics(metric1).withCategories(DATE));
        assertEquals(analysisPage.getChartReport().getTrackersCount(), 3);
        assertEquals(analysisPage.getFilterText(DATE), DATE + ": All time");
        assertEquals(analysisPage.getAllGranularities(),
                Arrays.asList("Day", "Week (Sun-Sat)", "Month", "Quarter", "Year"));
    }

    @Test(dependsOnGroups = {"init"}, groups = {FILTER_GROUP})
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

    @Test(dependsOnGroups = {"init"}, groups = {FILTER_GROUP})
    public void dragAndDropAttributeToFilterBucket() {
        initAnalysePage();

        analysisPage
                .createReport(new ReportDefinition().withMetrics(metric1).withCategories(attribute1));
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 3);
        assertEquals(analysisPage.getFilterText(attribute1), attribute1 + ": All");

        assertEquals(analysisPage.addFilter(attribute2).getFilterText(attribute2), attribute2 + ": All");
    }

    @Test(dependsOnGroups = {"init"}, groups = {FILTER_GROUP})
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

    @Test(dependsOnGroups = {"init"}, groups = {FILTER_GROUP})
    public void compararisonRecommendationOverrideDateFilter() {
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
        assertEquals(report.getTrackersCount(), 12);
        List<String> legends = report.getLegends();
        assertEquals(legends.size(), 2);
        assertEquals(legends, Arrays.asList(metric1 + " - previous year", metric1));
    }

    @Test(dependsOnGroups = {"init"}, groups = {FILTER_GROUP})
    public void checkTooltipDateFilterPreset() {
        initAnalysePage();

        analysisPage.addFilter(DATE);
        analysisPage.getFilter(DATE).click();
        DateFilterPickerPanel panel = Graphene.createPageFragment(DateFilterPickerPanel.class,
                waitForElementVisible(DateFilterPickerPanel.LOCATOR, browser));
        panel.hoverOnPeriod("This month");
        String currentMonthYear = new SimpleDateFormat("MMM YYYY")
            .format(Calendar.getInstance(TimeZone.getTimeZone("GMT-7:00")).getTime());
        System.out.println(currentMonthYear);
        assertTrue(panel.getTooltipFromPeriod().startsWith(currentMonthYear));
    }

    @Test(dependsOnGroups = {"init"}, groups = {FILTER_GROUP})
    public void checkDefaultValueInDateRange() {
        initAnalysePage();

        analysisPage.addFilter(DATE);
        analysisPage.getFilter(DATE).click();
        DateFilterPickerPanel panel = Graphene.createPageFragment(DateFilterPickerPanel.class,
                waitForElementVisible(DateFilterPickerPanel.LOCATOR, browser));

        panel.changeToDateRangeSection();

        Calendar date = Calendar.getInstance(TimeZone.getTimeZone("GMT-7:00"));
        assertEquals(panel.getToDate(), getTimeString(date));

        date.add(Calendar.DAY_OF_MONTH, -29);
        assertEquals(panel.getFromDate(), getTimeString(date));
    }

    @Test(dependsOnGroups = {"init"}, groups = {FILTER_GROUP})
    public void switchingDateRangeNotComputeReport() {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withMetrics(metric1).withCategories(attribute1)
                .withFilters(DATE));
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 3);
        assertEquals(analysisPage.getFilterText(DATE), DATE + ": All time");

        WebElement dateFilter = analysisPage.getFilter(DATE);
        dateFilter.click();
        DateFilterPickerPanel panel = Graphene.createPageFragment(DateFilterPickerPanel.class,
                waitForElementVisible(DateFilterPickerPanel.LOCATOR, browser));
        panel.changeToDateRangeSection();
        assertFalse(analysisPage.isReportComputing());
        panel.changeToPresetsSection();
        assertFalse(analysisPage.isReportComputing());
        dateFilter.click();
        waitForFragmentNotVisible(panel);
    }

    @Test(dependsOnGroups = {"init"}, groups = {FILTER_GROUP})
    public void allowDateFilterByRange() throws ParseException, InterruptedException {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withMetrics(metric1).withCategories(attribute1)
                .withFilters(DATE));
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 3);
        assertEquals(analysisPage.getFilterText(DATE), DATE + ": All time");
        analysisPage.configTimeFilterByRangeButNotApply("01/12/2014", "01/12/2015").exportReport();
        String currentWindowHandle = browser.getWindowHandle();
        for (String handle : browser.getWindowHandles()) {
            if (!handle.equals(currentWindowHandle))
                browser.switchTo().window(handle);
        }
        waitForFragmentVisible(reportPage);
        Screenshots.takeScreenshot(browser, "allowDateFilterByRange-emptyFilters", getClass());
        assertTrue(reportPage.getFilters().isEmpty());
        browser.close();
        browser.switchTo().window(currentWindowHandle);

        analysisPage.configTimeFilterByRange("01/12/2014", "01/12/2015");
        analysisPage.waitForReportComputing();
        assertEquals(report.getTrackersCount(), 3);
        analysisPage.exportReport();
        currentWindowHandle = browser.getWindowHandle();
        for (String handle : browser.getWindowHandles()) {
            if (!handle.equals(currentWindowHandle))
                browser.switchTo().window(handle);
        }
        waitForFragmentVisible(reportPage);
        List<String> filters = reportPage.getFilters();
        Screenshots.takeScreenshot(browser, "allowDateFilterByRange-dateFilters", getClass());
        assertEquals(filters.size(), 1);
        assertEquals(filters.get(0), "Date (Date) is between 01/12/2014 and 01/12/2015");
        browser.close();
        browser.switchTo().window(currentWindowHandle);
    }

    @Test(dependsOnGroups = {"init"}, groups = {PERIOD_OVER_PERIOD_GROUP})
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
        analysisPage.waitForReportComputing();
        assertEquals(report.getTrackersCount(), 6);
        List<String> legends = report.getLegends();
        assertEquals(legends.size(), 2);
        assertEquals(legends, Arrays.asList(metric1 + " - previous year", metric1));

        analysisPage.addMetric(metric2);
        analysisPage.waitForReportComputing();
        assertEquals(report.getTrackersCount(), 6);
        legends = report.getLegends();
        assertEquals(legends.size(), 2);
        assertEquals(legends, Arrays.asList(metric1, metric2));
    }

    @Test(dependsOnGroups = {"init"}, groups = {PERIOD_OVER_PERIOD_GROUP})
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

        assertTrue(analysisPage.getAllAddedCategoryNames().contains(DATE));
        assertTrue(analysisPage.isFilterVisible(DATE));
        assertEquals(analysisPage.getFilterText(DATE), DATE + ": Last 4 quarters");
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
    }

    @Test(dependsOnGroups = {"init"}, groups = {RESET_GROUP})
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

    @Test(dependsOnGroups = {"init"}, groups = {UNDO_REDO_GROUP})
    public void testUndoRedoAfterAddMetric() {
        initAnalysePage();

        analysisPage.addMetric(metric1);
        ReportState baseState = ReportState.getCurrentState(analysisPage);

        checkUndoRedoForEmptyState(true);
        checkUndoRedoForReport(baseState, false);

        analysisPage.addMetric(metric2);
        checkUndoRedoForReport(baseState, true);
    }

    @Test(dependsOnGroups = {"init"}, groups = {UNDO_REDO_GROUP})
    public void testUndoRedoAfterAddAtribute() {
        initAnalysePage();

        analysisPage.addCategory(attribute1);
        analysisPage.waitForReportComputing();

        checkUndoRedoForEmptyState(true);

        analysisPage.redo();
        assertTrue(analysisPage.getAllAddedCategoryNames().contains(attribute1));
    }

    @Test(dependsOnGroups = {"init"}, groups = {UNDO_REDO_GROUP})
    public void testUndoRedoAfterRemoveMetricAndAttribute() {
        initAnalysePage();

        analysisPage.addMetric(metric1);
        ReportState baseState = ReportState.getCurrentState(analysisPage);

        analysisPage.removeMetric(metric1);
        assertFalse(analysisPage.getAllAddedMetricNames().contains(metric1));

        checkUndoRedoForReport(baseState, true);
        checkUndoRedoForEmptyState(false);

        analysisPage.addMetric(metric1);
        analysisPage.addCategory(attribute1);
        ReportState baseStateWithAttribute = ReportState.getCurrentState(analysisPage);

        analysisPage.removeCategory(attribute1);
        analysisPage.waitForReportComputing();

        checkUndoRedoForReport(baseStateWithAttribute, true);
        checkUndoRedoForReport(baseState, false);
    }

    @Test(dependsOnGroups = {"init"}, groups = {UNDO_REDO_GROUP})
    public void testUndoRedoAfterAddFilter() {
        int actionsCount = 0;
        initAnalysePage();

        analysisPage.addCategory(attribute1); actionsCount++;
        analysisPage.addMetric(metric1); actionsCount++;
        analysisPage.addFilter(attribute2); actionsCount++;

        analysisPage.undo();
        assertFalse(analysisPage.isFilterVisible(attribute2));

        analysisPage.redo();
        assertTrue(analysisPage.isFilterVisible(attribute2));

        analysisPage.removeFilter(attribute2);
        actionsCount++;
        assertFalse(analysisPage.isFilterVisible(attribute2));
        Screenshots.takeScreenshot(browser, "Indigo_remove_filter", this.getClass());

        analysisPage.undo();
        assertTrue(analysisPage.isFilterVisible(attribute2));

        analysisPage.redo();
        assertFalse(analysisPage.isFilterVisible(attribute2));

        // Check that the undo must go back to the start of his session
        assertTrue(analysisPage.isUndoButtonEnabled());
        assertFalse(analysisPage.isRedoButtonEnabled());
        for (int i = 1; i <= actionsCount; i++) {
            analysisPage.undo();
        }
        assertFalse(analysisPage.isUndoButtonEnabled());
        assertTrue(analysisPage.isRedoButtonEnabled());
    }

    @Test(dependsOnGroups = {"init"}, groups = {UNDO_REDO_GROUP})
    public void testUndoRedoAfterChangeReportType() {
        initAnalysePage();

        analysisPage.changeReportType(ReportType.TABLE);
        assertTrue(analysisPage.isReportTypeSelected(ReportType.TABLE));

        analysisPage.undo();
        assertTrue(analysisPage.isReportTypeSelected(ReportType.COLUMN_CHART));

        analysisPage.redo();
        assertTrue(analysisPage.isReportTypeSelected(ReportType.TABLE));
    }

    @Test(dependsOnGroups = {"init"}, groups = {UNDO_REDO_GROUP})
    public void testUndoAfterReset() {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withMetrics(metric1).withCategories(attribute1));
        ReportState baseState = ReportState.getCurrentState(analysisPage);
        analysisPage.resetToBlankState();
        checkUndoRedoForReport(baseState, true);
    }

    @Test(dependsOnGroups = {"init"}, groups = {UNDO_REDO_GROUP})
    public void testUndoNotApplicableOnNonActiveSession() {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withMetrics(metric1));
        ReportState baseState = ReportState.getCurrentState(analysisPage);
        analysisPage.addCategory(attribute1)
            .searchBucketItem(attribute2);
        assertEquals(analysisPage.getAllCatalogueItemsInViewPort(), Arrays.asList("DATA FIELDS", attribute2));
        checkUndoRedoForReport(baseState, true);
        assertEquals(analysisPage.getAllCatalogueItemsInViewPort(), Arrays.asList("DATA FIELDS", attribute2));

        analysisPage.addCategory(attribute1).exportReport();
        checkUndoRedoForReport(baseState, true);
    }

    @Test(dependsOnGroups = {"init"}, groups = {DATA_COMBINATION})
    public void checkSeriesStateTransitions() {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withMetrics(metric1).withCategories(DATE));
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 3);
        assertTrue(analysisPage.isCompareSamePeriodConfigEnabled());
        assertTrue(analysisPage.isShowPercentConfigEnabled());
        assertEquals(report.getLegends(), Arrays.asList(metric1));
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        analysisPage.addMetric(metric2);
        assertEquals(report.getTrackersCount(), 6);
        assertFalse(analysisPage.isCompareSamePeriodConfigEnabled());
        assertFalse(analysisPage.isShowPercentConfigEnabled());
        assertEquals(report.getLegends(), Arrays.asList(metric1, metric2));
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() == 0);
        assertEquals(analysisPage.getAllAddedMetricNames(), Arrays.asList(metric1, metric2));

        analysisPage.addMetric(metric3);
        assertTrue(report.getTrackersCount() > 0);
        assertEquals(analysisPage.getAllAddedMetricNames(), Arrays.asList(metric1, metric2, metric3));

        analysisPage.addMetric(metric4);
        assertTrue(report.getTrackersCount() > 0);
        assertEquals(analysisPage.getAllAddedMetricNames(), Arrays.asList(metric2, metric3, metric4));

        analysisPage.addMetric(metric4);
        assertTrue(report.getTrackersCount() > 0);
        assertEquals(analysisPage.getAllAddedMetricNames(), Arrays.asList(metric2, metric3, metric4));
    }

    @Test(dependsOnGroups = {"init"}, groups = {DATA_COMBINATION})
    public void checkMetricFormating() {
        initMetricPage();

        waitForFragmentVisible(metricEditorPage).openMetricDetailPage(metric1);
        String oldFormat = waitForFragmentVisible(metricDetailPage).getMetricFormat();
        metricDetailPage.changeMetricFormat(oldFormat + "[red]");

        try {
            initAnalysePage();

            analysisPage.createReport(new ReportDefinition().withMetrics(metric1));
            ChartReport report = analysisPage.getChartReport();
            assertEquals(report.getTrackersCount(), 1);
            List<String> dataLabels = report.getDataLabels();
            assertEquals(dataLabels.size(), 1);

            TableReport tableReport = analysisPage.changeReportType(ReportType.TABLE).getTableReport();
            assertEquals(tableReport.getFormatFromValue(dataLabels.get(0)), "color:#FF0000");
        } finally {
            initMetricPage();
            waitForFragmentVisible(metricEditorPage).openMetricDetailPage(metric1);
            waitForFragmentVisible(metricDetailPage).changeMetricFormat(oldFormat);
        }
    }

    @Test(dependsOnGroups = {"init"}, groups = {DATA_COMBINATION})
    public void checkReportContentWhenAdd3Metrics1Attribute() {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withMetrics(metric1, metric2, metric3)
                .withCategories(attribute1).withType(ReportType.TABLE));
        TableReport report = analysisPage.getTableReport();
        List<List<String>> analysisContent = report.getContent();

        analysisPage.exportReport();
        String currentWindowHandle = browser.getWindowHandle();
        for (String handle : browser.getWindowHandles()) {
            if (!handle.equals(currentWindowHandle))
                browser.switchTo().window(handle);
        }

        assertEquals(analysisContent, getTableContentFromReportPage(Graphene.createPageFragment(
                        com.gooddata.qa.graphene.fragments.reports.TableReport.class,
                        waitForElementVisible(By.id("gridContainerTab"), browser))));

        browser.close();
        browser.switchTo().window(currentWindowHandle);
    }

    @Test(dependsOnGroups = {"init"}, groups = {DATA_COMBINATION})
    public void checkShowPercentAndLegendColor() {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withMetrics(metric1).withCategories(attribute1));
        analysisPage.turnOnShowInPercents();
        ChartReport report = analysisPage.getChartReport();
        assertTrue(report.getDataLabels().get(0).endsWith("%"));

        analysisPage.addMetric(metric2);
        assertFalse(analysisPage.isShowPercentConfigEnabled());
        assertFalse(analysisPage.isShowPercentConfigSelected());

        assertEquals(report.getLegendColors(), Arrays.asList("rgb(13, 103, 178)", "rgb(76, 178, 72)"));
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
        assertEquals(chartReport.getLegends(), reportDefinition.getMetrics());
        assertEquals(chartReport.getLegendColors(), Arrays.asList("rgb(13,103,178)"));
        assertEquals(chartReport.getLegendColorByName(reportDefinition.getMetrics().get(0)), "rgb(13, 103, 178)");
    }

    protected void verifyTableReportContent(ReportDefinition reportDefinition, List<String> headers, List<List<String>> content) {
        initAnalysePage();

        analysisPage.createReport(reportDefinition);

        TableReport tableReport = analysisPage.getTableReport();
        assertEquals(tableReport.getHeaders(), headers);
        assertEquals(tableReport.getContent(), content);
    }

    private void checkUndoRedoForReport(ReportState expectedState, boolean isUndo) {
      if (isUndo) {
          analysisPage.undo();
      } else {
          analysisPage.redo();
      }

      if (expectedState == null) {
          assertTrue(analysisPage.isBucketBlankState());
          assertTrue(analysisPage.isMainEditorBlankState());
      } else {
          ReportState currentState = ReportState.getCurrentState(analysisPage);
          assertTrue(currentState.equals(expectedState));
      }
    }

    private void checkUndoRedoForEmptyState(boolean isUndo) {
        checkUndoRedoForReport(null, isUndo);
    }

    private String getTimeString(Calendar date) {
        StringBuilder timeBuilder = new StringBuilder();
        timeBuilder.append(String.format("%02d", date.get(Calendar.MONTH) + 1)).append("/");
        timeBuilder.append(String.format("%02d", date.get(Calendar.DAY_OF_MONTH))).append("/");
        timeBuilder.append(date.get(Calendar.YEAR));
        return timeBuilder.toString();
    }

    private List<List<String>> getTableContentFromReportPage(
            com.gooddata.qa.graphene.fragments.reports.TableReport tableReport) {
        List<List<String>> content = Lists.newArrayList();
        List<String> attributes = tableReport.getAttributeElements();
        List<String> metrics = tableReport.getRawMetricElements();
        int totalAttributes = attributes.size();
        int i = 0;
        for (String attr: attributes) {
            List<String> row = Lists.newArrayList(attr);
            for (int k = i; k < metrics.size(); k += totalAttributes) {
                row.add(metrics.get(k));
            }
            content.add(row);
            i++;
        }

        return content;
    }

    private static class ReportState {
        private AnalysisPage analysisPage;

        private int reportTrackerCount;
        private List<String> addedAttributes;
        private List<String> addedMetrics;
        private List<String> reportDataLables;
        private List<String> reportAxisLables;

        public static ReportState getCurrentState(AnalysisPage analysisPage) {
            return new ReportState(analysisPage).saveCurrentState();
        }

        private ReportState(AnalysisPage analysisPage) {
            this.analysisPage = analysisPage;
        }

        private ReportState saveCurrentState() {
            analysisPage.waitForReportComputing();
            ChartReport report = analysisPage.getChartReport();

            reportTrackerCount = report.getTrackersCount();
            addedMetrics = analysisPage.getAllAddedMetricNames();
            addedAttributes = analysisPage.getAllAddedCategoryNames();

            reportDataLables = report.getDataLabels();
            reportAxisLables = report.getAxisLabels();

            return this;
        }

        @Override
        public boolean equals(Object obj){
            if (!(obj instanceof ReportState))
                return false;

            ReportState state = (ReportState)obj;

            if (this.reportTrackerCount != state.reportTrackerCount ||
                !this.addedAttributes.equals(state.addedAttributes) ||
                !this.addedMetrics.equals(state.addedMetrics) ||
                !this.reportDataLables.equals(state.reportDataLables) ||
                !this.reportAxisLables.equals(state.reportAxisLables))
                return false;

            return true;
        }
    }
}
