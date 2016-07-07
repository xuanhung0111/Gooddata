package com.gooddata.qa.graphene.fragments.dashboards.widget.filter;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class TimeFilterPanel extends FilterPanel {

    @FindBy(css = ".s-granularity span")
    private List<WebElement> dateGranularitys;

    @FindBy(css = ".timeline .timelineitem")
    private List<WebElement> timeLineItems;

    @FindBy(css = "div.fromInput input.input")
    private WebElement filterTimeFromInput;

    @FindBy(css = "div.toInput input.input")
    private WebElement filterTimeToInput;

    @FindBy(css = "div.fromInput label.label")
    private WebElement fromLabel;

    public TimeFilterPanel selectDateGranularity(final DateGranularity dateGranularity) {
        return selectTimeFilter(dateGranularitys, dateGranularity.toString());
    }

    public TimeFilterPanel selectTimeLine(final String timeLine) {
        return selectTimeFilter(timeLineItems, timeLine);
    }

    public void changeValueByEnterFromDateAndToDate(String startTime, String endTime) {
        waitForElementVisible(filterTimeFromInput).clear();
        waitForElementVisible(filterTimeToInput).clear();
        filterTimeFromInput.sendKeys(startTime);
        filterTimeFromInput.click();
        filterTimeToInput.sendKeys(endTime);
        filterTimeToInput.click();
        submit();
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

    public enum DateGranularity {
        YEAR,
        QUARTER,
        MONTH,
        WEEK,
        DAY;
    }
}
