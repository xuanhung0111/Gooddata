package com.gooddata.qa.graphene.fragments.disc;

import java.util.Calendar;
import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder.CronTimeBuilder;
import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder.Parameter;
import com.gooddata.qa.graphene.enums.disc.ScheduleCronTimes;
import com.gooddata.qa.graphene.enums.disc.DeployPackages.Executables;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

public class ScheduleForm extends AbstractFragment {

    private static final String EMPTY_SCHEDULE_NAME_ERROR = "can't be blank";
    private static final String INVALID_SCHEDULE_NAME_ERROR =
            "\'${scheduleName}\' name already in use within the process. Change the name.";
    private static final String EMPTY_SCHEDULE_TRIGGER_MESSAGE =
            "Schedules cannot be scheduled in a loop";

    protected static By BY_PARAMETER_VALUE = By.cssSelector(".param-value input");
    protected static By BY_PARAMETER_NAME = By.cssSelector(".param-name input");
    protected static By BY_PARAMETER_REMOVE_ACTION = By
            .cssSelector(".param-action a[title='Remove this parameter.']");
    protected static By BY_SCHEDULE_BUTTON =
            By.xpath("//button[contains(@class, 'ait-new-schedule-confirm-btn') and text()='Schedule']");
    protected static By BY_CANCEL_BUTTON = By
            .xpath("//a[contains(@class, 'ait-new-schedule-cancel-btn') and text()='Cancel']");
    private static By BY_PARAMETER_SHOW_SECURE_VALUE = By
            .cssSelector(".param-show-secure-value input");

    private String XPATH_SELECT_SCHEDULE_TRIGGER_OPTION =
            "//select/optgroup[@label='${optionGroup}']/option[text()='${option}']";

    @FindBy(css = ".ait-new-schedule-process-select-btn")
    private WebElement selectProcessForNewSchedule;

    @FindBy(css = ".ait-new-schedule-executable-select-btn")
    private WebElement selectExecutableForNewSchedule;

    @FindBy(css = ".ait-schedule-cron-select-btn")
    protected WebElement cronPicker;

    @FindBy(css = ".cron-editor-line select.select-small")
    protected WebElement selectDayInWeek;

    @FindBy(xpath = "//span[@class='option-content everyDay everyWeek cron-editor-line']/select")
    protected WebElement selectHourInDay;

    @FindBy(
            xpath = "//span[@class='option-content everyHour everyDay everyWeek cron-editor-line']/select")
    protected WebElement selectMinuteInHour;

    @FindBy(css = ".whenSchedule select")
    protected WebElement selectTriggerSchedule;

    @FindBy(css = ".schedule-param")
    protected List<WebElement> parameters;
    @FindBy(css = ".whenSchedule")
    private WebElement triggerScheduleMessage;

    @FindBy(css = ".ait-schedule-cron-user-value input.input-text")
    private WebElement cronExpression;

    @FindBy(css = ".userCron .bubble-overlay")
    private WebElement cronExpressionErrorBubble;

    @FindBy(xpath = "//div[@class='schedule-params-actions']//a[text()='Add parameter']")
    private WebElement addParameterLink;

    @FindBy(xpath = "//div[@class='schedule-params-actions']//a[text()='Add secure parameter']")
    private WebElement addSecureParameterLink;

    @FindBy(
            xpath = "//button[contains(@class, 'ait-new-schedule-confirm-btn') and text()='Schedule']")
    private WebElement confirmScheduleButton;

    @FindBy(css = ".schedule-param:last-child")
    private WebElement lastParameter;

    @FindBy(css = ".ait-new-schedule-fragment-name input")
    private WebElement scheduleNameInput;

    @FindBy(css = ".ait-new-schedule-fragment-name .bubble-overlay")
    private WebElement scheduleNameErrorBubble;

    public void createNewSchedule(ScheduleBuilder scheduleBuilder) {
        selectProcess(scheduleBuilder.getProcessName());
        selectExecutable(scheduleBuilder.getExecutable());
        selectCron(scheduleBuilder.getCronTimeBuilder());
        addParameters(scheduleBuilder.getParameters());
        setCustomScheduleName(scheduleBuilder);
        if (scheduleBuilder.isConfirmed())
            waitForElementVisible(BY_SCHEDULE_BUTTON, browser).click();
        else
            waitForElementVisible(BY_CANCEL_BUTTON, browser).click();
    }

    public void createScheduleWithInvalidScheduleName(ScheduleBuilder scheduleBuilder,
            String validScheduleName) {
        createNewSchedule(scheduleBuilder);
        if (scheduleBuilder.getScheduleName().isEmpty()) {
            assertTrue(scheduleNameInput.getAttribute("class").contains("has-error"));
            waitForElementVisible(scheduleNameErrorBubble);
            assertEquals(scheduleNameErrorBubble.getText(), EMPTY_SCHEDULE_NAME_ERROR);
        } else {
            assertTrue(scheduleNameInput.getAttribute("class").contains("has-error"));
            waitForElementVisible(scheduleNameErrorBubble);
            assertEquals(
                    scheduleNameErrorBubble.getText(),
                    INVALID_SCHEDULE_NAME_ERROR.replace("${scheduleName}",
                            scheduleBuilder.getScheduleName()));
        }
        setCustomScheduleName(scheduleBuilder.setScheduleName(validScheduleName));
        waitForElementVisible(confirmScheduleButton).click();
    }

    public void checkScheduleWithIncorrectCron(CronTimeBuilder cronTimeBuilder) {
        selectCron(cronTimeBuilder);
        getRoot().click();
        waitForElementVisible(cronExpression).click();
        waitForElementVisible(cronExpressionErrorBubble);
        System.out.println("cron exepression: " + cronExpression.getAttribute("class"));
        System.out.println("cron bubble: " + cronExpressionErrorBubble.getText());
        assertTrue(cronExpression.getAttribute("class").contains("has-error"));
        assertTrue(cronExpressionErrorBubble.getText().equals(
                "Inserted cron format is invalid. Please verify and try again."));
    }

    public void checkNoTriggerScheduleOptions() {
        Select selectCron = new Select(cronPicker);
        waitForElementVisible(selectCron);
        waitForElementVisible(selectCron.getFirstSelectedOption());
        selectCron.selectByVisibleText(ScheduleCronTimes.AFTER.getCronTimeOption());
        waitForElementVisible(triggerScheduleMessage);
        assertEquals(triggerScheduleMessage.getText(), EMPTY_SCHEDULE_TRIGGER_MESSAGE);
        System.out.println(triggerScheduleMessage.getText());
        browser.navigate().refresh();
    }

    public void checkScheduleTriggerOptions(List<ScheduleBuilder> expectedTriggerSchedules) {
        Select selectCron = new Select(cronPicker);
        waitForElementVisible(selectCron);
        selectCron.selectByVisibleText(ScheduleCronTimes.AFTER.getCronTimeOption());
        waitForElementVisible(selectTriggerSchedule);
        Select selectTrigger = new Select(selectTriggerSchedule);
        assertEquals(selectTrigger.getOptions().size(), expectedTriggerSchedules.size());
        assertTrue(Iterables.all(expectedTriggerSchedules, new Predicate<ScheduleBuilder>() {

            @Override
            public boolean apply(ScheduleBuilder scheduleOption) {
                return selectTriggerSchedule.findElement(By
                        .xpath(XPATH_SELECT_SCHEDULE_TRIGGER_OPTION.replace("${optionGroup}",
                                scheduleOption.getProcessName()).replace("${option}",
                                scheduleOption.getExecutable().getExecutablePath()))) != null;
            }
        }));
        browser.navigate().refresh();
    }

    protected void selectCron(CronTimeBuilder cronTimeBuilder) {
        if (cronTimeBuilder.getCronTime() == null) {
            cronTimeBuilder.setDefaultCronTime();
            return;
        }
        Select selectCron = new Select(cronPicker);
        waitForElementVisible(selectCron);
        selectCron.selectByVisibleText(cronTimeBuilder.getCronTime().getCronTimeOption());
        switch (cronTimeBuilder.getCronTime()) {
            case CRON_EVERYWEEK:
                selectDayInWeek(cronTimeBuilder);
            case CRON_EVERYDAY:
                selectHourInDay(cronTimeBuilder);
            case CRON_EVERYHOUR:
                selectMinuteInHour(cronTimeBuilder);
                break;
            case CRON_EXPRESSION:
                setCronExpression(cronTimeBuilder.getCronTimeExpression());
                break;
            case AFTER:
                selectTriggerSchedule(cronTimeBuilder);
                break;
            default:
                break;
        }
    }

    protected void addParameters(List<Parameter> paramList) {
        if (paramList.isEmpty())
            return;
        int actualParamListSize = parameters.size();
        for (Parameter param : paramList) {
            if (param.isSecureParam())
                addSecureParameterLink.click();
            else
                addParameterLink.click();
            actualParamListSize++;
            assertEquals(parameters.size(), actualParamListSize);

            lastParameter.findElement(BY_PARAMETER_NAME).sendKeys(param.getParamName());
            lastParameter.findElement(BY_PARAMETER_VALUE).sendKeys(param.getParamValue());

            if (param.isSecureParam()) {
                lastParameter.findElement(BY_PARAMETER_SHOW_SECURE_VALUE).click();
                assertEquals("text",
                        lastParameter.findElement(BY_PARAMETER_VALUE).getAttribute("type"));
            }
            assertEquals(param.getParamValue(), lastParameter.findElement(BY_PARAMETER_VALUE)
                    .getAttribute("value"));
        }
    }

    private void selectProcess(String processName) {
        if (processName == null)
            return;
        waitForElementVisible(selectProcessForNewSchedule);
        Select select = new Select(selectProcessForNewSchedule);
        select.selectByVisibleText(processName);
    }

    private void selectExecutable(Executables executable) {
        if (executable == null)
            return;
        waitForElementVisible(selectExecutableForNewSchedule);
        Select select = new Select(selectExecutableForNewSchedule);
        select.selectByVisibleText(executable.getExecutablePath());
    }

    private void selectDayInWeek(CronTimeBuilder cronTimeBuilder) {
        if (cronTimeBuilder.getDayInWeek() == null) {
            cronTimeBuilder.setDefaultDayInWeek();
            return;
        }
        waitForElementVisible(selectDayInWeek);
        new Select(selectDayInWeek).selectByVisibleText(cronTimeBuilder.getDayInWeek());
    }

    private void selectHourInDay(CronTimeBuilder cronTimeBuilder) {
        if (cronTimeBuilder.getHourInDay() == null) {
            cronTimeBuilder.setDefaultHourInDay();
            return;
        }
        waitForElementVisible(selectHourInDay);
        new Select(selectHourInDay).selectByVisibleText(cronTimeBuilder.getHourInDay());
    }

    private void selectMinuteInHour(CronTimeBuilder cronTimeBuilder) {
        if (cronTimeBuilder.getMinuteInHour() == null) {
            cronTimeBuilder.setDefaultMinuteInHour();
            return;
        }
        waitForElementVisible(selectMinuteInHour);
        Select selectMinute = new Select(selectMinuteInHour);
        if (!cronTimeBuilder.getMinuteInHour().equals("${minute}")) {
            selectMinute.selectByVisibleText(cronTimeBuilder.getMinuteInHour());
            return;
        }
        int existingMinute = Calendar.getInstance().get(Calendar.MINUTE) + 2;
        existingMinute = existingMinute >= 60 ? 2 : existingMinute;
        selectMinute.selectByValue(String.valueOf(existingMinute));
        cronTimeBuilder.setMinuteInHour(String.format("%02d", existingMinute));
        cronTimeBuilder.setWaitingAutoRunInMinutes(2);
    }

    private void selectTriggerSchedule(CronTimeBuilder cronTimeBuilder) {
        if (cronTimeBuilder.getTriggerScheduleGroup() != null
                && cronTimeBuilder.getTriggerScheduleOption() != null) {
            waitForElementVisible(selectTriggerSchedule);
            selectTriggerSchedule.findElement(
                    By.xpath(XPATH_SELECT_SCHEDULE_TRIGGER_OPTION.replace("${optionGroup}",
                            cronTimeBuilder.getTriggerScheduleGroup()).replace("${option}",
                            cronTimeBuilder.getTriggerScheduleOption()))).click();
        }
    }

    private void setCronExpression(String cronTimeExpression) {
        if (cronTimeExpression == null)
            return;
        Graphene.waitGui().until(new Predicate<WebDriver>() {

            @Override
            public boolean apply(WebDriver arg0) {
                return !cronExpression.getAttribute("value").isEmpty();
            }
        });
        System.out.println("Cron expression value: " + cronExpression.getAttribute("value"));
        waitForElementVisible(cronExpression).clear();
        Graphene.waitGui().until(new Predicate<WebDriver>() {

            @Override
            public boolean apply(WebDriver arg0) {
                return cronExpression.getAttribute("value").isEmpty();
            }
        });
        System.out.println("Cron expression value after clearing: "
                + cronExpression.getAttribute("value"));
        cronExpression.sendKeys(cronTimeExpression);
        assertEquals(cronTimeExpression, cronExpression.getAttribute("value"));
        System.out.println("Cron expression is set to... " + cronExpression.getAttribute("value"));
    }

    private void setCustomScheduleName(ScheduleBuilder scheduleBuilder) {
        if (scheduleBuilder.getScheduleName() == null)
            return;

        if (scheduleBuilder.getScheduleName().equals(scheduleNameInput.getAttribute("value")))
            return;

        waitForElementVisible(scheduleNameInput).clear();
        Graphene.waitGui().until(new Predicate<WebDriver>() {

            @Override
            public boolean apply(WebDriver arg0) {
                return scheduleNameInput.getText().isEmpty();
            }
        });
        scheduleNameInput.sendKeys(scheduleBuilder.getScheduleName());
    }
}
