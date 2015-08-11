package com.gooddata.qa.graphene.fragments.indigo.user;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementPresent;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

public class GroupDialog extends AbstractFragment {

    public static final By LOCATOR = By.className("group-dialog");

    @FindBy(css = ".gd-dialog-header > h2")
    private WebElement title;

    @FindBy(id = "group-dialog-name")
    private WebElement nameInput;

    @FindBy(className = "s-btn-cancel")
    private WebElement cancelButton;

    @FindBy(className = "group-dialog-submit")
    private WebElement submitButton;

    @FindBy(className = "gd-dialog-close")
    private WebElement closeDialog;

    private static By BY_ERROR_MESSAGE = By.cssSelector(".bubble-content .content");

    public boolean isSubmitButtonVisible() {
        return waitForElementPresent(submitButton).getClass().toString().contains("disabled");
    }

    public void submitDialogGroup(String name) {
        enterGroupName(name);
        waitForElementVisible(submitButton).click();
    }

    public void cancelSubmitDialogGroup(String name) {
        enterGroupName(name);
        waitForElementVisible(cancelButton).click();
    }

    public String getErrorMessage() {
        waitForElementVisible(nameInput).click();
        List<WebElement> contents = browser.findElements(BY_ERROR_MESSAGE);
        return waitForElementVisible(contents.get(contents.size() - 1)).getText().trim();
    }

    public String getGroupNameText() {
        return waitForElementVisible(nameInput).getAttribute("value").trim();
    }

    public void closeDialog() {
        waitForElementVisible(closeDialog).click();
        waitForElementNotVisible(this.getRoot());
    }

    public void enterGroupName(String name) {
        waitForElementVisible(nameInput).clear();
        nameInput.sendKeys(name);
    }

    public void verifyStateOfDialog(State state) {
        assertTrue(state.title.equals(waitForElementVisible(title).getText()));
        assertTrue(state.submitButtonName.equals(waitForElementVisible(submitButton).getText()));
        assertFalse(isSubmitButtonVisible());
        assertEquals(waitForElementVisible(nameInput).getAttribute("value").trim().isEmpty(),
                state.isGroupNameEmpty);
    }

    public static enum State {
        CREATE("Create group", "Create", true),
        EDIT("Rename group", "Rename", false);

        private String title;
        private String submitButtonName;
        private boolean isGroupNameEmpty;

        private State(String title, String submitButtonName, boolean isGroupNameEmpty) {
            this.title = title;
            this.submitButtonName = submitButtonName;
            this.isGroupNameEmpty = isGroupNameEmpty;
        }
    }
}
