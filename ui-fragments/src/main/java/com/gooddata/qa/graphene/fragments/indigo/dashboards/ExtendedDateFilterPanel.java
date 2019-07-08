package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementEnabled;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static java.lang.String.format;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;

import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.utils.ElementUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.Keys;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.stream.Stream;

public class ExtendedDateFilterPanel extends AbstractFragment {

    @FindBy(css = ".s-date-range-picker-from .input-text")
    private WebElement fromDate;

    @FindBy(css = ".s-date-range-picker-to .input-text")
    private WebElement toDate;

    @FindBy(css = ".s-relative-range-picker-from input")
    private WebElement fromRange;

    @FindBy(css = ".s-relative-range-picker-to input")
    private WebElement toRange;

    @FindBy(className = "s-exclude-current-period")
    private WebElement excludeCurrentPeriod;

    @FindBy(className = "s-cancel")
    private WebElement cancelButton;

    @FindBy(className = "s-apply")
    private WebElement applyButton;

    @FindBy(css = ".gd-extended-date-filter-body-scrollable button")
    private List<WebElement> filters;

    @FindBy(className = "gd-filter-list-item-selected")
    private WebElement selectedFilter;

    public static ExtendedDateFilterPanel getInstance(SearchContext context) {
        return Graphene.createPageFragment(ExtendedDateFilterPanel.class,
                waitForElementVisible(className("s-extended-date-filters-body"), context));
    }

    public ExtendedDateFilterPanel selectPeriod(DateRange period) {
        waitForCollectionIsNotEmpty(filters).stream()
                .filter(filter -> filter.getText().equals(period.toString()))
                .findFirst()
                .get()
                .click();
        return this;
    }

    public ExtendedDateFilterPanel selectStaticPeriod(String from, String to) {
        selectPeriod(DateRange.STATIC_PERIOD);
        fillInDateRange(waitForElementVisible(fromDate), from);
        new Actions(browser).sendKeys(Keys.TAB).perform();

        fillInDateRange(waitForElementVisible(toDate), to);
        new Actions(browser).sendKeys(Keys.TAB).perform();
        return this;
    }

    public ExtendedDateFilterPanel selectFloatingRange(DateGranularity granularity, String from, String to) {
        selectPeriod(DateRange.FLOATING_RANGE);
        waitForElementVisible(className(granularity.getCssSelector()), getRoot()).click();
        waitForElementVisible(cssSelector(format(".%s.is-active", granularity.getCssSelector())), getRoot());
        fillInDateRange(waitForElementVisible(fromRange), from);
        fillInDateRange(waitForElementVisible(toRange), to);
        return this;
    }

    public DateRange getSelectedDateFilter() {
        return Stream.of(DateRange.values())
                .filter(dateRange -> waitForElementVisible(selectedFilter).getText().equals(dateRange.toString()))
                .findFirst()
                .get();
    }

    public enum DateGranularity {

        DAYS("s-granularity-day"),
        MONTHS("s-granularity-month"),
        QUARTERS("s-granularity-quarter"),
        YEARS("s-granularity-year");

        private String cssSelector;

        DateGranularity(String cssSelector) {
            this.cssSelector = cssSelector;
        }

        public String getCssSelector() {
            return cssSelector;
        }
    }
    public ExtendedDateFilterPanel checkExcludeCurrent() {
        waitForElementEnabled(excludeCurrentPeriod).click();
        return this;
    }

    public void apply() {
        waitForElementVisible(applyButton).click();
        waitForFragmentNotVisible(this);
    }

    public void cancel() {
        waitForElementVisible(cancelButton).click();
        waitForFragmentNotVisible(this);
    }

    private void fillInDateRange(WebElement dateInput, String date) {
        dateInput.sendKeys(Keys.END);
        for (int i = 0; i <= dateInput.getAttribute("value").length(); i++) {
            dateInput.sendKeys(Keys.BACK_SPACE);
        }
        if (!dateInput.getAttribute("value").isEmpty()) {
            ElementUtils.clear(dateInput);
        }
        dateInput.sendKeys(date, Keys.ENTER);
    }
}
