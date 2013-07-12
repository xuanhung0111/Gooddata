package com.gooddata.qa.graphene.upload;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.graphene.fragments.upload.UploadColumns;
import com.gooddata.qa.graphene.fragments.upload.UploadFragment;
import com.gooddata.qa.utils.graphene.Screenshots;

public class SimpleUploadTest extends AbstractTest {
	
	private static final By BY_UPLOAD_PANEL = By.cssSelector(".l-primary");
	
	private String csvFilePath;
	
	@BeforeClass
	public void initStartPage() {
		startPage = "upload.html";
		
		csvFilePath = loadProperty("upload.file.simple.csv");
	}
	
	@Test(groups = { "SimpleUploadInit" } )
	public void init() throws JSONException {
		// sign in with demo user
		validSignInWithDemoUser(false);
	}
	
	@Test(dependsOnGroups = { "SimpleUploadInit" })
	public void testUploadFile() throws InterruptedException {
		waitForElementVisible(BY_UPLOAD_PANEL);
		UploadFragment upload = Graphene.createPageFragment(UploadFragment.class, browser.findElement(BY_UPLOAD_PANEL));
		Screenshots.takeScreenshot(browser, "1-upload-csv", this.getClass());
		// upload file
		upload.uploadFile(csvFilePath);
		UploadColumns uploadColumns = upload.getUploadColumns();
		System.out.println(uploadColumns.getNumberOfColumns() + " columns are available for upload, " + uploadColumns.getColumnNames() + " ," + uploadColumns.getColumnTypes());
		// change label of first column
		uploadColumns.setColumnName(0, "Test");
		// change column type of eighth column
		uploadColumns.setColumnType(7, UploadColumns.OptionDataType.TEXT);
		Screenshots.takeScreenshot(browser, "2-upload-definition", this.getClass());
		//confirm CSV load
		upload.confirmloadCsv();
		//verify redirect to dashboard
		waitForElementVisible(By.cssSelector(".s-we_made_some_kpis_for_you"));
		waitForDashboardPageLoaded();
		Screenshots.takeScreenshot(browser, "3-upload-dashboard", this.getClass());
	}

}
