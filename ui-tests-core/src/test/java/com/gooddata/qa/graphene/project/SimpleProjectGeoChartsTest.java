package com.gooddata.qa.graphene.project;

import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.graphene.enums.WidgetTypes;
import com.gooddata.qa.graphene.fragments.manage.AttributeDetailPage;
import com.gooddata.qa.graphene.fragments.manage.AttributesTable;
import com.gooddata.qa.graphene.fragments.upload.UploadColumns;
import com.gooddata.qa.graphene.fragments.upload.UploadFragment;
import com.gooddata.qa.utils.graphene.Screenshots;

@Test(groups = { "projectSimpleGeo" }, description = "Tests for geo charts on simple project in GD platform")
public class SimpleProjectGeoChartsTest extends AbstractTest {
	
	private String csvFilePath;
	
	@FindBy(css=".l-primary")
	private UploadFragment upload;
	
	@FindBy(id="attributesTable")
	private AttributesTable attributesTable;
	
	@FindBy(id="p-objectPage")
	private AttributeDetailPage attributeDetailPage;
	
	@BeforeClass
	public void initStartPage() {
		startPage = "projects.html";
	
		csvFilePath = loadProperty("csvFilePath");
	}
	
	@Test(groups = { "projectSimpleGeoInit" } )
	public void init() throws JSONException {
		// sign in with demo user
		signInAtUI(user, password);
	}
	
	@Test(dependsOnGroups = { "projectSimpleGeoInit" })
	public void createSimpleProjectGeo() throws JSONException, InterruptedException {
		openUrl(PAGE_GDC_PROJECTS);
		waitForElementVisible(gpProject.getRoot());
		projectId = gpProject.createProject("simple-project-geo", "", "", authorizationToken, 12);
		Screenshots.takeScreenshot(browser, "simple-project-geo-created", this.getClass());
	}
	
	@Test(dependsOnMethods = { "createSimpleProjectGeo" }, groups = { "geo-charts" })
	public void uploadDataForGeoCharts() throws InterruptedException {
		openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|projectDashboardPage");
		waitForDashboardPageLoaded();
		uploadFile(csvFilePath + "/geo_test.csv", 1);
		uploadFile(csvFilePath + "/geo_test_pins.csv", 2);
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
	
	@Test(dependsOnMethods = { "addNewTabs" }, groups = { "geo-charts" })
	public void addGeoWidgetsOnTab() throws InterruptedException {
		addGeoWidgetOnTab(2, "Sum of amount");
		logout();
		signInAtUI(user, password);
		addGeoWidgetOnTab(3, "Avg of hodnota");
		successfulTest = true;
	}
	
	@Test(dependsOnGroups = { "geo-charts" }, alwaysRun = true)
	public void deleteSimpleProject() {
		deleteProjectByDeleteMode(successfulTest);
	}
	
	private void uploadFile(String filePath, int order) throws InterruptedException {
		openUrl(PAGE_UPLOAD);
		waitForElementVisible(upload.getRoot());
		upload.uploadFile(filePath);
		Screenshots.takeScreenshot(browser, "simple-project-upload-" + order, this.getClass());
		UploadColumns uploadColumns = upload.getUploadColumns();
		System.out.println(uploadColumns.getNumberOfColumns() + " columns are available for upload, " + uploadColumns.getColumnNames() + " ," + uploadColumns.getColumnTypes());
		Screenshots.takeScreenshot(browser, "upload-definition", this.getClass());
		upload.confirmloadCsv();
		waitForElementVisible(By.xpath("//iframe[contains(@src,'Auto-Tab')]"));
		waitForDashboardPageLoaded();
		Screenshots.takeScreenshot(browser, "simple-project-upload-" + order + "-dashboard", this.getClass());
	}
	
	private void configureAttributeLabel(String attributeName, String attributeLabelType) throws InterruptedException {
		openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|dataPage|attributes");
		waitForElementVisible(attributesTable.getRoot());
		waitForDataPageLoaded();
		attributesTable.selectAttribute(attributeName);
		waitForElementVisible(attributeDetailPage.getRoot());
		waitForObjectPageLoaded();
		Assert.assertEquals(attributeDetailPage.getAttributeName(), attributeName, "Invalid attribute name on detail page");
		attributeDetailPage.selectLabelType(attributeLabelType);
		Thread.sleep(2000);
		Assert.assertEquals(attributeDetailPage.getAttributeLabelType(), attributeLabelType, "Label type not set properly");
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
