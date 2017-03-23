package com.gooddata.qa.graphene.fragments.disc;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class ConfirmationDialog extends AbstractFragment {

    private static final By LOCATOR = By.className("gd-dialog");

    @FindBy(className = "dialog-header-area")
    private WebElement header;

    @FindBy(className = "dialog-body")
    private WebElement body;

    @FindBy(css = ".button-bar-area button:first-child")
    private WebElement confirmButton;

    @FindBy(css = ".button-bar-area button:last-child")
    private WebElement discardButton;

    public static final ConfirmationDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(ConfirmationDialog.class, waitForElementVisible(LOCATOR, searchContext));
    }

    public String getTitle() {
        return waitForElementVisible(header).getText();
    }

    public String getMessage() {
        return waitForElementVisible(body).getText();
    }

    public void confirm() {
        waitForElementVisible(confirmButton).click();
        waitForFragmentNotVisible(this);
    }

    public void discard() {
        waitForElementVisible(discardButton).click();
        waitForFragmentNotVisible(this);
    }
}
