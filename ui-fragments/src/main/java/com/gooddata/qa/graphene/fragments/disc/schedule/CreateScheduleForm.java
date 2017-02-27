package com.gooddata.qa.graphene.fragments.disc.schedule;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

public class CreateScheduleForm extends AbstractScheduleFragment {

    private static final By LOCATOR = By.className("ait-new-schedule-fragment");

    @FindBy(className = "ait-new-schedule-process-select-btn")
    private Select processSelect;

    @FindBy(css = ".ait-new-schedule-fragment-name input")
    private WebElement scheduleNameInput;

    @FindBy(className = "ait-new-schedule-confirm-btn")
    private WebElement createScheduleButton;

    @FindBy(className = "ait-new-schedule-cancel-btn")
    private WebElement cancelButton;

    public static final CreateScheduleForm getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(CreateScheduleForm.class, waitForElementVisible(LOCATOR, searchContext))
                .waitForLoaded();
    }

    public CreateScheduleForm selectProcess(String processName) {
        waitForElementVisible(processSelect).selectByVisibleText(processName);
        return this;
    }

    public CreateScheduleForm enterScheduleName(String name) {
        waitForElementVisible(scheduleNameInput).clear();
        scheduleNameInput.sendKeys(name);
        return this;
    }

    public boolean isScheduleNameInputError() {
        return waitForElementVisible(scheduleNameInput).getAttribute("class").contains("has-error");
    }

    public void schedule() {
        waitForElementVisible(createScheduleButton).click();
    }

    public void cancelSchedule() {
        waitForElementVisible(cancelButton).click();
        waitForFragmentNotVisible(this);
    }

    private CreateScheduleForm waitForLoaded() {
        waitForElementNotPresent(By.className("gd-spinner"), getRoot());
        return this;
    }
}
