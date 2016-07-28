package com.gooddata.qa.graphene.fragments.account;

import static com.gooddata.qa.graphene.fragments.account.LostPasswordPage.ERROR_MESSAGE_LOCATOR;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.mail.ImapUtils.waitForMessages;
import static org.openqa.selenium.By.className;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.entity.account.RegistrationForm;
import com.gooddata.qa.graphene.enums.GDEmails;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.login.LoginFragment;
import com.gooddata.qa.utils.mail.ImapClient;
import com.google.common.collect.Iterables;

public class RegistrationPage extends AbstractFragment {

    private static final String CAPTCHA_SECRITY_CODE = "WA-587";
    private static final String CAPTCHA_INPUT_CLASS_NAME = ".captcha-input input";
    private static final String REGISTRATION_EMAIL_SUBJECT = "Activate Your GoodData Account";

    @FindBy
    private WebElement firstname;

    @FindBy(name = "surname")
    private WebElement lastname;

    @FindBy
    private WebElement email;

    @FindBy
    private WebElement password;

    @FindBy
    private WebElement phone;

    @FindBy
    private WebElement company;

    @FindBy
    private WebElement title;

    @FindBy(css = ".s-registration-industry select")
    private Select industrySelect;

    @FindBy(css = CAPTCHA_INPUT_CLASS_NAME)
    private WebElement captchaInput;

    @FindBy(css = ".s-registration-license input")
    private WebElement agreeCheckbox;

    @FindBy(css = ".s-btn-register")
    private WebElement registerButton;

    @FindBy(css = "a[href*='login']")
    private WebElement loginLink;

    private static RegistrationPage instance;

    public static final RegistrationPage getInstance(SearchContext context) {
        if (instance == null) {
            instance = Graphene.createPageFragment(RegistrationPage.class,
                    waitForElementVisible(className("s-registrationPage"), context));
        }
        return waitForFragmentVisible(instance);
    }

    public RegistrationPage fillInRegistrationForm(RegistrationForm registrationForm) {
        String captchaSecurityCode = CAPTCHA_SECRITY_CODE;
        String emailValue = registrationForm.getEmail();

        if(!isCaptchaFieldPresent()){
            captchaSecurityCode = null;
        }

        if(!isEmailFieldEditable()){
            emailValue = null;
        }

        return enterData(firstname, registrationForm.getFirstName())
                .enterData(lastname, registrationForm.getLastName())
                .enterData(email, emailValue)
                .enterData(password, registrationForm.getPassword())
                .enterData(phone, registrationForm.getPhone())
                .enterData(company, registrationForm.getCompany())
                .enterData(title, registrationForm.getJobTitle())
                .selectIndustry(registrationForm.getIndustry())
                .enterData(captchaInput, captchaSecurityCode);
    }

    public String registerNewUserSuccessfully(ImapClient imapClient, RegistrationForm registrationForm)
            throws MessagingException, IOException {
        int messageCount = imapClient.getMessagesCount(GDEmails.REGISTRATION, REGISTRATION_EMAIL_SUBJECT);

        registerNewUserSuccessfully(registrationForm);

        return getActivationLink(imapClient, messageCount + 1);
    }

    public void registerNewUserSuccessfully(RegistrationForm registrationForm) {
        fillInRegistrationForm(registrationForm).agreeRegistrationLicense()
                .submitForm();
        waitForFragmentNotVisible(this);
    }

    public RegistrationPage enterEmail(String userEmail) {
        return enterData(email, userEmail);
    }

    public RegistrationPage enterPassword(String userPassword) {
        return enterData(password, userPassword);
    }

    public RegistrationPage enterPhoneNumber(String phoneNumber) {
        return enterData(phone, phoneNumber);
    }

    public RegistrationPage enterCaptcha(String captchaCode) {
        return enterData(captchaInput, captchaCode);
    }

    public RegistrationPage enterSpecialCaptcha() {
        return enterCaptcha(CAPTCHA_SECRITY_CODE);
    }

    public void submitForm() {
        waitForElementVisible(registerButton).click();
    }

    public String getErrorMessage() {
        return waitForElementVisible(ERROR_MESSAGE_LOCATOR, browser).getText();
    }

    public boolean isEmailInputError() {
        return isInputError(email);
    }

    public boolean isCaptchaInputError() {
        return isInputError(captchaInput);
    }

    public LoginFragment selectLoginLink() {
        waitForElementVisible(loginLink).click();
        return LoginFragment.getInstance(browser);
    }

    public boolean isEmailFieldEditable() {
        return Objects.isNull(waitForElementVisible(email).getAttribute("disabled"));
    }

    public boolean isCaptchaFieldPresent() {
        return isElementPresent(By.cssSelector(CAPTCHA_INPUT_CLASS_NAME), browser);
    }

    public RegistrationPage agreeRegistrationLicense() {
        if (!waitForElementVisible(agreeCheckbox).isSelected()) {
            agreeCheckbox.click();
        }
        return this;
    }

    private RegistrationPage selectIndustry(String industry) {
        waitForElementVisible(industrySelect).selectByVisibleText(industry);
        return this;
    }

    private RegistrationPage enterData(WebElement input, String data) {
        if(Objects.isNull(data)){
            return this;
        }
        waitForElementVisible(input).clear();
        input.sendKeys(data);
        return this;
    }

    private boolean isInputError(WebElement input) {
        return waitForElementVisible(input).getAttribute("class").contains("has-error");
    }

    private String getActivationLink(ImapClient imapClient, int expectedMessageCount)
            throws MessagingException, IOException {
        Collection<Message> messages = waitForMessages(imapClient, GDEmails.REGISTRATION,
                REGISTRATION_EMAIL_SUBJECT, expectedMessageCount);
        Message activationMessage = Iterables.getLast(messages);
        String messageBody = ImapClient.getEmailBody(activationMessage);
        int beginIndex = messageBody.indexOf("/i/");
        return messageBody.substring(beginIndex, messageBody.indexOf("\n", beginIndex));
    }
}
