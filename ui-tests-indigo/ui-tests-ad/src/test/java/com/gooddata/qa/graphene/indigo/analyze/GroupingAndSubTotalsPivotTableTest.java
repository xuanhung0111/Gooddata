package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.sdk.model.md.Fact;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.AggregationItem;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.CompareTypeDropdown;
import com.gooddata.qa.graphene.fragments.indigo.analyze.PivotAggregationPopup;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.DateFilterPickerPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricsBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.PivotTableReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.fragments.manage.MetricFormatterDialog.Formatter;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.sdk.model.md.Restriction.title;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT_BOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_REGION;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_FORECAST_CATEGORY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STATUS;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.util.List;
import java.util.stream.Collectors;

public class GroupingAndSubTotalsPivotTableTest extends AbstractAnalyseTest {

    private static final String INSIGHT_HAS_MEASURES_AND_ATTRIBUTES_COLUMN_AND_ROW = "Measures and attributes, rows";
    private static final String INSIGHT_HAS_THREE_ATTRIBUTES_COLUMN_AND_A_ROW = "Measures and three attributes, a row";
    private static final String INSIGHT_APPLIED_SPPY = "Insight SPPY";
    private static final String INSIGHT_APPLIED_LOCAL_FILTER = "Local Filter";
    private static final String INSIGHT_APPLIED_ARITHMETIC_MEASURE = "Arithmetic Measure";
    private static final String INSIGHT_APPLY_NUMBER_FORMAT = "Insight Number Format";
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
    private ProjectRestRequest projectRestRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Table Grouping & Sub-Totals (Basic)";
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createAmountMetric();
        metrics.createAvgAmountMetric();
        metrics.createAmountBOPMetric();

        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_TABLE_COLUMN_AUTO_RESIZING, false);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void prepareTable() throws NoSuchFieldException {
        indigoRestRequest.createInsight(
            new InsightMDConfiguration(INSIGHT_HAS_MEASURES_AND_ATTRIBUTES_COLUMN_AND_ROW, ReportType.TABLE)
                .setMeasureBucket(asList(MeasureBucket
                        .createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)),
                    MeasureBucket
                        .createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT_BOP))))
                .setCategoryBucket(asList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                        CategoryBucket.Type.ATTRIBUTE),
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_REGION),
                        CategoryBucket.Type.ATTRIBUTE),
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                        CategoryBucket.Type.COLUMNS),
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                        CategoryBucket.Type.COLUMNS))));

        indigoRestRequest.createInsight(
            new InsightMDConfiguration(INSIGHT_APPLIED_SPPY, ReportType.TABLE)
                .setMeasureBucket(asList(MeasureBucket
                        .createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)),
                    MeasureBucket
                        .createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT_BOP))))
                .setCategoryBucket(asList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                        CategoryBucket.Type.ATTRIBUTE),
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_REGION),
                        CategoryBucket.Type.ATTRIBUTE))));

        initAnalysePage().openInsight(INSIGHT_APPLIED_SPPY).addDate().waitForReportComputing();

        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel.configTimeFilterByRangeHelper("01/01/2006", "01/01/2020")
            .changeCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_PREVIOUS_YEAR)
            .openCompareApplyMeasures().selectAllValues().apply();
        dateFilterPickerPanel.apply();
        analysisPage.saveInsight().waitForReportComputing();

        initAnalysePage().changeReportType(ReportType.TABLE).addMetric(METRIC_AMOUNT).addAttribute(ATTR_FORECAST_CATEGORY)
            .addAttribute(ATTR_REGION).getFilterBuckets().configAttributeFilter(ATTR_FORECAST_CATEGORY, "Include");
        analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT).expandConfiguration().showPercents();
        analysisPage.saveInsight(INSIGHT_APPLIED_LOCAL_FILTER).waitForReportComputing();

        indigoRestRequest.createInsight(
            new InsightMDConfiguration(INSIGHT_APPLIED_ARITHMETIC_MEASURE, ReportType.TABLE)
                .setMeasureBucket(asList(MeasureBucket
                        .createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)),
                    MeasureBucket
                        .createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT_BOP))))
                .setCategoryBucket(asList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                        CategoryBucket.Type.ATTRIBUTE),
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                        CategoryBucket.Type.ATTRIBUTE))));


        MetricsBucket metricsBucket = initAnalysePage().openInsight(INSIGHT_APPLIED_ARITHMETIC_MEASURE).getMetricsBucket();
        MetricConfiguration metricConfiguration = metricsBucket.createCalculatedMeasure()
            .getMetricConfiguration("Ratio of …");
        metricConfiguration.chooseArithmeticMeasureA(METRIC_AMOUNT, 1);
        metricConfiguration.chooseArithmeticMeasureB(METRIC_AMOUNT_BOP, 2);
        metricConfiguration.chooseOperator(MetricConfiguration.OperatorCalculated.SUM);
        analysisPage.saveInsight().waitForReportComputing();
        initAnalysePage().changeReportType(ReportType.TABLE).addMetric(METRIC_AMOUNT)
            .addMetric(METRIC_AMOUNT_BOP).addAttribute(ATTR_DEPARTMENT).addAttribute(ATTR_REGION)
            .addAttribute(ATTR_STATUS).addColumnsAttribute(ATTR_FORECAST_CATEGORY)
            .saveInsight(INSIGHT_HAS_THREE_ATTRIBUTES_COLUMN_AND_A_ROW).waitForReportComputing();
    }

    @Test(dependsOnMethods = {"prepareTable"})
    public void checkGroupingPivotTableMoreAttributes() {
        PivotTableReport pivotTableReport = initAnalysePage()
            .openInsight(INSIGHT_HAS_MEASURES_AND_ATTRIBUTES_COLUMN_AND_ROW).waitForReportComputing()
            .getPivotTableReport();

        assertEquals(pivotTableReport.getRowAttributeColumns(), asList("Direct Sales", "East Coast", "West Coast",
            "Inside Sales", "East Coast", "West Coast"));
        assertEquals(pivotTableReport.getColumnGroupHeaders(), asList("Direct Sales", "Exclude", "Include"));
    }

    @Test(dependsOnMethods = {"prepareTable"})
    public void checkGroupingPivotTableAppliedSPPY() {
        PivotTableReport pivotTableReport = initAnalysePage().openInsight(INSIGHT_APPLIED_SPPY)
            .waitForReportComputing().getPivotTableReport();

        List<String> rowAttributeColumns = pivotTableReport.getRowAttributeColumns();

        long countAttribute = rowAttributeColumns.stream()
            .filter(e -> e.equals("East Coast"))
            .count();

        assertEquals(countAttribute, 2);

        countAttribute = rowAttributeColumns.stream()
            .filter(e -> e.equals("West Coast"))
            .count();
        assertEquals(countAttribute, 2);

        countAttribute = rowAttributeColumns.stream()
            .filter(e -> e.equals("Direct Sales"))
            .count();
        assertEquals(countAttribute, 1);

        countAttribute = rowAttributeColumns.stream()
            .filter(e -> e.equals("Inside Sales"))
            .count();
        assertEquals(countAttribute, 1);
    }

    @Test(dependsOnMethods = {"prepareTable"})
    public void checkGroupingPivotTableAppliedLocalFilter() {
        PivotTableReport pivotTableReport = initAnalysePage().openInsight(INSIGHT_APPLIED_LOCAL_FILTER)
            .waitForReportComputing().getPivotTableReport();

        assertEquals(pivotTableReport.getRowAttributeColumns(), asList("Include", "East Coast", "West Coast"));
        assertEquals(pivotTableReport.getValueMeasuresPresent(), asList("20.09%", "79.91%"));
        assertEquals(pivotTableReport.getHeadersMeasure(), asList("% " + METRIC_AMOUNT));
    }

    @Test(dependsOnMethods = {"prepareTable"})
    public void checkGroupingPivotTableAppliedArithmeticMeasure() {
        PivotTableReport pivotTableReport = initAnalysePage().openInsight(INSIGHT_APPLIED_ARITHMETIC_MEASURE)
            .waitForReportComputing().getPivotTableReport();

        assertEquals(pivotTableReport.getRowAttributeColumns(), asList("Exclude", "Direct Sales", "Inside Sales",
            "Include", "Direct Sales", "Inside Sales"));
    }

    @Test(dependsOnMethods = {"prepareTable"})
    public void addGrandTotalsForMeasureHeader() {
        PivotTableReport pivotTableReport = initAnalysePage().openInsight(INSIGHT_HAS_THREE_ATTRIBUTES_COLUMN_AND_A_ROW)
            .waitForReportComputing().getPivotTableReport();
        pivotTableReport.openAggregationPopup(METRIC_AMOUNT, 0).hoverItem(AggregationItem.SUM)
            .selectRowsItem("of all rows");

        assertEquals(pivotTableReport.getGrandTotalsContent(),
            singletonList(asList("Sum", EMPTY, EMPTY, "$48,932,639.59", EMPTY, "$67,692,816.95", EMPTY)));
    }

    @Test(dependsOnMethods = {"prepareTable"})
    public void addGrandTotalsForAttributeFromMeasureHeader() {
        PivotTableReport pivotTableReport = initAnalysePage().openInsight(INSIGHT_HAS_THREE_ATTRIBUTES_COLUMN_AND_A_ROW)
            .waitForReportComputing().getPivotTableReport();
        pivotTableReport.openAggregationPopup(METRIC_AMOUNT, 0).hoverItem(AggregationItem.SUM)
            .selectRowsItem("within " + ATTR_DEPARTMENT);

        List<String> expectedResult = asList(
            "Sum", EMPTY, "$33,562,482.51", EMPTY, "$46,843,842.45", EMPTY,
            "Sum", EMPTY, "$15,370,157.08", EMPTY, "$20,848,974.50", EMPTY);

        assertEquals(pivotTableReport.getSubTotalsContent(), expectedResult);

        pivotTableReport.openAggregationPopup(METRIC_AMOUNT_BOP, 0).hoverItem(AggregationItem.SUM)
            .selectRowsItem("within " + ATTR_REGION);

       List<String> expectedResult1 = asList(
            "Sum", EMPTY, "$566,635.71", EMPTY, "$1,195,713.20",
            "Sum", EMPTY, "$874,451.54", EMPTY, "$1,206,600.09",
            "Sum", EMPTY, "$33,562,482.51", EMPTY, "$46,843,842.45", EMPTY,
            "Sum", EMPTY, "$227,539.37", EMPTY, "$196,126.06",
            "Sum", EMPTY, "$576,959.76", EMPTY, "$290,371.92",
            "Sum", EMPTY, "$15,370,157.08", EMPTY, "$20,848,974.50", EMPTY);

        assertEquals(pivotTableReport.getSubTotalsContent(), expectedResult1);
    }

    @Test(dependsOnMethods = {"prepareTable"})
    public void addGrandTotalsPivotTableAppliedSPPY() throws NoSuchFieldException {
        PivotTableReport pivotTableReport = initAnalysePage().openInsight(INSIGHT_HAS_THREE_ATTRIBUTES_COLUMN_AND_A_ROW)
            .addDateToColumnsAttribute().waitForReportComputing().getPivotTableReport();

        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel.configTimeFilterByRangeHelper("01/01/2006", "01/01/2020")
            .changeCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_PREVIOUS_YEAR)
            .openCompareApplyMeasures().selectByNames(METRIC_AMOUNT).apply();
        dateFilterPickerPanel.apply();
        analysisPage.waitForReportComputing();

        pivotTableReport.openAggregationPopup(METRIC_AMOUNT, 0).hoverItem(AggregationItem.SUM)
            .selectRowsItem("within " + ATTR_DEPARTMENT);

        assertEquals(pivotTableReport.getSubTotalsContent(),
            asList("Sum", EMPTY, EMPTY, "$5,638,785.08", EMPTY, EMPTY,
                "Sum", EMPTY, EMPTY, "$3,221,037.39", EMPTY, EMPTY));
    }

    @Test(dependsOnMethods = {"prepareTable"})
    public void addGrandTotalsAppliedPercentAndLocalFilter() throws NoSuchFieldException {
        PivotTableReport pivotTableReport = initAnalysePage().openInsight(INSIGHT_APPLIED_LOCAL_FILTER)
            .addDateToColumnsAttribute().waitForReportComputing().getPivotTableReport();

        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel.configTimeFilterByRangeHelper("01/01/2006", "01/01/2020")
            .changeCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_PREVIOUS_YEAR)
            .openCompareApplyMeasures().selectByNames("% " + METRIC_AMOUNT).apply();
        dateFilterPickerPanel.apply();
        analysisPage.waitForReportComputing();

        pivotTableReport.openAggregationPopup("% " + METRIC_AMOUNT, 0).hoverItem(AggregationItem.SUM)
            .selectRowsItem("within " + ATTR_FORECAST_CATEGORY);

        assertEquals(pivotTableReport.getSubTotalsContent(), asList("Sum", EMPTY, "14.89%", EMPTY, "50.93%", EMPTY));
    }

    @Test(dependsOnMethods = {"prepareTable"})
    public void addGrandTotalsPivotTableAppliedArithmeticMeasure() {
        MetricsBucket metricsBucket = initAnalysePage().openInsight(INSIGHT_HAS_THREE_ATTRIBUTES_COLUMN_AND_A_ROW).getMetricsBucket();
        MetricConfiguration metricConfiguration = metricsBucket.createCalculatedMeasure()
            .getMetricConfiguration("Ratio of …");
        metricConfiguration.chooseArithmeticMeasureA(METRIC_AMOUNT, 1);
        metricConfiguration.chooseArithmeticMeasureB(METRIC_AMOUNT_BOP, 2);
        metricConfiguration.chooseOperator(MetricConfiguration.OperatorCalculated.SUM);

        PivotTableReport pivotTableReport = analysisPage.getPivotTableReport();
        pivotTableReport.openAggregationPopup(METRIC_AMOUNT, 0).hoverItem(AggregationItem.SUM)
            .selectRowsItem("within " + ATTR_DEPARTMENT);

        assertEquals(pivotTableReport.getSubTotalsContent(),
            asList("Sum", EMPTY, "$33,562,482.51", EMPTY, EMPTY, "$46,843,842.45",
                "Sum", EMPTY, "$15,370,157.08", EMPTY, EMPTY, "$20,848,974.50"));
    }

    @Test(dependsOnMethods = {"prepareTable"})
    public void removeGrandTotalsAndSubtotalsOnSubMenu() {
        PivotTableReport pivotTableReport = initAnalysePage().openInsight(INSIGHT_HAS_THREE_ATTRIBUTES_COLUMN_AND_A_ROW)
            .waitForReportComputing().getPivotTableReport();

        PivotAggregationPopup pivotAggregationPopup = pivotTableReport.openAggregationPopup(METRIC_AMOUNT_BOP, 0)
            .hoverItem(AggregationItem.SUM);
        pivotAggregationPopup.selectRowsItem("within " + ATTR_DEPARTMENT);

        pivotTableReport.openAggregationPopup(METRIC_AMOUNT_BOP, 0).hoverItem(AggregationItem.SUM);
        assertTrue(pivotAggregationPopup.isRowsItemChecked((pivotAggregationPopup.getRowsItem("within " + ATTR_DEPARTMENT))));

        pivotAggregationPopup.unSelectRowsItem("within " + ATTR_DEPARTMENT);
        pivotTableReport.openAggregationPopup(METRIC_AMOUNT_BOP, 0).hoverItem(AggregationItem.SUM);
        assertFalse(pivotAggregationPopup.isRowsItemChecked(pivotAggregationPopup.getRowsItem("within " + ATTR_DEPARTMENT)));
    }

    @Test(dependsOnMethods = {"prepareTable"})
    public void removeGrandTotalsAndSubtotalsOnParentMenu() {
        PivotTableReport pivotTableReport = initAnalysePage().openInsight(INSIGHT_HAS_THREE_ATTRIBUTES_COLUMN_AND_A_ROW)
            .waitForReportComputing().getPivotTableReport();

        pivotTableReport.openAggregationPopup("Exclude", 0)
            .hoverItem(AggregationItem.MAX).selectRowsItem("of all rows");
        assertEquals(pivotTableReport.getGrandTotalsContent(),
            singletonList(asList("Max", EMPTY, EMPTY, "$8,675,554.99", "$804,518.12", "$17,485,183.84", EMPTY)));

        pivotTableReport.openAggregationPopup("Exclude", 0)
            .hoverItem(AggregationItem.MAX).unSelectRowsItem("of all rows");
        assertFalse(pivotTableReport.containsGrandTotals(), "The total rows should be hidden");
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
    public void applyFormatterOnGrandAndSubtotals() {
        final String metricExpression = format("SELECT SUM([%s]) WHERE 1 = 0",
            getMdService().getObjUri(getProject(), Fact.class, title("Amount")));
        createMetric(METRIC_NULL_VALUE, metricExpression, "#'##0,00 formatted; [=null] null value!");

        indigoRestRequest.createInsight(
            new InsightMDConfiguration(INSIGHT_APPLY_NUMBER_FORMAT, ReportType.TABLE)
                .setMeasureBucket(
                    asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_DEFAULT)),
                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_PERCENT)),
                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_BARS)),
                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_TRUNCATE_NUMBER))))
                .setCategoryBucket(asList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                        CategoryBucket.Type.ATTRIBUTE),
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_REGION),
                        CategoryBucket.Type.ATTRIBUTE),
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                        CategoryBucket.Type.COLUMNS))));

        PivotTableReport pivotTableReport = initAnalysePage().openInsight(INSIGHT_APPLY_NUMBER_FORMAT)
            .waitForReportComputing().getPivotTableReport();

        pivotTableReport.openAggregationPopup("Exclude", 0).hoverItem(AggregationItem.SUM)
            .selectRowsItem("of all rows");
        pivotTableReport.openAggregationPopup(METRIC_DEFAULT, 0).hoverItem(AggregationItem.SUM)
            .selectRowsItem("within " + ATTR_DEPARTMENT);

       assertThat(pivotTableReport.getGrandTotalsContent().stream()
            .flatMap(x -> x.stream())
            .collect(Collectors.toList()),
           hasItems("Sum", "2,805,057,310", "280,505,731,024%", "██████████", "$2.8 B"));

        assertThat(pivotTableReport.getSubTotalsContent(),
            hasItems("Sum", "1,965,840,136", "1,961,175,991", "839,217,174", "851,680,407"));

        indigoRestRequest.createInsight(
            new InsightMDConfiguration(INSIGHT_APPLY_NUMBER_FORMAT, ReportType.TABLE)
                .setMeasureBucket(
                    asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_COLORS)),
                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_COLORS_FORMAT)),
                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_BACKGROUND_FORMAT)),
                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_XSS_FORMAT))))
                .setCategoryBucket(asList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                        CategoryBucket.Type.ATTRIBUTE),
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_REGION),
                        CategoryBucket.Type.ATTRIBUTE),
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                        CategoryBucket.Type.COLUMNS))));

        pivotTableReport = initAnalysePage().openInsight(INSIGHT_APPLY_NUMBER_FORMAT)
            .waitForReportComputing().getPivotTableReport();

        pivotTableReport.openAggregationPopup("Exclude", 0).hoverItem(AggregationItem.SUM)
            .selectRowsItem("of all rows");
        pivotTableReport.openAggregationPopup(METRIC_COLORS, 0).hoverItem(AggregationItem.SUM)
            .selectRowsItem("within " + ATTR_DEPARTMENT);

        assertThat(pivotTableReport.getGrandTotalsContent().stream()
                .flatMap(x -> x.stream())
                .collect(Collectors.toList()),
            hasItems("Sum", "$2,805,057,310.24", "<button>2,805,057,310.24</button>"));

        assertThat(pivotTableReport.getSubTotalsContent(),
            hasItems("Sum", "$1,965,840,135.87", "$1,961,175,991.19", "$839,217,174.37", "$851,680,407.29"));

        indigoRestRequest.createInsight(
            new InsightMDConfiguration(INSIGHT_APPLY_NUMBER_FORMAT, ReportType.TABLE)
                .setMeasureBucket(
                    asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_UTF)),
                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_NULL_VALUE)),
                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_LONG))))
                .setCategoryBucket(asList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                        CategoryBucket.Type.ATTRIBUTE),
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_REGION),
                        CategoryBucket.Type.ATTRIBUTE),
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                        CategoryBucket.Type.COLUMNS))));

        pivotTableReport = initAnalysePage().openInsight(INSIGHT_APPLY_NUMBER_FORMAT)
            .waitForReportComputing().getPivotTableReport();

        pivotTableReport.openAggregationPopup("Exclude", 0).hoverItem(AggregationItem.SUM)
            .selectRowsItem("of all rows");
        pivotTableReport.openAggregationPopup(METRIC_UTF, 0).hoverItem(AggregationItem.SUM)
            .selectRowsItem("within " + ATTR_DEPARTMENT);

        assertThat(pivotTableReport.getGrandTotalsContent().stream()
                .flatMap(x -> x.stream())
                .collect(Collectors.toList()),
            hasItems("Sum", "2805057'310.24 kiểm tra nghiêm khắc", "null value!",
                "$2,805,057,310 long format long format long format long format" +
                " long format long format long format"));

        assertThat(pivotTableReport.getSubTotalsContent(),
            hasItems("Sum", "1965840'135.87 kiểm tra nghiêm khắc", "839217'174.37 kiểm tra nghiêm khắc"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void someActionsWithGroupingAndSubtotals() {
        String insight = "Insight :" + generateHashString();
        MetricsBucket metricsBucket = initAnalysePage().changeReportType(ReportType.TABLE).addMetric(METRIC_AMOUNT)
            .addMetric(METRIC_AMOUNT_BOP).addAttribute(ATTR_DEPARTMENT).addAttribute(ATTR_REGION)
            .addAttribute(ATTR_STATUS).addColumnsAttribute(ATTR_FORECAST_CATEGORY)
            .saveInsight(insight).waitForReportComputing().getMetricsBucket();

        MetricConfiguration metricConfiguration = metricsBucket.createCalculatedMeasure()
            .getMetricConfiguration("Ratio of …");
        metricConfiguration.chooseArithmeticMeasureA(METRIC_AMOUNT, 1);
        metricConfiguration.chooseArithmeticMeasureB(METRIC_AMOUNT_BOP, 2);
        metricConfiguration.chooseOperator(MetricConfiguration.OperatorCalculated.SUM);

        PivotTableReport pivotTableReport = analysisPage.getPivotTableReport();
        pivotTableReport.openAggregationPopup(METRIC_AMOUNT, 0).hoverItem(AggregationItem.SUM)
            .selectRowsItem("within " + ATTR_DEPARTMENT);

        String renamedInsight = "Renamed Insight";
        analysisPage.saveInsightAs(renamedInsight).waitForReportComputing().openInsight(renamedInsight)
            .waitForReportComputing();
        assertEquals(pivotTableReport.getSubTotalsContent(),
            asList("Sum", EMPTY, "$33,562,482.51", EMPTY, EMPTY, "$46,843,842.45",
                "Sum", EMPTY, "$15,370,157.08", EMPTY, EMPTY, "$20,848,974.50"));

        analysisPage.resetToBlankState();
        checkRedBar(browser);
    }

    @Test(dependsOnMethods = {"prepareTable"})
    public void checkGroupingAndSubtotalsOnKD() {
        String insight = "Insight :" + generateHashString();

        indigoRestRequest.createInsight(
            new InsightMDConfiguration(insight, ReportType.TABLE)
                .setMeasureBucket(singletonList(MeasureBucket
                    .createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT))))
                .setCategoryBucket(asList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                        CategoryBucket.Type.ATTRIBUTE),
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                        CategoryBucket.Type.ATTRIBUTE),
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_REGION),
                        CategoryBucket.Type.COLUMNS))));

        PivotTableReport pivotTableReport = initAnalysePage().openInsight(insight)
            .waitForReportComputing().getPivotTableReport();
        pivotTableReport.openAggregationPopup(METRIC_AMOUNT, 0).hoverItem(AggregationItem.SUM)
            .selectRowsItem("of all rows");
        analysisPage.saveInsight().waitForReportComputing();

        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage().addDashboard()
            .addInsight(insight).selectDateFilterByName("All time");

        pivotTableReport = indigoDashboardsPage.getFirstWidget(Insight.class).getPivotTableReport();
        pivotTableReport.sortBaseOnHeader(ATTR_DEPARTMENT);
        indigoDashboardsPage.waitForWidgetsLoading();

        assertEquals(pivotTableReport.getRowAttributeColumns(), asList("Inside Sales", "Exclude", "Include",
            "Direct Sales", "Exclude", "Include", "Sum"));

        pivotTableReport.sortBaseOnHeader(ATTR_FORECAST_CATEGORY);
        indigoDashboardsPage.waitForWidgetsLoading();

        assertEquals(pivotTableReport.getRowAttributeColumns(),
            asList("Direct Sales", "Exclude", "Inside Sales", "Exclude", "Direct Sales", "Include",
                "Inside Sales", "Include", "Sum"));

        assertThat(pivotTableReport.getGrandTotalsContent().stream()
                .flatMap(x -> x.stream())
                .collect(Collectors.toList()), hasItems("Sum", "$28,017,096.42", "$88,608,360.12"));
    }
}
