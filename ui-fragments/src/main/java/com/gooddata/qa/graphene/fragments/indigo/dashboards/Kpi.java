package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
/**
 * Kpi - key performance indicator widget
 */
public class Kpi extends AbstractFragment {
    public static final String MAIN_CLASS = "s-dashboard-kpi-component";

    public static final By IS_LOADING = By.cssSelector("." + MAIN_CLASS + ".is-loading");

    @FindBy(css = ".kpi-headline > h3")
    private WebElement headline;

    @FindBy(css = ".kpi-headline > h3 .inplaceedit")
    private WebElement headlineInplaceEdit;

    @FindBy(css = ".kpi-headline > h3 textarea")
    private WebElement headlineTextarea;

    @FindBy(css = ".kpi-value")
    private WebElement value;

    @FindBy(css = ".kpi-metrics")
    private WebElement metric;

    public String getHeadline() {
        return waitForElementVisible(headline).getText();
    }

    public void setHeadline(String newHeadline) {
        waitForElementVisible(headlineInplaceEdit).click();
        waitForElementVisible(headlineTextarea).clear();
        headlineTextarea.sendKeys(newHeadline);
        headlineTextarea.sendKeys(Keys.ENTER);
    }

    public String getValue() {
        return waitForElementVisible(value).getText();
    }

    public String getMetric() {
        return waitForElementVisible(metric).getText();
    }
}
