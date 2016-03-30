package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;

public class ComparisonSelect extends AbstractReactDropDown {

    @Override
    protected String getDropdownCssSelector() {
        return ".overlay .comparison-list";
    }

    @Override
    protected String getSearchInputCssSelector() {
        return null;
    }
}
