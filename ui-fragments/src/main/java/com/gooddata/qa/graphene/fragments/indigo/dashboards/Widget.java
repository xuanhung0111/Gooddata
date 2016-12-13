package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.tagName;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

public class Widget extends AbstractFragment {

    public static final By HINT_LOCATOR = By.cssSelector(".gd-editable-label:hover");

    @FindBy(className = "dash-item-content")
    private WebElement content;

    @FindBy(className = "dash-item-action-delete")
    protected WebElement deleteButton;

    @FindBy(css = ".item-headline .s-headline")
    protected WebElement headline;

    @FindBy(css = ".item-headline .s-editable-label")
    private WebElement headlineInplaceEdit;

    @FindBy(css = ".item-headline .s-editable-label textarea")
    private WebElement headlineTextarea;

    public String getHeadline() {
        return waitForElementVisible(headline).getText().replace("\n", " ");
    }

    public String getHeadlinePlaceholder() {
        return waitForElementVisible(headline).findElement(tagName("textarea")).getAttribute("placeholder");
    }

    public Widget clearHeadline() {
        waitForElementVisible(headlineInplaceEdit).click();

        // hit backspace multiple times, because .clear()
        // event does not trigger onchange event
        // https://selenium.googlecode.com/svn/trunk/docs/api/java/org/openqa/selenium/WebElement.html#clear%28%29
        waitForElementVisible(headlineTextarea);
        int headlineLength = headlineInplaceEdit.getText().length();
        for (int i = 0; i < headlineLength; i++) {
            headlineTextarea.sendKeys(Keys.BACK_SPACE);
        }
        return this;
    }

    public void setHeadline(String newHeadline) {
        clearHeadline();
        headlineTextarea.sendKeys(newHeadline);
        headlineTextarea.sendKeys(Keys.ENTER);

        waitForElementVisible(headlineInplaceEdit);
    }

    public String hoverToHeadline() {
        new Actions(browser).moveToElement(headlineInplaceEdit).perform();
        return waitForElementVisible(getRoot().findElement(HINT_LOCATOR))
                .getCssValue("border-top-color");
    }

    public void clickDeleteButton() {
        waitForElementVisible(deleteButton).click();
    }

    public boolean isDeleteButtonVisible() {
        return isElementVisible(deleteButton);
    }

    public Widget clickOnContent() {
        waitForElementVisible(content).click();

        return this;
    }

    public static enum DropZone {
        PREV(".dropzone.prev"),
        NEXT(".dropzone.next"),
        LAST(".s-last-drop-position");

        private String css;

        private DropZone(final String css) {
            this.css = css;
        }

        public String getCss() {
            return this.css;
        }
    }
}
