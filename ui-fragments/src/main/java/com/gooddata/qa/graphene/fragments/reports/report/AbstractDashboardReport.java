package com.gooddata.qa.graphene.fragments.reports.report;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.DrillingConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.FiltersConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel;

public class AbstractDashboardReport extends AbstractReport {
    @FindBy(css = ".c-report-too-big p")
    protected WebElement reportTooBig;

    @FindBy(css = "button.s-btn-show_anyway")
    protected WebElement showAnywayBtn;

    private static final String CELL_LIMIT = "Report too large to display.";
    private static final String SHOW_ANYWAY = "Show anyway";
    private static final By REPORT_TITLE = By.cssSelector(".yui3-c-reportdashboardwidget-reportTitle > a");

    public void addDrilling(Pair<List<String>, String> pairs, String group) {
        WidgetConfigPanel configPanel = WidgetConfigPanel.openConfigurationPanelFor(getRoot(), browser);
        configPanel.getTab(WidgetConfigPanel.Tab.DRILLING, DrillingConfigPanel.class).addDrilling(pairs, group);
        configPanel.saveConfiguration();
    }

    public void addDrilling(Pair<List<String>, String> pairs) {
        addDrilling(pairs, "Attributes");
    }

    public void editDrilling(Pair<List<String>, String> oldDrilling, Pair<List<String>, String> newDrilling,
            String group) {
        WidgetConfigPanel configPanel = WidgetConfigPanel.openConfigurationPanelFor(getRoot(), browser);
        configPanel.getTab(WidgetConfigPanel.Tab.DRILLING, DrillingConfigPanel.class).editDrilling(oldDrilling,
                newDrilling, group);
        configPanel.saveConfiguration();
    }

    public void deleteDrilling(List<String> drillSourceName) {
        WidgetConfigPanel configPanel = WidgetConfigPanel.openConfigurationPanelFor(getRoot(), browser);
        configPanel.getTab(WidgetConfigPanel.Tab.DRILLING, DrillingConfigPanel.class).deleteDrillingByLeftValues(
                drillSourceName);
        configPanel.saveConfiguration();
    }

    public List<String> getAllFilterNames() {
        WidgetConfigPanel configPanel = WidgetConfigPanel.openConfigurationPanelFor(this.getRoot(), browser);
        List<String> filters =
                configPanel.getTab(WidgetConfigPanel.Tab.FILTERS, FiltersConfigPanel.class).getAllFilters();
        configPanel.discardConfiguration();
        return filters;
    }

    public void removeFilters(String... filters) {
        WidgetConfigPanel configPanel = WidgetConfigPanel.openConfigurationPanelFor(this.getRoot(), browser);
        configPanel.getTab(WidgetConfigPanel.Tab.FILTERS, FiltersConfigPanel.class).removeFiltersFromAffectedList(
                filters);
        configPanel.saveConfiguration();
    }

    public boolean isCellLimit() {
        String text = waitForElementVisible(reportTooBig).getText();
        return (CELL_LIMIT.equals(text) && SHOW_ANYWAY.equals(showAnywayBtn.getText()));
    }

    public void showAnyway() {
        waitForElementVisible(showAnywayBtn).click();
    }

    public boolean isReportTitleVisible() {
        final By reportLabelLocator = By.cssSelector(".yui3-c-reportdashboardwidget-reportTitle > a");

        if (!isElementPresent(reportLabelLocator, getRoot())) {
            return false;
        }

        return !getRoot().findElement(reportLabelLocator).getCssValue("display").startsWith("none");
    }

    public String getReportTiTle() {
        return waitForElementVisible(getRoot().findElement(REPORT_TITLE)).getText();
    }
}
