package com.gooddata.qa.graphene.project;

import org.openqa.selenium.support.FindBy;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.WidgetTypes;
import com.gooddata.qa.graphene.fragments.manage.AttributeDetailPage;
import com.gooddata.qa.graphene.fragments.manage.AttributesTable;
import com.gooddata.qa.utils.graphene.Screenshots;

import static org.testng.Assert.*;

@Test(groups = { "projectSimpleGeo" }, description = "Tests for geo charts on simple project in GD platform")
public class SimpleProjectGeoChartsTest extends AbstractProjectTest {
	
	private String csvFilePath;
	
	@FindBy(id="attributesTable")
	private AttributesTable attributesTable;
	
	@FindBy(id="p-objectPage")
	private AttributeDetailPage attributeDetailPage;
	
	@BeforeClass
	public void initProperties() {
		csvFilePath = loadProperty("csvFilePath");
		projectTitle = "simple-project-geo";
	}
	
	@Test(dependsOnMethods = { "createSimpleProject" }, groups = { "geo-charts" })
	public void uploadDataForGeoCharts() throws InterruptedException {
		uploadSimpleCSV(csvFilePath + "/geo_test.csv", "geo-1");
		uploadSimpleCSV(csvFilePath + "/geo_test_pins.csv", "geo-2");
	}
	
	@Test(dependsOnMethods = { "uploadDataForGeoCharts" }, groups = { "geo-charts" })
	public void configureGeoAttributes() throws InterruptedException {
		configureAttributeLabel("geo_id", "US States (US Census ID)");
		configureAttributeLabel("Pin", "Geo pushpin");
	}
	
	@Test(dependsOnMethods = { "configureGeoAttributes" }, groups = { "geo-charts" })
	public void addNewTabs() throws InterruptedException {
		addNewTabOnDashboard("Default dashboard", "geochart", "simple-geo-1");
		addNewTabOnDashboard("Default dashboard", "geochart-pins", "simple-geo-2");
	}
	
	@Test(dependsOnMethods = { "addNewTabs" }, groups = { "geo-charts", "tests" })
	public void addGeoWidgetsOnTab() throws InterruptedException {
		addGeoWidgetOnTab(2, "Sum of amount");
		logout();
		signInAtUI(user, password);
		addGeoWidgetOnTab(3, "Avg of hodnota");
		successfulTest = true;
	}
	
	private void configureAttributeLabel(String attributeName, String attributeLabelType) throws InterruptedException {
		openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|dataPage|attributes");
		waitForElementVisible(attributesTable.getRoot());
		waitForDataPageLoaded();
		attributesTable.selectAttribute(attributeName);
		waitForElementVisible(attributeDetailPage.getRoot());
		waitForObjectPageLoaded();
		assertEquals(attributeDetailPage.getAttributeName(), attributeName, "Invalid attribute name on detail page");
		attributeDetailPage.selectLabelType(attributeLabelType);
		Thread.sleep(2000);
		assertEquals(attributeDetailPage.getAttributeLabelType(), attributeLabelType, "Label type not set properly");
	}
	
	private void addGeoWidgetOnTab(int tabIndex, String metric) throws InterruptedException {
		initDashboardsPage();
		dashboardsPage.getTabs().openTab(tabIndex);
		waitForDashboardPageLoaded();
		dashboardsPage.editDashboard();
		dashboardsPage.getDashboardEditBar().addWidgetToDashboard(WidgetTypes.GEO_CHART, metric);
		dashboardsPage.getDashboardEditBar().saveDashboard();
		Thread.sleep(5000);
		waitForDashboardPageLoaded();
		Thread.sleep(5000);
		Screenshots.takeScreenshot(browser, "geochart-new-tab-" + tabIndex, this.getClass());
	}

}
