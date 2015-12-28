package com.gooddata.qa.graphene.fragments.csvuploader;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class DatasetDetailPage extends AbstractFragment {

    @FindBy(className = "s-dataset-name")
    private WebElement datasetName;

    @FindBy(className = "s-dataset-detail-back-button")
    private WebElement backButton;

    @FindBy(className = "s-dataset-columns-table")
    private DatasetColumnsTable datasetColumns;

    @FindBy(className = "s-dataset-update-ds-from-file-button")
    private WebElement refreshDatasetButton;
    
    @FindBy(xpath = "//.[@class='datasets-sidebar']//a")
    private WebElement latestCsvFileUpload;
    
    @FindBy(css = ".file-detail")
    private WebElement createdDateTime;

    @FindBy(css = ".icon-analyze.button-link")
    private WebElement analyzeButton;

    @FindBy(css = "button.s-delete")
    private WebElement deleteButton;

    public String getDatasetAnalyzeLink() {
        return waitForElementVisible(analyzeButton).getAttribute("href");
    }

    public void downloadTheLatestCsvFileUpload() {
        waitForElementVisible(latestCsvFileUpload).click();
    }
    
    public String getCreatedDateTime() {
        return waitForElementVisible(createdDateTime).getText();
    }

    public void clickBackButton() {
        waitForElementVisible(backButton).click();
    }

    public void clickRefreshButton() {
        waitForElementVisible(refreshDatasetButton).click();
    }

    public void clickDeleteButton() {
        waitForElementVisible(deleteButton).click();
    }

    public String getDatasetName() {
        return waitForElementVisible(datasetName).getText();
    }

    public List<String> getColumnNames() {
        return waitForFragmentVisible(datasetColumns).getColumnNames();
    }

    public List<String> getColumnTypes() {
        return waitForFragmentVisible(datasetColumns).getColumnTypes();
    }
}
