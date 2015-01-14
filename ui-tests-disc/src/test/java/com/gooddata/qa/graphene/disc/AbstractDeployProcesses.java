package com.gooddata.qa.graphene.disc;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebDriver;

import com.gooddata.qa.graphene.entity.disc.ProjectInfo;
import com.gooddata.qa.graphene.enums.disc.DeployPackages;
import com.gooddata.qa.graphene.enums.disc.ProcessTypes;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.google.common.base.Predicate;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

public abstract class AbstractDeployProcesses extends AbstractDISCTest {

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
    private static final String FAILED_DEPLOY_ERROR_BAR_IN_PROJECTS_PAGE =
            "Failed to deploy the %s process as %s to the projects below. Reasons: Process contains no executables.";

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
