package com.gooddata.qa.graphene.fragments.dashboards.widget;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class VariableStatusWidget extends AbstractFragment {

    @FindBy(className = "yui3-c-variabledashboardwidget-variableContent")
    private WebElement content;

    @FindBy(className = "yui3-c-variabledashboardwidget-variableLabel")
    private WebElement label;

    public String getContent() {
        return waitForElementVisible(content).getText();
    }

    public String getLabel() {
        return waitForElementVisible(label).getText();
    }
}
