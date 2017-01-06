package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;

import java.util.Collection;
import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.analyze.DateDimensionSelect;

public class DateFilterPickerPanel extends AbstractFragment {

    // presets and date range sections are just small parts. No need to separate more fragments now.

    @FindBy(className = "s-tab-presets")
    private WebElement presetsSection;

    @FindBy(css = ".adi-tab-presets .filter-picker-text")
    private List<WebElement> periods;

    // ****************  date range section  ****************
    @FindBy(className = "s-tab-range")
    private WebElement dateRangeSection;

    @FindBy(css = ".adi-date-input-from > input")
    private WebElement fromDate;

    @FindBy(css = ".adi-date-input-to > input")
    private WebElement toDate;

    @FindBy(css = ".adi-date-input-from > span")
    private WebElement fromDateCalendarIcon;

    @FindBy(css = ".adi-date-input-to > span")
    private WebElement toDateCalendarIcon;

    @FindBy(className = "s-date-range-cancel")
    private WebElement cancelButton;

    @FindBy(className = "s-date-range-apply")
    private WebElement applyButton;

    public static final By LOCATOR = By.className("adi-date-filter-picker");

    public void select(final String period) {
        waitForCollectionIsNotEmpty(periods).stream()
            .filter(e -> period.equals(e.getText()))
            .findFirst()
            .get()
            .click();
        waitForFragmentNotVisible(this);
    }

    public List<String> getPeriods() {
        return getElementTexts(waitForCollectionIsNotEmpty(periods));
    }

    public Collection<String> getDimensionSwitchs() {
        return getDateDatasetSelect().getValues();
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
    public void configTimeFilter(String from, String to) {
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

    public void changeDateDimension(String switchDimension) {
        getDateDatasetSelect().selectByName(switchDimension);
    }

    public boolean isDimensionSwitcherEnabled() {
        return getDateDatasetSelect().isEnabled();
    }

    public String getSelectedDimensionSwitch() {
        return getDateDatasetSelect().getRoot().getText();
    }

    public DateDimensionSelect getDateDatasetSelect() {
        return Graphene.createPageFragment(DateDimensionSelect.class,
                waitForElementVisible(By.className("adi-date-dataset-select-dropdown"), browser));
    }

    private void configTimeFilterByRangeHelper(String from, String to, boolean apply) {
        waitForElementVisible(dateRangeSection).click();
        waitForElementVisible(fromDate).clear();
        fromDate.sendKeys(from);

        waitForElementVisible(toDate).clear();
        toDate.sendKeys(to);
        waitForElementVisible(toDateCalendarIcon).click();

        waitForElementVisible(apply ? applyButton : cancelButton).click();
        waitForFragmentNotVisible(this);
    }
}
