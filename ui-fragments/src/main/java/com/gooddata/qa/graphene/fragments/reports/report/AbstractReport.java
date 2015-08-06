package com.gooddata.qa.graphene.fragments.reports.report;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.dashboards.ReportInfoViewPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.DrillingConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.FiltersConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel;

public abstract class AbstractReport extends AbstractFragment {

    @FindBy(css = ".reportInfoPanelHandle")
    protected WebElement reportInfoButton;

    @FindBy(css = ".c-report-too-big p")
    protected WebElement reportTooBig;

    @FindBy(css = "button.s-btn-show_anyway_br__may_crash_browser_")
    protected WebElement showAnywayBtn;

    public static final By DRILL_REPORT_LOCATOR = By.cssSelector(".c-drillDialog-report");

    private static final String CELL_LIMIT = "Report too large to display.";
    
    private static final String SHOW_ANYWAY = "Show anyway\n(may crash browser)";

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

    public ReportInfoViewPanel openReportInfoViewPanel() {
        waitForElementVisible(this.getRoot());
        waitForElementPresent(reportInfoButton);
        new Actions(browser).moveToElement(this.getRoot()).perform();
        waitForElementVisible(reportInfoButton).click();
        return Graphene.createPageFragment(ReportInfoViewPanel.class,
                waitForElementVisible(By.cssSelector(".reportInfoView"), browser));
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

    public boolean areAllFiltersDisabled() {
        WidgetConfigPanel configPanel = WidgetConfigPanel.openConfigurationPanelFor(this.getRoot(), browser);
        boolean ret =
                configPanel.getTab(WidgetConfigPanel.Tab.FILTERS, FiltersConfigPanel.class)
                        .areAllFiltersDisabled();
        configPanel.discardConfiguration();
        return ret;
    }

    public boolean isCellLimit() {
        String text = waitForElementVisible(reportTooBig).getText();
        return (CELL_LIMIT.equals(text) && SHOW_ANYWAY.equals(showAnywayBtn.getText()));
    }

    public void showAnyway() {
        waitForElementVisible(showAnywayBtn).click();
    }
}
