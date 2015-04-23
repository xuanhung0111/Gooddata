package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class DateFilterPickerPanel extends AbstractFragment {

    @FindBy(css = DIMENSION_SWITCH_LOCATOR)
    private Select dimensionSwitch;

    // presets and date range sections are just small parts. No need to separate more fragments now.

    @FindBy(className = "s-tab-presets")
    private WebElement presetsSection;

    @FindBy(css = ".adi-tab-presets .filter-picker-text")
    private List<WebElement> periods;

    // ****************  date range section  ****************
    @FindBy(className = "s-tab-date-range")
    private WebElement dateRangeSection;

    @FindBy(css = ".adi-date-input-from > input")
    private WebElement fromDate;

    @FindBy(css = ".adi-date-input-to > input")
    private WebElement toDate;

    @FindBy(css = ".adi-tab-date-range .s-btn-cancel")
    private WebElement cancelButton;

    @FindBy(css = ".adi-tab-date-range .s-btn-apply")
    private WebElement applyButton;

    public static final By LOCATOR = By.cssSelector(".adi-date-filter-picker");

    private static final String DIMENSION_SWITCH_LOCATOR = ".adi-dimension-select> select"; 

    public void select(final String period) {
        waitForCollectionIsNotEmpty(periods);
        Iterables.find(periods, new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement input) {
                return period.equals(input.getText());
            }
        }).click();
        waitForFragmentNotVisible(this);
    }

    public List<String> getAllPeriods() {
        waitForCollectionIsNotEmpty(periods);
        return Lists.newArrayList(Collections2.transform(periods,
                new Function<WebElement, String>() {
            @Override
            public String apply(WebElement input) {
                return input.getText();
            }
        }));
    }

    public List<String> getAllDimensionSwitchs() {
        waitForElementVisible(dimensionSwitch);
        return Lists.newArrayList(Collections2.transform(dimensionSwitch.getOptions(),
                new Function<WebElement, String>() {
            @Override
            public String apply(WebElement input) {
                return input.getText();
            }
        }));
    }

    public void hoverOnPeriod(final String period) {
        waitForCollectionIsNotEmpty(periods);
        new Actions(browser).moveToElement(Iterables.find(periods, new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement input) {
                return period.equals(input.getText());
            }
        })).perform();
    }

    public String getTooltipFromPeriod() {
        return waitForElementVisible(By.cssSelector(".bubble-content > .content"), browser).getText().trim();
    }

    /**
     * @param from format MM/DD/YYYY
     * @param to   format MM/DD/YYYY
     */
    public void configTimeFilterByRangeButNotApply(String from, String to) {
        configTimeFilterByRangeHelper(from, to, false);
    }

    /**
     * @param from format MM/DD/YYYY
     * @param to   format MM/DD/YYYY
     */
    public void configTimeFilterByRange(String from, String to) {
        configTimeFilterByRangeHelper(from, to, true);
    }

    public void changeToDateRangeSection() {
        waitForElementVisible(dateRangeSection).click();
    }

    public String getFromDate() {
        return waitForElementVisible(fromDate).getAttribute("value");
    }

    public String getToDate() {
        return waitForElementVisible(toDate).getAttribute("value");
    }

    public void changeToPresetsSection() {
        waitForElementVisible(presetsSection).click();
    }

    private void configTimeFilterByRangeHelper(String from, String to, boolean apply) {
        waitForElementVisible(dateRangeSection).click();
        waitForElementVisible(fromDate).clear();
        fromDate.sendKeys(from);

        waitForElementVisible(toDate).clear();
        toDate.sendKeys(to);

        waitForElementVisible(apply ? applyButton : cancelButton).click();
        waitForFragmentNotVisible(this);
    }

    public void changeDimensionSwitchInFilter(String dimensionSwitch) {
        waitForElementVisible(this.dimensionSwitch).selectByVisibleText(dimensionSwitch);
    }

    public boolean isDimensionSwitcherEnabled() {
        return waitForElementVisible(By.cssSelector(DIMENSION_SWITCH_LOCATOR), getRoot()).isEnabled();
    }

    public String getSelectedDimensionSwitch() {
        return waitForElementVisible(dimensionSwitch).getFirstSelectedOption().getText();
    }
}
