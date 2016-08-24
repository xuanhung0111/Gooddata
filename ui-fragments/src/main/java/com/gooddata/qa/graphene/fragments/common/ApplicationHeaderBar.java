package com.gooddata.qa.graphene.fragments.common;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForReportsPageLoaded;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.reports.ReportsPage;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;

public class ApplicationHeaderBar extends AbstractFragment {

    @FindBy(className = "navigation-picker")
    private WebElement navigationPicker;

    @FindBy(className = KPIS_LINK_CLASS)
    private WebElement kpisLink;

    @FindBy(className = "dashboard-link")
    private WebElement dashboardLink;

    @FindBy(className = "report-link")
    private WebElement reportLink;

    @FindBy(className = "manage-link")
    private WebElement manageLink;

    @FindBy(className = "analysis-link")
    private WebElement analysisLink;

    @FindBy(className = "csv-uploader-link")
    private WebElement csvUploaderLink;

    @FindBy(className = "users-link")
    private WebElement usersLink;

    public static final String ROOT_LOCATOR = "appHeader";
    public static final String KPIS_LINK_CLASS = "kpis-link";

    private static ApplicationHeaderBar getInstance(WebDriver browser) {
        return Graphene.createPageFragment(ApplicationHeaderBar.class,
                waitForElementVisible(By.className(ROOT_LOCATOR), browser));
    }

    public static void goToKpisPage(WebDriver browser) {
        waitForElementVisible(getInstance(browser).kpisLink).click();
    }

    public static boolean isKpisLinkVisible(WebDriver browser) {
        return isElementPresent(By.className(KPIS_LINK_CLASS), browser);
    }

    public static void goToCsvUploaderPage(WebDriver browser) {
        waitForElementVisible(getInstance(browser).csvUploaderLink).click();
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

    public static ReportsPage goToReportsPage(WebDriver browser) {
        waitForElementVisible(getInstance(browser).reportLink).click();
        waitForReportsPageLoaded(browser);
        return ReportsPage.getInstance(browser);
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

    public static void selectProject(String name, WebDriver browser) {
        waitForElementVisible(getInstance(browser).navigationPicker).click();

        Graphene.createPageFragment(DropDown.class,
                waitForElementVisible(By.className("project-selector"), browser))
                .searchAndSelectItem(name);
    }

    public static String getCurrentProjectName(WebDriver browser) {
        return waitForElementVisible(getInstance(browser).navigationPicker).getText();
    }
}
