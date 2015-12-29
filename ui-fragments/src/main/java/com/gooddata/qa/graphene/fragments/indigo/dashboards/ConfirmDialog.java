package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class ConfirmDialog extends AbstractFragment {
    @FindBy(className = "s-dialog-cancel-button")
    private WebElement cancelButton;

    @FindBy(className = "s-dialog-submit-button")
    private WebElement submitButton;

    public ConfirmDialog cancelClick() {
        waitForElementVisible(cancelButton).click();

        return this;
    }

    public ConfirmDialog submitClick() {
        waitForElementVisible(submitButton).click();

        return this;
    }
}
