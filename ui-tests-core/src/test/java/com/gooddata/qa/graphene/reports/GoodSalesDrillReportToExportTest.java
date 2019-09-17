package com.gooddata.qa.graphene.reports;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.report.HowItem;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardDrillDialog;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport.CellType;
import com.gooddata.qa.utils.asserts.AssertUtils;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTight;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.XlsxUtils.excelFileToRead;
import static com.gooddata.qa.utils.asserts.AssertUtils.assertIgnoreCaseAndIndex;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class GoodSalesDrillReportToExportTest extends GoodSalesAbstractTest {

    private static final String TEST_DASHBOARD_NAME = "test-drill-report-to-export";
    private static final String REPORT_NAME = "Drill report to export";
    private static final String NO = "no";
    private static final String YES = "yes";
    private DashboardRestRequest dashboardRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle = "GoodSales-test-drill-report-to-export";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createAmountMetric();
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createOppFirstSnapshotMetric();
        getMetricCreator().createCloseEOPMetric();
        dashboardRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createDrillReportToExport() {
        initReportsPage();
        UiReportDefinition reportDefinition = new UiReportDefinition()
                .withName(REPORT_NAME)
                .withWhats(METRIC_AMOUNT)
                .withHows(new HowItem(ATTR_STAGE_NAME, HowItem.Position.LEFT))
                .withHows(new HowItem(ATTR_DEPARTMENT, HowItem.Position.LEFT));
        createReport(reportDefinition, REPORT_NAME);
        checkRedBar(browser);

    }

    @Test(dependsOnMethods = {"createDrillReportToExport"})
    public void drillAcrossReportToExport() throws IOException, JSONException {
        List<List<String>> expectedValues = asList(
                asList("Region", "Department", "Amount"),
                asList("East Coast", "Direct Sales", "1736919.98"),
                asList("East Coast", "Inside Sales", "76871.0"),
                asList("West Coast", "Direct Sales", "1.522844584E7"),
                asList("West Coast", "Inside Sales", "1405029.32"),
                asList("Rollup", "1.844726614E7"));
        try {
            addReportToNewDashboard(REPORT_NAME, TEST_DASHBOARD_NAME);
            dashboardsPage.editDashboard();
            TableReport tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.addDrilling(Pair.of(asList("Department", "Stage Name"), "Region"));
            dashboardsPage.saveDashboard();
            setDrillReportTargetAsExport(ExportFormat.EXCEL_XLSX.getName(), NO, NO);
            tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.drillOn("Interest", CellType.ATTRIBUTE_VALUE);
            sleepTight(4000);
            Screenshots.takeScreenshot(browser, "Drill report Interest", GoodSalesDrillReportToExportTest.class);
            verifyReportExport(ExportFormat.EXCEL_XLSX, "Interest");
            String fileURL = testParams.getDownloadFolder() + testParams.getFolderSeparator() + "Interest" + "."
                    + ExportFormat.EXCEL_XLSX.getName();
            List<List<String>> actualValues = excelFileToRead(fileURL,0);
            assertEquals(actualValues, expectedValues);
            checkRedBar(browser);
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createDrillReportToExport"})
    public void drillAcrossReportToExportWithMergeHeader() throws IOException, JSONException {
        List<List<String>> expectedValues = asList(
                asList("Region", "Department", "Amount"),
                asList("East Coast", "Direct Sales", "739054.69"),
                asList("Inside Sales", "209399.35"),
                asList("West Coast", "Direct Sales", "2651451.23"),
                asList("Inside Sales", "649122.61"),
                asList("Rollup", "4249027.88"));
        try {
            addReportToNewDashboard(REPORT_NAME, TEST_DASHBOARD_NAME);
            dashboardsPage.editDashboard();
            TableReport tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.addDrilling(Pair.of(Arrays.asList("Department", "Stage Name"), "Region"));
            dashboardsPage.saveDashboard();
            setDrillReportTargetAsExport(ExportFormat.EXCEL_XLSX.getName(), YES, NO);
            tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.drillOn("Discovery", CellType.ATTRIBUTE_VALUE);
            sleepTight(4000);
            Screenshots.takeScreenshot(browser, "Drill report Discovery", GoodSalesDrillReportToExportTest.class);
            verifyReportExport(ExportFormat.EXCEL_XLSX, "Discovery");
            String fileURL = testParams.getDownloadFolder() + testParams.getFolderSeparator() + "Discovery" + "."
                    + ExportFormat.EXCEL_XLSX.getName();
            List<List<String>> actualValues = excelFileToRead(fileURL,0);
            assertEquals(actualValues, expectedValues);
            checkRedBar(browser);
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createDrillReportToExport"})
    public void drillAcrossReportToExportWithIncludeFilterContent() throws IOException, JSONException {
        List<List<String>> expectedValues = asList(
                asList("Applied filters:", "Stage Name IN (Short List)"),
                asList("Region", "Department", "Amount"),
                asList("East Coast", "Direct Sales", "1094194.05"),
                asList("East Coast", "Inside Sales", "174467.75"),
                asList("West Coast", "Direct Sales", "2578220.45"),
                asList("West Coast", "Inside Sales", "1765180.35"),
                asList("Rollup", "5612062.6"));
        try {
            addReportToNewDashboard(REPORT_NAME, TEST_DASHBOARD_NAME);
            dashboardsPage.editDashboard();
            TableReport tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.addDrilling(Pair.of(Arrays.asList("Department", "Stage Name"), "Region"));
            dashboardsPage.saveDashboard();
            setDrillReportTargetAsExport(ExportFormat.EXCEL_XLSX.getName(), NO, YES);
            tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.drillOn("Short List", CellType.ATTRIBUTE_VALUE);
            sleepTight(4000);
            Screenshots.takeScreenshot(browser, "Drill report Short List", GoodSalesDrillReportToExportTest.class);
            verifyReportExport(ExportFormat.EXCEL_XLSX, "Short List");
            String fileURL = testParams.getDownloadFolder() + testParams.getFolderSeparator() + "Short List" + "."
                    + ExportFormat.EXCEL_XLSX.getName();
            List<List<String>> actualValues = excelFileToRead(fileURL,0);
            assertEquals(actualValues, expectedValues);
            checkRedBar(browser);
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createDrillReportToExport"})
    public void drillAcrossReportToExportWithMergeHeaderAndIncludeFilterContent() throws IOException, JSONException {
        List<List<String>> expectedValues = asList(
                asList("Applied filters:", "Stage Name IN (Risk Assessment)"),
                asList("Region", "Department", "Amount"),
                asList("East Coast", "Direct Sales", "1000533.73"),
                asList("Inside Sales", "444736.24"),
                asList("West Coast", "Direct Sales", "606883.42"),
                asList("Inside Sales", "554140.07"),
                asList("Rollup", "2606293.46"));
        try {
            addReportToNewDashboard(REPORT_NAME, TEST_DASHBOARD_NAME);
            dashboardsPage.editDashboard();
            TableReport tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.addDrilling(Pair.of(Arrays.asList("Department", "Stage Name"), "Region"));
            dashboardsPage.saveDashboard();
            setDrillReportTargetAsExport(ExportFormat.EXCEL_XLSX.getName(), YES, YES);
            tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.drillOn("Risk Assessment", CellType.ATTRIBUTE_VALUE);
            sleepTight(4000);
            Screenshots.takeScreenshot(browser, "Drill report Risk Assessment", GoodSalesDrillReportToExportTest.class);
            verifyReportExport(ExportFormat.EXCEL_XLSX, "Risk Assessment");
            String fileURL = testParams.getDownloadFolder() + testParams.getFolderSeparator() + "Risk Assessment" + "."
                    + ExportFormat.EXCEL_XLSX.getName();
            List<List<String>> actualValues = excelFileToRead(fileURL,0);
            assertEquals(actualValues, expectedValues);
            checkRedBar(browser);
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createDrillReportToExport"})
    public void changeSettingsInDrillReportToExport() throws JSONException, IOException {
        try {
            String targetReportName = "Target Report";
            initReportsPage();
            UiReportDefinition reportDefinition = new UiReportDefinition()
                    .withName(targetReportName)
                    .withWhats(METRIC_NUMBER_OF_ACTIVITIES)
                    .withHows(new HowItem(ATTR_DEPARTMENT, HowItem.Position.LEFT))
                    .withHows(new HowItem(ATTR_ACTIVITY_TYPE, HowItem.Position.LEFT));
            createReport(reportDefinition, REPORT_NAME);
            checkRedBar(browser);

            addReportToNewDashboard(REPORT_NAME, TEST_DASHBOARD_NAME);
            dashboardsPage.editDashboard();
            TableReport tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.addDrilling(Pair.of(Arrays.asList("Stage Name"), targetReportName), "Reports");
            dashboardsPage.saveDashboard();
            setDrillReportTargetAsExport(ExportFormat.CSV.getName(), YES, YES);
            tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.drillOn("Discovery", CellType.ATTRIBUTE_VALUE);
            sleepTight(6000);
            verifyReportExport(ExportFormat.EXCEL_XLSX, "Discovery");
            checkRedBar(browser);

            setDrillReportTargetAsExport(ExportFormat.CSV.getName(), NO, NO);
            tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.drillOn("Short List", CellType.ATTRIBUTE_VALUE);
            sleepTight(4000);
            verifyReportExport(ExportFormat.CSV, "Short List");
            checkRedBar(browser);

            dashboardsPage.editDashboard();
            tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.editDrilling(Pair.of(Arrays.asList("Stage Name"), targetReportName),
                    Pair.of(Arrays.asList("Stage Name"), "Account"), "Attributes");
            dashboardsPage.saveDashboard();
            tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.drillOn("Risk Assessment", CellType.ATTRIBUTE_VALUE);
            sleepTight(4000);
            verifyReportExport(ExportFormat.CSV, "Risk Assessment");
            checkRedBar(browser);

            setDrillReportTargetAsPopup();
            drillReportToPopupDialog("Interest");

        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createDrillReportToExport"})
    public void checkDrillToExportNotCached() throws JSONException, IOException {
        try {
            addReportToNewDashboard(REPORT_NAME, TEST_DASHBOARD_NAME);
            dashboardsPage.editDashboard();
            TableReport tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.addDrilling(Pair.of(Arrays.asList("Stage Name"), "Account"));
            dashboardsPage.saveDashboard();
            setDrillReportTargetAsExport(ExportFormat.EXCEL_XLSX.getName(), YES, YES);
            dashboardsPage.editDashboard();
            tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.deleteDrilling(Arrays.asList("Stage Name"));
            tableReport.addDrilling(Pair.of(Arrays.asList("Stage Name"), "Account"));
            dashboardsPage.saveDashboard();
            drillReportToPopupDialog("Short List");
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    private void setDrillReportTargetAsPopup() throws JSONException, IOException {
        dashboardRequest.setDrillReportTargetAsPopup(getDashboardID());
        browser.navigate().refresh();
        waitForDashboardPageLoaded(browser);
    }

    private void setDrillReportTargetAsExport(String exportFormat, String mergeHeaders, String includeFilterContext) throws JSONException, IOException {
        dashboardRequest.setDrillReportTargetAsExport(getDashboardID(), exportFormat, mergeHeaders, includeFilterContext);
        browser.navigate().refresh();
        waitForDashboardPageLoaded(browser);
    }

    private String getDashboardID() {
        String currentURL = browser.getCurrentUrl();
        return currentURL.substring(currentURL.lastIndexOf("/obj/") + 5, currentURL.lastIndexOf("|"));
    }

    private void drillReportToPopupDialog(String selectedAttributeName) {
        TableReport tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
        tableReport.drillOn(selectedAttributeName, CellType.ATTRIBUTE_VALUE);
        DashboardDrillDialog drillDialog =
                Graphene.createPageFragment(DashboardDrillDialog.class,
                        waitForElementVisible(DashboardDrillDialog.LOCATOR, browser));
        tableReport = drillDialog.getReport(TableReport.class);
        assertTrue(tableReport.hasValue("Rollup", CellType.TOTAL_HEADER), "Total header should have roll up");
        AssertUtils.assertIgnoreCase(tableReport.getAttributeHeaders(), asList("Account", "Department"));
        assertIgnoreCaseAndIndex(tableReport.getMetricHeaders(), Sets.newHashSet("Amount"));
        assertEquals(drillDialog.getBreadcrumbsString(),
                StringUtils.join(asList("Drill report to export", selectedAttributeName), ">>"));
        drillDialog.closeDialog();
    }
}
