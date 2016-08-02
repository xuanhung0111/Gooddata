package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.md.Restriction.identifier;
import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createAnalyticalDashboard;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createKpiWidget;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.getInsightUri;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.md.Dataset;
import com.gooddata.md.Fact;
import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.entity.kpi.KpiMDConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.analyze.DateDimensionSelect;
import com.gooddata.qa.graphene.fragments.indigo.analyze.DateDimensionSelect.DateDimensionGroup;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonDirection;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonType;
import com.gooddata.qa.graphene.indigo.analyze.common.GoodSalesAbstractAnalyseTest;

public class GoodSalesRelatedAndUnrelatedDateDimensionsTest extends GoodSalesAbstractAnalyseTest {

    private static final String RECOMMENDED = "RECOMMENDED";
    private static final String OTHER = "OTHER";
    private static final String INSIGHT = "Insight";

    private static final String CREATED = "Created";
    private static final String CLOSED = "Closed";
    private static final String SNAPSHOT = "Snapshot";
    private static final String HIDDEN_DATE_DIMENSION_DESCRIPTION = "2 unrelated dates hidden";

    private String createdDateUri;
    private String closedDateUri;
    private String snapshotDateUri;

    private Metric metric1;
    private Metric metric2;
    private Metric metric3;

    @Test(dependsOnGroups = {"init"})
    public void initData() {
        final String amountFactUri = getMdService().getObjUri(getProject(), Fact.class, title(METRIC_AMOUNT));
        final String expression = format("SELECT SUM([%s])", amountFactUri);

        metric1 = createMetric("Metric1", expression, "#,##0");
        metric2 = createMetric("Metric2", expression, "#,##0");
        metric3 = createMetric("Metric3", expression, "#,##0");

        createdDateUri = getMdService().getObjUri(getProject(), Dataset.class, identifier("created.dataset.dt"));
        closedDateUri = getMdService().getObjUri(getProject(), Dataset.class, identifier("closed.dataset.dt"));
        snapshotDateUri = getMdService().getObjUri(getProject(), Dataset.class, identifier("snapshot.dataset.dt"));
    }

    @Test(dependsOnMethods = {"initData"})
    public void checkRecommendedForKpiMetricInViewBy() throws JSONException, IOException {
        final String kpiUri = createKpi(metric1.getTitle(), metric1.getUri(), createdDateUri);
        final String indigoDashboardUri = createAnalyticalDashboard(
                getRestApiClient(), testParams.getProjectId(), singletonList(kpiUri));

        try {
            analysisPage.addMetric(metric1.getTitle()).addDate();

            DateDimensionSelect dateDatasetSelect = analysisPage.getAttributesBucket().getDateDatasetSelect();
            DateDimensionGroup recommended = dateDatasetSelect.getDateDimensionGroup(RECOMMENDED);
            DateDimensionGroup other = dateDatasetSelect.getDateDimensionGroup(OTHER);

            takeScreenshot(browser, "Recommended-date-dimensions-for-kpi-metric-show-in-View-By", getClass());
            assertEquals(recommended.getDateDimensions(), singletonList(CREATED));
            assertEquals(other.getDateDimensions(), asList(CLOSED, SNAPSHOT));
            assertEquals(dateDatasetSelect.getHiddenDescription(), HIDDEN_DATE_DIMENSION_DESCRIPTION);

            dateDatasetSelect.selectByName(CLOSED);
            assertEquals(dateDatasetSelect.getSelection(), CLOSED);

            dateDatasetSelect.selectByName(SNAPSHOT);
            assertEquals(dateDatasetSelect.getSelection(), SNAPSHOT);

        } finally {
            getMdService().removeObjByUri(indigoDashboardUri);
        }
    }

    @Test(dependsOnMethods = {"initData"})
    public void checkRecommendedForKpiMetricInFilterBucket() throws JSONException, IOException {
        final String kpiUri = createKpi(metric1.getTitle(), metric1.getUri(), createdDateUri);
        final String indigoDashboardUri = createAnalyticalDashboard(
                getRestApiClient(), testParams.getProjectId(), singletonList(kpiUri));

        try {
            analysisPage.addMetric(metric1.getTitle()).addDateFilter();

            FiltersBucket filterBucket = analysisPage.getFilterBuckets();
            DateDimensionSelect dateDatasetSelect = filterBucket
                    .openDatePanelOfFilter(filterBucket.getDateFilter())
                    .getDateDatasetSelect();

            DateDimensionGroup recommended = dateDatasetSelect.getDateDimensionGroup(RECOMMENDED);
            DateDimensionGroup other = dateDatasetSelect.getDateDimensionGroup(OTHER);

            takeScreenshot(browser, "Recommended-date-dimensions-for-kpi-metric-show-in-Filter-Bucket", getClass());
            assertEquals(recommended.getDateDimensions(), singletonList(CREATED));
            assertEquals(other.getDateDimensions(), asList(CLOSED, SNAPSHOT));
            assertEquals(dateDatasetSelect.getHiddenDescription(), HIDDEN_DATE_DIMENSION_DESCRIPTION);

            dateDatasetSelect.selectByName(CLOSED);
            assertEquals(dateDatasetSelect.getSelection(), CLOSED);

            dateDatasetSelect.selectByName(SNAPSHOT);
            assertEquals(dateDatasetSelect.getSelection(), SNAPSHOT);

        } finally {
            getMdService().removeObjByUri(indigoDashboardUri);
        }
    }

    @Test(dependsOnMethods = {"initData"})
    public void checkRecommendedForInsightMetricInViewBy() throws JSONException, IOException {
        try {
            analysisPage
                    .addMetric(metric1.getTitle())
                    .addDate()
                    .waitForReportComputing()
                 // Prepare an insight which metric1 already combines with a date dimensions for test case
                    .saveInsight(INSIGHT) 
                    .resetToBlankState()
                    .addMetric(metric1.getTitle())
                    .addDate();

            DateDimensionSelect dateDatasetSelect = analysisPage.getAttributesBucket().getDateDatasetSelect();
            DateDimensionGroup recommended = dateDatasetSelect.getDateDimensionGroup(RECOMMENDED);
            DateDimensionGroup other = dateDatasetSelect.getDateDimensionGroup(OTHER);

            takeScreenshot(browser, "Recommended-date-dimensions-for-insight-metric-show-in-View-By", getClass());
            assertEquals(recommended.getDateDimensions(), singletonList(CLOSED));
            assertEquals(other.getDateDimensions(), asList(CREATED, SNAPSHOT));
            assertEquals(dateDatasetSelect.getHiddenDescription(), HIDDEN_DATE_DIMENSION_DESCRIPTION);

        } finally {
            getMdService().removeObjByUri(getInsightUri(INSIGHT, getRestApiClient(), testParams.getProjectId()));
        }
    }

    @Test(dependsOnMethods = {"initData"})
    public void checkRecommendedForInsightMetricInFilterBucket() throws JSONException, IOException {
        try {
            analysisPage
                    .addMetric(metric1.getTitle())
                    .addDate()
                    .waitForReportComputing()
                 // Prepare an insight which metric1 already combines with a date dimensions for test case
                    .saveInsight(INSIGHT)
                    .resetToBlankState()
                    .addMetric(metric1.getTitle())
                    .addDateFilter();

            FiltersBucket filterBucket = analysisPage.getFilterBuckets();
            DateDimensionSelect dateDatasetSelect = filterBucket
                    .openDatePanelOfFilter(filterBucket.getDateFilter())
                    .getDateDatasetSelect();

            DateDimensionGroup recommended = dateDatasetSelect.getDateDimensionGroup(RECOMMENDED);
            DateDimensionGroup other = dateDatasetSelect.getDateDimensionGroup(OTHER);

            takeScreenshot(browser, "Recommended-date-dimensions-for-insight-metric-show-in-View-By", getClass());
            assertEquals(recommended.getDateDimensions(), singletonList(CLOSED));
            assertEquals(other.getDateDimensions(), asList(CREATED, SNAPSHOT));
            assertEquals(dateDatasetSelect.getHiddenDescription(), HIDDEN_DATE_DIMENSION_DESCRIPTION);

        } finally {
            getMdService().removeObjByUri(getInsightUri(INSIGHT, getRestApiClient(), testParams.getProjectId()));
        }
    }

    @Test(dependsOnMethods = {"initData"})
    public void notShowRecommendedWithMetricNotCombineDate() {
        analysisPage.addMetric(metric1.getTitle()).addDate();

        DateDimensionSelect dateDatasetSelect = analysisPage
                .getAttributesBucket()
                .getDateDatasetSelect();
        List<DateDimensionGroup> groups = dateDatasetSelect.getDateDimensionGroups();

        takeScreenshot(browser, "Recommended-date-dimensions-not-show-in-View-By", getClass());
        assertEquals(groups.size(), 1);
        assertEquals(groups.get(0).getDateDimensions(), asList(CLOSED, CREATED, SNAPSHOT));
        assertEquals(dateDatasetSelect.getHiddenDescription(), HIDDEN_DATE_DIMENSION_DESCRIPTION);

        FiltersBucket filtersBucket = analysisPage
                .removeAttribute(DATE)
                .addDateFilter()
                .getFilterBuckets();

        dateDatasetSelect = filtersBucket
                .openDatePanelOfFilter(filtersBucket.getDateFilter())
                .getDateDatasetSelect();
        groups = dateDatasetSelect.getDateDimensionGroups();

        takeScreenshot(browser, "Recommended-date-dimensions-not-show-in-Filter-Bucket", getClass());
        assertEquals(groups.size(), 1);
        assertEquals(groups.get(0).getDateDimensions(), asList(CLOSED, CREATED, SNAPSHOT));
        assertEquals(dateDatasetSelect.getHiddenDescription(), HIDDEN_DATE_DIMENSION_DESCRIPTION);
    }

    @Test(dependsOnMethods = {"initData"})
    public void checkMultipleMetricsCombineSameDateInViewBy() throws JSONException, IOException {
        final String kpiUri1 = createKpi(metric1.getTitle(), metric1.getUri(), createdDateUri);
        final String kpiUri2 = createKpi(metric2.getTitle(), metric2.getUri(), createdDateUri);

        final String indigoDashboardUri = createAnalyticalDashboard(
                getRestApiClient(), testParams.getProjectId(), asList(kpiUri1, kpiUri2));

        try {
            analysisPage
                    .addMetric(metric1.getTitle())
                    .addMetric(metric2.getTitle())
                    .addDate();

            DateDimensionSelect dateDatasetSelect = analysisPage.getAttributesBucket().getDateDatasetSelect();

            DateDimensionGroup recommended = dateDatasetSelect.getDateDimensionGroup(RECOMMENDED);
            DateDimensionGroup other = dateDatasetSelect.getDateDimensionGroup(OTHER);

            takeScreenshot(browser, "Recommended-date-dimensions-show-for-multiple-metrics-in-View-By", getClass());
            assertEquals(recommended.getDateDimensions(), singletonList(CREATED));
            assertEquals(other.getDateDimensions(), asList(CLOSED, SNAPSHOT));
            assertEquals(dateDatasetSelect.getHiddenDescription(), HIDDEN_DATE_DIMENSION_DESCRIPTION);

        } finally {
            getMdService().removeObjByUri(indigoDashboardUri);
        }
    }

    @Test(dependsOnMethods = {"initData"})
    public void checkMultipleMetricsCombineSameDateInFilterBucket() throws JSONException, IOException {
        final String kpiUri1 = createKpi(metric1.getTitle(), metric1.getUri(), createdDateUri);
        final String kpiUri2 = createKpi(metric2.getTitle(), metric2.getUri(), createdDateUri);

        final String indigoDashboardUri = createAnalyticalDashboard(
                getRestApiClient(), testParams.getProjectId(), asList(kpiUri1, kpiUri2));

        try {
            analysisPage
                    .addMetric(metric1.getTitle())
                    .addMetric(metric2.getTitle())
                    .addDateFilter();

            FiltersBucket filterBucket = analysisPage.getFilterBuckets();
            DateDimensionSelect dateDatasetSelect = filterBucket
                    .openDatePanelOfFilter(filterBucket.getDateFilter())
                    .getDateDatasetSelect();

            DateDimensionGroup recommended = dateDatasetSelect.getDateDimensionGroup(RECOMMENDED);
            DateDimensionGroup other = dateDatasetSelect.getDateDimensionGroup(OTHER);

            takeScreenshot(browser, "Recommended-date-dimensions-show-for-multiple-metrics-in-Filter-Bucket", getClass());
            assertEquals(recommended.getDateDimensions(), singletonList(CREATED));
            assertEquals(other.getDateDimensions(), asList(CLOSED, SNAPSHOT));
            assertEquals(dateDatasetSelect.getHiddenDescription(), HIDDEN_DATE_DIMENSION_DESCRIPTION);

        } finally {
            getMdService().removeObjByUri(indigoDashboardUri);
        }
    }

    @DataProvider(name = "metricProvider1")
    public Object[][] getMetricProvider1() {
        return new Object[][] {
            {metric1, metric1, "Single-metric"},
            {metric1, metric2, "Multiple-metrics"}
        };
    }

    @Test(dependsOnMethods = {"initData"}, dataProvider = "metricProvider1")
    public void checkMetricsCombineDifferenceDateInViewBy(Metric metric1, Metric metric2, String metricType)
            throws JSONException, IOException {
        final String kpiUri1 = createKpi(metric1.getTitle(), metric1.getUri(), createdDateUri);
        final String kpiUri2 = createKpi(metric2.getTitle(), metric2.getUri(), closedDateUri);

        final String indigoDashboardUri = createAnalyticalDashboard(
                getRestApiClient(), testParams.getProjectId(), asList(kpiUri1, kpiUri2));

        try {
            analysisPage
                    .addMetric(metric1.getTitle())
                    .addMetric(metric2.getTitle())
                    .addDate();

            DateDimensionSelect dateDatasetSelect = analysisPage.getAttributesBucket().getDateDatasetSelect();

            DateDimensionGroup recommended = dateDatasetSelect.getDateDimensionGroup(RECOMMENDED);
            DateDimensionGroup other = dateDatasetSelect.getDateDimensionGroup(OTHER);

            takeScreenshot(browser,
                    "Recommended-date-dimensions-in-View-By-show-for-" + metricType, getClass());
            assertEquals(recommended.getDateDimensions(), asList(CLOSED, CREATED));
            assertEquals(other.getDateDimensions(), singletonList(SNAPSHOT));
            assertEquals(dateDatasetSelect.getHiddenDescription(), HIDDEN_DATE_DIMENSION_DESCRIPTION);

        } finally {
            getMdService().removeObjByUri(indigoDashboardUri);
        }
    }

    @Test(dependsOnMethods = {"initData"}, dataProvider = "metricProvider1")
    public void checkMetricsCombineDifferenceDateInFilterBucket(Metric metric1, Metric metric2,
            String metricType) throws JSONException, IOException {
        final String kpiUri1 = createKpi(metric1.getTitle(), metric1.getUri(), createdDateUri);
        final String kpiUri2 = createKpi(metric2.getTitle(), metric2.getUri(), closedDateUri);

        final String indigoDashboardUri = createAnalyticalDashboard(
                getRestApiClient(), testParams.getProjectId(), asList(kpiUri1, kpiUri2));

        try {
            analysisPage
                    .addMetric(metric1.getTitle())
                    .addMetric(metric2.getTitle())
                    .addDateFilter();

            FiltersBucket filterBucket = analysisPage.getFilterBuckets();
            DateDimensionSelect dateDatasetSelect = filterBucket
                    .openDatePanelOfFilter(filterBucket.getDateFilter())
                    .getDateDatasetSelect();

            DateDimensionGroup recommended = dateDatasetSelect.getDateDimensionGroup(RECOMMENDED);
            DateDimensionGroup other = dateDatasetSelect.getDateDimensionGroup(OTHER);

            takeScreenshot(browser,
                    "Recommended-date-dimensions-in-filter-bucket-show-for-" + metricType, getClass());
            assertEquals(recommended.getDateDimensions(), asList(CLOSED, CREATED));
            assertEquals(other.getDateDimensions(), singletonList(SNAPSHOT));
            assertEquals(dateDatasetSelect.getHiddenDescription(), HIDDEN_DATE_DIMENSION_DESCRIPTION);

        } finally {
            getMdService().removeObjByUri(indigoDashboardUri);
        }
    }

    @DataProvider(name = "metricProvider2")
    public Object[][] getMetricProvider2() {
        return new Object[][] {
            {metric1, metric1, metric1, "Single-metric"},
            {metric1, metric2, metric3, "Multiple-metrics"}
        };
    }

    @Test(dependsOnMethods = {"initData"}, dataProvider = "metricProvider2")
    public void notShowRecommendedWithMetricsCombineAllDateInViewBy(Metric metric1, Metric metric2, Metric metric3,
            String metricType) throws JSONException, IOException {
        final String kpiUri1 = createKpi(metric1.getTitle(), metric1.getUri(), createdDateUri);
        final String kpiUri2 = createKpi(metric2.getTitle(), metric2.getUri(), closedDateUri);
        final String kpiUri3 = createKpi(metric3.getTitle(), metric3.getUri(), snapshotDateUri);

        final String indigoDashboardUri = createAnalyticalDashboard(
                getRestApiClient(), testParams.getProjectId(), asList(kpiUri1, kpiUri2, kpiUri3));

        try {
            analysisPage
                    .addMetric(metric1.getTitle())
                    .addMetric(metric2.getTitle())
                    .addMetric(metric3.getTitle())
                    .addDate();

            DateDimensionSelect dateDatasetSelect = analysisPage.getAttributesBucket().getDateDatasetSelect();
            List<DateDimensionGroup> groups = dateDatasetSelect.getDateDimensionGroups();

            takeScreenshot(browser, "Recommended-date-dimension-not-show-in-View-By-with" + metricType, getClass());
            assertEquals(groups.size(), 1);
            assertEquals(groups.get(0).getDateDimensions(), asList(CLOSED, CREATED, SNAPSHOT));
            assertEquals(dateDatasetSelect.getHiddenDescription(), HIDDEN_DATE_DIMENSION_DESCRIPTION);

        } finally {
            getMdService().removeObjByUri(indigoDashboardUri);
        }
    }

    @Test(dependsOnMethods = {"initData"}, dataProvider = "metricProvider2")
    public void notShowRecommendedWithMetricsCombineAllDateInFilterBucket(Metric metric1, Metric metric2, Metric metric3,
            String metricType) throws JSONException, IOException {
        final String kpiUri1 = createKpi(metric1.getTitle(), metric1.getUri(), createdDateUri);
        final String kpiUri2 = createKpi(metric2.getTitle(), metric2.getUri(), closedDateUri);
        final String kpiUri3 = createKpi(metric3.getTitle(), metric3.getUri(), snapshotDateUri);

        final String indigoDashboardUri = createAnalyticalDashboard(
                getRestApiClient(), testParams.getProjectId(), asList(kpiUri1, kpiUri2, kpiUri3));

        try {
            analysisPage
                    .addMetric(metric1.getTitle())
                    .addMetric(metric2.getTitle())
                    .addMetric(metric3.getTitle())
                    .addDateFilter();

            FiltersBucket filterBucket = analysisPage.getFilterBuckets();
            DateDimensionSelect dateDatasetSelect = filterBucket
                    .openDatePanelOfFilter(filterBucket.getDateFilter())
                    .getDateDatasetSelect();
            List<DateDimensionGroup> groups = dateDatasetSelect.getDateDimensionGroups();

            takeScreenshot(browser, "Recommended-date-dimension-not-show-in-Filter-Bucket-with" + metricType, getClass());
            assertEquals(groups.size(), 1);
            assertEquals(groups.get(0).getDateDimensions(), asList(CLOSED, CREATED, SNAPSHOT));
            assertEquals(dateDatasetSelect.getHiddenDescription(), HIDDEN_DATE_DIMENSION_DESCRIPTION);

        } finally {
            getMdService().removeObjByUri(indigoDashboardUri);
        }
    }

    private String createKpi(String title, String metricUri, String dateDatasetUri) throws JSONException, IOException {
        return createKpiWidget(getRestApiClient(), testParams.getProjectId(),
                new KpiMDConfiguration.Builder()
                        .title(title)
                        .metric(metricUri)
                        .dateDataSet(dateDatasetUri)
                        .comparisonType(ComparisonType.PREVIOUS_PERIOD)
                        .comparisonDirection(ComparisonDirection.GOOD)
                        .build());
    }
}
