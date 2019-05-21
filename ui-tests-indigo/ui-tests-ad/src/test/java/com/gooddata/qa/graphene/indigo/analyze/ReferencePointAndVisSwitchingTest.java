package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricsBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_REGION;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AVG_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_WON;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_FORECAST_CATEGORY;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;

public class ReferencePointAndVisSwitchingTest extends AbstractAnalyseTest {

    private static final String INSIGHT_HAS_SOME_MEASURES_AND_SOME_ATTRIBUTES = "Some measures, attributes";
    private IndigoRestRequest indigoRestRequest;
    private ProjectRestRequest projectRestRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Create reference point and vis switching";
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createAmountMetric();
        metrics.createAvgAmountMetric();
        metrics.createWonMetric();
        projectRestRequest= new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProject(ProjectFeatureFlags.ENABLE_METRIC_DATE_FILTER, true);
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)),
                testParams.getProjectId());
    }

    @Test(dependsOnGroups = "createProject")
    public void prepareInsights() {
        createTableInsight(INSIGHT_HAS_SOME_MEASURES_AND_SOME_ATTRIBUTES);
    }

    @Test(dependsOnMethods = {"prepareInsights"})
    public void switchToChartDoesNotAllowCombination() {
        initAnalysePage().openInsight(INSIGHT_HAS_SOME_MEASURES_AND_SOME_ATTRIBUTES)
                .addFilter(ATTR_DEPARTMENT).addFilter(ATTR_FORECAST_CATEGORY).addFilter(ATTR_REGION)
                .changeReportType(ReportType.PIE_CHART).waitForReportComputing();

        assertEquals(analysisPage.getMetricsBucket().getItemNames(), singletonList(METRIC_AMOUNT));
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), singletonList(ATTR_DEPARTMENT));
        assertEquals(parseFilterText(analysisPage.getFilterBuckets()
                .getFilterText(ATTR_DEPARTMENT)), asList(ATTR_DEPARTMENT, "All"));
    }

    @Test(dependsOnMethods = {"prepareInsights"})
    public void switchToChartAllowCombination() {
        initAnalysePage().openInsight(INSIGHT_HAS_SOME_MEASURES_AND_SOME_ATTRIBUTES)
            .addFilter(ATTR_DEPARTMENT).addFilter(ATTR_FORECAST_CATEGORY).addFilter(ATTR_REGION)
            .changeReportType(ReportType.PIE_CHART)
            .changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();

        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT));
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(ATTR_DEPARTMENT, ATTR_FORECAST_CATEGORY));
        assertEquals(parseFilterText(analysisPage.getFilterBuckets()
                .getFilterText(ATTR_DEPARTMENT)), asList(ATTR_DEPARTMENT, "All"));
        assertEquals(parseFilterText(analysisPage.getFilterBuckets()
                .getFilterText(ATTR_FORECAST_CATEGORY)), asList(ATTR_FORECAST_CATEGORY, "All"));
    }

    @Test(dependsOnMethods = {"prepareInsights"})
    public void replaceAttributeToCreateANewReferencePoint() {
        initAnalysePage().openInsight(INSIGHT_HAS_SOME_MEASURES_AND_SOME_ATTRIBUTES)
                .changeReportType(ReportType.COLUMN_CHART)
                .replaceAttribute(ATTR_DEPARTMENT, ATTR_STAGE_NAME).changeReportType(ReportType.TABLE)
                .waitForReportComputing();

        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT));
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(ATTR_STAGE_NAME, ATTR_FORECAST_CATEGORY));
    }

    @Test(dependsOnMethods = {"prepareInsights"})
    public void addNewMetricToCreateANewReferencePoint() {
        initAnalysePage().openInsight(INSIGHT_HAS_SOME_MEASURES_AND_SOME_ATTRIBUTES)
                .changeReportType(ReportType.COLUMN_CHART)
                .addMetric(METRIC_WON).changeReportType(ReportType.TABLE)
                .waitForReportComputing();

        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT, METRIC_WON));
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(ATTR_DEPARTMENT, ATTR_FORECAST_CATEGORY));
    }

    @Test(dependsOnMethods = {"prepareInsights"})
    public void removeSecondaryMetricToCreateANewReferencePoint() {
        initAnalysePage().openInsight(INSIGHT_HAS_SOME_MEASURES_AND_SOME_ATTRIBUTES)
                .changeReportType(ReportType.COLUMN_CHART)
                .removeMetric(METRIC_AVG_AMOUNT).changeReportType(ReportType.TABLE)
                .waitForReportComputing();

        assertEquals(analysisPage.getMetricsBucket().getItemNames(), singletonList(METRIC_AMOUNT));
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(ATTR_DEPARTMENT, ATTR_FORECAST_CATEGORY));
    }

    @Test(dependsOnMethods = {"prepareInsights"})
    public void changeValuesOnFiltersToCreateANewReferencePoint() {
        initAnalysePage().openInsight(INSIGHT_HAS_SOME_MEASURES_AND_SOME_ATTRIBUTES)
                .addFilter(ATTR_DEPARTMENT).addFilter(ATTR_FORECAST_CATEGORY).addFilter(ATTR_REGION)
                .changeReportType(ReportType.COLUMN_CHART)
                .getFilterBuckets().configAttributeFilter(ATTR_DEPARTMENT, "Inside Sales");
        analysisPage.changeReportType(ReportType.TABLE).waitForReportComputing();

        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT));
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(ATTR_DEPARTMENT, ATTR_FORECAST_CATEGORY));
        assertEquals(parseFilterText(analysisPage.getFilterBuckets()
                .getFilterText(ATTR_DEPARTMENT)), asList(ATTR_DEPARTMENT, "Inside Sales"));
        assertEquals(parseFilterText(analysisPage.getFilterBuckets()
            .getFilterText(ATTR_FORECAST_CATEGORY)), asList(ATTR_FORECAST_CATEGORY, "All"));
    }

    @Test(dependsOnMethods = {"prepareInsights"})
    public void addFilterOnMeasureToCreateANewReferencePoint() {
        initAnalysePage().openInsight(INSIGHT_HAS_SOME_MEASURES_AND_SOME_ATTRIBUTES)
                .changeReportType(ReportType.COLUMN_CHART);
        MetricsBucket metricsBucket = analysisPage.getMetricsBucket();
        metricsBucket.getMetricConfiguration(METRIC_AMOUNT).expandConfiguration().addFilterWithAllValue(ATTR_STAGE_NAME);
        analysisPage.changeReportType(ReportType.TABLE).waitForReportComputing();

        assertEquals(metricsBucket.getMetricConfiguration(METRIC_AMOUNT).getSubHeader(), "Stage Name: All");
        assertEquals(metricsBucket.getItemNames(), asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT));
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(ATTR_DEPARTMENT, ATTR_FORECAST_CATEGORY));
    }

    @Test(dependsOnMethods = {"prepareInsights"})
    public void changeDateDimensionToCreateANewReferencePoint() {
        initAnalysePage().addMetric(METRIC_AMOUNT).addMetric(METRIC_AVG_AMOUNT).addDate()
                .changeReportType(ReportType.COLUMN_CHART);
        analysisPage.getAttributesBucket().changeDateDimension(DATE_DATASET_CREATED);
        analysisPage.changeReportType(ReportType.TABLE).waitForReportComputing();

        assertEquals(analysisPage.getAttributesBucket().getSelectedDimensionSwitch(), DATE_DATASET_CREATED);
    }

    @Test(dependsOnMethods = {"prepareInsights"})
    public void keepOldReferencePointWhenSwitchToOtherReport() {
        initAnalysePage().openInsight(INSIGHT_HAS_SOME_MEASURES_AND_SOME_ATTRIBUTES)
                .addDateFilter().changeReportType(ReportType.COLUMN_CHART);
        FiltersBucket filterBucket = analysisPage.getFilterBuckets();
        filterBucket.openDatePanelOfFilter(filterBucket.getDateFilter());
        analysisPage.changeReportType(ReportType.TABLE).waitForReportComputing();

        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT));
        assertEquals(analysisPage.getAttributesBucket().getItemNames(),
                asList(ATTR_DEPARTMENT, ATTR_FORECAST_CATEGORY, ATTR_REGION));
    }

    private void createTableInsight(String title) {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(title, ReportType.TABLE)
                        .setMeasureBucket(
                                asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)),
                                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AVG_AMOUNT))))
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                        CategoryBucket.Type.ATTRIBUTE),
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                                        CategoryBucket.Type.ATTRIBUTE),
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_REGION),
                                        CategoryBucket.Type.ATTRIBUTE))));
    }
}
