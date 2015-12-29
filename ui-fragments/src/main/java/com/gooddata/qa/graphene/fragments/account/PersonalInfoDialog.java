package com.gooddata.qa.graphene.fragments.account;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.account.PersonalInfo;
import com.gooddata.qa.graphene.fragments.common.AbstractDialog;

public class PersonalInfoDialog extends AbstractDialog {

    private static final By FIRSTNAME_ERROR_MESSAGE_LOCATOR = By.cssSelector(".firstnameMessages");
    private static final By LASTNAME_ERROR_MESSAGE_LOCATOR = By.cssSelector(".surnameMessages");
    private static final By COMPANY_ERROR_MESSAGE_LOCATOR = By.cssSelector(".accountCompanyMessages");
    private static final By PHONE_ERROR_MESSAGE_LOCATOR = By.cssSelector(".phoneMessages");

    @FindBy(className = "emailPlaceholder")
    private WebElement emailField;

    @FindBy
    private WebElement firstname;

    @FindBy
    private WebElement lastname;

    @FindBy
    private WebElement company;

    @FindBy
    private WebElement phoneNumber;

    public String getFirstName() {
        return waitForElementVisible(firstname).getAttribute("value");
    }

    public String getLastName() {
        return waitForElementVisible(lastname).getAttribute("value");
    }

    public String getFullName() {
        return getFirstName() + " " + getLastName();
    }

    public String getEmail() {
        return waitForElementVisible(emailField).getText();
    }

    public String getCompany() {
        return waitForElementVisible(company).getAttribute("value");
    }

    public String getPhoneNumber() {
        return waitForElementVisible(phoneNumber).getAttribute("value");
    }

    public PersonalInfoDialog fillInfoFrom(PersonalInfo formDefination) {
        editData(firstname, formDefination.getFirstName());
        editData(lastname, formDefination.getLastName());
        editData(company, formDefination.getCompany());
        editData(phoneNumber, formDefination.getPhoneNumber());
        return this;
    }

    public boolean isEmailInputFieldEditable() {
        return isElementPresent(By.tagName("input"), emailField);
    }

    public String getFirstNameErrorMessage() {
        return waitForElementVisible(FIRSTNAME_ERROR_MESSAGE_LOCATOR, browser).getText();
    }

    public String getLastNameErrorMessage() {
        return waitForElementVisible(LASTNAME_ERROR_MESSAGE_LOCATOR, browser).getText();
    }

    public String getCompanyErrorMessage() {
        return waitForElementVisible(COMPANY_ERROR_MESSAGE_LOCATOR, browser).getText();
    }

    public String getPhoneNumberErrorMessage() {
        return waitForElementVisible(PHONE_ERROR_MESSAGE_LOCATOR, browser).getText();
    }

    public PersonalInfo getUserInfo() {
        return new PersonalInfo()
                .withFirstName(getFirstName())
                .withLastName(getLastName())
                .withEmail(getEmail())
                .withCompany(getCompany())
                .withPhoneNumber(getPhoneNumber());
    }

    private PersonalInfoDialog editData(WebElement inputElement, String data) {
        waitForElementVisible(inputElement).clear();
        inputElement.sendKeys(data);
        return this;
    }
}
