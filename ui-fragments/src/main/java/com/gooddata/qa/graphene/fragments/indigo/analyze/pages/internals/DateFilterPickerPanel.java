package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentNotVisible;
import static java.util.stream.Collectors.toList;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

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

    public static final By LOCATOR = By.className("adi-date-filter-picker");

    private static final String DIMENSION_SWITCH_LOCATOR = ".adi-dimension-select> select"; 

    public void select(final String period) {
        waitForCollectionIsNotEmpty(periods).stream()
            .filter(e -> period.equals(e.getText()))
            .findFirst()
            .get()
            .click();
        waitForFragmentNotVisible(this);
    }

    public List<String> getAllPeriods() {
        return waitForCollectionIsNotEmpty(periods).stream()
            .map(WebElement::getText)
            .collect(toList());
    }

    public List<String> getAllDimensionSwitchs() {
        return waitForElementVisible(dimensionSwitch).getOptions()
            .stream()
            .map(WebElement::getText)
            .collect(toList());
    }

    public void hoverOnPeriod(final String period) {
        WebElement element = waitForCollectionIsNotEmpty(periods).stream()
            .filter(e -> period.equals(e.getText()))
            .findFirst()
            .get();
        getActions().moveToElement(element).perform();
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
