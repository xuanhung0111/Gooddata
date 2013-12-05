package com.gooddata.qa.graphene.fragments.dashboards;

import org.openqa.selenium.By;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class DashboardTab extends AbstractFragment {
	
	private static final By BY_TAB_LABEL_SPAN = By.xpath("div/span[@class='label']");
	
	public String getLabel() {
		return getRoot().findElement(BY_TAB_LABEL_SPAN).getText();
	}
	
	/**
	 * Method to verify that tab is selected
	 * 
	 * @return true if tab is selected
	 */
	public boolean isSelected() {
		return getRoot().getAttribute("class").contains("yui3-dashboardtab-selected");
	}
	
	public void open() {
		getRoot().click();
	}
	
}
