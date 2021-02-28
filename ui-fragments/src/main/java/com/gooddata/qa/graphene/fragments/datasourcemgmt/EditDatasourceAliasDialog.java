package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static org.openqa.selenium.By.className;

public class EditDatasourceAliasDialog extends AbstractFragment {

    @FindBy(className = "gd-input-field")
    private WebElement inputField;

    @FindBy(className = "s-cancel")
    private WebElement cancelBtn;

    @FindBy(className = "s-dialog-close-button")
    private WebElement closeBtn;

    @FindBy(className = "s-save")
    private WebElement saveBtn;

    @FindBy(className = "generic-data-source-error-message")
    private WebElement errorMessage;

    @FindBy(className = "generic-data-source-alias-warning")
    private WebElement warningMessage;


    public static EditDatasourceAliasDialog getInstance(SearchContext context) {
        return Graphene.createPageFragment(EditDatasourceAliasDialog.class,
                waitForElementVisible(className("data-source-alias-editing-dialog"), context));
    }

    public void inputAliasName(String aliasName) {
        waitForElementVisible(inputField);
        inputField.clear();
        inputField.sendKeys(aliasName);
        clickSaveButton();
    }

    public void clickCancelButton() {
        waitForElementVisible(cancelBtn).click();
        waitForFragmentNotVisible(this);
    }

    public void clickCloseButton() {
        waitForElementVisible(closeBtn).click();
        waitForFragmentNotVisible(this);
    }

    public void clickSaveButton() {
        waitForElementVisible(saveBtn).click();
    }

    public boolean isErrorMessageDisplayed() {
        return isElementVisible(errorMessage);
    }

    public String getErrorMessageContent() {
        return waitForElementVisible(errorMessage).getText();
    }

    public String getWarningMessageContent() {
        return waitForElementVisible(warningMessage).getText();
    }
}
