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
import com.gooddata.qa.graphene.enums.ScheduleCronTimes;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

public class ScheduleDetail extends ScheduleForm {

    private static final String INVALID_SCHEDULE_TITLE_ERROR = "\'${scheduleName}\' name already in use within the process. Change the name.";
    private static final String EMPTY_SCHEDULE_TITLE_ERROR = "can't be blank";
    private static final String DEFAULT_RETRY_DELAY_VALUE = "30";
    private static final String RESCHEDULE_FORM_MESSAGE =
            "Restart every minutes until success (or 30th consecutive failure)";
    private static final String FAILED_SCHEDULE_FOR_5TH_TIME_MESSAGE =
            "This schedule has failed for the %dth time. We highly recommend disable this schedule until the issue is addressed. If you want to disable the schedule, click here or read troubleshooting article for more information.";
    private static final String AUTO_DISABLED_SCHEDULE_MESSAGE =
            "This schedule has been automatically disabled following its %dth consecutive failure. If you addressed the issue, you can enable it.";
    private static final String AUTO_DISABLED_SCHEDULE_MORE_INFO =
            "For more information read Automatic Disabling of Failed Schedules article at our support portal.";
    private static final String BROKEN_SCHEDULE_MESSAGE =
            "The graph %s doesn't exist because it has been changed (renamed or deleted). "
                    + "It isn't possible to execute this schedule because there is no graph to execute.";
    private static final String OK_GROUP_DESCRIPTION_FORMAT = "OK %dx";

    private static final By BY_EXECUTION_STATUS = By.cssSelector(".execution-status");
    private static final By BY_EXECUTION_DESCRIPTION = By
            .cssSelector(".ait-execution-history-item-description");
    private static final By BY_EXECUTION_LOG = By.cssSelector(".ait-execution-history-item-log");
    private static final By BY_EXECUTION_RUNTIME = By.cssSelector(".execution-runtime");
    private static final By BY_EXECUTION_DATE = By.cssSelector(".execution-date");
    private static final By BY_EXECUTION_TIMES = By.cssSelector(".execution-times");
    private static final By BY_OK_STATUS_ICON = By.cssSelector(".status-icon-ok");
    private static final By BY_ERROR_STATUS_ICON = By.cssSelector(".status-icon-error");
    private static final By BY_OK_LAST_RUN = By.cssSelector(".last-run");
    private static final By BY_MANUAL_ICON = By.cssSelector(".icon-manual");
    private static final By BY_CONFIRM_STOP_EXECUTION = By.cssSelector(".button-negative");
    private static final By BY_RUN_STOP_BUTTON = By
            .xpath("//div[contains(@class, 'ait-schedule-title-section')]//button[1]");
    private static final By BY_OK_GROUP_EXPAND_BUTTON = By.cssSelector(".icon-navigatedown");

    @FindBy(css = ".ait-schedule-title-section-heading")
    protected WebElement scheduleTitle;
    
    @FindBy(css = ".ait-schedule-title-field input")
    protected WebElement scheduleTitleInput;
    
    @FindBy(css = ".ait-schedule-title-edit-buttons .button-positive")
    protected WebElement saveScheduleTitleButton;
    
    @FindBy(css = ".ait-schedule-title-field .bubble-overlay")
    protected WebElement scheduleTitleErrorBubble;
    
    @FindBy(css = ".ait-schedule-title-edit-buttons .button-secondary")
    protected WebElement cancelChangeScheduleTitleButton;
    
    @FindBy(css = ".ait-schedule-close-btn .icon-delete")
    protected WebElement closeButton;

    @FindBy(css = ".ait-execution-history-item")
    protected List<WebElement> scheduleExecutionItems;

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

    @FindBy(css = ".ait-schedule-reschedule-delete-btn")
    protected WebElement removeRetryDelay;

    @FindBy(css = ".ait-schedule-retry-delete .dialog-body")
    protected WebElement removeRetryDialogBody;

    @FindBy(css = ".ait-schedule-retry-delete-confirm-btn")
    protected WebElement confirmRemoveRetryButton;

    @FindBy(css = ".ait-schedule-retry-delete-cancel-btn")
    protected WebElement cancelRemoveRetryButton;

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

    @FindBy(css = ".ait-schedule-executable-edit-buttons .button-positive")
    protected WebElement saveChangedCronTimeButton;

    @FindBy(css = ".ait-schedule-executable-edit-buttons .button-secondary")
    protected WebElement cancelChangedCronTimeButton;

    @FindBy(css = ".parameters-save-buttons .button-positive")
    protected WebElement saveChangedParameterButton;

    @FindBy(css = ".parameters-save-buttons .button-secondary")
    protected WebElement cancelChangedParameterButton;

    @FindBy(css = ".broken-schedule-info")
    protected WebElement brokenScheduleMessage;

    @FindBy(css = ".broken-schedule-info .schedule-title-select")
    protected WebElement brokenScheduleExecutable;

    @FindBy(css = ".ait-schedule-executable-edit-buttons .button-positive")
    protected WebElement brokenScheduleSaveChangeButton;

    @FindBy(css = ".info-section")
    protected WebElement failedScheduleInfoSection;

    @FindBy(css = ".ait-schedule-disabled .message p:nth-child(1)")
    protected WebElement autoDisableScheduleMessage;

    @FindBy(css = ".ait-schedule-disabled .message p:nth-child(2)")
    protected WebElement autoDisableScheduleMoreInfo;

    @FindBy(css = ".ait-schedule-executable-select-btn")
    protected WebElement selectExecutable;

    @FindBy(css = ".ait-execution-history-empty")
    protected WebElement executionHistoryEmptyState;

    public void clickOnCloseScheduleButton() {
        waitForElementVisible(closeButton).click();
    }

    public WebElement getSaveChangedCronTimeButton() {
        return saveChangedCronTimeButton;
    }

    public WebElement getExecutionHistoryEmptyState() {
        return waitForElementVisible(executionHistoryEmptyState);
    }

    public void waitForAutoRunSchedule(int waitingTimeInMinutes) throws InterruptedException {
        int executionNumber = scheduleExecutionItems.size();
        Calendar startWaitingTime = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("mm");
        int startWaitingMinute = Integer.valueOf(sdf.format(startWaitingTime.getTime()));
        for (int i = 0; i < waitingTimeInMinutes + 3; i++) {
            System.out.println("Number of executions: " + scheduleExecutionItems.size());
            if (scheduleExecutionItems.size() == executionNumber) {
                System.out.println("Waiting for auto execution...");
                Thread.sleep(60000);
            } else {
                if (scheduleExecutionItems.get(0).findElement(BY_EXECUTION_DESCRIPTION).getText()
                        .equals("SCHEDULED")) {
                    System.out.println("Schedule is in SCHEDULED state...");
                    Thread.sleep(60000);
                } else {
                    if (scheduleExecutionItems.get(0).findElement(BY_EXECUTION_DESCRIPTION)
                            .getText().equals("RUNNING"))
                        System.out.println("Schedule is in RUNNING state...");
                    Calendar startExecutionTime = Calendar.getInstance();
                    int startExecutionMinute =
                            Integer.valueOf(sdf.format(startExecutionTime.getTime()));
                    startExecutionMinute =
                            startExecutionMinute > startWaitingMinute ? startExecutionMinute
                                    : startExecutionMinute + 60;
                    int delayTime =
                            startExecutionMinute - startWaitingMinute - waitingTimeInMinutes;
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
        waitForElementVisible(scheduleExecutionItems.get(0));
        assertTrue(scheduleExecutionItems.get(0).isDisplayed());
        if (isSuccessful) {
            assertTrue(scheduleExecutionItems.get(0).findElement(BY_EXECUTION_STATUS)
                    .findElement(BY_OK_STATUS_ICON).isDisplayed());
            assertTrue(scheduleExecutionItems.get(0).findElement(BY_EXECUTION_DESCRIPTION)
                    .getText().contains("OK"));
        } else {
            assertTrue(scheduleExecutionItems.get(0).findElement(BY_EXECUTION_STATUS)
                    .findElement(BY_ERROR_STATUS_ICON).isDisplayed());
            System.out.println("Execution description: "
                    + scheduleExecutionItems.get(0).findElement(BY_EXECUTION_DESCRIPTION).getText());
            if (isStopped) {
                assertTrue(scheduleExecutionItems.get(0).findElement(BY_EXECUTION_DESCRIPTION)
                        .getText().equals("MANUALLY STOPPED"));
            } else {
                String errorMessage =
                        String.format("%s=%s error:", processType.getProcessTypeExecutable(),
                                executablePath).toLowerCase();
                assertTrue(scheduleExecutionItems.get(0).findElement(BY_EXECUTION_DESCRIPTION)
                        .getText().toLowerCase().contains(errorMessage));
            }
        }
        if (isManualRun)
            assertTrue(scheduleExecutionItems.get(0).findElement(BY_MANUAL_ICON).isDisplayed());
        assertTrue(scheduleExecutionItems.get(0).findElement(BY_EXECUTION_LOG).isDisplayed());
        assertTrue(scheduleExecutionItems.get(0).findElement(BY_EXECUTION_LOG).isEnabled());
        assertFalse(scheduleExecutionItems.get(0).findElement(BY_EXECUTION_RUNTIME).getText()
                .isEmpty());
        System.out.println("Execution Runtime: "
                + scheduleExecutionItems.get(0).findElement(BY_EXECUTION_RUNTIME).getText());
        assertFalse(scheduleExecutionItems.get(0).findElement(BY_EXECUTION_DATE).getText()
                .isEmpty());
        System.out.println("Execution Date: "
                + scheduleExecutionItems.get(0).findElement(BY_EXECUTION_DATE).getText());
        assertFalse(scheduleExecutionItems.get(0).findElement(BY_EXECUTION_TIMES).getText()
                .isEmpty());
        System.out.println("Execution Times: "
                + scheduleExecutionItems.get(0).findElement(BY_EXECUTION_TIMES).getText());
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

    public void removeRetryDelay(boolean isConfirmed) {
        waitForElementVisible(removeRetryDelay).click();
        waitForElementVisible(removeRetryDialogBody);
        if (isConfirmed) {
            waitForElementVisible(confirmRemoveRetryButton).click();
            waitForElementVisible(addRetryDelay);
        } else {
            waitForElementVisible(cancelRemoveRetryButton).click();
            waitForElementVisible(removeRetryDelay);
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
        if (scheduleExecutionItems.isEmpty())
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
        if (scheduleExecutionItems.size() > executionNumberBeforeDisable)
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
                    WebElement parameterValue =
                            parameters.get(index).findElement(BY_PARAMETER_VALUE);
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
            DISCProcessTypes processType, boolean isSuccessful) throws InterruptedException {
        waitForElementVisible(manualRunButton);
        for (int i = 0; i < executionTimes; i++) {
            manualRun();
            assertLastExecutionDetails(isSuccessful, true, false, executablePath, processType, 5);
        }
    }

    public void checkRepeatedFailureSchedule(String executablePath, DISCProcessTypes processType)
            throws InterruptedException {
        waitForElementVisible(cronPicker);
        repeatManualRun(5, executablePath, processType, false);
        System.out.println("Schedule failed for the 5th time...");
        waitForElementVisible(failedScheduleInfoSection);
        assertEquals(
                String.format(FAILED_SCHEDULE_FOR_5TH_TIME_MESSAGE, scheduleExecutionItems.size()),
                failedScheduleInfoSection.getText());
        repeatManualRun(25, executablePath, processType, false);
        System.out.println("Schedule failed for the 30th time...");
        waitForElementVisible(autoDisableScheduleMessage);
        assertEquals(String.format(AUTO_DISABLED_SCHEDULE_MESSAGE, scheduleExecutionItems.size()),
                autoDisableScheduleMessage.getText());
        assertEquals(AUTO_DISABLED_SCHEDULE_MORE_INFO, autoDisableScheduleMoreInfo.getText());
    }

    public boolean isInRunningState() throws InterruptedException {
        for (int i = 0; i < 10 && scheduleExecutionItems.isEmpty(); i++)
            Thread.sleep(1000);
        for (int i = 0; i < 50; i++) {
            if (scheduleExecutionItems.get(0).findElement(BY_EXECUTION_DESCRIPTION).getText()
                    .equals("RUNNING")) {
                return true;
            } else
                Thread.sleep(3000);
        }
        return false;
    }

    public boolean isInScheduledState() throws InterruptedException {
        for (int i = 0; i < 10 && scheduleExecutionItems.isEmpty(); i++)
            Thread.sleep(1000);
        for (int i = 0; i < 50; i++) {
            if (scheduleExecutionItems.get(0).findElement(BY_EXECUTION_DESCRIPTION).getText()
                    .equals("SCHEDULED")) {
                return true;
            } else
                Thread.sleep(3000);
        }
        return false;
    }

    public String getLastExecutionDate() {
        return scheduleExecutionItems.get(0).findElement(BY_EXECUTION_DATE).getText();
    }

    public String getLastExecutionTime() {
        return scheduleExecutionItems.get(0).findElement(BY_EXECUTION_TIMES).getText();
    }

    public String getExecutionRuntime() {
        return scheduleExecutionItems.get(0).findElement(BY_EXECUTION_RUNTIME).getText();
    }

    public String getExecutionDescription() {
        return scheduleExecutionItems.get(0).findElement(BY_EXECUTION_DESCRIPTION).getText();
    }

    public boolean isStarted() {
        return waitForElementVisible(manualStopButton).isDisplayed();
    }

    public int getExecutionItemsNumber() throws InterruptedException {
        for (int i = 0; i < 10 && scheduleExecutionItems.isEmpty(); i++)
            Thread.sleep(1000);
        return scheduleExecutionItems.size();
    }

    public WebElement getEnableButton() {
        return waitForElementVisible(enableScheduleButton);
    }

    public void expandLastOkExecutionGroup() {
        scheduleExecutionItems.get(0).findElement(BY_OK_GROUP_EXPAND_BUTTON).click();
    }

    public void checkOkExecutionGroup(int okExecutionNumber, int okGroupIndex)
            throws InterruptedException {
        int scheduleExecutionNumber = scheduleExecutionItems.size();
        String groupDescription = String.format(OK_GROUP_DESCRIPTION_FORMAT, okExecutionNumber);
        assertTrue(scheduleExecutionItems.get(okGroupIndex).findElement(BY_OK_STATUS_ICON)
                .isDisplayed());
        System.out.println("Execution description of ok execution group: "
                + scheduleExecutionItems.get(okGroupIndex).findElement(BY_EXECUTION_DESCRIPTION)
                        .getText());
        assertTrue(scheduleExecutionItems.get(okGroupIndex).findElement(BY_EXECUTION_DESCRIPTION)
                .getText().contains(groupDescription));
        assertTrue(scheduleExecutionItems.get(okGroupIndex).findElement(BY_OK_LAST_RUN)
                .isDisplayed());
        System.out.println("Execution description of ok execution group: "
                + scheduleExecutionItems.get(okGroupIndex).findElement(BY_EXECUTION_RUNTIME)
                        .getText());
        assertTrue(scheduleExecutionItems.get(okGroupIndex).findElement(BY_EXECUTION_RUNTIME)
                .isDisplayed());
        System.out.println("Execution description of ok execution group: "
                + scheduleExecutionItems.get(okGroupIndex).findElement(BY_EXECUTION_DATE).getText());
        assertTrue(scheduleExecutionItems.get(okGroupIndex).findElement(BY_EXECUTION_DATE)
                .isDisplayed());
        System.out.println("Execution description of ok execution group: "
                + scheduleExecutionItems.get(okGroupIndex).findElement(BY_EXECUTION_TIMES)
                        .getText());
        assertTrue(scheduleExecutionItems.get(okGroupIndex).findElement(BY_EXECUTION_TIMES)
                .isDisplayed());
        assertTrue(scheduleExecutionItems.get(okGroupIndex).findElement(BY_EXECUTION_LOG)
                .isDisplayed());
        waitForElementVisible(scheduleExecutionItems.get(okGroupIndex).findElement(
                BY_OK_GROUP_EXPAND_BUTTON));
        scheduleExecutionItems.get(okGroupIndex).findElement(BY_OK_GROUP_EXPAND_BUTTON).click();
        for (int i = 0; i < 10 && scheduleExecutionItems.size() == scheduleExecutionNumber; i++) {
            Thread.sleep(1000);
        }
        assertEquals(scheduleExecutionNumber + okExecutionNumber, scheduleExecutionItems.size());
        for (int i = okGroupIndex; i < okGroupIndex + okExecutionNumber + 1; i++) {
            if (i == okGroupIndex) {
                assertTrue(scheduleExecutionItems.get(i).findElement(BY_OK_STATUS_ICON)
                        .isDisplayed());
                System.out.println("Execution description at " + i + " index: "
                        + scheduleExecutionItems.get(i).findElement(BY_EXECUTION_DESCRIPTION)
                                .getText());
                assertTrue(scheduleExecutionItems.get(i).findElement(BY_EXECUTION_DESCRIPTION)
                        .getText().contains(groupDescription));
                assertFalse(scheduleExecutionItems.get(i).findElement(BY_EXECUTION_RUNTIME)
                        .isDisplayed());
                assertFalse(scheduleExecutionItems.get(i).findElement(BY_EXECUTION_DATE)
                        .isDisplayed());
                assertFalse(scheduleExecutionItems.get(i).findElement(BY_EXECUTION_TIMES)
                        .isDisplayed());
                assertFalse(scheduleExecutionItems.get(i).findElement(BY_EXECUTION_LOG)
                        .isDisplayed());
            } else {
                assertFalse(scheduleExecutionItems.get(i).findElement(BY_OK_STATUS_ICON)
                        .isDisplayed());
                System.out.println("Execution description at "+ i + " index: "
                        + scheduleExecutionItems.get(i).findElement(BY_EXECUTION_DESCRIPTION)
                                .getText());
                assertTrue(scheduleExecutionItems.get(i).findElement(BY_EXECUTION_DESCRIPTION)
                        .isDisplayed());
                System.out.println("Execution description at " + i + " index: "
                        + scheduleExecutionItems.get(i).findElement(BY_EXECUTION_RUNTIME).getText());
                assertTrue(scheduleExecutionItems.get(i).findElement(BY_EXECUTION_RUNTIME)
                        .isDisplayed());
                System.out.println("Execution description at " + i + " index: "
                        + scheduleExecutionItems.get(i).findElement(BY_EXECUTION_DATE).getText());
                assertTrue(scheduleExecutionItems.get(i).findElement(BY_EXECUTION_DATE)
                        .isDisplayed());
                System.out.println("Execution description at " + i + " index: "
                        + scheduleExecutionItems.get(i).findElement(BY_EXECUTION_TIMES).getText());
                assertTrue(scheduleExecutionItems.get(i).findElement(BY_EXECUTION_TIMES)
                        .isDisplayed());
                assertTrue(scheduleExecutionItems.get(i).findElement(BY_EXECUTION_LOG)
                        .isDisplayed());
            }
        }
    }
    
	public String getLastExecutionLogLink () {
		return scheduleExecutionItems.get(0).findElement(BY_EXECUTION_LOG).getAttribute("href");
	}
	
	public String getLastExecutionDescription() {
		return scheduleExecutionItems.get(0).findElement(BY_EXECUTION_DESCRIPTION).getText();
	}
    
    public void assertCronTime (Pair<String, List<String>> cronTime) throws InterruptedException {
        Select selectCron = new Select(cronPicker);
        assertEquals(selectCron.getFirstSelectedOption().getText(), cronTime.getKey());
        if (cronTime.getValue() != null) {
            if (cronTime.getKey().equals(ScheduleCronTimes.CRON_EXPRESSION.getCronTime())) {
                if (cronTime.getValue().get(0) != null) {
                    if (cronExpression.getAttribute("value").isEmpty())
                        Thread.sleep(1000);
                    assertEquals(cronTime.getValue().get(0),
                            cronExpression.getAttribute("value"));
                    System.out.println("Cron expression is set to... "
                            + cronExpression.getAttribute("value"));
                }
            }
            else {
                if (cronTime.getValue().size() > 2 && cronTime.getValue().get(2) != null) {
                    waitForElementVisible(selectDayInWeek);
                    Select selectWeek = new Select(selectDayInWeek);
                    assertEquals(cronTime.getValue().get(2), selectWeek.getFirstSelectedOption().getText());
                }
                else if (cronTime.getValue().size() > 1 && cronTime.getValue().get(1) != null) {
                    waitForElementVisible(selectHourInDay);
                    Select selectHour = new Select(selectHourInDay);
                    assertEquals(cronTime.getValue().get(1), selectHour.getFirstSelectedOption().getText());
                }
                else if (cronTime.getValue().size() > 0 && cronTime.getValue().get(0) != null) {
                    waitForElementVisible(selectMinuteInHour);
                    Select selectMinute = new Select(selectMinuteInHour);
                    assertEquals(cronTime.getValue().get(0), selectMinute.getFirstSelectedOption().getText());
                }
            }
        }
    }

    public void assertScheduleDetail(String scheduleName, String executable,
            Pair<String, List<String>> cronTime, Map<String, List<String>> parameters)
            throws InterruptedException {
        waitForElementVisible(scheduleTitle);
        assertEquals(scheduleName, scheduleTitle.getText());
        waitForElementVisible(selectExecutable);
        Select select = new Select(selectExecutable);
        if (!select.getFirstSelectedOption().equals(executable))
            Thread.sleep(2000);
        assertEquals(select.getFirstSelectedOption().getText(), executable);
        assertCronTime(cronTime);
        assertScheduleParameters(parameters);
    }
    
    public void changeScheduleTitle(String newScheduleName, boolean isSaved, boolean isValid)
            throws InterruptedException {
        waitForElementVisible(scheduleTitle).click();
        waitForElementVisible(scheduleTitleInput).clear();
        if (!scheduleTitleInput.getText().isEmpty())
            Thread.sleep(2000);
        scheduleTitleInput.sendKeys(newScheduleName);
        if (isSaved) {
            saveScheduleTitleButton.click();
            if (!isValid) {
                assertTrue(scheduleTitleInput.getAttribute("class").contains("has-error"));
                waitForElementVisible(scheduleTitleErrorBubble);
                if (newScheduleName.isEmpty())
                    assertEquals(scheduleTitleErrorBubble.getText(), EMPTY_SCHEDULE_TITLE_ERROR);
                else
                    assertEquals(scheduleTitleErrorBubble.getText(),
                            INVALID_SCHEDULE_TITLE_ERROR.replace("${scheduleName}", newScheduleName));
            }
        } else
            cancelChangeScheduleTitleButton.click();
    }
}
