package com.gooddata.qa.graphene.fragments.disc;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;

import static org.junit.Assert.*;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.DISCProcessTypes;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class ScheduleDetail extends ScheduleForm {

	private static final String DEFAULT_RETRY_DELAY_VALUE = "30";
	private static final String RESCHEDULE_FORM_MESSAGE = "Restart every minutes until success (or 30th consecutive failure)";
	private static final String FAILED_SCHEDULE_FOR_5TH_TIME_MESSAGE = "This schedule has failed for the %dth time. We highly recommend disable this schedule until the issue is addressed. If you want to disable the schedule, click here or read troubleshooting article for more information.";
	private static final String AUTO_DISABLED_SCHEDULE_MESSAGE = "This schedule has been automatically disabled following its %dth consecutive failure. If you addressed the issue, you can enable it.";
	private static final String AUTO_DISABLED_SCHEDULE_MORE_INFO = "For more information read Automatic Disabling of Failed Schedules article at our support portal.";
	private static final String BROKEN_SCHEDULE_MESSAGE = "The graph %s doesn't exist because it has been changed (renamed or deleted). "
			+ "It isn't possible to execute this schedule because there is no graph to execute.";

	private static final String XPATH_EXECUTABLE_SELECTION = "//select[contains(@class, 'ait-schedule-executable-select-btn')]/option[text()='${option}']";
	private static final String XPATH_EXECUTION_ITEM = "//div[@class='ember-view execution-history-list']/div[${index}]";

	private static final By BY_EXECUTION_STATUS = By.xpath("//div[@class='execution-status']");
	private static final By BY_EXECUTION_DESCRIPTION = By
			.xpath("//div[contains(@class, 'execution-history-item-description')]");
	private static final By BY_EXECUTION_LOG = By
			.xpath("//div[@class='list-item-cell execution-log']");
	private static final By BY_EXECUTION_RUNTIME = By
			.xpath("//div[@class='list-item-cell execution-runtime']");
	private static final By BY_EXECUTION_DATE = By
			.xpath("//div[@class='list-item-cell execution-date']");
	private static final By BY_EXECUTION_TIMES = By
			.xpath("//div[@class='list-item-cell execution-times']");
	private static final By BY_OK_STATUS_ICON = By.cssSelector(".status-icon-ok");
	private static final By BY_ERROR_STATUS_ICON = By.cssSelector(".status-icon-error");
	private static final By BY_MANUAL_ICON = By.cssSelector(".icon-manual");
	private static final By BY_CONFIRM_STOP_EXECUTION = By.cssSelector(".button-negative");
	private static final By BY_RUN_STOP_BUTTON = By
			.xpath("//div[@class='large-12 columns ait-schedule-executable-section']/div[@class='l-next']/button[1]");

	@FindBy(xpath = "//a[contains(@class, 'close-button')]")
	protected WebElement closeButton;

	@FindBy(css = ".ait-execution-history-item")
	protected List<WebElement> scheduleExecutionItem;

	@FindBy(css = ".ait-schedule-reschedule-add-btn")
	protected WebElement addRetryDelay;

	@FindBy(css = ".reschedule-form")
	protected WebElement rescheduleForm;

	@FindBy(xpath = "//div[contains(@class, 'ait-schedule-reschedule-value')]//input")
	protected WebElement retryDelayInput;

	@FindBy(css = ".ait-schedule-reschedule-edit-buttons .ember-view .button-positive")
	protected WebElement saveRetryDelayButton;

	@FindBy(css = ".ait-schedule-reschedule-edit-buttons .button-secondary")
	protected WebElement cancelAddRetryDelayButton;

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

	@FindBy(css = ".ait-schedule-executable-edit-buttons .l-inline .button-positive")
	protected WebElement saveChangedExecutable;

	@FindBy(css = ".ait-schedule-delete-btn")
	protected WebElement deleteScheduleButton;

	@FindBy(css = ".dialog-main.ait-schedule-delete-fragment")
	protected WebElement deleteScheduleDialog;

	@FindBy(css = ".ait-schedule-delete-confirm-btn")
	protected WebElement confirmDeleteScheduleButton;

	@FindBy(css = ".ait-schedule-cron-edit-buttons .button-positive")
	protected WebElement saveChangedCronTimeButton;

	@FindBy(xpath = "//div[contains(@class, 'parameters-save-buttons')]/div/button[text()='Save']")
	protected WebElement saveChangedParameterButton;

	@FindBy(css = "p.broken-schedule-info")
	protected WebElement brokenScheduleMessage;

	@FindBy(css = ".broken-schedule-title-save .button-positive")
	protected WebElement brokenScheduleSaveChangeButton;

	@FindBy(css = ".info-section")
	protected WebElement failedScheduleInfoSection;

	@FindBy(css = ".ait-schedule-disabled .message p:nth-child(1)")
	protected WebElement autoDisableScheduleMessage;

	@FindBy(css = ".ait-schedule-disabled .message p:nth-child(2)")
	protected WebElement autoDisableScheduleMoreInfo;

	public void clickOnCloseScheduleButton() {
		waitForElementVisible(closeButton).click();
	}

	public WebElement getSaveChangedCronTimeButton() {
		return saveChangedCronTimeButton;
	}

	public void waitForAutoRunSchedule(int waitingTimeInMinutes) throws InterruptedException {
		int executionNumber = scheduleExecutionItem.size();
		for (int i = 0; i < waitingTimeInMinutes + 3; i++) {
			System.out.println("Number of executions: " + scheduleExecutionItem.size());
			if (scheduleExecutionItem.size() == executionNumber) {
				System.out.println("Waiting for auto execution...");
				Thread.sleep(60000);
			} else {
				if (scheduleExecutionItem.get(0).findElement(BY_EXECUTION_DESCRIPTION).getText()
						.equals("SCHEDULED")) {
					System.out.println("Schedule is in SCHEDULED state...");
					Thread.sleep(1000);
				} else if (scheduleExecutionItem.get(0).findElement(BY_EXECUTION_DESCRIPTION)
						.getText().equals("RUNNING")) {
					System.out.println("Schedule is in RUNNING state...");
					Calendar startTime = Calendar.getInstance();
					SimpleDateFormat sdf = new SimpleDateFormat("mm");
					int startMinute = Integer.valueOf(sdf.format(startTime.getTime()));
					int delayTime = startMinute % waitingTimeInMinutes;
					System.out.println("Delay time: " + delayTime);
					if (delayTime >= 0 && delayTime < 4)
						System.out.println("Start time in minute: " + startMinute);
					else {
						System.out.println("Schedule execution is too early, started at: "
								+ startMinute);
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
		waitForElementVisible(getRoot().findElement(
				By.xpath(XPATH_EXECUTION_ITEM.replace("${index}", "1"))));
		assertTrue(getRoot().findElement(By.xpath(XPATH_EXECUTION_ITEM.replace("${index}", "1")))
				.isDisplayed());
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

	public void addRetryDelay(int retryDelay) throws InterruptedException {
		if (cronExpression.isDisplayed() && cronExpression.getAttribute("value").isEmpty())
			Thread.sleep(2000);
		waitForElementVisible(addRetryDelay).click();
		waitForElementVisible(rescheduleForm);
		System.out.println("reschedule form info: " + rescheduleForm.getText());
		assertEquals(RESCHEDULE_FORM_MESSAGE, rescheduleForm.getText());
		waitForElementVisible(retryDelayInput);
		if (retryDelayInput.getAttribute("value").isEmpty())
			Thread.sleep(2000);
		assertEquals(DEFAULT_RETRY_DELAY_VALUE, retryDelayInput.getAttribute("value").toString());
		waitForElementVisible(cancelAddRetryDelayButton).click();
		waitForElementVisible(addRetryDelay).click();
		waitForElementVisible(retryDelayInput).clear();
		if (!retryDelayInput.getText().isEmpty())
			Thread.sleep(1000);
		retryDelayInput.sendKeys(String.valueOf(retryDelay));
		waitForElementVisible(saveRetryDelayButton).click();
		assertEquals(String.valueOf(retryDelay), retryDelayInput.getAttribute("value"));
	}

	public void manualRun() throws InterruptedException {
		waitForElementVisible(manualRunButton).click();
		waitForElementVisible(manualRunDialog);
		waitForElementVisible(confirmRunButton).click();
		waitForElementVisible(manualStopButton);
	}

	public void manualStop() throws InterruptedException {
		if (scheduleExecutionItem.isEmpty())
			Thread.sleep(2000);
		waitForElementVisible(manualStopButton).click();
		waitForElementVisible(manualStopDialog);
		waitForElementVisible(manualStopDialog.findElement(BY_CONFIRM_STOP_EXECUTION)).click();
		waitForElementVisible(manualRunButton);
	}

	public void disableSchedule() {
		waitForElementVisible(disableScheduleButton).click();
		waitForElementVisible(enableScheduleButton);
	}

	public void enableSchedule() {
		waitForElementVisible(enableScheduleButton).click();
		waitForElementVisible(disableScheduleButton);
	}

	public boolean assertDisableSchedule(int waitingTimeInMinutes) throws InterruptedException {
		assertTrue(enableScheduleButton.isDisplayed());
		assertTrue(disabledScheduleIcon.isDisplayed());
		for (int i = 0; i < waitingTimeInMinutes + 3; i++) {
			if (getRoot().findElement(BY_RUN_STOP_BUTTON).getText().equals("Run")) {
				System.out.println("Checking disable schedule...");
				Thread.sleep(60000);
			} else
				return false;
		}
		return true;
	}

	public void changeExecutable(String newExecutable) {
		getRoot().findElement(
				By.xpath(ScheduleDetail.XPATH_EXECUTABLE_SELECTION.replace("${option}",
						newExecutable))).click();
		waitForElementVisible(saveChangedExecutable).click();
		waitForElementNotPresent(saveChangedExecutable);
		clickOnCloseScheduleButton();
	}

	public void deleteSchedule() {
		waitForElementVisible(getRoot());
		waitForElementVisible(deleteScheduleButton).click();
		waitForElementVisible(deleteScheduleDialog);
		waitForElementVisible(confirmDeleteScheduleButton).click();
		waitForElementNotPresent(deleteScheduleDialog);
	}

	public void changeCronTime(Pair<String, List<String>> newCronTime) throws InterruptedException {
		waitForElementVisible(getRoot());
		selectCron(newCronTime);
		waitForElementVisible(saveChangedCronTimeButton).click();
		clickOnCloseScheduleButton();
	}

	public void editScheduleParameters(Map<String, List<String>> changedParameters,
			boolean newParameters) {
		int index = 1;
		if (!newParameters) {
			for (Entry<String, List<String>> changedParameter : changedParameters.entrySet()) {
				By byParameterName = By.xpath(XPATH_PARAMETER_NAME.replace("${index}",
						String.valueOf(index)));
				By byParameterValue = By.xpath(XPATH_PARAMETER_VALUE.replace("${index}",
						String.valueOf(index)));
				getRoot().findElement(byParameterName).clear();
				getRoot().findElement(byParameterName).sendKeys(changedParameter.getKey());
				getRoot().findElement(byParameterValue).clear();
				getRoot().findElement(byParameterValue)
						.sendKeys(changedParameter.getValue().get(1));
				index++;
			}
		} else {
			addParameters(changedParameters);
		}
		waitForElementVisible(saveChangedParameterButton).click();
		clickOnCloseScheduleButton();
	}

	public void assertScheduleParameters(Map<String, List<String>> parameters)
			throws InterruptedException {
		waitForElementVisible(getRoot());
		int i = 1;
		if (parameters != null) {
			Thread.sleep(1000);
			for (Entry<String, List<String>> parameter : parameters.entrySet()) {
				assertEquals(parameter.getKey(), getRoot().findElement(
					By.xpath(XPATH_PARAMETER_NAME.replace("${index}", String.valueOf(i)))).getAttribute("value"));
				if (parameter.getValue().get(0).equals("secure")) {
					assertEquals("password", getRoot().findElement(
						By.xpath(XPATH_PARAMETER_VALUE.replace("${index}", String.valueOf(i)))).getAttribute("type"));
					assertEquals("Secure parameter value", getRoot().findElement(
						By.xpath(XPATH_PARAMETER_VALUE.replace("${index}", String.valueOf(i)))).getAttribute("placeholder"));
				} else {
					assertEquals(parameter.getValue().get(1),
						getRoot().findElement(By.xpath(XPATH_PARAMETER_VALUE.replace("${index}",
								String.valueOf(i)))).getAttribute("value"));
				}
				i++;
			}
		}
	}

	public void checkBrokenSchedule(String oldExecutable, String newExecutable) {
		waitForElementVisible(getRoot());
		waitForElementVisible(brokenScheduleMessage);
		System.out.println("Check broken schedule detail page...");
		assertEquals(String.format(BROKEN_SCHEDULE_MESSAGE, oldExecutable),
				brokenScheduleMessage.getText());
		getRoot().findElement(
				By.xpath(XPATH_EXECUTABLE_SELECTION_IN_BROKEN_SCHEDULE.replace("${option}",
						newExecutable))).click();
		waitForElementVisible(brokenScheduleSaveChangeButton).click();
		waitForElementNotPresent(brokenScheduleSaveChangeButton);
		clickOnCloseScheduleButton();
	}

	public void manualRunSchedule(int executionTimes, String executablePath,
			DISCProcessTypes processType) throws InterruptedException {
		waitForElementVisible(manualRunButton);
		for (int i = 0; i < executionTimes; i++) {
			manualRun();
			assertLastExecutionDetails(false, true, false, executablePath, processType, 5);
		}
	}

	public void checkFailedSchedule(String executablePath, DISCProcessTypes processType)
			throws InterruptedException {
		waitForElementVisible(cronPicker);
		manualRunSchedule(5, executablePath, processType);
		System.out.println("Schedule failed for the 5th time...");
		assertEquals(
				String.format(FAILED_SCHEDULE_FOR_5TH_TIME_MESSAGE, scheduleExecutionItem.size()),
				failedScheduleInfoSection.getText());
		manualRunSchedule(25, executablePath, processType);
		System.out.println("Schedule failed for the 30th time...");
		assertEquals(String.format(AUTO_DISABLED_SCHEDULE_MESSAGE, scheduleExecutionItem.size()),
				autoDisableScheduleMessage.getText());
		assertEquals(AUTO_DISABLED_SCHEDULE_MORE_INFO, autoDisableScheduleMoreInfo.getText());
		assertDisableSchedule(15);
		clickOnCloseScheduleButton();
	}
}
