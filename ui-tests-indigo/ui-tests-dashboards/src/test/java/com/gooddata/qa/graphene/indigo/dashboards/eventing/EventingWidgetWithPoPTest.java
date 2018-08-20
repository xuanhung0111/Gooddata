package com.gooddata.qa.graphene.indigo.dashboards.eventing;

import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardEventingTest;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

public class EventingWidgetWithPoPTest extends AbstractDashboardEventingTest {

    @DataProvider(name = "hasPoPWidgetProvider")
    public Object[][] getWidgetHasPoPProvider() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasPoP();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);
        return new Object[][]{
                {dashboardId + "?preventDefault=false", kpiUris.getRight().getUri()},
                {dashboardId + "?preventDefault=true", kpiUris.getRight().getUri()},
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "hasPoPWidgetProvider")
    public void drillOnWidgetHasPoP(String dashboardId, String metricUri) throws Exception {
        final String file = createTemplateHtmlFile(dashboardId, buildEventingMesage(metricUri));
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);

        cleanUpLogger();
        kpiWidget.getPopSection().clickPeriodTilte();
        assertTrue(getLoggerContent().isEmpty(), "Eventing on widget has pop should return empty message");
    }

    @DataProvider(name = "hasSamePoPWidgetProvider")
    public Object[][] getWidgetHasSamePoPProvider() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasSamePoP();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);
        return new Object[][]{
                {dashboardId + "?preventDefault=false", kpiUris.getRight().getUri()},
                {dashboardId + "?preventDefault=true", kpiUris.getRight().getUri()},
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "hasSamePoPWidgetProvider")
    public void drillOnWidgetHasSamePoP(String dashboardId, String metricUri) throws Exception {
        final String file = createTemplateHtmlFile(dashboardId, buildEventingMesage(metricUri));
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);

        cleanUpLogger();
        kpiWidget.getPopSection().clickPeriodTilte();
        assertTrue(getLoggerContent().isEmpty(), "Eventing on widget has same pop should return empty message");
    }

    private String buildEventingMesage(String obj) {
        return new JSONArray() {{
            put(obj);
        }}.toString();
    }
}
