package com.gooddata.qa.graphene.fragments.disc.notification;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.google.common.collect.Iterables.getLast;

import java.util.Collection;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.disc.NotificationRule;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Predicate;

public class __NotificationRulesDialog extends AbstractFragment {

    private static final By LOCATOR = By.className("notification-rules-dialog");

    @FindBy(className = "empty-state")
    private WebElement emptyState;

    @FindBy(className = "ait-notification-rules-add-btn")
    private WebElement addNotificationRuleButton;

    @FindBy(className = "ait-notification-rule-list-item")
    private Collection<NotificationRuleItem> notificationRuleItems;

    @FindBy(className = "ait-notification-rules-close-btn")
    private WebElement closeButton;

    public static final __NotificationRulesDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(__NotificationRulesDialog.class,
                waitForElementVisible(LOCATOR, searchContext));
    }

    public String getEmptyStateMessage() {
        return waitForElementVisible(emptyState).getText().trim();
    }

    public NotificationRuleItem clickAddNotificationRule() {
        int notificationRuleNumber = getNotificationRuleNumber();
        waitForElementVisible(addNotificationRuleButton).click();

        Predicate<WebDriver> newRuleItemCreated = browser -> getNotificationRuleNumber() > notificationRuleNumber;
        Graphene.waitGui().until(newRuleItemCreated);

        return getLastNotificationRuleItem();
    }

    public __NotificationRulesDialog createNotificationRule(NotificationRule notificationRule) {
        clickAddNotificationRule().fillInformation(notificationRule).save();
        return this;
    }

    public int getNotificationRuleNumber() {
        return notificationRuleItems.size();
    }

    public NotificationRuleItem getLastNotificationRuleItem() {
        return getLast(notificationRuleItems);
    }

    public void closeDialog() {
        waitForElementVisible(closeButton).click();
        waitForFragmentNotVisible(this);
    }
}
