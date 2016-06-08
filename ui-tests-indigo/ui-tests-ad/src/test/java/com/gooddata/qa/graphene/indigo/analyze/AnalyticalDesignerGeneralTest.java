package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.Entry;
import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.indigo.ShortcutPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributesBucketReact;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.CataloguePanelReact;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucketReact;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.ComparisonRecommendation;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.TrendingRecommendation;
import com.gooddata.qa.graphene.indigo.analyze.common.GoodSalesAbstractAnalyseTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReportReact;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class AnalyticalDesignerGeneralTest extends GoodSalesAbstractAnalyseTest {

    private List<String> attributes;
    private List<String> metrics;
    private Random random = new Random();
    private List<String> brokenMetrics = Lists.newArrayList();
    private List<Pair<String, String>> cache = Lists.newArrayList();
    private List<String> remainedAttributes;

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Indigo-General-Test";
    }

    @Test(dependsOnGroups = {"init"}, groups = {"prepare"})
    public void loadAttributes() {
        Supplier<Stream<String>> allAttributes = () -> getMdService().find(getProject(), Attribute.class)
                .stream()
                .map(Entry::getTitle);

        Collection<String> dates = allAttributes.get()
                .filter(attribute -> attribute.startsWith(DATE))
                .map(attribute -> attribute.replaceFirst(DATE, "").trim())
                .collect(toList());

        attributes = allAttributes.get()
                .filter(attribute -> {
                    if (attribute.startsWith("Records of")) return false;
                    if (dates.stream().anyMatch(attribute::endsWith)) return false;
                    return true;
                })
                .collect(toList());

        remainedAttributes = Lists.newArrayList(attributes);
    }

    @Test(dependsOnGroups = {"init"}, groups = {"prepare"})
    public void loadMetrics() {
        initAnalysePage();

        metrics = getMdService().find(getProject(), Metric.class)
            .stream()
            .map(Entry::getTitle)
            .filter(metric -> {
                final boolean goodMetric = isGoodMetric(metric);
                analysisPageReact.resetToBlankState();
                if (!goodMetric) {
                    log.info(format("Metric [%s] is not good to test."
                        + " Maybe it has empty value, no data or it's not connected to any attributes.", metric));
                }
                return goodMetric;
            })
            .collect(toList());
    }

    @Test(dependsOnGroups = {"prepare"})
    public void testCustomDiscovery() {
        initAnalysePage();

        String metric = doSafetyMetricAction(analysisPageReact::addMetric, "testCustomDiscovery");
        ChartReportReact report = analysisPageReact.getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        analysisPageReact.changeReportType(ReportType.BAR_CHART);
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() == 0);

        doSafetyAttributeAction(metric, analysisPageReact::addAttribute, "testCustomDiscovery");

        assertTrue(report.getTrackersCount() >= 1);
        analysisPageReact.resetToBlankState();
    }

    @Test(dependsOnGroups = {"prepare"})
    public void testWithAttribute() {
        String attribute = getRandomAttribute();
        initAnalysePage();

        assertEquals(analysisPageReact.addAttribute(attribute).getExplorerMessage(), "Now select a measure to display");

        assertEquals(analysisPageReact.changeReportType(ReportType.BAR_CHART).getExplorerMessage(),
                "Now select a measure to display");

        assertEquals(analysisPageReact.changeReportType(ReportType.LINE_CHART).getExplorerMessage(),
                "Now select a measure to display");

        analysisPageReact.changeReportType(ReportType.TABLE);
        if (analysisPageReact.isExplorerMessageVisible()) {
            System.out.println("Cannot render table because of message:");
            System.out.println(analysisPageReact.getExplorerMessage());
        }
    }

    @Test(dependsOnGroups = {"prepare"})
    public void dragMetricToColumnChartShortcutPanel() {
        initAnalysePage();

        final Supplier<WebElement> recommendation = () ->
            waitForElementPresent(ShortcutPanel.AS_A_COLUMN_CHART.getLocator(), browser);

        String metric = doSafetyMetricAction(
            data -> {
                WebElement source = analysisPageReact.getCataloguePanel().searchAndGet(data, FieldType.METRIC);
                analysisPageReact.drag(source, recommendation);
            },
            "dragMetricToColumnChartShortcutPanel");

        ChartReportReact report = analysisPageReact.getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        analysisPageReact.changeReportType(ReportType.BAR_CHART);
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() == 0);

        doSafetyAttributeAction(metric, analysisPageReact::addAttribute, "dragMetricToColumnChartShortcutPanel");

        assertTrue(report.getTrackersCount() >= 1);
    }

    @Test(dependsOnGroups = {"prepare"})
    public void dragMetricToTrendShortcutPanel() {
        initAnalysePage();

        final Supplier<WebElement> recommendation = () ->
            waitForElementPresent(ShortcutPanel.TRENDED_OVER_TIME.getLocator(), browser);

        doSafetyMetricAction(
            data -> {
                WebElement source = analysisPageReact.getCataloguePanel().searchAndGet(data, FieldType.METRIC);
                analysisPageReact.drag(source, recommendation);
            },
            "dragMetricToTrendShortcutPanel");

        ChartReportReact report = analysisPageReact.getChartReport();
        assertTrue(report.getTrackersCount() >= 1);

        FiltersBucketReact FiltersBucketReact = analysisPageReact.getFilterBuckets();
        assertTrue(FiltersBucketReact.isDateFilterVisible());
        assertTrue(FiltersBucketReact.getDateFilterText().endsWith(": Last 4 quarters"));

        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
    }

    @Test(dependsOnGroups = {"prepare"})
    public void testSimpleContribution() {
        initAnalysePage();

        String metric = doSafetyMetricAction(analysisPageReact::addMetric, "testSimpleContribution");
        final String attribute = doSafetyAttributeAction(metric, analysisPageReact::addAttribute, "testSimpleContribution");

        final ChartReportReact report = analysisPageReact.getChartReport();
        int oldTrackersCount = report.getTrackersCount();
        assertTrue(oldTrackersCount >= 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_PERCENTS));
        recommendationContainer.getRecommendation(RecommendationStep.SEE_PERCENTS).apply();
        assertTrue(analysisPageReact.isReportTypeSelected(ReportType.BAR_CHART));
        assertEquals(report.getTrackersCount(), oldTrackersCount);

        MetricConfiguration metricConfiguration = analysisPageReact.getMetricsBucket()
                .getMetricConfiguration("% " + metric)
                .expandConfiguration();
        assertTrue(metricConfiguration.isShowPercentEnabled());
        assertTrue(metricConfiguration.isShowPercentSelected());

        doSafetyAttributeAction(metric, attr -> this.replaceAttribute(attribute, attr),
                "testSimpleContribution");

        assertTrue(analysisPageReact.isReportTypeSelected(ReportType.BAR_CHART));
        assertTrue(report.getTrackersCount() >= 1);
        assertTrue(metricConfiguration.isShowPercentEnabled());
    }

    @Test(dependsOnGroups = {"prepare"})
    public void testAnotherApproachToShowContribution() {
        initAnalysePage();

        String metric = doSafetyMetricAction(analysisPageReact::addMetric, "testAnotherApproachToShowContribution");
        ChartReportReact report = analysisPageReact.getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
        final ComparisonRecommendation comparisonRecommendation =
                recommendationContainer.getRecommendation(RecommendationStep.COMPARE);

        final CataloguePanelReact cataloguePanel = analysisPageReact.getCataloguePanel();
        String attribute;
        while (true) {
            attribute = getRandomeAttributeFromMetric(metric);
            if (!cataloguePanel.search(attribute) || !cataloguePanel.isDataApplicable(attribute)) {
                Screenshots.takeScreenshot(browser,
                        "[Inapplicable attribute] testAnotherApproachToShowContribution", this.getClass());
                System.out.println(format("Attribute [%s] is not available!", attribute));
                cache.add(Pair.of(metric, attribute));
                continue;
            }

            analysisPageReact.addAttribute(attribute).waitForReportComputing();
            if (analysisPageReact.isExplorerMessageVisible()) {
                System.out.println(format("Report with metric [%s] and attribute [%s] shows message: %s",
                        metric, attribute, analysisPageReact.getExplorerMessage()));
                System.out.println("Try another attribute");
                analysisPageReact.removeAttribute(attribute).waitForReportComputing();
            } else {
                analysisPageReact.removeAttribute(attribute).waitForReportComputing();
                System.out.println(format("Good pair to test: metric [%s] and attribute [%s]", metric, attribute));
                break;
            }
        }

        waitForFragmentVisible(comparisonRecommendation);
        comparisonRecommendation.select(attribute).apply();

        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_PERCENTS));
    }

    @Test(dependsOnGroups = {"prepare"})
    public void testSimpleComparison() {
        initAnalysePage();

        String metric = doSafetyMetricAction(analysisPageReact::addMetric, "testSimpleComparison");
        ChartReportReact report = analysisPageReact.getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        final ComparisonRecommendation comparisonRecommendation =
                recommendationContainer.getRecommendation(RecommendationStep.COMPARE);
        final String attribute = doSafetyAttributeAction(metric, analysisPageReact::addAttribute, "testSimpleComparison");

        analysisPageReact.resetToBlankState().addMetric(metric);
        waitForFragmentVisible(comparisonRecommendation);

        comparisonRecommendation.select(attribute).apply();

        assertTrue(analysisPageReact.getAttributesBucket().getItemNames().contains(attribute));
        assertEquals(analysisPageReact.getFilterBuckets().getFilterText(attribute), attribute + ": All");

        if (analysisPageReact.waitForReportComputing().isExplorerMessageVisible()) {
            log.info(format(
                    "Report with metric [%s] and attribute [%s] shows message: %s", metric,
                    attribute, analysisPageReact.getExplorerMessage()));
        } else {
            assertTrue(report.getTrackersCount() >= 1);
            assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
        }

        String newAttribute = doSafetyAttributeAction(metric,
                attr -> this.replaceAttribute(attribute, attr), "testSimpleComparison");

        assertTrue(analysisPageReact.getAttributesBucket().getItemNames().contains(newAttribute));
        assertEquals(analysisPageReact.getFilterBuckets().getFilterText(newAttribute), newAttribute + ": All");

        if (analysisPageReact.waitForReportComputing().isExplorerMessageVisible()) {
            log.info(format(
                    "Report with metric [%s] and attribute [%s] shows message: %s", metric,
                    newAttribute, analysisPageReact.getExplorerMessage()));
        } else {
            assertTrue(report.getTrackersCount() >= 1);
            assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
        }
    }

    @Test(dependsOnGroups = {"prepare"})
    public void supportParameter() {
        List<String> badMetrics = Lists.newArrayList();
        String metric;

        initAnalysePage();
        final AttributesBucketReact categoriesBucket = analysisPageReact.getAttributesBucket();
        final FiltersBucketReact filtersBucketReact = analysisPageReact.getFilterBuckets();

        while (true) {
            if (badMetrics.size() >= 3) {
                System.out.println("Tried with 3 metrics. Skip this test.");
                break;
            }

            while (true) {
                metric = doSafetyMetricAction(analysisPageReact::addMetric, "supportParameter");
                if (!badMetrics.contains(metric)) {
                    break;
                }
                analysisPageReact.resetToBlankState();
            }

            ChartReportReact report = analysisPageReact.getChartReport();
            assertEquals(report.getTrackersCount(), 1);
            RecommendationContainer recommendationContainer =
                    Graphene.createPageFragment(RecommendationContainer.class,
                            waitForElementVisible(RecommendationContainer.LOCATOR, browser));
            assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
            assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

            TrendingRecommendation trendingRecommendation =
                    recommendationContainer.getRecommendation(RecommendationStep.SEE_TREND);
            trendingRecommendation.select("Month").apply();
            analysisPageReact.waitForReportComputing();
            assertTrue(categoriesBucket.getItemNames().contains(DATE));
            assertTrue(filtersBucketReact.isDateFilterVisible());
            assertTrue(filtersBucketReact.getDateFilterText().endsWith(": Last 4 quarters"));

            MetricConfiguration metricConfiguration =  analysisPageReact.getMetricsBucket()
                    .getMetricConfiguration(metric).expandConfiguration();
            assertTrue(metricConfiguration.isShowPercentEnabled());
            assertTrue(metricConfiguration.isPopEnabled());

            if (!browser.findElements(RecommendationContainer.LOCATOR).isEmpty()) {
                assertFalse(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
                assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
                assertTrue(report.getTrackersCount() >= 1);
                return;
            }
            System.out.println(format("Report with metric [%s] shows message when comparing "
                    + "last 4 quarters: %s", metric, analysisPageReact.getExplorerMessage()));
            System.out.println("trying with another metric");
            badMetrics.add(metric);
            analysisPageReact.resetToBlankState();
        }
    }

    @Test(dependsOnGroups = {"prepare"})
    public void displayInColumnChartWithOnlyMetric() {
        initAnalysePage();

        String metric = doSafetyMetricAction(analysisPageReact::addMetric, "displayInColumnChartWithOnlyMetric");
        ChartReportReact report = analysisPageReact.getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));

        doSafetyAttributeAction(metric, analysisPageReact::addFilter, "displayInColumnChartWithOnlyMetric");

        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));

        analysisPageReact.changeReportType(ReportType.BAR_CHART);
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() == 0);

        analysisPageReact.changeReportType(ReportType.COLUMN_CHART);
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));

        doSafetyAttributeAction(metric, analysisPageReact::addAttribute, "displayInColumnChartWithOnlyMetric");

        assertFalse(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
    }

    @Test(dependsOnGroups = {"prepare"})
    public void displayWhenDraggingFirstMetric() {
        initAnalysePage();
        final AttributesBucketReact categoriesBucket = analysisPageReact.getAttributesBucket();
        final FiltersBucketReact filtersBucketReact = analysisPageReact.getFilterBuckets();

        final Supplier<WebElement> recommendation = () ->
            waitForElementPresent(ShortcutPanel.TRENDED_OVER_TIME.getLocator(), browser);

        doSafetyMetricAction(
            data -> {
                WebElement source = analysisPageReact.getCataloguePanel().searchAndGet(data, FieldType.METRIC);
                analysisPageReact.drag(source, recommendation);
            },
            "displayWhenDraggingFirstMetric");

        assertTrue(categoriesBucket.getItemNames().contains(DATE));
        assertTrue(filtersBucketReact.isDateFilterVisible());
        assertTrue(filtersBucketReact.getDateFilterText().endsWith(": Last 4 quarters"));
        assertTrue(analysisPageReact.getChartReport().getTrackersCount() >= 1);
    }

    @Test(dependsOnGroups = {"prepare"})
    public void exportVisualizationWithOneAttributeInChart() {
        initAnalysePage();

        assertEquals(analysisPageReact.addAttribute(getRandomAttribute()).getExplorerMessage(),
                "Now select a measure to display");
        assertFalse(analysisPageReact.getPageHeader().isExportButtonEnabled());
    }

    @Test(dependsOnGroups = {"prepare"})
    public void filterOnDateAttribute() {
        initAnalysePage();
        final FiltersBucketReact filtersBucketReact = analysisPageReact.getFilterBuckets();

        String metric = doSafetyMetricAction(
                data -> analysisPageReact.addMetric(data).addDateFilter(),
                "filterOnDateAttribute");
        doSafetyAttributeAction(metric, analysisPageReact::addAttribute, "filterOnDateAttribute");

        ChartReportReact report = analysisPageReact.getChartReport();
        assertTrue(report.getTrackersCount() >= 1);
        assertTrue(filtersBucketReact.getDateFilterText().endsWith(": All time"));

        filtersBucketReact.configDateFilter("This year");
        assertTrue(filtersBucketReact.getDateFilterText().endsWith(": This year"));
    }

    @Test(dependsOnGroups = {"prepare"})
    public void testDateInCategoryAndDateInFilter() {
        initAnalysePage();

        doSafetyMetricAction(
                data -> analysisPageReact.addMetric(data).addDate(),
                "testDateInCategoryAndDateInFilter");
        assertTrue(analysisPageReact.getChartReport().getTrackersCount() >= 1);
        assertTrue(analysisPageReact.getFilterBuckets().getDateFilterText().endsWith(": All time"));
        assertTrue(isEqualCollection(analysisPageReact.getAttributesBucket().getAllGranularities(),
                asList("Day", "Week (Sun-Sat)", "Month", "Quarter", "Year")));
    }

    @Test(dependsOnGroups = {"prepare"})
    public void trendingRecommendationOverrideDateFilter() {
        initAnalysePage();
        final FiltersBucketReact filtersBucketReact = analysisPageReact.getFilterBuckets();

        String metric = doSafetyMetricAction(
                data -> analysisPageReact.addMetric(data).addDateFilter(),
                "trendingRecommendationOverrideDateFilter");
        assertTrue(filtersBucketReact.getDateFilterText().endsWith(": All time"));

        boolean timeFilterOk = false;
        for (String period : Sets.newHashSet(filtersBucketReact.getDateFilterOptions())) {
            if ("All time".equals(period)) continue;
            System.out.println(format("Try with time period [%s]", period));
            filtersBucketReact.configDateFilter(period);
            if (analysisPageReact.waitForReportComputing().isExplorerMessageVisible()) {
                System.out.println(format("Report shows message: %s", analysisPageReact.getExplorerMessage()));
            } else {
                System.out.println(format("Time period [%s] is ok with metric [%s]", period, metric));
                timeFilterOk = true;
                break;
            }
        }

        if (!timeFilterOk) {
            System.out.println(format("Report with metric [%s] has no data with all time filter!", metric));
            return;
        }

        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        recommendationContainer.getRecommendation(RecommendationStep.SEE_TREND).apply();;
        assertTrue(filtersBucketReact.getDateFilterText().endsWith(": Last 4 quarters"));
    }

    @Test(dependsOnGroups = {"prepare"})
    public void dragAndDropAttributeToFilterBucket() {
        initAnalysePage();
        final FiltersBucketReact filtersBucketReact = analysisPageReact.getFilterBuckets();

        String metric = doSafetyMetricAction(analysisPageReact::addMetric, "dragAndDropAttributeToFilterBucket");
        String attribute = doSafetyAttributeAction(metric, analysisPageReact::addAttribute,
                "dragAndDropAttributeToFilterBucket");

        ChartReportReact report = analysisPageReact.getChartReport();
        assertTrue(report.getTrackersCount() >= 1);
        assertEquals(filtersBucketReact.getFilterText(attribute), attribute + ": All");

        attribute = doSafetyAttributeAction(metric, analysisPageReact::addFilter, "dragAndDropAttributeToFilterBucket");

        assertEquals(filtersBucketReact.getFilterText(attribute), attribute + ": All");
    }

    @Test(dependsOnGroups = {"prepare"})
    public void addFilterDoesNotHideRecommendation() {
        initAnalysePage();

        String metric = doSafetyMetricAction(analysisPageReact::addMetric, "addFilterDoesNotHideRecommendation");
        doSafetyAttributeAction(metric, analysisPageReact::addAttribute, "addFilterDoesNotHideRecommendation");

        ChartReportReact report = analysisPageReact.getChartReport();
        assertTrue(report.getTrackersCount() >= 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_PERCENTS));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        doSafetyAttributeAction(metric, analysisPageReact::addFilter, "addFilterDoesNotHideRecommendation");

        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_PERCENTS));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
    }

    @Test(dependsOnGroups = {"prepare"})
    public void testSimplePoP() {
        initAnalysePage();
        final FiltersBucketReact filtersBucketReact = analysisPageReact.getFilterBuckets();

        final String metric1 = doSafetyMetricAction(
                data -> analysisPageReact.addMetric(data).addDate(),
                "testSimplePoP");

        assertTrue(filtersBucketReact.isDateFilterVisible());
        assertTrue(filtersBucketReact.getDateFilterText().endsWith(": All time"));
        ChartReportReact report = analysisPageReact.getChartReport();
        assertTrue(report.getTrackersCount() >= 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
        recommendationContainer.getRecommendation(RecommendationStep.COMPARE).apply();
        assertTrue(report.getTrackersCount() >= 1);
        List<String> legends = report.getLegends();
        assertEquals(legends.size(), 2);
        assertTrue(isEqualCollection(legends, asList(metric1 + " - previous year", metric1)));

        doSafetyMetricAction(data -> this.replaceMetric(metric1, data), "testSimplePoP");
    }

    @Test(dependsOnGroups = {"prepare"})
    public void testAnotherApproachToShowPoP() {
        initAnalysePage();
        final FiltersBucketReact filtersBucketReact = analysisPageReact.getFilterBuckets();

        doSafetyMetricAction(analysisPageReact::addMetric, "testAnotherApproachToShowPoP");
        ChartReportReact report = analysisPageReact.getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        recommendationContainer.getRecommendation(RecommendationStep.SEE_TREND).apply();

        assertTrue(analysisPageReact.getAttributesBucket().getItemNames().contains(DATE));
        assertTrue(filtersBucketReact.isDateFilterVisible());
        assertTrue(filtersBucketReact.getDateFilterText().endsWith(": Last 4 quarters"));

        if (analysisPageReact.isExplorerMessageVisible()) {
            System.out.println(format("After applying 'see trend', report shows message: %s",
                    analysisPageReact.getExplorerMessage()));
            return;
        }
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
    }

    @Test(dependsOnGroups = {"prepare"})
    public void compararisonRecommendationOverrideDateFilter() {
        initAnalysePage();
        final FiltersBucketReact filtersBucketReact = analysisPageReact.getFilterBuckets();

        String metric = doSafetyMetricAction(
                data -> analysisPageReact.addMetric(data).addDateFilter(),
                "compararisonRecommendationOverrideDateFilter");
        String attribute = doSafetyAttributeAction(metric, analysisPageReact::addAttribute,
                "compararisonRecommendationOverrideDateFilter");

        boolean timeFilterOk = false;
        for (String period : Sets.newHashSet(filtersBucketReact.getDateFilterOptions())) {
            if ("All time".equals(period)) continue;
            System.out.println(format("Try with time period [%s]", period));
            filtersBucketReact.configDateFilter(period);
            if (analysisPageReact.waitForReportComputing().isExplorerMessageVisible()) {
                System.out.println(format("Report shows message: %s", analysisPageReact.getExplorerMessage()));
            } else {
                System.out.println(format("Time period [%s] is ok with metric [%s] and attribute [%s]",
                        period, metric, attribute));
                timeFilterOk = true;
                break;
            }
        }

        if (!timeFilterOk) {
            System.out.println(format("Report with metric [%s] and attribute [%s] has no data"
                    + " with all time filter!", metric, attribute));
            return;
        }

        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        ComparisonRecommendation comparisonRecommendation =
                recommendationContainer.getRecommendation(RecommendationStep.COMPARE);
        comparisonRecommendation.select("This month").apply();
        assertTrue(filtersBucketReact.getDateFilterText().endsWith(": This month"));
        if (analysisPageReact.waitForReportComputing().isExplorerMessageVisible()) {
            System.out.println(format("After comparing 'This month', report shows message: %s",
                    analysisPageReact.getExplorerMessage()));
            return;
        }

        ChartReportReact report = analysisPageReact.getChartReport();
        assertTrue(report.getTrackersCount() >= 1);
        List<String> legends = report.getLegends();
        assertEquals(legends.size(), 2);
        assertTrue(isEqualCollection(legends, asList(metric + " - previous year", metric)));
    }

    private String getRandomMetric() {
        String metric;
        do {
            metric = metrics.get(random.nextInt(metrics.size()));
        } while (brokenMetrics.contains(metric));

        return metric;
    }

    private String getRandomAttribute() {
        if (remainedAttributes.isEmpty()) {
            System.out.println("Could not find any different attributes to test this case! Reuse old attributes");
            remainedAttributes = newArrayList(attributes);
            cache.clear();
        }
        String attribute =  remainedAttributes.get(random.nextInt(remainedAttributes.size()));
        remainedAttributes.remove(attribute);
        return attribute;
    }

    private String getRandomeAttributeFromMetric(String metric) {
        String attribute = getRandomAttribute();
        while (containsInCache(metric, attribute)) {
            attribute = getRandomAttribute();
        }
        cache.add(Pair.of(metric, attribute));
        remainedAttributes = Lists.newArrayList(attributes);
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

    private boolean isGoodMetric(String metric) {
        analysisPageReact.addMetric(metric)
            .waitForReportComputing();

        if (analysisPageReact.isExplorerMessageVisible())
            return false;

        if (analysisPageReact.getChartReport().getTrackersCount() == 0)
            return false;

        if (!isElementPresent(RecommendationContainer.LOCATOR, browser))
            return false;

        if (!Graphene.createPageFragment(RecommendationContainer.class,
                waitForElementVisible(RecommendationContainer.LOCATOR, browser))
                .isRecommendationVisible(RecommendationStep.COMPARE))
            return false;

        return true;
    }

    private String doSafetyMetricAction(Consumer<String> action, Consumer<String> failedAction, String screenshot) {
        String metric;
        while (true) {
            metric = getRandomMetric();

            if (analysisPageReact.getCataloguePanel().search(metric)) {
                action.accept(metric);
                analysisPageReact.waitForReportComputing();
            } else {
                Screenshots.takeScreenshot(browser, "[Inapplicable metric]" + screenshot +
                        System.currentTimeMillis(), this.getClass());
                System.out.println(format("Metric [%s] is not available!", metric));
                brokenMetrics.add(metric);
                continue;
            }

            if (!analysisPageReact.isExplorerMessageVisible())
                break;

            Screenshots.takeScreenshot(browser, screenshot + System.currentTimeMillis(),
                    this.getClass());
            System.out.println(format("Report with metric [%s] shows message: %s", metric,
                    analysisPageReact.getExplorerMessage()));
            brokenMetrics.add(metric);
            failedAction.accept(metric);
            continue;
        }
        System.out.println(format("Good metric [%s] to test", metric));
        return metric;
    }

    private String doSafetyMetricAction(Consumer<String> action, String screenshot) {
        return doSafetyMetricAction(action, metric -> analysisPageReact.resetToBlankState(), screenshot);
    }

    private String doSafetyAttributeAction(String metric, Consumer<String> action, Consumer<String> failedAction,
            String screenshot) {
        CataloguePanelReact cataloguePanel = analysisPageReact.getCataloguePanel();
        String attribute;
        while (true) {
            attribute = getRandomeAttributeFromMetric(metric);

            if (cataloguePanel.search(attribute) && cataloguePanel.isDataApplicable(attribute)) {
                action.accept(attribute);
                analysisPageReact.waitForReportComputing();
            } else {
                Screenshots.takeScreenshot(browser, "[Inapplicable attribute]" + screenshot +
                        System.currentTimeMillis(), this.getClass());
                System.out.println(format("Attribute [%s] is not available!", attribute));
                cache.add(Pair.of(metric, attribute));
                continue;
            }

            if (!analysisPageReact.isExplorerMessageVisible())
                break;

            Screenshots.takeScreenshot(browser, screenshot + System.currentTimeMillis(),
                    this.getClass());
            System.out.println(format(
                    "Report with metric [%s] and attribute [%s] shows message: %s", metric,
                    attribute, analysisPageReact.getExplorerMessage()));
            failedAction.accept(attribute);
            System.out.println("Try another pair to test");
            continue;
        }
        System.out.println(format("Good pair to test: metric [%s] and attribute [%s]", metric, attribute));
        return attribute;
    }

    private String doSafetyAttributeAction(String metric, Consumer<String> action, String screenshot) {
        return doSafetyAttributeAction(metric, action, analysisPageReact::removeAttribute, screenshot);
    }

    private void replaceAttribute(String oldAttr, String newAttr) {
        if (analysisPageReact.getAttributesBucket().getItemNames().contains(oldAttr)) {
            analysisPageReact.replaceAttribute(oldAttr, newAttr);
        } else {
            analysisPageReact.addAttribute(newAttr);
        }
    }

    private void replaceMetric(String oldMetric, String newMetric) {
        if (analysisPageReact.getMetricsBucket().getItemNames().contains(oldMetric)) {
            analysisPageReact.replaceMetric(oldMetric, newMetric);
        } else {
            analysisPageReact.addMetric(newMetric);
        }
    }
}
