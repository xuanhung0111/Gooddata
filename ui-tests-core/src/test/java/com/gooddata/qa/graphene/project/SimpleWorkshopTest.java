package com.gooddata.qa.graphene.project;

import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.graphene.fragments.upload.UploadColumns;
import com.gooddata.qa.graphene.fragments.upload.UploadFragment;
import com.gooddata.qa.utils.graphene.Screenshots;

@Test(groups = { "projectSimpleWS" }, description = "Tests for workshop simple project in GD platform")
public class SimpleWorkshopTest extends AbstractTest {
	
	private String csvFilePath;
	
	@FindBy(css=".l-primary")
	private UploadFragment upload;
	
	@BeforeClass
	public void initStartPage() {
		startPage = "projects.html";
	
		csvFilePath = loadProperty("csvFilePath");
	}
	
	@Test(groups = { "projectSimpleWSInit" } )
	public void init() throws JSONException {
		// sign in with demo user
		signInAtUI(user, password);
	}
	
	@Test(dependsOnGroups = { "projectSimpleWSInit" })
	public void createSimpleProjectWS() throws JSONException, InterruptedException {
		openUrl(PAGE_GDC_PROJECTS);
		waitForElementVisible(gpProject.getRoot());
		projectId = gpProject.createProject("simple-project-ws", "", "", authorizationToken, 12);
		Screenshots.takeScreenshot(browser, "simple-project-ws-created", this.getClass());
	}
	
	@Test(dependsOnMethods = { "createSimpleProjectWS" }, groups = { "ws-charts" })
	public void uploadData() throws InterruptedException {
		uploadFile(csvFilePath + "/payroll.csv", 1);
	}
	
	@Test(dependsOnMethods = { "uploadData" }, groups = { "ws-charts" })
	public void addNewTabs() throws InterruptedException {
		addNewTabOnDashboard("Default dashboard", "workshop", "simple-ws");
	}
	
	@Test(dependsOnGroups = { "ws-charts" }, alwaysRun = true)
	public void deleteSimpleProject() {
		deleteProjectByDeleteMode(successfulTest);
	}
	
	private void uploadFile(String filePath, int order) throws InterruptedException {
		openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|projectDashboardPage");
		waitForDashboardPageLoaded();
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
}
