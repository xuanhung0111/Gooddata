package com.gooddata.qa.graphene.indigo.analyze.eventing;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.EmbeddedAnalysisPage;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractEventingTest;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static com.gooddata.qa.graphene.enums.indigo.ReportType.COLUMN_CHART;
import static com.gooddata.qa.graphene.enums.indigo.ReportType.TABLE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_REGION;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_OPPORTUNITIES;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class EventingBasicTest extends AbstractEventingTest {

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createNumberOfActivitiesMetric();
        metrics.createAmountMetric();
        metrics.createNumberOfOpportunitiesMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void eventingTableReportSingleMetric() throws IOException {
        String insightUri = createInsight("single_metric_table_insight", TABLE,
                Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES), Collections.emptyList());
        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};
        final String file = createTemplateHtmlFile(getObjectIdFromUri(insightUri), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        embeddedAnalysisPage.getTableReport().getCellElement(METRIC_NUMBER_OF_ACTIVITIES, 0).click();
        JSONObject content = getLatestPostMessageObj();
        verifyTableReport(content, METRIC_NUMBER_OF_ACTIVITIES, activityUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void eventingColumnReportSingleMetric() throws IOException {
        String insightUri = createInsight("single_metric_column_insight", COLUMN_CHART,
                Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES), Collections.emptyList());
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
    public void eventingTableReportMultipleMetrics() throws IOException {
        String insightUri = createInsight("multiple_metrics_table_insight", TABLE,
                Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_NUMBER_OF_OPPORTUNITIES), Collections.emptyList());
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
        embeddedAnalysisPage.getTableReport().getCellElement(METRIC_NUMBER_OF_ACTIVITIES, 0).click();
        JSONObject content = getLatestPostMessageObj();
        verifyTableReport(content, METRIC_NUMBER_OF_ACTIVITIES, activityUri);

        cleanUpLogger();
        embeddedAnalysisPage.getTableReport().getCellElement(METRIC_NUMBER_OF_OPPORTUNITIES, 0).click();
        content = getLatestPostMessageObj();
        verifyTableReport(content, METRIC_NUMBER_OF_OPPORTUNITIES, opportunityUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void eventingColumnReportMultipleMetrics() throws IOException {
        String insightUri = createInsight("multiple_metrics_column_insight", COLUMN_CHART,
                Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_NUMBER_OF_OPPORTUNITIES), Collections.emptyList());
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
    public void eventingTableReportSingleAttribute() throws IOException {
        String insightUri = createInsight("single_attribute_table_insight", TABLE, Collections.emptyList(),
                Arrays.asList(ATTR_STAGE_NAME));
        final String stageUri = getAttributeByTitle(ATTR_STAGE_NAME).getDefaultDisplayForm().getUri();
        final JSONArray uris = new JSONArray() {{
            put(stageUri);
        }};
        final String file = createTemplateHtmlFile(getObjectIdFromUri(insightUri), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        embeddedAnalysisPage.getTableReport().getCellElement(ATTR_STAGE_NAME, 0).click();
        JSONObject content = getLatestPostMessageObj();
        verifyTableReport(content, ATTR_STAGE_NAME, stageUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void eventingTableReportMultipleAttributes() throws IOException {
        String insightUri = createInsight("multiple_attributes_table_insight", TABLE, Collections.emptyList(),
                Arrays.asList(ATTR_STAGE_NAME, ATTR_REGION));
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
        embeddedAnalysisPage.getTableReport().getCellElement(ATTR_STAGE_NAME, 0).click();
        JSONObject content = getLatestPostMessageObj();
        verifyTableReport(content, ATTR_STAGE_NAME, stageUri);

        cleanUpLogger();
        embeddedAnalysisPage.getTableReport().getCellElement(ATTR_REGION, 0).click();
        content = getLatestPostMessageObj();
        verifyTableReport(content, ATTR_REGION, regionUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void eventingTableReportMultipleMetricsSingleAttribute() throws IOException {
        String insightUri = createInsight("single_attribute__multiple_metric_table_insight", TABLE,
                Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_NUMBER_OF_OPPORTUNITIES),
                Arrays.asList(ATTR_REGION));
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
        embeddedAnalysisPage.getTableReport().getCellElement(ATTR_REGION, 0).click();
        JSONObject content = getLatestPostMessageObj();
        verifyTableReport(content, ATTR_REGION, regionUri);

        cleanUpLogger();
        embeddedAnalysisPage.getTableReport().getCellElement(METRIC_NUMBER_OF_ACTIVITIES, 0).click();
        content = getLatestPostMessageObj();
        verifyTableReport(content, METRIC_NUMBER_OF_ACTIVITIES, activityUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void eventingTableReportSingleMetricSingleAttribute() throws IOException {
        String insightUri = createInsight("single_attribute_single_metric_table_insight", TABLE,
                Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES),
                Arrays.asList(ATTR_REGION));
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
        embeddedAnalysisPage.getTableReport().getCellElement(ATTR_REGION, 0).click();
        JSONObject content = getLatestPostMessageObj();
        verifyTableReport(content, ATTR_REGION, regionUri);

        cleanUpLogger();
        embeddedAnalysisPage.getTableReport().getCellElement(METRIC_NUMBER_OF_ACTIVITIES, 0).click();
        content = getLatestPostMessageObj();
        verifyTableReport(content, METRIC_NUMBER_OF_ACTIVITIES, activityUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void eventingColumnReportSingleMetricSingleAttribute() throws IOException {
        String insightUri = createInsight("single_attribute_single_metric_column_insight", COLUMN_CHART,
                Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES),
                Arrays.asList(ATTR_REGION));
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
        String insightUri = createInsight("test_stackby_column_insight", COLUMN_CHART,
                Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES),
                Arrays.asList(ATTR_ACTIVITY_TYPE), ATTR_REGION);

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
        String insightUri = createInsight("test_same_attr_column_insight", COLUMN_CHART,
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
    public void eventingTableReportSingleMetricViewByDate() throws IOException {
        String insightUri = createInsight("test_view_date_table_insight", TABLE,
                Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES),
                Arrays.asList(ATTR_YEAR_ACTIVITY));

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
        embeddedAnalysisPage.getTableReport().getCellElement(METRIC_NUMBER_OF_ACTIVITIES, 0).click();
        JSONObject content = getLatestPostMessageObj();
        verifyTableReport(content, METRIC_NUMBER_OF_ACTIVITIES, activityUri);

        cleanUpLogger();
        embeddedAnalysisPage.getTableReport().getCellElement(ATTR_YEAR_ACTIVITY, 0).click();
        content = getLatestPostMessageObj();
        verifyTableReport(content, ATTR_YEAR_ACTIVITY, yearActivity);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void eventingColumnReportSingleMetricViewByDate() throws IOException {
        String insightUri = createInsight("test_view_date_column_insight", COLUMN_CHART,
                Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES),
                Arrays.asList(ATTR_YEAR_ACTIVITY));

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
        String insightUri = createInsight("test_view_date_multi_metrics_column_insight", COLUMN_CHART,
                Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_NUMBER_OF_OPPORTUNITIES),
                Arrays.asList(ATTR_YEAR_CREATED));

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
        String insightUri = createInsight("test_view_date_has_stack_column_insight", COLUMN_CHART,
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

        final String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl(), uris.toString());
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
