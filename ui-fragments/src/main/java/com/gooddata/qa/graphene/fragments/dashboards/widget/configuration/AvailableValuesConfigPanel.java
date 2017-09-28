package com.gooddata.qa.graphene.fragments.dashboards.widget.configuration;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;

public class AvailableValuesConfigPanel extends AbstractFragment {

    @FindBy(className = "availaleDescription")
    private WebElement availableValuesDescriptions;

    @FindBy(css = ".availaleDescription a")
    private WebElement moreInfoLink;

    @FindBy(className = "addMetricButton")
    private WebElement addMetricButton;

    @FindBy(className = "s-btn-apply")
    private WebElement applyButton;

    public String getAvailableValuesDescriptions() {
        return waitForElementVisible(availableValuesDescriptions).getText();
    }

    public String getMoreInfoText() {
        return waitForElementVisible(moreInfoLink).getText();
    }

    public boolean isAddMetricButtonVisible() {
        return isElementVisible(addMetricButton);
    }

    public SelectItemPopupPanel openMetricPickerDropDown() {
        waitForElementVisible(addMetricButton).click();
        return SelectItemPopupPanel.getInstance(browser);
    }

    public AvailableValuesConfigPanel selectMetric(String metricName) {
        openMetricPickerDropDown().searchAndSelectItem(metricName);
        return this;
    }
}
