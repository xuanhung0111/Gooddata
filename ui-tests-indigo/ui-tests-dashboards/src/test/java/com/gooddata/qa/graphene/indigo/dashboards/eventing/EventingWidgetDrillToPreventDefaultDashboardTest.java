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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;

public class EventingWidgetDrillToPreventDefaultDashboardTest extends AbstractDashboardEventingTest {

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

    @DataProvider(name = "noDataWidgetProvider")
    public Object[][] getNoDataWidgetProvider() throws Exception {
        final Pair<String, Metric> kpiUris = createNodataKpi(oldDashboardUri, firstTabIdentifier);
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);
        return new Object[][]{
                {dashboardId, kpiUris.getRight().getUri()},
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
        assertTrue(getLoggerContent().isEmpty(), "Eventing on preventDefault=false should return empty message");
        waitForFragmentNotVisible(indigoDashboardsPage);
        waitForDashboardPageLoaded(browser);
    }

    @DataProvider(name = "invalidDataWidgetProvider")
    public Object[][] getInvalidDataWidgetProvider() throws Exception {
        Pair<String, Metric> kpiUris = createInvalidKpi(oldDashboardUri, firstTabIdentifier);
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);
        return new Object[][]{
                {dashboardId, kpiUris.getRight().getUri()},
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
        assertTrue(getLoggerContent().isEmpty(), "Eventing on preventDefault=false should return empty message");
        waitForFragmentNotVisible(indigoDashboardsPage);
        waitForDashboardPageLoaded(browser);
    }

    @DataProvider(name = "applyAttributeDataWidgetProvider")
    public Object[][] getApplyAtributeDataWidgetProvider() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasAttributeOnMaql(oldDashboardUri, firstTabIdentifier);
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);
        return new Object[][]{
                {dashboardId, kpiUris.getRight().getUri()},
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
        assertTrue(getLoggerContent().isEmpty(), "Eventing on preventDefault=false should return empty message");
        waitForFragmentNotVisible(indigoDashboardsPage);
        waitForDashboardPageLoaded(browser);
    }

    @DataProvider(name = "applyDateTimeWidgetProvider")
    public Object[][] getApplyDateTimeDataWidgetProvider() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasDate(oldDashboardUri, firstTabIdentifier);
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);
        return new Object[][]{
                {dashboardId, kpiUris.getRight().getUri()},
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
        assertTrue(getLoggerContent().isEmpty(), "Eventing on preventDefault=false should return empty message");
        waitForFragmentNotVisible(indigoDashboardsPage);
        waitForDashboardPageLoaded(browser);
    }

    @DataProvider(name = "applyVariableWidgetProvider")
    public Object[][] getApplyVariableTimeDataWidgetProvider() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasVarible(oldDashboardUri, firstTabIdentifier);
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);
        return new Object[][]{
                {dashboardId, kpiUris.getRight().getUri()},
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
        assertTrue(getLoggerContent().isEmpty(), "Eventing on preventDefault=false should return empty message");
        waitForFragmentNotVisible(indigoDashboardsPage);
        waitForDashboardPageLoaded(browser);
    }

    @DataProvider(name = "hasNegativeWidgetProvider")
    public Object[][] getWidgetHasNegativeProvider() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasNegativeMetric(oldDashboardUri, firstTabIdentifier);
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);
        return new Object[][]{
                {dashboardId, kpiUris.getRight().getUri()},
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
        assertTrue(getLoggerContent().isEmpty(), "Eventing on preventDefault=false should return empty message");
        waitForFragmentNotVisible(indigoDashboardsPage);
        waitForDashboardPageLoaded(browser);
    }

    @DataProvider(name = "hasZeroWidgetProvider")
    public Object[][] getWidgetHasZeroProvider() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasZeroMetric(oldDashboardUri, firstTabIdentifier);
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);
        return new Object[][]{
                {dashboardId, kpiUris.getRight().getUri()},
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
        assertTrue(getLoggerContent().isEmpty(), "Eventing on preventDefault=false should return empty message");
        waitForFragmentNotVisible(indigoDashboardsPage);
        waitForDashboardPageLoaded(browser);
    }

    @DataProvider(name = "hasShareInWidgetProvider")
    public Object[][] getWidgetHasShareInProvider() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasMetricInShare(oldDashboardUri, firstTabIdentifier);
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);
        return new Object[][]{
                {dashboardId, kpiUris.getRight().getUri()},
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
        assertTrue(getLoggerContent().isEmpty(), "Eventing on preventDefault=false should return empty message");
        waitForFragmentNotVisible(indigoDashboardsPage);
        waitForDashboardPageLoaded(browser);
    }

    @DataProvider(name = "hasShareInDifferentWidgetProvider")
    public Object[][] getWidgetHasShareInDiffProvider() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasMetricInDifference(oldDashboardUri, firstTabIdentifier);
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);
        return new Object[][]{
                {dashboardId, kpiUris.getRight().getUri()},
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
        assertTrue(getLoggerContent().isEmpty(), "Eventing on preventDefault=false should return empty message");
        waitForFragmentNotVisible(indigoDashboardsPage);
        waitForDashboardPageLoaded(browser);
    }

    @DataProvider(name = "hasShareInRatioWidgetProvider")
    public Object[][] getWidgetHasShareInRatioDiffProvider() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasMetricInRatio(oldDashboardUri, firstTabIdentifier);
        final String dashboardUri = createAnalyticalDashboardWithWidget("eventing_widget_" + generateHashString(),
                kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);
        return new Object[][]{
                {dashboardId, kpiUris.getRight().getUri()},
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
        assertTrue(getLoggerContent().isEmpty(), "Eventing on preventDefault=false should return empty message");
        waitForFragmentNotVisible(indigoDashboardsPage);
        waitForDashboardPageLoaded(browser);
    }

    @DataProvider(name = "noDateFilterWidgetProvider")
    public Object[][] getWidgetHasNoDateFilterProvider() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasNoDate(oldDashboardUri, firstTabIdentifier);
        final String dashboardTitle = "eventing_widget_" + generateHashString();
        final String dashboardUri = createAnalyticalDashboardWithWidget(dashboardTitle, kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);

        return new Object[][]{
                {dashboardId, dashboardTitle, kpiUris.getRight().getUri()},
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
        assertTrue(getLoggerContent().isEmpty(), "Eventing on preventDefault=false should return empty message");
        waitForFragmentNotVisible(indigoDashboardsPage);
        waitForDashboardPageLoaded(browser);
    }

    @DataProvider(name = "hasAlertWidgetProvider")
    public Object[][] getWidgetHasAlertProvider() throws Exception {
        final Pair<String, Metric> kpiUris = createKpiHasAlert(oldDashboardUri, firstTabIdentifier);
        final String dashboardTitle = "eventing_widget_" + generateHashString();
        final String dashboardUri = createAnalyticalDashboardWithWidget(dashboardTitle, kpiUris.getLeft());
        final String dashboardId = getObjectIdFromUri(dashboardUri);
        return new Object[][]{
                {dashboardId, dashboardTitle, kpiUris.getRight().getUri()},
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
        assertTrue(getLoggerContent().isEmpty(), "Eventing on preventDefault=false should return empty message");
        waitForFragmentNotVisible(indigoDashboardsPage);
        waitForDashboardPageLoaded(browser);
    }

    private String buildEventingMesage(String obj) {
        return new JSONArray() {{
            put(obj);
        }}.toString();
    }
}
