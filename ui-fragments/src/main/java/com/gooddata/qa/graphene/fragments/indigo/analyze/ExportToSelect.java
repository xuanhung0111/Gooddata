package com.gooddata.qa.graphene.fragments.indigo.analyze;

import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;
import org.openqa.selenium.By;

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

    public void exportTo(ExportDataType type) {
        waitForElementVisible(By.className(type.toString()), getRoot()).click();
    }

    public enum ExportDataType {
        CSV("gd-export-menu-export-csv"),
        XLSX("gd-export-menu-export-xlsx");

        private String type;

        ExportDataType(String type) {
            this.type = type;
        }

        public String toString() {
            return type;
        }
    }
}
