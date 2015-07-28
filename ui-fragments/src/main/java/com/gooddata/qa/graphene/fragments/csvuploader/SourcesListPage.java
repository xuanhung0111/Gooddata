package com.gooddata.qa.graphene.fragments.csvuploader;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class SourcesListPage extends AbstractFragment {

    private static final By BY_EMPTY_STATE = By.className("sources-empty-state");
    private static final By BY_MY_SOURCES_EMPTY_STATE = By.className("my-sources-empty-state");
    private static final By BY_OTHERS_SOURCES_EMPTY_STATE = By.className("others-sources-empty-state");

    @FindBy(className = "s-sources-list-header")
    private WebElement sourcesHeader;

    @FindBy(className = "s-add-data-button")
    private WebElement addDataButton;

    @FindBy(className = "s-my-sources-list")
    private SourcesTable mySourcesTable;

    @FindBy(className = "others-sources")
    private SourcesTable othersSourcesTable;

    public void clickAddDataButton() {
        waitForAddDataButtonVisible();
        addDataButton.click();
    }

    public WebElement waitForHeaderVisible() {
        return waitForElementVisible(sourcesHeader);
    }

    public WebElement waitForAddDataButtonVisible() {
        return waitForElementVisible(addDataButton);
    }

    public WebElement waitForEmptyStateLoaded() {
        return waitForElementVisible(BY_EMPTY_STATE, browser);
    }

    public WebElement waitForMySourcesEmptyStateLoaded() {
        return waitForElementVisible(BY_MY_SOURCES_EMPTY_STATE, browser);
    }

    public WebElement waitForOthersSourcesEmptyStateLoaded() {
        return waitForElementVisible(BY_OTHERS_SOURCES_EMPTY_STATE, browser);
    }

    public String getEmptyStateMessage() {
        return waitForEmptyStateLoaded().getText();
    }

    public SourcesTable getMySourcesTable() {
        return waitForFragmentVisible(mySourcesTable);
    }

    public int getMySourcesCount() {
        return getMySourcesTable().getNumberOfSources();
    }

    public SourcesTable getOthersSourcesTable() {
        return waitForFragmentVisible(othersSourcesTable);
    }
}
