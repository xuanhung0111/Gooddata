package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import com.gooddata.qa.graphene.utils.CheckUtils;
import org.openqa.selenium.By;

public class DateFilter extends ReactDropdown {

    private static final String DROPDOWN_CSS_SELECTOR = ".overlay .s-date-filter-dropdown";
    private static final String DROPDOWN_MESSAGE_CSS_SELECTOR = DROPDOWN_CSS_SELECTOR + " .set-default-filter-message";

    @Override
    public String getDropdownCssSelector() {
        return DROPDOWN_CSS_SELECTOR;
    }

    public boolean isInfoMessageDisplayed() {
        return CheckUtils.isElementPresent(By.cssSelector(DROPDOWN_MESSAGE_CSS_SELECTOR), browser);
    }
}
