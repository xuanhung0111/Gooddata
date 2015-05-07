package com.gooddata.qa.graphene.fragments.disc;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder.CronTimeBuilder;
import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder.Parameter;
import com.gooddata.qa.graphene.enums.disc.ScheduleCronTimes;
import com.gooddata.qa.graphene.enums.disc.DeployPackages.Executables;
import com.google.common.base.Predicate;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

public class ScheduleDetail extends ScheduleForm {

    private static final int MAX_DELAY_TIME_WAITING_AUTO_RUN = 2; // In minutes

    public enum Confirmation {
        SAVE_CHANGES,
        CANCEL_CHANGES;
    }

    private static final int MAX_SCHEDULE_RUN_TIME = 15; // In minutes

    private static final String INVALID_SCHEDULE_TITLE_ERROR =
            "\'${scheduleName}\' name already in use within the process. Change the name.";
    private static final String EMPTY_SCHEDULE_TITLE_ERROR = "can't be blank";
    private static final String TRIGGER_SCHEDULE_MISSING_MESSAGE =
            "The schedule that triggers this schedule is missing. To run this schedule, set a new trigger or select a cron frequency.";
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
    private static final String OK_GROUP_DESCRIPTION_FORMAT = "OK %d×";

    private static final By BY_EXECUTION_STATUS = By.cssSelector(".execution-status");
    private static final By BY_EXECUTION_DESCRIPTION = By
            .cssSelector(".ait-execution-history-item-description");
    private static final By BY_EXECUTION_ERROR_DESCRIPTION = By
            .cssSelector(".ait-execution-history-item-description.is-error");
    private static final By BY_EXECUTION_LOG = By.cssSelector(".ait-execution-history-item-log");
    private static final By BY_EXECUTION_RUNTIME = By.cssSelector(".execution-runtime");
    private static final By BY_EXECUTION_DATE = By.cssSelector(".execution-date");
    private static final By BY_EXECUTION_TIMES = By.cssSelector(".execution-times");
    private static final By BY_OK_STATUS_ICON = By.cssSelector(".status-icon-ok");
    private static final By BY_ERROR_STATUS_ICON = By.cssSelector(".status-icon-error");
    private static final By BY_SCHEDULER_ERROR_STATUS_ICON = By.cssSelector(".status-icon-scheduler-error");
    private static final By BY_OK_LAST_RUN = By.cssSelector(".last-run");
    private static final By BY_MANUAL_ICON = By.cssSelector(".icon-manual");
    private static final By BY_CONFIRM_STOP_EXECUTION = By.cssSelector(".button-negative");
    private static final By BY_RUN_STOP_BUTTON = By
            .xpath("//div[contains(@class, 'ait-schedule-title-section')]//button[1]");
    private static final By BY_OK_GROUP_EXPAND_BUTTON = By.cssSelector(".icon-navigatedown");
    private static final By BY_PARAMETERS_EDIT_SECTION = By.cssSelector(".parameters-section.modified");

    @FindBy(css = ".ait-schedule-title-section-heading")
    private WebElement scheduleTitle;

    @FindBy(css = ".ait-schedule-title-field input")
    private WebElement scheduleTitleInput;

    @FindBy(css = ".ait-schedule-title-edit-buttons .button-positive")
    private WebElement saveScheduleTitleButton;

    @FindBy(css = ".ait-schedule-title-edit-buttons .button-secondary")
    private WebElement cancelChangeScheduleTitleButton;

    @FindBy(css = ".ait-schedule-close-btn .icon-delete")
    private WebElement closeButton;

    @FindBy(css = ".ait-execution-history-item")
    private List<WebElement> scheduleExecutionItems;

    @FindBy(css = ".ait-execution-history-item:first-child")
    private WebElement lastExecutionItem;

    @FindBy(css = ".ait-schedule-reschedule-add-btn")
    private WebElement addRetryDelay;

    @FindBy(css = ".reschedule-form")
    private WebElement rescheduleForm;

    @FindBy(css = ".ait-schedule-reschedule-value input")
    private WebElement retryDelayInput;

    @FindBy(css = ".ait-schedule-reschedule-edit-buttons .button-positive")
    private WebElement saveRetryDelayButton;

    @FindBy(css = ".ait-schedule-reschedule-edit-buttons .button-secondary")
    private WebElement cancelAddRetryDelayButton;

    @FindBy(css = ".ait-schedule-reschedule-delete-btn")
    private WebElement removeRetryDelay;

    @FindBy(css = ".ait-schedule-retry-delete .dialog-body")
    private WebElement removeRetryDialogBody;

    @FindBy(css = ".ait-schedule-retry-delete-confirm-btn")
    private WebElement confirmRemoveRetryButton;

    @FindBy(css = ".ait-schedule-retry-delete-cancel-btn")
    private WebElement cancelRemoveRetryButton;

    @FindBy(css = ".ait-dataset-selection-radio-all")
    private WebElement selectSynchronizeAllDatasets;

    @FindBy(css = ".ait-dataset-selection-radio-custom")
    private WebElement selectSynchronizeSelectedDatasets;

    @FindBy(css = ".ait-dataset-selection-dropdown-button")
    private WebElement openDatasetPickerButton;

    @FindBy(css = ".s-btn-select_all")
    private WebElement selectAllCustomDatasetsButton;

    @FindBy(css = ".s-btn-clear")
    private WebElement selectNoneCustomDatasetsButton;

    @FindBy(css = ".ait-dataset-selection-dropdown")
    private WebElement datasetDialog;

    @FindBy(css = ".datasets-messages")
    private WebElement dataloadDatasetsMessages;

    @FindBy(css = ".ait-schedule-run-btn")
    private WebElement manualRunButton;

    @FindBy(css = ".dialog-main.ait-schedule-run")
    private WebElement manualRunDialog;

    @FindBy(css = ".ait-schedule-run-confirm-btn")
    private WebElement confirmRunButton;

    @FindBy(css = ".ait-schedule-stop-btn")
    private WebElement manualStopButton;

    @FindBy(css = ".overlay .dialog-main")
    private WebElement manualStopDialog;

    @FindBy(css = ".ait-schedule-cron-section .disable-button button")
    private WebElement disableScheduleButton;

    @FindBy(css = ".schedule-title .status-icon-disabled")
    private WebElement disabledScheduleIcon;

    @FindBy(css = ".ait-schedule-enable-btn")
    private WebElement enableScheduleButton;

    @FindBy(css = ".ait-schedule-executable-edit-buttons .button-positive")
    private WebElement saveChangedExecutable;

    @FindBy(css = ".ait-schedule-executable-edit-buttons .button-secondary")
    private WebElement cancelChangedExecutable;

    @FindBy(css = ".ait-schedule-delete-btn")
    private WebElement deleteScheduleButton;

    @FindBy(css = ".dialog-main.ait-schedule-delete-fragment")
    private WebElement deleteScheduleDialog;

    @FindBy(css = ".ait-schedule-delete-confirm-btn")
    private WebElement confirmDeleteScheduleButton;

    @FindBy(css = ".ait-schedule-delete-cancel-btn")
    private WebElement cancelDeleteScheduleButton;

    @FindBy(css = ".ait-schedule-executable-edit-buttons .button-positive")
    private WebElement saveChangedCronTimeButton;

    @FindBy(css = ".ait-schedule-executable-edit-buttons .button-secondary")
    private WebElement cancelChangedCronTimeButton;

    @FindBy(css = ".parameters-save-buttons .button-positive")
    private WebElement saveChangedParameterButton;

    @FindBy(css = ".parameters-save-buttons .button-secondary")
    private WebElement cancelChangedParameterButton;

    @FindBy(css = ".broken-schedule-info")
    private WebElement brokenScheduleMessage;

    @FindBy(css = ".broken-schedule-info .schedule-title-select")
    private WebElement brokenScheduleExecutable;

    @FindBy(css = ".ait-schedule-executable-edit-buttons .button-positive")
    private WebElement brokenScheduleSaveChangeButton;

    @FindBy(css = ".info-section")
    private WebElement failedScheduleInfoSection;

    @FindBy(css = ".ait-schedule-disabled .message p:nth-child(1)")
    private WebElement autoDisableScheduleMessage;

    @FindBy(css = ".ait-schedule-disabled .message p:nth-child(2)")
    private WebElement autoDisableScheduleMoreInfo;

    @FindBy(css = ".ait-schedule-executable-select-btn")
    private WebElement selectExecutable;

    @FindBy(css = ".ait-execution-history-empty")
    private WebElement executionHistoryEmptyState;

    @FindBy(css = ".error-trigger-message")
    private WebElement triggerScheduleMissingMessage;

    @FindByJQuery(".ait-execution-history-item-description:contains(RUNNING)")
    private WebElement runningExecutionItem;

    @FindByJQuery(".ait-execution-history-item-description:contains(SCHEDULED)")
    private WebElement scheduledExecutionItem;

    public void assertSchedule(final ScheduleBuilder scheduleBuilder) {
        waitForElementVisible(scheduleTitle);
        assertEquals(scheduleBuilder.getScheduleName(), scheduleTitle.getText());

        if (!scheduleBuilder.isDataloadProcess()) {
            waitForElementVisible(selectExecutable);
            final Select select = new Select(selectExecutable);
            Graphene.waitGui().until(new Predicate<WebDriver>() {

                @Override
                public boolean apply(WebDriver arg0) {
                    return select.getFirstSelectedOption().getText()
                            .equals(scheduleBuilder.getExecutable().getExecutablePath());
                }
            });
        }

        assertCronTime(scheduleBuilder.getCronTimeBuilder());
        if (!scheduleBuilder.getParameters().isEmpty())
            assertScheduleParameters(scheduleBuilder.getParameters());

        if (scheduleBuilder.isDataloadProcess()) {
            assertDataloadScheduleDatasets(scheduleBuilder);
        }
    }

    public WebElement getExecutionHistoryEmptyState() {
        return waitForElementVisible(executionHistoryEmptyState);
    }

    public void clickOnCloseScheduleButton() {
        waitForElementVisible(closeButton).click();
    }

    public void tryToWaitForAutoRun(CronTimeBuilder cronTimebuilder) {
        final int executionNumber = scheduleExecutionItems.size();
        try {
            Graphene.waitGui()
                    .withTimeout(
                            cronTimebuilder.getWaitingAutoRunInMinutes()
                                    + MAX_DELAY_TIME_WAITING_AUTO_RUN, TimeUnit.MINUTES)
                    .pollingEvery(5, TimeUnit.SECONDS)
                    .withMessage("Schedule doesn't run automatically!")
                    .until(new Predicate<WebDriver>() {

                        @Override
                        public boolean apply(WebDriver arg0) {
                            System.out.println("Waiting for auto execution...");
                            return scheduleExecutionItems.size() > executionNumber;
                        }
                    });
        } catch (TimeoutException ex) {
            System.out.println("Schedule doesn't run automatically");
        }
    }

    public void waitForAutoRunSchedule(CronTimeBuilder cronTimebuilder) {
        waitForAutoRun(cronTimebuilder.getWaitingAutoRunInMinutes());
    }

    public void waitForRetrySchedule(ScheduleBuilder scheduleBuilder) {
        waitForAutoRun(scheduleBuilder.getRetryDelay());
    }

    public void waitForExecutionFinish() {
        Graphene.waitGui().withTimeout(MAX_SCHEDULE_RUN_TIME, TimeUnit.MINUTES)
                .pollingEvery(5, TimeUnit.SECONDS)
                .withMessage("Schedule execution is not finished!")
                .until(new Predicate<WebDriver>() {

                    @Override
                    public boolean apply(WebDriver arg0) {
                        System.out.println("Waiting for running schedule...");
                        return getRoot().findElement(BY_RUN_STOP_BUTTON).getText().equals("Run");
                    }

                });
        waitForElementVisible(lastExecutionItem);
    }

    public void assertSuccessfulExecution() {
        waitForExecutionFinish();
        assertTrue(lastExecutionItem.findElement(BY_EXECUTION_STATUS)
                .findElement(BY_OK_STATUS_ICON).isDisplayed());
        assertTrue(lastExecutionItem.findElement(BY_EXECUTION_DESCRIPTION).getText().contains("OK"));
        assertExecutionDetail();
    }

    public void assertFailedExecution(Executables executable) {
        waitForExecutionFinish();
        assertTrue(lastExecutionItem.findElement(BY_EXECUTION_STATUS)
                .findElement(BY_ERROR_STATUS_ICON).isDisplayed());
        System.out.println("Error message of failed execution: "
                + lastExecutionItem.findElement(BY_EXECUTION_DESCRIPTION).getText());
        assertTrue(lastExecutionItem.findElement(BY_EXECUTION_DESCRIPTION).getText()
                .contains(executable.getErrorMessage()));
        assertExecutionDetail();
    }

    public void assertManualStoppedExecution() {
        waitForElementVisible(BY_ERROR_STATUS_ICON, browser);
        assertTrue(lastExecutionItem.findElement(BY_EXECUTION_STATUS)
                .findElement(BY_ERROR_STATUS_ICON).isDisplayed());
        assertTrue(lastExecutionItem.findElement(BY_EXECUTION_DESCRIPTION).getText()
                .equals("MANUALLY STOPPED"));
        assertExecutionDetail();
    }

    public void assertManualRunExecution() {
        assertTrue(lastExecutionItem.findElement(BY_MANUAL_ICON).isDisplayed());
    }

    public void checkRescheduleMessageAndDefault() {
        waitForElementVisible(addRetryDelay).click();
        waitForElementVisible(rescheduleForm);
        System.out.println("Reschedule form info: " + rescheduleForm.getText());
        assertEquals(RESCHEDULE_FORM_MESSAGE, rescheduleForm.getText());
        waitForElementVisible(retryDelayInput);
        assertEquals(DEFAULT_RETRY_DELAY_VALUE, retryDelayInput.getAttribute("value"));
        waitForElementVisible(cancelAddRetryDelayButton).click();
    }

    public void addValidRetry(String retryDelay, Confirmation saveChange) {
        addRetry(retryDelay);
        if (saveChange == Confirmation.SAVE_CHANGES) {
            waitForElementVisible(saveRetryDelayButton).click();
            waitForElementNotPresent(saveRetryDelayButton);
            assertEquals(String.valueOf(retryDelay), retryDelayInput.getAttribute("value"));
        } else {
            waitForElementVisible(cancelAddRetryDelayButton).click();
            waitForElementNotPresent(retryDelayInput);
            waitForElementVisible(addRetryDelay);
        }
    }

    public void addInvalidRetry(String invalidRetryDelay) {
        addRetry(invalidRetryDelay);
        waitForElementVisible(saveRetryDelayButton).click();
        assertTrue(waitForElementVisible(retryDelayInput).getAttribute("class").contains("has-error"));
        String errorBubbleMessage = waitForElementVisible(BY_ERROR_BUBBLE, browser).getText();
        System.out.println("Error retry delay: " + errorBubbleMessage);
        assertTrue(errorBubbleMessage
                .matches("The minimal delay is every 15 minutes.([\\n]*[\\r]*)Use numbers only."));
    }

    public void removeRetryDelay(Confirmation remove) {
        waitForElementVisible(removeRetryDelay).click();
        waitForElementVisible(removeRetryDialogBody);
        if (remove == Confirmation.SAVE_CHANGES) {
            waitForElementVisible(confirmRemoveRetryButton).click();
            waitForElementVisible(addRetryDelay);
        } else {
            waitForElementVisible(cancelRemoveRetryButton).click();
            waitForElementVisible(removeRetryDelay);
        }
    }

    public void manualRun() {
        tryToRun();
        waitForElementVisible(manualStopButton);
        System.out.println("Schedule is executed manually!");
    }

    public void tryToRun() {
        waitForElementVisible(manualRunButton).click();
        waitForElementVisible(manualRunDialog);
        waitForElementVisible(confirmRunButton).click();
    }

    public void manualStop() {
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

    public boolean isDisabledSchedule(CronTimeBuilder cronTimeBuilder) {
        int executionNumberBeforeDisable = scheduleExecutionItems.size();
        tryToWaitForAutoRun(cronTimeBuilder);
        if (scheduleExecutionItems.size() > executionNumberBeforeDisable)
            return false;
        System.out.println("Schedule is disabled successfully!");
        return true;
    }

    public void changeExecutable(Executables newExecutable, Confirmation saveChange) {
        waitForElementVisible(selectExecutable);
        final Select select = new Select(selectExecutable);
        Graphene.waitGui().until(new Predicate<WebDriver>() {

            @Override
            public boolean apply(WebDriver arg0) {
                return !select.getFirstSelectedOption().equals(select.getOptions().get(0));
            }
        });
        select.selectByVisibleText(newExecutable.getExecutablePath());
        if (saveChange == Confirmation.SAVE_CHANGES) {
            waitForElementVisible(saveChangedExecutable).click();
            waitForElementNotPresent(saveChangedExecutable);
        } else {
            waitForElementVisible(cancelChangedExecutable).click();
        }
        clickOnCloseScheduleButton();
    }

    public void changeCronTime(CronTimeBuilder cronTimeBuilder, Confirmation saveChange) {
        waitForElementVisible(getRoot());
        selectCron(cronTimeBuilder);
        if (saveChange == Confirmation.SAVE_CHANGES)
            waitForElementVisible(saveChangedCronTimeButton).click();
        else
            waitForElementVisible(cancelChangedCronTimeButton).click();
    }

    public void confirmParamsChange(Confirmation saveChange) {
        if (saveChange == Confirmation.SAVE_CHANGES)
            waitForElementVisible(saveChangedParameterButton).click();
        else
            waitForElementVisible(cancelChangedParameterButton).click();
        waitForElementNotPresent(BY_PARAMETERS_EDIT_SECTION);
    }

    public void addNewParams(List<Parameter> newParams, Confirmation saveChange) {
        addParameters(newParams);
        confirmParamsChange(saveChange);
    }

    public void editParameter(final Parameter existingParam, Parameter editedParam) {
        WebElement parameter = Iterables.find(parameters, new Predicate<WebElement>() {

            @Override
            public boolean apply(WebElement param) {
                return param.findElement(BY_PARAMETER_NAME).getAttribute("value")
                        .equals(existingParam.getParamName());
            }
        });

        WebElement paramName = parameter.findElement(BY_PARAMETER_NAME);
        paramName.clear();
        paramName.sendKeys(editedParam.getParamName());
        WebElement paramValue = parameter.findElement(BY_PARAMETER_VALUE);
        paramValue.clear();
        paramValue.sendKeys(editedParam.getParamValue());
    }

    public void removeParameter(List<Parameter> paramsToRemove, Confirmation saveChange) {
        for (final Parameter paramToRemove : paramsToRemove) {
            Iterables.find(parameters, new Predicate<WebElement>() {

                @Override
                public boolean apply(WebElement param) {
                    return param.findElement(BY_PARAMETER_NAME).getAttribute("value")
                            .equals(paramToRemove.getParamName());
                }
            }).findElement(BY_PARAMETER_REMOVE_ACTION).click();
        }
        confirmParamsChange(saveChange);
    }

    public void checkMessageInBrokenScheduleDetail(String scheduleName) {
        waitForElementVisible(getRoot());
        waitForElementVisible(brokenScheduleMessage);
        System.out.println("Check broken schedule detail page...");
        assertEquals(String.format(BROKEN_SCHEDULE_MESSAGE, scheduleName),
                brokenScheduleMessage.getText());
    }

    public void fixBrokenSchedule(Executables newExecutable) {
        Select select = new Select(brokenScheduleExecutable);
        select.selectByVisibleText(newExecutable.getExecutablePath());
        waitForElementVisible(brokenScheduleSaveChangeButton).click();
        waitForElementNotPresent(brokenScheduleSaveChangeButton);
        clickOnCloseScheduleButton();
    }

    public void repeatManualRunSuccessfulSchedule(int executionTimes) {
        for (int i = 0; i < executionTimes; i++) {
            manualRun();
            assertSuccessfulExecution();
        }
    }

    public void repeatManualRunFailedSchedule(int executionTimes, Executables executable) {
        for (int i = 0; i < executionTimes; i++) {
            manualRun();
            assertFailedExecution(executable);
        }
    }

    public void checkRepeatedFailureSchedule(CronTimeBuilder cronTimeBuilder, Executables executable) {
        repeatManualRunFailedSchedule(5, executable);
        System.out.println("Schedule failed for the 5th time...");
        waitForElementVisible(failedScheduleInfoSection);
        assertEquals(
                String.format(FAILED_SCHEDULE_FOR_5TH_TIME_MESSAGE, scheduleExecutionItems.size()),
                failedScheduleInfoSection.getText());
        repeatManualRunFailedSchedule(25, executable);
        System.out.println("Schedule failed for the 30th time...");
        waitForElementVisible(autoDisableScheduleMessage);
        assertEquals(String.format(AUTO_DISABLED_SCHEDULE_MESSAGE, scheduleExecutionItems.size()),
                autoDisableScheduleMessage.getText());
        assertEquals(AUTO_DISABLED_SCHEDULE_MORE_INFO, autoDisableScheduleMoreInfo.getText());
        assertTrue(isDisabledSchedule(cronTimeBuilder));
    }

    public boolean isInRunningState() {
        waitForCollectionIsNotEmpty(scheduleExecutionItems);
        try {
            Graphene.waitGui().withTimeout(MAX_SCHEDULE_RUN_TIME, TimeUnit.MINUTES)
                    .pollingEvery(2, TimeUnit.SECONDS).until().element(runningExecutionItem).is()
                    .visible();
            Graphene.waitGui().until(new Predicate<WebDriver>() {

                @Override
                public boolean apply(WebDriver arg0) {
                    return !lastExecutionItem.findElement(BY_EXECUTION_RUNTIME).getText().isEmpty();
                }});
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public boolean isInScheduledState() {
        waitForCollectionIsNotEmpty(scheduleExecutionItems);
        try {
            waitForElementVisible(scheduledExecutionItem);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public String getLastExecutionDate() {
        return lastExecutionItem.findElement(BY_EXECUTION_DATE).getText();
    }

    public String getLastExecutionTime() {
        return lastExecutionItem.findElement(BY_EXECUTION_TIMES).getText();
    }

    public String getExecutionRuntime() {
        return lastExecutionItem.findElement(BY_EXECUTION_RUNTIME).getText();
    }

    public String getExecutionDescription() {
        return lastExecutionItem.findElement(BY_EXECUTION_DESCRIPTION).getText();
    }

    public String getExecutionErrorDescription() {
        waitForElementVisible(lastExecutionItem);
        return waitForElementVisible(lastExecutionItem.findElement(BY_EXECUTION_ERROR_DESCRIPTION)).getText();
    }

    public boolean isStarted() {
        return waitForElementVisible(manualStopButton).isDisplayed();
    }

    public int getExecutionItemsNumber() {
        return scheduleExecutionItems.size();
    }

    public WebElement getEnableButton() {
        return waitForElementVisible(enableScheduleButton);
    }

    public void checkOkExecutionGroup(final int okExecutionNumber, int okGroupIndex) {
        final int scheduleExecutionNumber = scheduleExecutionItems.size();
        String groupDescription = String.format(OK_GROUP_DESCRIPTION_FORMAT, okExecutionNumber);
        WebElement okExecutionGroup = scheduleExecutionItems.get(okGroupIndex);

        assertOkExecutionGroupInfo(okExecutionGroup, groupDescription);
        okExecutionGroup.findElement(BY_OK_GROUP_EXPAND_BUTTON).click();
        Graphene.waitGui().until(new Predicate<WebDriver>() {

            @Override
            public boolean apply(WebDriver arg0) {
                return scheduleExecutionItems.size() == scheduleExecutionNumber + okExecutionNumber;
            }
        });
        assertExecutionItemsInfo(okGroupIndex, okExecutionNumber, groupDescription);
    }

    public String getLastExecutionLogLink() {
        return lastExecutionItem.findElement(BY_EXECUTION_LOG).getAttribute("href");
    }

    public String getLastExecutionLogTitle() {
        return lastExecutionItem.findElement(BY_EXECUTION_LOG).getAttribute("title").trim();
    }

    public boolean isLastSchedulerErrorIconVisible() {
        return waitForElementVisible(lastExecutionItem).findElements(BY_SCHEDULER_ERROR_STATUS_ICON).size() > 0 ;
    }

    public void changeValidScheduleName(String newScheduleName, Confirmation saveChange) {
        changeScheduleName(newScheduleName);
        if (saveChange == Confirmation.SAVE_CHANGES)
            waitForElementVisible(saveScheduleTitleButton).click();
        else
            waitForElementVisible(cancelChangeScheduleTitleButton).click();
    }

    public void changeAndCheckDatasetDialog(ScheduleBuilder scheduleBuilder) {
        assertTrue(!selectSynchronizeAllDatasets.isSelected());
        assertTrue(selectSynchronizeSelectedDatasets.isSelected());
        assertTrue(openDatasetPickerButton.getText().contains(
                scheduleBuilder.getDatasetsToSynchronize().size() + " of "
                        + scheduleBuilder.getAllDatasets().size() + " datasets"));

        openDatasetPickerButton.click();
        assertChecked(scheduleBuilder.getDatasetsToSynchronize());

        selectAllCustomDatasetsButton.click();
        assertChecked(scheduleBuilder.getAllDatasets());

        selectNoneCustomDatasetsButton.click();
        assertChecked(Collections.<String>emptyList());

        waitForElementVisible(datasetDialog).findElement(By.className("button-positive")).click();
        scheduleBuilder.setDatasetsToSynchronize(Collections.<String>emptyList());
        assertSchedule(scheduleBuilder);
    }

    private void assertChecked(List<String> datasetsToSynchronize) {
        List<WebElement> items =
                waitForElementVisible(datasetDialog)
                        .findElements(By.className("gd-list-view-item"));
        for (WebElement item : items) {
            if (datasetsToSynchronize.contains(item.getText())) {
                assertTrue(item.getAttribute("class").contains("is-selected"));
            } else {
                assertTrue(!item.getAttribute("class").contains("is-selected"));
            }
        }
    }

    public void changeInvalidScheduleName(String invalidScheduleName) {
        changeScheduleName(invalidScheduleName);
        waitForElementVisible(saveScheduleTitleButton).click();
        waitForElementVisible(scheduleTitleInput).click();
        assertTrue(scheduleTitleInput.getAttribute("class").contains("has-error"));
        String errorBubbleMessage = waitForElementVisible(BY_ERROR_BUBBLE, browser).getText();
        if (invalidScheduleName.isEmpty())
            assertEquals(errorBubbleMessage, EMPTY_SCHEDULE_TITLE_ERROR);
        else
            assertEquals(errorBubbleMessage,
                    INVALID_SCHEDULE_TITLE_ERROR.replace("${scheduleName}", invalidScheduleName));

        clickOnCloseScheduleButton();
    }

    public void checkTriggerScheduleMissing() {
        waitForElementVisible(triggerScheduleMissingMessage);
        assertEquals(triggerScheduleMissingMessage.getText(), TRIGGER_SCHEDULE_MISSING_MESSAGE);
        assertCronTime(new CronTimeBuilder().setCronTime(ScheduleCronTimes.CRON_EXPRESSION)
                .setCronTimeExpression("0 * * * *"));
        waitForElementPresent(saveChangedCronTimeButton);
        waitForElementNotPresent(cancelChangedCronTimeButton);
    }

    public void deleteSchedule(Confirmation delete) {
        waitForElementVisible(getRoot());
        waitForElementVisible(deleteScheduleButton).click();
        waitForElementVisible(deleteScheduleDialog);
        if (delete == Confirmation.SAVE_CHANGES)
            waitForElementVisible(confirmDeleteScheduleButton).click();
        else
            waitForElementVisible(cancelDeleteScheduleButton).click();
        waitForElementNotPresent(deleteScheduleDialog);
    }

    private void waitForAutoRun(int waitingTimeInMinutes) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        final int executionNumberBeforeAutoRun = scheduleExecutionItems.size();
        Graphene.waitGui()
                .withTimeout(waitingTimeInMinutes + MAX_DELAY_TIME_WAITING_AUTO_RUN,
                        TimeUnit.MINUTES).pollingEvery(5, TimeUnit.SECONDS)
                .withMessage("Schedule doesn't run automatically!")
                .until(new Predicate<WebDriver>() {

                    @Override
                    public boolean apply(WebDriver arg0) {
                        System.out.println("Waiting for auto execution...");
                        return scheduleExecutionItems.size() > executionNumberBeforeAutoRun;
                    }
                });
        stopwatch.stop();
        assertEquals(scheduleExecutionItems.size(), executionNumberBeforeAutoRun + 1);
        long delayTime = stopwatch.elapsed(TimeUnit.MINUTES) - waitingTimeInMinutes;
        System.out.println("Delay time: " + delayTime);
        assertTrue(delayTime >= -2 && delayTime <= 2, "Auto run at incorrect time! Delay time: "
                + delayTime);
    }

    private void assertExecutionDetail() {
        assertTrue(lastExecutionItem.findElement(BY_EXECUTION_LOG).isDisplayed());
        assertTrue(lastExecutionItem.findElement(BY_EXECUTION_LOG).isEnabled());
        assertFalse(lastExecutionItem.findElement(BY_EXECUTION_RUNTIME).getText().isEmpty());
        System.out.println("Execution Runtime: "
                + lastExecutionItem.findElement(BY_EXECUTION_RUNTIME).getText());
        assertFalse(lastExecutionItem.findElement(BY_EXECUTION_DATE).getText().isEmpty());
        System.out.println("Execution Date: "
                + lastExecutionItem.findElement(BY_EXECUTION_DATE).getText());
        assertFalse(lastExecutionItem.findElement(BY_EXECUTION_TIMES).getText().isEmpty());
        System.out.println("Execution Times: "
                + lastExecutionItem.findElement(BY_EXECUTION_TIMES).getText());
    }

    private void addRetry(String retryDelay) {
        waitForElementVisible(addRetryDelay).click();
        waitForElementVisible(rescheduleForm);
        waitForElementVisible(retryDelayInput).clear();
        retryDelayInput.sendKeys(String.valueOf(retryDelay));
    }

    private void changeScheduleName(String newScheduleName) {
        waitForElementVisible(scheduleTitle).click();
        waitForElementVisible(scheduleTitleInput).clear();
        scheduleTitleInput.sendKeys(newScheduleName);
    }

    private void assertDataloadScheduleDatasets(ScheduleBuilder scheduleBuilder) {
        if (scheduleBuilder.isSynchronizeAllDatasets()) {
            assertTrue(selectSynchronizeAllDatasets.isSelected());
            assertTrue(!selectSynchronizeSelectedDatasets.isSelected());
        } else {
            assertTrue(!selectSynchronizeAllDatasets.isSelected());
            assertTrue(selectSynchronizeSelectedDatasets.isSelected());
            assertTrue(openDatasetPickerButton.getText().contains(
                    scheduleBuilder.getDatasetsToSynchronize().size() + " of "
                            + scheduleBuilder.getAllDatasets().size() + " datasets"));
        }

        if (scheduleBuilder.isDataloadDatasetsOverlap()) {
            assertEquals(
                    dataloadDatasetsMessages.getText(),
                    "One or more of the selected datasets is already included in an existing schedule. If multiple schedules that load same dataset run concurrently, all schedules except the first will fail.");
        }
    }

    private void assertScheduleParameters(List<Parameter> expectedParams) {
        for (final Parameter expectedParam : expectedParams) {
            WebElement actualParam = Iterables.find(parameters, new Predicate<WebElement>() {

                @Override
                public boolean apply(WebElement param) {
                    return param.findElement(BY_PARAMETER_NAME).getAttribute("value")
                            .equals(expectedParam.getParamName());
                }
            });
            if (expectedParam.isSecureParam()) {
                assertEquals("password",
                        actualParam.findElement(BY_PARAMETER_VALUE).getAttribute("type"));
                assertEquals("Secure parameter value", actualParam.findElement(BY_PARAMETER_VALUE)
                        .getAttribute("placeholder"));
            } else {
                assertEquals(actualParam.findElement(BY_PARAMETER_VALUE).getAttribute("value"),
                        expectedParam.getParamValue());
            }
        }
    }

    private void assertCronTime(final CronTimeBuilder cronTimeBuilder) {
        final Select selectCron = new Select(cronPicker);
        Graphene.waitGui().until(new Predicate<WebDriver>() {

            @Override
            public boolean apply(WebDriver arg0) {
                return selectCron.getFirstSelectedOption().getText()
                        .equals(cronTimeBuilder.getCronTime().getCronTimeOption());
            }
        });
        switch (cronTimeBuilder.getCronTime()) {
            case CRON_EVERYWEEK:
                waitForElementVisible(selectDayInWeek);
                Select selectWeek = new Select(selectDayInWeek);
                assertEquals(selectWeek.getFirstSelectedOption().getText(),
                        cronTimeBuilder.getDayInWeek());
            case CRON_EVERYDAY:
                waitForElementVisible(selectHourInDay);
                Select selectHour = new Select(selectHourInDay);
                assertEquals(selectHour.getFirstSelectedOption().getText(),
                        cronTimeBuilder.getHourInDay());
            case CRON_EVERYHOUR:
                waitForElementVisible(selectMinuteInHour);
                Select selectMinute = new Select(selectMinuteInHour);
                assertEquals(selectMinute.getFirstSelectedOption().getText(),
                        cronTimeBuilder.getMinuteInHour());
                break;
            case AFTER:
                waitForElementVisible(selectTriggerSchedule);
                Select selectTrigger = new Select(selectTriggerSchedule);
                assertEquals(selectTrigger.getFirstSelectedOption().getText(),
                        cronTimeBuilder.getTriggerScheduleOption());
                break;
            default:
                break;
        }
    }

    private void assertOkExecutionGroupInfo(WebElement okExecutionGroup, String groupDescription) {
        assertTrue(okExecutionGroup.findElement(BY_OK_STATUS_ICON).isDisplayed());
        System.out.println("Execution description of ok execution group: "
                + okExecutionGroup.findElement(BY_EXECUTION_DESCRIPTION).getText());
        assertTrue(okExecutionGroup.findElement(BY_EXECUTION_DESCRIPTION).getText()
                .contains(groupDescription));
        assertTrue(okExecutionGroup.findElement(BY_OK_LAST_RUN).isDisplayed());
        System.out.println("Execution description of ok execution group: "
                + okExecutionGroup.findElement(BY_EXECUTION_RUNTIME).getText());
        assertTrue(okExecutionGroup.findElement(BY_EXECUTION_RUNTIME).isDisplayed());
        System.out.println("Execution description of ok execution group: "
                + okExecutionGroup.findElement(BY_EXECUTION_DATE).getText());
        assertTrue(okExecutionGroup.findElement(BY_EXECUTION_DATE).isDisplayed());
        System.out.println("Execution description of ok execution group: "
                + okExecutionGroup.findElement(BY_EXECUTION_TIMES).getText());
        assertTrue(okExecutionGroup.findElement(BY_EXECUTION_TIMES).isDisplayed());
        assertTrue(okExecutionGroup.findElement(BY_EXECUTION_LOG).isDisplayed());
        waitForElementVisible(okExecutionGroup.findElement(BY_OK_GROUP_EXPAND_BUTTON));
    }

    private void assertExecutionItemsInfo(int okGroupIndex, int okExecutionNumber,
            String groupDescription) {
        for (int i = okGroupIndex; i < okGroupIndex + okExecutionNumber + 1; i++) {
            WebElement scheduleExecutionItem = scheduleExecutionItems.get(i);
            if (i == okGroupIndex) {
                assertTrue(scheduleExecutionItem.findElement(BY_OK_STATUS_ICON).isDisplayed());
                System.out.println("Execution description at " + i + " index: "
                        + scheduleExecutionItem.findElement(BY_EXECUTION_DESCRIPTION).getText());
                assertTrue(scheduleExecutionItem.findElement(BY_EXECUTION_DESCRIPTION).getText()
                        .contains(groupDescription));
                assertFalse(scheduleExecutionItem.findElement(BY_EXECUTION_RUNTIME).isDisplayed());
                assertFalse(scheduleExecutionItem.findElement(BY_EXECUTION_DATE).isDisplayed());
                assertFalse(scheduleExecutionItem.findElement(BY_EXECUTION_TIMES).isDisplayed());
                assertFalse(scheduleExecutionItem.findElement(BY_EXECUTION_LOG).isDisplayed());
            } else {
                assertFalse(scheduleExecutionItem.findElement(BY_OK_STATUS_ICON).isDisplayed());
                System.out.println("Execution description at " + i + " index: "
                        + scheduleExecutionItem.findElement(BY_EXECUTION_DESCRIPTION).getText());
                assertTrue(scheduleExecutionItem.findElement(BY_EXECUTION_DESCRIPTION)
                        .isDisplayed());
                System.out.println("Execution description at " + i + " index: "
                        + scheduleExecutionItem.findElement(BY_EXECUTION_RUNTIME).getText());
                assertTrue(scheduleExecutionItem.findElement(BY_EXECUTION_RUNTIME).isDisplayed());
                System.out.println("Execution description at " + i + " index: "
                        + scheduleExecutionItem.findElement(BY_EXECUTION_DATE).getText());
                assertTrue(scheduleExecutionItem.findElement(BY_EXECUTION_DATE).isDisplayed());
                System.out.println("Execution description at " + i + " index: "
                        + scheduleExecutionItem.findElement(BY_EXECUTION_TIMES).getText());
                assertTrue(scheduleExecutionItem.findElement(BY_EXECUTION_TIMES).isDisplayed());
                assertTrue(scheduleExecutionItem.findElement(BY_EXECUTION_LOG).isDisplayed());
            }
        }
    }
}
