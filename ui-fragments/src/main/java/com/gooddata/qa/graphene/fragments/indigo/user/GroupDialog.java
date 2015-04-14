package com.gooddata.qa.graphene.fragments.indigo.user;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementPresent;

public class GroupDialog extends AbstractFragment {

    @FindBy(id = "group-dialog-name")
    private WebElement nameInput;

    @FindBy(className = "s-btn-cancel")
    private WebElement cancelButton;

    @FindBy(className = "s-btn-create")
    private WebElement createButton;

    @FindBy(className = "gd-dialog-close")
    private WebElement closeDialog;

    private static By BY_ERROR_MESSAGE = By.cssSelector(".bubble-content .content");

    public boolean isCreateButtonVisible() {
        return waitForElementPresent(createButton).getClass().toString().contains("disabled");
    }

    public void createNewGroup(String name) {
        enterGroupName(name);
        waitForElementVisible(createButton).click();
    }

    public void cancelCreatingGroup(String name) {
        enterGroupName(name);
        waitForElementVisible(cancelButton).click();
    }

    public String getErrorMessage() {
        waitForElementVisible(nameInput).click();
        List<WebElement> contents = browser.findElements(BY_ERROR_MESSAGE);
        return waitForElementVisible(contents.get(contents.size() -1 )).getText().trim();
    }

    public void closeDialog() {
        waitForElementVisible(closeDialog).click();
        waitForElementNotVisible(this.getRoot());
    }

    public void enterGroupName(String name) {
        waitForElementVisible(nameInput).clear();
        nameInput.sendKeys(name);
    }
}