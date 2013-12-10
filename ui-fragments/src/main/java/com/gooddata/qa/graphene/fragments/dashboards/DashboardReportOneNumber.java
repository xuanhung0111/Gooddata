package com.gooddata.qa.graphene.fragments.dashboards;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class DashboardReportOneNumber extends DashboardReport {
	
	@FindBy(className = "number")
	private WebElement number;
	
	@FindBy(className = "description")
	private WebElement description;
	
	public String getValue() {
		return number.getText();
	}
	
	public String getDescription() {
		return description.getText();
	}
}
