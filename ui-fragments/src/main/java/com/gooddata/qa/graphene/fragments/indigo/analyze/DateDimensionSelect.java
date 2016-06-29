package com.gooddata.qa.graphene.fragments.indigo.analyze;

import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;

public class DateDimensionSelect extends AbstractReactDropDown {

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
    protected WebElement getElementByName(final String name) {
        waitForPickerLoaded();
        return getElements()
                .stream()
                .filter(e -> name.equals(e.getText()))
                .findFirst()
                .get();
    }
}
