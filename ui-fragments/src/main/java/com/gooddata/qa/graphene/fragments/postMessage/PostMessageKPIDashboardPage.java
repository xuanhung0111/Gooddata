package com.gooddata.qa.graphene.fragments.postMessage;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.dashboards.EmbeddedDashboard;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.EmbeddedAnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.utils.ElementUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.ElementUtils.BY_PROGRESS_MESSAGE_BAR;
import static com.gooddata.qa.graphene.utils.ElementUtils.BY_SUCCESS_MESSAGE_BAR;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;

public class PostMessageKPIDashboardPage extends AbstractFragment {

    @FindBy(id = "editDashboard")
    private WebElement editDashboard;

    @FindBy(id = "cancelDashboard")
    private WebElement cancelDashboard;

    @FindBy(id = "exportToPDF")
    private WebElement exportToPDF;

    @FindBy(id = "addFilter")
    private WebElement addFilter;

    @FindBy(id = "addKpi")
    private WebElement addKpi;

    @FindBy(id = "nameDashboard")
    private WebElement nameDashboard;

    @FindBy(id = "saveDashboard")
    private WebElement saveDashboard;

    @FindBy(id = "logger")
    private WebElement logger;

    @FindBy(id = "MuiInput-input")
    private WebElement embeddedPageInput;

    @FindBy(id = "getEmbeddedBtn")
    private WebElement getEmbedded;

    @FindBy(id = "urisOrIdentifier")
    private WebElement urisOrIdentifier;

    @FindBy(id = "addInsight")
    private WebElement addInsight;

    @FindBy(id = "uriInsight")
    private WebElement uriInsight;

    @FindBy(id = "setFilter")
    private WebElement setFilter;

    @FindBy(id = "setDateFilter")
    private WebElement dateFilter;

    @FindBy(id = "setDuplicatedDateFilter")
    private WebElement setDuplicatedFilter;

    @FindBy(id = "setCombineFilter")
    private WebElement setCombineFilter;

    @FindBy(id = "saveAsNew")
    private WebElement saveAsNew;

    private static final String ROOT_CLASS = "root";

    public static PostMessageKPIDashboardPage getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(PostMessageKPIDashboardPage.class,
                waitForElementVisible(By.id(ROOT_CLASS), searchContext));
    }

    public void editDashboard() {
        waitForElementVisible(editDashboard).click();
        browser.switchTo().frame("iframe");
    }

    public void exportToPDF() {
        //wait for exporting dashboard in maximum 10 minutes
        int exportingTextDisplayedTimeoutInSeconds = 600;

        waitForElementVisible(exportToPDF).click();
        browser.switchTo().frame("iframe");

        waitForElementVisible(BY_PROGRESS_MESSAGE_BAR, browser);
        waitForElementNotPresent(BY_PROGRESS_MESSAGE_BAR, exportingTextDisplayedTimeoutInSeconds);

        waitForElementVisible(BY_SUCCESS_MESSAGE_BAR, browser);
        waitForElementNotPresent(BY_SUCCESS_MESSAGE_BAR, exportingTextDisplayedTimeoutInSeconds);
    }

    public void addKpi() {
        waitForElementVisible(addKpi).click();
        browser.switchTo().frame("iframe");
    }

    public void addFilter() {
        waitForElementVisible(addFilter).click();
        browser.switchTo().frame("iframe");
    }

    public void addInsight(String uriOrIdentifier) {
        waitForElementVisible(uriInsight).sendKeys(uriOrIdentifier);
        waitForElementVisible(addInsight).click();
        browser.switchTo().frame("iframe");
    }

    public void cancelDashboard() {
        waitForElementVisible(cancelDashboard).click();
        browser.switchTo().frame("iframe");
    }

    public void setFilter() {
        waitForElementVisible(setFilter).click();
        browser.switchTo().frame("iframe");
    }

    public void setDateFilter() {
        waitForElementVisible(dateFilter).click();
        browser.switchTo().frame("iframe");
    }

    public void setDuplicatedDateFilter() {
        waitForElementVisible(setDuplicatedFilter).click();
        browser.switchTo().frame("iframe");
    }

    public void setCombineFilter() {
        waitForElementVisible(setCombineFilter).click();
        browser.switchTo().frame("iframe");
    }

    public void saveDashboard(String title) {
        waitForElementVisible(nameDashboard).sendKeys(title);
        waitForElementVisible(saveDashboard).click();
        browser.switchTo().frame("iframe");
    }

    public void getEmbeddedPage(String page) {
        browser.switchTo().defaultContent();
        ElementUtils.clear(waitForElementVisible(embeddedPageInput));
        getActions().sendKeys(page).perform();
        waitForElementVisible(getEmbedded).click();

        browser.switchTo().frame("iframe");
        waitForFragmentVisible(IndigoDashboardsPage.getInstance(browser));
    }

    public void saveAsNew(String title) {
        waitForElementVisible(nameDashboard).sendKeys(title);
        waitForElementVisible(saveAsNew).click();
        browser.switchTo().frame("iframe");
    }
}
