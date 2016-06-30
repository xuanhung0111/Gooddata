package com.gooddata.qa.graphene.fragments.indigo.analyze;

import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;

public class DateDimensionSelect extends AbstractReactDropDown {

    public boolean isEnabled() {
        return !getDropdownButton().getAttribute("class").contains("disabled");
    }

    @Override
    protected String getDropdownCssSelector() {
        return ".overlay.dropdown-body";
    }

    @Override
    protected boolean isDropdownOpen() {
        return getDropdownButton().getAttribute("class").contains("s-expanded");
    }

    @Override
    protected String getSearchInputCssSelector() {
        return null;
    }

    @Override
    protected String getListItemsCssSelector() {
        return ".gd-list-item:not([class*='item-header'])";
    }

    @Override
    protected WebElement getElementByName(final String name) {
        waitForPickerLoaded();
        return getElements()
                .stream()
                .filter(e -> name.equals(e.getText()))
                .findFirst()
                .get();
    }
}
