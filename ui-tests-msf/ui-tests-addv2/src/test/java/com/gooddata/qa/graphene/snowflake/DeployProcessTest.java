package com.gooddata.qa.graphene.snowflake;

import static com.gooddata.qa.graphene.AbstractTest.Profile.VIEWER;
import static com.gooddata.qa.graphene.utils.ElementUtils.getBubbleMessage;
import static com.gooddata.qa.utils.cloudresources.DataSourceRestRequest.DATA_SOURCE_REST_URI;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.gooddata.sdk.model.dataload.processes.DataloadProcess;
import com.gooddata.sdk.service.dataload.processes.ProcessService;
import com.gooddata.qa.graphene.AbstractADDProcessTest;
import com.gooddata.qa.graphene.enums.project.DeleteMode;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm;
import com.gooddata.qa.graphene.fragments.disc.process.DeploySDDProcessDialog;
import com.gooddata.qa.graphene.fragments.disc.process.DeploySDDProcessDialog.Scope;
import com.gooddata.qa.graphene.fragments.disc.projects.ProjectDetailPage;
import com.gooddata.qa.utils.cloudresources.DataSourceRestRequest;
import com.gooddata.qa.utils.cloudresources.DataSourceUtils;
import com.gooddata.qa.utils.cloudresources.DatabaseType;
import com.gooddata.qa.utils.datasource.DataDistributionProcess;

import net.snowflake.client.jdbc.DBMetadataResultSetMetadata;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.IOException;

public class DeployProcessTest extends AbstractADDProcessTest {

    private static final String SUPPORT_URI =
            "https://help.gooddata.com/display/doc/Data+Preparation+and+Distribution#DataPreparationandDistribution-Bricks";
    private static final String DATA_BASE = "Database";
    private static final String ANOTHER_DATA_BASE = "Another database";
    private String firstSegmentID;
    private String secondSegmentID;
    private DataSourceUtils domainDataSourceUtils;
    private DataSourceUtils dataSourceUtils;
    private DataSourceRestRequest dataSourceRestRequest;

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
        createAndAddUserToProject(UserRoles.VIEWER);
    }

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        String firstClientProjectId = createNewEmptyProject(domainRestClient, "First client project");
        String secondClientProjectId = createNewEmptyProject(domainRestClient, "Second client project");
        firstSegmentID = createSegment("att_client_1st" + generateHashString(), firstClientProjectId);
        secondSegmentID = createSegment("att_client_2nd" + generateHashString(), secondClientProjectId);
        domainDataSourceUtils = new DataSourceUtils(testParams.getDomainUser());
        dataSourceUtils = new DataSourceUtils(testParams.getUser());
        dataSourceRestRequest = new DataSourceRestRequest(getAdminRestClient(), testParams.getProjectId());
    }

    @Test(dependsOnGroups = "createProject")
    public void checkUIForDeployDialogWithAdminUser() throws IOException {
        String prefix = generateHashString();
        String specialDataSourceTitle = "♔ ♕ ♖ ♗ ♘ ♙ ♚ ♛ ♜ ♝ ♞ ♟" + prefix;
        String dataSourceTitle = generateDataSourceTitle();
        new DataSourceUtils(testParams.getEditorUser()).createDefaultDataSource(dataSourceTitle, DATA_BASE, DatabaseType.SNOWFLAKE);
        String specialDataSourceID =
                new DataSourceUtils(testParams.getUser()).createDefaultDataSource(specialDataSourceTitle, ANOTHER_DATA_BASE, DatabaseType.SNOWFLAKE);
        try {
            ProjectDetailPage projectDetailPage = initDiscProjectDetailPage();
            DeployProcessForm deployForm = projectDetailPage.clickDeployButton();
            assertTrue(projectDetailPage.isDeployProcessFormVisible(), "Deploy Process Form should be displayed");
            assertEquals(deployForm.getRedirectedPageFromLearnMore(), SUPPORT_URI);
            DeploySDDProcessDialog deploySDDProcessDialog = deployForm.selectADDProcess();
            deploySDDProcessDialog.selectDataSource("_______________________" + prefix); // get css from specialTitle
            assertEquals(deploySDDProcessDialog.getSelectedDataSourceName(), specialDataSourceTitle);
            assertFalse(dataSourceRestRequest.getAllDataSourceNames().contains(dataSourceTitle), "Editor's Data source shouldn't display");
        } finally {
            dataSourceRestRequest.deleteDataSource(specialDataSourceID);
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void checkUIForDeployDialogWithDomainUser() throws IOException {
        String prefix = generateHashString();
        String specialDataSourceTitle = "<h1 style=\"color:red\">This is dataSource</h1>" + prefix;
        String dataSourceID = domainDataSourceUtils.createDefaultDataSource(specialDataSourceTitle, DATA_BASE, DatabaseType.SNOWFLAKE);
        logout();
        signInAtUI(testParams.getDomainUser(), testParams.getPassword());
        try {
            ProjectDetailPage projectDetailPage = initDiscProjectDetailPage();
            DeployProcessForm deployForm = projectDetailPage.clickDeployButton();
            assertTrue(projectDetailPage.isDeployProcessFormVisible(), "Deploy Process Form should be displayed");
            assertEquals(deployForm.getRedirectedPageFromLearnMore(), SUPPORT_URI);
            DeploySDDProcessDialog deploySDDProcessDialog = deployForm.selectADDProcess();
            deploySDDProcessDialog.selectDataSource("_h1_style__color_red__this_is_datasource__h1_" + prefix);
            assertEquals(deploySDDProcessDialog.getSelectedDataSourceName(), specialDataSourceTitle);
            deploySDDProcessDialog.selectScope(DeploySDDProcessDialog.Scope.SEGMENT).selectSegment(firstSegmentID);
            assertTrue(deploySDDProcessDialog.isSelectedSegment(firstSegmentID), "Cannot change segment");
        } finally {
            domainDataSourceUtils.getDataSourceRestRequest().deleteDataSource(dataSourceID);
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void checkUIForDeployDialogWithoutDataSource() {
        String workingProject = testParams.getProjectId();
        testParams.setProjectId(createNewEmptyProject(getProfile(VIEWER), "Empty project"));
        logoutAndLoginAs(true, UserRoles.VIEWER);
        try {
            ProjectDetailPage projectDetailPage = initDiscProjectDetailPage();
            DeployProcessForm deployForm = projectDetailPage.clickDeployButton();
            DeploySDDProcessDialog deploySDDProcessDialog = deployForm.selectADDProcess();
            assertEquals(deploySDDProcessDialog.getSelectedDataSourceName(), "No data source found");
            assertTrue(deployForm.enterProcessName("Process Name").isSubmitButtonDisabled(),
                    "Should disable deploy button by missing data source");
        } finally {
            testParams.setProjectId(workingProject);
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void deployNewProcessADDWithSegmentMode() throws IOException {
        String firstDataSource = generateDataSourceTitle();
        String secondDataSource = generateDataSourceTitle();
        String processName = "This process has special character @#$%^&*";
        String firstDataSourceID = domainDataSourceUtils.createDefaultDataSource(firstDataSource, DATA_BASE, DatabaseType.SNOWFLAKE);
        String secondDataSourceID = domainDataSourceUtils.createDefaultDataSource(secondDataSource, DATA_BASE, DatabaseType.SNOWFLAKE);
        logout();
        signInAtUI(testParams.getDomainUser(), testParams.getPassword());
        try {
            ProjectDetailPage projectDetailPage = initDiscProjectDetailPage();
            DeployProcessForm deployForm = projectDetailPage.clickDeployButton();
            DeploySDDProcessDialog deploySDDProcessDialog = deployForm.selectADDProcess()
                    .selectDataSource(firstDataSource)
                    .selectScope(Scope.SEGMENT)
                    .selectSegment(secondSegmentID)
                    .selectSegment(firstSegmentID);
            assertTrue(deploySDDProcessDialog.isSelectedSegment(firstSegmentID), "Cannot change segment");

            deployForm.clickSubmitButton();
            assertTrue(deployForm.isProcessNameInputError(), "Process name input not show error");
            assertEquals(getBubbleMessage(browser), "Process Name cannot be empty.");
            deployForm.enterProcessName(processName).submit();
            takeScreenshot(browser, "Process-deployed-with-current-project-mode-successfully", getClass());
            assertTrue(initDiscProjectDetailPage().hasProcess(processName), "Process is not deployed");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
            ProcessService processService = domainRestClient.getProcessService();
            processService.removeProcess(processService
                    .listProcesses(domainRestClient.getProjectService().getProjectById(testParams.getProjectId())).iterator().next());
            domainDataSourceUtils.getDataSourceRestRequest().deleteDataSource(firstDataSourceID);
            domainDataSourceUtils.getDataSourceRestRequest().deleteDataSource(secondDataSourceID);
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void deployNewProcessADDWithCurrentProjectMode() throws IOException {
        String firstDataSource = generateDataSourceTitle();
        String secondDataSource = generateDataSourceTitle();
        String processName = "<h1 style=\"color:red\">This is process</h1>";
        String firstDataSourceID = dataSourceUtils.createDefaultDataSource(firstDataSource, DATA_BASE, DatabaseType.SNOWFLAKE);
        String secondDataSourceID = dataSourceUtils.createDefaultDataSource(secondDataSource, DATA_BASE, DatabaseType.SNOWFLAKE);
        try {
            ProjectDetailPage projectDetailPage = initDiscProjectDetailPage();
            DeployProcessForm deployForm = projectDetailPage.clickDeployButton();
            deployForm.selectADDProcess()
                    .selectDataSource(firstDataSource)
                    .selectScope(Scope.CURRENT_PROJECT);
            deployForm.clickSubmitButton();
            assertTrue(deployForm.isProcessNameInputError(), "Process name input not show error");
            assertEquals(getBubbleMessage(browser), "Process Name cannot be empty.");
            deployForm.enterProcessName(processName).submit();
            takeScreenshot(browser, "Process-deployed-with-current-project-mode-successfully", getClass());
            assertTrue(initDiscProjectDetailPage().hasProcess(processName), "Process is not deployed");
        } finally {
            ProcessService processService = getProcessService();
            processService.removeProcess(processService.listProcesses(getProject()).iterator().next());
            dataSourceUtils.getDataSourceRestRequest().deleteDataSource(firstDataSourceID);
            dataSourceUtils.getDataSourceRestRequest().deleteDataSource(secondDataSourceID);
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void redeployProcessWithCurrentProjectMode() throws IOException {
        String dataSource = generateDataSourceTitle();
        String processName = generateProcessName();
        String otherProcessName = generateProcessName();
        String dataSourceID = dataSourceUtils.createDefaultDataSource(dataSource, DATA_BASE, DatabaseType.SNOWFLAKE);
        DataloadProcess dataloadProcess =
                getProcessService().createProcess(getProject(), new DataDistributionProcess(processName, dataSourceID, "1"));
        try {
            DeployProcessForm deployProcessForm = initDiscProjectDetailPage().getProcess(processName).clickRedeployButton();
            deployProcessForm.enterProcessName("").clickSubmitButton();
            assertEquals(getBubbleMessage(browser), "Process Name cannot be empty.");

            deployProcessForm.enterProcessName(otherProcessName).submit();
            ProjectDetailPage projectDetailPage = initDiscProjectDetailPage();
            assertTrue(projectDetailPage.hasProcess(otherProcessName), "Process should re-deployed with new name");
            assertFalse(projectDetailPage.hasProcess(processName), "Old process should remove");
        } finally {
            getProcessService().removeProcess(dataloadProcess);
            dataSourceUtils.getDataSourceRestRequest().deleteDataSource(dataSourceID);
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void redeployProcessWithSegmentMode() throws IOException {
        String dataSource = generateDataSourceTitle();
        String processName = generateProcessName();
        String otherProcessName = generateProcessName();
        String dataSourceID = domainDataSourceUtils.createDefaultDataSource(dataSource, DATA_BASE, DatabaseType.SNOWFLAKE);
        DataloadProcess dataloadProcess = domainRestClient.getProcessService().createProcess(
                domainRestClient.getProjectService().getProjectById(testParams.getProjectId()),
                new DataDistributionProcess(processName, dataSourceID,
                        String.format("/gdc/domains/%s/dataproducts/att_lcm_default_data_product/segments/%s", testParams.getUserDomain(), firstSegmentID), "1"));
        logout();
        signInAtUI(testParams.getDomainUser(), testParams.getPassword());
        try {
            DeployProcessForm deployProcessForm = initDiscProjectDetailPage().getProcess(processName).clickRedeployButton();
            deployProcessForm.enterProcessName("").clickSubmitButton();
            assertEquals(getBubbleMessage(browser), "Process Name cannot be empty.");

            deployProcessForm.enterProcessName(otherProcessName).submit();
            ProjectDetailPage projectDetailPage = initDiscProjectDetailPage();
            assertFalse(projectDetailPage.hasProcess(processName), "Old process should remove");
            assertTrue(projectDetailPage.hasProcess(otherProcessName), "Process should re-deployed with new name");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
            domainRestClient.getProcessService().removeProcess(dataloadProcess);
            domainDataSourceUtils.getDataSourceRestRequest().deleteDataSource(dataSourceID);
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void deleteProcess() throws IOException {
        String dataSource = generateDataSourceTitle();
        String processName = generateProcessName();
        String dataSourceID = dataSourceUtils.createDefaultDataSource(dataSource, DATA_BASE, DatabaseType.SNOWFLAKE);
        DataloadProcess dataloadProcess =
                getProcessService().createProcess(getProject(), new DataDistributionProcess(processName, dataSourceID, "1"));
        try {
            ProjectDetailPage projectDetailPage = initDiscProjectDetailPage();
            projectDetailPage.getProcess(processName).deleteProcess();
            assertFalse(projectDetailPage.hasProcess(processName), "Process is not delete yet");

            dataloadProcess = getProcessService().createProcess(getProject(), new DataDistributionProcess(processName, dataSourceID, "1"));
            assertTrue(initDiscProjectDetailPage().hasProcess(processName), "Can't create process has name which is deleted");
        } finally {
            getProcessService().removeProcess(dataloadProcess);
            dataSourceUtils.getDataSourceRestRequest().deleteDataSource(dataSourceID);
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        testParams.setProjectId(testParams.getProjectId());
        if (testParams.getDeleteMode() == DeleteMode.DELETE_NEVER) {
            return;
        }
        lcmBrickFlowBuilder.destroy();
    }
}
