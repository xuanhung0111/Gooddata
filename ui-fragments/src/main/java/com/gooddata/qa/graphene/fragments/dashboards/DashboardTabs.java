package com.gooddata.qa.graphene.fragments.dashboards;

import org.jboss.arquillian.graphene.enricher.findby.FindBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

import java.util.ArrayList;
import java.util.List;

public class DashboardTabs extends AbstractFragment {
	
	private static final By BY_TAB_LABEL_SPAN = By.xpath("div/span[@class='label']");

	@FindBy(className="yui3-dashboardtab")
	private List<WebElement> tabs;
	
	/**
	 * Method to get number of dashboard tabs for selected project
	 * 
	 * @return number of dashboard tabs for selected project
	 */
	public int getNumberOfTabs() {
		return tabs.size();
	}

	/**
	 * Method for switching tab by index, no HTTP/XHR requests is expected on this click
	 * 
	 * @param i - tab index
	 */
	public void openTab(int i) {
		getTabWebElement(i).click();
	}
	
	/**
	 * Method to verify that tab with given index is selected
	 * 
	 * @param i - tab index
	 * @return true is tab with given index is selected
	 */
	public boolean isTabSelected(int i) {
		return getTabWebElement(i).getAttribute("class").contains("yui3-dashboardtab-selected");
	}
	
	/**
	 * Method to get index of selected tab
	 * 
	 * @return index of selected tab
	 */
	public int getSelectedTabIndex() {
		for (int i = 0; i < tabs.size(); i++) {
			if (isTabSelected(i)) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Method to get label of tab with given index
	 * 
	 * @param i - tab index
	 * @return label of tab with given index 
	 */
	public String getTabLabel(int i) {
		WebElement elem = getTabWebElement(i).findElement(BY_TAB_LABEL_SPAN);
		return elem.getText();
	}
	
	/**
	 * Method to get all dashboard tab labels of selected project
	 * 
	 * @return List<String> with all tab names
	 */
	public List<String> getAllTabNames() {
		List<String> tabNames = new ArrayList<String>();
		for (int i = 0; i < tabs.size(); i++) {
			tabNames.add(getTabLabel(i));
		}
		return tabNames;
	}
	
	private WebElement getTabWebElement(int i) {
		return tabs.get(i);
	}
}
