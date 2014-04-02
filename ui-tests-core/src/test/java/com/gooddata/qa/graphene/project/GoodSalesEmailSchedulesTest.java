package com.gooddata.qa.graphene.project;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import org.openqa.selenium.By;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.ExportFormat;
import com.gooddata.qa.utils.graphene.Screenshots;

import static org.testng.Assert.*;

@Test(groups = { "GoodSalesSchedules" }, description = "Tests for GoodSales project (email schedules functionality) in GD platform")
public class GoodSalesEmailSchedulesTest extends GoodSalesAbstractTest {
	
	private static final By BY_SCHEDULES_LOADING = By.cssSelector(".loader");
	
	@Test(dependsOnMethods = { "createProject" }, groups = { "schedules" })
	public void verifyEmptySchedules() {
		initEmailSchedulesPage();
		assertEquals(emailSchedulesPage.getNumberOfSchedules(), 0, "There are some not expected schedules");
		Screenshots.takeScreenshot(browser, "Goodsales-no-schedules", this.getClass());
	}
	
	@Test(dependsOnMethods = { "verifyEmptySchedules" }, groups = { "schedules" })
	public void createDashboardSchedule() {
		initEmailSchedulesPage();
		emailSchedulesPage.scheduleNewDahboardEmail(user, "UI-Graphene-core-Dashboard: " + host, "Scheduled email test - dashboard.", "Outlook");
		checkRedBar();
		Screenshots.takeScreenshot(browser, "Goodsales-schedules-dashboard", this.getClass());
	}
	
	@Test(dependsOnMethods = { "verifyEmptySchedules" }, groups = { "schedules" })
	public void createReportSchedule() {
		initEmailSchedulesPage();
		emailSchedulesPage.scheduleNewReportEmail(user, "UI-Graphene-core-Report: " + host, "Scheduled email test - report.", "Activities by Type", ExportFormat.ALL);
		checkRedBar();
		Screenshots.takeScreenshot(browser, "Goodsales-schedules-report", this.getClass());
	}
	
	@Test(groups = { "tests" }, dependsOnGroups = { "schedules" })
	public void verifyCreatedSchedules() {
		initEmailSchedulesPage();
		assertEquals(emailSchedulesPage.getNumberOfSchedules(), 2, "2 schedules weren't created properly");
		Screenshots.takeScreenshot(browser, "Goodsales-schedules", this.getClass());
		successfulTest = true;
	}
	
	private void initEmailSchedulesPage() {
		browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId + "|emailSchedulePage");
		waitForSchedulesPageLoaded();
		waitForElementNotVisible(BY_SCHEDULES_LOADING);
		waitForElementVisible(emailSchedulesPage.getRoot());
	}

}
