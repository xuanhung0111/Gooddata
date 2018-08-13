package com.gooddata.qa.graphene.fragments.csvuploader;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class Dataset extends AbstractFragment {

    public static final By BY_UPDATE_BUTTON = By.className("s-dataset-update-button");
    public static final By BY_DELETE_BUTTON = By.className("s-dataset-delete-button");

    private static final By BY_DETAIL_BUTTON = By.className("s-dataset-detail-button");
    private static final By BY_ANALYZE_LINK = By.cssSelector(".icon-analyze.button-link");

    @FindBy(className = "s-dataset-status")
    private WebElement status;

    @FindBy(className = "datasets-col-created")
    private WebElement createdDate;

    @FindBy(css = "td.datasets-col-created + td")
    private WebElement updatedDate;

    public String getName() {
        return waitForElementVisible(getRoot().findElement(BY_DETAIL_BUTTON)).getText();
    }

    public String getStatus() {
        return waitForElementVisible(status).getText();
    }

    public String getCreatedDate() {
        return waitForElementVisible(createdDate).getText();
    }

    public String getUpdatedDate() {
        return waitForElementVisible(updatedDate).getText();
    }

    public String getAnalyzeLink() {
        return waitForElementVisible(getRoot()).findElement(BY_ANALYZE_LINK).getAttribute("href");
    }

    public DatasetDetailPage openDetailPage() {
        waitForElementVisible(getRoot().findElement(BY_DETAIL_BUTTON)).click();
        return DatasetDetailPage.getInstance(browser);
    }

    public FileUploadDialog clickUpdateButton() {
        waitForElementVisible(getRoot().findElement(BY_UPDATE_BUTTON)).click();
        return FileUploadDialog.getInstane(browser);
    }

    public DatasetDeleteDialog clickDeleteButton() {
        waitForElementVisible(getRoot().findElement(BY_DELETE_BUTTON)).click();
        return DatasetDeleteDialog.getInstance(browser);
    }

    public boolean isDetailButtonVisible() {
        return isElementVisible(BY_DETAIL_BUTTON, getRoot()); 
    }

    public boolean isUpdateButtonVisible() {
        return isElementVisible(BY_UPDATE_BUTTON, getRoot());
    }

    public boolean isDeleteButtonVisible() {
        return isElementVisible(BY_DELETE_BUTTON, getRoot());
    }

    public boolean isAnalyzeLinkDisabled() {
        return waitForElementVisible(getRoot().findElement(BY_ANALYZE_LINK))
                .getAttribute("class")
                .contains("disabled");
    }

    public static WebElement waitForDatasetLoading(SearchContext searchContext) {
        return waitForElementVisible(className("item-in-progress"), searchContext);
    }

    public static void waitForDatasetLoaded(SearchContext searchContext) {
        final int uploadTimeout = 10 * 60; // 10 minutes
        final By itemInProgress = className("item-in-progress");

        if (isElementPresent(itemInProgress, searchContext)) {
            log.info("Dataset progress is visible. Wait for it disappear.");
            waitForElementNotPresent(itemInProgress, uploadTimeout);
            return;
        }

        final By message = className("gd-message");

        // loading progress is too fast, cannot catch it. So return if final message (successful or error) is shown
        if (isElementVisible(message, searchContext)) {
            waitForElementNotPresent(cssSelector(".gd-message.progress"), uploadTimeout);
            log.info("Dataset progress is disappeared. It's too fast, cannot catch it. But final message is shown.");
            return;
        }

        try {
            // wait for loading progress if it's not shown and there's no final message appeared
            waitForElementNotPresent(waitForDatasetLoading(searchContext), uploadTimeout);
        } catch (NoSuchElementException | TimeoutException e) {
            // for some reasons we cannot find 'progress item', we will assume that the loading dataset process
            // is finished
        }
    }
}
