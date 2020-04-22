package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestRequest;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

public class DateFilterConfigurationTest extends AbstractDashboardTest {

    private JSONObject defaultContent;
    private CommonRestRequest commonRestRequest;
    private String defaultDateFilterConfigUri;
    private JSONObject defaultConfig;
    private IndigoRestRequest indigoRestRequest;

    @Override
    protected void customizeProject() throws Throwable {
        ProjectRestRequest projectRestRequest =
                new ProjectRestRequest(new RestClient(getProfile(AbstractTest.Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_KPI_DASHBOARD_EXTENDED_DATE_FILTERS, true);
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_WEEK_FILTERS, true);
        String dateFilterConfigUrl = String.format("/gdc/md/%s/objects/query?category=dateFilterConfig&limit=5", testParams.getProjectId());
        commonRestRequest = new CommonRestRequest(new RestClient(getProfile(ADMIN)), testParams.getProjectId());
        defaultConfig = commonRestRequest.getJsonObject(RestRequest.initGetRequest(dateFilterConfigUrl)).getJSONObject("objects")
                .getJSONArray("items").getJSONObject(0);
        JSONObject defaultDateFilterConfig = defaultConfig.getJSONObject("dateFilterConfig");
        defaultContent = defaultDateFilterConfig.getJSONObject("content");
        defaultDateFilterConfigUri = defaultDateFilterConfig.getJSONObject("meta").getString("uri");

        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
    }

    @Test(dependsOnGroups = "createProject")
    public void checkDefaultDateFilterOptions() {
        assertTrue(defaultContent.has("absoluteForm"), "Missing absoluteForm");
        assertTrue(defaultContent.has("allTime"), "Missing allTime");
        assertTrue(defaultContent.has("relativeForm"), "Missing relativeForm");
        assertTrue(defaultContent.has("selectedOption"), "Missing selectedOption");
        assertEquals(defaultContent.getJSONObject("relativeForm").getJSONArray("granularities").toList(),
                asList("GDC.time.month", "GDC.time.year", "GDC.time.quarter", "GDC.time.date"));
    }

    @Test(dependsOnGroups = "createProject")
    public void setDateFilterOptions() {
        JSONObject dateFilterConfig = defaultConfig;
        JSONArray relativePresets = dateFilterConfig.getJSONObject("dateFilterConfig")
                .getJSONObject("content").getJSONArray("relativePresets");
        relativePresets.put(new JSONObject()
                .put("from", -2)
                .put("granularity", "GDC.time.quarter")
                .put("localIdentifier", "relative_last_2_quarters")
                .put("name", "Last 2 quarters")
                .put("to", 0)
                .put("visible", true));
        commonRestRequest
                .executeRequest(RestRequest.initPutRequest(defaultDateFilterConfigUri, dateFilterConfig.toString()), HttpStatus.OK);
        try {
            assertTrue(initIndigoDashboardsPage().addDashboard().openExtendedDateFilterPanel().getDateRangeOptions()
                .contains("Last 2 quarters"), "Last 2 quarters option doesn't display");
            relativePresets.remove(relativePresets.length() - 1);
            commonRestRequest
                .executeRequest(RestRequest.initPutRequest(defaultDateFilterConfigUri, dateFilterConfig.toString()), HttpStatus.OK);
            assertFalse(initIndigoDashboardsPage().addDashboard().openExtendedDateFilterPanel().getDateRangeOptions()
                .contains("Last 2 quarters"), "Next quarter option doesn't remove");
            dateFilterConfig.getJSONObject("dateFilterConfig").getJSONObject("content").put("selectedOption", "relative_last_7_days");
            commonRestRequest
                .executeRequest(RestRequest.initPutRequest(defaultDateFilterConfigUri, dateFilterConfig.toString()), HttpStatus.OK);
            assertEquals(initIndigoDashboardsPage().addDashboard().getDateFilter().getSelection(), "Last 7 days");
        } finally {
            commonRestRequest
                .executeRequest(RestRequest.initPutRequest(defaultDateFilterConfigUri, defaultConfig.toString()), HttpStatus.OK);
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void setDateFilterOptionsAtDashboardLevel() throws IOException {
        String dashboardUri = indigoRestRequest.createAnalyticalDashboard(singletonList(createAmountKpi()));
        JSONObject defaultAnalyticalDashboard = commonRestRequest.getJsonObject(RestRequest.initGetRequest(dashboardUri));
        JSONObject analyticalDashboard = defaultAnalyticalDashboard;
        analyticalDashboard.getJSONObject("analyticalDashboard").getJSONObject("content").put(
            "dateFilterConfig", new JSONObject()
                .put("filterName", "my filter")
                .put("mode", "active")
                .put("hideOptions", new JSONArray().put("allTime"))
                .put("hideGranularities", new JSONArray().put("GDC.time.month"))
                .put("addPresets", new JSONObject().put(
                    "relativePresets", new JSONArray().put(new JSONObject()
                        .put("localIdentifier", "last_2_years")
                        .put("name", "Last 2 years")
                        .put("granularity", "GDC.time.year")
                        .put("visible", true)
                        .put("from", -2)
                        .put("to", 0)
        ))));
        commonRestRequest.executeRequest(RestRequest.initPutRequest(dashboardUri, analyticalDashboard.toString()), HttpStatus.OK);
        try {
            List<String > options = initIndigoDashboardsPage()
                    .selectKpiDashboard("title").openExtendedDateFilterPanel().getDateRangeOptions();
            assertTrue(options.contains("Last 2 years"), "Should display new added preset");
            assertFalse(options.contains(DateRange.ALL_TIME.toString()), "Shouldn't display hide option");
            assertFalse(options.contains(DateRange.LAST_MONTH.toString()), "Shouldn't display hide granularities");
            assertEquals(indigoDashboardsPage.getDateFilter().getSelection(), DateRange.ALL_TIME.toString(),
                    "Should apply date filter configuration of project level");
            analyticalDashboard.getJSONObject("analyticalDashboard").getJSONObject("content").getJSONObject("dateFilterConfig")
                    .put("mode", "hidden");
            commonRestRequest.executeRequest(RestRequest.initPutRequest(dashboardUri, analyticalDashboard.toString()), HttpStatus.OK);
            assertFalse(initIndigoDashboardsPage().hasDateFilter(), "Date filter shouldn't display with hidden mode");
            assertTrue(indigoDashboardsPage.switchToEditMode().waitForWidgetsLoading().hasDateFilter(), "Date filter should display with edit mode");
        } finally {
            commonRestRequest
                    .executeRequest(RestRequest.initPutRequest(dashboardUri, defaultAnalyticalDashboard.toString()), HttpStatus.OK);
        }
    }
}
