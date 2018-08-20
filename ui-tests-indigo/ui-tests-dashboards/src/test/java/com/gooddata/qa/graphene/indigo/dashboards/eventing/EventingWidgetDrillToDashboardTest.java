package com.gooddata.qa.graphene.indigo.dashboards.eventing;

import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardEventingTest;
import com.gooddata.qa.mdObjects.dashboard.Dashboard;
import com.gooddata.qa.mdObjects.dashboard.tab.Tab;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CREATED;
import static org.testng.Assert.assertEquals;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;

public class EventingWidgetDrillToDashboardTest extends AbstractDashboardEventingTest {

    private String oldDashboardUri;
    private String firstTabIdentifier;
    private String createdDatasetUri;
    private String expectedDrillToUri;

    @Override
    public void prepareProject() throws Throwable {
        super.prepareProject();
        String dashboardName = "eventing_test_old_dashboard_" + generateHashString();
        Dashboard oldDashboard = new Dashboard().setName(dashboardName).addTab(new Tab());
        DashboardRestRequest dashboardRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
        oldDashboardUri = dashboardRequest.createDashboard(oldDashboard.getMdObject());
        firstTabIdentifier = dashboardRequest.getTabId(dashboardName, "First Tab");
        createdDatasetUri = getDateDatasetUri(DATE_DATASET_CREATED);
        expectedDrillToUri = String.format("/dashboard.html#project=/gdc/projects/%s&dashboard=%s&tab=%s",
                testParams.getProjectId(), oldDashboardUri, firstTabIdentifier);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillOnWidgetHasNoData() throws Exception {
        final Pair<String, Metric> kpiUris = createNodataKpi(oldDashboardUri, firstTabIdentifier);
        final String metricUri = kpiUris.getRight().getUri();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri) + "?preventDefault=true";
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
        assertEquals(jsonObject.getJSONObject("data").getJSONObject("drillTo").getString("uri"), expectedDrillToUri);
        waitForFragmentVisible(indigoDashboardsPage);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillOnWidgetInvalidData() throws Exception {
        Pair<String, Metric> kpiUris = createInvalidKpi(oldDashboardUri, firstTabIdentifier);
        final String metricUri = kpiUris.getRight().getUri();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri) + "?preventDefault=true";
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
        assertEquals(jsonObject.getJSONObject("data").getJSONObject("drillTo").getString("uri"), expectedDrillToUri);
        waitForFragmentVisible(indigoDashboardsPage);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillOnWidgetApplyAttribute() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasAttributeOnMaql(oldDashboardUri, firstTabIdentifier);
        final String metricUri = kpiUris.getRight().getUri();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri) + "?preventDefault=true";

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
        assertEquals(jsonObject.getJSONObject("data").getJSONObject("drillTo").getString("uri"), expectedDrillToUri);
        waitForFragmentVisible(indigoDashboardsPage);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillOnWidgetApplyDateTime() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasDate(oldDashboardUri, firstTabIdentifier);
        final String metricUri = kpiUris.getRight().getUri();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri) + "?preventDefault=true";
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
        assertEquals(jsonObject.getJSONObject("data").getJSONObject("drillTo").getString("uri"), expectedDrillToUri);
        waitForFragmentVisible(indigoDashboardsPage);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillOnWidgetApplyVarible() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasVarible(oldDashboardUri, firstTabIdentifier);
        final String metricUri = kpiUris.getRight().getUri();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri) + "?preventDefault=true";
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
        assertEquals(jsonObject.getJSONObject("data").getJSONObject("drillTo").getString("uri"), expectedDrillToUri);
        waitForFragmentVisible(indigoDashboardsPage);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillOnWidgetHasNegative() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasNegativeMetric(oldDashboardUri, firstTabIdentifier);
        final String metricUri = kpiUris.getRight().getUri();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri) + "?preventDefault=true";
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
        assertEquals(jsonObject.getJSONObject("data").getJSONObject("drillTo").getString("uri"), expectedDrillToUri);
        waitForFragmentVisible(indigoDashboardsPage);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillOnWidgetHasZeroMetric() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasZeroMetric(oldDashboardUri, firstTabIdentifier);
        final String metricUri = kpiUris.getRight().getUri();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri) + "?preventDefault=true";
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
        assertEquals(jsonObject.getJSONObject("data").getJSONObject("drillTo").getString("uri"), expectedDrillToUri);
        waitForFragmentVisible(indigoDashboardsPage);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillOnWidgetHasShareInMetric() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasMetricInShare(oldDashboardUri, firstTabIdentifier);
        final String metricUri = kpiUris.getRight().getUri();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri) + "?preventDefault=true";
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
        assertEquals(jsonObject.getJSONObject("data").getJSONObject("drillTo").getString("uri"), expectedDrillToUri);
        waitForFragmentVisible(indigoDashboardsPage);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillOnWidgetHasShareInDifferent() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasMetricInDifference(oldDashboardUri, firstTabIdentifier);
        final String metricUri = kpiUris.getRight().getUri();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri) + "?preventDefault=true";
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
        assertEquals(jsonObject.getJSONObject("data").getJSONObject("drillTo").getString("uri"), expectedDrillToUri);
        waitForFragmentVisible(indigoDashboardsPage);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillOnWidgetHasShareInRatio() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasMetricInRatio(oldDashboardUri, firstTabIdentifier);
        final String metricUri = kpiUris.getRight().getUri();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri) + "?preventDefault=true";

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
        assertEquals(jsonObject.getJSONObject("data").getJSONObject("drillTo").getString("uri"), expectedDrillToUri);
        waitForFragmentVisible(indigoDashboardsPage);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillOnWidgetHasNoDateFilter() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasNoDate(oldDashboardUri, firstTabIdentifier);
        final String metricUri = kpiUris.getRight().getUri();
        final String dashboardTitle = "eventing_widget_" + generateHashString();
        final String dashboardUri = createAnalyticalDashboardWithWidget(dashboardTitle, kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri) + "?preventDefault=true";

        initIndigoDashboardsPageWithWidgets().selectKpiDashboard(dashboardTitle);
        indigoDashboardsPage.switchToEditMode().waitForWidgetsLoading().selectLastWidget(Kpi.class);
        indigoDashboardsPage.getConfigurationPanel().disableDateFilter();
        indigoDashboardsPage.saveEditModeWithWidgets().waitForWidgetsLoading();
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
        assertEquals(jsonObject.getJSONObject("data").getJSONObject("drillTo").getString("uri"), expectedDrillToUri);
        waitForFragmentVisible(indigoDashboardsPage);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillOnWidgetHasAlert() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasAlert(oldDashboardUri, firstTabIdentifier);
        final String metricUri = kpiUris.getRight().getUri();
        final String dashboardTitle = "eventing_widget_" + generateHashString();
        final String dashboardUri = createAnalyticalDashboardWithWidget(dashboardTitle, kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri) + "?preventDefault=true";
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
        assertEquals(jsonObject.getJSONObject("data").getJSONObject("drillTo").getString("uri"), expectedDrillToUri);
        waitForFragmentVisible(indigoDashboardsPage);
    }

    private String buildEventingMesage(String metric) {
        return new JSONArray() {{
            put(metric);
            put(oldDashboardUri);
        }}.toString();
    }
}
