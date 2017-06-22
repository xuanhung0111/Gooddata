package com.gooddata.qa.graphene.fragments.indigo;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDataPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForReportsPageLoaded;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.id;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;

public class HamburgerMenu extends AbstractFragment {

    public static final By LOCATOR = className("gd-header-menu-vertical-wrapper");

    @FindBy(className = "gd-header-menu-item")
    private List<WebElement> menuItems;

    @FindBy(className = "logout-button")
    private WebElement logoutButton;

    public void logout() {
        waitForElementVisible(logoutButton).click();
        waitForElementNotVisible(getRoot());
    }

    public List<String> getAllMenuItems() {
        return getElementTexts(waitForCollectionIsNotEmpty(menuItems));
    }

    public void goToPage(final String page) {
        WebElement pageElement = waitForCollectionIsNotEmpty(menuItems).stream()
            .filter(e -> page.equals(e.getText()))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("Cannot find sub menu: " + page));

        // do nothing with active page
        if (pageElement.getAttribute("class").contains("active")) {
            return;
        }

        pageElement.click();

        switch(page) {
            case "KPIs":
                waitForElementVisible(id(IndigoDashboardsPage.MAIN_ID), browser);
                return;
            case "Dashboards":
                waitForDashboardPageLoaded(browser);
                return;
            case "Analyze":
                waitForElementVisible(className(AnalysisPage.MAIN_CLASS), browser);
                return;
            case "Reports":
                waitForReportsPageLoaded(browser);
                return;
            case "Manage":
                waitForDataPageLoaded(browser);
                return;
            case "Load":
                waitForElementVisible(By.className("s-datasets-list"), browser);
                return;
        }
    }
}
