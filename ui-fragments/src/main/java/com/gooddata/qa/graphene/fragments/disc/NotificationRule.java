package com.gooddata.qa.graphene.fragments.disc;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.stream.Collectors.joining;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.entity.disc.NotificationBuilder;
import com.gooddata.qa.graphene.enums.disc.NotificationEvents;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Predicate;

public class NotificationRule extends AbstractFragment {

    @FindBy(css = ".ait-notification-rule-list-item-params ul li")
    private List<WebElement> availableParams;

    private String ERROR_NOTIFICATION_EMAIL =
            "Email address is invalid. Please insert an email address in this format: email@domain.com. "
            + "You may insert only one recipient per notification rule.";
    private String ERROR_NOTIFICATION_SUBJECT = "Subject cannot be empty.";
    private String ERROR_NOTIFICATION_MESSAGE = "Message cannot be empty.";
    private String ERROR_NOTIFICATION_CUSTOM_FIELD =
            "The event name is invalid.([\\n]*[\\r]*)Please insert alphanumeric characters only. No spaces.";

    private By BY_NOTIFICATION_RULE_EMAIL = By.cssSelector(".ait-notification-rule-list-item-email input");
    private By BY_NOTIFICATION_SUBJECT = By.cssSelector(".ait-notification-rule-list-item-subject input");
    private By BY_NOTIFICATION_MESSAGE = By.cssSelector(".ait-notification-rule-list-item-message textarea");
    private By BY_NOTIFICATION_EVENT = By.cssSelector("select");
    private By BY_CUSTOM_EVENT_NAME = By.cssSelector(".event-field input");
    private By BY_SAVE_BUTTON = By.cssSelector(".button-positive");
    private By BY_CANCEL_BUTTON = By.cssSelector(".button-secondary");
    private By BY_NOTIFICATION_EXPAND_BUTTON = By.cssSelector(".ait-notification-rule-list-item-expand-btn");
    private By BY_ERROR_BUBBLE = By.cssSelector(".bubble-negative");
    private By BY_DELETE_NOTIFICATION_BUTTON = By
            .xpath("//span[contains(@class, 'delete-btns')]/span[text()='Delete this rule']");
    private By BY_CONFIRM_DELETE_NOTIFICATION = By
            .xpath("//span[contains(@class, 'delete-confirmation-btn')]/span[text()='Delete']");
    private By BY_CANCEL_DELETE_NOTIFICATION = By
            .xpath("//span[contains(@class, 'delete-confirmation-btn')]/span[text()='Cancel']");

    public void setNotificationFields(NotificationBuilder notificationBuilder) {
        fillInEmail(notificationBuilder.getEmail());
        fillInSubject(notificationBuilder.getSubject());
        fillInMessage(notificationBuilder.getMessage());
        Select scheduleEvent = getEventSelect();
        scheduleEvent.selectByVisibleText(notificationBuilder.getEvent().getEventOption());
        if (notificationBuilder.getEvent() == NotificationEvents.CUSTOM_EVENT)
            fillInNotificationCustomEventName(notificationBuilder.getCustomEventName());
    }

    public void fillInEmail(String email) {
        waitForElementVisible(BY_NOTIFICATION_RULE_EMAIL, getRoot()).sendKeys(email);
        activateInputValidate();
    }

    public String getEmail() {
        return waitForElementVisible(BY_NOTIFICATION_RULE_EMAIL, getRoot()).getAttribute("value");
    }

    public void fillInSubject(String subject) {
        waitForElementVisible(BY_NOTIFICATION_SUBJECT, getRoot()).sendKeys(subject);
        activateInputValidate();
    }

    public String getSubject() {
        return waitForElementVisible(BY_NOTIFICATION_SUBJECT, getRoot()).getAttribute("value");
    }

    public void fillInMessage(String message) {
        waitForElementVisible(BY_NOTIFICATION_MESSAGE, getRoot()).sendKeys(message);
        activateInputValidate();
    }

    public String getMessage() {
        return waitForElementVisible(BY_NOTIFICATION_MESSAGE, getRoot()).getAttribute("value");
    }

    public void fillInNotificationCustomEventName(String customEventName) {
        waitForElementVisible(BY_CUSTOM_EVENT_NAME, getRoot()).sendKeys(customEventName);
        activateInputValidate();
    }

    public Select getEventSelect() {
        return new Select(waitForElementVisible(BY_NOTIFICATION_EVENT, getRoot()));
    }

    public String getCustomEvent() {
        return waitForElementVisible(BY_CUSTOM_EVENT_NAME, getRoot()).getAttribute("value");
    }

    public void clearNotificationEmail() {
        waitForElementVisible(BY_NOTIFICATION_RULE_EMAIL, getRoot()).clear();
    }

    public void setNotificationEvent(NotificationEvents event) {
        Select scheduleEvent = new Select(waitForElementVisible(BY_NOTIFICATION_EVENT, getRoot()));
        scheduleEvent.selectByVisibleText(event.getEventOption());
    }

    public void clearNotificationSubject() {
        waitForElementVisible(BY_NOTIFICATION_SUBJECT, getRoot()).clear();
    }

    public void clearNotificationMessage() {
        waitForElementVisible(BY_NOTIFICATION_MESSAGE, getRoot()).clear();;
    }

    public String getAvailableParams() {
        return availableParams.stream().map(WebElement::getText).collect(joining());
    }

    public boolean isCorrectEmailValidationError() {
        waitForElementVisible(BY_NOTIFICATION_RULE_EMAIL, getRoot()).click();
        return ERROR_NOTIFICATION_EMAIL.equals(waitForElementVisible(BY_ERROR_BUBBLE, browser).getText());
    }

    public boolean isCorrectSubjectValidationError() {
        waitForElementVisible(BY_NOTIFICATION_SUBJECT, getRoot()).click();
        return ERROR_NOTIFICATION_SUBJECT.equals(waitForElementVisible(BY_ERROR_BUBBLE, browser).getText());
    }

    public boolean isCorrectMessageValidationError() {
        waitForElementVisible(BY_NOTIFICATION_MESSAGE, getRoot()).click();
        return ERROR_NOTIFICATION_MESSAGE.equals(waitForElementVisible(BY_ERROR_BUBBLE, browser).getText());
    }

    public boolean isCorrectCustomEventNameValidationError() {
        waitForElementVisible(BY_CUSTOM_EVENT_NAME, getRoot()).click();
        String errorBubbleMessage = waitForElementVisible(BY_ERROR_BUBBLE, browser).getText();
        return errorBubbleMessage.matches(ERROR_NOTIFICATION_CUSTOM_FIELD);
    }

    public void deleteNotification(boolean confirm) {
        expandNotificationRule();
        waitForElementVisible(BY_DELETE_NOTIFICATION_BUTTON, getRoot()).click();
        if (confirm)
            waitForElementVisible(getRoot().findElement(BY_CONFIRM_DELETE_NOTIFICATION)).click();
        else
            waitForElementVisible(getRoot().findElement(BY_CANCEL_DELETE_NOTIFICATION)).click();
    }

    public boolean isNotExpanded() {
        return waitForElementVisible(BY_NOTIFICATION_EXPAND_BUTTON, getRoot()).getAttribute("class").contains(
                "icon-navigatedown");
    }

    public void expandNotificationRule() {
        if (waitForElementVisible(BY_NOTIFICATION_EXPAND_BUTTON, getRoot()).getAttribute("class").contains(
                "icon-navigatedown"))
            waitForElementVisible(BY_NOTIFICATION_EXPAND_BUTTON, getRoot()).click();
    }

    public void saveNotification() {
        waitForElementVisible(BY_SAVE_BUTTON, getRoot()).click();
        waitForElementNotPresent(BY_SAVE_BUTTON);
        Predicate<WebDriver> notificationRuleSaved = 
                browser -> !getRoot().getAttribute("class").contains("modified");
        Graphene.waitGui().until(notificationRuleSaved);
    }

    public void cancelSaveNotification() {
        waitForElementVisible(BY_CANCEL_BUTTON, getRoot()).click();
        waitForElementNotPresent(BY_SAVE_BUTTON);
    }
    
    private void activateInputValidate() {
        getRoot().click();
    }
}
