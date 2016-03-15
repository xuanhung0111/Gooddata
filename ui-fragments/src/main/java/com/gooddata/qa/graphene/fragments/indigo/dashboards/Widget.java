package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class Widget extends AbstractFragment {
    @FindBy(className = "dash-item-action-delete")
    protected WebElement deleteButton;

    public void clickDeleteButton() {
        waitForElementVisible(deleteButton).click();
    }
}
