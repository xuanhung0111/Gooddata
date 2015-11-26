package com.gooddata.qa.graphene.fragments.account;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class LostPasswordPage extends AbstractFragment {

    public static final String LOST_PASSWORD_PAGE_CLASS_NAME = "lostPasswordPage";

    private static final By ERROR_MESSAGE_LOCATOR = By.cssSelector("#gd-overlays div.content");
    private static final By PAGE_MESSAGE_LOCATOR = By.cssSelector(".message");

    @FindBy(css = "input[type='email']")
    private WebElement emailInput;

    @FindBy(css = ".s-btn-reset")
    private WebElement resetButton;

    @FindBy(css = "a[href*='login']")
    private WebElement backToLoginLink;

    @FindBy(css = "input[type='password']")
    private WebElement newPasswordInput;

    @FindBy(css = ".s-btn-set_password")
    private WebElement setPasswordButton;

    public void resetPassword(String email, boolean validReset) {
        waitForElementVisible(emailInput).clear();
        emailInput.sendKeys(email);
        waitForElementVisible(resetButton).click();
        if (validReset) {
            waitForElementNotVisible(resetButton);
        }
    }

    public void setNewPassword(String password) {
        waitForElementVisible(newPasswordInput).clear();
        newPasswordInput.sendKeys(password);
        waitForElementVisible(setPasswordButton).click();
    }

    public String getPageLocalMessage() {
        return waitForElementVisible(PAGE_MESSAGE_LOCATOR, browser).getText();
    }

    public String getErrorMessage() {
        return waitForElementVisible(ERROR_MESSAGE_LOCATOR, browser).getText();
    }

    public void backToLoginPage() {
        waitForElementVisible(backToLoginLink).click();
    }

}
