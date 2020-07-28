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

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;

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
        waitForElementVisible(exportToPDF).click();
        browser.switchTo().frame("iframe");
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
}
