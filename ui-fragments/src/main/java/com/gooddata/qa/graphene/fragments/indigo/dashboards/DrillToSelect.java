package com.gooddata.qa.graphene.fragments.indigo.dashboards;

public class DrillToSelect extends ReactDropdown {

    public static final String PLACEHOLDER = "Add link";

    @Override
    public String getDropdownCssSelector() {
        return ".overlay .s-drill-to-list";
    }

}
