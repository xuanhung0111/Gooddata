package com.gooddata.qa.graphene.indigo.analyze.eventing;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.AggregationItem;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.EmbeddedAnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.PivotTableReport;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractEventingTest;
import com.gooddata.qa.graphene.indigo.analyze.utils.InsightUtils;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestClient;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static com.gooddata.qa.graphene.enums.indigo.ReportType.COLUMN_CHART;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_REGION;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_FORECAST_CATEGORY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_OPPORTUNITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT_BOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_BEST_CASE;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class EventingBasicTest extends AbstractEventingTest {
    public InsightUtils insightUtils;

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();

        Metrics metrics = getMetricCreator();
        metrics.createAmountBOPMetric();
        metrics.createBestCaseMetric();
        insightUtils = new InsightUtils(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void eventingColumnReportSingleMetric() throws IOException {
        String insightUri = insightUtils.createSimpleColumnInsight("single_metric_column_insight",
                Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES), Collections.emptyList(), "");
        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};
        final String file = createTemplateHtmlFile(getObjectIdFromUri(insightUri), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        embeddedAnalysisPage.getChartReport().clickOnElement(Pair.of(0, 0));
        JSONObject content = getLatestPostMessageObj();
        verifyColumnDrillContext(content);

        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        JSONArray intersection = drillContext.getJSONArray("intersection");
        assertEquals(intersection.length(), 1);
        verifyColumnIntersection(intersection.getJSONObject(0), METRIC_NUMBER_OF_ACTIVITIES, activityUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void eventingColumnReportMultipleMetrics() throws IOException {
        String insightUri = insightUtils.createSimpleColumnInsight("multiple_metrics_column_insight",
                Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_NUMBER_OF_OPPORTUNITIES),
                Collections.emptyList(), "");
        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String opportunityUri = getMetricByTitle(METRIC_NUMBER_OF_OPPORTUNITIES).getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(opportunityUri);
        }};
        final String file = createTemplateHtmlFile(getObjectIdFromUri(insightUri), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        embeddedAnalysisPage.getChartReport().clickOnElement(Pair.of(0, 0));
        JSONObject content = getLatestPostMessageObj();
        verifyColumnDrillContext(content);

        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        JSONArray intersection = drillContext.getJSONArray("intersection");
        assertEquals(intersection.length(), 1);
        verifyColumnIntersection(intersection.getJSONObject(0), METRIC_NUMBER_OF_ACTIVITIES, activityUri);

        cleanUpLogger();
        embeddedAnalysisPage.getChartReport().clickOnElement(Pair.of(1, 0));
        content = getLatestPostMessageObj();
        verifyColumnDrillContext(content);

        drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        intersection = drillContext.getJSONArray("intersection");
        assertEquals(intersection.length(), 1);
        verifyColumnIntersection(intersection.getJSONObject(0), METRIC_NUMBER_OF_OPPORTUNITIES, opportunityUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void eventingColumnReportSingleMetricSingleAttribute() throws IOException {
        String insightUri = insightUtils.createSimpleColumnInsight("single_attribute_single_metric_column_insight",
            Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES), Arrays.asList(ATTR_REGION),"");
        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String regionUri = getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(regionUri);
        }};
        final String file = createTemplateHtmlFile(getObjectIdFromUri(insightUri), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        Pair<Integer, Integer> position  = getColumnPosition(embeddedAnalysisPage.getChartReport(),
            METRIC_NUMBER_OF_ACTIVITIES, "East Coast");
        embeddedAnalysisPage.getChartReport().clickOnElement(position);

        JSONObject content = getLatestPostMessageObj();
        verifyColumnDrillContext(content);
        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        JSONArray intersection = drillContext.getJSONArray("intersection");
        assertEquals(intersection.length(), 2);
        verifyColumnIntersection(intersection.getJSONObject(0), METRIC_NUMBER_OF_ACTIVITIES, activityUri);
        verifyColumnIntersection(intersection.getJSONObject(1), "East Coast", regionUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void eventingColumnReportHasStackBy() throws IOException {
        String insightUri = insightUtils.createSimpleColumnInsight("test_stackby_column_insight",
            Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES),
            Arrays.asList(ATTR_ACTIVITY_TYPE),  ATTR_REGION);

        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String activityTypeUri = getAttributeByTitle(ATTR_ACTIVITY_TYPE).getDefaultDisplayForm().getUri();
        final String regionUri = getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(activityTypeUri);
            put(regionUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromUri(insightUri), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();
        Pair<Integer, Integer> position  = getColumnPosition(embeddedAnalysisPage.getChartReport(), "East Coast", "Email");

        cleanUpLogger();
        embeddedAnalysisPage.getChartReport().clickOnElement(position);
        JSONObject content = getLatestPostMessageObj();
        verifyColumnDrillContext(content);
        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        JSONArray intersection = drillContext.getJSONArray("intersection");
        assertEquals(intersection.length(), 3);
        verifyColumnIntersection(intersection.getJSONObject(0), METRIC_NUMBER_OF_ACTIVITIES, activityUri);
        verifyColumnIntersection(intersection.getJSONObject(1), "Email", activityTypeUri);
        verifyColumnIntersection(intersection.getJSONObject(2), "East Coast", regionUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void eventingColumnReportHasStackBySameAsViewBy() throws IOException {
        String insightUri = insightUtils.createSimpleColumnInsight("test_same_attr_column_insight",
            Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES),
            Arrays.asList(ATTR_ACTIVITY_TYPE), ATTR_ACTIVITY_TYPE);

        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String activityTypeUri = getAttributeByTitle(ATTR_ACTIVITY_TYPE).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(activityTypeUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromUri(insightUri), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();
        Pair<Integer, Integer> position  = getColumnPosition(embeddedAnalysisPage.getChartReport(), "Email", "Email");
        cleanUpLogger();
        embeddedAnalysisPage.getChartReport().clickOnElement(position);

        JSONObject content = getLatestPostMessageObj();
        verifyColumnDrillContext(content);
        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        JSONArray intersection = drillContext.getJSONArray("intersection");
        assertEquals(intersection.length(), 3);

        verifyColumnIntersection(intersection.getJSONObject(0), METRIC_NUMBER_OF_ACTIVITIES, activityUri);
        verifyColumnIntersection(intersection.getJSONObject(1), "Email", activityTypeUri);
        verifyColumnIntersection(intersection.getJSONObject(2), "Email", activityTypeUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void eventingColumnReportSingleMetricViewByDate() throws IOException {
        String insightUri = insightUtils.createSimpleColumnInsight("test_view_date_column_insight",
            Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES),
            Arrays.asList(ATTR_YEAR_ACTIVITY), "");

        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String yearActivity = getAttributeByTitle(ATTR_YEAR_ACTIVITY).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(yearActivity);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromUri(insightUri), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        Pair<Integer, Integer> position  = getColumnPosition(embeddedAnalysisPage.getChartReport(),
            METRIC_NUMBER_OF_ACTIVITIES, "2011");

        cleanUpLogger();
        embeddedAnalysisPage.getChartReport().clickOnElement(position);

        JSONObject content = getLatestPostMessageObj();
        verifyColumnDrillContext(content);
        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        JSONArray intersection = drillContext.getJSONArray("intersection");
        assertEquals(intersection.length(), 2);

        verifyColumnIntersection(intersection.getJSONObject(0), METRIC_NUMBER_OF_ACTIVITIES, activityUri);
        verifyColumnIntersection(intersection.getJSONObject(1), "2011", yearActivity);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void eventingColumnReportMultipleMetricsViewByDate() throws IOException {
        String insightUri = insightUtils.createSimpleColumnInsight("test_view_date_multi_metrics_column_insight",
            Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_NUMBER_OF_OPPORTUNITIES),
            Arrays.asList(ATTR_YEAR_CREATED), "");

        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String opportunityUri = getMetricByTitle(METRIC_NUMBER_OF_OPPORTUNITIES).getUri();
        final String yearCreated = getAttributeByTitle(ATTR_YEAR_CREATED).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(opportunityUri);
            put(yearCreated);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromUri(insightUri), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        Pair<Integer, Integer> position  = getColumnPosition(embeddedAnalysisPage.getChartReport(),
            METRIC_NUMBER_OF_ACTIVITIES, "2011");

        cleanUpLogger();
        embeddedAnalysisPage.getChartReport().clickOnElement(position);

        JSONObject content = getLatestPostMessageObj();
        verifyColumnDrillContext(content);
        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        JSONArray intersection = drillContext.getJSONArray("intersection");
        assertEquals(intersection.length(), 2);

        verifyColumnIntersection(intersection.getJSONObject(0), METRIC_NUMBER_OF_ACTIVITIES, activityUri);
        verifyColumnIntersection(intersection.getJSONObject(1), "2011", yearCreated);

        position  = getColumnPosition(embeddedAnalysisPage.getChartReport(),
            METRIC_NUMBER_OF_OPPORTUNITIES, "2011");
        cleanUpLogger();
        embeddedAnalysisPage.getChartReport().clickOnElement(position);

        content = getLatestPostMessageObj();
        verifyColumnDrillContext(content);
        drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        intersection = drillContext.getJSONArray("intersection");
        assertEquals(intersection.length(), 2);

        verifyColumnIntersection(intersection.getJSONObject(0), METRIC_NUMBER_OF_OPPORTUNITIES, opportunityUri);
        verifyColumnIntersection(intersection.getJSONObject(1), "2011", yearCreated);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void eventingColumnReportSingleMetricViewByDateHasStack() throws IOException {
        String insightUri = insightUtils.createSimpleColumnInsight("test_view_date_has_stack_column_insight",
            Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES),
            Arrays.asList(ATTR_YEAR_ACTIVITY), ATTR_REGION);

        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String yearActivity = getAttributeByTitle(ATTR_YEAR_ACTIVITY).getDefaultDisplayForm().getUri();
        final String regionUri = getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(yearActivity);
            put(regionUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromUri(insightUri), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        Pair<Integer, Integer> position  = getColumnPosition(embeddedAnalysisPage.getChartReport(), "East Coast", "2011");
        cleanUpLogger();
        embeddedAnalysisPage.getChartReport().clickOnElement(position);

        JSONObject content = getLatestPostMessageObj();
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
        initAnalysePage().addMetric(FACT_AMOUNT, FieldType.FACT);
        analysisPage.changeReportType(COLUMN_CHART).saveInsight("test_adhoc_fact_insight");
        analysisPage.waitForReportComputing();
        final String amountUri = getFactByTitle(FACT_AMOUNT).getUri();

        JSONArray uris = new JSONArray() {{
            put(amountUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl() + "?preventDefault=true", uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        embeddedAnalysisPage.getChartReport().clickOnElement(Pair.of(0, 0));

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
    public void eventingPivotTableReportSingleMetric() throws IOException {
        String insightUri = insightUtils.createSimplePivotTableInsight("single_metric_pivot_table_insight",
            Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES), Collections.emptyList(), "");
        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};
        final String file = createTemplateHtmlFile(getObjectIdFromUri(insightUri), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        embeddedAnalysisPage.getPivotTableReport().getCellElement(METRIC_NUMBER_OF_ACTIVITIES, 0).click();
        JSONObject content = getLatestPostMessageObj();
        verifyTableReport(content, METRIC_NUMBER_OF_ACTIVITIES, activityUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void eventingPivotTableReportSingleAttribute() throws IOException {
        String insightUri = insightUtils.createSimplePivotTableInsight("single_attribute_pivot_table_insight", Collections.emptyList(),
            Arrays.asList(ATTR_STAGE_NAME), "");
        final String stageUri = getAttributeByTitle(ATTR_STAGE_NAME).getDefaultDisplayForm().getUri();
        final JSONArray uris = new JSONArray() {{
            put(stageUri);
        }};
        final String file = createTemplateHtmlFile(getObjectIdFromUri(insightUri), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        embeddedAnalysisPage.getPivotTableReport().getCellElement(ATTR_STAGE_NAME, 0).click();
        JSONObject content = getLatestPostMessageObj();
        verifyTableReport(content, ATTR_STAGE_NAME, stageUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void eventingPivotTableReportSingleMetricViewByDate() throws IOException {
        String insightUri = insightUtils.createSimplePivotTableInsight("test_view_date_pivot_table_insight",
            Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES),
            Arrays.asList(ATTR_YEAR_ACTIVITY),"");

        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String yearActivity = getAttributeByTitle(ATTR_YEAR_ACTIVITY).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(yearActivity);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromUri(insightUri), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        embeddedAnalysisPage.getPivotTableReport().getCellElement(METRIC_NUMBER_OF_ACTIVITIES, 0).click();
        JSONObject content = getLatestPostMessageObj();
        verifyTableReport(content, METRIC_NUMBER_OF_ACTIVITIES, activityUri);

        cleanUpLogger();
        embeddedAnalysisPage.getPivotTableReport().getCellElement(ATTR_YEAR_ACTIVITY, 0).click();
        content = getLatestPostMessageObj();
        verifyTableReport(content, ATTR_YEAR_ACTIVITY, yearActivity);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void eventingPivotTableReportSingleMetricSingleAttribute() throws IOException {
        String insightUri = insightUtils.createSimplePivotTableInsight("single_attribute_single_metric_pivot_table_insight",
            Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES),
            Arrays.asList(ATTR_REGION), "");
        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String regionUri = getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(regionUri);
        }};
        final String file = createTemplateHtmlFile(getObjectIdFromUri(insightUri), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        embeddedAnalysisPage.getPivotTableReport().getCellElement(ATTR_REGION, 0).click();
        JSONObject content = getLatestPostMessageObj();
        verifyTableReport(content, ATTR_REGION, regionUri);

        cleanUpLogger();
        embeddedAnalysisPage.getPivotTableReport().getCellElement(METRIC_NUMBER_OF_ACTIVITIES, 0).click();
        content = getLatestPostMessageObj();
        verifyTableReport(content, METRIC_NUMBER_OF_ACTIVITIES, activityUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void eventingPivotTableReportMultipleMetricsSingleAttribute() throws IOException {
        String insightUri = insightUtils.createSimplePivotTableInsight("single_attribute__multiple_metric_pivot_table_insight",
            Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_NUMBER_OF_OPPORTUNITIES),
            Arrays.asList(ATTR_REGION), "");
        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String opportunityUri = getMetricByTitle(METRIC_NUMBER_OF_OPPORTUNITIES).getUri();
        final String regionUri = getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(opportunityUri);
            put(regionUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromUri(insightUri), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        embeddedAnalysisPage.getPivotTableReport().getCellElement(ATTR_REGION, 0).click();
        JSONObject content = getLatestPostMessageObj();
        verifyTableReport(content, ATTR_REGION, regionUri);

        cleanUpLogger();
        embeddedAnalysisPage.getPivotTableReport().getCellElement(METRIC_NUMBER_OF_ACTIVITIES, 0).click();
        content = getLatestPostMessageObj();
        verifyTableReport(content, METRIC_NUMBER_OF_ACTIVITIES, activityUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void eventingPivotTableReportMultipleAttributes() throws IOException {
        String insightUri = insightUtils.createSimplePivotTableInsight("multiple_attributes_pivot_table_insight", Collections.emptyList(),
            Arrays.asList(ATTR_STAGE_NAME, ATTR_REGION), "");
        final String stageUri = getAttributeByTitle(ATTR_STAGE_NAME).getDefaultDisplayForm().getUri();
        final String regionUri = getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(stageUri);
            put(regionUri);
        }};
        final String file = createTemplateHtmlFile(getObjectIdFromUri(insightUri), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        embeddedAnalysisPage.getPivotTableReport().getCellElement(ATTR_STAGE_NAME, 0).click();
        JSONObject content = getLatestPostMessageObj();
        verifyTableReport(content, ATTR_STAGE_NAME, stageUri);

        cleanUpLogger();
        embeddedAnalysisPage.getPivotTableReport().getCellElement(ATTR_REGION, 0).click();
        content = getLatestPostMessageObj();
        verifyTableReport(content, ATTR_REGION, regionUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void eventingPivotTableReportMultipleMetrics() throws IOException {
        String insightUri = insightUtils.createSimplePivotTableInsight("multiple_metrics_pivot_table_insight",
            Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_NUMBER_OF_OPPORTUNITIES),
                Collections.emptyList(),"");
        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String opportunityUri = getMetricByTitle(METRIC_NUMBER_OF_OPPORTUNITIES).getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(opportunityUri);
        }};
        final String file = createTemplateHtmlFile(getObjectIdFromUri(insightUri), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        embeddedAnalysisPage.getPivotTableReport().getCellElement(METRIC_NUMBER_OF_ACTIVITIES, 0).click();
        JSONObject content = getLatestPostMessageObj();
        verifyTableReport(content, METRIC_NUMBER_OF_ACTIVITIES, activityUri);

        cleanUpLogger();
        embeddedAnalysisPage.getPivotTableReport().getCellElement(METRIC_NUMBER_OF_OPPORTUNITIES, 0).click();
        content = getLatestPostMessageObj();
        verifyTableReport(content, METRIC_NUMBER_OF_OPPORTUNITIES, opportunityUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void eventingComboReport() throws IOException {
        String insightUri = insightUtils.createSimpleComboInsight("combo_insight",
                singletonList(METRIC_AMOUNT), singletonList(METRIC_NUMBER_OF_ACTIVITIES), ATTR_DEPARTMENT);

        final String amountUri = getMetricByTitle(METRIC_AMOUNT).getUri();
        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final JSONArray uris = new JSONArray() {{
            put(amountUri);
            put(activityUri);
        }};
        final String file = createTemplateHtmlFile(getObjectIdFromUri(insightUri), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        embeddedAnalysisPage.getChartReport().clickOnElement(Pair.of(0, 0));
        JSONObject content = getLatestPostMessageObj();
        verifyComboChartDrillContext(content, "column");

        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        JSONArray intersection = drillContext.getJSONArray("intersection");
        assertEquals(intersection.length(), 2);
        verifyColumnIntersection(intersection.getJSONObject(0), METRIC_AMOUNT, amountUri);

        cleanUpLogger();
        embeddedAnalysisPage.getChartReport().clickOnElement(Pair.of(1, 0));
        content = getLatestPostMessageObj();
        verifyComboChartDrillContext(content, "point");

        drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        intersection = drillContext.getJSONArray("intersection");
        assertEquals(intersection.length(), 2);
        verifyColumnIntersection(intersection.getJSONObject(0), METRIC_NUMBER_OF_ACTIVITIES, activityUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void eventingPivotTableWithGroupingAndSubtotals() throws IOException {
        String insightUri = insightUtils.createSimplePivotTableInsight("grouping_and_subtotals_on_pivot",
            Collections.singletonList(METRIC_AMOUNT), Arrays.asList(ATTR_DEPARTMENT, ATTR_REGION), ATTR_FORECAST_CATEGORY);

        final String departmentUri = getAttributeByTitle(ATTR_DEPARTMENT).getDefaultDisplayForm().getUri();
        final String regionUri = getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(departmentUri);
            put(regionUri);
        }};
        final String file = createTemplateHtmlFile(getObjectIdFromUri(insightUri), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        PivotTableReport pivotTableReport = embeddedAnalysisPage.getPivotTableReport();
        pivotTableReport.addTotal(AggregationItem.SUM, METRIC_AMOUNT, 0);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        pivotTableReport.getCellElement(0, 0).click();
        JSONObject content = getLatestPostMessageObj();
        verifyTableReport(content, ATTR_DEPARTMENT, departmentUri);

        cleanUpLogger();
        pivotTableReport.getCellElement(1, 0).click();
        content = getLatestPostMessageObj();
        verifyTableReport(content, ATTR_REGION, regionUri);

        assertFalse(pivotTableReport.isTotalCellUnderlined(2, 0),
            "The total table should not be underlined");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void eventingTreeMapReport() throws IOException {
        String insightUri = insightUtils.createSimpleTreeMapInsight("tree_map_insight",
                singletonList(METRIC_NUMBER_OF_ACTIVITIES), singletonList(ATTR_ACTIVITY_TYPE), ATTR_REGION);

        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String activityTypeUri = getAttributeByTitle(ATTR_ACTIVITY_TYPE).getDefaultDisplayForm().getUri();
        final String regionUri = getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri();

        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(activityTypeUri);
            put(regionUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromUri(insightUri), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        embeddedAnalysisPage.getChartReport().clickOnElement(Pair.of(0, 0));
        JSONObject content = getLatestPostMessageObj();
        verifyTreeMapChartDrillContext(content);

        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        JSONArray intersection = drillContext.getJSONArray("intersection");
        assertEquals(intersection.length(), 3);
        verifyColumnIntersection(intersection.getJSONObject(0), METRIC_NUMBER_OF_ACTIVITIES, activityUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void eventingHeatMapReport() throws IOException {
        String insightUri = insightUtils.createSimpleHeatMapInsight("heat_map_insight",
                singletonList(METRIC_NUMBER_OF_ACTIVITIES), singletonList(ATTR_ACTIVITY_TYPE), ATTR_REGION);

        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String activityTypeUri = getAttributeByTitle(ATTR_ACTIVITY_TYPE).getDefaultDisplayForm().getUri();
        final String regionUri = getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri();

        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(activityTypeUri);
            put(regionUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromUri(insightUri), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();

        ChartReport chartReport = embeddedAnalysisPage.getChartReport();
        chartReport.clickOnElement(Pair.of(0, 0));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList("Activity Type", "Web Meeting"), asList("Region", "West Coast"), asList("# of Activities", "21,045")));
        assertEquals(chartReport.getTrackersCount(), 8);
        Screenshots.takeScreenshot(browser, "eventing HeatMap", getClass());
        
        JSONObject content = getLatestPostMessageObj();
        log.info("content" + content);
        verifyHeatMapChartDrillContext(content);

        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        JSONArray intersection = drillContext.getJSONArray("intersection");
        assertEquals(intersection.length(), 3);
        verifyColumnIntersection(intersection.getJSONObject(0), METRIC_NUMBER_OF_ACTIVITIES, activityUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void eventingHeatMapReportWithDateAttributeOnRows() throws IOException {
        String insightUri = insightUtils.createSimpleHeatMapInsight("heat_map_insight_has_date_on_rows",
                singletonList(METRIC_NUMBER_OF_ACTIVITIES), singletonList(ATTR_YEAR_ACTIVITY), ATTR_ACTIVITY_TYPE);

        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String activityTypeUri = getAttributeByTitle(ATTR_ACTIVITY_TYPE).getDefaultDisplayForm().getUri();
        final String yearActivityUri = getAttributeByTitle(ATTR_YEAR_ACTIVITY).getDefaultDisplayForm().getUri();

        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(yearActivityUri);
            put(activityTypeUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromUri(insightUri), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();

        ChartReport chartReport = embeddedAnalysisPage.getChartReport();
        chartReport.clickOnElement(Pair.of(0, 0));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList("Year (Activity)", "2016"), asList("Activity Type", "In Person Meeting"), asList("# of Activities", "1")));
        Screenshots.takeScreenshot(browser, "eventing HeatMap", getClass());

        JSONObject content = getLatestPostMessageObj();
        log.info("content" + content);
        verifyHeatMapChartDrillContext(content);

        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        JSONArray intersection = drillContext.getJSONArray("intersection");
        assertEquals(intersection.length(), 3);
        verifyColumnIntersection(intersection.getJSONObject(0), METRIC_NUMBER_OF_ACTIVITIES, activityUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void eventingHeatMapReportHasDateAttributeOnColumns() throws IOException {
        String insightUri = insightUtils.createSimpleHeatMapInsight("heat_map_insight_has_date_on_columns",
                singletonList(METRIC_NUMBER_OF_ACTIVITIES), singletonList(ATTR_ACTIVITY_TYPE), ATTR_YEAR_ACTIVITY);

        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String activityTypeUri = getAttributeByTitle(ATTR_ACTIVITY_TYPE).getDefaultDisplayForm().getUri();
        final String yearActivityUri = getAttributeByTitle(ATTR_YEAR_ACTIVITY).getDefaultDisplayForm().getUri();

        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(activityTypeUri);
            put(yearActivityUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromUri(insightUri), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();

        ChartReport chartReport = embeddedAnalysisPage.getChartReport();
        chartReport.clickOnElement(Pair.of(0, 0));
        assertEquals(chartReport.getTooltipTextOnTrackerByIndex(0, 0),
                asList(asList("Activity Type", "Web Meeting"), asList("Year (Activity)", "2009"), asList("# of Activities", "415")));
        Screenshots.takeScreenshot(browser, "eventing HeatMap", getClass());

        JSONObject content = getLatestPostMessageObj();
        log.info("content" + content);
        verifyHeatMapChartDrillContext(content);

        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        JSONArray intersection = drillContext.getJSONArray("intersection");
        assertEquals(intersection.length(), 3);
        verifyColumnIntersection(intersection.getJSONObject(0), METRIC_NUMBER_OF_ACTIVITIES, activityUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void eventingBulletChartReport() throws IOException {
        String insightUri = indigoRestRequest.createInsight(
            new InsightMDConfiguration("primary_target_comparative_metric_bullet_insight", ReportType.BULLET_CHART)
                .setMeasureBucket(asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT_BOP)),
                    MeasureBucket.createMeasureBucket(getMetricByTitle(METRIC_AMOUNT), MeasureBucket.Type.SECONDARY_MEASURES),
                    MeasureBucket.createMeasureBucket(getMetricByTitle(METRIC_BEST_CASE), MeasureBucket.Type.TERTIARY_MEASURES)))
                .setCategoryBucket(asList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                        CategoryBucket.Type.VIEW),
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                        CategoryBucket.Type.VIEW))));

        final String metricAmountUri = getMetricByTitle(METRIC_AMOUNT_BOP).getUri();
        final JSONArray uris = new JSONArray() {{
            put(metricAmountUri);
        }};
        final String file = createTemplateHtmlFile(getObjectIdFromUri(insightUri), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        embeddedAnalysisPage.getChartReport().clickOnElement(Pair.of(0, 0));
        JSONObject content = getLatestPostMessageObj();

        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        assertEquals(drillContext.getString("type"), "bullet");
        assertEquals(drillContext.getString("element"), "primary");
        assertFalse(drillContext.isNull("x"), "drill event of bullet chart should show X");
        assertFalse(drillContext.isNull("y"), "drill event of bullet chart should show Y");

        drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        JSONArray intersection = drillContext.getJSONArray("intersection");
        assertEquals(intersection.length(), 3);
        verifyColumnIntersection(intersection.getJSONObject(0), METRIC_AMOUNT_BOP, metricAmountUri);
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

    private void verifyComboChartDrillContext(JSONObject content, String typeElement) {
        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        assertEquals(drillContext.getString("type"), "combo");

        if (typeElement.equals("column")) {
            assertEquals(drillContext.getString("element"), "bar");
        } else {
            assertEquals(drillContext.getString("element"), "point");
        }
        assertFalse(drillContext.isNull("x"), "drill event of column chart should show X");
        assertFalse(drillContext.isNull("y"), "drill event of column chart should show Y");
    }

    private void verifyTreeMapChartDrillContext(JSONObject content) {
        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        assertEquals(drillContext.getString("type"), "treemap");
        assertEquals(drillContext.getString("element"), "slice");
    }

    private void verifyHeatMapChartDrillContext(JSONObject content) {
        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        assertEquals(drillContext.getString("type"), "heatmap");
        assertEquals(drillContext.getString("element"), "cell");
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
}
