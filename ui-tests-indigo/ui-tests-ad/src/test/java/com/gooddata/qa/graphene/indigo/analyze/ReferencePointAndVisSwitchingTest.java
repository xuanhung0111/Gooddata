package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.FilterAttribute;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricsBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.PivotTableReport;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.testng.annotations.Test;

import java.util.List;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AVG_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_FORECAST_CATEGORY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_WON;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_REGION;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CREATED;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertEquals;

public class ReferencePointAndVisSwitchingTest extends AbstractAnalyseTest {

    private static final String INSIGHT_HAS_SOME_MEASURES_AND_SOME_ATTRIBUTES = "Some measures, attributes";
    private static final String INSIGHT_HAS_SOME_MEASURES_AND_AN_ATTRIBUTE = "Some measures, an attribute";
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
        createTableInsight(INSIGHT_HAS_SOME_MEASURES_AND_AN_ATTRIBUTE, ReportType.TABLE,
            asList(METRIC_WON, METRIC_AMOUNT, METRIC_AVG_AMOUNT),
            singletonList(Pair.of(ATTR_PRODUCT, CategoryBucket.Type.ATTRIBUTE)), singletonList(ATTR_PRODUCT));

        createTableInsight(INSIGHT_HAS_SOME_MEASURES_AND_SOME_ATTRIBUTES, ReportType.TABLE,
            asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT), asList(Pair.of(ATTR_DEPARTMENT, CategoryBucket.Type.ATTRIBUTE),
                Pair.of(ATTR_FORECAST_CATEGORY, CategoryBucket.Type.ATTRIBUTE),
                Pair.of(ATTR_REGION, CategoryBucket.Type.ATTRIBUTE)),
            asList(ATTR_DEPARTMENT, ATTR_FORECAST_CATEGORY, ATTR_REGION));
    }

    @Test(dependsOnMethods = {"prepareInsights"})
    public void switchToChartDoesNotAllowCombination() {
        initAnalysePage().openInsight(INSIGHT_HAS_SOME_MEASURES_AND_SOME_ATTRIBUTES)
                .changeReportType(ReportType.PIE_CHART).waitForReportComputing();

        assertEquals(analysisPage.getMetricsBucket().getItemNames(), singletonList(METRIC_AMOUNT));
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), singletonList(ATTR_DEPARTMENT));
        assertEquals(parseFilterText(analysisPage.getFilterBuckets()
                .getFilterText(ATTR_DEPARTMENT)), asList(ATTR_DEPARTMENT, "All"));
    }

    @Test(dependsOnMethods = {"prepareInsights"})
    public void switchToChartAllowCombination() {
        initAnalysePage().openInsight(INSIGHT_HAS_SOME_MEASURES_AND_SOME_ATTRIBUTES)
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

    @Test(dependsOnMethods = {"prepareInsights"})
    public void switchFromPieOrDonutToBulletChart() {
        //https://jira.intgdc.com/browse/CL-12966
        initAnalysePage().openInsight(INSIGHT_HAS_SOME_MEASURES_AND_AN_ATTRIBUTE).waitForReportComputing()
            .getPivotTableReport().sortBaseOnHeader(METRIC_AMOUNT);

        analysisPage.waitForReportComputing().changeReportType(ReportType.BUBBLE_CHART).waitForReportComputing();
        checkRedBar(browser);

        analysisPage.changeReportType(ReportType.PIE_CHART).waitForReportComputing();
        checkRedBar(browser);

        analysisPage.changeReportType(ReportType.DONUT_CHART).waitForReportComputing();
        checkRedBar(browser);
    }

    @Test(dependsOnMethods = {"prepareInsights"})
    public void switchFromScatterOrBubbleToBulletChart() {
        //https://jira.intgdc.com/browse/CL-12941
        initAnalysePage().openInsight(INSIGHT_HAS_SOME_MEASURES_AND_AN_ATTRIBUTE).waitForReportComputing()
            .getPivotTableReport().sortBaseOnHeader(METRIC_WON);

        analysisPage.waitForReportComputing().changeReportType(ReportType.BUBBLE_CHART).waitForReportComputing();
        checkRedBar(browser);

        analysisPage.changeReportType(ReportType.SCATTER_PLOT).waitForReportComputing();
        checkRedBar(browser);

        analysisPage.changeReportType(ReportType.BUBBLE_CHART).waitForReportComputing();
        checkRedBar(browser);
    }

    @Test(dependsOnMethods = {"prepareInsights"})
    public void checkTableConfigurationIsKeptAfterSwitchingChart() {
        //https://jira.intgdc.com/browse/CL-12904
        PivotTableReport pivotTableReport = initAnalysePage().openInsight(INSIGHT_HAS_SOME_MEASURES_AND_AN_ATTRIBUTE)
            .waitForReportComputing().getPivotTableReport().sortBaseOnHeader(METRIC_AMOUNT);
        analysisPage.waitForReportComputing();

        assertFalse(pivotTableReport.isRowHeaderSortedUp(METRIC_AMOUNT));

        analysisPage.changeReportType(ReportType.BUBBLE_CHART).waitForReportComputing();
        analysisPage.openConfigurationPanelBucket().expandConfigurationPanel().getItemConfiguration("Legend")
            .switchOff();

        analysisPage.changeReportType(ReportType.TABLE).waitForReportComputing();
        checkRedBar(browser);
        assertFalse(pivotTableReport.isRowHeaderSortedUp(METRIC_AMOUNT));
    }

    private String createTableInsight(String insightTitle, ReportType reportType, List<String> metricsTitle,
                                 List<Pair<String, CategoryBucket.Type>> attributeConfigurations, List<String> filterAttributes) {
        return indigoRestRequest.createInsight(
            new InsightMDConfiguration(insightTitle, reportType)
                .setMeasureBucket(metricsTitle.stream()
                    .map(metric -> MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric)))
                    .collect(toList()))
                .setCategoryBucket(attributeConfigurations.stream()
                    .map(attribute -> CategoryBucket.createCategoryBucket(
                        getAttributeByTitle(attribute.getKey()), attribute.getValue()))
                    .collect(toList()))
                .setFilter(filterAttributes.stream()
                    .map(filter -> FilterAttribute.createFilter(getAttributeByTitle(filter)))
                    .collect(toList())));
    }
}
