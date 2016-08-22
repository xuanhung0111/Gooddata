package com.gooddata.qa.graphene.fragments.common;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static org.openqa.selenium.By.cssSelector;

import org.jboss.arquillian.graphene.Graphene;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class IpeEditor extends AbstractFragment {

    private static final By LOCATOR = cssSelector(".c-ipeEditor[style*='display: block']");

    @FindBy(css = ".c-ipeEditorIn > *")
    private WebElement input;

    @FindBy(className = "s-ipeSaveButton")
    private WebElement saveButton;

    @FindBy(className = "s-ipeCancelButton")
    private WebElement cancelButton;

    public static IpeEditor getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(IpeEditor.class, waitForElementVisible(LOCATOR, searchContext));
    }

    public static boolean isPresent(SearchContext searchContext) {
        return isElementPresent(LOCATOR, searchContext);
    }

    public void setText(String text) {
        waitForElementVisible(input).clear();
        input.sendKeys(text);
        waitForElementVisible(saveButton).click();
    }

    public void cancel() {
        waitForElementVisible(cancelButton).click();
        waitForFragmentNotVisible(this);
    }
}
