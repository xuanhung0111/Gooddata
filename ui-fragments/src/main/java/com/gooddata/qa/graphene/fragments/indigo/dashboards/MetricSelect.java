package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.By;

import static com.gooddata.qa.CssUtils.simplifyText;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class MetricSelect extends AbstractFragment {
    private final String metricClassname = ".s-metricItem-${name}";

    @FindBy(css = ".searchfield-input")
    private WebElement searchField;

    @FindBy(css = ".searchfield-clear")
    private WebElement clearSearch;

    public MetricSelect byName(String name) {
        waitForElementVisible(searchField).sendKeys(name);
        By selectedMetric = By.cssSelector(metricClassname.replace("${name}", simplifyText(name)));
        waitForElementVisible(selectedMetric, browser).click();
        waitForElementVisible(clearSearch).click();

        return this;
    }
}
