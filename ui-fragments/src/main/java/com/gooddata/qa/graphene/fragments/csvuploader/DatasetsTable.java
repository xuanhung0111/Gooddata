package com.gooddata.qa.graphene.fragments.csvuploader;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.util.CollectionUtils;

import com.gooddata.qa.graphene.fragments.AbstractTable;
import com.gooddata.qa.graphene.utils.Sleeper;

public class DatasetsTable extends AbstractTable {

    private static final By BY_DATASET_NAME = By.className("s-dataset-name");
    private static final By BY_DATASET_STATUS = By.className("s-dataset-status");
    private static final By BY_DATASET_DELETE_BUTTON = By.className("s-dataset-delete-button");
    private static final By BY_DATASET_DETAIL_BUTTON = By.className("s-dataset-detail-button");
    private static final By BY_DATASET_REFRESH_BUTTON = By.className("s-dataset-update-button");
    private static final By BY_DATASET_ANALYZE_BUTTON = By.cssSelector(".icon-analyze.button-link");

    public List<String> getDatasetNames() {
        // To get the correct number in both cases: empty and non-empty list
        Sleeper.sleepTightInSeconds(3);
        return getElementTexts(getRows(), row -> waitForElementVisible(BY_DATASET_NAME, row));
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
    
    public WebElement getDatasetAnalyzeButton(String datasetName) {
        return getDatasetRowCell(datasetName, BY_DATASET_ANALYZE_BUTTON);
    }

    public int getNumberOfDatasets() {
        // To get the correct number in both cases: empty and non-empty list
        Sleeper.sleepTightInSeconds(3);
        return getNumberOfRows();
    }

    private WebElement getDatasetRowCell(String datasetName, By by) {
        final WebElement datasetRow = getDatasetRow(datasetName);

        notNull(datasetRow, "Dataset with name '" + datasetName + "' not found.");

        return waitForElementVisible(by, datasetRow);
    }
}
