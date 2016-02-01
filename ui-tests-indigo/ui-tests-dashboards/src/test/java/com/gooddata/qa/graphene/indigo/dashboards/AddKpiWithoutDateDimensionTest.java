package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.md.Restriction.identifier;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertFalse;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.md.Fact;
import com.gooddata.md.MetadataService;
import com.gooddata.md.Metric;
import com.gooddata.project.Project;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;
import java.io.IOException;
import java.net.URISyntaxException;
import org.json.JSONException;

public class AddKpiWithoutDateDimensionTest extends AbstractProjectTest {

    private static final String METRIC_CONNECT_WITH_DATE_DIMENSION = "Connected";
    private static final String METRIC_NOT_CONNECT_WITH_DATE_DIMENSION = "NotConnected";
    private static final String DATE_DIMENSION = "templ:minimalistic";

    private MetadataService mdService;
    private Project project;

    @BeforeClass
    public void initProperties() {
        projectTitle = "Add-kpi-without-date-dimension-test";
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"precondition"})
    public void setupFeatureFlag() throws JSONException {
        ProjectRestUtils.setFeatureFlagInProject(getGoodDataClient(), testParams.getProjectId(),
                ProjectFeatureFlags.ENABLE_ANALYTICAL_DASHBOARDS, true);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"precondition"})
    public void initializeGoodDataSDK() {
        goodDataClient = getGoodDataClient();
        mdService = goodDataClient.getMetadataService();
        project = goodDataClient.getProjectService().getProjectById(testParams.getProjectId());
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"nodate-precondition"})
    public void setupForNoDate()
            throws JSONException, IOException, URISyntaxException {
        setupMaql("/no-date/no-date-maql.txt");
        setupData("/no-date/no-date.csv", "/no-date/upload_info.json");

        String age = mdService.getObjUri(project, Fact.class, identifier("fact.fact"));
        mdService.createObj(project, new Metric(METRIC_NOT_CONNECT_WITH_DATE_DIMENSION,
                "SELECT SUM([" + age + "])", "#,##0"));
    }

    @Test(dependsOnGroups = {"nodate-precondition"}, groups = {"nodate-test"})
    public void testAddKpiNotConnectedWithDateDimension() {
        initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets();

        indigoDashboardsPage
            .waitForDashboardLoad()
            .clickAddWidget();

        assertFalse(indigoDashboardsPage.getConfigurationPanel()
                .selectMetricByName(METRIC_NOT_CONNECT_WITH_DATE_DIMENSION)
                .isDateDimensionEnabled());

        indigoDashboardsPage.saveEditModeWithKpis();

        takeScreenshot(browser, "add-kpi-not-connect-with-date-dimension", getClass());
    }

    @Test(dependsOnGroups = {"nodate-test"}, groups = {"date-precondition"})
    public void setupWithDate()
            throws JSONException, IOException, URISyntaxException {
        setupMaql("/add-date/add-date-maql.txt");
        setupData("/add-date/add-date.csv", "/add-date/upload_info.json");

        String age = mdService.getObjUri(project, Fact.class, identifier("fact.fact"));
        mdService.createObj(project, new Metric(METRIC_CONNECT_WITH_DATE_DIMENSION,
                "SELECT 1", "#,##0"));
    }

    @Test(dependsOnGroups = {"date-precondition"}, groups = {"date-test"})
    public void testUpdateKpiConnectedWithDateDimension() {

        initIndigoDashboardsPage()
            .switchToEditMode()
            .selectLastKpi();

        indigoDashboardsPage.getConfigurationPanel()
            .selectMetricByName(METRIC_CONNECT_WITH_DATE_DIMENSION)
            .selectDateDimensionByName(DATE_DIMENSION);

        indigoDashboardsPage.saveEditModeWithKpis();
        takeScreenshot(browser, "update-kpi-connect-with-date-dimension", getClass());
    }
}
