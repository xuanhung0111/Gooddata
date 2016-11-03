package com.gooddata.qa.graphene.fragments.csvuploader;

import static java.lang.String.format;
import static java.util.Objects.isNull;

import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;
import com.gooddata.qa.utils.EnumUtils;

public class DataTypeSelect extends AbstractReactDropDown {

    @Override
    protected String getDropdownButtonCssSelector() {
        return "button.button-dropdown:not([data-reactid*=date-format])";
    }

    @Override
    protected String getDropdownCssSelector() {
        return ".dropdown-list";
    }

    @Override
    protected String getSearchInputCssSelector() {
        return null;
    }

    @Override
    protected String getListItemsCssSelector() {
        return ".gd-list-item:not(.is-header)";
    }

    @Override
    protected void waitForPickerLoaded() {
    }

    @Override
    public AbstractReactDropDown selectByName(String name) {
        ensureDropdownOpen();
        getElementByName(name).click();
        waitForSelectionIsApplied(name);
        return this;
    }

    @Override
    public WebElement getElementByName(String name) {
        final ColumnType type = EnumUtils.lookup(name, ColumnType.class, null, "getVisibleText");
        if (isNull(type)) {
            throw new IllegalArgumentException("Unknown data type: " + name);
        }

        ensureDropdownOpen();
        return getElement(format("[data-reactid*='%s']", type.getValue()));
    }

    @Override
    public boolean isDropdownOpen() {
        return getDropdownButton().getAttribute("class").contains("is-dropdown-open");
    }

    public String getTypeSelection() {
        return getDropdownButton().getAttribute("title");
    }

    public static enum ColumnType {
        ATTRIBUTE("Attribute"),
        FACT("Measure"),
        DATE("Date");

        private String typeByVisibleText;

        private ColumnType(String typeByVisibleText) {
            this.typeByVisibleText = typeByVisibleText;
        }

        public String getVisibleText() {
            return typeByVisibleText;
        }

        public String getValue() {
            return this.name();
        }
    }
}
