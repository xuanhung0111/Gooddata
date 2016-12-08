package com.gooddata.qa.graphene.fragments.disc.schedule;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static java.lang.Integer.parseInt;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.arquillian.graphene.Graphene;
import org.joda.time.DateTime;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.enums.disc.ScheduleStatus;
import com.gooddata.qa.graphene.enums.disc.__Executable;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.disc.ConfirmationDialog;
import com.google.common.base.Predicate;

public class __ScheduleDetailFragment extends AbstractFragment {

    private static final By LOCATOR = By.className("ait-schedule-detail-fragment");
    private static final By BY_DISABLE_BUTTON = By.className("ait-schedule-disable-btn");

    @FindBy(className = "s-btn-run")
    private WebElement runButton;

    @FindBy(className = "execution-history-item")
    private List<__ExecutionHistoryItem> executionHistoryItems;

    @FindBy(className = "ait-schedule-executable-select-btn")
    private Select executableSelect;

    @FindBy(className = "button-positive")
    private WebElement saveButton;

    public static final __ScheduleDetailFragment getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(__ScheduleDetailFragment.class, waitForElementVisible(LOCATOR, searchContext))
                .waitForLoaded();
    }

    public boolean isDisabled() {
        return !isElementPresent(BY_DISABLE_BUTTON, getRoot());
    }

    public __ScheduleDetailFragment disableSchedule() {
        waitForElementVisible(BY_DISABLE_BUTTON, getRoot()).click();
        return this;
    }

    public __ScheduleDetailFragment executeSchedule() {
        int executionItems = executionHistoryItems.size();

        waitForElementVisible(runButton).click();
        ConfirmationDialog.getInstance(browser).confirm();

        Predicate<WebDriver> scheduleExecuted = browser -> executionHistoryItems.size() == executionItems + 1;
        Graphene.waitGui().until(scheduleExecuted);
        return this;
    }

    public __ScheduleDetailFragment waitForStatus(ScheduleStatus status) {
        Predicate<WebDriver> statusReached = browser ->
                getLastExecutionHistoryItem().getStatusDescription().equals(status.toString());
        Graphene.waitGui().until(statusReached);

        return this;
    }

    public __ScheduleDetailFragment waitForAutoExecute(DateTime startTime) {
        int executionItems = executionHistoryItems.size();

        while (DateTime.now().compareTo(startTime) < 0) {
            sleepTightInSeconds(3);
        }

        Predicate<WebDriver> autoExecutionTriggered = browser -> executionHistoryItems.size() == executionItems + 1;
        Graphene.waitGui().until(autoExecutionTriggered);
        return this;
    }

    public __ScheduleDetailFragment waitForExecutionFinish() {
        Predicate<WebDriver> executionFinished = browser -> {
            String executionStatus = getLastExecutionHistoryItem().getStatusDescription();

            return !executionStatus.equals(ScheduleStatus.SCHEDULED.toString()) &&
                    !executionStatus.equals(ScheduleStatus.RUNNING.toString());
        };

        Graphene.waitGui().until(executionFinished);
        return this;
    }

    public __ScheduleDetailFragment editExecutable(__Executable executable) {
        waitForElementVisible(executableSelect).selectByVisibleText(executable.getValue());
        waitForElementVisible(saveButton).click();
        return this;
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

    private __ScheduleDetailFragment waitForLoaded() {
        final By byHistoryLoadingItem = By.cssSelector(".history-section .loading");
        int waitingTimeInSecond = 30;

        try {
            waitForElementNotPresent(byHistoryLoadingItem, waitingTimeInSecond);

        // Sometimes history icon loading never end, so should refresh page until it disappears
        } catch (TimeoutException e) {
            Predicate<WebDriver> pageLoaded = browser -> {
                if (isElementPresent(byHistoryLoadingItem, browser)) {
                    browser.navigate().refresh();
                    waitForFragmentVisible(this);
                    return false;
                }
                return true;
            };
            Graphene.waitGui()
                    .withTimeout(5, TimeUnit.MINUTES)
                    .pollingEvery(waitingTimeInSecond, TimeUnit.SECONDS)
                    .until(pageLoaded);
        }

        return this;
    }

    public class __ExecutionHistoryItem extends AbstractFragment {

        @FindBy(className = "execution-history-item-description")
        private WebElement description;

        @FindBy(className = "execution-date")
        private WebElement executionDate;

        @FindBy(className = "execution-times")
        private WebElement executionTime;

        public String getStatusDescription() {
            return waitForElementVisible(description).getText();
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
