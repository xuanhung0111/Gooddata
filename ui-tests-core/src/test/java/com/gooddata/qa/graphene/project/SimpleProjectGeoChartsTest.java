package com.gooddata.qa.graphene.project;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.WidgetTypes;
import com.gooddata.qa.utils.graphene.Screenshots;

import static org.testng.Assert.*;

@Test(groups = {"projectSimpleGeo"}, description = "Tests for geo charts on simple project in GD platform")
public class SimpleProjectGeoChartsTest extends AbstractProjectTest {

    private String csvFilePath;

    @BeforeClass
    public void initProperties() {
        csvFilePath = testParams.loadProperty("csvFilePath");
        projectTitle = "simple-project-geo";
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"geo-charts"})
    public void uploadDataForGeoCharts() throws InterruptedException {
        ui.uploadCSV(csvFilePath + "/geo_test.csv", null, "geo-1");
        ui.uploadCSV(csvFilePath + "/geo_test_pins.csv", null, "geo-2");
    }

    @Test(dependsOnMethods = {"uploadDataForGeoCharts"}, groups = {"geo-charts"})
    public void configureGeoAttributes() throws InterruptedException {
        configureAttributeLabel("geo_id", "US States (US Census ID)");
        configureAttributeLabel("Pin", "Geo pushpin");
    }

    @Test(dependsOnMethods = {"configureGeoAttributes"}, groups = {"geo-charts"})
    public void addNewTabs() throws InterruptedException {
        ui.addNewTabOnDashboard("Default dashboard", "geochart", "simple-geo-1");
        ui.addNewTabOnDashboard("Default dashboard", "geochart-pins", "simple-geo-2");
    }

    @Test(dependsOnMethods = {"addNewTabs"}, groups = {"geo-charts", "tests"})
    public void addGeoWidgetsOnTab() throws InterruptedException {
        addGeoWidgetOnTab(2, "Sum of amount");
        ui.logout();
        ui.signInAtUI(testParams.getUser(), testParams.getPassword());
        addGeoWidgetOnTab(3, "Avg of hodnota");
        successfulTest = true;
    }

    private void configureAttributeLabel(String attributeName, String attributeLabelType) throws InterruptedException {
        openUrl(ui.PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|attributes");
        waitForElementVisible(ui.attributesTable.getRoot());
        checkUtils.waitForDataPageLoaded();
        ui.attributesTable.selectObject(attributeName);
        waitForElementVisible(ui.attributeDetailPage.getRoot());
        checkUtils.waitForObjectPageLoaded();
        assertEquals(ui.attributeDetailPage.getAttributeName(), attributeName, "Invalid attribute name on detail page");
        ui.attributeDetailPage.selectLabelType(attributeLabelType);
        Thread.sleep(2000);
        assertEquals(ui.attributeDetailPage.getAttributeLabelType(), attributeLabelType, "Label type not set properly");
    }

    private void addGeoWidgetOnTab(int tabIndex, String metric) throws InterruptedException {
        ui.initDashboardsPage();
        ui.dashboardsPage.getTabs().openTab(tabIndex);
        checkUtils.waitForDashboardPageLoaded();
        ui.dashboardsPage.editDashboard();
        ui.dashboardsPage.getDashboardEditBar().addWidgetToDashboard(WidgetTypes.GEO_CHART, metric);
        ui.dashboardsPage.getDashboardEditBar().saveDashboard();
        Thread.sleep(5000);
        checkUtils.waitForDashboardPageLoaded();
        Thread.sleep(5000);
        Screenshots.takeScreenshot(browser, "geochart-new-tab-" + tabIndex, this.getClass());
    }

}
