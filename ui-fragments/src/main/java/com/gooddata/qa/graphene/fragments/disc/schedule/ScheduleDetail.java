package com.gooddata.qa.graphene.fragments.disc.schedule;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.getTooltipFromElement;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static java.lang.Integer.parseInt;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.enums.disc.schedule.ScheduleStatus;
import com.gooddata.qa.graphene.enums.disc.schedule.Executable;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.disc.ConfirmationDialog;
import com.google.common.base.Predicate;

public class ScheduleDetail extends AbstractScheduleFragment {

    private static final By LOCATOR = By.className("ait-schedule-detail-fragment");
    private static final By BY_DISABLE_BUTTON = By.className("ait-schedule-disable-btn");
    private static final By BY_NAME_INPUT = By.cssSelector(".ait-schedule-title-field input");

    @FindBy(className = "abbreviate-schedule-title")
    private WebElement title;

    @FindBy(className = "s-btn-run")
    private WebElement runButton;

    @FindBy(className = "s-btn-stop")
    private WebElement stopButton;

    @FindBy(className = "s-btn-delete")
    private WebElement deleteButton;

    @FindBy(className = "close-button")
    private WebElement closeButton;

    @FindBy(className = "ait-schedule-enable-btn")
    private WebElement enableButton;

    @FindBy(className = "ait-schedule-reschedule-add-btn")
    private WebElement addRetryDelayLink;

    @FindBy(css = ".reschedule-text input")
    private WebElement retryDelayInput;

    @FindBy(className = "execution-history-item")
    private List<__ExecutionHistoryItem> executionHistoryItems;

    public static final ScheduleDetail getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(ScheduleDetail.class, waitForElementVisible(LOCATOR, searchContext))
                .waitForLoaded();
    }

    public static final boolean isVisible(SearchContext searchContext) {
        return isElementVisible(LOCATOR, searchContext);
    }

    public void close() {
        waitForElementVisible(closeButton).click();
        waitForFragmentNotVisible(this);
    }

    public ScheduleDetail editNameByClickOnTitle(String name) {
        waitForElementVisible(title).click();
        return editName(name);
    }

    public ScheduleDetail editNameByClickOnEditIcon(String name) {
        waitForElementVisible(By.className("icon-edit"), getRoot()).click();
        return editName(name);
    }

    public boolean isNameInputError() {
        return waitForElementVisible(BY_NAME_INPUT, getRoot()).getAttribute("class").contains("has-error");
    }

    public String getName() {
        return waitForElementVisible(title).getText();
    }

    public ScheduleDetail replaceBrokenExecutableWith(Executable validExecutable) {
        new Select(waitForElementVisible(By.cssSelector(".broken-schedule-info select"), getRoot()))
                .selectByVisibleText(validExecutable.getPath());
        return this;
    }

    public boolean isDisabled() {
        return !isElementPresent(BY_DISABLE_BUTTON, getRoot());
    }

    public ScheduleDetail disableSchedule() {
        waitForElementVisible(BY_DISABLE_BUTTON, getRoot()).click();
        return this;
    }

    public String getDisabledMessage() {
        return waitForElementVisible(By.className("disabled-info"), getRoot()).getText();
    }

    public String getDisableRecommendMessage() {
        return waitForElementVisible(By.className("info-section"), getRoot()).getText();
    }

    public String getAutoDisabledMessage() {
        return waitForElementVisible(By.cssSelector(".ait-schedule-disabled .message p"), getRoot()).getText();
    }

    public String getBrokenScheduleMessage() {
        return waitForElementVisible(By.cssSelector("p.broken-schedule-info"), getRoot()).getText();
    }

    public String getTriggeringScheduleErrorMessage() {
        return waitForElementVisible(By.className("error-trigger-message"), getRoot()).getText();
    }

    public String getEffectiveUser() {
        return waitForElementVisible(By.cssSelector(".ait-schedule-executable-section span strong"), getRoot()).getText();
    }

    public ScheduleDetail enableSchedule() {
        waitForElementVisible(enableButton).click();
        return this;
    }

    public ScheduleDetail executeSchedule() {
        int executionItems = executionHistoryItems.size();

        waitForElementVisible(runButton).click();
        ConfirmationDialog.getInstance(browser).confirm();

        Predicate<WebDriver> scheduleExecuted = browser -> executionHistoryItems.size() == executionItems + 1;
        Graphene.waitGui().until(scheduleExecuted);
        return this;
    }

    public ScheduleDetail stopExecution() {
        waitForElementVisible(stopButton).click();
        ConfirmationDialog.getInstance(browser).confirm();
        return this;
    }

    public void deleteSchedule() {
        clickDeleteScheduleButton().confirm();
        waitForFragmentNotVisible(this);
    }

    public ConfirmationDialog clickDeleteScheduleButton() {
        waitForElementVisible(deleteButton).click();
        return ConfirmationDialog.getInstance(browser);
    }

    public ScheduleDetail saveChanges() {
        clickSaveButton();

        Predicate<WebDriver> saved = browser -> !findSaveButtonsGroup().isPresent();
        Graphene.waitGui().until(saved);

        return this;
    }

    public ScheduleDetail cancelChanges() {
        waitForElementVisible(By.className("button-secondary"), findSaveButtonsGroup().get()).click();
        return this;
    }

    public ScheduleDetail clickSaveButton() {
        waitForElementVisible(By.className("button-positive"), findSaveButtonsGroup().get()).click();
        return this;
    }

    public ScheduleDetail waitForStatus(ScheduleStatus status) {
        Predicate<WebDriver> statusReached = browser ->
                getLastExecutionHistoryItem().getStatusDescription().equals(status.toString());
        Graphene.waitGui().until(statusReached);

        return this;
    }

    public ScheduleDetail waitForAutoExecute(LocalTime startTime) {
        if (!canAutoTriggered(startTime)) {
            throw new RuntimeException("Schedule execution not triggered by auto!");
        }

        return this;
    }

    public boolean canAutoTriggered(LocalTime startTime) {
        int executionItems = executionHistoryItems.size();

        while (LocalTime.now().compareTo(startTime) < 0) {
            sleepTightInSeconds(3);
        }

        try {
            Predicate<WebDriver> autoExecutionTriggered = browser -> executionHistoryItems.size() == executionItems + 1;
            Graphene.waitGui().until(autoExecutionTriggered);
            return true;

        } catch (TimeoutException e) {
            return false;
        }
    }

    public ScheduleDetail waitForExecutionFinish() {
        Predicate<WebDriver> executionFinished = browser -> {
            String executionStatus = getLastExecutionHistoryItem().getStatusDescription();

            return !executionStatus.equals(ScheduleStatus.SCHEDULED.toString()) &&
                    !executionStatus.equals(ScheduleStatus.RUNNING.toString());
        };

        Graphene.waitGui().withTimeout(5, TimeUnit.MINUTES)
                .pollingEvery(2, TimeUnit.SECONDS).until(executionFinished);
        return this;
    }

    public ScheduleDetail addRetryDelay(int retryInMinute) {
        waitForElementVisible(addRetryDelayLink).click();

        waitForElementVisible(retryDelayInput).clear();
        retryDelayInput.sendKeys(String.valueOf(retryInMinute));

        return this;
    }

    public boolean isRetryDelayInputError() {
        return waitForElementVisible(retryDelayInput).getAttribute("class").contains("has-error");
    }

    public boolean hasRetryDelay() {
        return isElementPresent(By.className("reschedule-form"), getRoot());
    }

    public int getRetryDelayValue() {
        return parseInt(waitForElementVisible(retryDelayInput).getAttribute("value"));
    }

    public ScheduleDetail deleteRetryDelay() {
        clickDeleteRetryDelay().confirm();
        return this;
    }

    public ConfirmationDialog clickDeleteRetryDelay() {
        waitForElementVisible(By.className("ait-schedule-reschedule-delete-btn"), getRoot()).click();
        return ConfirmationDialog.getInstance(browser);
    }

    public String getExecutionHistoryEmptyMessage() {
        return waitForElementVisible(By.className("ait-execution-history-empty"), getRoot()).getText();
    }

    public String getExecutionTimelineTooltip() {
        return getTooltipFromElement(By.cssSelector(".execution-timeline .execution"), browser);
    }

    public int getExecutionHistoryItemNumber() {
        return executionHistoryItems.stream()
                .map(item -> item.getExecutionNumber())
                .mapToInt(Integer::intValue)
                .sum();
    }

    public __ExecutionHistoryItem getLastExecutionHistoryItem() {
        return executionHistoryItems.get(0);
    }

    public String getLastExecutionDateTime() {
        return getLastExecutionHistoryItem().getExecutionDateTime();
    }

    public String getLastExecutionLogUri() {
        return getLastExecutionHistoryItem().getExecutionLogUri();
    }

    private ScheduleDetail waitForLoaded() {
        waitForElementNotPresent(By.className("loading"), getRoot());
        return this;
    }

    private ScheduleDetail editName(String name) {
        WebElement nameInput = waitForElementVisible(BY_NAME_INPUT, getRoot());
        nameInput.clear();
        nameInput.sendKeys(name);

        return this;
    }

    // Use this action to identify the visibility of save buttons group on schedule detail after edit some information 
    // such as name, executable, cron time,...
    // More flexibility when save changes make in any context instead of write own method for each one  
    private Optional<WebElement> findSaveButtonsGroup() {
        return getRoot().findElements(By.cssSelector("[class*='buttons']"))
                .stream()
                .filter(e -> !e.getAttribute("class").contains("is-hidden") &&
                        !isElementPresent(By.className("is-hidden"), e))
                .findFirst();
    }

    public class __ExecutionHistoryItem extends AbstractFragment {

        @FindBy(className = "execution-history-item-description")
        private WebElement description;

        @FindBy(className = "execution-date")
        private WebElement executionDate;

        @FindBy(className = "execution-times")
        private WebElement executionTime;

        @FindBy(css = ".execution-log a")
        private WebElement executionLog;

        public String getStatusDescription() {
            return waitForElementVisible(description).getText();
        }

        public String getErrorMessage() {
            return waitForElementVisible(By.className("execution-history-error"), getRoot()).getText();
        }

        public boolean isItemGroup() {
            return getRoot().getAttribute("class").contains("group-header");
        }

        public String getExecutionLogUri() {
            return waitForElementVisible(executionLog).getAttribute("href");
        }

        private String getExecutionDateTime() {
            return waitForElementVisible(executionDate).getText() + " " +
                    waitForElementVisible(executionTime).getText().split("-")[1].trim();
        }

        private int getExecutionNumber() {
            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = pattern.matcher(getStatusDescription());

            if (matcher.find()) {
                return parseInt(matcher.group());
            }

            return 1;
        }
    }
}
