package com.gooddata.qa.graphene.fragments.disc.schedule.add;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;

import java.util.Collection;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class DatasetUploadSection extends AbstractFragment {

    @FindBy(className = "ait-dataset-selection-radio-all")
    private WebElement allDatasetsOption;

    @FindBy(className = "ait-dataset-selection-radio-custom")
    private WebElement customDatasetsOption;

    @FindBy(className = "ait-dataset-selection-dropdown-button")
    private DatasetDropdown datasetDropdown;

    public DatasetDropdown getDatasetDropdown() {
        return waitForFragmentVisible(datasetDropdown);
    }

    public void selectAllDatasetsOption() {
        waitForElementVisible(allDatasetsOption).click();
    }

    public void selectCustomDatasetsOption() {
        waitForElementVisible(customDatasetsOption).click();
    }

    public void selectDatasets(String... datasets) {
        selectCustomDatasetsOption();
        getDatasetDropdown().expand().selectDatasets(datasets).submit();
    }

    public boolean isAllDatasetsOptionSelected() {
        return waitForElementVisible(allDatasetsOption).isSelected();
    }

    public boolean isCustomDatasetsOptionSelected() {
        return waitForElementVisible(customDatasetsOption).isSelected();
    }

    public Collection<String> getSelectedDatasets() {
        DatasetDropdown dropdown = getDatasetDropdown();
        try {
            return dropdown.expand().getSelectedDatasets();
        } finally {
            dropdown.collapse();
        }
    }

    public String getOverlappedDatasetMessage() {
        return waitForElementVisible(By.className("datasets-messages"), getRoot()).getText();
    }
}
