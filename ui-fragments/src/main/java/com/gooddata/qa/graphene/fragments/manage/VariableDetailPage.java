package com.gooddata.qa.graphene.fragments.manage;

import static com.gooddata.qa.graphene.utils.CheckUtils.*;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.utils.CssUtils;
import com.gooddata.qa.graphene.entity.variable.AttributeVariable;
import com.gooddata.qa.graphene.entity.variable.NumericVariable;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

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

    @FindBy(xpath = "//div[contains(@class, 'attributeElements')]//input[contains(@class, 'gdc-input')]")
    private WebElement searchAttributeElement;

    @FindBy(css = "#p-objectPage .s-btn-delete")
    private WebElement deleteButton;

    private static final By confirmDeleteButtonLocator = By
            .cssSelector(".yui3-d-modaldialog:not(.gdc-hidden) .c-modalDialog .s-btn-delete");
    
    private static final String setBtnLocator = "//table[contains(@class,'usersTable')]//td[contains(@class,'role') and text()='%s']/following-sibling::td//button[contains(@class,'s-btn-set')]";

    @FindBy(id = "p-objectPage")
    protected ObjectPropertiesPage objectPropertiesPage;

    private static final String listOfAttributeLocator =
            "div.yui3-c-simpleColumn-underlay > div.c-label.s-item-${label}:not(.gdc-hidden):not(.hidden)";

    private static final String listOfElementLocator =
            "div.yui3-c-simpleColumn-underlay > div.c-checkBox.s-item-${label}:not(.gdc-hidden)";

    public void createNumericVariable(NumericVariable var) {
        waitForElementVisible(objectPropertiesPage.objectNameInput).sendKeys(var.getName());
        waitForElementVisible(okNameButton).click();
        waitForElementVisible(numericalVariable).click();
        waitForElementVisible(scalarValueInput).sendKeys(String.valueOf(var.getDefaultNumber()));
        waitForElementVisible(numericalVariable).click();
        waitForElementVisible(saveChangeButton).click();
        waitForElementVisible(unSavedBarInactive);
        waitForElementVisible(dataLink).click();
    }

    public void createFilterVariable(AttributeVariable var) {
        waitForElementVisible(objectPropertiesPage.objectNameInput).sendKeys(var.getName());
        waitForElementVisible(okNameButton).click();
        waitForElementVisible(filterVariable).click();
        waitForElementVisible(selectAttribute).click();
        waitForElementVisible(searchAttributeText);

        String attribute = var.getAttribute();
        searchAttributeText.sendKeys(attribute);
        By listOfAttribute =
                By.cssSelector(listOfAttributeLocator.replace("${label}", CssUtils.simplifyText(attribute)));
        waitForElementVisible(listOfAttribute, browser).click();
        waitForElementVisible(selectButton).click();

        if (!var.getAttributeElements().isEmpty()) {
            if (var.isUserSpecificValues()) {
                waitForElementVisible(chooseButton).click();
            } else {
                waitForElementVisible(editButton).click();
            }
    
            selectAttrElement(var.getAttributeElements());
        }
        waitForElementVisible(saveChangeButton).click();
        waitForElementVisible(unSavedBarInactive);
        waitForElementVisible(userTable);
        waitForElementVisible(dataLink).click();
    }

    public void selectAttrElement(List<String> elements) {
        By listOfElement;
        for (String ele : elements) {
            waitForElementVisible(searchAttributeElement).clear();
            searchAttributeElement.sendKeys(ele);
            listOfElement = By.cssSelector(listOfElementLocator.replace("${label}", CssUtils.simplifyText(ele)));
            waitForElementVisible(listOfElement, browser).click();
        }
        waitForElementVisible(setButton).click();
        waitForElementNotVisible(setButton);
    }

    public void setUserValueNumericVariable(UserRoles userRole, int number) {
        waitForElementVisible(By.xpath(String.format(setBtnLocator, userRole.getName())), this.getRoot()).click();
        waitForElementVisible(userNumberSet).sendKeys(String.valueOf(number));
        waitForElementVisible(userOkButton).click();
        waitForElementNotVisible(userNumberSet);
        assertEquals(waitForElementVisible(userNumberValue).getText(), String.valueOf(number),
                "Set value for specific user doesn't work properly");
        waitForElementVisible(saveChangeButton).click();
        waitForElementVisible(unSavedBarInactive);
        waitForElementVisible(userTable);
        waitForElementVisible(dataLink).click();
    }

    public void verifyNumericalVariable(NumericVariable var) {
        waitForElementVisible(userTable);
        String defValue = waitForElementVisible(scalarValueInput).getAttribute("value");
        assertEquals(defValue, String.valueOf(var.getDefaultNumber()),
                "Default value of numeric variable is NOT set properly");

        if (var.getUserNumber() != Integer.MAX_VALUE) {
            String userNumber = waitForElementVisible(userNumberValue).getText();
            assertEquals(userNumber, String.valueOf(var.getUserNumber()),
                    "User specifc value of numeric variable is NOT set properly");
        }

        waitForElementVisible(dataLink).click();
    }

    public void verifyAttributeVariable(AttributeVariable var) {
        List<String> elements = var.getAttributeElements();
        if (elements.isEmpty()) {
            waitForElementVisible(dataLink).click();
            return;
        }

        waitForElementVisible(userTable);
        if (var.isUserSpecificValues()) {
            String userValue = waitForElementVisible(userListValue).getAttribute("title");
            List<String> actualUserList = Arrays.asList(userValue.split(", "));
            assertEquals(actualUserList, elements, "User value of attribute variable is NOT set properly");
        } else {
            String defaultValue = waitForElementVisible(selectedValues).getAttribute("title");
            List<String> actualDefaultList = Arrays.asList(defaultValue.split(", "));
            assertEquals(actualDefaultList, elements, "Default value of attribute variable is NOT set properly");
        }
        waitForElementVisible(dataLink).click();
    }

    public void deleteVariable() {
        waitForElementVisible(deleteButton).click();
        waitForElementVisible(confirmDeleteButtonLocator, browser).click();
        waitForDataPageLoaded(browser);
    }

    public void setDefaultValue(int value) {
        waitForElementVisible(scalarValueInput).click();
        scalarValueInput.clear();
        sleepTightInSeconds(1);
        scalarValueInput.sendKeys(String.valueOf(value));
        // do not focus on scalarValueInput
        waitForElementVisible(numericalVariable).click();
        assertEquals(scalarValueInput.getAttribute("value"), String.valueOf(value));
        waitForElementVisible(saveChangeButton).click();
        waitForElementVisible(unSavedBarInactive);
        waitForElementVisible(userTable);
        waitForElementVisible(dataLink).click();
    }
}
