package com.gooddata.qa.graphene.fragments.indigo.analyze;

import com.gooddata.qa.graphene.enums.indigo.CompareType;
import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;
import org.openqa.selenium.WebElement;

import static com.gooddata.qa.utils.CssUtils.simplifyText;

/**
 * Fragment representing dropdown of compare types of date filter
 */
public class CompareTypeDropdown extends AbstractReactDropDown {

    @Override
    protected String getDropdownCssSelector() {
        return ".s-compare-types-list";
    }
    
    public boolean isCompareTypeEnabled(final CompareType compareType) {
        ensureDropdownOpen();
        final WebElement elementByName = getElementByName(compareType.getCompareTypeName());
        return elementByName != null && !elementByName.getAttribute("class").contains("disabled");
    }
}
