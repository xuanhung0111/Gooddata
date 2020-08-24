package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.io.File;

import static com.gooddata.qa.graphene.utils.WaitUtils.*;
import static org.openqa.selenium.By.className;

public class FileUploadDialog extends AbstractFragment {
    @FindBy(className = "s-file-picker")
    private WebElement pickFileButton;

    @FindBy(className = "s-import-file")
    private WebElement importButton;

    @FindBy(className = "s-cancel-file")
    private WebElement cancelButton;

    public static FileUploadDialog getInstance(SearchContext context) {
        return Graphene.createPageFragment(FileUploadDialog.class,
                waitForElementVisible(className("import-csv"), context));
    }

    public FileUploadDialog pickCsvFile(String csvFilePath) {
        log.finest("Csv file path: " + csvFilePath);
        log.finest("Is file exists? " + new File(csvFilePath).exists());

        waitForElementVisible(pickFileButton).sendKeys(csvFilePath);
        return this;
    }

    public void clickImportButton() {
        waitForElementEnabled(importButton).click();
    }

    public ErrorContent importInvalidCSV() {
        clickImportButton();
        return OverlayWrapper.getInstance(browser).getWaitingDialog().waitForLoading().getErrorContent();
    }

    public PreviewCSVDialog importCSVShowPreview() {
        clickImportButton();
        return OverlayWrapper.getInstance(browser).getPreviewCSVDialog();
    }

    public void cancelDialog() {
        waitForElementVisible(cancelButton).click();
        waitForFragmentNotVisible(this);
    }
}
