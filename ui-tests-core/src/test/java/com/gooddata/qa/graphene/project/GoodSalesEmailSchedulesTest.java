package com.gooddata.qa.graphene.project;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import org.openqa.selenium.By;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.ExportFormat;
import com.gooddata.qa.utils.graphene.Screenshots;

import static org.testng.Assert.*;

@Test(groups = {"GoodSalesSchedulesFull"}, description = "Tests for GoodSales project (email schedules functionality, incl. imap verification) in GD platform")
public class GoodSalesEmailSchedulesTest extends GoodSalesAbstractTest {

    private static final By BY_SCHEDULES_LOADING = By.cssSelector(".loader");

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedules"})
    public void verifyEmptySchedules() {
        initEmailSchedulesPage();
        assertEquals(ui.emailSchedulesPage.getNumberOfSchedules(), 0, "There are some not expected schedules");
        Screenshots.takeScreenshot(browser, "Goodsales-no-schedules", this.getClass());
    }

    @Test(dependsOnMethods = {"verifyEmptySchedules"}, groups = {"schedules"})
    public void createDashboardSchedule() {
        initEmailSchedulesPage();
        ui.emailSchedulesPage.scheduleNewDahboardEmail(testParams.getUser(), "UI-Graphene-core-Dashboard: " + testParams.getHost(),
                "Scheduled email test - dashboard.", "Outlook");
        checkUtils.checkRedBar();
        Screenshots.takeScreenshot(browser, "Goodsales-schedules-dashboard", this.getClass());
    }

    @Test(dependsOnMethods = {"verifyEmptySchedules"}, groups = {"schedules"})
    public void createReportSchedule() {
        initEmailSchedulesPage();
        ui.emailSchedulesPage.scheduleNewReportEmail(testParams.getUser(), "UI-Graphene-core-Report: " + testParams.getHost(),
                "Scheduled email test - report.", "Activities by Type", ExportFormat.ALL);
        checkUtils.checkRedBar();
        Screenshots.takeScreenshot(browser, "Goodsales-schedules-report", this.getClass());
    }

    @Test(groups = {"tests"}, dependsOnGroups = {"schedules"})
    public void verifyCreatedSchedules() {
        initEmailSchedulesPage();
        assertEquals(ui.emailSchedulesPage.getNumberOfSchedules(), 2, "2 schedules weren't created properly");
        Screenshots.takeScreenshot(browser, "Goodsales-schedules", this.getClass());
        successfulTest = true;
    }

    private void initEmailSchedulesPage() {
        openUrl(ui.PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|emailSchedulePage");
        checkUtils.waitForSchedulesPageLoaded();
        waitForElementNotVisible(BY_SCHEDULES_LOADING);
        waitForElementVisible(ui.emailSchedulesPage.getRoot());
    }

}
