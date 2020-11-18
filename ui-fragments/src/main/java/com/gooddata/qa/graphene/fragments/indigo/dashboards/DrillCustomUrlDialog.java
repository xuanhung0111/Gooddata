package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;

import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import java.util.List;

public class DrillCustomUrlDialog extends AbstractFragment {

    @FindBy(className = "s-dialog-cancel-button")
    private WebElement cancelButton;

    @FindBy(className = "s-dialog-submit-button")
    private WebElement submitButton;

    @FindBy(className = "s-dialog-close-button")
    private WebElement closeButton;

    @FindBy(id = "project_id")
    private WebElement projectID;

    @FindBy(id = "dashboard_id")
    private WebElement dashboardID;

    @FindBy(id = "widget_id")
    private WebElement widgetID;

    @FindBy(id = "insight_id")
    private WebElement insightID;

    @FindBy(className = "CodeMirror-code")
    private WebElement UrlInput;

    @FindBy(className = "icon-hyperlink-warning")
    private List<WebElement> insights;

    private static By ROOT = className("s-gd-drill-custom-url-editor");

    public static DrillCustomUrlDialog getInstance(final SearchContext searchContext) {
        return Graphene.createPageFragment(DrillCustomUrlDialog.class,
            waitForElementVisible(ROOT, searchContext));
    }

    public void cancel() {
        waitForElementVisible(cancelButton).click();
        waitForFragmentNotVisible(this);
    }

    public void apply() {
        waitForElementVisible(submitButton).click();
        waitForFragmentNotVisible(this);
    }

    public void close() {
        waitForElementVisible(closeButton).click();
        waitForFragmentNotVisible(this);
    }

    public DrillCustomUrlDialog inputUrl(String url) {
        String javaScript = "document.querySelector(\".CodeMirror\").CodeMirror.setValue('" + url + "');";
        ((JavascriptExecutor)browser).executeScript(javaScript); 
        return this;
    }

    public String getToolTipFromVisibilityQuestionIcon(String IdItem) {
        new Actions(browser).moveToElement(waitForElementVisible(cssSelector(IdItem), browser))
            .moveByOffset(1, 1).perform();
        new Actions(browser).moveToElement(waitForElementVisible(cssSelector(IdItem + " .gd-list-item-tooltip"), browser))
            .moveByOffset(1, 1).perform();
        return browser.findElements(className("s-parameter-detail-value")).stream()
            .findFirst()
            .get()
            .getText();
    }

    public DrillCustomUrlDialog addProjectID() {
        waitForElementVisible(projectID).click();
        return this;
    }

    public DrillCustomUrlDialog addInsightID() {
        waitForElementVisible(insightID).click();
        return this;
    }

    public DrillCustomUrlDialog addWidgetID() {
        waitForElementVisible(widgetID).click();
        return this;
    }

    public DrillCustomUrlDialog addDashboardID() {
        waitForElementVisible(dashboardID).click();
        return this;
    }

    public DrillCustomUrlDialog addInsight(String title) {
        insights.stream()
            .filter(insight -> {
                return waitForElementVisible(insight).findElement(className("gd-parameter-title")).getText().contains(title);
            })
            .findFirst()
            .get()
            .click();
        return this;
    }

    public WebElement getInsight(String title) {
        return insights.stream()
            .filter(insight -> {
                return waitForElementVisible(insight).findElement(className("gd-parameter-title")).getText().contains(title);
            })
            .findFirst()
            .get();
    }

    public static boolean isPresent(final SearchContext searchContext) {
        return isElementPresent(ROOT, searchContext);
    }
}
