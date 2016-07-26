package com.gooddata.qa.graphene.reports;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.CheckUtils.BY_RED_BAR;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_OPP_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_REGION;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STATUS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AVG_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_LOST;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_WON;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_WON_OPPS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_PRODUCTIVE_REPS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.VARIABLE_STATUS;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForExportReport;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.Metric;
import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.GridElement;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.Report;
import com.gooddata.md.report.ReportDefinition;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.report.HowItem;
import com.gooddata.qa.graphene.entity.report.HowItem.Position;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.utils.UrlParserUtils;
import com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils;
import com.google.common.base.Predicate;

public class GoodSalesDrillDownToExportSpecialTest extends GoodSalesAbstractTest {

    private static final String INCOMPUTABLE_REPORT = "Incomputable-Report";
    private static final String TOO_LARGE_REPORT = "Too-Large-Report";
    private static final String EMPTY_REPORT = "Empty-Report";
    private static final String REPORT_NOT_COMPUTABLE_MESSAGE = "Report not computable due to improper metric definition";
    private static final String REPORT_NO_DATA_MESSAGE = "No data match the filtering criteria";
    private static final String RAW_FORMAT = "raw";
    private static final String SALES_SEASONALITY= "Sales Seasonality";
    private static final String DRILL_DOWN_VALUE = "2,647";

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-drill-down-to-export-special-test";
    }

    @Test(dependsOnGroups = { "createProject" })
    public void testCreatingLargeReport() {
        initReportCreation().createReport(new UiReportDefinition()
                .withName(TOO_LARGE_REPORT)
                .withWhats(METRIC_AMOUNT, METRIC_AVG_AMOUNT)
                .withHows(new HowItem(ATTR_OPP_SNAPSHOT, Position.TOP)));
        assertTrue(reportPage.waitForReportExecutionProgress().isReportTooLarge(),
                "The created report is not large report type");
    }

    @Test(dependsOnGroups = { "createProject" })
    public void testCreatingIncomputableReport() {
        final String amountUri = getMdService().getObjUri(getProject(), Metric.class, title(METRIC_AMOUNT));
        final String activityUri = getMdService().getObj(getProject(), Attribute.class, title(ATTR_ACTIVITY))
                .getDefaultDisplayForm().getUri();
        ReportDefinition definition = GridReportDefinitionContent.create(INCOMPUTABLE_REPORT,
                singletonList("metricGroup"),
                singletonList(new AttributeInGrid(activityUri)),
                singletonList(new GridElement(amountUri, METRIC_AMOUNT)));
        definition = getMdService().createObj(getProject(), definition);
        getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));

        assertEquals(openReport(INCOMPUTABLE_REPORT).getInvalidDataReportMessage(), REPORT_NOT_COMPUTABLE_MESSAGE,
                "The created report is not incomputable type");
    }

    @Test(dependsOnGroups = { "createProject" })
    public void testCreatingEmptyReport() {
        initReportCreation().createReport(new UiReportDefinition()
                .withName(EMPTY_REPORT)
                .withWhats(METRIC_LOST)
                .withHows(ATTR_STATUS)
                .withFilters(FilterItem.Factory.createPromptFilter(VARIABLE_STATUS)));
        assertEquals(reportPage.waitForReportExecutionProgress().getDataReportHelpMessage(), REPORT_NO_DATA_MESSAGE,
                "The created report is not empty type");
    }

    @DataProvider(name = "exportFormatDataProvider")
    public Object[][] exportFormatDataProvider() {
        return new Object[][] {
            {ExportFormat.CSV},
            {ExportFormat.EXCEL_XLSX}
        };
    }

    @Test(dependsOnMethods = { "testCreatingLargeReport" }, dataProvider = "exportFormatDataProvider")
    public void testRedBarVisibleForExportLargeReport(ExportFormat format) throws JSONException, IOException {
        final String dashboard = "Dashboard-For-Export-Large-Report-Using-" + format.getLabel();
        createDashboardForExportTest(dashboard, SALES_SEASONALITY, METRIC_NUMBER_OF_WON, TOO_LARGE_REPORT);
        try {
            setDrillReportTargetAsExport(format.getName());
            dashboardsPage.getContent().getLatestReport(TableReport.class).drillOnMetricValue(DRILL_DOWN_VALUE);
            checkRebBarVisible();
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = { "testCreatingIncomputableReport" }, dataProvider = "exportFormatDataProvider")
    public void testRedBarVisibleForExportIncomputableReport(ExportFormat format) throws JSONException, IOException {
        final String dashboard = "Dashboard-For-Export-Incomputable-Report-Using-" + format.getLabel();
        createDashboardForExportTest(dashboard, SALES_SEASONALITY, METRIC_NUMBER_OF_WON, INCOMPUTABLE_REPORT);
        try {
            setDrillReportTargetAsExport(format.getName());
            dashboardsPage.getContent().getLatestReport(TableReport.class).drillOnMetricValue(DRILL_DOWN_VALUE);
            checkRebBarVisible();
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = { "testCreatingEmptyReport" }, dataProvider = "exportFormatDataProvider")
    public void testRedBarNotVisibleForExportEmptyReport(ExportFormat format) throws JSONException, IOException {
        final String dashboard = "Dashboard-For-Export-Empty-Report-Using-" + format.getLabel();
        createDashboardForExportTest(dashboard, SALES_SEASONALITY, METRIC_NUMBER_OF_WON, EMPTY_REPORT);
        try {
            setDrillReportTargetAsExport(format.getName());
            dashboardsPage.getContent().getLatestReport(TableReport.class).drillOnMetricValue(DRILL_DOWN_VALUE);
            checkRedBar(browser);
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnGroups = { "createProject" })
    public void exportReportUsingRawFormat() throws JSONException, IOException {
        final String dashboard = "Dashboard-For-Export-Report-Using-Raw-Format";
        final String report = "Report-For-Export-Report-Using-Raw-Format";
        final String drillDownReport = "Report-For-Drill-Down";
        createSimpleReport(report, METRIC_NUMBER_OF_ACTIVITIES, ATTR_ACTIVITY_TYPE);
        createSimpleReport(drillDownReport, METRIC_NUMBER_OF_WON_OPPS, ATTR_REGION);
        createDashboardForExportTest(dashboard, report, METRIC_NUMBER_OF_ACTIVITIES, drillDownReport);
        try {
            setDrillReportTargetAsExport(RAW_FORMAT);
            dashboardsPage.getContent().getLatestReport(TableReport.class).drillOnMetricValue();
            final File exportFile = new File(testParams.getDownloadFolder(), "Email.csv");
            waitForExportReport(exportFile, 69);
            assertEquals(readCsvFile(exportFile), asList(asList("East Coast", "1060"), asList("West Coast", "2251")),
                    "The content of export file is not correct");
            checkRedBar(browser);
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = { "testCreatingLargeReport" })
    public void exportLargeReportUsingRawFormat() throws JSONException, IOException  {
        final String dashboard = "Dashboard-For-Export-Large-Report-Using-Raw-Format";
        final String report = "Report-For-Export-Large-Report-Using-Raw-Format";
        createSimpleReport(report, METRIC_PRODUCTIVE_REPS, ATTR_PRODUCT);
        createDashboardForExportTest(dashboard, report, METRIC_PRODUCTIVE_REPS, TOO_LARGE_REPORT);
        try {
            setDrillReportTargetAsExport(RAW_FORMAT);
            dashboardsPage.getContent().getLatestReport(TableReport.class).drillOnMetricValue();
            final File exportFile = new File(testParams.getDownloadFolder(), "CompuSci.csv");
            waitForExportReport(exportFile, 2042960);
            assertEquals(readCsvFile(exportFile).size(), 69923, "The number of lines in export file is not correct");
            checkRedBar(browser);
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    private List<List<String>> readCsvFile(final File file) throws FileNotFoundException, IOException {
        List<List<String>> actualResult = new ArrayList<>();
        try (CsvListReader reader = new CsvListReader(new FileReader(file), CsvPreference.STANDARD_PREFERENCE)) {
            reader.getHeader(true);
            List<String> reportResult;
            while ((reportResult = reader.read()) != null) {
                actualResult.add(reportResult);
            }
        }
        return actualResult;
    }

    private void createDashboardForExportTest(final String dashboard, final String report, final String drillDownValue,
            final String drillDownReport) {
        initDashboardsPage().addNewDashboard(dashboard)
                .editDashboard()
                .addReportToDashboard(report);
        dashboardsPage.getContent()
                .getLatestReport(TableReport.class)
                .addDrilling(Pair.of(singletonList(drillDownValue), drillDownReport), "Reports");
        dashboardsPage.getDashboardEditBar().saveDashboard();
    }

    private void createSimpleReport(final String report, final String metric, final String attribute) {
        final String metricUri = getMdService().getObjUri(getProject(), Metric.class, title(metric));
        final String attributeUri = getMdService().getObj(getProject(), Attribute.class, title(attribute))
                .getDefaultDisplayForm().getUri();
        ReportDefinition definition = GridReportDefinitionContent.create(report, singletonList("metricGroup"),
                singletonList(new AttributeInGrid(attributeUri)),
                singletonList(new GridElement(metricUri, metric)));
        definition = getMdService().createObj(getProject(), definition);
        getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));
    }

    private void setDrillReportTargetAsExport(final String format) throws JSONException, IOException {
        final String workingDashboard = dashboardsPage.getDashboardName();
        DashboardsRestUtils.setDrillReportTargetAsExport(getRestApiClient(), testParams.getProjectId(),
                UrlParserUtils.getObjId(browser.getCurrentUrl()), format);
        //refresh to make sure drill settings are applied 
        browser.navigate().refresh();
        waitForDashboardPageLoaded(browser);
        //make sure that we are on correct dashboard
        dashboardsPage.selectDashboard(workingDashboard);
        waitForDashboardPageLoaded(browser);
    }

    private void checkRebBarVisible() {
        try {
            Predicate<WebDriver> isRedBarVisible = browser -> isElementVisible(BY_RED_BAR, browser);
            Graphene.waitGui().withTimeout(20, TimeUnit.SECONDS).until(isRedBarVisible);
        } catch (TimeoutException e) {
            fail("Red Bar is not displayed");
        }
    }
}
