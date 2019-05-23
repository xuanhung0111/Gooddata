package com.gooddata.qa.graphene.fragments.indigo.analyze;

import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.function.Function;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementDisabled;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class ExportToSelect extends AbstractReactDropDown {

    @FindBy(className = "s-gd-export-menu-open-report")
    private WebElement exportToReportButton;

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

    public void exportReport() {
        final int numberOfWindows = browser.getWindowHandles().size();
        waitForElementVisible(exportToReportButton).click();

        //make sure the new window is displayed to prevent unexpected errors
        Function<WebDriver, Boolean> hasNewWindow = browser -> browser.getWindowHandles().size() == numberOfWindows + 1;
        Graphene.waitGui().until(hasNewWindow);
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
