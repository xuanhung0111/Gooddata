package com.gooddata.qa.graphene.fragments.csvuploader;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.util.CollectionUtils;

import com.gooddata.qa.graphene.fragments.AbstractTable;
import java.util.List;

public class DatasetsTable extends AbstractTable {

    private static final By BY_DATASET_NAME = By.className("s-dataset-name");
    private static final By BY_DATASET_STATUS = By.className("s-dataset-status");
    private static final By BY_DATASET_DELETE_BUTTON = By.className("s-dataset-delete-button");
    private static final By BY_DATASET_DETAIL_BUTTON = By.className("s-dataset-detail-button");
    private static final By BY_DATASET_REFRESH_BUTTON = By.className("s-dataset-update-button");

    public List<String> getDatasetNames() {
        return getRows().stream()
                .map(row -> waitForElementVisible(BY_DATASET_NAME, row).getText())
                .collect(toList());
    }

    public WebElement getDatasetRow(final String datasetName) {
        notEmpty(datasetName, "datasetName cannot be empty!");

        if (CollectionUtils.isEmpty(getRows())) {
            return null;
        }

        return getRows().stream()
                .filter(row -> datasetName.equals(row.findElement(BY_DATASET_NAME).getText()))
                .findFirst()
                .orElse(null);
    }

    public String getDatasetStatus(String datasetName) {
        return getDatasetRowCell(datasetName, BY_DATASET_STATUS).getText();
    }

    public WebElement getDatasetDeleteButton(String datasetName) {
        return getDatasetRowCell(datasetName, BY_DATASET_DELETE_BUTTON);
    }
    
    public WebElement getDatasetDetailButton(String datasetName) {
        return getDatasetRowCell(datasetName, BY_DATASET_DETAIL_BUTTON);
    }

    public WebElement getDatasetRefreshButton(String datasetName) {
        return getDatasetRowCell(datasetName, BY_DATASET_REFRESH_BUTTON);
    }

    public int getNumberOfDatasets() {
        return getNumberOfRows();
    }

    private WebElement getDatasetRowCell(String datasetName, By by) {
        final WebElement datasetRow = getDatasetRow(datasetName);

        notNull(datasetRow, "Dataset with name '" + datasetName + "' not found.");

        return waitForElementVisible(by, datasetRow);
    }
}
