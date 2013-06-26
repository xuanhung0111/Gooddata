package com.gooddata.qa.graphene.fragments.dashboards;

import org.jboss.arquillian.graphene.enricher.findby.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardTabs;

public class DashboardsPage extends AbstractFragment {
	
	@FindBy(xpath="//div[@id='abovePage']/div[contains(@class,'yui3-dashboardtabs-content')]/div[contains(@class,'c-collectionWidget')]/div")
	private DashboardTabs tabs;
	
	public DashboardTabs getTabs() {
		return tabs;
	}
}
