package com.gooddata.qa.graphene.fragments.reports;

import org.jboss.arquillian.graphene.enricher.findby.FindBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class ReportsPage extends AbstractFragment {
	
	private static final By BY_ADD_FOLDER_INPUT = By.xpath("..//input");
	private static final By BY_ADD_FOLDER_SUBMIT_BUTTON = By.xpath("../div//button[contains(@class,'s-newSpaceButton')]");
	
	@FindBy(id="folderDomains")
	private ReportFolders defaultFolders;
	
	@FindBy(id="sharedDomains")
	private ReportFolders customFolders;
	
	@FindBy(xpath="//span[@id='newDomain']/button")
	private WebElement addFolderButton;
	
	@FindBy(xpath="//div[@id='domain']/div/h1")
	private WebElement selectedFolderName;
	
	@FindBy(xpath="//div[@id='domain']/div/p[@class='description']")
	private WebElement selectedFolderDescription;
	
	public ReportFolders getDefaultFolders() {
		return defaultFolders;
	}
	
	public ReportFolders getCustomFolders() {
		return customFolders;
	}
	
	public void addNewFolder(String folderName) {
		int currentFoldersCount = customFolders.getNumberOfFolders();
		addFolderButton.click();
		waitForElementVisible(addFolderButton.findElement(BY_ADD_FOLDER_INPUT));
		addFolderButton.findElement(BY_ADD_FOLDER_INPUT).sendKeys(folderName);
		addFolderButton.findElement(BY_ADD_FOLDER_SUBMIT_BUTTON).click();
		Assert.assertEquals(customFolders.getNumberOfFolders(), currentFoldersCount + 1, "Number of folders increased");
		Assert.assertTrue(customFolders.getAllFolderNames().contains(folderName), "New folder name is present in list");
	}
	
	public String getSelectedFolderName() {
		return selectedFolderName.getText();
	}
	
	public String getSelectedFolderDescription() {
		return selectedFolderDescription.getText();
	}
}
