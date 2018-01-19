package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static org.openqa.selenium.By.xpath;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class ConfirmDialog extends AbstractFragment {
    @FindBy(className = "s-dialog-cancel-button")
    private WebElement cancelButton;

    @FindBy(className = "s-dialog-submit-button")
    private WebElement submitButton;

    @FindBy(className = "s-dialog-close-button")
    private WebElement closeButton;

    private static By ROOT = xpath("//*[contains(concat(' ', normalize-space(@class), ' '), ' s-dialog ')]");

    public static ConfirmDialog getInstance(final SearchContext searchContext) {
        return Graphene.createPageFragment(ConfirmDialog.class,
                waitForElementVisible(ROOT, searchContext));
    }

    public void cancelClick() {
        waitForElementVisible(cancelButton).click();
        waitForFragmentNotVisible(this);
    }

    public ConfirmDialog submitClick() {
        waitForElementVisible(submitButton).click();

        return this;
    }

    public void closeClick() {
        waitForElementVisible(closeButton).click();
        waitForFragmentNotVisible(this);
    }

    public static boolean isPresent(final SearchContext searchContext) {
        return isElementPresent(ROOT, searchContext);
    }
}
