package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.md.Fact;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.OptionalStacking;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.CanvasSelect.DataLabel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricsBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.StacksBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.ConfigurationPanelBucket.Items.CANVAS;
import static com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.ConfigurationPanelBucket.Items.Y_AXIS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_FORECAST_CATEGORY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_REGION;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_WON;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_BEST_CASE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_CLOSE_EOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT_BOP;
import static com.gooddata.qa.utils.http.ColorPaletteRequestData.ColorPalette;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertFalse;

public class OptionalStackingTest extends AbstractAnalyseTest {

    private static final String INSIGHT_HAS_A_MEASURE_AND_SOME_ATTRIBUTES = "A measure and attributes";
    private static final String INSIGHT_HAS_SOME_MEASURES_AND_SOME_ATTRIBUTES = "Some measures and attributes";
    private static final String INSIGHT_HAS_TWO_MEASURES_AND_SOME_ATTRIBUTES = "Two measures and attributes";
    private static final String INSIGHT_HAS_SOME_ATTRIBUTES_AND_A_STACK = "Some attributes and stack";
    private static final String INSIGHT_HAS_SOME_METRICS = "Some metrics";
    private static final String INSIGHT_HAS_A_MEASURE_AND_AN_ATTRIBUTE_AND_A_STACK = "Measure, attribute, stack";
    private static final List<ReportType> REPORT_TYPES = asList(ReportType.COLUMN_CHART, ReportType.BAR_CHART);
    private IndigoRestRequest indigoRestRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "SD-155 - Optional stacking for AD";
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
    public void checkStackToPercentForInsightHasAnAttribute() {
        createInsightHasAMeasureAndAnAttributeAndAStack(INSIGHT_HAS_A_MEASURE_AND_AN_ATTRIBUTE_AND_A_STACK,
                METRIC_AMOUNT, ATTR_FORECAST_CATEGORY, ATTR_DEPARTMENT, ReportType.COLUMN_CHART);
        initAnalysePage().openInsight(INSIGHT_HAS_A_MEASURE_AND_AN_ATTRIBUTE_AND_A_STACK).waitForReportComputing();
        ChartReport chartReport = analysisPage.getChartReport();
        analysisPage.getStacksBucket().checkOption(OptionalStacking.PERCENT);

        for (ReportType reportType : REPORT_TYPES) {
            analysisPage.changeReportType(reportType).waitForReportComputing();
            assertThat(chartReport.getDataLabels(), hasItems("68.59%", "69.2%", "31.41%", "30.8%"));
            assertEquals(chartReport.getXaxisTitle(), ATTR_FORECAST_CATEGORY);
        }

        analysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"), asList("Direct Sales", "68.59%")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 1),
                asList(asList(ATTR_FORECAST_CATEGORY, "Include"), asList("Direct Sales", "69.2%")));
        assertEquals(chartReport.getXaxisLabels(), asList("Exclude", "Include"));

        analysisPage.changeReportType(ReportType.BAR_CHART).waitForReportComputing();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Include"), asList("Direct Sales", "69.2%")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 1),
                asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"), asList("Direct Sales", "68.59%")));
        assertEquals(chartReport.getXaxisLabels(), asList("Include", "Exclude"));
    }

    @Test(dependsOnGroups = {"createProject"}, dependsOnMethods = "checkStackToPercentForInsightHasAnAttribute")
    public void uncheckStackToPercentForInsightHasAnAttribute() {
        initAnalysePage().openInsight(INSIGHT_HAS_A_MEASURE_AND_AN_ATTRIBUTE_AND_A_STACK).waitForReportComputing();
        ChartReport chartReport = analysisPage.getChartReport();
        analysisPage.getStacksBucket().uncheckOption(OptionalStacking.PERCENT);

        for (ReportType reportType : REPORT_TYPES) {
            analysisPage.changeReportType(reportType).waitForReportComputing();
            assertThat(chartReport.getDataLabels(),
                    hasItems("$33,562,482.51", "$46,843,842.45", "$15,370,157.08", "$20,848,974.50"));
            assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.CYAN.toString());
            assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.LIME_GREEN.toString());
            assertNotEquals(chartReport.getHeightColumn(0, 0), chartReport.getHeightColumn(0, 1),
                    "Each column should has a different height.");
            assertEquals(chartReport.getXaxisTitle(), ATTR_FORECAST_CATEGORY);
        }
        analysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"), asList("Direct Sales", "$33,562,482.51")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 1),
                asList(asList(ATTR_FORECAST_CATEGORY, "Include"), asList("Direct Sales", "$46,843,842.45")));
        assertEquals(chartReport.getTotalsStackedColumn(), asList("$48,932,639.59", "$67,692,816.95"));
        assertEquals(chartReport.getXaxisLabels(), asList("Exclude", "Include"));

        analysisPage.changeReportType(ReportType.BAR_CHART).waitForReportComputing();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Include"), asList("Direct Sales", "$46,843,842.45")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 1),
                asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"), asList("Direct Sales", "$33,562,482.51")));
        assertEquals(chartReport.getXaxisLabels(), asList("Include", "Exclude"));
        assertEquals(chartReport.getXLocationColumn(0, 0), chartReport.getXLocationColumn(1, 0));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void stackForInsightHasSomeAttributes() {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_A_MEASURE_AND_SOME_ATTRIBUTES, ReportType.COLUMN_CHART)
                        .setMeasureBucket(
                                singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT))))
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                                        CategoryBucket.Type.ATTRIBUTE),
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                        CategoryBucket.Type.ATTRIBUTE))));

        initAnalysePage().openInsight(INSIGHT_HAS_A_MEASURE_AND_SOME_ATTRIBUTES).waitForReportComputing();
        ChartReport chartReport = analysisPage.getChartReport();

        for (ReportType reportType : REPORT_TYPES) {
            analysisPage.changeReportType(reportType).waitForReportComputing();
            assertThat(chartReport.getDataLabels(),
                    hasItems("$33,562,482.51", "$15,370,157.08", "$46,843,842.45", "$20,848,974.50"));
        }

        analysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"), asList(ATTR_DEPARTMENT, "Direct Sales"),
                        asList(METRIC_AMOUNT, "$33,562,482.51")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 1),
                asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"), asList(ATTR_DEPARTMENT, "Inside Sales"),
                        asList(METRIC_AMOUNT, "$15,370,157.08")));
        assertEquals(chartReport.getXaxisLabels(),
                asList("Direct Sales", "Exclude", "Inside Sales",  "Direct Sales", "Include", "Inside Sales"));
        assertNotEquals(chartReport.getXLocationColumn(0, 0), chartReport.getXLocationColumn(0, 1));

        analysisPage.changeReportType(ReportType.BAR_CHART).waitForReportComputing();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Include"), asList(ATTR_DEPARTMENT, "Direct Sales"),
                        asList(METRIC_AMOUNT, "$46,843,842.45")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 1),
                asList(asList(ATTR_FORECAST_CATEGORY, "Include"), asList(ATTR_DEPARTMENT, "Inside Sales"),
                        asList(METRIC_AMOUNT, "$33,562,482.51")));
        assertEquals(chartReport.getXaxisLabels(),
                asList("Direct Sales", "Include", "Inside Sales",  "Direct Sales", "Exclude", "Inside Sales"));
        assertNotEquals(chartReport.getXLocationColumn(0, 0), chartReport.getXLocationColumn(0, 1));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void stackForInsightHasStackSameAsParentAttribute() {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_SOME_ATTRIBUTES_AND_A_STACK, ReportType.COLUMN_CHART)
                        .setMeasureBucket(
                                singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT))))
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                                        CategoryBucket.Type.ATTRIBUTE),
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                        CategoryBucket.Type.ATTRIBUTE),
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                                        CategoryBucket.Type.STACK))));

        initAnalysePage().openInsight(INSIGHT_HAS_SOME_ATTRIBUTES_AND_A_STACK).waitForReportComputing();
        ChartReport chartReport = analysisPage.getChartReport();

        for (ReportType reportType : REPORT_TYPES) {
            analysisPage.changeReportType(reportType).waitForReportComputing();
            assertThat(chartReport.getDataLabels(),
                    hasItems("$33,562,482.51", "$15,370,157.08", "$46,843,842.45", "$20,848,974.50"));
            assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.CYAN.toString());
            assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.LIME_GREEN.toString());
            assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                    asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"), asList(ATTR_DEPARTMENT, "Direct Sales"),
                            asList("Exclude", "$33,562,482.51")));
            assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 1),
                    asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"), asList(ATTR_DEPARTMENT, "Inside Sales"),
                            asList("Exclude", "$15,370,157.08")));
        }

        analysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();
        assertEquals(chartReport.getXaxisLabels(),
                asList("Direct Sales", "Exclude", "Inside Sales",  "Direct Sales", "Include", "Inside Sales"));
        assertNotEquals(chartReport.getXLocationColumn(0, 0), chartReport.getXLocationColumn(1, 0));

        analysisPage.changeReportType(ReportType.BAR_CHART).waitForReportComputing();
        assertEquals(chartReport.getXaxisLabels(),
                asList("Direct Sales", "Include", "Inside Sales",  "Direct Sales", "Exclude", "Inside Sales"));
        assertNotEquals(chartReport.getXLocationColumn(0, 0), chartReport.getXLocationColumn(1, 0));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void stackForInsightHasStackSameAsChildAttribute() {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_SOME_ATTRIBUTES_AND_A_STACK, ReportType.COLUMN_CHART)
                        .setMeasureBucket(
                                singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT))))
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                                        CategoryBucket.Type.ATTRIBUTE),
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                        CategoryBucket.Type.ATTRIBUTE),
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                        CategoryBucket.Type.STACK))));

        initAnalysePage().openInsight(INSIGHT_HAS_SOME_ATTRIBUTES_AND_A_STACK).waitForReportComputing();
        ChartReport chartReport = analysisPage.getChartReport();

        for (ReportType reportType : REPORT_TYPES) {
            analysisPage.changeReportType(reportType).waitForReportComputing();
            assertThat(chartReport.getDataLabels(),
                    hasItems("$33,562,482.51", "$15,370,157.08", "$46,843,842.45", "$20,848,974.50"));
            assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.CYAN.toString());
            assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.LIME_GREEN.toString());
        }

        analysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"), asList(ATTR_DEPARTMENT, "Direct Sales"),
                        asList("Direct Sales", "$33,562,482.51")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 1),
                asList(asList(ATTR_FORECAST_CATEGORY, "Include"), asList(ATTR_DEPARTMENT, "Direct Sales"),
                        asList("Direct Sales", "$46,843,842.45")));
        assertEquals(chartReport.getXaxisLabels(),
                asList("Direct Sales", "Exclude", "Inside Sales",  "Direct Sales", "Include", "Inside Sales"));
        assertNotEquals(chartReport.getXLocationColumn(0, 0), chartReport.getXLocationColumn(0, 1));

        analysisPage.changeReportType(ReportType.BAR_CHART).waitForReportComputing();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Include"), asList(ATTR_DEPARTMENT, "Direct Sales"),
                        asList("Direct Sales", "$46,843,842.45")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 1),
                asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"), asList(ATTR_DEPARTMENT, "Direct Sales"),
                        asList("Direct Sales", "$33,562,482.51")));
        assertEquals(chartReport.getXaxisLabels(),
                asList("Direct Sales", "Include", "Inside Sales",  "Direct Sales", "Exclude", "Inside Sales"));
        assertNotEquals(chartReport.getXLocationColumn(0, 0), chartReport.getXLocationColumn(0, 1));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void stackForInsightHasStackDifferentWithAttribute() {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_SOME_ATTRIBUTES_AND_A_STACK, ReportType.COLUMN_CHART)
                        .setMeasureBucket(
                                singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT))))
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                                        CategoryBucket.Type.ATTRIBUTE),
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                        CategoryBucket.Type.ATTRIBUTE),
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_REGION),
                                        CategoryBucket.Type.STACK))));

        initAnalysePage().openInsight(INSIGHT_HAS_SOME_ATTRIBUTES_AND_A_STACK).waitForReportComputing();
        ChartReport chartReport = analysisPage.getChartReport();

        for (ReportType reportType : REPORT_TYPES) {
            analysisPage.changeReportType(reportType).waitForReportComputing();
            assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.CYAN.toString());
            assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.LIME_GREEN.toString());
        }

        analysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"), asList(ATTR_DEPARTMENT, "Direct Sales"),
                        asList("East Coast", "$11,960,614.36")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"), asList(ATTR_DEPARTMENT, "Direct Sales"),
                        asList("West Coast", "$21,601,868.15")));
        assertEquals(chartReport.getXaxisLabels(),
                asList("Direct Sales", "Exclude", "Inside Sales",  "Direct Sales", "Include", "Inside Sales"));
        assertEquals(chartReport.getTotalsStackedColumn(),
                asList("$33,562,482.51", "$15,370,157.08", "$46,843,842.45", "$20,848,974.50"));

        analysisPage.changeReportType(ReportType.BAR_CHART).waitForReportComputing();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Include"), asList(ATTR_DEPARTMENT, "Direct Sales"),
                        asList("East Coast", "$10,018,081.10")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Include"), asList(ATTR_DEPARTMENT, "Direct Sales"),
                        asList("West Coast", "$36,825,761.35")));
        assertEquals(chartReport.getXaxisLabels(),
                asList("Direct Sales", "Include", "Inside Sales",  "Direct Sales", "Exclude", "Inside Sales"));
        assertEquals(chartReport.getXLocationColumn(0, 0), chartReport.getXLocationColumn(1, 0));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkStackMeasuresAndUncheckStackPercentWithAnAttribute() {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_SOME_METRICS, ReportType.COLUMN_CHART)
                        .setMeasureBucket(
                                asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)),
                                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_WON))))
                        .setCategoryBucket(singletonList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                                        CategoryBucket.Type.ATTRIBUTE))));

        initAnalysePage().openInsight(INSIGHT_HAS_SOME_METRICS).waitForReportComputing();
        ChartReport chartReport = analysisPage.getChartReport();

        analysisPage.getStacksBucket().checkOption(OptionalStacking.MEASURES).uncheckOption(OptionalStacking.PERCENT);

        for (ReportType reportType : REPORT_TYPES) {
            analysisPage.changeReportType(reportType).waitForReportComputing();
            assertThat(chartReport.getDataLabels(),
                    hasItems("$48,932,639.59", "$67,692,816.95", "19,616,755", "18,693,998"));
            assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.CYAN.toString());
            assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.LIME_GREEN.toString());
        }

        analysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"), asList(METRIC_AMOUNT, "$48,932,639.59")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"), asList(METRIC_WON, "19,616,755")));
        assertEquals(chartReport.getTotalsStackedColumn(), asList("$68,549,394.96", "$86,386,815.03"));
        assertEquals(chartReport.getXaxisLabels(), asList("Exclude", "Include"));

        analysisPage.changeReportType(ReportType.BAR_CHART).waitForReportComputing();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Include"), asList(METRIC_AMOUNT, "$67,692,816.95")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Include"), asList(METRIC_WON, "18,693,998")));
        assertEquals(chartReport.getXaxisLabels(), asList("Include", "Exclude"));
        assertEquals(chartReport.getXLocationColumn(0, 0), chartReport.getXLocationColumn(1, 0));
    }

    @Test(dependsOnGroups = {"createProject"},
        dependsOnMethods = "checkStackMeasuresAndUncheckStackPercentWithAnAttribute")
    public void checkStackMeasuresAndStackPercentWithAnAttributeWithAnAttribute() {
        initAnalysePage().openInsight(INSIGHT_HAS_SOME_METRICS).waitForReportComputing();
        ChartReport chartReport = analysisPage.getChartReport();

        analysisPage.getStacksBucket().checkOption(OptionalStacking.MEASURES).checkOption(OptionalStacking.PERCENT);

        for (ReportType reportType : REPORT_TYPES) {
            analysisPage.changeReportType(reportType).waitForReportComputing();
            assertThat(chartReport.getDataLabels(), hasItems("71.38%", "78.36%", "28.62%", "21.64%"));
            assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.CYAN.toString());
            assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.LIME_GREEN.toString());
        }
        analysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"), asList(METRIC_AMOUNT, "71.38%")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"), asList(METRIC_WON, "28.62%")));
        assertEquals(chartReport.getXaxisLabels(), asList("Exclude", "Include"));

        analysisPage.changeReportType(ReportType.BAR_CHART).waitForReportComputing();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Include"), asList(METRIC_AMOUNT, "78.36%")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Include"), asList(METRIC_WON, "21.64%")));
        assertEquals(chartReport.getXaxisLabels(), asList("Include", "Exclude"));
        assertEquals(chartReport.getXLocationColumn(0, 0), chartReport.getXLocationColumn(1, 0));
    }

    @Test(dependsOnGroups = {"createProject"},
        dependsOnMethods = "checkStackMeasuresAndUncheckStackPercentWithAnAttribute")
    public void uncheckStackMeasuresAndStackPercentWithAnAttribute() {
        initAnalysePage().openInsight(INSIGHT_HAS_SOME_METRICS).waitForReportComputing();
        ChartReport chartReport = analysisPage.getChartReport();
        analysisPage.getStacksBucket().uncheckOption(OptionalStacking.MEASURES);

        for (ReportType reportType : REPORT_TYPES) {
            analysisPage.changeReportType(reportType).waitForReportComputing();
            assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.CYAN.toString());
            assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.LIME_GREEN.toString());
        }

        analysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"), asList(METRIC_AMOUNT, "$48,932,639.59")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"), asList(METRIC_WON, "19,616,755")));
        assertEquals(chartReport.getXaxisLabels(), asList("Exclude", "Include"));
        assertNotEquals(chartReport.getXLocationColumn(0, 0), chartReport.getXLocationColumn(0, 1));

        analysisPage.changeReportType(ReportType.BAR_CHART).waitForReportComputing();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Include"), asList(METRIC_AMOUNT, "$67,692,816.95")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Include"), asList(METRIC_WON, "18,693,998")));
        assertEquals(chartReport.getXaxisLabels(), asList("Include", "Exclude"));
        assertNotEquals(chartReport.getXLocationColumn(0, 0), chartReport.getXLocationColumn(0, 1));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkStackMeasuresAndUncheckStackPercentWithSomeAttributes() {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_TWO_MEASURES_AND_SOME_ATTRIBUTES, ReportType.COLUMN_CHART)
                        .setMeasureBucket(
                                asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)),
                                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_BEST_CASE))))
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                                        CategoryBucket.Type.ATTRIBUTE),
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                        CategoryBucket.Type.ATTRIBUTE))));

        initAnalysePage().openInsight(INSIGHT_HAS_TWO_MEASURES_AND_SOME_ATTRIBUTES).waitForReportComputing();
        ChartReport chartReport = analysisPage.getChartReport();
        analysisPage.getStacksBucket().checkOption(OptionalStacking.MEASURES).uncheckOption(OptionalStacking.PERCENT);

        for (ReportType reportType : REPORT_TYPES) {
            analysisPage.changeReportType(reportType).waitForReportComputing();
            assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.CYAN.toString());
            assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.LIME_GREEN.toString());
        }

        analysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"), asList(ATTR_DEPARTMENT, "Direct Sales"),
                        asList(METRIC_AMOUNT, "$33,562,482.51")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"), asList(ATTR_DEPARTMENT, "Direct Sales"),
                        asList(METRIC_BEST_CASE, "8,403,394")));
        assertEquals(chartReport.getXaxisLabels(),
                asList("Direct Sales", "Exclude", "Inside Sales",  "Direct Sales", "Include", "Inside Sales"));
        assertEquals(chartReport.getTotalsStackedColumn(),
                asList("$41,965,876.85", "$18,870,335.32", "$67,301,832.18", "$24,331,544.12"));

        analysisPage.changeReportType(ReportType.BAR_CHART).waitForReportComputing();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Include"), asList(ATTR_DEPARTMENT, "Direct Sales"),
                        asList(METRIC_AMOUNT, "$46,843,842.45")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Include"), asList(ATTR_DEPARTMENT, "Direct Sales"),
                        asList(METRIC_BEST_CASE, "20,457,990")));
        assertEquals(chartReport.getXaxisLabels(),
                asList("Direct Sales", "Include", "Inside Sales",  "Direct Sales", "Exclude", "Inside Sales"));
        assertEquals(chartReport.getXLocationColumn(0, 0), chartReport.getXLocationColumn(1, 0));
    }

    @Test(dependsOnGroups = {"createProject"},
        dependsOnMethods = "checkStackMeasuresAndUncheckStackPercentWithSomeAttributes")
    public void checkStackMeasuresAndStackPercentWithSomeAttributes() {
        initAnalysePage().openInsight(INSIGHT_HAS_TWO_MEASURES_AND_SOME_ATTRIBUTES).waitForReportComputing();
        ChartReport chartReport = analysisPage.getChartReport();
        analysisPage.getStacksBucket().checkOption(OptionalStacking.MEASURES).checkOption(OptionalStacking.PERCENT);

        for (ReportType reportType : REPORT_TYPES) {
            analysisPage.changeReportType(reportType).waitForReportComputing();
            assertThat(chartReport.getDataLabels(), hasItems("79.98%", "20.02%", "81.45%", "81.45%"));
            assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.CYAN.toString());
            assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.LIME_GREEN.toString());
        }

        analysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"), asList(ATTR_DEPARTMENT, "Direct Sales"),
                        asList(METRIC_AMOUNT, "79.98%")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"), asList(ATTR_DEPARTMENT, "Direct Sales"),
                        asList(METRIC_BEST_CASE, "20.02%")));
        assertEquals(chartReport.getXaxisLabels(),
                asList("Direct Sales", "Exclude", "Inside Sales",  "Direct Sales", "Include", "Inside Sales"));

        analysisPage.changeReportType(ReportType.BAR_CHART).waitForReportComputing();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Include"), asList(ATTR_DEPARTMENT, "Direct Sales"),
                        asList(METRIC_AMOUNT, "69.6%")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Include"), asList(ATTR_DEPARTMENT, "Direct Sales"),
                        asList(METRIC_BEST_CASE, "30.4%")));
        assertEquals(chartReport.getXaxisLabels(),
                asList("Direct Sales", "Include", "Inside Sales",  "Direct Sales", "Exclude", "Inside Sales"));
    }

    @Test(dependsOnGroups = {"createProject"},
        dependsOnMethods = "checkStackMeasuresAndUncheckStackPercentWithSomeAttributes")
    public void uncheckStackMeasuresAndStackPercentWithSomeAttributes() {
        initAnalysePage().openInsight(INSIGHT_HAS_TWO_MEASURES_AND_SOME_ATTRIBUTES).waitForReportComputing();
        ChartReport chartReport = analysisPage.getChartReport();
        analysisPage.getStacksBucket().uncheckOption(OptionalStacking.MEASURES);

        for (ReportType reportType : REPORT_TYPES) {
            analysisPage.changeReportType(reportType).waitForReportComputing();
            assertEquals(chartReport.checkColorColumn(0, 0), ColorPalette.CYAN.toString());
            assertEquals(chartReport.checkColorColumn(1, 0), ColorPalette.LIME_GREEN.toString());
        }

        analysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"), asList(ATTR_DEPARTMENT, "Direct Sales"),
                        asList(METRIC_AMOUNT, "$33,562,482.51")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"), asList(ATTR_DEPARTMENT, "Direct Sales"),
                        asList(METRIC_BEST_CASE, "8,403,394")));
        assertEquals(chartReport.getXaxisLabels(),
                asList("Direct Sales", "Exclude", "Inside Sales",  "Direct Sales", "Include", "Inside Sales"));

        analysisPage.changeReportType(ReportType.BAR_CHART).waitForReportComputing();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Include"), asList(ATTR_DEPARTMENT, "Direct Sales"),
                        asList(METRIC_AMOUNT, "$46,843,842.45")));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Include"), asList(ATTR_DEPARTMENT, "Direct Sales"),
                        asList(METRIC_BEST_CASE, "20,457,990")));
        assertEquals(chartReport.getXaxisLabels(),
                asList("Direct Sales", "Include", "Inside Sales",  "Direct Sales", "Exclude", "Inside Sales"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void disableStackingOptionalWithSomeAttributesAndAMeasure() {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_A_MEASURE_AND_SOME_ATTRIBUTES, ReportType.COLUMN_CHART)
                        .setMeasureBucket(
                                singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT))))
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                                        CategoryBucket.Type.ATTRIBUTE),
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                        CategoryBucket.Type.ATTRIBUTE))));

        initAnalysePage().openInsight(INSIGHT_HAS_A_MEASURE_AND_SOME_ATTRIBUTES).waitForReportComputing();
        StacksBucket stacksBucket = analysisPage.getStacksBucket();

        assertFalse(stacksBucket.isOptionCheckPresent(OptionalStacking.MEASURES), "Stacking Optional should be disabled when " +
                "Insight has two viewed by attributes and one measure");

        createInsightHasAMeasureAndAnAttributeAndAStack(INSIGHT_HAS_A_MEASURE_AND_AN_ATTRIBUTE_AND_A_STACK,
                METRIC_AMOUNT, ATTR_FORECAST_CATEGORY, ATTR_DEPARTMENT, ReportType.COLUMN_CHART);

        initAnalysePage().openInsight(INSIGHT_HAS_A_MEASURE_AND_AN_ATTRIBUTE_AND_A_STACK).waitForReportComputing();
        assertFalse(stacksBucket.isOptionCheckPresent(OptionalStacking.MEASURES), "Stacking Optional should be disabled when " +
                "Insight has two viewed by attributes and one measure");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkDualAxisChartWithSomeMeasures() {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_SOME_MEASURES_AND_SOME_ATTRIBUTES, ReportType.COLUMN_CHART)
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

        MetricsBucket metricsBucket = initAnalysePage().openInsight(INSIGHT_HAS_SOME_MEASURES_AND_SOME_ATTRIBUTES)
                .waitForReportComputing().getMetricsBucket();
        metricsBucket.getMetricConfiguration(METRIC_AMOUNT).expandConfiguration().checkShowOnSecondaryAxis();
        metricsBucket.getMetricConfiguration(METRIC_BEST_CASE).expandConfiguration().checkShowOnSecondaryAxis();

        ChartReport chartReport = analysisPage.getChartReport();
        assertEquals(chartReport.getXaxisLabels(),
                asList("Direct Sales", "Exclude", "Inside Sales",  "Direct Sales", "Include", "Inside Sales"));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_FORECAST_CATEGORY, "Exclude"), asList(ATTR_DEPARTMENT, "Direct Sales"),
                        asList(METRIC_AMOUNT, "$33,562,482.51")));
        assertEquals(chartReport.getLegends(), asList(METRIC_WON, METRIC_AMOUNT_BOP, METRIC_AMOUNT, METRIC_BEST_CASE));
        assertEquals(chartReport.getLegendIndicators(), asList("Left:", "Right:"));
    }

    @Test(dependsOnGroups = {"createProject"}, dependsOnMethods = "checkDualAxisChartWithSomeMeasures")
    public void checkStackToMeasuresForDualAxisChartWithSomeMeasures() {
        MetricsBucket metricsBucket = initAnalysePage().openInsight(INSIGHT_HAS_SOME_MEASURES_AND_SOME_ATTRIBUTES)
                .waitForReportComputing().getMetricsBucket();
        metricsBucket.getMetricConfiguration(METRIC_AMOUNT).expandConfiguration().checkShowOnSecondaryAxis();
        metricsBucket.getMetricConfiguration(METRIC_BEST_CASE).expandConfiguration().checkShowOnSecondaryAxis();
        analysisPage.getStacksBucket().checkOption(OptionalStacking.MEASURES);

        ChartReport chartReport = analysisPage.getChartReport();
        assertEquals(chartReport.getXLocationColumn(0, 0), chartReport.getXLocationColumn(1, 0));
        assertEquals(chartReport.getXLocationColumn(2, 0), chartReport.getXLocationColumn(3, 0));
    }

    @Test(dependsOnGroups = {"createProject"}, dependsOnMethods = "checkDualAxisChartWithSomeMeasures")
    public void checkDataLabelsForDualAxisChartWithSomeMeasures() {
        MetricsBucket metricsBucket = initAnalysePage().openInsight(INSIGHT_HAS_SOME_MEASURES_AND_SOME_ATTRIBUTES)
                .waitForReportComputing().getMetricsBucket();
        metricsBucket.getMetricConfiguration(METRIC_AMOUNT).expandConfiguration().checkShowOnSecondaryAxis();
        metricsBucket.getMetricConfiguration(METRIC_BEST_CASE).expandConfiguration().checkShowOnSecondaryAxis();
        analysisPage.openConfigurationPanelBucket()
                .getItemConfiguration(CANVAS.toString()).expandConfiguration()
                .getCanvasSelect().selectByName(DataLabel.SHOW.toString());

        assertThat(analysisPage.waitForReportComputing().getChartReport().getDataLabels(),
                hasItems("$33,562,482.51", "8,403,394", "12,910,068", "$1,441,087.25"));
    }

    @Test(dependsOnGroups = {"createProject"}, dependsOnMethods = "checkDualAxisChartWithSomeMeasures" )
    public void checkStackToPercentForDualAxisChartWithSomeMeasures() {
        MetricsBucket metricsBucket = initAnalysePage().openInsight(INSIGHT_HAS_SOME_MEASURES_AND_SOME_ATTRIBUTES)
                .waitForReportComputing().getMetricsBucket();
        metricsBucket.getMetricConfiguration(METRIC_AMOUNT).expandConfiguration().checkShowOnSecondaryAxis();
        metricsBucket.getMetricConfiguration(METRIC_BEST_CASE).expandConfiguration().checkShowOnSecondaryAxis();

        analysisPage.getStacksBucket().checkOption(OptionalStacking.MEASURES).checkOption(OptionalStacking.PERCENT);
        ChartReport chartReport = analysisPage.waitForReportComputing().getChartReport();

        assertThat(chartReport.getValuePrimaryYaxis().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList()), hasItems("100%", "0%"));

        assertThat(chartReport.getValueSecondaryYaxis().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList()), hasItems("72M", "0"));
    }

    @Test(dependsOnGroups = {"createProject"}, dependsOnMethods = "checkDualAxisChartWithSomeMeasures")
    public void setMinMaxValueForLeftAxisChart() {
        String leftMin = "2000000";
        String leftMax = "16000000";

        MetricsBucket metricsBucket = initAnalysePage().openInsight(INSIGHT_HAS_SOME_MEASURES_AND_SOME_ATTRIBUTES)
                .waitForReportComputing().getMetricsBucket();
        metricsBucket.getMetricConfiguration(METRIC_AMOUNT).expandConfiguration().checkShowOnSecondaryAxis();
        metricsBucket.getMetricConfiguration(METRIC_BEST_CASE).expandConfiguration().checkShowOnSecondaryAxis();

        analysisPage.getStacksBucket().checkOption(OptionalStacking.MEASURES);
        analysisPage.openConfigurationPanelBucket()
                .getItemConfiguration(Y_AXIS.toString() + " (Left)").expandConfiguration()
                .setMinMaxValueOnAxis(leftMin, leftMax);

        ChartReport chartReport = analysisPage.waitForReportComputing().getChartReport();

        assertThat(chartReport.getValuePrimaryYaxis().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList()), hasItems("1.5M", "16.5M"));
    }

    @Test(dependsOnGroups = {"createProject"}, dependsOnMethods = "checkDualAxisChartWithSomeMeasures")
    public void setMinMaxValueForRightAxisChart() {
        String rightMin = "30000000";
        String rightMax = "50000000";

        MetricsBucket metricsBucket = initAnalysePage().openInsight(INSIGHT_HAS_SOME_MEASURES_AND_SOME_ATTRIBUTES)
                .waitForReportComputing().getMetricsBucket();
        metricsBucket.getMetricConfiguration(METRIC_AMOUNT).expandConfiguration().checkShowOnSecondaryAxis();
        metricsBucket.getMetricConfiguration(METRIC_BEST_CASE).expandConfiguration().checkShowOnSecondaryAxis();

        analysisPage.getStacksBucket().checkOption(OptionalStacking.MEASURES).checkOption(OptionalStacking.PERCENT);
        analysisPage.openConfigurationPanelBucket()
                .getItemConfiguration(Y_AXIS.toString() + " (Right)").expandConfiguration()
                .setMinMaxValueOnAxis(rightMin, rightMax);

        ChartReport chartReport = analysisPage.waitForReportComputing().getChartReport();

        assertThat(chartReport.getValueSecondaryYaxis().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList()), hasItems("30M", "50M"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void stackMeasuresWithPositiveAndNegativeValues() {
        createMetric("Min of Amount", format("SELECT MIN([%s])",
                getMdService().getObjUri(getProject(), Fact.class, title("Amount"))), "#,##0.00");
        final String INSIGHT = "Insight has some positive and negative values";
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT, ReportType.COLUMN_CHART)
                        .setMeasureBucket(
                                asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_CLOSE_EOP)),
                                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_CLOSE_EOP)),
                                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle("Min of Amount")),
                                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle("Min of Amount"))))
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                                        CategoryBucket.Type.ATTRIBUTE),
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                        CategoryBucket.Type.ATTRIBUTE))));
        initAnalysePage().openInsight(INSIGHT).waitForReportComputing().getStacksBucket()
                .checkOption(OptionalStacking.MEASURES);

        ChartReport chartReport = analysisPage.waitForReportComputing().getChartReport();
        assertEquals(chartReport.getXLocationColumn(0, 0), chartReport.getXLocationColumn(1, 0));
        assertEquals(chartReport.getXLocationColumn(0, 0), chartReport.getXLocationColumn(2, 0));
        assertEquals(chartReport.getXLocationColumn(0, 0), chartReport.getXLocationColumn(3, 0));

        assertThat(chartReport.getTotalsStackedColumn(), hasItems("85,226.00", "85,588.00", "83,560.00",
                "83,980.00","-800,000.00", "-319,200.00", "-800,000.00", "-319,200.00"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkSortingBarChartWhenStackingByTotalValue() {
        String insight = "Insight" + generateHashString();
        createMetric("Sum of Amount", format("SELECT SUM([%s])", getFactByTitle("Amount").getUri()), "#,##0.00");
        indigoRestRequest.createInsight(
            new InsightMDConfiguration(insight, ReportType.BAR_CHART)
                .setMeasureBucket(
                    asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)),
                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle("Sum of Amount"))))
                .setCategoryBucket(singletonList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_PRODUCT),
                        CategoryBucket.Type.ATTRIBUTE))));

        ChartReport chartReport = initAnalysePage().openInsight(insight).waitForReportComputing().getChartReport();
        analysisPage.getStacksBucket().checkOption(OptionalStacking.MEASURES);
        analysisPage.waitForReportComputing();

        assertEquals(chartReport.getXaxisLabels(), asList("Explorer", "Educationly", "CompuSci", "WonderKid",
            "PhoenixSoft", "Grammar Plus"));
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
