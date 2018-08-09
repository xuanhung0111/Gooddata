package com.gooddata.qa.graphene.indigo.dashboards.eventing;

import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardEventingTest;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.gooddata.qa.graphene.enums.indigo.ReportType.COLUMN_CHART;
import static com.gooddata.qa.graphene.enums.indigo.ReportType.TABLE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_REGION;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static java.util.Collections.*;
import static org.testng.Assert.assertTrue;

public class EventingInEditMode extends AbstractDashboardEventingTest {

    @Test(dependsOnGroups = {"createProject"})
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
        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri), uris.toString());
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        indigoDashboardsPage.switchToEditMode();
        Insight insight = indigoDashboardsPage.getLastWidget(Insight.class);

        cleanUpLogger();
        Pair<Integer, Integer> position = getColumnPosition(insight.getChartReport(),
                METRIC_NUMBER_OF_ACTIVITIES, "East Coast");
        insight.getChartReport().clickOnElement(position);

        String contentStr = getLoggerContent();
        assertTrue(contentStr.isEmpty(), "should no drill event triggered");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void eventingTableReportSingleMetricSingleAttribute() throws IOException {
        String insightUri = createInsight("single_attribute_single_metric_table_insight", TABLE,
                singletonList(METRIC_NUMBER_OF_ACTIVITIES), singletonList(ATTR_REGION));
        final String dashboardUri = createAnalyticalDashboard("kpi_eventing_8", insightUri);
        final String activityUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        final String regionUri = getAttributeByTitle(ATTR_REGION).getDefaultDisplayForm().getUri();
        JSONArray uris = new JSONArray() {{
            put(activityUri);
            put(regionUri);
        }};
        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri), uris.toString());
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        indigoDashboardsPage.switchToEditMode();
        Insight insight = indigoDashboardsPage.getLastWidget(Insight.class);

        cleanUpLogger();
        insight.getTableReport().getCellElement(ATTR_REGION, 0).click();
        String contentStr = getLoggerContent();
        assertTrue(contentStr.isEmpty(), "should no drill event triggered");
    }
}
