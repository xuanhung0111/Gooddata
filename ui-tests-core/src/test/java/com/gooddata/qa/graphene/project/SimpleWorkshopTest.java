package com.gooddata.qa.graphene.project;

import java.util.ArrayList;
import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.graphene.enums.ReportTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardReportOneNumber;
import com.gooddata.qa.utils.graphene.Screenshots;

@Test(groups = { "projectSimpleWS" }, description = "Tests for workshop simple project in GD platform")
public class SimpleWorkshopTest extends AbstractTest {
	
	private String csvFilePath;
	
	@BeforeClass
	public void initStartPage() {
		startPage = "projects.html";
	
		csvFilePath = loadProperty("csvFilePath");
	}
	
	@Test(groups = { "projectSimpleWSInit" } )
	public void init() throws JSONException {
		// sign in with demo user
		signInAtUI(user, password);
	}
	
	@Test(dependsOnGroups = { "projectSimpleWSInit" })
	public void createSimpleProjectWS() throws JSONException, InterruptedException {
		openUrl(PAGE_GDC_PROJECTS);
		waitForElementVisible(gpProject.getRoot());
		projectId = gpProject.createProject("simple-project-ws", "", "", authorizationToken, 12);
		Screenshots.takeScreenshot(browser, "simple-project-ws-created", this.getClass());
	}
	
	@Test(dependsOnMethods = { "createSimpleProjectWS" }, groups = { "ws-charts" })
	public void uploadData() throws InterruptedException {
		uploadSimpleCSV(csvFilePath + "/payroll.csv", "simple-ws");
	}
	
	@Test(dependsOnMethods = { "uploadData" }, groups = { "ws-charts" })
	public void addNewTabs() throws InterruptedException {
		addNewTabOnDashboard("Default dashboard", "workshop", "simple-ws");
	}
	
	@Test(dependsOnMethods = { "uploadData" }, groups = "ws-charts")
	public void createBasicReport() throws InterruptedException {
		initReportsPage();
		reportsPage.startCreateReport();
		waitForAnalysisPageLoaded();
		waitForElementVisible(reportPage.getRoot());
		List<String> what = new ArrayList<String>();
		what.add("Sum of Amount");
		reportPage.createReport("Headline test", ReportTypes.HEADLINE, what, null);
		Screenshots.takeScreenshot(browser, "simple-ws-headline-report", this.getClass());
	}
	
	@Test(dependsOnMethods = { "createBasicReport" }, groups = { "ws-charts" })
	
	public void addReportOnDashboardTab() throws InterruptedException {
		initDashboardsPage();
		dashboardsPage.getTabs().openTab(1);
		waitForDashboardPageLoaded();
		dashboardsPage.editDashboard();
		dashboardsPage.getDashboardEditBar().addReportToDashboard("Headline test");
		dashboardsPage.getDashboardEditBar().saveDashboard();
		waitForDashboardPageLoaded();
		Screenshots.takeScreenshot(browser, "simple-ws-headline-report-dashboard", this.getClass());
	}
	
	@Test(dependsOnMethods = { "addReportOnDashboardTab" }, groups = { "ws-charts" })
	public void verifyHeadlineReport() {
		initDashboardsPage();
		//Assert.assertEquals(1, dashboardsPage.getContent().getNumberOfReports(), "Report is not available on dashboard");
		System.out.println("reports: " + dashboardsPage.getContent().getNumberOfReports());
		DashboardReportOneNumber report = Graphene.createPageFragment(DashboardReportOneNumber.class, dashboardsPage.getContent().getReport(0).getRoot());
		Assert.assertEquals(report.getValue(), "7,252,542.63", "Invalid value in headline report");
		Assert.assertEquals(report.getDescription(), "Sum of Amount", "Invalid description in headline report");
		successfulTest = true;
	}
	
	@Test(dependsOnGroups = { "ws-charts" }, alwaysRun = true)
	public void deleteSimpleProject() {
		deleteProjectByDeleteMode(successfulTest);
	}
}
