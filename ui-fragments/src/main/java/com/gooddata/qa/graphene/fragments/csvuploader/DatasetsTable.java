package com.gooddata.qa.graphene.fragments.csvuploader;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.fragments.AbstractTable;

import java.util.List;

public class DatasetsTable extends AbstractTable {

    private static final By BY_DATASET_NAME = By.className("s-dataset-name");
    private static final By BY_DATASET_STATUS = By.className("s-dataset-status");

    public List<String> getDatasetNames() {
        return getRows().stream()
                .map(row -> waitForElementVisible(BY_DATASET_NAME, row).getText())
                .collect(toList());
    }

    public WebElement getDatasetRow(final String datasetName) {
        notEmpty(datasetName, "datasetName cannot be empty!");

        return getRows().stream()
                .filter(row -> datasetName.equals(row.findElement(BY_DATASET_NAME).getText()))
                .findFirst()
                .orElse(null);
    }

    public String getDatasetStatus(final String datasetName) {
        final WebElement datasetRow = getDatasetRow(datasetName);

        notNull(datasetRow, "Dataset with name '" + datasetName + "' not found.");

        return waitForElementVisible(BY_DATASET_STATUS, datasetRow).getText();
    }

    public int getNumberOfDatasets() {
        return getNumberOfRows();
    }
}
