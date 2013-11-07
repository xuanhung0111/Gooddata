package com.gooddata.qa.graphene.project;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.utils.graphene.Screenshots;

public class GoodSalesAbstractTest extends AbstractTest {
	
	protected static final String GOODSALES_TEMPLATE = "/projectTemplates/GoodSalesDemo/2";
	
	protected static final String[] expectedGoodSalesTabs = {
		"Outlook", "What's Changed", "Waterfall Analysis", "Leaderboards", "Activities", "Sales Velocity", "Quarterly Trends", "Seasonality", "...and more"
	};
	
	protected static final int expectedGoodSalesReportsCount = 103;
	protected static final int expectedGoodSalesReportsCustomFoldersCount = 9;
	
	@BeforeClass
	public void initStartPage() {
		startPage = "projects.html";
	}
	
	@Test(groups = { "GoodSalesInit" } )
	public void signIn() throws JSONException, InterruptedException {
		signInAtUI(user, password);
	}
	
	@Test(dependsOnGroups = { "GoodSalesInit" })
	public void createProject() throws JSONException, InterruptedException {
		waitForProjectsPageLoaded();
		browser.get(getRootUrl() + PAGE_GDC_PROJECTS);
		waitForElementVisible(gpProject.getRoot());
		projectId = gpProject.createProject("GoodSales-test", "", GOODSALES_TEMPLATE, authorizationToken, 240);
		Screenshots.takeScreenshot(browser, "GoodSales-project-created", this.getClass());
	}

	@Test(dependsOnGroups = { "lastTest" }, alwaysRun = true)
	public void deleteProject() {
		deleteProjectByDeleteMode(successfulTest);
	}
}
