package com.gooddata.qa.graphene.fragments.account;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.mail.ImapUtils.waitForMessages;
import static org.openqa.selenium.By.className;

import java.io.IOException;
import java.util.Collection;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.GDEmails;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.login.LoginFragment;
import com.gooddata.qa.utils.mail.ImapClient;
import com.gooddata.qa.utils.mail.ImapUtils;
import com.google.common.collect.Iterables;

public class LostPasswordPage extends AbstractFragment {

    public static final By ERROR_MESSAGE_LOCATOR = By.cssSelector(".validation-error, #gd-overlays div.content");
    public static final String PASSWORD_HINT = "Choose a unique password of at least 7 characters.";

    private static final By PAGE_MESSAGE_LOCATOR = By.className("login-message");
    private static final String RESET_PASSWORD_EMAIL_SUBJECT = "GoodData password reset request";

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

    private static LostPasswordPage instance = null;

    public static final LostPasswordPage getInstance(SearchContext context) {
        if (instance == null) {
            instance = Graphene.createPageFragment(LostPasswordPage.class,
                    waitForElementVisible(className("lostPasswordPage"), context));
        }
        return waitForFragmentVisible(instance);
    }

    public static final LostPasswordPage getInstance(By rootLocator, SearchContext context) {
        return Graphene.createPageFragment(LostPasswordPage.class, waitForElementVisible(rootLocator, context));
    }

    // this element is not in lost password page. It's in info page. So to avoid creating new fragment,
    // make it a static method for checking
    public static String getPageLocalMessage(SearchContext context) {
        return waitForElementVisible(PAGE_MESSAGE_LOCATOR, context).getText();
    }

    public LostPasswordPage resetPassword(String email, boolean validReset) {
        waitForElementVisible(emailInput).clear();
        emailInput.sendKeys(email);
        waitForElementVisible(resetButton).click();
        if (validReset) {
            waitForElementNotVisible(resetButton);
        }
        return this;
    }

    public String resetPassword(ImapClient imapClient, String email)
            throws MessagingException, IOException {
        int messageCount = imapClient.getMessagesCount(GDEmails.REGISTRATION, RESET_PASSWORD_EMAIL_SUBJECT);

        resetPassword(email, true);

        return getResetPasswordLink(imapClient, messageCount + 1);
    }

    public void setNewPassword(String password) {
        waitForElementVisible(newPasswordInput).clear();
        newPasswordInput.sendKeys(password);
        waitForElementVisible(setPasswordButton).click();
    }

    public String getErrorMessage() {
        return waitForElementVisible(ERROR_MESSAGE_LOCATOR, browser).getText();
    }

    public LoginFragment backToLoginPage() {
        waitForElementVisible(backToLoginLink).click();
        return LoginFragment.getInstance(browser);
    }

    public String getPasswordHint() {
        return waitForElementVisible(By.className("field-hint"), getRoot()).getText();
    }

    private String getResetPasswordLink(ImapClient imapClient, int expectedMessageCount)
            throws MessagingException, IOException {
        Collection<Message> messages = waitForMessages(imapClient, GDEmails.REGISTRATION,
                RESET_PASSWORD_EMAIL_SUBJECT, expectedMessageCount);
        Message resetPasswordMessage = Iterables.getLast(messages);
        String messageBody = ImapUtils.getEmailBody(resetPasswordMessage);
        int beginIndex = messageBody.indexOf("/l/");
        return messageBody.substring(beginIndex, messageBody.indexOf("\n", beginIndex));
    }
}
