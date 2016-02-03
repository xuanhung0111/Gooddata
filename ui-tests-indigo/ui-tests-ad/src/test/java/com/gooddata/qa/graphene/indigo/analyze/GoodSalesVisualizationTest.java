package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTight;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils.changeMetricFormat;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.GoodData;
import com.gooddata.md.Attribute;
import com.gooddata.md.MetadataService;
import com.gooddata.md.Metric;
import com.gooddata.md.Restriction;
import com.gooddata.project.Project;
import com.gooddata.qa.graphene.enums.indigo.CatalogFilterType;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AnalysisPageHeader;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributesBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.CataloguePanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.StacksBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport;

public class GoodSalesVisualizationTest extends AnalyticalDesignerAbstractTest {

    private static final String EXPORT_ERROR_MESSAGE = "Visualization is not compatible with Report Editor. "
            + "\"Stage Name\" is in configuration twice. Remove one attribute to Open as Report.";

    private static final String PERCENT_OF_GOAL_URI = "/gdc/md/%s/obj/8136";

    private Project project;
    private MetadataService mdService;

    @SuppressWarnings("serial")
    private static final Map<String, String> walkmeContents = new HashMap<String, String>() {{
        put("Welcome to the Analytical Designer", "This interactive environment allows you to explore your data "
                + "and create visualizations quickly and easily. Intelligent on-screen recommendations help you "
                + "discover new and surprising insights. Let's get started!");

        put("Begin by exploring your data", "Measures represent quantitative data (values).\n\n"
                + "Attributes represent qualitative data (categories).\n\n"
                + "Date is a special item which represents all the dates in your project.");

        put("Create a new visualization", "Drag data from the list onto the canvas and watch as your "
                + "visualization takes shape!");

        put("Remove data", "Drag data items from these zones back to the list to remove them from your "
                + "visualization.");

        put("Change visualization type", "Choose how to visualize your data.");

        put("Filter your visualization", "Drag the Date field or any attribute here.");

        put("Save your visualization as a report", "When you are ready, open your visualization in the "
                + "Report Editor. From there you can save it and add it to a dashboard.");

        put("Clear your canvas", "Restart your exploration at any time.");

        put("You're ready.", "Go ahead. Start discovering what insights await in your data!");
    }};

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Indigo-GoodSales-Demo-Visualization-Test";
    }

    @Test(dependsOnGroups = {"setupProject"}, groups = {"turnOfWalkme"}, priority = 0)
    public void testWalkme() {
        initAnalysePage();

        final By title = By.className("walkme-custom-balloon-title");
        final By content = By.className("walkme-custom-balloon-content");
        final By nextBtn = By.className("walkme-action-next");
        final By backBtn = By.className("walkme-action-back");
        final By doneBtn = By.className("walkme-action-done");

        waitForElementVisible(title, browser);
        while (true) {
            assertEquals(waitForElementVisible(content, browser).getText(),
                    walkmeContents.get(waitForElementVisible(title, browser).getText()));

            waitForElementVisible(nextBtn, browser).click();

            if (!browser.findElements(doneBtn).isEmpty()) {
                break;
            }

            waitForElementVisible(backBtn, browser).click();
            assertEquals(waitForElementVisible(content, browser).getText(),
                    walkmeContents.get(waitForElementVisible(title, browser).getText()));
            waitForElementVisible(nextBtn, browser).click();
        }
        waitForElementVisible(doneBtn, browser).click();
        isWalkmeTurnOff = true;
    }

    @Test(dependsOnGroups = {"init"})
    public void testResetFunction() {
        initAnalysePage();

        ChartReport report = analysisPage.addMetric(NUMBER_OF_ACTIVITIES).getChartReport();
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
    public void disableExportForUnexportableVisualization() {
        initAnalysePage();
        final AnalysisPageHeader pageHeader = analysisPage.getPageHeader();
        ChartReport report = analysisPage.addMetric(AMOUNT).getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        assertTrue(pageHeader.isExportButtonEnabled());

        analysisPage.addAttribute(STAGE_NAME).waitForReportComputing();
        assertEquals(report.getTrackersCount(), 8);

        analysisPage.addStack(STAGE_NAME);
        assertEquals(report.getTrackersCount(), 8);

        assertFalse(pageHeader.isExportButtonEnabled());
        assertEquals(pageHeader.getExportButtonTooltipText(), EXPORT_ERROR_MESSAGE);
    }

    @Test(dependsOnGroups = {"init"})
    public void dontShowLegendWhenOnlyOneMetric() {
        initAnalysePage();
        ChartReport report = analysisPage.addMetric(AMOUNT).addAttribute(STAGE_NAME).waitForReportComputing()
                .getChartReport();
        assertEquals(report.getTrackersCount(), 8);
        assertFalse(report.isLegendVisible());

        analysisPage.changeReportType(ReportType.BAR_CHART);
        assertFalse(report.isLegendVisible());

        analysisPage.changeReportType(ReportType.LINE_CHART);
        assertFalse(report.isLegendVisible());
    }

    @Test(dependsOnGroups = {"init"})
    public void testLegendsInChartHasManyMetrics() {
        initAnalysePage();
        ChartReport report = analysisPage.addMetric(AMOUNT).addMetric(NUMBER_OF_ACTIVITIES)
                .waitForReportComputing().getChartReport();
        assertTrue(report.isLegendVisible());
        assertTrue(report.areLegendsHorizontal());
    }

    @Test(dependsOnGroups = {"init"})
    public void testLegendsInStackBy() {
        initAnalysePage();
        ChartReport report = analysisPage.addMetric(NUMBER_OF_ACTIVITIES).addAttribute(ACTIVITY_TYPE)
                .addStack(DEPARTMENT).waitForReportComputing().getChartReport();
        assertTrue(report.isLegendVisible());
        assertTrue(report.areLegendsVertical());

        analysisPage.changeReportType(ReportType.BAR_CHART).waitForReportComputing();
        assertTrue(report.isLegendVisible());
        assertTrue(report.areLegendsVertical());

        analysisPage.changeReportType(ReportType.LINE_CHART).waitForReportComputing();
        assertTrue(report.isLegendVisible());
        assertTrue(report.areLegendsVertical());
    }

    @Test(dependsOnGroups = {"init"})
    public void showLegendForStackedChartWithOneSeries() {
        initAnalysePage();
        ChartReport report = analysisPage.addMetric(NUMBER_OF_WON_OPPS).addStack(STAGE_NAME)
                .waitForReportComputing().getChartReport();
        assertTrue(report.isLegendVisible());
        List<String> legends = report.getLegends();
        assertEquals(legends.size(), 1);
        assertEquals(legends.get(0), "Closed Won");

        analysisPage.changeReportType(ReportType.BAR_CHART).waitForReportComputing();
        report = analysisPage.getChartReport();
        assertTrue(report.isLegendVisible());
        legends = report.getLegends();
        assertEquals(legends.size(), 1);
        assertEquals(legends.get(0), "Closed Won");
    }

    @Test(dependsOnGroups = {"init"})
    public void resetSpecialReports() {
        initAnalysePage();
        analysisPage.resetToBlankState();

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES).addAttribute(ACCOUNT).waitForReportComputing();
        assertTrue(analysisPage.isExplorerMessageVisible());
        assertEquals(analysisPage.getExplorerMessage(), "Too many data points to display");
        analysisPage.resetToBlankState();
    }

    @Test(dependsOnGroups = {"init"})
    public void testFilteringFieldsInCatalog() {
        initAnalysePage();
        final CataloguePanel cataloguePanel = analysisPage.getCataloguePanel();

        cataloguePanel.filterCatalog(CatalogFilterType.MEASURES)
            .search("am");

        assertTrue(cataloguePanel.getFieldsInViewPort()
            .stream()
            .allMatch(input -> {
                final String cssClass = input.getAttribute("class");
                return cssClass.contains(FieldType.METRIC.toString()) ||
                        cssClass.contains(FieldType.FACT.toString());
            })
        );

        cataloguePanel.filterCatalog(CatalogFilterType.ATTRIBUTES)
            .search("am");

        assertTrue(cataloguePanel.getFieldsInViewPort()
            .stream()
            .allMatch(input -> input.getAttribute("class").contains(FieldType.ATTRIBUTE.toString()))
        );
    }

    @Test(dependsOnGroups = {"init"})
    public void testCreateReportWithFieldsInCatalogFilter() {
        initAnalysePage();
        final CataloguePanel cataloguePanel = analysisPage.getCataloguePanel();

        cataloguePanel.filterCatalog(CatalogFilterType.MEASURES);
        analysisPage.addMetric(AMOUNT);
        cataloguePanel.filterCatalog(CatalogFilterType.ATTRIBUTES);
        analysisPage.addAttribute(STAGE_NAME)
            .waitForReportComputing();
        assertTrue(analysisPage.getChartReport().getTrackersCount() >= 1);
    }

    @Test(dependsOnGroups = {"init"}, description = "https://jira.intgdc.com/browse/CL-7777")
    public void testAggregationFunctionList() {
        initAnalysePage();
        analysisPage.addMetric(AMOUNT, FieldType.FACT);

        assertEquals(analysisPage.getMetricsBucket()
                .getMetricConfiguration("Sum of " + AMOUNT)
                .expandConfiguration()
                .getAllAggregations(),
            asList("Sum", "Average", "Minimum", "Maximum", "Median", "Running sum"));
    }

    @Test(dependsOnGroups = {"init"}, description = "https://jira.intgdc.com/browse/CL-6401")
    public void gridlinesShouldBeCheckedWhenExportBarChart() {
        initAnalysePage();
        analysisPage.addMetric(AMOUNT)
                .addAttribute(STAGE_NAME)
                .changeReportType(ReportType.BAR_CHART)
                .waitForReportComputing()
                .exportReport();
        String currentWindowHandle = browser.getWindowHandle();
        for (String handle : browser.getWindowHandles()) {
            if (!handle.equals(currentWindowHandle))
                browser.switchTo().window(handle);
        }
        waitForAnalysisPageLoaded(browser);
        waitForFragmentVisible(reportPage);
        checkRedBar(browser);

        reportPage.showConfiguration();
        waitForElementVisible(By.cssSelector(".globalSettings .btnSilver"), browser).click();
        WebElement gridlines = waitForElementVisible(
                By.xpath("//input[./following-sibling::*[@title='Gridlines']]"), browser);
        assertTrue(gridlines.isSelected());

        browser.close();
        browser.switchTo().window(currentWindowHandle);
    }

    @Test(dependsOnGroups = {"init"})
    public void initGoodDataClient() {
        GoodData goodDataClient = getGoodDataClient();
        project = goodDataClient.getProjectService().getProjectById(testParams.getProjectId());
        mdService = goodDataClient.getMetadataService();
    }

    @Test(dependsOnMethods = {"initGoodDataClient"}, description = "https://jira.intgdc.com/browse/CL-6942")
    public void testCaseSensitiveSortInAttributeMetric() {
        initManagePage();
        String attribute = mdService.getObjUri(project, Attribute.class,
                Restriction.identifier("attr.product.id"));
        mdService.createObj(project, new Metric("aaaaA1", "SELECT COUNT([" + attribute + "])", "#,##0"));
        mdService.createObj(project, new Metric("AAAAb2", "SELECT COUNT([" + attribute + "])", "#,##0"));

        try {
            initAnalysePage();
            analysisPage.getCataloguePanel().search("aaaa");
            assertEquals(analysisPage.getCataloguePanel().getFieldNamesInViewPort(),
                    asList("aaaaA1", "AAAAb2"));
        } finally {
            deleteMetric("aaaaA1");
            deleteMetric("AAAAb2");
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void checkXssInMetricAttribute() {
        String xssAttribute = "<button>" + IS_WON + "</button>";
        String xssMetric = "<button>" + PERCENT_OF_GOAL + "</button>";

        initAttributePage();
        waitForFragmentVisible(attributePage).initAttribute(IS_WON);
        waitForFragmentVisible(attributeDetailPage).renameAttribute(xssAttribute);

        initMetricPage();
        waitForFragmentVisible(metricPage).openMetricDetailPage(PERCENT_OF_GOAL);
        waitForFragmentVisible(metricDetailPage).renameMetric(xssMetric);

        try {
            initAnalysePage();
            final CataloguePanel cataloguePanel = analysisPage.getCataloguePanel();

            assertFalse(cataloguePanel.search("<button> test XSS </button>"));
            assertFalse(cataloguePanel.search("<script> alert('test'); </script>"));
            assertTrue(cataloguePanel.search("<button>"));
            assertEquals(cataloguePanel.getFieldNamesInViewPort(), asList(xssMetric, xssAttribute));

            StringBuilder expected = new StringBuilder(xssMetric).append("\n")
                    .append("Field Type\n")
                    .append("Calculated Measure\n")
                    .append("Defined As\n")
                    .append("select Won/Quota\n");
            assertEquals(cataloguePanel.getMetricDescription(xssMetric), expected.toString());

            expected = new StringBuilder(xssAttribute).append("\n")
                    .append("Field Type\n")
                    .append("Attribute\n")
                    .append("Values\n")
                    .append("false\n")
                    .append("true\n");
            assertEquals(cataloguePanel.getAttributeDescription(xssAttribute), expected.toString());

            analysisPage.addMetric(xssMetric).addAttribute(xssAttribute)
                .waitForReportComputing();
            assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(xssMetric));
            assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(xssAttribute));
            assertTrue(analysisPage.getFilterBuckets().isFilterVisible(xssAttribute));
            assertEquals(analysisPage.getChartReport().getTooltipTextOnTrackerByIndex(0),
                    asList(asList(xssAttribute, "true"), asList(xssMetric, "1,160.9%")));
        } finally {
            initAttributePage();
            waitForFragmentVisible(attributePage).initAttribute(xssAttribute);
            waitForFragmentVisible(attributeDetailPage).renameAttribute(IS_WON);

            initMetricPage();
            waitForFragmentVisible(metricPage).openMetricDetailPage(xssMetric);
            waitForFragmentVisible(metricDetailPage).renameMetric(PERCENT_OF_GOAL);
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void checkXssInMetricData() throws ParseException, JSONException, IOException {
        initMetricPage();
        waitForFragmentVisible(metricPage).openMetricDetailPage(PERCENT_OF_GOAL);
        String oldFormat = waitForFragmentVisible(metricDetailPage).getMetricFormat();

        String uri = format(PERCENT_OF_GOAL_URI, testParams.getProjectId());
        changeMetricFormat(getRestApiClient(), uri, "<script> alert('test'); </script> #,##0.00");

        try {
            initAnalysePage();
            analysisPage.addMetric(PERCENT_OF_GOAL)
                  .addAttribute(IS_WON)
                  .addStack(IS_WON)
                  .waitForReportComputing();
            ChartReport report = analysisPage.getChartReport();
            assertTrue(report.getTrackersCount() >= 1);
            assertEquals(report.getLegends(), asList("true"));

            assertEquals(report.getTooltipTextOnTrackerByIndex(0),
                    asList(asList(IS_WON, "true"), asList("true", "<script> alert('test')")));
        } finally {
            changeMetricFormat(getRestApiClient(), uri, oldFormat);
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void exportCustomDiscovery() {
        initAnalysePage();

        assertTrue(analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
                .addAttribute(ACTIVITY_TYPE)
                .changeReportType(ReportType.TABLE)
                .getPageHeader()
                .isExportButtonEnabled());
        TableReport analysisReport = analysisPage.getTableReport();
        List<List<String>> analysisContent = analysisReport.getContent();
        Iterator<String> analysisHeaders = analysisReport.getHeaders().iterator();

        analysisPage.exportReport();
        String currentWindowHandle = browser.getWindowHandle();
        for (String handle : browser.getWindowHandles()) {
            if (!handle.equals(currentWindowHandle))
                browser.switchTo().window(handle);
        }

        com.gooddata.qa.graphene.fragments.reports.report.TableReport tableReport =
                Graphene.createPageFragment(
                        com.gooddata.qa.graphene.fragments.reports.report.TableReport.class,
                        waitForElementVisible(By.id("gridContainerTab"), browser));

        Iterator<String> attributes = tableReport.getAttributeElements().iterator();

        sleepTight(2000); // wait for metric values is calculated and loaded
        Iterator<String> metrics = tableReport.getRawMetricElements().iterator();

        List<List<String>> content = new ArrayList<>();
        while (attributes.hasNext() && metrics.hasNext()) {
            content.add(asList(attributes.next(), metrics.next()));
        }

        assertThat(content, equalTo(analysisContent));

        List<String> headers = tableReport.getAttributesHeader();
        headers.addAll(tableReport.getMetricsHeader());
        Iterator<String> reportheaders = headers.iterator();

        while (analysisHeaders.hasNext() && reportheaders.hasNext()) {
            assertThat(reportheaders.next().toLowerCase(), equalTo(analysisHeaders.next().toLowerCase()));
        }
        checkRedBar(browser);

        browser.close();
        browser.switchTo().window(currentWindowHandle);
    }

    @Test(dependsOnGroups = {"init"})
    public void exportVisualizationWithOneAttributeInChart() {
        initAnalysePage();

        assertEquals(analysisPage.addAttribute(ACTIVITY_TYPE).getExplorerMessage(),
                "Now select a measure to display");
        assertFalse(analysisPage.getPageHeader().isExportButtonEnabled());
    }

    @Test(dependsOnGroups = {"init"})
    public void exploreDate() {
        initAnalysePage();
        StringBuilder expected = new StringBuilder(DATE).append("\n")
                .append("Represents all your dates in project. Can group by Day, Week, Month, Quarter & Year.\n")
                .append("Field Type\n")
                .append("Date\n");
        assertEquals(analysisPage.getCataloguePanel().getDateDescription(), expected.toString());
    }

    @Test(dependsOnGroups = {"init"})
    public void exploreAttribute() {
        initAnalysePage();
        StringBuilder expected = new StringBuilder(DEPARTMENT).append("\n")
                .append("Field Type\n")
                .append("Attribute\n")
                .append("Values\n")
                .append("Direct Sales\n")
                .append("Inside Sales\n");
        assertEquals(analysisPage.getCataloguePanel().getAttributeDescription(DEPARTMENT), expected.toString());
    }

    @Test(dependsOnGroups = {"init"})
    public void exploreMetric() {
        initAnalysePage();
        StringBuilder expected = new StringBuilder(NUMBER_OF_ACTIVITIES).append("\n")
                .append("Field Type\n")
                .append("Calculated Measure\n")
                .append("Defined As\n")
                .append("SELECT COUNT(Activity)\n");
        assertEquals(analysisPage.getCataloguePanel().getMetricDescription(NUMBER_OF_ACTIVITIES), expected.toString());
    }

    @Test(dependsOnGroups = {"init"})
    public void exploreAttributeInMetricFilter() {
        initAnalysePage();
        analysisPage.addMetric(NUMBER_OF_ACTIVITIES);
        final MetricConfiguration metricConfiguration = analysisPage.getMetricsBucket()
                .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
                .expandConfiguration();
        assertTrue(metricConfiguration.canAddAnotherFilter());

        StringBuilder expected = new StringBuilder(DEPARTMENT).append("\n")
                .append("Field Type\n")
                .append("Attribute\n")
                .append("Values\n")
                .append("Direct Sales\n")
                .append("Inside Sales\n");
        assertEquals(metricConfiguration.getAttributeDescription(DEPARTMENT),
                expected.toString());
    }

    @Test(dependsOnGroups = {"init"})
    public void testHiddenUnrelatedObjects() {
        initAnalysePage();
        final CataloguePanel cataloguePanel = analysisPage.getCataloguePanel();

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .addAttribute(ACTIVITY_TYPE);
        assertTrue(cataloguePanel.search(""));
        assertThat(cataloguePanel.getUnrelatedItemsHiddenCount(), equalTo(48));

        assertThat(cataloguePanel.filterCatalog(CatalogFilterType.MEASURES)
            .getUnrelatedItemsHiddenCount(), equalTo(39));
        assertThat(cataloguePanel.filterCatalog(CatalogFilterType.ATTRIBUTES)
                .getUnrelatedItemsHiddenCount(), equalTo(9));

        assertFalse(cataloguePanel.filterCatalog(CatalogFilterType.ALL)
                .search("Amo"));
        assertThat(cataloguePanel.getUnrelatedItemsHiddenCount(), equalTo(4));

        assertFalse(cataloguePanel.filterCatalog(CatalogFilterType.MEASURES)
                .search("Amo"));
        assertThat(cataloguePanel.getUnrelatedItemsHiddenCount(), equalTo(4));

        assertFalse(cataloguePanel.filterCatalog(CatalogFilterType.ATTRIBUTES)
                .search("Amo"));
        assertThat(cataloguePanel.getUnrelatedItemsHiddenCount(), equalTo(0));
    }

    @Test(dependsOnGroups = {"init"})
    public void searchNonExistent() {
        initAnalysePage();
        Stream.of(CatalogFilterType.values()).forEach(type -> {
            assertFalse(analysisPage.getCataloguePanel().filterCatalog(type)
                    .search("abcxyz"));
            assertTrue(isElementPresent(ByJQuery.selector(
                    ".adi-no-items:contains('No data matching\"abcxyz\"')"), browser));
        });

        analysisPage.getCataloguePanel().filterCatalog(CatalogFilterType.ALL);
        analysisPage.addAttribute(ACTIVITY_TYPE);
        Stream.of(CatalogFilterType.values()).forEach(type -> {
            assertFalse(analysisPage.getCataloguePanel().filterCatalog(type).search("Am"));
            assertTrue(isElementPresent(ByJQuery.selector(
                    ".adi-no-items:contains('No data matching\"Am\"')"), browser));

            assertTrue(waitForElementVisible(cssSelector(".adi-no-items .s-unavailable-items-matched"), browser)
                    .getText().matches("^\\d unrelated data item[s]? hidden$"));
        });
    }

    @Test(dependsOnGroups = {"init"})
    public void createReportWithManyAttributes() {
        initAnalysePage();
        List<List<String>> adReportContent = analysisPage.changeReportType(ReportType.TABLE)
            .addAttribute(ACTIVITY_TYPE)
            .addAttribute(DEPARTMENT)
            .addMetric(NUMBER_OF_ACTIVITIES)
            .waitForReportComputing()
            .getTableReport()
            .getContent();

        assertEquals(adReportContent, getTableReportContentInReportPage());
    }

    @Test(dependsOnGroups = {"init"})
    public void filterReportIncludeManyAttributes() {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE)
            .addAttribute(ACTIVITY_TYPE)
            .addAttribute(DEPARTMENT)
            .addMetric(NUMBER_OF_ACTIVITIES);
        analysisPage.getFilterBuckets().configAttributeFilter(ACTIVITY_TYPE, "Email")
            .configAttributeFilter(DEPARTMENT, "Direct Sales");

        List<List<String>> adReportContent = analysisPage.waitForReportComputing()
            .getTableReport()
            .getContent();

        assertEquals(adReportContent, getTableReportContentInReportPage());
    }

    @Test(dependsOnGroups = {"init"})
    public void switchReportHasOneMetricManyAttributes() {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE)
            .addAttribute(ACTIVITY_TYPE)
            .addAttribute(DEPARTMENT)
            .addMetric(NUMBER_OF_ACTIVITIES)
            .waitForReportComputing();

        Stream.of(ReportType.COLUMN_CHART, ReportType.BAR_CHART, ReportType.LINE_CHART)
            .forEach(type -> {
                analysisPage.changeReportType(type);
                takeScreenshot(browser, "switchReportHasOneMetricManyAttributes-" + type.name(), getClass());
                assertEquals(analysisPage.getStacksBucket().getAttributeName(), DEPARTMENT);
                assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(ACTIVITY_TYPE));
                assertEquals(analysisPage.getMetricsBucket().getWarningMessage(), type.getMetricMessage());
                analysisPage.undo();
        });

        analysisPage.changeReportType(ReportType.COLUMN_CHART)
            .changeReportType(ReportType.TABLE);
        takeScreenshot(browser, "switchReportHasOneMetricManyAttributes-backToTable", getClass());
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(ACTIVITY_TYPE, DEPARTMENT));
    }

    @Test(dependsOnGroups = {"init"})
    public void switchReportHasManyMetricsManyAttributes() {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE)
            .addAttribute(ACTIVITY_TYPE)
            .addAttribute(DEPARTMENT)
            .addMetric(NUMBER_OF_ACTIVITIES)
            .addMetric(QUOTA)
            .waitForReportComputing();

        Stream.of(ReportType.COLUMN_CHART, ReportType.BAR_CHART, ReportType.LINE_CHART)
            .forEach(type -> {
                analysisPage.changeReportType(type);
                takeScreenshot(browser, "switchReportHasManyMetricsManyAttributes-" + type.name(), getClass());
                assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(ACTIVITY_TYPE));
                assertEquals(analysisPage.getStacksBucket().getWarningMessage(), type.getStackByMessage());
                analysisPage.undo();
        });

        analysisPage.changeReportType(ReportType.COLUMN_CHART)
            .changeReportType(ReportType.TABLE);
        takeScreenshot(browser, "switchReportHasManyMetricsManyAttributes-backToTable", getClass());
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"init"})
    public void switchReportWithDateAttributes() {
        initAnalysePage();
        final StacksBucket stacksBucket = analysisPage.getStacksBucket();
        final AttributesBucket categoriesBucket = analysisPage.getAttributesBucket();

        analysisPage.changeReportType(ReportType.TABLE)
            .addDate()
            .addAttribute(ACTIVITY_TYPE)
            .addAttribute(DEPARTMENT);

        Stream.of(ReportType.COLUMN_CHART, ReportType.BAR_CHART, ReportType.LINE_CHART)
            .forEach(type -> {
                analysisPage.changeReportType(type);
                takeScreenshot(browser, "switchReportWithDateAttributes-firstDate-" + type.name(), getClass());
                assertEquals(stacksBucket.getAttributeName(), ACTIVITY_TYPE);
                assertEquals(categoriesBucket.getItemNames(), asList(DATE));
                analysisPage.undo();
        });

        analysisPage.resetToBlankState()
            .changeReportType(ReportType.TABLE)
            .addAttribute(ACTIVITY_TYPE)
            .addDate()
            .addAttribute(DEPARTMENT);

        Stream.of(ReportType.COLUMN_CHART, ReportType.BAR_CHART)
            .forEach(type -> {
                analysisPage.changeReportType(type);
                takeScreenshot(browser, "switchReportWithDateAttributes-secondDate-" + type.name(), getClass());
                assertEquals(stacksBucket.getAttributeName(), DEPARTMENT);
                assertEquals(categoriesBucket.getItemNames(), asList(ACTIVITY_TYPE));
                analysisPage.undo();
        });

        analysisPage.changeReportType(ReportType.LINE_CHART);
        takeScreenshot(browser, "switchReportWithDateAttributes-secondDate-" + ReportType.LINE_CHART.name(),
                getClass());
        assertEquals(stacksBucket.getAttributeName(), ACTIVITY_TYPE);
        assertEquals(categoriesBucket.getItemNames(), asList(DATE));

        analysisPage.resetToBlankState()
            .changeReportType(ReportType.TABLE)
            .addAttribute(ACTIVITY_TYPE)
            .addAttribute(DEPARTMENT)
            .addDate();

        Stream.of(ReportType.COLUMN_CHART, ReportType.BAR_CHART)
            .forEach(type -> {
                analysisPage.changeReportType(type);
                takeScreenshot(browser, "switchReportWithDateAttributes-thirdDate-" + type.name(), getClass());
                assertEquals(stacksBucket.getAttributeName(), DEPARTMENT);
                assertEquals(categoriesBucket.getItemNames(), asList(ACTIVITY_TYPE));
                analysisPage.undo();
        });

        analysisPage.changeReportType(ReportType.LINE_CHART);
        takeScreenshot(browser, "switchReportWithDateAttributes-thirdDate-" + ReportType.LINE_CHART.name(),
                getClass());
        assertEquals(stacksBucket.getAttributeName(), ACTIVITY_TYPE);
        assertEquals(categoriesBucket.getItemNames(), asList(DATE));
    }

    @Test(dependsOnGroups = {"init"})
    public void createTableReportWithMoreThan3Metrics() {
        initAnalysePage();
        List<String> headers = analysisPage.changeReportType(ReportType.TABLE)
            .addMetric(NUMBER_OF_LOST_OPPS)
            .addMetric(NUMBER_OF_OPEN_OPPS)
            .addMetric(NUMBER_OF_OPPORTUNITIES)
            .addMetric(NUMBER_OF_WON_OPPS)
            .addAttribute(DEPARTMENT)
            .addAttribute(PRODUCT)
            .waitForReportComputing()
            .getTableReport()
            .getHeaders()
            .stream()
            .map(String::toLowerCase)
            .collect(toList());
        assertEquals(headers, Stream.of(DEPARTMENT, PRODUCT, NUMBER_OF_LOST_OPPS, NUMBER_OF_OPEN_OPPS,
                NUMBER_OF_OPPORTUNITIES, NUMBER_OF_WON_OPPS).map(String::toLowerCase).collect(toList()));
        checkingOpenAsReport("createReportWithMoreThan3Metrics-tableReport");

        Stream.of(ReportType.COLUMN_CHART, ReportType.BAR_CHART, ReportType.LINE_CHART)
            .forEach(type -> {
                analysisPage.changeReportType(type);
                takeScreenshot(browser, "createReportWithMoreThan3Metrics-switchFromTableTo-" + type.name(),
                        getClass());
                assertFalse(analysisPage.waitForReportComputing()
                    .isExplorerMessageVisible());
                analysisPage.undo();
            });
    }

    @Test(dependsOnGroups = {"init"})
    public void createChartReportWithMoreThan3Metrics() {
        initAnalysePage();
        List<String> legends = analysisPage.addMetric(NUMBER_OF_LOST_OPPS)
                .addMetric(NUMBER_OF_OPEN_OPPS)
                .addMetric(NUMBER_OF_OPPORTUNITIES)
                .addMetric(NUMBER_OF_WON_OPPS)
                .waitForReportComputing()
                .getChartReport()
                .getLegends();
        assertEquals(legends, asList(NUMBER_OF_LOST_OPPS, NUMBER_OF_OPEN_OPPS, NUMBER_OF_OPPORTUNITIES,
                NUMBER_OF_WON_OPPS));
        checkingOpenAsReport("createReportWithMoreThan3Metrics-chartReport");

        List<String> headers = analysisPage.changeReportType(ReportType.TABLE)
            .waitForReportComputing()
            .getTableReport()
            .getHeaders()
            .stream()
            .map(String::toLowerCase)
            .collect(toList());
        assertEquals(headers, Stream.of(NUMBER_OF_LOST_OPPS, NUMBER_OF_OPEN_OPPS,
                NUMBER_OF_OPPORTUNITIES, NUMBER_OF_WON_OPPS).map(String::toLowerCase).collect(toList()));
    }

    @Test(dependsOnGroups = {"init"})
    public void orderDataInTableReport() {
        initAnalysePage();

        List<List<String>> content = sortReportBaseOnHeader(
                analysisPage.changeReportType(ReportType.TABLE)
                    .addMetric(NUMBER_OF_ACTIVITIES)
                    .waitForReportComputing()
                    .getTableReport(),
                NUMBER_OF_ACTIVITIES);
        assertEquals(content, asList(asList("154,271")));

        content = sortReportBaseOnHeader(
                analysisPage.addMetric(QUOTA)
                    .waitForReportComputing()
                    .getTableReport(),
                QUOTA);
        assertEquals(content, asList(asList("154,271", "$3,300,000")));

        content = sortReportBaseOnHeader(
                analysisPage.resetToBlankState()
                    .changeReportType(ReportType.TABLE)
                    .addAttribute(ACTIVITY_TYPE)
                    .waitForReportComputing()
                    .getTableReport(),
                ACTIVITY_TYPE);
        assertEquals(content, asList(asList("Web Meeting"), asList("Phone Call"), asList("In Person Meeting"),
                asList("Email")));

        content = sortReportBaseOnHeader(
                analysisPage.addAttribute(DEPARTMENT)
                    .waitForReportComputing()
                    .getTableReport(),
                DEPARTMENT);
        assertEquals(content, asList(asList("Email", "Direct Sales"), asList("In Person Meeting", "Direct Sales"),
                asList("Phone Call", "Direct Sales"), asList("Web Meeting", "Direct Sales"),
                asList("Email", "Inside Sales"), asList("In Person Meeting", "Inside Sales"),
                asList("Phone Call", "Inside Sales"), asList("Web Meeting", "Inside Sales")));

        content = sortReportBaseOnHeader(
                analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
                    .addMetric(QUOTA)
                    .waitForReportComputing()
                    .getTableReport(),
                NUMBER_OF_ACTIVITIES);
        assertEquals(content, asList(asList("Phone Call", "Direct Sales", "33,420", "$3,300,000"),
                asList("Web Meeting", "Direct Sales", "23,931", "$3,300,000"),
                asList("In Person Meeting", "Direct Sales", "22,088", "$3,300,000"),
                asList("Email", "Direct Sales", "21,615", "$3,300,000"),
                asList("Phone Call", "Inside Sales", "17,360", "$3,300,000"),
                asList("In Person Meeting", "Inside Sales", "13,887", "$3,300,000"),
                asList("Email", "Inside Sales", "12,305", "$3,300,000"),
                asList("Web Meeting", "Inside Sales", "9,665", "$3,300,000")));
    }

    private void deleteMetric(String metric) {
        initMetricPage();
        metricPage.openMetricDetailPage(metric);
        waitForFragmentVisible(metricDetailPage).deleteMetric();
        assertFalse(metricPage.isMetricVisible(metric));
    }

    private List<List<String>> getTableReportContentInReportPage() {
        analysisPage.exportReport();

        String currentWindowHandle = browser.getWindowHandle();
        for (String handle : browser.getWindowHandles()) {
            if (!handle.equals(currentWindowHandle))
                browser.switchTo().window(handle);
        }
        waitForAnalysisPageLoaded(browser);
        waitForFragmentVisible(reportPage);

        try {
            com.gooddata.qa.graphene.fragments.reports.report.TableReport report = reportPage.getTableReport();
            List<List<String>> attributesByRow = report.getAttributeElementsByRow();
            List<String> metrics = report.getRawMetricElements();

            for (int i = 0; i < metrics.size(); i++) {
                attributesByRow.get(i).add(metrics.get(i));
            }

            return attributesByRow;
        } finally {
            browser.close();
            browser.switchTo().window(currentWindowHandle);
        }
    }

    private List<List<String>> sortReportBaseOnHeader(TableReport report, String name) {
        report.sortBaseOnHeader(name);
        analysisPage.waitForReportComputing();
        return report.getContent();
    }
}
