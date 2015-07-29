package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.By;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class MetricSelect extends AbstractFragment {

    private static final String METRIC_BY_TEXT_LOCATOR =
            "//*[contains(@class, 'gd-list-view-item') and text()='${text}']";

    @FindBy(css = ".searchfield-input")
    private WebElement searchField;

    @FindBy(css = ".searchfield-clear")
    private WebElement clearSearch;

    public MetricSelect byName(String text) {
        waitForElementVisible(searchField).sendKeys(text);
        By selectedMetric = By.xpath(METRIC_BY_TEXT_LOCATOR.replace("${text}", text));
        waitForElementVisible(selectedMetric, browser).click();
        waitForElementVisible(clearSearch).click();

        return this;
    }
}
