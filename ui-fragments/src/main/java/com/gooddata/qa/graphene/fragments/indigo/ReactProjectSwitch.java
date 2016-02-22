package com.gooddata.qa.graphene.fragments.indigo;

import com.gooddata.qa.graphene.fragments.indigo.dashboards.ReactDropdown;

public class ReactProjectSwitch extends ReactDropdown {

    @Override
    public String getListItemCssSelector() {
        return ".gd-project-list-item";
    }

    @Override
    public String getDropdownButtonCssSelector() {
        return ".gd-header-project";
    }

    @Override
    public String getDropdownCssSelector() {
        return ".overlay.project-picker-dropdown";
    }

    // When switch project with Embeded Dashboard role, user is directed to Projects.html page 
    // and project picker button will disappears
    // So override the origin action because cannot wait that button here
    @Override
    public ReactDropdown selectByName(String name) {
        searchByName(name);
        getElementByName(name).click();

        return this;
    }

}
