package com.gooddata.qa.graphene.indigo.analyze.eventing;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.CompareTypeDropdown;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.EmbeddedAnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractEventingTest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;

import java.io.IOException;
import java.text.ParseException;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES_YEAR_AGO;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ContributionAndComparisionTest extends AbstractEventingTest {
    private static final String ACTIVITIES_IN_PERCENTATION = "% " + METRIC_NUMBER_OF_ACTIVITIES;
    private static final String ACTIVITIES_YEAR_AGO_IN_PERCENTATION = "% " + METRIC_NUMBER_OF_ACTIVITIES_YEAR_AGO;

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingTableReportWithPercentationAndDateAttribute() throws IOException {
        String insightUri = createSimpleInsightWithPercentation("eventing_table_show_percent_date_attr",
                ReportType.TABLE, METRIC_NUMBER_OF_ACTIVITIES, ATTR_YEAR_ACTIVITY);

        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String yearActivityUri = getAttributeByTitle(ATTR_YEAR_ACTIVITY).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(yearActivityUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromUri(insightUri), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        TableReport tableReport = embeddedAnalysisPage.getTableReport();
        tableReport.getCellElement(ACTIVITIES_IN_PERCENTATION, 0).click();

        verifyReportHasShowPercentation(yearActivityUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingColumnReportWithPercentationAndDateAttribute() throws IOException {
        String insightUri = createSimpleInsightWithPercentation("eventing_column_show_percent_date_attr",
                ReportType.COLUMN_CHART, METRIC_NUMBER_OF_ACTIVITIES, ATTR_YEAR_ACTIVITY);

        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String yearActivityUri = getAttributeByTitle(ATTR_YEAR_ACTIVITY).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(yearActivityUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromUri(insightUri), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        ChartReport chartReport = embeddedAnalysisPage.getChartReport();
        chartReport.clickOnElement(getColumnPosition(chartReport, ACTIVITIES_IN_PERCENTATION, "2011"));

        verifyReportHasShowPercentation(yearActivityUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingTableReportWithPercentationAndNonDateAttribute() throws IOException {
        String insightUri = createSimpleInsightWithPercentation("eventing_column_show_percent_nondate_attr",
                ReportType.TABLE, METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE);
        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String activityTypeUri = getAttributeByTitle(ATTR_ACTIVITY_TYPE).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(activityTypeUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromUri(insightUri), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        TableReport tableReport = embeddedAnalysisPage.getTableReport();
        tableReport.getCellElement(ACTIVITIES_IN_PERCENTATION, 0).click();

        verifyReportHasShowPercentation(activityTypeUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingColumnReportWithPercentationAndNonDateAttribute() throws IOException {
        String insightUri = createSimpleInsightWithPercentation("eventing_column_show_percent_nondate_attr",
                ReportType.COLUMN_CHART, METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE);

        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String activityTypeUri = getAttributeByTitle(ATTR_ACTIVITY_TYPE).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(activityTypeUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromUri(insightUri), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        ChartReport chartReport = embeddedAnalysisPage.getChartReport();
        chartReport.clickOnElement(getColumnPosition(chartReport, ACTIVITIES_IN_PERCENTATION, "Email"));

        verifyReportHasShowPercentation(activityTypeUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingPoPTableReportWithDateAttribute() throws IOException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE);
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDate().waitForReportComputing();

        analysisPage.getFilterBuckets().openDateFilterPickerPanel()
                .applyCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_LAST_YEAR);

        analysisPage.saveInsight("eventing_table_report_pop_date_attribute");

        String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl(), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        TableReport tableReport = embeddedAnalysisPage.getTableReport();
        tableReport.getCellElement(METRIC_NUMBER_OF_ACTIVITIES, 0).click();

        verifyReportWithPoP();

        cleanUpLogger();
        tableReport.getCellElement(METRIC_NUMBER_OF_ACTIVITIES_YEAR_AGO, 0).click();
        verifyReportWithPoP();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingPoPColumnReportWithDateAttribute() throws IOException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.COLUMN_CHART);
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDate().waitForReportComputing();

        analysisPage.getFilterBuckets().openDateFilterPickerPanel()
                .applyCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_LAST_YEAR);

        analysisPage.saveInsight("eventing_column_report_pop_date_attribute");

        String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl(), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        ChartReport chartReport = embeddedAnalysisPage.getChartReport();
        chartReport.clickOnElement(getColumnPosition(chartReport, METRIC_NUMBER_OF_ACTIVITIES, "2011"));
        verifyReportWithPoP();

        cleanUpLogger();
        chartReport.clickOnElement(getColumnPosition(chartReport, METRIC_NUMBER_OF_ACTIVITIES_YEAR_AGO, "2011"));
        verifyReportWithPoP();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingPoPColumnReportNoAttributeWithDateFilter() throws IOException, ParseException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.COLUMN_CHART);
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDateFilter()
                .getFilterBuckets().configDateFilter("1/1/2011", "12/31/2011")
                .getRoot().click();
        analysisPage.getFilterBuckets().openDateFilterPickerPanel().
                applyCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_LAST_YEAR);

        analysisPage.waitForReportComputing();
        analysisPage.saveInsight("eventing_pop_date_filter_no_attr");

        String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl(), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        ChartReport chartReport = embeddedAnalysisPage.getChartReport();
        chartReport.clickOnElement(getColumnPosition(chartReport, METRIC_NUMBER_OF_ACTIVITIES));
        verifyReportWithPoP();

        cleanUpLogger();
        chartReport.clickOnElement(getColumnPosition(chartReport, METRIC_NUMBER_OF_ACTIVITIES_YEAR_AGO));
        verifyReportWithPoP();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingPoPTableReportHasMetricAttributeAndDateFilter() throws IOException, ParseException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE).waitForReportComputing();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACTIVITY_TYPE).addDateFilter()
                .getFilterBuckets().configDateFilter("1/1/2011", "12/31/2011");
        analysisPage.getFilterBuckets().openDateFilterPickerPanel().
                applyCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_LAST_YEAR);

        analysisPage.waitForReportComputing();
        analysisPage.saveInsight("eventing_table_report_pop_date_filter");

        String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        String activityTypeUri = getAttributeByTitle(ATTR_ACTIVITY_TYPE).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(activityTypeUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl(), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        TableReport tableReport = embeddedAnalysisPage.getTableReport();
        tableReport.getCellElement(METRIC_NUMBER_OF_ACTIVITIES, 0).click();
        verifyReportWithPoP();

        cleanUpLogger();
        tableReport.getCellElement(METRIC_NUMBER_OF_ACTIVITIES_YEAR_AGO, 0).click();
        verifyReportWithPoP();

        cleanUpLogger();
        tableReport.getCellElement(ATTR_ACTIVITY_TYPE, 0).click();
        verifyReportWithPoP();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingPoPColumnReportHasMetricAttributeAndDateFilter() throws IOException, ParseException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACTIVITY_TYPE).addDateFilter()
                .getFilterBuckets().configDateFilter("1/1/2011", "12/31/2011");
        analysisPage.getFilterBuckets().openDateFilterPickerPanel().
                applyCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_LAST_YEAR);

        analysisPage.waitForReportComputing();
        analysisPage.saveInsight("eventing_table_report_pop_date_filter");

        String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        String activityTypeUri = getAttributeByTitle(ATTR_ACTIVITY_TYPE).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(activityTypeUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl(), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        ChartReport chartReport = embeddedAnalysisPage.getChartReport();
        chartReport.clickOnElement(getColumnPosition(chartReport, METRIC_NUMBER_OF_ACTIVITIES, "Email"));
        verifyReportWithPoP();

        cleanUpLogger();
        chartReport.clickOnElement(getColumnPosition(chartReport, METRIC_NUMBER_OF_ACTIVITIES_YEAR_AGO, "Email"));
        verifyReportWithPoP();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingTableReportWithCombinationOfPoPAndContributionDateAttribute() throws IOException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE);
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDate().waitForReportComputing();
        analysisPage.getMetricsBucket().getLastMetricConfiguration().expandConfiguration().showPercents();

        analysisPage.getFilterBuckets().openDateFilterPickerPanel().
                applyCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_LAST_YEAR);
        analysisPage.saveInsight("eventing_table_combination_pop_contribution");

        String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl(), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        TableReport tableReport = embeddedAnalysisPage.getTableReport();
        tableReport.getCellElement(ACTIVITIES_IN_PERCENTATION, 3).click();
        verifyReportWithCombination(ACTIVITIES_IN_PERCENTATION, 1, true);

        cleanUpLogger();
        tableReport.getCellElement(ACTIVITIES_YEAR_AGO_IN_PERCENTATION, 3).click();
        verifyReportWithCombination(ACTIVITIES_YEAR_AGO_IN_PERCENTATION, 0, false);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingColumnReportWithCombinationOfPoPAndContributionDateAttribute() throws IOException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.COLUMN_CHART);
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDate().waitForReportComputing();
        analysisPage.getMetricsBucket().getLastMetricConfiguration().expandConfiguration().showPercents();

        analysisPage.getFilterBuckets().openDateFilterPickerPanel().
                applyCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_LAST_YEAR);
        analysisPage.saveInsight("eventing_column_combination_pop_contribution");

        String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl(), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        ChartReport chartReport = embeddedAnalysisPage.getChartReport();
        chartReport.clickOnElement(getColumnPosition(chartReport, ACTIVITIES_IN_PERCENTATION, "2011"));
        verifyReportWithCombination(ACTIVITIES_IN_PERCENTATION, 1, true);

        cleanUpLogger();
        chartReport.clickOnElement(getColumnPosition(chartReport, ACTIVITIES_YEAR_AGO_IN_PERCENTATION, "2011"));
        verifyReportWithCombination(ACTIVITIES_YEAR_AGO_IN_PERCENTATION, 0, false);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingTableReportWithCombinationOfPoPAndComparisionDateFilter()
            throws IOException, ParseException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE).waitForReportComputing();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACTIVITY_TYPE).addDateFilter()
                .getFilterBuckets().configDateFilter("1/1/2011", "12/31/2011");
        analysisPage.getMetricsBucket().getLastMetricConfiguration().expandConfiguration().showPercents();
        analysisPage.getFilterBuckets().openDateFilterPickerPanel().
                applyCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_LAST_YEAR);

        analysisPage.waitForReportComputing();
        analysisPage.saveInsight("eventing_table_pop_contribution_date_filter");

        String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        String activityTypeUri = getAttributeByTitle(ATTR_ACTIVITY_TYPE).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(activityTypeUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl(), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        TableReport tableReport = embeddedAnalysisPage.getTableReport();
        tableReport.getCellElement(ACTIVITIES_IN_PERCENTATION, 3).click();
        verifyReportWithCombination(ACTIVITIES_IN_PERCENTATION, 1, true);

        cleanUpLogger();
        tableReport.getCellElement(ACTIVITIES_YEAR_AGO_IN_PERCENTATION, 3).click();
        verifyReportWithCombination(ACTIVITIES_YEAR_AGO_IN_PERCENTATION, 0, false);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testEventingColumnReportWithCombinationOfPoPAndComparisionDateFilter()
            throws IOException, ParseException {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACTIVITY_TYPE).addDateFilter()
                .getFilterBuckets().configDateFilter("1/1/2011", "12/31/2011");
        analysisPage.getMetricsBucket().getLastMetricConfiguration().expandConfiguration().showPercents();
        analysisPage.getFilterBuckets().openDateFilterPickerPanel().
                applyCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_LAST_YEAR);

        analysisPage.waitForReportComputing();
        analysisPage.saveInsight("eventing_table_pop_contribution_date_filter");

        String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        String activityTypeUri = getAttributeByTitle(ATTR_ACTIVITY_TYPE).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(activityTypeUri);
        }};

        final String file = createTemplateHtmlFile(getObjectIdFromBrowserUrl(), uris.toString());
        EmbeddedAnalysisPage embeddedAnalysisPage = openEmbeddedPage(file);
        embeddedAnalysisPage.waitForReportComputing();

        cleanUpLogger();
        ChartReport chartReport = embeddedAnalysisPage.getChartReport();
        chartReport.clickOnElement(getColumnPosition(chartReport, ACTIVITIES_IN_PERCENTATION, "Email"));
        verifyReportWithCombination(ACTIVITIES_IN_PERCENTATION, 1, true);

        cleanUpLogger();
        chartReport.clickOnElement(getColumnPosition(chartReport, ACTIVITIES_YEAR_AGO_IN_PERCENTATION, "Email"));
        verifyReportWithCombination(ACTIVITIES_YEAR_AGO_IN_PERCENTATION, 0, false);
    }

    private void verifyReportHasShowPercentation(String expectedAttributeUri) {
        String contentStr = getLoggerContent();
        log.info(contentStr);

        JSONObject content = new JSONObject(contentStr);
        JSONArray measures = content.getJSONObject("data").getJSONObject("executionContext").getJSONArray("measures");
        assertEquals(measures.length(), 1);
        assertEquals(measures.getJSONObject(0).getString("format"), "#,##0.00%");
        assertEquals(measures.getJSONObject(0)
                .getJSONObject("definition").getJSONObject("measure").getBoolean("computeRatio"), true);

        JSONArray attributes = content.getJSONObject("data").getJSONObject("executionContext").getJSONArray("attributes");
        assertEquals(attributes.getJSONObject(0)
                .getJSONObject("displayForm").getString("uri"), expectedAttributeUri);
    }

    private void verifyReportWithPoP() {
        String contentStr = getLoggerContent();
        log.info(contentStr);

        JSONObject content = new JSONObject(contentStr);
        JSONObject definition = content.getJSONObject("data").getJSONObject("executionContext").getJSONArray("measures")
                .getJSONObject(0).getJSONObject("definition");

        assertTrue(definition.has("popMeasure"), "popMeasure should be in executionContext");
        assertTrue(definition.getJSONObject("popMeasure")
                .has("popAttribute"), "popAttribute should be in executionContext");
    }

    private void verifyReportWithCombination(String metric, int index, boolean hasFormat) {
        String contentStr = getLoggerContent();
        log.info(contentStr);

        JSONObject content = new JSONObject(contentStr);
        JSONObject measure = content.getJSONObject("data").getJSONObject("executionContext").getJSONArray("measures")
                .getJSONObject(index);
        assertEquals(measure.getString("alias"), metric);
        assertEquals(measure.has("format"), hasFormat, "hasFormat should be " + String.valueOf(hasFormat));
        if (hasFormat) {
            assertEquals(measure.getString("format"), "#,##0.00%");
        }
    }
}
