package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.browser.BrowserUtils.canAccessGreyPage;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DASH_TAB_OUTLOOK;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForStringMissingInUrl;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.deleteAnalyticalDashboard;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.DateFilter;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.SplashScreen;
import com.gooddata.qa.graphene.indigo.dashboards.common.GoodSalesAbstractDashboardTest;

public class SplashScreenTest extends GoodSalesAbstractDashboardTest {

    private static final String SPLASH_SCREEN_MOBILE_MESSAGE = "To set up a KPI dashboard, head to your desktop and make your browser window wider.";

    private static final KpiConfiguration kpi = new KpiConfiguration.Builder()
        .metric(METRIC_AMOUNT)
        .dataSet(DATE_CREATED)
        .comparison(Kpi.ComparisonType.NO_COMPARISON.toString())
        .drillTo(DASH_TAB_OUTLOOK)
        .build();

    private boolean isMobileRunning;

    @BeforeClass(alwaysRun = true)
    public void addUsersOnDesktopExecution(ITestContext context) {
        isMobileRunning = Boolean.parseBoolean(context.getCurrentXmlTest().getParameter("isMobileRunning"));
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop", "empty-state"})
    public void checkNewProjectWithoutKpisFallsToSplashScreen() {
        initIndigoDashboardsPage().getSplashScreen();

        takeScreenshot(browser, "checkNewProjectWithoutKpisFallsToSplashScreen", getClass());
    }

    @Test(dependsOnGroups = {"empty-state"}, groups = {"desktop"})
    public void checkCreateNewKpiDashboard() throws JSONException, IOException {
        initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .addKpi(kpi)
            .saveEditModeWithWidgets();

        takeScreenshot(browser, "checkCreateNewKpiDashboard", getClass());

        deleteAnalyticalDashboard(getRestApiClient(), getWorkingDashboardUri());
    }

    @Test(dependsOnMethods = {"checkDeleteDashboardButtonMissingOnUnsavedDashboard"},
            groups = {"desktop","empty-state"})
    public void checkEnterCreateNewKpiDashboardAndCancel() {
        initIndigoDashboardsPage()
                .getSplashScreen()
                .startEditingWidgets()
                .waitForDashboardLoad()
                .addKpi(kpi)
                .cancelEditModeWithChanges();

        takeScreenshot(browser, "checkEnterCreateNewKpiDashboardAndCancel", getClass());

        assertEquals(waitForFragmentVisible(indigoDashboardsPage)
                .getSplashScreen()
                .startEditingWidgets()
                .getKpisCount(), 0);
    }

    @Test(dependsOnGroups = {"empty-state"}, groups = {"desktop"})
    public void checkCreateNewKpiDashboardRemoveAndCreateAgain() {
        initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .addKpi(kpi)
            .saveEditModeWithWidgets();

        waitForFragmentVisible(indigoDashboardsPage).switchToEditMode().getLastWidget(Kpi.class).delete();

        indigoDashboardsPage.saveEditModeWithoutWidgets();

        // do not use setupKpi here - it refreshes the page
        // this is a test case without page refresh
        waitForFragmentVisible(indigoDashboardsPage)
                .getSplashScreen()
                .startEditingWidgets()
                .addKpi(kpi)
                .saveEditModeWithWidgets();

        takeScreenshot(browser, "checkCreateNewKpiDashboardRemoveAndCreateAgain", getClass());

        // do not use teardownKpi here - it refreshes the page
        // this is a test case without page refresh
        waitForFragmentVisible(indigoDashboardsPage).switchToEditMode().getLastWidget(Kpi.class).delete();

        indigoDashboardsPage
                .saveEditModeWithoutWidgets()
                .getSplashScreen();
    }

    @Test(dependsOnGroups = {"empty-state"}, groups = {"desktop"})
    public void checkDeleteDashboardWithCancelAndConfirm() {
        initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .addKpi(kpi)
            .saveEditModeWithWidgets();

        waitForFragmentVisible(indigoDashboardsPage)
                .switchToEditMode()
                .deleteDashboard(false);

        waitForFragmentVisible(indigoDashboardsPage)
                .waitForEditingControls()
                .waitForSplashscreenMissing();

        takeScreenshot(browser, "checkDeleteDashboardWithCancelAndConfirm-cancel", getClass());

        waitForFragmentVisible(indigoDashboardsPage)
                .deleteDashboard(true);

        indigoDashboardsPage.getSplashScreen();

        takeScreenshot(browser, "checkDeleteDashboardWithCancelAndConfirm-confirm", getClass());
    }

    @Test(dependsOnGroups = {"empty-state"}, groups = {"desktop"})
    public void checkDefaultDateFilterWhenCreatingDashboard() {
        String dateFilterDefault = DATE_FILTER_THIS_MONTH;
        initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .addKpi(kpi)
            .saveEditModeWithWidgets();

        String dateFilterSelection = initIndigoDashboardsPageWithWidgets()
                .waitForDateFilter()
                .getSelection();

        takeScreenshot(browser, "checkDefaultDateFilterWhenCreatingDashboard-" + dateFilterDefault, getClass());
        assertEquals(dateFilterSelection, dateFilterDefault);

        DateFilter dateFilterAfterRefresh = initIndigoDashboardsPageWithWidgets().waitForDateFilter();

        takeScreenshot(browser, "Default date interval when refresh Indigo dashboard page-" + dateFilterDefault, getClass());
        assertEquals(dateFilterAfterRefresh.getSelection(), dateFilterDefault);

        waitForFragmentVisible(indigoDashboardsPage)
                .switchToEditMode()
                .deleteDashboard(true);

        indigoDashboardsPage.getSplashScreen();
    }

    @Test(dependsOnMethods = {"checkNewProjectWithoutKpisFallsToSplashScreen"}, groups = {"desktop", "empty-state"})
    public void checkDeleteDashboardButtonMissingOnUnsavedDashboard() {
        assertFalse(initIndigoDashboardsPage()
                .getSplashScreen()
                .startEditingWidgets()
                .waitForEditingControls()
                .isDeleteButtonVisible());

        takeScreenshot(browser, "checkDeleteDashboardButtonMissingOnUnsavedDashboard", getClass());
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"mobile"})
    public void checkCreateNewKpiDashboardNotAvailableOnMobile() {
        SplashScreen splashScreen = initIndigoDashboardsPage().getSplashScreen();
        String mobileMessage = splashScreen.getMobileMessage();

        assertEquals(mobileMessage, SPLASH_SCREEN_MOBILE_MESSAGE);
        splashScreen.waitForCreateKpiDashboardButtonMissing();

        takeScreenshot(browser, "checkCreateNewKpiDashboardNotAvailableOnMobile", getClass());
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop", "empty-state"})
    public void checkViewerCannotCreateDashboard() throws JSONException {
        try {
            signIn(canAccessGreyPage(browser), UserRoles.VIEWER);

            // viewer accessing "dashboards" with no kpi dashboards created should be redirected
            openUrl(getIndigoDashboardsPageUri());

            // check that we are not on dashboards page
            // instead of that, viewer user should have been switch to OLD dashboard page
            waitForElementVisible(By.className("s-displayed"), browser, 300);
            waitForStringMissingInUrl("/dashboards");

        } finally {
            signIn(canAccessGreyPage(browser), UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop", "empty-state"})
    public void checkEditorCanCreateDashboard() throws JSONException {
        try {
            signIn(canAccessGreyPage(browser), UserRoles.EDITOR);

            initIndigoDashboardsPage()
                .getSplashScreen()
                .waitForCreateKpiDashboardButtonVisible();

        } finally {
            signIn(canAccessGreyPage(browser), UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop", "empty-state"})
    public void checkCannotSaveNewEmptyDashboard() throws JSONException {
        initIndigoDashboardsPage()
                .getSplashScreen()
                .startEditingWidgets()
                .selectDateFilterByName(DATE_FILTER_THIS_QUARTER);

        takeScreenshot(browser, "checkCannotSaveNewEmptyDashboard", getClass());
        assertFalse(indigoDashboardsPage.isSaveEnabled());
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        if (isMobileRunning) return;

        createAndAddUserToProject(UserRoles.EDITOR);
        createAndAddUserToProject(UserRoles.VIEWER);
    }
}
