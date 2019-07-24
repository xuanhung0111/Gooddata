package com.gooddata.qa.graphene.fragments.disc.process;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.CssUtils.simplifyText;

import com.gooddata.qa.graphene.fragments.common.AbstractDropDown;
import com.gooddata.qa.graphene.utils.ElementUtils;
import org.openqa.selenium.By;

public class DataSourceDropdown extends AbstractDropDown {

    @Override
    protected String getDropdownCssSelector() {
        return ".datasource-selection-dropdown";
    }

    @Override
    protected String getDropdownButtonCssSelector() {
        return "button";
    }

    @Override
    protected String getListItemsCssSelector() {
        return ".gd-list-view-item";
    }

    @Override
    protected void waitForPickerLoaded() {
        // Picker is loaded instantly and no need to wait more
    }

    public DataSourceDropdown expand() {
        if (isCollapsed()) {
            this.getRoot().click();
        }
        return this;
    }

    public DataSourceDropdown collapse() {
        if (!isCollapsed()) {
            this.getRoot().click();
        }
        return this;
    }

    public DataSourceDropdown selectDataSource(String dataSourceTitle) {
        By selector = By.cssSelector(".s-" + simplifyText(dataSourceTitle));
        ElementUtils.scrollElementIntoView(
                By.cssSelector(".datasource-selection-dropdown .ember-view.ember-list-view"), selector, browser, 50);
        waitForElementVisible(selector, browser).click();
        return this;
    }

    private boolean isCollapsed() {
        return !isElementVisible(By.cssSelector(getDropdownCssSelector()), browser);
    }
}
