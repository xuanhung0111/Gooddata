package com.gooddata.qa.graphene.fragments.indigo.analyze;

import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementDisabled;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class ExportToSelect extends AbstractReactDropDown {

    @Override
    protected String getDropdownCssSelector() {
        throw new UnsupportedOperationException("Unsupported getDropdownCssSelector() method");
    }

    @Override
    protected String getListItemsCssSelector() {
        return ".gd-list-item:not([class*='item-header'])";
    }

    public void exportTo(DataType type) {
        waitForElementVisible(By.className(type.toString()), getRoot()).click();
    }

    public boolean isExportToButtonEnabled(DataType type) {
        return !isElementDisabled(waitForElementVisible(By.className(type.toString()), getRoot()));
    }

    public enum DataType {
        CSV("gd-export-menu-export-csv"),
        XLSX("gd-export-menu-export-xlsx");

        private String type;

        DataType(String type) {
            this.type = type;
        }

        public String toString() {
            return type;
        }
    }
}
