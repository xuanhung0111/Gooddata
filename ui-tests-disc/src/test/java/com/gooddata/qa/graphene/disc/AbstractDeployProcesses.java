package com.gooddata.qa.graphene.disc;

import java.util.List;

import org.json.JSONException;
import org.junit.Assert;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.DISCProcessTypes;
import com.gooddata.qa.utils.graphene.Screenshots;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class AbstractDeployProcesses extends AbstractProjectTest {

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

	protected void deployProcess(String zipFile, DISCProcessTypes processType, String processName,
			String progressDialogMessage, String deployResultMessage) throws InterruptedException {
		waitForElementVisible(deployForm.getRoot());
		deployForm.setDeployProcessInput(zipFile, processType, processName);
		Screenshots.takeScreenshot(browser, "input-fields-deploy-" + processName, getClass());
		deployForm.getDeployConfirmButton().click();
		while (deployForm.getDeployProcessDialogButton().getText().contains("Cancel"))
			Thread.sleep(100);
		Assert.assertEquals(deployForm.getDeployProcessDialog().getText(), progressDialogMessage);
		if (deployResultMessage != null) {
			while (deployForm.getDeployProcessDialogButton().getText().equals("Deploying")
					|| deployForm.getDeployProcessDialogButton().getText().equals("Re-deploying"))
				Thread.sleep(100);
			Assert.assertEquals(deployForm.getDeployProcessDialog().getText(), deployResultMessage);
			waitForElementNotPresent(deployForm.getRoot());
		}
	}

	protected void assertDeployedProcessInProjects(String processName, List<String> projectNames,
			DISCProcessTypes processType, List<String> executables) {
		for (String projectName : projectNames) {
			waitForElementVisible(discProjectsList.getRoot());
			discProjectsList.selectProject(projectName);
			waitForElementVisible(projectDetailPage.getRoot());
			projectDetailPage.assertProcessInList(processName, processType, executables);
			Screenshots.takeScreenshot(browser, "assert-deployed-process-" + processName,
					getClass());
			discNavigation.clickOnProjectsButton();
		}
	}

	protected void deployInProjectsPage(List<String> projects, String zipFileName,
			DISCProcessTypes processType, String processName, List<String> executables,
			boolean isSuccessful) throws JSONException, InterruptedException {
		selectProjectsToDeployInProjectsPage(projects);
		String progressDialogMessage = String.format(DEPLOY_PROGRESS_MESSAGE_IN_PROJECTS_PAGE,
				zipFileName, processName);
		String failedDeployMessage = String.format(FAILED_DEPLOY_MESSAGE_IN_PROJECTS_PAGE,
				zipFileName);
		String failedDeployError = String.format(FAILED_DEPLOY_ERROR_IN_PROJECTS_PAGE, zipFileName,
				processName);
		String successfulDeployMessage = String.format(SUCCESSFUL_DEPLOY_MESSAGE_IN_PROJECTS_PAGE,
				zipFileName);
		if (isSuccessful) {
			deployProcess(zipFilePath + zipFileName + ".zip", processType, processName,
					progressDialogMessage, successfulDeployMessage);
			assertDeployedProcessInProjects(processName, projects, processType, executables);
		} else {
			deployProcess(zipFilePath + zipFileName + ".zip", processType, processName,
					progressDialogMessage, failedDeployMessage);
			waitForElementVisible(discProjectsList.getRoot());
			Assert.assertEquals(failedDeployError, discProjectsList.getErrorBar().getText());
		}
	}

	protected void deployInProjectDetailPage(String projectName, String zipFileName,
			DISCProcessTypes processType, String processName, List<String> executables,
			boolean isSuccessful) throws JSONException, InterruptedException {
		openUrl(DISC_PROJECTS_PAGE_URL);
		waitForElementVisible(discProjectsList.getRoot());
		discProjectsList.selectProject(projectName);
		waitForElementVisible(projectDetailPage.getRoot());
		projectDetailPage.clickOnDeployProcessButton();
		String progressDialogMessage = String.format(
				DEPLOY_PROGRESS_MESSAGE_IN_PROJECT_DETAIL_PAGE, zipFileName + ".zip", processName);
		String successfulDeployMessage = String.format(
				SUCCESSFUL_DEPLOY_MESSAGE_IN_PROJECT_DETAIL_PAGE, processName);
		String failedDeployMessage = String.format(FAILED_DEPLOY_MESSAGE_IN_PROJECT_DETAIL_PAGE,
				zipFileName, processName);
		if (isSuccessful) {
			deployProcess(zipFilePath + zipFileName + ".zip", processType, processName,
					progressDialogMessage, successfulDeployMessage);
			waitForElementVisible(projectDetailPage.getRoot());
			projectDetailPage.assertProcessInList(processName, processType, executables);
			Screenshots.takeScreenshot(browser,
					"assert-successful-deployed-process-" + processName, getClass());
		} else {
			deployProcess(zipFilePath + zipFileName + ".zip", processType, processName,
					progressDialogMessage, null);
			waitForElementVisible(projectDetailPage.getDeployErrorDialog());
			Assert.assertEquals(failedDeployMessage, projectDetailPage.getDeployErrorDialog()
					.getText());
			Screenshots.takeScreenshot(browser, "assert-failed-deployed-process-" + processName,
					getClass());
		}
	}

	protected void redeployProcess(String projectName, String processName, String newZipFileName,
			String newProcessName, DISCProcessTypes newProcessType, List<String> newExecutables,
			boolean isSuccessful) throws JSONException, InterruptedException {
		openUrl(DISC_PROJECTS_PAGE_URL);
		waitForElementVisible(discProjectsList.getRoot());
		discProjectsList.selectProject(projectName);
		waitForElementVisible(projectDetailPage.getRoot());
		projectDetailPage.getRedeployButton(processName).click();
		String progressDialogMessage = String.format(REDEPLOY_PROGRESS_MESSAGE, processName,
				newZipFileName, newProcessName);
		String successfulDeployMessage = String.format(SUCCESSFUL_REDEPLOY_MESSAGE, processName,
				newProcessName);
		String failedDeployMessage = String.format(FAILED_REDEPLOY_MESSAGE, newZipFileName,
				newProcessName);
		if (isSuccessful) {
			deployProcess(zipFilePath + newZipFileName + ".zip", newProcessType, newProcessName,
					progressDialogMessage, successfulDeployMessage);
			waitForElementVisible(projectDetailPage.getRoot());
			projectDetailPage.assertProcessInList(processName, newProcessType, newExecutables);
			Screenshots.takeScreenshot(browser, "assert-successful-redeployed-process-"
					+ newProcessName, getClass());
		} else {
			deployProcess(zipFilePath + newZipFileName + ".zip", newProcessType, newProcessName,
					progressDialogMessage, null);
			waitForElementPresent(projectDetailPage.getDeployErrorDialog());
			Assert.assertEquals(failedDeployMessage, projectDetailPage.getDeployErrorDialog()
					.getText());
			Screenshots.takeScreenshot(browser, "assert-failed-redeployed-process-"
					+ newProcessName, getClass());
		}
	}

	protected void selectProjectsToDeployInProjectsPage(List<String> projects) {
		openUrl(DISC_PROJECTS_PAGE_URL);
		waitForElementVisible(discProjectsList.getRoot());
		discProjectsList.checkOnProjects(projects);
		Assert.assertTrue(discProjectsList.getDeployProcessButton().isEnabled());
		discProjectsList.getDeployProcessButton().click();
		waitForElementVisible(deployForm.getRoot());
	}
}
