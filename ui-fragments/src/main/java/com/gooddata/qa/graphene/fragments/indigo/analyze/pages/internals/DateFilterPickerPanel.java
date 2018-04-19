package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.gooddata.qa.graphene.fragments.indigo.analyze.DatePresetsSelect;
import com.gooddata.qa.graphene.utils.ElementUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;
import com.gooddata.qa.graphene.fragments.indigo.analyze.DateDimensionSelect;

public class DateFilterPickerPanel extends AbstractFragment {

    public static final String STATIC_PERIOD_DROPDOWN_ITEM = "Static period";

    @FindBy(className = "s-date-preset-button")
    private WebElement presetsDropdown;

    @FindBy(css = ".s-date-presets-list .gd-list-item")
    private List<WebElement> periods;

    @FindBy(css = ".adi-date-input-from .input-text")
    private WebElement fromDate;

    @FindBy(css = ".adi-date-input-to .input-text")
    private WebElement toDate;

    @FindBy(css = ".adi-date-input-from > span")
    private WebElement fromDateCalendarIcon;

    @FindBy(css = ".adi-date-input-to > span")
    private WebElement toDateCalendarIcon;

    @FindBy(className = "s-date-filter-cancel")
    private WebElement cancelButton;

    @FindBy(className = "s-date-filter-apply")
    private WebElement applyButton;

    public static final By LOCATOR = By.className("adi-date-filter-picker");

    public void select(final String period) {
        getDatePresetSelect().selectByName(period);
        getDatePresetSelect().ensureDropdownClosed();
    }

    public List<String> getPeriods() {
        return getDatePresetSelect()
                .getValues()
                .stream()
                .collect(Collectors.toList());
    }

    public Collection<String> getDimensionSwitchs() {
        DateDimensionSelect select = getDateDatasetSelect();
        Collection<String> values = select.getValues();
        select.ensureDropdownClosed();
        return values;
    }

    /**
     * @param from format MM/DD/YYYY
     * @param to format MM/DD/YYYY
     */
    public void configTimeFilterByRangeButNotApply(String from, String to) {
        configTimeFilterByRangeHelper(from, to, false);
    }

    /**
     * @param from format MM/DD/YYYY
     * @param to format MM/DD/YYYY
     */
    public void configTimeFilter(String from, String to) {
        configTimeFilterByRangeHelper(from, to, true);
    }

    public void selectStaticPeriod() {
        select(STATIC_PERIOD_DROPDOWN_ITEM);
    }

    public String getFromDate() {
        return waitForElementVisible(fromDate).getAttribute("value");
    }

    public String getToDate() {
        return waitForElementVisible(toDate).getAttribute("value");
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

    public AbstractReactDropDown getDatePresetSelect() {
        return Graphene.createPageFragment(DatePresetsSelect.class,
                waitForElementVisible(By.className("adi-date-preset-select-dropdown"), browser));
    }

    public void apply() {
        waitForElementVisible(applyButton).click();
        waitForFragmentNotVisible(this);
    }

    private void configTimeFilterByRangeHelper(String from, String to, boolean apply) {
        selectStaticPeriod();

        ElementUtils.clear(fromDate);
        fromDate.sendKeys(from);

        ElementUtils.clear(toDate);
        toDate.sendKeys(to);

        waitForElementVisible(toDateCalendarIcon).click();

        waitForElementVisible(apply ? applyButton : cancelButton).click();
        waitForFragmentNotVisible(this);
    }
}
