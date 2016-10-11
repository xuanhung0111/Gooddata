package com.gooddata.qa.graphene.disc;

import static com.gooddata.qa.graphene.enums.ResourceDirectory.ZIP_FILES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static org.openqa.selenium.By.className;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.disc.DeployPackages;
import com.gooddata.qa.graphene.enums.disc.ProcessTypes;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.google.common.base.Predicate;

public class DeployProcessTest extends AbstractDISCTest {

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

    @BeforeClass
    public void initProperties() {
        projectTitle = "Disc-test-deploy-process";
    }

    @Test(dependsOnGroups = {"createProject"})
    public void deployRubyInProjectsPage() {
        try {
            deployInProjectsPage(DeployPackages.RUBY, "Ruby - Projects List Page",
                    testParams.getProjectId());
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void deployRubyInProjectDetailPage() {
        try {
            openProjectDetailPage(testParams.getProjectId());
            deployInProjectDetailPage(DeployPackages.RUBY, "Ruby - Project Detail Page");
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void redeployProcessWithDifferentProcessType() {
        try {
            openProjectDetailPage(testParams.getProjectId());
            String processName = "Redeploy process with different process type";
            deployInProjectDetailPage(DeployPackages.EXECUTABLES_GRAPH, processName);
            redeployProcess(processName, DeployPackages.EXECUTABLES_RUBY, processName);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void redeployProcessWithSamePackage() {
        try {
            openProjectDetailPage(testParams.getProjectId());
            String processName = "Deploy process";
            deployInProjectDetailPage(DeployPackages.EXECUTABLES_GRAPH, processName);
            String newProcessName = "Redeploy process with the same package";
            redeployProcess(processName, DeployPackages.EXECUTABLES_GRAPH, newProcessName);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void emptyInputErrorDeployment() {
        openUrl(DISC_PROJECTS_PAGE_URL);
        selectProjectsToDeployInProjectsPage(testParams.getProjectId());
        deployForm.tryToDeployProcess("", ProcessTypes.DEFAULT, "");
        assertTrue(deployForm.inputFileHasError(), "Error is not shown for file input!");
        assertTrue(deployForm.isCorrectInvalidPackageError(), "Incorrect package validation error!");
        assertTrue(deployForm.inputProcessNameHasError(), "Error is not shown for process name input!");
        assertTrue(deployForm.isCorrectInvalidProcessNameError(), "Incorrect process name validation error!");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void notZipFileErrorDeployment() {
        openUrl(DISC_PROJECTS_PAGE_URL);
        selectProjectsToDeployInProjectsPage(testParams.getProjectId());

        String filePath = getFilePathFromResource("/" + ZIP_FILES + "/not-zip-file.7z");
        deployForm.tryToDeployProcess(filePath, ProcessTypes.DEFAULT, "Not zip file");
        assertTrue(deployForm.inputFileHasError(), "Error is not shown for file input!");
        assertTrue(deployForm.isCorrectInvalidPackageError(), "Incorrect package validation error!");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void tooLargeZipFileErrorDeployment() {
        openUrl(DISC_PROJECTS_PAGE_URL);
        selectProjectsToDeployInProjectsPage(testParams.getProjectId());

        String filePath = getFilePathFromResource("/" + ZIP_FILES + "/too-large-file.zip");
        deployForm.tryToDeployProcess(filePath, ProcessTypes.DEFAULT, "Too large file");
        assertTrue(deployForm.inputFileHasError(), "Error is not shown for file input!");
        assertTrue(deployForm.isCorrectInvalidPackageError(), "Incorrect package validation error!");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void deployWithoutExecutablesInProjectsPage() {
        failedDeployInProjectsPage(DeployPackages.NOT_EXECUTABLE, ProcessTypes.DEFAULT,
                "Not Executables", testParams.getProjectId());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void deployCloudConnectWithRubyTypeInProjectsPage() {
        failedDeployInProjectsPage(DeployPackages.CLOUDCONNECT, ProcessTypes.RUBY,
                "CloudConnect with Ruby type", testParams.getProjectId());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void deployRubyWithCloudConnectTypeInProjectsPage() {
        failedDeployInProjectsPage(DeployPackages.RUBY, ProcessTypes.GRAPH,
                "Ruby with CloudConnect type", testParams.getProjectId());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void deployWithoutExecutablesInProjectDetailPage() {
        openProjectDetailPage(testParams.getProjectId());
        failedDeployInProjectDetailPage(DeployPackages.NOT_EXECUTABLE, ProcessTypes.DEFAULT, "Not Executable");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void deployCloudConnectWithRubyTypeInProjectDetailPage() {
        openProjectDetailPage(testParams.getProjectId());
        failedDeployInProjectDetailPage(DeployPackages.CLOUDCONNECT, ProcessTypes.RUBY,
                "Deploy CloudConnect package with ruby type");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void deployRubyWithCloudConnectTypeInProjectDetailPage() {
        openProjectDetailPage(testParams.getProjectId());
        failedDeployInProjectDetailPage(DeployPackages.RUBY, ProcessTypes.GRAPH,
                "Deploy Ruby package with graph type");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void redeployWithoutExecutables() {
        try {
            openProjectDetailPage(testParams.getProjectId());
            String processName = "Redeploy process without executables";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);
            failedRedeployProcess(processName, DeployPackages.NOT_EXECUTABLE, ProcessTypes.GRAPH, processName);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void redeployCloudConnectWithRubyType() {
        try {
            openProjectDetailPage(testParams.getProjectId());
            String processName = "Redeploy CloudConnect process with Ruby type";
            deployInProjectDetailPage(DeployPackages.CLOUDCONNECT, processName);
            failedRedeployProcess(processName, DeployPackages.CLOUDCONNECT, ProcessTypes.RUBY, processName);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void redeployRubyWithCloudConnectType() {
        try {
            openProjectDetailPage(testParams.getProjectId());
            String processName = "Redploy Ruby process with Graph type";
            deployInProjectDetailPage(DeployPackages.RUBY, processName);
            failedRedeployProcess(processName, DeployPackages.RUBY, ProcessTypes.GRAPH, processName);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    /*
     * The tests for checking deploy dialog message are disabled until MSF-7601, MSF-6156 are
     * considered by MSF team
     */
    @Test(enabled = false, dependsOnGroups = {"createProject"})
    public void checkDeployDialogMessageInProjectDetail() {
        try {
            openProjectDetailPage(testParams.getProjectId());
            checkSuccessfulDeployDialogMessageInProjectDetail(DeployPackages.BASIC, ProcessTypes.GRAPH);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(enabled = false, dependsOnGroups = {"createProject"})
    public void checkFailedDeployMessageInProjectDetail() {
        try {
            openProjectDetailPage(testParams.getProjectId());
            checkFailedDeployDialogMessageInProjectDetail(DeployPackages.BASIC, ProcessTypes.RUBY);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(enabled = false, dependsOnGroups = {"createProject"})
    public void checkDeployDialogMessageInProjectsPage() {
        try {
            checkSuccessfulDeployDialogMessageInProjectsPage(DeployPackages.BASIC, ProcessTypes.GRAPH, testParams.getProjectId());
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @Test(enabled = false, dependsOnGroups = {"createProject"})
    public void checkFailedDeployMessageInProjectsPage() {
        try {
            checkFailedDeployDialogMessageInProjectsPage(DeployPackages.BASIC, ProcessTypes.RUBY, testParams.getProjectId());
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    @DataProvider(name = "invalidGitStorePath")
    public Object[][] invalidGitStorePath() {
        return new Object[][] {
            {""},
            {"           "},
            {"!@#$%^&*()'\"~"},
            {"C:\\Testdata\\abc"},
            {"${PUBLIC_APPSTORE}:branch"}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "invalidGitStorePath")
    public void cannotDeployRubyGitStoreProcessWithInvalidFormat(String gitPath) {
        openProjectDetailPage(testParams.getProjectId());
        String processName = "Cannot deploy process";
        waitForFragmentVisible(projectDetailPage).clickOnDeployProcessButton();
        deployForm.deployProcess(gitPath, processName);
        assertTrue(deployForm.isGitStoreError(), "Error is not shown in git store path");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void cannotDeployRubySelfServiceProcessInCaseMissingInfoJson() {
        String gitPath = "${PUBLIC_APPSTORE}:branch/prodigy-testing:/abc/doesnotexist";
        openProjectDetailPage(testParams.getProjectId());
        String processName = "Cannot deploy process";
        waitForFragmentVisible(projectDetailPage).clickOnDeployProcessButton();
        deployForm.deployProcess(gitPath, processName);
        assertEquals(waitForElementVisible(className("dialog-title"), browser).getText().trim(),
                "Process failed to deploy");
        //TODO: this failed message will be udpated after the bug MSF-11118 is fixed 
        assertEquals(waitForElementVisible(className("dialog-body"), browser).getText().trim(),
                "Failed to deploy the process as " + processName +
                ". Reason: Deployment failed on internal error, error_messsage: Info.json is missing in root application directory .");
        waitForElementVisible(className("s-btn-ok"), browser).click();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void cannotDeployRubySelfServiceProcessWithRelativePath() {
        String gitPath = "${PUBLIC_APPSTORE}:branch/prodigy-testing:/../ReadFile";
        openProjectDetailPage(testParams.getProjectId());
        String processName = "Cannot deploy process";
        waitForFragmentVisible(projectDetailPage).clickOnDeployProcessButton();
        deployForm.deployProcess(gitPath, processName);
        assertEquals(waitForElementVisible(className("dialog-title"), browser).getText().trim(),
                "Process failed to deploy");
        assertEquals(waitForElementVisible(className("dialog-body"), browser).getText().trim(),
                "Failed to deploy the process as " + processName + ". Reason: Invalid path. Deployment path " +
                        gitPath + " cannot contain relative references..");
        waitForElementVisible(className("s-btn-ok"), browser).click();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void cannotAccessGitPrivateStoreOfAnotherDomain() {
        String gitRubyPath = "${PRIVATE_APPSTORE}:branch/prodigy-testing:/vietnam/giraffes/ReadFile";
        openProjectDetailPage(testParams.getProjectId());
        String processName = "Cannot deploy process";
        waitForFragmentVisible(projectDetailPage).clickOnDeployProcessButton();
        deployForm.deployProcess(gitRubyPath, processName);
        assertEquals(waitForElementVisible(className("dialog-title"), browser).getText().trim(),
                "Process failed to deploy");
        //TODO: this failed message will be udpated after the bug MSF-11120 is fixed
        assertEquals(waitForElementVisible(className("dialog-body"), browser).getText().trim(),
                "Failed to deploy the process as " + processName + ". Reason: Invalid path. Deployment path " +
                        "/vietnam/giraffes/ReadFile/" + " not allowed for this domain.");
        waitForElementVisible(className("s-btn-ok"), browser).click();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkFailedMessageOnDeployWrongGitOnProjectsPage() {
        String gitRubyPath = "${PUBLIC_APPSTORE}:branch/prodigy-testing:/../ReadFile";
        String processName = "Check Failed Git Deploy Message In Projects Page";

        initDISCProjectsPage();
        waitForFragmentVisible(discProjectsList).checkOnProjects(testParams.getProjectId())
            .clickOnDeployProcessButton();
        waitForFragmentVisible(deployForm).deployProcess(gitRubyPath, processName);
        waitForFragmentNotVisible(deployForm);
        assertEquals(waitForElementVisible(className("error-bar"), browser).getText().trim(),
                "Failed to deploy git process to the projects below. Reasons: Invalid path. Deployment path " +
                        gitRubyPath + " cannot contain relative references..\nPlease resolve the issue and try "
                                + "again.\nDismiss");
        waitForElementVisible(className("s-btn-dismiss"), browser).click();
    }

    private void failedDeployInProjectsPage(DeployPackages deployPackage, ProcessTypes processType,
            String processName, String... projectIds) {
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

    private void failedDeployInProjectDetailPage(DeployPackages deployPackage, ProcessTypes processType,
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

    private void failedRedeployProcess(String processName, DeployPackages redeployPackage,
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

    private void checkSuccessfulDeployDialogMessageInProjectDetail(DeployPackages deployPackage,
            ProcessTypes processType) {
        checkDeployDialogMessageInProjectDetail(deployPackage, processType, true);
    }

    private void checkFailedDeployDialogMessageInProjectDetail(DeployPackages deployPackage,
            ProcessTypes processType) {
        checkDeployDialogMessageInProjectDetail(deployPackage, processType, false);
    }

    private void checkSuccessfulDeployDialogMessageInProjectsPage(DeployPackages deployPackage,
            ProcessTypes processType, String... projectIds) {
        checkDeployDialogMessageInProjectsPage(deployPackage, processType, true, projectIds);
    }

    private void checkFailedDeployDialogMessageInProjectsPage(DeployPackages deployPackage,
            ProcessTypes processType, String... projectIds) {
        checkDeployDialogMessageInProjectsPage(deployPackage, processType, false, projectIds);
    }

    private void checkDeployDialogMessageInProjectsPage(DeployPackages deployPackage, ProcessTypes processType,
            boolean isSuccessful, String... projectIds) {
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
