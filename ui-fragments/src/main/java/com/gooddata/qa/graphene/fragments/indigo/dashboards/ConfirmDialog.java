package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;

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
