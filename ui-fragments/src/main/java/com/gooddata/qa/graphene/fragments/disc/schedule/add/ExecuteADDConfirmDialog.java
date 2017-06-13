package com.gooddata.qa.graphene.fragments.disc.schedule.add;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;

import com.gooddata.qa.graphene.fragments.disc.ConfirmationDialog;

public class ExecuteADDConfirmDialog extends ConfirmationDialog {

    public static ExecuteADDConfirmDialog getInstance(SearchContext searchContext) {
        return getInstance(searchContext, ExecuteADDConfirmDialog.class);
    }

    public ExecuteADDConfirmDialog setMode(LoadMode mode) {
        waitForElementVisible(mode.getLocator(), getRoot()).click();
        return this;
    }

    public enum LoadMode {
        DEFAULT("input[value=DEFAULT]"),
        FULL("input[value=FULL]"),
        INCREMENTAL("input[value=INCREMENTAL]");

        private String locator;

        private LoadMode(String locator) {
            this.locator = locator;
        }

        public By getLocator() {
            return By.cssSelector(locator);
        }
    }
}
