package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.md.Restriction.identifier;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.UPLOAD_CSV;
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
import com.gooddata.qa.utils.io.ResourceUtils;
import org.json.JSONException;

public class AddKpiWithoutDateDimensionTest extends AbstractProjectTest {

    private static final String METRIC_CONNECT_WITH_DATE_DIMENSION = "Amount[Sum]";
    private static final String METRIC_NOT_CONNECT_WITH_DATE_DIMENSION = "Age[Sum]";
    private static final String DATE_DIMENSION = "Paydate";

    private MetadataService mdService;
    private Project project;

    @BeforeClass
    public void initProperties() {
        projectTitle = "Add-kpi-without-date-dimension-test";
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"precondition"})
    public void uploadDatasetHasDateDimension() {
        uploadCSV(ResourceUtils.getFilePathFromResource("/" + UPLOAD_CSV + "/payroll.csv"));
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"precondition"})
    public void setupFeatureFlag() throws JSONException {
        setupFeatureFlagInProject(testParams.getProjectId(), ProjectFeatureFlags.ENABLE_ANALYTICAL_DASHBOARDS);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"precondition"})
    public void initializeGoodDataSDK() {
        goodDataClient = getGoodDataClient();
        mdService = goodDataClient.getMetadataService();
        project = goodDataClient.getProjectService().getProjectById(testParams.getProjectId());
    }

    @Test(dependsOnMethods = {"uploadDatasetHasDateDimension", "initializeGoodDataSDK"}, groups = {"precondition"})
    public void createMetricConnectWithDateDimension() {
        String amount = mdService.getObjUri(project, Fact.class, identifier("fact.csv_payroll.amount"));
        mdService.createObj(project, new Metric(METRIC_CONNECT_WITH_DATE_DIMENSION,
                "SELECT SUM([" + amount + "])", "#,##0"));
    }

    @Test(dependsOnMethods = {"createMetricConnectWithDateDimension"}, groups = {"precondition"})
    public void uploadDatasetWithoutDateDimension() {
        uploadCSV(ResourceUtils.getFilePathFromResource("/" + UPLOAD_CSV + "/customer.csv"));
    }

    @Test(dependsOnMethods = {"uploadDatasetWithoutDateDimension"}, groups = {"precondition"})
    public void createMetricNotConnectWithDateDimension() {
        String age = mdService.getObjUri(project, Fact.class, identifier("fact.csv_customer.age"));
        mdService.createObj(project, new Metric(METRIC_NOT_CONNECT_WITH_DATE_DIMENSION,
                "SELECT SUM([" + age + "])", "#,##0"));
    }

    @Test(dependsOnGroups = {"precondition"})
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

        takeScreenshot(browser, "add-kip-not-connect-with-date-dimension", getClass());

        indigoDashboardsPage
            .switchToEditMode()
            .selectLastKpi();

        indigoDashboardsPage.getConfigurationPanel()
            .selectMetricByName(METRIC_CONNECT_WITH_DATE_DIMENSION)
            .selectDateDimensionByName(DATE_DIMENSION);

        indigoDashboardsPage.saveEditModeWithKpis();
        takeScreenshot(browser, "update-kip-connect-with-date-dimension", getClass());
    }
}
