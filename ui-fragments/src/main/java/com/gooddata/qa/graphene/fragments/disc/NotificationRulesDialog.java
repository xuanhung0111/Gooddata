package com.gooddata.qa.graphene.fragments.disc;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import static com.gooddata.qa.graphene.utils.CheckUtils.*;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Predicate;

public class NotificationRulesDialog extends AbstractFragment {

    @FindBy(xpath = "//form[contains(@class, 'ait-notification-rule-list-item')]")
    private List<NotificationRule> notificationRules;

    @FindBy(css = ".ait-notification-rules-empty-state")
    private WebElement notificationRulesEmptyState;

    @FindBy(css = ".ait-notification-rules-add-btn")
    private WebElement addNotificationRuleButton;

    @FindBy(css = ".ait-notification-rules-close-btn")
    private WebElement closeNotificationRulesButton;

    @FindBy(css = ".ait-notification-rule-list-item-params ul li")
    private List<WebElement> availableParams;

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
        return notificationRules.size();
    }

    public void closeNotificationRulesDialog() {
        waitForElementVisible(closeNotificationRulesButton).click();
        waitForElementNotPresent(getRoot());
    }

    public NotificationRule getNotificationRule(int index) {
        waitForCollectionIsNotEmpty(notificationRules);
        return notificationRules.get(index);
    }
}
