package com.gooddata.qa.graphene.fragments.disc.schedule.add;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.stream.Stream;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.fragments.common.AbstractDropDown;

public class DatasetDropdown extends AbstractDropDown {

    @Override
    protected String getDropdownCssSelector() {
        return ".ait-dataset-selection-dropdown";
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

    @Override
    protected String getSearchInputCssSelector() {
        return ".searchfield-input";
    }

    @Override
    protected WebElement getElementByName(String name) {
        return getElements().stream()
                .filter(e -> name.equals(getDatasetTitle(e)))
                .findFirst().get();
    }

    public String getButtonText() {
        return getRoot().getText();
    }

    public DatasetDropdown expand() {
        if (isCollapsed()) {
            this.getRoot().click();
        }
        return this;
    }

    public DatasetDropdown collapse() {
        if (!isCollapsed()) {
            this.getRoot().click();
        }
        return this;
    }

    public DatasetDropdown searchDataset(String searchKey) {
        WebElement searchInput = waitForElementVisible(By.cssSelector(getSearchInputCssSelector()), getPanelRoot());
        searchInput.clear();
        searchInput.sendKeys(searchKey);
        return this;
    }

    public DatasetDropdown selectDatasets(String... datasets) {
        clearAllSelected();
        Stream.of(datasets).map(dataset -> getElementByName(dataset)).forEach(WebElement::click);
        return this;
    }

    public void submit() {
        waitForElementVisible(getSaveButtonLocator(), getPanelRoot()).click();
    }

    public boolean isSaveButtonEnabled() {
        return !waitForElementVisible(getSaveButtonLocator(), getPanelRoot())
                .getAttribute("class").contains("disabled");
    }

    public void cancel() {
        waitForElementVisible(getCancelButtonLocator(), getPanelRoot()).click();
    }

    public DatasetDropdown clearAllSelected() {
        waitForElementVisible(By.className("s-btn-clear"), getPanelRoot()).click();
        return this;
    }

    public DatasetDropdown selectAllDatasets() {
        waitForElementVisible(By.className("s-btn-select_all"), getPanelRoot()).click();
        return this;
    }

    public Collection<String> getAvailableDatasets() {
        return getElements().stream()
                .map(this::getDatasetTitle)
                .collect(toList());
    }

    public Collection<String> getSelectedDatasets() {
        return getElements().stream()
                .filter(e -> e.getAttribute("class").contains("is-selected"))
                .map(this::getDatasetTitle)
                .collect(toList());
    }

    public String getLSLTSOf(String dataset) {
        return getElementByName(dataset).findElement(By.className("dataset-status-icon-container")).getText();
    }

    public boolean hasLSLTSValueFor(String dataset) {
        return !getLSLTSOf(dataset).isEmpty();
    }

    private boolean isCollapsed() {
        return !isElementVisible(By.cssSelector(getDropdownCssSelector()), browser);
    }

    private By getSaveButtonLocator() {
        return By.className("button-positive");
    }

    private By getCancelButtonLocator() {
        return By.className("s-btn-cancel");
    }

    private String getDatasetTitle(WebElement dataset) {
        return dataset.findElement(By.className("dataset-title")).getText();
    }
}
