package com.gooddata.qa.graphene.fragments.csvuploader;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import static org.openqa.selenium.By.className;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.utils.CheckUtils;

public class DatasetsListPage extends AbstractFragment {

    private static final By BY_EMPTY_STATE = By.className("datasets-empty-state");
    private static final By BY_MY_DATASETS_EMPTY_STATE = By.className("my-datasets-empty-state");

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
    
    public String getDatasetAnalyzeLink(String datasetName) {
        return getMyDatasetsTable().getDatasetAnalyzeButton(datasetName).getAttribute("href");
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

    public String getEmptyStateMessage() {
        return waitForEmptyStateLoaded().getText();
    }

    public DatasetsTable getMyDatasetsTable() {
        return waitForFragmentVisible(myDatasetsTable);
    }
    
    public boolean isMyDatasetsEmpty() {
        return getMyDatasetsTable().getRoot().getAttribute("class").contains("empty-state");
    }

    public int getMyDatasetsCount() {
        return isMyDatasetsEmpty()? 0 : getMyDatasetsTable().getNumberOfDatasets();
    }

    public boolean isOtherDatasetsEmpty() {
        waitForFragmentVisible(myDatasetsTable); //this is used as indicator for datasets table is loaded
        return !CheckUtils.isElementPresent(By.cssSelector(".others-datasets"), getRoot());
    }

    public DatasetsTable getOthersDatasetsTable() {
        return waitForFragmentVisible(othersDatasetsTable);
    }

    public int getOtherDatasetsCount() {
        return isOtherDatasetsEmpty()? 0 : waitForFragmentVisible(othersDatasetsTable).getNumberOfDatasets();
    }

    public void uploadFile(String filePath) {
        waitForElementVisible(addDataButton).click();

        Graphene.createPageFragment(FileUploadDialog.class,
                waitForElementVisible(className("s-upload-dialog"), browser))
                .pickCsvFile(filePath)
                .clickUploadButton();
    }
}
