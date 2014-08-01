package com.gooddata.qa.graphene.fragments.disc;

import java.io.File;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.DISCProcessTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

public class ProjectDetailPage extends AbstractFragment {

	private static final By BY_PROCESS_TITLE = By.cssSelector(".ait-process-title");
	private final static By BY_PROCESS_DELETE_BUTTON = By.cssSelector(".ait-process-delete-btn");
	private final static By BY_PROCESS_DOWNLOAD_BUTTON = By
			.cssSelector(".ait-process-download-btn");
	private final static By BY_PROCESS_REDEPLOY_BUTTON = By
			.cssSelector(".ait-process-redeploy-btn");
	private final static By BY_CREATE_NEW_SCHEDULE_LINK = By.cssSelector(".action-important-link");
	private final static By BY_PROCESS_SCHEDULE_TAB = By.cssSelector(".ait-process-schedules-btn");
	private final static By BY_PROCESS_EXECUTABLE_TAB = By
			.cssSelector(".ait-process-executables-btn");
	private final static By BY_PROCESS_METADATA_TAB = By.cssSelector(".ait-process-metadata-btn");
	private final static By BY_BROKEN_SCHEDULE_MESSAGE = By
			.cssSelector(".broken-schedules-section .message");

	private static final String DELETE_PROCESS_DIALOG_MESSAGE = "Are you sure you want to delete process %s?";
	private static final String DELETE_PROCESS_DIALOG_TITLE = "Delete process %s";
	private final static String BROKEN_SCHEDULE_SECTION_MESSAGE = "The schedules cannot be executed. "
			+ "Its process has been re-deployed with modified graphs or a different folder structure.";
	private static final String EXECUTABLE_NO_SCHEDULES = "No schedules";
	private static final String EXECUTABLE_SCHEDULE_NUMBER = "Scheduled %d time%s";

	@FindBy(css = ".ait-project-title")
	protected WebElement displayedProjectTitle;

	@FindBy(xpath = "//a/span[text()='Go to Dashboards']")
	protected WebElement goToDashboardsLink;

	@FindBy(css = ".ait-project-empty-state .title")
	protected WebElement projectEmptyStateTitle;

	@FindBy(css = ".ait-project-empty-state .message")
	protected WebElement projectEmptyStateMessage;

	@FindBy(css = ".ait-project-deploy-process-btn")
	protected WebElement deployProcessButton;

	@FindBy(css = ".ait-process-list-item.active .ait-process-metadata-value")
	protected WebElement processMetadataValue;

	@FindBy(css = ".ait-process-list-item")
	protected List<WebElement> processes;

	@FindBy(css = ".ait-process-list-item.active")
	protected WebElement activeProcess;

	@FindBy(css = ".ait-process-executable-list")
	protected ExecutablesTable executablesTable;

	@FindBy(css = ".error_dialog .dialog-body")
	protected WebElement deployErrorDialog;

	@FindBy(css = ".ait-project-new-schedule-btn")
	protected WebElement newScheduleButton;

	@FindBy(css = ".schedule-title-cell")
	private WebElement scheduleTitle;

	@FindBy(css = ".schedule-cron-cell")
	private WebElement scheduleCron;

	@FindBy(css = ".ait-process-schedule-list")
	protected List<SchedulesTable> schedulesTablesList;

	@FindBy(css = ".ait-process-delete-fragment")
	protected WebElement processDeleteDialog;

	@FindBy(css = ".dialog-title")
	protected WebElement processDeleteDialogTitle;

	@FindBy(css = ".dialog-body")
	protected WebElement processDeleteDialogMessage;

	@FindBy(css = ".ait-process-delete-confirm-btn")
	protected WebElement processDeleteConfirmButton;

	@FindBy(css = ".ait-process-delete-cancel-btn")
	protected WebElement processDeleteCancelButton;

	public String getDisplayedProjectTitle() {
		return waitForElementVisible(displayedProjectTitle).getText();
	}

	public void goToDashboards() {
		waitForElementVisible(goToDashboardsLink).click();
	}

	public String getProjectEmptyStateTitle() {
		return waitForElementVisible(projectEmptyStateTitle).getText();
	}

	public String getProjectEmptyStateMessage() {
		return waitForElementVisible(projectEmptyStateMessage).getText();
	}

	public WebElement getDeployErrorDialog() {
		return deployErrorDialog;
	}

	public void clickOnDeployProcessButton() {
		waitForElementPresent(deployProcessButton).click();
	}

	public void clickOnNewScheduleButton() {
		waitForElementVisible(newScheduleButton).click();
	}

	public int getNumberOfProcesses() {
		if (processes == null) {
			throw new NullPointerException();
		}
		return processes.size();
	}

	public WebElement getProcess(int processIndex) {
		if (processIndex < 0 || processIndex > getNumberOfProcesses()) {
			throw new IndexOutOfBoundsException();
		}
		return processes.get(processIndex);
	}

	public WebElement getActiveProcess() {
		return waitForElementVisible(activeProcess);
	}

	public WebElement getScheduleTab(int processIndex) {
		return waitForElementVisible(processes.get(processIndex).findElement(
				BY_PROCESS_SCHEDULE_TAB));
	}

	public WebElement getExecutableTab(int processIndex) {
		return waitForElementVisible(processes.get(processIndex).findElement(
				BY_PROCESS_EXECUTABLE_TAB));
	}

	public WebElement getMetadataTab(int processIndex) {
		return waitForElementVisible(processes.get(processIndex).findElement(
				BY_PROCESS_METADATA_TAB));
	}

	protected void assertExecutablesList(DISCProcessTypes processType, List<String> executables) {
		executablesTable.assertExecutablesList(processType, executables);
	}

	public boolean assertProcessInList(String processName, DISCProcessTypes processType,
			List<String> executables) {
		for (int i = 0; i < this.getNumberOfProcesses(); i++) {
			if (getProcess(i).findElement(BY_PROCESS_TITLE).getText().equals(processName)) {
				getScheduleTab(i).click();
				assertEquals(getScheduleTab(i).getText(), "0 schedules");
				getExecutableTab(i).click();
				String executableTitle = processType.getProcessTypeExecutable();
				if (executables.size() > 1)
					executableTitle = processType.getProcessTypeExecutable() + "s";
				assertEquals(getExecutableTab(i).getText(),
						String.format("%d %s total", executables.size(), executableTitle));
				waitForElementVisible(executablesTable.getRoot());
				assertExecutablesList(processType, executables);
				return true;
			}
		}
		return false;
	}

	public WebElement getElementFromSpecificProcess(String processName, By elementLocator) {
		for (int i = 0; i < this.getNumberOfProcesses(); i++) {
			waitForElementVisible(getProcess(i));
			if (getProcess(i).findElement(BY_PROCESS_TITLE).getText().equals(processName)) {
				return getProcess(i).findElement(elementLocator);
			}
		}
		return null;
	}

	public void clickOnDownloadProcessButton(String processName) {
		waitForElementVisible(
				getElementFromSpecificProcess(processName, BY_PROCESS_DOWNLOAD_BUTTON)).click();
	}

	public void clickOnDeleteProcessButton(String processName) {
		waitForElementVisible(getElementFromSpecificProcess(processName, BY_PROCESS_DELETE_BUTTON))
				.click();
		waitForElementVisible(processDeleteDialog);
	}

	public WebElement getNewScheduleLinkInSchedulesList(String processName) {
		return getElementFromSpecificProcess(processName, BY_CREATE_NEW_SCHEDULE_LINK);
	}

	public WebElement getRedeployButton(String processName) {
		return getElementFromSpecificProcess(processName, BY_PROCESS_REDEPLOY_BUTTON);
	}

	public WebElement getExecutableTabByProcessName(String processName) {
		return getElementFromSpecificProcess(processName, BY_PROCESS_EXECUTABLE_TAB);
	}

	public WebElement getScheduleTabByProcessName(String processName) {
		return getElementFromSpecificProcess(processName, BY_PROCESS_SCHEDULE_TAB);
	}

	public WebElement getExecutableScheduleLink(String executableName) {
		return executablesTable.getExecutableScheduleLink(executableName);
	}

	public WebElement checkEmptySchedulesList(String processName) {
		return getElementFromSpecificProcess(processName, BY_CREATE_NEW_SCHEDULE_LINK);
	}

	public void checkBrokenScheduleSection(String processName) {
		System.out.println("Broken schedule message in project detail page: "
				+ getElementFromSpecificProcess(processName, BY_BROKEN_SCHEDULE_MESSAGE).getText());
		assertEquals(BROKEN_SCHEDULE_SECTION_MESSAGE,
				getElementFromSpecificProcess(processName, BY_BROKEN_SCHEDULE_MESSAGE).getText());
	}

	public String getProcessID(String processName) {
		waitForElementVisible(getElementFromSpecificProcess(processName, BY_PROCESS_METADATA_TAB))
				.click();
		return waitForElementVisible(processMetadataValue).getText();
	}

	public String getProcessTitle(WebElement process) {
		return process.findElement(BY_PROCESS_TITLE).getText();
	}

	public void checkDownloadProcess(String processName, String downloadFolder, String projectID,
			long minimumDownloadedFileSize) throws InterruptedException {
		String processID = getProcessID(processName);
		clickOnDownloadProcessButton(processName);
		File zipDownload = new File(downloadFolder + projectID + "_" + processID
				+ "-decrypted.zip");
		for (int i = 0; zipDownload.length() < minimumDownloadedFileSize && i < 10; i++) {
			Thread.sleep(1000);
		}
		System.out.println("Download file size: " + zipDownload.length());
		System.out.println("Download file path: " + zipDownload.getPath());
		System.out.println("Download file name: " + zipDownload.getName());
		assertTrue(zipDownload.length() > minimumDownloadedFileSize, "Process \"" + processName
				+ "\" is downloaded sucessfully!");
		zipDownload.delete();
	}

	public void checkSortedProcesses() {
		for (int i = 0; i < processes.size(); i++) {
			System.out
					.println("Title of Process[" + i + "] : " + getProcessTitle(processes.get(i)));
			if (i > 0) {
				assertTrue(getProcessTitle(processes.get(i)).compareTo(
						getProcessTitle(processes.get(i - 1))) >= 0);
			}
		}
	}

	public void checkFocusedProcess(String processName) {
		waitForElementVisible(activeProcess);
		assertEquals(processName, getProcessTitle(activeProcess));
	}

	public void deleteProcess(String processName) throws InterruptedException {
		int processNumberBeforeDelete = processes.size();
		System.out.println("Process to delete: " + processName);
		clickOnDeleteProcessButton(processName);
		waitForElementVisible(processDeleteConfirmButton).click();
		waitForElementNotPresent(processDeleteDialog);
		if (processNumberBeforeDelete == processes.size())
			Thread.sleep(3000);
		if (processNumberBeforeDelete > processes.size())
			System.out.println("Process " + processName + " has been deleted!");
		waitForElementVisible(getRoot());
	}

	public void checkDeleteProcessDialog(String processName) {
		String deleteProcessTitle = String.format(DELETE_PROCESS_DIALOG_TITLE, processName);
		String deleteProcessMessage = String.format(DELETE_PROCESS_DIALOG_MESSAGE, processName);
		clickOnDeleteProcessButton(processName);
		waitForElementVisible(processDeleteDialogTitle);
		assertEquals(deleteProcessTitle, processDeleteDialogTitle.getText());
		waitForElementVisible(processDeleteDialogMessage);
		assertEquals(deleteProcessMessage, processDeleteDialogMessage.getText());
	}

	public void checkCancelDeleteProcess(String processName) {
		clickOnDeleteProcessButton(processName);
		waitForElementVisible(processDeleteCancelButton).click();
		waitForElementNotPresent(processDeleteDialog);
		assertTrue(assertIsExistingProcess(processName));
	}

	public void deleteAllProcesses() throws InterruptedException {
		for (int i = processes.size() - 1; i >= 0; i--) {
			deleteProcess(getProcessTitle(processes.get(i)));
		}
	}

	public boolean assertIsExistingProcess(String processName) {
		waitForElementVisible(getRoot());
		for (int i = 0; i < processes.size(); i++) {
			if (getProcessTitle(processes.get(i)).equals(processName))
				return true;
		}
		return false;
	}

	public void checkExecutableScheduleNumber(String processName, String executableName,
			int scheduleNumber) throws InterruptedException {
		String executableScheduleNumber = String.format(EXECUTABLE_SCHEDULE_NUMBER, scheduleNumber,
				(scheduleNumber > 1 ? "s" : ""));
		getExecutableTabByProcessName(processName).click();
		waitForElementVisible(executablesTable.getRoot());
		if (scheduleNumber > 0) {
			assertEquals(executableScheduleNumber,
					executablesTable.getExecutableScheduleNumber(executableName));
		} else
			assertEquals(EXECUTABLE_NO_SCHEDULES,
					executablesTable.getExecutableScheduleNumber(executableName));
	}
}
