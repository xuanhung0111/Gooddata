package com.gooddata.qa.graphene.fragments.dashboards;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;

public class EmbeddedDashboard extends DashboardsPage {

    public static final By LOCATOR = By.id("root");

    private static final By BY_DASHBOARD_LOADED = By.cssSelector(".s-benchLoaded.s-displayed, .s-dashboardLoaded");
    private static final By BY_EDIT_BUTTON = By.className("s-editButton");

    public boolean isEditButtonVisible() {
        return isElementVisible(BY_EDIT_BUTTON, getRoot());
    }

    public static void waitForDashboardLoaded(SearchContext searchContext) {
        waitForElementVisible(BY_DASHBOARD_LOADED, searchContext);
    }

    @Override
    public DashboardEditBar editDashboard() {
        if (!isElementPresent(BY_DASHBOARD_EDIT_BAR, browser)) {
            waitForElementVisible(BY_EDIT_BUTTON, browser).click();
        }

        return getDashboardEditBar();
    }
}
