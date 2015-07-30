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

    public static final By IS_WIDGET_LOADING = By.cssSelector("." + MAIN_CLASS + ".widget-loading");

    public static final By IS_CONTENT_LOADING = By.cssSelector("." + MAIN_CLASS + ".content-loading");

    public static final By IS_EDITABLE = By.cssSelector("." + MAIN_CLASS + ".is-editable");

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

    public void clearHeadline() {
        waitForElementVisible(headlineInplaceEdit).click();

        // hit backspace multiple times, because .clear()
        // event does not trigger onchange event
        // https://selenium.googlecode.com/svn/trunk/docs/api/java/org/openqa/selenium/WebElement.html#clear%28%29
        waitForElementVisible(headlineTextarea);
        int headlineLength = headlineInplaceEdit.getText().length();
        for (int i = 0; i < headlineLength; i++) {
            headlineTextarea.sendKeys(Keys.BACK_SPACE);
        }
    }

    public void setHeadline(String newHeadline) {
        clearHeadline();
        headlineTextarea.sendKeys(newHeadline);
        headlineTextarea.sendKeys(Keys.ENTER);

        waitForElementVisible(headlineInplaceEdit);
    }

    public String getValue() {
        return waitForElementVisible(value).getText();
    }

    public String getMetric() {
        return waitForElementVisible(metric).getText();
    }
}
