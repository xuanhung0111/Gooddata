package com.gooddata.qa.graphene.fragments.disc.notification;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.stream.Stream;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.entity.disc.NotificationRule;
import com.gooddata.qa.graphene.enums.disc.notification.Variable;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class NotificationRuleItem extends AbstractFragment {

    @FindBy(className = "expand-btn")
    private WebElement expandButton;

    @FindBy(css = ".email-field input")
    private WebElement emailInput;

    @FindBy(tagName = "select")
    private Select eventSelect;

    @FindBy(css = ".event-field input")
    private WebElement eventNameInput;

    @FindBy(css = ".subject-field input")
    private WebElement subjectInput;

    @FindBy(css = "[class*='message'] textarea")
    private WebElement messageInput;

    @FindBy(css = ".variables-list li")
    private Collection<WebElement> variables;

    @FindBy(className = "button-positive")
    private WebElement saveButton;

    @FindBy(className = "button-secondary")
    private WebElement cancelButton;

    @FindBy(className = "delete-btns")
    private WebElement deleteButton;

    public NotificationRuleItem expand() {
        if (waitForElementVisible(expandButton).getAttribute("class").contains("icon-navigatedown")) {
            expandButton.click();
        }
        return this;
    }

    public NotificationRuleItem enterEmail(String email) {
        return enterData(emailInput, email);
    }

    public NotificationRuleItem enterSubject(String subject) {
        return enterData(subjectInput, subject);
    }

    public NotificationRuleItem enterMessage(String message) {
        return enterData(messageInput, message);
    }

    public NotificationRuleItem enterCustomEventName(String name) {
        return enterData(eventNameInput, name);
    }

    public NotificationRuleItem selectEvent(NotificationEvent event) {
        waitForElementVisible(eventSelect).selectByVisibleText(event.toString());
        return this;
    }

    public NotificationRuleItem fillInformation(NotificationRule notificationRule) {
        enterEmail(notificationRule.getEmail())
                .enterSubject(notificationRule.getSubject())
                .enterMessage(notificationRule.getMessage())
                .selectEvent(notificationRule.getEvent());

        if (notificationRule.getEvent() == NotificationEvent.CUSTOM_EVENT) {
            enterCustomEventName(notificationRule.getCustomEventName());
        }

        return this;
    }

    public boolean isEmailInputError() {
        return waitForElementVisible(emailInput).getAttribute("class").contains("has-error");
    }

    public boolean isCustomEventNameInputError() {
        return waitForElementVisible(eventNameInput).getAttribute("class").contains("has-error");
    }

    public boolean isSubjectInputError() {
        return waitForElementVisible(subjectInput).getAttribute("class").contains("has-error");
    }

    public boolean isMessageInputError() {
        return waitForElementVisible(messageInput).getAttribute("class").contains("has-error");
    }

    public String getEmail() {
        return waitForElementVisible(emailInput).getAttribute("value");
    }

    public NotificationEvent getSelectedEvent() {
        return getNotificationEventByValue(waitForElementVisible(eventSelect).getFirstSelectedOption().getText());
    }

    public Collection<NotificationEvent> getAvailableEvents() {
        return waitForElementVisible(eventSelect).getOptions()
                .stream().map(e -> getNotificationEventByValue(e.getText())).collect(toList());
    }

    public String getSubject() {
        return waitForElementVisible(subjectInput).getAttribute("value");
    }

    public String getMessage() {
        return waitForElementVisible(messageInput).getAttribute("value");
    }

    public Collection<Variable> getVariables() {
        return getElementTexts(variables).stream().map(v -> getVariableByValue(v)).collect(toList());
    }

    public NotificationRuleItem save() {
        clickSaveButton();

        waitForElementPresent(By.cssSelector("[class*='edit-buttons'] .is-hidden"), getRoot());
        return this;
    }

    public NotificationRuleItem clickSaveButton() {
        waitForElementVisible(saveButton).click();
        return this;
    }

    public void deleteRule() {
        waitForElementVisible(deleteButton).click();
        waitForElementVisible(By.cssSelector(".icon-check + span"), getRoot()).click();
    }

    public NotificationRuleItem tryDeleteButCancel() {
        waitForElementVisible(deleteButton).click();
        waitForElementVisible(By.cssSelector(".icon-delete + span"), getRoot()).click();
        return this;
    }

    public void cancel() {
        waitForElementVisible(cancelButton).click();
    }

    private NotificationRuleItem enterData(WebElement input, String value) {
        waitForElementVisible(input).clear();
        input.sendKeys(value);
        return this;
    }

    private Variable getVariableByValue(String value) {
        return Stream.of(Variable.values()).filter(v -> v.getValue().equals(value)).findFirst().get();
    }

    private NotificationEvent getNotificationEventByValue(String value) {
        return Stream.of(NotificationEvent.values()).filter(e -> e.toString().equals(value)).findFirst().get();
    }

    public enum NotificationEvent {
        SUCCESS,
        FAILURE,
        PROCESS_SCHEDULED,
        PROCESS_STARTED,
        CUSTOM_EVENT;

        @Override
        public String toString() {
            return this.name().toLowerCase().replace("_", " ");
        }
    }
}
