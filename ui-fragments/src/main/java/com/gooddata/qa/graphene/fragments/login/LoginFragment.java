package com.gooddata.qa.graphene.fragments.login;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;

import java.util.function.Function;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.account.LostPasswordPage;
import com.gooddata.qa.graphene.fragments.account.RegistrationPage;
import org.openqa.selenium.support.ui.ExpectedConditions;

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

    @FindBy(className = "s-btn-use_organization_login")
    private WebElement useOrganisationLoginButton;

    @FindBy(css = "button[class*='s-btn-login']")
    private WebElement loginServiceChanelAccount;

    private static final String ERROR_CLASS = "has-error";
    private static final By NOTIFICATION_MESSAGE_LOCATOR = By.cssSelector(".login-message.is-success");
    private static final By LOGIN_PAGE_LOADED = By.cssSelector(".s-loginPage.s-ready");

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

    public void clickUseLoginOrganisation() {
        waitForElementVisible(useOrganisationLoginButton).click();
    }

    public String getTextUseOrganisationLoginButton() {
        return waitForElementVisible(useOrganisationLoginButton).getText();
    }

    public String getTextLoginWithIsolatedStagingAccount() {
        return waitForElementVisible(cssSelector("button[class*='s-btn-login_with_your_isolated1']"), browser).getText();
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
        Graphene.waitGui().until(ExpectedConditions.elementToBeClickable(registrationLink)).click();

        try {
            if (href.endsWith("request-a-demo")) {
                Function<WebDriver, Boolean> requestADemoPageDisplayed = browser ->
                        browser.getCurrentUrl().startsWith("https://www.gooddata.com/request-a-demo");
                Graphene.waitGui().until(requestADemoPageDisplayed);
            } else {
                RegistrationPage.getInstance(browser);
            }
        } catch (Exception noSuchElement) {
            log.warning("System loading forever !!!. Refresh page");
            browser.navigate().refresh();
            Graphene.waitGui().until(ExpectedConditions.elementToBeClickable(registrationLink)).click();
        }
    }

    public LoginFragment waitForNotificationMessageDisplayed() {
        waitForElementVisible(NOTIFICATION_MESSAGE_LOCATOR, browser);
        return this;
    }

    public String getNotificationMessage() {
        return waitForElementVisible(NOTIFICATION_MESSAGE_LOCATOR, browser).getText();
    }

    public String getContainerLoginForm() {
        return waitForElementVisible(LOGIN_PAGE_LOADED, browser).getText();
    }

    public void clickLoginServiceChanelAccount(){
        waitForElementVisible(loginServiceChanelAccount).click();
    }
}
