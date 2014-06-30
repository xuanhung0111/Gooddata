package com.gooddata.qa.graphene.fragments.manage;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class ProjectAndUsersPage extends AbstractFragment {

    @FindBy(xpath = "//span[@class='deleteProject']/button")
    private WebElement deleteProjectButton;

    @FindBy(xpath = "//form/div/span/button[text()='Delete']")
    private WebElement deleteProjectDialogButton;

    private static final By BY_PROJECTS_LIST = By.className("userProjects");

    public void deteleProject() {
        waitForElementVisible(deleteProjectButton).click();
        waitForElementVisible(deleteProjectDialogButton).click();
        //redirect to projects page
        waitForElementVisible(BY_PROJECTS_LIST, browser);
        System.out.println("Project deleted...");
    }

}
