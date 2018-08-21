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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class EventingMultipleWidgetsTest extends AbstractDashboardEventingTest {

    private String oldDashboardUri;
    private String firstTabIdentifier;
    private String expectedDrillToUri;

    @Override
    public void prepareProject() throws Throwable {
        super.prepareProject();
        String dashboardName = "eventing_test_old_dashboard_" + generateHashString();
        Dashboard oldDashboard = new Dashboard().setName(dashboardName).addTab(new Tab());
        DashboardRestRequest dashboardRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
        oldDashboardUri = dashboardRequest.createDashboard(oldDashboard.getMdObject());
        firstTabIdentifier = dashboardRequest.getTabId(dashboardName, "First Tab");
        expectedDrillToUri = String.format("/dashboard.html#project=/gdc/projects/%s&dashboard=%s&tab=%s",
                testParams.getProjectId(), oldDashboardUri, firstTabIdentifier);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void eventingOnDashboardHasGreaterThan20Widgets() throws IOException {
        List<Pair<String, String>> widgetAndMetricUris = new ArrayList<>();
        widgetAndMetricUris.addAll(createMultipleWidgets());
        widgetAndMetricUris.addAll(createMultipleWidgets());
        widgetAndMetricUris.addAll(createMultipleWidgets());
        // we have 8x3 = 24 widgets
        final List<String> widgetUris = widgetAndMetricUris.stream()
                .map(Pair::getLeft).collect(Collectors.toList());

        final String dashboardTitle = "eventing_over_20_widget_" + generateHashString();
        final String dashboardUri = indigoRestRequest.createAnalyticalDashboard(widgetUris, dashboardTitle);
        final String dashboardId = getObjectIdFromUri(dashboardUri) + "?preventDefault=true";

        final JSONArray metricJsonArray = new JSONArray();
        widgetAndMetricUris.stream()
                .map(Pair::getRight).forEach(metricJsonArray::put);
        final String file = createTemplateHtmlFile(dashboardId, metricJsonArray.toString());
        IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file).waitForWidgetsLoading();

        int verifyWidgetIndex = generateRandomIntIntRange(0, 23);
        Kpi kpi = indigoDashboardsPage.getWidgetByIndex(Kpi.class, verifyWidgetIndex);
        log.info("random verify widget has index:" + verifyWidgetIndex);
        log.info("widget headline:" + kpi.getHeadline());

        assertTrue(kpi.isDrillable(), "kpi widget should be underlined");
        cleanUpLogger();
        kpi.clickKpiValue();
        JSONObject jsonObject = getLatestPostMessageObj();
        JSONObject executionContext = jsonObject.getJSONObject("data").getJSONObject("executionContext");
        assertEquals(executionContext.getJSONArray("measures").length(), 1);
        assertEquals(executionContext.getJSONArray("filters").length(), 1);
        assertEquals(jsonObject.getJSONObject("data").getJSONObject("drillTo").getString("uri"), expectedDrillToUri);
    }

    private List<Pair<String, String>> createMultipleWidgets() {
        List<Pair<String, String>> widgetAndMetricUris = new ArrayList<>();

        widgetAndMetricUris.add(generateWidget(createNodataKpi(oldDashboardUri, firstTabIdentifier)));
        widgetAndMetricUris.add(generateWidget(createInvalidKpi(oldDashboardUri, firstTabIdentifier)));
        widgetAndMetricUris.add(generateWidget(createKpiHasAttributeOnMaql(oldDashboardUri, firstTabIdentifier)));
        widgetAndMetricUris.add(generateWidget(createKpiHasDate(oldDashboardUri, firstTabIdentifier)));
        widgetAndMetricUris.add(generateWidget(createKpiHasVarible(oldDashboardUri, firstTabIdentifier)));
        widgetAndMetricUris.add(generateWidget(createKpiHasNegativeMetric(oldDashboardUri, firstTabIdentifier)));
        widgetAndMetricUris.add(generateWidget(createKpiHasZeroMetric(oldDashboardUri, firstTabIdentifier)));
        widgetAndMetricUris.add(generateWidget(createKpiHasMetricInShare(oldDashboardUri, firstTabIdentifier)));

        return widgetAndMetricUris;
    }

    private Pair<String, String> generateWidget(Pair<String, Metric> kpiUris) {
        return Pair.of(kpiUris.getLeft(), kpiUris.getRight().getUri());
    }

    private int generateRandomIntIntRange(int min, int max) {
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }
}
