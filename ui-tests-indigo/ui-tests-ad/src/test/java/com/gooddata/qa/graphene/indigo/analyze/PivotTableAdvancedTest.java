package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.sdk.model.md.Fact;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.indigo.AggregationItem;
import com.gooddata.qa.graphene.fragments.indigo.analyze.PivotAggregationPopup;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributesBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricsBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.StacksBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.PivotTableReport;
import com.gooddata.qa.graphene.fragments.manage.MetricFormatterDialog.Formatter;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.graphene.utils.ElementUtils;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static com.gooddata.sdk.model.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.*;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem; 
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertEquals;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AVG_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;

public class PivotTableAdvancedTest extends AbstractAnalyseTest {

    private static final String INSIGHT_APPLY_NUMBER_FORMAT = "Insight Number Format";
    private static final String INSIGHT_HEAT_MAP = "HeatMap";
    private static final String INSIGHT_TREE_MAP = "TreeMap";
    private static final String INSIGHT_HAS_ATTRIBUTE_AND_MEASURE = "Attribute and measure";
    private static final String INSIGHT_HAS_MEASURE_AND_STACK = "Measure and stack";
    private static final String INSIGHT_HAS_MEASURE_AND_ATTRIBUTE_AND_STACK = "Measure, attribute and stack";
    private static final String INSIGHT_HAS_ATTRIBUTES = "Attributes";
    private static final String INSIGHT_HAS_A_METRIC = "A metric";
    private static final String INSIGHT_HAS_METRICS = "Metrics";
    private static final String INSIGHT_PIVOT_SORTING = "Pivot Table Sorting on Measure";
    private static final List<ReportType> REPORT_TYPES =
            asList(ReportType.COLUMN_CHART, ReportType.SCATTER_PLOT, ReportType.PIE_CHART, ReportType.HEAT_MAP);

    private final String METRIC_DEFAULT = "metricDefault";
    private final String METRIC_PERCENT = "metricPercent";
    private final String METRIC_BARS = "metricBars";
    private final String METRIC_TRUNCATE_NUMBER = "metricTruncateNumber";
    private final String METRIC_COLORS = "metricColors";
    private final String METRIC_COLORS_FORMAT = "metricColorsFormat";
    private final String METRIC_BACKGROUND_FORMAT = "metricBackgroundColorsFormat";
    private final String METRIC_XSS_FORMAT = "metricXSSFormat";
    private final String METRIC_UTF = "metricUTF";
    private final String METRIC_NULL_VALUE = "metricNullValue";
    private final String METRIC_LONG = "metricLong";

    private IndigoRestRequest indigoRestRequest;
    private PivotTableReport pivotTableReport;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "New Table Grid, Paging and Pivoting (Advanced + UI- Layout)";
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createAmountMetric();
        metrics.createAvgAmountMetric();

        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
    }

    @DataProvider(name = "formattingProvider")
    public Object[][] formattingProvider() {
        return new Object[][]{
                {DEFAULT_METRIC_FORMAT, METRIC_DEFAULT},
                {DEFAULT_METRIC_FORMAT + "%", METRIC_PERCENT},
                {Formatter.BARS.toString(), METRIC_BARS},
                {Formatter.TRUNCATE_NUMBERS.toString(), METRIC_TRUNCATE_NUMBER},
                {Formatter.COLORS.toString(), METRIC_COLORS},
                {Formatter.COLORS_FORMAT.toString(), METRIC_COLORS_FORMAT},
                {Formatter.BACKGROUND_COLOR_FORMAT.toString(), METRIC_BACKGROUND_FORMAT},
                {Formatter.XSS.toString(), METRIC_XSS_FORMAT},
                {Formatter.UTF_8.toString(), METRIC_UTF},
                {Formatter.LONG.toString(), METRIC_LONG}
        };
    }    

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "formattingProvider")
    public void prepareMetrics(String formatter, String metric) {
        createMetric(metric, format(
                "SELECT SUM([%s])", getMdService().getObjUri(getProject(), Fact.class, title(METRIC_AMOUNT))), formatter);
    }

    @Test(dependsOnMethods = {"prepareMetrics"})
    public void applyNumberFormatOnMeasures() {
        final String metricExpression = format("SELECT SUM([%s]) WHERE 1 = 0",
                getMdService().getObjUri(getProject(), Fact.class, title("Amount")));
        createMetric(METRIC_NULL_VALUE, metricExpression, "#'##0,00 formatted; [=null] null value!");

        List<List<String>> expectedValues = singletonList(asList("5,617,913,709", "561,791,370,872%", "██████████",
                "$5.6 B"));
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_APPLY_NUMBER_FORMAT, ReportType.TABLE)
                        .setMeasureBucket(
                                asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_DEFAULT)),
                                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_PERCENT)),
                                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_BARS)),
                                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_TRUNCATE_NUMBER)))));
        PivotTableReport pivotTableReport = initAnalysePage().openInsight(INSIGHT_APPLY_NUMBER_FORMAT)
                .waitForReportComputing().getPivotTableReport();
        assertEquals(pivotTableReport.getBodyContent(), expectedValues);

        expectedValues = singletonList(asList("$5,617,913,708.72", "5,617,913,708.72", "5,617,913,708.72",
                "<button>5,617,913,708.72</button>"));
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_APPLY_NUMBER_FORMAT, ReportType.TABLE)
                        .setMeasureBucket(
                                asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_COLORS)),
                                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_COLORS_FORMAT)),
                                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_BACKGROUND_FORMAT)),
                                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_XSS_FORMAT)))));

        pivotTableReport = initAnalysePage().openInsight(INSIGHT_APPLY_NUMBER_FORMAT)
                .waitForReportComputing().getPivotTableReport();
        assertEquals(pivotTableReport.getBodyContent(), expectedValues);

        expectedValues = singletonList(
                asList("5617913'708.72 kiểm tra nghiêm khắc", "null value!", "$5,617,913,709 " +
                        "long format long format long format long format long format long format long format"));
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_APPLY_NUMBER_FORMAT, ReportType.TABLE)
                        .setMeasureBucket(
                                asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_UTF)),
                                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_NULL_VALUE)),
                                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_LONG)))));

        pivotTableReport = initAnalysePage().openInsight(INSIGHT_APPLY_NUMBER_FORMAT)
                .waitForReportComputing().getPivotTableReport();
        assertEquals(pivotTableReport.getBodyContent(), expectedValues);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkReferencePointWithMeasure() {
        createSimpleInsight(INSIGHT_HAS_A_METRIC, METRIC_AMOUNT, ReportType.COLUMN_CHART);
        initAnalysePage().openInsight(INSIGHT_HAS_A_METRIC).waitForReportComputing();
        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        for (ReportType reportType : REPORT_TYPES) {
            analysisPage.changeReportType(reportType);
            assertThat(metricsBucket.getItemNames(), hasItem(METRIC_AMOUNT));

            analysisPage.changeReportType(ReportType.TABLE);
            assertThat(metricsBucket.getItemNames(), hasItem(METRIC_AMOUNT));
        }

    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkReferencePointWithMeasureAndViewBy() {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE, ReportType.COLUMN_CHART)
                        .setMeasureBucket(
                                singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT))))
                        .setCategoryBucket(singletonList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                        CategoryBucket.Type.ATTRIBUTE))));

        initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE).waitForReportComputing();
        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        AttributesBucket attributesBucket = analysisPage.getAttributesBucket();

        for (ReportType reportType : REPORT_TYPES) {
            analysisPage.changeReportType(reportType);
            assertThat(metricsBucket.getItemNames(), hasItem(METRIC_AMOUNT));
            assertThat(attributesBucket.getItemNames(), hasItem(ATTR_DEPARTMENT));

            analysisPage.changeReportType(ReportType.TABLE);
            assertThat(metricsBucket.getItemNames(), hasItem(METRIC_AMOUNT));
            assertThat(attributesBucket.getItemNames(), hasItem(ATTR_DEPARTMENT));
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkReferencePointWithMeasureAndStackBy() {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_MEASURE_AND_STACK, ReportType.COLUMN_CHART)
                        .setMeasureBucket(
                                singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT))))
                        .setCategoryBucket(singletonList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                        CategoryBucket.Type.STACK))));
        initAnalysePage().openInsight(INSIGHT_HAS_MEASURE_AND_STACK).waitForReportComputing();
        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        StacksBucket stacksBucket = analysisPage.getStacksBucket();
        AttributesBucket attributesBucket = analysisPage.getAttributesBucket();

        for (ReportType reportType : REPORT_TYPES) {
            analysisPage.changeReportType(reportType);
            assertThat(metricsBucket.getItemNames(), hasItem(METRIC_AMOUNT));
            if (reportType.equals(ReportType.HEAT_MAP) || reportType.equals(ReportType.COLUMN_CHART)) {
                assertEquals(stacksBucket.getAttributeName(), ATTR_DEPARTMENT);
            } else {
                assertThat(attributesBucket.getItemNames(), hasItem(ATTR_DEPARTMENT));
            }
            analysisPage.changeReportType(ReportType.TABLE);
            AttributesBucket attributesColumnsBucket = analysisPage.getAttributesColumnsBucket();
            assertThat(metricsBucket.getItemNames(), hasItem(METRIC_AMOUNT));
            assertThat(attributesColumnsBucket.getItemNames(), hasItem(ATTR_DEPARTMENT));
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkReferencePointWithMeasureAndViewByAndStackBy() {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_MEASURE_AND_ATTRIBUTE_AND_STACK, ReportType.COLUMN_CHART)
                        .setMeasureBucket(
                                singletonList(
                                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT))))
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                        CategoryBucket.Type.ATTRIBUTE),
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_ACTIVITY_TYPE),
                                        CategoryBucket.Type.STACK))));

        initAnalysePage().openInsight(INSIGHT_HAS_MEASURE_AND_ATTRIBUTE_AND_STACK).waitForReportComputing();
        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        StacksBucket stacksBucket = analysisPage.getStacksBucket();
        AttributesBucket attributesBucket = analysisPage.getAttributesBucket();
        for (ReportType reportType : REPORT_TYPES) {
            analysisPage.changeReportType(reportType);
            assertThat(metricsBucket.getItemNames(), hasItem(METRIC_AMOUNT));
            if (reportType.equals(ReportType.HEAT_MAP) || reportType.equals(ReportType.COLUMN_CHART)) {
                assertThat(attributesBucket.getItemNames(), hasItem(ATTR_DEPARTMENT));
                assertEquals(stacksBucket.getAttributeName(), ATTR_ACTIVITY_TYPE);
            } else {
                assertThat(attributesBucket.getItemNames(), hasItem(ATTR_DEPARTMENT));
            }

            analysisPage.changeReportType(ReportType.TABLE);
            AttributesBucket attributesColumnsBucket = analysisPage.getAttributesColumnsBucket();
            assertThat(metricsBucket.getItemNames(), hasItem(METRIC_AMOUNT));
            assertThat(attributesBucket.getItemNames(), hasItem(ATTR_DEPARTMENT));
            assertThat(attributesColumnsBucket.getItemNames(), hasItem(ATTR_ACTIVITY_TYPE));
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkReferencePointWithThreeMeasureAndViewBy() {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_MEASURE_AND_ATTRIBUTE_AND_STACK, ReportType.COLUMN_CHART)
                        .setMeasureBucket(
                                asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)),
                                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)),
                                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT))))
                        .setCategoryBucket(singletonList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                        CategoryBucket.Type.ATTRIBUTE))));

        initAnalysePage().openInsight(INSIGHT_HAS_MEASURE_AND_ATTRIBUTE_AND_STACK).waitForReportComputing();
        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        AttributesBucket attributesBucket = analysisPage.getAttributesBucket();

        for (ReportType reportType : REPORT_TYPES) {
            analysisPage.changeReportType(reportType);
            if (reportType.equals(ReportType.SCATTER_PLOT)) {
                MetricsBucket metricsSecondaryBucket = analysisPage.getMetricsSecondaryBucket();
                assertEquals(metricsBucket.getItemNames(), singletonList(METRIC_AMOUNT));
                assertEquals(metricsSecondaryBucket.getItemNames(), singletonList(METRIC_AMOUNT));
                assertThat(attributesBucket.getItemNames(), hasItem(ATTR_DEPARTMENT));
            } else if (reportType.equals(ReportType.COLUMN_CHART)) {
                assertEquals(metricsBucket.getItemNames(), asList(METRIC_AMOUNT, METRIC_AMOUNT, METRIC_AMOUNT));
                assertThat(attributesBucket.getItemNames(), hasItem(ATTR_DEPARTMENT));
            } else {
                assertEquals(metricsBucket.getItemNames(), singletonList(METRIC_AMOUNT));
                assertThat(attributesBucket.getItemNames(), hasItem(ATTR_DEPARTMENT));
            }

            analysisPage.changeReportType(ReportType.TABLE);
            assertEquals(metricsBucket.getItemNames(), asList(METRIC_AMOUNT, METRIC_AMOUNT, METRIC_AMOUNT));
            assertThat(attributesBucket.getItemNames(), hasItem(ATTR_DEPARTMENT));
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkReferencePointWhenSwitchingFromHeatMap() {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HEAT_MAP, ReportType.HEAT_MAP)
                        .setMeasureBucket(
                                asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT))))
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                        CategoryBucket.Type.STACK))));

        initAnalysePage().openInsight(INSIGHT_HEAT_MAP).waitForReportComputing();
        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        StacksBucket stacksBucket = analysisPage.getStacksBucket();

        assertEquals(metricsBucket.getItemNames(), singletonList(METRIC_AMOUNT));
        assertEquals(stacksBucket.getAttributeName(), ATTR_DEPARTMENT);

        analysisPage.changeReportType(ReportType.TABLE);
        AttributesBucket attributesColumnsBucket = analysisPage.getAttributesColumnsBucket();
        assertEquals(metricsBucket.getItemNames(), singletonList(METRIC_AMOUNT));
        assertEquals(attributesColumnsBucket.getItemNames(), singletonList(ATTR_DEPARTMENT));

        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HEAT_MAP, ReportType.HEAT_MAP)
                        .setMeasureBucket(
                                asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT))))
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_ACTIVITY_TYPE),
                                        CategoryBucket.Type.ATTRIBUTE),
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                        CategoryBucket.Type.ATTRIBUTE))));

        initAnalysePage().openInsight(INSIGHT_HEAT_MAP).waitForReportComputing();
        AttributesBucket attributesBucket = analysisPage.getAttributesBucket();
        assertEquals(metricsBucket.getItemNames(), singletonList(METRIC_AMOUNT));
        assertEquals(attributesBucket.getItemNames(), singletonList(ATTR_ACTIVITY_TYPE));
        assertEquals(stacksBucket.getAttributeName(), ATTR_DEPARTMENT);

        analysisPage.changeReportType(ReportType.TABLE);
        assertEquals(metricsBucket.getItemNames(), singletonList(METRIC_AMOUNT));
        assertEquals(attributesBucket.getItemNames(), singletonList(ATTR_ACTIVITY_TYPE));
        assertEquals(attributesColumnsBucket.getItemNames(), singletonList(ATTR_DEPARTMENT));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkReferencePointWhenSwitchingFromTreeMap() {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_TREE_MAP, ReportType.TREE_MAP)
                        .setMeasureBucket(
                                asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)),
                                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)),
                                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT))))
                        .setCategoryBucket(singletonList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                        CategoryBucket.Type.STACK))));

        initAnalysePage().openInsight(INSIGHT_TREE_MAP).waitForReportComputing();
        assertEquals(analysisPage.getMetricsBucket().getItemNames(),
                asList(METRIC_AMOUNT, METRIC_AMOUNT, METRIC_AMOUNT));
        assertEquals(analysisPage.getStacksBucket().getAttributeName(), ATTR_DEPARTMENT);

        analysisPage.changeReportType(ReportType.TABLE);
        assertEquals(analysisPage.getMetricsBucket().getItemNames(),
                asList(METRIC_AMOUNT, METRIC_AMOUNT, METRIC_AMOUNT));
        assertEquals(analysisPage.getAttributesColumnsBucket().getItemNames(), singletonList(ATTR_DEPARTMENT));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkReferencePointWhenSwitchingFromInsightHasSomeAttribute() {
        indigoRestRequest.createInsight(
            new InsightMDConfiguration(INSIGHT_HAS_ATTRIBUTES, ReportType.COLUMN_CHART)
                .setCategoryBucket(singletonList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                        CategoryBucket.Type.STACK))));

        initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTES).waitForReportComputing();
        assertEquals(analysisPage.getStacksBucket().getAttributeName(), ATTR_DEPARTMENT);

        analysisPage.changeReportType(ReportType.TABLE);
        assertEquals(analysisPage.getAttributesColumnsBucket().getItemNames(), singletonList(ATTR_DEPARTMENT));

        indigoRestRequest.createInsight(
            new InsightMDConfiguration(INSIGHT_HAS_ATTRIBUTES, ReportType.COLUMN_CHART)
                .setCategoryBucket(singletonList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                        CategoryBucket.Type.ATTRIBUTE))));

        initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTES).waitForReportComputing();
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), singletonList(ATTR_DEPARTMENT));

        analysisPage.changeReportType(ReportType.TABLE);
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), singletonList(ATTR_DEPARTMENT));

        indigoRestRequest.createInsight(
            new InsightMDConfiguration(INSIGHT_HAS_ATTRIBUTES, ReportType.COLUMN_CHART)
                    .setCategoryBucket(asList(
                            CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_ACTIVITY_TYPE),
                                    CategoryBucket.Type.ATTRIBUTE),
                            CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                    CategoryBucket.Type.STACK))));

        initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTES).waitForReportComputing();
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), singletonList(ATTR_ACTIVITY_TYPE));
        assertEquals(analysisPage.getStacksBucket().getAttributeName(), ATTR_DEPARTMENT);

        analysisPage.changeReportType(ReportType.TABLE);
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), singletonList(ATTR_ACTIVITY_TYPE));
        assertEquals(analysisPage.getAttributesColumnsBucket().getItemNames(), singletonList(ATTR_DEPARTMENT));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkDefaultSortingOnPivotTable() {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_TREE_MAP, ReportType.TABLE)
                        .setMeasureBucket(
                                asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)),
                                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AVG_AMOUNT))))
                        .setCategoryBucket(singletonList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_PRODUCT),
                                        CategoryBucket.Type.ATTRIBUTE))));

        initAnalysePage().openInsight(INSIGHT_TREE_MAP).waitForReportComputing();
        assertTrue(analysisPage.getPivotTableReport().isRowHeaderSortedUp(ATTR_PRODUCT),
                "Default sorting should be with ASC");

        analysisPage.addAttribute(ATTR_DEPARTMENT).waitForReportComputing();
        assertTrue(analysisPage.getPivotTableReport().isRowHeaderSortedUp(ATTR_PRODUCT),
                "Default sorting should be kept with ASC");

        analysisPage.addAttribute(ATTR_IS_CLOSED).reorderAttribute(ATTR_PRODUCT, ATTR_IS_CLOSED)
            .reorderAttribute(ATTR_DEPARTMENT, ATTR_IS_CLOSED).waitForReportComputing();
        takeScreenshot(browser, "Validate-GoodSales-project", getClass());
        assertTrue(analysisPage.getPivotTableReport().isRowHeaderSortedUp(ATTR_IS_CLOSED),
                "Default sorting should be kept with ASC");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkSortingIsNotApplied() {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_METRICS, ReportType.TABLE)
                        .setMeasureBucket(
                                asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)),
                                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AVG_AMOUNT)))));

        initAnalysePage().openInsight(INSIGHT_HAS_METRICS).waitForReportComputing();
        assertFalse(analysisPage.getPivotTableReport().isTableSortArrowPresent(),
                "Should be not sort on table");

        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_ATTRIBUTES, ReportType.TABLE)
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                        CategoryBucket.Type.COLUMNS),
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_PRODUCT),
                                        CategoryBucket.Type.COLUMNS))));
        initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTES).waitForReportComputing();
        assertFalse(analysisPage.getPivotTableReport().isTableSortArrowPresent(),
                "Should be not sort on table");

        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE, ReportType.TABLE)
                        .setMeasureBucket(
                                asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)),
                                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AVG_AMOUNT))))
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                        CategoryBucket.Type.COLUMNS),
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_PRODUCT),
                                        CategoryBucket.Type.COLUMNS))));
        initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE).waitForReportComputing();
        assertFalse(analysisPage.getPivotTableReport().isTableSortArrowPresent(),
                "Should be not sort on table");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkCustomSortingOnAttributeRow() {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE, ReportType.TABLE)
                        .setMeasureBucket(
                                singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT))))
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                        CategoryBucket.Type.ATTRIBUTE),
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_PRODUCT),
                                        CategoryBucket.Type.ATTRIBUTE))));

        initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE).waitForReportComputing();
        PivotTableReport pivotTableReport = analysisPage.getPivotTableReport();
        pivotTableReport.sortBaseOnHeader(ATTR_PRODUCT);
        analysisPage.waitForReportComputing();
        assertTrue(pivotTableReport.isRowHeaderSortedUp(ATTR_PRODUCT),
                "Product Attribute should be sorted with ASC");

        analysisPage.addAttribute(ATTR_IS_CLOSED).waitForReportComputing();
        assertTrue(pivotTableReport.isRowHeaderSortedUp(ATTR_PRODUCT),
                "Product Attribute should be still sorted with ASC");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkCustomSortingOnMeasure() {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE, ReportType.TABLE)
                        .setMeasureBucket(
                                singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT))))
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                        CategoryBucket.Type.ATTRIBUTE),
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_PRODUCT),
                                        CategoryBucket.Type.COLUMNS))));

        initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE).waitForReportComputing();
        PivotTableReport pivotTableReport = analysisPage.getPivotTableReport();
        pivotTableReport.sortBaseOnHeader(METRIC_AMOUNT);

        analysisPage.addMetric(METRIC_AVG_AMOUNT).waitForReportComputing();
        assertFalse(pivotTableReport.isRowHeaderSortedUp(METRIC_AMOUNT),
                "Custom sort on M1 should be still kept");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkCustomSortingOnFilterBar() {
        PivotTableReport pivotTableReport = initAnalysePage().changeReportType(ReportType.TABLE).addMetric(METRIC_AMOUNT)
                .addAttribute(ATTR_DEPARTMENT).addColumnsAttribute(ATTR_PRODUCT).waitForReportComputing()
                .getPivotTableReport();
        pivotTableReport.sortBaseOnHeader(METRIC_AMOUNT);
        analysisPage.getFilterBuckets().configAttributeFilter(ATTR_PRODUCT,
                "Educationly", "Explorer", "Grammar Plus", "PhoenixSoft", "TouchAll", "WonderKid");
        analysisPage.waitForReportComputing();
        assertTrue(pivotTableReport.isRowHeaderSortedUp(ATTR_DEPARTMENT),
                "Sorting should be changed to " + ATTR_DEPARTMENT + " attribute A1 with ASC");

        analysisPage.getFilterBuckets().configAttributeFilter(ATTR_PRODUCT, "All");
        analysisPage.waitForReportComputing();
        assertTrue(pivotTableReport.isRowHeaderSortedUp(ATTR_DEPARTMENT),
                "Sorting should be still kept on " + ATTR_DEPARTMENT + " attribute A1 with ASC");

    }

    @Test(dependsOnGroups = {"createProject"})
    public void hoverIntoMeasureHeaderColumn() {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE, ReportType.TABLE)
                        .setMeasureBucket(
                                singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT))))
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                                        CategoryBucket.Type.ATTRIBUTE),
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                        CategoryBucket.Type.COLUMNS))));

        PivotTableReport pivotTableReport = initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE)
                .waitForReportComputing().getPivotTableReport();
        pivotTableReport.hoverOnBurgerMenuColumn(METRIC_AMOUNT, 0);
        assertTrue(pivotTableReport.isBurgerMenuVisible(METRIC_AMOUNT, 0),
                "Burger menu should be appeared when I hover into header column");
        ElementUtils.moveToElementActions(pivotTableReport.getRoot(), 1, 1).perform();
        assertFalse(pivotTableReport.isBurgerMenuVisible(METRIC_AMOUNT, 0),
                "Burger menu should be disappeared when I hover out header column");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void clickIntoMeasureHeaderColumn() {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE, ReportType.TABLE)
                        .setMeasureBucket(
                                singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT))))
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                                        CategoryBucket.Type.ATTRIBUTE),
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                        CategoryBucket.Type.COLUMNS))));

        PivotTableReport pivotTableReport = initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE)
                .waitForReportComputing().getPivotTableReport();
        assertEquals(pivotTableReport.openAggregationPopup(METRIC_AMOUNT, 0).getItemsList(),
                AggregationItem.getAllFullNames(),
                "Aggregation items should include 6 functions in order is sum, max, min, avg, median, rollup (total)");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void selectIntoAggregationItem() {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE, ReportType.TABLE)
                        .setMeasureBucket(
                                asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)),
                                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AVG_AMOUNT))))
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                                        CategoryBucket.Type.ATTRIBUTE),
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                        CategoryBucket.Type.COLUMNS))));

        PivotTableReport pivotTableReport = initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE)
                .waitForReportComputing().getPivotTableReport();
        pivotTableReport.addTotal(AggregationItem.SUM, METRIC_AMOUNT, 0);
        analysisPage.waitForReportComputing();

        assertEquals(pivotTableReport.getGrandTotalValues(AggregationItem.SUM), asList("$80,406,324.96", EMPTY, "$36,219,131.58", EMPTY));

        PivotAggregationPopup aggregationPopup = pivotTableReport.openAggregationPopup(METRIC_AMOUNT, 0);
        assertTrue(aggregationPopup.isItemChecked(AggregationItem.SUM),
                "The SUM item should be checked");
        pivotTableReport.collapseBurgerMenuColumn(METRIC_AMOUNT);
        assertFalse(pivotTableReport.openAggregationPopup(METRIC_AVG_AMOUNT, 0).isItemChecked(AggregationItem.SUM),
                "The SUM item should be not checked");
        pivotTableReport.collapseBurgerMenuColumn(METRIC_AVG_AMOUNT);
        assertFalse(pivotTableReport.openAggregationPopup("Direct Sales", 0)
                .isItemChecked(AggregationItem.SUM), "The SUM item it should be not checked");

        aggregationPopup.selectItem(AggregationItem.SUM);
        analysisPage.waitForReportComputing();
        assertTrue(pivotTableReport.openAggregationPopup("Direct Sales", 0)
                .isItemChecked(AggregationItem.SUM), "The SUM item should be checked");
    }

    @Test(dependsOnGroups = "createProject")
    public void createPiVotTableHasSortOnMeasure() {
        initAnalysePage().addMetric(METRIC_AMOUNT).addMetric(METRIC_AVG_AMOUNT)
                .addAttribute(ATTR_PRODUCT).addColumnsAttribute(ATTR_DEPARTMENT).waitForReportComputing();

        pivotTableReport = analysisPage.getPivotTableReport();
        pivotTableReport.sortBaseOnHeader(METRIC_AMOUNT);
        analysisPage.saveInsight(INSIGHT_PIVOT_SORTING).waitForReportComputing();
        assertFalse(pivotTableReport.isRowHeaderSortedUp(METRIC_AMOUNT), "Metric Amount should be sorted with DESC");

        List<String> expectedValues = asList("$30,029,658.14", "$16,188,138.24", "$15,582,695.69", "$6,978,618.41", "$5,863,972.18", "$5,763,242.30");
        assertThat(pivotTableReport.getBodyContentColumn(1).stream().flatMap(List::stream).collect(toList()), equalTo(expectedValues));
    }

    @Test(dependsOnMethods = "createPiVotTableHasSortOnMeasure")
    public void removeAttributeOnColumn() {
        pivotTableReport = analysisPage.openInsight(INSIGHT_PIVOT_SORTING).waitForReportComputing().getPivotTableReport();
        analysisPage.removeColumn(ATTR_DEPARTMENT).waitForReportComputing();
        analysisPage.saveInsight().waitForReportComputing();
        assertFalse(pivotTableReport.isRowHeaderSortedUp(METRIC_AMOUNT), "Metric Amount should be kept sort by DESC");
        
        List<String> expectedValues = asList("$38,596,194.86", "$27,222,899.64", "$22,946,895.47", "$10,291,576.74", "$9,525,857.91", "$8,042,031.92");
        assertThat(pivotTableReport.getBodyContentColumn(1).stream().flatMap(List::stream).collect(toList()), equalTo(expectedValues));
    }
    
    @Test(dependsOnMethods = "removeAttributeOnColumn")
    public void reOpenPivotTableAfterRemoveColumn() {        
        assertFalse(pivotTableReport.isRowHeaderSortedUp(METRIC_AMOUNT), "Metric Amount should be kept sort by DESC");

        List<String> expectedValues = asList("$38,596,194.86", "$27,222,899.64", "$22,946,895.47", "$10,291,576.74", "$9,525,857.91", "$8,042,031.92");
        assertThat(pivotTableReport.getBodyContentColumn(1).stream().flatMap(List::stream).collect(toList()), equalTo(expectedValues));
    }

    private void createSimpleInsight(String title, String metric, ReportType reportType) {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(title, reportType)
                        .setMeasureBucket(
                                singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric)))));
    }
}
