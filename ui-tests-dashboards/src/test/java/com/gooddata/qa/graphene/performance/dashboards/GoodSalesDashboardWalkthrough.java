package com.gooddata.qa.graphene.performance.dashboards;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.graphene.fragments.greypages.projects.ProjectFragment;
import com.gooddata.qa.graphene.fragments.manage.ProjectAndUsersPage;
import com.gooddata.qa.utils.graphene.Screenshots;

@Test(groups = { "dashboardPerf" }, description = "Tests for performance od rendering dashboards in GoodSales project")
public class GoodSalesDashboardWalkthrough extends AbstractTest { 

	private static final String GOODSALES_TEMPLATE = "/projectTemplates/GoodSalesDemo/2";
	
	private static final String[] expectedGoodSalesTabs = {
		"Outlook", "What's Changed", "Waterfall Analysis", "Leaderboards", "Activities", "Sales Velocity", "Quarterly Trends", "Seasonality", "...and more"
	};
	
	@BeforeClass
	public void initStartPage() {
		startPage = "gdc";
	}
	
	@Test(groups = { "GoodSalesPerfInit" } )
	public void init() throws JSONException {
		signInAtGreyPages(user, password);
	}
	
	@Test(dependsOnGroups = { "GoodSalesPerfInit" })
	public void createProject() throws JSONException, InterruptedException {
		browser.get(getRootUrl() + PAGE_GDC_PROJECTS);
		waitForElementVisible(BY_GP_FORM);
		ProjectFragment project = Graphene.createPageFragment(ProjectFragment.class, browser.findElement(BY_GP_FORM));
		projectId = project.createProject("GoodSales-perf-test", "", GOODSALES_TEMPLATE, authorizationToken, 240);
		Screenshots.takeScreenshot(browser, "GoodSales-perf-project-created", this.getClass());
	}
	
	@Test(dependsOnMethods = { "createProject" })
	public void dashboardsWalkthrough() throws InterruptedException {
		browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX.replace("#s", "#_keepLogs=1&s") + projectId + "|projectDashboardPage");
		waitForDashboardPageLoaded();
		for (int i = 1; i <= 10; i++) {
			System.out.println("Iteration:" + i);
			verifyProjectDashboardTabs(expectedGoodSalesTabs.length, expectedGoodSalesTabs, false);
		}
		String output = (String) ((JavascriptExecutor) browser).executeScript("return GDC.perf.logger.getCsEvents()");
		createPerfOutputFile(output);
	}
	
	@Test(dependsOnMethods = { "dashboardsWalkthrough" })
	public void deleteProject() {
		browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId + "|projectPage");
		waitForProjectPageLoaded();
		ProjectAndUsersPage projectPage = Graphene.createPageFragment(ProjectAndUsersPage.class, browser.findElement(BY_PROJECT_PANEL));
		System.out.println("Going to delete project: " + projectId);
		projectPage.deteleProject();
	}
	
	private void createPerfOutputFile(String csvContent) {
		File mavenProjectBuildDirectory = new File(System.getProperty("maven.project.build.directory", "./target/"));
		File outputFile = new File(mavenProjectBuildDirectory, "perf.csv");
		try {
            FileUtils.writeStringToFile(outputFile, csvContent);
            System.out.println("Created performance statistics at /target/perf.csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}
	
}
