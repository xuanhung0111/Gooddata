package com.gooddata.qa.graphene.fragments.disc;

import java.util.List;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

import com.gooddata.qa.graphene.enums.DISCNotificationEvents;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class NotificationRulesDialog extends AbstractFragment {

    @FindBy(xpath = "//form[contains(@class, 'ait-notification-rule-list-item')]")
    private List<WebElement> notificationRuleItems;

    @FindBy(css = ".ait-notification-rules-empty-state")
    private WebElement notificationRulesEmptyState;

    @FindBy(css = ".ait-notification-rules-add-btn")
    private WebElement addNotificationRuleButton;

    @FindBy(css = ".ait-notification-rules-close-btn")
    private WebElement closeNotificationRulesButton;

    @FindBy(css = ".ait-notification-rule-list-item-params ul li")
    private List<WebElement> availableParams;

    private String ERROR_NOTIFICATION_EMAIL =
            "Email address is invalid. Please insert an email address in this format: email@domain.com. You may insert only one recipient per notification rule.";
    private String ERROR_NOTIFICATION_SUBJECT = "Subject cannot be empty.";
    private String ERROR_NOTIFICATION_MESSAGE = "Message cannot be empty.";
    private String ERROR_NOTIFICATION_CUSTOM_FIELD =
            "Invalid Custom Event Name Bubble Message: The event name is invalid." + "*"
                    + "Please insert alphanumeric characters only. No spaces.";

    private By BY_NOTIFICATION_RULE_EMAIL = By
            .cssSelector(".ait-notification-rule-list-item-email input");
    private By BY_NOTIFICATION_SUBJECT = By
            .cssSelector(".ait-notification-rule-list-item-subject input");
    private By BY_NOTIFICATION_MESSAGE = By
            .cssSelector(".ait-notification-rule-list-item-message textarea");
    private By BY_NOTIFICATION_EVENT = By.cssSelector("select");
    private By BY_CUSTOM_EVENT_NAME = By.cssSelector(".event-field input");
    private By BY_SAVE_BUTTON = By.cssSelector(".button-positive");
    private By BY_CANCEL_BUTTON = By.cssSelector(".button-secondary");
    private By BY_NOTIFICATION_EXPAND_BUTTON = By
            .cssSelector(".ait-notification-rule-list-item-expand-btn");
    private By BY_ERROR_BUBBLE = By.cssSelector(".bubble-negative");
    private By BY_DELETE_NOTIFICATION_BUTTON = By
            .xpath("//span[contains(@class, 'delete-btns')]/span[text()='Delete this rule']");
    private By BY_CONFIRM_DELETE_NOTIFICATION = By
            .xpath("//span[contains(@class, 'delete-confirmation-btn')]/span[text()='Delete']");
    private By BY_CANCEL_DELETE_NOTIFICATION = By
            .xpath("//span[contains(@class, 'delete-confirmation-btn')]/span[text()='Cancel']");

    public String getEmptyStateMessage() {
        return waitForElementVisible(notificationRulesEmptyState).getText();
    }

    public void clickOnAddNotificationButton() throws InterruptedException {
        int notificationNumber = getNotificationNumber();
        waitForElementVisible(addNotificationRuleButton).click();
        for (int i = 0; i < 10 && notificationNumber == getNotificationNumber(); i++) {
            Thread.sleep(1000);
        }
    }

    public int getNotificationNumber() {
        return notificationRuleItems.size();
    }

    public void setNotificationEmail(int notificationIndex, String email) {
        notificationRuleItems.get(notificationIndex).findElement(BY_NOTIFICATION_RULE_EMAIL)
                .sendKeys(email);
    }

    public String getNotificationEmail(int notificationIndex) throws InterruptedException {
        for (int i = 0; i < 5
                && notificationRuleItems.get(notificationIndex)
                        .findElement(BY_NOTIFICATION_RULE_EMAIL).getAttribute("value").isEmpty(); i++)
            Thread.sleep(1000);
        return notificationRuleItems.get(notificationIndex).findElement(BY_NOTIFICATION_RULE_EMAIL)
                .getAttribute("value");
    }

    public void clearNotificationEmail(int notificationIndex) {
        notificationRuleItems.get(notificationIndex).findElement(BY_NOTIFICATION_RULE_EMAIL)
                .clear();
    }

    public void setNotificationEvent(int notificationIndex, DISCNotificationEvents event) {
        Select scheduleEvent =
                new Select(notificationRuleItems.get(notificationIndex).findElement(
                        BY_NOTIFICATION_EVENT));
        switch (event) {
            case SUCCESS:
                scheduleEvent.selectByVisibleText(DISCNotificationEvents.SUCCESS.getEventOption());
                break;
            case FAILURE:
                scheduleEvent.selectByVisibleText(DISCNotificationEvents.FAILURE.getEventOption());
                break;
            case PROCESS_STARTED:
                scheduleEvent.selectByVisibleText(DISCNotificationEvents.PROCESS_STARTED
                        .getEventOption());
                break;
            case PROCESS_SCHEDULED:
                scheduleEvent.selectByVisibleText(DISCNotificationEvents.PROCESS_SCHEDULED
                        .getEventOption());
                break;
            case CUSTOM_EVENT:
                scheduleEvent.selectByVisibleText(DISCNotificationEvents.CUSTOM_EVENT
                        .getEventOption());
                break;
        }
    }

    public void setCustomEventName(int notifcationNumber, String customEventName) {
        notificationRuleItems.get(notifcationNumber).findElement(BY_CUSTOM_EVENT_NAME)
                .sendKeys(customEventName);
    }

    public String getCustomEventName(int notifcationNumber) {
        return notificationRuleItems.get(notifcationNumber).findElement(BY_CUSTOM_EVENT_NAME)
                .getAttribute("value");
    }

    public void clearCustomEventName(int notifcationNumber) {
        notificationRuleItems.get(notifcationNumber).findElement(BY_CUSTOM_EVENT_NAME).clear();
    }

    public void setNotificationSubject(int notificationIndex, String subject) {
        notificationRuleItems.get(notificationIndex).findElement(BY_NOTIFICATION_SUBJECT)
                .sendKeys(subject);
    }

    public String getNotificationSubject(int notificationIndex) {
        return notificationRuleItems.get(notificationIndex).findElement(BY_NOTIFICATION_SUBJECT)
                .getAttribute("value");
    }

    public void clearNotificationSubject(int notificationIndex) {
        notificationRuleItems.get(notificationIndex).findElement(BY_NOTIFICATION_SUBJECT).clear();
    }

    public void setNotificationMessage(int notificationIndex, String message) {
        notificationRuleItems.get(notificationIndex).findElement(BY_NOTIFICATION_MESSAGE)
                .sendKeys(message);
    }

    public String getNotificationMessage(int notificationIndex) {
        return notificationRuleItems.get(notificationIndex).findElement(BY_NOTIFICATION_MESSAGE)
                .getAttribute("value");
    }

    public void clearNotificationMessage(int notificationIndex) {
        notificationRuleItems.get(notificationIndex).findElement(BY_NOTIFICATION_MESSAGE).clear();;
    }

    public String getAvailableParams() {
        String availableParamsList = "";
        for (int i = 0; i < availableParams.size(); i++) {
            availableParamsList = availableParamsList.concat(availableParams.get(i).getText());
        }
        return availableParamsList;
    }

    public void saveNotification(int notificationIndex) {
        notificationRuleItems.get(notificationIndex).findElement(BY_SAVE_BUTTON).click();
    }

    public void cancelSaveNotification(int notificationIndex) {
        notificationRuleItems.get(notificationIndex).findElement(BY_CANCEL_BUTTON).click();
    }

    public void setNotificationFields(int notificationIndex, String email, String subject,
            String message, DISCNotificationEvents event, String customEventName) {
        waitForElementVisible(notificationRuleItems.get(notificationIndex));
        setNotificationEmail(notificationIndex, email);
        setNotificationSubject(notificationIndex, subject);
        setNotificationMessage(notificationIndex, message);
        setNotificationEvent(notificationIndex, event);
        if (customEventName != null)
            setCustomEventName(notificationIndex, customEventName);
    }

    public void expandNotificationRule(int notificationIndex) {
        waitForElementVisible(notificationRuleItems.get(notificationIndex));
        if (notificationRuleItems.get(notificationIndex).findElement(BY_NOTIFICATION_EXPAND_BUTTON)
                .getAttribute("class").contains("icon-navigatedown"))
            notificationRuleItems.get(notificationIndex).findElement(BY_NOTIFICATION_EXPAND_BUTTON)
                    .click();
    }

    public boolean isNotExpanded(int notificationIndex) {
        waitForElementVisible(notificationRuleItems.get(notificationIndex));
        return notificationRuleItems.get(notificationIndex)
                .findElement(BY_NOTIFICATION_EXPAND_BUTTON).getAttribute("class")
                .contains("icon-navigatedown");
    }

    public void closeNotificationRulesDialog() {
        waitForElementVisible(closeNotificationRulesButton).click();
        waitForElementNotPresent(getRoot());
    }

    public void assertNotificationFields(String processName, int notificationIndex, String email,
            String subject, String message, DISCNotificationEvents event, String customEventName)
            throws InterruptedException {
        assertEquals(email, getNotificationEmail(notificationIndex));
        assertEquals(subject, getNotificationSubject(notificationIndex));
        assertEquals(message, getNotificationMessage(notificationIndex));
        Select scheduleEvent =
                new Select(notificationRuleItems.get(notificationIndex).findElement(
                        BY_NOTIFICATION_EVENT));
        assertEquals(event.getEventOption(), scheduleEvent.getFirstSelectedOption().getText());
        if (customEventName != null)
            assertEquals(customEventName, getCustomEventName(notificationIndex));
    }

    public void checkInvalidNotificationFields(int notificationIndex, String email, String subject,
            String message, DISCNotificationEvents event, String customEventName) {
        if (email != null) {
            setNotificationEmail(notificationIndex, email);
            notificationRuleItems.get(notificationIndex).click();
            notificationRuleItems.get(notificationIndex).findElement(BY_NOTIFICATION_RULE_EMAIL)
                    .click();
            waitForElementVisible(notificationRuleItems.get(notificationIndex).findElement(
                    BY_ERROR_BUBBLE));
            System.out.println("Invalid Email Bubble Message: "
                    + notificationRuleItems.get(notificationIndex).findElement(BY_ERROR_BUBBLE)
                            .getText());
            assertEquals(ERROR_NOTIFICATION_EMAIL, notificationRuleItems.get(notificationIndex)
                    .findElement(BY_ERROR_BUBBLE).getText());
        }
        if (subject != null) {
            setNotificationSubject(notificationIndex, subject);
            notificationRuleItems.get(notificationIndex).click();
            notificationRuleItems.get(notificationIndex).findElement(BY_NOTIFICATION_SUBJECT)
                    .click();
            waitForElementVisible(notificationRuleItems.get(notificationIndex).findElement(
                    BY_ERROR_BUBBLE));
            System.out.println("Invalid Subject Bubble Message: "
                    + notificationRuleItems.get(notificationIndex).findElement(BY_ERROR_BUBBLE)
                            .getText());
            assertEquals(ERROR_NOTIFICATION_SUBJECT, notificationRuleItems.get(notificationIndex)
                    .findElement(BY_ERROR_BUBBLE).getText());
        }
        if (message != null) {
            setNotificationMessage(notificationIndex, message);
            notificationRuleItems.get(notificationIndex).click();
            notificationRuleItems.get(notificationIndex).findElement(BY_NOTIFICATION_MESSAGE)
                    .click();
            waitForElementVisible(notificationRuleItems.get(notificationIndex).findElement(
                    BY_ERROR_BUBBLE));
            System.out.println("Invalid Message Bubble Message: "
                    + notificationRuleItems.get(notificationIndex).findElement(BY_ERROR_BUBBLE)
                            .getText());
            assertEquals(ERROR_NOTIFICATION_MESSAGE, notificationRuleItems.get(notificationIndex)
                    .findElement(BY_ERROR_BUBBLE).getText());
        }
        if (customEventName != null) {
            setNotificationEvent(notificationIndex, DISCNotificationEvents.CUSTOM_EVENT);
            setCustomEventName(notificationIndex, customEventName);
            notificationRuleItems.get(notificationIndex).click();
            notificationRuleItems.get(notificationIndex).findElement(BY_CUSTOM_EVENT_NAME).click();
            waitForElementVisible(notificationRuleItems.get(notificationIndex).findElement(
                    BY_ERROR_BUBBLE));
            System.out.println("Invalid Custom Event Name Bubble Message: "
                    + notificationRuleItems.get(notificationIndex).findElement(BY_ERROR_BUBBLE)
                            .getText());
            Pattern.matches(ERROR_NOTIFICATION_CUSTOM_FIELD,
                    notificationRuleItems.get(notificationIndex).findElement(BY_ERROR_BUBBLE)
                            .getText());
        }
    }

    public void deleteNotification(int notificationIndex, boolean isConfirmed)
            throws InterruptedException {
        expandNotificationRule(notificationIndex);
        notificationRuleItems.get(notificationIndex).findElement(BY_DELETE_NOTIFICATION_BUTTON)
                .click();
        if (isConfirmed)
            waitForElementVisible(getRoot().findElement(BY_CONFIRM_DELETE_NOTIFICATION)).click();
        else
            waitForElementVisible(getRoot().findElement(BY_CANCEL_DELETE_NOTIFICATION)).click();
    }
}
