package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.io.File;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementEnabled;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static org.openqa.selenium.By.className;

public class FileUploadDialog extends AbstractFragment {
    @FindBy(className = "s-file-picker-input")
    private WebElement pickFileButton;

    @FindBy(className = "s-import-file")
    private WebElement importButton;

    @FindBy(className = "s-cancel-file")
    private WebElement cancelButton;

    @FindBy(className = "import-error-message")
    private WebElement errorMessage;

    public static FileUploadDialog getInstance(SearchContext context) {
        return Graphene.createPageFragment(FileUploadDialog.class,
                waitForElementPresent(className("import-csv"), context));
    }

    public FileUploadDialog pickCsvFile(String csvFilePath) {
        log.finest("Csv file path: " + csvFilePath);
        log.finest("Is file exists? " + new File(csvFilePath).exists());

        waitForElementPresent(pickFileButton).sendKeys(csvFilePath);
        return this;
    }

    public void clickImportButton() {
        waitForElementEnabled(importButton).click();
    }

    public ErrorContent importInvalidCSV() {
        return OverlayWrapper.getInstance(browser).getWaitingDialog().waitForLoading().getErrorContent();
    }

    public void importValidData() {
        OverlayWrapper wrapper = OverlayWrapper.getInstance(browser);
        wrapper.waittingDialog();
        wrapper.closeWaitingDialog();
    }

    public PreviewCSVDialog importCSVShowPreview() {
        return OverlayWrapper.getInstance(browser).getPreviewCSVDialog();
    }

    public void cancelDialog() {
        waitForElementVisible(cancelButton).click();
        waitForFragmentNotVisible(this);
    }

    public String getErrorMessage() {
        return waitForElementVisible(errorMessage).getText();
    }
}
