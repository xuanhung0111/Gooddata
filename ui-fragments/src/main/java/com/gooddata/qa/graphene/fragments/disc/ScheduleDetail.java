package com.gooddata.qa.graphene.fragments.disc;

import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.enums.DISCProcessTypes;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

public class ScheduleDetail extends ScheduleForm {

	private static final String DEFAULT_RETRY_DELAY_VALUE = "30";
	private static final String RESCHEDULE_FORM_MESSAGE = "Restart every minutes until success (or 30th consecutive failure)";
	private static final String FAILED_SCHEDULE_FOR_5TH_TIME_MESSAGE = "This schedule has failed for the %dth time. We highly recommend disable this schedule until the issue is addressed. If you want to disable the schedule, click here or read troubleshooting article for more information.";
	private static final String AUTO_DISABLED_SCHEDULE_MESSAGE = "This schedule has been automatically disabled following its %dth consecutive failure. If you addressed the issue, you can enable it.";
	private static final String AUTO_DISABLED_SCHEDULE_MORE_INFO = "For more information read Automatic Disabling of Failed Schedules article at our support portal.";
	private static final String BROKEN_SCHEDULE_MESSAGE = "The graph %s doesn't exist because it has been changed (renamed or deleted). "
			+ "It isn't possible to execute this schedule because there is no graph to execute.";

	private static final By BY_EXECUTION_STATUS = By.cssSelector(".execution-status");
	private static final By BY_EXECUTION_DESCRIPTION = By
			.cssSelector(".ait-execution-history-item-description");
	private static final By BY_EXECUTION_LOG = By.cssSelector(".ait-execution-history-item-log");
	private static final By BY_EXECUTION_RUNTIME = By.cssSelector(".execution-runtime");
	private static final By BY_EXECUTION_DATE = By.cssSelector(".execution-date");
	private static final By BY_EXECUTION_TIMES = By.cssSelector(".execution-times");
	private static final By BY_OK_STATUS_ICON = By.cssSelector(".status-icon-ok");
	private static final By BY_ERROR_STATUS_ICON = By.cssSelector(".status-icon-error");
	private static final By BY_MANUAL_ICON = By.cssSelector(".icon-manual");
	private static final By BY_CONFIRM_STOP_EXECUTION = By.cssSelector(".button-negative");
	private static final By BY_RUN_STOP_BUTTON = By
			.xpath("//div[@class='large-12 columns ait-schedule-executable-section']/div[@class='l-next']/button[1]");

	@FindBy(css = ".ait-schedule-close-btn .icon-delete")
	protected WebElement closeButton;

	@FindBy(css = ".ait-execution-history-item")
	protected List<WebElement> scheduleExecutionItem;

	@FindBy(css = ".ait-schedule-reschedule-add-btn")
	protected WebElement addRetryDelay;

	@FindBy(css = ".reschedule-form")
	protected WebElement rescheduleForm;

	@FindBy(css = ".ait-schedule-reschedule-value input")
	protected WebElement retryDelayInput;

	@FindBy(css = ".ait-schedule-reschedule-edit-buttons .button-positive")
	protected WebElement saveRetryDelayButton;

	@FindBy(css = ".ait-schedule-reschedule-edit-buttons .button-secondary")
	protected WebElement cancelAddRetryDelayButton;

	@FindBy(css = ".ait-schedule-reschedule-value .bubble-overlay")
	protected WebElement errorRetryDelayBubble;

	@FindBy(css = ".ait-schedule-run-btn")
	protected WebElement manualRunButton;

	@FindBy(css = ".dialog-main.ait-schedule-run")
	protected WebElement manualRunDialog;

	@FindBy(css = ".ait-schedule-run-confirm-btn")
	protected WebElement confirmRunButton;

	@FindBy(css = ".ait-schedule-stop-btn")
	protected WebElement manualStopButton;

	@FindBy(css = ".overlay .dialog-main")
	protected WebElement manualStopDialog;

	@FindBy(css = ".ait-schedule-cron-section .disable-button button")
	protected WebElement disableScheduleButton;

	@FindBy(css = ".schedule-title .status-icon-disabled")
	protected WebElement disabledScheduleIcon;

	@FindBy(css = ".ait-schedule-enable-btn")
	protected WebElement enableScheduleButton;

	@FindBy(css = ".ait-schedule-executable-edit-buttons .button-positive")
	protected WebElement saveChangedExecutable;

	@FindBy(css = ".ait-schedule-executable-edit-buttons .button-secondary")
	protected WebElement cancelChangedExecutable;

	@FindBy(css = ".ait-schedule-delete-btn")
	protected WebElement deleteScheduleButton;

	@FindBy(css = ".dialog-main.ait-schedule-delete-fragment")
	protected WebElement deleteScheduleDialog;

	@FindBy(css = ".ait-schedule-delete-confirm-btn")
	protected WebElement confirmDeleteScheduleButton;

	@FindBy(css = ".ait-schedule-delete-cancel-btn")
	protected WebElement cancelDeleteScheduleButton;

	@FindBy(css = ".ait-schedule-cron-edit-buttons .button-positive")
	protected WebElement saveChangedCronTimeButton;

	@FindBy(css = ".ait-schedule-cron-edit-buttons .button-secondary")
	protected WebElement cancelChangedCronTimeButton;

	@FindBy(css = ".parameters-save-buttons .button-positive")
	protected WebElement saveChangedParameterButton;

	@FindBy(css = ".parameters-save-buttons .button-secondary")
	protected WebElement cancelChangedParameterButton;

	@FindBy(css = ".broken-schedule-info")
	protected WebElement brokenScheduleMessage;

	@FindBy(css = ".broken-schedule-info .schedule-title-select")
	protected WebElement brokenScheduleExecutable;

	@FindBy(css = ".broken-schedule-title-save .button-positive")
	protected WebElement brokenScheduleSaveChangeButton;

	@FindBy(css = ".info-section")
	protected WebElement failedScheduleInfoSection;

	@FindBy(css = ".ait-schedule-disabled .message p:nth-child(1)")
	protected WebElement autoDisableScheduleMessage;

	@FindBy(css = ".ait-schedule-disabled .message p:nth-child(2)")
	protected WebElement autoDisableScheduleMoreInfo;

	@FindBy(css = ".ait-schedule-executable-select-btn")
	protected WebElement selectExecutable;

	public void clickOnCloseScheduleButton() {
		waitForElementVisible(closeButton).click();
	}

	public WebElement getSaveChangedCronTimeButton() {
		return saveChangedCronTimeButton;
	}

	public void waitForAutoRunSchedule(int waitingTimeInMinutes) throws InterruptedException {
		int executionNumber = scheduleExecutionItem.size();
		Calendar startWaitingTime = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("mm");
		int startWaitingMinute = Integer.valueOf(sdf.format(startWaitingTime.getTime()));
		for (int i = 0; i < waitingTimeInMinutes + 3; i++) {
			System.out.println("Number of executions: " + scheduleExecutionItem.size());
			if (scheduleExecutionItem.size() == executionNumber) {
				System.out.println("Waiting for auto execution...");
				Thread.sleep(60000);
			} else {
				if (scheduleExecutionItem.get(0).findElement(BY_EXECUTION_DESCRIPTION).getText()
						.equals("SCHEDULED")) {
					System.out.println("Schedule is in SCHEDULED state...");
					Thread.sleep(60000);
				} else {
					if (scheduleExecutionItem.get(0).findElement(BY_EXECUTION_DESCRIPTION)
							.getText().equals("RUNNING"))
						System.out.println("Schedule is in RUNNING state...");
					Calendar startExecutionTime = Calendar.getInstance();
					int startExecutionMinute = Integer.valueOf(sdf.format(startExecutionTime
							.getTime()));
					startExecutionMinute = startExecutionMinute > startWaitingMinute ? startExecutionMinute
							: startExecutionMinute + 60;
					int delayTime = startExecutionMinute - startWaitingMinute
							- waitingTimeInMinutes;
					System.out.println("Delay time: " + delayTime);
					if (delayTime >= 0)
						System.out.println("Start time in minute: " + startExecutionMinute);
					else {
						System.out.println("Schedule execution started too early, started at: "
								+ startExecutionMinute);
					}
					break;
				}
			}
		}
	}

	public void assertLastExecutionDetails(boolean isSuccessful, boolean isManualRun,
			boolean isStopped, String executablePath, DISCProcessTypes processType,
			int waitingTimeInMinutes) throws InterruptedException {
		for (int i = 0; i < waitingTimeInMinutes * 6; i++) {
			if (getRoot().findElement(BY_RUN_STOP_BUTTON).getText().equals("Stop")) {
				System.out.println("Waiting for running schedule...");
				Thread.sleep(10000);
			} else if (getRoot().findElement(BY_RUN_STOP_BUTTON).getText().equals("Run"))
				break;
		}
		waitForElementVisible(scheduleExecutionItem.get(0));
		assertTrue(scheduleExecutionItem.get(0).isDisplayed());
		if (isSuccessful) {
			assertTrue(scheduleExecutionItem.get(0).findElement(BY_EXECUTION_STATUS)
					.findElement(BY_OK_STATUS_ICON).isDisplayed());
			assertEquals(scheduleExecutionItem.get(0).findElement(BY_EXECUTION_DESCRIPTION)
					.getText(), "OK");
		} else {
			assertTrue(scheduleExecutionItem.get(0).findElement(BY_EXECUTION_STATUS)
					.findElement(BY_ERROR_STATUS_ICON).isDisplayed());
			System.out.println("Execution description: "
					+ scheduleExecutionItem.get(0).findElement(BY_EXECUTION_DESCRIPTION).getText());
			if (isStopped) {
				assertTrue(scheduleExecutionItem.get(0).findElement(BY_EXECUTION_DESCRIPTION)
						.getText().equals("MANUALLY STOPPED"));
			} else {
				String errorMessage = String.format("%s=%s error:",
						processType.getProcessTypeExecutable(), executablePath).toLowerCase();
				assertTrue(scheduleExecutionItem.get(0).findElement(BY_EXECUTION_DESCRIPTION)
						.getText().toLowerCase().contains(errorMessage));
			}
		}
		if (isManualRun)
			assertTrue(scheduleExecutionItem.get(0).findElement(BY_MANUAL_ICON).isDisplayed());
		assertTrue(scheduleExecutionItem.get(0).findElement(BY_EXECUTION_LOG).isDisplayed());
		assertTrue(scheduleExecutionItem.get(0).findElement(BY_EXECUTION_LOG).isEnabled());
		assertFalse(scheduleExecutionItem.get(0).findElement(BY_EXECUTION_RUNTIME).getText()
				.isEmpty());
		System.out.println("Execution Runtime: "
				+ scheduleExecutionItem.get(0).findElement(BY_EXECUTION_RUNTIME).getText());
		assertFalse(scheduleExecutionItem.get(0).findElement(BY_EXECUTION_DATE).getText().isEmpty());
		System.out.println("Execution Date: "
				+ scheduleExecutionItem.get(0).findElement(BY_EXECUTION_DATE).getText());
		assertFalse(scheduleExecutionItem.get(0).findElement(BY_EXECUTION_TIMES).getText()
				.isEmpty());
		System.out.println("Execution Times: "
				+ scheduleExecutionItem.get(0).findElement(BY_EXECUTION_TIMES).getText());
	}

	public void addRetryDelay(String retryDelay, boolean isSaved, boolean isValidDelayValue)
			throws InterruptedException {
		waitForElementVisible(selectExecutable);
		Select select = new Select(selectExecutable);
		if (select.getFirstSelectedOption().equals(select.getOptions().get(0)))
			Thread.sleep(2000);
		waitForElementVisible(addRetryDelay).click();
		waitForElementVisible(rescheduleForm);
		System.out.println("Reschedule form info: " + rescheduleForm.getText());
		assertEquals(RESCHEDULE_FORM_MESSAGE, rescheduleForm.getText());
		waitForElementVisible(retryDelayInput);
		if (retryDelayInput.getAttribute("value").toString().isEmpty())
			Thread.sleep(5000);
		assertEquals(DEFAULT_RETRY_DELAY_VALUE, retryDelayInput.getAttribute("value"));
		waitForElementVisible(retryDelayInput).clear();
		if (!retryDelayInput.getText().isEmpty())
			Thread.sleep(1000);
		retryDelayInput.sendKeys(String.valueOf(retryDelay));
		if (isSaved) {
			waitForElementVisible(saveRetryDelayButton).click();
			if (isValidDelayValue) {
				waitForElementNotPresent(saveRetryDelayButton);
				assertEquals(String.valueOf(retryDelay), retryDelayInput.getAttribute("value"));
			} else {
				assertTrue(retryDelayInput.getAttribute("class").contains("has-error"));
				System.out.println("error retry delay: " + errorRetryDelayBubble.getText());
			}
		} else {
			waitForElementVisible(cancelAddRetryDelayButton).click();
			waitForElementNotPresent(retryDelayInput);
			waitForElementVisible(addRetryDelay);
		}
	}

	public void manualRun() throws InterruptedException {
		waitForElementVisible(manualRunButton).click();
		waitForElementVisible(manualRunDialog);
		waitForElementVisible(confirmRunButton).click();
		waitForElementVisible(manualStopButton);
		System.out.println("Schedule is executed manually!");
	}

	public void manualStop() throws InterruptedException {
		if (scheduleExecutionItem.isEmpty())
			Thread.sleep(60000);
		waitForElementVisible(manualStopButton).click();
		waitForElementVisible(manualStopDialog);
		waitForElementVisible(manualStopDialog.findElement(BY_CONFIRM_STOP_EXECUTION)).click();
		waitForElementVisible(manualRunButton);
		System.out.println("Schedule is stopped manually!");
	}

	public void disableSchedule() {
		waitForElementVisible(disableScheduleButton).click();
		waitForElementVisible(enableScheduleButton);
		waitForElementVisible(disabledScheduleIcon);
		System.out.println("Schedule is disabled!");
	}

	public void enableSchedule() {
		waitForElementVisible(enableScheduleButton).click();
		waitForElementVisible(disableScheduleButton);
		System.out.println("Schedule is enabled!");
	}

	public boolean isDisabledSchedule(int waitingTimeInMinutes, int executionNumberBeforeDisable)
			throws InterruptedException {
		Calendar startWaitingTime = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("mm");
		int startWaitingMinute = Integer.valueOf(sdf.format(startWaitingTime.getTime()));
		int waitingTimeFromNow = waitingTimeInMinutes - startWaitingMinute % waitingTimeInMinutes;
		waitForAutoRunSchedule(waitingTimeFromNow);
		if (scheduleExecutionItem.size() > executionNumberBeforeDisable)
			return false;
		System.out.println("Schedule is disabled successfully!");
		return true;
	}

	public void changeExecutable(String newExecutable, boolean isSaved) throws InterruptedException {
		waitForElementVisible(selectExecutable);
		Select select = new Select(selectExecutable);
		if (select.getFirstSelectedOption().equals(select.getOptions().get(0)))
			Thread.sleep(2000);
		select.selectByVisibleText(newExecutable);
		if (isSaved) {
			waitForElementVisible(saveChangedExecutable).click();
			waitForElementNotPresent(saveChangedExecutable);
		} else {
			waitForElementVisible(cancelChangedExecutable).click();
		}
		clickOnCloseScheduleButton();
	}

	public void deleteSchedule(boolean isConfirmed) {
		waitForElementVisible(getRoot());
		waitForElementVisible(deleteScheduleButton).click();
		waitForElementVisible(deleteScheduleDialog);
		if (isConfirmed)
			waitForElementVisible(confirmDeleteScheduleButton).click();
		else
			waitForElementVisible(cancelDeleteScheduleButton).click();
		waitForElementNotPresent(deleteScheduleDialog);
	}

	public void changeCronTime(Pair<String, List<String>> newCronTime, boolean isSaved)
			throws InterruptedException {
		waitForElementVisible(getRoot());
		selectCron(newCronTime);
		if (isSaved)
			waitForElementVisible(saveChangedCronTimeButton).click();
		else
			waitForElementVisible(cancelChangedCronTimeButton).click();
		clickOnCloseScheduleButton();
	}

	public void editScheduleParameters(Map<String, List<String>> changedParameters,
			boolean newParameters, boolean isSaved) {
		int index = 0;
		if (!newParameters) {
			for (Entry<String, List<String>> changedParameter : changedParameters.entrySet()) {
				if (changedParameter.getValue() == null) {
					parameters.get(index).findElement(BY_PARAMETER_REMOVE_ACTION).click();
					waitForElementVisible(saveChangedParameterButton);
				} else {
					WebElement parameterName = parameters.get(index).findElement(BY_PARAMETER_NAME);
					WebElement parameterValue = parameters.get(index).findElement(
							BY_PARAMETER_VALUE);
					parameterName.clear();
					parameterName.sendKeys(changedParameter.getKey());
					parameterValue.clear();
					parameterValue.sendKeys(changedParameter.getValue().get(1));
					index++;
				}
			}
		} else {
			addParameters(changedParameters);
		}
		if (isSaved)
			waitForElementVisible(saveChangedParameterButton).click();
		else
			waitForElementVisible(cancelChangedParameterButton).click();
		clickOnCloseScheduleButton();
	}

	public void assertScheduleParameters(Map<String, List<String>> expectedParameters)
			throws InterruptedException {
		waitForElementVisible(getRoot());
		int i = 0;
		if (expectedParameters != null) {
			Thread.sleep(1000);
			for (Entry<String, List<String>> parameter : expectedParameters.entrySet()) {
				assertEquals(parameter.getKey(), parameters.get(i).findElement(BY_PARAMETER_NAME)
						.getAttribute("value"));
				if (parameter.getValue().get(0).equals("secure")) {
					assertEquals("password", parameters.get(i).findElement(BY_PARAMETER_VALUE)
							.getAttribute("type"));
					assertEquals(
							"Secure parameter value",
							parameters.get(i).findElement(BY_PARAMETER_VALUE)
									.getAttribute("placeholder"));
				} else {
					assertEquals(parameter.getValue().get(1),
							parameters.get(i).findElement(BY_PARAMETER_VALUE).getAttribute("value"));
				}
				i++;
			}
		}
	}

	public void checkBrokenSchedule(String oldExecutable, String newExecutable)
			throws InterruptedException {
		waitForElementVisible(getRoot());
		waitForElementVisible(brokenScheduleMessage);
		System.out.println("Check broken schedule detail page...");
		assertEquals(String.format(BROKEN_SCHEDULE_MESSAGE, oldExecutable),
				brokenScheduleMessage.getText());
		Select select = new Select(brokenScheduleExecutable);
		select.selectByVisibleText(newExecutable);
		waitForElementVisible(brokenScheduleSaveChangeButton).click();
		waitForElementNotPresent(brokenScheduleSaveChangeButton);
		clickOnCloseScheduleButton();
	}

	public void repeatManualRun(int executionTimes, String executablePath,
			DISCProcessTypes processType) throws InterruptedException {
		waitForElementVisible(manualRunButton);
		for (int i = 0; i < executionTimes; i++) {
			manualRun();
			assertLastExecutionDetails(false, true, false, executablePath, processType, 5);
		}
	}

	public void checkRepeatedFailureSchedule(String executablePath, DISCProcessTypes processType)
			throws InterruptedException {
		waitForElementVisible(cronPicker);
		repeatManualRun(5, executablePath, processType);
		System.out.println("Schedule failed for the 5th time...");
		waitForElementVisible(failedScheduleInfoSection);
		assertEquals(
				String.format(FAILED_SCHEDULE_FOR_5TH_TIME_MESSAGE, scheduleExecutionItem.size()),
				failedScheduleInfoSection.getText());
		repeatManualRun(25, executablePath, processType);
		System.out.println("Schedule failed for the 30th time...");
		waitForElementVisible(autoDisableScheduleMessage);
		assertEquals(String.format(AUTO_DISABLED_SCHEDULE_MESSAGE, scheduleExecutionItem.size()),
				autoDisableScheduleMessage.getText());
		assertEquals(AUTO_DISABLED_SCHEDULE_MORE_INFO, autoDisableScheduleMoreInfo.getText());
	}

	public boolean isExecutionInRunningState() throws InterruptedException {
		int executionNumber = scheduleExecutionItem.size();
		for (int i = 0; executionNumber == scheduleExecutionItem.size() && i < 10; i++)
			Thread.sleep(3000);
		for (int i = 0; i < 20; i++) {
			if (scheduleExecutionItem.get(0).findElement(BY_EXECUTION_DESCRIPTION).getText()
					.equals("RUNNING")) {
				return true;
			} else
				Thread.sleep(3000);
		}
		return false;
	}

	public String getLastExecutionDate() {
		return scheduleExecutionItem.get(0).findElement(BY_EXECUTION_DATE).getText();
	}

	public String getLastExecutionTime() {
		return scheduleExecutionItem.get(0).findElement(BY_EXECUTION_TIMES).getText();
	}
}
