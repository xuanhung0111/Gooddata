package com.gooddata.qa.graphene.fragments.disc.schedule.add;

import com.gooddata.qa.graphene.fragments.common.AbstractDropDown;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.gooddata.qa.graphene.utils.ElementUtils.getTooltipFromElement;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

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
        return "div[class*=gd-list-view-item]";
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
                .filter(e -> !isHeaderItem(e))
                .filter(e -> name.equals(getDatasetTitle(e)))
                .findFirst().get();
    }

    public boolean isDisabled() {
        return getRoot().getAttribute("class").contains("disabled");
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

    public DatasetDropdown selectMappedDatasetsByLink() {
        waitForElementVisible(By.className("s-btn-select_mapped"), getPanelRoot()).click();
        return this;
    }

    public Collection<String> getAvailableDatasets() {
        return getElements().stream()
                .filter(e -> !isHeaderItem(e))
                .map(this::getDatasetTitle)
                .collect(toList());
    }

    public Collection<String> getSelectedDatasets() {
        return getElements().stream()
                .filter(e -> !isHeaderItem(e))
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

    public Map<String, List<String>> getDatasetGroups() {
        Map<String, List<String>> datasetGroups = new HashMap<>();
        String groupName = null;
        List<String> datasets = null;
        List<WebElement> items = getElements();

        for (WebElement item : items) {
            if (isHeaderItem(item)) {
                if (nonNull(groupName)) {
                    datasetGroups.put(groupName, datasets);
                }
                groupName = item.getText();
                datasets = new ArrayList<>();
            } else {
                datasets.add(getDatasetTitle(item));
            }
        }

        datasetGroups.put(groupName, datasets);
        return datasetGroups;
    }

    public String getTooltipFromIncrementalGroup() {
        WebElement helpIcon = waitForElementVisible(By.className("inlineBubbleHelp"),
                getHeaderItem("INCREMENTAL LOAD"));
        return getTooltipFromElement(helpIcon, browser);
    }

    public String getTooltipFromUnmappedGroup() {
        WebElement errorIcon = waitForElementVisible(By.className("dataset-status-icon-unmapped"),
                getHeaderItem("UNMAPPED"));
        return getTooltipFromElement(errorIcon, browser);
    }

    public String getTooltipFromUnloadedDataset(String dataset) {
        WebElement status = waitForElementVisible(By.className("dataset-status-text-incremetal"),
                getElementByName(dataset));
        return getTooltipFromElement(status, browser);
    }

    private boolean isHeaderItem(WebElement item) {
        return item.getAttribute("class").contains("header");
    }

    private WebElement getHeaderItem(String item) {
        return getElements().stream()
                .filter(this::isHeaderItem)
                .filter(e -> e.getText().equals(item)).findFirst().get();
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
