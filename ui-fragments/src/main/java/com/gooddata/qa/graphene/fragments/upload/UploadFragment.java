package com.gooddata.qa.graphene.fragments.upload;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class UploadFragment extends AbstractFragment {

	private static final By BY_ERROR_TITLE = By.cssSelector(".s-uploadIndex-errorTitle");
	private static final By BY_ERROR_MESSAGE = By.cssSelector(".s-uploadIndex-errorMessage");
	private static final By BY_ERROR_SUPPORT = By.cssSelector(".s-uploadIndex-errorSupport");
	
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
    
    public WebElement getErrorTitle(WebElement errorMessageElement) {
    	return errorMessageElement.findElement(BY_ERROR_TITLE);
    }
    
    public WebElement getErrorMessage(WebElement errorMessageElement) {
    	return errorMessageElement.findElement(BY_ERROR_MESSAGE);
    }
    
    public WebElement getErrorSupport(WebElement errorMessageElement) {
    	return errorMessageElement.findElement(BY_ERROR_SUPPORT);
    }
}
