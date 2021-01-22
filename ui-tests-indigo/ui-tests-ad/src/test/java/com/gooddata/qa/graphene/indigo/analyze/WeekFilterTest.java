package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.visualization.FilterDate;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.FilterAttribute;
import com.gooddata.qa.graphene.enums.DateGranularity;
import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.CompareTypeDropdown;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributesBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.DateFilterPickerPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.sdk.model.md.Attribute;
import com.gooddata.sdk.model.md.Fact;
import com.gooddata.sdk.model.md.Restriction;
import org.apache.commons.lang3.tuple.Pair;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags.XAE_VERSION;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_FORECAST_CATEGORY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT_YEAR_AGO;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SUM_OF_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DIMENSION_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_QUARTER_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_MONTH_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_WEEK_SUN_SAT_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DATE_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.UrlParserUtils.getObjId;
import static com.gooddata.sdk.model.md.Restriction.title;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;

public class WeekFilterTest extends AbstractAnalyseTest {

    private static final String GOODDATA_DATE_SNAPSHOT_DATASET_ID = "snapshot.dataset.dt";
    private static final String GOODDATA_DATE_CREATED_DATASET_ID = "created.dataset.dt";
    private static final String METRIC_CONTAINS_FOR_PREVIOUS_YEAR = "Metric Previous Year";
    private static final String METRIC_CONTAINS_FOR_PREVIOUS_QUARTER = "Metric Previous Quarter";
    private static final String METRIC_CONTAINS_FOR_PREVIOUS_MONTH = "Metric Previous Month";
    private static final String METRIC_CONTAINS_FOR_PREVIOUS_WEEK = "Metric Previous Week";
    private static final String METRIC_CONTAINS_FOR_PREVIOUS_DAY = "Metric Previous Day";
    private static final String METRIC_CONTAINS_FOR_PREVIOUS_TWO_YEAR = "Metric Previous Two Year";
    private static final String METRIC_CONTAINS_FOR_PREVIOUS_TWO_QUARTER = "Metric Previous Two Quarter";
    private static final String METRIC_CONTAINS_FOR_PREVIOUS_TWO_WEEK = "Metric Previous Two Week";
    private static final String URN_CUSTOM_V2_DATE = "urn:custom_v2:date";
    private static final String INSIGHT_HAS_POP_METRIC_IN_A_CONTEXT_OF_WEEKS =
        "Insight has pop metric in a context of weeks";
    private static final String INSIGHT_HAS_SOME_GLOBAL_AND_LOCAL_FILTER =
        "Insight has global and local filter";
    private static final String INSIGHT_HAS_METRIC_CONTAINS_FOR_PREVIOUS_WEEK = "Insight Has Metric Previous Week";
    private static final String DASHBOARD_HAS_METRIC_CONTAINS_FOR_PREVIOUS_WEEK =
        "Dashboard has metric contains for previous week";

    private ProjectRestRequest projectRestRequest;
    private CommonRestRequest commonRestRequest;
    private IndigoRestRequest indigoRestRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Week-Filtering";
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createAmountMetric();
        metrics.createAvgAmountMetric();

        projectRestRequest = new ProjectRestRequest(
            new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        commonRestRequest = new CommonRestRequest(new RestClient(getProfile(ADMIN)), testParams.getProjectId());
        projectRestRequest.upgradeCustomV2Date(commonRestRequest, GOODDATA_DATE_SNAPSHOT_DATASET_ID);
        projectRestRequest.upgradeCustomV2Date(commonRestRequest, GOODDATA_DATE_CREATED_DATASET_ID);
        projectRestRequest.updateProjectConfiguration(XAE_VERSION.getFlagName(), "3");

        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_METRIC_DATE_FILTER, true);
        projectRestRequest.setFeatureFlagInProject(ProjectFeatureFlags.ENABLE_WEEK_FILTERS, true);

        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());

        prepareMetricsContainsForPreviousExpression();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkApplyingNewDateDimension() throws IOException {
        assertEquals(commonRestRequest.getJsonObject(getDatasetByIdentifier(GOODDATA_DATE_SNAPSHOT_DATASET_ID).getUri())
            .getJSONObject("dataSet").getJSONObject("content").get("urn"), URN_CUSTOM_V2_DATE);
    }

    @Test(dependsOnMethods = {"checkApplyingNewDateDimension"})
    public void applyPOPMetricInContextOfWeek() throws NoSuchFieldException{
        List<Pair<String, Integer>> objectElementsByID = getObjectElementsByID(Integer.parseInt(getObjId(
            getAttributeByTitle(ATTR_FORECAST_CATEGORY).getDefaultDisplayForm().getUri())));

        indigoRestRequest.createInsight(
            new InsightMDConfiguration(INSIGHT_HAS_POP_METRIC_IN_A_CONTEXT_OF_WEEKS, ReportType.COLUMN_CHART)
                .setMeasureBucket(asList(MeasureBucket
                        .createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT))))
                .setCategoryBucket(asList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DATE_SNAPSHOT),
                        CategoryBucket.Type.VIEW)))
                .setFilter(singletonList(FilterAttribute.createFilter(
                    getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                    singletonList(objectElementsByID.get(0).getRight()))))
                .setDateFilter(FilterDate.createFilterDate(getDatasetByTitle(ATTR_DATE_SNAPSHOT).getUri(),
                    "2000-06-06", "2020-07-05")));

        initAnalysePage().openInsight(INSIGHT_HAS_POP_METRIC_IN_A_CONTEXT_OF_WEEKS).waitForReportComputing()
            .getAttributesBucket().changeGranularity(DateGranularity.WEEK_SUN_SAT);

        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        dateFilterPickerPanel.changeCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_PREVIOUS_YEAR)
            .openCompareApplyMeasures().selectByNames(METRIC_AMOUNT).apply();
        dateFilterPickerPanel.apply();

        ChartReport chartReport = analysisPage.waitForReportComputing().getChartReport();
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
            asList(asList("Week (Sun-Sat)/Year (Snapshot)", "W24/2010"), asList(METRIC_AMOUNT, "$2,888,811.27")));
        chartReport.clickLegend(METRIC_AMOUNT);

        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            asList(asList("Week (Sun-Sat)/Year (Snapshot)", "W24/2011"), asList(METRIC_AMOUNT_YEAR_AGO, "$2,888,811.27")));

        assertEquals(chartReport.getXaxisLabels().get(0), "W24/2010");
        analysisPage.saveInsight().waitForReportComputing();
    }

    @Test(dependsOnMethods = {"applyPOPMetricInContextOfWeek"})
    public void placeOnKPIDashboardPOPMetricInContextOfWeek() throws IOException{
        String dashboardTitle = generateHashString();
        final String dashboardUri = indigoRestRequest.createAnalyticalDashboard(
            singletonList(
                indigoRestRequest.createVisualizationWidget(
                    indigoRestRequest.getInsightUri(INSIGHT_HAS_POP_METRIC_IN_A_CONTEXT_OF_WEEKS),
                    INSIGHT_HAS_POP_METRIC_IN_A_CONTEXT_OF_WEEKS
                )
            ), dashboardTitle);
        indigoRestRequest.editWidthOfWidget(dashboardUri,0,0, 12);

        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage().selectKpiDashboard(dashboardTitle);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.waitForWidgetsLoading();

        ChartReport chartReport = indigoDashboardsPage
            .getWidgetByHeadline(Insight.class, INSIGHT_HAS_POP_METRIC_IN_A_CONTEXT_OF_WEEKS).getChartReport();

        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
            asList(asList("Week (Sun-Sat)/Year (Snapshot)", "W24/2010"), asList(METRIC_AMOUNT, "$2,888,811.27")));

        assertThat(chartReport.getXaxisLabels(), hasItems("W24/2010", "W24/2011"));
    }

    @Test(dependsOnMethods = {"checkApplyingNewDateDimension"})
    public void applyMetricContainForPreviousYear() {
        String insight = generateHashString();
        indigoRestRequest.createInsight(
            new InsightMDConfiguration(insight, ReportType.COLUMN_CHART)
                .setMeasureBucket(asList(MeasureBucket
                        .createSimpleMeasureBucket(getMetricByTitle(METRIC_SUM_OF_AMOUNT)),
                    MeasureBucket
                        .createSimpleMeasureBucket(getMetricByTitle(METRIC_CONTAINS_FOR_PREVIOUS_YEAR)),
                    MeasureBucket
                        .createSimpleMeasureBucket(getMetricByTitle(METRIC_CONTAINS_FOR_PREVIOUS_TWO_YEAR))))
                .setCategoryBucket(asList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_YEAR_SNAPSHOT),
                        CategoryBucket.Type.VIEW))));

        initAnalysePage().openInsight(insight).waitForReportComputing().getAttributesBucket()
            .changeGranularity(DateGranularity.WEEK_SUN_SAT);
        ChartReport chartReport = analysisPage.waitForReportComputing().getChartReport();

        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            asList(asList("Week (Sun-Sat)/Year (Snapshot)", "W24/2010"), asList(METRIC_SUM_OF_AMOUNT, "5,134,398")));
        chartReport.clickLegend(METRIC_SUM_OF_AMOUNT);
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
            asList(asList("Week (Sun-Sat)/Year (Snapshot)", "W24/2011"), asList(METRIC_CONTAINS_FOR_PREVIOUS_YEAR, "5,134,398")));

        chartReport.clickLegend(METRIC_CONTAINS_FOR_PREVIOUS_YEAR);
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(2, 0),
            asList(asList("Week (Sun-Sat)/Year (Snapshot)", "W24/2012"), asList(METRIC_CONTAINS_FOR_PREVIOUS_TWO_YEAR, "5,134,398")));

        assertEquals(chartReport.getXaxisLabels().get(0), "W24/2010");
    }

    @Test(dependsOnMethods = {"checkApplyingNewDateDimension"})
    public void applyMetricContainForPreviousQuarter() {
        String insight = generateHashString();
        indigoRestRequest.createInsight(
            new InsightMDConfiguration(insight, ReportType.COLUMN_CHART)
                .setMeasureBucket(asList(MeasureBucket
                        .createSimpleMeasureBucket(getMetricByTitle(METRIC_SUM_OF_AMOUNT)),
                    MeasureBucket
                        .createSimpleMeasureBucket(getMetricByTitle(METRIC_CONTAINS_FOR_PREVIOUS_QUARTER)),
                    MeasureBucket
                        .createSimpleMeasureBucket(getMetricByTitle(METRIC_CONTAINS_FOR_PREVIOUS_TWO_QUARTER))))
                .setCategoryBucket(asList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_YEAR_SNAPSHOT),
                        CategoryBucket.Type.VIEW))));

        initAnalysePage().openInsight(insight).waitForReportComputing().getAttributesBucket()
            .changeGranularity(DateGranularity.WEEK_SUN_SAT);
        ChartReport chartReport = analysisPage.waitForReportComputing().getChartReport();

        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            asList(asList("Week (Sun-Sat)/Year (Snapshot)", "W24/2010"), asList(METRIC_SUM_OF_AMOUNT, "5,134,398")));
        chartReport.clickLegend(METRIC_SUM_OF_AMOUNT);
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
            asList(asList("Week (Sun-Sat)/Year (Snapshot)", "W37/2010"), asList(METRIC_CONTAINS_FOR_PREVIOUS_QUARTER, "5,134,398")));
        chartReport.clickLegend(METRIC_CONTAINS_FOR_PREVIOUS_QUARTER);
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(2, 0),
            asList(asList("Week (Sun-Sat)/Year (Snapshot)", "W50/2010"), asList(METRIC_CONTAINS_FOR_PREVIOUS_TWO_QUARTER, "5,134,398")));
        assertEquals(chartReport.getXaxisLabels().get(0), "W24/2010");
    }

    @Test(dependsOnMethods = {"checkApplyingNewDateDimension"})
    public void applyMetricContainForPreviousWeek() {
        indigoRestRequest.createInsight(
            new InsightMDConfiguration(INSIGHT_HAS_METRIC_CONTAINS_FOR_PREVIOUS_WEEK, ReportType.COLUMN_CHART)
                .setMeasureBucket(asList(MeasureBucket
                        .createSimpleMeasureBucket(getMetricByTitle(METRIC_SUM_OF_AMOUNT)),
                    MeasureBucket
                        .createSimpleMeasureBucket(getMetricByTitle(METRIC_CONTAINS_FOR_PREVIOUS_WEEK)),
                    MeasureBucket
                        .createSimpleMeasureBucket(getMetricByTitle(METRIC_CONTAINS_FOR_PREVIOUS_TWO_WEEK))))
                .setCategoryBucket(asList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_YEAR_SNAPSHOT),
                        CategoryBucket.Type.VIEW))));

        initAnalysePage().openInsight(INSIGHT_HAS_METRIC_CONTAINS_FOR_PREVIOUS_WEEK).waitForReportComputing()
            .getAttributesBucket().changeGranularity(DateGranularity.WEEK_SUN_SAT);
        ChartReport chartReport = analysisPage.saveInsight().waitForReportComputing().getChartReport();

        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            asList(asList("Week (Sun-Sat)/Year (Snapshot)", "W24/2010"), asList(METRIC_SUM_OF_AMOUNT, "5,134,398")));
        chartReport.clickLegend(METRIC_SUM_OF_AMOUNT);

        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
            asList(asList("Week (Sun-Sat)/Year (Snapshot)", "W25/2010"), asList(METRIC_CONTAINS_FOR_PREVIOUS_WEEK, "5,134,398")));

        chartReport.clickLegend(METRIC_CONTAINS_FOR_PREVIOUS_WEEK);
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(2, 0),
            asList(asList("Week (Sun-Sat)/Year (Snapshot)", "W26/2010"), asList(METRIC_CONTAINS_FOR_PREVIOUS_TWO_WEEK, "5,134,398")));
        assertEquals(chartReport.getXaxisLabels().get(0), "W24/2010");
    }

    @Test(dependsOnMethods = {"checkApplyingNewDateDimension"})
    public void applyMetricContainForPreviousMonth() {
        String insight = generateHashString();
        indigoRestRequest.createInsight(
            new InsightMDConfiguration(insight, ReportType.COLUMN_CHART)
                .setMeasureBucket(asList(MeasureBucket
                        .createSimpleMeasureBucket(getMetricByTitle(METRIC_SUM_OF_AMOUNT)),
                    MeasureBucket
                        .createSimpleMeasureBucket(getMetricByTitle(METRIC_CONTAINS_FOR_PREVIOUS_MONTH))))
                .setCategoryBucket(asList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_YEAR_SNAPSHOT),
                        CategoryBucket.Type.VIEW))));

        initAnalysePage().openInsight(insight).waitForReportComputing().getAttributesBucket()
            .changeGranularity(DateGranularity.WEEK_SUN_SAT);
        ChartReport chartReport = analysisPage.waitForReportComputing().getChartReport();

        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            asList(asList("Week (Sun-Sat)/Year (Snapshot)", "W24/2010"), asList(METRIC_SUM_OF_AMOUNT, "5,134,398")));
        chartReport.clickLegend(METRIC_SUM_OF_AMOUNT);

        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
            asList(asList("Week (Sun-Sat)/Year (Snapshot)", "W24/2010"), asList(METRIC_CONTAINS_FOR_PREVIOUS_MONTH, "5,134,398")));
    }

    @Test(dependsOnMethods = {"checkApplyingNewDateDimension"})
    public void applyMetricContainForPreviousDateMonthDay() {
        String insight = generateHashString();
        indigoRestRequest.createInsight(
            new InsightMDConfiguration(insight, ReportType.COLUMN_CHART)
                .setMeasureBucket(asList(MeasureBucket
                        .createSimpleMeasureBucket(getMetricByTitle(METRIC_SUM_OF_AMOUNT)),
                    MeasureBucket
                        .createSimpleMeasureBucket(getMetricByTitle(METRIC_CONTAINS_FOR_PREVIOUS_DAY))))
                .setCategoryBucket(asList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_YEAR_SNAPSHOT),
                        CategoryBucket.Type.VIEW))));

        AttributesBucket attributesBucket = initAnalysePage().openInsight(insight)
            .getAttributesBucket();
        attributesBucket.changeGranularity(DateGranularity.WEEK_SUN_SAT);

        ChartReport chartReport = analysisPage.waitForReportComputing().getChartReport();

        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            asList(asList("Week (Sun-Sat)/Year (Snapshot)", "W24/2010"), asList(METRIC_SUM_OF_AMOUNT, "5,134,398")));
        chartReport.clickLegend(METRIC_SUM_OF_AMOUNT);

        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
            asList(asList("Week (Sun-Sat)/Year (Snapshot)", "W24/2010"), asList(METRIC_CONTAINS_FOR_PREVIOUS_DAY, "5,134,398")));

        attributesBucket.changeGranularity(DateGranularity.DAY);

        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            asList(asList(DATE_DIMENSION_SNAPSHOT, "06/06/2010"), asList(METRIC_SUM_OF_AMOUNT, "5,134,398")));
        chartReport.clickLegend(METRIC_SUM_OF_AMOUNT);

        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
            asList(asList(DATE_DIMENSION_SNAPSHOT, "06/07/2010"), asList(METRIC_CONTAINS_FOR_PREVIOUS_DAY, "5,134,398")));
    }

    @Test(dependsOnMethods = {"checkApplyingNewDateDimension"})
    public void applyContextOfWeekSomeFilter() {
        List<Pair<String, Integer>> objectElementsByID = getObjectElementsByID(Integer.parseInt(getObjId(
            getAttributeByTitle(ATTR_STAGE_NAME).getDefaultDisplayForm().getUri())));

        indigoRestRequest.createInsight(
            new InsightMDConfiguration(INSIGHT_HAS_SOME_GLOBAL_AND_LOCAL_FILTER, ReportType.COLUMN_CHART)
                .setMeasureBucket(asList(MeasureBucket
                    .createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT))))
                .setCategoryBucket(asList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DATE_SNAPSHOT),
                        CategoryBucket.Type.VIEW)))
                .setFilter(singletonList(FilterAttribute.createFilter(
                    getAttributeByTitle(ATTR_STAGE_NAME), asList(objectElementsByID.get(0).getRight(),
                        objectElementsByID.get(1).getRight(), objectElementsByID.get(2).getRight(),
                        objectElementsByID.get(3).getRight(), objectElementsByID.get(4).getRight()))))
                .setDateFilter(FilterDate.createFilterDate(getDatasetByTitle(ATTR_DATE_SNAPSHOT).getUri(),
                    "2020-06-06", "2020-07-05")));

        initAnalysePage().openInsight(INSIGHT_HAS_SOME_GLOBAL_AND_LOCAL_FILTER).waitForReportComputing()
            .getAttributesBucket().changeGranularity(DateGranularity.WEEK_SUN_SAT);

        analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT)
            .expandConfiguration().addFilter(
                ATTR_STAGE_NAME, "Risk Assessment", "Conviction", "Negotiation", "Closed Won", "Closed Lost")
            .addFilterByDate(DATE_DATASET_SNAPSHOT, "06/07/2000", "07/06/2020");

        ChartReport chartReport = analysisPage.waitForReportComputing().saveInsight().getChartReport();

        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            asList(asList("Week (Sun-Sat)/Year (Snapshot)", "W24/2010"),
                asList("Amount, Snapshot: Jun 7, 2000 - Jul 6, 2020: Stage Name (5)", "$2,625,010.75")));

        assertEquals(chartReport.getXaxisLabels().get(0), "W24/2010");
    }

    @Test(dependsOnMethods = {"checkApplyingNewDateDimension"})
    public void placeOnKPIDashboardForPreviousInContextOfWeek() throws IOException{
        final String dashboardUri = indigoRestRequest.createAnalyticalDashboard(
            singletonList(
                indigoRestRequest.createVisualizationWidget(
                    indigoRestRequest.getInsightUri(INSIGHT_HAS_METRIC_CONTAINS_FOR_PREVIOUS_WEEK),
                    INSIGHT_HAS_METRIC_CONTAINS_FOR_PREVIOUS_WEEK
                )
            ), DASHBOARD_HAS_METRIC_CONTAINS_FOR_PREVIOUS_WEEK);
        indigoRestRequest.editWidthOfWidget(dashboardUri,0,0, 12);

        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPageWithWidgets();
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.waitForWidgetsLoading();

        Insight insight = indigoDashboardsPage
            .getWidgetByHeadline(Insight.class, INSIGHT_HAS_METRIC_CONTAINS_FOR_PREVIOUS_WEEK);
        ChartReport chartReport = insight.getChartReport();

        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
            asList(asList("Week (Sun-Sat)/Year (Snapshot)", "W24/2010"), asList(METRIC_SUM_OF_AMOUNT, "5,134,398")));
        insight.clickLegend(METRIC_SUM_OF_AMOUNT);

        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(1, 0),
            asList(asList("Week (Sun-Sat)/Year (Snapshot)", "W25/2010"), asList(METRIC_CONTAINS_FOR_PREVIOUS_WEEK, "5,134,398")));

        insight.clickLegend(METRIC_CONTAINS_FOR_PREVIOUS_WEEK);
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(2, 0),
            asList(asList("Week (Sun-Sat)/Year (Snapshot)", "W26/2010"), asList(METRIC_CONTAINS_FOR_PREVIOUS_TWO_WEEK, "5,134,398")));
        assertEquals(chartReport.getXaxisLabels().get(0), "W24/2010");
    }

    private void prepareMetricsContainsForPreviousExpression() {
        createMetric(METRIC_SUM_OF_AMOUNT,
            format("SELECT SUM([%s])", getMdService().getObjUri(getProject(), Fact.class, title(METRIC_AMOUNT)),
                DEFAULT_CURRENCY_METRIC_FORMAT));

        List<Pair<String, String>> metrics = asList(Pair.of(METRIC_CONTAINS_FOR_PREVIOUS_YEAR, ATTR_YEAR_SNAPSHOT),
            Pair.of(METRIC_CONTAINS_FOR_PREVIOUS_QUARTER, ATTR_QUARTER_YEAR_SNAPSHOT),
            Pair.of(METRIC_CONTAINS_FOR_PREVIOUS_MONTH, ATTR_MONTH_YEAR_SNAPSHOT),
            Pair.of(METRIC_CONTAINS_FOR_PREVIOUS_WEEK, ATTR_WEEK_SUN_SAT_SNAPSHOT),
            Pair.of(METRIC_CONTAINS_FOR_PREVIOUS_DAY, ATTR_DATE_SNAPSHOT));

        for (int i = 0; i < metrics.size(); i++) {
            createMetric(metrics.get(i).getLeft(),
                format("SELECT SUM([%s]) FOR Previous([%s])",
                    getMdService().getObjUri(getProject(), Fact.class, title(METRIC_AMOUNT)),
                    getMdService().getObjUri(getProject(), Attribute.class, Restriction.title(metrics.get(i).getRight()))),
                DEFAULT_METRIC_FORMAT);
        }

        metrics = asList(Pair.of(METRIC_CONTAINS_FOR_PREVIOUS_TWO_YEAR, ATTR_YEAR_SNAPSHOT),
            Pair.of(METRIC_CONTAINS_FOR_PREVIOUS_TWO_QUARTER, ATTR_QUARTER_YEAR_SNAPSHOT),
            Pair.of(METRIC_CONTAINS_FOR_PREVIOUS_TWO_WEEK, ATTR_WEEK_SUN_SAT_SNAPSHOT));

        for (int i = 0; i < metrics.size(); i++) {
            createMetric(metrics.get(i).getLeft(),
                format("SELECT SUM([%s]) FOR Previous([%s], 2)",
                    getMdService().getObjUri(getProject(), Fact.class, title(METRIC_AMOUNT)),
                    getMdService().getObjUri(getProject(), Attribute.class, Restriction.title(metrics.get(i).getRight()))),
                DEFAULT_METRIC_FORMAT);
        }
    }
}
