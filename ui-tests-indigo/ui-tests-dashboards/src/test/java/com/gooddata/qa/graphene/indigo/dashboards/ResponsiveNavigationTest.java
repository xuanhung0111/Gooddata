package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForStringInUrl;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.openqa.selenium.By.className;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;

import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.common.ApplicationHeaderBar;
import com.gooddata.qa.graphene.fragments.indigo.HamburgerMenu;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;

public class ResponsiveNavigationTest extends AbstractDashboardTest {

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .createAnalyticalDashboard(singletonList(createAmountKpi()));
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"mobile"})
    public void checkHamburgerMenuDisplayed() {
        if (!isDeviceSupportHamburgerMenu()) return;

        takeScreenshot(browser, "Hamburger-menu-link", getClass());

        waitForFragmentVisible(indigoDashboardsPage).openHamburgerMenu();
        takeScreenshot(browser, "Hamburger-menu-is-opened", getClass());

        indigoDashboardsPage.closeHamburgerMenu();
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"mobile"})
    public void checkHamburgerMenuItems() {
        if (!isDeviceSupportHamburgerMenu()) return;

        HamburgerMenu menu = waitForFragmentVisible(indigoDashboardsPage).openHamburgerMenu();
        List<String> pages = menu.getAllMenuItems();
        assertThat(pages, hasItem("Dashboards"));
        indigoDashboardsPage.closeHamburgerMenu();

        pages.forEach(page -> {
            log.info("Navigate to page: " + page);

            navigateToEachHamburgerMenuItem(page);

            takeScreenshot(browser, "checkHamburgerMenuItems-" + page, getClass());
            assertThat(browser.getCurrentUrl(), containsString(testParams.getProjectId()));
        });
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"mobile"})
    public void checkLogout() throws JSONException {
        if (!isDeviceSupportHamburgerMenu()) return;

        try {
            String currentUrl = browser.getCurrentUrl();

            waitForFragmentVisible(indigoDashboardsPage).openHamburgerMenu()
                .logout();
            browser.get(currentUrl);
            waitForStringInUrl(ACCOUNT_PAGE);
        } finally {
            signIn(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkHamburgerMenuNotPresentInDesktop() {
        assertFalse(initIndigoDashboardsPage().isHamburgerMenuLinkPresent(), "Hamburger menu link shouldn't display");
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void testNavigateToIndigoDashboardWithoutLogin() throws JSONException {
        try {
            initDashboardsPage();

            logout();
            openUrl(getIndigoDashboardsPageUri());
            waitForStringInUrl(ACCOUNT_PAGE);
        } finally {
            signIn(false, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void accessDashboardsFromTopMenu() throws JSONException {
        initDashboardsPage();
        assertTrue(isElementPresent(className(ApplicationHeaderBar.KPIS_LINK_CLASS), browser), "KPI link should be visible");

        ApplicationHeaderBar.goToReportsPage(browser);
        assertTrue(isElementPresent(className(ApplicationHeaderBar.KPIS_LINK_CLASS), browser), "KPI link should be visible");

        ApplicationHeaderBar.goToKpisPage(browser);
        waitForFragmentVisible(indigoDashboardsPage)
            .waitForDashboardLoad()
            .waitForWidgetsLoading();
    }

    private boolean isDeviceSupportHamburgerMenu() {
        if (!initIndigoDashboardsPage().isHamburgerMenuLinkPresent()) {
            log.warning("Hamburger menu is NOT DISPLAYED in this device. Please check it!");
            return false;
        }

        return true;
    }

    private void navigateToEachHamburgerMenuItem(String page) {
        initIndigoDashboardsPage()
            .openHamburgerMenu()
            .goToPage(page);
    }
}
