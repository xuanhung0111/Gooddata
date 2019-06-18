package com.gooddata.qa.graphene.indigo.analyze.eventing;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.EmbeddedAnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.PivotTableReport;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractEventingTest;
import org.json.JSONArray;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_REGION;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class VisualizationMeasureAttributeTest extends AbstractEventingTest {

    private static final int TESTED_ROW_INDEX = 3;

    private String insightObjectId;

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();

        final String insightUri = createInsight("Simple_Drillable_Insight", ReportType.TABLE, Collections.singletonList(METRIC_NUMBER_OF_ACTIVITIES),
                Arrays.asList(ATTR_ACTIVITY_TYPE, ATTR_REGION));
        insightObjectId = getObjectIdFromUri(insightUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testVisualizationOfNonDefinedDrillableReports() throws IOException {
        final String file = createTemplateHtmlFile(insightObjectId);
        verifyNotUnderlineAndHighlight(file);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testVisualizationOfDrillingUrisReports() throws IOException {
        JSONArray uris = new JSONArray() {{
            put(getAttributeByTitle(ATTR_ACTIVITY_TYPE).getDefaultDisplayForm().getUri());
        }};
        final String file = createTemplateHtmlFile(insightObjectId, uris.toString());
        verifyUnderlineAndHighlight(file);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testVisualizationOfDrillingIdentifiersReports() throws IOException {
        JSONArray identifiers = new JSONArray() {{
            put(getAttributeByTitle(ATTR_ACTIVITY_TYPE).getDefaultDisplayForm().getIdentifier());
        }};
        final String file = createTemplateHtmlFile(insightObjectId,
                "[]", identifiers.toString());
        verifyUnderlineAndHighlight(file);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testVisualizationOfDrillingUrisAndIdentifiersReports() throws IOException {
        JSONArray uris = new JSONArray() {{
            put(getAttributeByTitle(ATTR_ACTIVITY_TYPE).getDefaultDisplayForm().getUri());
        }};

        JSONArray identifiers = new JSONArray() {{
            put(getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getIdentifier());
        }};
        final String file = createTemplateHtmlFile(insightObjectId,
                uris.toString(), identifiers.toString());

        EmbeddedAnalysisPage analysisPage = openEmbeddedPage(file);
        analysisPage.waitForReportComputing();
        PivotTableReport report = analysisPage.getPivotTableReport();

        assertTrue(report.isCellUnderlined(ATTR_ACTIVITY_TYPE, TESTED_ROW_INDEX),
                String.format("Column %s should be underlined", ATTR_ACTIVITY_TYPE));

        assertTrue(report.isCellUnderlined(ATTR_REGION, TESTED_ROW_INDEX),
                String.format("Column %s should be underlined", ATTR_REGION));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testVisualizationOfInvalidDrillingConfig() throws IOException {
        JSONArray uris = new JSONArray() {{
            put("/gdc/invalid_uri");
        }};

        JSONArray identifiers = new JSONArray() {{
            put(getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getIdentifier());
        }};
        final String file = createTemplateHtmlFile(insightObjectId,
                uris.toString(), identifiers.toString());

        EmbeddedAnalysisPage analysisPage = openEmbeddedPage(file);
        analysisPage.waitForReportComputing();
        PivotTableReport report = analysisPage.getPivotTableReport();

        assertTrue(report.isCellUnderlined(ATTR_REGION, TESTED_ROW_INDEX),
                String.format("Column %s should be underlined", ATTR_REGION));
    }

    private void verifyNotUnderlineAndHighlight(final String htmlFile) {
        EmbeddedAnalysisPage analysisPage = openEmbeddedPage(htmlFile);
        analysisPage.waitForReportComputing();
        PivotTableReport report = analysisPage.getPivotTableReport();

        assertFalse(report.isCellUnderlined(ATTR_ACTIVITY_TYPE, TESTED_ROW_INDEX),
                String.format("Column %s should not be underlined", ATTR_ACTIVITY_TYPE));

        analysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();
        ChartReport chartReport = analysisPage.getChartReport();
        assertFalse(chartReport
                        .isColumnHighlighted(getColumnPosition(chartReport, "East Coast", "Email")),
                "Chart(East Coast, Email) should not be highlighted");
    }

    private void verifyUnderlineAndHighlight(final String htmlFile) {
        EmbeddedAnalysisPage analysisPage = openEmbeddedPage(htmlFile);
        analysisPage.waitForReportComputing();
        PivotTableReport report = analysisPage.getPivotTableReport();

        assertTrue(report.isCellUnderlined(ATTR_ACTIVITY_TYPE, TESTED_ROW_INDEX),
                String.format("Column %s should be underlined", ATTR_ACTIVITY_TYPE));
        analysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();
        ChartReport chartReport = analysisPage.getChartReport();
        assertTrue(chartReport
                        .isColumnHighlighted(getColumnPosition(chartReport, "East Coast", "Email")),
                "Chart(East Coast, Email) should be highlighted");
    }
}
