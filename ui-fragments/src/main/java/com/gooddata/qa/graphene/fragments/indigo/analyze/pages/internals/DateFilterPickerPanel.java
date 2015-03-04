package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class DateFilterPickerPanel extends AbstractFragment {

    // presets and date range sections are just small parts. No need to separate more fragments now.

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

    private void configTimeFilterByRangeHelper(String from, String to, boolean apply) {
        waitForElementVisible(dateRangeSection).click();
        waitForElementVisible(fromDate).clear();
        fromDate.sendKeys(from);

        waitForElementVisible(toDate).clear();
        toDate.sendKeys(to);

        waitForElementVisible(apply ? applyButton : cancelButton).click();
        waitForFragmentNotVisible(this);
    }
}
