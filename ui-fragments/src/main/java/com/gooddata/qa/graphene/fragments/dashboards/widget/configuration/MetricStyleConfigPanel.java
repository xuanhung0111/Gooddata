package com.gooddata.qa.graphene.fragments.dashboards.widget.configuration;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class MetricStyleConfigPanel extends AbstractFragment {

    @FindBy(tagName = "textarea")
    private WebElement numberFormatArea;

    public void editMetricFormat(String format) {
        waitForElementVisible(numberFormatArea).clear();
        numberFormatArea.sendKeys(format);
    }
}
