package com.gooddata.qa.graphene.fragments.csvuploader;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class DataUploadPage extends AbstractFragment {

    private static final By BY_EMPTY_STATE = By.cssSelector(".datasets-empty-state");
    private static final By BY_MY_DATA_EMPTY_STATE = By.cssSelector(".my-datasets-empty-state");
    private static final By BY_OTHERS_DATA_EMPTY_STATE = By.cssSelector(".others-datasets-empty-state");

    @FindBy(css = ".datasets-header")
    private WebElement datasetsHeader;

    @FindBy(xpath = "//button[text()='Add data']")
    private WebElement addDataButton;

    @FindBy(css = ".my-datasets")
    private DatasetTable myDataTable;

    @FindBy(css = ".others-datasets")
    private DatasetTable othersDataTable;

    public WebElement waitForHeaderVisible() {
        return waitForElementVisible(datasetsHeader);
    }

    public WebElement waitForAddDataButtonVisible() {
        return waitForElementVisible(addDataButton);
    }

    public WebElement waitForEmptyStateLoaded() {
        return waitForElementVisible(BY_EMPTY_STATE, browser);
    }

    public WebElement waitForMyDataEmptyStateLoaded() {
        return waitForElementVisible(BY_MY_DATA_EMPTY_STATE, browser);
    }

    public WebElement waitForOthersDataEmptyStateLoaded() {
        return waitForElementVisible(BY_OTHERS_DATA_EMPTY_STATE, browser);
    }

    public String getEmptyStateMessage() {
        return waitForEmptyStateLoaded().getText();
    }

    public DatasetTable getMyDataTable() {
        return waitForFragmentVisible(myDataTable);
    }

    public DatasetTable getOthersDataTable() {
        return waitForFragmentVisible(othersDataTable);
    }
}
