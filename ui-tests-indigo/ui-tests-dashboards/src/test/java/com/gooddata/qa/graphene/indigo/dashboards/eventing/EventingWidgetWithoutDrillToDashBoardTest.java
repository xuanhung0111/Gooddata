package com.gooddata.qa.graphene.indigo.dashboards.eventing;

import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardEventingTest;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CREATED;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class EventingWidgetWithoutDrillToDashBoardTest extends AbstractDashboardEventingTest {

    private String createdDatasetUri;

    @Override
    public void prepareProject() throws Throwable {
        super.prepareProject();
        createdDatasetUri = getDateDatasetUri(DATE_DATASET_CREATED);
    }

    @DataProvider(name = "noDataWidgetProvider")
    public Object[][] getNoDataWidgetProvider() throws Exception {
        final Pair<String, Metric> kpiUris = createNodataKpi();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);
        return new Object[][]{
                {dashboardId, kpiUris.getRight().getUri()},
                {dashboardId + "?preventDefault=true", kpiUris.getRight().getUri()},
                {dashboardId + "?preventDefault=false", kpiUris.getRight().getUri()},
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "noDataWidgetProvider")
    public void drillOnWidgetHasNoData(String dashboardId, String metricUri) throws Exception {
        final String file = createTemplateHtmlFile(dashboardId, buildEventingMesage(metricUri));
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);

        cleanUpLogger();
        kpiWidget.clickKpiValue();
        JSONObject jsonObject = getLatestPostMessageObj();
        JSONObject executionContext = jsonObject.getJSONObject("data").getJSONObject("executionContext");
        assertEquals(executionContext.getJSONArray("measures").length(), 1);
        JSONObject measure = executionContext.getJSONArray("measures").getJSONObject(0);
        assertEquals(measure.getJSONObject("definition").getJSONObject("measure").getJSONObject("item").getString("uri"),
                metricUri);
        JSONObject filter = executionContext.getJSONArray("filters").getJSONObject(0);
        assertEquals(filter.getJSONObject("relativeDateFilter").getJSONObject("dataSet").getString("uri"),
                createdDatasetUri);
        assertTrue(jsonObject.getJSONObject("data").get("drillTo").toString().equals("null"), "DrillTo should be null");
    }

    @DataProvider(name = "invalidDataWidgetProvider")
    public Object[][] getInvalidDataWidgetProvider() throws Exception {
        Pair<String, Metric> kpiUris = createInvalidKpi();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);
        return new Object[][]{
                {dashboardId, kpiUris.getRight().getUri()},
                {dashboardId + "?preventDefault=true", kpiUris.getRight().getUri()},
                {dashboardId + "?preventDefault=false", kpiUris.getRight().getUri()},
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "invalidDataWidgetProvider")
    public void drillOnWidgetInvalidData(String dashboardId, String metricUri) throws Exception {
        final String file = createTemplateHtmlFile(dashboardId, buildEventingMesage(metricUri));
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);

        cleanUpLogger();
        kpiWidget.clickKpiValue();
        JSONObject jsonObject = getLatestPostMessageObj();
        JSONObject executionContext = jsonObject.getJSONObject("data").getJSONObject("executionContext");
        assertEquals(executionContext.getJSONArray("measures").length(), 1);
        JSONObject measure = executionContext.getJSONArray("measures").getJSONObject(0);
        assertEquals(measure.getJSONObject("definition").getJSONObject("measure").getJSONObject("item").getString("uri"),
                metricUri);
        JSONObject filter = executionContext.getJSONArray("filters").getJSONObject(0);
        assertEquals(filter.getJSONObject("relativeDateFilter").getJSONObject("dataSet").getString("uri"),
                createdDatasetUri);
        assertTrue(jsonObject.getJSONObject("data").get("drillTo").toString().equals("null"), "DrillTo should be null");
    }

    @DataProvider(name = "applyAttributeDataWidgetProvider")
    public Object[][] getApplyAtributeDataWidgetProvider() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasAttributeOnMaql();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);
        return new Object[][]{
                {dashboardId, kpiUris.getRight().getUri()},
                {dashboardId + "?preventDefault=true", kpiUris.getRight().getUri()},
                {dashboardId + "?preventDefault=false", kpiUris.getRight().getUri()},
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "applyAttributeDataWidgetProvider")
    public void drillOnWidgetApplyAttribute(String dashboardId, String metricUri) throws Exception {
        final String file = createTemplateHtmlFile(dashboardId, buildEventingMesage(metricUri));
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);

        cleanUpLogger();
        kpiWidget.clickKpiValue();
        JSONObject jsonObject = getLatestPostMessageObj();
        JSONObject executionContext = jsonObject.getJSONObject("data").getJSONObject("executionContext");
        assertEquals(executionContext.getJSONArray("measures").length(), 1);
        JSONObject measure = executionContext.getJSONArray("measures").getJSONObject(0);
        assertEquals(measure.getJSONObject("definition").getJSONObject("measure").getJSONObject("item").getString("uri"),
                metricUri);
        JSONObject filter = executionContext.getJSONArray("filters").getJSONObject(0);
        assertEquals(filter.getJSONObject("relativeDateFilter").getJSONObject("dataSet").getString("uri"),
                createdDatasetUri);
        assertTrue(jsonObject.getJSONObject("data").get("drillTo").toString().equals("null"), "DrillTo should be null");
    }

    @DataProvider(name = "applyDateTimeWidgetProvider")
    public Object[][] getApplyDateTimeDataWidgetProvider() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasDate();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);
        return new Object[][]{
                {dashboardId, kpiUris.getRight().getUri()},
                {dashboardId + "?preventDefault=true", kpiUris.getRight().getUri()},
                {dashboardId + "?preventDefault=false", kpiUris.getRight().getUri()},
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "applyDateTimeWidgetProvider")
    public void drillOnWidgetApplyDateTime(String dashboardId, String metricUri) throws Exception {
        final String file = createTemplateHtmlFile(dashboardId, buildEventingMesage(metricUri));
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);

        cleanUpLogger();
        kpiWidget.clickKpiValue();
        JSONObject jsonObject = getLatestPostMessageObj();
        JSONObject executionContext = jsonObject.getJSONObject("data").getJSONObject("executionContext");
        assertEquals(executionContext.getJSONArray("measures").length(), 1);
        JSONObject measure = executionContext.getJSONArray("measures").getJSONObject(0);
        assertEquals(measure.getJSONObject("definition").getJSONObject("measure").getJSONObject("item").getString("uri"),
                metricUri);
        JSONObject filter = executionContext.getJSONArray("filters").getJSONObject(0);
        assertEquals(filter.getJSONObject("relativeDateFilter").getJSONObject("dataSet").getString("uri"),
                createdDatasetUri);
        assertTrue(jsonObject.getJSONObject("data").get("drillTo").toString().equals("null"), "DrillTo should be null");
    }

    @DataProvider(name = "applyVariableWidgetProvider")
    public Object[][] getApplyVariableTimeDataWidgetProvider() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasVarible();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);
        return new Object[][]{
                {dashboardId, kpiUris.getRight().getUri()},
                {dashboardId + "?preventDefault=true", kpiUris.getRight().getUri()},
                {dashboardId + "?preventDefault=false", kpiUris.getRight().getUri()},
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "applyVariableWidgetProvider")
    public void drillOnWidgetApplyVarible(String dashboardId, String metricUri) throws Exception {
        final String file = createTemplateHtmlFile(dashboardId, buildEventingMesage(metricUri));
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);

        cleanUpLogger();
        kpiWidget.clickKpiValue();
        JSONObject jsonObject = getLatestPostMessageObj();
        JSONObject executionContext = jsonObject.getJSONObject("data").getJSONObject("executionContext");
        assertEquals(executionContext.getJSONArray("measures").length(), 1);
        JSONObject measure = executionContext.getJSONArray("measures").getJSONObject(0);
        assertEquals(measure.getJSONObject("definition").getJSONObject("measure").getJSONObject("item").getString("uri"),
                metricUri);
        JSONObject filter = executionContext.getJSONArray("filters").getJSONObject(0);
        assertEquals(filter.getJSONObject("relativeDateFilter").getJSONObject("dataSet").getString("uri"),
                createdDatasetUri);
        assertTrue(jsonObject.getJSONObject("data").get("drillTo").toString().equals("null"), "DrillTo should be null");
    }

    @DataProvider(name = "hasNegativeWidgetProvider")
    public Object[][] getWidgetHasNegativeProvider() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasNegativeMetric();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);
        return new Object[][]{
                {dashboardId, kpiUris.getRight().getUri()},
                {dashboardId + "?preventDefault=true", kpiUris.getRight().getUri()},
                {dashboardId + "?preventDefault=false", kpiUris.getRight().getUri()},
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "hasNegativeWidgetProvider")
    public void drillOnWidgetHasNegative(String dashboardId, String metricUri) throws Exception {
        final String file = createTemplateHtmlFile(dashboardId, buildEventingMesage(metricUri));
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);

        cleanUpLogger();
        kpiWidget.clickKpiValue();
        JSONObject jsonObject = getLatestPostMessageObj();
        JSONObject executionContext = jsonObject.getJSONObject("data").getJSONObject("executionContext");
        assertEquals(executionContext.getJSONArray("measures").length(), 1);
        JSONObject measure = executionContext.getJSONArray("measures").getJSONObject(0);
        assertEquals(measure.getJSONObject("definition").getJSONObject("measure").getJSONObject("item").getString("uri"),
                metricUri);
        JSONObject filter = executionContext.getJSONArray("filters").getJSONObject(0);
        assertEquals(filter.getJSONObject("relativeDateFilter").getJSONObject("dataSet").getString("uri"),
                createdDatasetUri);
        assertTrue(jsonObject.getJSONObject("data").get("drillTo").toString().equals("null"), "DrillTo should be null");
    }

    @DataProvider(name = "hasZeroWidgetProvider")
    public Object[][] getWidgetHasZeroProvider() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasZeroMetric();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);
        return new Object[][]{
                {dashboardId, kpiUris.getRight().getUri()},
                {dashboardId + "?preventDefault=true", kpiUris.getRight().getUri()},
                {dashboardId + "?preventDefault=false", kpiUris.getRight().getUri()},
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "hasZeroWidgetProvider")
    public void drillOnWidgetHasZeroMetric(String dashboardId, String metricUri) throws Exception {
        final String file = createTemplateHtmlFile(dashboardId, buildEventingMesage(metricUri));
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);

        cleanUpLogger();
        kpiWidget.clickKpiValue();
        JSONObject jsonObject = getLatestPostMessageObj();
        JSONObject executionContext = jsonObject.getJSONObject("data").getJSONObject("executionContext");
        assertEquals(executionContext.getJSONArray("measures").length(), 1);
        JSONObject measure = executionContext.getJSONArray("measures").getJSONObject(0);
        assertEquals(measure.getJSONObject("definition").getJSONObject("measure").getJSONObject("item").getString("uri"),
                metricUri);
        JSONObject filter = executionContext.getJSONArray("filters").getJSONObject(0);
        assertEquals(filter.getJSONObject("relativeDateFilter").getJSONObject("dataSet").getString("uri"),
                createdDatasetUri);
        assertTrue(jsonObject.getJSONObject("data").get("drillTo").toString().equals("null"), "DrillTo should be null");
    }

    @DataProvider(name = "hasShareInWidgetProvider")
    public Object[][] getWidgetHasShareInProvider() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasMetricInShare();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);
        return new Object[][]{
                {dashboardId, kpiUris.getRight().getUri()},
                {dashboardId + "?preventDefault=true", kpiUris.getRight().getUri()},
                {dashboardId + "?preventDefault=false", kpiUris.getRight().getUri()},
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "hasShareInWidgetProvider")
    public void drillOnWidgetHasShareInMetric(String dashboardId, String metricUri) throws Exception {
        final String file = createTemplateHtmlFile(dashboardId, buildEventingMesage(metricUri));
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);

        cleanUpLogger();
        kpiWidget.clickKpiValue();
        JSONObject jsonObject = getLatestPostMessageObj();
        JSONObject executionContext = jsonObject.getJSONObject("data").getJSONObject("executionContext");
        assertEquals(executionContext.getJSONArray("measures").length(), 1);
        JSONObject measure = executionContext.getJSONArray("measures").getJSONObject(0);
        assertEquals(measure.getJSONObject("definition").getJSONObject("measure").getJSONObject("item").getString("uri"),
                metricUri);
        JSONObject filter = executionContext.getJSONArray("filters").getJSONObject(0);
        assertEquals(filter.getJSONObject("relativeDateFilter").getJSONObject("dataSet").getString("uri"),
                createdDatasetUri);
        assertTrue(jsonObject.getJSONObject("data").get("drillTo").toString().equals("null"), "DrillTo should be null");
    }

    @DataProvider(name = "hasShareInDifferentWidgetProvider")
    public Object[][] getWidgetHasShareInDiffProvider() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasMetricInDifference();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);
        return new Object[][]{
                {dashboardId, kpiUris.getRight().getUri()},
                {dashboardId + "?preventDefault=true", kpiUris.getRight().getUri()},
                {dashboardId + "?preventDefault=false", kpiUris.getRight().getUri()},
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "hasShareInDifferentWidgetProvider")
    public void drillOnWidgetHasShareInDifferent(String dashboardId, String metricUri) throws Exception {
        final String file = createTemplateHtmlFile(dashboardId, buildEventingMesage(metricUri));
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);

        cleanUpLogger();
        kpiWidget.clickKpiValue();
        JSONObject jsonObject = getLatestPostMessageObj();
        JSONObject executionContext = jsonObject.getJSONObject("data").getJSONObject("executionContext");
        assertEquals(executionContext.getJSONArray("measures").length(), 1);
        JSONObject measure = executionContext.getJSONArray("measures").getJSONObject(0);
        assertEquals(measure.getJSONObject("definition").getJSONObject("measure").getJSONObject("item").getString("uri"),
                metricUri);
        JSONObject filter = executionContext.getJSONArray("filters").getJSONObject(0);
        assertEquals(filter.getJSONObject("relativeDateFilter").getJSONObject("dataSet").getString("uri"),
                createdDatasetUri);
        assertTrue(jsonObject.getJSONObject("data").get("drillTo").toString().equals("null"), "DrillTo should be null");
    }

    @DataProvider(name = "hasShareInRatioWidgetProvider")
    public Object[][] getWidgetHasShareInRatioDiffProvider() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasMetricInRatio();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);
        return new Object[][]{
                {dashboardId, kpiUris.getRight().getUri()},
                {dashboardId + "?preventDefault=true", kpiUris.getRight().getUri()},
                {dashboardId + "?preventDefault=false", kpiUris.getRight().getUri()},
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "hasShareInRatioWidgetProvider")
    public void drillOnWidgetHasShareInRatio(String dashboardId, String metricUri) throws Exception {
        final String file = createTemplateHtmlFile(dashboardId, buildEventingMesage(metricUri));
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);

        cleanUpLogger();
        kpiWidget.clickKpiValue();
        JSONObject jsonObject = getLatestPostMessageObj();
        JSONObject executionContext = jsonObject.getJSONObject("data").getJSONObject("executionContext");
        assertEquals(executionContext.getJSONArray("measures").length(), 1);
        JSONObject measure = executionContext.getJSONArray("measures").getJSONObject(0);
        assertEquals(measure.getJSONObject("definition").getJSONObject("measure").getJSONObject("item").getString("uri"),
                metricUri);
        JSONObject filter = executionContext.getJSONArray("filters").getJSONObject(0);
        assertEquals(filter.getJSONObject("relativeDateFilter").getJSONObject("dataSet").getString("uri"), createdDatasetUri);
        assertTrue(jsonObject.getJSONObject("data").get("drillTo").toString().equals("null"), "DrillTo should be null");
    }

    @DataProvider(name = "noDateFilterWidgetProvider")
    public Object[][] getWidgetHasNoDateFilterProvider() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasNoDate();
        final String dashboardTitle = "eventing_widget_" + generateHashString();
        final String dashboardUri = createAnalyticalDashboardWithWidget(dashboardTitle, kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);

        return new Object[][]{
                {dashboardId, dashboardTitle, kpiUris.getRight().getUri()},
                {dashboardId + "?preventDefault=true", dashboardTitle, kpiUris.getRight().getUri()},
                {dashboardId + "?preventDefault=false", dashboardTitle, kpiUris.getRight().getUri()},
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "noDateFilterWidgetProvider")
    public void drillOnWidgetHasNoDateFilter(String dashboardId, String dashboardTitle, String metricUri)
            throws Exception {
        initIndigoDashboardsPageWithWidgets().selectKpiDashboard(dashboardTitle);
        indigoDashboardsPage.switchToEditMode().waitForWidgetsLoading().selectLastWidget(Kpi.class);
        if (indigoDashboardsPage.getConfigurationPanel().isDateFilterCheckboxEnabled()) {
            indigoDashboardsPage.getConfigurationPanel().disableDateFilter();
            indigoDashboardsPage.saveEditModeWithWidgets().waitForWidgetsLoading();
        }

        final String file = createTemplateHtmlFile(dashboardId, buildEventingMesage(metricUri));
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);

        cleanUpLogger();
        kpiWidget.clickKpiValue();
        JSONObject jsonObject = getLatestPostMessageObj();
        JSONObject executionContext = jsonObject.getJSONObject("data").getJSONObject("executionContext");
        assertEquals(executionContext.getJSONArray("measures").length(), 1);
        JSONObject measure = executionContext.getJSONArray("measures").getJSONObject(0);
        assertEquals(measure.getJSONObject("definition").getJSONObject("measure").getJSONObject("item").getString("uri"),
                metricUri);
        assertEquals(executionContext.getJSONArray("filters").length(), 0);
        assertTrue(jsonObject.getJSONObject("data").get("drillTo").toString().equals("null"), "DrillTo should be null");
    }

    @DataProvider(name = "hasAlertWidgetProvider")
    public Object[][] getWidgetHasAlertProvider() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasAlert();
        final String dashboardTitle = "eventing_widget_" + generateHashString();
        final String dashboardUri = createAnalyticalDashboardWithWidget(dashboardTitle, kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);
        return new Object[][]{
                {dashboardId, dashboardTitle, kpiUris.getRight().getUri()},
                {dashboardId + "?preventDefault=true", dashboardTitle, kpiUris.getRight().getUri()},
                {dashboardId + "?preventDefault=false", dashboardTitle, kpiUris.getRight().getUri()},
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "hasAlertWidgetProvider")
    public void drillOnWidgetHasAlert(String dashboardId, String dashboardTitle, String metricUri)
            throws Exception {
        initIndigoDashboardsPageWithWidgets().selectKpiDashboard(dashboardTitle);
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);
        kpiWidget.openAlertDialog().setThreshold("100000000").setAlert();
        final String file = createTemplateHtmlFile(dashboardId, buildEventingMesage(metricUri));
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);

        cleanUpLogger();
        kpiWidget.clickKpiValue();
        JSONObject jsonObject = getLatestPostMessageObj();
        JSONObject executionContext = jsonObject.getJSONObject("data").getJSONObject("executionContext");
        assertEquals(executionContext.getJSONArray("measures").length(), 1);
        JSONObject measure = executionContext.getJSONArray("measures").getJSONObject(0);
        assertEquals(measure.getJSONObject("definition").getJSONObject("measure").getJSONObject("item").getString("uri"),
                metricUri);
        JSONObject filter = executionContext.getJSONArray("filters").getJSONObject(0);
        assertEquals(filter.getJSONObject("relativeDateFilter").getJSONObject("dataSet").getString("uri"),
                createdDatasetUri);
        assertTrue(jsonObject.getJSONObject("data").get("drillTo").toString().equals("null"), "DrillTo should be null");
    }

    private String buildEventingMesage(String obj) {
        return new JSONArray() {{
            put(obj);
        }}.toString();
    }
}
