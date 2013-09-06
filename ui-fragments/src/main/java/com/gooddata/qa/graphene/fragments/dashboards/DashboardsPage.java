package com.gooddata.qa.graphene.fragments.dashboards;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardTabs;

public class DashboardsPage extends AbstractFragment {
	
	@FindBy(xpath="//div[@id='abovePage']/div[contains(@class,'yui3-dashboardtabs-content')]/div[contains(@class,'c-collectionWidget')]/div")
	private DashboardTabs tabs;
	
	@FindBy(xpath="//button[@title='Edit, Embed or Export']")
	private WebElement editExportEmbedButton;
	
	@FindBy(xpath="//span[@title='Export to PDF']")
	private WebElement exportPdfButton;
	
	public DashboardTabs getTabs() {
		return tabs;
	}
	
	private static final By BY_EXPORTING_PANEL = By.xpath("//div[@class='box']//div[@class='rightContainer' and text()='Exporting…']");
	
			
	public String exportDashboard(int dashboardIndex, long dashboardExportTimeoutMillis) throws InterruptedException {
		tabs.openTab(dashboardIndex);
		waitForDashboardPageLoaded();
		String tabName = tabs.getTabLabel(0);
		waitForElementVisible(editExportEmbedButton);
		editExportEmbedButton.click();
		waitForElementVisible(exportPdfButton);
		exportPdfButton.click();
		waitForElementVisible(BY_EXPORTING_PANEL);
		Thread.sleep(dashboardExportTimeoutMillis);
		waitForElementNotPresent(BY_EXPORTING_PANEL);
		System.out.println("Dashboard " + tabName + " exported to PDF...");
		return tabName;
	}
}
