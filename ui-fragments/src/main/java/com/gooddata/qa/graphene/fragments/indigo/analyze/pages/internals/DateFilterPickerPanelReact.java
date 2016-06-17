package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class DateFilterPickerPanelReact extends AbstractFragment {

    @FindBy(css = DIMENSION_SWITCH_LOCATOR)
    private Select dimensionSwitch;

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

    @FindBy(className = "s-date-range-cancel")
    private WebElement cancelButton;

    @FindBy(className = "s-date-range-apply")
    private WebElement applyButton;

    public static final By LOCATOR = By.className("adi-date-filter-picker");

    private static final String DIMENSION_SWITCH_LOCATOR = ".s-filter-date-date-dataset-switch"; 

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

    public List<String> getDimensionSwitchs() {
        return getElementTexts(waitForElementVisible(dimensionSwitch).getOptions());
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
        waitForElementVisible(this.dimensionSwitch).selectByVisibleText(switchDimension);
    }

    public boolean isDimensionSwitcherEnabled() {
        return waitForElementVisible(By.cssSelector(DIMENSION_SWITCH_LOCATOR), getRoot()).isEnabled();
    }

    public String getSelectedDimensionSwitch() {
        return waitForElementVisible(dimensionSwitch).getFirstSelectedOption().getText();
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
