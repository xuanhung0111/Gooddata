package com.gooddata.qa.graphene.fragments.disc.schedule.add;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.stream.Stream;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.disc.ConfirmationDialog;

public class RunOneOffDialog extends ConfirmationDialog {

    @FindBy(className = "ait-dataset-selection-dropdown-button")
    private DatasetDropdown datasetDropdown;

    public static RunOneOffDialog getInstance(SearchContext searchContext) {
        return getInstance(searchContext, RunOneOffDialog.class);
    }

    public Collection<LoadMode> getModes() {
        return Stream.of(LoadMode.values())
                .filter(mode -> isElementVisible(mode.getLocator(), getRoot())).collect(toList());
    }

    public LoadMode getSelectedMode() {
        return Stream.of(LoadMode.values())
                .filter(mode -> waitForElementVisible(mode.getLocator(), getRoot()).isSelected())
                .findFirst().get();
    }

    public RunOneOffDialog setMode(LoadMode mode) {
        waitForElementVisible(mode.getLocator(), getRoot()).click();
        return this;
    }

    public DatasetDropdown getDatasetDropdown() {
        return waitForFragmentVisible(datasetDropdown);
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
