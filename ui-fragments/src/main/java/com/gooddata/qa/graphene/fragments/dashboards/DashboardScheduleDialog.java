/**
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.fragments.dashboards;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Joiner;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;

public class DashboardScheduleDialog extends AbstractFragment {

    private final String CSS_LIST_ITEM = ".ember-list-container .gd-list-view-item";

    @FindBy(css = ".s-btn-schedule")
    private WebElement scheduleButton;

    @FindBy(css = ".tabs-dropdown-button")
    private WebElement tabsButton;

    @FindBy(css = ".frequency-dropdown-button")
    private WebElement frequencyButton;

    @FindBy(css = ".time-dropdown-button")
    private WebElement timeButton;

    @FindBy(css = ".weekly-dropdown-button")
    private WebElement weeklyButton;

    @FindBy(css = ".ondaymulti-dropdown-button")
    private WebElement weeklyOnDayButton;

    @FindBy(css = ".dayofmonth-dropdown-button")
    private WebElement dayOfMonthButton;

    @FindBy(css = ".executeon-dropdown-button")
    private WebElement executeOnButton;

    @FindBy(css = ".weeknumber-dropdown-button")
    private WebElement weekNumberButton;

    @FindBy(css = ".onday-dropdown-button")
    private WebElement onDayButton;

    @FindBy(css = ".custom-message")
    private WebElement showCustomFormButton;

    @FindBy(css = CSS_LIST_ITEM)
    private List<WebElement> dropDownList;

    @FindBy(css = ".gd-dropdown-buttons .s-btn-apply")
    private WebElement dropDownListApply;

    @FindBy(css = ".ember-list-container .gd-list-view-item.is-selected")
    private List<WebElement> dropDownListSelected;

    @FindBy(css = ".s-email-subject input")
    private WebElement emailSubjectInput;

    @FindBy(css = ".s-email-body textarea")
    private WebElement emailMessageInput;

    @FindBy(css = ".s-email-recipients textarea")
    private WebElement emailRecipientsInput;

    @FindBy(css = ".s-schedule-info-message")
    private WebElement infoText;

    public String getCustomEmailSubject() {
        return waitForElementVisible(emailSubjectInput).getAttribute("value");
    }

    public void setCustomEmailSubject(String newValue) {
        waitForElementVisible(emailSubjectInput).click();
        emailSubjectInput.clear();
        emailSubjectInput.sendKeys(newValue);
    }

    public String getCustomEmailMessage() {
        return waitForElementVisible(emailMessageInput).getText();
    }

    public void setCustomEmailMessage(String newValue) {
        waitForElementVisible(emailMessageInput).click();
        emailMessageInput.clear();
        emailMessageInput.sendKeys(newValue);
    }

    public String getInfoText() {
        return waitForElementVisible(infoText).getText();
    }

    public void showCustomForm() {
        waitForElementVisible(showCustomFormButton).click();
        waitForElementVisible(emailSubjectInput);
        waitForElementVisible(emailMessageInput);
    }

    public void setCustomRecipients(List<String> recipients) {
        waitForElementVisible(emailRecipientsInput).click();
        emailRecipientsInput.clear();
        emailRecipientsInput.sendKeys(Joiner.on(",").join(recipients));
    }

    public void selectTabs(int[] indices) {
        selectMultiple(indices, tabsButton);
    }

    public void selectFrequency(int index) {
        selectSingle(index, frequencyButton);
    }

    public void selectTime(int index) {
        selectSingle(index, timeButton);
    }

    public void selectWeeklyEvery(int index) {
        selectSingle(index, weeklyButton);
    }

    public void selectWeeklyOnDay(int[] indices) {
        selectMultiple(indices, weeklyOnDayButton);
    }

    public void selectMonthlyOn(int index) {
        selectSingle(index, executeOnButton);
    }

    public void selectDayOfMonth(int index) {
        selectSingle(index, dayOfMonthButton);
    }

    public void selectRepeatEvery(int index) {
        selectSingle(index, weekNumberButton);
    }

    public void selectDayOfWeek(int index) {
        selectSingle(index, onDayButton);
    }

    public void schedule() {
        scheduleButton.click();
        waitForElementNotVisible(scheduleButton);
    }

    public boolean isFilterMessagePresent() {
        By filteredInfoMessage = By.cssSelector(".s-dashboard-is-filtered");
        return browser.findElements(filteredInfoMessage).size() > 0;
    }

    private void selectSingle(int index, WebElement button) {
        waitForElementVisible(button).click();
        waitForDropDownList();
        selectItems(new int[]{index}, false);
    }

    private void selectMultiple(int[] indices, WebElement button) {
        waitForElementVisible(button).click();
        waitForDropDownList();
        resetSelectedItems();
        selectItems(indices, true);
    }

    private void selectItems(final int[] indexes, boolean confirm) {
        for (int index : indexes) {
            dropDownList.get(index).click();
        }
        if (confirm) {
            dropDownListApply.click();
        }
        waitForElementNotVisible(dropDownListApply);
    }

    private void resetSelectedItems() {
        while (!dropDownListSelected.isEmpty()) {
            dropDownListSelected.get(0).click();
        }
    }

    private void waitForDropDownList() {
        waitForElementVisible(By.cssSelector(CSS_LIST_ITEM), browser);
    }
}
