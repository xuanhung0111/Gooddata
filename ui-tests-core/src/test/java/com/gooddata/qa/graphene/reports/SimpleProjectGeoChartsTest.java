package com.gooddata.qa.graphene.reports;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.WidgetTypes;
import com.gooddata.qa.utils.graphene.Screenshots;

import static org.testng.Assert.*;
import static com.gooddata.qa.graphene.common.CheckUtils.*;

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
        uploadCSV(csvFilePath + "/geo_test.csv", null, "geo-1");
        uploadCSV(csvFilePath + "/geo_test_pins.csv", null, "geo-2");
    }

    @Test(dependsOnMethods = {"uploadDataForGeoCharts"}, groups = {"geo-charts"})
    public void configureGeoAttributes() throws InterruptedException {
        configureAttributeLabel("geo_id", "US States (US Census ID)");
        configureAttributeLabel("Pin", "Geo pushpin");
    }

    @Test(dependsOnMethods = {"configureGeoAttributes"}, groups = {"geo-charts"})
    public void addNewTabs() throws InterruptedException {
        addNewTabOnDashboard("Default dashboard", "geochart", "simple-geo-1");
        addNewTabOnDashboard("Default dashboard", "geochart-pins", "simple-geo-2");
    }

    @Test(dependsOnMethods = {"addNewTabs"}, groups = {"geo-charts", "tests"})
    public void addGeoWidgetsOnTab() throws InterruptedException {
        addGeoWidgetOnTab(2, "Sum of amount");
        logout();
        signInAtUI(testParams.getUser(), testParams.getPassword());
        addGeoWidgetOnTab(3, "Avg of hodnota");
        successfulTest = true;
    }

    private void configureAttributeLabel(String attributeName, String attributeLabelType) throws InterruptedException {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|attributes");
        waitForElementVisible(attributesTable.getRoot());
        waitForDataPageLoaded(browser);
        attributesTable.selectObject(attributeName);
        waitForElementVisible(attributeDetailPage.getRoot());
        waitForObjectPageLoaded(browser);
        assertEquals(attributeDetailPage.getAttributeName(), attributeName, "Invalid attribute name on detail page");
        attributeDetailPage.selectLabelType(attributeLabelType);
        Thread.sleep(2000);
        assertEquals(attributeDetailPage.getAttributeLabelType(), attributeLabelType, "Label type not set properly");
    }

    private void addGeoWidgetOnTab(int tabIndex, String metric) throws InterruptedException {
        initDashboardsPage();
        dashboardsPage.getTabs().openTab(tabIndex);
        waitForDashboardPageLoaded(browser);
        dashboardsPage.editDashboard();
        dashboardsPage.getDashboardEditBar().addWidgetToDashboard(WidgetTypes.GEO_CHART, metric);
        dashboardsPage.getDashboardEditBar().saveDashboard();
        Thread.sleep(5000);
        waitForDashboardPageLoaded(browser);
        Thread.sleep(5000);
        Screenshots.takeScreenshot(browser, "geochart-new-tab-" + tabIndex, this.getClass());
    }

}
