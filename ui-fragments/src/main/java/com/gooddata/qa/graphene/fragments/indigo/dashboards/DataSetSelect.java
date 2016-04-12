package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;

public class DataSetSelect extends AbstractReactDropDown {

    @Override
    protected String getDropdownCssSelector() {
        return ".overlay .dataSets-list";
    }

    @Override
    protected String getSearchInputCssSelector() {
        return null;
    }
}
