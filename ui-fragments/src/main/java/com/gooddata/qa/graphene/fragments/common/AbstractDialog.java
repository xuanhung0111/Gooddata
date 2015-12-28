package com.gooddata.qa.graphene.fragments.common;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public abstract class AbstractDialog extends AbstractFragment {

    @FindBy(css = ".s-btn-save")
    protected WebElement saveButton;

    @FindBy(css = ".s-btn-cancel")
    protected WebElement cancelButton;

    public void saveChange() {
        waitForElementVisible(saveButton).click();
    }

    public void discardChange() {
        waitForElementVisible(cancelButton).click();
        waitForElementNotVisible(this.getRoot());
    }
}
