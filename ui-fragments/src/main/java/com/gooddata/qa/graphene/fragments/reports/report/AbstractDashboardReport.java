package com.gooddata.qa.graphene.fragments.reports.report;

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

    public static final By DRILL_REPORT_LOCATOR = By.cssSelector(".c-drillDialog-report");

    private static final String CELL_LIMIT = "Report too large to display.";
    
    private static final String SHOW_ANYWAY = "Show anyway";

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
        configPanel.getTab(WidgetConfigPanel.Tab.DRILLING, DrillingConfigPanel.class).deleteDrilling(
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
}
