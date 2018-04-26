package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.md.Attribute;
import com.gooddata.md.Metric;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport.AggregationItem;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils;
import org.apache.http.ParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_CLOSED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_SALES_REP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_CLOSE_EOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.utils.http.RestUtils.getJsonObject;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createInsight;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.getInsightUri;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class TotalsResultWithInsightTest extends AbstractAnalyseTest{

    private static final String INSIGHT_HAS_ATTRIBUTE_AND_MEASURE = "Insight has attribute and measure";
    private static final String INSIGHT_HAS_ATTRIBUTES_AND_MEASURES = "Insight has attributes and measures";
    private static final String INSIGHT_HAS_DATE_ATTRIBUTE_AND_MEASURE = "Insight has date attribute and measure";
    private static final String INSIGHT_SHOW_POP = "Insight show POP";
    private static final String INSIGHT_SHOW_PERCENT = "Insight show percent";

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
    }

    @Test(dependsOnGroups = "createProject")
    public void prepareInsights() {
        createSimpleInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE, METRIC_NUMBER_OF_ACTIVITIES, ATTR_DEPARTMENT);
        createSimpleInsight(INSIGHT_HAS_DATE_ATTRIBUTE_AND_MEASURE, METRIC_NUMBER_OF_ACTIVITIES, ATTR_YEAR_ACTIVITY);

        createInsight(getRestApiClient(), testParams.getProjectId(),
            new InsightMDConfiguration(INSIGHT_HAS_ATTRIBUTES_AND_MEASURES, ReportType.TABLE)
                    .setMeasureBucket(asList(
                            MeasureBucket.createSimpleMeasureBucket(getMetric(METRIC_NUMBER_OF_ACTIVITIES)),
                            MeasureBucket.createSimpleMeasureBucket(getMetric(METRIC_CLOSE_EOP))))
                    .setCategoryBucket(asList(
                            CategoryBucket.createViewByBucket(getAttribute(ATTR_DEPARTMENT)),
                            CategoryBucket.createViewByBucket(getAttribute(ATTR_SALES_REP)))));

        createInsight(getRestApiClient(), testParams.getProjectId(),
            new InsightMDConfiguration(INSIGHT_SHOW_PERCENT, ReportType.TABLE)
                    .setMeasureBucket(singletonList(MeasureBucket.createMeasureBucketWithShowInPercent(
                            getMetric(METRIC_NUMBER_OF_ACTIVITIES), true)))
                    .setCategoryBucket(singletonList(CategoryBucket.createViewByBucket(getAttribute(ATTR_YEAR_ACTIVITY)))));

        createSimpleInsight(INSIGHT_SHOW_POP, METRIC_NUMBER_OF_ACTIVITIES, ATTR_YEAR_ACTIVITY);
        AnalysisPage analysisPage = initAnalysePage();
        analysisPage.openInsight(INSIGHT_SHOW_POP).getMetricsBucket().getLastMetricConfiguration().expandConfiguration().showPop();
        analysisPage.saveInsight();
    }

    @DataProvider
    public Object[][] getSavedInsight() {
        return new Object[][] {
                {INSIGHT_HAS_ATTRIBUTE_AND_MEASURE, "101,054"},
                {INSIGHT_HAS_DATE_ATTRIBUTE_AND_MEASURE, "73,073"},
                {INSIGHT_SHOW_POP, "73,073"},
                {INSIGHT_SHOW_PERCENT, "47.37%"}
        };
    }

    @Test(dependsOnMethods = "prepareInsights", dataProvider = "getSavedInsight")
    public void saveInsightHasTotalsResult(String insight, String totalsValue) throws JSONException, IOException {
        String metric = METRIC_NUMBER_OF_ACTIVITIES;
        if (insight.equals(INSIGHT_SHOW_PERCENT))
            metric = "% " + METRIC_NUMBER_OF_ACTIVITIES;
        AnalysisPage analysisPage = initAnalysePage();

        //open exist insight
        analysisPage.openInsight(insight).waitForReportComputing();
        TableReport tableReport = analysisPage.getTableReport();
        tableReport.addNewTotals(AggregationItem.MAX, metric);
        Screenshots.takeScreenshot(browser, "Save " + insight + " has totals result", getClass());
        assertEquals(tableReport.getTotalsValue(AggregationItem.MAX, metric), totalsValue);

        //save insight as
        analysisPage.saveInsightAs(insight + generateHashString());
        checkRedBar(browser);
        assertEquals(tableReport.getTotalsValue(AggregationItem.MAX, metric), totalsValue);

        //modify and save
        analysisPage.addAttribute(ATTR_IS_CLOSED).saveInsightAs(insight + generateHashString());
        checkRedBar(browser);
        assertEquals(tableReport.getTotalsValue(AggregationItem.MAX, metric), totalsValue);
    }

    @DataProvider
    public Object[][] getRemovedInsight() {
        return new Object[][] {
                {INSIGHT_HAS_ATTRIBUTE_AND_MEASURE},
                {INSIGHT_HAS_ATTRIBUTES_AND_MEASURES},
                {INSIGHT_SHOW_POP}
        };
    }

    @Test(dependsOnMethods = "prepareInsights", dataProvider = "getRemovedInsight")
    public void removedInsightHasTotalsResult(String insight) throws JSONException, IOException {
        String metric = METRIC_NUMBER_OF_ACTIVITIES;
        if (insight.equals(INSIGHT_SHOW_PERCENT))
            metric = "% " + METRIC_NUMBER_OF_ACTIVITIES;
        AnalysisPage analysisPage = initAnalysePage();
        analysisPage.openInsight(insight).waitForReportComputing();
        //remove one of totals results
        TableReport tableReport = analysisPage.getTableReport()
            .addNewTotals(AggregationItem.MAX, metric)
            .addNewTotals(AggregationItem.SUM, metric)
            .deleteTotalsResultCell(AggregationItem.MAX, metric);
        Screenshots.takeScreenshot(browser, "Remove " + insight + " has totals result", getClass());
        assertTrue(tableReport.hasTotalsResult(), "The rest row should be kept as before");

        //remove all totals results
        tableReport.deleteTotalsResultCell(AggregationItem.SUM, metric);
        assertFalse(tableReport.hasTotalsResult(), "The empty total row should be disappeared");

        //save empty totals result
        analysisPage.saveInsightAs(insight + generateHashString());
        assertFalse(tableReport.hasTotalsResult(), "The empty total row cannot be saved");
    }

    @Test(dependsOnMethods = "prepareInsights")
    public void emptyTotalsResultOnKD() {
        AnalysisPage analysisPage = initAnalysePage();
        analysisPage.openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE).getTableReport()
            .addNewTotals(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES)
            .deleteTotalsResultRow(AggregationItem.MAX);
        analysisPage.saveInsightAs("empty totals result");

        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage() ;
        indigoDashboardsPage.addDashboard().addInsight("empty totals result").getConfigurationPanel().disableDateFilter();
        assertFalse(indigoDashboardsPage.waitForWidgetsLoading().getLastWidget(Insight.class).getTableReport().hasTotalsResult(),
            "The empty total row cannot show at KD");
    }

    @Test(dependsOnMethods = "prepareInsights")
    public void checkMetadataTotalsResult() throws JSONException, IOException {
        AnalysisPage analysisPage = initAnalysePage();
        analysisPage.openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE).getTableReport()
            .addNewTotals(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES);
        analysisPage.saveInsightAs("Add totals result");
        Screenshots.takeScreenshot(browser, "Check metadata totals result", getClass());
        assertEquals(countTotalsDefinitions("Add totals result"), 1);

        analysisPage.getTableReport().deleteTotalsResultRow(AggregationItem.MAX);
        analysisPage.saveInsightAs("Delete totals result");
        assertEquals(countTotalsDefinitions("Delete totals result"), 0);
    }

    @Test(dependsOnMethods = "prepareInsights")
    public void undoAndRedoInsightHasTotalsResult() {
        AnalysisPage analysisPage = initAnalysePage();
        analysisPage.openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE);

        TableReport tableReport = analysisPage.getTableReport();
        tableReport.addNewTotals(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES).deleteTotalsResultRow(AggregationItem.MAX);

        //Undo
        analysisPage.undo().waitForReportComputing();
        assertTrue(tableReport.hasTotalsResult(), "Totals Result should be reverted");
        assertEquals(tableReport.getTotalsValue(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES), "101,054");

        //Redo
        analysisPage.redo().waitForReportComputing();
        assertFalse(tableReport.hasTotalsResult(), "Totals Result should be removed");
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
        String assignedMufUserId = UserManagementRestUtils
            .getUserProfileUri(getDomainUserRestApiClient(), testParams.getUserDomain(), testParams.getEditorUser());

        dashboardRestRequest.addMufToUser(assignedMufUserId, mufUri);

        AnalysisPage analysisPage = initAnalysePage();
        TableReport tableReport = analysisPage.openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE).getTableReport();
        tableReport.addNewTotals(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES);
        analysisPage.saveInsightAs(newInsight);
        assertEquals(tableReport.getTotalsValue(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES), "101,054");

        logoutAndLoginAs(true, UserRoles.EDITOR);
        try {
            tableReport = initAnalysePage().openInsight(newInsight).getTableReport();
            Screenshots.takeScreenshot(browser, "Check totals results with MUF", getClass());
            assertEquals(tableReport.getTotalsValue(AggregationItem.MAX, METRIC_NUMBER_OF_ACTIVITIES), "53,217");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    private int countTotalsDefinitions(String insight) throws JSONException, IOException {
        int countTotals = 0;
        JSONArray buckets = getJsonObject(restApiClient, getInsightUri(insight, getRestApiClient(), testParams.getProjectId()))
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
        createInsight(getRestApiClient(), testParams.getProjectId(),
            new InsightMDConfiguration(title, ReportType.TABLE)
                    .setMeasureBucket(singletonList(MeasureBucket.createSimpleMeasureBucket(getMetric(metric))))
                    .setCategoryBucket(singletonList(CategoryBucket.createViewByBucket(getAttribute(attribute)))));
    }
}
