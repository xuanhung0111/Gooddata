package com.gooddata.qa.graphene.fragments.reports;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

import com.gooddata.qa.graphene.enums.ExportFormat;
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
	
	@FindBy(xpath="//button[contains(@class, 'exportButton')]")
	private WebElement exportButton;
	
	@FindBy(xpath="//div[contains(@class, 'yui3-m-export')]//a[contains(@class, 'pdf')]")
	private WebElement exportToPDF;
	
	@FindBy(xpath="//div[contains(@class, 'yui3-m-export')]//a[contains(@class, 'png')]")
	private WebElement exportToPNG;
	
	@FindBy(xpath="//div[contains(@class, 'yui3-m-export')]//a[contains(@class, 'xls')]")
	private WebElement exportToXLS;
	
	@FindBy(xpath="//div[contains(@class, 'yui3-m-export')]//a[contains(@class, 'csv')]")
	private WebElement exportToCSV;
	
	private static final By BY_EXPORTING_STATUS = By.xpath("//span[@class='exportProgress']/span[text()='Exporting...']");
	
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
	
	public String getReportName() {
		return reportName.getAttribute("title");
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
		waitForAnalysisPageLoaded();
		waitForElementVisible(createReportButton);
		Thread.sleep(2000);
		createReportButton.click();
		waitForElementVisible(confirmDialogCreateButton);
		confirmDialogCreateButton.click();
		waitForElementNotVisible(confirmDialogCreateButton);
		Assert.assertEquals(createReportButton.getText(), "Saved", "Report wasn't saved");
	}
	
	public String exportReport(ExportFormat format) throws InterruptedException {
		String reportName = getReportName();
		waitForElementVisible(exportButton);
		exportButton.click();
		WebElement currentExportLink = null;
		switch (format) {
		case PDF:
			currentExportLink = exportToPDF;
			break;
		case IMAGE_PNG:
			currentExportLink = exportToPNG;
			break;
		case EXCEL_XLS:
			currentExportLink = exportToXLS;
			break;
		case CSV:
			currentExportLink = exportToCSV;
			break;
		}
		waitForElementVisible(currentExportLink);
		currentExportLink.click();
		Thread.sleep(5000);
		// waitForElementVisible(BY_EXPORTING_STATUS); //this waiting is causing some unexpected issues in tests when the export (xls/csv) is too fast
		waitForElementVisible(exportButton);
		Thread.sleep(3000);
		System.out.println("Report " + reportName + " exported to " + format.getName());
		return reportName;
	}
	
}
