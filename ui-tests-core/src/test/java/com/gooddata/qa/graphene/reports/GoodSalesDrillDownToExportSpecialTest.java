package com.gooddata.qa.graphene.reports;

import com.gooddata.sdk.model.md.Attribute;
import com.gooddata.sdk.model.md.Metric;
import com.gooddata.sdk.model.md.report.AttributeInGrid;
import com.gooddata.sdk.model.md.report.GridReportDefinitionContent;
import com.gooddata.sdk.model.md.report.MetricElement;
import com.gooddata.sdk.model.md.report.Report;
import com.gooddata.sdk.model.md.report.ReportDefinition;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.report.HowItem;
import com.gooddata.qa.graphene.entity.report.HowItem.Position;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport.CellType;
import com.gooddata.qa.graphene.utils.UrlParserUtils;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import static com.gooddata.sdk.model.md.Restriction.title;
import static com.gooddata.sdk.model.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.utils.CheckUtils.BY_RED_BAR;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_OPP_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_REGION;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AVG_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_LOST;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_WON_OPPS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_PRODUCTIVE_REPS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_SALES_SEASONALITY;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForExporting;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class GoodSalesDrillDownToExportSpecialTest extends GoodSalesAbstractTest {

    private static final String INCOMPUTABLE_REPORT = "Incomputable-Report";
    private static final String TOO_LARGE_REPORT = "Too-Large-Report";
    private static final String EMPTY_REPORT = "Empty-Report";
    private static final String REPORT_NOT_COMPUTABLE_MESSAGE = "Report not computable due to improper metric definition";
    private static final String REPORT_NO_DATA_MESSAGE = "No data match the filtering criteria";
    private static final String RAW_FORMAT = "raw";
    private static final String DRILL_DOWN_VALUE = "2,647";
    private static final String NO = "no";
    private static final String YES = "yes";

    private DashboardRestRequest dashboardRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle = "GoodSales-drill-down-to-export-special-test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        dashboardRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
        Metrics metricCreator = getMetricCreator();
        metricCreator.createNumberOfActivitiesMetric();
        metricCreator.createWonMetric();
        metricCreator.createNumberOfWonOppsMetric();
        metricCreator.createProductiveRepsMetric();
        metricCreator.createLostMetric();
        metricCreator.createAvgAmountMetric();
        metricCreator.createAvgWonMetric();
        getVariableCreator().createStatusVariable();

        getReportCreator().createSalesSeasonalityReport();
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
        final Metric amountMetric = getMdService().getObj(getProject(), Metric.class, title(METRIC_AMOUNT));
        final Attribute activity = getMdService().getObj(getProject(), Attribute.class, title(ATTR_ACTIVITY));
        ReportDefinition definition = GridReportDefinitionContent.create(INCOMPUTABLE_REPORT,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(activity.getDefaultDisplayForm().getUri(), activity.getTitle())),
                singletonList(new MetricElement(amountMetric)));
        definition = getMdService().createObj(getProject(), definition);
        getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));

        assertEquals(initReportsPage().openReport(INCOMPUTABLE_REPORT).getInvalidDataReportMessage(), REPORT_NOT_COMPUTABLE_MESSAGE,
                "The created report is not incomputable type");
    }

    @Test(dependsOnGroups = { "createProject" })
    public void testCreatingEmptyReport() {
        createReport(GridReportDefinitionContent.create(EMPTY_REPORT,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByIdentifier("attr.stage.status"))),
                singletonList(new MetricElement(getMetricByTitle(METRIC_LOST)))));

        initReportsPage().openReport(EMPTY_REPORT);
        reportPage.addFilter(FilterItem.Factory.createPromptFilter("Status"));
        assertEquals(reportPage.waitForReportExecutionProgress().getDataReportHelpMessage(), REPORT_NO_DATA_MESSAGE,
                "The created report is not empty type");
    }

    @DataProvider(name = "exportFormatDataProvider")
    public Object[][] exportFormatDataProvider() {
        return new Object[][] {
            {ExportFormat.CSV, NO, NO},
            {ExportFormat.EXCEL_XLSX, NO, NO},
            {ExportFormat.EXCEL_XLSX, YES, NO},
            {ExportFormat.EXCEL_XLSX, NO, YES},
            {ExportFormat.EXCEL_XLSX, YES, YES}
        };
    }

    @Test(dependsOnMethods = { "testCreatingLargeReport" }, dataProvider = "exportFormatDataProvider")
    public void testRedBarVisibleForExportLargeReport(ExportFormat format, String mergeHeaders, String includeFilterContext) throws JSONException, IOException {
        final String dashboard = "Dashboard-For-Export-Large-Report-Using-" + format.getLabel();
        createDashboardForExportTest(dashboard, REPORT_SALES_SEASONALITY, METRIC_NUMBER_OF_WON_OPPS, TOO_LARGE_REPORT);
        try {
            setDrillReportTargetAsExport(format.getName(), mergeHeaders, includeFilterContext);
            dashboardsPage.getContent().getLatestReport(TableReport.class).drillOn(DRILL_DOWN_VALUE, CellType.METRIC_VALUE);
            checkRebBarVisible();
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = { "testCreatingIncomputableReport" }, dataProvider = "exportFormatDataProvider")
    public void testRedBarVisibleForExportIncomputableReport(ExportFormat format, String mergeHeaders, String includeFilterContext) throws JSONException, IOException {
        final String dashboard = "Dashboard-For-Export-Incomputable-Report-Using-" + format.getLabel();
        createDashboardForExportTest(dashboard, REPORT_SALES_SEASONALITY, METRIC_NUMBER_OF_WON_OPPS, INCOMPUTABLE_REPORT);
        try {
            setDrillReportTargetAsExport(format.getName(), mergeHeaders, includeFilterContext);
            dashboardsPage.getContent().getLatestReport(TableReport.class).drillOn(DRILL_DOWN_VALUE, CellType.METRIC_VALUE);
            checkRebBarVisible();
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = { "testCreatingEmptyReport" }, dataProvider = "exportFormatDataProvider")
    public void testRedBarNotVisibleForExportEmptyReport(ExportFormat format, String mergeHeaders, String includeFilterContext) throws JSONException, IOException {
        final String dashboard = "Dashboard-For-Export-Empty-Report-Using-" + format.getLabel();
        createDashboardForExportTest(dashboard, REPORT_SALES_SEASONALITY, METRIC_NUMBER_OF_WON_OPPS, EMPTY_REPORT);
        try {
            setDrillReportTargetAsExport(format.getName(), mergeHeaders, includeFilterContext);
            dashboardsPage.getContent().getLatestReport(TableReport.class).drillOn(DRILL_DOWN_VALUE, CellType.METRIC_VALUE);
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
            setDrillReportTargetAsExport(RAW_FORMAT, "yes", "yes");
            dashboardsPage.getContent().getLatestReport(TableReport.class).drillOnFirstValue(CellType.METRIC_VALUE);
            final File exportFile = new File(testParams.getDownloadFolder(), "Email.csv");
            waitForExporting(exportFile);
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
            setDrillReportTargetAsExport(RAW_FORMAT, "yes", "yes");
            dashboardsPage.getContent().getLatestReport(TableReport.class).drillOnFirstValue(CellType.METRIC_VALUE);
            final File exportFile = new File(testParams.getDownloadFolder(), "CompuSci.csv");
            waitForExporting(exportFile);
            assertEquals(readCsvFile(exportFile).size(), 69923, "The number of lines in export file is not correct");
            checkRedBar(browser);
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    private List<List<String>> readCsvFile(final File file) throws IOException {
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
        //wait for report loading finish before adding drilling. Otherwise, the select item popup panel on
        //drilling setting is disappeared when report finishes loading (might be due to lost focus)
                .waitForLoaded()
                .addDrilling(Pair.of(singletonList(drillDownValue), drillDownReport), "Reports");
        dashboardsPage.getDashboardEditBar().saveDashboard();
    }

    private void createSimpleReport(final String report, final String metric, final String attribute) {
        final Metric metricObj = getMdService().getObj(getProject(), Metric.class, title(metric));
        final Attribute attr = getMdService().getObj(getProject(), Attribute.class, title(attribute));
        ReportDefinition definition = GridReportDefinitionContent.create(report, singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(attr.getDefaultDisplayForm().getUri(), attr.getTitle())),
                singletonList(new MetricElement(metricObj)));
        definition = getMdService().createObj(getProject(), definition);
        getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));
    }

    private void setDrillReportTargetAsExport(final String format, String mergeHeaders, String includeFilterContext) throws JSONException, IOException {
        final String workingDashboard = dashboardsPage.getDashboardName();
        dashboardRequest.setDrillReportTargetAsExport(UrlParserUtils.getObjId(browser.getCurrentUrl()), format, mergeHeaders, includeFilterContext);
        //refresh to make sure drill settings are applied
        browser.navigate().refresh();
        waitForDashboardPageLoaded(browser);
        //make sure that we are on correct dashboard
        dashboardsPage.selectDashboard(workingDashboard);
        waitForDashboardPageLoaded(browser);
    }

    private void checkRebBarVisible() {
        try {
            Function<WebDriver, Boolean> isRedBarVisible = browser -> isElementVisible(BY_RED_BAR, browser);
            Graphene.waitGui().withTimeout(20, TimeUnit.SECONDS).until(isRedBarVisible);
        } catch (TimeoutException e) {
            fail("Red Bar is not displayed");
        }
    }
}
