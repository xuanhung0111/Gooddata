package com.gooddata.qa.graphene.fragments.indigo.analyze;

import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

/**
 * Fragment representing dropdown of compare types of date filter
 */
public class CompareTypeDropdown extends AbstractReactDropDown {

    @FindBy(className = "s-compare-apply-incompatible")
    private WebElement messageCompareApplyIncompatible;

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

    public String getMessageCompareApplyIncompatible() {
        return waitForElementVisible(messageCompareApplyIncompatible).getText();
    }

    /**
     * compare types of {@link CompareTypeDropdown}
     */
    public enum CompareType {

        NOTHING("nothing"),
        SAME_PERIOD_PREVIOUS_YEAR("same_period_previous_year"),
        PREVIOUS_PERIOD("Previous Period");

        private String compareTypeName;

        CompareType(String type) {
            this.compareTypeName = type;
        }

        public String getCompareTypeName() {
            return compareTypeName;
        }
    }
}
