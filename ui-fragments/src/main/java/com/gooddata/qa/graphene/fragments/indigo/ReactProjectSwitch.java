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

}
