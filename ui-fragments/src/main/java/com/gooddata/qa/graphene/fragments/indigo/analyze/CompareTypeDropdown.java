package com.gooddata.qa.graphene.fragments.indigo.analyze;

import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;
import org.openqa.selenium.WebElement;

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

    /**
     * select compare type
     * @param compareType
     */
    public void selectCompareType(final String compareType) {
        selectByName(compareType);
        ensureDropdownClosed();
    }

    /**
     * compare types of {@link CompareTypeDropdown}
     */
    public enum CompareType {

        NOTHING("nothing"),
        SAME_PERIOD_LAST_YEAR("same_period_last_year");

        private String compareTypeName;

        CompareType(String type) {
            this.compareTypeName = type;
        }

        public String getCompareTypeName() {
            return compareTypeName;
        }
    }
}
