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
import java.text.ParseException;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_REGION;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_OPPORTUNITIES;
import static org.testng.Assert.assertEquals;

public class EventingBasicFiltersTest extends AbstractEventingTest {

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingTableReportHasOneMetricOneAttribute() throws IOException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE);
        analysisPage.addFilter(ATTR_REGION).getFilterBuckets().configAttributeFilter(ATTR_REGION, "East Coast");

        analysisPage.saveInsight("eventing_table_filter_1");

        String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        String regionUri = getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(regionUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl(), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        embeddedAnalysisPage.getTableReport().getCellElement(METRIC_NUMBER_OF_ACTIVITIES, 0).click();
        JSONObject content = getLatestPostMessageObj();

        JSONObject executionContext = content.getJSONObject("data").getJSONObject("executionContext");
        JSONArray filters = executionContext.getJSONArray("filters");
        assertEquals(filters.length(), 1);
        assertEquals(filters.getJSONObject(0).getJSONObject("positiveAttributeFilter")
                .getJSONObject("displayForm").getString("uri"), regionUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingColumnReportHasOneMetricOneAttribute() throws IOException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE);
        analysisPage.addFilter(ATTR_REGION).getFilterBuckets().configAttributeFilter(ATTR_REGION, "East Coast");

        analysisPage.saveInsight("eventing_column_filter_1");

        String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        String regionUri = getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(regionUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl(), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        ChartReport chartReport = embeddedAnalysisPage.getChartReport();
        Pair<Integer, Integer> position = getColumnPosition(chartReport, METRIC_NUMBER_OF_ACTIVITIES, "Email");
        chartReport.clickOnElement(position);

        JSONObject content = getLatestPostMessageObj();

        JSONObject executionContext = content.getJSONObject("data").getJSONObject("executionContext");
        JSONArray filters = executionContext.getJSONArray("filters");
        assertEquals(filters.length(), 1);
        assertEquals(filters.getJSONObject(0).getJSONObject("positiveAttributeFilter")
                .getJSONObject("displayForm").getString("uri"), regionUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingTableReportHasOneMetricOneDate() throws IOException, ParseException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE).addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDate()
                .getFilterBuckets().configDateFilter("1/1/2011", "12/31/2011");
        analysisPage.waitForReportComputing();

        analysisPage.saveInsight("eventing_table_filter_2");

        String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        String yearCreatedUri = getAttributeByTitle(ATTR_YEAR_CREATED).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(yearCreatedUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl(), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        embeddedAnalysisPage.getTableReport().getCellElement(METRIC_NUMBER_OF_ACTIVITIES, 0).click();
        JSONObject content = getLatestPostMessageObj();

        JSONObject executionContext = content.getJSONObject("data").getJSONObject("executionContext");
        JSONArray filters = executionContext.getJSONArray("filters");
        assertEquals(filters.length(), 1);
        assertEquals(filters.getJSONObject(0)
                .getJSONObject("absoluteDateFilter").getJSONObject("dataSet").getString("uri"),
                getDatasetByIdentifier("created.dataset.dt").getUri());

        cleanUpLogger();
        embeddedAnalysisPage.getTableReport().getCellElement(ATTR_YEAR_CREATED, 0).click();
        content = getLatestPostMessageObj();

        executionContext = content.getJSONObject("data").getJSONObject("executionContext");
        filters = executionContext.getJSONArray("filters");
        assertEquals(filters.length(), 1);
        assertEquals(filters.getJSONObject(0).getJSONObject("absoluteDateFilter").getJSONObject("dataSet").getString("uri"),
                getDatasetByIdentifier("created.dataset.dt").getUri());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingColumnReportHasOneMetricOneDate() throws IOException, ParseException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDate()
                .getFilterBuckets().configDateFilter("1/1/2011", "12/31/2011");
        analysisPage.waitForReportComputing();

        analysisPage.saveInsight("eventing_column_filter_2");

        String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        String yearActivityUri = getAttributeByTitle(ATTR_YEAR_ACTIVITY).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(yearActivityUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl(), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        ChartReport chartReport = embeddedAnalysisPage.getChartReport();
        Pair<Integer, Integer> position = getColumnPosition(chartReport, METRIC_NUMBER_OF_ACTIVITIES, "2011");
        chartReport.clickOnElement(position);
        JSONObject content = getLatestPostMessageObj();

        JSONObject executionContext = content.getJSONObject("data").getJSONObject("executionContext");
        JSONArray filters = executionContext.getJSONArray("filters");
        assertEquals(filters.length(), 1);
        assertEquals(filters.getJSONObject(0).getJSONObject("absoluteDateFilter").getJSONObject("dataSet").getString("uri"),
                getDatasetByIdentifier("created.dataset.dt").getUri());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingTableReportHasOneMetricOneAttributeMultipleFilters() throws IOException, ParseException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE);
        analysisPage.addFilter(ATTR_REGION).getFilterBuckets().configAttributeFilter(ATTR_REGION, "East Coast");
        analysisPage.addDateFilter();
        analysisPage.getFilterBuckets().configDateFilter("1/1/2011", "12/31/2011");

        analysisPage.saveInsight("eventing_table_filter_3");

        String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        String activityTypeUri = getAttributeByTitle(ATTR_ACTIVITY_TYPE).getDefaultDisplayForm().getUri();
        String regionUri = getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(activityTypeUri);
            put(regionUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl(), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        embeddedAnalysisPage.getTableReport().getCellElement(METRIC_NUMBER_OF_ACTIVITIES, 0).click();
        JSONObject content = getLatestPostMessageObj();

        JSONObject executionContext = content.getJSONObject("data").getJSONObject("executionContext");
        JSONArray filters = executionContext.getJSONArray("filters");
        assertEquals(filters.length(), 2);
        assertEquals(filters.getJSONObject(0).getJSONObject("positiveAttributeFilter")
                .getJSONObject("displayForm").getString("uri"), regionUri);
        assertEquals(filters.getJSONObject(1).getJSONObject("absoluteDateFilter")
                .getJSONObject("dataSet").getString("uri"), getDatasetByIdentifier("created.dataset.dt").getUri());

        cleanUpLogger();
        embeddedAnalysisPage.getTableReport().getCellElement(ATTR_ACTIVITY_TYPE, 0).click();
        content = getLatestPostMessageObj();

        executionContext = content.getJSONObject("data").getJSONObject("executionContext");
        filters = executionContext.getJSONArray("filters");
        assertEquals(filters.length(), 2);
        assertEquals(filters.getJSONObject(0).getJSONObject("positiveAttributeFilter")
                .getJSONObject("displayForm").getString("uri"), regionUri);
        assertEquals(filters.getJSONObject(1).getJSONObject("absoluteDateFilter")
                .getJSONObject("dataSet").getString("uri"), getDatasetByIdentifier("created.dataset.dt").getUri());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingColumnReportHasOneMetricOneAttributeMultipleFilters() throws IOException, ParseException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE);
        analysisPage.addFilter(ATTR_REGION).getFilterBuckets().configAttributeFilter(ATTR_REGION, "East Coast");
        analysisPage.addDateFilter();
        analysisPage.getFilterBuckets().configDateFilter("1/1/2011", "12/31/2011");

        analysisPage.saveInsight("eventing_column_filter_3");

        String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        String activityTypeUri = getAttributeByTitle(ATTR_ACTIVITY_TYPE).getDefaultDisplayForm().getUri();
        String regionUri = getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(activityTypeUri);
            put(regionUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl(), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        ChartReport chartReport = embeddedAnalysisPage.getChartReport();
        Pair<Integer, Integer> position = getColumnPosition(chartReport, METRIC_NUMBER_OF_ACTIVITIES, "Email");
        chartReport.clickOnElement(position);
        JSONObject content = getLatestPostMessageObj();

        JSONObject executionContext = content.getJSONObject("data").getJSONObject("executionContext");
        JSONArray filters = executionContext.getJSONArray("filters");
        assertEquals(filters.length(), 2);
        assertEquals(filters.getJSONObject(0).getJSONObject("positiveAttributeFilter")
                .getJSONObject("displayForm").getString("uri"), regionUri);
        assertEquals(filters.getJSONObject(1).getJSONObject("absoluteDateFilter")
                .getJSONObject("dataSet").getString("uri"), getDatasetByIdentifier("created.dataset.dt").getUri());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingTableReportHasMultipleMetricsSingleAttribute() throws IOException, ParseException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addMetric(METRIC_NUMBER_OF_OPPORTUNITIES)
                .addAttribute(ATTR_REGION);
        analysisPage.getFilterBuckets().configAttributeFilter(ATTR_REGION, "East Coast");
        analysisPage.waitForReportComputing();
        analysisPage.saveInsight("eventing_table_filter_4");

        String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        String opportunityUri = getMetricByTitle(METRIC_NUMBER_OF_OPPORTUNITIES).getUri();
        String regionUri = getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(opportunityUri);
            put(regionUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl(), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        embeddedAnalysisPage.getTableReport().getCellElement(METRIC_NUMBER_OF_ACTIVITIES, 0).click();
        JSONObject content = getLatestPostMessageObj();

        JSONObject executionContext = content.getJSONObject("data").getJSONObject("executionContext");
        JSONArray filters = executionContext.getJSONArray("filters");
        assertEquals(filters.length(), 1);
        assertEquals(filters.getJSONObject(0).getJSONObject("positiveAttributeFilter")
                .getJSONObject("displayForm").getString("uri"), regionUri);

        cleanUpLogger();
        embeddedAnalysisPage.getTableReport().getCellElement(ATTR_REGION, 0).click();
        content = getLatestPostMessageObj();

        executionContext = content.getJSONObject("data").getJSONObject("executionContext");
        filters = executionContext.getJSONArray("filters");
        assertEquals(filters.length(), 1);
        assertEquals(filters.getJSONObject(0).getJSONObject("positiveAttributeFilter")
                .getJSONObject("displayForm").getString("uri"), regionUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingColumnReportHasMultipleMetricsSingleAttribute() throws IOException, ParseException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addMetric(METRIC_NUMBER_OF_OPPORTUNITIES)
                .addAttribute(ATTR_REGION);
        analysisPage.getFilterBuckets().configAttributeFilter(ATTR_REGION, "East Coast");
        analysisPage.waitForReportComputing();
        analysisPage.saveInsight("eventing_column_filter_4");

        String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        String opportunityUri = getMetricByTitle(METRIC_NUMBER_OF_OPPORTUNITIES).getUri();
        String regionUri = getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(opportunityUri);
            put(regionUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl(), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        ChartReport chartReport = embeddedAnalysisPage.getChartReport();
        Pair<Integer, Integer> position = getColumnPosition(chartReport, METRIC_NUMBER_OF_ACTIVITIES, "East Coast");
        chartReport.clickOnElement(position);
        JSONObject content = getLatestPostMessageObj();

        JSONObject executionContext = content.getJSONObject("data").getJSONObject("executionContext");
        JSONArray filters = executionContext.getJSONArray("filters");
        assertEquals(filters.length(), 1);
        assertEquals(filters.getJSONObject(0).getJSONObject("positiveAttributeFilter")
                .getJSONObject("displayForm").getString("uri"), regionUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingTableReportHasMultipleMetricsDateAttribute() throws IOException, ParseException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addMetric(METRIC_NUMBER_OF_OPPORTUNITIES)
                .addDate();
        analysisPage.getFilterBuckets().configDateFilter("1/1/2011", "12/31/2011");
        analysisPage.waitForReportComputing();
        analysisPage.saveInsight("eventing_table_filter_5");

        String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        String opportunityUri = getMetricByTitle(METRIC_NUMBER_OF_OPPORTUNITIES).getUri();
        String dateCreatedDs = getDatasetByIdentifier("created.dataset.dt").getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(opportunityUri);
            put(dateCreatedDs);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl(), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        embeddedAnalysisPage.getTableReport().getCellElement(METRIC_NUMBER_OF_ACTIVITIES, 0).click();
        JSONObject content = getLatestPostMessageObj();

        JSONObject executionContext = content.getJSONObject("data").getJSONObject("executionContext");
        JSONArray filters = executionContext.getJSONArray("filters");
        assertEquals(filters.length(), 1);
        assertEquals(filters.getJSONObject(0).getJSONObject("absoluteDateFilter")
                .getJSONObject("dataSet").getString("uri"), dateCreatedDs);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingColumnReportHasMultipleMetricsDateAttribute() throws IOException, ParseException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addMetric(METRIC_NUMBER_OF_OPPORTUNITIES)
                .addDate();
        analysisPage.getFilterBuckets().configDateFilter("1/1/2011", "12/31/2011");
        analysisPage.waitForReportComputing();
        analysisPage.saveInsight("eventing_column_filter_5");

        String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        String opportunityUri = getMetricByTitle(METRIC_NUMBER_OF_OPPORTUNITIES).getUri();
        String dateCreatedDs = getDatasetByIdentifier("created.dataset.dt").getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(opportunityUri);
            put(dateCreatedDs);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl(), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        ChartReport chartReport = embeddedAnalysisPage.getChartReport();
        Pair<Integer, Integer> position = getColumnPosition(chartReport, METRIC_NUMBER_OF_ACTIVITIES, "2011");
        chartReport.clickOnElement(position);
        JSONObject content = getLatestPostMessageObj();

        JSONObject executionContext = content.getJSONObject("data").getJSONObject("executionContext");
        JSONArray filters = executionContext.getJSONArray("filters");
        assertEquals(filters.length(), 1);
        assertEquals(filters.getJSONObject(0).getJSONObject("absoluteDateFilter")
                .getJSONObject("dataSet").getString("uri"), dateCreatedDs);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingTableReportHasMultipleMetricsOneAttributeDateOnFilter() throws IOException, ParseException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addMetric(METRIC_NUMBER_OF_OPPORTUNITIES).addAttribute(ATTR_REGION);
        analysisPage.getFilterBuckets().configAttributeFilter(ATTR_REGION, "East Coast");
        analysisPage.addDateFilter();
        analysisPage.waitForReportComputing();
        analysisPage.saveInsight("eventing_table_filter_6");

        String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        String opportunityUri = getMetricByTitle(METRIC_NUMBER_OF_OPPORTUNITIES).getUri();
        String regionUri = getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(opportunityUri);
            put(regionUri);
        }};

        String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl(), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        embeddedAnalysisPage.getTableReport().getCellElement(METRIC_NUMBER_OF_ACTIVITIES, 0).click();
        JSONObject content = getLatestPostMessageObj();

        JSONObject executionContext = content.getJSONObject("data").getJSONObject("executionContext");
        JSONArray filters = executionContext.getJSONArray("filters");
        assertEquals(filters.length(), 1);
        assertEquals(filters.getJSONObject(0).getJSONObject("positiveAttributeFilter")
                .getJSONObject("displayForm").getString("uri"), regionUri);

        cleanUpLogger();
        embeddedAnalysisPage.getTableReport().getCellElement(ATTR_REGION, 0).click();
        content = getLatestPostMessageObj();

        executionContext = content.getJSONObject("data").getJSONObject("executionContext");
        filters = executionContext.getJSONArray("filters");
        assertEquals(filters.length(), 1);
        assertEquals(filters.getJSONObject(0).getJSONObject("positiveAttributeFilter")
                .getJSONObject("displayForm").getString("uri"), regionUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingColumnReportHasMultipleMetricsOneAttributeDateOnFilter() throws IOException, ParseException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addMetric(METRIC_NUMBER_OF_OPPORTUNITIES).addAttribute(ATTR_REGION);
        analysisPage.getFilterBuckets().configAttributeFilter(ATTR_REGION, "East Coast");
        analysisPage.addDateFilter();
        analysisPage.waitForReportComputing();
        analysisPage.saveInsight("eventing_column_filter_6");

        String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        String opportunityUri = getMetricByTitle(METRIC_NUMBER_OF_OPPORTUNITIES).getUri();
        String regionUri = getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(opportunityUri);
            put(regionUri);
        }};

        String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl(), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        ChartReport chartReport = embeddedAnalysisPage.getChartReport();
        Pair<Integer, Integer> position = getColumnPosition(chartReport, METRIC_NUMBER_OF_ACTIVITIES, "East Coast");
        chartReport.clickOnElement(position);
        JSONObject content = getLatestPostMessageObj();

        JSONObject executionContext = content.getJSONObject("data").getJSONObject("executionContext");
        JSONArray filters = executionContext.getJSONArray("filters");
        assertEquals(filters.length(), 1);
        assertEquals(filters.getJSONObject(0).getJSONObject("positiveAttributeFilter")
                .getJSONObject("displayForm").getString("uri"), regionUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingColumnReportHasMetricViewAndStack() throws IOException, ParseException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.COLUMN_CHART);
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE).addStack(ATTR_REGION);
        analysisPage.addFilter(ATTR_DEPARTMENT).getFilterBuckets().configAttributeFilter(ATTR_DEPARTMENT, "Direct Sales");
        analysisPage.addDateFilter().getFilterBuckets().configDateFilter("1/1/2011", "12/31/2011");

        analysisPage.waitForReportComputing();
        analysisPage.saveInsight("eventing_table_filter_7");

        String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        String activityTypeUri = getAttributeByTitle(ATTR_ACTIVITY_TYPE).getDefaultDisplayForm().getUri();
        String departmentUri = getAttributeByTitle(ATTR_DEPARTMENT).getDefaultDisplayForm().getUri();
        String regionUri = getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(activityTypeUri);
            put(regionUri);
            put(departmentUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl(), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        ChartReport chartReport = embeddedAnalysisPage.getChartReport();
        Pair<Integer, Integer> position = getColumnPosition(chartReport, "East Coast", "Email");
        embeddedAnalysisPage.getChartReport().clickOnElement(position);
        JSONObject content = getLatestPostMessageObj();

        JSONObject executionContext = content.getJSONObject("data").getJSONObject("executionContext");
        JSONArray filters = executionContext.getJSONArray("filters");
        assertEquals(filters.length(), 2);
        assertEquals(filters.getJSONObject(0).getJSONObject("positiveAttributeFilter")
                .getJSONObject("displayForm").getString("uri"), departmentUri);
        assertEquals(filters.getJSONObject(1).getJSONObject("absoluteDateFilter")
                .getJSONObject("dataSet").getString("uri"), getDatasetByIdentifier("activity.dataset.dt").getUri());
    }
}
