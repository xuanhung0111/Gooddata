package com.gooddata.qa.graphene.fragments.disc.schedule;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.enums.disc.schedule.Executable;
import com.gooddata.qa.graphene.fragments.disc.ConfirmationDialog;
import com.gooddata.qa.graphene.fragments.disc.schedule.common.AbstractScheduleDetail;

public class ScheduleDetail extends AbstractScheduleDetail {

    @FindBy(className = "schedule-title-select")
    private Select executableSelect;

    public static final ScheduleDetail getInstance(SearchContext searchContext) {
        return getInstance(searchContext, ScheduleDetail.class);
    }

    public ScheduleDetail selectExecutable(Executable executable) {
        waitForElementVisible(executableSelect).selectByVisibleText(executable.getPath());
        return this;
    }

    public Executable getSelectedExecutable() {
        return Stream.of(Executable.values())
                .filter(e -> e.getPath().equals(
                        waitForElementVisible(executableSelect).getFirstSelectedOption().getText()))
                .findFirst()
                .get();
    }

    public String getBrokenScheduleMessage() {
        return waitForElementVisible(By.cssSelector("p.broken-schedule-info"), getRoot()).getText();
    }

    public String getEffectiveUser() {
        return waitForElementVisible(By.cssSelector(".ait-schedule-executable-section span strong"), getRoot()).getText();
    }

    public ScheduleDetail executeSchedule() {
        int executionItems = executionHistoryItems.size();

        waitForElementVisible(runButton).click();
        ConfirmationDialog.getInstance(browser).confirm();

        Function<WebDriver, Boolean> scheduleExecuted = browser -> executionHistoryItems.size() == executionItems + 1;
        Graphene.waitGui().until(scheduleExecuted);
        return this;
    }
}
