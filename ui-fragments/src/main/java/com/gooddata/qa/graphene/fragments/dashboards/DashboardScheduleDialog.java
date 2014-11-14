/**
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.fragments.dashboards;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class DashboardScheduleDialog extends AbstractFragment {

    private final String CSS_LIST_ITEM = ".ember-list-container .gd-list-view-item";

    @FindBy(css = ".s-btn-schedule")
    private WebElement scheduleButton;

    @FindBy(css = ".tabs-dropdown-button")
    private WebElement tabsButton;

    @FindBy(css = ".time-dropdown-button")
    private WebElement timeButton;

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

    @FindBy(css = ".s-schedule-info-message")
    private WebElement infoText;

    public String getCustomEmailSubject() {
        return waitForElementVisible(emailSubjectInput).getAttribute("value");
    }

    public String getCustomEmailMessage() {
        return waitForElementVisible(emailMessageInput).getText();
    }

    public String getInfoText() {
        return waitForElementVisible(infoText).getText();
    }

    public void setCustomEmailSubject(String newValue) {
        waitForElementVisible(emailSubjectInput).click();
        emailSubjectInput.clear();
        emailSubjectInput.sendKeys(newValue);
    }

    public void setCustomEmailMessage(String newValue) {
        waitForElementVisible(emailMessageInput).click();
        emailMessageInput.clear();
        emailMessageInput.sendKeys(newValue);
    }

    public void showCustomForm() {
        waitForElementVisible(showCustomFormButton).click();
        waitForElementVisible(emailSubjectInput);
        waitForElementVisible(emailMessageInput);
    }

    public void selectTabs(int[] indexes) {
        waitForElementVisible(tabsButton).click();
        waitForDropDownList();
        resetSelectedItems();
        selectItems(indexes, true);
    }

    public void selectTime(int index) {
        waitForElementVisible(timeButton).click();
        waitForDropDownList();
        selectItems(new int[] {index}, false);
    }

    public void schedule() {
        scheduleButton.click();
        waitForElementNotVisible(scheduleButton);
    }

    private void selectItems(final int[] indexes, boolean confirm) {
        for (int index: indexes) {
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
