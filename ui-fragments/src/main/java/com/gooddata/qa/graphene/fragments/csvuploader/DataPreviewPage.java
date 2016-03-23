package com.gooddata.qa.graphene.fragments.csvuploader;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.AbstractTable;

public class DataPreviewPage extends AbstractFragment {

    private static final By DISABLED_INTEGRATION_BUTTON = cssSelector(".s-integration-button.disabled");
    private static final String SET_HEADER_LINK_LOCATOR = "s-select-header-button";
    private static final String PREVIEW_PAGE_ERROR_LOCATOR = "s-preview-page-error";

    @FindBy(className = PREVIEW_PAGE_ERROR_LOCATOR)
    private WebElement previewPageError;

    @FindBy(className = "data-table")
    private DataPreviewTable dataPreviewTable;

    @FindBy(css = ".new-load.data-table")
    private AbstractTable rowSelectionTable;

    @FindBy(className = "s-integration-button")
    private WebElement triggerIntegrationButton;

    @FindBy(className = "s-integration-cancel-button")
    private WebElement triggerIntegrationCancelButton;

    @FindBy(className = SET_HEADER_LINK_LOCATOR)
    private WebElement selectHeaderButton;

    @FindBy(className = "s-row-count-message")
    private WebElement rowCountMessage;

    @FindBy(css = ".warning .gd-message-text")
    private WebElement warningMessage;

    public static DataPreviewPage getInstance(SearchContext context) {
        return Graphene.createPageFragment(DataPreviewPage.class,
                waitForElementVisible(className("s-data-preview"), context));
    }

    public String getWarningMessage() {
        return waitForElementVisible(warningMessage).getText();
    }

    public boolean isIntegrationButtonDisabled() {
        return isElementPresent(DISABLED_INTEGRATION_BUTTON, browser);
    }

    public void triggerIntegration() {
        waitForElementVisible(triggerIntegrationButton).click();
    }

    public void cancelTriggerIntegration() {
        waitForElementVisible(triggerIntegrationCancelButton).click();
    }

    public DataPreviewPage selectHeader() {
        waitForElementVisible(selectHeaderButton).click();
        return this;
    }

    public boolean isSetHeaderLinkHidden() {
        return !isElementPresent(className(SET_HEADER_LINK_LOCATOR), browser);
    }

    public String getPreviewPageErrorMessage() {
        return waitForElementVisible(previewPageError).getText();
    }

    public boolean hasPreviewPageErrorMessage() {
        return isElementPresent(className(PREVIEW_PAGE_ERROR_LOCATOR), browser);
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
