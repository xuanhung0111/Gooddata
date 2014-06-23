package com.gooddata.qa.graphene.fragments.disc;

import java.util.List;

import junit.framework.Assert;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.DISCProcessTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class ProjectDetailPage extends AbstractFragment {

	private final static String XPATH_PROCESS_TAB_BUTTON = "//div[@class='project-detail-content']//div[${processIndex}]//div[contains(@class, 'tab-buttons')]/a[${index}]";
	private final static String XPATH_PROCESS_REDEPLOY_BUTTON = "//div[@class='ait-project-process-list']/div[${processIndex}]//a[text()='Re-deploy']";
	private final static String XPATH_CREATE_NEW_SCHEDULE_LINK = "//div[@class='ait-project-process-list']/div[${processIndex}]//div[@class='empty-state-small']/span/a[text()='Create new schedule']";
	private final static String XPATH_EMPTY_SCHEDULES_LIST = "//div[@class='ait-project-process-list']/div[${processIndex}]//div[@class='empty-state-small']/span/a[text()='Create new schedule']";
	private final static String XPATH_BROKEN_SCHEDULE_MESSAGE = "//div[@class='ait-project-process-list']/div[${processIndex}]//div[contains(@class, 'broken-schedules-section')]/p";

	private final static By BY_PROCESS_NAME = By.cssSelector(".secondary-title");
	private final static String BROKEN_SCHEDULE_SECTION_MESSAGE = "The schedules cannot be executed. "
			+ "Its process has been re-deployed with modified graphs or a different folder structure.";

	@FindBy(xpath = "//div[@class='project-header']")
	protected WebElement projectDetailHeader;

	@FindBy(css = ".page-header .s-btn-deploy_process")
	protected WebElement deployProcessButton;

	@FindBy(css = ".process-detail")
	protected List<WebElement> processes;

	@FindBy(css = ".ait-process-executable-list")
	protected ExecutablesTable executablesTable;

	@FindBy(css = ".error_dialog .dialog-body")
	protected WebElement deployErrorDialog;

	@FindBy(xpath = "//div[@class='project-detail-content']/div/div[${processIndex}]//a[text()='Re-deploy']")
	protected WebElement redeployButton;

	@FindBy(xpath = "//div[@class='row page-header']/div[2]/a[text()=' New schedule']")
	protected WebElement newScheduleButton;

	@FindBy(css = ".schedule-title-cell")
	private WebElement scheduleTitle;

	@FindBy(css = ".schedule-cron-cell")
	private WebElement scheduleCron;

	@FindBy(css = ".ait-process-schedule-list")
	protected List<SchedulesTable> schedulesTablesList;

	@FindBy(css = ".active .broken-schedules-section .message")
	protected WebElement brokenSchedulesMessage;

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

	public WebElement getScheduleTab(int processIndex) {
		return waitForElementVisible(
				By.xpath(XPATH_PROCESS_TAB_BUTTON.replace("${processIndex}",
						String.valueOf(processIndex)).replace("${index}", "1")), browser);
	}

	public WebElement getExecutableTab(int processIndex) {
		return waitForElementVisible(
				By.xpath(XPATH_PROCESS_TAB_BUTTON.replace("${processIndex}",
						String.valueOf(processIndex)).replace("${index}", "2")), browser);
	}

	protected void assertExecutablesList(DISCProcessTypes processType, List<String> executables) {
		executablesTable.assertExecutablesList(processType, executables);
	}

	public boolean assertProcessInList(String processName, DISCProcessTypes processType,
			List<String> executables) {
		for (int i = 0; i < this.getNumberOfProcesses(); i++) {
			if (getProcess(i).findElement(BY_PROCESS_NAME).getText().equals(processName)) {
				getScheduleTab(i + 1).click();
				Assert.assertEquals(getScheduleTab(i + 1).getText(), "0 schedules");
				getExecutableTab(i + 1).click();
				String executableTitle = processType.getProcessTypeExecutable();
				if (executables.size() > 1)
					executableTitle = processType.getProcessTypeExecutable() + "s";
				Assert.assertEquals(getExecutableTab(i + 1).getText(),
						String.format("%d %s total", executables.size(), executableTitle));
				waitForElementVisible(executablesTable.getRoot());
				assertExecutablesList(processType, executables);
				return true;
			}
		}
		return false;
	}

	public WebElement getElementFromSpecificProcess(String processName, String elementXpath) {
		for (int i = 0; i < this.getNumberOfProcesses(); i++) {
			waitForElementVisible(getProcess(i));
			if (getProcess(i).findElement(BY_PROCESS_NAME).getText().equals(processName)) {
				return getProcess(i).findElement(
						By.xpath(elementXpath.replace("${processIndex}", String.valueOf(i + 1))));
			}
		}
		return null;
	}

	public WebElement getNewScheduleLinkInSchedulesList(String processName) {
		return getElementFromSpecificProcess(processName, XPATH_CREATE_NEW_SCHEDULE_LINK);
	}

	public WebElement getRedeployButton(String processName) {
		return getElementFromSpecificProcess(processName, XPATH_PROCESS_REDEPLOY_BUTTON);
	}

	public WebElement getExecutableTabByProcessName(String processName) {
		return getElementFromSpecificProcess(processName,
				XPATH_PROCESS_TAB_BUTTON.replace("${index}", "2"));
	}

	public WebElement getScheduleTabByProcessName(String processName) {
		return getElementFromSpecificProcess(processName,
				XPATH_PROCESS_TAB_BUTTON.replace("${index}", "1"));
	}

	public WebElement getExecutableScheduleLink(String executableName) {
		return executablesTable.getExecutableScheduleLink(executableName);
	}

	public WebElement checkEmptySchedulesList(String processName) {
		return getElementFromSpecificProcess(processName, XPATH_EMPTY_SCHEDULES_LIST);
	}

	public void checkBrokenScheduleSection(String processName) {
		System.out.println("Broken schedule message in project detail page: "
				+ getElementFromSpecificProcess(processName, XPATH_BROKEN_SCHEDULE_MESSAGE)
						.getText());
		Assert.assertEquals(BROKEN_SCHEDULE_SECTION_MESSAGE,
				getElementFromSpecificProcess(processName, XPATH_BROKEN_SCHEDULE_MESSAGE).getText());
	}
}
