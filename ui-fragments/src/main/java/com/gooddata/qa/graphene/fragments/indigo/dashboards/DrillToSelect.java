package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;

public class DrillToSelect extends AbstractReactDropDown {

    public static final String PLACEHOLDER = "Select a dashboard";

    @Override
    protected String getDropdownCssSelector() {
        return ".overlay .s-drill-to-list";
    }

    @Override
    protected String getSearchInputCssSelector() {
        return null;
    }
}
