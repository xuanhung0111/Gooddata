package com.gooddata.qa.graphene.fragments.upload;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class UploadFragment extends AbstractFragment {
	
	@FindBy
	private WebElement uploadFile;
	
	@FindBy(css="button.s-btn-load")
	private WebElement loadButton;
	
	private UploadColumns uploadColumns;
	
	private static final By BY_UPLOAD_COLUMNS = By.cssSelector(".table-lineList");
	
	public void uploadFile(String filePath) throws InterruptedException {
		waitForElementPresent(uploadFile).sendKeys(filePath);
		waitForElementNotVisible(uploadFile);
		waitForElementVisible(BY_UPLOAD_COLUMNS);
	}
	
	public UploadColumns getUploadColumns() {
		if (uploadColumns == null) {
			uploadColumns = Graphene.createPageFragment(UploadColumns.class, this.root.findElement(BY_UPLOAD_COLUMNS));
		}
		return uploadColumns;
	}
	
	public void confirmloadCsv() {
		waitForElementVisible(loadButton).click();
		waitForElementNotVisible(BY_UPLOAD_COLUMNS);
	}
}
