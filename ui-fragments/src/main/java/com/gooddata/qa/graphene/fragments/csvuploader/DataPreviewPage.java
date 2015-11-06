package com.gooddata.qa.graphene.fragments.csvuploader;

import static com.gooddata.qa.graphene.utils.CheckUtils.*;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.AbstractTable;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class DataPreviewPage extends AbstractFragment {

    private static final By DISABLED_INTEGRATION_BUTTON = By.cssSelector(".s-integration-button.disabled");
    private static final String SET_HEADER_BUTTON_LOCATION = "s-select-header-button";

    @FindBy(className = "s-preview-page-error")
    private WebElement previewPageError;

    @FindBy(className = "data-table")
    private DataPreviewTable dataPreviewTable;

    @FindBy(css = ".new-load.data-table")
    private AbstractTable rowSelectionTable;

    @FindBy(className = "s-integration-button")
    private WebElement triggerIntegrationButton;

    @FindBy(className = "s-integration-cancel-button")
    private WebElement triggerIntegrationCancelButton;

    @FindBy(className = SET_HEADER_BUTTON_LOCATION)
    private WebElement selectHeaderButton;

    @FindBy(className = "s-row-count-message")
    private WebElement rowCountMessage;

    @FindBy(css = ".warning .gd-message-text")
    private WebElement warningMessage;

    public String getWarningMessage() {
        return waitForElementVisible(warningMessage).getText();
    }

    public boolean isIntegrationButtonDisabled() {
        return isElementPresent(DISABLED_INTEGRATION_BUTTON, browser);
    }
    
    public DataPreviewPage triggerIntegration() {
        waitForElementVisible(triggerIntegrationButton).click();
        return this;
    }

    public DataPreviewPage cancelTriggerIntegration() {
        waitForElementVisible(triggerIntegrationCancelButton).click();
        return this;
    }

    public DataPreviewPage selectHeader() {
        waitForElementVisible(selectHeaderButton).click();
        return this;
    }
    
    public boolean isSetHeaderButtonHidden() {
        return !isElementPresent(By.className(SET_HEADER_BUTTON_LOCATION), browser);
    }

    public String getPreviewPageErrorMessage() {
        return waitForElementVisible(previewPageError).getText();
    }

    public DataPreviewTable getDataPreviewTable() {
        return waitForFragmentVisible(dataPreviewTable);
    }

    public AbstractTable getRowSelectionTable() {
        return waitForFragmentVisible(rowSelectionTable);
    }

    public String getRowCountMessage() {
        return waitForElementVisible(rowCountMessage).getText();
    }
}
