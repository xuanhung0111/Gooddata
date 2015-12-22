package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.shuffle;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
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
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.CataloguePanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.CategoriesBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.ComparisonRecommendation;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.TrendingRecommendation;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class AnalyticalDesignerGeneralTest extends AnalyticalDesignerAbstractTest {

    private static final List<String> PROJECT_TEMPLATES = Lists.newArrayList("/projectTemplates/GoodSalesDemo/2",
            "/projectTemplates/MarketingFunnelDemo/1", "/projectTemplates/SocialECommerceDemo/1");

    private List<String> attributes;
    private List<String> metrics;
    private Random random = new Random();
    private List<String> brokenMetrics = Lists.newArrayList();
    private List<Pair<String, String>> cache = Lists.newArrayList();
    private List<String> remainedAttributes;

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        shuffle(PROJECT_TEMPLATES);
        projectTemplate = PROJECT_TEMPLATES.get(0);
        projectTitle = "Indigo-General-Test";
        System.out.println("Use project template: " + projectTemplate);
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
                analysisPage.resetToBlankState();
                if (!goodMetric) {
                    log.info(format("Metric [%s] is not good to test. Maybe it has empty value or no data.", metric));
                }
                return goodMetric;
            })
            .collect(toList());
    }

    @Test(dependsOnGroups = {"prepare"})
    public void testCustomDiscovery() {
        initAnalysePage();

        String metric = doSafetyMetricAction(analysisPage::addMetric, "testCustomDiscovery");
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        analysisPage.changeReportType(ReportType.BAR_CHART);
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() == 0);

        doSafetyAttributeAction(metric, analysisPage::addAttribute, "testCustomDiscovery");

        assertTrue(report.getTrackersCount() >= 1);
        analysisPage.resetToBlankState();
    }

    @Test(dependsOnGroups = {"prepare"}, groups = {"sanity"})
    public void testWithAttribute() {
        String attribute = getRandomAttribute();
        initAnalysePage();

        assertEquals(analysisPage.addAttribute(attribute).getExplorerMessage(), "Now select a measure to display");

        assertEquals(analysisPage.changeReportType(ReportType.BAR_CHART).getExplorerMessage(),
                "Now select a measure to display");

        assertEquals(analysisPage.changeReportType(ReportType.LINE_CHART).getExplorerMessage(),
                "Now select a measure to display");

        analysisPage.changeReportType(ReportType.TABLE);
        if (analysisPage.isExplorerMessageVisible()) {
            System.out.println("Cannot render table because of message:");
            System.out.println(analysisPage.getExplorerMessage());
        }
    }

    @Test(dependsOnGroups = {"prepare"}, groups = {"sanity"})
    public void dragMetricToColumnChartShortcutPanel() {
        initAnalysePage();

        final Supplier<WebElement> recommendation = () ->
            waitForElementPresent(ShortcutPanel.AS_A_COLUMN_CHART.getLocator(), browser);

        String metric = doSafetyMetricAction(
            data -> {
                WebElement source = analysisPage.getCataloguePanel().searchAndGet(data, FieldType.METRIC);
                analysisPage.drag(source, recommendation);
            },
            "dragMetricToColumnChartShortcutPanel");

        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        analysisPage.changeReportType(ReportType.BAR_CHART);
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() == 0);

        doSafetyAttributeAction(metric, analysisPage::addAttribute, "dragMetricToColumnChartShortcutPanel");

        assertTrue(report.getTrackersCount() >= 1);
    }

    @Test(dependsOnGroups = {"prepare"})
    public void dragMetricToTrendShortcutPanel() {
        initAnalysePage();

        final Supplier<WebElement> recommendation = () ->
            waitForElementPresent(ShortcutPanel.TRENDED_OVER_TIME.getLocator(), browser);

        doSafetyMetricAction(
            data -> {
                WebElement source = analysisPage.getCataloguePanel().searchAndGet(data, FieldType.METRIC);
                analysisPage.drag(source, recommendation);
            },
            "dragMetricToTrendShortcutPanel");

        ChartReport report = analysisPage.getChartReport();
        assertTrue(report.getTrackersCount() >= 1);

        FiltersBucket filtersBucket = analysisPage.getFilterBuckets();
        assertTrue(filtersBucket.isDateFilterVisible());
        assertTrue(filtersBucket.getDateFilterText().endsWith(": Last 4 quarters"));

        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
    }

    @Test(dependsOnGroups = {"prepare"}, groups = {"sanity"})
    public void testSimpleContribution() {
        initAnalysePage();

        String metric = doSafetyMetricAction(analysisPage::addMetric, "testSimpleContribution");
        final String attribute = doSafetyAttributeAction(metric, analysisPage::addAttribute, "testSimpleContribution");

        final ChartReport report = analysisPage.getChartReport();
        int oldTrackersCount = report.getTrackersCount();
        assertTrue(oldTrackersCount >= 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_PERCENTS));
        recommendationContainer.getRecommendation(RecommendationStep.SEE_PERCENTS).apply();
        assertTrue(analysisPage.isReportTypeSelected(ReportType.BAR_CHART));
        assertEquals(report.getTrackersCount(), oldTrackersCount);

        MetricConfiguration metricConfiguration = analysisPage.getMetricsBucket()
                .getMetricConfiguration("% " + metric)
                .expandConfiguration();
        assertTrue(metricConfiguration.isShowPercentEnabled());
        assertTrue(metricConfiguration.isShowPercentSelected());

        doSafetyAttributeAction(metric, attr -> this.replaceAttribute(attribute, attr),
                "testSimpleContribution");

        assertTrue(analysisPage.isReportTypeSelected(ReportType.BAR_CHART));
        assertTrue(report.getTrackersCount() >= 1);
        assertTrue(metricConfiguration.isShowPercentEnabled());
    }

    @Test(dependsOnGroups = {"prepare"})
    public void testAnotherApproachToShowContribution() {
        initAnalysePage();

        String metric = doSafetyMetricAction(analysisPage::addMetric, "testAnotherApproachToShowContribution");
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
        final ComparisonRecommendation comparisonRecommendation =
                recommendationContainer.getRecommendation(RecommendationStep.COMPARE);

        final CataloguePanel cataloguePanel = analysisPage.getCataloguePanel();
        String attribute;
        while (true) {
            attribute = getRandomeAttributeFromMetric(metric);
            if (!cataloguePanel.searchBucketItem(attribute) || !cataloguePanel.isDataApplicable(attribute)) {
                Screenshots.takeScreenshot(browser,
                        "[Inapplicable attribute] testAnotherApproachToShowContribution", this.getClass());
                System.out.println(format("Attribute [%s] is not available!", attribute));
                cache.add(Pair.of(metric, attribute));
                continue;
            }

            analysisPage.addAttribute(attribute).waitForReportComputing();
            if (analysisPage.isExplorerMessageVisible()) {
                System.out.println(format("Report with metric [%s] and attribute [%s] shows message: %s",
                        metric, attribute, analysisPage.getExplorerMessage()));
                System.out.println("Try another attribute");
                analysisPage.removeCategory(attribute).waitForReportComputing();
            } else {
                analysisPage.removeCategory(attribute).waitForReportComputing();
                System.out.println(format("Good pair to test: metric [%s] and attribute [%s]", metric, attribute));
                break;
            }
        }

        waitForFragmentVisible(comparisonRecommendation);
        comparisonRecommendation.select(attribute).apply();

        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_PERCENTS));
    }

    @Test(dependsOnGroups = {"prepare"}, groups = {"sanity"})
    public void testSimpleComparison() {
        initAnalysePage();

        String metric = doSafetyMetricAction(analysisPage::addMetric, "testSimpleComparison");
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        final ComparisonRecommendation comparisonRecommendation =
                recommendationContainer.getRecommendation(RecommendationStep.COMPARE);
        final String attribute = doSafetyAttributeAction(metric, analysisPage::addAttribute, "testSimpleComparison");

        analysisPage.resetToBlankState().addMetric(metric);
        waitForFragmentVisible(comparisonRecommendation);
        comparisonRecommendation.select(attribute).apply();
        assertTrue(analysisPage.getCategoriesBucket().getItemNames().contains(attribute));
        assertEquals(analysisPage.getFilterBuckets().getFilterText(attribute), attribute + ": All");
        assertTrue(report.getTrackersCount() >= 1);
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        String newAttribute = doSafetyAttributeAction(metric,
                attr -> this.replaceAttribute(attribute, attr), "testSimpleComparison");

        assertTrue(analysisPage.getCategoriesBucket().getItemNames().contains(newAttribute));
        assertEquals(analysisPage.getFilterBuckets().getFilterText(newAttribute), newAttribute + ": All");
        assertTrue(report.getTrackersCount() >= 1);
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
    }

    @Test(dependsOnGroups = {"prepare"})
    public void supportParameter() {
        List<String> badMetrics = Lists.newArrayList();
        String metric;

        initAnalysePage();
        final CategoriesBucket categoriesBucket = analysisPage.getCategoriesBucket();
        final FiltersBucket filtersBucket = analysisPage.getFilterBuckets();

        while (true) {
            if (badMetrics.size() >= 3) {
                System.out.println("Tried with 3 metrics. Skip this test.");
                break;
            }

            while (true) {
                metric = doSafetyMetricAction(analysisPage::addMetric, "supportParameter");
                if (!badMetrics.contains(metric)) {
                    break;
                }
                analysisPage.resetToBlankState();
            }

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
            analysisPage.waitForReportComputing();
            assertTrue(categoriesBucket.getItemNames().contains(DATE));
            assertTrue(filtersBucket.isDateFilterVisible());
            assertTrue(filtersBucket.getDateFilterText().endsWith(": Last 4 quarters"));

            MetricConfiguration metricConfiguration =  analysisPage.getMetricsBucket()
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
                    + "last 4 quarters: %s", metric, analysisPage.getExplorerMessage()));
            System.out.println("trying with another metric");
            badMetrics.add(metric);
            analysisPage.resetToBlankState();
        }
    }

    @Test(dependsOnGroups = {"prepare"})
    public void displayInColumnChartWithOnlyMetric() {
        initAnalysePage();

        String metric = doSafetyMetricAction(analysisPage::addMetric, "displayInColumnChartWithOnlyMetric");
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));

        doSafetyAttributeAction(metric, analysisPage::addFilter, "displayInColumnChartWithOnlyMetric");

        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));

        analysisPage.changeReportType(ReportType.BAR_CHART);
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() == 0);

        analysisPage.changeReportType(ReportType.COLUMN_CHART);
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));

        doSafetyAttributeAction(metric, analysisPage::addAttribute, "displayInColumnChartWithOnlyMetric");

        assertFalse(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
    }

    @Test(dependsOnGroups = {"prepare"}, groups = {"sanity"})
    public void displayWhenDraggingFirstMetric() {
        initAnalysePage();
        final CategoriesBucket categoriesBucket = analysisPage.getCategoriesBucket();
        final FiltersBucket filtersBucket = analysisPage.getFilterBuckets();

        final Supplier<WebElement> recommendation = () ->
            waitForElementPresent(ShortcutPanel.TRENDED_OVER_TIME.getLocator(), browser);

        doSafetyMetricAction(
            data -> {
                WebElement source = analysisPage.getCataloguePanel().searchAndGet(data, FieldType.METRIC);
                analysisPage.drag(source, recommendation);
            },
            "displayWhenDraggingFirstMetric");

        assertTrue(categoriesBucket.getItemNames().contains(DATE));
        assertTrue(filtersBucket.isDateFilterVisible());
        assertTrue(filtersBucket.getDateFilterText().endsWith(": Last 4 quarters"));
        assertTrue(analysisPage.getChartReport().getTrackersCount() >= 1);
    }

    @Test(dependsOnGroups = {"prepare"}, groups = {"sanity"})
    public void exportCustomDiscovery() {
        initAnalysePage();

        String metric = doSafetyMetricAction(
                data -> analysisPage.addMetric(data).changeReportType(ReportType.TABLE),
                "exportCustomDiscovery");
        doSafetyAttributeAction(metric, analysisPage::addAttribute, "exportCustomDiscovery");

        assertTrue(analysisPage.getPageHeader().isExportToReportButtonEnabled());
        TableReport analysisReport = analysisPage.getTableReport();
        List<List<String>> analysisContent = analysisReport.getContent();

        analysisPage.exportReport();
        String currentWindowHandel = browser.getWindowHandle();
        for (String handel : browser.getWindowHandles()) {
            if (!handel.equals(currentWindowHandel))
                browser.switchTo().window(handel);
        }

        com.gooddata.qa.graphene.fragments.reports.report.TableReport tableReport =
                Graphene.createPageFragment(
                        com.gooddata.qa.graphene.fragments.reports.report.TableReport.class,
                        waitForElementVisible(By.id("gridContainerTab"), browser));

        Iterator<String> attributes = tableReport.getAttributeElements().iterator();

        sleepTightInSeconds(2); // wait for metric values is calculated and loaded
        Iterator<String> metrics = tableReport.getRawMetricElements().iterator();

        List<List<String>> content = new ArrayList<List<String>>();
        while (attributes.hasNext() && metrics.hasNext()) {
            content.add(asList(attributes.next(), metrics.next()));
        }

        // Just compare the first 10 lines if table data is big
        if (content.size() >= 10) {
            content = content.subList(0, 10);
        }
        if (analysisContent.size() >= 10) {
            analysisContent = analysisContent.subList(0, 10);
        }
        assertEquals(content, analysisContent, "Content is not correct");

        browser.close();
        browser.switchTo().window(currentWindowHandel);
    }

    @Test(dependsOnGroups = {"prepare"})
    public void exportVisualizationWithOneAttributeInChart() {
        initAnalysePage();

        assertEquals(analysisPage.addAttribute(getRandomAttribute()).getExplorerMessage(),
                "Now select a measure to display");
        assertFalse(analysisPage.getPageHeader().isExportToReportButtonEnabled());
    }

    @Test(dependsOnGroups = {"prepare"}, groups = {"sanity"})
    public void filterOnDateAttribute() {
        initAnalysePage();
        final FiltersBucket filtersBucket = analysisPage.getFilterBuckets();

        String metric = doSafetyMetricAction(
                data -> analysisPage.addMetric(data).addDateFilter(),
                "filterOnDateAttribute");
        doSafetyAttributeAction(metric, analysisPage::addAttribute, "filterOnDateAttribute");

        ChartReport report = analysisPage.getChartReport();
        assertTrue(report.getTrackersCount() >= 1);
        assertTrue(filtersBucket.getDateFilterText().endsWith(": All time"));

        filtersBucket.configTimeFilter("This year");
        assertTrue(filtersBucket.getDateFilterText().endsWith(": This year"));
    }

    @Test(dependsOnGroups = {"prepare"})
    public void testDateInCategoryAndDateInFilter() {
        initAnalysePage();

        doSafetyMetricAction(
                data -> analysisPage.addMetric(data).addDate(),
                "testDateInCategoryAndDateInFilter");
        assertTrue(analysisPage.getChartReport().getTrackersCount() >= 1);
        assertTrue(analysisPage.getFilterBuckets().getDateFilterText().endsWith(": All time"));
        assertTrue(isEqualCollection(analysisPage.getCategoriesBucket().getAllGranularities(),
                asList("Day", "Week (Sun-Sat)", "Month", "Quarter", "Year")));
    }

    @Test(dependsOnGroups = {"prepare"})
    public void trendingRecommendationOverrideDateFilter() {
        initAnalysePage();
        final FiltersBucket filtersBucket = analysisPage.getFilterBuckets();

        String metric = doSafetyMetricAction(
                data -> analysisPage.addMetric(data).addDateFilter(),
                "trendingRecommendationOverrideDateFilter");
        assertTrue(filtersBucket.getDateFilterText().endsWith(": All time"));

        boolean timeFilterOk = false;
        for (String period : Sets.newHashSet(filtersBucket.getAllTimeFilterOptions())) {
            if ("All time".equals(period)) continue;
            System.out.println(format("Try with time period [%s]", period));
            filtersBucket.configTimeFilter(period);
            if (analysisPage.waitForReportComputing().isExplorerMessageVisible()) {
                System.out.println(format("Report shows message: %s", analysisPage.getExplorerMessage()));
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
        assertTrue(filtersBucket.getDateFilterText().endsWith(": Last 4 quarters"));
    }

    @Test(dependsOnGroups = {"prepare"})
    public void dragAndDropAttributeToFilterBucket() {
        initAnalysePage();
        final FiltersBucket filtersBucket = analysisPage.getFilterBuckets();

        String metric = doSafetyMetricAction(analysisPage::addMetric, "dragAndDropAttributeToFilterBucket");
        String attribute = doSafetyAttributeAction(metric, analysisPage::addAttribute,
                "dragAndDropAttributeToFilterBucket");

        ChartReport report = analysisPage.getChartReport();
        assertTrue(report.getTrackersCount() >= 1);
        assertEquals(filtersBucket.getFilterText(attribute), attribute + ": All");

        attribute = doSafetyAttributeAction(metric, analysisPage::addFilter, "dragAndDropAttributeToFilterBucket");

        assertEquals(filtersBucket.getFilterText(attribute), attribute + ": All");
    }

    @Test(dependsOnGroups = {"prepare"})
    public void addFilterDoesNotHideRecommendation() {
        initAnalysePage();

        String metric = doSafetyMetricAction(analysisPage::addMetric, "addFilterDoesNotHideRecommendation");
        doSafetyAttributeAction(metric, analysisPage::addAttribute, "addFilterDoesNotHideRecommendation");

        ChartReport report = analysisPage.getChartReport();
        assertTrue(report.getTrackersCount() >= 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_PERCENTS));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        doSafetyAttributeAction(metric, analysisPage::addFilter, "addFilterDoesNotHideRecommendation");

        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_PERCENTS));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
    }

    @Test(dependsOnGroups = {"prepare"}, groups = {"sanity"})
    public void testSimplePoP() {
        initAnalysePage();
        final FiltersBucket filtersBucket = analysisPage.getFilterBuckets();

        final String metric1 = doSafetyMetricAction(
                data -> analysisPage.addMetric(data).addDate(),
                "testSimplePoP");

        assertTrue(filtersBucket.isDateFilterVisible());
        assertTrue(filtersBucket.getDateFilterText().endsWith(": All time"));
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
        assertTrue(isEqualCollection(legends, asList(metric1 + " - previous year", metric1)));

        doSafetyMetricAction(data -> this.replaceMetric(metric1, data), "testSimplePoP");
    }

    @Test(dependsOnGroups = {"prepare"})
    public void testAnotherApproachToShowPoP() {
        initAnalysePage();
        final FiltersBucket filtersBucket = analysisPage.getFilterBuckets();

        doSafetyMetricAction(analysisPage::addMetric, "testAnotherApproachToShowPoP");
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        recommendationContainer.getRecommendation(RecommendationStep.SEE_TREND).apply();

        assertTrue(analysisPage.getCategoriesBucket().getItemNames().contains(DATE));
        assertTrue(filtersBucket.isDateFilterVisible());
        assertTrue(filtersBucket.getDateFilterText().endsWith(": Last 4 quarters"));

        if (analysisPage.isExplorerMessageVisible()) {
            System.out.println(format("After applying 'see trend', report shows message: %s",
                    analysisPage.getExplorerMessage()));
            return;
        }
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
    }

    @Test(dependsOnGroups = {"prepare"})
    public void compararisonRecommendationOverrideDateFilter() {
        initAnalysePage();
        final FiltersBucket filtersBucket = analysisPage.getFilterBuckets();

        String metric = doSafetyMetricAction(
                data -> analysisPage.addMetric(data).addDateFilter(),
                "compararisonRecommendationOverrideDateFilter");
        String attribute = doSafetyAttributeAction(metric, analysisPage::addAttribute,
                "compararisonRecommendationOverrideDateFilter");

        boolean timeFilterOk = false;
        for (String period : Sets.newHashSet(filtersBucket.getAllTimeFilterOptions())) {
            if ("All time".equals(period)) continue;
            System.out.println(format("Try with time period [%s]", period));
            filtersBucket.configTimeFilter(period);
            if (analysisPage.waitForReportComputing().isExplorerMessageVisible()) {
                System.out.println(format("Report shows message: %s", analysisPage.getExplorerMessage()));
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
        assertTrue(filtersBucket.getDateFilterText().endsWith(": This month"));
        if (analysisPage.waitForReportComputing().isExplorerMessageVisible()) {
            System.out.println(format("After comparing 'This month', report shows message: %s",
                    analysisPage.getExplorerMessage()));
            return;
        }

        ChartReport report = analysisPage.getChartReport();
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
        analysisPage.addMetric(metric)
            .waitForReportComputing();

        if (analysisPage.isExplorerMessageVisible())
            return false;

        return analysisPage.getChartReport()
                .getTrackersCount() > 0;
    }

    private String doSafetyMetricAction(Consumer<String> action, Consumer<String> failedAction, String screenshot) {
        String metric;
        while (true) {
            metric = getRandomMetric();

            if (analysisPage.getCataloguePanel().searchBucketItem(metric)) {
                action.accept(metric);
                analysisPage.waitForReportComputing();
            } else {
                Screenshots.takeScreenshot(browser, "[Inapplicable metric]" + screenshot +
                        System.currentTimeMillis(), this.getClass());
                System.out.println(format("Metric [%s] is not available!", metric));
                brokenMetrics.add(metric);
                continue;
            }

            if (!analysisPage.isExplorerMessageVisible())
                break;

            Screenshots.takeScreenshot(browser, screenshot + System.currentTimeMillis(),
                    this.getClass());
            System.out.println(format("Report with metric [%s] shows message: %s", metric,
                    analysisPage.getExplorerMessage()));
            brokenMetrics.add(metric);
            failedAction.accept(metric);
            continue;
        }
        System.out.println(format("Good metric [%s] to test", metric));
        return metric;
    }

    private String doSafetyMetricAction(Consumer<String> action, String screenshot) {
        return doSafetyMetricAction(action, metric -> analysisPage.resetToBlankState(), screenshot);
    }

    private String doSafetyAttributeAction(String metric, Consumer<String> action, Consumer<String> failedAction,
            String screenshot) {
        CataloguePanel cataloguePanel = analysisPage.getCataloguePanel();
        String attribute;
        while (true) {
            attribute = getRandomeAttributeFromMetric(metric);

            if (cataloguePanel.searchBucketItem(attribute) && cataloguePanel.isDataApplicable(attribute)) {
                action.accept(attribute);
                analysisPage.waitForReportComputing();
            } else {
                Screenshots.takeScreenshot(browser, "[Inapplicable attribute]" + screenshot +
                        System.currentTimeMillis(), this.getClass());
                System.out.println(format("Attribute [%s] is not available!", attribute));
                cache.add(Pair.of(metric, attribute));
                continue;
            }

            if (!analysisPage.isExplorerMessageVisible())
                break;

            Screenshots.takeScreenshot(browser, screenshot + System.currentTimeMillis(),
                    this.getClass());
            System.out.println(format(
                    "Report with metric [%s] and attribute [%s] shows message: %s", metric,
                    attribute, analysisPage.getExplorerMessage()));
            failedAction.accept(attribute);
            System.out.println("Try another pair to test");
            continue;
        }
        System.out.println(format("Good pair to test: metric [%s] and attribute [%s]", metric, attribute));
        return attribute;
    }

    private String doSafetyAttributeAction(String metric, Consumer<String> action, String screenshot) {
        return doSafetyAttributeAction(metric, action, analysisPage::removeCategory, screenshot);
    }

    private void replaceAttribute(String oldAttr, String newAttr) {
        if (analysisPage.getCategoriesBucket().getItemNames().contains(oldAttr)) {
            analysisPage.replaceAttribute(oldAttr, newAttr);
        } else {
            analysisPage.addAttribute(newAttr);
        }
    }

    private void replaceMetric(String oldMetric, String newMetric) {
        if (analysisPage.getMetricsBucket().getItemNames().contains(oldMetric)) {
            analysisPage.replaceMetric(oldMetric, newMetric);
        } else {
            analysisPage.addMetric(newMetric);
        }
    }
}
