package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import com.gooddata.qa.graphene.common.CheckUtils;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.By;
/**
 * Kpi - key performance indicator widget
 */
public class Kpi extends AbstractFragment {
    // TODO: when having more widget types, separate, keep "Add widget" in mind
    public static final String MAIN_CLASS = "dash-item";
    public static final String KPI_CSS_SELECTOR = "." + MAIN_CLASS + ":not(.is-placeholder)";

    public static final String WIDGET_LOADING_CLASS = "widget-loading";
    public static final String CONTENT_LOADING_CLASS = "content-loading";

    public static final By IS_WIDGET_LOADING = By.cssSelector("." + MAIN_CLASS + " ." + WIDGET_LOADING_CLASS);
    public static final By IS_CONTENT_LOADING = By.cssSelector("." + MAIN_CLASS + " ." + CONTENT_LOADING_CLASS);
    public static final By IS_NOT_EDITABLE = By.cssSelector("." + MAIN_CLASS + " .kpi:not(.is-editable)");

    @FindBy(css = ".dash-item-delete")
    protected WebElement deleteButton;

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

    @FindBy(className = CONTENT_LOADING_CLASS)
    private WebElement contentLoading;

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

    public void deleteKpi() {
        waitForElementVisible(deleteButton).click();
    }

    public void waitForLoading() {
        waitForElementVisible(contentLoading);
    }
}
