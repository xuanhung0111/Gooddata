package com.gooddata.qa.graphene.fragments.reports;

import java.util.List;

import org.jboss.arquillian.graphene.enricher.findby.FindBy;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

import com.gooddata.qa.graphene.enums.ReportTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class ReportPage extends AbstractFragment {

	@FindBy(id="analysisReportTitle")
	private WebElement reportName;
	
	@FindBy(xpath="//input[@class='ipeEditor']")
	private WebElement reportNameInput;
	
	@FindBy(xpath="//div[@class='c-ipeEditorControls']/button")
	private WebElement reportNameSaveButton;
	
	@FindBy(xpath="//div[@id='reportSaveButtonContainer']/button")
	private WebElement createReportButton;
	
	@FindBy(xpath="//div[contains(@class, 's-saveReportDialog')]//div[@class='bd_controls']//button[text()='Create']")
	private WebElement confirmDialogCreateButton;
	
	@FindBy(id="reportVisualizer")
	private ReportVisualizer visualiser;
	
	public ReportVisualizer getVisualiser() {
		return visualiser;
	}
	
	public void setReportName(String reportName) {
		waitForElementVisible(this.reportName);
		this.reportName.click();
		waitForElementVisible(reportNameInput);
		reportNameInput.clear();
		reportNameInput.sendKeys(reportName);
		waitForElementVisible(reportNameSaveButton);
		reportNameSaveButton.click();
		waitForElementNotVisible(reportNameInput);
		Assert.assertEquals(this.reportName.getText(), reportName, "Report name wasn't updated");
	}
	
	public void createReport(String reportName, ReportTypes reportType, List<String> what, List<String> how) throws InterruptedException {
		setReportName(reportName);
		// select what - metrics
		visualiser.selectWhatArea(what);
		
		// select how - attributes
		visualiser.selectHowArea(how);
		
		visualiser.finishReportChanges();
		
		//visualiser.selectFilterArea();
		//TODO
		
		visualiser.selectReportVisualisation(reportType);
		Thread.sleep(5000);
		waitForElementVisible(createReportButton);
		createReportButton.click();
		waitForElementVisible(confirmDialogCreateButton);
		confirmDialogCreateButton.click();
		waitForElementNotVisible(confirmDialogCreateButton);
		Assert.assertEquals(createReportButton.getText(), "Saved", "Report wasn't saved");
	}
	
}
