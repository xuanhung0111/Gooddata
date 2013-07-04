package com.gooddata.qa.graphene.project;

import java.util.ArrayList;
import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.graphene.enums.ReportTypes;
import com.gooddata.qa.graphene.fragments.greypages.projects.ProjectFragment;
import com.gooddata.qa.graphene.fragments.manage.ProjectAndUsersPage;
import com.gooddata.qa.graphene.fragments.reports.ReportPage;
import com.gooddata.qa.graphene.fragments.reports.ReportsPage;
import com.gooddata.qa.utils.graphene.Screenshots;

@Test(groups = { "projectGoodSales" }, description = "Tests for GoodSales project functionality in GD platform")
public class GoodSalesProjectTest extends AbstractTest {
	
	private static final String GOODSALES_TEMPLATE = "/projectTemplates/GoodSalesDemo/2";
	
	private static final String[] expectedGoodSalesTabs = {
		"Outlook", "What's Changed", "Waterfall Analysis", "Leaderboards", "Activities", "Sales Velocity", "Quarterly Trends", "Seasonality", "...and more"
	};
	
	private static final int expectedGoodSalesReportsCount = 103;
	private static final int expectedGoodSalesReportsCustomFoldersCount = 9;
	
	private int createdReportsCount = 0;
	
	@BeforeClass
	public void initStartPage() {
		startPage = "projects.html";
	}
	
	@Test(groups = { "GoodSalesInit" } )
	public void init() throws JSONException {
		// sign in with demo user
		signInAtUI(user, password);
	}
	
	@Test(dependsOnGroups = { "GoodSalesInit" })
	public void createProject() throws JSONException, InterruptedException {
		waitForProjectsPageLoaded();
		browser.get(getRootUrl() + PAGE_GDC_PROJECTS);
		waitForElementVisible(BY_GP_FORM);
		ProjectFragment project = Graphene.createPageFragment(ProjectFragment.class, browser.findElement(BY_GP_FORM));
		projectId = project.createProject("GoodSales-test", "", GOODSALES_TEMPLATE, authorizationToken, 240);
		Screenshots.takeScreenshot(browser, "GoodSales-project-created", this.getClass());
	}
	
	@Test(dependsOnMethods = { "createProject" })
	public void verifyDashboardTabs() throws InterruptedException {
		verifyProjectDashboardTabs(expectedGoodSalesTabs.length, expectedGoodSalesTabs, true);
	}
	
	@Test(dependsOnMethods = { "createProject" })
	public void verifyReportsPage() throws InterruptedException {
		ReportsPage reports = initReportsPage();
		Assert.assertEquals(reports.getReportsList().getNumberOfReports(), expectedGoodSalesReportsCount, "Number of expected reports doesn't match");
		Assert.assertEquals(reports.getCustomFolders().getNumberOfFolders(), expectedGoodSalesReportsCustomFoldersCount, "Number of expected report custom folders doesn't match");
		Screenshots.takeScreenshot(browser, "GoodSales-reports", this.getClass());
	}
	
	@Test(dependsOnMethods = { "verifyReportsPage" }, groups = { "goodsales-chart" })
	public void createHeadLineReport() throws InterruptedException {
		List<String> what = new ArrayList<String>();
		what.add("# of Activities");
		prepareReport("Simple headline report", ReportTypes.HEADLINE, what, null);
	}
	
	@Test(dependsOnMethods = { "verifyReportsPage" }, groups = { "goodsales-chart" })
	public void createTabularReport() throws InterruptedException {
		List<String> what = new ArrayList<String>();
		what.add("# of Activities");
		List<String> how = new ArrayList<String>();
		how.add("Region");
		how.add("Priority");
		prepareReport("Simple tabular report", ReportTypes.TABLE, what, how);
	}
	
	@Test(dependsOnMethods = { "verifyReportsPage" }, groups = { "goodsales-chart" })
	public void createLineChartReport() throws InterruptedException {
		List<String> what = new ArrayList<String>();
		what.add("# of Activities");
		List<String> how = new ArrayList<String>();
		how.add("Region");
		how.add("Priority");
		prepareReport("Simple line chart report", ReportTypes.LINE, what, how);
	}
	
	@Test(dependsOnMethods = { "verifyReportsPage" }, groups = { "goodsales-chart" })
	public void createAreaChartReport() throws InterruptedException {
		List<String> what = new ArrayList<String>();
		what.add("# of Activities");
		List<String> how = new ArrayList<String>();
		how.add("Region");
		how.add("Priority");
		prepareReport("Simple area chart report", ReportTypes.AREA, what, how);
	}
	
	@Test(dependsOnMethods = { "verifyReportsPage" }, groups = { "goodsales-chart" })
	public void createStackedAreaChartReport() throws InterruptedException {
		List<String> what = new ArrayList<String>();
		what.add("# of Activities");
		List<String> how = new ArrayList<String>();
		how.add("Region");
		how.add("Priority");
		prepareReport("Simple stacked area chart report", ReportTypes.STACKED_AREA, what, how);
	}
	
	@Test(dependsOnMethods = { "verifyReportsPage" }, groups = { "goodsales-chart" })
	public void createBarChartReport() throws InterruptedException {
		List<String> what = new ArrayList<String>();
		what.add("# of Activities");
		List<String> how = new ArrayList<String>();
		how.add("Region");
		how.add("Priority");
		prepareReport("Simple bar chart report", ReportTypes.BAR, what, how);
	}
	
	@Test(dependsOnMethods = { "verifyReportsPage" }, groups = { "goodsales-chart" })
	public void createStackedBarChartReport() throws InterruptedException {
		List<String> what = new ArrayList<String>();
		what.add("# of Activities");
		List<String> how = new ArrayList<String>();
		how.add("Region");
		how.add("Priority");
		prepareReport("Simple stacked bar chart report", ReportTypes.STACKED_BAR, what, how);
	}
	
	@Test(dependsOnGroups = { "goodsales-chart" })
	public void verifyCreatedReports() {
		ReportsPage reports = initReportsPage();
		Assert.assertEquals(reports.getReportsList().getNumberOfReports(), expectedGoodSalesReportsCount + createdReportsCount, "Number of expected reports (all) doesn't match");
		reports.getDefaultFolders().openFolder("My Reports");
		waitForReportsPageLoaded();
		Assert.assertEquals(reports.getSelectedFolderName(), "My Reports", "Selected folder name doesn't match: " + reports.getSelectedFolderName());
		Assert.assertEquals(reports.getReportsList().getNumberOfReports(), createdReportsCount, "Number of expected reports (my reports) doesn't match");
		Screenshots.takeScreenshot(browser, "GoodSales-new-reports", this.getClass());
	}
	
	private void prepareReport(String reportName, ReportTypes reportType, List<String> what, List<String> how) throws InterruptedException {
		ReportsPage reports = initReportsPage();
		reports.getDefaultFolders().openFolder("My Reports");
		waitForReportsPageLoaded();
		Assert.assertEquals(reports.getSelectedFolderName(), "My Reports", "Selected folder name doesn't match: " + reports.getSelectedFolderName());
		reports.startCreateReport();
		waitForAnalysisPageLoaded();
		ReportPage report = Graphene.createPageFragment(ReportPage.class, browser.findElement(BY_REPORT_PAGE));
		Assert.assertNotNull(report, "Report page not initialized!");
		report.createReport(reportName, reportType, what, how);
		Screenshots.takeScreenshot(browser, "GoodSales-" + reportName + "-" + reportType.getName(), this.getClass());
		createdReportsCount++;
	}
	
	private ReportsPage initReportsPage() {
		browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId + "|domainPage");
		waitForReportsPageLoaded();
		return Graphene.createPageFragment(ReportsPage.class, browser.findElement(BY_REPORTS_PANEL));
	}
	
	/**
	 * Do not delete project - can be used for manual verification after test
	 * 
	@Test(dependsOnMethods = { "verifyCreatedReports" })
	public void deleteProject() {
		browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId + "|projectPage");
		waitForProjectPageLoaded();
		ProjectAndUsersPage projectPage = Graphene.createPageFragment(ProjectAndUsersPage.class, browser.findElement(BY_PROJECT_PANEL));
		System.out.println("Going to delete project: " + projectId);
		projectPage.deteleProject();
	}
	*/
	
}
