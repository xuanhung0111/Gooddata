package com.gooddata.qa.graphene.filters;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.sonatype.aether.util.filter.PatternExclusionsDependencyFilter;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardTabs;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardsPage;
import com.gooddata.qa.graphene.fragments.dashboards.FilterWidget;

public class SimpleFiltersTest extends AbstractTest {

	DashboardsPage dashboards;

	FilterWidget filter;
	
	FilterWidget.FilterPanel panel;
	
	List<FilterWidget.FilterPanel.FilterPanelRow> rows;
	
	@BeforeClass
	public void initStartPage() {
		projectId = loadProperty("projectId");
		projectName = loadProperty("projectName");

		startPage = PAGE_UI_PROJECT_PREFIX + projectId
				+ "|projectDashboardPage";
	}
	
	@BeforeMethod
	public void openFilterPanel() {
		if (filter == null) return;
		
		filter.openPanel();
		
		panel = filter.getPanel();
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
	@Test(groups = { "filterInit" }, alwaysRun = true)
	public void initializeDashboard() throws InterruptedException,
			JSONException {
		// TODO - redirect
		Thread.sleep(5000);
		validSignInWithDemoUser(false);
		waitForDashboardPageLoaded();
		dashboards = Graphene.createPageFragment(DashboardsPage.class,
				browser.findElement(BY_PANEL_ROOT));
		Assert.assertNotNull(dashboards, "Dashboard page not initialized!");

		DashboardTabs tabs = dashboards.getTabs();
		tabs.getTab(0).open();

		List<FilterWidget> filters = dashboards.getFilters();
		Assert.assertNotEquals(filters.size(), 0, "No filter on tab!");

		filter = dashboards.getFilters().get(0);
	}
	
	@Test(dependsOnGroups = { "filterInit" })
	public void testDoesNotDisplayOnlyAnchor() throws InterruptedException {
		for (int i = 0; i < rows.size(); i++) {
			FilterWidget.FilterPanel.FilterPanelRow row = rows.get(i);
			
			// Scroll row into view
			((JavascriptExecutor) browser).executeScript("arguments[0].scrollTop = $(arguments[1]).position().top", panel.getScroller(), row.getRoot());
			
			// Move cursor away from element
			Actions actions = new Actions(browser);
			actions.moveByOffset(-50, -50).build().perform();
			
			Assert.assertFalse(row.getSelectOnly().isDisplayed(), "'Select only' link is displayed");
		}
	}
	
	@Test(dependsOnGroups = { "filterInit" })
	public void testDisplaysOnlyAnchorOnHover() throws InterruptedException {
		for (int i = 0; i < rows.size(); i++) {
			FilterWidget.FilterPanel.FilterPanelRow row = rows.get(i);
			
			// Scroll row into view
			((JavascriptExecutor) browser).executeScript("arguments[0].scrollTop = $(arguments[1]).position().top", panel.getScroller(), row.getRoot());
			
			// Hover over element
			Actions actions = new Actions(browser);
			actions.moveToElement(row.getRoot()).build().perform();
			
			Assert.assertTrue(row.getSelectOnly().isDisplayed(), "'Select only' link is displayed on hover");
		}
	}
	
	@Test(dependsOnGroups = { "filterInit" })
	public void testSelectOneValueOnSelectOnlyClick() throws InterruptedException {
		FilterWidget.FilterPanel.FilterPanelRow selectedRow = rows.get(0);
		
		// Hover over selected row
		Actions actions = new Actions(browser);
		actions.moveToElement(selectedRow.getRoot()).build().perform();
		
		// Select first value
		// Due to some weird black magic link does not react to clicks until it is typed to
		rows.get(0).getSelectOnly().sendKeys("something");
		rows.get(0).getSelectOnly().click();
		
		Assert.assertTrue(selectedRow.isSelected(), "Row is selected after click on 'Select only' link");
		
		for (int i = 1; i < rows.size(); i++) {
			FilterWidget.FilterPanel.FilterPanelRow row = rows.get(i);
			
			Assert.assertFalse(row.isSelected(), "Only one row is selected after click on 'Select only' link");
		}
	}
	
	@Test(dependsOnGroups = { "filterInit" })
	public void testAllValuesAreSelectedByDefault() throws InterruptedException {
		for (int i = 1; i < rows.size(); i++) {
			FilterWidget.FilterPanel.FilterPanelRow row = rows.get(i);
			
			Assert.assertTrue(row.isSelected(), "Row is selected by default");
		}
	}
	
	@Test(dependsOnGroups = { "filterInit" })
	public void testDeselectAllValues() throws InterruptedException {
		panel.getDeselectAll().click();
		
		for (int i = 1; i < rows.size(); i++) {
			FilterWidget.FilterPanel.FilterPanelRow row = rows.get(i);
			
			Assert.assertFalse(row.isSelected(), "Row is not selected");
		}
	}
	
	@Test(dependsOnGroups = { "filterInit" })
	public void testSelectAllValues() throws InterruptedException {
		panel.getDeselectAll().click();
		panel.getSelectAll().click();
		
		for (int i = 1; i < rows.size(); i++) {
			FilterWidget.FilterPanel.FilterPanelRow row = rows.get(i);
			
			Assert.assertTrue(row.isSelected(), "Row is selected");
		}
	}
	
	@Test(dependsOnGroups = { "filterInit" })
	public void testValuesAreFileteredCorrectly() throws InterruptedException {
		panel.getSearch().sendKeys("jon");
		panel.waitForValuesToLoad();
		rows = panel.getRows();
		
		for (int i = 1; i < rows.size(); i++) {
			FilterWidget.FilterPanel.FilterPanelRow row = rows.get(i);
			
			Assert.assertTrue(!row.getLabel().isDisplayed() || row.getLabel().getText().toLowerCase().contains("jon"), "Row is displayed whan matches search criteria");
		}
	}
	
	@Test(dependsOnGroups = { "filterInit" })
	public void testSelectAllFiltered() throws InterruptedException {
		panel.getDeselectAll().click();
		
		panel.getSearch().sendKeys("jon");
		panel.waitForValuesToLoad();
		
		panel.getSelectAll().click();
		
		panel.getSearch().sendKeys("\u0008\u0008\u0008");
		panel.waitForValuesToLoad();
		
		rows = panel.getRows();
		
		for (int i = 1; i < rows.size(); i++) {
			FilterWidget.FilterPanel.FilterPanelRow row = rows.get(i);
			
			Assert.assertTrue(!row.getLabel().isSelected() || row.getLabel().getText().toLowerCase().contains("jon"), "Row is displayed whan matches search criteria");
		}
	}
	
}
