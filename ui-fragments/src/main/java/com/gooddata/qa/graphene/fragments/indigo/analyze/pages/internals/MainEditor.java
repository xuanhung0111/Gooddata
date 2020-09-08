package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.PivotTableReport;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;

/**
 * dda-table-component-content renamed to indigo-table-component-content
 * switchable-visualization-component renamed to highcharts-container
 * TableReportReact fragment is used instead of TableReport
 */
public class MainEditor extends AbstractFragment {

    @FindBy(css = CSS_EXPLORER_MESSAGE)
    private WebElement explorerMessage;

    @FindBy(className = "adi-canvas-message")
    private WebElement canvasMessage;

    @FindBy(className = WARNING_UNSUPPORTED_MESSAGE_CLASS_NAME)
    private WebElement warningUnsupportedMessage;

    private static final String CSS_EXPLORER_MESSAGE = ".adi-canvas-message h2";
    private static final String CSS_REPORT_EMPTY = ".s-blank-canvas-message";
    private static final String WARNING_UNSUPPORTED_MESSAGE_CLASS_NAME = "s-hidden-items-notification";
    private static final By BY_PIVOT_TABLE_REPORT = By.className("s-pivot-table");
    private static final By BY_CHART_REPORT = By.className("adi-report-visualization");
    private static final By BY_REPORT_COMPUTING = By.className("adi-computing");
    private static final By BY_REPORT_NO_DATA = By.className("s-error-empty-result");

    public boolean isEmpty() {
        return isElementPresent(cssSelector(CSS_REPORT_EMPTY), browser);
    }

    public String getReportEmpty() {
        return waitForElementVisible(cssSelector(CSS_REPORT_EMPTY), getRoot()).getText();
    }

    public boolean isNoData() {
        return isElementPresent(BY_REPORT_NO_DATA, browser);
    }

    public PivotTableReport getPivotTableReport() {
        try {
            sleepTightInSeconds(1);
            if (isElementPresent(className("s-loading"), browser)) {
                WebElement computingElement = browser.findElement(className("s-loading"));
                waitForElementNotVisible(computingElement);
            }
        } catch(Exception e) {
            // in case report is rendered so fast, loading icon is not shown.
            // Ignore the exception.
        }

        return Graphene.createPageFragment(PivotTableReport.class,
                waitForElementVisible(BY_PIVOT_TABLE_REPORT, browser));
    }

    public ChartReport getChartReport() {
        return Graphene.createPageFragment(ChartReport.class,
                waitForElementVisible(BY_CHART_REPORT, browser));
    }

    public String getExplorerMessage() {
        return waitForElementVisible(explorerMessage).getText().trim();
    }

    public String getCanvasMessage() {
        return waitForElementVisible(canvasMessage).getText();
    }

    public String getWarningUnsupportedMessage() {
        return waitForElementVisible(warningUnsupportedMessage).getText();
    }

    public Boolean isWarningUnsupportedMessageVisible() {
        return isElementVisible(By.className(WARNING_UNSUPPORTED_MESSAGE_CLASS_NAME), browser);
    }

    public boolean isExplorerMessageVisible() {
        return browser.findElements(By.cssSelector(CSS_EXPLORER_MESSAGE)).size() > 0;
    }

    public void waitForReportComputing() {
        try {
            sleepTightInSeconds(1);
            if (isReportComputing()) {
                WebElement computingElement = browser.findElement(BY_REPORT_COMPUTING);
                waitForElementNotVisible(computingElement);
            }
        } catch(Exception e) {
            // in case report is rendered so fast, computing label is not shown.
            // Ignore the exception.
        }
    }

    public boolean isReportComputing() {
        return isElementPresent(BY_REPORT_COMPUTING, browser);
    }
}
