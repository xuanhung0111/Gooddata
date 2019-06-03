package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.md.Fact;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket.Type;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.OptionalStacking;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MeasureAsColumnBucket.DisplayAsSelect;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.analyze.CompareTypeDropdown;
import com.gooddata.qa.graphene.fragments.indigo.analyze.ExportToSelect;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.*;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Widget;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.graphene.utils.ElementUtils;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.ConfigurationPanelBucket.Items.*;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.*;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ComboChartReportTest extends AbstractAnalyseTest {

    private static final String INSIGHT_HAS_MEASURE_AS_LINE_COLUMN_AND_ATTRIBUTE = "Measure and secondary measure, attribute";
    private static final String INSIGHT_HAS_TWO_PRIMARY_MEASURES = "Two primary measures";
    private static final String INSIGHT_HAS_SOME_METRICS = "Some metrics";
    private static final String INSIGHT_HAS_DUAL_AXIS = "Dual Axis";
    private static final String INSIGHT_HAS_NEGATIVE_VALUES = "Negative Values";
    private static final String INSIGHT_HAS_NO_MEASURES = "No Measures";
    private static final String INSIGHT_HAS_TOO_LARGE_REPORT_VALUES = "Too large values";
    private static final String INSIGHT_HAS_PLATFORM_LIMIT = "Platform limit";
    private static final String INSIGHT_HAS_VALID_VALUES = "Valid Values";
    private static final String INSIGHT_HAS_NO_VALUES = "No values";
    private static final String INSIGHT_IS_ADDED_DASHBOARD_FILLTER_ATTRIBUTE = "Added Dashboard Filter";
    private static final String INSIGHT_IS_ADDED_DASHBOARD = "Added Dashboard";
    private static final String INSIGHT_IS_ADDED_DASHBOARD_MUF = "Applied MUF";
    private static final List<ReportType> listReportTypes = asList(ReportType.TABLE, ReportType.COLUMN_CHART,
        ReportType.BAR_CHART, ReportType.LINE_CHART, ReportType.STACKED_AREA_CHART);

    private IndigoRestRequest indigoRestRequest;
    private String sourceProjectId;
    private String targetProjectId;
    private String insightJsonObject;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "SD-81 Combo chart - AD";
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createAnotherProject() {
        sourceProjectId = testParams.getProjectId();
        targetProjectId = createNewEmptyProject("TARGET_PROJECT_TITLE" + generateHashString());
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createAmountMetric();
        metrics.createAvgAmountMetric();
        metrics.createAmountBOPMetric();
        metrics.createWonMetric();
        metrics.createBestCaseMetric();
        metrics.createSnapshotBOPMetric();
        ProjectRestRequest projectRestRequest = new ProjectRestRequest(
            new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProject(ProjectFeatureFlags.ENABLE_METRIC_DATE_FILTER, true);
    }

    @Test(dependsOnGroups = "createProject")
    public void prepareInsights() {
        insightJsonObject = createComboSimpleInsight(INSIGHT_HAS_MEASURE_AS_LINE_COLUMN_AND_ATTRIBUTE, METRIC_AMOUNT,
            METRIC_AVG_AMOUNT, ATTR_DEPARTMENT);

        indigoRestRequest.createInsight(
            new InsightMDConfiguration(INSIGHT_HAS_TWO_PRIMARY_MEASURES, ReportType.COMBO_CHART)
                .setMeasureBucket(asList(
                    MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)),
                    MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AVG_AMOUNT)))));
    }

    @Test(dependsOnMethods = "prepareInsights")
    public void switchToPieChartForEmptyADPage() {
        DisplayAsSelect measuresDisplayAsSelect = initAnalysePage()
            .openInsight(INSIGHT_HAS_MEASURE_AS_LINE_COLUMN_AND_ATTRIBUTE).waitForReportComputing()
            .getMeasureAsColumnBucketBucket().expandMeasuresDisplayAs();

        assertFalse(measuresDisplayAsSelect.isOptionCheckPresent(OptionalStacking.MEASURES),
            "Stacking Optional should be not present");
    }

    @Test(dependsOnMethods = "prepareInsights")
    public void disableStackingOptionWhenSwitchingLineChartOnPrimaryMeasureBucket() {
        MeasureAsColumnBucket metricsBucket = initAnalysePage().openInsight(INSIGHT_HAS_TWO_PRIMARY_MEASURES)
            .waitForReportComputing().getMeasureAsColumnBucketBucket();
        metricsBucket.expandMeasuresDisplayAs().selectTo(ReportType.LINE_CHART);

        assertFalse(metricsBucket.expandMeasuresDisplayAs().isOptionEnabled(OptionalStacking.MEASURES),
            "Stacking Optional should be disabled");
    }

    @Test(dependsOnMethods = "prepareInsights")
    public void enableCheckboxStackMeasuresWithTwoMeasures() {
        DisplayAsSelect displayAsSelect = initAnalysePage().openInsight(INSIGHT_HAS_TWO_PRIMARY_MEASURES)
            .waitForReportComputing().getMeasureAsColumnBucketBucket().expandMeasuresDisplayAs();

        assertTrue(displayAsSelect.isOptionEnabled(OptionalStacking.MEASURES),
            "Stacking Optional should be not present");
        assertFalse(displayAsSelect.isOptionCheck(OptionalStacking.MEASURES),
            "Stacking Optional should be not present");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void keepOptionalStackingWhenSwitchingBetweenChartTypes() {
        indigoRestRequest.createInsight(
            new InsightMDConfiguration(INSIGHT_HAS_SOME_METRICS, ReportType.COLUMN_CHART)
                .setMeasureBucket(
                    asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)),
                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_WON))))
                .setCategoryBucket(singletonList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                        CategoryBucket.Type.ATTRIBUTE))));

        initAnalysePage().openInsight(INSIGHT_HAS_SOME_METRICS).waitForReportComputing().getStacksBucket()
            .checkOption(OptionalStacking.MEASURES);
        ChartReport chartReport = analysisPage.changeReportType(ReportType.COMBO_CHART).waitForReportComputing()
            .getChartReport();

        MeasureAsColumnBucket metricsBucket = analysisPage.getMeasureAsColumnBucketBucket();
        assertEquals(metricsBucket.getItemNames(), asList(METRIC_AMOUNT, METRIC_WON));
        assertEquals(chartReport.getXLocationColumn(0, 0), chartReport.getXLocationColumn(1, 0));
        assertEquals(chartReport.getXLocationColumn(0, 1), chartReport.getXLocationColumn(1, 1));
        assertTrue(metricsBucket.expandMeasuresDisplayAs().isOptionCheck(OptionalStacking.MEASURES),
            "Stacked Measures checkbox should be checked");

        analysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();

        assertEquals(metricsBucket.getItemNames(), asList(METRIC_AMOUNT, METRIC_WON));
        assertEquals(chartReport.getXLocationColumn(0, 0), chartReport.getXLocationColumn(1, 0));
        assertEquals(chartReport.getXLocationColumn(0, 1), chartReport.getXLocationColumn(1, 1));
        assertTrue(analysisPage.getStacksBucket().isOptionCheck(OptionalStacking.MEASURES),
            "Stacked Measures checkbox should be checked");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void switchDualAxisOptionalStackingToComboChart() {
        indigoRestRequest.createInsight(
            new InsightMDConfiguration(INSIGHT_HAS_DUAL_AXIS, ReportType.COLUMN_CHART)
                .setMeasureBucket(
                    asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)),
                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_BEST_CASE)),
                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AVG_AMOUNT)),
                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_WON)))));

        MetricsBucket metricsBucket = initAnalysePage().openInsight(INSIGHT_HAS_DUAL_AXIS).waitForReportComputing()
            .getMetricsBucket();
        metricsBucket.getMetricConfiguration(METRIC_AVG_AMOUNT).expandConfiguration().checkShowOnSecondaryAxis();
        metricsBucket.getMetricConfiguration(METRIC_WON).expandConfiguration().checkShowOnSecondaryAxis();

        analysisPage.getStacksBucket().checkOption(OptionalStacking.MEASURES);

        ChartReport chartReport = analysisPage.changeReportType(ReportType.COMBO_CHART).waitForReportComputing()
            .getChartReport();
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(METRIC_AMOUNT, METRIC_BEST_CASE));
        assertEquals(analysisPage.getMetricsSecondaryBucket().getItemNames(), asList(METRIC_AVG_AMOUNT, METRIC_WON));

        assertEquals(chartReport.getXLocationColumn(0, 0), chartReport.getXLocationColumn(1, 0));

        assertEquals(chartReport.getTrackerType(0, 0), "rect");
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            singletonList(asList(METRIC_AMOUNT, "$116,625,456.54")));

        assertEquals(chartReport.getTrackerType(2, 0), "path");
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(2, 0),
            singletonList(asList(METRIC_AVG_AMOUNT, "$20,286.22")));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkComboChartHasAMeasure() {
        initAnalysePage().changeReportType(ReportType.COMBO_CHART).addMetric(METRIC_AMOUNT).waitForReportComputing();
        ChartReport chartReport = analysisPage.getChartReport();
        assertTrue(Integer.valueOf(chartReport.getHeightColumn(0, 0)) > 0);
        assertEquals(chartReport.getDataLabels(), asList("$116,625,456.54"));

        initAnalysePage().changeReportType(ReportType.COMBO_CHART).addMetricToSecondaryBucket(METRIC_AMOUNT)
            .waitForReportComputing();
        assertEquals(chartReport.getMarkersCount(), 1);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void prepareInsightsHaveInvalidValue() {
        indigoRestRequest.createInsight(
            new InsightMDConfiguration(INSIGHT_HAS_NO_MEASURES, ReportType.COMBO_CHART)
                .setCategoryBucket(singletonList(
                    CategoryBucket.createCategoryBucket(
                        getAttributeByTitle(ATTR_DEPARTMENT), CategoryBucket.Type.ATTRIBUTE))));

        indigoRestRequest.createInsight(
            new InsightMDConfiguration(INSIGHT_HAS_TOO_LARGE_REPORT_VALUES, ReportType.COMBO_CHART)
                .setMeasureBucket(singletonList(MeasureBucket
                    .createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT))))
                .setCategoryBucket(singletonList(
                    CategoryBucket.createCategoryBucket(
                        getAttributeByTitle(ATTR_ACCOUNT), CategoryBucket.Type.ATTRIBUTE))));

        indigoRestRequest.createInsight(
            new InsightMDConfiguration(INSIGHT_HAS_PLATFORM_LIMIT, ReportType.COMBO_CHART)
                .setMeasureBucket(singletonList(MeasureBucket
                    .createSimpleMeasureBucket(getMetricByTitle(METRIC_SNAPSHOT_BOP))))
                .setCategoryBucket(singletonList(
                    CategoryBucket.createCategoryBucket(
                        getAttributeByTitle(ATTR_ACTIVITY), CategoryBucket.Type.ATTRIBUTE))));

        String dateBigRange = "1/1/5000";
        initAnalysePage().changeReportType(ReportType.COMBO_CHART).addMetric(METRIC_AMOUNT).addDate()
            .waitForReportComputing().getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT).expandConfiguration()
            .addFilterByDate(DATE_DATASET_CLOSED, dateBigRange, dateBigRange);
        analysisPage.saveInsight(INSIGHT_HAS_VALID_VALUES).waitForReportComputing();

        String date = "01/01/2019";
        initAnalysePage().addMetric(METRIC_WON).waitForReportComputing().getMetricsBucket()
            .getMetricConfiguration(METRIC_WON).expandConfiguration().addFilterByDate(DATE_DATASET_CLOSED, date, date);
        analysisPage.saveInsight(INSIGHT_HAS_NO_VALUES).waitForReportComputing();
    }

    @DataProvider(name = "errorMessageOnCanvasProvider")
    public Object[][] getInsightHasErrorMessageOnCanvas() {
        return new Object[][]{
            {INSIGHT_HAS_NO_MEASURES, "NO MEASURE IN YOUR INSIGHT\nAdd a measure to your insight, or switch to table view." +
                "\nOnce done, you'll be able to save it."},
            {INSIGHT_HAS_TOO_LARGE_REPORT_VALUES, "TOO MANY DATA POINTS TO DISPLAY\nAdd a filter, or switch to table view."},
            {INSIGHT_HAS_PLATFORM_LIMIT, "TOO MANY DATA POINTS TO DISPLAY\nTry applying one or more filters."},
            {INSIGHT_HAS_VALID_VALUES, "SORRY, WE CAN'T DISPLAY THIS INSIGHT" +
                "\nTry applying different filters, or using different measures or attributes." +
                "\nIf this did not help, contact your administrator."},
            {INSIGHT_HAS_NO_VALUES, "NO DATA FOR YOUR FILTER SELECTION" +
                "\nTry adjusting or removing some of the filters."}
        };
    }

    @Test(dependsOnMethods = {"prepareInsightsHaveInvalidValue"}, dataProvider = "errorMessageOnCanvasProvider")
    public void checkComboChartHasInvalidValue(String insight, String errorMessage) {
        assertEquals(initAnalysePage().openInsight(insight).waitForReportComputing().getMainEditor()
            .getCanvasMessage(), errorMessage);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkComboChartHasSameNegativeValue() {
        final String metricNegativeValue = "Min of Amount";
        createMetric(metricNegativeValue, format("SELECT MIN([%s])",
            getMdService().getObjUri(getProject(), Fact.class, title("Amount"))), "#,##0.00");

        indigoRestRequest.createInsight(
            new InsightMDConfiguration(INSIGHT_HAS_NEGATIVE_VALUES, ReportType.COMBO_CHART)
                .setMeasureBucket(asList(
                    MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metricNegativeValue)),
                    MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metricNegativeValue)),
                    MeasureBucket.createMeasureBucket(getMetricByTitle(metricNegativeValue), Type.SECONDARY_MEASURES),
                    MeasureBucket.createMeasureBucket(getMetricByTitle(metricNegativeValue), Type.SECONDARY_MEASURES))));

        initAnalysePage().openInsight(INSIGHT_HAS_NEGATIVE_VALUES).waitForReportComputing();
        ChartReport chartReport = analysisPage.getChartReport();
        assertEquals(chartReport.getDataLabels(), asList("-400,000.00", "-400,000.00"));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(2, 0),
            singletonList(asList(metricNegativeValue, "-400,000.00")));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createComboChartViaRecommendedStepsPanelOnCanvas() {
        initAnalysePage().changeReportType(ReportType.COMBO_CHART).addMetricToRecommendedStepsPanelOnCanvas(METRIC_AMOUNT)
            .waitForNonEmptyBuckets().waitForReportComputing();
        assertEquals(analysisPage.getChartReport().getDataLabels(), asList("$116,625,456.54"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void switchFromComboChartToOtherChartAndVerseVice() {
        initAnalysePage().changeReportType(ReportType.COMBO_CHART).addMetric(METRIC_AMOUNT)
            .addMetricToSecondaryBucket(METRIC_AVG_AMOUNT).addDate().waitForReportComputing();

        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        AttributesBucket attributesBucket = analysisPage.getAttributesBucket();

        listReportTypes.forEach(type -> {
            analysisPage.changeReportType(type);
            checkRedBar(browser);
            assertThat(metricsBucket.getItemNames(), hasItems(METRIC_AMOUNT, METRIC_AVG_AMOUNT));
            assertThat(attributesBucket.getItemNames(), hasItem(DATE));

            ElementUtils.moveToElementActions(analysisPage.getPageHeader().getResetButton(), 1, 1).perform();
            analysisPage.changeReportType(ReportType.COMBO_CHART);
            MetricsBucket metricsSecondaryBucket = analysisPage.getMetricsSecondaryBucket();
            assertThat(metricsBucket.getItemNames(), hasItem(METRIC_AMOUNT));
            assertThat(metricsSecondaryBucket.getItemNames(), hasItem(METRIC_AVG_AMOUNT));
            assertThat(attributesBucket.getItemNames(), hasItem(DATE));
        });

        analysisPage.changeReportType(ReportType.HEAD_LINE).waitForReportComputing();
        assertThat(analysisPage.getMetricsBucket().getItemNames(), hasItem(METRIC_AMOUNT));
        assertThat(analysisPage.getMetricsSecondaryBucket().getItemNames(), hasItem(METRIC_AVG_AMOUNT));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void swapBetweenAMeasureInBuckets() {
        String insight = "Insight :" + generateHashString();
        indigoRestRequest.createInsight(
            new InsightMDConfiguration(insight, ReportType.COMBO_CHART)
                .setMeasureBucket(singletonList(MeasureBucket
                    .createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)))));

        initAnalysePage().openInsight(insight).waitForReportComputing();
        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        MetricsBucket metricsSecondaryBucket = analysisPage.getMetricsSecondaryBucket();

        ChartReport chartReport = analysisPage.drag(metricsBucket.get(METRIC_AMOUNT), metricsSecondaryBucket.getInvitation())
            .waitForReportComputing().getChartReport();
        assertEquals(chartReport.getMarkersCount(), 1);
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            singletonList(asList(METRIC_AMOUNT, "$116,625,456.54")));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void swapBetweenTwoMeasureInBuckets() {
        String insight = "Insight :" + generateHashString();
        indigoRestRequest.createInsight(
            new InsightMDConfiguration(insight, ReportType.COMBO_CHART)
                .setMeasureBucket(
                    asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AVG_AMOUNT)),
                        MeasureBucket.createMeasureBucket(getMetricByTitle(METRIC_AMOUNT), Type.SECONDARY_MEASURES))));

        ChartReport chartReport = initAnalysePage().openInsight(insight).waitForReportComputing().getChartReport();
        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        MetricsBucket metricsSecondaryBucket = analysisPage.getMetricsSecondaryBucket();

        analysisPage.drag(metricsBucket.get(METRIC_AVG_AMOUNT), metricsSecondaryBucket.getInvitation());
        analysisPage.drag(metricsSecondaryBucket.get(METRIC_AMOUNT), metricsBucket.getInvitation()).waitForReportComputing();

        assertEquals(chartReport.getTrackerType(0, 0), "rect");
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            singletonList(asList(METRIC_AMOUNT, "$116,625,456.54")));

        assertEquals(chartReport.getTrackerType(1, 0), "path");
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
            singletonList(asList(METRIC_AVG_AMOUNT, "$20,286.22")));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void swapBetweenTwoMeasuresInBuckets() {
        String insight = "Insight :" + generateHashString();
        indigoRestRequest.createInsight(
            new InsightMDConfiguration(insight, ReportType.COMBO_CHART)
                .setMeasureBucket(asList(MeasureBucket
                        .createSimpleMeasureBucket(getMetricByTitle(METRIC_AVG_AMOUNT)),
                    MeasureBucket
                        .createMeasureBucket(getMetricByTitle(METRIC_AMOUNT), Type.SECONDARY_MEASURES))));

        ChartReport chartReport = initAnalysePage().openInsight(insight).waitForReportComputing().getChartReport();
        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        MetricsBucket metricsSecondaryBucket = analysisPage.getMetricsSecondaryBucket();

        analysisPage.drag(metricsBucket.get(METRIC_AVG_AMOUNT), metricsSecondaryBucket.getInvitation());
        analysisPage.drag(metricsSecondaryBucket.get(METRIC_AMOUNT), metricsBucket.getInvitation()).waitForReportComputing();

        assertEquals(chartReport.getTrackerType(0, 0), "rect");
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            singletonList(asList(METRIC_AMOUNT, "$116,625,456.54")));

        assertEquals(chartReport.getTrackerType(1, 0), "path");
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
            singletonList(asList(METRIC_AVG_AMOUNT, "$20,286.22")));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void swapBetweenFourMeasuresInBuckets() {
        ChartReport chartReport = initAnalysePage().changeReportType(ReportType.COMBO_CHART).addMetric(METRIC_AMOUNT)
            .addMetric(ATTR_OPP_SNAPSHOT, FieldType.ATTRIBUTE).addMetricToSecondaryBucket(METRIC_AMOUNT_BOP)
            .addMetricToSecondaryBucket(ATTR_FORECAST_CATEGORY, FieldType.ATTRIBUTE).waitForReportComputing()
            .getChartReport();
        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        MetricsBucket metricsSecondaryBucket = analysisPage.getMetricsSecondaryBucket();

        analysisPage.drag(metricsBucket.get("Count of " + ATTR_OPP_SNAPSHOT), metricsSecondaryBucket.getInvitation());
        analysisPage.drag(metricsSecondaryBucket.get("Count of " + ATTR_FORECAST_CATEGORY),
            metricsBucket.getInvitation()).waitForReportComputing();

        assertEquals(chartReport.getTrackerType(1, 0), "rect");
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            singletonList(asList(METRIC_AMOUNT, "$116,625,456.54")));

        assertEquals(chartReport.getTrackerType(3, 0), "path");
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(3, 0),
            singletonList(asList("Count of " + ATTR_OPP_SNAPSHOT, "294,794")));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void swapBetweenPOPWithDateBuckets() throws NoSuchFieldException {
        initAnalysePage().changeReportType(ReportType.COMBO_CHART)
            .addMetric(METRIC_AMOUNT).addMetricToSecondaryBucket(METRIC_AVG_AMOUNT).addDate().waitForReportComputing();

        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel.changeCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_PREVIOUS_YEAR)
            .openCompareApplyMeasures().selectAllValues().apply();
        dateFilterPickerPanel.apply();
        analysisPage.waitForReportComputing();

        analysisPage.drag(analysisPage.getMetricsBucket().get(METRIC_AMOUNT + " - SP year ago"),
            analysisPage.getMetricsSecondaryBucket().getInvitation());
        assertEquals(analysisPage.getMetricsSecondaryBucket().getItemNames(),
            asList(METRIC_AVG_AMOUNT + " - SP year ago", METRIC_AVG_AMOUNT, METRIC_AMOUNT + " - SP year ago"));
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(METRIC_AMOUNT));
    }

    @Test(dependsOnMethods = "prepareInsights")
    public void replaceAttributeInViewBy() {
        initAnalysePage().openInsight(INSIGHT_HAS_MEASURE_AS_LINE_COLUMN_AND_ATTRIBUTE)
            .replaceAttribute(ATTR_FORECAST_CATEGORY).waitForReportComputing();
        assertEquals(analysisPage.getChartReport().getXaxisTitle(), ATTR_FORECAST_CATEGORY);
        assertEquals(analysisPage.getChartReport().getXaxisLabels(), asList("Exclude", "Include"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void disableOpenAsReportOptionWithComboChart() {
        String insight = "Insight :" + generateHashString();

        indigoRestRequest.createInsight(
            new InsightMDConfiguration(insight, ReportType.COMBO_CHART)
                .setMeasureBucket(singletonList(MeasureBucket
                    .createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT))))
                .setCategoryBucket(singletonList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT), CategoryBucket.Type.ATTRIBUTE))));

        ExportToSelect exportToSelect = initAnalysePage().openInsight(insight).getPageHeader().clickOptionsButton();
        assertFalse(exportToSelect.isOpenAsReportButtonEnabled(), "Open as Report option should be disabled");
        assertEquals(exportToSelect.getExportButtonTooltipText(),
            "The insight is not compatible with Report Editor. To open the insight as a report, " +
                "select another insight type.");
    }

    @Test(dependsOnMethods = "prepareInsights")
    public void changeGeneralComboChartConfigurations() {
        initAnalysePage().openInsight(INSIGHT_HAS_MEASURE_AS_LINE_COLUMN_AND_ATTRIBUTE)
            .waitForReportComputing().openConfigurationPanelBucket()
            .getItemConfiguration(X_AXIS.toString()).expandConfiguration().switchOff();
        assertEquals(analysisPage.waitForReportComputing().getChartReport().getXaxisTitle(), StringUtils.EMPTY);
        assertEquals(analysisPage.getChartReport().getXaxisLabels(), emptyList());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void addComboChartToKPIsDashboard() {
        initAnalysePage().changeReportType(ReportType.COMBO_CHART).addMetric(METRIC_AMOUNT)
            .addMetricToSecondaryBucket(METRIC_AVG_AMOUNT).addAttribute(ATTR_DEPARTMENT)
            .saveInsight(INSIGHT_IS_ADDED_DASHBOARD_FILLTER_ATTRIBUTE).waitForReportComputing();

        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage().addDashboard()
            .addInsight(INSIGHT_IS_ADDED_DASHBOARD_FILLTER_ATTRIBUTE)
            .selectDateFilterByName("All time").waitForWidgetsLoading();

        assertEquals(indigoDashboardsPage.getFirstWidget(Insight.class).getChartReport()
            .getDataLabels(), asList("$80,406,324.96", "$36,219,131.58"));
        assertEquals(indigoDashboardsPage.getFirstWidget(Insight.class).getChartReport().getXaxisLabels(),
            asList("Direct Sales", "Inside Sales"));

        indigoDashboardsPage.selectDateFilterByName("This month").selectFirstWidget(Insight.class);
        indigoDashboardsPage.waitForWidgetsLoading();

        assertThat(indigoDashboardsPage.getFirstWidget(Insight.class).getRoot().getText(),
            containsString("No data for your filter selection"));

        indigoDashboardsPage.selectDateFilterByName("All time").addAttributeFilter(ATTR_DEPARTMENT, "Inside Sales");

        assertEquals(indigoDashboardsPage.getFirstWidget(Insight.class).getChartReport().getDataLabels(),
            asList("$36,219,131.58"));
        assertEquals(indigoDashboardsPage.getFirstWidget(Insight.class).getChartReport().getXaxisLabels(),
            asList("Inside Sales"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void doSomeBasicActionsOnKD() {
        createComboSimpleInsight(INSIGHT_IS_ADDED_DASHBOARD, METRIC_AMOUNT, METRIC_AVG_AMOUNT, ATTR_DEPARTMENT);
        String insight = "Insight test reorder:" + generateHashString();
        createComboSimpleInsight(insight, METRIC_AMOUNT, METRIC_AVG_AMOUNT, ATTR_DEPARTMENT);

        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage().addDashboard()
            .addInsight(INSIGHT_IS_ADDED_DASHBOARD)
            .addInsight(insight)
            .selectDateFilterByName("All time").waitForWidgetsLoading();

        String newTitle = "New Title:" + generateHashString();
        Widget firstWidget = indigoDashboardsPage.getFirstWidget(Insight.class);
        Widget secondWidget = indigoDashboardsPage.getLastWidget(Insight.class);
        firstWidget.setHeadline(newTitle);

        assertEquals(firstWidget.getHeadline(), newTitle);
        firstWidget.setHeadline(INSIGHT_IS_ADDED_DASHBOARD);

        indigoDashboardsPage.dragWidget(firstWidget, secondWidget, Widget.DropZone.NEXT).waitForWidgetsLoading();
        assertEquals(indigoDashboardsPage.getInsightTitles(), asList(insight, INSIGHT_IS_ADDED_DASHBOARD));

        ChartReport chartReport = indigoDashboardsPage.getLastWidget(Insight.class).getChartReport();
        indigoDashboardsPage.getLastWidget(Insight.class).clickLegend(METRIC_AMOUNT);
        assertEquals(chartReport.getPrimaryYaxisTitle(), StringUtils.EMPTY);

        assertTrue(indigoDashboardsPage.searchInsight(INSIGHT_IS_ADDED_DASHBOARD),
            INSIGHT_IS_ADDED_DASHBOARD + " should exist");

        indigoDashboardsPage.selectLastWidget(Insight.class);
        indigoDashboardsPage.deleteInsightItem();
        assertEquals(indigoDashboardsPage.getInsightTitles(), asList(insight));
    }

    @Test(dependsOnMethods = "prepareInsights")
    public void checkTotalsResultWithMUF() throws ParseException, JSONException, IOException {
        createComboSimpleInsight(INSIGHT_IS_ADDED_DASHBOARD_MUF, METRIC_AMOUNT, METRIC_AVG_AMOUNT, ATTR_DEPARTMENT);

        final String newInsight = "Insight with MUF";
        final String productValues = format("[%s]",
            getMdService().getAttributeElements(getAttributeByTitle(ATTR_DEPARTMENT)).get(1).getUri());
        final String expression = format("[%s] IN (%s)", getAttributeByTitle(ATTR_DEPARTMENT).getUri(), productValues);
        DashboardRestRequest dashboardRestRequest = new DashboardRestRequest(getAdminRestClient(), testParams
            .getProjectId());
        final String mufUri = dashboardRestRequest.createMufObjectByUri("muf", expression);
        final UserManagementRestRequest userManagementRestRequest = new UserManagementRestRequest(
            new RestClient(getProfile(Profile.DOMAIN)), testParams.getProjectId());
        String assignedMufUserId = userManagementRestRequest
            .getUserProfileUri(testParams.getUserDomain(), testParams.getEditorUser());

        dashboardRestRequest.addMufToUser(assignedMufUserId, mufUri);

        ChartReport chartReport = initAnalysePage().openInsight(INSIGHT_IS_ADDED_DASHBOARD_MUF)
            .saveInsightAs(newInsight).getChartReport();
        Screenshots.takeScreenshot(browser, INSIGHT_IS_ADDED_DASHBOARD_MUF, getClass());

        assertEquals(chartReport.getDataLabels(), asList("$80,406,324.96", "$36,219,131.58"));
        assertEquals(chartReport.getXaxisLabels(), asList("Direct Sales", "Inside Sales"));
        logoutAndLoginAs(true, UserRoles.EDITOR);
        try {
            initAnalysePage().openInsight(newInsight).waitForReportComputing();
            Screenshots.takeScreenshot(browser, newInsight, getClass());

            assertEquals(chartReport.getDataLabels(), asList("$36,219,131.58"));
            assertEquals(chartReport.getXaxisLabels(), asList("Inside Sales"));
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testExportAndImportProjectWithInsight() {
        final int statusPollingCheckIterations = 60; // (60*5s)
        String exportToken = exportProject(
            true, true, true, statusPollingCheckIterations);
        testParams.setProjectId(targetProjectId);
        try {
            importProject(exportToken, statusPollingCheckIterations);
            ChartReport chartReport = initAnalysePage().openInsight(INSIGHT_HAS_MEASURE_AS_LINE_COLUMN_AND_ATTRIBUTE)
                .waitForReportComputing().getChartReport();

            assertEquals(chartReport.getTrackerType(0, 0), "rect");
            assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_DEPARTMENT, "Direct Sales"), asList(METRIC_AMOUNT, "$80,406,324.96")));

            assertEquals(chartReport.getTrackerType(1, 0), "path");
            assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
                asList(asList(ATTR_DEPARTMENT, "Inside Sales"), asList(METRIC_AVG_AMOUNT, "$18,329.52")));

        } finally {
            testParams.setProjectId(sourceProjectId);
        }
    }

    @Test(dependsOnMethods = {"testExportAndImportProjectWithInsight"})
    public void testPartialExportAndImportProject() {
        String exportToken = exportPartialProject(insightJsonObject, DEFAULT_PROJECT_CHECK_LIMIT);
        testParams.setProjectId(targetProjectId);
        try {
            importPartialProject(exportToken, DEFAULT_PROJECT_CHECK_LIMIT);
            ChartReport chartReport = initAnalysePage().openInsight(INSIGHT_HAS_MEASURE_AS_LINE_COLUMN_AND_ATTRIBUTE)
                .waitForReportComputing().getChartReport();

            assertEquals(chartReport.getTrackerType(0, 0), "rect");
            assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList(ATTR_DEPARTMENT, "Direct Sales"), asList(METRIC_AMOUNT, "$80,406,324.96")));

            assertEquals(chartReport.getTrackerType(1, 0), "path");
            assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
                asList(asList(ATTR_DEPARTMENT, "Inside Sales"), asList(METRIC_AVG_AMOUNT, "$18,329.52")));
        } finally {
            testParams.setProjectId(sourceProjectId);
            if (nonNull(targetProjectId)) {
                deleteProject(targetProjectId);
            }
        }
    }

    private String createComboSimpleInsight(String title, String metric, String secondaryMetric,
                                            String attribute) {
        return indigoRestRequest.createInsight(
            new InsightMDConfiguration(title, ReportType.COMBO_CHART)
                .setMeasureBucket(asList(
                    MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric)),
                    MeasureBucket.createMeasureBucket(getMetricByTitle(secondaryMetric), Type.SECONDARY_MEASURES)))
                .setCategoryBucket(singletonList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(attribute), CategoryBucket.Type.ATTRIBUTE))));
    }
}
