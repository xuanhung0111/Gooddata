package com.gooddata.qa.graphene.reports;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardDrillDialog;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport.CellType;
import com.gooddata.qa.utils.asserts.AssertUtils;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTight;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.asserts.AssertUtils.assertIgnoreCaseAndIndex;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class GoodSalesDrillReportToExportTest extends GoodSalesAbstractTest {
    
    private static final String TEST_DASHBOAD_NAME = "test-drill-report-to-export";
    private static final String REPORT_NAME = "Drill report to export";

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
        dashboardRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createDrillReportToExport() {
        initReportsPage();
        UiReportDefinition reportDefinition = new UiReportDefinition()
            .withName(REPORT_NAME)
            .withWhats("Amount")
            .withHows("Stage Name");
        createReport(reportDefinition, REPORT_NAME);
        checkRedBar(browser);
    }

    @Test(dependsOnMethods = {"createDrillReportToExport"})
    public void drillAcrossReportToExport() throws IOException, JSONException {
        try {
            addReportToNewDashboard(REPORT_NAME, TEST_DASHBOAD_NAME);
            dashboardsPage.editDashboard();
            TableReport tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.addDrilling(Pair.of(Arrays.asList("Stage Name"), "Account"));
            dashboardsPage.saveDashboard();
            setDrillReportTargetAsExport(ExportFormat.CSV.getName());
            tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.drillOnFirstValue(CellType.ATTRIBUTE_VALUE);
            sleepTight(4000);
            verifyReportExport(ExportFormat.CSV, "Interest", 6000);
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
                .withWhats("# of Activities")
                .withHows("Activity Type");
            createReport(reportDefinition, REPORT_NAME);
            checkRedBar(browser);
            
            addReportToNewDashboard(REPORT_NAME, TEST_DASHBOAD_NAME);
            dashboardsPage.editDashboard();
            TableReport tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.addDrilling(Pair.of(Arrays.asList("Stage Name"), targetReportName), "Reports");
            dashboardsPage.saveDashboard();
            setDrillReportTargetAsExport(ExportFormat.EXCEL_XLSX.getName());
            tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.drillOn("Discovery", CellType.ATTRIBUTE_VALUE);
            sleepTight(6000);
            verifyReportExport(ExportFormat.EXCEL_XLSX, "Discovery", 5510);
            checkRedBar(browser);
            
            setDrillReportTargetAsExport(ExportFormat.CSV.getName());
            tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.drillOn("Short List", CellType.ATTRIBUTE_VALUE);
            sleepTight(4000);
            verifyReportExport(ExportFormat.CSV, "Short List", 120);
            checkRedBar(browser);
            
            dashboardsPage.editDashboard();
            tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.editDrilling(Pair.of(Arrays.asList("Stage Name"), targetReportName), 
                    Pair.of(Arrays.asList("Stage Name"), "Account"), "Attributes");
            dashboardsPage.saveDashboard();
            tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.drillOn("Risk Assessment", CellType.ATTRIBUTE_VALUE);
            sleepTight(4000);
            verifyReportExport(ExportFormat.CSV, "Risk Assessment", 1295);
            checkRedBar(browser);
            
            setDrillReportTargetAsPopup();
            drillReportToPopupDialog("Conviction");
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createDrillReportToExport"})
    public void checkDrillToExportNotCached() throws JSONException, IOException {
        try {
            addReportToNewDashboard(REPORT_NAME, TEST_DASHBOAD_NAME);
            dashboardsPage.editDashboard();
            TableReport tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.addDrilling(Pair.of(Arrays.asList("Stage Name"), "Account"));
            dashboardsPage.saveDashboard();
            setDrillReportTargetAsExport(ExportFormat.CSV.getName());
            dashboardsPage.editDashboard();
            tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
            tableReport.deleteDrilling(Arrays.asList("Stage Name"));
            tableReport.addDrilling(Pair.of(Arrays.asList("Stage Name"), "Account"));
            dashboardsPage.saveDashboard();
            drillReportToPopupDialog("Negotiation");
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    private void setDrillReportTargetAsPopup() throws JSONException, IOException {
        dashboardRequest.setDrillReportTargetAsPopup(getDashboardID());
        browser.navigate().refresh();
        waitForDashboardPageLoaded(browser);
    }

    private void setDrillReportTargetAsExport(String exportFormat) throws JSONException, IOException {
        dashboardRequest.setDrillReportTargetAsExport(getDashboardID(), exportFormat);
        browser.navigate().refresh();
        waitForDashboardPageLoaded(browser);
    }

    private String getDashboardID() {
        String currentURL = browser.getCurrentUrl();
        return currentURL.substring(currentURL.lastIndexOf("/obj/")+5 , currentURL.lastIndexOf("|"));
    }

    private void drillReportToPopupDialog(String selectedAttributeName) {
        TableReport tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class);
        tableReport.drillOn(selectedAttributeName, CellType.ATTRIBUTE_VALUE);
        DashboardDrillDialog drillDialog = 
                Graphene.createPageFragment(DashboardDrillDialog.class,
                        waitForElementVisible(DashboardDrillDialog.LOCATOR, browser));
        tableReport = drillDialog.getReport(TableReport.class);
        assertTrue(tableReport.hasValue("Rollup", CellType.TOTAL_HEADER));
        AssertUtils.assertIgnoreCase(tableReport.getAttributeHeaders(), Arrays.asList("Account"));
        assertIgnoreCaseAndIndex(tableReport.getMetricHeaders(), Sets.newHashSet("Amount"));
        assertEquals(drillDialog.getBreadcrumbsString(),
                StringUtils.join(Arrays.asList("Drill report to export", selectedAttributeName), ">>"));
        drillDialog.closeDialog();
    }
}
