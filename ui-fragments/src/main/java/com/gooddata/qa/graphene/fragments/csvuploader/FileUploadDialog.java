package com.gooddata.qa.graphene.fragments.csvuploader;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static java.util.stream.Collectors.toList;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class FileUploadDialog extends AbstractFragment {

    @FindBy(className = "s-file-picker")
    private WebElement pickFileButton;

    @FindBy(className = "s-upload-file")
    private WebElement uploadButton;

    @FindBy(className = "s-cancel-file")
    private WebElement cancelButton;

    @FindBy(className = "s-backend-validation-errors")
    private WebElement backendValidationErrorsList;

    public FileUploadDialog pickCsvFile(String csvFilePath) {
        waitForElementVisible(pickFileButton).sendKeys(csvFilePath);
        return this;
    }

    public void clickUploadButton() {
        waitForElementVisible(uploadButton).click();
    }

    public void clickCancelButton() {
        waitForElementVisible(cancelButton).click();
    }

    public List<String> getBackendValidationErrors() {
        waitForElementVisible(backendValidationErrorsList);

        return backendValidationErrorsList.findElements(By.tagName("div"))
                .stream()
                .map(WebElement::getText)
                .collect(toList());
    }
}
