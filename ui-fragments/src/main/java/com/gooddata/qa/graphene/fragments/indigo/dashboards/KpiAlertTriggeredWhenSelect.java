package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;

public class KpiAlertTriggeredWhenSelect extends AbstractReactDropDown {

    @Override
    protected String getDropdownCssSelector() {
        return ".overlay .s-alert-list";
    }

    @Override
    protected String getSearchInputCssSelector() {
        return null;
    }
}
