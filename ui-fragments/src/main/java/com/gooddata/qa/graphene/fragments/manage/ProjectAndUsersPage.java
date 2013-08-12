package com.gooddata.qa.graphene.fragments.manage;

import org.jboss.arquillian.graphene.enricher.findby.FindBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class ProjectAndUsersPage extends AbstractFragment {
	
	@FindBy(xpath="//span[@class='deleteProject']/button")
	private WebElement deleteProjectButton;
	
	@FindBy(xpath="//form/div/span/button[text()='Delete']")
	private WebElement deleteProjectDialogButton;
	
	private static final By BY_PROJECTS_LIST = By.className("userProjects");
	
	public void deteleProject() {
		waitForElementVisible(deleteProjectButton);
		deleteProjectButton.click();
		waitForElementVisible(deleteProjectDialogButton);
		deleteProjectDialogButton.click();
		//redirect to projects page
		waitForElementVisible(BY_PROJECTS_LIST);
		System.out.println("Project deleted...");
	}

}
