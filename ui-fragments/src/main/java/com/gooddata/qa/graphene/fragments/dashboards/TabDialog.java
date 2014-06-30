package com.gooddata.qa.graphene.fragments.dashboards;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class TabDialog extends AbstractFragment {

    @FindBy(tagName = "input")
    private WebElement title;

    @FindBy(css = ".s-btn-save")
    private WebElement saveButton;

    @FindBy(css = ".s-btn-cancel")
    private WebElement cancelButton;

    public void createTab(String tabName) {
        waitForElementVisible(title).sendKeys(tabName);
        waitForElementVisible(saveButton).click();
        waitForElementNotPresent(title);
    }

}
