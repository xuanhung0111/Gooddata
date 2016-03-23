package com.gooddata.qa.graphene.fragments.csvuploader;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementEnabled;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.tagName;

import java.io.File;
import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class FileUploadDialog extends AbstractFragment {

    private static final By DISABLED_UPLOAD_BUTTON = By.cssSelector(".s-upload-file.disabled");

    @FindBy(className = "s-file-picker")
    private WebElement pickFileButton;

    @FindBy(className = "s-upload-file")
    private WebElement uploadButton;

    @FindBy(className = "s-cancel-file")
    private WebElement cancelButton;

    @FindBy(className = "s-backend-validation-errors")
    private WebElement backendValidationErrorsList;

    @FindBy(css = ".s-backend-validation-errors a")
    private WebElement backendValidationErrorLink;

    // this is workaround for bug MSF-9734
    @FindBy(css = ".gd-message.error")
    private WebElement validationError;

    public static FileUploadDialog getInstane(SearchContext context) {
        return Graphene.createPageFragment(FileUploadDialog.class,
                waitForElementVisible(className("s-upload-dialog"), context));
    }

    public FileUploadDialog pickCsvFile(String csvFilePath) {
        log.finest("Csv file path: " + csvFilePath);
        log.finest("Is file exists? " + new File(csvFilePath).exists());

        waitForElementVisible(pickFileButton).sendKeys(csvFilePath);
        return this;
    }

    public boolean isUploadButtonDisabled() {
        return !getRoot().findElements(DISABLED_UPLOAD_BUTTON).isEmpty();
    }

    public void clickUploadButton() {
        waitForElementEnabled(uploadButton).click();
    }

    public void clickCancelButton() {
        waitForElementVisible(cancelButton).click();
        waitForFragmentNotVisible(this);
    }

    public List<String> getBackendValidationErrors() {
        return getElementTexts(waitForElementVisible(backendValidationErrorsList).findElements(tagName("span")));
    }

    public String getValidationErrorMessage() {
        return waitForElementVisible(validationError).getText();
    }

    public String getLinkInBackendValidationError() {
        return waitForElementVisible(backendValidationErrorLink).getAttribute("href");
    }
}
