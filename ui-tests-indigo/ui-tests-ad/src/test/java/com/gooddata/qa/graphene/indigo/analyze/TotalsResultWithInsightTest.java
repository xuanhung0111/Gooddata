package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.sdk.model.md.Attribute;
import com.gooddata.sdk.model.md.Metric;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket.Type;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.AggregationItem;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.analyze.CompareTypeDropdown;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.PivotTableReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestRequest;
import org.apache.http.ParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static com.gooddata.sdk.model.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_CLOSED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_SALES_REP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_CLOSE_EOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.apache.commons.lang3.StringUtils.EMPTY;

public class TotalsResultWithInsightTest extends AbstractAnalyseTest{

    private static final String INSIGHT_HAS_ATTRIBUTE_AND_MEASURE = "Attribute and measure";
    private static final String INSIGHT_HAS_ATTRIBUTES_AND_MEASURES = "Attributes and measures";
    private static final String INSIGHT_HAS_DATE_ATTRIBUTE_AND_MEASURE = "Date and measure";
    private static final String INSIGHT_SHOW_SAME_PERIOD_COMPARISON = "Same period comparison";
    private static final String INSIGHT_SHOW_PERCENT = "Insight show percent";
    private IndigoRestRequest indigoRestRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Totals-Result-Test";
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics= getMetricCreator();
        metrics.createNumberOfActivitiesMetric();
        metrics.createCloseEOPMetric();
        metrics.createAmountMetric();
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)),
                testParams.getProjectId());
    }

    @Test(dependsOnGroups = "createProject")
    public void prepareInsights() {
        createSimpleInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE, METRIC_NUMBER_OF_ACTIVITIES, ATTR_DEPARTMENT);
        createSimpleInsight(INSIGHT_HAS_DATE_ATTRIBUTE_AND_MEASURE, METRIC_NUMBER_OF_ACTIVITIES, ATTR_YEAR_ACTIVITY);

        indigoRestRequest.createInsight(
            new InsightMDConfiguration(INSIGHT_HAS_ATTRIBUTES_AND_MEASURES, ReportType.TABLE)
                    .setMeasureBucket(asList(
                            MeasureBucket.createSimpleMeasureBucket(getMetric(METRIC_NUMBER_OF_ACTIVITIES)),
                            MeasureBucket.createSimpleMeasureBucket(getMetric(METRIC_CLOSE_EOP))))
                    .setCategoryBucket(asList(
                            CategoryBucket.createCategoryBucket(getAttribute(ATTR_DEPARTMENT), Type.ATTRIBUTE),
                            CategoryBucket.createCategoryBucket(getAttribute(ATTR_SALES_REP), Type.ATTRIBUTE))));

        indigoRestRequest.createInsight(
            new InsightMDConfiguration(INSIGHT_SHOW_PERCENT, ReportType.TABLE)
                    .setMeasureBucket(singletonList(MeasureBucket.createMeasureBucketWithShowInPercent(
                            getMetric(METRIC_NUMBER_OF_ACTIVITIES), true)))
                    .setCategoryBucket(singletonList(
                            CategoryBucket.createCategoryBucket(getAttribute(ATTR_YEAR_ACTIVITY), Type.ATTRIBUTE))));

        createSimpleInsight(INSIGHT_SHOW_SAME_PERIOD_COMPARISON, METRIC_NUMBER_OF_ACTIVITIES, ATTR_YEAR_ACTIVITY);
        AnalysisPage analysisPage = initAnalysePage();

        analysisPage.openInsight(INSIGHT_SHOW_SAME_PERIOD_COMPARISON);
        analysisPage.addDateFilter();

        analysisPage.getFilterBuckets()
                .openDateFilterPickerPanel()
                .configTimeFilterByRangeHelper("1/1/2006", "1/1/2020")
                .applyCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_PREVIOUS_YEAR);

        analysisPage.waitForReportComputing();

        analysisPage.saveInsight();
    }

    @DataProvider
    public Object[][] getSavedInsight() {
        return new Object[][] {
                {INSIGHT_HAS_ATTRIBUTE_AND_MEASURE, singletonList("101,054")},
                {INSIGHT_HAS_DATE_ATTRIBUTE_AND_MEASURE, singletonList("73,073")},
                {INSIGHT_SHOW_SAME_PERIOD_COMPARISON, asList(EMPTY, "73,073")},
                {INSIGHT_SHOW_PERCENT, singletonList("47.37%")}
        };
    }

    @Test(dependsOnMethods = "prepareInsights", dataProvider = "getSavedInsight")
    public void saveInsightHasTotalsResult(String insight, List<String> totalsValues) throws JSONException {
        String metric = METRIC_NUMBER_OF_ACTIVITIES;
        if (insight.equals(INSIGHT_SHOW_PERCENT))
            metric = "% " + METRIC_NUMBER_OF_ACTIVITIES;
        AnalysisPage analysisPage = initAnalysePage();

        //open exist insight
        analysisPage.openInsight(insight).waitForReportComputing();
        PivotTableReport tableReport = analysisPage.getPivotTableReport();
        tableReport.addTotal(AggregationItem.MAX, metric, 0);
        Screenshots.takeScreenshot(browser, "Save " + insight + " has totals result", getClass());
        assertEquals(tableReport.getGrandTotalValues(AggregationItem.MAX), totalsValues);

        //save insight as
        analysisPage.saveInsightAs(insight + generateHashString());
        checkRedBar(browser);
        assertEquals(tableReport.getGrandTotalValues(AggregationItem.MAX), totalsValues);

        //modify and save
        analysisPage.addAttribute(ATTR_IS_CLOSED).saveInsightAs(insight + generateHashString());
        checkRedBar(browser);
        final List<String> grandTotals = tableReport.getGrandTotalValues(AggregationItem.MAX);
        assertEquals(grandTotals.subList(1, grandTotals.size()), totalsValues);
    }

    @DataProvider
    public Object[][] getRemovedInsight() {
        return new Object[][] {
                {INSIGHT_HAS_ATTRIBUTE_AND_MEASURE},
                {INSIGHT_HAS_ATTRIBUTES_AND_MEASURES},
                {INSIGHT_SHOW_SAME_PERIOD_COMPARISON}
        };
    }

    @Test(dependsOnMethods = "prepareInsights", dataProvider = "getRemovedInsight")
    public void removedInsightHasTotalsResult(String insight) throws JSONException {
        String metric = METRIC_NUMBER_OF_ACTIVITIES;
        if (insight.equals(INSIGHT_SHOW_PERCENT))
            metric = "% " + METRIC_NUMBER_OF_ACTIVITIES;
        AnalysisPage analysisPage = initAnalysePage();
        analysisPage.openInsight(insight).waitForReportComputing();
        //remove one of totals results
        PivotTableReport tableReport = analysisPage.getPivotTableReport()
            .addTotal(AggregationItem.MAX, metric, 0)
            .addTotal(AggregationItem.SUM, metric, 0)
            .removeTotal(AggregationItem.MAX, metric, 0);
        Screenshots.takeScreenshot(browser, "Remove " + insight + " has totals result", getClass());
        assertTrue(tableReport.containsGrandTotals(), "The rest row should be kept as before");

        //remove all totals results
        tableReport.removeTotal(AggregationItem.SUM, metric, 0);
        assertFalse(tableReport.containsGrandTotals(), "The empty total row should be disappeared");

        //save empty totals result
        analysisPage.saveInsightAs(insight + generateHashString());
        assertFalse(tableReport.containsGrandTotals(), "The empty total row cannot be saved");
    }

    @Test(dependsOnMethods = "prepareInsights")
    public void emptyTotalsResultOnKD() {
        AnalysisPage analysisPage = initAnalysePage();
        analysisPage.openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE)
            .waitForReportComputing()
            .getPivotTableReport()
            .addTotal(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES, 0)
            .removeTotal(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES, 0);
        analysisPage.saveInsightAs("empty totals result");

        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage() ;
        indigoDashboardsPage.addDashboard().addInsight("empty totals result").getConfigurationPanel().disableDateFilter();
        assertFalse(indigoDashboardsPage.waitForWidgetsLoading().getLastWidget(Insight.class).getPivotTableReport().containsGrandTotals(),
            "The empty total row cannot show at KD");
    }

    @Test(dependsOnMethods = "prepareInsights")
    public void checkMetadataTotalsResult() throws JSONException, IOException {
        AnalysisPage analysisPage = initAnalysePage();
        analysisPage
                .openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE)
                .getPivotTableReport()
                .addTotal(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES, 0);
        analysisPage.saveInsightAs("Add totals result");
        Screenshots.takeScreenshot(browser, "Check metadata totals result", getClass());
        assertEquals(countTotalsDefinitions("Add totals result"), 1);

        analysisPage.getPivotTableReport().removeTotal(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES, 0);
        analysisPage.saveInsightAs("Delete totals result");
        assertEquals(countTotalsDefinitions("Delete totals result"), 0);
    }

    @Test(dependsOnMethods = "prepareInsights")
    public void undoAndRedoInsightHasTotalsResult() {
        AnalysisPage analysisPage = initAnalysePage();
        analysisPage.openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE);

        PivotTableReport tableReport = analysisPage.getPivotTableReport();
        tableReport
                .addTotal(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES, 0)
                .removeTotal(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES, 0);

        // Undo
        analysisPage.undo().waitForReportComputing();
        assertTrue(tableReport.containsGrandTotals(), "Totals Result should be reverted");
        assertEquals(tableReport.getGrandTotalValues(AggregationItem.MAX), singletonList("101,054"));

        // Redo
        analysisPage.redo().waitForReportComputing();
        assertFalse(tableReport.containsGrandTotals(), "Totals Result should be removed");
    }

    @Test(dependsOnMethods = "prepareInsights")
    public void checkTotalsResultWithMUF() throws ParseException, JSONException, IOException {
        final String newInsight = "Totals Result with MUF";
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

        AnalysisPage analysisPage = initAnalysePage();
        PivotTableReport tableReport = analysisPage.openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE).getPivotTableReport();
        tableReport.addTotal(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES, 0);
        analysisPage.saveInsightAs(newInsight);
        assertEquals(tableReport.getGrandTotalValues(AggregationItem.MAX), singletonList("101,054"));

        logoutAndLoginAs(true, UserRoles.EDITOR);
        try {
            tableReport = initAnalysePage().openInsight(newInsight).getPivotTableReport();
            Screenshots.takeScreenshot(browser, "Check totals results with MUF", getClass());
            assertEquals(tableReport.getGrandTotalValues(AggregationItem.MAX), singletonList("53,217"));
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    private int countTotalsDefinitions(String insight) throws JSONException, IOException {
        int countTotals = 0;
        JSONArray buckets = indigoRestRequest.getJsonObject(indigoRestRequest.getInsightUri(insight))
            .getJSONObject("visualizationObject")
            .getJSONObject("content")
            .getJSONArray("buckets");

        for (int i = 0; i < buckets.length(); i++) {
            if ("attribute".equals(buckets.getJSONObject(i).getString("localIdentifier"))) {
                if (!buckets.getJSONObject(i).has("totals"))
                    return 0;
                countTotals = buckets.getJSONObject(i).getJSONArray("totals").length();
            }
        }
        return countTotals;
    }

    private Metric getMetric(String title) {
        return getMdService().getObj(getProject(), Metric.class, title(title));
    }

    private Attribute getAttribute(String title) {
        return getMdService().getObj(getProject(), Attribute.class, title(title));
    }

    private void createSimpleInsight(String title, String metric, String attribute) {
        indigoRestRequest.createInsight(
            new InsightMDConfiguration(title, ReportType.TABLE)
                    .setMeasureBucket(singletonList(MeasureBucket.createSimpleMeasureBucket(getMetric(metric))))
                    .setCategoryBucket(singletonList(
                            CategoryBucket.createCategoryBucket(getAttribute(attribute), Type.ATTRIBUTE))));
    }
}
