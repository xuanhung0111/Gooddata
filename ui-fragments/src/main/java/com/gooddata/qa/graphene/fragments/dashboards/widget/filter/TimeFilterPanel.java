package com.gooddata.qa.graphene.fragments.dashboards.widget.filter;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.utils.ElementUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementEnabled;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class TimeFilterPanel extends AbstractFragment {

    private static final By LOCATOR = By.cssSelector(".yui3-c-tabtimefilterbase:not(.gdc-hidden)");

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

    @FindBy(css = "div.fromInput:not(.loading) input.input")
    private WebElement filterTimeFromInput;

    @FindBy(css = "div.toInput:not(.loading) input.input")
    private WebElement filterTimeToInput;

    @FindBy(css = "div.fromInput label.label")
    private WebElement fromLabel;

    @FindBy(css = "div.toInput label.label")
    private WebElement toLabel;

    @FindBy(css = ".s-btn-apply,.s-btn-add")
    private WebElement applyButton;

    public static TimeFilterPanel getInstance(SearchContext searchContext) {
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
        submit();
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
