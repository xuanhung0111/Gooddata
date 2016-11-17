package com.gooddata.qa.graphene.fragments.disc.schedule;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static java.lang.Integer.parseInt;

import java.util.List;
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

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Predicate;

public class __ScheduleDetailFragment extends AbstractFragment {

    private static final By LOCATOR = By.className("ait-schedule-detail-fragment");
    private static final By BY_DISABLE_BUTTON = By.className("ait-schedule-disable-btn");

    @FindBy(className = "s-btn-run")
    private WebElement runButton;

    @FindBy(className = "s-btn-stop")
    private WebElement stopButton;

    @FindBy(className = "execution-history-item")
    private List<__ExecutionHistoryItem> executionHistoryItems;

    public static final __ScheduleDetailFragment getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(__ScheduleDetailFragment.class, waitForElementVisible(LOCATOR, searchContext))
                .waitForLoaded();
    }

    public boolean isDisabled() {
        return !isElementPresent(BY_DISABLE_BUTTON, getRoot());
    }

    public __ScheduleDetailFragment executeSchedule() {
        waitForElementVisible(runButton).click();
        waitForElementVisible(By.className("ait-schedule-run-confirm-btn"), browser).click();
        return this;
    }

    public __ScheduleDetailFragment waitForExecutionFinish() {
        waitForElementVisible(stopButton);
        waitForElementNotVisible(stopButton);
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

        public String getStatusDescription() {
            return waitForElementVisible(description).getText();
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
