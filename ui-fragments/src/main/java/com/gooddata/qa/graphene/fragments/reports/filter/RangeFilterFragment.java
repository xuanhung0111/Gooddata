package com.gooddata.qa.graphene.fragments.reports.filter;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;

import java.util.List;
import java.util.stream.Stream;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.filter.FloatingTime.Time;
import com.gooddata.qa.graphene.entity.filter.RangeFilterItem;

public class RangeFilterFragment extends AbstractFilterFragment {

    @FindBy(css = ".s-btn-select_attribute")
    private WebElement selectAttributeButton;

    @FindBy(css = "button[class*='s-btn-add_another_attribute']")
    private WebElement addAnotherAttributeButton;

    @FindBy(css = ".s-btn-select_metric")
    private WebElement selectMetricButton;

    @FindBy(css = ".s-select-rangefi-operator")
    private Select rangeSelect;

    @FindBy(css = ".s-input-number")
    private WebElement numberInput;

    @FindBy(className = "yui3-c-subfilters")
    private SubFilterContainer subFilterContainer;

    @Override
    public void addFilter(FilterItem filterItem) {
        RangeFilterItem rangeFilterItem = (RangeFilterItem) filterItem;

        selectAttributes(rangeFilterItem.getAttributes())
                .selectMetric(rangeFilterItem.getMetric())
                .selectRange(rangeFilterItem)
                .apply();
        waitForFragmentNotVisible(this);
    }

    public RangeFilterFragment addSubFilterByAttributeValues(String attribute, String...values) {
        waitForFragmentVisible(subFilterContainer).addSubFilterByAttributeValues(attribute, values);
        return this;
    }

    public RangeFilterFragment addSubFilterByDateRange(String attrDate, Time time) {
        waitForFragmentVisible(subFilterContainer).addSubFilterByDateRange(attrDate, time);
        return this;
    }

    public RangeFilterFragment addSubFilterByVariable(String variableName) {
        waitForFragmentVisible(subFilterContainer).addSubFilterByVariable(variableName);
        return this;
    }

    public RangeFilterFragment deleteLatestSubFilter() {
        getLatestSubFilter().delete();
        return this;
    }

    public RangeFilterFragment changeLatestSubFilterOperator(String operator) {
        getLatestSubFilter().changeOperator(operator);
        return this;
    }

    private RangeFilterFragment selectAttributes(List<String> attribute) {
        attribute.stream().forEach(this::selectAttribute);
        return this;
    }

    private RangeFilterFragment selectAttribute(String attribute) {
        Stream.of(selectAttributeButton, addAnotherAttributeButton)
                .filter(e -> !e.getAttribute("class").contains("gdc-hidden"))
                .findFirst()
                .get()
                .click();

        searchAndSelectItem(attribute);
        return this;
    }

    private RangeFilterFragment selectMetric(String metric) {
        waitForElementVisible(selectMetricButton).click();
        searchAndSelectItem(metric);
        return this;
    }

    private RangeFilterFragment selectRange(RangeFilterItem filterItem) {
        waitForElementVisible(rangeSelect).selectByVisibleText(filterItem.getRangeType().toString());
        waitForElementVisible(numberInput).clear();
        numberInput.sendKeys(String.valueOf(filterItem.getRangeNumber()));
        return this;
    }

    private SubFilter getLatestSubFilter() {
        return waitForFragmentVisible(subFilterContainer).getLatestSubFilter();
    }
}