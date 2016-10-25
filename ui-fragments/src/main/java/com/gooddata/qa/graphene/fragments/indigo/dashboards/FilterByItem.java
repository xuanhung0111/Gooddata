package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class FilterByItem extends AbstractFragment {

    @FindBy(css = "input")
    private WebElement checkbox;

    @FindBy(className = "title")
    private WebElement title;

    private void clickTitle() {
        waitForElementPresent(title).click();
    }

    public boolean isChecked() {
        return waitForElementPresent(checkbox).isSelected();
    }

    public FilterByItem setChecked(boolean checked) {
        if (this.isChecked() == checked) {
            return this;
        }

        this.clickTitle();
        return this;
    }

    public String getTitle() {
        return waitForElementPresent(title).getText();
    }
}
