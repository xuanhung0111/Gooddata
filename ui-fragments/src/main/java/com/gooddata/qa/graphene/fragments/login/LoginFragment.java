package com.gooddata.qa.graphene.fragments.login;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class LoginFragment extends AbstractFragment {

    @FindBy
    private WebElement email;

    @FindBy
    private WebElement password;

    @FindBy(css = ".s-login-button")
    private WebElement signInButton;
    
    @FindBy(css = ".s-notAuthorized")
    private WebElement notAuthorizedMessage;

    private static final String ERROR_CLASS = "has-error";

    public void login(String username, String password, boolean validLogin) {
        waitForElementVisible(email).clear();
        waitForElementVisible(this.password).clear();
        email.sendKeys(username);
        this.password.sendKeys(password);
        waitForElementVisible(signInButton).click();
        if (validLogin) {
            waitForElementNotVisible(this.getRoot());
            waitForElementNotVisible(email);
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
    
    public String getNotAuthorizedMessage() {
        return waitForElementVisible(notAuthorizedMessage).getText();
    }
}
