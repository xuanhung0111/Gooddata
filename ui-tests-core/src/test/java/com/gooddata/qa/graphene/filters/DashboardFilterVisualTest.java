package com.gooddata.qa.graphene.filters;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.AttributeFilterPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.FilterPanelRow;

public class DashboardFilterVisualTest extends GoodSalesAbstractTest {

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-dashboard-filter-visual";
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testDoesNotDisplayOnlyAnchor() {
        List<FilterPanelRow> rows = getProductFilterInFirstTab().getPanel(AttributeFilterPanel.class)
                .getRows();

        for (int i = 0; i < rows.size(); i++) {
            FilterPanelRow row = rows.get(i);

            // Move cursor away from element
            Actions actions = new Actions(browser);
            actions.moveByOffset(-50, -50).build().perform();

            assertFalse(row.isSelectOnlyDisplayed(), "'Select only' link is displayed");
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testDisplaysOnlyAnchorOnHover() {
        List<FilterPanelRow> rows = getProductFilterInFirstTab().getPanel(AttributeFilterPanel.class)
                .getRows();

        for (int i = 0; i < rows.size(); i++) {
            FilterPanelRow row = rows.get(i);

            // Hover over element
            Actions actions = new Actions(browser);
            actions.moveToElement(row.getRoot()).build().perform();

            assertTrue(row.isSelectOnlyDisplayed(), "'Select only' link is displayed on hover");
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testSelectOneValueOnSelectOnlyClick() {
        List<FilterPanelRow> rows = getProductFilterInFirstTab().getPanel(AttributeFilterPanel.class)
                .getRows();
        FilterPanelRow selectedRow = rows.get(0);

        // Hover over selected row
        Actions actions = new Actions(browser);
        actions.moveToElement(selectedRow.getRoot()).build().perform();

        // Select first value
        // Due to some weird black magic link does not react to clicks until it is typed to
        rows.get(0).getSelectOnly().sendKeys("something");
        rows.get(0).getSelectOnly().click();

        assertTrue(selectedRow.isSelected(), "Row is selected after click on 'Select only' link");

        for (int i = 1; i < rows.size(); i++) {
            FilterPanelRow row = rows.get(i);

            assertFalse(row.isSelected(), "Only one row is selected after click on 'Select only' link");
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testSelectAllFiltered() {
        List<FilterPanelRow> rows = getProductFilterInFirstTab().getPanel(AttributeFilterPanel.class)
                .deselectAll()
                .search("on")
                .waitForValuesToLoad()
                .selectAll()
                .search("\u0008\u0008\u0008")
                .waitForValuesToLoad()
                .getRows();

        for (int i = 1; i < rows.size(); i++) {
            FilterPanelRow row = rows.get(i);

            assertTrue(!row.getLabel().isSelected() || row.getLabel().getText().toLowerCase().contains("on"),
                    "Row is displayed whan matches search criteria");
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testAllValuesAreSelectedByDefault() {
        List<FilterPanelRow> rows = getProductFilterInFirstTab().getPanel(AttributeFilterPanel.class)
                .getRows();

        for (int i = 1; i < rows.size(); i++) {
            FilterPanelRow row = rows.get(i);

            assertTrue(row.isSelected(), "Row is selected by default");
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testDeselectAllValues() {
        List<FilterPanelRow> rows = getProductFilterInFirstTab().getPanel(AttributeFilterPanel.class)
                .deselectAll()
                .getRows();

        for (int i = 1; i < rows.size(); i++) {
            FilterPanelRow row = rows.get(i);

            assertFalse(row.isSelected(), "Row is not selected");
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testSelectAllValues() {
        List<FilterPanelRow> rows = getProductFilterInFirstTab().getPanel(AttributeFilterPanel.class)
                .deselectAll()
                .selectAll()
                .getRows();

        for (int i = 1; i < rows.size(); i++) {
            FilterPanelRow row = rows.get(i);

            assertTrue(row.isSelected(), "Row is selected");
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testValuesAreFileteredCorrectly() {
        List<FilterPanelRow> rows = getProductFilterInFirstTab().getPanel(AttributeFilterPanel.class)
                .search("on")
                .waitForValuesToLoad()
                .getRows();

        for (int i = 1; i < rows.size(); i++) {
            FilterPanelRow row = rows.get(i);

            assertTrue(!row.getLabel().isDisplayed() || row.getLabel().getText().toLowerCase().contains("on"),
                    "Row is displayed whan matches search criteria");
        }
    }

    private FilterWidget getProductFilterInFirstTab() {
        initDashboardsPage();
        dashboardsPage.getTabs().openTab(0);

        return dashboardsPage.getContent().getFilterWidget("product").openPanel();
    }
}
