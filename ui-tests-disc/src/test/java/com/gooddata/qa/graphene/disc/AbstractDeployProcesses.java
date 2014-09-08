package com.gooddata.qa.graphene.disc;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.DISCProcessTypes;
import com.gooddata.qa.utils.graphene.Screenshots;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

public abstract class AbstractDeployProcesses extends AbstractProjectTest {

	private static final String FAILED_REDEPLOY_MESSAGE = "Failed to re-deploy the %s.zip process as %s. Reason: Process contains no executables.";
	private static final String SUCCESSFUL_REDEPLOY_MESSAGE = "Re-deploy %s process" + "\n"
			+ "%s process has been re-deployed successfully.";
	private static final String REDEPLOY_PROGRESS_MESSAGE = "Re-deploy %s process" + "\n"
			+ "Re-deploying \"%s.zip\" process as %s.";
	private static final String FAILED_DEPLOY_MESSAGE_IN_PROJECT_DETAIL_PAGE = "Failed to deploy the %s.zip process as %s. Reason: Process contains no executables.";
	private static final String SUCCESSFUL_DEPLOY_MESSAGE_IN_PROJECT_DETAIL_PAGE = "Deploy process to project"
			+ "\n" + "%s process has been deployed successfully.";
	private static final String DEPLOY_PROGRESS_MESSAGE_IN_PROJECT_DETAIL_PAGE = "Deploy process to project"
			+ "\n" + "Deploying \"%s\" process as %s.";
	private static final String SUCCESSFUL_DEPLOY_MESSAGE_IN_PROJECTS_PAGE = "Deploy process to selected projects"
			+ "\n" + "The \"%s.zip\" has been deployed successfully to all selected projects.";
	private static final String FAILED_DEPLOY_MESSAGE_IN_PROJECTS_PAGE = "Deploy process to selected projects"
			+ "\n"
			+ "Deployment of the \"%s.zip\" has finished."
			+ " The process has not been deployed successfully into some projects. See the error message";
	private static final String DEPLOY_PROGRESS_MESSAGE_IN_PROJECTS_PAGE = "Deploy process to selected projects"
			+ "\n" + "Deploying %s.zip as %s.";
	protected static final String DISC_PROJECTS_PAGE_URL = "admin/disc/#/projects";
	private static final String FAILED_DEPLOY_ERROR_IN_PROJECTS_PAGE = "Failed to deploy the %s.zip process as %s to the projects below. Reasons: Process contains no executables.";
	protected String zipFilePath;

	protected Map<String, String> projects;

	protected Map<String, String> getProjectsMap() {
		if (projects == null) {
			projects = new LinkedHashMap<String, String>();
			projects.put(projectTitle, testParams.getProjectId());
		}
		return projects;
	}

	protected void openProjectDetailPage(String projectName, String projectId) {
		openUrl(DISC_PROJECTS_PAGE_URL);
		waitForElementVisible(discProjectsList.getRoot());
		discProjectsList.clickOnProjectTitle(projectName, projectId);
		waitForElementVisible(projectDetailPage.getRoot());
	}

	protected void deployProcess(String zipFile, DISCProcessTypes processType, String processName,
			String progressDialogMessage, String deployResultMessage) throws InterruptedException {
		waitForElementVisible(deployForm.getRoot());
		deployForm.setDeployProcessInput(zipFile, processType, processName);
		assertFalse(deployForm.inputFileHasError());
		assertFalse(deployForm.inputProcessNameHasError());
		Screenshots.takeScreenshot(browser, "input-fields-deploy-" + processName, getClass());
		deployForm.getDeployConfirmButton().click();
		for (int i = 0; deployForm.getDeployProcessDialogButton().getText().contains("Cancel")
				&& i < 50; i++)
			Thread.sleep(100);
		assertEquals(deployForm.getDeployProcessDialog().getText(), progressDialogMessage);
		if (deployResultMessage != null) {
			for (int i = 0; i < 100 && (deployForm.getDeployProcessDialogButton().getText().equals("Deploying") ||
					deployForm.getDeployProcessDialogButton().getText().equals("Re-deploying")); i++)
				Thread.sleep(100);
			assertEquals(deployForm.getDeployProcessDialog().getText(), deployResultMessage);
			waitForElementNotPresent(deployForm.getRoot());
		}
	}

	protected void assertDeployedProcessInProjects(String processName,
			Map<String, String> projects, DISCProcessTypes processType, List<String> executables) {
		for (Entry<String, String> project : projects.entrySet()) {
			waitForElementVisible(discProjectsList.getRoot());
			discProjectsList.clickOnProjectTitle(project.getKey(), project.getValue());
			waitForElementVisible(projectDetailPage.getRoot());
			projectDetailPage.assertProcessInList(processName, processType, executables);
			Screenshots.takeScreenshot(browser, "assert-deployed-process-" + processName,
					getClass());
			discNavigation.clickOnProjectsButton();
		}
	}

	protected void deployInProjectsPage(Map<String, String> projects, String zipFileName,
			DISCProcessTypes processType, String processName, List<String> executables,
			boolean isSuccessful) throws JSONException, InterruptedException {
		selectProjectsToDeployInProjectsPage(projects);
		String progressDialogMessage = String.format(DEPLOY_PROGRESS_MESSAGE_IN_PROJECTS_PAGE,
				zipFileName, processName);
		if (isSuccessful) {
			String successfulDeployMessage = String.format(
					SUCCESSFUL_DEPLOY_MESSAGE_IN_PROJECTS_PAGE, zipFileName);
			deployProcess(zipFilePath + zipFileName + ".zip", processType, processName,
					progressDialogMessage, successfulDeployMessage);
			assertDeployedProcessInProjects(processName, projects, processType, executables);
		} else {
			String failedDeployMessage = String.format(FAILED_DEPLOY_MESSAGE_IN_PROJECTS_PAGE,
					zipFileName);
			String failedDeployError = String.format(FAILED_DEPLOY_ERROR_IN_PROJECTS_PAGE,
					zipFileName, processName);
			deployProcess(zipFilePath + zipFileName + ".zip", processType, processName,
					progressDialogMessage, failedDeployMessage);
			waitForElementVisible(discProjectsList.getRoot());
			assertEquals(failedDeployError, discProjectsList.getErrorBar().getText());
		}
	}

	protected void deployInProjectDetailPage(String projectName, String projectId,
			String zipFileName, DISCProcessTypes processType, String processName,
			List<String> executables, boolean isSuccessful) throws JSONException,
			InterruptedException {
		openProjectDetailPage(projectName, projectId);
		waitForElementVisible(projectDetailPage.getRoot());
		projectDetailPage.clickOnDeployProcessButton();
		String progressDialogMessage = String.format(
				DEPLOY_PROGRESS_MESSAGE_IN_PROJECT_DETAIL_PAGE, zipFileName + ".zip", processName);
		if (isSuccessful) {
			String successfulDeployMessage = String.format(
					SUCCESSFUL_DEPLOY_MESSAGE_IN_PROJECT_DETAIL_PAGE, processName);
			deployProcess(zipFilePath + zipFileName + ".zip", processType, processName,
					progressDialogMessage, successfulDeployMessage);
			waitForElementVisible(projectDetailPage.getRoot());
			projectDetailPage.checkFocusedProcess(processName);
			projectDetailPage.assertProcessInList(processName, processType, executables);
			Screenshots.takeScreenshot(browser,
					"assert-successful-deployed-process-" + processName, getClass());
		} else {
			String failedDeployMessage = String.format(
					FAILED_DEPLOY_MESSAGE_IN_PROJECT_DETAIL_PAGE, zipFileName, processName);
			deployProcess(zipFilePath + zipFileName + ".zip", processType, processName,
					progressDialogMessage, null);
			waitForElementVisible(projectDetailPage.getDeployErrorDialog());
			assertEquals(failedDeployMessage, projectDetailPage.getDeployErrorDialog().getText());
			Screenshots.takeScreenshot(browser, "assert-failed-deployed-process-" + processName,
					getClass());
		}
	}

	protected void redeployProcess(String projectName, String projectId, String processName,
			String newZipFileName, String newProcessName, DISCProcessTypes newProcessType,
			List<String> newExecutables, boolean isSuccessful) throws JSONException,
			InterruptedException {
		openProjectDetailPage(projectName, projectId);
		waitForElementVisible(projectDetailPage.getRoot());
		projectDetailPage.getRedeployButton(processName).click();
		String progressDialogMessage = String.format(REDEPLOY_PROGRESS_MESSAGE, processName,
				newZipFileName, newProcessName);
		if (isSuccessful) {
			String successfulDeployMessage = String.format(SUCCESSFUL_REDEPLOY_MESSAGE,
					processName, newProcessName);
			deployProcess(zipFilePath + newZipFileName + ".zip", newProcessType, newProcessName,
					progressDialogMessage, successfulDeployMessage);
			waitForElementVisible(projectDetailPage.getRoot());
			projectDetailPage.checkFocusedProcess(newProcessName);
			projectDetailPage.assertProcessInList(processName, newProcessType, newExecutables);
			Screenshots.takeScreenshot(browser, "assert-successful-redeployed-process-"
					+ newProcessName, getClass());
		} else {
			String failedDeployMessage = String.format(FAILED_REDEPLOY_MESSAGE, newZipFileName,
					newProcessName);
			deployProcess(zipFilePath + newZipFileName + ".zip", newProcessType, newProcessName,
					progressDialogMessage, null);
			waitForElementPresent(projectDetailPage.getDeployErrorDialog());
			assertEquals(failedDeployMessage, projectDetailPage.getDeployErrorDialog().getText());
			Screenshots.takeScreenshot(browser, "assert-failed-redeployed-process-"
					+ newProcessName, getClass());
		}
	}

	protected void selectProjectsToDeployInProjectsPage(Map<String, String> projects) {
		openUrl(DISC_PROJECTS_PAGE_URL);
		waitForElementVisible(discProjectsList.getRoot());
		discProjectsList.checkOnProjects(projects);
		assertTrue(discProjectsList.getDeployProcessButton().isEnabled());
		discProjectsList.getDeployProcessButton().click();
		waitForElementVisible(deployForm.getRoot());
	}
}
