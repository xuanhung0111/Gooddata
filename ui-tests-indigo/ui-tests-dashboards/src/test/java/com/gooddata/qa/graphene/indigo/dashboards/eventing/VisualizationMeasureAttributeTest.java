package com.gooddata.qa.graphene.indigo.dashboards.eventing;

import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardEventingTest;
import org.json.JSONArray;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_REGION;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class VisualizationMeasureAttributeTest extends AbstractDashboardEventingTest {

    private String dashboardId;

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();

        String insightUri = createSimpleTableInsight("Kpi_Eventing_Simple_Insight", METRIC_NUMBER_OF_ACTIVITIES,
                ATTR_ACTIVITY_TYPE, ATTR_REGION);
        log.info(insightUri);
        final String dashboardUri = createAnalyticalDashboard("kpi_test_eventing_visualization_11", insightUri);
        log.info(dashboardUri);
        dashboardId = getObjectIdFromUri(dashboardUri);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testVisualizationOfNonDefinedDrillableReports() throws IOException {
        final String file = createTemplateHtmlFile(dashboardId);
        verifyNotUnderlineAndHighlight(file);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testVisualizationOfDrillingUrisReports() throws IOException {
        JSONArray uris = new JSONArray() {{
            put(getAttributeByTitle(ATTR_ACTIVITY_TYPE).getDefaultDisplayForm().getUri());
        }};
        final String file = createTemplateHtmlFile(dashboardId, uris.toString());
        verifyUnderlineAndHighlight(file);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testVisualizationOfDrillingIdentifiersReports() throws IOException {
        JSONArray identifiers = new JSONArray() {{
            put(getAttributeByTitle(ATTR_ACTIVITY_TYPE).getDefaultDisplayForm().getIdentifier());
        }};
        final String file = createTemplateHtmlFile(dashboardId,
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
        final String file = createTemplateHtmlFile(dashboardId,
                uris.toString(), identifiers.toString());

        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Insight insight = indigoDashboardsPage.getLastWidget(Insight.class);
        TableReport report = insight.getTableReport();

        assertTrue(report.isCellUnderlined(ATTR_ACTIVITY_TYPE, 0),
                String.format("Column %s should be underlined", ATTR_ACTIVITY_TYPE));

        assertTrue(report.isCellUnderlined(ATTR_REGION, 0),
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
        final String file = createTemplateHtmlFile(dashboardId,
                uris.toString(), identifiers.toString());

        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Insight insight = indigoDashboardsPage.getLastWidget(Insight.class);
        TableReport report = insight.getTableReport();

        assertTrue(report.isCellUnderlined(ATTR_REGION, 0),
                String.format("Column %s should be underlined", ATTR_REGION));
    }

    private void verifyNotUnderlineAndHighlight(final String htmlFile) {
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(htmlFile).waitForWidgetsLoading();
        Insight insight = indigoDashboardsPage.getLastWidget(Insight.class);
        TableReport report = insight.getTableReport();

        assertFalse(report.isCellUnderlined(ATTR_ACTIVITY_TYPE, 0),
                String.format("Column %s should not be underlined", ATTR_ACTIVITY_TYPE));
    }

    private void verifyUnderlineAndHighlight(final String htmlFile) {
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(htmlFile).waitForWidgetsLoading();
        Insight insight = indigoDashboardsPage.getLastWidget(Insight.class);
        TableReport report = insight.getTableReport();

        assertTrue(report.isCellUnderlined(ATTR_ACTIVITY_TYPE, 0),
                String.format("Column %s should be underlined", ATTR_ACTIVITY_TYPE));
    }
}
