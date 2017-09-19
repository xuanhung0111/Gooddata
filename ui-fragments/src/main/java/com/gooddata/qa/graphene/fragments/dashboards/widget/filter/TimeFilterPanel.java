package com.gooddata.qa.graphene.fragments.dashboards.widget.filter;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementEnabled;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.utils.ElementUtils;

public class TimeFilterPanel extends AbstractFragment {

    private static final By LOCATOR = By.cssSelector(".yui3-c-tabtimefilterbase:not(.gdc-hidden)");
    private static final By FROM_DATE_PICKER_ICON = By.cssSelector(".fromInput .datepicker-icon");
    private static final By TO_DATE_PICKER_ICON = By.cssSelector(".toInput .datepicker-icon");
    private static final By DATE_PICKER = By.cssSelector(".c-call:not(.gdc-hidden)");
    private static final By INTERVAL = By.cssSelector(".interval");
    private static final By BY_FISCAL_MESSAGE_LOADING = By.cssSelector(".fiscalMessageLoading:not(.gdc-hidden)");

    @FindBy(css = ".s-granularity span")
    private List<WebElement> dateGranularitys;

    @FindBy(css = ".timeline-main-content")
    private WebElement timelineContent;

    @FindBy(css = ".timeline .timelineitem")
    private List<WebElement> timeLineItems;

    @FindBy(css = ".timeline .timelineitem-selected")
    private List<WebElement> selectedTimelineItems;

    @FindBy(css = ".timeline .arrow-left")
    private WebElement leftArrow;

    @FindBy(css = ".fromInput:not(.loading) input")
    private WebElement filterTimeFromInput;

    @FindBy(css = ".toInput:not(.loading) input")
    private WebElement filterTimeToInput;

    @FindBy(css = ".s-btn-apply,.s-btn-add")
    private WebElement applyButton;

    public static TimeFilterPanel getInstance(SearchContext searchContext) {
        //wait for loading progress finished
        waitForElementNotPresent(BY_FISCAL_MESSAGE_LOADING);
        return Graphene.createPageFragment(TimeFilterPanel.class, waitForElementVisible(LOCATOR, searchContext));
    }

    public List<String> getSelectedTimelineItemNames() {
        waitForElementVisible(timelineContent);
        return selectedTimelineItems.stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    public String getFromValue() {
        return waitForElementVisible(filterTimeFromInput).getAttribute("value");
    }

    public String getToValue() {
        return waitForElementVisible(filterTimeToInput).getAttribute("value");
    }

    public TimeFilterPanel selectDateGranularity(final DateGranularity dateGranularity) {
        return selectTimeFilter(dateGranularitys, dateGranularity.toString());
    }

    public TimeFilterPanel selectTimeLine(final String timeLine) {
        waitForElementVisible(timelineContent);
        while (!isExistingTimeline(timeLine)) {
            if (!moveLeftOnTimeline()) {
                break;
            }
        }
        if (isExistingTimeline(timeLine)) {
            selectTimeFilter(timeLineItems, timeLine);
        } else {
            throw new NoSuchElementException("No value present");
        }
        return this;
    }

    public void changeValueByEnterFromDateAndToDate(String startTime, String endTime) {
        waitForElementVisible(filterTimeFromInput).clear();
        filterTimeFromInput.sendKeys(startTime);
        waitForElementVisible(filterTimeToInput).clear();
        filterTimeToInput.sendKeys(endTime);
        //click out of the filterTimeToInput to make it lost focus, So it is applied the input value in sendKeys
        waitForElementVisible(By.className("dateExample2"), getRoot()).click();
        submit();
    }

    public boolean isDatePickerIconNotPresent() {
        return (!isElementPresent(FROM_DATE_PICKER_ICON, getRoot()) && !isElementPresent(TO_DATE_PICKER_ICON, getRoot()));
    }

    public boolean isDatePickerNotPresent() {
        return !isElementPresent(DATE_PICKER, browser);
    }

    public TimeFilterPanel clickOnFromInput() {
        waitForElementVisible(filterTimeFromInput).click();
        return this;
    }

    public TimeFilterPanel clickOnToInput() {
        waitForElementVisible(filterTimeToInput).click();
        return this;
    }

    public boolean isFromToNotVisible() {
        return (!isElementVisible(INTERVAL, getRoot()));
    }

    public void submit() {
        waitForElementEnabled(applyButton).click();
    }

    public boolean isDateRangeVisible() {
        // ensure that the panel is loaded completely
        waitForElementVisible(applyButton);

        return ElementUtils.isElementVisible(filterTimeFromInput)
                && ElementUtils.isElementVisible(filterTimeToInput);
    }

    private TimeFilterPanel selectTimeFilter(final List<WebElement> timeFilters, final String time) {
        waitForCollectionIsNotEmpty(timeFilters)
                .stream()
                .filter(e -> time.equalsIgnoreCase(e.getText()))
                .findFirst()
                .get()
                .click();

        return this;
    }

    private boolean moveLeftOnTimeline() {
        boolean hasMoved = false;
        waitForElementVisible(timelineContent);
        if (leftArrow.isDisplayed()) {
            leftArrow.click();
            hasMoved = true;
        }
        return hasMoved;
    }

    private boolean isExistingTimeline(String timeline) {
        return timeLineItems.stream().filter(item -> item.getText().contains(timeline)).findFirst().isPresent();
    }

    public enum DateGranularity {
        YEAR,
        QUARTER,
        MONTH,
        WEEK,
        DAY;
    }
}
