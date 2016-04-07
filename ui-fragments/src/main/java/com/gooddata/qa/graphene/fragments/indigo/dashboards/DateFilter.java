package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static org.openqa.selenium.By.cssSelector;

import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;

public class DateFilter extends AbstractReactDropDown {

    @Override
    protected String getDropdownCssSelector() {
        return ".overlay .s-date-filter-dropdown";
    }

    @Override
    protected String getSearchInputCssSelector() {
        return null;
    }

    public boolean isInfoMessageDisplayed() {
        return isElementPresent(cssSelector(".set-default-filter-message"), getPanelRoot());
    }
}
