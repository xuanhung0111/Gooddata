package com.gooddata.qa.graphene.fragments.disc;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.BY_BUBBLE_CONTENT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder.CronTimeBuilder;
import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder.Parameter;
import com.gooddata.qa.graphene.enums.disc.DeployPackages.Executables;
import com.google.common.base.Predicate;
import com.google.common.base.Stopwatch;

public class ScheduleDetail extends ScheduleForm {

    private static final String OVERLAP_DATASET_MESSAGE = "One or more of the selected datasets "
            + "is already included in an existing schedule. "
            + "If multiple schedules that load same dataset run concurrently, "
            + "all schedules except the first will fail.";
    private static final int MAX_EXECUTION_HISTORY_LOADING_TIME = 10; // In minutes
    private static final int MAX_SCHEDULE_RUN_TIME = 15; // In minutes
    private static final int MAX_DELAY_TIME_WAITING_AUTO_RUN = 2; // In minutes

    public enum Confirmation {
        SAVE_CHANGES,
        CANCEL_CHANGES;
    }

    private static final String BROKEN_SCHEDULE_MESSAGE =
            "The graph %s doesn't exist because it has been changed (renamed or deleted). "
                    + "It isn't possible to execute this schedule because there is no graph to execute.";
    private static final String SELECT_SYNCHRONIZE_ALL_DATASETS_TEXT = "All datasets in the project";
    private static final String SELECT_SYNCHRONIZE_SELECTED_DATASETS_TEXT = "Only selected";
    private static final String UPLOAD_DATA_HELP_TEXT = "Data will be uploaded using full load.";

    private static final By BY_EXECUTION_STATUS = By.cssSelector(".execution-status");
    private static final By BY_EXECUTION_DESCRIPTION = By.cssSelector(".ait-execution-history-item-description");
    private static final By BY_EXECUTION_ERROR_DESCRIPTION = By
            .cssSelector(".execution-history-error");
    private static final By BY_EXECUTION_LOG = By.cssSelector(".ait-execution-history-item-log");
    private static final By BY_EXECUTION_RUNTIME = By.cssSelector(".execution-runtime");
    private static final By BY_EXECUTION_DATE = By.cssSelector(".execution-date");
    private static final By BY_EXECUTION_TIME = By.cssSelector(".execution-times");
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
    private static final By BY_EXECUTION_TOOLTIP = By.cssSelector(".execution-tooltip");

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

    @FindBy(className = "ait-schedule-stop-btn")
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
    private Select brokenScheduleExecutable;

    @FindBy(css = ".ait-schedule-executable-edit-buttons .button-positive")
    private WebElement brokenScheduleSaveChangeButton;

    @FindBy(css = ".info-section")
    private WebElement failedScheduleInfoSection;

    @FindBy(css = ".ait-schedule-disabled .message p:nth-child(1)")
    private WebElement autoDisableScheduleMessage;

    @FindBy(css = ".ait-schedule-disabled .message p:nth-child(2)")
    private WebElement autoDisableScheduleMoreInfo;

    @FindBy(css = ".ait-schedule-executable-select-btn")
    private Select selectExecutable;

    @FindBy(css = ".ait-execution-history-empty")
    private WebElement executionHistoryEmptyState;

    @FindBy(css = ".error-trigger-message")
    private WebElement triggerScheduleMissingMessage;

    @FindBy(css = ".ait-execution-history-item-description")
    private WebElement executionItemDescription;

    @FindBy(css = ".mouseoverTrigger.inlineBubbleHelp")
    private WebElement inlineBubbleHelp;

    @FindBy(css = ".searchfield-input")
    private WebElement searchDatasetInput;

    @FindBy(css = ".gd-list-view-item span:not(.ember-view)")
    private List<WebElement> datasets;

    @FindBy(css = ".ait-schedule-title-section .icon-edit")
    private WebElement editScheduleNamePencilIcon;

    @FindBy(css = ".ait-schedule-timeline-section .execution")
    private WebElement timelineExecution;

    @FindBy(xpath = "//.[@class='schedule-title']//span[2]")
    private WebElement effectiveUser;

    public String getScheduleTitle() {
        return waitForElementVisible(scheduleTitle).getText();
    }

    public String getSelectedExecutablePath() {
        waitForElementVisible(selectExecutable);
        return selectExecutable.getFirstSelectedOption().getText();
    }

    public WebElement getExecutionHistoryEmptyState() {
        return waitForElementVisible(executionHistoryEmptyState);
    }

    public void clickOnCloseScheduleButton() {
        waitForElementVisible(closeButton).click();
        waitForFragmentNotVisible(this);
    }

    public void tryToWaitForAutoRun(CronTimeBuilder cronTimebuilder) {
        final int executionNumber = scheduleExecutionItems.size();
        try {
            Predicate<WebDriver> autoExecutionFinished = webDriver -> {
                System.out.println("Waiting for auto execution...");
                return scheduleExecutionItems.size() > executionNumber;
            };
            Graphene.waitGui()
                    .withTimeout(cronTimebuilder.getWaitingAutoRunInMinutes() + MAX_DELAY_TIME_WAITING_AUTO_RUN,
                            TimeUnit.MINUTES).pollingEvery(5, TimeUnit.SECONDS)
                    .withMessage("Schedule doesn't run automatically!").until(autoExecutionFinished);
        } catch (TimeoutException ex) {
            System.out.println("Schedule doesn't run automatically");
        }
    }

    public boolean waitForAutoRunSchedule(CronTimeBuilder cronTimebuilder) {
        return waitForAutoRun(cronTimebuilder.getWaitingAutoRunInMinutes());
    }

    public boolean waitForRetrySchedule(ScheduleBuilder scheduleBuilder) {
        return waitForAutoRun(scheduleBuilder.getRetryDelay());
    }

    public void waitForExecutionFinish() {
        Predicate<WebDriver> runningScheduleFinished = webDriver -> {
            System.out.println("Waiting for running schedule...");
            return getRoot().findElement(BY_RUN_STOP_BUTTON).getText().equals("Run");  
        };
        
        Graphene.waitGui().withTimeout(MAX_SCHEDULE_RUN_TIME, TimeUnit.MINUTES).pollingEvery(5, TimeUnit.SECONDS)
                .withMessage("Schedule execution is not finished!").until(runningScheduleFinished);
        getLastExecution();
    }

    public void clickOnAddRetryButton() {
        waitForElementVisible(addRetryDelay).click();
    }

    public boolean isLastExecutionManualIconDisplay() {
        return waitForElementVisible(BY_MANUAL_ICON, getLastExecution()).isDisplayed();
    }

    public String getRescheduleFormMessage() {
        return waitForElementVisible(rescheduleForm).getText();
    }

    public String getRescheduleTime() {
        return waitForElementPresent(retryDelayInput).getAttribute("value");
    }

    public void cancelAddRetryDelay() {
        waitForElementVisible(cancelAddRetryDelayButton).click();
    }

    public void saveRetryDelay() {
        waitForElementVisible(saveRetryDelayButton).click();
    }

    public boolean isRetryErrorDisplayed() {
        return waitForElementVisible(retryDelayInput).getAttribute("class").contains("has-error");
    }

    public void addValidRetry(String retryDelay, Confirmation saveChange) {
        addRetry(retryDelay);
        if (saveChange == Confirmation.SAVE_CHANGES) {
            waitForElementVisible(saveRetryDelayButton).click();
            waitForElementNotPresent(saveRetryDelayButton);
        } else {
            waitForElementVisible(cancelAddRetryDelayButton).click();
            waitForElementNotPresent(retryDelayInput);
            waitForElementVisible(addRetryDelay);
        }
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
        waitForElementVisible(manualRunButton).sendKeys(Keys.ENTER);
        //the Run dialog is separated to ScheduleDetail. So search it from root browser.
        waitForElementVisible(By.cssSelector(".dialog-main.ait-schedule-run"), browser);
        waitForElementVisible(By.cssSelector(".ait-schedule-run-confirm-btn"), browser).click();
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

    public boolean isScheduleDisabledInUI() {
        return isElementVisible(enableScheduleButton);
    }

    public void changeExecutable(Executables newExecutable, Confirmation saveChange) {
        waitForElementVisible(selectExecutable);
        Predicate<WebDriver> selectExecutableChanged = 
                browser -> !selectExecutable.getFirstSelectedOption().equals(selectExecutable.getOptions().get(0));
        Graphene.waitGui().until(selectExecutableChanged);
        selectExecutable.selectByVisibleText(newExecutable.getExecutablePath());
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
        WebElement parameter = parameters.stream()
                .filter(param -> param.findElement(BY_PARAMETER_NAME).getAttribute("value")
                        .equals(existingParam.getParamName()))
                .findFirst()
                .get();
        WebElement paramName = parameter.findElement(BY_PARAMETER_NAME);
        paramName.clear();
        paramName.sendKeys(editedParam.getParamName());
        WebElement paramValue = parameter.findElement(BY_PARAMETER_VALUE);
        paramValue.clear();
        paramValue.sendKeys(editedParam.getParamValue());
    }

    public void removeParameter(List<Parameter> paramsToRemove, Confirmation saveChange) {
        paramsToRemove.stream().forEach(paramToRemove -> {
                parameters.stream()
                .filter(param -> param.findElement(BY_PARAMETER_NAME).getAttribute("value").
                        equals(paramToRemove.getParamName()))
                .map(e -> e.findElement(BY_PARAMETER_REMOVE_ACTION))
                .findFirst()
                .get()
                .click();
            });
        confirmParamsChange(saveChange);
    }

    public boolean isCorrectMessageOnBrokenScheduleDetail(String scheduleName) {
        waitForElementVisible(getRoot());
        waitForElementVisible(brokenScheduleMessage);
        System.out.println("Check broken schedule detail page...");
        String expectedMessage = String.format(BROKEN_SCHEDULE_MESSAGE, scheduleName);
        return expectedMessage.equals(brokenScheduleMessage.getText());
    }

    public void fixBrokenSchedule(Executables newExecutable) {
        brokenScheduleExecutable.selectByVisibleText(newExecutable.getExecutablePath());
        waitForElementVisible(brokenScheduleSaveChangeButton).click();
        waitForElementNotPresent(brokenScheduleSaveChangeButton);
        clickOnCloseScheduleButton();
    }

    public String getRepeatedFailureInfo() {
        return waitForElementVisible(failedScheduleInfoSection).getText();
    }

    public String getAutoDisabledScheduleMessage() {
        return waitForElementVisible(autoDisableScheduleMessage).getText();
    }

    public String getAutoDisabledScheduleMoreInfo() {
        return waitForElementVisible(autoDisableScheduleMoreInfo).getText();
    }

    public boolean isInRunningState() {
        waitForCollectionIsNotEmpty(scheduleExecutionItems);
        try {
            Predicate<WebDriver> executionStateRunning = browser -> {
                System.out.println("Wait for execution state changed to RUNNING!");
                return "RUNNING".equals(waitForElementVisible(executionItemDescription).getText());
            };
            Graphene.waitGui().withTimeout(MAX_SCHEDULE_RUN_TIME, TimeUnit.MINUTES)
                    .pollingEvery(2, TimeUnit.SECONDS).until(executionStateRunning);
            Predicate<WebDriver> executionStateFinished = 
                    browser -> !getLastExecution().findElement(BY_EXECUTION_RUNTIME).getText().isEmpty();
            Graphene.waitGui().until(executionStateFinished);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public boolean isInScheduledState() {
        waitForCollectionIsNotEmpty(scheduleExecutionItems);
        try {
            Predicate<WebDriver> executionStateScheduled = browser -> {
                System.out.println("Wait for execution state changed to SCHEDULED!");
                return "SCHEDULED".equals(waitForElementVisible(executionItemDescription).getText());
            };
            Graphene.waitGui().withTimeout(MAX_SCHEDULE_RUN_TIME, TimeUnit.MINUTES)
                    .pollingEvery(2, TimeUnit.SECONDS).until(executionStateScheduled);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public String getLastExecutionDate() {
        return getLastExecution().findElement(BY_EXECUTION_DATE).getText();
    }

    public String getLastExecutionTime() {
        return getLastExecution().findElement(BY_EXECUTION_TIME).getText();
    }

    public String getLastExecutionRuntime() {
        return getLastExecution().findElement(BY_EXECUTION_RUNTIME).getText();
    }

    public String getLastExecutionDescription() {
        return getLastExecution().findElement(BY_EXECUTION_DESCRIPTION).getText();
    }

    public String getExecutionErrorDescription() {
        return getLastExecution().findElement(BY_EXECUTION_ERROR_DESCRIPTION).getText();
    }

    public boolean isStarted() {
        try {
            waitForElementVisible(manualStopButton, 10);
            return true;
        } catch (TimeoutException | NoSuchElementException e) {
            return false;
        } 
    }

    public int getExecutionItemsNumber() {
        return scheduleExecutionItems.size();
    }

    public List<WebElement> getExecutionItems() {
        return scheduleExecutionItems;
    }

    public WebElement getEnableButton() {
        return waitForElementVisible(enableScheduleButton);
    }

    public WebElement getOkGroupExpandButton(WebElement execution) {
        return waitForElementVisible(BY_OK_GROUP_EXPAND_BUTTON, execution);
    }

    public String getLastExecutionLogLink() {
        return getLastExecution().findElement(BY_EXECUTION_LOG).getAttribute("href");
    }

    public String getLastExecutionLogTitle() {
        return getLastExecution().findElement(BY_EXECUTION_LOG).getAttribute("title").trim();
    }

    public boolean isErrorIconVisible() {
        return waitForElementVisible(BY_ERROR_STATUS_ICON, getLastExecution().findElement(BY_EXECUTION_STATUS))
                .isDisplayed();
    }

    public boolean isSchedulerErrorIconVisible() {
        return waitForElementVisible(BY_SCHEDULER_ERROR_STATUS_ICON,
                getLastExecution().findElement(BY_EXECUTION_STATUS)).isDisplayed();
    }

    private WebElement getLastExecution() {
        if (scheduleExecutionItems.isEmpty())
            waitForExecutionHistoryLoading();
        return waitForElementVisible(lastExecutionItem);
    }

    public void changeValidScheduleName(String newScheduleName, Confirmation saveChange) {
        changeScheduleName(newScheduleName);
        if (saveChange == Confirmation.SAVE_CHANGES)
            waitForElementVisible(saveScheduleTitleButton).click();
        else
            waitForElementVisible(cancelChangeScheduleTitleButton).click();
    }

    public void editScheduleNameByPencilIcon(String newScheduleName, Confirmation saveChange) {
        waitForElementVisible(editScheduleNamePencilIcon).click();
        waitForElementVisible(scheduleTitleInput).sendKeys(newScheduleName);
        if (saveChange == Confirmation.SAVE_CHANGES)
            waitForElementVisible(saveScheduleTitleButton).click();
        else
            waitForElementVisible(cancelChangeScheduleTitleButton).click();
    }

    public boolean isDatasetChecked(ScheduleBuilder scheduleBuilder) {
        waitForElementVisible(openDatasetPickerButton).click();
        return isSelectedDatasetsChecked(scheduleBuilder.getDatasetsToSynchronize());
    }

    public boolean isAllDatasetChecked(ScheduleBuilder scheduleBuilder) {
        waitForElementVisible(selectAllCustomDatasetsButton).click();
        return isSelectedDatasetsChecked(scheduleBuilder.getAllDatasets());
    }

    public boolean isNoneDatasetChecked() {
        waitForElementVisible(selectNoneCustomDatasetsButton).click();
        return isSelectedDatasetsChecked(Collections.<String>emptyList());
    }

    public void clickOnDatasetSelectButton() {
        waitForElementVisible(openDatasetPickerButton).click();
    }

    public void clickOnNoneDatasetButton() {
        waitForElementVisible(selectNoneCustomDatasetsButton).click();
    }

    public void clickOnAllDatasetsButton() {
        waitForElementVisible(selectAllCustomDatasetsButton).click();
    }

    public void cancelSelectSynchronizeDatasets() {
        waitForElementVisible(datasetDialog).findElement(By.className("button-secondary")).click();
    }

    public void openDatasetDialog() {
        waitForElementVisible(openDatasetPickerButton).click();
        waitForElementVisible(datasetDialog);
    }

    public void searchDatasets(String searchKey) {
        waitForElementVisible(searchDatasetInput).clear();
        searchDatasetInput.sendKeys(searchKey);
    }

    public List<String> getSearchedDatasets() {
        return getElementTexts(datasets);
    }

    public int getDatasetListCount() {
        return datasets.size();
    }

    public String getTriggerScheduleMissingMessage() {
        waitForElementPresent(saveChangedCronTimeButton);
        waitForElementNotPresent(cancelChangedCronTimeButton);
        return waitForElementVisible(triggerScheduleMissingMessage).getText();
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

    public String getTimelineExecutionTooltip() {
        new Actions(browser).moveToElement(timelineExecution).perform();
        return waitForElementVisible(BY_EXECUTION_TOOLTIP, browser).getText();
    }

    public boolean isCorrectSuccessfulExecutionTooltip() {
        String excutionTooltip = getTimelineExecutionTooltip();
        return excutionTooltip.contains("Successful execution") &&
                excutionTooltip.contains(String.format("Runtime %s", getLastExecutionRuntime()));
    }

    public boolean isCorrectFailedExecutionTooltip(Executables executable) {
        String excutionTooltip = getTimelineExecutionTooltip();
        return excutionTooltip.contains("Failed execution") && 
                excutionTooltip.contains(executable.getErrorMessage());
    }

    public String getEffectiveUser() {
        return waitForElementVisible(effectiveUser).getText();
    }

    public void addRetry(String retryDelay) {
        waitForElementVisible(addRetryDelay).click();
        waitForElementVisible(rescheduleForm);
        waitForElementVisible(retryDelayInput).clear();
        retryDelayInput.sendKeys(retryDelay);
    }

    public void changeScheduleName(String newScheduleName) {
        waitForElementVisible(scheduleTitle).click();
        waitForElementVisible(scheduleTitleInput).clear();
        scheduleTitleInput.sendKeys(newScheduleName);
    }

    public void saveEditedScheduleTitle() {
        waitForElementVisible(saveScheduleTitleButton).sendKeys(Keys.ENTER);
        waitForElementVisible(scheduleTitleInput).click();
    }

    public boolean isErrorScheduleTitle() {
        return waitForElementVisible(scheduleTitleInput).getAttribute("class").contains("has-error");
    }

    public boolean isSelectedDatasetsChecked(List<String> datasetsToSynchronize) {
        List<WebElement> items =
                waitForElementVisible(datasetDialog).findElements(By.className("gd-list-view-item"));
        for (WebElement item : items) {
            if (datasetsToSynchronize.contains(item.getText()) ^ item.getAttribute("class").contains("is-selected")) {
                return false;
            }
        }

        return true;
    }

    public boolean isCorrectDataloadOption() {
        if (!SELECT_SYNCHRONIZE_ALL_DATASETS_TEXT.equals(waitForElementVisible(selectSynchronizeAllDatasets)
                .findElement(BY_PARENT).getText().trim()))
            return false;
        return waitForElementVisible(selectSynchronizeSelectedDatasets).findElement(BY_PARENT).getText().trim()
                .contains(SELECT_SYNCHRONIZE_SELECTED_DATASETS_TEXT);
    }

    public boolean isCorrectInlineBubbleHelp() {
        Actions action = new Actions(browser);
        action.moveToElement(scheduleTitle).perform();
        action.moveToElement(inlineBubbleHelp).perform();
        return UPLOAD_DATA_HELP_TEXT.equals(waitForElementVisible(BY_BUBBLE_CONTENT, browser).getText().trim());
    }

    public boolean isCorrectAllDatasetSelected() {
        if (!waitForElementVisible(selectSynchronizeAllDatasets).isSelected())
            return false;
        return !waitForElementVisible(selectSynchronizeSelectedDatasets).isSelected();
    }

    public boolean isCorrectDatasetsSelected(ScheduleBuilder scheduleBuilder) {
        if (waitForElementVisible(selectSynchronizeAllDatasets).isSelected())
            return false;
        if (!waitForElementVisible(selectSynchronizeSelectedDatasets).isSelected())
            return false;
        String expectedButtonText =
                String.format("%d of %d datasets", scheduleBuilder.getDatasetsToSynchronize().size(),
                        scheduleBuilder.getAllDatasets().size());
        return waitForElementVisible(openDatasetPickerButton).getText().contains(expectedButtonText);
    }

    public boolean isCorrectDatasetOverlapMessage() {
        return OVERLAP_DATASET_MESSAGE.equals(dataloadDatasetsMessages.getText());
    }

    public void saveSelectedSynchronizeDatasets() {
        waitForElementVisible(datasetDialog).findElement(By.className("button-positive")).click();
    }

    /**
     * check the status of save button in selected synchronized dataset drop-down menu
     */
    public boolean isSelectedSynchronziedSavedButtonEnabled() {
        return waitForElementVisible(datasetDialog).findElement(By.className("button-positive")).isEnabled();
    }

    public boolean isCorrectScheduleParameters(List<Parameter> expectedParams) {
        for (final Parameter expectedParam : expectedParams) {
            WebElement actualParam = parameters.stream()
                    .filter(param -> param.findElement(BY_PARAMETER_NAME).getAttribute("value").equals(expectedParam.getParamName()))
                    .findFirst()
                    .get();
            if (expectedParam.isSecureParam()) {
                if (!"password".equals(actualParam.findElement(BY_PARAMETER_VALUE).getAttribute("type")))
                    return false;
                return "Secure parameter value".equals(actualParam.findElement(BY_PARAMETER_VALUE).getAttribute(
                        "placeholder"));
            } else {
                return expectedParam.getParamValue().equals(
                        actualParam.findElement(BY_PARAMETER_VALUE).getAttribute("value"));
            }
        }
        return true;
    }

    public boolean isCorrectCronTime(final CronTimeBuilder cronTimeBuilder) {
        Predicate<WebDriver> cronTimeDisplayed = webDriver -> cronPicker.getFirstSelectedOption().getText()
                .equals(cronTimeBuilder.getCronTime().getCronTimeOption());
        Graphene.waitGui().until(cronTimeDisplayed);
        switch (cronTimeBuilder.getCronTime()) {
            case CRON_EVERYWEEK:
                waitForElementVisible(selectDayInWeek);
                if (!cronTimeBuilder.getDayInWeek().equals(selectDayInWeek.getFirstSelectedOption().getText()))
                    return false;
            case CRON_EVERYDAY:
                waitForElementVisible(selectHourInDay);
                if (!cronTimeBuilder.getHourInDay().equals(selectHourInDay.getFirstSelectedOption().getText()))
                    return false;
            case CRON_EVERYHOUR:
                waitForElementVisible(selectMinuteInHour);
                return cronTimeBuilder.getMinuteInHour().equals(selectMinuteInHour.getFirstSelectedOption().getText());
            case AFTER:
                waitForElementVisible(selectTriggerSchedule);
                Select selectTrigger = new Select(selectTriggerSchedule);
                return cronTimeBuilder.getTriggerScheduleOption().equals(
                        selectTrigger.getFirstSelectedOption().getText());
            default:
                return true;
        }
    }

    public WebElement getLastExecutionOfOkGroup(WebElement execution) {
        return waitForElementVisible(BY_OK_LAST_RUN, execution);
    }

    public WebElement getExecutionItem(int index) {
        return waitForElementVisible(scheduleExecutionItems.get(index));
    }

    public boolean isLastExecutionRunTimeDisplayed() {
        return waitForElementVisible(BY_EXECUTION_RUNTIME, getLastExecution()).isDisplayed();
    }

    public WebElement getExecutionRunTime(WebElement execution) {
        return execution.findElement(BY_EXECUTION_RUNTIME);
    }

    public boolean isExecutionRuntimePresent(WebElement execution) {
        return execution.findElement(BY_EXECUTION_RUNTIME).isDisplayed();
    }

    public boolean isLastExecutionDateDisplayed() {
        return waitForElementVisible(BY_EXECUTION_DATE, getLastExecution()).isDisplayed();
    }

    public WebElement getExecutionDate(WebElement execution) {
        return execution.findElement(BY_EXECUTION_DATE);
    }

    public boolean isExecutionDatePresent(WebElement execution) {
        return execution.findElement(BY_EXECUTION_DATE).isDisplayed();
    }

    public boolean isLastExecutionTimeDisplayed() {
        return waitForElementVisible(BY_EXECUTION_TIME, getLastExecution()).isDisplayed();
    }

    public WebElement getExecutionTime(WebElement execution) {
        return execution.findElement(BY_EXECUTION_TIME);
    }

    public boolean isExecutionTimePresent(WebElement execution) {
        return execution.findElement(BY_EXECUTION_TIME).isDisplayed();
    }

    public boolean isLastExecutionLogDisplayed() {
        return waitForElementVisible(BY_EXECUTION_LOG, getLastExecution()).isDisplayed();
    }

    public WebElement getExecutionLog(WebElement execution) {
        return execution.findElement(BY_EXECUTION_LOG);
    }

    public boolean isExecutionLogPresent(WebElement execution) {
        return execution.findElement(BY_EXECUTION_LOG).isDisplayed();
    }

    public boolean isLastExecutionDescriptionDisplayed() {
        return waitForElementVisible(BY_EXECUTION_DESCRIPTION, getLastExecution()).isDisplayed();
    }

    public WebElement getExecutionDescription(WebElement execution) {
        return execution.findElement(BY_EXECUTION_DESCRIPTION);
    }

    public boolean isOkStatusDisplayedForLastExecution() {
        return waitForElementVisible(BY_OK_STATUS_ICON, getLastExecution().findElement(BY_EXECUTION_STATUS))
                .isDisplayed();
    }

    public boolean isOkExecutionIconDisplayed(WebElement execution) {
        if (!isElementPresent(BY_OK_STATUS_ICON, execution.findElement(BY_EXECUTION_STATUS))) {
            return false;
        }

        return getOkExecutionIcon(execution).isDisplayed();
    }

    public boolean isLastExecutionLogLinkEnabled() {
        return waitForElementVisible(BY_EXECUTION_LOG, getLastExecution()).isEnabled();
    }

    private WebElement getOkExecutionIcon(WebElement execution) {
        return waitForElementPresent(BY_OK_STATUS_ICON, execution.findElement(BY_EXECUTION_STATUS));
    }

    private void waitForExecutionHistoryLoading() {
        Predicate<WebDriver> executionHistoryLoaded = webDriver -> {
            if (!scheduleExecutionItems.isEmpty())
                return true;
            System.out.println("Execution history is loading!");
            browser.navigate().refresh();
            return false;
        };
        Graphene.waitGui().withTimeout(MAX_EXECUTION_HISTORY_LOADING_TIME, TimeUnit.MINUTES)
                .pollingEvery(1, TimeUnit.MINUTES).until(executionHistoryLoaded);
    }

    private boolean waitForAutoRun(int waitingTimeInMinutes) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        final int executionNumberBeforeAutoRun = scheduleExecutionItems.size();
        Predicate<WebDriver> autoExecutionFinished = webDriver -> {
            System.out.println("Waiting for auto execution...");
            return scheduleExecutionItems.size() > executionNumberBeforeAutoRun;
        };
        Graphene.waitGui().withTimeout(waitingTimeInMinutes + MAX_DELAY_TIME_WAITING_AUTO_RUN, TimeUnit.MINUTES)
                .pollingEvery(5, TimeUnit.SECONDS).withMessage("Schedule doesn't run automatically!")
                .until(autoExecutionFinished);
        stopwatch.stop();
        if (scheduleExecutionItems.size() != executionNumberBeforeAutoRun + 1)
            return false;
        long delayTime = stopwatch.elapsed(TimeUnit.MINUTES) - waitingTimeInMinutes;
        System.out.println("Delay time: " + delayTime);
        return Math.abs(delayTime) <= 2;
    }
}
