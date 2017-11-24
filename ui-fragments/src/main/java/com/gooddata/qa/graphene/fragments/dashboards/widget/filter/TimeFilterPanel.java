package com.gooddata.qa.graphene.fragments.dashboards.widget.filter;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementEnabled;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import com.gooddata.qa.graphene.entity.filter.TimeRange;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.SelectTimeRangePanel;
import com.gooddata.qa.graphene.utils.ElementUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

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

    @FindBy(css = ".timeline .arrow-right")
    private WebElement rightArrow;

    @FindBy(css = ".fromInput:not(.loading) input")
    private WebElement filterTimeFromInput;

    @FindBy(css = ".toInput:not(.loading) input")
    private WebElement filterTimeToInput;

    @FindBy(css = ".s-btn-apply,.s-btn-add")
    private WebElement applyButton;

    @FindBy(className = "example")
    private WebElement preview;

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

    public List<String> getVisibleTimelines() {
        waitForElementVisible(timelineContent);
        return getElementTexts(timeLineItems);
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

    public TimeFilterPanel selectRange(final String startTime, final String endTime) {
        waitForElementVisible(timelineContent);

        while (!isExistingTimeline(startTime)) {
            if (!moveLeftOnTimeline()) {
                break;
            }
        }

        WebElement startTimeElement = getTimeLineElement(startTime);

        startTimeElement.click();
        WebElement moveButton;
        while ((moveButton = getMoveButton(endTime)) != null) {
            moveButton.click();
        }
        WebElement endTimeElement = getTimeLineElement(endTime);
        try {
            getActions().keyDown(Keys.SHIFT).perform();
            endTimeElement.click();
        } finally {
            getActions().keyUp(Keys.SHIFT).perform();
            return this;
        }
    }

    public TimeFilterPanel setValueFromDateAndToDateByAdvance(TimeRange timeFrom, TimeRange timeTo) {
        getTimeRangePanel().setTimeRange(timeFrom, timeTo);
        return this;
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

    public boolean isDatePickerIconPresent() {
        return (isElementPresent(FROM_DATE_PICKER_ICON, getRoot()) && isElementPresent(TO_DATE_PICKER_ICON, getRoot()));
    }

    public boolean isDatePickerPresent() {
        return isElementPresent(DATE_PICKER, browser);
    }

    public TimeFilterPanel clickOnFromInput() {
        waitForElementVisible(filterTimeFromInput).click();
        return this;
    }

    public TimeFilterPanel clickOnToInput() {
        waitForElementVisible(filterTimeToInput).click();
        return this;
    }

    public TimeFilterPanel moveLeftOnTimelines() {
        if (!canMoveLeftOnTimeline()) throw new RuntimeException("Reached to limit range");
        leftArrow.click();
        return this;
    }

    public TimeFilterPanel moveRightOnTimelines() {
        if (!canMoveRightOnTimeline()) throw new RuntimeException("Reached to limit range");
        rightArrow.click();
        return this;
    }

    public boolean isFromToNotVisible() {
        return (!isElementVisible(INTERVAL, getRoot()));
    }

    public void submit() {
        // submit changes on grouped time filter
        if(isElementPresent(By.className("s-btn-default"), getRoot())) {
            if(!isElementVisible(By.className("s-btn-apply"), getRoot())
                    && !isElementVisible((By.className("s-btn-cancel")), getRoot())) {
                waitForElementVisible(By.cssSelector(".inline.dateExample"), getRoot()).sendKeys(Keys.ESCAPE);
                return;
            }
        }

        waitForElementEnabled(applyButton).click();
    }

    public boolean isDateRangeVisible() {
        // ensure that the panel is loaded completely
        waitForElementVisible(applyButton);

        return ElementUtils.isElementVisible(filterTimeFromInput)
                && ElementUtils.isElementVisible(filterTimeToInput);
    }

    public boolean canMoveLeftOnTimeline() {
        return isElementVisible(leftArrow);
    }

    public boolean canMoveRightOnTimeline() {
        return isElementVisible(rightArrow);
    }

    public String getPreviewValue() {
        return waitForElementVisible(preview).getText().substring(9);
    }

    private SelectTimeRangePanel getTimeRangePanel() {
        return SelectTimeRangePanel.getInstance(By.className("advanced"), browser);
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

    private WebElement getMoveButton(final String timeLine) {
        if (isExistingTimeline(timeLine)) {
            return null;
        }

        WebElement firstElement = timeLineItems.get(0);
        if (timeLine.compareTo(firstElement.getText()) < 0) {
            //move left
            return leftArrow.isDisplayed() ? leftArrow : null;
        } else {
            //move right
            return rightArrow.isDisplayed() ? rightArrow : null;
        }
    }

    private WebElement getTimeLineElement(final String timeValue) {
        waitForCollectionIsNotEmpty(timeLineItems);
        return timeLineItems.stream()
                .filter(e -> timeValue.equalsIgnoreCase(e.getText()))
                .findFirst()
                .get();
    }

    public enum DateGranularity {
        YEAR,
        QUARTER,
        MONTH,
        WEEK,
        DAY;
    }
}
