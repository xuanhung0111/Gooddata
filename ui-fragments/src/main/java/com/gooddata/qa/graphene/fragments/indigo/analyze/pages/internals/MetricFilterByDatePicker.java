package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import com.gooddata.qa.graphene.fragments.common.AbstractPicker;
import com.gooddata.qa.graphene.fragments.indigo.analyze.DateDimensionSelect;
import com.gooddata.qa.graphene.utils.ElementUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.function.Function;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementDisabled;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.utils.CssUtils.simplifyText;

public class MetricFilterByDatePicker extends AbstractPicker {

    @FindBy(css = ".adi-date-input-from input")
    private WebElement fromDateInput;

    @FindBy(css = ".adi-date-input-from > span")
    private WebElement fromDateCalendarIcon;

    @FindBy(css = ".adi-date-input-to input")
    private WebElement toDateInput;

    @FindBy(css = ".adi-date-dataset-select-dropdown button")
    private WebElement dateDimension;

    @FindBy(className = "s-date-range-cancel")
    private WebElement cancelButton;

    @FindBy(className = "s-date-range-apply")
    private WebElement applyButton;

    @FindBy(className = "adi-date-range-picker-content")
    private WebElement dateRangePicker;

    @FindBy(className = "icon-navigateleft")
    private WebElement iconBackToOtherPeriods;

    @FindBy(css = "div[aria-disabled='false']")
    private List<WebElement> allDaysInMonth;

    public static final String STATIC_PERIOD_DROPDOWN_ITEM = "Static period";
    private static final By BY_BUBBLE_CONTENT = By.className("bubble-content");

    @Override
    protected String getListItemsCssSelector() {
        return ".gd-list-item";
    }

    @Override
    protected void waitForPickerLoaded() {}

    @Override
    protected WebElement getElementByName(final String name) {
        //Prevent to same attribute name
        return getElement(".s-" + simplifyText(name) + ":not(.is-selected)");
    }

    public WebElement getFromDateInput() { return waitForElementVisible(fromDateInput); }

    public WebElement getToDateInput() { return waitForElementVisible(toDateInput); }

    public WebElement fromDateCalendarIcon() {
        return waitForElementVisible(fromDateCalendarIcon);
    }

    public MetricFilterByDatePicker expandDateDimension() {
        if (!isConfigurationExpanded()) {
            waitForElementVisible(dateDimension).click();
        }
        return this;
    }

    public MetricFilterByDatePicker selectDayInCalendar(String dayOfMonth) {
        allDaysInMonth.stream().filter(e -> e.getText().equalsIgnoreCase(dayOfMonth)).findFirst().get().click();
        return this;
    }

    private boolean isConfigurationExpanded() {
        return waitForElementVisible(dateDimension).getAttribute("class").contains("is-active");
    }

    public MetricFilterByDatePicker selectStaticPeriod() { selectDateFilter(STATIC_PERIOD_DROPDOWN_ITEM); return this; }

    public MetricFilterByDatePicker selectDateFilter(String dateRange) {
        getElementByName(dateRange).click();
        return this;
    }

    public MetricFilterByDatePicker openFromDateCalendar() {
        waitForElementVisible(fromDateInput).click();
        return this;
    }

    public MetricFilterByDatePicker openToDateCalendar() {
        waitForElementVisible(toDateInput).click();
        return this;
    }

    public MetricFilterByDatePicker backToOtherPeriods() {
        makeSureNoPopupDateFilterVisible();
        waitForElementVisible(iconBackToOtherPeriods).click();
        return this;
    }

    public String getDateDimension() {
        return waitForElementVisible(dateDimension).getText();
    }

    public String getFromDate() {
        return waitForElementVisible(fromDateInput).getAttribute("value");
    }

    public String getToDate() {
        return waitForElementVisible(toDateInput).getAttribute("value");
    }

    public boolean isDateRangePickerVisible() {
        return isElementVisible(dateRangePicker);
    }

    public boolean isApplyButtonEnabled() {
        return !isElementDisabled(waitForElementVisible(applyButton));
    }

    public boolean isToCalendarPickerVisible() {
        return isElementVisible(By.cssSelector(".adi-date-input-to .gd-datepicker-picker"), getRoot());
    }

    public boolean isFromCalendarPickerVisible() {
        return isElementVisible(By.cssSelector(".adi-date-input-from .gd-datepicker-picker"), getRoot());
    }

    public MetricFilterByDatePicker changeDateDimension(String switchDimension) {
        getDateDatasetSelect().selectByName(switchDimension);
        return this;
    }

    public DateDimensionSelect getDateDatasetSelect() {
        return Graphene.createPageFragment(DateDimensionSelect.class,
                waitForElementVisible(By.cssSelector(".adi-date-dataset-select-dropdown div"), browser));
    }

    public MetricFilterByDatePicker configTimeFilter(String from, String to) {
        configTimeFilterByRangeHelper(from, to, true);
        return this;
    }

    public void tryToConfigTimeFilterByRange(String from, String to) {
        configTimeFilterByRangeHelper(from, to, false);
    }

    public void fillInDateRange(WebElement dateInput, String date) {
        dateInput.sendKeys(Keys.END);
        int length = dateInput.getAttribute("value").length();
        for (int i = 0; i <= length; i++) {
            dateInput.sendKeys(Keys.BACK_SPACE);
        }
        dateInput.sendKeys(date, Keys.ENTER);
    }

    private MetricFilterByDatePicker configTimeFilterByRangeHelper(String from, String to, boolean apply) {
        selectStaticPeriod();

        fillInDateRange(waitForElementVisible(fromDateInput), from);
        waitForElementVisible(fromDateCalendarIcon).click();

        fillInDateRange(waitForElementVisible(toDateInput), to);
        waitForElementVisible(fromDateCalendarIcon).click();

        waitForElementVisible(apply ? applyButton : cancelButton).click();
        waitForFragmentNotVisible(this);

        return this;
    }

    private void makeSureNoPopupDateFilterVisible() {
        // Click to icon From Date input to make sure no popup is displayed
        ElementUtils.moveToElementActions(waitForElementPresent(fromDateCalendarIcon), 1, 1).click().perform();

        Function<WebDriver, Boolean> isDismissed = context -> !isElementVisible(ElementUtils.BY_BUBBLE_CONTENT, context);
        Graphene.waitGui().until(isDismissed);
    }
}
