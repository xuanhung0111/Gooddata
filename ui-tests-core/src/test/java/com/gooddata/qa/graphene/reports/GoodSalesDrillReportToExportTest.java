package com.gooddata.qa.graphene.reports;

import static com.gooddata.qa.graphene.utils.CheckUtils.*;
import static org.testng.Assert.*;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.report.ReportDefinition;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardDrillDialog;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.utils.http.RestUtils;

@Test(groups = {"GoodSalesDrillReportToExport"}, description = "Drill report to export")
public class GoodSalesDrillReportToExportTest extends GoodSalesAbstractTest {
    
    private static final String TEST_DASHBOAD_NAME = "test-drill-report-to-export";
    private static final String REPORT_NAME = "Drill report to export";
    
    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-test-drill-report-to-export";
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createDrillReportToExport() throws InterruptedException {
        initReportsPage();
        ReportDefinition reportDefinition = new ReportDefinition()
            .withName(REPORT_NAME)
            .withWhats("Amount")
            .withHows("Stage Name");
        createReport(reportDefinition, REPORT_NAME);
        checkRedBar(browser);
    }

    @Test(dependsOnMethods = {"createDrillReportToExport"})
    public void drillAcrossReportToExport() throws InterruptedException, IOException, JSONException {
        try {
            addReportToNewDashboard(REPORT_NAME, TEST_DASHBOAD_NAME);
            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardsPage.editDashboard();
            TableReport tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.addDrilling(Pair.of(Arrays.asList("Stage Name"), "Account"));
            dashboardEditBar.saveDashboard();
            setDrillReportTargetAsExport(ExportFormat.CSV.getName());
            tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.drillOnAttributeValue();
            Thread.sleep(4000);
            verifyReportExport(ExportFormat.CSV, "Interest", 6000);
            checkRedBar(browser);
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createDrillReportToExport"})
    public void changeSettingsInDrillReportToExport() throws InterruptedException, JSONException, IOException {
        try {
            String targetReportName = "Target Report";
            initReportsPage();
            ReportDefinition reportDefinition = new ReportDefinition()
                .withName(targetReportName)
                .withWhats("# of Activities")
                .withHows("Activity Type");
            createReport(reportDefinition, REPORT_NAME);
            checkRedBar(browser);
            
            addReportToNewDashboard(REPORT_NAME, TEST_DASHBOAD_NAME);
            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardsPage.editDashboard();
            TableReport tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.addDrilling(Pair.of(Arrays.asList("Stage Name"), targetReportName), "Reports");
            dashboardEditBar.saveDashboard();
            setDrillReportTargetAsExport(ExportFormat.EXCEL_XLSX.getName());
            tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.drillOnAttributeValue("Discovery");
            Thread.sleep(6000);
            verifyReportExport(ExportFormat.EXCEL_XLSX, "Discovery", 5510);
            checkRedBar(browser);
            
            setDrillReportTargetAsExport(ExportFormat.CSV.getName());
            tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.drillOnAttributeValue("Short List");
            Thread.sleep(4000);
            verifyReportExport(ExportFormat.CSV, "Short List", 120);
            checkRedBar(browser);
            
            dashboardsPage.editDashboard();
            tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.editDrilling(Pair.of(Arrays.asList("Stage Name"), targetReportName), 
                    Pair.of(Arrays.asList("Stage Name"), "Account"), "Attributes");
            dashboardEditBar.saveDashboard();
            tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.drillOnAttributeValue("Risk Assessment");
            Thread.sleep(4000);
            verifyReportExport(ExportFormat.CSV, "Risk Assessment", 1295);
            checkRedBar(browser);
            
            setDrillReportTargetAsPopup();
            drillReportToPopupDialog("Conviction");
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createDrillReportToExport"})
    public void checkDrillToExportNotCached() throws InterruptedException, JSONException, IOException {
        try {
            addReportToNewDashboard(REPORT_NAME, TEST_DASHBOAD_NAME);
            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardsPage.editDashboard();
            TableReport tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.addDrilling(Pair.of(Arrays.asList("Stage Name"), "Account"));
            dashboardEditBar.saveDashboard();
            setDrillReportTargetAsExport(ExportFormat.CSV.getName());
            dashboardsPage.editDashboard();
            tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.deleteDrilling(Arrays.asList("Stage Name"));
            tableReport.addDrilling(Pair.of(Arrays.asList("Stage Name"), "Account"));
            dashboardEditBar.saveDashboard();
            drillReportToPopupDialog("Negotiation");
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }
    
    private void setDrillReportTargetAsPopup() throws JSONException, IOException {
        RestUtils.setDrillReportTargetAsPopup(getRestApiClient(), testParams.getProjectId(), getDashboardID());
        browser.navigate().refresh();
        waitForDashboardPageLoaded(browser);
    }
    
    private void setDrillReportTargetAsExport(String exportFormat) throws JSONException, IOException {
        RestUtils.setDrillReportTargetAsExport(getRestApiClient(), testParams.getProjectId(), getDashboardID(), exportFormat);
        browser.navigate().refresh();
        waitForDashboardPageLoaded(browser);
    }

    private String getDashboardID() {
        String currentURL = browser.getCurrentUrl();
        return currentURL.substring(currentURL.lastIndexOf("/obj/")+5 , currentURL.lastIndexOf("|"));
    }
    
    private void drillReportToPopupDialog(String selectedAttributeName) {
        TableReport tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
        tableReport.drillOnAttributeValue(selectedAttributeName);
        DashboardDrillDialog drillDialog = 
                Graphene.createPageFragment(DashboardDrillDialog.class,
                        waitForElementVisible(DashboardDrillDialog.LOCATOR, browser));
        tableReport = drillDialog.getReport(TableReport.class);
        assertTrue(tableReport.isRollupTotalVisible());
        assertEquals(tableReport.getAttributesHeader(), Arrays.asList("Account"));
        assertEquals(tableReport.getMetricsHeader(), Arrays.asList("Amount"));
        assertEquals(drillDialog.getBreadcrumbsString(), 
                StringUtils.join(Arrays.asList("Drill report to export", selectedAttributeName), ">>"));
        drillDialog.closeDialog();
    }
}
