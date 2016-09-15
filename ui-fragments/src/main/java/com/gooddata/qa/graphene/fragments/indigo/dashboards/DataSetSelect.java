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

    @Override
    protected String getListItemsCssSelector() {
        // need to remove item headers like RECOMMENDED, OTHER from list of items
        return ".gd-list-item:not(.is-header):not(.gd-list-item-header)";
    }
}
