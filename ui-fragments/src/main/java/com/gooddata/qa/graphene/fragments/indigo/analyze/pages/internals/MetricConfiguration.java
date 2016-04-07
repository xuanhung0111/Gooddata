package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.lang.String.format;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.tagName;
import static org.testng.Assert.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.AbstractPicker;
import com.gooddata.qa.graphene.fragments.indigo.analyze.description.DescriptionPanel;

public class MetricConfiguration extends AbstractFragment {

    @FindBy(className = "adi-bucket-item-header")
    private WebElement header;

    @FindBy(className = ADD_ATTRIBUTE_FILTER_CLASS)
    private WebElement addAttributeFilter;

    @FindBy(className = "s-show-in-percent")
    private WebElement showInPercents;

    @FindBy(className = "s-show-pop")
    private WebElement compareToSamePeriod;

    private static final By BY_REMOVE_ATTRIBUTE_FILTER = By.className("remove-attribute-filter");
    public static final By BY_ATTRIBUTE_FILTER_PICKER = By.className("adi-attr-filter-picker");
    private static final By BY_ATTRIBUTE_FILTER_BUTTON = By.className("adi-attr-filter-button");
    private static final By BY_FACT_AGGREGATION = By.className("s-fact-aggregation-switch");

    private static final String ADD_ATTRIBUTE_FILTER_CLASS = "s-add_attribute_filter";

    private static final String DISABLED = "is-disabled";

    public String getHeader() {
        return waitForElementVisible(className("s-title"), waitForElementVisible(header)).getText();
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

    public MetricConfiguration expandConfiguration() {
        if (isConfigurationCollapsed()) {
            waitForElementVisible(header).click();
        }
        return this;
    }

    public void collapseConfiguration() {
        if (isConfigurationCollapsed()) {
            return;
        }
        waitForElementVisible(header).click();
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

    public MetricConfiguration addFilterBySelectOnly(String attribute, String value) {
        clickAddAttributeFilter().selectAttribute(attribute);

        Graphene.createPageFragment(AttributeFilterPicker.class,
                waitForElementVisible(BY_ATTRIBUTE_FILTER_PICKER, browser))
                .clear()
                .selectOnly(value)
                .apply();
        return this;
    }

    public void addFilterWithLargeNumberValues(String attribute, String... unselectedValues) {
        clickAddAttributeFilter().selectAttribute(attribute);

        Graphene.createPageFragment(AttributeFilterPicker.class,
                waitForElementVisible(BY_ATTRIBUTE_FILTER_PICKER, browser))
                .selectAll()
                .selectItems(unselectedValues)
                .apply();
    }

    public String getFilterText() {
        return waitForElementVisible(BY_ATTRIBUTE_FILTER_BUTTON, getRoot()).getText();
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

    public static class AttributeFilterPicker extends AbstractPicker {

        @FindBy(className = "s-clear")
        private WebElement clearButton;

        @FindBy(className = "s-select_all")
        private WebElement selectAllButton;

        @FindBy(css = ".s-apply:not(.disabled)")
        private WebElement applyButton;

        private static final By CLEAR_SEARCH_TEXT_SHORTCUT = className("searchfield-clear");

        @Override
        protected String getListItemsCssSelector() {
            return ".adi-filter-item";
        }

        @Override
        protected String getSearchInputCssSelector() {
            return ".searchfield-input";
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

        public List<String> getAllAttributesInViewPort() {
            return getElementTexts(getElements(), e -> e.findElement(cssSelector(".attr-field-icon + span")));
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
    }
}
