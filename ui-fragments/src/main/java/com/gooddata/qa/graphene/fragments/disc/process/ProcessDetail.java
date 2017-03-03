package com.gooddata.qa.graphene.fragments.disc.process;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.disc.ScheduleStatus;
import com.gooddata.qa.graphene.enums.disc.__Executable;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.disc.ConfirmationDialog;
import com.gooddata.qa.graphene.fragments.disc.notification.__NotificationRulesDialog;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.ProcessType;
import com.gooddata.qa.graphene.fragments.disc.schedule.CreateScheduleForm;
import com.gooddata.qa.graphene.fragments.disc.schedule.__ScheduleDetailFragment;

public class ProcessDetail extends AbstractFragment {

    private static final By BY_CREATE_NEW_SCHEDULE_LINK = By.className("action-important-link");

    @FindBy(className = "ait-process-title")
    private WebElement title;

    @FindBy(className = "ait-process-schedule-list-item")
    private Collection<WebElement> schedules;

    @FindBy(className = "ait-process-executable-list-item")
    private Collection<WebElement> executables;

    @FindBy(className = "ait-process-metadata-list-item")
    private Collection<WebElement> metadataObjects;

    @FindBy(className = "notification-rule-link")
    private WebElement notificationRuleLink;

    @FindBy(className = "ait-process-delete-btn")
    private WebElement deleteButton;

    @FindBy(className = "ait-process-download-btn")
    private WebElement downloadButton;

    @FindBy(className = "ait-process-redeploy-btn")
    private WebElement redeployButton;

    public ProcessDetail openTab(Tab tab) {
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

    public Collection<String> getExecutables() {
        return executables.stream().map(this::getExecutableTitle).collect(toList());
    }

    public String getScheduleInfoFrom(__Executable executable) {
        return getExecutableElement(executable).findElement(By.className("executable-schedules-cell")).getText();
    }

    public String getScheduleCronTime(String scheduleName) {
        return findSchedule(scheduleName).get().findElement(By.className("schedule-cron-cell")).getText();
    }

    public CreateScheduleForm clickCreateScheduleLink() {
        waitForElementVisible(BY_CREATE_NEW_SCHEDULE_LINK, getRoot()).click();
        return CreateScheduleForm.getInstance(browser);
    }

    public CreateScheduleForm clickScheduleLinkFrom(__Executable executable) {
        getExecutableElement(executable).findElement(By.cssSelector("a[class*='new-schedule-btn']")).click();
        return CreateScheduleForm.getInstance(browser);
    }

    public String getMetadata(String key) {
        return metadataObjects.stream()
                .filter(m -> key.equals(m.findElement(By.className("metadata-name")).getText()))
                .map(m -> m.findElement(By.className("metadata-value")))
                .map(WebElement::getText)
                .findFirst()
                .get();
    }

    public void downloadProcess() {
        waitForElementVisible(downloadButton).click();
    }

    public String getTitle() {
        return waitForElementVisible(title).getText();
    }

    public ProcessDetail redeployWithZipFile(String processName, ProcessType processType, File packageFile) {
        waitForElementVisible(redeployButton).click();
        DeployProcessForm.getInstance(browser).deployProcessWithZipFile(processName, processType, packageFile);
        return this;
    }

    public ConfirmationDialog clickDeleteButton() {
        waitForElementVisible(deleteButton).click();
        return ConfirmationDialog.getInstance(browser);
    }

    public void deleteProcess() {
        clickDeleteButton().confirm();
    }

    public ScheduleStatus getScheduleStatus(String scheduleName) {
        return Stream.of(ScheduleStatus.values())
                .filter(status -> isElementPresent(status.getIconByCss(), findSchedule(scheduleName).get()))
                .findFirst()
                .get();
    }

    public __ScheduleDetailFragment openSchedule(String scheduleName) {
        findSchedule(scheduleName).get().findElement(By.cssSelector(".schedule-title-cell a")).click();
        return __ScheduleDetailFragment.getInstance(browser);
    }

    public String getBrokenScheduleMessage() {
        return waitForElementVisible(By.cssSelector(".broken-schedules-section .message"), getRoot()).getText();
    }

    public __NotificationRulesDialog openNotificationRuleDialog() {
        waitForElementVisible(notificationRuleLink).click();
        return __NotificationRulesDialog.getInstance(browser);
    }

    public String getNotificationRuleDescription() {
        return waitForElementVisible(notificationRuleLink).getText();
    }

    private Optional<WebElement> findSchedule(String scheduleName) {
        return schedules.stream()
                .filter(s -> scheduleName.equals(s.findElement(By.tagName("a")).getText()))
                .findFirst();
    }

    private WebElement getExecutableElement(__Executable executable) {
        return executables.stream()
                .filter(e -> executable.getPath().equals(getExecutableTitle(e)))
                .findFirst()
                .get();
    }

    private String getExecutableTitle(WebElement executable) {
        return waitForElementVisible(executable).findElement(By.className("executable-title-cell"))
                .getText().replace(" ", "");
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
