package com.gooddata.qa.graphene.fragments.indigo.dashboards;


import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;
import com.gooddata.qa.graphene.utils.ElementUtils;

public class AttributeSelect extends AbstractReactDropDown {

    @Override
    protected String getDropdownCssSelector() {
        return ".overlay .attributes-list";
    }

    @Override
    public AbstractReactDropDown selectByName(String name) {
        getSearchElement(name).click();

        return this;
    }

    /**
     * Get tooltip when hovering on attribute that has long name
     * 
     * @param attribute
     * @return tooltip content
     */
    public String getTooltipOnAttribute(String attribute) {
        return ElementUtils.getTooltipFromElement(getSearchElement(attribute), browser);
    }

    private WebElement getSearchElement(String name) {
        waitForPickerLoaded();
        searchForText(name);
        return getElementByName(name);
    }
}
