package com.gooddata.qa.graphene.fragments.manage;

import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

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

	@FindBy(xpath = "//div[contains(@class,'defaultValue')]/button[contains(@class,'iconBtnEdit')]")
	private WebElement editButton;
	
	@FindBy(xpath = "//div[@class='confirm']/div[@class='btns']/button[text()='Set']")
	private WebElement setButton;
	
	@FindBy(xpath = "//button[text()='Save Changes']")
	private WebElement saveChangeButton;

	@FindBy(xpath = "//div[contains(@class,'defaultValue')]/span[@class='answer']/span")
	private WebElement selectedValues;
	
	@FindBy(xpath = "//div[@class='subContent']/table")
	private WebElement userTable;

	private String listOfAttributeLocator = "//div[contains(@class,'yui3-c-simpleColumn-underlay')]/div[contains(@class,'c-label') and contains(@class,'s-item-${label}')]";

	private String listOfElementLocator = "//div[contains(@class,'yui3-c-simpleColumn-underlay')]/div[contains(@class,'c-checkBox') and contains(@class,'s-item-${label}')]";
	
	public void createFilterVariable(String attribute, String variableName, List<String> elements)
			throws InterruptedException {
		waitForElementVisible(createVariableButton).click();
		waitForElementVisible(variableNameInput);
		variableNameInput.sendKeys(variableName);
		waitForElementVisible(okNameButton).click();
		waitForElementVisible(filterVariable).click();
		waitForElementVisible(selectAttribute).click();
		waitForElementVisible(searchAttributeText);
		searchAttributeText.sendKeys(attribute);
		By listOfAttribute = By.xpath(listOfAttributeLocator.replace("${label}",
				attribute.trim().toLowerCase().replaceAll(" ", "_")));
		waitForElementVisible(listOfAttribute);
		By attributeToAdd = By.xpath(attributeToAddLocator.replace(
				"${variableName}", attribute));
		waitForElementVisible(attributeToAdd).click();
		waitForElementVisible(selectButton).click();
		waitForElementVisible(editButton).click();
		By listOfElement;		
		for(int i = 0; i< elements.size(); i++){
			listOfElement = By.xpath(listOfElementLocator.replace("${label}", elements.get(i).trim().toLowerCase().replaceAll(" ", "_")));
			waitForElementVisible(listOfElement).click();
		}
		waitForElementVisible(setButton).click();
		waitForElementNotVisible(setButton);
		waitForElementVisible(saveChangeButton).click();
		Thread.sleep(3000);
		waitForElementNotVisible(saveChangeButton);
		waitForElementVisible(userTable);
		List<String> actualElementList = getPromptElements(variableName);
		Assert.assertEquals(actualElementList, elements, "Variable isn't added correctly");
	}
	
	public List<String> getPromptElements(String promptName){
		String str = waitForElementVisible(selectedValues).getAttribute("title");
		System.out.println("selectedValue = " + str);
		List<String> elementList = Arrays.asList(str.split(", "));
		return elementList;
	}

}
