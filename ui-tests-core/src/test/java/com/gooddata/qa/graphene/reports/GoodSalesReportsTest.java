package com.gooddata.qa.graphene.reports;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.metrics.SimpleMetricTypes;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.enums.report.ReportTypes;
import com.gooddata.qa.graphene.fragments.reports.ReportsPage;
import com.gooddata.qa.graphene.fragments.reports.report.ExportXLSXDialog;
import com.gooddata.qa.graphene.fragments.reports.report.ReportPage;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_REGION;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_SALES_REP;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class GoodSalesReportsTest extends GoodSalesAbstractTest {

    private int createdReportsCount = 0;
    protected static final int expectedGoodSalesReportsCount = 0;
    protected static final int expectedGoodSalesReportsFoldersCount = 4;

    private static final long expectedLineChartExportPDFSize = 40000L; //quick fix for issue QA-7481
    private static final long expectedAreaChartReportExportPNGSize = 43000L;
    private static final long expectedBarChartReportExportCSVSize = 300L;
    private static final long expectedTabularReportExportPDFSize = 20000L;
    private static final long expectedTabularReportExportXLSXSize = 4000L;
    private static final long expectedTabularReportExportCSVSize = 1650L;

    private static final String SIMPLE_TABULAR_REPORT = "Simple tabular report";
    private static final String SIMPLE_CA_REPORT = "Simple CA report";
    private static final String FOLDER_UNSORTED = "Unsorted";
    private String regionLocator = "//div[@*[local-name() = 'gdc:region']='0,0,0,0']/span";

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createNumberOfWonOppsMetric();
        getMetricCreator().createNumberOfLostOppsMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void verifyReportsPage() {
        ReportsPage reportsPage = initReportsPage();
        assertEquals(reportsPage.getReportsCount(), expectedGoodSalesReportsCount,
                "Number of expected reports doesn't match");
        assertEquals(reportsPage.getFoldersCount(), expectedGoodSalesReportsFoldersCount,
                "Number of expected report custom folders doesn't match");
        Screenshots.takeScreenshot(browser, "GoodSales-reports", this.getClass());
    }

    @Test(dependsOnMethods = {"verifyReportsPage"})
    public void createComputedAttributesTabularReport() throws IOException, JSONException {

        By includeArea = By.xpath("//div[@title='Include']");
        By excludeArea = By.xpath("//div[@title='Exclude']");
        String expectedValue = "31,110.00";

        URL maqlResource = getClass().getResource("/comp-attributes/ca-maql-simple.txt");
        postMAQL(IOUtils.toString(maqlResource), 60);

        initReportsPage().startCreateReport().openWhatPanel()
                .clickAddNewMetric().createGlobalSimpleMetric(SimpleMetricTypes.SUM, "Duration", FOLDER_UNSORTED);

        List<String> what = new ArrayList<String>();
        what.add("Duration [Sum]");
        List<String> how = new ArrayList<String>();
        how.add("Forecast Category");

        prepareReport(SIMPLE_CA_REPORT, ReportTypes.TABLE, what, how, emptyList());
        String bucketRegion = waitForElementPresent(includeArea, browser).getAttribute("gdc:region").replace('0', '1');
        String computedAttr = waitForElementPresent(By.xpath(regionLocator.replaceAll("\\d+,\\d+,\\d+,\\d+", bucketRegion)), browser).getText();
        assertEquals(computedAttr, expectedValue);

        /*** invert condition and check if it's reflected on report ***/
        maqlResource = getClass().getResource("/comp-attributes/ca-maql-simple-inv.txt");
        postMAQL(IOUtils.toString(maqlResource), 60);

        initReportPage(SIMPLE_CA_REPORT);
        bucketRegion = waitForElementPresent(excludeArea, browser).getAttribute("gdc:region").replace('0', '1');
        computedAttr = waitForElementPresent(By.xpath(regionLocator.replaceAll("\\d+,\\d+,\\d+,\\d+", bucketRegion)), browser).getText();
        assertEquals(computedAttr, expectedValue);
    }

    @Test(dependsOnMethods = {"verifyReportsPage"}, groups = {"goodsales-chart"})
    public void createHeadLineReport() {
        List<String> what = new ArrayList<String>();
        what.add("# of Activities");
        prepareReport("Simple headline report", ReportTypes.HEADLINE, what, emptyList(), emptyList());
    }

    @Test(dependsOnMethods = {"verifyReportsPage"}, groups = {"goodsales-chart"})
    public void createTabularReport() {
        List<String> what = new ArrayList<String>();
        what.add("# of Activities");
        List<String> how = new ArrayList<String>();
        how.add("Region");
        how.add("Priority");
        how.add("Sales Rep");
        how.add("Department");
        List<FilterItem> filters = new ArrayList<FilterItem>();
        filters.add(FilterItem.Factory.createAttributeFilter(ATTR_REGION, "East Coast"));
        filters.add(FilterItem.Factory.createAttributeFilter(ATTR_SALES_REP, "Adam Bradley"));
        prepareReport(SIMPLE_TABULAR_REPORT, ReportTypes.TABLE, what, how, filters);
    }

    @Test(dependsOnMethods = {"createTabularReport"}, groups = {"tabular-report-exports"})
    public void verifyExportedTabularReportPDF() {
        exportReport(SIMPLE_TABULAR_REPORT, ExportFormat.PDF_PORTRAIT);
        verifyReportExport(ExportFormat.PDF_PORTRAIT, "Simple tabular report", expectedTabularReportExportPDFSize);
        String pdfContent = getContentFrom("Simple tabular report");
        //verify title
        assertThat(pdfContent, containsString("Region Priority Sales Rep Department # of Activities"));
        assertThat(pdfContent, containsString("Simple tabular report"));
        //verify content
        assertThat(pdfContent, containsString("East Coast HIGH Adam Bradley Direct Sales 1,065"));
        assertThat(pdfContent, containsString("LOW Adam Bradley Direct Sales 2,059"));
        assertThat(pdfContent, containsString("NORMAL Adam Bradley Direct Sales 1,888"));
        //verify filter
        assertThat(pdfContent, not(containsString("West Coast HIGH Alejandro Vabiano")));
    }

    @Test(dependsOnMethods = {"createTabularReport"}, groups = {"tabular-report-exports"})
    public void verifyExportXLSXDialog() {
        enableExportXLSXFlag();
        initReportPage(SIMPLE_TABULAR_REPORT).doExporting(ExportFormat.EXCEL_XLSX);
        ExportXLSXDialog dialog = reportPage.getExportXLSXDialog();

        assertTrue(dialog.isCellMergedChecked(), "Cells merged checkbox is not selected by default");
        assertTrue(dialog.isActiveFiltersChecked(), "Active Filters checkbox is not selected by default");

        setCellMergedFlag(false);
        setActiveFiltersFlag(false);
        sleepTightInSeconds(3);
        browser.navigate().refresh();
        waitForAnalysisPageLoaded(browser);
        reportPage.doExporting(ExportFormat.EXCEL_XLSX);

        assertFalse(dialog.isCellMergedChecked(), "Cells merged checkbox is checked when cell merged flag is not set");
        assertFalse(dialog.isActiveFiltersChecked(), "Active filters checkbox is checked when active filters flag is not set");
    }

    @Test(dependsOnMethods = {"verifyReportsPage"}, groups = {"goodsales-chart"})
    public void createTabularReport2() {
        List<String> what = new ArrayList<String>();
        what.add("# of Won Opps.");
        List<String> how = new ArrayList<String>();
        how.add("Region");
        how.add("Product");
        how.add("Sales Rep");
        how.add("Department");
        prepareReport("Simple tabular report - 2", ReportTypes.TABLE, what, how, emptyList());
    }

    @Test(dependsOnMethods = {"createTabularReport2"}, groups = {"tabular-report-exports"})
    public void exportTabularReportToCSV() {
        exportReport("Simple tabular report - 2", ExportFormat.CSV);
    }

    @Test(dependsOnMethods = {"exportTabularReportToCSV"}, groups = {"tabular-report-exports"})
    public void verifyExportedTabularReportCSV() {
        verifyReportExport(ExportFormat.CSV, "Simple tabular report - 2", expectedTabularReportExportCSVSize);
    }

    @Test(dependsOnMethods = {"verifyReportsPage"}, groups = {"goodsales-chart"})
    public void createTabularReport3() {
        List<String> what = new ArrayList<String>();
        what.add("# of Lost Opps.");
        List<String> how = new ArrayList<String>();
        how.add("Region");
        how.add("Product");
        how.add("Sales Rep");
        how.add("Department");
        prepareReport("Simple tabular report - 3", ReportTypes.TABLE, what, how, emptyList());
    }

    @Test(dependsOnMethods = {"verifyReportsPage"}, groups = {"goodsales-chart"})
    public void createTabularReport4() {
        List<String> what = new ArrayList<String>();
        what.add("# of Opportunities");
        List<String> how = new ArrayList<String>();
        how.add("Region");
        how.add("Product");
        how.add("Sales Rep");
        how.add("Department");
        List<FilterItem> filters = new ArrayList<FilterItem>();
        filters.add(FilterItem.Factory.createAttributeFilter(ATTR_PRODUCT, "Educationly", "Explorer"));
        filters.add(FilterItem.Factory.createAttributeFilter(ATTR_SALES_REP, "Adam Bradley", "Alejandro Vabiano"));
        prepareReport("Simple tabular report - 4", ReportTypes.TABLE, what, how, filters);
    }

    @Test(dependsOnMethods = {"createTabularReport4"}, groups = {"tabular-report-exports"})
    public void verifyExportedTabularReportXLSX() throws IOException {
        setActiveFiltersFlag(true);
        try {
            exportReport("Simple tabular report - 4", ExportFormat.EXCEL_XLSX);
            List<List<String>> xlsxContent;
            verifyReportExport(ExportFormat.EXCEL_XLSX, "Simple tabular report - 4",
                    expectedTabularReportExportXLSXSize);
            xlsxContent = excelFileToRead("Simple tabular report - 4", 0);
            //verify header title
            assertThat(xlsxContent, hasItem(asList("Region", "Product", "Sales Rep", "Department", "# of Opportunities")));
            //verify content
            assertThat(xlsxContent, hasItem(asList("East Coast", "Educationly", "Adam Bradley", "Direct Sales", "55.0")));
            assertThat(xlsxContent, hasItem(asList("East Coast", "Educationly", "Alejandro Vabiano", "Direct Sales", "53.0")));
            assertThat(xlsxContent, hasItem(asList("East Coast", "Explorer", "Adam Bradley", "Direct Sales", "42.0")));
            assertThat(xlsxContent, hasItem(asList("East Coast", "Explorer", "Alejandro Vabiano", "Direct Sales", "45.0")));
            //verify filter
            assertThat(xlsxContent, not(hasItem(asList("West Coast", "CompuSci", "John Jovi", "Direct Sales", "46.0"))));
            //verify attribute filter
            assertThat(xlsxContent, hasItem(asList("Applied filters:", "Product IN (Educationly, Explorer)")));
            assertThat(xlsxContent, hasItem(asList("Sales Rep IN (Adam Bradley, Alejandro Vabiano)")));
        } finally {
            setActiveFiltersFlag(false);
        }
    }

    @Test(dependsOnMethods = {"verifyReportsPage"}, groups = {"goodsales-chart"})
    public void createLineChartReport() {
        List<String> what = new ArrayList<String>();
        what.add("# of Activities");
        List<String> how = new ArrayList<String>();
        how.add("Region");
        how.add("Priority");
        prepareReport("Simple line chart report", ReportTypes.LINE, what, how, emptyList());
    }

    @Test(dependsOnMethods = {"createLineChartReport"}, groups = {"chart-exports"})
    public void exportLineChartToPDF() {
        exportReport("Simple line chart report", ExportFormat.PDF);
    }

    @Test(dependsOnMethods = {"exportLineChartToPDF"}, groups = {"chart-exports"})
    public void verifyExportedLineChartPDF() {
        verifyReportExport(ExportFormat.PDF, "Simple line chart report", expectedLineChartExportPDFSize);
    }

    @Test(dependsOnMethods = {"verifyReportsPage"}, groups = {"goodsales-chart"})
    public void createAreaChartReport() {
        List<String> what = new ArrayList<String>();
        what.add("# of Activities");
        List<String> how = new ArrayList<String>();
        how.add("Region");
        how.add("Priority");
        prepareReport("Simple area chart report", ReportTypes.AREA, what, how, emptyList());
    }

    @Test(dependsOnMethods = {"createAreaChartReport"}, groups = {"chart-exports"})
    public void exportAreaChartToPNG() {
        exportReport("Simple area chart report", ExportFormat.IMAGE_PNG);
    }

    @Test(dependsOnMethods = {"exportAreaChartToPNG"}, groups = {"chart-exports"})
    public void verifyExportedAreaChartPNG() {
        verifyReportExport(ExportFormat.IMAGE_PNG, "Simple area chart report", expectedAreaChartReportExportPNGSize);
    }

    @Test(dependsOnMethods = {"verifyReportsPage"}, groups = {"goodsales-chart"})
    public void createStackedAreaChartReport() {
        List<String> what = new ArrayList<String>();
        what.add("# of Activities");
        List<String> how = new ArrayList<String>();
        how.add("Region");
        how.add("Priority");
        prepareReport("Simple stacked area chart report", ReportTypes.STACKED_AREA, what, how, emptyList());
    }

    @Test(dependsOnMethods = {"verifyReportsPage"}, groups = {"goodsales-chart"})
    public void createBarChartReport() {
        List<String> what = new ArrayList<String>();
        what.add("# of Activities");
        List<String> how = new ArrayList<String>();
        how.add("Region");
        how.add("Priority");
        prepareReport("Simple bar chart report", ReportTypes.BAR, what, how, emptyList());
    }

    @Test(dependsOnMethods = {"createBarChartReport"}, groups = {"chart-exports"})
    public void exportBarChartToCSV() {
        exportReport("Simple bar chart report", ExportFormat.CSV);
    }

    @Test(dependsOnMethods = {"exportBarChartToCSV"}, groups = {"chart-exports"})
    public void verifyExportedBarChartCSV() {
        verifyReportExport(ExportFormat.CSV, "Simple bar chart report", expectedBarChartReportExportCSVSize);
    }

    @Test(dependsOnMethods = {"verifyReportsPage"}, groups = {"goodsales-chart"})
    public void createStackedBarChartReport() {
        List<String> what = new ArrayList<String>();
        what.add("# of Activities");
        List<String> how = new ArrayList<String>();
        how.add("Region");
        how.add("Priority");
        prepareReport("Simple stacked bar chart report", ReportTypes.STACKED_BAR, what, how, emptyList());
    }

    @Test(dependsOnGroups = {"goodsales-chart", "chart-exports", "tabular-report-exports"})
    public void verifyCreatedReports() {
        initReportsPage().openFolder("All");
        sleepTightInSeconds(5);
        assertEquals(ReportsPage.getInstance(browser).getReportsCount(),
                expectedGoodSalesReportsCount + createdReportsCount, "Number of expected reports (all) doesn't match");
        Screenshots.takeScreenshot(browser, "GoodSales-reports", this.getClass());
    }

    @Test(dependsOnMethods = {"verifyCreatedReports"})
    public void deleteReport() {
        initReportPage(SIMPLE_CA_REPORT).deleteCurrentReport();
        assertFalse(ReportsPage.getInstance(browser).isReportVisible(SIMPLE_CA_REPORT));
    }

    private void prepareReport(String reportName, ReportTypes reportType, List<String> what,
                               List<String> how, List<FilterItem> filterItems) {
        UiReportDefinition reportDefinition = new UiReportDefinition().withName(reportName).withType(reportType);

        if (!what.isEmpty()) {
            for (String metric : what)
                reportDefinition.withWhats(metric);
        }

        if (!how.isEmpty()) {
            for (String attribute : how)
                reportDefinition.withHows(attribute);
        }

        if (!filterItems.isEmpty()) {
            for (FilterItem filter : filterItems)
                reportDefinition.withFilters(filter);
        }

        createReport(reportDefinition, "GoodSales");
        createdReportsCount++;
    }

    private ReportPage initReportPage(String reportName) {
        return initReportsPage()
            .openFolder("My Reports")
            .openReport(reportName);
    }

    private void exportReport(String reportName, ExportFormat format) {
        initReportPage(reportName)
            .exportReport(format);
        checkRedBar(browser);
    }

    private void enableExportXLSXFlag() throws JSONException {
        new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .setFeatureFlagInProject(ProjectFeatureFlags.EXPORT_TO_XLSX_ENABLED, true);
    }

    private void setCellMergedFlag(Boolean value) throws JSONException {
        new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .setFeatureFlagInProject(ProjectFeatureFlags.CELL_MERGED_BY_DEFAULT, value);
    }

    private void setActiveFiltersFlag(Boolean value) throws JSONException {
        new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .setFeatureFlagInProject(ProjectFeatureFlags.ACTIVE_FILTERS_BY_DEFAULT, value);
    }
}
