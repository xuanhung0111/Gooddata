package com.gooddata.qa.graphene.fragments.manage;

import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import static org.testng.Assert.*;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class VariableDetailPage extends AbstractFragment {

    @FindBy(xpath = "//div[contains(@class,'s-name-ipe-editor')]//button[text()='Ok']")
    private WebElement okNameButton;

    @FindBy(xpath = "//label[text()='Filtered Variable']")
    private WebElement filterVariable;

    @FindBy(xpath = "//label[text()='Numerical Variable']")
    private WebElement numericalVariable;

    @FindBy(xpath = "//div[@class='pickerRoot']//input[contains(@class,'s-afp-input')]")
    private WebElement searchAttributeText;

    @FindBy(xpath = "//span[text()='Select Attribute']")
    private WebElement selectAttribute;

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

    @FindBy(xpath = "//div[contains(@class, 'c-textBox')]/input")
    private WebElement scalarValueInput;

    @FindBy(xpath = "//div[contains(@class,'unSavedBarInactive')]")
    private WebElement unSavedBarInactive;

    @FindBy(xpath = "//table[contains(@class,'usersTable')]//button[text()='Set']")
    private WebElement userSetButton;

    @FindBy(xpath = "//div[contains(@class, 's-btn-ipe-editor')]//input[@class='ipeEditor']")
    private WebElement userNumberSet;

    @FindBy(xpath = "//div[contains(@class, 's-btn-ipe-editor')]//button[text() = 'Ok']")
    private WebElement userOkButton;

    @FindBy(xpath = "//span[contains(@class, 'defaultVal')]")
    private WebElement userDefaultValue;

    @FindBy(xpath = "//span[contains(@class,'numberVal')]/span")
    private WebElement userNumberValue;

    @FindBy(css = ".s-btn-default")
    private WebElement userDefaultButton;

    @FindBy(xpath = "//div[contains(@class = 'clearAnswer') and text() = 'Reset']")
    private WebElement resetButton;

    @FindBy(xpath = "//span[@class = 'answer' and text() = '(all values)']")
    private WebElement allValues;

    @FindBy(xpath = "//button[text() = 'Choose']")
    private WebElement chooseButton;

    @FindBy(xpath = "//span[contains(@class,'listVal')]/span")
    private WebElement userListValue;

    @FindBy(xpath = "//a[@class='interpolateProject']")
    private WebElement dataLink;

    @FindBy(id = "p-objectPage")
    protected ObjectPropertiesPage objectPropertiesPage;

    private static final String listOfAttributeLocator = "//div[contains(@class,'yui3-c-simpleColumn-underlay')]/div[contains(@class,'c-label') and contains(@class,'s-item-${label}')]";

    private static final String listOfElementLocator = "//div[contains(@class,'yui3-c-simpleColumn-underlay')]/div[contains(@class,'c-checkBox') and contains(@class,'s-item-${label}')]";

    private static final String attributeToAddLocator = "//span[text()='${variableName}']";

    public void createNumericVariable(String variableName, int number)
	    throws InterruptedException {
	waitForElementVisible(objectPropertiesPage.objectNameInput).sendKeys(variableName);
	waitForElementVisible(okNameButton).click();
	waitForElementVisible(numericalVariable).click();
	waitForElementVisible(scalarValueInput)
		.sendKeys(String.valueOf(number));
	waitForElementVisible(numericalVariable).click();
	waitForElementVisible(saveChangeButton).click();
	waitForElementVisible(unSavedBarInactive);
	waitForElementVisible(dataLink).click();
    }

    public void createFilterVariable(String attribute, String variableName,
	    List<String> elements, boolean userValueSet)
	    throws InterruptedException {
	waitForElementVisible(objectPropertiesPage.objectNameInput).sendKeys(variableName);
	waitForElementVisible(okNameButton).click();
	waitForElementVisible(filterVariable).click();
	waitForElementVisible(selectAttribute).click();
	waitForElementVisible(searchAttributeText);
	searchAttributeText.sendKeys(attribute);
	By listOfAttribute = By.xpath(listOfAttributeLocator
		.replace("${label}",
			attribute.trim().toLowerCase().replaceAll(" ", "_")));
	waitForElementVisible(listOfAttribute, browser);
	By attributeToAdd = By.xpath(attributeToAddLocator.replace(
		"${variableName}", attribute));
	waitForElementVisible(attributeToAdd, browser).click();
	waitForElementVisible(selectButton).click();
	if (userValueSet) {
	    waitForElementVisible(chooseButton).click();
	} else {
	    waitForElementVisible(editButton).click();
	}

	selectAttrElement(elements);
	waitForElementVisible(saveChangeButton).click();
	waitForElementVisible(unSavedBarInactive);
	waitForElementVisible(userTable);
	waitForElementVisible(dataLink).click();
    }

    public void selectAttrElement(List<String> elements) {
	By listOfElement;
	for (int i = 0; i < elements.size(); i++) {
	    listOfElement = By.xpath(listOfElementLocator.replace("${label}",
		    elements.get(i).trim().toLowerCase().replaceAll(" ", "_")));
	    waitForElementVisible(listOfElement, browser).click();
	}
	waitForElementVisible(setButton).click();
	waitForElementNotVisible(setButton);
    }

    public void setUserValueNumericVariable(int number)
	    throws InterruptedException {
	waitForElementVisible(userSetButton).click();// note
	waitForElementVisible(userNumberSet).sendKeys(String.valueOf(number));
	waitForElementVisible(userOkButton).click();
	waitForElementNotVisible(userNumberSet);
	assertEquals(waitForElementVisible(userNumberValue).getText(),
		String.valueOf(number),
		"Set value for specific user doesn't work properly");
	waitForElementVisible(saveChangeButton).click();
	waitForElementVisible(unSavedBarInactive);
	waitForElementVisible(userTable);
	waitForElementVisible(dataLink).click();
    }

    public void verifyNumericalVariable(String expectedDefaultValue,
	    String expectedUserNumber) {
	waitForElementVisible(userTable);
	String defValue = waitForElementVisible(scalarValueInput).getAttribute(
		"value");
	String userNumber = waitForElementVisible(userNumberValue).getText();
	assertEquals(defValue, expectedDefaultValue,
		"Default value of numeric variable is NOT set properly");
	assertEquals(userNumber, expectedUserNumber,
		"User specifc value of numeric variable is NOT set properly");
    }

    public void verifyAttributeVariable(List<String> elements,
	    boolean userValueSet) {
	waitForElementVisible(userTable);
	if (userValueSet) {
	    String userValue = waitForElementVisible(userListValue)
		    .getAttribute("title");
	    List<String> actualUserList = Arrays.asList(userValue.split(", "));
	    assertEquals(actualUserList, elements,
		    "User value of attribute variable is NOT set properly");
	} else {
	    String defaultValue = waitForElementVisible(selectedValues)
		    .getAttribute("title");
	    List<String> actualDefaultList = Arrays.asList(defaultValue
		    .split(", "));
	    assertEquals(actualDefaultList, elements,
		    "Default value of attribute variable is NOT set properly");
	}
    }

}
