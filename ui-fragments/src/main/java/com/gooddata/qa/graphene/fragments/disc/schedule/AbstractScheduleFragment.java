package com.gooddata.qa.graphene.fragments.disc.schedule;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.google.common.collect.Iterables.getLast;

import java.time.DayOfWeek;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.enums.disc.__Executable;
import com.gooddata.qa.graphene.enums.disc.__ScheduleCronTime;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class AbstractScheduleFragment extends AbstractFragment {

    @FindBy(css = "[class*='schedule-executable-select-btn']")
    private Select executableSelect;

    @FindBy(className = "cron-editor-main")
    private CronEditor cronEditor;

    @FindBy(css = ".schedule-params-actions a:first-child")
    private WebElement addParameterLink;

    @FindBy(css = ".schedule-params-actions a:last-child")
    private WebElement addSecureParameterLink;

    @FindBy(className = "schedule-param")
    private Collection<ScheduleParameter> scheduleParams;

    public AbstractScheduleFragment selectExecutable(__Executable executable) {
        waitForElementVisible(executableSelect).selectByVisibleText(executable.getPath());
        return this;
    }

    public __Executable getSelectedExecutable() {
        return Stream.of(__Executable.values())
                .filter(e -> e.getPath().equals(
                        waitForElementVisible(executableSelect).getFirstSelectedOption().getText()))
                .findFirst()
                .get();
    }

    public AbstractScheduleFragment selectRunTime(__ScheduleCronTime cronTime) {
        getCronEditor().selectRunTime(cronTime);
        return this;
    }

    public AbstractScheduleFragment selectRunTimeByEveryHour(int minuteOfHour) {
        getCronEditor().selectRunTimeByEveryHour(minuteOfHour);
        return this;
    }

    public AbstractScheduleFragment selectRunTimeByEveryDay(int hourOfDay, int minuteOfHour) {
        getCronEditor().selectRunTimeByEveryDay(hourOfDay, minuteOfHour);
        return this;
    }

    public AbstractScheduleFragment selectRunTimeByEveryWeek(DayOfWeek dayOfWeek, int hourOfDay, int minuteOfHour) {
        getCronEditor().selectRunTimeByEveryWeek(dayOfWeek, hourOfDay, minuteOfHour);
        return this;
    }

    public AbstractScheduleFragment selectRunTimeByCronExpression(String cronExpression) {
        getCronEditor().selectRunTimeByCronExpression(cronExpression);
        return this;
    }

    public AbstractScheduleFragment selectRunTimeByTriggeringSchedule(String scheduleId) {
        getCronEditor().selectRunTimeByTriggeringSchedule(scheduleId);
        return this;
    }

    public CronEditor getCronEditor() {
        return waitForFragmentVisible(cronEditor);
    }

    public AbstractScheduleFragment addParameter(String name, String value) {
        waitForElementVisible(addParameterLink).click();
        getLast(scheduleParams).editNameValuePair(name, value);
        return this;
    }

    public AbstractScheduleFragment addParameters(Map<String, String> parameters) {
        for (String param : parameters.keySet()) {
            addParameter(param, parameters.get(param));
        }
        return this;
    }

    public AbstractScheduleFragment addSecureParameter(String name, String value) {
        waitForElementVisible(addSecureParameterLink).click();
        getLast(scheduleParams).editNameValuePair(name, value);
        return this;
    }

    public AbstractScheduleFragment addSecureParameters(Map<String, String> secureParameters) {
        for (String secureParam : secureParameters.keySet()) {
            addSecureParameter(secureParam, secureParameters.get(secureParam));
        }
        return this;
    }

    public Map<String, String> getAllParametersInfo() {
        return getParametersByPredicate(param -> !param.isSecure());
    }

    public Map<String, String> getAllSecureParametersInfo() {
        return getParametersByPredicate(ScheduleParameter::isSecure);
    }

    public AbstractScheduleFragment deleteParameter(String parameter) {
        getParameter(parameter).delete();
        return this;
    }

    public ScheduleParameter getParameter(String parameter) {
        return findParameter(parameter).get();
    }

    public boolean hasParameter(String parameter) {
        return findParameter(parameter).isPresent();
    }

    private Optional<ScheduleParameter> findParameter(String parameter) {
        return scheduleParams.stream().filter(p -> parameter.equals(p.getName())).findFirst();
    }

    private Map<String, String> getParametersByPredicate(Predicate<ScheduleParameter> predicate) {
        Map<String, String> returnParams = new HashMap<>();

        scheduleParams.stream()
                .filter(predicate)
                .forEach(param -> returnParams.put(param.getName(), param.getValue()));

        return returnParams;
    }
}
