package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.browser.BrowserUtils.canAccessGreyPage;
import static com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog.TRIGGERED_WHEN_GOES_ABOVE;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import org.json.JSONException;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardWithWidgetsTest;

public class EditModeTest extends DashboardWithWidgetsTest {

    private static final KpiConfiguration kpiConfig = new KpiConfiguration.Builder()
        .metric(AMOUNT)
        .dateDimension(DATE_CREATED)
        .build();

    @BeforeClass(alwaysRun = true)
    public void before(ITestContext context) {
        super.before();
        boolean isMobileRunning = Boolean.parseBoolean(context.getCurrentXmlTest().getParameter("isMobileRunning"));
        addUsersWithOtherRoles = !isMobileRunning;
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkEditButtonPresent() {
        initIndigoDashboardsPageWithWidgets();

        takeScreenshot(browser, "checkEditButtonPresent", getClass());

        assertTrue(indigoDashboardsPage.isEditButtonVisible());
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"mobile"})
    public void checkEditButtonMissing() {
        initIndigoDashboardsPageWithWidgets();

        takeScreenshot(browser, "checkEditButtonMissing", getClass());

        assertFalse(indigoDashboardsPage.isEditButtonVisible());
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"desktop"})
    public void checkViewerCannotEditDashboard() throws JSONException {
        try {
            initDashboardsPage();

            logout();
            signIn(canAccessGreyPage(browser), UserRoles.VIEWER);

            assertFalse(initIndigoDashboardsPageWithWidgets().isEditButtonVisible());

        } finally {
            logout();
            signIn(canAccessGreyPage(browser), UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"desktop"})
    public void checkEditorCanEditDashboard() throws JSONException {
        try {
            initDashboardsPage();

            logout();
            signIn(canAccessGreyPage(browser), UserRoles.EDITOR);

            assertTrue(initIndigoDashboardsPageWithWidgets().isEditButtonVisible());

        } finally {
            logout();
            signIn(canAccessGreyPage(browser), UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"desktop"})
    public void checkMessageIsNotShownWhenEditingKpiWithoutAlerts() throws JSONException {
        initIndigoDashboardsPageWithWidgets()
            .switchToEditMode()
            .selectLastKpi();

        indigoDashboardsPage
            .getConfigurationPanel()
            .waitForAlertEditWarningMissing();
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"desktop"})
    public void checkMessageIsShownWhenEditingKpiWithOwnAlert() throws JSONException {
        setupKpi(kpiConfig);

        try {
            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, "200");

            initIndigoDashboardsPageWithWidgets()
                    .switchToEditMode()
                    .selectLastKpi();

            takeScreenshot(browser, "checkMessageIsShownWhenEditingKpiWithOwnAlert", getClass());
            indigoDashboardsPage
                    .getConfigurationPanel()
                    .waitForAlertEditWarning();
        } finally {
            teardownKpi();
        }
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"desktop"})
    public void checkMessageIsShownWhenEditingKpiWithOthersAlerts() throws JSONException {
        setupKpi(kpiConfig);

        // add alert as different user
        try {
            initDashboardsPage();

            logout();
            signIn(canAccessGreyPage(browser), UserRoles.VIEWER);

            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, "200");

        } finally {
            logout();
            signIn(canAccessGreyPage(browser), UserRoles.ADMIN);
        }

        try {
            initIndigoDashboardsPageWithWidgets()
                    .switchToEditMode()
                    .selectLastKpi();

            takeScreenshot(browser, "checkMessageIsShownWhenEditingKpiWithOthersAlerts", getClass());
            indigoDashboardsPage
                    .getConfigurationPanel()
                    .waitForAlertEditWarning();
        } finally {
            teardownKpi();
        }
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"desktop"})
    public void checkNumberOfAlertAreSet() throws JSONException {
        setupKpi(kpiConfig);

        try {
            logout();
            signIn(canAccessGreyPage(browser), UserRoles.VIEWER);

            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, "200");

            logout();
            signIn(canAccessGreyPage(browser), UserRoles.EDITOR);

            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, "200");

        } finally {
            logout();
            signIn(canAccessGreyPage(browser), UserRoles.ADMIN);
        }

        try {
            initIndigoDashboardsPageWithWidgets()
                    .switchToEditMode()
                    .selectLastKpi();

            takeScreenshot(browser, "Check alert message with number of users is shown correctly", getClass());
            assertThat(indigoDashboardsPage
                    .getConfigurationPanel()
                    .getKpiAlertMessage(), containsString("2 alerts"));

        } finally {
            teardownKpi();
        }
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"desktop"})
    public void checkKpiAlertKeptAfterEditedByAnotherUser() throws JSONException {
        setupKpi(kpiConfig);

        try {
            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, "200");

            logout();
            signIn(canAccessGreyPage(browser), UserRoles.EDITOR);

            initIndigoDashboardsPageWithWidgets()
                    .switchToEditMode()
                    .selectLastKpi();

            indigoDashboardsPage
                    .getConfigurationPanel()
                    .selectMetricByName(NUMBER_OF_ACTIVITIES);

            indigoDashboardsPage
                    .waitForAllKpiWidgetContentLoaded()
                    .leaveEditMode();

        } finally {
            logout();
            signIn(canAccessGreyPage(browser), UserRoles.ADMIN);
        }

        try {
            Kpi kpi = initIndigoDashboardsPageWithWidgets()
                    .switchToEditMode()
                    .selectLastKpi();

            takeScreenshot(browser, "KPI Alert are kept after updating from another user", getClass());
            indigoDashboardsPage
                    .getConfigurationPanel()
                    .waitForAlertEditWarning();
            assertEquals(kpi.getHeadline(), NUMBER_OF_ACTIVITIES);

        } finally {
            teardownKpi();
        }
    }
}
