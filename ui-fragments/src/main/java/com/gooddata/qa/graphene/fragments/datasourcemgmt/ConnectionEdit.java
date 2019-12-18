package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class ConnectionEdit extends AbstractFragment {
    private static final By CONNECTION_DETAIL_CLASS = By.className("can-create-new-datasource");

    @FindBy(className = "title")
    private WebElement name;

    @FindBy(className = "save-button")
    private WebElement saveButton;

    @FindBy(className = "cancelButton")
    private WebElement cancelButton;

    @FindBy(className = "validateButton")
    private WebElement validateButton;

    @FindBy(className = "required-message")
    private List<WebElement> requiredMessage;

    @FindBy(className = "schema")
    private WebElement schema ;

    @FindBy(className = "prefix")
    private WebElement prefix ;

    @FindBy(className = "gd-message-text")
    private WebElement validateMessage;


    public void addName( String value) {
        waitForElementVisible(name).sendKeys(value);
    }

    public void addSchema(String value) {
        waitForElementVisible(schema).sendKeys(value);
    }

    public void addPrefix(String value) {
        waitForElementVisible(prefix).sendKeys(value);
    }

    public void clickSavebutton() {
        waitForElementVisible(saveButton).click();
    }

    public void clickCancelButton () {
        waitForElementVisible(cancelButton).click();
    }

    public void clickValidateButton() {
        waitForElementVisible(validateButton).click();
    }

    public int getNumberOfRequiredMessage() {
        waitForCollectionIsNotEmpty(requiredMessage);
        return requiredMessage.size();
    }

    public String getValidateMessage() {
        waitForElementVisible(validateMessage);
        return validateMessage.getText();
    }
}
