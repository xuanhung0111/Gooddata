package com.gooddata.qa.graphene.indigo.analyze.eventing;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.EmbeddedAnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractEventingTest;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_REGION;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_OPPORTUNITIES;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class EventingSpecialCaseTest extends AbstractEventingTest {

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingTableReportChangeExistedMetric() throws IOException {
        final String insightUri = createInsight("special_test_insight_16_1_" + generateHashString(), ReportType.TABLE,
                Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_NUMBER_OF_OPPORTUNITIES),
                Arrays.asList(ATTR_REGION, ATTR_DEPARTMENT));

        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String opportunityUri = getMetricByTitle(METRIC_NUMBER_OF_OPPORTUNITIES).getUri();
        final String regionUri = getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri();
        final JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(regionUri);
        }};

        String file = createTemplateHtmlFile(getObjectIdFromUri(insightUri), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        setDrillableItems(regionUri, opportunityUri);

        cleanUpLogger();
        embeddedAnalysisPage.getTableReport().getCellElement(METRIC_NUMBER_OF_ACTIVITIES, 0).click();
        String contentStr = getLoggerContent();
        assertTrue(contentStr.trim().isEmpty(), String.format("%s should not drillable", METRIC_NUMBER_OF_ACTIVITIES));

        cleanUpLogger();
        embeddedAnalysisPage.getTableReport().getCellElement(METRIC_NUMBER_OF_OPPORTUNITIES, 0).click();
        JSONObject content = getLatestPostMessageObj();

        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        assertEquals(drillContext.getInt("columnIndex"), 3);
        assertEquals(drillContext.getInt("rowIndex"), 0);

        JSONArray intersection = drillContext.getJSONArray("intersection");
        String uri = intersection.getJSONObject(0).getJSONObject("header").getString("uri");
        String title = intersection.getJSONObject(0).getString("title");
        assertEquals(drillContext.getString("type"), "table");
        assertEquals(drillContext.getString("element"), "cell");
        assertEquals(uri, opportunityUri);
        assertEquals(title, METRIC_NUMBER_OF_OPPORTUNITIES);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingColumnReportChangeExistedMetric() throws IOException {
        final String insightUri = createInsight("special_test_insight_16_2_" + generateHashString(), ReportType.TABLE,
                Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_NUMBER_OF_OPPORTUNITIES),
                Arrays.asList(ATTR_REGION, ATTR_DEPARTMENT));

        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String opportunityUri = getMetricByTitle(METRIC_NUMBER_OF_OPPORTUNITIES).getUri();
        final String regionUri = getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri();
        final JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(regionUri);
        }};

        String file = createTemplateHtmlFile(getObjectIdFromUri(insightUri), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();
        embeddedAnalysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();

        setDrillableItems(regionUri, opportunityUri);

        cleanUpLogger();
        ChartReport chartReport = embeddedAnalysisPage.getChartReport();
        Pair<Integer, Integer> position = getColumnPosition(chartReport, METRIC_NUMBER_OF_OPPORTUNITIES, "East Coast");
        chartReport.clickOnElement(position);

        JSONObject drillContext = getLatestPostMessageObj().getJSONObject("data").getJSONObject("drillContext");
        assertEquals(drillContext.getString("type"), "column");
        assertEquals(drillContext.getString("element"), "bar");
        assertFalse(drillContext.isNull("x"), "drill event of column chart should show X");
        assertFalse(drillContext.isNull("y"), "drill event of column chart should show Y");

        JSONArray intersection = drillContext.getJSONArray("intersection");
        assertEquals(intersection.length(), 2);

        String uri = intersection.getJSONObject(0).getJSONObject("header").getString("uri");
        String title = intersection.getJSONObject(0).getString("title");
        assertEquals(uri, opportunityUri);
        assertEquals(title, METRIC_NUMBER_OF_OPPORTUNITIES);

        uri = intersection.getJSONObject(1).getJSONObject("header").getString("uri");
        title = intersection.getJSONObject(1).getString("title");
        assertEquals(uri, regionUri);
        assertEquals(title, "East Coast");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingTableReportChangeExistedAttribute() throws IOException {
        final String insightUri = createInsight("special_test_insight_16_3_" + generateHashString(), ReportType.TABLE,
                Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_NUMBER_OF_OPPORTUNITIES),
                Arrays.asList(ATTR_REGION, ATTR_DEPARTMENT));

        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String opportunityUri = getMetricByTitle(METRIC_NUMBER_OF_OPPORTUNITIES).getUri();
        final String regionUri = getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri();
        final String departmentUri = getAttributeByTitle(ATTR_DEPARTMENT).getDefaultDisplayForm().getUri();
        final JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(opportunityUri);
            put(regionUri);
        }};

        String file = createTemplateHtmlFile(getObjectIdFromUri(insightUri), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        setDrillableItems(activityUri, departmentUri);

        cleanUpLogger();
        embeddedAnalysisPage.getTableReport().getCellElement(ATTR_REGION, 0).click();
        String contentStr = getLoggerContent();
        assertTrue(contentStr.trim().isEmpty(), String.format("%s should not drillable", ATTR_REGION));
        embeddedAnalysisPage.getTableReport().getCellElement(ATTR_DEPARTMENT, 0).click();
        JSONObject content = getLatestPostMessageObj();

        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        assertEquals(drillContext.getInt("columnIndex"), 1);
        assertEquals(drillContext.getInt("rowIndex"), 0);

        JSONArray intersection = drillContext.getJSONArray("intersection");
        String uri = intersection.getJSONObject(0).getJSONObject("header").getString("uri");
        String title = intersection.getJSONObject(0).getString("title");
        assertEquals(drillContext.getString("type"), "table");
        assertEquals(drillContext.getString("element"), "cell");
        assertEquals(uri, departmentUri);
        assertEquals(title, ATTR_DEPARTMENT);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingColumnReportChangeExistedAttribute() throws IOException {
        final String insightUri = createInsight("special_test_insight_16_4_" + generateHashString(), ReportType.TABLE,
                Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_NUMBER_OF_OPPORTUNITIES),
                Arrays.asList(ATTR_REGION, ATTR_DEPARTMENT));

        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String opportunityUri = getMetricByTitle(METRIC_NUMBER_OF_OPPORTUNITIES).getUri();
        final String regionUri = getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri();
        final String departmentUri = getAttributeByTitle(ATTR_DEPARTMENT).getDefaultDisplayForm().getUri();
        final JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(opportunityUri);
            put(regionUri);
        }};

        String file = createTemplateHtmlFile(getObjectIdFromUri(insightUri), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();
        embeddedAnalysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();

        setDrillableItems(activityUri, departmentUri);

        cleanUpLogger();

        ChartReport chartReport = embeddedAnalysisPage.getChartReport();
        Pair<Integer, Integer> position = getColumnPosition(chartReport, METRIC_NUMBER_OF_ACTIVITIES, "East Coast");
        chartReport.clickOnElement(position);

        JSONObject content = getLatestPostMessageObj();

        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        assertEquals(drillContext.getString("type"), "column");
        assertEquals(drillContext.getString("element"), "bar");
        assertTrue(drillContext.has("x"), "drill event of column chart should show X");
        assertTrue(drillContext.has("y"), "drill event of column chart should show Y");

        JSONArray intersection = drillContext.getJSONArray("intersection");
        assertEquals(intersection.length(), 2);

        String uri = intersection.getJSONObject(0).getJSONObject("header").getString("uri");
        String title = intersection.getJSONObject(0).getString("title");
        assertEquals(uri, activityUri);
        assertEquals(title, METRIC_NUMBER_OF_ACTIVITIES);

        uri = intersection.getJSONObject(1).getJSONObject("header").getString("uri");
        title = intersection.getJSONObject(1).getString("title");
        assertEquals(uri, regionUri);
        assertEquals(title, "East Coast");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingColumnReportRearrangeMetric() throws IOException {
        final String insightUri = createInsight("special_test_insight_16_5_" + generateHashString(), ReportType.TABLE,
                Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_NUMBER_OF_OPPORTUNITIES),
                Arrays.asList(ATTR_REGION, ATTR_DEPARTMENT));

        final String opportunityUri = getMetricByTitle(METRIC_NUMBER_OF_OPPORTUNITIES).getUri();
        final String regionUri = getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri();
        final JSONArray uris = new JSONArray() {{
            put(opportunityUri);
            put(regionUri);
        }};

        String file = createTemplateHtmlFile(getObjectIdFromUri(insightUri), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.changeReportType(ReportType.COLUMN_CHART);
        embeddedAnalysisPage.waitForReportComputing();

        analysisPage.reorderMetric(METRIC_NUMBER_OF_OPPORTUNITIES, METRIC_NUMBER_OF_ACTIVITIES).waitForReportComputing();

        cleanUpLogger();
        ChartReport chartReport = embeddedAnalysisPage.getChartReport();
        Pair<Integer, Integer> position = getColumnPosition(chartReport, METRIC_NUMBER_OF_ACTIVITIES, "East Coast");
        chartReport.clickOnElement(position);
        JSONObject content = getLatestPostMessageObj();

        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        assertTrue(drillContext.has("x"), "drill event of column chart should show X");
        assertTrue(drillContext.has("y"), "drill event of column chart should show Y");
        assertEquals(drillContext.getString("type"), "column");
        assertEquals(drillContext.getString("element"), "bar");

        JSONArray intersection = drillContext.getJSONArray("intersection");
        String uri = intersection.getJSONObject(0).getJSONObject("header").getString("uri");
        String title = intersection.getJSONObject(0).getString("title");
        assertEquals(intersection.length(), 2);
        assertEquals(uri, getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri());
        assertEquals(title, METRIC_NUMBER_OF_ACTIVITIES);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingTableReportRearrangeMetric() throws IOException {
        final String insightUri = createInsight("special_test_insight_16_6_" + generateHashString(), ReportType.TABLE,
                Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_NUMBER_OF_OPPORTUNITIES),
                Arrays.asList(ATTR_REGION, ATTR_DEPARTMENT));

        final String opportunityUri = getMetricByTitle(METRIC_NUMBER_OF_OPPORTUNITIES).getUri();
        final String regionUri = getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri();
        final JSONArray uris = new JSONArray() {{
            put(opportunityUri);
            put(regionUri);
        }};

        String file = createTemplateHtmlFile(getObjectIdFromUri(insightUri), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        analysisPage.reorderMetric(METRIC_NUMBER_OF_OPPORTUNITIES, METRIC_NUMBER_OF_ACTIVITIES).waitForReportComputing();

        cleanUpLogger();
        embeddedAnalysisPage.getTableReport().getCellElement(METRIC_NUMBER_OF_OPPORTUNITIES, 0).click();

        JSONObject drillContext = getLatestPostMessageObj().getJSONObject("data").getJSONObject("drillContext");
        assertEquals(drillContext.getInt("columnIndex"), 3);
        assertEquals(drillContext.getInt("rowIndex"), 0);

        JSONArray intersection = drillContext.getJSONArray("intersection");
        String uri = intersection.getJSONObject(0).getJSONObject("header").getString("uri");
        String title = intersection.getJSONObject(0).getString("title");
        assertEquals(drillContext.getString("type"), "table");
        assertEquals(drillContext.getString("element"), "cell");
        assertEquals(uri, opportunityUri);
        assertEquals(title, METRIC_NUMBER_OF_OPPORTUNITIES);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingTableReportRearrangeAttribute() throws IOException {
        final String insightUri = createInsight("special_test_insight_16_7_" + generateHashString(), ReportType.TABLE,
                Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_NUMBER_OF_OPPORTUNITIES),
                Arrays.asList(ATTR_REGION, ATTR_DEPARTMENT));

        final String opportunityUri = getMetricByTitle(METRIC_NUMBER_OF_OPPORTUNITIES).getUri();
        final String regionUri = getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri();
        final JSONArray uris = new JSONArray() {{
            put(opportunityUri);
            put(regionUri);
        }};

        String file = createTemplateHtmlFile(getObjectIdFromUri(insightUri), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        embeddedAnalysisPage.removeAttribute(ATTR_REGION);
        embeddedAnalysisPage.addAttribute(ATTR_REGION);

        cleanUpLogger();
        embeddedAnalysisPage.getTableReport().getCellElement(ATTR_REGION, 0).click();

        JSONObject drillContext = getLatestPostMessageObj().getJSONObject("data").getJSONObject("drillContext");
        assertEquals(drillContext.getInt("columnIndex"), 1);
        assertEquals(drillContext.getInt("rowIndex"), 0);

        JSONArray intersection = drillContext.getJSONArray("intersection");
        String uri = intersection.getJSONObject(0).getJSONObject("header").getString("uri");
        String title = intersection.getJSONObject(0).getString("title");
        assertEquals(drillContext.getString("type"), "table");
        assertEquals(drillContext.getString("element"), "cell");
        assertEquals(uri, regionUri);
        assertEquals(title, ATTR_REGION);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingTableReportUndoRedo() throws IOException {
        final String insightUri = createInsight("special_test_insight_16_8_" + generateHashString(), ReportType.TABLE,
                Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_NUMBER_OF_OPPORTUNITIES),
                Arrays.asList(ATTR_REGION, ATTR_DEPARTMENT));

        final String opportunityUri = getMetricByTitle(METRIC_NUMBER_OF_OPPORTUNITIES).getUri();
        final String regionUri = getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri();
        final JSONArray uris = new JSONArray() {{
            put(opportunityUri);
            put(regionUri);
        }};

        String file = createTemplateHtmlFile(getObjectIdFromUri(insightUri), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        embeddedAnalysisPage.removeAttribute(ATTR_REGION);
        embeddedAnalysisPage.waitForReportComputing();
        embeddedAnalysisPage.undo();

        cleanUpLogger();
        embeddedAnalysisPage.getTableReport().getCellElement(ATTR_REGION, 0).click();
        JSONObject content = getLatestPostMessageObj();

        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        assertEquals(drillContext.getInt("columnIndex"), 0);
        assertEquals(drillContext.getInt("rowIndex"), 0);

        JSONArray intersection = drillContext.getJSONArray("intersection");
        String uri = intersection.getJSONObject(0).getJSONObject("header").getString("uri");
        String title = intersection.getJSONObject(0).getString("title");
        assertEquals(drillContext.getString("type"), "table");
        assertEquals(drillContext.getString("element"), "cell");
        assertEquals(uri, regionUri);
        assertEquals(title, ATTR_REGION);

        embeddedAnalysisPage.removeAttribute(ATTR_REGION);
        embeddedAnalysisPage.addAttribute(ATTR_REGION);
        embeddedAnalysisPage.undo();
        embeddedAnalysisPage.waitForReportComputing();
        embeddedAnalysisPage.redo();
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        embeddedAnalysisPage.getTableReport().getCellElement(ATTR_REGION, 0).click();
        content = getLatestPostMessageObj();

        drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        assertEquals(drillContext.getInt("columnIndex"), 1);
        assertEquals(drillContext.getInt("rowIndex"), 0);

        intersection = drillContext.getJSONArray("intersection");
        uri = intersection.getJSONObject(0).getJSONObject("header").getString("uri");
        title = intersection.getJSONObject(0).getString("title");
        assertEquals(drillContext.getString("type"), "table");
        assertEquals(drillContext.getString("element"), "cell");
        assertEquals(uri, regionUri);
        assertEquals(title, ATTR_REGION);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingTableReportDoSorting() throws IOException {
        final String insightUri = createInsight("special_test_insight_16_9_" + generateHashString(), ReportType.TABLE,
                Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_NUMBER_OF_OPPORTUNITIES),
                Arrays.asList(ATTR_REGION, ATTR_DEPARTMENT));

        final String opportunityUri = getMetricByTitle(METRIC_NUMBER_OF_OPPORTUNITIES).getUri();
        final String regionUri = getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri();
        final JSONArray uris = new JSONArray() {{
            put(opportunityUri);
            put(regionUri);
        }};

        String file = createTemplateHtmlFile(getObjectIdFromUri(insightUri), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        embeddedAnalysisPage.getTableReport().sortBaseOnHeader(ATTR_REGION);
        cleanUpLogger();
        embeddedAnalysisPage.getTableReport().getCellElement(ATTR_REGION, 0).click();
        JSONObject content = getLatestPostMessageObj();

        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        assertEquals(drillContext.getInt("columnIndex"), 0);
        assertEquals(drillContext.getInt("rowIndex"), 0);

        JSONArray intersection = drillContext.getJSONArray("intersection");
        String uri = intersection.getJSONObject(0).getJSONObject("header").getString("uri");
        String title = intersection.getJSONObject(0).getString("title");
        assertEquals(drillContext.getString("type"), "table");
        assertEquals(drillContext.getString("element"), "cell");
        assertEquals(uri, regionUri);
        assertEquals(title, ATTR_REGION);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingTableReportChangDateAs() throws IOException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE).waitForReportComputing();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDate();
        analysisPage.getAttributesBucket().getDateDatasetSelect().selectByName("Created").ensureDropdownClosed();
        analysisPage.saveInsight("special_test_insight_18_1_" + generateHashString());

        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl(), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        embeddedAnalysisPage.getTableReport().getCellElement(METRIC_NUMBER_OF_ACTIVITIES, 0).click();

        JSONObject drillContext = getLatestPostMessageObj().getJSONObject("data").getJSONObject("drillContext");
        assertEquals(drillContext.getInt("columnIndex"), 1);
        assertEquals(drillContext.getInt("rowIndex"), 0);

        JSONArray intersection = drillContext.getJSONArray("intersection");
        String uri = intersection.getJSONObject(0).getJSONObject("header").getString("uri");
        String title = intersection.getJSONObject(0).getString("title");
        assertEquals(drillContext.getString("type"), "table");
        assertEquals(drillContext.getString("element"), "cell");
        assertEquals(uri, activityUri);
        assertEquals(title, METRIC_NUMBER_OF_ACTIVITIES);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingTableReportChangDateGroupBy() throws IOException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE).waitForReportComputing();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDate();
        analysisPage.getAttributesBucket().changeGranularity("Quarter");
        analysisPage.saveInsight("special_test_insight_18_2_" + generateHashString());

        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl(), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        embeddedAnalysisPage.getTableReport().getCellElement(METRIC_NUMBER_OF_ACTIVITIES, 0).click();

        JSONObject drillContext = getLatestPostMessageObj().getJSONObject("data").getJSONObject("drillContext");
        assertEquals(drillContext.getInt("columnIndex"), 1);
        assertEquals(drillContext.getInt("rowIndex"), 0);

        JSONArray intersection = drillContext.getJSONArray("intersection");
        String uri = intersection.getJSONObject(0).getJSONObject("header").getString("uri");
        String title = intersection.getJSONObject(0).getString("title");
        assertEquals(drillContext.getString("type"), "table");
        assertEquals(drillContext.getString("element"), "cell");
        assertEquals(uri, activityUri);
        assertEquals(title, METRIC_NUMBER_OF_ACTIVITIES);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingTableReportSameMetrics() throws IOException {
        final String insightUri = createInsight("special_test_insight_19_" + generateHashString(), ReportType.TABLE,
                Arrays.asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_NUMBER_OF_ACTIVITIES),
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
        embeddedAnalysisPage.getTableReport().getCellElement(1, 0).click();

        JSONObject drillContext = getLatestPostMessageObj().getJSONObject("data").getJSONObject("drillContext");
        assertEquals(drillContext.getInt("columnIndex"), 1);
        assertEquals(drillContext.getInt("rowIndex"), 0);

        JSONArray intersection = drillContext.getJSONArray("intersection");
        String uri = intersection.getJSONObject(0).getJSONObject("header").getString("uri");
        String title = intersection.getJSONObject(0).getString("title");
        assertEquals(drillContext.getString("type"), "table");
        assertEquals(drillContext.getString("element"), "cell");
        assertEquals(uri, activityUri);
        assertEquals(title, METRIC_NUMBER_OF_ACTIVITIES);

        cleanUpLogger();
        embeddedAnalysisPage.getTableReport().getCellElement(2, 0).click();

        drillContext = getLatestPostMessageObj().getJSONObject("data").getJSONObject("drillContext");
        assertEquals(drillContext.getInt("columnIndex"), 2);
        assertEquals(drillContext.getInt("rowIndex"), 0);

        intersection = drillContext.getJSONArray("intersection");
        uri = intersection.getJSONObject(0).getJSONObject("header").getString("uri");
        title = intersection.getJSONObject(0).getString("title");
        assertEquals(drillContext.getString("type"), "table");
        assertEquals(drillContext.getString("element"), "cell");
        assertEquals(uri, activityUri);
        assertEquals(title, METRIC_NUMBER_OF_ACTIVITIES);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingTableReportHasOneElement() throws IOException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE).waitForReportComputing();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_REGION);
        analysisPage.getFilterBuckets().configAttributeFilter(ATTR_REGION, "East Coast");
        analysisPage.saveInsight("special_test_insight_20_" + generateHashString());

        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl(), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        embeddedAnalysisPage.getTableReport().getCellElement(METRIC_NUMBER_OF_ACTIVITIES, 0).click();

        JSONObject drillContext = getLatestPostMessageObj().getJSONObject("data").getJSONObject("drillContext");
        assertEquals(drillContext.getInt("columnIndex"), 1);
        assertEquals(drillContext.getInt("rowIndex"), 0);

        JSONArray intersection = drillContext.getJSONArray("intersection");
        String uri = intersection.getJSONObject(0).getJSONObject("header").getString("uri");
        String title = intersection.getJSONObject(0).getString("title");
        assertEquals(drillContext.getString("type"), "table");
        assertEquals(drillContext.getString("element"), "cell");
        assertEquals(uri, activityUri);
        assertEquals(title, METRIC_NUMBER_OF_ACTIVITIES);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingReportHasZeroPercent() throws IOException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE).waitForReportComputing();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDate();
        analysisPage.getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES).expandConfiguration().showPercents();
        analysisPage.saveInsight("special_test_insight_21_" + generateHashString());

        String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        String yearActivityUri = getAttributeByTitle(ATTR_YEAR_ACTIVITY).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl(), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        embeddedAnalysisPage.getTableReport().getCellElement("% " + METRIC_NUMBER_OF_ACTIVITIES, 7).click();

        JSONObject content = getLatestPostMessageObj();
        JSONObject drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        assertEquals(drillContext.getInt("columnIndex"), 1);
        assertEquals(drillContext.getInt("rowIndex"), 7);
        assertEquals(drillContext.getString("type"), "table");
        assertEquals(drillContext.getString("element"), "cell");

        JSONArray intersection = drillContext.getJSONArray("intersection");
        String uri = intersection.getJSONObject(0).getJSONObject("header").getString("uri");
        String title = intersection.getJSONObject(0).getString("title");
        assertEquals(uri, activityUri);
        assertEquals(title, "% " + METRIC_NUMBER_OF_ACTIVITIES);

        embeddedAnalysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();

        cleanUpLogger();
        ChartReport chartReport = embeddedAnalysisPage.getChartReport();
        Pair<Integer, Integer> position = getColumnPosition(chartReport, "% " + METRIC_NUMBER_OF_ACTIVITIES, "2015");
        chartReport.clickOnDataLabel(position);
        content = getLatestPostMessageObj();

        drillContext = content.getJSONObject("data").getJSONObject("drillContext");
        assertTrue(drillContext.has("x"), "drill context should has x");
        assertTrue(drillContext.has("y"), "drill context should has y");
        assertEquals(drillContext.getString("type"), "column");
        assertEquals(drillContext.getString("element"), "bar");

        intersection = drillContext.getJSONArray("intersection");
        uri = intersection.getJSONObject(0).getJSONObject("header").getString("uri");
        title = intersection.getJSONObject(0).getString("title");
        assertEquals(uri, activityUri);
        assertEquals(title, "% " + METRIC_NUMBER_OF_ACTIVITIES);

        uri = intersection.getJSONObject(1).getJSONObject("header").getString("uri");
        title = intersection.getJSONObject(1).getString("title");
        assertEquals(uri, yearActivityUri);
        assertEquals(title, "2015");
    }
}
