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
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRIORITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_REGION;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_OPPORTUNITIES;
import static org.testng.Assert.assertEquals;

public class EventingFiltersUnderMetric extends AbstractEventingTest {

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingTableReportHasOneMetricOneAttribute() throws IOException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE);
        analysisPage.getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES).expandConfiguration()
                .addFilter(ATTR_REGION, "East Coast");

        analysisPage.saveInsight("eventing_table_metric_filter_1");

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
        embeddedAnalysisPage.getTableReport().getCellElement(String.format("%s (Region: East Coast)",
                METRIC_NUMBER_OF_ACTIVITIES), 0).click();
        JSONObject content = getLatestPostMessageObj();

        JSONObject executionContext = content.getJSONObject("data").getJSONObject("executionContext");
        JSONArray filters = executionContext.getJSONArray("measures").getJSONObject(0)
                .getJSONObject("definition").getJSONObject("measure").getJSONArray("filters");
        assertEquals(filters.length(), 1);
        assertEquals(filters.getJSONObject(0).getJSONObject("positiveAttributeFilter")
                .getJSONObject("displayForm").getString("uri"), regionUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingColumnReportHasOneMetricOneAttribute() throws IOException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE);
        analysisPage.getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES).expandConfiguration()
                .addFilter(ATTR_REGION, "East Coast");

        analysisPage.saveInsight("eventing_column_metric_filter_1");

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
        JSONArray filters = executionContext.getJSONArray("measures").getJSONObject(0)
                .getJSONObject("definition").getJSONObject("measure").getJSONArray("filters");
        assertEquals(filters.length(), 1);
        assertEquals(filters.getJSONObject(0).getJSONObject("positiveAttributeFilter")
                .getJSONObject("displayForm").getString("uri"), regionUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingTableReportHasOneMetricOneDate() throws IOException, ParseException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE).addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDate();
        analysisPage.getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES).expandConfiguration()
                .addFilter(ATTR_REGION, "East Coast");
        analysisPage.waitForReportComputing();

        analysisPage.saveInsight("eventing_table_metric_filter_2");

        String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        String yearActivity = getAttributeByTitle(ATTR_YEAR_ACTIVITY).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(yearActivity);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl(), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        embeddedAnalysisPage.getTableReport().getCellElement(String.format("%s (Region: East Coast)",
                METRIC_NUMBER_OF_ACTIVITIES), 0).click();
        JSONObject content = getLatestPostMessageObj();

        JSONObject executionContext = content.getJSONObject("data").getJSONObject("executionContext");
        JSONArray filters = executionContext.getJSONArray("measures").getJSONObject(0)
                .getJSONObject("definition").getJSONObject("measure").getJSONArray("filters");
        assertEquals(filters.length(), 1);
        assertEquals(filters.getJSONObject(0)
                .getJSONObject("positiveAttributeFilter").getJSONObject("displayForm").getString("uri"),
                getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingColumnReportHasOneMetricOneDate() throws IOException, ParseException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDate();
        analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES).expandConfiguration()
                .addFilter(ATTR_REGION, "East Coast");
        analysisPage.waitForReportComputing();

        analysisPage.saveInsight("eventing_column_metric_filter_2");

        String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        String yearActivity = getAttributeByTitle(ATTR_YEAR_ACTIVITY).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(yearActivity);
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
        JSONArray filters = executionContext.getJSONArray("measures").getJSONObject(0)
                .getJSONObject("definition").getJSONObject("measure").getJSONArray("filters");
        assertEquals(filters.length(), 1);
        assertEquals(filters.getJSONObject(0)
                .getJSONObject("positiveAttributeFilter").getJSONObject("displayForm").getString("uri"),
                getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingTableReportHasMultipleMetricsSingleAttribute() throws IOException, ParseException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addMetric(METRIC_NUMBER_OF_OPPORTUNITIES)
                .addAttribute(ATTR_REGION);
        analysisPage.getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES).expandConfiguration()
                .addFilter(ATTR_REGION, "East Coast");
        analysisPage.waitForReportComputing();
        analysisPage.saveInsight("eventing_table_metric_filter_3");

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
        embeddedAnalysisPage.getTableReport()
                .getCellElement(String.format("%s (Region: East Coast)", METRIC_NUMBER_OF_ACTIVITIES), 0).click();
        JSONObject content = getLatestPostMessageObj();

        JSONObject executionContext = content.getJSONObject("data").getJSONObject("executionContext");
        JSONArray filters = executionContext.getJSONArray("measures").getJSONObject(0)
                .getJSONObject("definition").getJSONObject("measure").getJSONArray("filters");
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
        analysisPage.getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES).expandConfiguration()
                .addFilter(ATTR_PRIORITY, "HIGH");
        analysisPage.waitForReportComputing();
        analysisPage.saveInsight("eventing_column_metric_filter_3");

        String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl(), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        ChartReport chartReport = embeddedAnalysisPage.getChartReport();
        Pair<Integer, Integer> position = getColumnPosition(chartReport,
                String.format("%s (Priority: HIGH)", METRIC_NUMBER_OF_ACTIVITIES), "East Coast");
        chartReport.clickOnElement(position);
        JSONObject content = getLatestPostMessageObj();

        JSONObject executionContext = content.getJSONObject("data").getJSONObject("executionContext");
        JSONArray filters = executionContext.getJSONArray("measures").getJSONObject(0)
                .getJSONObject("definition").getJSONObject("measure").getJSONArray("filters");
        assertEquals(filters.length(), 1);
        assertEquals(filters.getJSONObject(0).getJSONObject("positiveAttributeFilter")
                .getJSONObject("displayForm").getString("uri"), getAttributeByTitle(ATTR_PRIORITY)
                .getDefaultDisplayForm().getUri());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingTableReportHasMultipleMetricsDateAttribute() throws IOException, ParseException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addMetric(METRIC_NUMBER_OF_OPPORTUNITIES)
                .addDate();
        analysisPage.getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES).expandConfiguration()
                .addFilter(ATTR_REGION, "East Coast");
        analysisPage.waitForReportComputing();
        analysisPage.saveInsight("eventing_table_metric_filter_4");

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
        embeddedAnalysisPage.getTableReport()
                .getCellElement(String.format("%s (Region: East Coast)", METRIC_NUMBER_OF_ACTIVITIES), 0).click();
        JSONObject content = getLatestPostMessageObj();

        JSONObject executionContext = content.getJSONObject("data").getJSONObject("executionContext");
        JSONArray filters = executionContext.getJSONArray("measures").getJSONObject(0)
                .getJSONObject("definition").getJSONObject("measure").getJSONArray("filters");
        assertEquals(filters.length(), 1);
        assertEquals(filters.getJSONObject(0).getJSONObject("positiveAttributeFilter")
                .getJSONObject("displayForm").getString("uri"), getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri());

    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingColumnReportHasMultipleMetricsDateAttribute() throws IOException, ParseException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addMetric(METRIC_NUMBER_OF_OPPORTUNITIES)
                .addDate();
        analysisPage.getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES).expandConfiguration()
                .addFilter(ATTR_REGION, "East Coast");
        analysisPage.waitForReportComputing();
        analysisPage.saveInsight("eventing_column_metric_filter_4");

        String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        String dateCreatedDs = getDatasetByIdentifier("created.dataset.dt").getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(dateCreatedDs);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl(), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        ChartReport chartReport = embeddedAnalysisPage.getChartReport();
        Pair<Integer, Integer> position = getColumnPosition(chartReport,
                String.format("%s (Region: East Coast)", METRIC_NUMBER_OF_ACTIVITIES), "2011");
        chartReport.clickOnElement(position);
        JSONObject content = getLatestPostMessageObj();

        JSONObject executionContext = content.getJSONObject("data").getJSONObject("executionContext");
        JSONArray filters = executionContext.getJSONArray("measures").getJSONObject(0)
                .getJSONObject("definition").getJSONObject("measure").getJSONArray("filters");
        assertEquals(filters.length(), 1);
        assertEquals(filters.getJSONObject(0).getJSONObject("positiveAttributeFilter")
                .getJSONObject("displayForm").getString("uri"), getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri());

    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingColumnReportHasMetricViewAndStack() throws IOException, ParseException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.COLUMN_CHART);
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE).addStack(ATTR_REGION);

        analysisPage.getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES).expandConfiguration()
                .addFilter(ATTR_DEPARTMENT, "Direct Sales");

        analysisPage.addFilter(ATTR_DEPARTMENT).getFilterBuckets().configAttributeFilter(ATTR_DEPARTMENT, "Direct Sales");

        analysisPage.waitForReportComputing();
        analysisPage.saveInsight("eventing_table_metric_filter_5");

        String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        String departmentUri = getAttributeByTitle(ATTR_DEPARTMENT).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(departmentUri);
        }};

        String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl(), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        ChartReport chartReport = embeddedAnalysisPage.getChartReport();
        Pair<Integer, Integer> position = getColumnPosition(chartReport, "East Coast", "Email");
        embeddedAnalysisPage.getChartReport().clickOnElement(position);
        JSONObject content = getLatestPostMessageObj();

        JSONObject executionContext = content.getJSONObject("data").getJSONObject("executionContext");
        JSONArray filters = executionContext.getJSONArray("measures").getJSONObject(0)
                .getJSONObject("definition").getJSONObject("measure").getJSONArray("filters");
        assertEquals(filters.length(), 1);
        assertEquals(filters.getJSONObject(0).getJSONObject("positiveAttributeFilter")
                .getJSONObject("displayForm").getString("uri"), departmentUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingTableReportCombineFiltersHasDate() throws IOException, ParseException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE);
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE);
        analysisPage.addDateFilter();
        analysisPage.getFilterBuckets().configDateFilter("1/1/2011", "12/31/2011");
        analysisPage.addFilter(ATTR_REGION);
        analysisPage.getFilterBuckets().configAttributeFilter(ATTR_REGION, "East Coast");

        analysisPage.getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
                .expandConfiguration().addFilter(ATTR_DEPARTMENT, "Direct Sales");

        analysisPage.waitForReportComputing();
        analysisPage.saveInsight("eventing_table_metric_filter_6");

        String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        String departmentUri = getAttributeByTitle(ATTR_DEPARTMENT).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(departmentUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl(), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        embeddedAnalysisPage.getTableReport()
                .getCellElement(String.format("%s (Department: Direct Sales)", METRIC_NUMBER_OF_ACTIVITIES), 0).click();
        JSONObject content = getLatestPostMessageObj();

        JSONObject executionContext = content.getJSONObject("data").getJSONObject("executionContext");
        JSONArray filters = executionContext.getJSONArray("measures").getJSONObject(0)
                .getJSONObject("definition").getJSONObject("measure").getJSONArray("filters");
        assertEquals(filters.length(), 1);
        assertEquals(filters.getJSONObject(0).getJSONObject("positiveAttributeFilter")
                .getJSONObject("displayForm").getString("uri"), departmentUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingColumnReportCombineFiltersHasDate() throws IOException, ParseException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.COLUMN_CHART);
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE);
        analysisPage.addDateFilter();
        analysisPage.getFilterBuckets().configDateFilter("1/1/2011", "12/31/2011");
        analysisPage.addFilter(ATTR_REGION);
        analysisPage.getFilterBuckets().configAttributeFilter(ATTR_REGION, "East Coast");

        analysisPage.getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES).expandConfiguration()
                .addFilter(ATTR_DEPARTMENT, "Direct Sales");

        analysisPage.waitForReportComputing();
        analysisPage.saveInsight("eventing_column_metric_filter_6");

        String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        String departmentUri = getAttributeByTitle(ATTR_DEPARTMENT).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(departmentUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl(), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        ChartReport chartReport = embeddedAnalysisPage.getChartReport();
        Pair<Integer, Integer> position = getColumnPosition(chartReport, METRIC_NUMBER_OF_ACTIVITIES, "Email");
        embeddedAnalysisPage.getChartReport().clickOnElement(position);
        JSONObject content = getLatestPostMessageObj();

        JSONObject executionContext = content.getJSONObject("data").getJSONObject("executionContext");
        JSONArray filters = executionContext.getJSONArray("measures").getJSONObject(0)
                .getJSONObject("definition").getJSONObject("measure").getJSONArray("filters");
        assertEquals(filters.length(), 1);
        assertEquals(filters.getJSONObject(0).getJSONObject("positiveAttributeFilter")
                .getJSONObject("displayForm").getString("uri"), departmentUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingTableReportCombineMultipleMetricsFiltersHasDate() throws IOException, ParseException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE);
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addMetric(METRIC_NUMBER_OF_OPPORTUNITIES)
                .addAttribute(ATTR_DEPARTMENT);
        analysisPage.addDateFilter();
        analysisPage.getFilterBuckets().configDateFilter("1/1/2011", "12/31/2011");
        analysisPage.addFilter(ATTR_REGION);
        analysisPage.getFilterBuckets().configAttributeFilter(ATTR_REGION, "East Coast");

        analysisPage.getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES).expandConfiguration()
                .addFilter(ATTR_PRIORITY, "HIGH");

        analysisPage.waitForReportComputing();
        analysisPage.saveInsight("eventing_table_metric_filter_7");

        String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl(), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        embeddedAnalysisPage.getTableReport()
                .getCellElement(String.format("%s (Priority: HIGH)", METRIC_NUMBER_OF_ACTIVITIES), 0).click();
        JSONObject content = getLatestPostMessageObj();

        JSONObject executionContext = content.getJSONObject("data").getJSONObject("executionContext");
        JSONArray filters = executionContext.getJSONArray("measures").getJSONObject(0)
                .getJSONObject("definition").getJSONObject("measure").getJSONArray("filters");
        assertEquals(filters.length(), 1);
        assertEquals(filters.getJSONObject(0).getJSONObject("positiveAttributeFilter")
                .getJSONObject("displayForm").getString("uri"), getAttributeByTitle(ATTR_PRIORITY)
                .getDefaultDisplayForm().getUri());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingColumnReportCombineMultipleMetricsFiltersHasDate() throws IOException, ParseException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.COLUMN_CHART);
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addMetric(METRIC_NUMBER_OF_OPPORTUNITIES)
                .addAttribute(ATTR_DEPARTMENT);
        analysisPage.addDateFilter();
        analysisPage.getFilterBuckets().configDateFilter("1/1/2011", "12/31/2011");
        analysisPage.addFilter(ATTR_REGION);
        analysisPage.getFilterBuckets().configAttributeFilter(ATTR_REGION, "East Coast");

        analysisPage.getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES).expandConfiguration().addFilter(ATTR_PRIORITY, "HIGH");

        analysisPage.waitForReportComputing();
        analysisPage.saveInsight("eventing_column_metric_filter_7");

        String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl(), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        ChartReport chartReport = embeddedAnalysisPage.getChartReport();
        Pair<Integer, Integer> position = getColumnPosition(chartReport,
                String.format("%s (Priority: HIGH)", METRIC_NUMBER_OF_ACTIVITIES), "Direct Sales");
        embeddedAnalysisPage.getChartReport().clickOnElement(position);
        JSONObject content = getLatestPostMessageObj();

        JSONObject executionContext = content.getJSONObject("data").getJSONObject("executionContext");
        JSONArray filters = executionContext.getJSONArray("measures").getJSONObject(0)
                .getJSONObject("definition").getJSONObject("measure").getJSONArray("filters");
        assertEquals(filters.length(), 1);
        assertEquals(filters.getJSONObject(0).getJSONObject("positiveAttributeFilter")
                        .getJSONObject("displayForm").getString("uri"),
                getAttributeByTitle(ATTR_PRIORITY).getDefaultDisplayForm().getUri());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingTableReportMultipleMetricsNoAttribute() throws IOException, ParseException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE);
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addMetric(METRIC_NUMBER_OF_OPPORTUNITIES);

        analysisPage.getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES).expandConfiguration().addFilter(ATTR_PRIORITY, "HIGH");

        analysisPage.waitForReportComputing();
        analysisPage.saveInsight("eventing_table_metric_filter_8");

        String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl(), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        embeddedAnalysisPage.getTableReport()
                .getCellElement(String.format("%s (Priority: HIGH)", METRIC_NUMBER_OF_ACTIVITIES), 0).click();
        JSONObject content = getLatestPostMessageObj();

        JSONObject executionContext = content.getJSONObject("data").getJSONObject("executionContext");
        JSONArray filters = executionContext.getJSONArray("measures").getJSONObject(0)
                .getJSONObject("definition").getJSONObject("measure").getJSONArray("filters");
        assertEquals(filters.length(), 1);
        assertEquals(filters.getJSONObject(0).getJSONObject("positiveAttributeFilter")
                        .getJSONObject("displayForm").getString("uri"),
                getAttributeByTitle(ATTR_PRIORITY).getDefaultDisplayForm().getUri());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingColumnReportultipleMetricsNoAttribute() throws IOException, ParseException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.COLUMN_CHART);
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addMetric(METRIC_NUMBER_OF_OPPORTUNITIES);

        analysisPage.getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES).expandConfiguration()
                .addFilter(ATTR_PRIORITY, "HIGH");

        analysisPage.waitForReportComputing();
        analysisPage.saveInsight("eventing_column_metric_filter_8");

        String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl(), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        ChartReport chartReport = embeddedAnalysisPage.getChartReport();
        Pair<Integer, Integer> position = getColumnPosition(chartReport,
                String.format("%s (Priority: HIGH)", METRIC_NUMBER_OF_ACTIVITIES));
        embeddedAnalysisPage.getChartReport().clickOnElement(position);
        JSONObject content = getLatestPostMessageObj();

        JSONObject executionContext = content.getJSONObject("data").getJSONObject("executionContext");
        JSONArray filters = executionContext.getJSONArray("measures").getJSONObject(0)
                .getJSONObject("definition").getJSONObject("measure").getJSONArray("filters");
        assertEquals(filters.length(), 1);
        assertEquals(filters.getJSONObject(0).getJSONObject("positiveAttributeFilter")
                        .getJSONObject("displayForm").getString("uri"),
                getAttributeByTitle(ATTR_PRIORITY).getDefaultDisplayForm().getUri());
    }
}
