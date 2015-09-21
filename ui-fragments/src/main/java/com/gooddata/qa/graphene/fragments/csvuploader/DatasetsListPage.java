package com.gooddata.qa.graphene.fragments.csvuploader;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.CheckUtils.*;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class DatasetsListPage extends AbstractFragment {

    private static final By BY_EMPTY_STATE = By.className("datasets-empty-state");
    private static final By BY_MY_DATASETS_EMPTY_STATE = By.className("my-datasets-empty-state");
    private static final By BY_OTHERS_DATASETS_EMPTY_STATE = By.className("others-datasets-empty-state");

    @FindBy(className = "s-datasets-list-header")
    private WebElement datasetsHeader;

    @FindBy(className = "s-add-data-button")
    private WebElement addDataButton;

    @FindBy(className = "s-my-datasets-list")
    private DatasetsTable myDatasetsTable;

    @FindBy(className = "others-datasets")
    private DatasetsTable othersDatasetsTable;

    public void clickDatasetDetailButton(String datasetName) {
        getMyDatasetsTable().getDatasetDetailButton(datasetName).click();
    }

    public void clickAddDataButton() {
        waitForAddDataButtonVisible().click();
    }

    public WebElement waitForHeaderVisible() {
        return waitForElementVisible(datasetsHeader);
    }

    public WebElement waitForAddDataButtonVisible() {
        return waitForElementVisible(addDataButton);
    }

    public WebElement waitForEmptyStateLoaded() {
        return waitForElementVisible(BY_EMPTY_STATE, browser);
    }

    public WebElement waitForMyDatasetsEmptyStateLoaded() {
        return waitForElementVisible(BY_MY_DATASETS_EMPTY_STATE, browser);
    }

    public WebElement waitForOthersDatasetsEmptyStateLoaded() {
        return waitForElementVisible(BY_OTHERS_DATASETS_EMPTY_STATE, browser);
    }

    public String getEmptyStateMessage() {
        return waitForEmptyStateLoaded().getText();
    }

    public DatasetsTable getMyDatasetsTable() {
        return waitForFragmentVisible(myDatasetsTable);
    }

    public int getMyDatasetsCount() {
        return getMyDatasetsTable().getNumberOfDatasets();
    }

    public DatasetsTable getOthersDatasetsTable() {
        return waitForFragmentVisible(othersDatasetsTable);
    }
}
