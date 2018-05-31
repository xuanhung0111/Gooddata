package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CLOSED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_TIMELINE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static  com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_OPP_FIRST_SNAPSHOT ;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.gooddata.md.Metric;
import com.gooddata.md.Restriction;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.DateDimensionSelect;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import com.google.common.collect.Ordering;

public class DateDatasetRecommendationTest extends AbstractDashboardTest {

    private static String INSIGHT_WITHOUT_DATE_FILTER = "Insight-Without-Date-Filter";
    private IndigoRestRequest indigoRestRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Date-Filter-Recommendation-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createOppFirstSnapshotMetric();
        getReportCreator().createActiveLevelReport();
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
    }

    @AfterMethod
    public void deleteWorkingInsights(Method method) throws JSONException, IOException {
        if (!method.getDeclaringClass().equals(this.getClass()))
            return;

        // unused objects could affect to other tests
        List<String> workingInsightUris = indigoRestRequest.getInsightUris();
        final CommonRestRequest restRequest = new CommonRestRequest(
                new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        if (!workingInsightUris.isEmpty()) {
            restRequest.deleteObjectsUsingCascade(workingInsightUris.toArray(new String[0]));
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void selectMostRelevantDateDimention() throws ParseException, JSONException, IOException {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_WITHOUT_DATE_FILTER, ReportType.COLUMN_CHART).setMeasureBucket(
                        singletonList(MeasureBucket.createSimpleMeasureBucket(getMetric(METRIC_NUMBER_OF_ACTIVITIES)))));

        initIndigoDashboardsPage().getSplashScreen().startEditingWidgets().addInsight(INSIGHT_WITHOUT_DATE_FILTER)
                .waitForWidgetsLoading();

        takeScreenshot(browser, "Select-Most-Relevant-Date-Dimension", getClass());
        assertEquals(indigoDashboardsPage.getConfigurationPanel().getSelectedDataSet(), DATE_DATASET_ACTIVITY,
                "Most relevant date dimension is not selected");
        assertTrue(indigoDashboardsPage.getConfigurationPanel().isDateDataSetSelectCollapsed(),
                "Date dataset select is not collapsed");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testOrderOfRecommendedDateDimensions() throws ParseException, JSONException, IOException {
        createInsightUsingDateFilter("Insight-Using-Date-Filter-Snapshot-1", METRIC_OPP_FIRST_SNAPSHOT,
                DATE_DATASET_SNAPSHOT);
        createInsightUsingDateFilter("Insight-Using-Date-Filter-Snapshot-2", METRIC_OPP_FIRST_SNAPSHOT,
                DATE_DATASET_SNAPSHOT);
        createInsightUsingDateFilter("Insight-Using-Date-Filter-Closed", METRIC_OPP_FIRST_SNAPSHOT, DATE_DATASET_CLOSED);

        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_WITHOUT_DATE_FILTER, ReportType.COLUMN_CHART).setMeasureBucket(
                        singletonList(MeasureBucket.createSimpleMeasureBucket(getMetric(METRIC_OPP_FIRST_SNAPSHOT)))));

        initIndigoDashboardsPage().getSplashScreen().startEditingWidgets().addInsight(INSIGHT_WITHOUT_DATE_FILTER)
                .waitForWidgetsLoading();

        DateDimensionSelect dropDown = indigoDashboardsPage.getConfigurationPanel().openDateDataSet();
        takeScreenshot(browser, "Order-Of-Recommended-Date-Dimensions", getClass());
        assertEquals(dropDown.getDateDimensionGroup("RECOMMENDED").getDateDimensions(),
                asList(DATE_DATASET_SNAPSHOT, DATE_DATASET_CLOSED), "The recommended dimensions are not sorted by most relevant");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testOrderOfOtherDateDimensions() throws ParseException, JSONException, IOException {
        createInsightUsingDateFilter("Insight-Using-Date-Filter-Closed", METRIC_OPP_FIRST_SNAPSHOT, DATE_DATASET_CLOSED);

        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_WITHOUT_DATE_FILTER, ReportType.COLUMN_CHART).setMeasureBucket(
                        singletonList(MeasureBucket.createSimpleMeasureBucket(getMetric(METRIC_OPP_FIRST_SNAPSHOT)))));

        List<String> otherDimensions = initIndigoDashboardsPage().getSplashScreen().startEditingWidgets()
                .addInsight(INSIGHT_WITHOUT_DATE_FILTER).waitForWidgetsLoading().getConfigurationPanel()
                .openDateDataSet().getDateDimensionGroup("OTHER").getDateDimensions();

        takeScreenshot(browser, "Order-Of-Other-Date-Dimensions", getClass());
        assertTrue(Ordering.natural().isOrdered(otherDimensions),
                "The Other date dimensions are not sorted by alphabetical order");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testMaximumNumberOfRecommendations() throws ParseException, JSONException, IOException {
        createInsightUsingDateFilter("Insight-Using-Date-Filter-Snapshot", METRIC_OPP_FIRST_SNAPSHOT,
                DATE_DATASET_SNAPSHOT);
        createInsightUsingDateFilter("Insight-Using-Date-Filter-Activity", METRIC_OPP_FIRST_SNAPSHOT,
                DATE_DATASET_ACTIVITY);
        createInsightUsingDateFilter("Insight-Using-Date-Filter-Closed", METRIC_OPP_FIRST_SNAPSHOT, DATE_DATASET_CLOSED);
        createInsightUsingDateFilter("Insight-Using-Date-Filter-Created", METRIC_OPP_FIRST_SNAPSHOT, DATE_DATASET_CREATED);

        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_WITHOUT_DATE_FILTER, ReportType.COLUMN_CHART).setMeasureBucket(
                        singletonList(MeasureBucket.createSimpleMeasureBucket(getMetric(METRIC_OPP_FIRST_SNAPSHOT)))));

        initIndigoDashboardsPage().getSplashScreen().startEditingWidgets().addInsight(INSIGHT_WITHOUT_DATE_FILTER)
                .waitForWidgetsLoading();

        DateDimensionSelect dateDimensionSelect = indigoDashboardsPage.getConfigurationPanel().openDateDataSet();
        takeScreenshot(browser, "Maximum-Number-Of-Recommendations", getClass());
        assertEquals(dateDimensionSelect.getDateDimensionGroup("RECOMMENDED").getDateDimensions(),
                asList(DATE_DATASET_ACTIVITY, DATE_DATASET_CLOSED, DATE_DATASET_CREATED),
                        "The recommended dimensions are not correct");
        assertEquals(dateDimensionSelect.getDateDimensionGroup("OTHER").getDateDimensions(),
                asList(DATE_DATASET_SNAPSHOT, DATE_DATASET_TIMELINE), "The other dimensions are not correct");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testDateDatasetHavingNoGroup() throws ParseException, JSONException, IOException {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_WITHOUT_DATE_FILTER, ReportType.COLUMN_CHART).setMeasureBucket(
                        singletonList(MeasureBucket.createSimpleMeasureBucket(getMetric(METRIC_OPP_FIRST_SNAPSHOT)))));

        initIndigoDashboardsPage().getSplashScreen().startEditingWidgets().addInsight(INSIGHT_WITHOUT_DATE_FILTER)
                .waitForWidgetsLoading();
        assertFalse(indigoDashboardsPage.getConfigurationPanel().isDateDataSetSelectCollapsed(),
                "The date dataset is not expanded");
        DateDimensionSelect dropDown = indigoDashboardsPage.getConfigurationPanel().openDateDataSet();
        takeScreenshot(browser, "Date-Dateset-Having-No-Group", getClass());
        assertTrue(
                isEqualCollection(dropDown.getDateDimensionGroup("DEFAULT").getDateDimensions(),
                        asList(DATE_DATASET_ACTIVITY, DATE_DATASET_CLOSED, DATE_DATASET_CREATED,
                                DATE_DATASET_SNAPSHOT, DATE_DATASET_TIMELINE)),
                "The date dimensions which belong to no group are not correct");
    }

    private void createInsightUsingDateFilter(String insight, String metricName, String switchDimension) {
        AnalysisPage page = initAnalysePage().addMetric(metricName).addDateFilter().waitForReportComputing();
        page.getFilterBuckets().changeDateDimension(switchDimension);
        page.saveInsight(insight);
    }

    private Metric getMetric(String title) {
        return getMdService().getObj(getProject(), Metric.class, Restriction.title(title));
    }
}
