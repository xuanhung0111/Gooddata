package com.gooddata.qa.graphene.fragments.reports.filter;

import static com.gooddata.qa.graphene.utils.CheckUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;

import java.util.Collection;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.filter.AttributeFilterItem;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;

public class AttributeFilterFragment extends AbstractFilterFragment {

    @FindBy(className = "listContainer")
    private SelectItemPopupPanel valuesPanel;

    @FindBy(className = "s-btn-select_attribute")
    private WebElement selectButton;

    @Override
    public void addFilter(FilterItem filterItem) {
        AttributeFilterItem attributeFilterItem = (AttributeFilterItem) filterItem;

        searchAndSelectAttribute(attributeFilterItem.getAttribute())
                .searchAndSelectAttributeValues(attributeFilterItem.getValues())
                .apply();
        waitForFragmentNotVisible(this);
    }

    private AttributeFilterFragment searchAndSelectAttribute(String attribute) {
        if(!isElementPresent(SelectItemPopupPanel.LOCATOR, browser)){
            waitForElementVisible(selectButton).click();
        }

        searchAndSelectItem(attribute);
        return this;
    }

    private AttributeFilterFragment searchAndSelectAttributeValues(Collection<String> values) {
        waitForFragmentVisible(valuesPanel).searchAndSelectItems(values);
        return this;
    }
}
