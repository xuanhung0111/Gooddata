package com.gooddata.qa.graphene.indigo.dashboards.eventing;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardEventingTest;
import com.gooddata.qa.graphene.utils.ElementUtils;
import com.gooddata.qa.utils.java.RetryCommand;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.TimeoutException;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;

import static com.gooddata.qa.graphene.enums.indigo.ReportType.COLUMN_CHART;
import static com.gooddata.qa.graphene.enums.indigo.ReportType.TABLE;
import static com.gooddata.qa.graphene.enums.indigo.ReportType.HEAT_MAP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_REGION;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_OPPORTUNITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class EventingBasicInsightTest extends AbstractDashboardEventingTest {

    private boolean isMobileRunning;

    @BeforeClass(alwaysRun = true)
    public void addUsersOnDesktopExecution(ITestContext context) {
        isMobileRunning = Boolean.parseBoolean(context.getCurrentXmlTest().getParameter("isMobileRunning"));
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"mobile"})
    public void eventingTableReportSingleMetric() throws IOException {
        String insightUri = createInsight("single_metric_table_insight", TABLE,
                singletonList(METRIC_NUMBER_OF_ACTIVITIES), emptyList());
        final String dashboardUri = createAnalyticalDashboard("kpi_eventing_1", insightUri);
        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};
        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri) + "?preventDefault=true", uris.toString());
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Insight insight = indigoDashboardsPage.getLastWidget(Insight.class);

        cleanUpLogger();
        insight.getPivotTableReport().getCellElement(METRIC_NUMBER_OF_ACTIVITIES, 0).click();
        JSONObject content = getLatestPostMessageObj();
        verifyTableReport(content, METRIC_NUMBER_OF_ACTIVITIES, activityUri);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"mobile"})
    public void eventingColumnReportSingleMetric() throws IOException {
        String insightUri = createInsight("single_metric_column_insight", COLUMN_CHART,
                singletonList(METRIC_NUMBER_OF_ACTIVITIES), emptyList());
        final String dashboardUri = createAnalyticalDashboard("kpi_eventing_2", insightUri);
        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};
        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri) + "?preventDefault=true", uris.toString());
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Insight insight = indigoDashboardsPage.getLastWidget(Insight.class);

        cleanUpLogger();
        insight.getChartReport().clickOnElement(Pair.of(0, 0));
        JSONObject content = getLatestPostMessageObj();
        verifyColumnDrillContext(content);

        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        JSONArray intersection = drillContext.getJSONArray("intersection");
        assertEquals(intersection.length(), 1);
        verifyColumnIntersection(intersection.getJSONObject(0), METRIC_NUMBER_OF_ACTIVITIES, activityUri);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"mobile"})
    public void eventingTableReportMultipleMetrics() throws IOException {
        String insightUri = createInsight("multiple_metrics_table_insight", TABLE,
                Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_NUMBER_OF_OPPORTUNITIES), emptyList());
        final String dashboardUri = createAnalyticalDashboard("kpi_eventing_3", insightUri);
        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String opportunityUri = getMetricByTitle(METRIC_NUMBER_OF_OPPORTUNITIES).getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(opportunityUri);
        }};
        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri) + "?preventDefault=true", uris.toString());
        if (isMobileRunning) {
            indigoRestRequest.editWidthOfWidget(dashboardUri, 0, 0, 12);
        }
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Insight insight = indigoDashboardsPage.getLastWidget(Insight.class);

        cleanUpLogger();
        insight.getPivotTableReport().getCellElement(METRIC_NUMBER_OF_ACTIVITIES, 0).click();
        JSONObject content = getLatestPostMessageObj();
        verifyTableReport(content, METRIC_NUMBER_OF_ACTIVITIES, activityUri);

        cleanUpLogger();
        insight.getPivotTableReport().getCellElement(METRIC_NUMBER_OF_OPPORTUNITIES, 0).click();
        content = getLatestPostMessageObj();
        verifyTableReport(content, METRIC_NUMBER_OF_OPPORTUNITIES, opportunityUri);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"mobile"})
    public void eventingColumnReportMultipleMetrics() throws IOException {
        String insightUri = createInsight("multiple_metrics_column_insight", COLUMN_CHART,
                Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_NUMBER_OF_OPPORTUNITIES), emptyList());
        final String dashboardUri = createAnalyticalDashboard("kpi_eventing_4", insightUri);
        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String opportunityUri = getMetricByTitle(METRIC_NUMBER_OF_OPPORTUNITIES).getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(opportunityUri);
        }};
        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri) + "?preventDefault=true", uris.toString());
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Insight insight = indigoDashboardsPage.getLastWidget(Insight.class);

        cleanUpLogger();
        insight.getChartReport().clickOnElement(Pair.of(0, 0));
        JSONObject content = getLatestPostMessageObj();
        verifyColumnDrillContext(content);

        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        JSONArray intersection = drillContext.getJSONArray("intersection");
        assertEquals(intersection.length(), 1);
        verifyColumnIntersection(intersection.getJSONObject(0), METRIC_NUMBER_OF_ACTIVITIES, activityUri);

        cleanUpLogger();
        insight.getChartReport().clickOnElement(Pair.of(1, 0));
        content = getLatestPostMessageObj();
        verifyColumnDrillContext(content);

        drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        intersection = drillContext.getJSONArray("intersection");
        assertEquals(intersection.length(), 1);
        verifyColumnIntersection(intersection.getJSONObject(0), METRIC_NUMBER_OF_OPPORTUNITIES, opportunityUri);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"mobile"})
    public void eventingTableReportSingleAttribute() throws IOException {
        String insightUri = createInsight("single_attribute_table_insight", TABLE, emptyList(),
                singletonList(ATTR_STAGE_NAME));
        final String dashboardUri = createAnalyticalDashboard("kpi_eventing_5", insightUri);
        final String stageUri = getAttributeByTitle(ATTR_STAGE_NAME).getDefaultDisplayForm().getUri();
        final JSONArray uris = new JSONArray() {{
            put(stageUri);
        }};
        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri) + "?preventDefault=true", uris.toString());
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Insight insight = indigoDashboardsPage.getLastWidget(Insight.class);

        cleanUpLogger();
        insight.getPivotTableReport().getCellElement(ATTR_STAGE_NAME, 0).click();
        JSONObject content = getLatestPostMessageObj();
        verifyTableReport(content, ATTR_STAGE_NAME, stageUri);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"mobile"})
    public void eventingTableReportMultipleAttributes() throws IOException {
        String insightUri = createInsight("multiple_attributes_table_insight", TABLE, emptyList(),
                Arrays.asList(ATTR_STAGE_NAME, ATTR_REGION));
        final String dashboardUri = createAnalyticalDashboard("kpi_eventing_6", insightUri);
        final String stageUri = getAttributeByTitle(ATTR_STAGE_NAME).getDefaultDisplayForm().getUri();
        final String regionUri = getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(stageUri);
            put(regionUri);
        }};
        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri) + "?preventDefault=true", uris.toString());
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Insight insight = indigoDashboardsPage.getLastWidget(Insight.class);

        cleanUpLogger();
        insight.getPivotTableReport().getCellElement(ATTR_STAGE_NAME, 0).click();
        JSONObject content = getLatestPostMessageObj();
        verifyTableReport(content, ATTR_STAGE_NAME, stageUri);

        cleanUpLogger();
        insight.getPivotTableReport().getCellElement(ATTR_REGION, 0).click();
        content = getLatestPostMessageObj();
        verifyTableReport(content, ATTR_REGION, regionUri);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"mobile"})
    public void eventingTableReportMultipleMetricsSingleAttribute() throws IOException {
        String insightUri = createInsight("single_attribute__multiple_metric_table_insight", TABLE,
                Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_NUMBER_OF_OPPORTUNITIES),
                singletonList(ATTR_REGION));
        final String dashboardUri = createAnalyticalDashboard("kpi_eventing_7", insightUri);
        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String opportunityUri = getMetricByTitle(METRIC_NUMBER_OF_OPPORTUNITIES).getUri();
        final String regionUri = getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(opportunityUri);
            put(regionUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri) + "?preventDefault=true", uris.toString());
        if (isMobileRunning) {
            indigoRestRequest.editWidthOfWidget(dashboardUri, 0, 0, 12);
        }
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Insight insight = indigoDashboardsPage.getLastWidget(Insight.class);

        cleanUpLogger();
        insight.getPivotTableReport().getCellElement(ATTR_REGION, 0).click();
        JSONObject content = getLatestPostMessageObj();
        verifyTableReport(content, ATTR_REGION, regionUri);

        cleanUpLogger();
        insight.getPivotTableReport().getCellElement(METRIC_NUMBER_OF_ACTIVITIES, 0).click();
        content = getLatestPostMessageObj();
        verifyTableReport(content, METRIC_NUMBER_OF_ACTIVITIES, activityUri);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"mobile"})
    public void eventingTableReportSingleMetricSingleAttribute() throws IOException {
        String insightUri = createInsight("single_attribute_single_metric_table_insight", TABLE,
                singletonList(METRIC_NUMBER_OF_ACTIVITIES),
                singletonList(ATTR_REGION));
        final String dashboardUri = createAnalyticalDashboard("kpi_eventing_8", insightUri);
        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String regionUri = getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(regionUri);
        }};
        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri) + "?preventDefault=true", uris.toString());
        if (isMobileRunning) {
            indigoRestRequest.editWidthOfWidget(dashboardUri, 0, 0, 12);
        }
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Insight insight = indigoDashboardsPage.getLastWidget(Insight.class);

        cleanUpLogger();
        insight.getPivotTableReport().getCellElement(ATTR_REGION, 0).click();
        JSONObject content = getLatestPostMessageObj();
        verifyTableReport(content, ATTR_REGION, regionUri);

        cleanUpLogger();
        insight.getPivotTableReport().getCellElement(METRIC_NUMBER_OF_ACTIVITIES, 0).click();
        content = getLatestPostMessageObj();
        verifyTableReport(content, METRIC_NUMBER_OF_ACTIVITIES, activityUri);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"mobile"})
    public void eventingColumnReportSingleMetricSingleAttribute() throws IOException {
        String insightUri = createInsight("single_attribute_single_metric_column_insight", COLUMN_CHART,
                singletonList(METRIC_NUMBER_OF_ACTIVITIES), singletonList(ATTR_REGION));
        final String dashboardUri = createAnalyticalDashboard("kpi_eventing_9", insightUri);
        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String regionUri = getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(regionUri);
        }};
        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri) + "?preventDefault=true", uris.toString());
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Insight insight = indigoDashboardsPage.getLastWidget(Insight.class);

        cleanUpLogger();
        Pair<Integer, Integer> position = getColumnPosition(insight.getChartReport(),
                METRIC_NUMBER_OF_ACTIVITIES, "East Coast");
        insight.getChartReport().clickOnElement(position);

        JSONObject content = getLatestPostMessageObj();
        verifyColumnDrillContext(content);
        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        JSONArray intersection = drillContext.getJSONArray("intersection");
        assertEquals(intersection.length(), 2);
        verifyColumnIntersection(intersection.getJSONObject(0), METRIC_NUMBER_OF_ACTIVITIES, activityUri);
        verifyColumnIntersection(intersection.getJSONObject(1), "East Coast", regionUri);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"mobile"})
    public void eventingColumnReportHasStackBy() throws IOException {
        String insightUri = createInsight("test_stackby_column_insight", COLUMN_CHART,
                singletonList(METRIC_NUMBER_OF_ACTIVITIES),
                singletonList(ATTR_ACTIVITY_TYPE), ATTR_REGION);

        final String dashboardUri = createAnalyticalDashboard("kpi_eventing_10", insightUri);
        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String activityTypeUri = getAttributeByTitle(ATTR_ACTIVITY_TYPE).getDefaultDisplayForm().getUri();
        final String regionUri = getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(activityTypeUri);
            put(regionUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri) + "?preventDefault=true", uris.toString());
        if (isMobileRunning) {
            indigoRestRequest.editWidthOfWidget(dashboardUri, 0, 0, 12);
        }
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Insight insight = indigoDashboardsPage.getLastWidget(Insight.class);
        Pair<Integer, Integer> position = getColumnPosition(insight.getChartReport(), "East Coast", "Email");

        cleanUpLogger();
        insight.getChartReport().clickOnElement(position);
        JSONObject content = getLatestPostMessageObj();
        verifyColumnDrillContext(content);
        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        JSONArray intersection = drillContext.getJSONArray("intersection");
        assertEquals(intersection.length(), 3);
        verifyColumnIntersection(intersection.getJSONObject(0), METRIC_NUMBER_OF_ACTIVITIES, activityUri);
        verifyColumnIntersection(intersection.getJSONObject(1), "Email", activityTypeUri);
        verifyColumnIntersection(intersection.getJSONObject(2), "East Coast", regionUri);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"mobile"})
    public void eventingColumnReportHasStackBySameAsViewBy() throws IOException {
        String insightUri = createInsight("test_same_attr_column_insight", COLUMN_CHART,
                singletonList(METRIC_NUMBER_OF_ACTIVITIES), singletonList(ATTR_ACTIVITY_TYPE), ATTR_ACTIVITY_TYPE);
        final String dashboardUri = createAnalyticalDashboard("kpi_eventing_11", insightUri);
        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String activityTypeUri = getAttributeByTitle(ATTR_ACTIVITY_TYPE).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(activityTypeUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri) + "?preventDefault=true", uris.toString());
        if (isMobileRunning) {
            indigoRestRequest.editWidthOfWidget(dashboardUri, 0, 0, 12);
        }
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Insight insight = indigoDashboardsPage.getLastWidget(Insight.class);

        Pair<Integer, Integer> position = getColumnPosition(insight.getChartReport(), "Email", "Email");
        cleanUpLogger();
        insight.getChartReport().clickOnElement(position);

        JSONObject content = getLatestPostMessageObj();
        verifyColumnDrillContext(content);
        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        JSONArray intersection = drillContext.getJSONArray("intersection");
        assertEquals(intersection.length(), 3);

        verifyColumnIntersection(intersection.getJSONObject(0), METRIC_NUMBER_OF_ACTIVITIES, activityUri);
        verifyColumnIntersection(intersection.getJSONObject(1), "Email", activityTypeUri);
        verifyColumnIntersection(intersection.getJSONObject(2), "Email", activityTypeUri);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"mobile"})
    public void eventingTableReportSingleMetricViewByDate() throws IOException {
        String insightUri = createInsight("test_view_date_table_insight", TABLE,
                singletonList(METRIC_NUMBER_OF_ACTIVITIES), singletonList(ATTR_YEAR_ACTIVITY));
        final String dashboardUri = createAnalyticalDashboard("kpi_eventing_12", insightUri);
        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String yearActivity = getAttributeByTitle(ATTR_YEAR_ACTIVITY).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(yearActivity);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri) + "?preventDefault=true", uris.toString());
        if (isMobileRunning) {
            indigoRestRequest.editWidthOfWidget(dashboardUri, 0, 0, 12);
        }
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Insight insight = indigoDashboardsPage.getLastWidget(Insight.class);

        cleanUpLogger();
        insight.getPivotTableReport().getCellElement(METRIC_NUMBER_OF_ACTIVITIES, 0).click();
        JSONObject content = getLatestPostMessageObj();
        verifyTableReport(content, METRIC_NUMBER_OF_ACTIVITIES, activityUri);

        cleanUpLogger();
        insight.getPivotTableReport().getCellElement(ATTR_YEAR_ACTIVITY, 0).click();
        content = getLatestPostMessageObj();
        verifyTableReport(content, ATTR_YEAR_ACTIVITY, yearActivity);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"mobile"})
    public void eventingColumnReportSingleMetricViewByDate() throws IOException {
        String insightUri = createInsight("test_view_date_column_insight", COLUMN_CHART,
                singletonList(METRIC_NUMBER_OF_ACTIVITIES), singletonList(ATTR_YEAR_ACTIVITY));
        final String dashboardUri = createAnalyticalDashboard("kpi_eventing_13", insightUri);
        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String yearActivity = getAttributeByTitle(ATTR_YEAR_ACTIVITY).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(yearActivity);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri) + "?preventDefault=true", uris.toString());
        if (isMobileRunning) {
            indigoRestRequest.editWidthOfWidget(dashboardUri, 0, 0, 12);
        }
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Insight insight = indigoDashboardsPage.getLastWidget(Insight.class);

        Pair<Integer, Integer> position = getColumnPosition(insight.getChartReport(),
                METRIC_NUMBER_OF_ACTIVITIES, "2011");

        cleanUpLogger();
        insight.getChartReport().clickOnElement(position);

        JSONObject content = getLatestPostMessageObj();
        verifyColumnDrillContext(content);
        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        JSONArray intersection = drillContext.getJSONArray("intersection");
        assertEquals(intersection.length(), 2);

        verifyColumnIntersection(intersection.getJSONObject(0), METRIC_NUMBER_OF_ACTIVITIES, activityUri);
        verifyColumnIntersection(intersection.getJSONObject(1), "2011", yearActivity);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"mobile"})
    public void eventingColumnReportMultipleMetricsViewByDate() throws IOException {
        String insightUri = createInsight("test_view_date_multi_metrics_column_insight", COLUMN_CHART,
                Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_NUMBER_OF_OPPORTUNITIES),
                singletonList(ATTR_YEAR_CREATED));
        final String dashboardUri = createAnalyticalDashboard("kpi_eventing_14", insightUri);
        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String opportunityUri = getMetricByTitle(METRIC_NUMBER_OF_OPPORTUNITIES).getUri();
        final String yearCreated = getAttributeByTitle(ATTR_YEAR_CREATED).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(opportunityUri);
            put(yearCreated);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri) + "?preventDefault=true", uris.toString());
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Insight insight = indigoDashboardsPage.getLastWidget(Insight.class);

        cleanUpLogger();
        insight.getChartReport().clickOnElement(Pair.of(0, 3));

        JSONObject content = getLatestPostMessageObj();
        verifyColumnDrillContext(content);
        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        JSONArray intersection = drillContext.getJSONArray("intersection");
        assertEquals(intersection.length(), 2);

        verifyColumnIntersection(intersection.getJSONObject(0), METRIC_NUMBER_OF_ACTIVITIES, activityUri);
        verifyColumnIntersection(intersection.getJSONObject(1), "2011", yearCreated);

        cleanUpLogger();
        insight.getChartReport().clickOnElement(Pair.of(1, 3));

        content = getLatestPostMessageObj();
        verifyColumnDrillContext(content);
        drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        intersection = drillContext.getJSONArray("intersection");
        assertEquals(intersection.length(), 2);

        verifyColumnIntersection(intersection.getJSONObject(0), METRIC_NUMBER_OF_OPPORTUNITIES, opportunityUri);
        verifyColumnIntersection(intersection.getJSONObject(1), "2011", yearCreated);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"mobile"})
    public void eventingColumnReportSingleMetricViewByDateHasStack() throws IOException {
        String insightUri = createInsight("test_view_date_has_stack_column_insight", COLUMN_CHART,
                singletonList(METRIC_NUMBER_OF_ACTIVITIES), singletonList(ATTR_YEAR_ACTIVITY), ATTR_REGION);
        final String dashboardUri = createAnalyticalDashboard("kpi_eventing_15", insightUri);
        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String yearActivity = getAttributeByTitle(ATTR_YEAR_ACTIVITY).getDefaultDisplayForm().getUri();
        final String regionUri = getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(yearActivity);
            put(regionUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri) + "?preventDefault=true", uris.toString());
        if (isMobileRunning) {
            indigoRestRequest.editWidthOfWidget(dashboardUri, 0, 0, 12);
        }
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Insight insight = indigoDashboardsPage.getLastWidget(Insight.class);

        Pair<Integer, Integer> position = getColumnPosition(insight.getChartReport(), "East Coast", "2011");

        // Firefox have issue with moveToElement which make application cannot catch 'drill' event at first click,
        // retry is just a workaround
        RetryCommand retryCommand = new RetryCommand(2);
        JSONObject content = retryCommand.retryOnException(TimeoutException.class, () -> {
            try {
                cleanUpLogger();
                insight.getChartReport().clickOnElement(position);
                return getLatestPostMessageObj();
            } catch (JSONException e) {
                // Json exception thrown when eventing data is empty
                // move to top left corner to close all opening popup
                ElementUtils.moveToTopLetCorner();
                // then force a retry
                throw new TimeoutException(e);
            }

        });

        verifyColumnDrillContext(content);
        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        JSONArray intersection = drillContext.getJSONArray("intersection");
        assertEquals(intersection.length(), 3);

        verifyColumnIntersection(intersection.getJSONObject(0), METRIC_NUMBER_OF_ACTIVITIES, activityUri);
        verifyColumnIntersection(intersection.getJSONObject(1), "2011", yearActivity);
        verifyColumnIntersection(intersection.getJSONObject(2), "East Coast", regionUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void eventingAdhocMetric() throws IOException {
        // Cannot use rest to create insight has adhocmetric, use UI to make it
        AnalysisPage analysisPage = initAnalysePage();
        analysisPage.addMetric(FACT_AMOUNT, FieldType.FACT);
        analysisPage.changeReportType(COLUMN_CHART).saveInsight("test_adhoc_fact_insight");
        analysisPage.waitForReportComputing();
        final String dashboardUri = createAnalyticalDashboard("kpi_eventing_16", getInsightUriFromBrowserUrl());
        final String amountUri = getFactByTitle(FACT_AMOUNT).getUri();

        JSONArray uris = new JSONArray() {{
            put(amountUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri) + "?preventDefault=true", uris.toString());
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Insight insight = indigoDashboardsPage.getLastWidget(Insight.class);

        cleanUpLogger();
        insight.getChartReport().clickOnElement(Pair.of(0, 0));

        JSONObject content = getLatestPostMessageObj();
        verifyColumnDrillContext(content);
        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        JSONArray intersection = drillContext.getJSONArray("intersection");

        assertEquals(drillContext.getString("type"), "column");
        assertEquals(drillContext.getString("element"), "bar");
        assertFalse(drillContext.isNull("x"), "drill event of column chart should show X");
        assertFalse(drillContext.isNull("y"), "drill event of column chart should show Y");

        assertEquals(intersection.length(), 1);
        assertEquals(intersection.getJSONObject(0).get("title"), String.format("Sum of %s", FACT_AMOUNT));
        assertEquals(intersection.getJSONObject(0).getJSONObject("header").get("identifier"), "");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void eventingHeatMapReport() throws IOException {
        String insightUri = createInsight("test_view_date_has_stack_heatmap_insight", HEAT_MAP,
                singletonList(METRIC_NUMBER_OF_ACTIVITIES), singletonList(ATTR_ACTIVITY_TYPE), ATTR_DEPARTMENT);
        final String dashboardUri = createAnalyticalDashboard("kpi_eventing_17", insightUri);
        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String activityTypeUri = getAttributeByTitle(ATTR_ACTIVITY_TYPE).getDefaultDisplayForm().getUri();
        final String departmentUri = getAttributeByTitle(ATTR_DEPARTMENT).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(activityTypeUri);
            put(departmentUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri) + "?preventDefault=true", uris.toString());
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Insight insight = indigoDashboardsPage.getLastWidget(Insight.class);

        Pair<Integer, Integer> position = Pair.of(0,0);

        // Firefox have issue with moveToElement which make application cannot catch 'drill' event at first click,
        // retry is just a workaround
        RetryCommand retryCommand = new RetryCommand(2);
        JSONObject content = retryCommand.retryOnException(TimeoutException.class, () -> {
            try {
                cleanUpLogger();
                insight.getChartReport().clickOnElement(position);
                return getLatestPostMessageObj();
            } catch (JSONException e) {
                // Json exception thrown when eventing data is empty
                // move to top left corner to close all opening popup
                ElementUtils.moveToTopLetCorner();
                // then force a retry
                throw new TimeoutException(e);
            }

        });

        verifyHeadMapDrillContext(content);
        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        JSONArray intersection = drillContext.getJSONArray("intersection");
        assertEquals(intersection.length(), 3);

        verifyColumnIntersection(intersection.getJSONObject(0), METRIC_NUMBER_OF_ACTIVITIES, activityUri);
        verifyColumnIntersection(intersection.getJSONObject(1), "Direct Sales", departmentUri);
        verifyColumnIntersection(intersection.getJSONObject(2), "Web Meeting", activityTypeUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void eventingHeatMapReportWithDateOnRows() throws IOException {
        String insightUri = createInsight("test_view_date_has_stack_heatmap_insight", HEAT_MAP,
                singletonList(METRIC_NUMBER_OF_ACTIVITIES), singletonList(ATTR_YEAR_ACTIVITY), ATTR_REGION);
        final String dashboardUri = createAnalyticalDashboard("kpi_eventing_17", insightUri);
        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String yearActivity = getAttributeByTitle(ATTR_YEAR_ACTIVITY).getDefaultDisplayForm().getUri();
        final String regionUri = getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(yearActivity);
            put(regionUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri) + "?preventDefault=true", uris.toString());
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Insight insight = indigoDashboardsPage.getLastWidget(Insight.class);

        Pair<Integer, Integer> position = Pair.of(0,0);

        // Firefox have issue with moveToElement which make application cannot catch 'drill' event at first click,
        // retry is just a workaround
        RetryCommand retryCommand = new RetryCommand(2);
        JSONObject content = retryCommand.retryOnException(TimeoutException.class, () -> {
            try {
                cleanUpLogger();
                insight.getChartReport().clickOnElement(position);
                return getLatestPostMessageObj();
            } catch (JSONException e) {
                // Json exception thrown when eventing data is empty
                // move to top left corner to close all opening popup
                ElementUtils.moveToTopLetCorner();
                // then force a retry
                throw new TimeoutException(e);
            }

        });

        verifyHeadMapDrillContext(content);
        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        JSONArray intersection = drillContext.getJSONArray("intersection");
        assertEquals(intersection.length(), 3);

        verifyColumnIntersection(intersection.getJSONObject(0), METRIC_NUMBER_OF_ACTIVITIES, activityUri);
        verifyColumnIntersection(intersection.getJSONObject(1), "East Coast", regionUri);
        verifyColumnIntersection(intersection.getJSONObject(2), "2016", yearActivity);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void eventingHeatMapReportWithDateOnColumns() throws IOException {
        String insightUri = createInsight("test_view_date_has_stack_heatmap_insight", HEAT_MAP,
                singletonList(METRIC_NUMBER_OF_ACTIVITIES), singletonList(ATTR_REGION), ATTR_YEAR_ACTIVITY);
        final String dashboardUri = createAnalyticalDashboard("kpi_eventing_17", insightUri);
        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String yearActivity = getAttributeByTitle(ATTR_YEAR_ACTIVITY).getDefaultDisplayForm().getUri();
        final String regionUri = getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(yearActivity);
            put(regionUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri) + "?preventDefault=true", uris.toString());
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Insight insight = indigoDashboardsPage.getLastWidget(Insight.class);

        Pair<Integer, Integer> position = Pair.of(0,0);

        // Firefox have issue with moveToElement which make application cannot catch 'drill' event at first click,
        // retry is just a workaround
        RetryCommand retryCommand = new RetryCommand(2);
        JSONObject content = retryCommand.retryOnException(TimeoutException.class, () -> {
            try {
                cleanUpLogger();
                insight.getChartReport().clickOnElement(position);
                return getLatestPostMessageObj();
            } catch (JSONException e) {
                // Json exception thrown when eventing data is empty
                // move to top left corner to close all opening popup
                ElementUtils.moveToTopLetCorner();
                // then force a retry
                throw new TimeoutException(e);
            }

        });

        verifyHeadMapDrillContext(content);
        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        JSONArray intersection = drillContext.getJSONArray("intersection");
        assertEquals(intersection.length(), 3);

        verifyColumnIntersection(intersection.getJSONObject(0), METRIC_NUMBER_OF_ACTIVITIES, activityUri);
        verifyColumnIntersection(intersection.getJSONObject(1), "2008", yearActivity);
        verifyColumnIntersection(intersection.getJSONObject(2), "West Coast", regionUri);
    }

    private void verifyTableReport(JSONObject content, String columnTitle, String expectedUri) {
        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        JSONArray intersection = drillContext.getJSONArray("intersection");
        assertFalse(drillContext.isNull("columnIndex"), "drill event of table report should show columnIndex");
        assertFalse(drillContext.isNull("rowIndex"), "drill event of table report should show rowIndex");

        String uri = intersection.getJSONObject(0).getJSONObject("header").getString("uri");
        String title = intersection.getJSONObject(0).getString("title");
        assertEquals(drillContext.getString("type"), "table");
        assertEquals(drillContext.getString("element"), "cell");
        assertEquals(uri, expectedUri);
        assertEquals(title, columnTitle);
    }

    private void verifyColumnIntersection(JSONObject intersection, String expectedTitle, String expectedUri) {
        String uri = intersection.getJSONObject("header").getString("uri");
        String title = intersection.getString("title");
        assertEquals(title, expectedTitle);
        assertEquals(uri, expectedUri);
    }

    private void verifyColumnDrillContext(JSONObject content) {
        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        assertEquals(drillContext.getString("type"), "column");
        assertEquals(drillContext.getString("element"), "bar");
        assertFalse(drillContext.isNull("x"), "drill event of column chart should show X");
        assertFalse(drillContext.isNull("y"), "drill event of column chart should show Y");
    }

    private void verifyHeadMapDrillContext(JSONObject content) {
        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        log.info("drillContext : " + drillContext);
        assertEquals(drillContext.getString("type"), "heatmap");
        assertEquals(drillContext.getString("element"), "cell");
    }
}
