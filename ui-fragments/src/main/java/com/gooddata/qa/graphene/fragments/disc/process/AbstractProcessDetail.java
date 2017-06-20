package com.gooddata.qa.graphene.fragments.disc.process;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.disc.schedule.ScheduleStatus;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.disc.notification.NotificationRulesDialog;
import com.gooddata.qa.graphene.fragments.disc.schedule.CreateScheduleForm;

public class AbstractProcessDetail extends AbstractFragment {

    private static final By BY_CREATE_NEW_SCHEDULE_LINK = By.className("action-important-link");

    @FindBy(className = "ait-process-title")
    private WebElement title;

    @FindBy(className = "ait-process-schedule-list-item")
    private Collection<WebElement> schedules;

    @FindBy(className = "ait-process-metadata-list-item")
    private Collection<WebElement> metadataObjects;

    @FindBy(className = "notification-rule-link")
    private WebElement notificationRuleLink;

    public String getTitle() {
        return waitForElementVisible(title).getText();
    }

    public AbstractProcessDetail openTab(Tab tab) {
        waitForElementVisible(tab.getLocator(), getRoot()).click();
        return this;
    }

    public boolean isTabActive(Tab tab) {
        return waitForElementVisible(tab.getLocator(), getRoot()).getAttribute("class").contains("active");
    }

    public String getTabTitle(Tab tab) {
        return waitForElementVisible(tab.getLocator(), getRoot()).getText();
    }

    public boolean hasNoSchedule() {
        return isElementPresent(BY_CREATE_NEW_SCHEDULE_LINK, getRoot());
    }

    public boolean hasSchedule(String scheduleName) {
        return findSchedule(scheduleName).isPresent();
    }

    public String getScheduleCronTime(String scheduleName) {
        return findSchedule(scheduleName).get().findElement(By.className("schedule-cron-cell")).getText();
    }

    public CreateScheduleForm clickCreateScheduleLink() {
        waitForElementVisible(BY_CREATE_NEW_SCHEDULE_LINK, getRoot()).click();
        return CreateScheduleForm.getInstance(browser);
    }

    public ScheduleStatus getScheduleStatus(String scheduleName) {
        return Stream.of(ScheduleStatus.values())
                .filter(status -> isElementPresent(status.getIconByCss(), findSchedule(scheduleName).get()))
                .findFirst()
                .get();
    }

    public String getBrokenScheduleMessage() {
        return waitForElementVisible(By.cssSelector(".broken-schedules-section .message"), getRoot()).getText();
    }

    public String getMetadata(String key) {
        return metadataObjects.stream()
                .filter(m -> key.equals(m.findElement(By.className("metadata-name")).getText()))
                .map(m -> m.findElement(By.className("metadata-value")))
                .map(WebElement::getText)
                .findFirst()
                .get();
    }

    public NotificationRulesDialog openNotificationRuleDialog() {
        waitForElementVisible(notificationRuleLink).click();
        return NotificationRulesDialog.getInstance(browser);
    }

    public String getNotificationRuleDescription() {
        return waitForElementVisible(notificationRuleLink).getText();
    }

    protected Optional<WebElement> findSchedule(String scheduleName) {
        return schedules.stream()
                .filter(s -> scheduleName.equals(s.findElement(By.tagName("a")).getText()))
                .findFirst();
    }

    public enum Tab {
        SCHEDULE("ait-process-schedules-btn"),
        EXECUTABLE("ait-process-executables-btn"),
        METADATA("ait-process-metadata-btn");

        private String locator;

        private Tab(String locator) {
            this.locator = locator;
        }

        public By getLocator() {
            return By.className(locator);
        }
    }
}
