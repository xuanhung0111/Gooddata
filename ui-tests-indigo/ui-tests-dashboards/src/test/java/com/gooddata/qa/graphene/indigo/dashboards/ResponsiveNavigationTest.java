package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.browser.BrowserUtils.canAccessGreyPage;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForStringInUrl;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.openqa.selenium.By.className;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.json.JSONException;
import org.testng.SkipException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.common.ApplicationHeaderBar;
import com.gooddata.qa.graphene.fragments.indigo.HamburgerMenu;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardWithWidgetsTest;

public class ResponsiveNavigationTest extends DashboardWithWidgetsTest {

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"mobile"})
    public void checkHamburgerMenuDisplayed() {
        if (!isDeviceSupportHamburgerMenu()) return;

        takeScreenshot(browser, "Hamburger-menu-link", getClass());

        waitForFragmentVisible(indigoDashboardsPage).openHamburgerMenu();
        takeScreenshot(browser, "Hamburger-menu-is-opened", getClass());

        indigoDashboardsPage.closeHamburgerMenu();
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"mobile"})
    public void checkHamburgerMenuItems() {
        if (!isDeviceSupportHamburgerMenu()) return;

        HamburgerMenu menu = waitForFragmentVisible(indigoDashboardsPage).openHamburgerMenu();
        List<String> pages = menu.getAllMenuItems();
        assertTrue(pages.contains("Dashboards"));
        indigoDashboardsPage.closeHamburgerMenu();

        pages.stream().forEach(page -> {
            log.info("Navigate to page: " + page);

            initIndigoDashboardsPage()
                .openHamburgerMenu()
                .goToPage(page);

            takeScreenshot(browser, "checkHamburgerMenuItems-" + page, getClass());
            assertTrue(browser.getCurrentUrl().contains(testParams.getProjectId()));
        });
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"mobile"})
    public void checkLogout() throws JSONException {
        if (!isDeviceSupportHamburgerMenu()) return;

        try {
            String currentUrl = browser.getCurrentUrl();

            waitForFragmentVisible(indigoDashboardsPage).openHamburgerMenu()
                .logout();
            browser.get(currentUrl);
            waitForStringInUrl(ACCOUNT_PAGE);
        } finally {
            signIn(canAccessGreyPage(browser), UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"mobile"})
    public void checkLandingPage() throws JSONException {
        String executionEnv = System.getProperty("test.execution.env");
        if (executionEnv == null || !executionEnv.contains("browserstack-mobile")) {
            String message = "This test should be executed in browserstack mobile."
                    + " Skip this test in local testing.";
            log.info(message);
            throw new SkipException(message);
        }

        if (!isDeviceSupportHamburgerMenu()) return;

        HamburgerMenu menu = waitForFragmentVisible(indigoDashboardsPage).openHamburgerMenu();
        List<String> pages = menu.getAllMenuItems();
        indigoDashboardsPage.closeHamburgerMenu();

        for (String page : pages) {
            log.info("Navigate to page: " + page);

            indigoDashboardsPage
                .openHamburgerMenu()
                .goToPage(page);

            logout();
            signIn(false, UserRoles.ADMIN);

            waitForFragmentVisible(indigoDashboardsPage)
                .waitForDashboardLoad()
                .waitForAllKpiWidgetsLoaded();
        }
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkHamburgerMenuNotPresentInDesktop() {
        assertFalse(initIndigoDashboardsPage().isHamburgerMenuLinkPresent());
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"desktop"})
    public void testNavigateToIndigoDashboardWithoutLogin() throws JSONException {
        try {
            initDashboardsPage();

            logout();
            openUrl(getIndigoDashboardsPageUri());
            waitForStringInUrl(ACCOUNT_PAGE);
        } finally {
            signIn(canAccessGreyPage(browser), UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"desktop"})
    public void accessDashboardsFromTopMenu() throws JSONException {
        initDashboardsPage();
        assertTrue(isElementPresent(className(ApplicationHeaderBar.KPIS_LINK_CLASS), browser));

        ApplicationHeaderBar.goToReportsPage(browser);
        waitForFragmentVisible(reportsPage);
        assertTrue(isElementPresent(className(ApplicationHeaderBar.KPIS_LINK_CLASS), browser));

        ApplicationHeaderBar.goToKpisPage(browser);
        waitForFragmentVisible(indigoDashboardsPage)
            .waitForDashboardLoad()
            .waitForAllKpiWidgetsLoaded();
    }

    private boolean isDeviceSupportHamburgerMenu() {
        if (!initIndigoDashboardsPage().isHamburgerMenuLinkPresent()) {
            log.warning("Hamburger menu is NOT DISPLAYED in this device. Please check it!");
            return false;
        }

        return true;
    }
}
