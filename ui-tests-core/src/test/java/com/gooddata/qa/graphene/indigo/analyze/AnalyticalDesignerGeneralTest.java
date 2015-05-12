package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForFragmentVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import com.gooddata.GoodData;
import com.gooddata.md.Attribute;
import com.gooddata.md.Entry;
import com.gooddata.md.MetadataService;
import com.gooddata.md.Metric;
import com.gooddata.project.Project;
import com.gooddata.qa.graphene.AbstractUITest;
import com.gooddata.qa.graphene.entity.indigo.ReportDefinition;
import com.gooddata.qa.graphene.enums.UserRoles;
import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.indigo.ShortcutPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.ComparisonRecommendation;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.TrendingRecommendation;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class AnalyticalDesignerGeneralTest extends AbstractUITest {

    private static final String DATE = "Date";

    private List<String> attributes;
    private List<String> metrics;
    private Random random = new Random();
    private List<String> brokenMetrics = Lists.newArrayList();
    private List<Pair<String, String>> cache = Lists.newArrayList();

    private Project project;
    private MetadataService mdService;

    @Test(groups = {PROJECT_INIT_GROUP})
    public void init() throws JSONException {
        signIn(false, UserRoles.ADMIN);
        GoodData goodDataClient = getGoodDataClient();
        project = goodDataClient.getProjectService().getProjectById(testParams.getProjectId());
        mdService = goodDataClient.getMetadataService();
    }

    @Test(dependsOnMethods = {"init"}, groups = {PROJECT_INIT_GROUP})
    public void loadAttributes() {
        attributes = Lists.newArrayList(FluentIterable.from(mdService.find(project, Attribute.class))
                .transform(new Function<Entry, String>() {
            @Override
            public String apply(Entry input) {
                return input.getTitle();
            }
        }).filter(new Predicate<String>() {
            @Override
            public boolean apply(String input) {
                return !input.contains(DATE) && !input.contains("Records of");
            }
        }));
    }

    @Test(dependsOnMethods = {"init"}, groups = {PROJECT_INIT_GROUP})
    public void loadMetrics() {
        metrics = Lists.newArrayList(Collections2.transform(mdService.find(project, Metric.class),
                new Function<Entry, String>() {
            @Override
            public String apply(Entry input) {
                return input.getTitle();
            }
        }));
    }

    @Test(dependsOnMethods = {"init"}, groups = {PROJECT_INIT_GROUP})
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

    @Test(dependsOnGroups = {PROJECT_INIT_GROUP})
    public void testCustomDiscovery() {
        initAnalysePage();

        String metric = doSafetyMetricAction(new SafetyActionAdapter() {
            @Override
            public void action(String metric) {
                analysisPage.createReport(new ReportDefinition().withMetrics(metric));
            }
        }, "testCustomDiscovery");

        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        analysisPage.changeReportType(ReportType.BAR_CHART);
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() == 0);

        doSafetyAttributeAction(metric, new SafetyActionAdapter() {
            @Override
            public void action(String attribute) {
                analysisPage.addCategory(attribute);
            }
        }, "testCustomDiscovery");

        assertTrue(report.getTrackersCount() >= 1);
        analysisPage.resetToBlankState();
    }

    @Test(dependsOnGroups = {PROJECT_INIT_GROUP})
    public void testWithAttribute() {
        String attribute = getRandomAttribute();
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withCategories(attribute));
        assertEquals(analysisPage.getExplorerMessage(), "Now select a metric to display");

        assertEquals(analysisPage.changeReportType(ReportType.BAR_CHART).getExplorerMessage(),
                "Now select a metric to display");

        assertEquals(analysisPage.changeReportType(ReportType.LINE_CHART).getExplorerMessage(),
                "Now select a metric to display");

        TableReport report = analysisPage.changeReportType(ReportType.TABLE).getTableReport();
        assertEquals(report.getHeaders(), Arrays.asList(attribute.toUpperCase()));
    }

    @Test(dependsOnGroups = {PROJECT_INIT_GROUP})
    public void dragMetricToColumnChartShortcutPanel() {
        initAnalysePage();

        String metric = doSafetyMetricAction(new SafetyAction() {
            @Override
            public void action(String metric) {
                analysisPage.dragAndDropMetricToShortcutPanel(metric,
                        ShortcutPanel.AS_A_COLUMN_CHART);
            }

            @Override
            public void actionWhenFailed(String data) {
                analysisPage.resetToBlankState();
            }
        }, "dragMetricToColumnChartShortcutPanel");

        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        analysisPage.changeReportType(ReportType.BAR_CHART);
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() == 0);

        doSafetyAttributeAction(metric, new SafetyActionAdapter() {
            @Override
            public void action(String attribute) {
                analysisPage.addCategory(attribute);
            }
        }, "dragMetricToColumnChartShortcutPanel");

        assertTrue(report.getTrackersCount() >= 1);
    }

    @Test(dependsOnGroups = {PROJECT_INIT_GROUP})
    public void dragMetricToTrendShortcutPanel() {
        initAnalysePage();

        doSafetyMetricAction(new SafetyActionAdapter() {
            @Override
            public void action(String metric) {
                analysisPage.dragAndDropMetricToShortcutPanel(metric,
                        ShortcutPanel.TRENDED_OVER_TIME);
            }
        }, "dragMetricToTrendShortcutPanel");

        ChartReport report = analysisPage.getChartReport();
        assertTrue(report.getTrackersCount() >= 1);
        assertTrue(analysisPage.isFilterVisible(DATE));
        assertEquals(analysisPage.getFilterText(DATE), DATE + ": Last 4 quarters");
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
    }

    @Test(dependsOnGroups = {PROJECT_INIT_GROUP})
    public void testSimpleContribution() {
        initAnalysePage();

        String metric = doSafetyMetricAction(new SafetyActionAdapter() {
            @Override
            public void action(String metric) {
                analysisPage.createReport(new ReportDefinition().withMetrics(metric));
            }
        }, "testSimpleContribution");

        doSafetyAttributeAction(metric, new SafetyActionAdapter() {
            @Override
            public void action(String attribute) {
                analysisPage.addCategory(attribute);
            }
        }, "testSimpleContribution");

        ChartReport report = analysisPage.getChartReport();
        int oldTrackersCount = report.getTrackersCount();
        assertTrue(oldTrackersCount >= 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_PERCENTS));
        recommendationContainer.getRecommendation(RecommendationStep.SEE_PERCENTS).apply();
        assertTrue(analysisPage.isReportTypeSelected(ReportType.BAR_CHART));
        assertEquals(report.getTrackersCount(), oldTrackersCount);
        assertTrue(analysisPage.isShowPercentConfigEnabled());
        assertTrue(analysisPage.isShowPercentConfigSelected());

        doSafetyAttributeAction(metric, new SafetyActionAdapter() {
            @Override
            public void action(String attribute) {
                analysisPage.addCategory(attribute);
            }
        }, "testSimpleContribution");

        assertTrue(analysisPage.isReportTypeSelected(ReportType.BAR_CHART));
        assertTrue(report.getTrackersCount() >= 1);
        assertTrue(analysisPage.isShowPercentConfigEnabled());
        assertTrue(analysisPage.isShowPercentConfigSelected());
    }

    @Test(dependsOnGroups = {PROJECT_INIT_GROUP})
    public void testAnotherApproachToShowContribution() {
        initAnalysePage();

        String metric = doSafetyMetricAction(new SafetyActionAdapter() {
            @Override
            public void action(String metric) {
                analysisPage.createReport(new ReportDefinition().withMetrics(metric));
            }
        }, "testAnotherApproachToShowContribution");

        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
        final ComparisonRecommendation comparisonRecommendation =
                recommendationContainer.getRecommendation(RecommendationStep.COMPARE);
        doSafetyAttributeAction(metric, new SafetyAction() {
            @Override
            public void action(String attribute) {
                waitForFragmentVisible(comparisonRecommendation);
                comparisonRecommendation.select(attribute).apply();
            }

            @Override
            public void actionWhenFailed(String attribute) {
                analysisPage.removeCategory(attribute);
            }
        }, "testAnotherApproachToShowContribution");

        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_PERCENTS));
    }

    @Test(dependsOnGroups = {PROJECT_INIT_GROUP})
    public void testSimpleComparison() {
        initAnalysePage();

        String metric = doSafetyMetricAction(new SafetyActionAdapter() {
            @Override
            public void action(String metric) {
                analysisPage.createReport(new ReportDefinition().withMetrics(metric));
            }
        }, "testSimpleComparison");

        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        final ComparisonRecommendation comparisonRecommendation =
                recommendationContainer.getRecommendation(RecommendationStep.COMPARE);
        String attribute = doSafetyAttributeAction(metric, new SafetyAction() {
            @Override
            public void actionWhenFailed(String attribute) {
                analysisPage.removeCategory(attribute);
            }

            @Override
            public void action(String attribute) {
                waitForFragmentVisible(comparisonRecommendation);
                comparisonRecommendation.select(attribute).apply();
            }
        }, "testSimpleComparison");

        assertTrue(analysisPage.getAllAddedCategoryNames().contains(attribute));
        assertEquals(analysisPage.getFilterText(attribute), attribute + ": All");
        assertTrue(report.getTrackersCount() >= 1);
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        attribute = doSafetyAttributeAction(metric, new SafetyActionAdapter() {
            @Override
            public void action(String attribute) {
                analysisPage.addCategory(attribute);
            }
        }, "testSimpleComparison");

        assertTrue(analysisPage.getAllAddedCategoryNames().contains(attribute));
        assertEquals(analysisPage.getFilterText(attribute), attribute + ": All");
        assertTrue(report.getTrackersCount() >= 1);
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
    }

    @Test(dependsOnGroups = {PROJECT_INIT_GROUP})
    public void supportParameter() {
        initAnalysePage();

        doSafetyMetricAction(new SafetyActionAdapter() {
            @Override
            public void action(String metric) {
                analysisPage.createReport(new ReportDefinition().withMetrics(metric));
            }
        }, "supportParameter");

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
        assertTrue(report.getTrackersCount() >= 1);
    }

    @Test(dependsOnGroups = {PROJECT_INIT_GROUP})
    public void displayInColumnChartWithOnlyMetric() {
        initAnalysePage();

        String metric = doSafetyMetricAction(new SafetyActionAdapter() {
            @Override
            public void action(String metric) {
                analysisPage.createReport(new ReportDefinition().withMetrics(metric));
            }
        }, "displayInColumnChartWithOnlyMetric");

        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));

        doSafetyAttributeAction(metric, new SafetyActionAdapter() {
            @Override
            public void action(String attribute) {
                analysisPage.addFilter(attribute);
            }
        }, "displayInColumnChartWithOnlyMetric");

        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));

        analysisPage.changeReportType(ReportType.BAR_CHART);
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() == 0);

        analysisPage.changeReportType(ReportType.COLUMN_CHART);
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));

        doSafetyAttributeAction(metric, new SafetyActionAdapter() {
            @Override
            public void action(String attribute) {
                analysisPage.addCategory(attribute);
            }
        }, "displayInColumnChartWithOnlyMetric");

        assertFalse(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
    }

    @Test(dependsOnGroups = {PROJECT_INIT_GROUP})
    public void displayWhenDraggingFirstMetric() {
        initAnalysePage();

        doSafetyMetricAction(new SafetyActionAdapter() {
            @Override
            public void action(String metric) {
                analysisPage.dragAndDropMetricToShortcutPanel(metric,
                        ShortcutPanel.TRENDED_OVER_TIME);
            }
        }, "displayWhenDraggingFirstMetric");

        assertTrue(analysisPage.getAllAddedCategoryNames().contains(DATE));
        assertTrue(analysisPage.isFilterVisible(DATE));
        assertEquals(analysisPage.getFilterText(DATE), DATE + ": Last 4 quarters");
        assertTrue(analysisPage.getChartReport().getTrackersCount() >= 1);
    }

    @Test(dependsOnGroups = {PROJECT_INIT_GROUP})
    public void exportCustomDiscovery() throws InterruptedException {
        initAnalysePage();

        String metric = doSafetyMetricAction(new SafetyActionAdapter() {
            @Override
            public void action(String metric) {
                analysisPage.createReport(new ReportDefinition().withType(ReportType.TABLE)
                        .withMetrics(metric));
            }
        }, "exportCustomDiscovery");

        doSafetyAttributeAction(metric, new SafetyActionAdapter() {
            @Override
            public void action(String attribute) {
                analysisPage.addCategory(attribute);
            }
        }, "exportCustomDiscovery");

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

        Thread.sleep(3000); // wait for metric values is calculated and loaded
        Iterator<String> metrics = tableReport.getRawMetricElements().iterator();

        List<List<String>> content = new ArrayList<List<String>>();
        while (attributes.hasNext() && metrics.hasNext()) {
            content.add(Arrays.asList(attributes.next(), metrics.next()));
        }

        // Just compare the first 10 lines if table data is big
        if (content.size() >= 10) {
            content = content.subList(0, 10);
        }
        if (analysisContent.size() >= 10) {
            analysisContent = analysisContent.subList(0, 10);
        }
        assertEquals(content, analysisContent, "Content is not correct");

        List<String> headers = tableReport.getAttributesHeader();
        headers.addAll(tableReport.getMetricsHeader());
        Iterator<String> reportheaders = headers.iterator();

        while (analysisHeaders.hasNext() && reportheaders.hasNext()) {
            assertEquals(reportheaders.next().toLowerCase(), analysisHeaders.next().toLowerCase(),
                    "Headers are not correct");
        }

        browser.close();
        browser.switchTo().window(currentWindowHandel);
    }

    @Test(dependsOnGroups = {PROJECT_INIT_GROUP})
    public void exportVisualizationWithOneAttributeInChart() {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withCategories(getRandomAttribute()));
        assertEquals(analysisPage.getExplorerMessage(), "Now select a metric to display");
        assertFalse(analysisPage.isExportToReportButtonEnabled());
    }

    @Test(dependsOnGroups = {PROJECT_INIT_GROUP})
    public void filterOnDateAttribute() {
        initAnalysePage();

        String metric = doSafetyMetricAction(new SafetyActionAdapter() {
            @Override
            public void action(String metric) {
                analysisPage.createReport(new ReportDefinition().withMetrics(metric).withFilters(
                        DATE));
            }
        }, "filterOnDateAttribute");

        doSafetyAttributeAction(metric, new SafetyActionAdapter() {
            @Override
            public void action(String attribute) {
                analysisPage.addCategory(attribute);
            }
        }, "filterOnDateAttribute");

        ChartReport report = analysisPage.getChartReport();
        assertTrue(report.getTrackersCount() >= 1);
        assertEquals(analysisPage.getFilterText(DATE), DATE + ": All time");

        analysisPage.configTimeFilter(DATE, "This year");
        assertEquals(analysisPage.getFilterText(DATE), DATE + ": This year");
    }

    @Test(dependsOnGroups = {PROJECT_INIT_GROUP})
    public void testDateInCategoryAndDateInFilter() {
        initAnalysePage();

        doSafetyMetricAction(new SafetyActionAdapter() {
            @Override
            public void action(String metric) {
                analysisPage.createReport(new ReportDefinition().withMetrics(metric).withCategories(DATE));
            }
        }, "testDateInCategoryAndDateInFilter");

        assertTrue(analysisPage.getChartReport().getTrackersCount() >= 1);
        assertEquals(analysisPage.getFilterText(DATE), DATE + ": All time");
        assertEquals(analysisPage.getAllGranularities(),
                Arrays.asList("Day", "Week (Sun-Sat)", "Month", "Quarter", "Year"));
    }

    @Test(dependsOnGroups = {PROJECT_INIT_GROUP})
    public void trendingRecommendationOverrideDateFilter() {
        initAnalysePage();

        String metric = doSafetyMetricAction(new SafetyActionAdapter() {
            @Override
            public void action(String metric) {
                analysisPage.createReport(new ReportDefinition().withMetrics(metric).withFilters(
                        DATE));
            }
        }, "trendingRecommendationOverrideDateFilter");

        assertEquals(analysisPage.getFilterText(DATE), DATE + ": All time");

        ChartReport report = null;
        for (String period : Sets.newHashSet(analysisPage.getAllTimeFilterOptions())) {
            if ("All time".equals(period)) continue;
            System.out.println(String.format("Try with time period [%s]", period));
            analysisPage.configTimeFilter(DATE, period);
            report = analysisPage.getChartReport();
            try {
                int count = report.getTrackersCount();
                assertEquals(count, 1);
                System.out.println(String.format("Time period [%s] is ok with metric [%s]",
                        period, metric));
                break;
            } catch(Exception e) {
                // ignore
            }
        }

        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        recommendationContainer.getRecommendation(RecommendationStep.SEE_TREND).apply();;
        assertEquals(analysisPage.getFilterText(DATE), DATE + ": Last 4 quarters");
    }

    @Test(dependsOnGroups = {PROJECT_INIT_GROUP})
    public void dragAndDropAttributeToFilterBucket() {
        initAnalysePage();

        String metric = doSafetyMetricAction(new SafetyActionAdapter() {
            @Override
            public void action(String metric) {
                analysisPage.createReport(new ReportDefinition().withMetrics(metric));
            }
        }, "dragAndDropAttributeToFilterBucket");

        String attribute = doSafetyAttributeAction(metric, new SafetyActionAdapter() {
            @Override
            public void action(String attribute) {
                analysisPage.addCategory(attribute);
            }
        }, "dragAndDropAttributeToFilterBucket");

        ChartReport report = analysisPage.getChartReport();
        assertTrue(report.getTrackersCount() >= 1);
        assertEquals(analysisPage.getFilterText(attribute), attribute + ": All");

        attribute = doSafetyAttributeAction(metric, new SafetyActionAdapter() {
            @Override
            public void action(String attribute) {
                analysisPage.addFilter(attribute);
            }
        }, "dragAndDropAttributeToFilterBucket");

        assertEquals(analysisPage.getFilterText(attribute), attribute + ": All");
    }

    @Test(dependsOnGroups = {PROJECT_INIT_GROUP})
    public void addFilterDoesNotHideRecommendation() {
        initAnalysePage();

        String metric = doSafetyMetricAction(new SafetyActionAdapter() {
            @Override
            public void action(String metric) {
                analysisPage.createReport(new ReportDefinition().withMetrics(metric));
            }
        }, "addFilterDoesNotHideRecommendation");

        doSafetyAttributeAction(metric, new SafetyActionAdapter() {
            @Override
            public void action(String attribute) {
                analysisPage.addCategory(attribute);
            }
        }, "addFilterDoesNotHideRecommendation");

        ChartReport report = analysisPage.getChartReport();
        assertTrue(report.getTrackersCount() >= 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_PERCENTS));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        doSafetyAttributeAction(metric, new SafetyActionAdapter() {
            @Override
            public void action(String attribute) {
                analysisPage.addFilter(attribute);
            }
        }, "addFilterDoesNotHideRecommendation");

        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_PERCENTS));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
    }

    @Test(dependsOnGroups = {PROJECT_INIT_GROUP})
    public void testSimplePoP() {
        initAnalysePage();

        String metric = doSafetyMetricAction(new SafetyActionAdapter() {
            @Override
            public void action(String metric) {
                analysisPage.createReport(new ReportDefinition().withMetrics(metric)
                        .withCategories(DATE));
            }
        }, "testSimplePoP");

        assertTrue(analysisPage.isFilterVisible(DATE));
        assertEquals(analysisPage.getFilterText(DATE), DATE + ": All time");
        ChartReport report = analysisPage.getChartReport();
        assertTrue(report.getTrackersCount() >= 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
        recommendationContainer.getRecommendation(RecommendationStep.COMPARE).apply();
        assertTrue(report.getTrackersCount() >= 1);
        List<String> legends = report.getLegends();
        assertEquals(legends.size(), 2);
        assertEquals(legends, Arrays.asList(metric + " - previous year", metric));

        doSafetyMetricAction(new SafetyActionAdapter() {
            @Override
            public void action(String metric) {
                analysisPage.addMetric(metric);
            }
        }, "testSimplePoP");

        assertTrue(report.getTrackersCount() >= 1);
        legends = report.getLegends();
        assertEquals(legends.size(), 1);
        assertEquals(legends, Arrays.asList("Series 1"));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
    }

    @Test(dependsOnGroups = {PROJECT_INIT_GROUP})
    public void testAnotherApproachToShowPoP() {
        initAnalysePage();

        doSafetyMetricAction(new SafetyActionAdapter() {
            @Override
            public void action(String metric) {
                analysisPage.createReport(new ReportDefinition().withMetrics(metric));
            }
        }, "testAnotherApproachToShowPoP");

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

    @Test(dependsOnGroups = {PROJECT_INIT_GROUP})
    public void compararisonRecommendationOverrideDateFilter() {
        initAnalysePage();

        String metric = doSafetyMetricAction(new SafetyActionAdapter() {
            @Override
            public void action(String metric) {
                analysisPage.createReport(new ReportDefinition().withMetrics(metric).withFilters(
                        DATE));
            }
        }, "compararisonRecommendationOverrideDateFilter");

        String attribute = doSafetyAttributeAction(metric, new SafetyActionAdapter() {
            @Override
            public void action(String attribute) {
                analysisPage.addCategory(attribute);
            }
        }, "compararisonRecommendationOverrideDateFilter");

        ChartReport report = null;
        for (String period : Sets.newHashSet(analysisPage.getAllTimeFilterOptions())) {
            if ("All time".equals(period)) continue;
            System.out.println(String.format("Try with time period [%s]", period));
            analysisPage.configTimeFilter(DATE, period);
            report = analysisPage.getChartReport();
            try {
                int count = report.getTrackersCount();
                assertTrue(count >= 1);
                System.out.println(String.format("Time period [%s] is ok with metric [%s] and attribute [%s]",
                        period, metric, attribute));
                break;
            } catch(Exception e) {
                // ignore
            }
        }

        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        ComparisonRecommendation comparisonRecommendation =
                recommendationContainer.getRecommendation(RecommendationStep.COMPARE);
        comparisonRecommendation.select("This month").apply();
        assertEquals(analysisPage.getFilterText(DATE), DATE + ": This month");
        assertTrue(report.getTrackersCount() >= 1);
        List<String> legends = report.getLegends();
        assertEquals(legends.size(), 2);
        assertEquals(legends, Arrays.asList(metric + " - previous year", metric));
    }

  @Test(dependsOnGroups = {PROJECT_INIT_GROUP})
  public void testUndoRedo() {
      int baseTrackerCount1;
      int baseTrackerCount2;
      initAnalysePage();

      String metric = doSafetyMetricAction(new SafetyActionAdapter() {
          @Override
          public void action(String metric) {
              analysisPage.createReport(new ReportDefinition().withMetrics(metric));
          }
      }, "testUndoRedo");
      ChartReport report = analysisPage.getChartReport();
      baseTrackerCount1 = report.getTrackersCount();

      analysisPage.undo();
      analysisPage.waitForReportComputing();
      assertFalse(analysisPage.getAllAddedMetricNames().contains(metric));
      assertTrue(analysisPage.isBucketBlankState(), "Metric is not removed after using 'Undo'");
      assertTrue(analysisPage.isMainEditorBlankState(), "Report is not loaded correctly after using 'Undo'");

      analysisPage.redo();
      analysisPage.waitForReportComputing();
      assertTrue(analysisPage.getAllAddedMetricNames().contains(metric));
      assertEquals(report.getTrackersCount(), baseTrackerCount1);

      String attribute = doSafetyAttributeAction(metric, new SafetyActionAdapter() {
          @Override
          public void action(String attribute) {
              analysisPage.addCategory(attribute);
          }
      }, "testUndoRedo");
      baseTrackerCount2 = report.getTrackersCount();

      analysisPage.undo();
      analysisPage.waitForReportComputing();
      assertFalse(analysisPage.getAllAddedCategoryNames().contains(attribute));
      assertEquals(report.getTrackersCount(),baseTrackerCount1);

      analysisPage.redo();
      analysisPage.waitForReportComputing();
      assertTrue(analysisPage.getAllAddedCategoryNames().contains(attribute));
      assertEquals(report.getTrackersCount(), baseTrackerCount2);
      
      // change report type
      analysisPage.changeReportType(ReportType.TABLE);
      assertTrue(analysisPage.isReportTypeSelected(ReportType.TABLE));

      analysisPage.undo();
      assertTrue(analysisPage.isReportTypeSelected(ReportType.COLUMN_CHART));

      analysisPage.redo();
      assertTrue(analysisPage.isReportTypeSelected(ReportType.TABLE));
  }

    private String getRandomMetric() {
        String metric;
        do {
            metric = metrics.get(random.nextInt(metrics.size()));
        } while (brokenMetrics.contains(metric));

        return metric;
    }

    private String getRandomAttribute() {
        return attributes.get(random.nextInt(attributes.size()));
    }

    private String getRandomeAttributeFromMetric(String metric) {
        String attribute = getRandomAttribute();
        while (containsInCache(metric, attribute)) {
            attribute = getRandomAttribute();
        }
        cache.add(Pair.of(metric, attribute));
        return attribute;
    }

    private boolean containsInCache(String metric, String attribute) {
        for (Pair<String, String> pair : cache) {
            if (metric.equals(pair.getLeft()) && attribute.equals(pair.getRight())) {
                return true;
            }
        }
        return false;
    }

    private String doSafetyMetricAction(SafetyAction metricAction, String screenshot) {
        String metric;
        while (true) {
            metric = getRandomMetric();
            metricAction.action(metric);
            analysisPage.waitForReportComputing();
            if (!analysisPage.isExplorerMessageVisible())
                break;

            Screenshots.takeScreenshot(browser, screenshot + System.currentTimeMillis(),
                    this.getClass());
            System.out.println(String.format("Report with metric [%s] shows message: %s", metric,
                    analysisPage.getExplorerMessage()));
            brokenMetrics.add(metric);
            metricAction.actionWhenFailed(metric);
            continue;
        }
        System.out.println(String.format("Good metric [%s] to test", metric));
        return metric;
    }

    private String doSafetyAttributeAction(String metric, SafetyAction attributeAction,
            String screenshot) {
        String attribute;
        while (true) {
            attribute = getRandomeAttributeFromMetric(metric);
            attributeAction.action(attribute);
            analysisPage.waitForReportComputing();
            if (!analysisPage.isExplorerMessageVisible())
                break;

            Screenshots.takeScreenshot(browser, screenshot + System.currentTimeMillis(),
                    this.getClass());
            System.out.println(String.format(
                    "Report with metric [%s] and attribute [%s] shows message: %s", metric,
                    attribute, analysisPage.getExplorerMessage()));
            attributeAction.actionWhenFailed(attribute);
            System.out.println("Try another pair to test");
            continue;
        }
        System.out.println(String.format("Good pair to test: metric [%s] and attribute [%s]", metric, attribute));
        return attribute;
    }

    private static interface SafetyAction {
        void action(String data);

        void actionWhenFailed(String data);
    }

    private static abstract class SafetyActionAdapter implements SafetyAction {
        @Override
        public void action(String data) {}

        @Override
        public void actionWhenFailed(String data) {}
    }

}
