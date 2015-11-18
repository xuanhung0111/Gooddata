package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.browser.BrowserUtils.canAccessGreyPage;
import static com.gooddata.qa.graphene.utils.CheckUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForStringInUrl;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.HamburgerMenu;
import com.gooddata.qa.graphene.fragments.indigo.Header;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardsGeneralTest;

public class ResponsiveNavigationTest extends DashboardsGeneralTest {

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"mobile"})
    public void checkHamburgerMenuDisplayed() {
        if (!isDeviceSupportHamburgerMenu()) return;

        takeScreenshot(browser, "Hamburger-menu-link", getClass());

        indigoDashboardsPage.openHamburgerMenu();
        takeScreenshot(browser, "Hamburger-menu-is-opened", getClass());

        indigoDashboardsPage.closeHamburgerMenu();
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"mobile"})
    public void checkHamburgerMenuItems() {
        if (!isDeviceSupportHamburgerMenu()) return;

        HamburgerMenu menu = indigoDashboardsPage.openHamburgerMenu();
        List<String> pages = menu.getAllMenuItems();
        assertTrue(pages.contains("Dashboards"));
        indigoDashboardsPage.closeHamburgerMenu();

        pages.stream().forEach(page -> {
            log.info("Navigate to page: " + page);

            initIndigoDashboardsPage()
                .openHamburgerMenu()
                .goToPage(page);

            takeScreenshot(browser, "checkHamburgerMenuItems-" + page, getClass());
            assertEquals(Stream.of("Analyze", "KPIs").anyMatch(p -> p.equals(page)),
                    isElementPresent(Header.HAMBURGER_LINK, browser));
        });
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"mobile"})
    public void checkLogout() throws JSONException {
        if (!isDeviceSupportHamburgerMenu()) return;

        try {
            String currentUrl = browser.getCurrentUrl();

            indigoDashboardsPage.openHamburgerMenu()
                .logout();
            browser.get(currentUrl);
            waitForStringInUrl(ACCOUNT_PAGE);
        } finally {
            signIn(canAccessGreyPage(browser), UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"desktop"})
    public void checkHamburgerMenuNotPresentInDesktop() {
        assertFalse(initIndigoDashboardsPage().isHamburgerMenuLinkPresent());
    }

    private boolean isDeviceSupportHamburgerMenu() {
        if (!initIndigoDashboardsPage().isHamburgerMenuLinkPresent()) {
            log.warning("Hamburger menu is NOT DISPLAYED in this device. Please check it!");
            return false;
        }

        return true;
    }
}
