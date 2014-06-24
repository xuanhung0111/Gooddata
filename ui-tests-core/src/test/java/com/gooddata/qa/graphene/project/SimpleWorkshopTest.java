package com.gooddata.qa.graphene.project;

import java.util.ArrayList;
import java.util.List;

import com.gooddata.qa.graphene.fragments.reports.OneNumberReport;
import org.jboss.arquillian.graphene.Graphene;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.ReportTypes;
import com.gooddata.qa.utils.graphene.Screenshots;

import static org.testng.Assert.*;

@Test(groups = {"projectSimpleWS"}, description = "Tests for simple workshop test in GD platform")
public class SimpleWorkshopTest extends AbstractProjectTest {

    private String csvFilePath;

    @BeforeClass
    public void initProperties() {
        csvFilePath = testParams.loadProperty("csvFilePath");
        projectTitle = "simple-project-ws";
    }

    @Test(dependsOnMethods = {"createSimpleProject"}, groups = {"tests"})
    public void uploadData() throws InterruptedException {
        uiUtils.uploadCSV(csvFilePath + "/payroll.csv", null, "simple-ws");
    }

    @Test(dependsOnMethods = {"uploadData"}, groups = {"tests"})
    public void addNewTab() throws InterruptedException {
        uiUtils.addNewTabOnDashboard("Default dashboard", "workshop", "simple-ws");
    }

    @Test(dependsOnMethods = {"uploadData"}, groups = "tests")
    public void createBasicReport() throws InterruptedException {
        uiUtils.initReportsPage();
        uiUtils.reportsPage.startCreateReport();
        checkUtils.waitForAnalysisPageLoaded();
        waitForElementVisible(uiUtils.reportPage.getRoot());
        List<String> what = new ArrayList<String>();
        what.add("Sum of Amount");
        uiUtils.reportPage.createReport("Headline test", ReportTypes.HEADLINE, what, null);
        Screenshots.takeScreenshot(browser, "simple-ws-headline-report", this.getClass());
    }

    @Test(dependsOnMethods = {"createBasicReport"}, groups = {"tests"})
    public void addReportOnDashboardTab() throws InterruptedException {
        uiUtils.initDashboardsPage();
        uiUtils.dashboardsPage.getTabs().openTab(1);
        checkUtils.waitForDashboardPageLoaded();
        uiUtils.dashboardsPage.editDashboard();
        uiUtils.dashboardsPage.getDashboardEditBar().addReportToDashboard("Headline test");
        uiUtils.dashboardsPage.getDashboardEditBar().saveDashboard();
        checkUtils.waitForDashboardPageLoaded();
        Screenshots.takeScreenshot(browser, "simple-ws-headline-report-dashboard", this.getClass());
    }

    @Test(dependsOnMethods = {"addReportOnDashboardTab"}, groups = {"tests"})
    public void verifyHeadlineReport() {
        uiUtils.initDashboardsPage();
        assertEquals(1, uiUtils.dashboardsPage.getContent().getNumberOfReports(), "Invalid report(s) count on dashboard");
        OneNumberReport report = Graphene.createPageFragment(OneNumberReport.class,
                uiUtils.dashboardsPage.getContent().getReport(0).getRoot());
        assertEquals(report.getValue(), "7,252,542.63", "Invalid value in headline report");
        assertEquals(report.getDescription(), "Sum of Amount", "Invalid description in headline report");
        successfulTest = true;
    }
}
