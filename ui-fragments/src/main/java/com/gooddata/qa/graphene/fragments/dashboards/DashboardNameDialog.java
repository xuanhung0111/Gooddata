package com.gooddata.qa.graphene.fragments.dashboards;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class DashboardNameDialog extends AbstractFragment {
    @FindBy(tagName = "input")
    private WebElement title;

    @FindBy(css = ".s-btn-save")
    private WebElement saveButton;

    @FindBy(css = ".s-btn-cancel")
    private WebElement cancelButton;

    public void renameDashboard(String newName) {
        waitForElementVisible(title).clear();
        waitForElementVisible(title).sendKeys(newName);
        waitForElementVisible(saveButton).click();
        waitForElementNotPresent(title);
    }
}
