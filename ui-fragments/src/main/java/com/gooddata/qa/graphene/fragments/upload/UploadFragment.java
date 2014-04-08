package com.gooddata.qa.graphene.fragments.upload;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class UploadFragment extends AbstractFragment {

    @FindBy
    private WebElement uploadFile;

    @FindBy(css = "button.s-btn-load")
    private WebElement loadButton;

    @FindBy(css = "div.s-uploadPage-annotation table")
    private UploadColumns uploadColumns;

    public void uploadFile(String filePath) throws InterruptedException {
        System.out.println("Going to upload file: " + filePath);
        waitForElementPresent(uploadFile).sendKeys(filePath);
        waitForElementNotVisible(uploadFile);
        waitForElementVisible(uploadColumns.getRoot());
    }

    public UploadColumns getUploadColumns() {
        return uploadColumns;
    }

    public void confirmloadCsv() {
        waitForElementVisible(loadButton).click();
        waitForElementNotVisible(uploadColumns.getRoot());
    }
}
