package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;

import java.text.ParseException;
import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.indigo.ShortcutPanel;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport;

public class MainEditor extends AbstractFragment {

    @FindBy(css = ".s-bucket-filters")
    private FiltersBucket bucketsFilter;

    @FindBy(css = CSS_EXPLORER_MESSAGE)
    private WebElement explorerMessage;

    private static final String CSS_EXPLORER_MESSAGE = ".adi-canvas-message h2";
    private static final String CSS_REPORT = ".adi-chart-container:not(.invisible)";
    private static final By BY_TABLE_REPORT = By.cssSelector(".dda-table-component-content");
    private static final By BY_CHART_REPORT = By.cssSelector(".switchable-visualization-component");
    private static final By BY_REPORT_COMPUTING = By.cssSelector(".adi-computing");

    public void addFilter(WebElement filter) {
        waitForFragmentVisible(bucketsFilter);
        bucketsFilter.addFilter(filter);
    }

    public void removeFilter(String dateOrAttribute) {
        waitForFragmentVisible(bucketsFilter);
        bucketsFilter.removeFilter(dateOrAttribute);
    }

    public boolean isBlankState() {
        waitForFragmentVisible(bucketsFilter);
        return browser.findElements(By.cssSelector(CSS_REPORT)).size() == 0
                && bucketsFilter.isBlankState();
    }

    public void configTimeFilter(String period) {
        waitForFragmentVisible(bucketsFilter);
        bucketsFilter.configTimeFilter(period);
    }

    public void configAttributeFilter(String attribute, String... values) {
        waitForFragmentVisible(bucketsFilter);
        bucketsFilter.configAttributeFilter(attribute, values);
    }

    public TableReport getTableReport() {
        return Graphene.createPageFragment(TableReport.class,
                waitForElementVisible(BY_TABLE_REPORT, browser));
    }

    public ChartReport getChartReport() {
        return Graphene.createPageFragment(ChartReport.class,
                waitForElementVisible(BY_CHART_REPORT, browser));
    }

    public boolean isFilterVisible(String dateOrAttribute) {
        waitForFragmentVisible(bucketsFilter);
        return bucketsFilter.isFilterVisible(dateOrAttribute);
    }

    public String getFilterText(String dateOrAttribute) {
        waitForFragmentVisible(bucketsFilter);
        return bucketsFilter.getFilterText(dateOrAttribute);
    }

    public void dragAndDropMetricToShortcutPanel(WebElement metric, ShortcutPanel shortcutPanel) {
        Actions action = new Actions(browser);

        Point location = getRoot().getLocation();
        Dimension dimension = getRoot().getSize();
        action.clickAndHold(metric)
            .moveByOffset(location.x + dimension.width/2, location.y + dimension.height/2)
            .perform();
        try {
            action.moveToElement(waitForElementVisible(shortcutPanel.getLocator(), getRoot())).perform();
        } finally {
            action.release().perform();
        }
    }

    public String getExplorerMessage() {
        return waitForElementVisible(explorerMessage).getText().trim();
    }

    public WebElement getFilter(String dateOrAttribute) {
        waitForFragmentVisible(bucketsFilter);
        return bucketsFilter.getFilter(dateOrAttribute);
    }

    public boolean isExplorerMessageVisible() {
        return browser.findElements(By.cssSelector(CSS_EXPLORER_MESSAGE)).size() > 0;
    }

    public List<String> getAllTimeFilterOptions() {
        return waitForFragmentVisible(bucketsFilter).getAllTimeFilterOptions();
    }

    public void waitForReportComputing() {
        try {
            Thread.sleep(1000);
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
        return !browser.findElements(BY_REPORT_COMPUTING).isEmpty();
    }

    /**
     * @param from format MM/DD/YYYY
     * @param to   format MM/DD/YYYY
     */
    public void configTimeFilterByRangeButNotApply(String dateFilter, String from, String to) {
        waitForFragmentVisible(bucketsFilter).configTimeFilterByRangeButNotApply(dateFilter, from, to);
    }

    /**
     * @param from format MM/DD/YYYY
     * @param to   format MM/DD/YYYY
     * @throws ParseException 
     */
    public void configTimeFilterByRange(String dateFilter, String from, String to) throws ParseException {
        waitForFragmentVisible(bucketsFilter).configTimeFilterByRange(dateFilter, from, to);
    }

    public void changeDimensionSwitchInFilter(String currentRelatedDate, String dimensionSwitch) {
        waitForFragmentVisible(bucketsFilter).changeDimensionSwitchInFilter(currentRelatedDate, dimensionSwitch);
    }

    public boolean isDateFilterVisible() {
        return waitForFragmentVisible(bucketsFilter).isDateFilterVisible();
    }

    public String getDateFilterText() {
        return waitForFragmentVisible(bucketsFilter).getDateFilterText();
    }
}
