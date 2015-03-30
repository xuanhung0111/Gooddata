package com.gooddata.qa.graphene.filters;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForDashboardPageLoaded;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractUITest;
import com.gooddata.qa.graphene.enums.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardTabs;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardsPage;
import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.AttributeFilterPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.FilterPanelRow;

public class SimpleFiltersTest extends AbstractUITest {

    private DashboardsPage dashboards;
    private FilterWidget filter;
    private AttributeFilterPanel panel;
    private List<FilterPanelRow> rows;

    @BeforeClass
    public void initStartPage() {
        testParams.setProjectId(testParams.loadProperty("projectId"));

        startPage = PAGE_UI_PROJECT_PREFIX + testParams.getProjectId()
                + "|projectDashboardPage";
    }

    @BeforeMethod
    public void openFilterPanel() {
        if (filter == null) return;

        filter.openPanel();

        panel = filter.getPanel(AttributeFilterPanel.class);
        panel.waitForValuesToLoad();

        rows = panel.getRows();
    }

    @AfterMethod
    public void closeFilterPanel() {
        if (filter == null) return;

        filter.closePanel();
    }

    /**
     * Initial test for dashboard page - verifies/do login at the beginning of
     * the test
     *
     * @throws InterruptedException
     * @throws JSONException
     */
    @Test(groups = {"filterInit"}, alwaysRun = true)
    public void initializeDashboard() throws InterruptedException,
            JSONException {
        // TODO - redirect
        Thread.sleep(5000);
        signIn(false, UserRoles.ADMIN);
        waitForDashboardPageLoaded(browser);
        dashboards = Graphene.createPageFragment(DashboardsPage.class,
                browser.findElement(BY_PANEL_ROOT));
        assertNotNull(dashboards, "Dashboard page not initialized!");

        DashboardTabs tabs = dashboards.getTabs();
        tabs.getTab(0).open();

        List<FilterWidget> filters = dashboards.getFilters();
        assertNotEquals(filters.size(), 0, "No filter on tab!");

        filter = dashboards.getFilters().get(0);
    }

    @Test(dependsOnGroups = {"filterInit"})
    public void testDoesNotDisplayOnlyAnchor() throws InterruptedException {
        for (int i = 0; i < rows.size(); i++) {
            FilterPanelRow row = rows.get(i);

            // Scroll row into view
            ((JavascriptExecutor) browser).executeScript("arguments[0].scrollTop = $(arguments[1]).position().top",
                    panel.getScroller(), row.getRoot());

            // Move cursor away from element
            Actions actions = new Actions(browser);
            actions.moveByOffset(-50, -50).build().perform();

            assertFalse(row.getSelectOnly().isDisplayed(), "'Select only' link is displayed");
        }
    }

    @Test(dependsOnGroups = {"filterInit"})
    public void testDisplaysOnlyAnchorOnHover() throws InterruptedException {
        for (int i = 0; i < rows.size(); i++) {
            FilterPanelRow row = rows.get(i);

            // Scroll row into view
            ((JavascriptExecutor) browser).executeScript("arguments[0].scrollTop = $(arguments[1]).position().top",
                    panel.getScroller(), row.getRoot());

            // Hover over element
            Actions actions = new Actions(browser);
            actions.moveToElement(row.getRoot()).build().perform();

            assertTrue(row.getSelectOnly().isDisplayed(), "'Select only' link is displayed on hover");
        }
    }

    @Test(dependsOnGroups = {"filterInit"})
    public void testSelectOneValueOnSelectOnlyClick() throws InterruptedException {
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

    @Test(dependsOnGroups = {"filterInit"})
    public void testAllValuesAreSelectedByDefault() throws InterruptedException {
        for (int i = 1; i < rows.size(); i++) {
            FilterPanelRow row = rows.get(i);

            assertTrue(row.isSelected(), "Row is selected by default");
        }
    }

    @Test(dependsOnGroups = {"filterInit"})
    public void testDeselectAllValues() throws InterruptedException {
        panel.deselectAll();

        for (int i = 1; i < rows.size(); i++) {
            FilterPanelRow row = rows.get(i);

            assertFalse(row.isSelected(), "Row is not selected");
        }
    }

    @Test(dependsOnGroups = {"filterInit"})
    public void testSelectAllValues() throws InterruptedException {
        panel.deselectAll().selectAll();

        for (int i = 1; i < rows.size(); i++) {
            FilterPanelRow row = rows.get(i);

            assertTrue(row.isSelected(), "Row is selected");
        }
    }

    @Test(dependsOnGroups = {"filterInit"})
    public void testValuesAreFileteredCorrectly() throws InterruptedException {
        rows = panel.search("jon")
                .waitForValuesToLoad()
                .getRows();

        for (int i = 1; i < rows.size(); i++) {
            FilterPanelRow row = rows.get(i);

            assertTrue(!row.getLabel().isDisplayed() || row.getLabel().getText().toLowerCase().contains("jon"),
                    "Row is displayed whan matches search criteria");
        }
    }

    @Test(dependsOnGroups = {"filterInit"})
    public void testSelectAllFiltered() throws InterruptedException {
        panel.deselectAll().search("jon").waitForValuesToLoad();

        panel.selectAll();

        rows = panel.search("\u0008\u0008\u0008")
            .waitForValuesToLoad()
            .getRows();

        for (int i = 1; i < rows.size(); i++) {
            FilterPanelRow row = rows.get(i);

            assertTrue(!row.getLabel().isSelected() || row.getLabel().getText().toLowerCase().contains("jon"),
                    "Row is displayed whan matches search criteria");
        }
    }
}
