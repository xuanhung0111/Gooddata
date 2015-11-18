package com.gooddata.qa.graphene.fragments.reports.filter;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentNotVisible;

import java.util.List;
import java.util.stream.Stream;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.entity.filter.FilterItem;
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

    @Override
    public void addFilter(FilterItem filterItem) {
        RangeFilterItem rangeFilterItem = (RangeFilterItem) filterItem;

        selectAttributes(rangeFilterItem.getAttributes())
                .selectMetric(rangeFilterItem.getMetric())
                .selectRange(rangeFilterItem)
                .apply();
        waitForFragmentNotVisible(this);
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
}
