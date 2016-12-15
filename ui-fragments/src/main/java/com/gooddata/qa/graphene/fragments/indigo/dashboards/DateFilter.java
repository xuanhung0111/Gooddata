package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static org.openqa.selenium.By.cssSelector;

import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class DateFilter extends AbstractReactDropDown {
    @FindBy(className = "button-subtitle")
    private WebElement buttonText;

    @Override
    protected String getDropdownButtonCssSelector() {
        return ".date-filter-dropdown-button";
    }

    @Override
    protected String getDropdownCssSelector() {
        return ".overlay .s-date-filter-dropdown";
    }

    @Override
    protected String getSearchInputCssSelector() {
        return null;
    }

    public String getSelection() {
        waitForElementVisible(buttonText);
        return buttonText.getText();
    }

    public boolean isInfoMessageDisplayed() {
        return isElementPresent(cssSelector(".set-default-filter-message"), getPanelRoot());
    }
}
