package com.gooddata.qa.graphene.fragments.disc;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractTable;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class DISCProjectsList extends AbstractTable {

	private static final By BY_DISC_PROJECT_NAME = By.cssSelector("td.project-name-cell a");
	private static final By BY_PROJECT_CHECKBOX = By.cssSelector("td.project-checkbox-cell input");

	@FindBy(css = "button.s-btn-deploy_process")
	private WebElement deployProcessButton;

	@FindBy(xpath = "//div[@class='error-bar']/div[@class='error-bar-title']")
	private WebElement errorBar;

	@FindBy(css = ".page-cell")
	private List<WebElement> projectPages;

	public WebElement getDeployProcessButton() {
		return deployProcessButton;
	}

	public WebElement getErrorBar() {
		return errorBar;
	}

	public void checkOnProjects(Map<String, String> projects) {
		for (Entry<String, String> project : projects.entrySet()) {
			selectProject(project.getKey(), project.getValue()).findElement(BY_PROJECT_CHECKBOX).click();
		}
	}

	public void clickOnProjectTitle(String projectName, String projectId) {
		selectProject(projectName, projectId).findElement(BY_DISC_PROJECT_NAME).click();
	}

	public WebElement selectProject(String projectName, String projectId) {
		int pageIndex = 0;
		do {
			for (int i = 0; i < getNumberOfRows(); i++) {
				if (getRow(i).findElement(BY_PROJECT_CHECKBOX).isEnabled()) {
					if (getRow(i).findElement(BY_DISC_PROJECT_NAME).getText().equals(projectName)) {
						if(getRow(i).findElement(BY_DISC_PROJECT_NAME).getAttribute("href").contains(projectId))
							return getRow(i);
					}
				}
			}
			pageIndex++;
			if (projectPages != null && pageIndex < projectPages.size())
				projectPages.get(pageIndex).click();
			waitForElementVisible(getRoot());
		}
		while (projectPages != null && pageIndex < projectPages.size());
		return null;
	}
}
