package com.gooddata.qa.graphene.fragments.common;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class LoginFragment extends AbstractFragment {

    @FindBy
    private WebElement email;

    @FindBy
    private WebElement password;

    @FindBy(css = ".s-login-button")
    private WebElement signInButton;

    private static final String ERROR_CLASS = "has-error";

    private static final By BY_LOGGED_USER_BUTTON = By.cssSelector("a.account-menu");

    public void login(String username, String password, boolean validLogin) {
        waitForElementVisible(this.email).clear();
        waitForElementVisible(this.password).clear();
        this.email.sendKeys(username);
        this.password.sendKeys(password);
        signInButton.click();
        if (validLogin) {
            waitForElementNotVisible(this.getRoot());
            waitForElementVisible(BY_LOGGED_USER_BUTTON);
        }
    }

    public boolean allLoginElementsAvailable() {
        return email.isDisplayed() && password.isDisplayed() && signInButton.isDisplayed();
    }

    public void checkEmailInvalid() {
        Graphene.waitAjax().until().element(email).attribute("class").contains(ERROR_CLASS);
    }

    public void checkPasswordInvalid() {
        Graphene.waitAjax().until().element(password).attribute("class").contains(ERROR_CLASS);
    }

    public void checkInvalidLogin() {
        checkEmailInvalid();
        checkPasswordInvalid();
    }
}
