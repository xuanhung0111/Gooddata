package com.gooddata.qa.graphene.fragments.csvuploader;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static org.openqa.selenium.By.className;

import java.util.List;
import java.util.function.Function;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class DatasetDetailPage extends AbstractFragment {

    private static final String ANALYZE_BUTTON_LOCATOR = ".icon-analyze.button-link";
    private static final String DELETE_BUTTON_LOCATOR = "button.s-delete";
    private static final String UPDATE_BUTTON_LOCATOR = "s-dataset-update-ds-from-file-button";

    public static final By LOCATOR = className("s-dataset-detail");

    @FindBy(className = "s-dataset-name")
    private WebElement datasetName;

    @FindBy(className = "s-dataset-detail-back-button")
    private WebElement backButton;

    @FindBy(className = "s-dataset-columns-table")
    private DatasetColumnsTable datasetColumns;

    @FindBy(xpath = "//*[@class='datasets-sidebar']//a")
    private WebElement latestCsvFileUpload;

    @FindBy(css = ".file-detail")
    private WebElement createdDateTime;

    @FindBy(css = ANALYZE_BUTTON_LOCATOR)
    private WebElement analyzeButton;

    @FindBy(className = UPDATE_BUTTON_LOCATOR)
    private WebElement updateDatasetButton;

    @FindBy(css = DELETE_BUTTON_LOCATOR)
    private WebElement deleteButton;

    public static DatasetDetailPage getInstance(SearchContext context) {
        return Graphene.createPageFragment(DatasetDetailPage.class, waitForElementVisible(LOCATOR, context));
    }

    public static void waitForProgressItemLoaded(SearchContext searchContext) {
        final WebElement message = waitForElementVisible(className("gd-message"), searchContext);

        if (!message.getAttribute("class").contains("progress")) {
            log.info("Update csv progress is finished too fast!");
            return;
        }

        final Function<WebDriver, Boolean> progressFinished = b -> !message.getAttribute("class").contains("progress");
        Graphene.waitGui().until(progressFinished);
    }

    public String getDatasetAnalyzeLink() {
        return waitForElementVisible(analyzeButton).getAttribute("href");
    }

    public DatasetDetailPage downloadTheLatestCsvFileUpload() {
        waitForElementVisible(latestCsvFileUpload).click();
        return this;
    }

    public String getCreatedDateTime() {
        return waitForElementVisible(createdDateTime).getText();
    }

    public DatasetsListPage clickBackButton() {
        waitForElementVisible(backButton).click();
        return DatasetsListPage.getInstance(browser);
    }

    public FileUploadDialog clickUpdateButton() {
        waitForElementVisible(updateDatasetButton).click();
        return FileUploadDialog.getInstane(browser);
    }

    public DataPreviewPage updateCsv(String filePath) {
        clickUpdateButton().pickCsvFile(filePath).clickUploadButton();

        return DataPreviewPage.getInstance(browser);
    }

    public DatasetDeleteDialog clickDeleteButton() {
        waitForElementVisible(deleteButton).click();
        return DatasetDeleteDialog.getInstance(browser);
    }

    public boolean isDeleteButtonVisible() {
        return isElementPresent(By.cssSelector(DELETE_BUTTON_LOCATOR), getRoot());
    }

    public boolean isRefreshButtonVisible() {
        return isElementPresent(By.className(UPDATE_BUTTON_LOCATOR), getRoot());
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
