package com.gooddata.qa.graphene.fragments.reports;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

import java.util.ArrayList;
import java.util.List;

public class ReportsFolders extends AbstractFragment {
	
	@FindBy(tagName="li")
	private List<WebElement> folders;
	
	/**
	 * Method to get number of report folders
	 * 
	 * @return number of report folders
	 */
	public int getNumberOfFolders() {
		return folders.size();
	}

	/**
	 * Method for switching between folders
	 * 
	 * @param i - folder index
	 */
	public void openFolder(int i) {
		getFolderWebElement(i).findElement(BY_LINK).click();
	}
	
	/**
	 * Method for switching between folders
	 * 
	 * @param folderName - folder name
	 */
	public void openFolder(String folderName) {
		for (int i = 0; i < folders.size(); i++) {
			if (getFolderLabel(i).equals(folderName)) {
				openFolder(i);
				return;
			}
		}
		Assert.fail("Folder with given name does not exist!");
	}
	
	/**
	 * Method to verify that folder with given index is selected
	 * 
	 * @param i - folder index
	 * @return true is folder with given index is selected
	 */
	public boolean isFolderSelected(int i) {
		return getFolderWebElement(i).getAttribute("class").contains("active");
	}
	
	/**
	 * Method to get index of selected folder
	 * 
	 * @return index of selected folder
	 */
	public int getSelectedTabIndex() {
		for (int i = 0; i < folders.size(); i++) {
			if (isFolderSelected(i)) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Method to get label of folder with given index
	 * 
	 * @param i - folder index
	 * @return label of folder with given index 
	 */
	public String getFolderLabel(int i) {
		WebElement elem = getFolderWebElement(i).findElement(BY_LINK);
		return elem.getText();
	}
	
	/**
	 * Method to get link of folder with given index
	 * 
	 * @param i - folder index
	 * @return link of folder with given index
	 */
	public String getFolderLink(int i) {
		WebElement elem = getFolderWebElement(i).findElement(BY_LINK);
		return elem.getAttribute("href");
	}
	
	/**
	 * Method to get all folder labels
	 * 
	 * @return List<String> with all folder names
	 */
	public List<String> getAllFolderNames() {
		List<String> folderNames = new ArrayList<String>();
		for (int i = 0; i < folders.size(); i++) {
			folderNames.add(getFolderLabel(i));
		}
		return folderNames;
	}
	
	private WebElement getFolderWebElement(int i) {
		return folders.get(i);
	}
}
