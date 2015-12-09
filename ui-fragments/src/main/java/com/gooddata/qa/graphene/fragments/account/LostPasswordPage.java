package com.gooddata.qa.graphene.fragments.account;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.utils.mail.ImapUtils.*;

import java.io.IOException;
import java.util.Collection;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.GDEmails;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.utils.mail.ImapClient;
import com.google.common.collect.Iterables;

public class LostPasswordPage extends AbstractFragment {

    public static final String LOST_PASSWORD_PAGE_CLASS_NAME = "lostPasswordPage";

    private static final By ERROR_MESSAGE_LOCATOR = By.cssSelector("#gd-overlays div.content");
    private static final By PAGE_MESSAGE_LOCATOR = By.cssSelector(".message");

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

    public void resetPassword(String email, boolean validReset) {
        waitForElementVisible(emailInput).clear();
        emailInput.sendKeys(email);
        waitForElementVisible(resetButton).click();
        if (validReset) {
            waitForElementNotVisible(resetButton);
        }
    }

    public String resetPassword(ImapClient imapClient, String email)
            throws MessagingException, IOException {
        int expectedMessageCount = getMessageWithExpectedReceivedTime(imapClient,
                GDEmails.REGISTRATION, RESET_PASSWORD_EMAIL_SUBJECT, 0).size();

        resetPassword(email, true);

        return getResetPasswordLink(imapClient, expectedMessageCount);
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

    private String getResetPasswordLink(ImapClient imapClient, int expectedMessageCount)
            throws MessagingException, IOException {
        Collection<Message> messages = waitForMessageWithExpectedCount(imapClient, GDEmails.REGISTRATION,
                RESET_PASSWORD_EMAIL_SUBJECT, expectedMessageCount);
        Message resetPasswordMessage = Iterables.getLast(messages);
        String messageBody = ImapClient.getEmailBody(resetPasswordMessage);
        int beginIndex = messageBody.indexOf("/l/");
        return messageBody.substring(beginIndex, messageBody.indexOf("\n", beginIndex));
    }
}
