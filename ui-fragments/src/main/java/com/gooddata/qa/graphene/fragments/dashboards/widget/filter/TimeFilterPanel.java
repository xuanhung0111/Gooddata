package com.gooddata.qa.graphene.fragments.dashboards.widget.filter;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class TimeFilterPanel extends AbstractFragment {

    private static final By LOCATOR = By.cssSelector(".yui3-c-tabtimefilterbase:not(.gdc-hidden)");

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

    @FindBy(css = ".s-btn-apply,.s-btn-add")
    private WebElement applyButton;

    public static TimeFilterPanel getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(TimeFilterPanel.class, waitForElementVisible(LOCATOR, searchContext));
    }

    public TimeFilterPanel selectDateGranularity(final DateGranularity dateGranularity) {
        return selectTimeFilter(dateGranularitys, dateGranularity.toString());
    }

    public TimeFilterPanel selectTimeLine(final String timeLine) {
        return selectTimeFilter(timeLineItems, timeLine);
    }

    public void changeValueByEnterFromDateAndToDate(String startTime, String endTime) {
        waitForElementVisible(filterTimeFromInput).clear();
        filterTimeFromInput.sendKeys(startTime);
        waitForElementVisible(filterTimeToInput).clear();
        filterTimeToInput.sendKeys(endTime);
        submit();
    }

    public void submit() {
        waitForElementVisible(applyButton).click();
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
