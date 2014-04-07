package com.gooddata.qa.graphene.fragments.dashboards;

import java.util.List;

import com.gooddata.qa.graphene.fragments.reports.AbstractReport;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class DashboardContent extends AbstractFragment {
	@FindBy(css = ".c-projectdashboard-items .yui3-c-reportdashboardwidget")
	private List<AbstractReport> reports;
	
	public List<AbstractReport> getReports() {
		return reports;
	}
	
	public int getNumberOfReports() {
		return getReports().size();
	}
	
	public AbstractReport getReport(int reportIndex) {
		return getReports().get(reportIndex);
	}

}
