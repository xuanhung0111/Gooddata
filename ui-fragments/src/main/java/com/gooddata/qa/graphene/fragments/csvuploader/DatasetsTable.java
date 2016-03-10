package com.gooddata.qa.graphene.fragments.csvuploader;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.apache.commons.lang.Validate.notEmpty;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.springframework.util.CollectionUtils;

import com.gooddata.qa.graphene.fragments.AbstractTable;
import com.gooddata.qa.graphene.utils.Sleeper;

public class DatasetsTable extends AbstractTable {

    private static final By BY_DATASET_NAME = By.className("s-dataset-name");

    public List<String> getDatasetNames() {
        // To get the correct number in both cases: empty and non-empty list
        Sleeper.sleepTightInSeconds(3);
        return getElementTexts(getRows(), row -> waitForElementVisible(BY_DATASET_NAME, row));
    }

    public Dataset getDataset(final String datasetName) {
        notEmpty(datasetName, "datasetName cannot be empty!");

        if (CollectionUtils.isEmpty(getRows())) {
            throw new NoSuchElementException("Dataset name: " + datasetName + " not exist");
        }

        WebElement dataset = getRows()
                .stream()
                .filter(row -> datasetName.equals(row.findElement(BY_DATASET_NAME).getText()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Dataset name: " + datasetName + " not exist"));

        return Graphene.createPageFragment(Dataset.class, waitForElementVisible(dataset));
    }

    public int getNumberOfDatasets() {
        // To get the correct number in both cases: empty and non-empty list
        Sleeper.sleepTightInSeconds(3);
        return getNumberOfRows();
    }
}
