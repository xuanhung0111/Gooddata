package com.gooddata.qa.graphene.fragments.indigo.analyze;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementEnabled;
import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;

public class CanvasSelect extends AbstractReactDropDown {

    @Override
    protected String getDropdownCssSelector() {
        return ".overlay.dropdown-body";
    }

    @Override
    public boolean isDropdownOpen() {
        return waitForElementEnabled(getDropdownButton()).getAttribute("class").contains("is-dropdown-open");
    }

    public enum DataLabel {

        SHOW("show"),
        HIDE("hide"),
        AUTO("auto__default_");

        private String option;

        DataLabel(String option) {
            this.option = option;
        }

        @Override
        public String toString() {
            return option;
        }
    }
}
