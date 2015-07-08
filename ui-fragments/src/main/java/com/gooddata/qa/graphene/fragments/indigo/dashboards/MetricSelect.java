package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.By;

import static com.gooddata.qa.CssUtils.simplifyText;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;

public class MetricSelect extends AbstractFragment {
    private String metricClassname = ".s-metricItem-${name}";

    public MetricSelect byName(String name) {
        By selectedMetric = By.cssSelector(metricClassname.replace("${name}", simplifyText(name)));
        waitForElementVisible(selectedMetric, browser).click();

        return this;
    }
}
