package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.sdk.model.md.Fact;
import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.DateDimensionSelect;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.DateFilterPickerPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.PivotTableReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.utils.http.ColorPaletteRequestData;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import static com.gooddata.sdk.model.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementDisabled;
import static com.gooddata.qa.graphene.utils.ElementUtils.getTooltipFromElement;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AVG_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT_BOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_FORECAST_CATEGORY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class PieChartReportTest extends AbstractAnalyseTest {

    private static final String INSIGHT_HAS_MEASURE_AND_ATTRIBUTE = "Measure and attribute";
    private static final String INSIGHT_HAS_TWO_MEASURES = "Two measures";
    private static final String INSIGHT_HAS_NEGATIVE_VALUE = "Negative value";
    private static final String INSIGHT_HAS_MORE_TWENTY_VALUES = "More twenty values";

    private IndigoRestRequest indigoRestRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "CL-11698 Pie chart - AD";
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createAmountMetric();
        metrics.createAvgAmountMetric();
        metrics.createAmountBOPMetric();
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void switchToPieChartForEmptyADPage() {
        assertThat(initAnalysePage().changeReportType(ReportType.PIE_CHART).getMainEditor().getReportEmpty(),
                containsString("Get started"));
        assertTrue(analysisPage.getMetricsBucket().isEmpty(), "Measure Bucket should be empty");
        assertTrue(analysisPage.getAttributesBucket().isEmpty(), "ViewBy Bucket should be empty");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createPieChartWithAMeasureAndAnAttribute() {
        initAnalysePage().changeReportType(ReportType.PIE_CHART).addMetric(METRIC_AMOUNT).addAttribute(ATTR_DEPARTMENT)
                .saveInsight(INSIGHT_HAS_MEASURE_AND_ATTRIBUTE).waitForReportComputing();
        ChartReport chartReport = analysisPage.getChartReport();

        assertEquals(chartReport.getTrackersCount(), 2);
        assertEquals(chartReport.getLegends(), asList("Direct Sales", "Inside Sales"));
        assertEquals(parseFilterText(analysisPage.getFilterBuckets()
                .getFilterText(ATTR_DEPARTMENT)), asList(ATTR_DEPARTMENT, "All"));

        WebElement metric = analysisPage.getCatalogPanel().searchAndGet(METRIC_AVG_AMOUNT, FieldType.METRIC);
        analysisPage.tryToDrag(metric, analysisPage.getMetricsBucket().get(METRIC_AMOUNT));

        WebElement attribute = analysisPage.getCatalogPanel().searchAndGet(ATTR_FORECAST_CATEGORY, FieldType.ATTRIBUTE);
        analysisPage.tryToDrag(attribute, analysisPage.getAttributesBucket().get(ATTR_DEPARTMENT));

        assertEquals(analysisPage.getMetricsBucket().getItemNames(), singletonList(METRIC_AVG_AMOUNT));
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), singletonList(ATTR_FORECAST_CATEGORY));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createPieChartWithTwoMeasures() {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_TWO_MEASURES, ReportType.PIE_CHART)
                        .setMeasureBucket(
                                asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)),
                                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AVG_AMOUNT)))));

        initAnalysePage().openInsight(INSIGHT_HAS_TWO_MEASURES).waitForReportComputing();

        ChartReport chartReport = analysisPage.getChartReport();
        assertEquals(chartReport.getTrackersCount(), 2);
        assertEquals(chartReport.getLegends(), asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT));
        assertEquals(analysisPage.getAttributesBucket().getWarningMessage(), ReportType.PIE_CHART.getViewbyByMessage());

        WebElement attribute = analysisPage.getCatalogPanel().searchAndGet(ATTR_FORECAST_CATEGORY, FieldType.ATTRIBUTE);
        analysisPage.tryToDrag(attribute, analysisPage.getAttributesBucket().getRoot());
        assertTrue(analysisPage.getAttributesBucket().getItemNames().isEmpty(), "Attribute Bucket should be empty");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void replaceMeasureOrAttribute() {
        String insight = "Insight " + generateHashString();
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(insight, ReportType.PIE_CHART)
                        .setMeasureBucket(
                                singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT))))
                        .setCategoryBucket(
                                singletonList(CategoryBucket.createCategoryBucket(
                                        getAttributeByTitle(ATTR_DEPARTMENT), CategoryBucket.Type.ATTRIBUTE))));

        initAnalysePage().openInsight(insight).replaceAttribute(ATTR_DEPARTMENT, ATTR_FORECAST_CATEGORY)
                .waitForReportComputing();

        ChartReport chartReport = analysisPage.getChartReport();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 1),
                asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"), asList(METRIC_AMOUNT, "$48,932,639.59")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Include"), asList(METRIC_AMOUNT, "$67,692,816.95")));
        assertEquals(parseFilterText(analysisPage.getFilterBuckets()
                .getFilterText(ATTR_FORECAST_CATEGORY)), asList(ATTR_FORECAST_CATEGORY, "All"));
        assertEquals(analysisPage.getFilterBuckets().getFiltersCount(), 1);

        analysisPage.getFilterBuckets().configAttributeFilter(ATTR_FORECAST_CATEGORY, "Include");
        analysisPage.replaceAttribute(ATTR_FORECAST_CATEGORY, ATTR_STAGE_NAME);

        assertEquals(parseFilterText(analysisPage.getFilterBuckets()
                .getFilterText(ATTR_FORECAST_CATEGORY)), asList(ATTR_FORECAST_CATEGORY, "Include"));
        assertEquals(parseFilterText(analysisPage.getFilterBuckets()
                .getFilterText(ATTR_STAGE_NAME)), asList(ATTR_STAGE_NAME, "All"));
    }

    @Test(dependsOnMethods = {"createPieChartWithTwoMeasures"})
    public void reorderMeasures() {
        initAnalysePage().openInsight(INSIGHT_HAS_TWO_MEASURES).waitForReportComputing();

        ChartReport chartReport = analysisPage.getChartReport();
        assertEquals(chartReport.checkColorColumn(0, 0), ColorPaletteRequestData.ColorPalette.CYAN.toString());
        assertEquals(chartReport.checkColorColumn(0, 1), ColorPaletteRequestData.ColorPalette.LIME_GREEN.toString());

        analysisPage.reorderMetric(METRIC_AMOUNT, METRIC_AVG_AMOUNT).waitForReportComputing();
        assertEquals(chartReport.checkColorColumn(0, 1), ColorPaletteRequestData.ColorPalette.CYAN.toString());
        if (BrowserUtils.isFirefox()) {
            assertEquals(chartReport.checkColorColumn(0, 0), ColorPaletteRequestData.ColorPalette.LIME_GREEN.toString());
        } else {
            assertEquals(chartReport.checkColorColumn(0, 0), ColorPaletteRequestData.ColorPalette.CYAN_LIME_GREEN.toString());
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void disablePOPOnPieChart() {
        initAnalysePage().addMetric(METRIC_AMOUNT).addDate().changeReportType(ReportType.PIE_CHART)
                .waitForReportComputing();
        DateFilterPickerPanel dateFilterPickerPanel = analysisPage.getFilterBuckets().openDateFilterPickerPanel();

        assertTrue(isElementDisabled(dateFilterPickerPanel.getDateDatasetSelect().getDropdownButton()),
                "PoP date dimension should be disabled");
        assertEquals(dateFilterPickerPanel.getWarningUnsupportedMessage(),
                "Current visualization type doesn't support comparing. To compare, switch to another insight.");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkLegendPosition() {
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_AMOUNT)
                .addMetric(METRIC_AVG_AMOUNT).addMetric(METRIC_AMOUNT_BOP)
                .waitForReportComputing();
        assertEquals(analysisPage.getMetricsBucket().getItemNames(),
                asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT, METRIC_AMOUNT_BOP));
        assertEquals(analysisPage.getChartReport().getLegends(),
                asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT, METRIC_AMOUNT_BOP));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void showOrHideLegendsWhenClicking() {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_TWO_MEASURES, ReportType.PIE_CHART)
                        .setMeasureBucket(
                                asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)),
                                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AVG_AMOUNT)))));

        ChartReport chartReport = initAnalysePage().openInsight(INSIGHT_HAS_TWO_MEASURES)
                .waitForReportComputing().getChartReport();
        chartReport.clickLegend(METRIC_AMOUNT);
        assertEquals(chartReport.getTrackersCount(), 1);

        chartReport.clickLegend(METRIC_AMOUNT);
        assertEquals(chartReport.getTrackersCount(), 2);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void undoOrRedoChartReport() {
        ChartReport chartReport = initAnalysePage().changeReportType(ReportType.PIE_CHART).addMetric(METRIC_AMOUNT)
                .addAttribute(ATTR_FORECAST_CATEGORY).waitForReportComputing().getChartReport();
        analysisPage.undo().waitForReportComputing();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                singletonList(asList(METRIC_AMOUNT, "$116,625,456.54")));

        analysisPage.redo().waitForReportComputing();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Include"), asList(METRIC_AMOUNT, "$67,692,816.95")));

        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 1),
                asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"), asList(METRIC_AMOUNT, "$48,932,639.59")));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkTooltipWhenAddingFilterChartReport() {
        ChartReport chartReport = initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_AMOUNT)
                .addDate().waitForReportComputing().getChartReport();
        FiltersBucket filtersBucket = analysisPage.getFilterBuckets().configDateFilter("01/01/2011", "01/01/2019");
        analysisPage.waitForReportComputing();

        assertEquals(chartReport.getTrackersCount(), 3);

        DateDimensionSelect dateDatasetSelect = filtersBucket
                .openDatePanelOfFilter(filtersBucket.getDateFilter())
                .getDateDatasetSelect();
        assertEquals(getTooltipFromElement(dateDatasetSelect.getRoot(), browser),
                "To change the date you’re filtering by, " +
                        "choose a different date in bucket where you are using date filter.");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void switchOtherChartToPieChartAndViceVersa() {
        String insight = "Insight " + generateHashString();
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(insight, ReportType.COLUMN_CHART)
                        .setMeasureBucket(
                                asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)),
                                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AVG_AMOUNT))))
                        .setCategoryBucket(
                                asList(CategoryBucket.createCategoryBucket(
                                        getAttributeByTitle(ATTR_DEPARTMENT), CategoryBucket.Type.ATTRIBUTE),
                                        CategoryBucket.createCategoryBucket(
                                                getAttributeByTitle(ATTR_FORECAST_CATEGORY), CategoryBucket.Type.ATTRIBUTE))));

        initAnalysePage().openInsight(insight).addFilter(ATTR_DEPARTMENT).changeReportType(ReportType.PIE_CHART)
                .waitForReportComputing();
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), singletonList(METRIC_AMOUNT));
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), singletonList(ATTR_DEPARTMENT));
        assertEquals(analysisPage.getFilterBuckets().getFiltersCount(), 1);
        assertEquals(parseFilterText(analysisPage.getFilterBuckets()
                .getFilterText(ATTR_DEPARTMENT)), asList(ATTR_DEPARTMENT, "All"));

        insight = "Insight " + generateHashString();
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(insight, ReportType.PIE_CHART)
                        .setMeasureBucket(
                                singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT))))
                        .setCategoryBucket(
                                singletonList(CategoryBucket.createCategoryBucket(
                                        getAttributeByTitle(ATTR_DEPARTMENT), CategoryBucket.Type.ATTRIBUTE))));

        initAnalysePage().openInsight(insight).changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), singletonList(METRIC_AMOUNT));
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), singletonList(ATTR_DEPARTMENT));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void showErrorMessageWhenPieChartHasMoreTwentyValues() {
        initAnalysePage().changeReportType(ReportType.PIE_CHART).addMetric(METRIC_AMOUNT).addAttribute(ATTR_ACCOUNT)
                .saveInsight(INSIGHT_HAS_MORE_TWENTY_VALUES).waitForReportComputing();
        assertThat(analysisPage.getMainEditor().getCanvasMessage(),
                containsString("TOO MANY DATA POINTS TO DISPLAY\nAdd a filter, or switch to table view."));

        PivotTableReport tableReport = analysisPage.changeReportType(ReportType.TABLE).waitForReportComputing()
                .getPivotTableReport();
        assertEquals(tableReport.getHeaders(), asList(ATTR_ACCOUNT, METRIC_AMOUNT));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void showErrorMessageWhenChartHasNegativeValue() {
        final String metricNegativeValue = "Min of Amount";
        createMetric(metricNegativeValue, format("SELECT MIN([%s])",
                getMdService().getObjUri(getProject(), Fact.class, title("Amount"))), "#,##0.00");
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_NEGATIVE_VALUE, ReportType.PIE_CHART)
                        .setMeasureBucket(
                                singletonList(
                                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metricNegativeValue)))));

        initAnalysePage().openInsight(INSIGHT_HAS_NEGATIVE_VALUE).waitForReportComputing();
        assertThat(analysisPage.getMainEditor().getCanvasMessage(),
                containsString("Negative values cannot be rendered by current visualization." +
                        "\nSwitch to column chart to display."));
        ChartReport chartReport = analysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing()
                .getChartReport();
        assertEquals(chartReport.getDataLabels(), singletonList("-400,000.00"));
    }

    @Test(dependsOnMethods = {"showErrorMessageWhenPieChartHasMoreTwentyValues",
            "showErrorMessageWhenChartHasNegativeValue"})
    public void showErrorMessageIntoKPIDashboard() {
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage().addDashboard()
                .addInsight(INSIGHT_HAS_MORE_TWENTY_VALUES).selectDateFilterByName("All time").waitForWidgetsLoading();
        assertThat(indigoDashboardsPage.waitForWidgetsLoading().getFirstWidget(Insight.class).getRoot().getText(),
                containsString("TOO MANY DATA POINTS TO DISPLAY\n" +
                        "Try applying one or more filters to your dashboard."));

        initIndigoDashboardsPage().addDashboard().addInsight(INSIGHT_HAS_NEGATIVE_VALUE)
                .selectDateFilterByName("All time");
        assertThat(indigoDashboardsPage.waitForWidgetsLoading().getFirstWidget(Insight.class).getRoot().getText(),
                containsString("SORRY, WE CAN'T DISPLAY THIS INSIGHT\nContact your administrator."));
    }

    @Test(dependsOnMethods = {"createPieChartWithAMeasureAndAnAttribute"})
    public void openAsReportOnPieChart() {
        initAnalysePage().openInsight(INSIGHT_HAS_MEASURE_AND_ATTRIBUTE).waitForReportComputing()
                .clickOptionsButton().exportReport();

        BrowserUtils.switchToLastTab(browser);
        try {
            waitForAnalysisPageLoaded(browser);
            assertTrue(reportPage.getChartReport().isChart(), "Pie Chart should be rendered");
            assertEquals(reportPage.getHowButton(), "How (1)");
            assertEquals(reportPage.getWhatButton(), "What (1)");
        } finally {
            browser.close();
            BrowserUtils.switchToFirstTab(browser);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void openAsReportOnPieChartHasFactIntoMeasure() {
        initAnalysePage().changeReportType(ReportType.PIE_CHART).addMetric(ATTR_FORECAST_CATEGORY, FieldType.ATTRIBUTE)
                .addAttribute(ATTR_FORECAST_CATEGORY).waitForReportComputing().clickOptionsButton().exportReport();

        BrowserUtils.switchToLastTab(browser);
        try {
            waitForAnalysisPageLoaded(browser);
            assertTrue(reportPage.getChartReport().isChart(), "Pie Chart should be rendered");
            assertEquals(reportPage.getHowButton(), "How (1)");
            assertEquals(reportPage.getWhatButton(), "What (1)");
        } finally {
            browser.close();
            BrowserUtils.switchToFirstTab(browser);
        }
    }
}
