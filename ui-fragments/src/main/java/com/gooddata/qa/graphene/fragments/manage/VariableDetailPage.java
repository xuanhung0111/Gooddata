package com.gooddata.qa.graphene.fragments.manage;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class VariableDetailPage extends AbstractFragment {

	/**
	 * @param args
	 */
	@FindBy(xpath = "//a[text()='Manage']")
	private WebElement manageOption;

	@FindBy(xpath = "//strong[text()='Variables']")
	private WebElement variableOption;

	@FindBy(css = ".s-btn-create_variable")
	private WebElement createVariableButton;

	@FindBy(xpath = "//input[@class='ipeEditor']")
	private WebElement variableNameInput;

	@FindBy(xpath = "//button[text()='Ok']")
	private WebElement okNameButton;

	@FindBy(xpath = "//label[text()='Filtered Variable']")
	private WebElement filterVariable;

	@FindBy(xpath = "//div[2]/div/div/div/div/div/input")
	private WebElement searchAttributeText;

	@FindBy(xpath = "//span[text()='Select Attribute']")
	private WebElement selectAttribute;

	private static final String attributeToAddLocator = "//span[text()='${variableName}']";

	@FindBy(xpath = "//button[text()='Select']")
	private WebElement selectButton;

	@FindBy(xpath = "//button[text()='Save Changes']")
	private WebElement saveChangeButton;

	@FindBy(xpath = "//div[@class='subContent']/table")
	private WebElement userTable;

	private String listOfElementLocator = "//div[contains(@class,'yui3-c-simpleColumn-underlay')]/div[contains(@class,'c-label') and contains(@class,'s-item-${label}')]";

	public void createFilterVariable(String attribute, String variableName)
			throws InterruptedException {
		waitForElementVisible(createVariableButton).click();
		waitForElementVisible(variableNameInput);
		variableNameInput.sendKeys(variableName);
		waitForElementVisible(okNameButton).click();
		waitForElementVisible(filterVariable).click();
		waitForElementVisible(selectAttribute).click();
		waitForElementVisible(searchAttributeText);
		searchAttributeText.sendKeys(attribute);
		By listOfAttribute = By.xpath(listOfElementLocator.replace("${label}",
				attribute.trim().toLowerCase().replaceAll(" ", "_")));
		waitForElementVisible(listOfAttribute);
		By attributeToAdd = By.xpath(attributeToAddLocator.replace(
				"${variableName}", attribute));
		waitForElementVisible(attributeToAdd).click();
		waitForElementVisible(selectButton).click();
		waitForElementVisible(saveChangeButton).click();
		Thread.sleep(3000);
		waitForElementNotVisible(saveChangeButton);
		waitForElementVisible(userTable);
	}

}
