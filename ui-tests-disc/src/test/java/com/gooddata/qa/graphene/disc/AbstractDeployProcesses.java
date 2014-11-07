package com.gooddata.qa.graphene.disc;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;
import org.openqa.selenium.NoSuchElementException;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.DISCProcessTypes;
import com.gooddata.qa.utils.graphene.Screenshots;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

public abstract class AbstractDeployProcesses extends AbstractProjectTest {

    private static final String FAILED_REDEPLOY_MESSAGE =
            "Failed to re-deploy the %s.zip process as %s. Reason: Process contains no executables.";
    private static final String FAILED_DEPLOY_MESSAGE_IN_PROJECT_DETAIL_PAGE =
            "Failed to deploy the %s.zip process as %s. Reason: Process contains no executables.";
    private static final String SUCCESSFUL_DEPLOY_MESSAGE_IN_PROJECT_DETAIL_PAGE =
            "Deploy process to project" + "\n" + "%s process has been deployed successfully.";
    private static final String DEPLOY_PROGRESS_MESSAGE_IN_PROJECT_DETAIL_PAGE =
            "Deploy process to project" + "\n" + "Deploying \"%s\" process as %s.";
    private static final String SUCCESSFUL_DEPLOY_MESSAGE_IN_PROJECTS_PAGE =
            "Deploy process to selected projects" + "\n"
                    + "The \"%s.zip\" has been deployed successfully to all selected projects.";
    private static final String FAILED_DEPLOY_MESSAGE_IN_PROJECTS_PAGE =
            "Deploy process to selected projects"
                    + "\n"
                    + "Deployment of the \"%s.zip\" has finished."
                    + " The process has not been deployed successfully into some projects. See the error message";
    private static final String DEPLOY_PROGRESS_MESSAGE_IN_PROJECTS_PAGE =
            "Deploy process to selected projects" + "\n" + "Deploying %s.zip as %s.";
    protected static final String DISC_PROJECTS_PAGE_URL = "admin/disc/#/projects";
    private static final String FAILED_DEPLOY_ERROR_IN_PROJECTS_PAGE =
            "Failed to deploy the %s.zip process as %s to the projects below. Reasons: Process contains no executables.";
    protected String zipFilePath;

    protected Map<String, String> projects;

    protected Map<String, String> getProjectsMap() {
        if (projects == null) {
            projects = new LinkedHashMap<String, String>();
            projects.put(projectTitle, testParams.getProjectId());
        }
        return projects;
    }

    protected void openProjectDetailPage(String projectName, String projectId)
            throws InterruptedException {
        openUrl(DISC_PROJECTS_PAGE_URL);
        waitForElementVisible(discProjectsList.getRoot());
        discProjectsList.clickOnProjectTitle(projectName, projectId);
        waitForElementVisible(projectDetailPage.getRoot());
    }

    protected void openProjectDetailByUrl(String projectId) {
        openUrl(DISC_PROJECTS_PAGE_URL + "/" + projectId);
        waitForElementVisible(projectDetailPage.getRoot());
    }

    protected void deployProcess(String zipFile, DISCProcessTypes processType, String processName,
            boolean isSuccessful) throws InterruptedException {
        waitForElementVisible(deployForm.getRoot());
        deployForm.setDeployProcessInput(zipFile, processType, processName);
        assertFalse(deployForm.inputFileHasError());
        assertFalse(deployForm.inputProcessNameHasError());
        Screenshots.takeScreenshot(browser, "input-fields-deploy-" + processName, getClass());
        deployForm.getDeployConfirmButton().click();
        try {
            for (int i = 0; deployForm.getDeployProcessDialogButton().getText().contains("Cancel")
                    && i < 20; i++)
                Thread.sleep(500);
            for (int i = 0; i < 20
                    && deployForm.getDeployProcessDialogButton().getText().toLowerCase()
                            .contains("deploying"); i++) {
                System.out.println("Process is deploying!");
                Thread.sleep(500);
            }
            if (isSuccessful) {
                assertTrue(deployForm.getDeployProcessDialogButton().getText().toLowerCase()
                        .contains("deployed"));
                System.out.println("Deploy progress is finished!");
                for (int i = 0; i < 30 && deployForm.getRoot().isDisplayed(); i++)
                    Thread.sleep(1000);
            }
        } catch (NoSuchElementException ex) {
            /* The deploy dialog may be not checked by this test! */
        }
    }

    protected void assertDeployedProcessInProjects(String processName,
            Map<String, String> projects, DISCProcessTypes processType, List<String> executables)
            throws InterruptedException {
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
        openUrl(DISC_PROJECTS_PAGE_URL);
        selectProjectsToDeployInProjectsPage(projects);
        if (isSuccessful) {
            deployProcess(zipFilePath + zipFileName + ".zip", processType, processName, true);
            assertDeployedProcessInProjects(processName, projects, processType, executables);
        } else {
            String failedDeployError =
                    String.format(FAILED_DEPLOY_ERROR_IN_PROJECTS_PAGE, zipFileName, processName);
            deployProcess(zipFilePath + zipFileName + ".zip", processType, processName, false);
            try {
                for (int i = 0; i < 30 && deployForm.getRoot().isDisplayed(); i++)
                    Thread.sleep(1000);
            } catch (NoSuchElementException ex) {
                System.out.println("Deploy progress is finished!");
            }
            waitForElementVisible(discProjectsList.getRoot());
            System.out.println("Error bar in projects page: "
                    + discProjectsList.getErrorBar().getText());
            assertEquals(failedDeployError, discProjectsList.getErrorBar().getText());
        }
    }

    protected void deployInProjectDetailPage(String projectName, String projectId,
            String zipFileName, DISCProcessTypes processType, String processName,
            List<String> executables, boolean isSuccessful) throws JSONException,
            InterruptedException {
        waitForElementVisible(projectDetailPage.getRoot());
        projectDetailPage.clickOnDeployProcessButton();
        if (isSuccessful) {
            deployProcess(zipFilePath + zipFileName + ".zip", processType, processName, true);
            waitForElementVisible(projectDetailPage.getRoot());
            Thread.sleep(2000);
            projectDetailPage.checkFocusedProcess(processName);
            projectDetailPage.assertProcessInList(processName, processType, executables);
            Screenshots.takeScreenshot(browser,
                    "assert-successful-deployed-process-" + processName, getClass());
        } else {
            String failedDeployMessage =
                    String.format(FAILED_DEPLOY_MESSAGE_IN_PROJECT_DETAIL_PAGE, zipFileName,
                            processName);
            deployProcess(zipFilePath + zipFileName + ".zip", processType, processName, false);
            waitForElementVisible(projectDetailPage.getDeployErrorDialog());
            System.out.println("Error deploy dialog message: "
                    + projectDetailPage.getDeployErrorDialog().getText());
            assertEquals(failedDeployMessage, projectDetailPage.getDeployErrorDialog().getText());
            Screenshots.takeScreenshot(browser, "assert-failed-deployed-process-" + processName,
                    getClass());
        }
    }

    protected void redeployProcess(String projectName, String projectId, String processName,
            String newZipFileName, String newProcessName, DISCProcessTypes newProcessType,
            List<String> newExecutables, boolean isSuccessful) throws JSONException,
            InterruptedException {
        waitForElementVisible(projectDetailPage.getRoot());
        projectDetailPage.getRedeployButton(processName).click();
        if (isSuccessful) {
            deployProcess(zipFilePath + newZipFileName + ".zip", newProcessType, newProcessName,
                    true);
            waitForElementVisible(projectDetailPage.getRoot());
            Thread.sleep(2000);
            projectDetailPage.checkFocusedProcess(newProcessName);
            projectDetailPage.assertProcessInList(processName, newProcessType, newExecutables);
            Screenshots.takeScreenshot(browser, "assert-successful-redeployed-process-"
                    + newProcessName, getClass());
        } else {
            String failedDeployMessage =
                    String.format(FAILED_REDEPLOY_MESSAGE, newZipFileName, newProcessName);
            deployProcess(zipFilePath + newZipFileName + ".zip", newProcessType, newProcessName,
                    false);
            waitForElementPresent(projectDetailPage.getDeployErrorDialog());
            assertEquals(failedDeployMessage, projectDetailPage.getDeployErrorDialog().getText());
            Screenshots.takeScreenshot(browser, "assert-failed-redeployed-process-"
                    + newProcessName, getClass());
        }
    }

    protected void selectProjectsToDeployInProjectsPage(Map<String, String> projects)
            throws InterruptedException {
        waitForElementVisible(discProjectsList.getRoot());
        discProjectsList.checkOnProjects(projects);
        assertTrue(discProjectsList.getDeployProcessButton().isEnabled());
        discProjectsList.getDeployProcessButton().click();
        waitForElementVisible(deployForm.getRoot());
    }

    protected void checkDeployDialogMessageInProjectDetail(String projectName, String projectId,
            String zipFileName, DISCProcessTypes processType, String processName,
            boolean isSuccessful) throws InterruptedException {
        projectDetailPage.clickOnDeployProcessButton();
        String progressDialogMessage =
                String.format(DEPLOY_PROGRESS_MESSAGE_IN_PROJECT_DETAIL_PAGE, zipFileName + ".zip",
                        processName);
        waitForElementVisible(deployForm.getRoot());
        deployForm.setDeployProcessInput(zipFilePath + zipFileName + ".zip", processType,
                processName);
        assertFalse(deployForm.inputFileHasError());
        assertFalse(deployForm.inputProcessNameHasError());
        Screenshots.takeScreenshot(browser, "input-fields-deploy-" + processName, getClass());
        boolean correctProgressMessage = false;
        int index = 0;
        deployForm.getDeployConfirmButton().click();
        do {
            if (deployForm.getDeployProcessDialog().getText().equals(progressDialogMessage)) {
                correctProgressMessage = true;
                break;
            } else {
                Thread.sleep(100);
                index++;
            }
        } while (index < 10);
        assertTrue(correctProgressMessage, "Displayed progess message: "
                + deployForm.getDeployProcessDialog().getText());
        for (int i = 0; i < 10
                && deployForm.getDeployProcessDialogButton().getText().toLowerCase()
                        .contains("deploying"); i++)
            Thread.sleep(1000);
        if (isSuccessful) {
            String successfulDeployMessage =
                    String.format(SUCCESSFUL_DEPLOY_MESSAGE_IN_PROJECT_DETAIL_PAGE, processName);
            assertTrue(deployForm.getDeployProcessDialog().getText()
                    .equals(successfulDeployMessage));
        }
    }

    protected void checkDeployDialogMessageInProjectsPage(Map<String, String> projects,
            String zipFileName, DISCProcessTypes processType, String processName,
            boolean isSuccessful) throws InterruptedException {
        openUrl(DISC_PROJECTS_PAGE_URL);
        selectProjectsToDeployInProjectsPage(projects);
        String progressDialogMessage =
                String.format(DEPLOY_PROGRESS_MESSAGE_IN_PROJECTS_PAGE, zipFileName, processName);
        waitForElementVisible(deployForm.getRoot());
        deployForm.setDeployProcessInput(zipFilePath + zipFileName + ".zip", processType,
                processName);
        assertFalse(deployForm.inputFileHasError());
        assertFalse(deployForm.inputProcessNameHasError());
        Screenshots.takeScreenshot(browser, "input-fields-deploy-" + processName, getClass());
        boolean correctProgressMessage = false;
        int index = 0;
        deployForm.getDeployConfirmButton().click();
        correctProgressMessage =
                deployForm.getDeployProcessDialog().getText().equals(progressDialogMessage);
        do {
            if (correctProgressMessage) {
                break;
            } else {
                Thread.sleep(100);
                correctProgressMessage =
                        deployForm.getDeployProcessDialog().getText().equals(progressDialogMessage);
                index++;
            }
        } while (index < 10);
        assertTrue(correctProgressMessage, "Displayed progess message: "
                + deployForm.getDeployProcessDialog().getText());
        for (int i = 0; i < 10
                && deployForm.getDeployProcessDialogButton().getText().toLowerCase()
                        .contains("deploying"); i++)
            Thread.sleep(1000);
        if (isSuccessful) {
            String successfulDeployMessage =
                    String.format(SUCCESSFUL_DEPLOY_MESSAGE_IN_PROJECTS_PAGE, zipFileName);
            assertTrue(deployForm.getDeployProcessDialog().getText()
                    .equals(successfulDeployMessage));
        } else {
            String failedDeployMessage =
                    String.format(FAILED_DEPLOY_MESSAGE_IN_PROJECTS_PAGE, zipFileName);
            assertTrue(deployForm.getDeployProcessDialog().getText().equals(failedDeployMessage));
        }
    }

    protected Map<String, String> createMultipleProjects(String projectNamePrefix, int projectNumber)
            throws JSONException, InterruptedException {
        Map<String, String> additionalProjects = new HashMap<String, String>(1);
        for (int i = 0; i < projectNumber; i++) {
            openUrl(PAGE_GDC_PROJECTS);
            waitForElementVisible(gpProject.getRoot());
            additionalProjects.put(projectNamePrefix + String.valueOf(i), gpProject.createProject(
                    projectNamePrefix + i, projectNamePrefix + i, null,
                    testParams.getAuthorizationToken(), testParams.getDwhDriver(),
                    projectCreateCheckIterations));
        }
        return additionalProjects;
    }

    protected void deleteProjects(Map<String, String> projectsToDelete) throws InterruptedException {
        for (Entry<String, String> projectToDelete : projectsToDelete.entrySet()) {
            deleteProject(projectToDelete.getValue());
        }
    }
    
    protected void cleanProcessesInProjectDetail(String projectId) throws InterruptedException {
        openProjectDetailByUrl(projectId);
        projectDetailPage.deleteAllProcesses();
    }
}
