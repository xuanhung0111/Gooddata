package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static java.lang.String.format;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.tagName;
import static org.testng.Assert.assertTrue;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.AbstractPicker;
import com.gooddata.qa.graphene.fragments.indigo.analyze.description.DescriptionPanel;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MetricConfiguration extends AbstractFragment {

    @FindBy(className = "adi-bucket-item-header")
    private WebElement header;

    @FindBy(className = "adi-bucket-item-sub-header")
    private WebElement subHeader;

    @FindBy(className = ADD_ATTRIBUTE_FILTER_CLASS)
    private WebElement addAttributeFilter;

    @FindBy(className = "s-show-in-percent")
    private WebElement showInPercents;

    @FindBy(className = "s-show-pop")
    private WebElement compareToSamePeriod;

    private static final By BY_REMOVE_ATTRIBUTE_FILTER = By.className("s-remove-attribute-filter");
    public static final By BY_ATTRIBUTE_FILTER_PICKER = By.className("adi-attr-filter-picker");
    private static final By BY_ATTRIBUTE_FILTER_BUTTON = By.className("adi-attr-filter-button");
    private static final By BY_FACT_AGGREGATION = By.className("s-fact-aggregation-switch");
    private static final By BY_BUBBLE_CONTENT = By.className("bubble-content");

    private static final String ADD_ATTRIBUTE_FILTER_CLASS = "s-add_attribute_filter";

    private static final String DISABLED = "is-disabled";

    public String getHeader() {
        return waitForElementVisible(className("s-title"), waitForElementVisible(header)).getText();
    }

    public String getSubHeader() {
        return waitForElementVisible(subHeader).getText();
    }

    public String getToolTipSubHeader() {
        getActions().moveToElement(subHeader).perform();
        return waitForElementVisible(BY_BUBBLE_CONTENT, browser).getText();
    }

    public MetricConfiguration showPercents() {
        waitForElementVisible(showInPercents).click();
        assertTrue(showInPercents.isSelected());
        return this;
    }

    public boolean isShowPercentEnabled() {
        return !waitForElementPresent(showInPercents).findElement(BY_PARENT).getAttribute("class")
                .contains(DISABLED);
    }

    public boolean isPopEnabled() {
        return !waitForElementPresent(compareToSamePeriod).findElement(BY_PARENT)
                .getAttribute("class").contains(DISABLED);
    }

    public boolean isShowPercentSelected() {
        return waitForElementPresent(showInPercents).isSelected();
    }

    public boolean isPopSelected() {
        return waitForElementPresent(compareToSamePeriod).isSelected();
    }

    public MetricConfiguration showPop() {
        waitForElementVisible(compareToSamePeriod).click();
        assertTrue(compareToSamePeriod.isSelected());
        return this;
    }

    public MetricConfiguration hidePop() {
        if (isPopSelected())
            compareToSamePeriod.click();

        return this;
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
        // now click with offset because clicking in the middle (default by graphene/selenium)
        // causes activating editableLabel renaming instead of toggling measure configuration
        getActions().moveToElement(header, 2, 2).click().perform();
    }

    public MetricConfiguration expandConfiguration() {
        if (isConfigurationCollapsed()) {
            clickMetricHeader();
        }
        return this;
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

    public AttributeFilterPicker clickAddAttributeFilter() {
        waitForElementVisible(addAttributeFilter).click();
        return Graphene.createPageFragment(AttributeFilterPicker.class,
                waitForElementVisible(BY_ATTRIBUTE_FILTER_PICKER, browser));
    }

    public MetricConfiguration addFilter(String attribute, String... values) {
        clickAddAttributeFilter().selectAttribute(attribute);

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
        return addFilter(attribute, attributeFilterPicker -> attributeFilterPicker.clear().selectOnly(value).apply(), value);
    }

    public void addFilterWithLargeNumberValues(String attribute, String... unselectedValues) {
        addFilter(attribute, attributeFilterPicker ->
                attributeFilterPicker.selectAll().selectItems(unselectedValues).apply(), unselectedValues);
    }

    public String getFilterText() {
        return waitForElementVisible(BY_ATTRIBUTE_FILTER_BUTTON, getRoot()).getText().replaceAll("[\\r\\n]+", " ");
    }

    public List<String> getAllFilterText() {
        return Stream.of(waitForElementVisible(BY_ATTRIBUTE_FILTER_BUTTON, getRoot()))
                .map(filter -> filter.getText().replaceAll("[\\r\\n]+", " "))
                .collect(Collectors.toList());
    }

    public MetricConfiguration removeAttributeFilter(String attribute) {
        getRoot().findElements(By.className("metric-filter-wrapper")).stream()
                .filter(filter -> filter.getText().replaceAll("[\\r\\n]+", " ").equals(attribute))
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

    public static class AttributeFilterPicker extends AbstractPicker {

        @FindBy(className = "s-clear")
        private WebElement clearButton;

        @FindBy(className = "s-select_all")
        private WebElement selectAllButton;

        @FindBy(css = ".s-apply:not(.disabled)")
        private WebElement applyButton;

        @FindBy(css = ".s-cancel")
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
            waitForElementNotPresent(cssSelector(".filter-items-loading"));
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
            waitForElementVisible(clearButton).click();
            return this;
        }

        public AttributeFilterPicker selectAll() {
            waitForElementVisible(selectAllButton).click();
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
                    .findElement(tagName("input"))
                    .click();
            });
            return this;
        }

        public void apply() {
            waitForElementVisible(applyButton).click();
            waitForElementNotVisible(getRoot());
        }

        public void cancel() {
            waitForElementVisible(cancelButton).click();
            waitForElementNotVisible(getRoot());
        }
    }
}
