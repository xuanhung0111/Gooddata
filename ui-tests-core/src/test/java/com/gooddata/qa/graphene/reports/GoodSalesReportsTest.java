package com.gooddata.qa.graphene.reports;

import java.util.ArrayList;
import java.util.List;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;

import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.report.ReportDefinition;
import com.gooddata.qa.graphene.enums.ExportFormat;
import com.gooddata.qa.graphene.enums.ReportTypes;
import com.gooddata.qa.graphene.enums.metrics.SimpleMetricTypes;
import com.gooddata.qa.utils.graphene.Screenshots;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.testng.Assert;

import java.io.IOException;
import java.net.URL;

import static org.testng.Assert.*;
import static com.gooddata.qa.graphene.utils.CheckUtils.*;

@Test(groups = {"GoodSalesReports"}, description = "Tests for GoodSales project (reports functionality) in GD platform")
public class GoodSalesReportsTest extends GoodSalesAbstractTest {

    private int createdReportsCount = 0;
    protected static final int expectedGoodSalesReportsCount = 103;
    protected static final int expectedGoodSalesReportsCustomFoldersCount = 9;

    private static final long expectedLineChartExportPDFSize = 110000L;
    private static final long expectedAreaChartReportExportPNGSize = 43000L;
    private static final long expectedStackedAreaChartReportExportXLSSize = 5500L;
    private static final long expectedBarChartReportExportCSVSize = 300L;
    private static final long expectedTabularReportExportPDFSize = 28000L;
    private static final long expectedTabularReportExportXLSSize = 11000L;
    private static final long expectedTabularReportExportXLSXSize = 6600L;
    private static final long expectedTabularReportExportCSVSize = 1650L;

    private static final String SIMPLE_CA_REPORT = "Simple CA report";

    private String regionLocator = "//div[@*[local-name() = 'gdc:region']='0,0,0,0']/span";


    @Test(dependsOnMethods = {"createProject"})
    public void verifyReportsPage() throws InterruptedException {
        initReportsPage();
        assertEquals(reportsPage.getReportsList().getNumberOfReports(), expectedGoodSalesReportsCount,
                "Number of expected reports doesn't match");
        assertEquals(reportsPage.getCustomFolders().getNumberOfFolders(), expectedGoodSalesReportsCustomFoldersCount,
                "Number of expected report custom folders doesn't match");
        Screenshots.takeScreenshot(browser, "GoodSales-reports", this.getClass());
    }

    @Test(dependsOnMethods = {"verifyReportsPage"})
    public void createComputedAttributesTabularReport() throws InterruptedException, IOException, JSONException {

        By includeArea = By.xpath("//div[@title='Include']");
        By excludeArea = By.xpath("//div[@title='Exclude']");
        String expectedValue = "31,110.00";

        URL maqlResource = getClass().getResource("/comp-attributes/ca-maql-simple.txt");
        postMAQL(IOUtils.toString(maqlResource), 60);

        initReportsPage();
        reportsPage.startCreateReport();
        reportPage.createSimpleMetric(SimpleMetricTypes.SUM, "Duration", null, true);

        List<String> what = new ArrayList<String>();
        what.add("Duration [Sum]");
        List<String> how = new ArrayList<String>();
        how.add("Forecast Category");

        prepareReport(SIMPLE_CA_REPORT, ReportTypes.TABLE, what, how);
        String bucketRegion = waitForElementPresent(includeArea, browser).getAttribute("gdc:region").replace('0', '1');
        String computedAttr = waitForElementPresent(By.xpath(regionLocator.replaceAll("\\d+,\\d+,\\d+,\\d+", bucketRegion)), browser).getText();
        Assert.assertEquals(computedAttr, expectedValue);

        /*** invert condition and check if it's reflected on report ***/
        maqlResource = getClass().getResource("/comp-attributes/ca-maql-simple-inv.txt");
        postMAQL(IOUtils.toString(maqlResource), 60);

        initReportPage(SIMPLE_CA_REPORT);
        bucketRegion = waitForElementPresent(excludeArea, browser).getAttribute("gdc:region").replace('0', '1');
        computedAttr = waitForElementPresent(By.xpath(regionLocator.replaceAll("\\d+,\\d+,\\d+,\\d+", bucketRegion)), browser).getText();
        Assert.assertEquals(computedAttr, expectedValue);
    }

    @Test(dependsOnMethods = {"verifyReportsPage"}, groups = {"goodsales-chart"})
    public void createHeadLineReport() throws InterruptedException {
        List<String> what = new ArrayList<String>();
        what.add("# of Activities");
        prepareReport("Simple headline report", ReportTypes.HEADLINE, what, null);
    }

    @Test(dependsOnMethods = {"verifyReportsPage"}, groups = {"goodsales-chart"})
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

    @Test(dependsOnMethods = {"createTabularReport"}, groups = {"tabular-report-exports"})
    public void exportTabularReportToPDF() throws InterruptedException {
        exportReport("Simple tabular report", ExportFormat.PDF_PORTRAIT);
    }

    @Test(dependsOnMethods = {"exportTabularReportToPDF"}, groups = {"tabular-report-exports"})
    public void verifyExportedTabularReportPDF() {
        verifyReportExport(ExportFormat.PDF_PORTRAIT, "Simple tabular report", expectedTabularReportExportPDFSize);
    }

    @Test(dependsOnMethods = {"verifyReportsPage"}, groups = {"goodsales-chart"})
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

    @Test(dependsOnMethods = {"createTabularReport2"}, groups = {"tabular-report-exports"})
    public void exportTabularReportToCSV() throws InterruptedException {
        exportReport("Simple tabular report - 2", ExportFormat.CSV);
    }

    @Test(dependsOnMethods = {"exportTabularReportToCSV"}, groups = {"tabular-report-exports"})
    public void verifyExportedTabularReportCSV() {
        verifyReportExport(ExportFormat.CSV, "Simple tabular report - 2", expectedTabularReportExportCSVSize);
    }

    @Test(dependsOnMethods = {"verifyReportsPage"}, groups = {"goodsales-chart"})
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

    @Test(dependsOnMethods = {"createTabularReport3"}, groups = {"tabular-report-exports"})
    public void exportTabularReportToXLS() throws InterruptedException {
        exportReport("Simple tabular report - 3", ExportFormat.EXCEL_XLS);
    }

    @Test(dependsOnMethods = {"exportTabularReportToXLS"}, groups = {"tabular-report-exports"})
    public void verifyExportedTabularReportXLS() {
        verifyReportExport(ExportFormat.EXCEL_XLS, "Simple tabular report - 3", expectedTabularReportExportXLSSize);
    }

    @Test(dependsOnMethods = {"verifyReportsPage"}, groups = {"goodsales-chart"})
    public void createTabularReport4() throws InterruptedException {
        List<String> what = new ArrayList<String>();
        what.add("# of Opportunities");
        List<String> how = new ArrayList<String>();
        how.add("Region");
        how.add("Product");
        how.add("Sales Rep");
        how.add("Department");
        prepareReport("Simple tabular report - 4", ReportTypes.TABLE, what, how);
    }

    @Test(dependsOnMethods = {"createTabularReport4"}, groups = {"tabular-report-exports"})
    public void exportTabularReportToXLSX() throws InterruptedException {
        exportReport("Simple tabular report - 4", ExportFormat.EXCEL_XLSX);
    }

    @Test(dependsOnMethods = {"exportTabularReportToXLSX"}, groups = {"tabular-report-exports"})
    public void verifyExportedTabularReportXLSX() {
        verifyReportExport(ExportFormat.EXCEL_XLSX, "Simple tabular report - 4", expectedTabularReportExportXLSXSize);
    }

    @Test(dependsOnMethods = {"verifyReportsPage"}, groups = {"goodsales-chart"})
    public void createLineChartReport() throws InterruptedException {
        List<String> what = new ArrayList<String>();
        what.add("# of Activities");
        List<String> how = new ArrayList<String>();
        how.add("Region");
        how.add("Priority");
        prepareReport("Simple line chart report", ReportTypes.LINE, what, how);
    }

    @Test(dependsOnMethods = {"createLineChartReport"}, groups = {"chart-exports"})
    public void exportLineChartToPDF() throws InterruptedException {
        exportReport("Simple line chart report", ExportFormat.PDF);
    }

    @Test(dependsOnMethods = {"exportLineChartToPDF"}, groups = {"chart-exports"})
    public void verifyExportedLineChartPDF() {
        verifyReportExport(ExportFormat.PDF, "Simple line chart report", expectedLineChartExportPDFSize);
    }

    @Test(dependsOnMethods = {"verifyReportsPage"}, groups = {"goodsales-chart"})
    public void createAreaChartReport() throws InterruptedException {
        List<String> what = new ArrayList<String>();
        what.add("# of Activities");
        List<String> how = new ArrayList<String>();
        how.add("Region");
        how.add("Priority");
        prepareReport("Simple area chart report", ReportTypes.AREA, what, how);
    }

    @Test(dependsOnMethods = {"createAreaChartReport"}, groups = {"chart-exports"})
    public void exportAreaChartToPNG() throws InterruptedException {
        exportReport("Simple area chart report", ExportFormat.IMAGE_PNG);
    }

    @Test(dependsOnMethods = {"exportAreaChartToPNG"}, groups = {"chart-exports"})
    public void verifyExportedAreaChartPNG() {
        verifyReportExport(ExportFormat.IMAGE_PNG, "Simple area chart report", expectedAreaChartReportExportPNGSize);
    }

    @Test(dependsOnMethods = {"verifyReportsPage"}, groups = {"goodsales-chart"})
    public void createStackedAreaChartReport() throws InterruptedException {
        List<String> what = new ArrayList<String>();
        what.add("# of Activities");
        List<String> how = new ArrayList<String>();
        how.add("Region");
        how.add("Priority");
        prepareReport("Simple stacked area chart report", ReportTypes.STACKED_AREA, what, how);
    }

    @Test(dependsOnMethods = {"createStackedAreaChartReport"}, groups = {"chart-exports"})
    public void exportStackedAreaChartToXLS() throws InterruptedException {
        exportReport("Simple stacked area chart report", ExportFormat.EXCEL_XLS);
    }

    @Test(dependsOnMethods = {"exportStackedAreaChartToXLS"}, groups = {"chart-exports"})
    public void verifyExportedStackedAreaChartXLS() {
        verifyReportExport(ExportFormat.EXCEL_XLS, "Simple stacked area chart report",
                expectedStackedAreaChartReportExportXLSSize);
    }

    @Test(dependsOnMethods = {"verifyReportsPage"}, groups = {"goodsales-chart"})
    public void createBarChartReport() throws InterruptedException {
        List<String> what = new ArrayList<String>();
        what.add("# of Activities");
        List<String> how = new ArrayList<String>();
        how.add("Region");
        how.add("Priority");
        prepareReport("Simple bar chart report", ReportTypes.BAR, what, how);
    }

    @Test(dependsOnMethods = {"createBarChartReport"}, groups = {"chart-exports"})
    public void exportBarChartToCSV() throws InterruptedException {
        exportReport("Simple bar chart report", ExportFormat.CSV);
    }

    @Test(dependsOnMethods = {"exportBarChartToCSV"}, groups = {"chart-exports"})
    public void verifyExportedBarChartCSV() {
        verifyReportExport(ExportFormat.CSV, "Simple bar chart report", expectedBarChartReportExportCSVSize);
    }

    @Test(dependsOnMethods = {"verifyReportsPage"}, groups = {"goodsales-chart"})
    public void createStackedBarChartReport() throws InterruptedException {
        List<String> what = new ArrayList<String>();
        what.add("# of Activities");
        List<String> how = new ArrayList<String>();
        how.add("Region");
        how.add("Priority");
        prepareReport("Simple stacked bar chart report", ReportTypes.STACKED_BAR, what, how);
    }

    @Test(dependsOnGroups = {"goodsales-chart", "chart-exports", "tabular-report-exports"})
    public void verifyCreatedReports() throws InterruptedException {
        initReportsPage();
        selectReportsDomainFolder("All");
        waitForReportsPageLoaded(browser);
        Thread.sleep(5000);
        assertEquals(reportsPage.getReportsList().getNumberOfReports(),
                expectedGoodSalesReportsCount + createdReportsCount, "Number of expected reports (all) doesn't match");
        Screenshots.takeScreenshot(browser, "GoodSales-reports", this.getClass());
    }

    @Test(dependsOnMethods = {"verifyCreatedReports"})
    public void deleteReport() {
        initReportPage(SIMPLE_CA_REPORT);
        reportPage.deleteCurrentReport();
        assertFalse(waitForFragmentVisible(reportsPage).isReportVisible(SIMPLE_CA_REPORT));
    }

    private void prepareReport(String reportName, ReportTypes reportType, List<String> what, List<String> how)
            throws InterruptedException {
        ReportDefinition reportDefinition = new ReportDefinition().withName(reportName)
                                                                  .withType(reportType);

        if (what != null) {
            for (String metric : what)
                reportDefinition.withWhats(metric);
        }

        if (how != null) {
            for (String attribute : how)
                reportDefinition.withHows(attribute);
        }

        createReport(reportDefinition, "GoodSales");
        createdReportsCount++;
    }

    private void initReportPage(String reportName) {
        initReportsPage();
        selectReportsDomainFolder("My Reports");
        reportsPage.getReportsList().openReport(reportName);
        waitForAnalysisPageLoaded(browser);
        waitForElementVisible(reportPage.getRoot());
    }

    private void exportReport(String reportName, ExportFormat format) throws InterruptedException {
        initReportPage(reportName);
        reportPage.exportReport(format);
        checkRedBar(browser);
    }

}
