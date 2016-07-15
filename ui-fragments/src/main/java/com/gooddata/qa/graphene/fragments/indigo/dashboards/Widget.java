package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class Widget extends AbstractFragment {

    @FindBy(className = "dash-item-action-delete")
    protected WebElement deleteButton;

    @FindBy(css = ".item-headline > h3")
    protected WebElement headline;

    public String getHeadline() {
        return waitForElementVisible(headline).getText();
    }

    public void clickDeleteButton() {
        waitForElementVisible(deleteButton).click();
    }

    public boolean isDeleteButtonVisible() {
        return isElementVisible(deleteButton);
    }

    public static enum DropZone {
        PREV(".dropzone.prev"),
        NEXT(".dropzone.next");

        private String css;

        private DropZone(final String css) {
            this.css = css;
        }

        public String getCss() {
            return this.css;
        }
    }
}
