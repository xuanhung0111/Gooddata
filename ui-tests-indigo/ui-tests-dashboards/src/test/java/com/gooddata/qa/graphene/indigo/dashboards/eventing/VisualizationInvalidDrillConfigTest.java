package com.gooddata.qa.graphene.indigo.dashboards.eventing;

import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardEventingTest;
import com.gooddata.qa.graphene.utils.GoodSalesUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.assertFalse;

public class VisualizationInvalidDrillConfigTest extends AbstractDashboardEventingTest {

    private Metric opportunityMetric;

    @Override
    public void prepareProject() throws Throwable {
        super.prepareProject();
        opportunityMetric = getMetricByTitle(GoodSalesUtils.METRIC_NUMBER_OF_OPPORTUNITIES);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillOnWidgetHasNoData() throws IOException {
        final Pair<String, Metric> kpiUris = createNodataKpi();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(), kpiUris.getLeft());
        JSONArray uris = new JSONArray() {{
            put(opportunityMetric.getUri());
        }};
        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri), uris.toString());
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);
        assertFalse(kpiWidget.isDrillable(), "widget should not be drillable");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillOnWidgetInvalidData() throws IOException {
        Pair<String, Metric> kpiUris = createInvalidKpi();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(), kpiUris.getLeft());
        JSONArray drillableUris = new JSONArray() {{
            put(opportunityMetric.getUri());
        }};
        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri), drillableUris.toString());
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);
        assertFalse(kpiWidget.isDrillable(), "widget should not be drillable");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillOnWidgetApplyAttribute() throws IOException {
        final Pair<String, Metric> kpiUris = createKpiHasAttributeOnMaql();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(), kpiUris.getLeft());
        JSONArray drillableUris = new JSONArray() {{
            put(opportunityMetric.getUri());
        }};
        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri), drillableUris.toString());
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);
        assertFalse(kpiWidget.isDrillable(), "widget should not be drillable");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillOnWidgetApplyDateTime() throws IOException {
        final Pair<String, Metric> kpiUris = createKpiHasDate();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(), kpiUris.getLeft());
        JSONArray drillableUris = new JSONArray() {{
            put(opportunityMetric.getUri());
        }};
        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri), drillableUris.toString());
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);
        assertFalse(kpiWidget.isDrillable(), "widget should not be drillable");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillOnWidgetApplyVarible() throws IOException {
        final Pair<String, Metric> kpiUris = createKpiHasVarible();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(), kpiUris.getLeft());
        JSONArray drillableUris = new JSONArray() {{
            put(opportunityMetric.getUri());
        }};
        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri), drillableUris.toString());
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);
        assertFalse(kpiWidget.isDrillable(), "widget should not be drillable");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillOnWidgetHasNegative() throws IOException {
        final Pair<String, Metric> kpiUris = createKpiHasNegativeMetric();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(), kpiUris.getLeft());
        JSONArray drillableUris = new JSONArray() {{
            put(opportunityMetric.getUri());
        }};
        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri), drillableUris.toString());
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);
        assertFalse(kpiWidget.isDrillable(), "widget should not be drillable");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillOnWidgetHasZeroMetric() throws IOException {
        final Pair<String, Metric> kpiUris = createKpiHasZeroMetric();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(), kpiUris.getLeft());
        JSONArray drillableUris = new JSONArray() {{
            put(opportunityMetric.getUri());
        }};
        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri), drillableUris.toString());
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);
        assertFalse(kpiWidget.isDrillable(), "widget should not be drillable");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillOnWidgetHasShareInMetric() throws IOException {
        final Pair<String, Metric> kpiUris = createKpiHasMetricInShare();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(), kpiUris.getLeft());
        JSONArray drillableUris = new JSONArray() {{
            put(opportunityMetric.getUri());
        }};
        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri), drillableUris.toString());
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);
        assertFalse(kpiWidget.isDrillable(), "widget should not be drillable");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillOnWidgetHasShareInDifferent() throws IOException {
        final Pair<String, Metric> kpiUris = createKpiHasMetricInDifference();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(), kpiUris.getLeft());
        JSONArray drillableUris = new JSONArray() {{
            put(opportunityMetric.getUri());
        }};
        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri), drillableUris.toString());
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);
        assertFalse(kpiWidget.isDrillable(), "widget should not be drillable");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillOnWidgetHasShareInRatio() throws IOException {
        final Pair<String, Metric> kpiUris = createKpiHasMetricInRatio();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(), kpiUris.getLeft());
        JSONArray drillableUris = new JSONArray() {{
            put(opportunityMetric.getUri());
        }};
        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri), drillableUris.toString());
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);
        assertFalse(kpiWidget.isDrillable(), "widget should not be drillable");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillOnWidgetHasNoDateFilter() throws IOException {
        final Pair<String, Metric> kpiUris = createKpiHasNoDate();
        final String dashboardTitle = "eventing_widget_" + generateHashString();
        final String dashboardUri = createAnalyticalDashboardWithWidget(dashboardTitle, kpiUris.getLeft());
        initIndigoDashboardsPageWithWidgets().selectKpiDashboard(dashboardTitle);
        indigoDashboardsPage.switchToEditMode().waitForWidgetsLoading().selectLastWidget(Kpi.class);
        indigoDashboardsPage.getConfigurationPanel().disableDateFilter();
        indigoDashboardsPage.saveEditModeWithWidgets().waitForWidgetsLoading();

        JSONArray drillableUris = new JSONArray() {{
            put(opportunityMetric.getUri());
        }};
        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri), drillableUris.toString());
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);
        assertFalse(kpiWidget.isDrillable(), "widget should not be drillable");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillOnWidgetHasAlert() throws IOException {
        final Pair<String, Metric> kpiUris = createKpiHasAlert();
        final String dashboardTitle = "eventing_widget_" + generateHashString();
        final String dashboardUri = createAnalyticalDashboardWithWidget(dashboardTitle, kpiUris.getLeft());
        initIndigoDashboardsPageWithWidgets().selectKpiDashboard(dashboardTitle);
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);
        kpiWidget.openAlertDialog().setThreshold("100000000").setAlert();
        JSONArray drillableUris = new JSONArray() {{
            put(opportunityMetric.getUri());
        }};
        final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardUri), drillableUris.toString());
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);
        assertFalse(kpiWidget.isDrillable(), "widget should not be drillable");
    }
}
