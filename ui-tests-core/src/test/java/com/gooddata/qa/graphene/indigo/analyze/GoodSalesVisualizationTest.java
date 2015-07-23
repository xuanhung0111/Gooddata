package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.common.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForFragmentVisible;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.http.ParseException;
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
import com.gooddata.qa.graphene.entity.indigo.ReportDefinition;
import com.gooddata.qa.graphene.enums.indigo.CatalogFilterType;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.utils.http.RestUtils;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class GoodSalesVisualizationTest extends AnalyticalDesignerAbstractTest {

    private static final String NUMBER_OF_WON_OPPS = "# of Won Opps.";
    private static final String PERCENT_OF_GOAL = "% of Goal";
    private static final String IS_WON = "Is Won?";

    private static final String EXPORT_ERROR_MESSAGE = "Visualization is not compatible with Report Editor. "
            + "\"Stage Name\" is in configuration twice. Remove one attribute to Open as Report.";

    private static final String PERCENT_OF_GOAL_URI = "/gdc/md/%s/obj/8136";

    @BeforeClass
    public void initialize() {
        projectTemplate = "/projectTemplates/GoodSalesDemo/2";
        projectTitle = "Indigo-GoodSales-Demo-Visualization-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void disableExportForUnexportableVisualization() {
        initAnalysePage();
        analysisPage.createReport(new ReportDefinition().withMetrics(AMOUNT));
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        assertTrue(analysisPage.isExportToReportButtonEnabled());

        analysisPage.addCategory(STAGE_NAME).waitForReportComputing();
        assertEquals(report.getTrackersCount(), 8);

        analysisPage.addStackBy(STAGE_NAME);
        assertEquals(report.getTrackersCount(), 8);

        assertFalse(analysisPage.isExportToReportButtonEnabled());
        assertEquals(analysisPage.getExportToReportButtonTooltipText(), EXPORT_ERROR_MESSAGE);
    }

    @Test(dependsOnGroups = {"init"})
    public void dontShowLegendWhenOnlyOneMetric() {
        initAnalysePage();
        analysisPage.createReport(new ReportDefinition().withMetrics(AMOUNT).withCategories(STAGE_NAME));
        ChartReport report = analysisPage.waitForReportComputing().getChartReport();
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
        analysisPage.createReport(new ReportDefinition().withMetrics(AMOUNT, NUMBER_OF_ACTIVITIES));
        ChartReport report = analysisPage.waitForReportComputing().getChartReport();
        assertTrue(report.isLegendVisible());
        assertTrue(report.areLegendsHorizontal());
    }

    @Test(dependsOnGroups = {"init"})
    public void testLegendsInStackBy() {
        initAnalysePage();
        analysisPage.createReport(new ReportDefinition().withMetrics(NUMBER_OF_ACTIVITIES)
                .withCategories(ACTIVITY_TYPE)).addStackBy(DEPARTMENT);
        ChartReport report = analysisPage.waitForReportComputing().getChartReport();
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
        analysisPage.createReport(new ReportDefinition().withMetrics(NUMBER_OF_WON_OPPS)).addStackBy(STAGE_NAME);
        ChartReport report = analysisPage.waitForReportComputing().getChartReport();
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

        analysisPage.createReport(new ReportDefinition().withMetrics(NUMBER_OF_ACTIVITIES)
                .withCategories(ACCOUNT));
        analysisPage.waitForReportComputing();
        assertTrue(analysisPage.isExplorerMessageVisible());
        assertEquals(analysisPage.getExplorerMessage(), "Too many data points to display");
        analysisPage.resetToBlankState();

        analysisPage.createReport(new ReportDefinition().withMetrics(NUMBER_OF_ACTIVITIES)
                .withCategories(STAGE_NAME));
        analysisPage.waitForReportComputing();
        assertTrue(analysisPage.isExplorerMessageVisible());
        assertEquals(analysisPage.getExplorerMessage(), "Visualization cannot be displayed");
        analysisPage.resetToBlankState();
    }

    @Test(dependsOnGroups = {"init"})
    public void testFilteringFieldsInCatalog() {
        initAnalysePage();
        analysisPage.searchBucketItem("am");
        analysisPage.filterCatalog(CatalogFilterType.METRICS_N_FACTS);
        assertTrue(Iterables.all(analysisPage.getAllCatalogFieldsInViewPort(), new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement input) {
                String cssClass = input.getAttribute("class");
                return cssClass.contains(FieldType.METRIC.toString()) ||
                        cssClass.contains(FieldType.FACT.toString());
            }
        }));

        analysisPage.filterCatalog(CatalogFilterType.ATTRIBUTES);
        assertTrue(Iterables.all(analysisPage.getAllCatalogFieldsInViewPort(), new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement input) {
                return input.getAttribute("class").contains(FieldType.ATTRIBUTE.toString());
            }
        }));
    }

    @Test(dependsOnGroups = {"init"})
    public void testCreateReportWithFieldsInCatalogFilter() {
        initAnalysePage();
        analysisPage.filterCatalog(CatalogFilterType.METRICS_N_FACTS)
            .addMetric(AMOUNT)
            .filterCatalog(CatalogFilterType.ATTRIBUTES)
            .addCategory(STAGE_NAME)
            .waitForReportComputing();
        assertTrue(analysisPage.getChartReport().getTrackersCount() >= 1);
    }

    @Test(dependsOnGroups = {"init"}, description = "https://jira.intgdc.com/browse/CL-7777")
    public void testAggregationFunctionList() {
        initAnalysePage();
        assertEquals(analysisPage.addMetricFromFact(AMOUNT)
            .expandMetricConfiguration("Sum of " + AMOUNT)
            .getAllMetricAggregations("Sum of " + AMOUNT),
            asList("Sum", "Average", "Minimum", "Maximum", "Median", "Running sum"));
    }

    @Test(dependsOnGroups = {"init"}, description = "https://jira.intgdc.com/browse/CL-6401")
    public void gridlinesShouldBeCheckedWhenExportBarChart() {
        initAnalysePage();
        analysisPage.createReport(new ReportDefinition().withMetrics(AMOUNT)
                .withCategories(STAGE_NAME).withType(ReportType.BAR_CHART))
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

    @Test(dependsOnGroups = {"init"}, description = "https://jira.intgdc.com/browse/CL-6942")
    public void testCaseSensitiveSortInAttributeMetric() throws InterruptedException {
        GoodData goodDataClient = getGoodDataClient();
        Project project = goodDataClient.getProjectService().getProjectById(testParams.getProjectId());
        MetadataService mdService = goodDataClient.getMetadataService();

        initManagePage();
        String attribute = mdService.getObjUri(project, Attribute.class,
                Restriction.identifier("attr.product.id"));
        mdService.createObj(project, new Metric("aaaaA1", "SELECT COUNT([" + attribute + "])", "#,##0"));
        mdService.createObj(project, new Metric("AAAAb2", "SELECT COUNT([" + attribute + "])", "#,##0"));

        try {
            initAnalysePage();
            analysisPage.searchBucketItem("aaaa");
            assertEquals(analysisPage.getAllCatalogFieldNamesInViewPort(), asList("aaaaA1", "AAAAb2"));
        } finally {
            deleteMetric("aaaaA1");
            deleteMetric("AAAAb2");
        }
    }

    @SuppressWarnings("unchecked")
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
            assertFalse(analysisPage.searchBucketItem("<button> test XSS </button>"));
            assertFalse(analysisPage.searchBucketItem("<script> alert('test'); </script>"));
            assertTrue(analysisPage.searchBucketItem("<button>"));
            assertEquals(analysisPage.getAllCatalogFieldNamesInViewPort(), asList(xssMetric, xssAttribute));

            StringBuilder expected = new StringBuilder(xssMetric).append("\n")
                    .append("Field Type\n")
                    .append("Calculated Measure\n")
                    .append("Defined As\n")
                    .append("select Won/Quota\n");
            assertEquals(analysisPage.getMetricDescription(xssMetric), expected.toString());

            expected = new StringBuilder(xssAttribute).append("\n")
                    .append("Field Type\n")
                    .append("Attribute\n")
                    .append("Values\n")
                    .append("false\n")
                    .append("true\n");
            assertEquals(analysisPage.getAttributeDescription(xssAttribute), expected.toString());

            analysisPage.createReport(new ReportDefinition().withMetrics(xssMetric).withCategories(xssAttribute))
                .waitForReportComputing();
            assertEquals(analysisPage.getAllAddedMetricNames(), asList(xssMetric));
            assertEquals(analysisPage.getAllAddedCategoryNames(), asList(xssAttribute));
            assertTrue(analysisPage.isFilterVisible(xssAttribute));
            assertEquals(analysisPage.getChartReport().getTooltipTextOnTrackerByIndex(0),
                    asList(asList(IS_WON, "true"), asList(xssMetric, "[1161")));
        } finally {
            initAttributePage();
            waitForFragmentVisible(attributePage).initAttribute(xssAttribute);
            waitForFragmentVisible(attributeDetailPage).renameAttribute(IS_WON);

            initMetricPage();
            waitForFragmentVisible(metricPage).openMetricDetailPage(xssMetric);
            waitForFragmentVisible(metricDetailPage).renameMetric(PERCENT_OF_GOAL);
        }
    }

    @SuppressWarnings("unchecked")
    @Test(dependsOnGroups = {"init"})
    public void checkXssInMetricData() throws ParseException, JSONException, IOException {
        initMetricPage();
        waitForFragmentVisible(metricPage).openMetricDetailPage(PERCENT_OF_GOAL);
        String oldFormat = waitForFragmentVisible(metricDetailPage).getMetricFormat();

        String uri = format(PERCENT_OF_GOAL_URI, testParams.getProjectId());
        RestUtils.changeMetricFormat(getRestApiClient(), uri, "<script> alert('test'); </script> #,##0.00");

        try {
            initAnalysePage();
            analysisPage.createReport(new ReportDefinition().withMetrics(PERCENT_OF_GOAL)
                    .withCategories(IS_WON))
                  .addStackBy(IS_WON)
                  .waitForReportComputing();
            ChartReport report = analysisPage.getChartReport();
            assertTrue(report.getTrackersCount() >= 1);
            assertEquals(report.getLegends(), asList("true"));

            assertEquals(report.getTooltipTextOnTrackerByIndex(0),
                    asList(asList(IS_WON, "true"), asList("true", "<script> alert('test')")));
        } finally {
            RestUtils.changeMetricFormat(getRestApiClient(), uri, oldFormat);
        }
    }

    private void deleteMetric(String metric) throws InterruptedException {
        initMetricPage();
        metricPage.openMetricDetailPage(metric);
        waitForFragmentVisible(metricDetailPage).deleteMetric();
        assertFalse(metricPage.isMetricVisible(metric));
    }
}
