package com.gooddata.qa.graphene.indigo.dashboards.common;


import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import org.openqa.selenium.Dimension;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public abstract class DashboardsGeneralTest extends GoodSalesAbstractTest {

    private void maximizeWindow() {
        browser.manage().window().maximize();
    }

    private void setWindowSize(int width, int height) {
        browser.manage().window().setSize(new Dimension(width, height));
    }

    private void adjustWindowSize(String windowSize) {
        if ("maximize".equals(windowSize)) {
            maximizeWindow();
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

    @Test(dependsOnMethods = {"createProject"}, groups = {"dashboardsInit"})
    @Parameters({"windowSize"})
    public void initDashboardTests(@Optional("maximize") String windowSize) {
        adjustWindowSize(windowSize);
    }
}
