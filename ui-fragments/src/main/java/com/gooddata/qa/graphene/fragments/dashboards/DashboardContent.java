package com.gooddata.qa.graphene.fragments.dashboards;

import java.util.List;

import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class DashboardContent extends AbstractFragment {
	@FindBy(css = ".c-projectdashboard-items .yui3-c-reportdashboardwidget")
	private List<DashboardReport> reports;
	
	public List<DashboardReport> getReports() {
		return reports;
	}
	
	public int getNumberOfReports() {
		return getReports().size();
	}
	
	public DashboardReport getReport(int reportIndex) {
		return getReports().get(reportIndex);
	}

}
