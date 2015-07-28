package com.gooddata.qa.graphene.fragments.csvuploader;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class FileUploadDialog extends AbstractFragment {

    @FindBy(className = "s-file-picker")
    private WebElement pickFileButton;

    @FindBy(className = "s-upload-file")
    private WebElement uploadButton;

    public void pickCsvFile(String csvFilePath) {
        waitForElementVisible(pickFileButton);
        pickFileButton.sendKeys(csvFilePath);
    }

    public void clickUploadButton() {
        waitForElementVisible(uploadButton);
        uploadButton.click();
    }
}
