package com.gooddata.qa.graphene.fragments.postMessage;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.EmbeddedAnalysisPage;
import com.gooddata.qa.graphene.utils.ElementUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForIndigoMessageDisappear;

public class PostMessageAnalysisPage extends AbstractFragment {

    @FindBy(id = "undo")
    private WebElement undo;

    @FindBy(id = "redo")
    private WebElement redo;

    @FindBy(id = "clear")
    private WebElement clear;

    @FindBy(id = "openInsight")
    private WebElement openInsight;

    @FindBy(id = "save")
    private WebElement save;

    @FindBy(id = "saveAs")
    private WebElement saveAs;

    @FindBy(id = "export")
    private WebElement export;

    @FindBy(id = "logger")
    private WebElement logger;

    @FindBy(id = "MuiInput-input")
    private WebElement embeddedPageInput;

    @FindBy(id = "getEmbeddedBtn")
    private WebElement getEmbedded;

    @FindBy(id = "setFilter")
    private WebElement setFilterContext;

    @FindBy(id = "setDateFilter")
    private WebElement setDateFilter;

    @FindBy(id = "removeAllFilter")
    private WebElement removeFilter;

    @FindBy(id = "setCombineFilter")
    private WebElement setCombineFilter;

    @FindBy(id = "setGranularityDate")
    private WebElement setGranularityDate;

    private static final String ROOT_CLASS = "root";

    public static PostMessageAnalysisPage getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(PostMessageAnalysisPage.class,
            waitForElementVisible(By.id(ROOT_CLASS), searchContext));
    }

    public void undo() {
        waitForElementVisible(undo).click();
        browser.switchTo().frame("iframe");
    }

    public void redo() {
        waitForElementVisible(redo).click();
        browser.switchTo().frame("iframe");
    }

    public void clear() {
        waitForElementVisible(clear).click();
        browser.switchTo().frame("iframe");
    }

    public void openInsight() {
        waitForElementVisible(openInsight).click();
        browser.switchTo().frame("iframe");
    }

    public void setFilter() {
        waitForElementVisible(setFilterContext).click();
        browser.switchTo().frame("iframe");
    }

    public void setDateFilter() {
        waitForElementVisible(setDateFilter).click();
        browser.switchTo().frame("iframe");
    }

    public void setDuplicatedGranularityDateFilter() {
        waitForElementVisible( setGranularityDate).click();
        browser.switchTo().frame("iframe");
    }

    public void removeFilter() {
        waitForElementVisible(removeFilter).click();
        browser.switchTo().frame("iframe");
    }

    public void setCombineFilter() {
        waitForElementVisible(setCombineFilter).click();
        browser.switchTo().frame("iframe");
    }

    public void saveInsight() {
        waitForElementVisible(save).click();
        browser.switchTo().frame("iframe");
        waitForIndigoMessageDisappear(browser);
    }

    public void saveAsInsight() {
        waitForElementVisible(saveAs).click();
        browser.switchTo().frame("iframe");
        waitForIndigoMessageDisappear(browser);
    }

    public void exportInsight() {
        waitForElementVisible(export).click();
        browser.switchTo().frame("iframe");
    }

    public void getEmbeddedPage(String page) {
        browser.switchTo().defaultContent();

        ElementUtils.clear(waitForElementVisible(embeddedPageInput));
        getActions().sendKeys(page).perform();
        waitForElementVisible(getEmbedded).click();

        browser.switchTo().frame("iframe");
        waitForFragmentVisible(EmbeddedAnalysisPage.getInstance(browser));
    }
}
