package com.gooddata.qa.graphene.fragments.dashboards;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.dashboards.widget.DashboardEditWidgetToolbarPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.ParentFiltersConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel;
import com.gooddata.qa.utils.CssUtils;

/**
 * This fragment is for editing filter when dashboard in edit mode
 *
 */
public class DashboardEditFilter extends AbstractFragment{

    @FindBy(xpath = "//div[contains(@class,'s-active-tab')]//div[contains(@class,'yui3-c-dashboardwidget-editMode')]")
    private List<WebElement> filters;

    @FindBy(xpath = "//div[contains(@class,'yui3-c-tabtimefiltereditor-content')]")
    private TimeFilterEditorPanel timeFilterEditorPanel;

    /**
     * return time filter in dashboard
     * 
     * @return
     */
    public WebElement getTimeFilter() {
        for (WebElement filter : filters) {
            if (filter.getAttribute("class").contains("s-filter-time")) return filter;
        }
        return null;
    }

    /**
     * return filter in dashboard base on its name. It should work for Attribute & Date filter
     * 
     * @param name
     * @return
     */
    public WebElement getFilter(String name) {
        for (WebElement filter : filters) {
            if (filter.getAttribute("class").contains("s-" + CssUtils.simplifyText(name)))
                return filter;
        }
        return null;
    }

    /**
     * delete first time filter
     *
     */
    public void deleteTimeFilter() {
        DashboardEditWidgetToolbarPanel.removeWidget(getTimeFilter(), browser);
        sleepTightInSeconds(1);
        Assert.assertFalse(isDashboardContainsFilter("time"));
    }

    /**
     * check if dashboard contains time filter of not
     * 
     * @param timeOrAttribute
     * @return
     */
    public boolean isDashboardContainsFilter(String timeOrAttribute) {
        for (WebElement filter : filters) {
            if (filter.getAttribute("class").contains(String.format("s-%s", "time".equals(timeOrAttribute) ? "filter-time" : timeOrAttribute)))
                return true;
        }
        return false;
    }

    /**
     * Change type of time filter: Day, Week, Month, Quarter, Year
     * 
     * Just support basic cases
     * 
     * @param   type
     */
    public void changeTypeOfTimeFilter(String type) {
        DashboardEditWidgetToolbarPanel.openEditPanelFor(getTimeFilter(), browser);
        waitForElementVisible(By.xpath(String.format(TimeFilterEditorPanel.TYPE, type)), browser).click();
        waitForElementVisible(timeFilterEditorPanel.applyButton).click();
        waitForElementNotVisible(timeFilterEditorPanel.getRoot());
    }

    /**
     * add some parent filters for a filter
     * 
     * @param parentFilterNames
     */
    public void addParentFilters(String filterName, String... parentFilterNames) {
        openWidgetConfigPanel(filterName).getTab(WidgetConfigPanel.Tab.PARENT_FILTERS, ParentFiltersConfigPanel.class)
            .addParentsFilter(parentFilterNames);
    }

    public void addParentFiltersUsingDataset(String filterName, String linkedDataset, String... parentFilterNames) {
        openWidgetConfigPanel(filterName).getTab(WidgetConfigPanel.Tab.PARENT_FILTERS, ParentFiltersConfigPanel.class)
            .addParentsFilterUsingDataset(linkedDataset, parentFilterNames);
    }

    public WidgetConfigPanel openWidgetConfigPanel(String filterName) {
        return WidgetConfigPanel.openConfigurationPanelFor(getFilter(filterName), browser);
    }

    public DashboardEditFilter focusOnFilter(String filterName) {
        if (!isFilterSelected(filterName))
            getFilter(filterName).click();

        return this;
    }

    private boolean isFilterSelected(String filterName) {
        return getFilter(filterName).getAttribute("class").contains("yui3-c-filterdashboardwidget-selected");
    }

    /**
     * panel for re-editing time filter type
     *
     */
    private static class TimeFilterEditorPanel extends AbstractFragment {

        static final String TYPE = "//span[text()='%s']";

        @FindBy(xpath = "//div[contains(@class,'timefiltereditor')]//button[contains(@class,'s-btn-apply')]")
        private WebElement applyButton;
    }
}
