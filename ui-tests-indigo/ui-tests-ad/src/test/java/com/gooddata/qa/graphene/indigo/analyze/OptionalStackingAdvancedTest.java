package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.fixture.ResourceManagement;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.fragments.indigo.analyze.ExportToSelect.DataType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.EmbeddedAnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricsBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.StacksBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.StacksBucket.OptionalStacking;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.utils.XlsxUtils;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.ConfigurationPanelBucket.Items.Y_AXIS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_FORECAST_CATEGORY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_WON;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_BEST_CASE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT_BOP;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForExporting;
import static com.gooddata.qa.utils.http.ColorPaletteRequestData.ColorPalette;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.openqa.selenium.By.tagName;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class OptionalStackingAdvancedTest extends AbstractAnalyseTest {

    private static final String INSIGHT_HAS_SOME_METRICS = "Some metrics";
    private static final String INSIGHT_HAS_A_MEASURE_AND_AN_ATTRIBUTE_AND_A_STACK = "Measure, attribute, stack";
    private static final List<ReportType> REPORT_TYPES = asList(ReportType.COLUMN_CHART, ReportType.BAR_CHART);
    private static final String IFRAME_WRAPPER_URL = "http://gdc.sitina.net/wrapper.html";
    private static final String EMBEDDED_URI = "analyze/embedded/#/%s/reportId/edit";
    final List<ReportType> UNAPPLIED_REPORT_TYPES_OPTIONAL_STACKING = asList(ReportType.TABLE, ReportType.LINE_CHART,
        ReportType.HEAD_LINE, ReportType.SCATTER_PLOT, ReportType.BUBBLE_CHART, ReportType.PIE_CHART,
        ReportType.DONUT_CHART, ReportType.TREE_MAP, ReportType.HEAT_MAP);
    private IndigoRestRequest indigoRestRequest;
    private String sourceProjectId;
    private String targetProjectId;
    private String insightJsonObject;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "SD-155 - Optional stacking for AD (Advanced)";
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createAnotherProject() {
        sourceProjectId = testParams.getProjectId();
        targetProjectId = createProjectUsingFixture(
            "Copy of " + projectTitle, ResourceManagement.ResourceTemplate.GOODSALES);
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createAmountMetric();
        metrics.createWonMetric();
        metrics.createBestCaseMetric();
        metrics.createAmountBOPMetric();
        metrics.createCloseEOPMetric();

        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void theAxisScaleIsAutoResetWhenUncheckStackToPercentWithSettingMinMax() {
        String rightMin = "10000000";
        String rightMax = "60000000";
        String insight = "Insight: " + generateHashString();

        indigoRestRequest.createInsight(
            new InsightMDConfiguration(insight, ReportType.COLUMN_CHART)
                .setMeasureBucket(
                    asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)),
                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_BEST_CASE)),
                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_WON)),
                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT_BOP))))
                .setCategoryBucket(asList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                        CategoryBucket.Type.ATTRIBUTE),
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                        CategoryBucket.Type.ATTRIBUTE))));

        MetricsBucket metricsBucket = initAnalysePage().openInsight(insight).waitForReportComputing().getMetricsBucket();
        metricsBucket.getMetricConfiguration(METRIC_AMOUNT).expandConfiguration().checkShowOnSecondaryAxis();
        metricsBucket.getMetricConfiguration(METRIC_BEST_CASE).expandConfiguration().checkShowOnSecondaryAxis();

        StacksBucket stacksBucket = analysisPage.getStacksBucket().checkOption(OptionalStacking.MEASURES)
            .checkOption(OptionalStacking.PERCENT);
        analysisPage.openConfigurationPanelBucket()
            .getItemConfiguration(Y_AXIS.toString() + " (Right)").expandConfiguration()
            .setMinMaxValueOnAxis(rightMin, rightMax);

        ChartReport chartReport = analysisPage.waitForReportComputing().getChartReport();
        assertThat(chartReport.getValueSecondaryYaxis().stream()
            .flatMap(List::stream)
            .collect(Collectors.toList()), hasItems("10M", "60M"));

        metricsBucket.expandMeasureConfigurationPanel();
        stacksBucket.uncheckOption(OptionalStacking.PERCENT);
        analysisPage.waitForReportComputing();
        assertThat(chartReport.getValueSecondaryYaxis().stream()
            .flatMap(List::stream)
            .collect(Collectors.toList()), hasItems("0", "70.7M"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void theAxisScaleIsAutoResetWhenUncheckStackToPercentWithNoSettingMinMax() {
        String insight = "Insight: " + generateHashString();

        indigoRestRequest.createInsight(
            new InsightMDConfiguration(insight, ReportType.COLUMN_CHART)
                .setMeasureBucket(
                    asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)),
                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_BEST_CASE)),
                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_WON)),
                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT_BOP))))
                .setCategoryBucket(asList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                        CategoryBucket.Type.ATTRIBUTE),
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                        CategoryBucket.Type.ATTRIBUTE))));
        MetricsBucket metricsBucket = initAnalysePage().openInsight(insight).waitForReportComputing().getMetricsBucket();

        metricsBucket.getMetricConfiguration(METRIC_AMOUNT).expandConfiguration().checkShowOnSecondaryAxis();
        metricsBucket.getMetricConfiguration(METRIC_BEST_CASE).expandConfiguration().checkShowOnSecondaryAxis();

        analysisPage.getStacksBucket().checkOption(OptionalStacking.MEASURES).checkOption(OptionalStacking.PERCENT)
                .uncheckOption(OptionalStacking.PERCENT);
        analysisPage.waitForReportComputing();
        assertThat(analysisPage.getChartReport().getValueSecondaryYaxis().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList()), hasItems("0", "70.7M"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void theAxisScaleIsChangedToPercentWhenCheckStackToPercent() {
        String insight = "Insight: " + generateHashString();

        indigoRestRequest.createInsight(
            new InsightMDConfiguration(insight, ReportType.COLUMN_CHART)
                .setMeasureBucket(
                    asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)),
                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_BEST_CASE))))
                .setCategoryBucket(asList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                        CategoryBucket.Type.ATTRIBUTE),
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                        CategoryBucket.Type.ATTRIBUTE))));

        initAnalysePage().openInsight(insight).getStacksBucket()
            .checkOption(OptionalStacking.MEASURES).checkOption(OptionalStacking.PERCENT);
        ChartReport chartReport = analysisPage.waitForReportComputing().getChartReport();

        assertThat(chartReport.getValuePrimaryYaxis().stream()
            .flatMap(List::stream)
            .collect(Collectors.toList()), hasItems("100%", "0%"));

        analysisPage.openConfigurationPanelBucket()
            .getItemConfiguration(Y_AXIS.toString()).expandConfiguration()
            .setMinMaxValueOnAxis("0", "0.5");
        assertThat(chartReport.getValuePrimaryYaxis().stream()
            .flatMap(List::stream)
            .collect(Collectors.toList()), hasItems("0%", "50%"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkSomeOptionsWithInsightIsRendered() {
        createInsightHasAMeasureAndAnAttributeAndAStack(INSIGHT_HAS_A_MEASURE_AND_AN_ATTRIBUTE_AND_A_STACK,
            METRIC_AMOUNT, ATTR_FORECAST_CATEGORY, ATTR_DEPARTMENT, ReportType.COLUMN_CHART);
        initAnalysePage().openInsight(INSIGHT_HAS_A_MEASURE_AND_AN_ATTRIBUTE_AND_A_STACK).waitForReportComputing();

        analysisPage.addAttribute(ATTR_DEPARTMENT).waitForReportComputing();

        ChartReport chartReport = analysisPage.getChartReport();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"), asList(ATTR_DEPARTMENT, "Direct Sales"),
                asList("Direct Sales", "$33,562,482.51")));

        analysisPage.undo().waitForReportComputing();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"), asList("Direct Sales", "$33,562,482.51")));

        analysisPage.redo().waitForReportComputing();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"), asList(ATTR_DEPARTMENT, "Direct Sales"),
                asList("Direct Sales", "$33,562,482.51")));

        analysisPage.resetToBlankState();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkInsightIsRenderedWithEmbedding() {
        insightJsonObject = indigoRestRequest.createInsight(
            new InsightMDConfiguration(INSIGHT_HAS_SOME_METRICS, ReportType.COLUMN_CHART)
                .setMeasureBucket(
                    asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)),
                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_WON))))
                .setCategoryBucket(singletonList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                        CategoryBucket.Type.ATTRIBUTE))));

        embedAdToWrapperPage(getEmbeddedAdUrl());
        getEmbeddedAnalysisPage().openInsight(INSIGHT_HAS_SOME_METRICS).waitForReportComputing();
        checkInsightIsRendered();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkInsightIsRenderedWithExporting() throws IOException {
        String insight = "Insight-" + generateHashString();

        indigoRestRequest.createInsight(
            new InsightMDConfiguration(insight, ReportType.COLUMN_CHART)
                .setMeasureBucket(
                    asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)),
                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_WON))))
                .setCategoryBucket(singletonList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                        CategoryBucket.Type.ATTRIBUTE))));

        initAnalysePage().openInsight(insight).waitForReportComputing().exportTo(DataType.XLSX)
            .getExportXLSXDialog().confirmExport();

        final File exportFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
            + insight + "." + ExportFormat.EXCEL_XLSX.getName());

        waitForExporting(exportFile);
        assertEquals(XlsxUtils.excelFileToRead(exportFile.getPath(), 0), asList(
            asList(ATTR_FORECAST_CATEGORY, "Exclude", "Include"),
            asList(METRIC_AMOUNT, "4.893263959E7", "6.769281695E7"),
            asList(METRIC_WON, "1.961675537E7", "1.869399808E7")));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkBackwardCompatible() {
        String insight = "Insight-" + generateHashString();

        indigoRestRequest.createInsight(
            new InsightMDConfiguration(insight, ReportType.COLUMN_CHART)
                .setMeasureBucket(
                    asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)),
                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_BEST_CASE)),
                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_WON)),
                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT_BOP))))
                .setCategoryBucket(asList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                        CategoryBucket.Type.ATTRIBUTE),
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                        CategoryBucket.Type.ATTRIBUTE))));

        MetricsBucket metricsBucket = initAnalysePage().openInsight(insight)
            .waitForReportComputing().getMetricsBucket();
        metricsBucket.getMetricConfiguration(METRIC_AMOUNT).expandConfiguration().checkShowOnSecondaryAxis();
        metricsBucket.getMetricConfiguration(METRIC_BEST_CASE).expandConfiguration().checkShowOnSecondaryAxis();

        StacksBucket stacksBucket = analysisPage.getStacksBucket();
        UNAPPLIED_REPORT_TYPES_OPTIONAL_STACKING.stream().forEach(element -> {

            analysisPage.changeReportType(element).waitForReportComputing();
            assertFalse(stacksBucket.isOptionCheckPresent(OptionalStacking.MEASURES),
                "Should not have checkbox Stack Measures");
            assertFalse(stacksBucket.isOptionCheckPresent(OptionalStacking.PERCENT),
                "Should not have checkbox Stack to 100%");
        });

        REPORT_TYPES.stream().forEach(element -> {

            analysisPage.changeReportType(element).waitForReportComputing();
            assertFalse(stacksBucket.isOptionCheck(OptionalStacking.MEASURES),
                "Checkbox Stack Measures should be unchecked by Default on Bar/Column/Dual");
        });

        analysisPage.changeReportType(ReportType.STACKED_AREA_CHART).waitForReportComputing();
        assertTrue(stacksBucket.isOptionCheck(OptionalStacking.MEASURES),
            "Checkbox Stack measures should be checked by default on AreaChart");
    }

    @Test(dependsOnMethods = {"checkInsightIsRenderedWithEmbedding", "createAnotherProject"})
    public void testPartialExportAndImportProject() {
        String exportToken = exportPartialProject(insightJsonObject, DEFAULT_PROJECT_CHECK_LIMIT);
        testParams.setProjectId(targetProjectId);
        try {
            importPartialProject(exportToken, DEFAULT_PROJECT_CHECK_LIMIT);
            initAnalysePage().openInsight(INSIGHT_HAS_SOME_METRICS).waitForReportComputing();
            checkInsightIsRendered();
        } finally {
            testParams.setProjectId(sourceProjectId);
            if (nonNull(targetProjectId)) {
                deleteProject(targetProjectId);
            }
        }
    }

    private void checkInsightIsRendered() {
        ChartReport chartReport = getEmbeddedAnalysisPage().getChartReport();

        getEmbeddedAnalysisPage().getStacksBucket().checkOption(OptionalStacking.MEASURES)
            .checkOption(OptionalStacking.PERCENT);

        for (ReportType reportType : REPORT_TYPES) {
            getEmbeddedAnalysisPage().changeReportType(reportType).waitForReportComputing();
            assertThat(chartReport.getDataLabels(), hasItems("71.38%", "78.36%", "28.62%", "21.64%"));
            assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.CYAN.toString());
            assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.LIME_GREEN.toString());
        }
        getEmbeddedAnalysisPage().changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"), asList(METRIC_AMOUNT, "71.38%")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
            asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"), asList(METRIC_WON, "28.62%")));
        assertEquals(chartReport.getXaxisLabels(), asList("Exclude", "Include"));

        getEmbeddedAnalysisPage().changeReportType(ReportType.BAR_CHART).waitForReportComputing();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            asList(asList(ATTR_FORECAST_CATEGORY, "Include"), asList(METRIC_AMOUNT, "78.36%")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
            asList(asList(ATTR_FORECAST_CATEGORY, "Include"), asList(METRIC_WON, "21.64%")));
        assertEquals(chartReport.getXaxisLabels(), asList("Include", "Exclude"));
        assertEquals(chartReport.getXLocationColumn(0, 0), chartReport.getXLocationColumn(1, 0));
    }

    private String getEmbeddedAdUrl() {
        return getRootUrl() + format(EMBEDDED_URI, testParams.getProjectId());
    }

    private void embedAdToWrapperPage(final String url) {
        browser.get(IFRAME_WRAPPER_URL);
        final WebElement urlTextBox = waitForElementVisible(By.id("url"), browser);
        urlTextBox.sendKeys(url);
        // clicking on go button is not stable
        urlTextBox.submit();

        browser.switchTo().frame(waitForElementVisible(tagName("iframe"), browser));
    }

    private EmbeddedAnalysisPage getEmbeddedAnalysisPage() {
        return EmbeddedAnalysisPage.getInstance(browser);
    }

    private void createInsightHasAMeasureAndAnAttributeAndAStack(
        String title, String metric, String attribute, String stack, ReportType reportType) {
        indigoRestRequest.createInsight(
            new InsightMDConfiguration(title, reportType)
                .setMeasureBucket(
                    singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric))))
                .setCategoryBucket(asList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(attribute),
                        CategoryBucket.Type.ATTRIBUTE),
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(stack),
                        CategoryBucket.Type.STACK))));
    }
}
