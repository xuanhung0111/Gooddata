package com.gooddata.qa.graphene.fragments.reports.filter;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentNotVisible;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.filter.PromptFilterItem;

public class PromptFilterFragment extends AbstractFilterFragment {

    @FindBy(css = ".s-btn-select_variable")
    private WebElement selectVariableButton;

    @Override
    public void addFilter(FilterItem filterItem) {
        PromptFilterItem promptFilterItem = (PromptFilterItem) filterItem;

        selectVariable(promptFilterItem.getVariable()).apply();
        waitForFragmentNotVisible(this);
    }

    private PromptFilterFragment selectVariable(String variable) {
        waitForElementVisible(selectVariableButton).click();
        searchAndSelectItem(variable);
        return this;
    }
}
