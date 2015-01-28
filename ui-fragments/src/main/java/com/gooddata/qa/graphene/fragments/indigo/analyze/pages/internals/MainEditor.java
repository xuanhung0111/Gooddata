package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForFragmentVisible;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.enricher.WebElementUtils;
import org.openqa.selenium.By;
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

    @FindBy(css = ".adi-canvas-message h2")
    private WebElement explorerMessage;

    private static final String CSS_REPORT = ".adi-chart-container:not(.invisible)";
    private static final By BY_TABLE_REPORT = By.cssSelector(".dda-table-component");
    private static final By BY_CHART_REPORT = By.cssSelector(".switchable-visualization-component");

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
        new Actions(browser).clickAndHold(metric).moveToElement(getRoot())
                .release(WebElementUtils.findElementLazily(shortcutPanel.getLocator(), getRoot()))
                .perform();
    }

    public String getExplorerMessage() {
        return waitForElementVisible(explorerMessage).getText().trim();
    }

    public WebElement getFilter(String dateOrAttribute) {
        waitForFragmentVisible(bucketsFilter);
        return bucketsFilter.getFilter(dateOrAttribute);
    }
}
