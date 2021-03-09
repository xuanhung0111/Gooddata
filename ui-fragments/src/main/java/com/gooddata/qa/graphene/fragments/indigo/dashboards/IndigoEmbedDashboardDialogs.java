package com.gooddata.qa.graphene.fragments.indigo.dashboards;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.utils.ElementUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.*;
import static org.openqa.selenium.By.*;

public class IndigoEmbedDashboardDialogs extends AbstractFragment {

    private static final String EMBED_DIALOG_ROOT = "embed-dialog";

    @FindBy(css = ".Navigationpanel .s-checkbox-toggle")
    private WebElement navigationSwitch;

    @FindBy(css = ".Customheight .s-checkbox-toggle")
    private WebElement customHeightToggle;

    @FindBy(className = "embed-tabs-content")
    private WebElement embedTabsContent;

    @FindBy(className = "s-embed_dialog_preview")
    private WebElement previewTab;

    @FindBy(className = "preview-all")
    private WebElement previewAll;

    @FindBy(className = "s-embed_dialog_iframe")
    private WebElement iframeTab;

    @FindBy(className = "s-embed_dialog_url")
    private WebElement urlTab;

    @FindBy(className = "embed-dialog-iframe-content")
    private WebElement iframeContent;

    @FindBy(className = "embed-dialog-url-content")
    private WebElement urlContent;

    @FindBy(className = "embed-dialog-custom-height")
    private WebElement customHeightField;

    @FindBy(className = "gd-filter-tag__input")
    private WebElement inputTagsField;

    public static IndigoEmbedDashboardDialogs getInstance(SearchContext context) {
        return Graphene.createPageFragment(IndigoEmbedDashboardDialogs.class,
                waitForElementVisible(className(EMBED_DIALOG_ROOT), context));
    }

    public String getIframeContent() {
        return waitForElementPresent(iframeContent).getText();
    }

    public String getUrlContent() {
        return waitForElementPresent(urlContent).getText();
    }

    public boolean isConfiguredNoNavigationBar(){
        return isElementVisible(className("preview-no-navigation-panel"), root);
    }

    public boolean isPreviewAll(){
        return isElementVisible(previewAll);
    }

    public boolean isOffCustomHeight(){
        return !isElementVisible(className("embed-dialog-custom-height"), root);
    }

    public boolean isOffFilterByTags(){
        return !isElementVisible(className("embed-filter-content-buttons-wrapper"), root);
    }

    public boolean hoveringOnNavigationToggle(){
        waitForElementVisible(cssSelector(".Navigationpanel .s-checkbox-toggle-label"), root);
        getActions().moveToElement(navigationSwitch).perform();
        waitForElementVisible(className("embed-tabs-content"), root);
        return isElementVisible(className("preview-highlight-navigation"), root);
    }

    public boolean hoveringOnCustomHeightToggle(){
        waitForElementVisible(cssSelector(".Customheight .s-checkbox-toggle-label"), root);
        getActions().moveToElement(customHeightToggle).perform();
        return !isElementVisible(className("preview-highlight-navigation"), root);
    }

    public IndigoEmbedDashboardDialogs switchNavigationToggle(){
        String javaScript = "document.querySelector(\".Navigationpanel span.input-label-text\").click();";
        ((JavascriptExecutor)browser).executeScript(javaScript);
        return this;
    }

    public IndigoEmbedDashboardDialogs switchCustomHeightToggle(){
        String javaScript = "document.querySelector(\".Customheight span.input-label-text\").click();";
        ((JavascriptExecutor) browser).executeScript(javaScript);
        return this;
    }

    public void setHeight(String height){
        waitForElementVisible(customHeightField);
        customHeightField.sendKeys(height);
    }

    public boolean isInvalidHeightMessageVisible(String height){
        waitForElementVisible(customHeightField);
        customHeightField.sendKeys(height);
        waitForElementVisible(cssSelector(".overlay-wrapper .bubble-content"),browser);
        return isElementVisible(className("bubble-negative"),browser)|| isElementVisible(className("bubble-warning"),browser);
    }

    public void removeHeight(){
        ElementUtils.clear(customHeightField);
    }

    public void switchToPreviewTab(){
        waitForElementVisible(previewTab).click();
    }
    public void switchToIframeTab(){
            waitForElementVisible(iframeTab).click();
    }

    public boolean isIframeTabVisible(){
        return isElementVisible(cssSelector(".is-active.s-embed_dialog_iframe"), root);
    }

    public void switchToUrlTab(){
            waitForElementVisible(urlTab).click();
    }

    public boolean isUrlTabVisible(){
        return isElementVisible(cssSelector(".s-embed_dialog_url.is-active"), root);
    }

    public String getCopiedMessage(){
        waitForElementNotPresent(className("gd-message-overlay"), browser);
        waitForElementVisible(className("s-dialog-submit-button"), root).click();
        return waitForElementVisible(className("gd-message-overlay"), browser).getText();
    }

    public IndigoEmbedDashboardDialogs switchFilterByTagsToggle(){
        String javaScript = "document.querySelector(\".Filterbytags span.input-label-text\").click();";
        ((JavascriptExecutor) browser).executeScript(javaScript);
        return this;
    }

    public void setTags(String tags, Keys key){
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(inputTagsField).click().sendKeys(tags).sendKeys(Keys.chord(key)).build().perform();
    }

    public void removeTags(){
        inputTagsField.clear();
    }

    public boolean isIncludeTabVisible(){
        return isElementVisible(cssSelector(".include-button.filter-content-button-is-selected"), root);
    }

    public boolean isExcludeTabVisible(){
        return isElementVisible(cssSelector(".exclude-button.filter-content-button-is-selected"), root);
    }

    public IndigoEmbedDashboardDialogs switchToExcludeTab(){
        String javaScript = "document.querySelector(\"input#exclude-filter-content.exclude-filter-content\").click();";
        ((JavascriptExecutor) browser).executeScript(javaScript);
        return this;
    }

    public IndigoEmbedDashboardDialogs switchToIncludeTab(){
        String javaScript = "document.querySelector(\"input#include-filter-content.include-filter-content\").click();";
        ((JavascriptExecutor) browser).executeScript(javaScript);
        return this;
    }

    public void closeEmbeddedDialog(){
        waitForElementVisible(className("s-dialog-close-button"), root).click();
    }
}
