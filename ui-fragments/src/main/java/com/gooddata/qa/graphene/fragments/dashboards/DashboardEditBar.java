package com.gooddata.qa.graphene.fragments.dashboards;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

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
	
	@FindBy(xpath="//div[contains(@class,'s-report-picker')]//input")
	private WebElement reportSearchInput;
	
	private static final String reportToAddLocator = "//div[contains(@class,'s-report-picker')]//div[contains(@class,'s-enabled')]/span[@title='${reportName}']";
	private static final String reportOnDashboardLocator = "//div[@id='p-projectDashboardPage']//div[contains(@class,'yui3-c-reportdashboardwidget')]//a[@title='${reportName}']";
	
	public void addReportToDashboard(String reportName) {
		waitForElementVisible(reportMenuButton);
		reportMenuButton.click();
		waitForElementVisible(reportSearchInput);
		reportSearchInput.clear();
		reportSearchInput.sendKeys(reportName);
		By reportToAdd = By.xpath(reportToAddLocator.replace("${reportName}", reportName));
		waitForElementVisible(reportToAdd);
		browser.findElement(reportToAdd).click();
		waitForDashboardPageLoaded();
		By reportOnDashboard = By.xpath(reportOnDashboardLocator.replace("${reportName}", reportName));
		waitForElementVisible(reportOnDashboard);
	}
	
	public void addWidgetToDashboard() {
		//TODO
	}
	
	public void saveDashboard() {
		waitForElementVisible(saveButton);
		Graphene.guardAjax(saveButton).click();
	}
	
	public void deleteDashboard() throws InterruptedException {
		waitForElementVisible(actionsMenu);
		actionsMenu.click();
		waitForElementVisible(deleteButton);
		deleteButton.click();
		Thread.sleep(3000);
		waitForElementVisible(deleteDashboardDialogButton);
		deleteDashboardDialogButton.click();
		waitForElementNotPresent(this.getRoot());
	}

}
