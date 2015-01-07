package com.gooddata.qa.graphene.project;

import com.gooddata.qa.graphene.fragments.reports.OneNumberReport;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.ReportDefinition;
import com.gooddata.qa.graphene.enums.ReportTypes;
import com.gooddata.qa.utils.graphene.Screenshots;

import static org.testng.Assert.*;
import static com.gooddata.qa.graphene.common.CheckUtils.*;

@Test(groups = {"projectSimpleWS"}, description = "Tests for simple workshop test in GD platform")
public class SimpleWorkshopTest extends AbstractProjectTest {

    private String csvFilePath;

    @BeforeClass
    public void initProperties() {
        csvFilePath = testParams.loadProperty("csvFilePath") + testParams.getFolderSeparator();
        projectTitle = "SimpleProject-test-ws";
    }

    @Test(dependsOnMethods = {"createSimpleProject"}, groups = {"tests"})
    public void uploadData() throws InterruptedException {
        uploadCSV(csvFilePath + "payroll.csv", null, "simple-ws");
    }

    @Test(dependsOnMethods = {"uploadData"}, groups = {"tests"})
    public void addNewTab() throws InterruptedException {
        addNewTabOnDashboard("Default dashboard", "workshop", "simple-ws");
    }

    @Test(dependsOnMethods = {"uploadData"}, groups = "tests")
    public void createBasicReport() throws InterruptedException {
        initReportsPage();
        reportsPage.startCreateReport();
        waitForAnalysisPageLoaded(browser);
        waitForElementVisible(reportPage.getRoot());
        reportPage.createReport(new ReportDefinition().withName("Headline test")
                                                      .withType(ReportTypes.HEADLINE)
                                                      .withWhats("Sum of Amount"));
        Screenshots.takeScreenshot(browser, "simple-ws-headline-report", this.getClass());
    }

    @Test(dependsOnMethods = {"createBasicReport"}, groups = {"tests"})
    public void addReportOnDashboardTab() throws InterruptedException {
        initDashboardsPage();
        dashboardsPage.getTabs().openTab(1);
        waitForDashboardPageLoaded(browser);
        dashboardsPage.editDashboard();
        dashboardsPage.getDashboardEditBar().addReportToDashboard("Headline test");
        dashboardsPage.getDashboardEditBar().saveDashboard();
        waitForDashboardPageLoaded(browser);
        Screenshots.takeScreenshot(browser, "simple-ws-headline-report-dashboard", this.getClass());
    }

    @Test(dependsOnMethods = {"addReportOnDashboardTab"}, groups = {"tests"})
    public void verifyHeadlineReport() {
        initDashboardsPage();
        assertEquals(1, dashboardsPage.getContent().getNumberOfReports(), "Invalid report(s) count on dashboard");
        OneNumberReport report = dashboardsPage.getContent().getReport(0, OneNumberReport.class);
        assertEquals(report.getValue(), "7,252,542.63", "Invalid value in headline report");
        assertEquals(report.getDescription(), "Sum of Amount", "Invalid description in headline report");
    }
}
