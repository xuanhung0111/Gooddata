package com.gooddata.qa.graphene.fragments.manage;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class ProjectAndUsersPage extends AbstractFragment {

    @FindBy(xpath = "//span[@class='deleteProject']/button")
    private WebElement deleteProjectButton;

    @FindBy(xpath = "//form/div/span/button[text()='Delete']")
    private WebElement deleteProjectDialogButton;

    @FindBy(className = "project-page-manage-link")
    private WebElement userManagementLink;
    
    @FindBy(css = ".leaveProject .s-btn-leave")
    private WebElement leaveProjectButton;

    @FindBy(css = ".projectNameIpe")
    private WebElement projectNameTag;

    private static final By BY_PROJECTS_LIST = By.className("userProjects");
    private static final By BY_LEAVE_PROJECT_DIALOG_BUTTON = By.cssSelector("form .s-btn-leave");
    private static final By PROJECT_NAME_INPUT_LOCATOR = By.cssSelector(".ipeEditor");
    private static final By SAVE_BUTTON_LOCATOR = By.cssSelector(".s-ipeSaveButton");
    private static final By CANCEL_CONFIRMATION_DIALOG_BUTTON_LOCATOR = By
            .cssSelector(".yui3-d-modaldialog:not(.gdc-hidden) .s-btn-cancel");

    public void deteleProject() {
        waitForElementVisible(deleteProjectButton).click();
        waitForElementVisible(deleteProjectDialogButton).click();
        //redirect to projects page
        waitForElementVisible(BY_PROJECTS_LIST, browser);
        System.out.println("Project deleted...");
    }

    public void openUserManagementPage() {
        waitForElementVisible(userManagementLink).click();
    }
    
    public void leaveProject() {
        waitForElementVisible(leaveProjectButton).click();
        waitForElementVisible(BY_LEAVE_PROJECT_DIALOG_BUTTON, browser).click();
    }

    public void renameProject(String name) {
        waitForElementVisible(projectNameTag).click();
        WebElement projectNameInput = waitForElementVisible(PROJECT_NAME_INPUT_LOCATOR, browser);
        projectNameInput.clear();
        projectNameInput.sendKeys(name);
        waitForElementVisible(SAVE_BUTTON_LOCATOR, browser).click();
    }

    public String getProjectName() {
        return waitForElementVisible(projectNameTag).getText();
    }

    public boolean isDeleteButtonEnabled() {
        return !waitForElementVisible(deleteProjectButton)
                .getAttribute("class")
                .contains("button-disabled");
    }

    public void tryDeleteProjectButDiscard() {
        waitForElementVisible(deleteProjectButton).click();
        waitForElementVisible(CANCEL_CONFIRMATION_DIALOG_BUTTON_LOCATOR, browser).click();
    }

}
