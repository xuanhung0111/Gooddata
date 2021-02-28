package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static java.lang.String.format;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.AbstractPicker;
import com.gooddata.qa.graphene.fragments.indigo.analyze.description.DescriptionPanel;
import com.gooddata.qa.graphene.utils.ElementUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MetricConfiguration extends AbstractFragment {

    @FindBy(className = "s-metric-date-filter-button")
    private WebElement filterByDate;

    @FindBy(className = "adi-bucket-item-header")
    private WebElement header;

    @FindBy(className = SUB_HEADER_CLASS_NAME)
    private WebElement subHeader;

    @FindBy(className = ADD_ATTRIBUTE_FILTER_CLASS)
    private WebElement addAttributeFilter;

    @FindBy(className = "s-show-in-percent")
    private WebElement showInPercents;

    @FindBy(css = ".s-show-in-percent + span.input-label-text")
    private WebElement showInPercentLabel;

    @FindBy(className = SHOW_ON_SECONDARY_AXIS_CLASS_NAME)
    private WebElement showOnSecondaryAxis;

    @FindBy(css = ".s-show-on-secondary-axis + span.input-label-text")
    private WebElement showOnSecondaryAxisLabel;

    @FindBy(className = DATE_AND_ADD_ATTRIBUTE_FILTER_CLASS_NAME)
    private List<WebElement> byDateAndAddAttributeFilter;

    @FindBy(className = "adi-bucket-item-sequence-number")
    private WebElement itemSequenceNumber;

    @FindBy(className = "s-adi-am-operator-selector-title")
    private WebElement outcomeOfOperator;

    @FindBy(className = "adi-measure-number-format-button")
    private WebElement formatMeasureButton;

    private static final By BY_REMOVE_ATTRIBUTE_FILTER = By.className("s-remove-attribute-filter");
    private static final By BY_REMOVE_FILTER_BY_DATE = By.className("s-remove-date-filter");
    public static final By BY_ATTRIBUTE_FILTER_PICKER = By.className("adi-attr-filter-picker");
    public static final By BY_DATE_BY_FILTER_PICKER = By.className("adi-date-filter-picker");
    public static final By BY_SELECT_ALL_CHECKBOX = By.cssSelector(".adi-attr-filter-picker .s-select-all-checkbox");
    private static final By BY_ATTRIBUTE_FILTER_BUTTON = By.className("adi-attr-filter-button");
    private static final By BY_FACT_AGGREGATION = By.className("s-fact-aggregation-switch");
    private static final By BY_BUBBLE_CONTENT = By.className("bubble-content");
    private static final String SUB_HEADER_CLASS_NAME = "adi-bucket-item-sub-header";
    private static final String ADD_ATTRIBUTE_FILTER_CLASS = "s-add_attribute_filter";
    private static final String DATE_AND_ADD_ATTRIBUTE_FILTER_CLASS_NAME = "button-dropdown";
    private static final String SHOW_ON_SECONDARY_AXIS_CLASS_NAME = "s-show-on-secondary-axis";
    private static final By BY_CUSTOM_FORMAT_OPTION = By.className("s-format-preset-customFormat");
    private static final By BY_MEASURE_FORMAT_DROPDOWN = By.className("gd-measure-number-format-dropdown-body");

    private static final String DISABLED = "is-disabled";

    public MetricConfiguration chooseArithmeticMeasureA(String measureName, int index) {
        return chooseArithmeticMeasure(
            "s-arithmetic-measure-operand-0-dropdown-button", "M" + index + " (" + measureName +")");
    }
    
    public MetricConfiguration chooseArithmeticMeasureB(String measureName, int index) {
        return chooseArithmeticMeasure(
            "s-arithmetic-measure-operand-1-dropdown-button", "M" + index + " (" + measureName +")");
    }

    public MetricConfiguration chooseOperator(OperatorCalculated typeOperator) {
        waitForElementVisible(outcomeOfOperator).click();
        waitForElementVisible(className(typeOperator.toString()), browser).click();
        return this;
    }

    public List<String> getByDateAndAttributeFilterButton() {
        return byDateAndAddAttributeFilter.stream().map(WebElement::getText).collect(Collectors.toList());
    }

    public boolean isDateAndAddAttributeFilterVisible() {
        return isElementVisible(By.className(DATE_AND_ADD_ATTRIBUTE_FILTER_CLASS_NAME), getRoot());
    }

    public boolean isFilterByDateButtonVisible() {
        return isElementVisible(filterByDate);
    }

    public String getFilterByDate() {
        return waitForElementVisible(filterByDate).getText();
    }

    public String getHeader() {
        return waitForElementVisible(className("s-title"), waitForElementVisible(header)).getText();
    }

    public String getHeaderAndSequenceNumber() {
        return waitForElementVisible(header).getText();
    }

    public String getSubHeader() {
        return waitForElementVisible(subHeader).getText();
    }

    public String getToolTipSubHeader() {
        getActions().moveToElement(subHeader).perform();
        return waitForElementVisible(BY_BUBBLE_CONTENT, browser).getText();
    }

    public Boolean isSubHeaderPresent() {
        return isElementPresent(By.className(SUB_HEADER_CLASS_NAME), getRoot());
    }

    public String getFilterText() {
        return waitForElementVisible(BY_ATTRIBUTE_FILTER_BUTTON, getRoot()).getText().replaceAll("[\\r\\n]+", " ");
    }

    public String getFormatMeasureText() {
        return waitForElementVisible(formatMeasureButton).getText();
    }

    public MetricConfiguration showPercents(boolean isCheck) {
        if (waitForElementPresent(showInPercents).isSelected() != isCheck)
            waitForElementVisible(showInPercentLabel).click();
        return this;
    }

    public MetricConfiguration showPercents() {    
        return showPercents(true);
    }

    public MetricConfiguration checkShowOnSecondaryAxis() {
        if (!isShowOnSecondaryAxisChecked()) {
            waitForElementVisible(showOnSecondaryAxisLabel).click();
        }
        return this;
    }

    public void uncheckShowOnSecondaryAxis() {
        if (!isShowOnSecondaryAxisChecked()) {
            return;
        }
        waitForElementVisible(showOnSecondaryAxisLabel).click();
    }

    public boolean isShowOnSecondaryAxisChecked() {
        return waitForElementPresent(showOnSecondaryAxis).isSelected();
    }

    public String getShowOnSecondaryAxis() {
        return waitForElementVisible(waitForElementPresent(showOnSecondaryAxis).findElement(BY_PARENT)).getText();
    }

    public Boolean isShowOnSecondaryAxisPresent() {
        return isElementPresent(By.className(SHOW_ON_SECONDARY_AXIS_CLASS_NAME), getRoot());
    }

    public boolean isShowPercentEnabled() {
        return !waitForElementPresent(showInPercents).findElement(BY_PARENT).getAttribute("class")
                .contains(DISABLED);
    }

    public boolean isShowPercentSelected() {
        return waitForElementPresent(showInPercents).isSelected();
    }

    public String getAggregation() {
        return new Select(getRoot().findElement(BY_FACT_AGGREGATION)).getFirstSelectedOption()
                .getText();
    }

    public Collection<String> getAllAggregations() {
        return getElementTexts(new Select(getRoot().findElement(BY_FACT_AGGREGATION)).getOptions());
    }

    public void changeAggregation(String newAggregation) {
        new Select(getRoot().findElement(BY_FACT_AGGREGATION)).selectByVisibleText(newAggregation);
    }

    private void clickMetricHeader() {
        // Header element is not scrolled automatically into view (last check with Geckodriver 0.21 / Firefox 60.0.2).
        // All works as expected with Chrome. Workaround - explicitly scroll metric header into view.
        ElementUtils.scrollElementIntoView(header, browser);
        waitForElementVisible(itemSequenceNumber).click();
    }

    public MetricConfiguration expandConfiguration() {
        if (isConfigurationCollapsed()) {
            clickMetricHeader();
        }
        return this;
    }

    public boolean isShowInPercentHidden(){
        return isElementPresent(By.className(".s-show-in-percent"), getRoot());
    }

    public void collapseConfiguration() {
        if (isConfigurationCollapsed()) {
            return;
        }
        clickMetricHeader();
    }

    public boolean isConfigurationCollapsed() {
        return waitForElementVisible(header).getAttribute("class").contains("collapsed");
    }

    public MetricFilterByDatePicker expandFilterByDate() {
        if (!isFilterByDateExpanded()) {
            waitForElementVisible(filterByDate).click();
        }
        return getMetricFilterByDatePicker();
    }

    public boolean isFilterByDateExpanded() {
        return waitForElementVisible(filterByDate).getAttribute("class").contains("is-active");
    }

    public MetricFilterByDatePicker fillToDateRange(String date) {
        MetricFilterByDatePicker filterByDatePicker = getMetricFilterByDatePicker();
        filterByDatePicker.openToDateCalendar().fillInDateRange(filterByDatePicker.getToDateInput(), date);
        waitForElementVisible(filterByDatePicker.fromDateCalendarIcon()).click();
        return filterByDatePicker;
    }

    public MetricFilterByDatePicker fillFromDateRange(String date) {
        MetricFilterByDatePicker filterByDatePicker = getMetricFilterByDatePicker();
        filterByDatePicker.openFromDateCalendar().fillInDateRange(filterByDatePicker.getFromDateInput(), date);
        waitForElementVisible(filterByDatePicker.fromDateCalendarIcon()).click();
        return filterByDatePicker;
    }

    public AttributeFilterPicker clickAddAttributeFilter() {
        waitForElementVisible(addAttributeFilter).click();
        return Graphene.createPageFragment(AttributeFilterPicker.class,
                waitForElementVisible(BY_ATTRIBUTE_FILTER_PICKER, browser));
    }

    public MetricConfiguration addFilter(String attribute, String... values) {
        clickAddAttributeFilter().selectAttribute(attribute);

        waitForElementVisible(BY_SELECT_ALL_CHECKBOX, browser);

        Graphene.createPageFragment(AttributeFilterPicker.class,
                waitForElementVisible(BY_ATTRIBUTE_FILTER_PICKER, browser))
                .clear()
                .selectItems(values)
                .apply();
        return this;
    }

    public MetricConfiguration addFilterWithAllValue(String attribute) {
        addFilter(attribute, AttributeFilterPicker::cancel, null);
        return this;
    }

    public MetricConfiguration addFilterBySelectOnly(String attribute, String value) {
        return addFilter(attribute,
                attributeFilterPicker -> attributeFilterPicker.clear().selectOnly(value).apply(), value);
    }

    public MetricConfiguration addFilterByDate(String dateDimension, String from, String to) {
        expandFilterByDate().expandDateDimension().changeDateDimension(dateDimension).configTimeFilter(from, to);
        return this;
    }

    public MetricConfiguration tryToAddFilterByDate(String dateDimension, String from, String to) {
        expandFilterByDate().expandDateDimension().changeDateDimension(dateDimension)
                .tryToConfigTimeFilterByRange(from, to);
        return this;
    }

    public MetricConfiguration addFilterByDate(String dateRange) {
        return addFilterByDate(null, dateRange);
    }

    public MetricConfiguration addFilterByDate(String dateDimension, String dateRange) {
        MetricFilterByDatePicker filterByDatePicker = expandFilterByDate();
        if (dateDimension != null) {
            filterByDatePicker.expandDateDimension().changeDateDimension(dateDimension);
        }
        filterByDatePicker.selectDateFilter(dateRange);
        return this;
    }

    public List<String> getlistRecommended(){
        MetricFilterByDatePicker filterByDatePicker = expandFilterByDate();
        filterByDatePicker.expandDateDimension();
        return waitForCollectionIsNotEmpty(browser.findElements(className("gd-list-item-shortened")))
                .stream().map(e-> e.getText()).collect(Collectors.toList());
    }

    public void addFilterWithLargeNumberValues(String attribute, String... unselectedValues) {
        addFilter(attribute, attributeFilterPicker ->
                attributeFilterPicker.selectAll().selectItems(unselectedValues).apply(), unselectedValues);
    }

    public MetricConfiguration removeFilterByDate() {
        waitForElementVisible(BY_REMOVE_FILTER_BY_DATE, getRoot()).click();
        return this;
    }

    public int getNumberOfAttrFilter() {
        return getRoot().findElements(BY_ATTRIBUTE_FILTER_BUTTON).size();
    }

    public List<String> getAllFilterText() {
        return Stream.of(waitForElementVisible(BY_ATTRIBUTE_FILTER_BUTTON, getRoot()))
                .map(filter -> filter.getText().replaceAll("[\\r\\n]+", ""))
                .collect(Collectors.toList());
    }

    public MetricConfiguration removeAttributeFilter(String attribute) {
        getRoot().findElements(By.className("metric-filter-wrapper")).stream()
                .filter(filter -> filter.getText().replaceAll("[\\r\\n]+", "").equals(attribute))
                .forEach(filter -> filter.findElement(BY_REMOVE_ATTRIBUTE_FILTER).click());

        return this;
    }

    public boolean canAddAnotherFilter() {
        return isElementPresent(className(ADD_ATTRIBUTE_FILTER_CLASS), getRoot());
    }

    public MetricConfiguration removeFilter() {
        waitForElementVisible(BY_REMOVE_ATTRIBUTE_FILTER, getRoot()).click();
        return this;
    }

    public String getAttributeDescription(String attribute) {
        return clickAddAttributeFilter().getDescription(attribute);
    }

    public boolean isDisabledAttribute(String attribute) {
        AttributeFilterPicker attributeFilterPicker = clickAddAttributeFilter();
        boolean isDisable = attributeFilterPicker.isDisabledAttribute(attribute);
        waitForElementVisible(addAttributeFilter).click(); //To close picker
        waitForFragmentNotVisible(attributeFilterPicker);
        return isDisable;
    }

    private MetricConfiguration addFilter(String att, Consumer<AttributeFilterPicker> howToSelect, String... values) {
        clickAddAttributeFilter().selectAttribute(att);
        howToSelect.accept(Graphene.createPageFragment(AttributeFilterPicker.class,
                waitForElementVisible(BY_ATTRIBUTE_FILTER_PICKER, browser)));

        return this;
    }

    private MetricFilterByDatePicker getMetricFilterByDatePicker() {
        return Graphene.createPageFragment(MetricFilterByDatePicker.class,
                waitForElementVisible(BY_DATE_BY_FILTER_PICKER, browser));
    }

    private MetricConfiguration chooseArithmeticMeasure(String dropdownButtonClassName, String measureName) {
        waitForElementVisible(className(format(dropdownButtonClassName)), getRoot()).click();

        waitForCollectionIsNotEmpty(browser.findElements(className("gd-list-item")))
            .stream()
            .filter(e -> Objects.equals(measureName, e.getText()))
            .findFirst()
            .get().click();
        return this;
    }

    public List<String> getListFormatPresets() {
        waitForElementVisible(formatMeasureButton).click();
        WebElement tooltip = waitForElementVisible(BY_MEASURE_FORMAT_DROPDOWN, browser);
        
        return waitForCollectionIsNotEmpty(tooltip.findElements(cssSelector(".gd-list-item.gd-format-preset"))).stream()
            .map(item -> item.findElement(cssSelector(".gd-format-preset-name")).getText())
            .collect(Collectors.toList());
    }

    public MetricConfiguration changeFormatMeasure(FormatMeasurePreset formatMeasure) {
        if (!waitForElementVisible(formatMeasureButton).getAttribute("class").contains("is-active")) {
            formatMeasureButton.click();
        }
        waitForElementVisible(className(formatMeasure.toString()), browser).click();
        return this;
    }

    public MetricConfiguration openCustomFormatMeasureDialog() {
        waitForElementVisible(formatMeasureButton).click();
        waitForElementVisible(BY_CUSTOM_FORMAT_OPTION, browser).click();
        return this;
    }

    public CustomMeasureFormatDialog getCustomMeasureFormatDialog() {
        return CustomMeasureFormatDialog.getInstance(browser);
    }

    public static class AttributeFilterPicker extends AbstractPicker {

        @FindBy(css = ".s-select-all-checkbox")
        private WebElement selectAllLabel;

        @FindBy(css = ".gd-checkbox-selection")
        private WebElement selectAllCheckbox;

        @FindBy(css = ".gd-button-small.s-apply:not(.disabled)")
        private WebElement applyButton;

        @FindBy(css = ".gd-button-small.s-cancel:not(.disabled)")
        private WebElement cancelButton;

        private static final By CLEAR_SEARCH_TEXT_SHORTCUT = className("gd-input-icon-clear");

        @Override
        protected String getListItemsCssSelector() {
            return ".adi-filter-item";
        }

        @Override
        protected String getSearchInputCssSelector() {
            return ".gd-input-search input";
        }

        @Override
        protected void waitForPickerLoaded() {
            waitForElementNotPresent(className("s-dropdown-loading"));
        }

        @Override
        protected void clearSearchText() {
            if (isElementPresent(CLEAR_SEARCH_TEXT_SHORTCUT, getRoot())) {
                waitForElementVisible(CLEAR_SEARCH_TEXT_SHORTCUT, getRoot()).click();
                return;
            }

            super.clearSearchText();
        }

        @Override
        protected WebElement getElementByName(final String name) {
            //Prevent to same attribute name
            return getElement(".s-" + simplifyText(name) + ":not(.is-disabled)");
        }

        public AttributeFilterPicker clear() {
            WebElement checkbox = waitForElementPresent(selectAllCheckbox);
            if (checkbox.getAttribute("class").contains("checkbox-indefinite")) {
                checkbox.click(); // this will select all
            }

            if (checkbox.isSelected()) {
                checkbox.click();
            }
            return this;
        }

        public AttributeFilterPicker selectAll() {
            waitForElementVisible(selectAllLabel);
            if (selectAllCheckbox.getAttribute("class").contains("checkbox-indefinite")) {
                selectAllCheckbox.click(); // this will select all
            }

            if (!selectAllCheckbox.isSelected()) {
                selectAllCheckbox.click();
            }
            return this;
        }

        public AttributeFilterPicker selectOnly(String element) {
            searchForText(element);
            WebElement ele = getElement(format("[title='%s']", element));
            getActions().moveToElement(ele).perform();
            waitForElementVisible(className("gd-list-item-only"), ele).click();
            return this;
        }

        public String getDescription(String element) {
            searchForText(element);
            final WebElement ele = getElementByName(element);
            getActions().moveToElement(ele).perform();
            getActions().moveToElement(waitForElementPresent(cssSelector(".inlineBubbleHelp"), ele)).perform();

            return Graphene.createPageFragment(DescriptionPanel.class,
                    waitForElementVisible(DescriptionPanel.LOCATOR, browser)).getAttributeDescription();
        }

        public void selectAttribute(String element) {
            searchForText(element);
            getElementByName(element).click();
        }

        public boolean isDisabledAttribute(String element) {
            searchForText(element);
            return getElement(".s-" + simplifyText(element)).getAttribute("class").contains("is-disable");
        }

        public List<String> getAllAttributesInViewPort() {
            return getElementTexts(getElements());
        }

        public AttributeFilterPicker selectItems(String... items) {
            Stream.of(items).forEach(element -> {
                searchForText(element);
                getElement(format("[title='%s']", element))
                        .click();
            });
            return this;
        }

        public void apply() {
            //Click action on element does not affect sometimes, so switch to use java script executor.
            BrowserUtils.runScript(browser, "arguments[0].click();", waitForElementVisible(applyButton));
            waitForElementNotVisible(getRoot());
        }

        public void cancel() {
            //Click action on element does not affect sometimes, so switch to use java script executor.
            BrowserUtils.runScript(browser, "arguments[0].click();", waitForElementVisible(cancelButton));
            waitForElementNotVisible(getRoot());
        }
    }

    public enum OperatorCalculated {

        SUM("s-adi-am-operator-selector-item-sum"),
        DIFFERENCE("s-adi-am-operator-selector-item-difference"),
        PRODUCT ("s-adi-am-operator-selector-item-multiplication"),
        RATIO("s-adi-am-operator-selector-item-ratio"),
        CHANGE("s-adi-am-operator-selector-item-change");

        private String type;

        OperatorCalculated(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }
    }

    public enum FormatMeasurePreset {

        INHERIT("s-format-preset-inherit"),
        ROUNDED("s-format-preset-rounded"),
        DECIMAL_1("s-format-preset-decimal-1"),
        DECIMAL_2("s-format-preset-decimal-2"),
        PERCENT_ROUNDED("s-format-preset-percent-rounded"),
        PERCENT_1("s-format-preset-percent-1"),
        PERCENT_2("s-format-preset-percent-2");

        private String type;

        FormatMeasurePreset(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }
    }
}
