package com.gooddata.qa.graphene.project;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.ExportFormat;
import com.gooddata.qa.graphene.enums.ReportTypes;
import com.gooddata.qa.utils.graphene.Screenshots;

import static org.testng.Assert.*;

@Test(groups = { "GoodSalesReports" }, description = "Tests for GoodSales project (reports functionality) in GD platform")
public class GoodSalesReportsTest extends GoodSalesAbstractTest {
	
	private int createdReportsCount = 0;
	
	private long expectedLineChartExportPDFSize = 110000L;
	private long expectedAreaChartReportExportPNGSize = 43000L;
	private long expectedStackedAreaChartReportExportXLSSize = 5500L;
	private long expectedBarChartReportExportCSVSize = 300L;
	private long expectedTabularReportExportPDFSize = 28000L;
	private long expectedTabularReportExportXLSSize = 11000L;
	private long expectedTabularReportExportCSVSize = 1650L;
	
	@Test(dependsOnMethods = { "createProject" })
	public void verifyReportsPage() throws InterruptedException {
		initReportsPage();
		assertEquals(reportsPage.getReportsList().getNumberOfReports(), expectedGoodSalesReportsCount, "Number of expected reports doesn't match");
		assertEquals(reportsPage.getCustomFolders().getNumberOfFolders(), expectedGoodSalesReportsCustomFoldersCount, "Number of expected report custom folders doesn't match");
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
		exportReport("Simple tabular report", ExportFormat.PDF_PORTRAIT);
	}
	
	@Test(dependsOnMethods = { "exportTabularReportToPDF" }, groups = { "tabular-report-exports" })
	public void verifyExportedTabularReportPDF() {
		verifyReportExport(ExportFormat.PDF_PORTRAIT, "Simple tabular report", expectedTabularReportExportPDFSize);
	}
	
	@Test(dependsOnMethods = { "verifyReportsPage" }, groups = { "goodsales-chart" })
	public void createTabularReport2() throws InterruptedException {
		List<String> what = new ArrayList<String>();
		what.add("# of Won Opps.");
		List<String> how = new ArrayList<String>();
		how.add("Region");
		how.add("Product");
		how.add("Sales Rep");
		how.add("Department");
		prepareReport("Simple tabular report - 2", ReportTypes.TABLE, what, how);
	}
	
	@Test(dependsOnMethods = { "createTabularReport2" }, groups = { "tabular-report-exports" })
	public void exportTabularReportToCSV() throws InterruptedException {
		exportReport("Simple tabular report - 2", ExportFormat.CSV);
	}
	
	@Test(dependsOnMethods = { "exportTabularReportToCSV" }, groups = { "tabular-report-exports" })
	public void verifyExportedTabularReportCSV() {
		verifyReportExport(ExportFormat.CSV, "Simple tabular report - 2", expectedTabularReportExportCSVSize);
	}
	
	@Test(dependsOnMethods = { "verifyReportsPage" }, groups = { "goodsales-chart" })
	public void createTabularReport3() throws InterruptedException {
		List<String> what = new ArrayList<String>();
		what.add("# of Lost Opps.");
		List<String> how = new ArrayList<String>();
		how.add("Region");
		how.add("Product");
		how.add("Sales Rep");
		how.add("Department");
		prepareReport("Simple tabular report - 3", ReportTypes.TABLE, what, how);
	}
	
	@Test(dependsOnMethods = { "createTabularReport3" }, groups = { "tabular-report-exports" })
	public void exportTabularReportToXLS() throws InterruptedException {
		exportReport("Simple tabular report - 3", ExportFormat.EXCEL_XLS);
	}
	
	@Test(dependsOnMethods = { "exportTabularReportToXLS" }, groups = { "tabular-report-exports" })
	public void verifyExportedTabularReportXLS() {
		verifyReportExport(ExportFormat.EXCEL_XLS, "Simple tabular report - 3", expectedTabularReportExportXLSSize);
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
	
	@Test(dependsOnMethods = { "createLineChartReport" }, groups = { "chart-exports" })
	public void exportLineChartToPDF() throws InterruptedException {
		exportReport("Simple line chart report", ExportFormat.PDF);
	}
	
	@Test(dependsOnMethods = { "exportLineChartToPDF" }, groups = { "chart-exports" })
	public void verifyExportedLineChartPDF() {
		verifyReportExport(ExportFormat.PDF, "Simple line chart report", expectedLineChartExportPDFSize);
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
	
	@Test(dependsOnMethods = { "createAreaChartReport" }, groups = { "chart-exports" })
	public void exportAreaChartToPNG() throws InterruptedException {
		exportReport("Simple area chart report", ExportFormat.IMAGE_PNG);
	}
	
	@Test(dependsOnMethods = { "exportAreaChartToPNG" }, groups = { "chart-exports" })
	public void verifyExportedAreaChartPNG() {
		verifyReportExport(ExportFormat.IMAGE_PNG, "Simple area chart report", expectedAreaChartReportExportPNGSize);
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
	
	@Test(dependsOnMethods = { "createStackedAreaChartReport" }, groups = { "chart-exports" })
	public void exportStackedAreaChartToXLS() throws InterruptedException {
		exportReport("Simple stacked area chart report", ExportFormat.EXCEL_XLS);
	}
	
	@Test(dependsOnMethods = { "exportStackedAreaChartToXLS" }, groups = { "chart-exports" })
	public void verifyExportedStackedAreaChartXLS() {
		verifyReportExport(ExportFormat.EXCEL_XLS, "Simple stacked area chart report", expectedStackedAreaChartReportExportXLSSize);
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
	
	@Test(dependsOnMethods = { "createBarChartReport" }, groups = { "chart-exports" })
	public void exportBarChartToCSV() throws InterruptedException {
		exportReport("Simple bar chart report", ExportFormat.CSV);
	}
	
	@Test(dependsOnMethods = { "exportBarChartToCSV" }, groups = { "chart-exports" })
	public void verifyExportedBarChartCSV() {
		verifyReportExport(ExportFormat.CSV, "Simple bar chart report", expectedBarChartReportExportCSVSize);
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
	
	@Test(dependsOnGroups = { "goodsales-chart", "chart-exports", "tabular-report-exports" }, groups = { "tests" })
	public void verifyCreatedReports() throws InterruptedException {
		initReportsPage();
		selectReportsDomainFolder("All");
		waitForReportsPageLoaded();
		Thread.sleep(5000);
		assertEquals(reportsPage.getReportsList().getNumberOfReports(), expectedGoodSalesReportsCount + createdReportsCount, "Number of expected reports (all) doesn't match");
		Screenshots.takeScreenshot(browser, "GoodSales-reports", this.getClass());
		successfulTest = true;
	}
	
	private void prepareReport(String reportName, ReportTypes reportType, List<String> what, List<String> how) throws InterruptedException {
		initReportsPage();
		selectReportsDomainFolder("My Reports");
		reportsPage.startCreateReport();
		waitForAnalysisPageLoaded();
		waitForElementVisible(reportPage.getRoot());
		assertNotNull(reportPage, "Report page not initialized!");
		reportPage.createReport(reportName, reportType, what, how);
		Screenshots.takeScreenshot(browser, "GoodSales-" + reportName + "-" + reportType.getName(), this.getClass());
		createdReportsCount++;
	}
	
	private void selectReportsDomainFolder(String folderName) {
		reportsPage.getDefaultFolders().openFolder(folderName);
		waitForReportsPageLoaded();
		assertEquals(reportsPage.getSelectedFolderName(), folderName, "Selected folder name doesn't match: " + reportsPage.getSelectedFolderName());
	}
	
	private void initReportPage(String reportName) {
		initReportsPage();
		selectReportsDomainFolder("My Reports");
		reportsPage.getReportsList().openReport(reportName);
		waitForAnalysisPageLoaded();
		waitForElementVisible(reportPage.getRoot());
	}
	
	private void exportReport(String reportName, ExportFormat format) throws InterruptedException {
		initReportPage(reportName);
		reportPage.exportReport(format);
		checkRedBar();
	}
}
