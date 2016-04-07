package com.gooddata.qa.graphene.fragments.indigo;

import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;

public class ReactProjectSwitch extends AbstractReactDropDown {

    @Override
    protected String getListItemsCssSelector() {
        return ".gd-project-list-item";
    }

    @Override
    protected String getDropdownButtonCssSelector() {
        return ".gd-header-project";
    }

    @Override
    protected String getDropdownCssSelector() {
        return ".overlay.project-picker-dropdown";
    }

    @Override
    protected void waitForSelectionIsApplied(String name) {
        // When switch project with Embeded Dashboard role, user is directed to Projects.html page 
        // and project picker button will disappears
        // So override the origin action because cannot wait that button here
    }

}
