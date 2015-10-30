package com.gooddata.qa.graphene.fragments.reports.filter;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentNotVisible;

import java.util.Collection;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.filter.PromptFilterItem;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;

public class PromptFilterFragment extends AbstractFilterFragment {

    @FindBy(css = ".s-btn-select_variable")
    private WebElement selectVariableButton;

    @Override
    public void addFilter(FilterItem filterItem) {
        PromptFilterItem promptFilterItem = (PromptFilterItem) filterItem;

        selectVariable(promptFilterItem.getVariable()).apply();
        waitForFragmentNotVisible(this);
    }

    public Collection<String> getVariables() {
        waitForElementVisible(selectVariableButton).click();
        return Graphene.createPageFragment(SelectItemPopupPanel.class,
                waitForElementVisible(SelectItemPopupPanel.LOCATOR, browser))
                .getItems();
    }

    private PromptFilterFragment selectVariable(String variable) {
        waitForElementVisible(selectVariableButton).click();
        searchAndSelectItem(variable);
        return this;
    }
}
