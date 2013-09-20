package com.gooddata.qa.graphene.fragments.dashboards;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardTabs;

public class DashboardsPage extends AbstractFragment {
	
	@FindBy(xpath="//div[@id='abovePage']/div[contains(@class,'yui3-dashboardtabs-content')]/div[contains(@class,'c-collectionWidget')]/div")
	private DashboardTabs tabs;
	
	@FindBy(css=".q-dashboardSwitcher")
	private WebElement dashboardSwitcherButton;
	
	@FindBy(css=".menuArrow")
	private WebElement dashboardSwitcherArrowMenu;
	
	@FindBy(css=".s-dashboards-menu-item")
	private List<WebElement> dashboardSelectors;
	
	@FindBy(xpath="//button[@title='Edit, Embed or Export']")
	private WebElement editExportEmbedButton;
	
	@FindBy(css=".s-edit")
	private WebElement editButton;
	
	@FindBy(css=".s-export_to_pdf")
	private WebElement exportPdfButton;
	
	@FindBy(css=".s-embed")
	private WebElement embedButton;
	
	@FindBy(css=".s-add_dashboard")
	private WebElement addDashboardButton;
	
	@FindBy(xpath="//button[@title='Add a new tab']")
	private WebElement addNewTabButton;
	
	@FindBy(xpath="//div[contains(@class,'c-editTitleDialog')]")
	private TabDialog newTabDialog; 
	
	@FindBy(xpath="//div[contains(@class,'s-dashboard-edit-bar')]")
	private DashboardEditBar editDashboardBar;
	
	@FindBy(xpath="//div[contains(@class,'dashboardTitleEditBox')]/input")
	private WebElement newDashboardNameInput;
	
	private static final By BY_DASHBOARD_SELECTOR_TITLE = By.xpath("a/span");
	private static final By BY_EXPORTING_PANEL = By.xpath("//div[@class='box']//div[@class='rightContainer' and text()='Exporting…']");
	
	public DashboardTabs getTabs() {
		return tabs;
	}
	
	public DashboardEditBar getDashboardEditBar() {
		return editDashboardBar;
	}
	
	public String getDashboardName() {
		waitForElementVisible(dashboardSwitcherButton);
		String name = dashboardSwitcherButton.getText();
		if (getDashboardsCount() > 1) {
			return name.substring(0, name.length() - 1);
		} 
		return name;
	}
	
	public boolean selectDashboard(String dashboardName) {
		waitForElementVisible(dashboardSwitcherButton);
		dashboardSwitcherButton.click();
		if (dashboardSelectors != null && dashboardSelectors.size() > 0) {
			for (WebElement elem : dashboardSelectors) {
				if (elem.findElement(BY_DASHBOARD_SELECTOR_TITLE).getAttribute("title").equals(dashboardName)) {
					elem.findElement(BY_LINK).click();
					return true;
				}
			}
		}
		System.out.println("Dashboard not selected because it's not present!!!!");
		return false;
	}
	
	public boolean selectDashboard(int dashboardIndex) {
		waitForElementVisible(dashboardSwitcherButton);
		dashboardSwitcherButton.click();
		if (dashboardSelectors != null && dashboardSelectors.size() > 0) {
			for (WebElement elem : dashboardSelectors) {
				if (Integer.valueOf(elem.getAttribute("gdc:index")) == dashboardIndex) {
					elem.findElement(BY_LINK).click();
					waitForDashboardPageLoaded();
					return true;
				}
			}
		}
		System.out.println("Dashboard not selected because it's not present!!!!");
		return false;
	}
	
	public int getDashboardsCount() {
		if (dashboardSwitcherArrowMenu.isDisplayed()) {
			dashboardSwitcherArrowMenu.click();
			int dashboardsCount = dashboardSelectors.size();
			dashboardSwitcherArrowMenu.click();
			return dashboardsCount;
		} else if (dashboardSwitcherButton.isDisplayed()) {
			return 1;
		}
		return 0;
	}
	
	public void editDashboard() {
		waitForDashboardPageLoaded();
		waitForElementVisible(editExportEmbedButton);
		editExportEmbedButton.click();
		waitForElementVisible(editButton);
		editButton.click();
		waitForElementPresent(editDashboardBar.getRoot());
	}
	
	public String exportDashboardTab(int tabIndex) throws InterruptedException {
		tabs.openTab(tabIndex);
		waitForDashboardPageLoaded();
		String tabName = tabs.getTabLabel(0);
		waitForElementVisible(editExportEmbedButton);
		editExportEmbedButton.click();
		waitForElementVisible(exportPdfButton);
		Graphene.guardAjax(exportPdfButton).click();
		waitForElementVisible(BY_EXPORTING_PANEL);
		Thread.sleep(3000);
		waitForElementNotPresent(BY_EXPORTING_PANEL);
		Thread.sleep(3000);
		System.out.println("Dashboard " + tabName + " exported to PDF...");
		return tabName;
	}
	
	public void addNewTab(String tabName) {
		waitForElementVisible(addNewTabButton);
		addNewTabButton.click();
		waitForElementVisible(newTabDialog.getRoot());
		newTabDialog.createTab(tabName);
	}
	
	public void addNewDashboard(String dashbordName) throws InterruptedException {
		waitForElementVisible(editExportEmbedButton);
		editExportEmbedButton.click();
		waitForElementVisible(addDashboardButton);
		addDashboardButton.click();
		waitForElementVisible(newDashboardNameInput);
		newDashboardNameInput.clear();
		newDashboardNameInput.sendKeys(dashbordName);
		Thread.sleep(5000); // name is empty if the save button is used immediately
		editDashboardBar.saveDashboard();
	}
	
	public void deleteDashboard() throws InterruptedException {
		editDashboard();
		editDashboardBar.deleteDashboard();
	}
}
