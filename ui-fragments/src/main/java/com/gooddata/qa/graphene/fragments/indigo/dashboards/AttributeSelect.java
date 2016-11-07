package com.gooddata.qa.graphene.fragments.indigo.dashboards;


import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;

public class AttributeSelect extends AbstractReactDropDown {

    @Override
    protected String getDropdownCssSelector() {
        return ".overlay .attributes-list";
    }

    @Override
    public AbstractReactDropDown selectByName(String name) {
        ensureDropdownOpen();
        searchForText(name);
        getElementByName(name).click();

        return this;
    }
}
