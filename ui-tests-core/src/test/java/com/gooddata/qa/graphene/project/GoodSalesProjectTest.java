package com.gooddata.qa.graphene.project;

import java.util.ArrayList;
import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.ExportFormat;
import com.gooddata.qa.graphene.enums.ReportTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardsPage;
import com.gooddata.qa.graphene.fragments.reports.ReportPage;
import com.gooddata.qa.graphene.fragments.reports.ReportsPage;
import com.gooddata.qa.utils.graphene.Screenshots;

@Test(groups = { "projectGoodSales" }, description = "Tests for GoodSales project functionality in GD platform")
public class GoodSalesProjectTest extends GoodSalesAbstractTest {
	
	private int createdReportsCount = 0;
	
	private String exportedDashboardName;
	private long expectedDashboardExportSize = 65000L;
	
	private long expectedLineChartExportPDFSize = 100000L;
	private long expectedLineChartReportExportPNGSize = 36000L;
	private long expectedLineChartReportExportXLSSize = 5000L;
	private long expectedLineChartReportExportCSVSize = 140L;
	private long expectedTabularReportExportPDFSize = 28000L;
	private long expectedTabularReportExportXLSSize = 8000L;
	private long expectedTabularReportExportCSVSize = 1400L;
	
	@Test(dependsOnMethods = { "createProject" }, groups = { "dashboards-verification" })
	public void verifyDashboardTabs() throws InterruptedException {
		verifyProjectDashboardTabs(true, expectedGoodSalesTabs.length, expectedGoodSalesTabs, true);
	}
	
	@Test(dependsOnMethods = { "verifyDashboardTabs" }, groups = { "dashboards-verification" })
	public void exportFirstDashboard() throws InterruptedException {
		browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId + "|projectDashboardPage");
		waitForElementVisible(BY_LOGGED_USER_BUTTON);
		waitForDashboardPageLoaded();
		DashboardsPage dashboards = Graphene.createPageFragment(DashboardsPage.class, browser.findElement(BY_PANEL_ROOT));
		exportedDashboardName = dashboards.exportDashboardTab(0);
		checkRedBar();
	}
	
	@Test(dependsOnMethods = { "exportFirstDashboard" }, groups = { "dashboards-verification" })
	public void verifyExportedDashboardPDF() {
		verifyDashboardExport(exportedDashboardName, expectedDashboardExportSize);
	}
	
	@Test(dependsOnMethods = { "verifyDashboardTabs" }, groups = { "dashboards-verification" })
	public void addNewTab() throws InterruptedException {
		addNewTabOnDashboard("Pipeline Analysis", "test", "GoodSales-new-tab");
	}
	
	@Test(dependsOnMethods = { "verifyDashboardTabs" }, groups = { "dashboards-verification", "new-dashboard" })
	public void addNewDashboard() throws InterruptedException {
		DashboardsPage dashboards = initDashboardsPage();
		String dashboardName = "test";
		dashboards.addNewDashboard(dashboardName);
		waitForDashboardPageLoaded();
		waitForElementNotPresent(dashboards.getDashboardEditBar().getRoot());
		Thread.sleep(5000);
		checkRedBar();
		Assert.assertEquals(dashboards.getDashboardsCount(), 2, "New dashboard is not present");
		Assert.assertEquals(dashboards.getDashboardName(), dashboardName, "New dashboard has invalid name");
		Screenshots.takeScreenshot(browser, "GoodSales-new-dashboard", this.getClass());
	}
	
	@Test(dependsOnMethods = { "addNewDashboard" }, groups = { "dashboards-verification", "new-dashboard" })
	public void addNewTabOnNewDashboard() throws InterruptedException {
		addNewTabOnDashboard("test", "test2", "GoodSales-new-dashboard-new-tab");
	}
	
	@Test(dependsOnGroups = { "new-dashboard" }, groups = { "dashboards-verification" })
	public void deleteNewDashboard() throws InterruptedException {
		DashboardsPage dashboards = initDashboardsPage();
		int dashboardsCount = dashboards.getDashboardsCount();
		if (dashboards.selectDashboard("test")) {
			dashboards.deleteDashboard();
			waitForDashboardPageLoaded();
			Assert.assertEquals(dashboards.getDashboardsCount(), dashboardsCount - 1, "Dashboard wasn't deleted");
			checkRedBar();
		} else {
			Assert.fail("Dashboard wasn't selected and not deleted");
		}
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
		how.add("Sales Rep");
		how.add("Department");
		prepareReport("Simple tabular report", ReportTypes.TABLE, what, how);
	}
	
	@Test(dependsOnMethods = { "createTabularReport" }, groups = { "tabular-report-exports" })
	public void exportTabularReportToPDF() throws InterruptedException {
		exportReport("Simple tabular report", ExportFormat.PDF);
	}
	
	@Test(dependsOnMethods = { "exportTabularReportToPDF" }, groups = { "tabular-report-exports" })
	public void verifyExportedTabularReportPDF() {
		verifyReportExport(ExportFormat.PDF, "Simple tabular report", expectedTabularReportExportPDFSize);
	}
	
	@Test(dependsOnMethods = { "createTabularReport" }, groups = { "tabular-report-exports" })
	public void exportTabularReportToXLS() throws InterruptedException {
		exportReport("Simple tabular report", ExportFormat.EXCEL_XLS);
	}
	
	@Test(dependsOnMethods = { "exportTabularReportToXLS" }, groups = { "tabular-report-exports" })
	public void verifyExportedTabularReportXLS() {
		verifyReportExport(ExportFormat.EXCEL_XLS, "Simple tabular report", expectedTabularReportExportXLSSize);
	}
	
	@Test(dependsOnMethods = { "createTabularReport" }, groups = { "tabular-report-exports" })
	public void exportTabularReportToCSV() throws InterruptedException {
		exportReport("Simple tabular report", ExportFormat.CSV);
	}
	
	@Test(dependsOnMethods = { "exportTabularReportToCSV" }, groups = { "tabular-report-exports" })
	public void verifyExportedTabularReportCSV() {
		verifyReportExport(ExportFormat.CSV, "Simple tabular report", expectedTabularReportExportCSVSize);
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
	
	@Test(dependsOnMethods = { "addNewTab", "createLineChartReport" }, groups = { "dashboards-verification" })
	public void addReportOnTab() throws InterruptedException {
		DashboardsPage dashboards = initDashboardsPage();
		dashboards.selectDashboard("Pipeline Analysis");
		waitForDashboardPageLoaded();
		Thread.sleep(3000);
		dashboards.getTabs().openTab(9);
		waitForDashboardPageLoaded();
		dashboards.editDashboard();
		dashboards.getDashboardEditBar().addReportToDashboard("Simple line chart report");
		dashboards.getDashboardEditBar().saveDashboard();
		waitForDashboardPageLoaded();
		Screenshots.takeScreenshot(browser, "GoodSales-new-tab-with-simple-chart", this.getClass());
	}
	
	@Test(dependsOnMethods = { "createLineChartReport" }, groups = { "line-chart-exports" })
	public void exportLineChartToPDF() throws InterruptedException {
		exportReport("Simple line chart report", ExportFormat.PDF);
	}
	
	@Test(dependsOnMethods = { "exportLineChartToPDF" }, groups = { "line-chart-exports" })
	public void verifyExportedLineChartPDF() {
		verifyReportExport(ExportFormat.PDF, "Simple line chart report", expectedLineChartExportPDFSize);
	}
	
	@Test(dependsOnMethods = { "createLineChartReport" }, groups = { "line-chart-exports" })
	public void exportLineChartToPNG() throws InterruptedException {
		exportReport("Simple line chart report", ExportFormat.IMAGE_PNG);
	}
	
	@Test(dependsOnMethods = { "exportLineChartToPNG" }, groups = { "line-chart-exports" })
	public void verifyExportedLineChartPNG() {
		verifyReportExport(ExportFormat.IMAGE_PNG, "Simple line chart report", expectedLineChartReportExportPNGSize);
	}
	
	@Test(dependsOnMethods = { "createLineChartReport" }, groups = { "line-chart-exports" })
	public void exportLineChartToXLS() throws InterruptedException {
		exportReport("Simple line chart report", ExportFormat.EXCEL_XLS);
	}
	
	@Test(dependsOnMethods = { "exportLineChartToXLS" }, groups = { "line-chart-exports" })
	public void verifyExportedLineChartXLS() {
		verifyReportExport(ExportFormat.EXCEL_XLS, "Simple line chart report", expectedLineChartReportExportXLSSize);
	}
	
	@Test(dependsOnMethods = { "createLineChartReport" }, groups = { "line-chart-exports" })
	public void exportLineChartToCSV() throws InterruptedException {
		exportReport("Simple line chart report", ExportFormat.CSV);
	}
	
	@Test(dependsOnMethods = { "exportLineChartToCSV" }, groups = { "line-chart-exports" })
	public void verifyExportedLineChartCSV() {
		verifyReportExport(ExportFormat.CSV, "Simple line chart report", expectedLineChartReportExportCSVSize);
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
	
	@Test(dependsOnGroups = { "goodsales-chart", "line-chart-exports", "tabular-report-exports", "dashboards-verification" }, groups = { "lastTest" })
	public void verifyCreatedReports() {
		ReportsPage reports = initReportsPage();
		Assert.assertEquals(reports.getReportsList().getNumberOfReports(), expectedGoodSalesReportsCount + createdReportsCount, "Number of expected reports (all) doesn't match");
		selectFolder(reports, "My Reports");
		waitForReportsPageLoaded();
		Assert.assertEquals(reports.getReportsList().getNumberOfReports(), createdReportsCount, "Number of expected reports (my reports) doesn't match");
		Screenshots.takeScreenshot(browser, "GoodSales-new-reports", this.getClass());
		successfulTest = true;
	}
	
	private DashboardsPage initDashboardsPage() {
		browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId + "|projectDashboardPage");
		waitForElementVisible(BY_LOGGED_USER_BUTTON);
		waitForDashboardPageLoaded();
		return Graphene.createPageFragment(DashboardsPage.class, browser.findElement(BY_PANEL_ROOT));
	}
	
	private void prepareReport(String reportName, ReportTypes reportType, List<String> what, List<String> how) throws InterruptedException {
		ReportsPage reports = initReportsPage();
		selectFolder(reports, "My Reports");
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
	
	private void selectFolder(ReportsPage reportsPage, String folderName) {
		reportsPage.getDefaultFolders().openFolder("My Reports");
		waitForReportsPageLoaded();
		Assert.assertEquals(reportsPage.getSelectedFolderName(), "My Reports", "Selected folder name doesn't match: " + reportsPage.getSelectedFolderName());
	}
	
	private ReportPage initReportPage(String reportName) {
		ReportsPage reports = initReportsPage();
		selectFolder(reports, "My Reports");
		reports.getReportsList().openReport(reportName);
		waitForAnalysisPageLoaded();
		return Graphene.createPageFragment(ReportPage.class, browser.findElement(BY_REPORT_PAGE));
	}
	
	private void exportReport(String reportName, ExportFormat format) throws InterruptedException {
		ReportPage report = initReportPage(reportName);
		report.exportReport(format);
		checkRedBar();
	}
	
	private void addNewTabOnDashboard(String dashboardName, String tabName, String screenshotName) throws InterruptedException {
		DashboardsPage dashboards = initDashboardsPage();
		Assert.assertTrue(dashboards.selectDashboard(dashboardName), "Dashboard wasn't selected");
		waitForDashboardPageLoaded();
		Thread.sleep(3000);
		int tabsCount = dashboards.getTabs().getNumberOfTabs();
		dashboards.editDashboard();
		waitForDashboardPageLoaded();
		dashboards.addNewTab(tabName);
		checkRedBar();
		Assert.assertEquals(dashboards.getTabs().getNumberOfTabs(), tabsCount + 1, "New tab is not present");
		Assert.assertTrue(dashboards.getTabs().isTabSelected(tabsCount), "New tab is not selected");
		Assert.assertEquals(dashboards.getTabs().getTabLabel(tabsCount), tabName, "New tab has invalid label");
		dashboards.getDashboardEditBar().saveDashboard();
		waitForDashboardPageLoaded();
		waitForElementNotPresent(dashboards.getDashboardEditBar().getRoot());
		Assert.assertEquals(dashboards.getTabs().getNumberOfTabs(), tabsCount + 1, "New tab is not present after Save");
		Assert.assertTrue(dashboards.getTabs().isTabSelected(tabsCount), "New tab is not selected after Save");
		Assert.assertEquals(dashboards.getTabs().getTabLabel(tabsCount), tabName, "New tab has invalid label after Save");
		Screenshots.takeScreenshot(browser, screenshotName, this.getClass());
	}
}
