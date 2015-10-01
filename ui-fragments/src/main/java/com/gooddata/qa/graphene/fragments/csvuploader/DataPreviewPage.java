package com.gooddata.qa.graphene.fragments.csvuploader;

import static com.gooddata.qa.graphene.utils.CheckUtils.*;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class DataPreviewPage extends AbstractFragment {

    @FindBy(className = "s-preview-page-error")
    private WebElement previewPageError;

    @FindBy(css = ".new-load.data-table")
    private DataPreviewTable dataPreviewTable;

    @FindBy(className = "s-integration-button")
    private WebElement triggerIntegrationButton;

    @FindBy(className = "s-select-header-button")
    private WebElement selectHeaderButton;

    public DataPreviewPage triggerIntegration() {
        waitForElementVisible(triggerIntegrationButton).click();
        return this;
    }

    public DataPreviewPage selectHeader() {
        waitForElementVisible(selectHeaderButton).click();
        return this;
    }

    public String getPreviewPageErrorMassage() {
        return waitForElementVisible(previewPageError).getText();
    }

    public DataPreviewTable getDataPreviewTable() {
        return waitForFragmentVisible(dataPreviewTable);
    }
}
