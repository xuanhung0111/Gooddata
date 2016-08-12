package com.gooddata.qa.graphene.indigo.dashboards.common;

import org.openqa.selenium.Dimension;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;

import org.json.JSONException;

public abstract class DashboardsGeneralTest extends GoodSalesAbstractTest {

    private void maximizeWindow() {
        browser.manage().window().maximize();
    }

    private void setWindowSize(int width, int height) {
        browser.manage().window().setSize(new Dimension(width, height));
    }

    private void adjustWindowSize(String windowSize) {
        String executionEnv = System.getProperty("test.execution.env");
        if ("maximize".equals(windowSize)) {
            maximizeWindow();
        } else if (executionEnv != null && executionEnv.contains("browserstack-mobile")) {
            System.out.println("Window size is ignored for execution on mobile devices at Browserstack.");
        } else {
            String[] dimensions = windowSize.split(",");
            if (dimensions.length == 2) {
                try {
                    setWindowSize(Integer.valueOf(dimensions[0]), Integer.valueOf(dimensions[1]));
                } catch (NumberFormatException e) {
                    System.out.println("ERROR: Invalid window size given: " + windowSize + " (fallback to maximize)");
                    maximizeWindow();
                }
            } else {
                System.out.println("ERROR: Invalid window size given: " + windowSize + " (fallback to maximize)");
                maximizeWindow();
            }
        }
    }

    @BeforeClass(alwaysRun = true)
    public void before() {
        validateAfterClass = false;
    }


    @Test(dependsOnGroups = {"createProject"}, groups = {"dashboardsInit"})
    @Parameters({"windowSize"})
    public void initDashboardTests(@Optional("maximize") String windowSize) throws JSONException {
        adjustWindowSize(windowSize);
        ProjectRestUtils.setFeatureFlagInProject(getGoodDataClient(), testParams.getProjectId(),
                ProjectFeatureFlags.ENABLE_ANALYTICAL_DASHBOARDS, true);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"dashboardsInit"})
    public void setStartPageContext() {
        startPageContext = null;
    }
}
