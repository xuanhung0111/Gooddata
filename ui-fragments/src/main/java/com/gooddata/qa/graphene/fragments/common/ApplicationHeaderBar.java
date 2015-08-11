package com.gooddata.qa.graphene.fragments.common;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.CheckUtils.*;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class ApplicationHeaderBar extends AbstractFragment {

    @FindBy(className = "navigation-picker")
    private WebElement navigationPicker;

    @FindBy(className = "dashboard-link")
    private WebElement dashboardLink;

    @FindBy(className = "report-link")
    private WebElement reportLink;

    @FindBy(className = "manage-link")
    private WebElement manageLink;

    @FindBy(className = "analysis-link")
    private WebElement analysisLink;

    @FindBy(className = "users-link")
    private WebElement usersLink;

    @FindBy(className = "search-link")
    private WebElement searchLink;

    private static ApplicationHeaderBar getInstance(WebDriver browser) {
        return Graphene.createPageFragment(ApplicationHeaderBar.class,
                waitForElementVisible(By.className("appHeader"), browser));
    }

    public static void goToDashboardsPage(WebDriver browser) {
        WebElement link = waitForElementPresent(getInstance(browser).dashboardLink);

        if (link.getAttribute("class").contains("invisible")) {
            System.out.println("This project does not have dashboard link in application header bar!");
            System.out.println("Maybe this is a demo project!");
            return;
        }

        link.click();
    }

    public static void goToReportsPage(WebDriver browser) {
        waitForElementVisible(getInstance(browser).reportLink).click();
    }

    public static void goToManagePage(WebDriver browser) {
        waitForElementVisible(getInstance(browser).manageLink).click();
    }

    public static void goToAnalysisPage(WebDriver browser) {
        waitForElementVisible(getInstance(browser).analysisLink).click();
    }

    public static void goToUserManagementPage(WebDriver browser) {
        waitForElementVisible(getInstance(browser).usersLink).click();
    }

    public static void goToSearchPage(WebDriver browser) {
        waitForElementVisible(getInstance(browser).searchLink).click();
    }

    public static String getSearchLinkText(WebDriver browser) {
        return waitForElementVisible(getInstance(browser).searchLink).getText();
    }

    public static boolean isSearchIconPresent(WebDriver browser) {
        return waitForElementVisible(getInstance(browser).searchLink).findElements(
              By.className("search-icon")).size() > 0;
    }

    public static void selectProject(String idOrName, WebDriver browser) {
        waitForElementVisible(getInstance(browser).navigationPicker).click();
        DropDown projectSelector = Graphene.createPageFragment(DropDown.class,
                waitForElementVisible(By.className("project-selector"), browser));
        projectSelector.searchItem(idOrName);
        projectSelector.selectFirstItem();
    }
}
