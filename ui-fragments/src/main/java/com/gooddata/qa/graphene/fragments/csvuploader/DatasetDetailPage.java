package com.gooddata.qa.graphene.fragments.csvuploader;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class DatasetDetailPage extends AbstractFragment {
    
    private static final String ANALYZE_BUTTON_LOCATOR = ".icon-analyze.button-link";
    private static final String DELETE_BUTTON_LOCATOR = "button.s-delete";
    private static final String REFRESH_BUTTON_LOCATOR = "s-dataset-update-ds-from-file-button";

    @FindBy(className = "s-dataset-name")
    private WebElement datasetName;

    @FindBy(className = "s-dataset-detail-back-button")
    private WebElement backButton;

    @FindBy(className = "s-dataset-columns-table")
    private DatasetColumnsTable datasetColumns;

    @FindBy(xpath = "//.[@class='datasets-sidebar']//a")
    private WebElement latestCsvFileUpload;

    @FindBy(css = ".file-detail")
    private WebElement createdDateTime;

    @FindBy(css = ANALYZE_BUTTON_LOCATOR)
    private WebElement analyzeButton;

    @FindBy(className = REFRESH_BUTTON_LOCATOR)
    private WebElement refreshDatasetButton;

    @FindBy(css = DELETE_BUTTON_LOCATOR)
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

    public boolean isDeleteButtonVisible() {
        return isElementPresent(By.cssSelector(DELETE_BUTTON_LOCATOR), getRoot());
    }

    public boolean isRefreshButtonVisible() {
        return isElementPresent(By.className(REFRESH_BUTTON_LOCATOR), getRoot());
    }
    
    public boolean isAnalyzeButtonVisible() {
        return isElementPresent(By.cssSelector(ANALYZE_BUTTON_LOCATOR), getRoot());
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
