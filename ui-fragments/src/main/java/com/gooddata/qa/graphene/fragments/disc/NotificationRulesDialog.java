package com.gooddata.qa.graphene.fragments.disc;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

import com.gooddata.qa.graphene.entity.disc.NotificationBuilder;
import com.gooddata.qa.graphene.enums.disc.NotificationEvents;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Predicate;

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
            "The event name is invalid.([\\n]*[\\r]*)Please insert alphanumeric characters only. No spaces.";

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

    public void clickOnAddNotificationButton() {
        final int notificationNumberBeforeAdding = getNotificationNumber();
        waitForElementVisible(addNotificationRuleButton).click();
        Graphene.waitGui().until(new Predicate<WebDriver>() {

            @Override
            public boolean apply(WebDriver arg0) {
                return notificationNumberBeforeAdding < getNotificationNumber();
            }
        });
    }

    public int getNotificationNumber() {
        return notificationRuleItems.size();
    }

    public void setNotificationFields(NotificationBuilder notificationBuilder) {
        WebElement notificationRuleItem = notificationRuleItems.get(notificationBuilder.getIndex());
        waitForElementVisible(notificationRuleItem);
        notificationRuleItem.findElement(BY_NOTIFICATION_RULE_EMAIL).sendKeys(
                notificationBuilder.getEmail());
        notificationRuleItem.findElement(BY_NOTIFICATION_SUBJECT).sendKeys(
                notificationBuilder.getSubject());
        notificationRuleItem.findElement(BY_NOTIFICATION_MESSAGE).sendKeys(
                notificationBuilder.getMessage());
        Select scheduleEvent =
                new Select((notificationRuleItem).findElement(BY_NOTIFICATION_EVENT));
        scheduleEvent.selectByVisibleText(notificationBuilder.getEvent().getEventOption());
        if (notificationBuilder.getEvent() == NotificationEvents.CUSTOM_EVENT)
            notificationRuleItem.findElement(BY_CUSTOM_EVENT_NAME).sendKeys(
                    notificationBuilder.getCustomEventName());
    }

    public void assertNotificationFields(NotificationBuilder notificationBuilder) {
        final WebElement notificationRuleItem =
                notificationRuleItems.get(notificationBuilder.getIndex());
        Graphene.waitGui().until(new Predicate<WebDriver>() {

            @Override
            public boolean apply(WebDriver arg0) {
                return !notificationRuleItem.findElement(BY_NOTIFICATION_RULE_EMAIL)
                        .getAttribute("value").isEmpty();
            }
        });
        assertEquals(
                notificationRuleItem.findElement(BY_NOTIFICATION_RULE_EMAIL).getAttribute("value"),
                notificationBuilder.getEmail());
        assertEquals(notificationRuleItem.findElement(BY_NOTIFICATION_SUBJECT)
                .getAttribute("value"), notificationBuilder.getSubject());
        assertEquals(notificationRuleItem.findElement(BY_NOTIFICATION_MESSAGE)
                .getAttribute("value"), notificationBuilder.getMessage());
        Select scheduleEvent = new Select(notificationRuleItem.findElement(BY_NOTIFICATION_EVENT));
        assertEquals(notificationBuilder.getEvent().getEventOption(), scheduleEvent
                .getFirstSelectedOption().getText());
        if (notificationBuilder.getEvent() == NotificationEvents.CUSTOM_EVENT)
            assertEquals(
                    notificationRuleItem.findElement(BY_CUSTOM_EVENT_NAME).getAttribute("value"),
                    notificationBuilder.getCustomEventName());
    }

    public void closeNotificationRulesDialog() {
        waitForElementVisible(closeNotificationRulesButton).click();
        waitForElementNotPresent(getRoot());
    }

    public void checkEmptyNotificationFields(int notificationIndex) {
        WebElement notificationRuleItem = notificationRuleItems.get(notificationIndex);

        checkEmptyEmailField(notificationRuleItem);
        checkEmptySubjectField(notificationRuleItem);
        checkEmptyMessageField(notificationRuleItem);
        checkEmptyCustomEventNameField(notificationIndex, notificationRuleItem);
    }

    public void checkOnlyOneEmailError(int notificationIndex) {
        WebElement notificationRuleItem = notificationRuleItems.get(notificationIndex);
        notificationRuleItem.findElement(BY_NOTIFICATION_RULE_EMAIL).sendKeys(
                "abc@gmail.com,xyz@gmail.com");
        notificationRuleItem.click();
        notificationRuleItem.findElement(BY_NOTIFICATION_RULE_EMAIL).click();
        assertEquals(waitForElementVisible(BY_ERROR_BUBBLE, browser).getText(), ERROR_NOTIFICATION_EMAIL);
    }

    public void deleteNotification(NotificationBuilder notificationInfo) {
        expandNotificationRule(notificationInfo.getIndex());
        notificationRuleItems.get(notificationInfo.getIndex())
                .findElement(BY_DELETE_NOTIFICATION_BUTTON).click();
        if (notificationInfo.isSaved())
            waitForElementVisible(getRoot().findElement(BY_CONFIRM_DELETE_NOTIFICATION)).click();
        else
            waitForElementVisible(getRoot().findElement(BY_CANCEL_DELETE_NOTIFICATION)).click();
    }

    public boolean isNotExpanded(int notificationIndex) {
        waitForElementVisible(notificationRuleItems.get(notificationIndex));
        return notificationRuleItems.get(notificationIndex)
                .findElement(BY_NOTIFICATION_EXPAND_BUTTON).getAttribute("class")
                .contains("icon-navigatedown");
    }

    public void expandNotificationRule(int notificationIndex) {
        waitForElementVisible(notificationRuleItems.get(notificationIndex));
        if (notificationRuleItems.get(notificationIndex).findElement(BY_NOTIFICATION_EXPAND_BUTTON)
                .getAttribute("class").contains("icon-navigatedown"))
            notificationRuleItems.get(notificationIndex).findElement(BY_NOTIFICATION_EXPAND_BUTTON)
                    .click();
    }

    public void clearNotificationEmail(int notificationIndex) {
        notificationRuleItems.get(notificationIndex).findElement(BY_NOTIFICATION_RULE_EMAIL)
                .clear();
    }

    public void setNotificationEvent(int notificationIndex, NotificationEvents event) {
        Select scheduleEvent =
                new Select(notificationRuleItems.get(notificationIndex).findElement(
                        BY_NOTIFICATION_EVENT));
        scheduleEvent.selectByVisibleText(event.getEventOption());
    }

    public void clearNotificationSubject(int notificationIndex) {
        notificationRuleItems.get(notificationIndex).findElement(BY_NOTIFICATION_SUBJECT).clear();
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
        final WebElement notificationRuleItem = notificationRuleItems.get(notificationIndex);
        notificationRuleItem.findElement(BY_SAVE_BUTTON).click();
        waitForElementNotPresent(BY_SAVE_BUTTON);
        Graphene.waitGui().until(new Predicate<WebDriver>() {

            @Override
            public boolean apply(WebDriver arg0) {
                return !notificationRuleItem.getAttribute("class").contains("modified");
            }
        });
    }

    public void cancelSaveNotification(int notificationIndex) {
        final WebElement notificationRuleItem = notificationRuleItems.get(notificationIndex);
        notificationRuleItem.findElement(BY_CANCEL_BUTTON).click();
        waitForElementNotPresent(BY_SAVE_BUTTON);
    }

    private void checkEmptyEmailField(WebElement notificationRuleItem) {
        WebElement notificationEmailElement =
                notificationRuleItem.findElement(BY_NOTIFICATION_RULE_EMAIL);
        notificationEmailElement.sendKeys("");
        notificationRuleItem.click();
        notificationEmailElement.click();
        assertEquals(waitForElementVisible(BY_ERROR_BUBBLE, browser).getText(), ERROR_NOTIFICATION_EMAIL);
    }

    private void checkEmptySubjectField(WebElement notificationRuleItem) {
        WebElement notificationSubjectElement =
                notificationRuleItem.findElement(BY_NOTIFICATION_SUBJECT);
        notificationSubjectElement.sendKeys("");
        notificationRuleItem.click();
        notificationSubjectElement.click();
        assertEquals(waitForElementVisible(BY_ERROR_BUBBLE, browser).getText(), ERROR_NOTIFICATION_SUBJECT);
    }

    private void checkEmptyMessageField(WebElement notificationRuleItem) {
        WebElement notificationMessageElement =
                notificationRuleItem.findElement(BY_NOTIFICATION_MESSAGE);
        notificationMessageElement.sendKeys("");
        notificationRuleItem.click();
        notificationMessageElement.click();
        assertEquals(waitForElementVisible(BY_ERROR_BUBBLE, browser).getText(), ERROR_NOTIFICATION_MESSAGE);
    }

    private void checkEmptyCustomEventNameField(int notificationIndex,
            WebElement notificationRuleItem) {
        setNotificationEvent(notificationIndex, NotificationEvents.CUSTOM_EVENT);
        WebElement notificationCustomEventNameElement =
                notificationRuleItem.findElement(BY_CUSTOM_EVENT_NAME);
        notificationCustomEventNameElement.sendKeys("");
        notificationRuleItem.click();
        notificationCustomEventNameElement.click();
        String errorBubbleMessage = waitForElementVisible(BY_ERROR_BUBBLE, browser).getText();
        System.out.println("Invalid Custom Event Name Bubble Message: " + errorBubbleMessage);
        assertTrue(errorBubbleMessage.matches(ERROR_NOTIFICATION_CUSTOM_FIELD),
                "Incorrect message: " + errorBubbleMessage);
    }
}
