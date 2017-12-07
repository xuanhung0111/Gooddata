package com.gooddata.qa.graphene.fragments.dashboards.widget.filter;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.Objects;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementEnabled;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class DayTimeFilterPanel extends AbstractFragment {
    private static final By LOCATOR = By.cssSelector(".yui3-c-tabtimefilterbase:not(.gdc-hidden)");
    private static final By BY_FISCAL_MESSAGE_LOADING = By.cssSelector(".fiscalMessageLoading:not(.gdc-hidden)");
    private static final By BY_INPUT_CUSTOM_DAY = By.cssSelector(".yui3-c-textbox-content input.gdc-input");

    @FindBy(css = ".custom.fake")
    private WebElement customDayLabel;

    @FindBy(css = ".inline.cuts .c-label")
    private List<WebElement> dayAgo;

    @FindBy(css = ".fromInput:not(.loading) input")
    private WebElement fromDateInput;

    @FindBy(css = ".dateExample:not(.loading)")
    private WebElement fromDateDescription;

    @FindBy(css = ".dateExample2:not(.loading)")
    private WebElement toDateDescription;

    @FindBy(css = ".toInput:not(.loading) input")
    private WebElement toDateInput;

    @FindBy(css = ".s-btn-apply,.s-btn-add")
    private WebElement applyButton;

    public static DayTimeFilterPanel getInstance(SearchContext searchContext) {
        //wait for loading progress finished
        waitForElementNotPresent(BY_FISCAL_MESSAGE_LOADING);
        return Graphene.createPageFragment(DayTimeFilterPanel.class, waitForElementVisible(LOCATOR, searchContext));
    }

    public DayTimeFilterPanel setCustomDays(final int days) {
        waitForElementVisible(customDayLabel).click();
        WebElement we = waitForElementVisible(BY_INPUT_CUSTOM_DAY, this.getRoot());
        we.click();
        we.clear();
        we.sendKeys(String.valueOf(days));
        we.sendKeys(Keys.ENTER);
        return this;
    }

    public DayTimeFilterPanel selectLast(DayAgo day) {
        waitForCollectionIsNotEmpty(dayAgo).stream()
                .filter(e -> Objects.equals(day.toString(), e.getText())).findFirst().get().click();
        return this;
    }

    public DayAgo getSelectedDay() {
        String value = waitForCollectionIsNotEmpty(dayAgo).stream()
                .filter(e -> e.getAttribute("class").contains("yui3-c-label-selected")).findFirst().get().getText();
        return DayAgo.instanceOf(Integer.parseInt(value));
    }

    public void submit() {
        waitForElementEnabled(applyButton).click();
    }

    public String getFromDateValue() {
        return waitForElementVisible(fromDateInput).getAttribute("value");
    }

    public String getFromDateDescription() {
        return waitForElementVisible(fromDateDescription).getText();
    }

    public String getToDateValue() {
        return waitForElementVisible(toDateInput).getAttribute("value");
    }

    public String getToDateDescription() {
        return waitForElementVisible(toDateDescription).getText();
    }

    public enum DayAgo {
        LAST_7_DAYS(7),
        LAST_30_DAYS(30),
        LAST_60_DAYS(60),
        LAST_90_DAYS(90),
        LAST_120_DAYS(120),
        LAST_365_DAYS(365);

        private int value;

        DayAgo(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        public static DayAgo instanceOf(int val) {
            switch (val) {
                case 7:
                    return LAST_7_DAYS;
                case 30:
                    return LAST_30_DAYS;
                case 60:
                    return LAST_60_DAYS;
                case 90:
                    return LAST_90_DAYS;
                case 120:
                    return LAST_120_DAYS;
                case 365:
                    return LAST_365_DAYS;
                default:
                    throw new RuntimeException("Day is not defined");
            }
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
}
