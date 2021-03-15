package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.fragments.indigo.ExportXLSXDialog;
import com.gooddata.qa.graphene.fragments.indigo.OptionalExportMenu;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration.FormatMeasurePreset;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration.OperatorCalculated;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.OptionalStacking;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.enums.indigo.AggregationItem;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.CompareTypeDropdown;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.CustomMeasureFormatDialog;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.DateFilterPickerPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricsBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.CustomMeasureFormatDialog.Formatter;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.CustomMeasureFormatDialog.TemplatesFormat;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.PivotTableReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.ConfigurationPanel;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.DrillModalDialog;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.utils.CSVUtils;
import com.gooddata.qa.utils.XlsxUtils;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;

import org.openqa.selenium.WebDriver;
import org.jboss.arquillian.graphene.Graphene;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import static com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MeasureValueFilterPanel.LogicalOperator.GREATER_THAN_OR_EQUAL_TO;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_BEST_CASE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT_BOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_CLOSE_EOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STATUS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_REGION;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.containsString;
import static org.openqa.selenium.By.cssSelector;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForExporting;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;

public class ADMeasureFormatTest extends AbstractAnalyseTest {

    private static final String SUM_OF_FACT_AMOUNT = "Sum of Amount";
    private static final String INSIGHT_HAS_DEFAULT_MEASURE_FORMAT_PRESET = "Insight Has Default Measure Format Preset";
    private static final String INSIGHT_HAS_AM_APPLY_DEFAULT_FORMAT_NUMBER = "Insight Has AM Apply Format Measure";
    private static final String INSIGHT_HAS_SHOW_IN_PERCENT = "Column chart has show in percent";
    private static final String TABLE_HAS_TOTAL_SUB_TOTALS = "Pivot table has total";
    private static final String INSIGHT_HAS_EMPTY_VALUE = "Table has empty value";
    private static final String CALCULATED_RATIO_OF = "Ratio of …";
    private static final String DASHBOARD_HAS_EMPTY_VALUE = "Dashboard has empty value";
    private final String TIME_RANGE_FROM = "01/01/2011";
    private final String TIME_RANGE_TO = "12/31/2011";

    private ProjectRestRequest projectRestRequest;
    private AnalysisPage analysisPage;
    private MetricConfiguration metricConfiguration;
    private PivotTableReport pivotTableReport;
    private CustomMeasureFormatDialog customMeasureConfig;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "AD measure format";
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createAmountMetric();
        metrics.createAmountBOPMetric();
        metrics.createBestCaseMetric();
        metrics.createCloseEOPMetric();
        projectRestRequest = new ProjectRestRequest(getAdminRestClient(), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_NEW_AD_FILTER_BAR, true);
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_EDIT_INSIGHTS_FROM_KD, false);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void measureApplyDefaultFormatPreset() {
        analysisPage = initAnalysePage()
            .addMetricToRecommendedStepsPanelOnCanvas(FACT_AMOUNT, FieldType.FACT).waitForReportComputing();
        metricConfiguration = analysisPage.getMetricsBucket().getMetricConfiguration(SUM_OF_FACT_AMOUNT).expandConfiguration();
        ChartReport chartReport = analysisPage.getChartReport();
        assertEquals(metricConfiguration.getFormatMeasureText(), "Format: Decimal (2)");
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            singletonList(asList(SUM_OF_FACT_AMOUNT, "5,617,913,708.72")));
        analysisPage.addDate().waitForReportComputing();
        metricConfiguration.showPercents();
        analysisPage.saveInsight(INSIGHT_HAS_SHOW_IN_PERCENT);
        assertEquals(metricConfiguration.getFormatMeasureText(), "Format: Percent (2)");
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            asList(asList("Year (Closed)", "2010"), asList("% " + SUM_OF_FACT_AMOUNT, "31.83%")));
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_AMOUNT).waitForReportComputing();
        metricConfiguration = analysisPage.getMetricsBucket()
            .getMetricConfiguration(METRIC_AMOUNT).expandConfiguration();
        assertEquals(metricConfiguration.getFormatMeasureText(), "Format: Inherit");
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0), singletonList(
            asList(METRIC_AMOUNT, "$116,625,456.54")));
        analysisPage.addAttribute(ATTR_PRODUCT).waitForReportComputing();
        metricConfiguration.showPercents();
        assertEquals(metricConfiguration.getFormatMeasureText(), "Format: Percent (2)");
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            asList(asList(ATTR_PRODUCT, "CompuSci"), asList("% " + METRIC_AMOUNT, "23.34%")));

        analysisPage.changeReportType(ReportType.HEAT_MAP);
        assertEquals(metricConfiguration.getFormatMeasureText(), "Format: Inherit");
        assertEquals(chartReport.getTooltipTextOnTrackerByTitle("$27,222,899.64"),
            asList(asList(ATTR_PRODUCT, "CompuSci"), asList(METRIC_AMOUNT, "$27,222,899.64")));

        analysisPage.changeReportType(ReportType.BAR_CHART);
        assertEquals(metricConfiguration.getFormatMeasureText(), "Format: Percent (2)");
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            asList(asList(ATTR_PRODUCT, "Explorer"), asList("% " + METRIC_AMOUNT, "33.09%")));

        initAnalysePage().drag(analysisPage.getCatalogPanel().searchAndGet(METRIC_BEST_CASE, FieldType.METRIC),
            () -> waitForElementVisible(cssSelector(".s-recommendation-metric-over-time-canvas"), browser))
            .waitForReportComputing();
        metricConfiguration = analysisPage.getMetricsBucket()
            .getMetricConfiguration(METRIC_BEST_CASE).expandConfiguration();
        assertEquals(metricConfiguration.getFormatMeasureText(), "Format: Inherit");
        assertEquals(analysisPage.getMainEditor().getCanvasMessage(), "NO DATA FOR YOUR FILTER SELECTION\n" +
            "Try adjusting or removing some of the filters.");

        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetricByAttribute(ATTR_ACTIVITY)
            .addMetricByAttribute(ATTR_ACCOUNT).waitForReportComputing();
        MetricConfiguration metricConfigurationOfCountOfAccount = analysisPage.getMetricsBucket()
            .getMetricConfiguration("Count of " + ATTR_ACCOUNT).expandConfiguration();
        assertEquals(metricConfigurationOfCountOfAccount.getFormatMeasureText(), "Format: Rounded");
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
            singletonList(asList("Count of " + ATTR_ACCOUNT, "4,846")));

        analysisPage.removeMetric("Count of " + ATTR_ACCOUNT).waitForReportComputing();
        analysisPage.addMetric(METRIC_CLOSE_EOP).waitForReportComputing();
        MetricConfiguration metricConfigurationOfMetricAmountBop = analysisPage.getMetricsBucket()
            .getMetricConfiguration(METRIC_CLOSE_EOP).expandConfiguration();
        assertEquals(metricConfigurationOfMetricAmountBop.getFormatMeasureText(), "Format: Inherit");
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0), singletonList(asList(METRIC_CLOSE_EOP, "42,794.00")));

        analysisPage.undo();
        analysisPage.undo();
        analysisPage.getMetricsBucket()
            .getMetricConfiguration("Count of " + ATTR_ACCOUNT).expandConfiguration();
        assertEquals(metricConfigurationOfCountOfAccount.getFormatMeasureText(), "Format: Rounded");
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
            singletonList(asList("Count of " + ATTR_ACCOUNT, "4,846")));

        analysisPage.redo();
        analysisPage.redo();
        analysisPage.getMetricsBucket()
            .getMetricConfiguration(METRIC_CLOSE_EOP).expandConfiguration();
        assertEquals(metricConfigurationOfMetricAmountBop.getFormatMeasureText(), "Format: Inherit");
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0), singletonList(asList(METRIC_CLOSE_EOP,"42,794.00")));
        analysisPage.saveInsight(INSIGHT_HAS_DEFAULT_MEASURE_FORMAT_PRESET);
        List<String> expectedCustomPresetFormat = asList("Inherit", "Rounded", "Decimal (1)", "Decimal (2)",
            "Percent (rounded)", "Percent (1)", "Percent (2)", "Custom");
        assertEquals(metricConfigurationOfMetricAmountBop.getListFormatPresets(), expectedCustomPresetFormat);
    }

    @Test(dependsOnGroups = { "createProject" })
    public void insightHasArithmeticMeasureApplyDefaultMeasureFormat() {
        AnalysisPage analysisPage = initAnalysePage().addMetric(METRIC_AMOUNT_BOP).addMetric(METRIC_BEST_CASE)
            .waitForReportComputing();
        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        metricConfiguration = metricsBucket.createCalculatedMeasure()
            .getMetricConfiguration(CALCULATED_RATIO_OF);
        metricConfiguration.chooseOperator(MetricConfiguration.OperatorCalculated.SUM);
        metricConfiguration.chooseArithmeticMeasureA(METRIC_AMOUNT_BOP, 1);
        metricConfiguration.chooseArithmeticMeasureB(METRIC_BEST_CASE, 2);

        pivotTableReport = analysisPage.waitForReportComputing().getPivotTableReport();
        assertEquals(metricConfiguration.getFormatMeasureText(), "Format: Decimal (2)");
        assertEquals(
            pivotTableReport.getCellElementText("Sum of " + METRIC_AMOUNT_BOP + " and " + METRIC_BEST_CASE, 0, 0),
            "40,978,529.58");

        metricConfiguration.chooseOperator(OperatorCalculated.DIFFERENCE);
        analysisPage.waitForReportComputing();
        assertEquals(metricConfiguration.getFormatMeasureText(), "Format: Decimal (2)");
        assertEquals(
            pivotTableReport.getCellElementText("Difference of " + METRIC_AMOUNT_BOP + " and " + METRIC_BEST_CASE, 0, 0),
            "-30,709,734.28");

        metricConfiguration.chooseOperator(OperatorCalculated.PRODUCT);
        analysisPage.waitForReportComputing();
        assertEquals(metricConfiguration.getFormatMeasureText(), "Format: Decimal (2)");
        assertEquals(
            pivotTableReport.getCellElementText("Product of " + METRIC_AMOUNT_BOP + " and " + METRIC_BEST_CASE, 0, 0),
            "184,038,026,747,682.00");

        metricConfiguration.chooseOperator(OperatorCalculated.RATIO);
        analysisPage.waitForReportComputing();
        assertEquals(metricConfiguration.getFormatMeasureText(), "Format: Decimal (2)");
        assertEquals(
            pivotTableReport.getCellElementText("Ratio of " + METRIC_AMOUNT_BOP + " and " + METRIC_BEST_CASE, 0, 0),
            "0.14");

        metricConfiguration.chooseOperator(OperatorCalculated.CHANGE);
        analysisPage.waitForReportComputing().saveInsight(INSIGHT_HAS_AM_APPLY_DEFAULT_FORMAT_NUMBER);
        assertEquals(metricConfiguration.getFormatMeasureText(), "Format: Percent (2)");
        assertEquals(
            pivotTableReport.getCellElementText("Change from " + METRIC_BEST_CASE + " to " + METRIC_AMOUNT_BOP, 0, 0),
            "-85.68%");
    }

    @Test(dependsOnMethods = {"measureApplyDefaultFormatPreset", "insightHasArithmeticMeasureApplyDefaultMeasureFormat"})
    public void changeFormatMeasureToOtherPresets() {
        initAnalysePage().openInsight(INSIGHT_HAS_DEFAULT_MEASURE_FORMAT_PRESET).waitForReportComputing();
        metricConfiguration = analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_CLOSE_EOP)
            .expandConfiguration();
        metricConfiguration.changeFormatMeasure(FormatMeasurePreset.ROUNDED);
        analysisPage.waitForReportComputing();
        ChartReport chartReport = analysisPage.getChartReport();
        assertEquals(metricConfiguration.getFormatMeasureText(), "Format: Rounded");
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
            singletonList(asList(METRIC_CLOSE_EOP, "42,794")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            singletonList(asList("Count of " + ATTR_ACTIVITY, "154,271")));

        metricConfiguration.changeFormatMeasure(FormatMeasurePreset.PERCENT_1);
        analysisPage.waitForReportComputing();
        assertEquals(metricConfiguration.getFormatMeasureText(), "Format: Percent (1)");
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
            singletonList(asList(METRIC_CLOSE_EOP, "4,279,400.0%")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            singletonList(asList("Count of " + ATTR_ACTIVITY, "154,271")));

        metricConfiguration = analysisPage.getMetricsBucket().getMetricConfiguration("Count of " + ATTR_ACTIVITY)
            .expandConfiguration();
        metricConfiguration.changeFormatMeasure(FormatMeasurePreset.DECIMAL_1);
        analysisPage.waitForReportComputing();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
            singletonList(asList(METRIC_CLOSE_EOP, "4,279,400.0%")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            singletonList(asList("Count of " + ATTR_ACTIVITY, "154,271.0")));
        analysisPage.saveInsight().waitForReportComputing();

        analysisPage.openInsight(INSIGHT_HAS_SHOW_IN_PERCENT).waitForReportComputing();
        metricConfiguration = analysisPage.getMetricsBucket().getMetricConfiguration("% " + SUM_OF_FACT_AMOUNT)
            .expandConfiguration();
        metricConfiguration.changeFormatMeasure(FormatMeasurePreset.ROUNDED);
        analysisPage.waitForReportComputing();
        assertEquals(metricConfiguration.getFormatMeasureText(), "Format: Rounded");
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            asList(asList("Year (Closed)", "2010"), asList("% " + SUM_OF_FACT_AMOUNT, "0")));

        metricConfiguration.changeFormatMeasure(FormatMeasurePreset.DECIMAL_1);
        analysisPage.waitForReportComputing();
        assertEquals(metricConfiguration.getFormatMeasureText(), "Format: Decimal (1)");
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            asList(asList("Year (Closed)", "2010"), asList("% " + SUM_OF_FACT_AMOUNT, "0.3")));

        metricConfiguration.changeFormatMeasure(FormatMeasurePreset.PERCENT_ROUNDED);
        analysisPage.waitForReportComputing();
        assertEquals(metricConfiguration.getFormatMeasureText(), "Format: Percent (rounded)");
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            asList(asList("Year (Closed)", "2010"), asList("% " + SUM_OF_FACT_AMOUNT, "32%")));

        metricConfiguration.showPercents(false);
        analysisPage.waitForReportComputing();
        assertEquals(metricConfiguration.getFormatMeasureText(), "Format: Percent (rounded)");
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            asList(asList("Year (Closed)", "2010"), asList(SUM_OF_FACT_AMOUNT, "178,828,151,944%")));

        metricConfiguration.changeFormatMeasure(FormatMeasurePreset.DECIMAL_1);
        analysisPage.waitForReportComputing();
        assertEquals(metricConfiguration.getFormatMeasureText(), "Format: Decimal (1)");
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            asList(asList("Year (Closed)", "2010"), asList(SUM_OF_FACT_AMOUNT, "1,788,281,519.4")));

        metricConfiguration.showPercents();
        analysisPage.waitForReportComputing();
        assertEquals(metricConfiguration.getFormatMeasureText(), "Format: Decimal (1)");
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            asList(asList("Year (Closed)", "2010"), asList("% " + SUM_OF_FACT_AMOUNT, "0.3")));

        analysisPage.addMetric(METRIC_AMOUNT).waitForReportComputing();
        assertEquals(metricConfiguration.getFormatMeasureText(), "Format: Decimal (1)");
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            asList(asList("Year (Closed)", "2010"), asList(SUM_OF_FACT_AMOUNT, "1,788,281,519.4")));

        analysisPage.removeAttribute("Date").waitForReportComputing();
        assertEquals(metricConfiguration.getFormatMeasureText(), "Format: Decimal (1)");
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            singletonList(asList(SUM_OF_FACT_AMOUNT, "5,617,913,708.7")));

        pivotTableReport = initAnalysePage().openInsight(INSIGHT_HAS_AM_APPLY_DEFAULT_FORMAT_NUMBER)
            .waitForReportComputing().getPivotTableReport();
        metricConfiguration = analysisPage.getMetricsBucket()
            .getMetricConfiguration("Change from " + METRIC_BEST_CASE + " to " + METRIC_AMOUNT_BOP)
            .expandConfiguration();
        metricConfiguration.changeFormatMeasure(FormatMeasurePreset.ROUNDED);
        analysisPage.waitForReportComputing();
        assertEquals(metricConfiguration.getFormatMeasureText(), "Format: Rounded");
        assertEquals(
            pivotTableReport.getCellElementText("Change from " + METRIC_BEST_CASE + " to " + METRIC_AMOUNT_BOP, 0, 0),
            "-1");
        metricConfiguration.chooseOperator(OperatorCalculated.DIFFERENCE);
        analysisPage.saveInsight().waitForReportComputing();
        assertEquals(
            pivotTableReport.getCellElementText("Difference of " + METRIC_AMOUNT_BOP + " and " + METRIC_BEST_CASE, 0, 0),
            "-30,709,734");
    }

    @Test(dependsOnMethods = "measureApplyDefaultFormatPreset")
    public void notApplyFormatMeasureWhenStackToPercent() {
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_AMOUNT)
            .addAttribute(ATTR_DEPARTMENT).addStack(ATTR_PRODUCT).waitForReportComputing();
        analysisPage.getStacksBucket().checkOption(OptionalStacking.PERCENT);
        ChartReport chartReport = analysisPage.getChartReport();
        metricConfiguration = analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT).expandConfiguration();
        assertEquals(metricConfiguration.getFormatMeasureText(), "Format: Inherit");
        assertThat(chartReport.getDataLabels(), hasItems("19.38%", "20.13%", "37.35%", "7.29%", "7.17%", "8.68%"));

        metricConfiguration.changeFormatMeasure(FormatMeasurePreset.PERCENT_ROUNDED);
        analysisPage.waitForReportComputing();
        assertEquals(metricConfiguration.getFormatMeasureText(), "Format: Percent (rounded)");
        assertThat(chartReport.getDataLabels(), hasItems("19.38%", "20.13%", "37.35%", "7.29%", "7.17%", "8.68%"));

        metricConfiguration.changeFormatMeasure(FormatMeasurePreset.DECIMAL_2);
        analysisPage.waitForReportComputing();
        assertEquals(metricConfiguration.getFormatMeasureText(), "Format: Decimal (2)");
        assertThat(chartReport.getDataLabels(), hasItems("19.38%", "20.13%", "37.35%", "7.29%", "7.17%", "8.68%"));
    }

    @Test(dependsOnMethods = "changeFormatMeasureToOtherPresets")
    public void pivotTableTotalsApplyMeasureFormat() throws IOException {
        initAnalysePage().addMetric(METRIC_AMOUNT).addAttribute(ATTR_STATUS).addAttribute(ATTR_REGION)
            .addColumnsAttribute(ATTR_DEPARTMENT).waitForReportComputing();
        pivotTableReport = analysisPage.getPivotTableReport();
        pivotTableReport.openAggregationPopup(METRIC_AMOUNT, 0).hoverItem(AggregationItem.SUM)
            .selectRowsItem("of all rows");
        analysisPage.waitForReportComputing();
        pivotTableReport.openAggregationPopup(METRIC_AMOUNT, 0).hoverItem(AggregationItem.SUM)
            .selectRowsItem("within " + ATTR_STATUS);
        analysisPage.waitForReportComputing();
        metricConfiguration = analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT).expandConfiguration();
        List<String> expectedGrandTotals = asList("Sum", EMPTY, "$80,406,324.96", "$36,219,131.58");
        List<String> expectedSubTotals = asList(
            "Sum", "$25,040,080.81", "$17,430,490.35",
            "Sum", "$28,861,384.07", "$6,982,747.86",
            "Sum", "$26,504,860.08", "$11,805,893.37");
        assertEquals(metricConfiguration.getFormatMeasureText(), "Format: Inherit");
        assertEquals(pivotTableReport.getGrandTotalsContent(), asList(expectedGrandTotals));
        assertEquals(pivotTableReport.getSubTotalsContent(), expectedSubTotals);

        metricConfiguration.changeFormatMeasure(FormatMeasurePreset.PERCENT_2);
        analysisPage.saveInsight(TABLE_HAS_TOTAL_SUB_TOTALS).waitForReportComputing();

        List<String> expectedGrandTotalsPercent = asList("Sum", EMPTY, "8,040,632,496.00%", "3,621,913,158.00%");
        List<String> expectedSubTotalsPercent = asList(
            "Sum", "2,504,008,081.00%", "1,743,049,035.00%",
            "Sum", "2,886,138,407.00%", "698,274,786.00%",
            "Sum", "2,650,486,008.00%", "1,180,589,337.00%");
        assertEquals(metricConfiguration.getFormatMeasureText(), "Format: Percent (2)");
        assertEquals(pivotTableReport.getGrandTotalsContent(), asList(expectedGrandTotalsPercent));
        assertEquals(pivotTableReport.getSubTotalsContent(), expectedSubTotalsPercent);

        analysisPage.exportTo(OptionalExportMenu.File.XLSX);
        ExportXLSXDialog.getInstance(browser).checkOption(ExportXLSXDialog.OptionalExport.CELL_MERGED)
            .checkOption(ExportXLSXDialog.OptionalExport.FILTERS_CONTEXT).confirmExport();

        File exportFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
            + TABLE_HAS_TOTAL_SUB_TOTALS + "." + ExportFormat.EXCEL_XLSX.getName());

        waitForExporting(exportFile);
        log.info("XSLX :" + XlsxUtils.excelFileToRead(exportFile.getPath(), 0));
        assertThat(XlsxUtils.excelFileToRead(exportFile.getPath(), 0),
            hasItems(asList("Lost", "East Coast", "7754178.54", "2650359.42"),
                asList("Sum", "2.650486008E7", "1.180589337E7"),
                asList("Sum", "8.040632496E7", "3.621913158E7")));
    }

    @DataProvider(name = "customFormatsProvider")
    private Object[][] getCustomFotmatsProvider() {
        return new Object[][]{
            {Formatter.COLORS_FORMAT.toString(),
                "-1,234.568",
                "color: rgb(0, 0, 255);",
                asList(EMPTY, "1.234", "1,234.567", "1,234,567.891"),
                asList("Lost", "East Coast", "7,754,178.54", "2,650,359.42")},
            {Formatter.BACKGROUND_COLOR_FORMAT.toString(),
                "-1,234.57",
                "color: rgb(255, 0, 0); background-color: rgb(175, 248, 239);",
                asList("0.00", "1.23", "1,234.57", "1,234,567.89"),
                asList("Lost", "Sum", "25,040,080.81", "17,430,490.35")},
            {Formatter.UNIT_CONVERSION.toString(),
                "days, 00:20:34.568 hours",
                EMPTY,
                asList("days, 00:00:00.000 hours", "days, 00:00:01.234 hours", "days, 00:20:34.567 hours", "14 days, 06:56:07.891 hours"),
                asList("Lost", "East Coast", "89 days, 17:56:18.540 hours", "30 days, 16:12:39.420 hours")
            },
            {Formatter.UTF_8.toString(),
                "-1'234.57 kiểm tra nghiêm khắc",
                EMPTY,
                asList("'0.00 kiểm tra nghiêm khắc", "'1.23 kiểm tra nghiêm khắc", "1'234.57 kiểm tra nghiêm khắc", "1234'567.89 kiểm tra nghiêm khắc"),
                asList("Lost", "East Coast", "7754'178.54 kiểm tra nghiêm khắc", "2650'359.42 kiểm tra nghiêm khắc")
            },
            {Formatter.XSS.toString(),
                "<button>-1,234.57</button>",
                EMPTY,
                asList("<button>0.00</button>", "<button>1.23</button>", "<button>1,234.57</button>", "<button>1,234,567.89</button>"),
                asList("Lost", "East Coast", "<button>7,754,178.54</button>", "<button>2,650,359.42</button>")
            },
            {Formatter.BARS.toString(),
                "█░░░░░░░░░",
                "color: rgb(33, 144, 192);",
                asList("█░░░░░░░░░", "█░░░░░░░░░", "█░░░░░░░░░", "██░░░░░░░░"),
                asList("Lost", "East Coast", "████████░░", "███░░░░░░░")
            },
            {Formatter.TRUNCATE_NUMBERS.toString(),
                "-$1.2 K",
                EMPTY,
                asList("$0", "$1", "$1.2 K", "$1.2 M"),
                asList("Lost", "East Coast", "$7.8 M", "$2.7 M")
            }
        };
    }

    @Test(dependsOnMethods = "pivotTableTotalsApplyMeasureFormat", dataProvider = "customFormatsProvider")
    public void customMeasureFormatExample(String newFormat, String expectedPreviewLabel, String expectedColor,
        List<String> expectedListExtendedPreview, List<String> expectedValues) {
        pivotTableReport = initAnalysePage().openInsight(TABLE_HAS_TOTAL_SUB_TOTALS).waitForReportComputing()
            .getPivotTableReport();
        metricConfiguration = analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT).expandConfiguration();
        changeCustomFormat(newFormat);
        assertEquals(customMeasureConfig.getPreviewLabelText(), expectedPreviewLabel);
        assertEquals(customMeasureConfig.getColorPreviewLabel(), expectedColor);
        assertEquals(customMeasureConfig.getExtendedPreviewLabel(), expectedListExtendedPreview);
        customMeasureConfig.apply();
        analysisPage.waitForReportComputing();
        assertThat(pivotTableReport.getBodyContent(), hasItems(expectedValues));
    }

    @Test(dependsOnMethods = "changeFormatMeasureToOtherPresets")
    public void customMeasureFormatChartReport() {
        ChartReport chartReport = initAnalysePage().openInsight(INSIGHT_HAS_DEFAULT_MEASURE_FORMAT_PRESET)
            .waitForReportComputing().getChartReport();
        metricConfiguration = analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_CLOSE_EOP).expandConfiguration();
        customMeasureConfig = metricConfiguration.openCustomFormatMeasureDialog().getCustomMeasureFormatDialog();
        assertEquals(customMeasureConfig.getCurrentFormatOption(), "#,##0.0%");
        takeScreenshot(browser, "Measure-format-of-selected-preset", this.getClass());

        customMeasureConfig.setCustomMeasureFormat("#,###.000");
        assertEquals(customMeasureConfig.getPreviewLabelText(), "-1,234.568");
        takeScreenshot(browser, "New-format-option", this.getClass());
        customMeasureConfig.apply();
        analysisPage.waitForReportComputing();
        assertEquals(metricConfiguration.getFormatMeasureText(), "Format: Custom");
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0), singletonList(asList(METRIC_CLOSE_EOP, "42,794.000")));

        changeCustomFormat("%#,##0.00");
        assertEquals(customMeasureConfig.getPreviewLabelText(), "%-123,456.78");
        customMeasureConfig.apply();
        analysisPage.saveInsight().waitForReportComputing();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0), singletonList(asList(METRIC_CLOSE_EOP, "%4,279,400.00")));

        metricConfiguration.openCustomFormatMeasureDialog().getCustomMeasureFormatDialog();
        int windowHandles = browser.getWindowHandles().size();
        customMeasureConfig.clickHowToFormatButton();
        Function<WebDriver, Boolean> newTabOpened = browser -> browser.getWindowHandles().size() > windowHandles;
        Graphene.waitGui().until(newTabOpened);
        try {
            BrowserUtils.switchToLastTab(browser);
            boolean isOpenGoodDataHelpPageNumberFormat = browser.getCurrentUrl()
                .contains("doc/enterprise/en/dashboards-and-insights/analytical-designer/work-with-measures/format-numbers");
            assertTrue(isOpenGoodDataHelpPageNumberFormat, "Should open documentation of Format Numbers page");
        } finally {
            BrowserUtils.closeCurrentTab(browser);
            BrowserUtils.switchToFirstTab(browser);
        }
    }

    @Test(dependsOnMethods = "customMeasureFormatExample")
    public void customTemplateFormatMeasure() throws IOException {
        final String selectedFormat = "[<0][red]-#,##0.0;" + "\n[black]#,##0.0";
        final String zeroAsBlankValue = "[=null]0.00;" + "\n[>=0]#,#0.00;" + "\n[<0]-#,#0.00";
        String measureAM = "Difference of " + METRIC_AMOUNT_BOP + " and " + METRIC_BEST_CASE;

        pivotTableReport = initAnalysePage().openInsight(INSIGHT_HAS_AM_APPLY_DEFAULT_FORMAT_NUMBER)
            .waitForReportComputing().getPivotTableReport();
        analysisPage.addAttribute(ATTR_PRODUCT).waitForReportComputing();
        assertEquals(pivotTableReport.getCellElementText(measureAM, 0, 0), "-3,793,609");
        metricConfiguration = analysisPage.getMetricsBucket().getMetricConfiguration(measureAM).expandConfiguration();
        customMeasureConfig = metricConfiguration.openCustomFormatMeasureDialog().getCustomMeasureFormatDialog()
            .selectTemplateFormat(TemplatesFormat.NEGATIVE_NUMBERS_IN_RED);
        takeScreenshot(browser, "After-select-template-preset-format", this.getClass());
        assertEquals(customMeasureConfig.getCurrentFormatOption(), selectedFormat);
        assertEquals(customMeasureConfig.getPreviewLabelText(), "-1,234.6");
        assertEquals(customMeasureConfig.getColorPreviewLabel(), "color: rgb(255, 0, 0);");
        customMeasureConfig.apply();
        analysisPage.saveInsight().waitForReportComputing();
        takeScreenshot(browser, "Apply-template-format-measure-into-table", this.getClass());
        assertEquals(pivotTableReport.getCellElementText(measureAM, 0, 0), "-3,793,609.2");
        assertThat(pivotTableReport.getColorCellElement(measureAM, 0, 0), containsString("color: rgb(255, 0, 0);"));
        assertEquals(metricConfiguration.getFormatMeasureText(), "Format: Custom");

        pivotTableReport = initAnalysePage().addMetric(METRIC_BEST_CASE).addMetric(METRIC_AMOUNT_BOP)
            .addAttribute(ATTR_STAGE_NAME).waitForReportComputing().getPivotTableReport();
        metricConfiguration = analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT_BOP).expandConfiguration();
        metricConfiguration.openCustomFormatMeasureDialog().getCustomMeasureFormatDialog()
            .selectTemplateFormat(TemplatesFormat.ZERO_INSTEAD_OF_BLANK_VALUE);
        takeScreenshot(browser, "Zero-as-blank-value", this.getClass());
        assertEquals(customMeasureConfig.getCurrentFormatOption(), zeroAsBlankValue);
        assertEquals(customMeasureConfig.getPreviewLabelText(), "-1,234.57");
        customMeasureConfig.apply();
        analysisPage.saveInsight(INSIGHT_HAS_EMPTY_VALUE).waitForReportComputing();
        takeScreenshot(browser, "Apply-template-zero-as-blank-value-into-table", this.getClass());
        assertThat(pivotTableReport.getBodyContent(),
            hasItems(asList("Interest", "18,447,266", "0.00"), asList("Closed Won", "–", "7,622.95")));
        assertEquals(metricConfiguration.getFormatMeasureText(), "Format: Custom");

        analysisPage.exportTo(OptionalExportMenu.File.CSV);
        final File exportFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
            + INSIGHT_HAS_EMPTY_VALUE + "." + ExportFormat.CSV.getName());

        waitForExporting(exportFile);
        log.info("CSV: "+ CSVUtils.readCsvFile(exportFile));
        assertThat(CSVUtils.readCsvFile(exportFile),
            hasItems(asList("Interest", "18447266.14", null), asList("Closed Won", null, "7622.95")));

        metricConfiguration.openCustomFormatMeasureDialog().getCustomMeasureFormatDialog()
            .selectTemplateFormat(TemplatesFormat.ROUNDED);
        customMeasureConfig.apply();
        analysisPage.waitForReportComputing();
        assertEquals(metricConfiguration.getFormatMeasureText(), "Format: Rounded");
        assertThat(pivotTableReport.getBodyContent(),
            hasItems(asList("Interest", "18,447,266", "–"), asList("Closed Won", "–", "7,623")));

        metricConfiguration.openCustomFormatMeasureDialog().getCustomMeasureFormatDialog()
            .getToolTipFromQuestionIconTemplateFormat(TemplatesFormat.ROUNDED);
        takeScreenshot(browser, "Hover-to-see-preview-template-format", this.getClass());
        List<List<String>> actualValues = metricConfiguration.openCustomFormatMeasureDialog()
            .getCustomMeasureFormatDialog().getToolTipFromQuestionIconTemplateFormat(TemplatesFormat.ROUNDED);
        assertThat(actualValues, hasItems(asList("-1234567.891", "-1,234,568"), asList("-1234.567", "-1,235")));
    }

    @Test(dependsOnMethods = "customTemplateFormatMeasure")
    public void applyMeasureFormatForInsightHasMeasureValueFilter() throws NoSuchFieldException {
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_AMOUNT).addDateFilter()
            .addAttribute(ATTR_DEPARTMENT);
        createDerivedMeasure(METRIC_AMOUNT);
        ChartReport chartReport = analysisPage.waitForReportComputing().getChartReport();
        metricConfiguration = analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT)
            .expandConfiguration();
        metricConfiguration.changeFormatMeasure(FormatMeasurePreset.ROUNDED);
        analysisPage.waitForReportComputing();
        assertEquals(metricConfiguration.getFormatMeasureText(), "Format: Rounded");
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            asList(asList(ATTR_DEPARTMENT, "Direct Sales"), asList(METRIC_AMOUNT + " - SP year ago", "12,076,035")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
            asList(asList(ATTR_DEPARTMENT, "Direct Sales"), asList(METRIC_AMOUNT, "40,105,984")));
        metricConfiguration.showPercents();
        metricConfiguration.changeFormatMeasure(FormatMeasurePreset.PERCENT_ROUNDED);
        analysisPage.waitForReportComputing();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            asList(asList(ATTR_DEPARTMENT, "Direct Sales"), asList("% " + METRIC_AMOUNT + " - SP year ago", "64%")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
            asList(asList(ATTR_DEPARTMENT, "Direct Sales"), asList("% " + METRIC_AMOUNT, "75%")));

        analysisPage.openFilterBarPicker().checkItem("% " + METRIC_AMOUNT + " - SP year ago", 1).apply();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            asList(asList(ATTR_DEPARTMENT, "Direct Sales"), asList(METRIC_AMOUNT + " - SP year ago", "1,207,603,456%")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
            asList(asList(ATTR_DEPARTMENT, "Direct Sales"), asList(METRIC_AMOUNT, "4,010,598,396%")));
        assertFalse(metricConfiguration.isShowInPercentHidden(), "Show in % should be hidden when apply MVF");

        analysisPage.openMeasureFilterPanel(METRIC_AMOUNT + " - SP year ago", 1)
            .addMeasureValueFilter(GREATER_THAN_OR_EQUAL_TO, "686,375,597");
        analysisPage.waitForReportComputing();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            asList(asList(ATTR_DEPARTMENT, "Direct Sales"), asList(METRIC_AMOUNT + " - SP year ago", "1,207,603,456%")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
            asList(asList(ATTR_DEPARTMENT, "Direct Sales"), asList(METRIC_AMOUNT, "4,010,598,396%")));
    }

    @Test(dependsOnMethods = "customTemplateFormatMeasure")
    public void dashboardHasNumberFormatMeasure() {
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage().getSplashScreen().startEditingWidgets()
            .changeDashboardTitle(DASHBOARD_HAS_EMPTY_VALUE)
            .addInsight(INSIGHT_HAS_EMPTY_VALUE).waitForWidgetsLoading();
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.selectWidgetByHeadline(Insight.class, INSIGHT_HAS_EMPTY_VALUE);
        ConfigurationPanel configurationPanel = indigoDashboardsPage.waitForWidgetsLoading().getConfigurationPanel();
        configurationPanel.drillIntoInsight(METRIC_AMOUNT_BOP, INSIGHT_HAS_AM_APPLY_DEFAULT_FORMAT_NUMBER);
        indigoDashboardsPage.saveEditModeWithWidgets();
        pivotTableReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, INSIGHT_HAS_EMPTY_VALUE)
            .getPivotTableReport();
        takeScreenshot(browser, "KPI-dashboard-apply-measure-format-and-drill-to-insight", this.getClass());
        pivotTableReport.clickOnElement(METRIC_AMOUNT_BOP, 0, 0);
        indigoDashboardsPage.waitForDrillModalDialogLoading();
        pivotTableReport = DrillModalDialog.getInstance(browser).getPivotTableReport();
        List<String> expectedValues = asList("1,592,356", "1,338,134", "13,479,304", "423,088", "536,619", "1,077,766");
        takeScreenshot(browser, "Insight-in-drill-modal-dialog", this.getClass());
        assertEquals(pivotTableReport.getBodyContentColumn(2).stream().flatMap(List::stream).collect(toList()), expectedValues);
        assertThat(pivotTableReport
            .getColorCellElement("Difference of " + METRIC_AMOUNT_BOP + " and " + METRIC_BEST_CASE, 0, 0),
            containsString("color: rgb(255, 0, 0);"));
        DrillModalDialog.getInstance(browser).close();
        indigoDashboardsPage.exportDashboardToPDF();
        List<String> contents = asList(getContentFrom(DASHBOARD_HAS_EMPTY_VALUE).split("\n"));
        assertThat(contents, hasItems("Interest 18,447,266 0.00"));
    }

    private void changeCustomFormat(String newFormat) {
        metricConfiguration.openCustomFormatMeasureDialog().getCustomMeasureFormatDialog()
            .setCustomMeasureFormat(newFormat).clickShowMoreButton();
    }

    private void createDerivedMeasure(String metric) throws NoSuchFieldException {
        FiltersBucket filtersBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filtersBucket
            .openDatePanelOfFilter(filtersBucket.getDateFilter());
        dateFilterPickerPanel.configTimeFilterByRangeHelper(TIME_RANGE_FROM, TIME_RANGE_TO)
            .changeCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_PREVIOUS_YEAR).openCompareApplyMeasures()
            .selectByNames(metric).apply();
        dateFilterPickerPanel.apply();
        analysisPage.waitForReportComputing();
    }
}
