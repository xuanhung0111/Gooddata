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

public class VisualizationDrillableWidgetTest extends AbstractDashboardEventingTest {

    @DataProvider(name = "noDataWidgetProvider")
    public Object[][] getNoDataWidgetProvider() throws Exception {
        final Pair<String, Metric> kpiUris = createNodataKpi();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);
        final String uris = buildDrillableMesage(kpiUris.getRight().getUri());
        final String identifiers = buildDrillableMesage(kpiUris.getRight().getIdentifier());
        return new Object[][]{
                {dashboardId, uris, "[]"},
                {dashboardId, "[]", identifiers},
                {dashboardId, uris, identifiers}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "noDataWidgetProvider")
    public void drillOnWidgetHasNoData(String dashboardId, String uris, String identifiers) throws Exception {
        final String file = createTemplateHtmlFile(dashboardId, uris, identifiers);
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);
        assertTrue(kpiWidget.isDrillable(), "widget should be drillable");
    }

    @DataProvider(name = "invalidDataWidgetProvider")
    public Object[][] getInvalidDataWidgetProvider() throws Exception {
        Pair<String, Metric> kpiUris = createInvalidKpi();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);
        final String uris = buildDrillableMesage(kpiUris.getRight().getUri());
        final String identifiers = buildDrillableMesage(kpiUris.getRight().getIdentifier());
        return new Object[][]{
                {dashboardId, uris, "[]"},
                {dashboardId, "[]", identifiers},
                {dashboardId, uris, identifiers}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "invalidDataWidgetProvider")
    public void drillOnWidgetInvalidData(String dashboardId, String uris, String identifiers) throws Exception {
        final String file = createTemplateHtmlFile(dashboardId, uris, identifiers);
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);
        assertTrue(kpiWidget.isDrillable(), "widget should be drillable");
    }

    @DataProvider(name = "applyAttributeDataWidgetProvider")
    public Object[][] getApplyAtributeDataWidgetProvider() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasAttributeOnMaql();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);
        final String uris = buildDrillableMesage(kpiUris.getRight().getUri());
        final String identifiers = buildDrillableMesage(kpiUris.getRight().getIdentifier());
        return new Object[][]{
                {dashboardId, uris, "[]"},
                {dashboardId, "[]", identifiers},
                {dashboardId, uris, identifiers}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "applyAttributeDataWidgetProvider")
    public void drillOnWidgetApplyAttribute(String dashboardId, String uris, String identifiers) throws Exception {
        final String file = createTemplateHtmlFile(dashboardId, uris, identifiers);
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);
        assertTrue(kpiWidget.isDrillable(), "widget should drillable");
    }

    @DataProvider(name = "applyDateTimeWidgetProvider")
    public Object[][] getApplyDateTimeDataWidgetProvider() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasDate();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);
        final String uris = buildDrillableMesage(kpiUris.getRight().getUri());
        final String identifiers = buildDrillableMesage(kpiUris.getRight().getIdentifier());
        return new Object[][]{
                {dashboardId, uris, "[]"},
                {dashboardId, "[]", identifiers},
                {dashboardId, uris, identifiers}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "applyDateTimeWidgetProvider")
    public void drillOnWidgetApplyDateTime(String dashboardId, String uris, String identifiers) throws Exception {
        final String file = createTemplateHtmlFile(dashboardId, uris, identifiers);
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);
        assertTrue(kpiWidget.isDrillable(), "widget should be drillable");
    }

    @DataProvider(name = "applyVariableWidgetProvider")
    public Object[][] getApplyVariableTimeDataWidgetProvider() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasVarible();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);
        final String uris = buildDrillableMesage(kpiUris.getRight().getUri());
        final String identifiers = buildDrillableMesage(kpiUris.getRight().getIdentifier());
        return new Object[][]{
                {dashboardId, uris, "[]"},
                {dashboardId, "[]", identifiers},
                {dashboardId, uris, identifiers}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "applyVariableWidgetProvider")
    public void drillOnWidgetApplyVarible(String dashboardId, String uris, String identifiers) throws Exception {
        final String file = createTemplateHtmlFile(dashboardId, uris, identifiers);
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);
        assertTrue(kpiWidget.isDrillable(), "widget should be drillable");
    }

    @DataProvider(name = "hasNegativeWidgetProvider")
    public Object[][] getWidgetHasNegativeProvider() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasNegativeMetric();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);
        final String uris = buildDrillableMesage(kpiUris.getRight().getUri());
        final String identifiers = buildDrillableMesage(kpiUris.getRight().getIdentifier());
        return new Object[][]{
                {dashboardId, uris, "[]"},
                {dashboardId, "[]", identifiers},
                {dashboardId, uris, identifiers}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "hasNegativeWidgetProvider")
    public void drillOnWidgetHasNegative(String dashboardId, String uris, String identifiers) throws Exception {
        final String file = createTemplateHtmlFile(dashboardId, uris, identifiers);
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);
        assertTrue(kpiWidget.isDrillable(), "widget should be drillable");
    }

    @DataProvider(name = "hasZeroWidgetProvider")
    public Object[][] getWidgetHasZeroProvider() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasZeroMetric();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);
        final String uris = buildDrillableMesage(kpiUris.getRight().getUri());
        final String identifiers = buildDrillableMesage(kpiUris.getRight().getIdentifier());
        return new Object[][]{
                {dashboardId, uris, "[]"},
                {dashboardId, "[]", identifiers},
                {dashboardId, uris, identifiers}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "hasZeroWidgetProvider")
    public void drillOnWidgetHasZeroMetric(String dashboardId, String uris, String identifiers) throws Exception {
        final String file = createTemplateHtmlFile(dashboardId, uris, identifiers);
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);
        assertTrue(kpiWidget.isDrillable(), "widget should be drillable");
    }

    @DataProvider(name = "hasShareInWidgetProvider")
    public Object[][] getWidgetHasShareInProvider() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasMetricInShare();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);
        final String uris = buildDrillableMesage(kpiUris.getRight().getUri());
        final String identifiers = buildDrillableMesage(kpiUris.getRight().getIdentifier());
        return new Object[][]{
                {dashboardId, uris, "[]"},
                {dashboardId, "[]", identifiers},
                {dashboardId, uris, identifiers}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "hasShareInWidgetProvider")
    public void drillOnWidgetHasShareInMetric(String dashboardId, String uris, String identifiers) throws Exception {
        final String file = createTemplateHtmlFile(dashboardId, uris, identifiers);
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);
        assertTrue(kpiWidget.isDrillable(), "widget should be drillable");
    }

    @DataProvider(name = "hasShareInDifferentWidgetProvider")
    public Object[][] getWidgetHasShareInDiffProvider() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasMetricInDifference();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);
        final String uris = buildDrillableMesage(kpiUris.getRight().getUri());
        final String identifiers = buildDrillableMesage(kpiUris.getRight().getIdentifier());
        return new Object[][]{
                {dashboardId, uris, "[]"},
                {dashboardId, "[]", identifiers},
                {dashboardId, uris, identifiers}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "hasShareInDifferentWidgetProvider")
    public void drillOnWidgetHasShareInDifferent(String dashboardId, String uris, String identifiers) throws Exception {
        final String file = createTemplateHtmlFile(dashboardId, uris, identifiers);
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);
        assertTrue(kpiWidget.isDrillable(), "widget should be drillable");
    }

    @DataProvider(name = "hasShareInRatioWidgetProvider")
    public Object[][] getWidgetHasShareInRatioDiffProvider() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasMetricInRatio();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);
        final String uris = buildDrillableMesage(kpiUris.getRight().getUri());
        final String identifiers = buildDrillableMesage(kpiUris.getRight().getIdentifier());
        return new Object[][]{
                {dashboardId, uris, "[]"},
                {dashboardId, "[]", identifiers},
                {dashboardId, uris, identifiers}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "hasShareInRatioWidgetProvider")
    public void drillOnWidgetHasShareInRatio(String dashboardId, String uris, String identifiers) throws Exception {
        final String file = createTemplateHtmlFile(dashboardId, uris, identifiers);
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);
        assertTrue(kpiWidget.isDrillable(), "widget should be drillable");
    }

    @DataProvider(name = "noDateFilterWidgetProvider")
    public Object[][] getWidgetHasNoDateFilterProvider() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasNoDate();
        final String dashboardTitle = "eventing_widget_" + generateHashString();
        final String dashboardUri = createAnalyticalDashboardWithWidget(dashboardTitle, kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);
        final String uris = buildDrillableMesage(kpiUris.getRight().getUri());
        final String identifiers = buildDrillableMesage(kpiUris.getRight().getIdentifier());

        return new Object[][]{
                {dashboardId, dashboardTitle, uris, "[]"},
                {dashboardId, dashboardTitle, "[]", identifiers},
                {dashboardId, dashboardTitle, uris, identifiers}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "noDateFilterWidgetProvider")
    public void drillOnWidgetHasNoDateFilter(String dashboardId, String dashboardTitle, String uris, String identifiers)
            throws Exception {
        initIndigoDashboardsPageWithWidgets().selectKpiDashboard(dashboardTitle);
        indigoDashboardsPage.switchToEditMode().waitForWidgetsLoading().selectLastWidget(Kpi.class);
        if (indigoDashboardsPage.getConfigurationPanel().isDateFilterCheckboxEnabled()) {
            indigoDashboardsPage.getConfigurationPanel().disableDateFilter();
            indigoDashboardsPage.saveEditModeWithWidgets().waitForWidgetsLoading();
        }

        final String file = createTemplateHtmlFile(dashboardId, uris, identifiers);
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);
        assertTrue(kpiWidget.isDrillable(), "widget should be drillable");
    }

    @DataProvider(name = "hasAlertWidgetProvider")
    public Object[][] getWidgetHasAlertProvider() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasAlert();
        final String dashboardTitle = "eventing_widget_" + generateHashString();
        final String dashboardUri = createAnalyticalDashboardWithWidget(dashboardTitle, kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);
        final String uris = buildDrillableMesage(kpiUris.getRight().getUri());
        final String identifiers = buildDrillableMesage(kpiUris.getRight().getIdentifier());
        return new Object[][]{
                {dashboardId, dashboardTitle, uris, "[]"},
                {dashboardId, dashboardTitle, "[]", identifiers},
                {dashboardId, dashboardTitle, uris, identifiers}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "hasAlertWidgetProvider")
    public void drillOnWidgetHasAlert(String dashboardId, String dashboardTitle, String uris, String identifiers)
            throws Exception {
        initIndigoDashboardsPageWithWidgets().selectKpiDashboard(dashboardTitle);
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);
        kpiWidget.openAlertDialog().setThreshold("100000000").setAlert();
        final String file = createTemplateHtmlFile(dashboardId, uris, identifiers);
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);
        assertTrue(kpiWidget.isDrillable(), "widget should be drillable");
    }

    private String buildDrillableMesage(String obj) {
        return new JSONArray() {{
            put(obj);
        }}.toString();
    }
}
