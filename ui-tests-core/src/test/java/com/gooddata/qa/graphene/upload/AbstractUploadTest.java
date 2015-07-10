package com.gooddata.qa.graphene.upload;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.HowItem;
import com.gooddata.qa.graphene.entity.ReportDefinition;
import com.gooddata.qa.graphene.enums.ReportTypes;
import com.gooddata.qa.graphene.fragments.reports.TableReport;
import com.gooddata.qa.graphene.fragments.upload.UploadColumns;
import com.gooddata.qa.utils.graphene.Screenshots;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.BeforeClass;

import java.util.*;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

public abstract class AbstractUploadTest extends AbstractProjectTest {

	protected String csvFilePath;

	protected Map<String, String[]> expectedDashboardsAndTabs;
	protected Map<String, String[]> emptyDashboardsAndTabs;

	protected static final By BY_INPUT = By.xpath("//input[contains(@class, 'has-error')]");
	protected static final By BY_BUBBLE = By
			.xpath("//div[contains(@class, 'bubble-negative') and contains(@class, 'isActive')]");
	protected static final By BY_BUBBLE_CONTENT = By.xpath("//div[@class='content']");
    protected static final By BY_UPLOAD_DASHBOARD = By.xpath("//iframe[contains(@src,'Auto-Tab')]");

	@FindBy(xpath = "//div[@id='gridContainerTab']")
	protected TableReport report;

	@FindBy(css = "button.s-btn-load")
	protected WebElement loadButton;

	@FindBy(className = "s-uploadIndex-error")
	protected WebElement errorMessageElement;

	private static final By BY_EMPTY_DATASET = By
			.xpath("//div[@id='dataPage-empty-dataSets']");

	@FindBy
	protected WebElement uploadFile;

	private static final String EMPTY_DASHBOARD = "Empty dashboard";
	private static final String DEFAULT_DASHBOARD = "Default dashboard";

	@BeforeClass
	public void initProperties() {
		csvFilePath = testParams.loadProperty("csvFilePath") + testParams.getFolderSeparator();
		projectTitle = "SimpleProject-test-upload";

		expectedDashboardsAndTabs = new HashMap<String, String[]>();
		expectedDashboardsAndTabs.put(DEFAULT_DASHBOARD, new String[] { "Your Sample Reports" });
		emptyDashboardsAndTabs = new HashMap<String, String[]>();
		emptyDashboardsAndTabs.put(EMPTY_DASHBOARD, new String[] { "First Tab" });
	}

	protected void prepareReport(String reportName, ReportTypes reportType,
			List<String> what, List<String> how) throws InterruptedException {
		initReportsPage();
        reportsPage.startCreateReport();
		waitForAnalysisPageLoaded(browser);
		waitForElementVisible(reportPage.getRoot());
		assertNotNull(reportPage, "Report page not initialized!");
        ReportDefinition reportDefinition = new ReportDefinition().withName(reportName)
                                                                  .withType(reportType);
        for (String attributeName : how) {
            reportDefinition.withHows(new HowItem(attributeName));
        }

        if (what != null) {
            for (String metric : what) {
                reportDefinition.withWhats(metric);
            }
        }
        
        reportPage.createReport(reportDefinition);
	}

	protected void deleteDataset(String datasetName)
			throws InterruptedException {
	    initManagePage();
        datasetsTable.selectObject(datasetName);
        datasetDetailPage.deleteDataset();
	}

	protected void deleteDashboard() throws InterruptedException {
        dashboardsPage.deleteDashboard();
		waitForDashboardPageLoaded(browser);
        verifyProjectDashboardsAndTabs(true, emptyDashboardsAndTabs, false);
	}

	protected void checkAttributeName(String attributeName)
			throws InterruptedException {
		initAttributePage();
		System.out.println("Check attribute name is displayed well.");
		assertTrue(attributesTable.selectObject(attributeName));
	}

	protected void selectFileToUpload(String fileName)
			throws InterruptedException {
	    initEmptyDashboardsPage();
		initUploadPage();
        upload.uploadFile(csvFilePath + fileName + ".csv");
	}
	
	protected void checkErrorColumn(UploadColumns uploadColumns, int columnIndex, boolean hasBubble, String bubbleMessage){
		assertTrue(uploadColumns.getColumns().get(columnIndex).findElement(BY_INPUT)
				.isDisplayed());
		System.out.print("Border of field name turn to red.");
		if (hasBubble)
			{
			assertEquals(uploadColumns.getColumns().get(columnIndex).findElement(BY_BUBBLE)
					.findElement(BY_BUBBLE_CONTENT).getText(), bubbleMessage);
			System.out.println("System shows error message: " + bubbleMessage);
			};
	}
	
	protected void uploadInvalidCSVFile(String fileName, String errorTitle, String errorMessage, String errorSupport) throws InterruptedException{
	    initEmptyDashboardsPage();
		initUploadPage();
		String filePath = csvFilePath + fileName + ".csv";
		System.out.println("Going to upload file: " + filePath);
		waitForElementPresent(uploadFile).sendKeys(filePath);
		if (!waitForElementVisible(errorMessageElement).isDisplayed()){
			Thread.sleep(30000);
		}
		Screenshots.takeScreenshot(browser, "check-incorrect-csv-file-upload",
				this.getClass());
		if (errorTitle != null){
			assertEquals(upload.getErrorTitle(errorMessageElement).getText(), errorTitle);
		}
		if (errorMessage != null){
			assertEquals(upload.getErrorMessage(errorMessageElement).getText(), errorMessage);
		}
		if(errorSupport != null){
			assertEquals(upload.getErrorSupport(errorMessageElement).getText(), errorSupport);
		}
	}

	protected void assertMetricValuesInReport(List<Integer> metricIndexes,
			List<Float> metricValues, List<Double> expectedMetricValues) {
		int index = 0;
		for (int metricIndex : metricIndexes) {
			assertEquals(metricValues.get(metricIndex).doubleValue(),
					expectedMetricValues.get(index));
			index++;
		}
	}

	protected void assertEmptyMetricInReport(List<Integer> metricIndexes,
			List<Float> metricValues) {
		for (int metricIndex : metricIndexes) {
			assertEquals(metricValues.get(metricIndex).doubleValue(), 
					0.0);
		}
	}

	protected void assertAttributeElementsInReport(
			List<Integer> attributeIndexes, List<String> attributeElements,
			List<String> expectedAttribueElements) {
		int index = 0;
		for (int attributeIndex : attributeIndexes) {
			assertEquals(attributeElements.get(attributeIndex).toString(),
					expectedAttribueElements.get(index).toString());
			index++;
		}
	}

	public void uploadFileAndClean(String fileName) throws InterruptedException {
		try {
            uploadCSV(csvFilePath + fileName + ".csv", null, "simple-upload-"
							+ fileName);
            verifyProjectDashboardsAndTabs(true, expectedDashboardsAndTabs, false);
            addEmptyDashboard();
		} finally {
	        waitForDashboardPageLoaded(browser);
			deleteDashboard();
			dashboardsPage.selectDashboard(EMPTY_DASHBOARD);
			dashboardsPage.deleteDashboard();
			deleteDataset(fileName);
			waitForElementVisible(BY_EMPTY_DATASET, browser);
		}
	}

	protected void cleanDashboardAndDatasets(List<String> datasets)
			throws InterruptedException {
		initDashboardsPage();
		deleteDashboard();
		dashboardsPage.selectDashboard(EMPTY_DASHBOARD);
		dashboardsPage.deleteDashboard();
		for (String dataset : datasets) {
			deleteDataset(dataset);
		}
		assertTrue(dataPage.getRoot().findElement(BY_EMPTY_DATASET).isDisplayed());
	}

	protected void uploadDifferentDateFormat(String uploadFileName)
			throws InterruptedException {
		selectFileToUpload(uploadFileName);
		Screenshots.takeScreenshot(browser, "different-date-format-csv-upload-"
				+ uploadFileName, this.getClass());
		UploadColumns uploadColumns = upload.getUploadColumns();
		assertEquals(uploadColumns.getNumberOfColumns(), 9);
		List<Integer> columnIndexes = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8);
		List<String> guessedDataTypes = Arrays.asList("TEXT", "TEXT", "TEXT",
				"TEXT", "TEXT", "TEXT", "TEXT", "DATE", "NUMBER");
		List<String> expectedColumnNames = Arrays.asList("Lastname",
				"Firstname", "Education", "Position", "Store", "State",
				"County", "Paydate", "Amount");
		uploadColumns.assertColumnsType(columnIndexes, guessedDataTypes);
		uploadColumns.assertColumnsName(columnIndexes,
				expectedColumnNames);
        upload.confirmloadCsv();
		waitForElementVisible(By.xpath("//iframe[contains(@src,'Auto-Tab')]"), browser);
        verifyProjectDashboardsAndTabs(true, expectedDashboardsAndTabs, true);
		Screenshots.takeScreenshot(browser, uploadFileName + "-dashboard",
				this.getClass());
		addEmptyDashboard();

		// Check Date in report
		List<String> what = new ArrayList<String>();
		what.add("Sum of Amount");
		List<String> how = new ArrayList<String>();
		how.add("Month/Year (" + uploadFileName + "_paydate)");
		prepareReport("Report with " + uploadFileName, ReportTypes.TABLE, what,
				how);
		List<String> attributeElements = report.getAttributeElements();
		Screenshots.takeScreenshot(browser, "report-with-" + uploadFileName,
				this.getClass());
		System.out.println("Check the date format in report!");
		List<Integer> attributeIndex = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8,
				9, 10, 11, 12);
		List<String> expectedAttributeElements = Arrays.asList("Jan 2006",
				"Feb 2006", "Mar 2006", "Apr 2006", "May 2006", "Jun 2006",
				"Jul 2006", "Aug 2006", "Sep 2006", "Oct 2006", "Nov 2006",
				"Dec 2006", "Jan 2007");
		assertAttributeElementsInReport(attributeIndex, attributeElements,
				expectedAttributeElements);
		System.out.println("Date format with " + uploadFileName
				+ " is displayed well in report!");
	}

	protected void addEmptyDashboard() throws InterruptedException {
	    initDashboardsPage();
        dashboardsPage.addNewDashboard(EMPTY_DASHBOARD);
        dashboardsPage.selectDashboard(DEFAULT_DASHBOARD);
	}
}
