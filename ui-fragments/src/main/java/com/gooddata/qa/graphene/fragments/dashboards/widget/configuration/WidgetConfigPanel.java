package com.gooddata.qa.graphene.fragments.dashboards.widget.configuration;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.dashboards.widget.DashboardEditWidgetToolbarPanel;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class WidgetConfigPanel extends AbstractFragment {

    @FindBy(css = ".configPanel-tabs .s-enabled span")
    private List<WebElement> tabs;

    @FindBy(css = ".configPanel-views>div")
    private List<WebElement> configPanels;

    @FindBy(className = "s-btn-apply")
    private WebElement applyButton;

    @FindBy(className = "s-btn-cancel")
    private WebElement cancelButton;

    public static final By LOCATOR = By
            .cssSelector(".gdc-overlay-simple:not(.hidden):not(.yui3-overlay-hidden):not(.ember-view)");

    public static final WidgetConfigPanel openConfigurationPanelFor(WebElement element,
            SearchContext searchContext) {
        DashboardEditWidgetToolbarPanel.openConfigurationPanelFor(element, searchContext);

        return Graphene.createPageFragment(WidgetConfigPanel.class,
                waitForElementVisible(LOCATOR, searchContext));
    }

    public <T extends AbstractFragment> T getTab(final Tab tab, Class<T> clazz) {
        waitForCollectionIsNotEmpty(tabs);
        WebElement tabElement = Iterables.find(tabs, new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement input) {
                return tab.tabName.equals(input.getText().trim());
            }
        });

        if (!tabElement.findElement(BY_PARENT).getAttribute("class")
                .contains("yui3-c-label-selected"))
            tabElement.click();
        return Graphene.createPageFragment(clazz, waitForElementVisible(getConfigPanel(tab)));
    }

    public void saveConfiguration() {
        waitForElementVisible(applyButton).click();
        waitForFragmentNotVisible(this);
    }

    public void discardConfiguration() {
        waitForElementVisible(cancelButton).click();
        waitForFragmentNotVisible(this);
    }

    private WebElement getConfigPanel(final Tab tab) {
        waitForCollectionIsNotEmpty(configPanels);
        return Iterables.find(configPanels, new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement panel) {
                return panel.getAttribute("class").contains(tab.panelClassName);
            }
        });
    }

    public enum Tab {
        DRILLING("Drilling", "s-Drilling"),
        PARENT_FILTERS("Parent Filters", "s-Parent"),
        SELECTION("Selection", "s-Selection"),
        FILTERS("Filters", "filterTab"),
        GROUP("Group", "filterTab"),
        ARRANGE("Arrange", "s-Arrange"),
        STYLE("Style", "s-Style"),
        METRIC("Metric", "s-Metric"),
        METRIC_STYLE("Style", "s-activeConfigTab");

        private final String tabName;
        private final String panelClassName;

        private Tab(String tab, String panel) {
            this.tabName = tab;
            this.panelClassName = panel;
        }
    }
}
