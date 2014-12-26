package com.gooddata.qa.graphene.disc;

import java.util.Arrays;
import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebDriver;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.disc.ProjectInfo;
import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.enums.disc.DeployPackages;
import com.gooddata.qa.graphene.enums.disc.ProcessTypes;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.google.common.base.Predicate;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

public abstract class AbstractDeployProcesses extends AbstractProjectTest {

    private static final String FAILED_REDEPLOY_MESSAGE =
            "Failed to re-deploy the %s process as %s. Reason: Process contains no executables.";
    private static final String FAILED_DEPLOY_MESSAGE_IN_PROJECT_DETAIL_PAGE =
            "Failed to deploy the %s process as %s. Reason: Process contains no executables.";
    private static final String SUCCESSFUL_DEPLOY_MESSAGE_IN_PROJECT_DETAIL_PAGE =
            "Deploy process to project" + "\n" + "%s process has been deployed successfully.";
    private static final String DEPLOY_PROGRESS_MESSAGE_IN_PROJECT_DETAIL_PAGE =
            "Deploy process to project" + "\n" + "Deploying \"%s\" process as %s.";
    private static final String SUCCESSFUL_DEPLOY_MESSAGE_IN_PROJECTS_PAGE =
            "Deploy process to selected projects" + "\n"
                    + "The \"%s\" has been deployed successfully to all selected projects.";
    private static final String FAILED_DEPLOY_DIALOG_MESSAGE_IN_PROJECTS_PAGE =
            "Deploy process to selected projects"
                    + "\n"
                    + "Deployment of the \"%s\" has finished."
                    + " The process has not been deployed successfully into some projects. See the error message";
    private static final String DEPLOY_PROGRESS_MESSAGE_IN_PROJECTS_PAGE =
            "Deploy process to selected projects" + "\n" + "Deploying %s as %s.";
    protected static final String DISC_PROJECTS_PAGE_URL = "admin/disc/#/projects";
    private static final String FAILED_DEPLOY_ERROR_BAR_IN_PROJECTS_PAGE =
            "Failed to deploy the %s process as %s to the projects below. Reasons: Process contains no executables.";
    protected String zipFilePath;

    private List<ProjectInfo> projects;
    private ProjectInfo workingProject;

    protected List<ProjectInfo> getProjects() {
        if (projects == null)
            projects = Arrays.asList(getWorkingProject());
        return projects;
    }

    protected ProjectInfo getWorkingProject() {
        if (workingProject == null)
            workingProject =
                    new ProjectInfo().setProjectName(projectTitle).setProjectId(
                            testParams.getProjectId());
        return workingProject;
    }

    protected void openProjectDetailPage(ProjectInfo project) {
        openUrl(DISC_PROJECTS_PAGE_URL);
        waitForElementVisible(discProjectsList.getRoot());
        discProjectsList.clickOnProjectTitle(project);
        waitForElementVisible(projectDetailPage.getRoot());
    }

    protected void openProjectDetailByUrl(String projectId) {
        openUrl(DISC_PROJECTS_PAGE_URL + "/" + projectId);
        waitForElementVisible(projectDetailPage.getRoot());
    }

    protected void deployInProjectsPage(List<ProjectInfo> projects, DeployPackages deployPackage,
            String processName) {
        openUrl(DISC_PROJECTS_PAGE_URL);
        selectProjectsToDeployInProjectsPage(projects);
        deployForm.deployProcess(zipFilePath + deployPackage.getPackageName(),
                deployPackage.getPackageType(), processName);
        assertDeployedProcessInProjects(processName, projects, deployPackage);
    }

    protected void failedDeployInProjectsPage(List<ProjectInfo> projects,
            DeployPackages deployPackage, ProcessTypes processType, String processName) {
        openUrl(DISC_PROJECTS_PAGE_URL);
        selectProjectsToDeployInProjectsPage(projects);
        deployForm.tryToDeployProcess(zipFilePath + deployPackage.getPackageName(), processType,
                processName);
        String failedDeployError =
                String.format(FAILED_DEPLOY_ERROR_BAR_IN_PROJECTS_PAGE,
                        deployPackage.getPackageName(), processName);
        waitForElementNotPresent(deployForm.getRoot());
        waitForElementVisible(discProjectsList.getRoot());
        System.out.println("Error bar in projects page: "
                + discProjectsList.getErrorBar().getText());
        assertEquals(failedDeployError, discProjectsList.getErrorBar().getText());
    }

    protected String deployInProjectDetailPage(DeployPackages deployPackage, String processName) {
        String processUrl = null;
        waitForElementVisible(projectDetailPage.getRoot());
        projectDetailPage.clickOnDeployProcessButton();
        deployForm.deployProcess(zipFilePath + deployPackage.getPackageName(),
                deployPackage.getPackageType(), processName);
        waitForElementNotPresent(deployForm.getRoot());
        processUrl = browser.getCurrentUrl();
        projectDetailPage.checkFocusedProcess(processName);
        projectDetailPage.assertActiveProcessInList(processName, deployPackage);
        Screenshots.takeScreenshot(browser, "assert-successful-deployed-process-" + processName,
                getClass());
        return processUrl;
    }

    protected void failedDeployInProjectDetailPage(DeployPackages deployPackage,
            ProcessTypes processType, String processName) {
        waitForElementVisible(projectDetailPage.getRoot());
        projectDetailPage.clickOnDeployProcessButton();
        String failedDeployMessage =
                String.format(FAILED_DEPLOY_MESSAGE_IN_PROJECT_DETAIL_PAGE,
                        deployPackage.getPackageName(), processName);
        deployForm.tryToDeployProcess(zipFilePath + deployPackage.getPackageName(), processType,
                processName);
        waitForElementVisible(projectDetailPage.getDeployErrorDialog());
        System.out.println("Error deploy dialog message: "
                + projectDetailPage.getDeployErrorDialog().getText());
        assertEquals(failedDeployMessage, projectDetailPage.getDeployErrorDialog().getText());
        Screenshots.takeScreenshot(browser, "assert-failed-deployed-process-" + processName,
                getClass());
        projectDetailPage.closeDeployErrorDialogButton();
    }

    protected void redeployProcess(String processName, DeployPackages redeployPackage,
            String redeployProcessName, ScheduleBuilder... schedules) {
        waitForElementVisible(projectDetailPage.getRoot());
        projectDetailPage.clickOnRedeployButton(processName);
        deployForm.redeployProcess(zipFilePath + redeployPackage.getPackageName(),
                redeployPackage.getPackageType(), redeployProcessName);
        waitForElementNotPresent(deployForm.getRoot());
        projectDetailPage.checkFocusedProcess(redeployProcessName);
        projectDetailPage
                .assertActiveProcessInList(redeployProcessName, redeployPackage, schedules);
        Screenshots.takeScreenshot(browser, "assert-successful-deployed-process-"
                + redeployProcessName, getClass());
    }

    protected void failedRedeployProcess(String processName, DeployPackages redeployPackage,
            ProcessTypes redeployProcessType, String redeployProcessName) {
        waitForElementVisible(projectDetailPage.getRoot());
        projectDetailPage.clickOnRedeployButton(processName);
        String failedDeployMessage =
                String.format(FAILED_REDEPLOY_MESSAGE, redeployPackage.getPackageName(),
                        redeployProcessName);
        deployForm.tryToDeployProcess(zipFilePath + redeployPackage.getPackageName(),
                redeployProcessType, redeployProcessName);
        waitForElementPresent(projectDetailPage.getDeployErrorDialog());
        assertEquals(failedDeployMessage, projectDetailPage.getDeployErrorDialog().getText());
        Screenshots.takeScreenshot(browser, "assert-failed-redeployed-process-"
                + redeployProcessName, getClass());
        projectDetailPage.closeDeployErrorDialogButton();
    }

    protected void selectProjectsToDeployInProjectsPage(List<ProjectInfo> projects) {
        waitForElementVisible(discProjectsList.getRoot());
        discProjectsList.checkOnProjects(projects);
        assertTrue(discProjectsList.getDeployProcessButton().isEnabled());
        discProjectsList.clickOnDeployProcessButton();
        waitForElementVisible(deployForm.getRoot());
    }

    protected void checkSuccessfulDeployDialogMessageInProjectDetail(DeployPackages deployPackage,
            ProcessTypes processType) {
        checkDeployDialogMessageInProjectDetail(deployPackage, processType, true);
    }

    protected void checkSuccessfulDeployDialogMessageInProjectsPage(List<ProjectInfo> projects,
            DeployPackages deployPackage, ProcessTypes processType) {
        checkDeployDialogMessageInProjectsPage(projects, deployPackage, processType, true);
    }

    protected void checkFailedDeployDialogMessageInProjectsPage(List<ProjectInfo> projects,
            DeployPackages deployPackage, ProcessTypes processType) {
        checkDeployDialogMessageInProjectsPage(projects, deployPackage, processType, false);
    }

    protected void createMultipleProjects(List<ProjectInfo> additionalProjects)
            throws JSONException, InterruptedException {
        for (ProjectInfo project : additionalProjects) {
            openUrl(PAGE_GDC_PROJECTS);
            waitForElementVisible(gpProject.getRoot());
            project.setProjectId(gpProject.createProject(project.getProjectName(),
                    project.getProjectName(), null, testParams.getAuthorizationToken(),
                    testParams.getDwhDriver(), projectCreateCheckIterations));
        }
    }

    protected void deleteProjects(List<ProjectInfo> projectsToDelete) {
        for (ProjectInfo projectToDelete : projectsToDelete) {
            deleteProject(projectToDelete.getProjectId());
        }
    }

    protected void cleanProcessesInProjectDetail(String projectId) {
        openProjectDetailByUrl(projectId);
        browser.navigate().refresh();
        waitForElementVisible(projectDetailPage.getRoot());
        projectDetailPage.deleteAllProcesses();
    }

    private void assertDeployedProcessInProjects(String processName, List<ProjectInfo> projects,
            DeployPackages deployPackage) {
        for (ProjectInfo project : projects) {
            waitForElementVisible(discProjectsList.getRoot());
            discProjectsList.clickOnProjectTitle(project);
            waitForElementVisible(projectDetailPage.getRoot());
            projectDetailPage.assertNewDeployedProcessInList(processName, deployPackage);
            Screenshots.takeScreenshot(browser, "assert-deployed-process-" + processName,
                    getClass());
            discNavigation.clickOnProjectsButton();
        }
    }

    private void checkDeployDialogMessageInProjectsPage(List<ProjectInfo> projects,
            DeployPackages deployPackage, ProcessTypes processType, boolean isSuccessful) {
        openUrl(DISC_PROJECTS_PAGE_URL);
        selectProjectsToDeployInProjectsPage(projects);
        String processName = "Check Deploy Dialog Message";
        String zipFileName = deployPackage.getPackageName();
        final String progressDialogMessage =
                String.format(DEPLOY_PROGRESS_MESSAGE_IN_PROJECTS_PAGE, zipFileName, processName);
        waitForElementVisible(deployForm.getRoot());
        deployForm.setDeployProcessInput(zipFilePath + zipFileName, processType, processName);
        assertFalse(deployForm.inputFileHasError());
        assertFalse(deployForm.inputProcessNameHasError());
        Screenshots.takeScreenshot(browser, "input-fields-deploy-" + processName, getClass());
        deployForm.getDeployConfirmButton().click();

        Graphene.waitGui().until(new Predicate<WebDriver>() {

            @Override
            public boolean apply(WebDriver arg0) {
                return progressDialogMessage.equals(deployForm.getDeployProcessDialog().getText());
            }
        });

        if (isSuccessful) {
            final String successfulDeployMessage =
                    String.format(SUCCESSFUL_DEPLOY_MESSAGE_IN_PROJECTS_PAGE, zipFileName);
            Graphene.waitGui().until(new Predicate<WebDriver>() {

                @Override
                public boolean apply(WebDriver arg0) {
                    return successfulDeployMessage.equals(deployForm.getDeployProcessDialog()
                            .getText());
                }
            });
            waitForElementNotPresent(deployForm.getRoot());
        } else {
            final String failedDeployMessage =
                    String.format(FAILED_DEPLOY_DIALOG_MESSAGE_IN_PROJECTS_PAGE, zipFileName);
            Graphene.waitGui().until(new Predicate<WebDriver>() {

                @Override
                public boolean apply(WebDriver arg0) {
                    return failedDeployMessage
                            .equals(deployForm.getDeployProcessDialog().getText());
                }
            });
        }
    }

    private void checkDeployDialogMessageInProjectDetail(DeployPackages deployPackage,
            ProcessTypes processType, boolean isSuccessful) {
        projectDetailPage.clickOnDeployProcessButton();
        String processName = "Check Deploy Dialog Message";
        String zipFileName = deployPackage.getPackageName();
        final String expectedProgressDialogMessage =
                String.format(DEPLOY_PROGRESS_MESSAGE_IN_PROJECT_DETAIL_PAGE, zipFileName,
                        processName);
        waitForElementVisible(deployForm.getRoot());
        deployForm.setDeployProcessInput(zipFilePath + zipFileName, processType, processName);
        assertFalse(deployForm.inputFileHasError());
        assertFalse(deployForm.inputProcessNameHasError());
        deployForm.getDeployConfirmButton().click();

        Graphene.waitGui().until(new Predicate<WebDriver>() {

            @Override
            public boolean apply(WebDriver arg0) {
                return expectedProgressDialogMessage.equals(deployForm.getDeployProcessDialog()
                        .getText());
            }
        });

        if (!isSuccessful)
            return;

        final String successfulDeployMessage =
                String.format(SUCCESSFUL_DEPLOY_MESSAGE_IN_PROJECT_DETAIL_PAGE, processName);
        Graphene.waitGui().until(new Predicate<WebDriver>() {

            @Override
            public boolean apply(WebDriver arg0) {
                return successfulDeployMessage
                        .equals(deployForm.getDeployProcessDialog().getText());
            }
        });
        waitForElementNotPresent(deployForm.getRoot());
    }
}
