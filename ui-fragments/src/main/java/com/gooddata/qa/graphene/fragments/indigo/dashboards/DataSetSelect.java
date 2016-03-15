package com.gooddata.qa.graphene.fragments.indigo.dashboards;

public class DataSetSelect extends ReactDropdown {

    @Override
    public String getDropdownButtonCssSelector() {
        // datasets dropdown is ready immediately, so no .is-loaded flag
        return "button";
    }

    @Override
    public String getDropdownCssSelector() {
        return ".overlay .dataSets-list";
    }

}
