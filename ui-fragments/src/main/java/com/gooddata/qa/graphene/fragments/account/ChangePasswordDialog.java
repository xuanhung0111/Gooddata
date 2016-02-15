package com.gooddata.qa.graphene.fragments.account;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.common.AbstractDialog;

public class ChangePasswordDialog extends AbstractDialog {

    private static final By ERROR_MESSAGE_LOCATOR = By.cssSelector("#gd-overlays div.content");
    private static final String CHANGE_PASSWORD_DIALOG_CLASS_NAME = 
            "//*[contains(@class,'changePasswordDialog')]";

    @FindBy(xpath = CHANGE_PASSWORD_DIALOG_CLASS_NAME + "/section[1]//input")
    private WebElement oldPasswordInput;

    @FindBy(xpath = CHANGE_PASSWORD_DIALOG_CLASS_NAME + "/section[2]//input")
    private WebElement newPasswordInput;

    @FindBy(xpath = CHANGE_PASSWORD_DIALOG_CLASS_NAME + "/section[3]//input")
    private WebElement confirmNewPasswordInput;

    public ChangePasswordDialog enterOldPassword(String oldPassword) {
        return enterData(oldPasswordInput, oldPassword);
    }

    public ChangePasswordDialog enterNewPassword(String newPassword) {
        return enterData(newPasswordInput, newPassword);
    }

    public ChangePasswordDialog enterConfirmPassword(String confirmNewPassword) {
        return enterData(confirmNewPasswordInput, confirmNewPassword);
    }

    public void changePassword(String oldPassword, String newPassword) {
        enterOldPassword(oldPassword)
            .enterNewPassword(newPassword)
            .enterConfirmPassword(newPassword)
            .saveChange();
    }

    public String getErrorMessage() {
        return waitForElementVisible(ERROR_MESSAGE_LOCATOR, browser).getText();
    }

    public boolean areAllInputsFilled() {
        return waitForElementVisible(oldPasswordInput).getAttribute("value").length() > 0
                && waitForElementVisible(newPasswordInput).getAttribute("value").length() > 0
                && waitForElementVisible(confirmNewPasswordInput).getAttribute("value").length() > 0;
    }

    private ChangePasswordDialog enterData(WebElement input, String data) {
        waitForElementVisible(input).clear();
        input.sendKeys(data);
        return this;
    }

}
