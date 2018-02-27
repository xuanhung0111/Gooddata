package com.gooddata.qa.graphene.fragments.login;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static org.openqa.selenium.By.cssSelector;

import com.google.common.base.Predicate;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.account.LostPasswordPage;
import com.gooddata.qa.graphene.fragments.account.RegistrationPage;

public class LoginFragment extends AbstractFragment {

    @FindBy
    private WebElement email;

    @FindBy
    private WebElement password;

    @FindBy(css = ".s-login-button")
    private WebElement signInButton;
    
    @FindBy(css = ".s-notAuthorized")
    private WebElement notAuthorizedMessage;

    @FindBy(xpath = "//*[contains(@href, 'lostPassword')]")
    private WebElement forgotPasswordLink;

    @FindBy(css = ".s-registration-link")
    private WebElement registrationLink;

    private static final String ERROR_CLASS = "has-error";
    private static final By NOTIFICATION_MESSAGE_LOCATOR = By.cssSelector(".login-message.is-success");

    private static LoginFragment instance = null;

    public static final LoginFragment getInstance(SearchContext context) {
        if (instance == null) {
            instance = Graphene.createPageFragment(LoginFragment.class,
                    waitForElementVisible(cssSelector(".s-loginPage.s-ready"), context));
        }
        return waitForFragmentVisible(instance);
    }

    public static final LoginFragment waitForPageLoaded(SearchContext context) {
        return getInstance(context);
    }

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

    public LostPasswordPage openLostPasswordPage() {
        waitForElementVisible(forgotPasswordLink).click();
        return LostPasswordPage.getInstance(browser);
    }

    public void registerNewAccount() {
        // Use href to detect the environment instead of text which may be depend on user locale
        String href = waitForElementVisible(registrationLink).getAttribute("href");
        registrationLink.click();

        if (href.endsWith("request-a-demo")) {
            Predicate<WebDriver> requestADemoPageDisplayed = browser ->
                    browser.getCurrentUrl().equals("https://www.gooddata.com/request-a-demo");
            Graphene.waitGui().until(requestADemoPageDisplayed);
        } else {
            RegistrationPage.getInstance(browser);
        }
    }

    public LoginFragment waitForNotificationMessageDisplayed() {
        waitForElementVisible(NOTIFICATION_MESSAGE_LOCATOR, browser);
        return this;
    }

    public String getNotificationMessage() {
        return waitForElementVisible(NOTIFICATION_MESSAGE_LOCATOR, browser).getText();
    }
}
