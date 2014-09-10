package com.gooddata.qa.graphene.fragments.disc;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractTable;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

public class DISCProjectsList extends AbstractTable {

	private static final By BY_DISC_PROJECT_NAME = By.cssSelector("td.project-name-cell a");
	private static final By BY_PROJECT_CHECKBOX = By.cssSelector("td.project-checkbox-cell input");
	private static final By BY_DISC_PROJECT_NAME_NOT_ADMIN = By
			.cssSelector(".project-name-user-not-admin-cell .ait-project-list-item-title");
	private static final By BY_DISC_PROJECT_DATA_LOADING_PROCESSES = By
			.cssSelector(".ait-project-list-item-processes-label");
	private static final By BY_DISC_PROJECT_LAST_SUCCESSFUL_EXECUTION = By
			.cssSelector(".ait-project-list-item-last-loaded-label");

	@FindBy(css = "button.s-btn-deploy_process")
	private WebElement deployProcessButton;

	@FindBy(xpath = "//div[@class='error-bar']/div[@class='error-bar-title']")
	private WebElement errorBar;

	@FindBy(css = ".page-cell")
	private List<WebElement> projectPages;
	
	@FindBy(css = ".ait-project-detail-fragment")
	private WebElement projectDetail;

	public WebElement getDeployProcessButton() {
		return deployProcessButton;
	}

	public WebElement getErrorBar() {
		return errorBar;
	}

	public void checkOnProjects(Map<String, String> projects) {
		for (Entry<String, String> project : projects.entrySet()) {
			selectProject(project.getKey(), project.getValue(), true).findElement(BY_PROJECT_CHECKBOX)
					.click();
		}
	}

	public void clickOnProjectTitle(String projectName, String projectId) {
		selectProject(projectName, projectId, true).findElement(BY_DISC_PROJECT_NAME).click();
	}

	public WebElement selectProject(String projectName, String projectId, boolean isAdmin) {
		int pageIndex = 0;
		do {
			for (int i = 0; i < getNumberOfRows(); i++) {
				if (isAdmin) {
					if (getRow(i).findElement(BY_PROJECT_CHECKBOX).isEnabled()) {
						if (getRow(i).findElement(BY_DISC_PROJECT_NAME).getText().equals(projectName)) {
							if (getRow(i).findElement(BY_DISC_PROJECT_NAME).getAttribute("href")
									.contains(projectId))
								return getRow(i);
						}
					}
				}
				else {
					if (!getRow(i).findElement(BY_PROJECT_CHECKBOX).isEnabled()) {
						if (getRow(i).findElement(BY_DISC_PROJECT_NAME_NOT_ADMIN).getText()
								.equals(projectName)) {
							return getRow(i);
						}
					}
				}
			}
			pageIndex++;
			if (projectPages != null && pageIndex < projectPages.size())
				projectPages.get(pageIndex).click();
			waitForElementVisible(getRoot());
		} while (projectPages != null && pageIndex < projectPages.size());
		return null;
	}

	public void assertDataLoadingProcesses(int processNumber, int scheduleNumber,
			Map<String, String> projectsMap) {
		String expectedDataLoadingProcess = String.format("%d processes, %d schedules",
				processNumber, scheduleNumber);
		System.out.println("expectedDataLoadingProcess " + expectedDataLoadingProcess);
		for (Entry<String, String> project : projectsMap.entrySet()) {
			assertEquals(
					expectedDataLoadingProcess,
					selectProject(project.getKey(), project.getValue(), true).findElement(
							BY_DISC_PROJECT_DATA_LOADING_PROCESSES).getText());
		}
	}

	public void assertLastLoaded(String executionDate, String executionTime,
			Map<String, String> projectsMap) {
		String expectedLastLoaded = executionDate + " " + executionTime;
		System.out.println("expectedLastLoaded " + expectedLastLoaded);
		for (Entry<String, String> project : projectsMap.entrySet()) {
			assertEquals(expectedLastLoaded, selectProject(project.getKey(), project.getValue(), true)
					.findElement(BY_DISC_PROJECT_LAST_SUCCESSFUL_EXECUTION).getText());
		}
	}

	public void assertProjectNotAdmin(String projectName, String projectId) {
		waitForElementVisible(getRoot());
		WebElement projectCell = selectProject(projectName, projectId, false);
		assertNotNull(projectCell.findElement(BY_DISC_PROJECT_NAME_NOT_ADMIN));
		try {
			projectCell.findElement(BY_DISC_PROJECT_NAME_NOT_ADMIN).click();
			waitForElementVisible(projectDetail);
		}
		catch (NoSuchElementException ex) {
			System.out.println("Non-admin user cannot access project detail page!");
		}
		waitForElementVisible(getRoot());
	}
}
