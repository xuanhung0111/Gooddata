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
import org.testng.annotations.Test;

import java.io.IOException;
import static org.testng.Assert.assertTrue;

public class EventingWidgetEditModeTest extends AbstractDashboardEventingTest {

    private String oldDashboardUri;
    private String firstTabIdentifier;

    @Override
    public void prepareProject() throws Throwable {
        super.prepareProject();
        String dashboardName = "eventing_test_old_dashboard_" + generateHashString();
        Dashboard oldDashboard = new Dashboard().setName(dashboardName).addTab(new Tab());
        DashboardRestRequest dashboardRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
        oldDashboardUri = dashboardRequest.createDashboard(oldDashboard.getMdObject());
        firstTabIdentifier = dashboardRequest.getTabId(dashboardName, "First Tab");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillOnWidgetHasNoData() throws IOException {
        final Pair<String, Metric> kpiUris = createNodataKpi(oldDashboardUri, firstTabIdentifier);
        final String metricUri = kpiUris.getRight().getUri();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri) + "?preventDefault=true";
        final String file = createTemplateHtmlFile(dashboardId, buildEventingMesage(metricUri));
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        indigoDashboardsPage.switchToEditMode();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);

        cleanUpLogger();
        kpiWidget.clickKpiValue();
        assertTrue(getLoggerContent().isEmpty(), "eventing message in edit mode should be empty");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillOnWidgetInvalidData() throws IOException {
        Pair<String, Metric> kpiUris = createInvalidKpi();
        final String metricUri = kpiUris.getRight().getUri();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri) + "?preventDefault=true";
        final String file = createTemplateHtmlFile(dashboardId, buildEventingMesage(metricUri));
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        indigoDashboardsPage.switchToEditMode();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);

        cleanUpLogger();
        kpiWidget.clickKpiValue();
        assertTrue(getLoggerContent().isEmpty(), "eventing message in edit mode should be empty");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillOnWidgetApplyAttribute() throws IOException {
        final Pair<String, Metric> kpiUris = createKpiHasAttributeOnMaql(oldDashboardUri, firstTabIdentifier);
        final String metricUri = kpiUris.getRight().getUri();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri) + "?preventDefault=true";

        final String file = createTemplateHtmlFile(dashboardId, buildEventingMesage(metricUri));
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        indigoDashboardsPage.switchToEditMode();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);

        cleanUpLogger();
        kpiWidget.clickKpiValue();
        assertTrue(getLoggerContent().isEmpty(), "eventing message in edit mode should be empty");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillOnWidgetApplyDateTime() throws IOException {
        final Pair<String, Metric> kpiUris = createKpiHasDate();
        final String metricUri = kpiUris.getRight().getUri();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri) + "?preventDefault=true";
        final String file = createTemplateHtmlFile(dashboardId, buildEventingMesage(metricUri));
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        indigoDashboardsPage.switchToEditMode();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);

        cleanUpLogger();
        kpiWidget.clickKpiValue();
        assertTrue(getLoggerContent().isEmpty(), "eventing message in edit mode should be empty");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillOnWidgetApplyVarible() throws IOException {
        final Pair<String, Metric> kpiUris = createKpiHasVarible();
        final String metricUri = kpiUris.getRight().getUri();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri) + "?preventDefault=true";
        final String file = createTemplateHtmlFile(dashboardId, buildEventingMesage(metricUri));
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        indigoDashboardsPage.switchToEditMode();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);

        cleanUpLogger();
        kpiWidget.clickKpiValue();
        assertTrue(getLoggerContent().isEmpty(), "eventing message in edit mode should be empty");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillOnWidgetHasNegative() throws IOException {
        final Pair<String, Metric> kpiUris = createKpiHasNegativeMetric(oldDashboardUri, firstTabIdentifier);
        final String metricUri = kpiUris.getRight().getUri();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri) + "?preventDefault=true";
        final String file = createTemplateHtmlFile(dashboardId, buildEventingMesage(metricUri));
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        indigoDashboardsPage.switchToEditMode();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);

        cleanUpLogger();
        kpiWidget.clickKpiValue();
        assertTrue(getLoggerContent().isEmpty(), "eventing message in edit mode should be empty");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillOnWidgetHasZeroMetric() throws IOException {
        final Pair<String, Metric> kpiUris = createKpiHasZeroMetric();
        final String metricUri = kpiUris.getRight().getUri();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri) + "?preventDefault=true";
        final String file = createTemplateHtmlFile(dashboardId, buildEventingMesage(metricUri));
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        indigoDashboardsPage.switchToEditMode();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);

        cleanUpLogger();
        kpiWidget.clickKpiValue();
        assertTrue(getLoggerContent().isEmpty(), "eventing message in edit mode should be empty");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillOnWidgetHasShareInMetric() throws IOException {
        final Pair<String, Metric> kpiUris = createKpiHasMetricInShare(oldDashboardUri, firstTabIdentifier);
        final String metricUri = kpiUris.getRight().getUri();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri) + "?preventDefault=true";
        final String file = createTemplateHtmlFile(dashboardId, buildEventingMesage(metricUri));
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        indigoDashboardsPage.switchToEditMode();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);

        cleanUpLogger();
        kpiWidget.clickKpiValue();
        assertTrue(getLoggerContent().isEmpty(), "eventing message in edit mode should be empty");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillOnWidgetHasShareInDifferent() throws IOException {
        final Pair<String, Metric> kpiUris = createKpiHasMetricInDifference();
        final String metricUri = kpiUris.getRight().getUri();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri) + "?preventDefault=true";
        final String file = createTemplateHtmlFile(dashboardId, buildEventingMesage(metricUri));
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        indigoDashboardsPage.switchToEditMode();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);

        cleanUpLogger();
        kpiWidget.clickKpiValue();
        assertTrue(getLoggerContent().isEmpty(), "eventing message in edit mode should be empty");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillOnWidgetHasShareInRatio() throws IOException {
        final Pair<String, Metric> kpiUris = createKpiHasMetricInRatio();
        final String metricUri = kpiUris.getRight().getUri();
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);

        final String file = createTemplateHtmlFile(dashboardId, buildEventingMesage(metricUri));
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        indigoDashboardsPage.switchToEditMode();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);

        cleanUpLogger();
        kpiWidget.clickKpiValue();
        assertTrue(getLoggerContent().isEmpty(), "eventing message in edit mode should be empty");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillOnWidgetHasNoDateFilter() throws IOException {
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
        indigoDashboardsPage.switchToEditMode();
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);

        cleanUpLogger();
        kpiWidget.clickKpiValue();
        assertTrue(getLoggerContent().isEmpty(), "eventing message in edit mode should be empty");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillOnWidgetHasAlert() throws IOException {
        final Pair<String, Metric> kpiUris = createKpiHasAlert();
        final String metricUri = kpiUris.getRight().getUri();
        final String dashboardTitle = "eventing_widget_" + generateHashString();
        final String dashboardUri = createAnalyticalDashboardWithWidget(dashboardTitle, kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri) + "?preventDefault=true";
        initIndigoDashboardsPageWithWidgets().selectKpiDashboard(dashboardTitle);
        Kpi kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);
        kpiWidget.openAlertDialog().setThreshold("100000000").setAlert();
        final String file = createTemplateHtmlFile(dashboardId, buildEventingMesage(metricUri));
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();
        indigoDashboardsPage.switchToEditMode();
        kpiWidget = indigoDashboardsPage.getLastWidget(Kpi.class);

        cleanUpLogger();
        kpiWidget.clickKpiValue();
        assertTrue(getLoggerContent().isEmpty(), "eventing message in edit mode should be empty");
    }

    private String buildEventingMesage(String metric) {
        return new JSONArray() {{
            put(metric);
            put(oldDashboardUri);
        }}.toString();
    }
}
