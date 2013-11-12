package com.gooddata.qa.graphene.fragments.dashboards;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.WidgetTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class DashboardEditBar extends AbstractFragment {
	
	@FindBy(css=".s-btn-save")
	private WebElement saveButton;
	
	@FindBy(css=".s-btn-cancel")
	private WebElement cancelButton;
	
	@FindBy(css=".s-btn-actions")
	private WebElement actionsMenu;
	
	@FindBy(css=".s-delete")
	private WebElement deleteButton;
	
	@FindBy(xpath="//div[contains(@class,'c-confirmDeleteDialog')]//button[contains(@class,'s-btn-delete')]")
	private WebElement deleteDashboardDialogButton;
	
	@FindBy(css=".s-btn-report")
	private WebElement reportMenuButton;
	
	@FindBy(css=".s-btn-widget")
	private WebElement widgetMenuButton;
	
	@FindBy(xpath="//div[contains(@class,'s-report-picker')]//input")
	private WebElement reportSearchInput;
	
	@FindBy(xpath="//div[contains(@class, 'yui3-c-dashboardwidgetconfigpanel')]")
	private WebElement widgetConfigPanel;
	
	@FindBy(xpath="//div[contains(@class, 'yui3-c-dashboardwidgetconfigpanel')]//div[contains(@class,'configPanel-views')]//button")
	private WebElement widgetConfigMetricButton;
	
	@FindBy(xpath="//div[contains(@class, 'yui3-c-dashboardwidgetconfigpanel')]//button[contains(@class,'s-btn-apply')]")
	private WebElement widgetConfigApplyButton;
	
	private static final String reportToAddLocator = "//div[contains(@class,'s-report-picker')]//div[contains(@class,'yui3-c-label-content')]/span[@title='${reportName}']";
	private static final String reportOnDashboardLocator = "//div[@id='p-projectDashboardPage']//div[contains(@class,'yui3-c-reportdashboardwidget')]//a[@title='${reportName}']";
	
	private static final String widgetLocator = "//div[contains(@class,'yui3-c-adddashboardwidgetpickerpanel')]//div[contains(@class,'add-dashboard-item')]/div[contains(text(), '${widgetLabel}')]/../button";
	private static final String widgetMetricLocator = "//div[contains(@class,'yui3-widget-stacked shelterPlugin-plugged')]//div[contains(@class,'yui3-c-picker-content')]//div[contains(@class,'yui3-c-simplecolumn')]//div[contains(@class,'c-label') and contains(@class,'s-enabled')]/span[text()='${metricLabel}']";
	
	
	
	public void addReportToDashboard(String reportName) {
		waitForElementVisible(reportMenuButton).click();
		waitForElementVisible(reportSearchInput).clear();
		reportSearchInput.sendKeys(reportName);
		By reportToAdd = By.xpath(reportToAddLocator.replace("${reportName}", reportName));
		waitForElementVisible(reportToAdd).click();
		waitForDashboardPageLoaded();
		By reportOnDashboard = By.xpath(reportOnDashboardLocator.replace("${reportName}", reportName));
		waitForElementVisible(reportOnDashboard);
	}
	
	public void addWidgetToDashboard(WidgetTypes widgetype, String metricLabel) throws InterruptedException {
		waitForElementVisible(widgetMenuButton).click();
		By widgetType = By.xpath(widgetLocator.replace("${widgetLabel}", widgetype.getLabel()));
		waitForElementVisible(widgetType).click();
		// TODO fragments for widget config panel + metric selection can be used - but better IDs and UI organization is required
		waitForElementVisible(widgetConfigPanel);
		waitForElementVisible(widgetConfigMetricButton).click();
		By metricInWidget = By.xpath(widgetMetricLocator.replace("${metricLabel}", metricLabel));
		waitForElementVisible(metricInWidget).click();
		Thread.sleep(3000);
		waitForElementVisible(widgetConfigApplyButton).click();
		waitForElementNotVisible(widgetConfigPanel);
	}
	
	public void saveDashboard() {
		waitForElementVisible(saveButton);
		Graphene.guardAjax(saveButton).click();
	}
	
	public void deleteDashboard() throws InterruptedException {
		waitForElementVisible(actionsMenu).click();
		waitForElementVisible(deleteButton).click();
		Thread.sleep(3000);
		waitForElementVisible(deleteDashboardDialogButton).click();
		waitForElementNotPresent(this.getRoot());
	}

}
