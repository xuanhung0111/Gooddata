package com.gooddata.qa.graphene.fragments.disc;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractTable;

public class DISCProjectsList extends AbstractTable {

	private static final By BY_DISC_PROJECT_NAME = By.cssSelector("td.project-name-cell a");
	private static final By BY_PROJECT_CHECKBOX = By.cssSelector("td.project-checkbox-cell input");

	@FindBy(xpath = "//div[@class='row collapse']/div/span/input[@type='checkbox']")
	private WebElement checkAllCheckbox;

	@FindBy(css = "button.s-btn-deploy_process")
	private WebElement deployProcessButton;

	@FindBy(xpath = "//div[@class='error-bar']/div[@class='error-bar-title']")
	private WebElement errorBar;

	public WebElement getDeployProcessButton() {
		return deployProcessButton;
	}

	public WebElement getErrorBar() {
		return errorBar;
	}

	public void checkOnProjects(List<String> projectNames) {
		for (String projectName : projectNames) {
			for (int i = 0; i < getNumberOfRows(); i++) {
				if (getRow(i).findElement(BY_PROJECT_CHECKBOX).isEnabled()) {
					if (getRow(i).findElement(BY_DISC_PROJECT_NAME).getText().equals(projectName)) {
						getRow(i).findElement(BY_PROJECT_CHECKBOX).click();
						continue;
					}
				}
			}
		}
	}

	public void selectProject(String projectName) {
		for (int i = 0; i < getNumberOfRows(); i++) {
			if (getRow(i).findElement(BY_PROJECT_CHECKBOX).isEnabled()) {
				if (getRow(i).findElement(BY_DISC_PROJECT_NAME).getText().equals(projectName)) {
					getRow(i).findElement(BY_DISC_PROJECT_NAME).click();
					break;
				}
			}
		}
	}
}
