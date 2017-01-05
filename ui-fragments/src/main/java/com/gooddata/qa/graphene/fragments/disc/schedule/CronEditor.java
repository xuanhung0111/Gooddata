package com.gooddata.qa.graphene.fragments.disc.schedule;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.stream.Stream;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.enums.disc.__ScheduleCronTime;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class CronEditor extends AbstractFragment {

    @FindBy(className = "ait-schedule-cron-select-btn")
    private Select cronTypeSelect;

    @FindBy(className = "select-small")
    private Select cronDayOfWeekSelect;

    @FindBy(css = "span:not(.everyHour) .select-number")
    private Select cronHourSelect;

    @FindBy(className = "ait-schedule-cron-minutes-select-btn")
    private Select cronMinutesSelect;

    @FindBy(css = ".ait-schedule-cron-user-value input")
    private WebElement cronExpressionInput;

    @FindBy(css = ".whenSchedule select")
    private Select triggeringScheduleSelect;

    public CronEditor selectRunTime(__ScheduleCronTime cronTime) {
        waitForElementVisible(cronTypeSelect).selectByVisibleText(cronTime.toString());
        return this;
    }

    public CronEditor selectRunTimeByEveryHour(int minuteOfHour) {
        selectRunTime(__ScheduleCronTime.EVERY_HOUR).selectMinuteOfHour(minuteOfHour);
        return this;
    }

    public CronEditor selectRunTimeByEveryDay(int hourOfDay, int minuteOfHour) {
        selectRunTime(__ScheduleCronTime.EVERY_DAY).selectHourOfDay(hourOfDay)
                .selectMinuteOfHour(minuteOfHour);
        return this;
    }

    public CronEditor selectRunTimeByEveryWeek(DayOfWeek dayOfWeek, int hourOfDay, int minuteOfHour) {
        selectRunTime(__ScheduleCronTime.EVERY_WEEK);

        waitForElementVisible(cronDayOfWeekSelect)
                .selectByVisibleText(dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH));

        selectHourOfDay(hourOfDay).selectMinuteOfHour(minuteOfHour);
        return this;
    }

    public CronEditor selectRunTimeByCronExpression(String cronExpression) {
        selectRunTime(__ScheduleCronTime.CRON_EXPRESSION);

        waitForElementVisible(cronExpressionInput).clear();
        cronExpressionInput.sendKeys(cronExpression);
        return this;
    }

    public CronEditor selectRunTimeByTriggeringSchedule(String anotherScheduleId) {
        selectRunTime(__ScheduleCronTime.AFTER);
        waitForElementVisible(triggeringScheduleSelect).selectByValue(anotherScheduleId);
        return this;
    }

    public __ScheduleCronTime getSelectedCronType() {
        return Stream.of(__ScheduleCronTime.values())
                .filter(cron -> waitForElementVisible(cronTypeSelect).getFirstSelectedOption().getText()
                        .equals(cron.toString()))
                .findFirst().get();
    }

    public DayOfWeek getSelectedDayOfWeek() {
        return DayOfWeek.valueOf(
                waitForElementVisible(cronDayOfWeekSelect).getFirstSelectedOption().getText().toUpperCase());
    }

    public int getSelectedHourOfDay() {
        return parseInt(waitForElementVisible(cronHourSelect).getFirstSelectedOption().getText());
    }

    public int getSelectedMinuteOfHour() {
        return parseInt(waitForElementVisible(cronMinutesSelect).getFirstSelectedOption().getText());
    }

    public String getCronExpression() {
        return waitForElementVisible(cronExpressionInput).getAttribute("value");
    }

    public boolean isCronExpressionInputError() {
        return waitForElementVisible(cronExpressionInput).getAttribute("class").contains("has-error");
    }

    public String getTriggeringSchedule() {
        return waitForElementVisible(triggeringScheduleSelect).getFirstSelectedOption().getText();
    }

    public String getEmptyTriggeringScheduleMessage() {
        return waitForElementVisible(By.cssSelector(".whenSchedule span"), getRoot()).getText();
    }

    private CronEditor selectHourOfDay(int hourOfDay) {
        waitForElementVisible(cronHourSelect).selectByVisibleText(format("%02d", hourOfDay));
        return this;
    }

    private CronEditor selectMinuteOfHour(int minuteOfHour) {
        waitForElementVisible(cronMinutesSelect).selectByVisibleText(format("%02d", minuteOfHour));
        return this;
    }
}
