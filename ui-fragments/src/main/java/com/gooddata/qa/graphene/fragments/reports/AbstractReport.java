package com.gooddata.qa.graphene.fragments.reports;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.openqa.selenium.By;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.DrillingConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel;

public abstract class AbstractReport extends AbstractFragment {
    public static final By DRILL_REPORT_LOCATOR = By.cssSelector(".c-drillDialog-report");

    public void addDrilling(Pair<List<String>, String> pairs, String group) {
        WidgetConfigPanel configPanel = WidgetConfigPanel.openConfigurationPanelFor(getRoot(), browser);
        configPanel.getTab(WidgetConfigPanel.Tab.DRILLING,
                DrillingConfigPanel.class).addDrilling(pairs, group);
        configPanel.saveConfiguration();
    }

    public void addDrilling(Pair<List<String>, String> pairs) {
        addDrilling(pairs, "Attributes");
    }

    public void editDrilling(Pair<List<String>, String> oldDrilling,
            Pair<List<String>, String> newDrilling, String group) {
        WidgetConfigPanel configPanel = WidgetConfigPanel.openConfigurationPanelFor(getRoot(), browser);
        configPanel.getTab(WidgetConfigPanel.Tab.DRILLING,
                DrillingConfigPanel.class).editDrilling(oldDrilling, newDrilling, group);
        configPanel.saveConfiguration();
    }

    public void deleteDrilling(List<String> drillSourceName) {
        WidgetConfigPanel configPanel = WidgetConfigPanel.openConfigurationPanelFor(getRoot(), browser);
        configPanel.getTab(WidgetConfigPanel.Tab.DRILLING,
                DrillingConfigPanel.class).deleteDrilling(drillSourceName);
        configPanel.saveConfiguration();
    }
}
