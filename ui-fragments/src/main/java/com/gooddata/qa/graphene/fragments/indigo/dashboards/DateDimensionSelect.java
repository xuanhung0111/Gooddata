package com.gooddata.qa.graphene.fragments.indigo.dashboards;

public class DateDimensionSelect extends ReactDropdown {

    @Override
    public String getDropdownButtonCssSelector() {
        // dimensions dropdown is ready immediately, so no .is-loaded flag
        return "button";
    }

    @Override
    public String getDropdownCssSelector() {
        return ".overlay .dimensions-list";
    }

}
