package com.gooddata.qa.graphene.disc;

import static com.gooddata.qa.graphene.enums.ResourceDirectory.ZIP_FILES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import com.gooddata.qa.graphene.enums.disc.DeployPackages;
import com.gooddata.qa.graphene.enums.disc.ProcessTypes;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.google.common.base.Predicate;

public abstract class AbstractDeployProcessTest extends AbstractDISCTest {

    private static final String FAILED_REDEPLOY_MESSAGE =
            "Failed to re-deploy the \"%s\" process as \"%s\". Reason: Process contains no executables.";
    private static final String FAILED_DEPLOY_MESSAGE_IN_PROJECT_DETAIL_PAGE =
            "Failed to deploy the %s process as %s. Reason: Process contains no executables.";
    private static final String SUCCESSFUL_DEPLOY_MESSAGE_IN_PROJECT_DETAIL_PAGE = "Deploy process to project"
            + "\n" + "%s process has been deployed successfully.";
    private static final String DEPLOY_PROGRESS_MESSAGE_IN_PROJECT_DETAIL_PAGE = "Deploy process to project"
            + "\n" + "Deploying \"%s\" process as %s.";
    private static final String SUCCESSFUL_DEPLOY_MESSAGE_IN_PROJECTS_PAGE = "Deploy process to selected projects"
            + "\n" + "The \"%s\" has been deployed successfully to all selected projects.";
    private static final String FAILED_DEPLOY_DIALOG_MESSAGE_IN_PROJECTS_PAGE =
            "Deploy process to selected projects" + "\n" + "Deployment of the \"%s\" has finished."
                    + " The process has not been deployed successfully into some projects. See the error message";
    private static final String DEPLOY_PROGRESS_MESSAGE_IN_PROJECTS_PAGE = "Deploy process to selected projects"
            + "\n" + "Deploying %s as %s.";
    private static final String FAILED_DEPLOY_ERROR_BAR_IN_PROJECTS_PAGE =
            "Failed to deploy the %s process as %s to the projects below. Reasons: Process contains no executables.";

    protected void failedDeployInProjectsPage(List<String> projectIds, DeployPackages deployPackage,
            ProcessTypes processType, String processName) {
        openUrl(DISC_PROJECTS_PAGE_URL);
        selectProjectsToDeployInProjectsPage(projectIds);

        String filePath = getFilePathFromResource("/" + ZIP_FILES + "/" + deployPackage.getPackageName());
        deployForm.tryToDeployProcess(filePath, processType, processName);
        String failedDeployError =
                String.format(FAILED_DEPLOY_ERROR_BAR_IN_PROJECTS_PAGE, deployPackage.getPackageName(),
                        processName);
        waitForFragmentNotVisible(deployForm);
        waitForFragmentVisible(discProjectsList);
        System.out.println("Error bar in projects page: " + discProjectsList.getErrorBar().getText());
        assertEquals(discProjectsList.getErrorBar().getText(), failedDeployError,
                "Incorrect failed deploy error message!");
    }

    protected void failedDeployInProjectDetailPage(DeployPackages deployPackage, ProcessTypes processType,
            String processName) {
        waitForFragmentVisible(projectDetailPage);
        projectDetailPage.clickOnDeployProcessButton();
        String failedDeployMessage =
                String.format(FAILED_DEPLOY_MESSAGE_IN_PROJECT_DETAIL_PAGE, deployPackage.getPackageName(),
                        processName);

        String filePath = getFilePathFromResource("/" + ZIP_FILES + "/" + deployPackage.getPackageName());
        deployForm.tryToDeployProcess(filePath, processType, processName);
        waitForElementVisible(projectDetailPage.getDeployErrorDialog());
        System.out.println("Error deploy dialog message: " + projectDetailPage.getDeployErrorDialog().getText());
        assertEquals(projectDetailPage.getDeployErrorDialog().getText(), failedDeployMessage,
                "Incorrect failed deploy error message!");
        Screenshots.takeScreenshot(browser, "assert-failed-deployed-process-" + processName, getClass());
        projectDetailPage.closeDeployErrorDialogButton();
    }

    protected void failedRedeployProcess(String processName, DeployPackages redeployPackage,
            ProcessTypes redeployProcessType, String redeployProcessName) {
        waitForFragmentVisible(projectDetailPage);
        projectDetailPage.activeProcess(processName).clickOnRedeployButton();
        String failedDeployMessage =
                String.format(FAILED_REDEPLOY_MESSAGE, redeployPackage.getPackageName(), redeployProcessName);

        String filePath = getFilePathFromResource("/" + ZIP_FILES + "/" + redeployPackage.getPackageName());
        deployForm.tryToDeployProcess(filePath, redeployProcessType, redeployProcessName);
        waitForElementPresent(projectDetailPage.getDeployErrorDialog());
        assertEquals(projectDetailPage.getDeployErrorDialog().getText(), failedDeployMessage,
                "Incorrect failed re-deploy error message!");
        Screenshots.takeScreenshot(browser, "assert-failed-redeployed-process-" + redeployProcessName, getClass());
        projectDetailPage.closeDeployErrorDialogButton();
    }

    protected void checkSuccessfulDeployDialogMessageInProjectDetail(DeployPackages deployPackage,
            ProcessTypes processType) {
        checkDeployDialogMessageInProjectDetail(deployPackage, processType, true);
    }

    protected void checkFailedDeployDialogMessageInProjectDetail(DeployPackages deployPackage,
            ProcessTypes processType) {
        checkDeployDialogMessageInProjectDetail(deployPackage, processType, false);
    }

    protected void checkSuccessfulDeployDialogMessageInProjectsPage(List<String> projectIds,
            DeployPackages deployPackage, ProcessTypes processType) {
        checkDeployDialogMessageInProjectsPage(projectIds, deployPackage, processType, true);
    }

    protected void checkFailedDeployDialogMessageInProjectsPage(List<String> projectIds,
            DeployPackages deployPackage, ProcessTypes processType) {
        checkDeployDialogMessageInProjectsPage(projectIds, deployPackage, processType, false);
    }

    private void checkDeployDialogMessageInProjectsPage(List<String> projectIds, DeployPackages deployPackage,
            ProcessTypes processType, boolean isSuccessful) {
        openUrl(DISC_PROJECTS_PAGE_URL);
        selectProjectsToDeployInProjectsPage(projectIds);
        String processName = "Check Deploy Dialog Message";
        String zipFileName = deployPackage.getPackageName();
        final String progressDialogMessage =
                String.format(DEPLOY_PROGRESS_MESSAGE_IN_PROJECTS_PAGE, zipFileName, processName);
        waitForFragmentVisible(deployForm);

        String filePath = getFilePathFromResource("/" + ZIP_FILES + "/" + zipFileName);
        checkDeployProgress(filePath, processType, processName, progressDialogMessage);

        if (isSuccessful) {
            final String successfulDeployMessage =
                    String.format(SUCCESSFUL_DEPLOY_MESSAGE_IN_PROJECTS_PAGE, zipFileName);
            Predicate<WebDriver> successfulDeployMessageDisplayed = 
                    browser -> successfulDeployMessage.equals(deployForm.getDeployProcessDialog().getText());
            Graphene.waitGui().until(successfulDeployMessageDisplayed);
            waitForFragmentNotVisible(deployForm);
        } else {
            final String failedDeployMessage =
                    String.format(FAILED_DEPLOY_DIALOG_MESSAGE_IN_PROJECTS_PAGE, zipFileName);
            Predicate<WebDriver> failedDeployMessageDisplayed = 
                    browser -> failedDeployMessage.equals(deployForm.getDeployProcessDialog().getText());
            Graphene.waitGui().until(failedDeployMessageDisplayed);
        }
    }

    private void checkDeployDialogMessageInProjectDetail(DeployPackages deployPackage, ProcessTypes processType,
            boolean isSuccessful) {
        projectDetailPage.clickOnDeployProcessButton();
        String processName = "Check Deploy Dialog Message";
        String zipFileName = deployPackage.getPackageName();
        final String expectedProgressDialogMessage =
                String.format(DEPLOY_PROGRESS_MESSAGE_IN_PROJECT_DETAIL_PAGE, zipFileName, processName);
        waitForFragmentVisible(deployForm);
        String filePath = getFilePathFromResource("/" + ZIP_FILES + "/" + zipFileName);
        checkDeployProgress(filePath, processType, processName, expectedProgressDialogMessage);

        if (!isSuccessful)
            return;

        final String successfulDeployMessage =
                String.format(SUCCESSFUL_DEPLOY_MESSAGE_IN_PROJECT_DETAIL_PAGE, processName);
        Predicate<WebDriver> successfulDeployMessageDisplayed = 
                browser -> successfulDeployMessage.equals(deployForm.getDeployProcessDialog().getText());
        Graphene.waitGui().until(successfulDeployMessageDisplayed);
        waitForFragmentNotVisible(deployForm);
    }

    private void checkDeployProgress(String zipFile, ProcessTypes processType, String processName,
            final String progressDialogMessage) {
        waitForFragmentVisible(deployForm);
        deployForm.setDeployProcessInput(zipFile, processType, processName);
        assertFalse(deployForm.inputFileHasError(), "Error is shown for input file!");
        assertFalse(deployForm.inputProcessNameHasError(), "Error is showns for process name input!");
        deployForm.getDeployConfirmButton().click();
        try {
            assertEquals(deployForm.getDeployProcessDialog().getText(), progressDialogMessage,
                    "Incorrect deploy progress message on dialog!");
        } catch (NoSuchElementException e) {
            System.out.println("WARNING: Cannot get deploy progress message!");
        }
    }
}
