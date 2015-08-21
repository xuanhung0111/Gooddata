package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
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
import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.GoodData;
import com.gooddata.md.Attribute;
import com.gooddata.md.Entry;
import com.gooddata.md.MetadataService;
import com.gooddata.md.Metric;
import com.gooddata.project.Project;
import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.indigo.ShortcutPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.ComparisonRecommendation;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.TrendingRecommendation;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class AnalyticalDesignerGeneralTest extends AnalyticalDesignerAbstractTest {

    private static final String DATE = "Date";
    private static final List<String> PROJECT_TEMPLATES = Lists.newArrayList("/projectTemplates/GoodSalesDemo/2",
            "/projectTemplates/MarketingFunnelDemo/1", "/projectTemplates/SocialECommerceDemo/1");

    private List<String> attributes;
    private List<String> metrics;
    private Random random = new Random();
    private List<String> brokenMetrics = Lists.newArrayList();
    private List<Pair<String, String>> cache = Lists.newArrayList();
    private List<String> remainedAttributes;

    private Project project;
    private MetadataService mdService;

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        shuffle(PROJECT_TEMPLATES);
        projectTemplate = PROJECT_TEMPLATES.get(0);
        projectTitle = "Indigo-General-Test";
        System.out.println("Use project template: " + projectTemplate);
    }

    @Test(dependsOnGroups = {"init"}, groups = {"prepare"})
    public void initializeGoodDataSDK() throws JSONException {
        GoodData goodDataClient = getGoodDataClient();
        project = goodDataClient.getProjectService().getProjectById(testParams.getProjectId());
        mdService = goodDataClient.getMetadataService();
    }

    @Test(dependsOnMethods = {"initializeGoodDataSDK"}, groups = {"prepare"})
    public void loadAttributes() {
        Supplier<Stream<String>> allAttributes = () -> mdService.find(project, Attribute.class)
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

    @Test(dependsOnMethods = {"initializeGoodDataSDK"}, groups = {"prepare"})
    public void loadMetrics() {
        metrics = mdService.find(project, Metric.class)
                .stream()
                .map(Entry::getTitle)
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

        doSafetyAttributeAction(metric, analysisPage::addCategory, "testCustomDiscovery");

        assertTrue(report.getTrackersCount() >= 1);
        analysisPage.resetToBlankState();
    }

    @Test(dependsOnGroups = {"prepare"}, groups = {"sanity"})
    public void testWithAttribute() {
        String attribute = getRandomAttribute();
        initAnalysePage();

        assertEquals(analysisPage.addCategory(attribute).getExplorerMessage(), "Now select a measure to display");

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

        String metric = doSafetyMetricAction(
                data -> analysisPage.dragAndDropMetricToShortcutPanel(data, ShortcutPanel.AS_A_COLUMN_CHART),
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

        doSafetyAttributeAction(metric, analysisPage::addCategory, "dragMetricToColumnChartShortcutPanel");

        assertTrue(report.getTrackersCount() >= 1);
    }

    @Test(dependsOnGroups = {"prepare"})
    public void dragMetricToTrendShortcutPanel() {
        initAnalysePage();

        doSafetyMetricAction(
                data -> analysisPage.dragAndDropMetricToShortcutPanel(data, ShortcutPanel.TRENDED_OVER_TIME),
                "dragMetricToTrendShortcutPanel");

        ChartReport report = analysisPage.getChartReport();
        assertTrue(report.getTrackersCount() >= 1);
        assertTrue(analysisPage.isDateFilterVisible());
        assertTrue(analysisPage.getDateFilterText().endsWith(": Last 4 quarters"));
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
    }

    @Test(dependsOnGroups = {"prepare"}, groups = {"sanity"})
    public void testSimpleContribution() {
        initAnalysePage();

        String metric = doSafetyMetricAction(analysisPage::addMetric, "testSimpleContribution");
        doSafetyAttributeAction(metric, analysisPage::addCategory, "testSimpleContribution");

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
        analysisPage.expandMetricConfiguration("% " + metric);
        assertTrue(analysisPage.isShowPercentConfigEnabled());
        assertTrue(analysisPage.isShowPercentConfigSelected());

        doSafetyAttributeAction(metric, analysisPage::addCategory, "testSimpleContribution");

        assertTrue(analysisPage.isReportTypeSelected(ReportType.BAR_CHART));
        assertTrue(report.getTrackersCount() >= 1);
        assertTrue(analysisPage.isShowPercentConfigEnabled());
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

        String attribute;
        while (true) {
            attribute = getRandomeAttributeFromMetric(metric);
            analysisPage.addCategory(attribute).waitForReportComputing();
            if (analysisPage.isExplorerMessageVisible()) {
                System.out.println(format("Report with metric [%s] and attribute [%s] shows message: %s",
                        metric, attribute, analysisPage.getExplorerMessage()));
                System.out.println("Try another attribute");
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
        String attribute = doSafetyAttributeAction(metric, analysisPage::addCategory, "testSimpleComparison");

        analysisPage.resetToBlankState().addMetric(metric);
        waitForFragmentVisible(comparisonRecommendation);
        comparisonRecommendation.select(attribute).apply();
        assertTrue(analysisPage.getAllAddedCategoryNames().contains(attribute));
        assertEquals(analysisPage.getFilterText(attribute), attribute + ": All");
        assertTrue(report.getTrackersCount() >= 1);
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        attribute = doSafetyAttributeAction(metric, analysisPage::addCategory, "testSimpleComparison");

        assertTrue(analysisPage.getAllAddedCategoryNames().contains(attribute));
        assertEquals(analysisPage.getFilterText(attribute), attribute + ": All");
        assertTrue(report.getTrackersCount() >= 1);
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
    }

    @Test(dependsOnGroups = {"prepare"})
    public void supportParameter() {
        List<String> badMetrics = Lists.newArrayList();
        String metric;

        initAnalysePage();

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
            assertTrue(analysisPage.getAllAddedCategoryNames().contains(DATE));
            assertTrue(analysisPage.isDateFilterVisible());
            assertTrue(analysisPage.getDateFilterText().endsWith(": Last 4 quarters"));
            analysisPage.expandMetricConfiguration(metric);
            assertTrue(analysisPage.isShowPercentConfigEnabled());
            assertTrue(analysisPage.isCompareSamePeriodConfigEnabled());

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

        doSafetyAttributeAction(metric, analysisPage::addCategory, "displayInColumnChartWithOnlyMetric");

        assertFalse(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
    }

    @Test(dependsOnGroups = {"prepare"}, groups = {"sanity"})
    public void displayWhenDraggingFirstMetric() {
        initAnalysePage();

        doSafetyMetricAction(
                data -> analysisPage.dragAndDropMetricToShortcutPanel(data, ShortcutPanel.TRENDED_OVER_TIME),
                "displayWhenDraggingFirstMetric");

        assertTrue(analysisPage.getAllAddedCategoryNames().contains(DATE));
        assertTrue(analysisPage.isDateFilterVisible());
        assertTrue(analysisPage.getDateFilterText().endsWith(": Last 4 quarters"));
        assertTrue(analysisPage.getChartReport().getTrackersCount() >= 1);
    }

    @Test(dependsOnGroups = {"prepare"}, groups = {"sanity"})
    public void exportCustomDiscovery() {
        initAnalysePage();

        String metric = doSafetyMetricAction(
                data -> analysisPage.addMetric(data).changeReportType(ReportType.TABLE),
                "exportCustomDiscovery");
        doSafetyAttributeAction(metric, analysisPage::addCategory, "exportCustomDiscovery");

        assertTrue(analysisPage.isExportToReportButtonEnabled());
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

        assertEquals(analysisPage.addCategory(getRandomAttribute()).getExplorerMessage(),
                "Now select a measure to display");
        assertFalse(analysisPage.isExportToReportButtonEnabled());
    }

    @Test(dependsOnGroups = {"prepare"}, groups = {"sanity"})
    public void filterOnDateAttribute() {
        initAnalysePage();

        String metric = doSafetyMetricAction(
                data -> analysisPage.addMetric(data).addFilter(DATE),
                "filterOnDateAttribute");
        doSafetyAttributeAction(metric, analysisPage::addCategory, "filterOnDateAttribute");

        ChartReport report = analysisPage.getChartReport();
        assertTrue(report.getTrackersCount() >= 1);
        assertTrue(analysisPage.getDateFilterText().endsWith(": All time"));

        analysisPage.configTimeFilter("This year");
        assertTrue(analysisPage.getDateFilterText().endsWith(": This year"));
    }

    @Test(dependsOnGroups = {"prepare"})
    public void testDateInCategoryAndDateInFilter() {
        initAnalysePage();

        doSafetyMetricAction(
                data -> analysisPage.addMetric(data).addCategory(DATE),
                "testDateInCategoryAndDateInFilter");
        assertTrue(analysisPage.getChartReport().getTrackersCount() >= 1);
        assertTrue(analysisPage.getDateFilterText().endsWith(": All time"));
        assertTrue(isEqualCollection(analysisPage.getAllGranularities(),
                asList("Day", "Week (Sun-Sat)", "Month", "Quarter", "Year")));
    }

    @Test(dependsOnGroups = {"prepare"})
    public void trendingRecommendationOverrideDateFilter() {
        initAnalysePage();

        String metric = doSafetyMetricAction(
                data -> analysisPage.addMetric(data).addFilter(DATE),
                "trendingRecommendationOverrideDateFilter");
        assertTrue(analysisPage.getDateFilterText().endsWith(": All time"));

        boolean timeFilterOk = false;
        for (String period : Sets.newHashSet(analysisPage.getAllTimeFilterOptions())) {
            if ("All time".equals(period)) continue;
            System.out.println(format("Try with time period [%s]", period));
            analysisPage.configTimeFilter(period).waitForReportComputing();
            if (analysisPage.isExplorerMessageVisible()) {
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
        assertTrue(analysisPage.getDateFilterText().endsWith(": Last 4 quarters"));
    }

    @Test(dependsOnGroups = {"prepare"})
    public void dragAndDropAttributeToFilterBucket() {
        initAnalysePage();

        String metric = doSafetyMetricAction(analysisPage::addMetric, "dragAndDropAttributeToFilterBucket");
        String attribute = doSafetyAttributeAction(metric, analysisPage::addCategory,
                "dragAndDropAttributeToFilterBucket");

        ChartReport report = analysisPage.getChartReport();
        assertTrue(report.getTrackersCount() >= 1);
        assertEquals(analysisPage.getFilterText(attribute), attribute + ": All");

        attribute = doSafetyAttributeAction(metric, analysisPage::addFilter, "dragAndDropAttributeToFilterBucket");

        assertEquals(analysisPage.getFilterText(attribute), attribute + ": All");
    }

    @Test(dependsOnGroups = {"prepare"})
    public void addFilterDoesNotHideRecommendation() {
        initAnalysePage();

        String metric = doSafetyMetricAction(analysisPage::addMetric, "addFilterDoesNotHideRecommendation");
        doSafetyAttributeAction(metric, analysisPage::addCategory, "addFilterDoesNotHideRecommendation");

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

        final String metric1 = doSafetyMetricAction(
                data -> analysisPage.addMetric(data).addCategory(DATE),
                "testSimplePoP");

        assertTrue(analysisPage.isDateFilterVisible());
        assertTrue(analysisPage.getDateFilterText().endsWith(": All time"));
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

        doSafetyMetricAction(data -> analysisPage.replaceMetric(metric1, data), "testSimplePoP");
    }

    @Test(dependsOnGroups = {"prepare"})
    public void testAnotherApproachToShowPoP() {
        initAnalysePage();

        doSafetyMetricAction(analysisPage::addMetric, "testAnotherApproachToShowPoP");
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        recommendationContainer.getRecommendation(RecommendationStep.SEE_TREND).apply();

        assertTrue(analysisPage.getAllAddedCategoryNames().contains(DATE));
        assertTrue(analysisPage.isDateFilterVisible());
        assertTrue(analysisPage.getDateFilterText().endsWith(": Last 4 quarters"));

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

        String metric = doSafetyMetricAction(
                data -> analysisPage.addMetric(data).addFilter(DATE),
                "compararisonRecommendationOverrideDateFilter");
        String attribute = doSafetyAttributeAction(metric, analysisPage::addCategory,
                "compararisonRecommendationOverrideDateFilter");

        boolean timeFilterOk = false;
        for (String period : Sets.newHashSet(analysisPage.getAllTimeFilterOptions())) {
            if ("All time".equals(period)) continue;
            System.out.println(format("Try with time period [%s]", period));
            analysisPage.configTimeFilter(period).waitForReportComputing();
            if (analysisPage.isExplorerMessageVisible()) {
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
        assertTrue(analysisPage.getDateFilterText().endsWith(": This month"));
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
            throw new NoSuchElementException("Could not find any attributes to test this case!");
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

    private String doSafetyMetricAction(Consumer<String> action, Consumer<String> failedAction, String screenshot) {
        String metric;
        while (true) {
            metric = getRandomMetric();
            action.accept(metric);
            analysisPage.waitForReportComputing();
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
        String attribute;
        while (true) {
            attribute = getRandomeAttributeFromMetric(metric);
            action.accept(attribute);
            analysisPage.waitForReportComputing();
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
}
