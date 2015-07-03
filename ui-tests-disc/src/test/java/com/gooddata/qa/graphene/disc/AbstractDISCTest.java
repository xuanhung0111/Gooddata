package com.gooddata.qa.graphene.disc;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.ZIP_FILES;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;

import com.gooddata.qa.graphene.AbstractMSFTest;
import com.gooddata.qa.graphene.entity.disc.NotificationBuilder;
import com.gooddata.qa.graphene.entity.disc.ProjectInfo;
import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.enums.disc.DeployPackages;
import com.gooddata.qa.utils.graphene.Screenshots;

public abstract class AbstractDISCTest extends AbstractMSFTest {

    protected static final String DISC_PROJECTS_PAGE_URL = "admin/disc/#/projects";
    protected static final String DISC_OVERVIEW_PAGE = "admin/disc/#/overview";

    private List<ProjectInfo> projects;

    protected List<ProjectInfo> getProjects() {
        if (projects == null)
            projects = Arrays.asList(getWorkingProject());
        return projects;
    }

    protected void cleanWorkingProjectAfterTest(Method m) {
        if (!m.getDeclaringClass().equals(this.getClass()))
            return;
        cleanProcessesInWorkingProject();
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

        String filePath = getFilePathFromResource("/" + ZIP_FILES + "/" + deployPackage.getPackageName());
        deployForm.deployProcess(filePath, deployPackage.getPackageType(), processName);
        assertDeployedProcessInProjects(processName, projects, deployPackage);
    }

    protected String deployInProjectDetailPage(DeployPackages deployPackage, String processName) {
        String processUrl = null;
        waitForElementVisible(projectDetailPage.getRoot());
        projectDetailPage.clickOnDeployProcessButton();

        String filePath = getFilePathFromResource("/" + ZIP_FILES + "/" + deployPackage.getPackageName());
        deployForm.deployProcess(filePath, deployPackage.getPackageType(), processName);
        assertFalse(projectDetailPage.isErrorDialogVisible());
        processUrl = browser.getCurrentUrl();
        projectDetailPage.checkFocusedProcess(processName);
        projectDetailPage.assertActiveProcessInList(processName, deployPackage);
        Screenshots.takeScreenshot(browser, "assert-successful-deployed-process-" + processName,
                getClass());
        return processUrl;
    }

    protected void redeployProcess(String processName, DeployPackages redeployPackage,
            String redeployProcessName, ScheduleBuilder... schedules) {
        waitForElementVisible(projectDetailPage.getRoot());
        projectDetailPage.clickOnRedeployButton(processName);
        waitForElementVisible(deployForm.getRoot());

        String filePath = getFilePathFromResource("/" + ZIP_FILES + "/" + redeployPackage.getPackageName());
        deployForm.redeployProcess(filePath, redeployPackage.getPackageType(), redeployProcessName);
        waitForElementNotPresent(deployForm.getRoot());
        assertFalse(projectDetailPage.isErrorDialogVisible());
        projectDetailPage.checkFocusedProcess(redeployProcessName);
        projectDetailPage
                .assertActiveProcessInList(redeployProcessName, redeployPackage, schedules);
        Screenshots.takeScreenshot(browser, "assert-successful-deployed-process-"
                + redeployProcessName, getClass());
    }

    protected void createAndAssertSchedule(ScheduleBuilder scheduleBuilder) {
        createSchedule(scheduleBuilder);
        assertSchedule(scheduleBuilder);
    }

    protected void createSchedule(ScheduleBuilder scheduleBuilder) {
        projectDetailPage.clickOnNewScheduleButton();
        waitForElementVisible(scheduleForm.getRoot());
        scheduleForm.createNewSchedule(scheduleBuilder);
        if (scheduleBuilder.isConfirmed())
            waitForElementVisible(scheduleDetail.getRoot());
    }

    protected void openScheduleViaUrl (String scheduleUrl) {
        System.out.println("Loading schedule detail page ..." + scheduleUrl);
        // Use this step instead of OpenURL function
        // because schedule url contains many parts (project id, process id, schedule id) 
        browser.get(scheduleUrl);
        waitForFragmentVisible(scheduleDetail);
    }

    protected void assertSchedule(ScheduleBuilder scheduleBuilder) {
        projectDetailPage.checkFocusedProcess(scheduleBuilder.getProcessName());
        waitForElementVisible(schedulesTable.getRoot());
        projectDetailPage.assertScheduleInList(schedulesTable, scheduleBuilder);
        schedulesTable.getScheduleTitle(scheduleBuilder.getScheduleName()).click();
        waitForElementVisible(scheduleDetail.getRoot());
        scheduleDetail.assertSchedule(scheduleBuilder);
    }

    protected void prepareScheduleWithBasicPackage(ScheduleBuilder scheduleBuilder) {
        assertFalse(scheduleBuilder.getExecutable() == null);
        deployInProjectDetailPage(scheduleBuilder.getExecutable().getExecutablePackage(),
                scheduleBuilder.getProcessName());
        createAndAssertSchedule(scheduleBuilder);
        scheduleBuilder.setScheduleUrl(browser.getCurrentUrl());
    }

    protected NotificationBuilder createAndAssertNotification(
            NotificationBuilder notificationBuilder) {
        createNotification(notificationBuilder);
        try {
            Thread.sleep(2000); // Wait for notification is completely saved!
        } catch (InterruptedException e) {
            fail("There is an interruption during waiting for created notification: " + e);
        }
        assertNotification(notificationBuilder);

        return notificationBuilder;
    }

    protected NotificationBuilder createNotification(NotificationBuilder notificationBuilder) {
        openProjectDetailPage(getWorkingProject());
        projectDetailPage.getNotificationButton(notificationBuilder.getProcessName()).click();
        waitForElementVisible(discNotificationRules.getRoot());
        discNotificationRules.clickOnAddNotificationButton();
        int notificationIndex = discNotificationRules.getNotificationNumber() - 1;
        discNotificationRules
                .setNotificationFields(notificationBuilder.setIndex(notificationIndex));
        if (notificationBuilder.isSaved())
            discNotificationRules.saveNotification(notificationIndex);
        else
            discNotificationRules.cancelSaveNotification(notificationIndex);

        return notificationBuilder;
    }

    protected void assertNotification(NotificationBuilder notificationBuilder) {
        openProjectDetailPage(getWorkingProject());
        projectDetailPage.getNotificationButton(notificationBuilder.getProcessName()).click();
        waitForElementVisible(discNotificationRules.getRoot());
        assertTrue(discNotificationRules.isNotExpanded(notificationBuilder.getIndex()));
        discNotificationRules.expandNotificationRule(notificationBuilder.getIndex());
        discNotificationRules.assertNotificationFields(notificationBuilder);
    }

    protected void selectProjectsToDeployInProjectsPage(List<ProjectInfo> projects) {
        waitForElementVisible(discProjectsList.getRoot());
        discProjectsList.checkOnProjects(projects);
        assertTrue(discProjectsList.getDeployProcessButton().isEnabled());
        discProjectsList.clickOnDeployProcessButton();
        waitForElementVisible(deployForm.getRoot());
    }

    protected void createMultipleProjects(List<ProjectInfo> additionalProjects) {
        for (ProjectInfo project : additionalProjects) {
            openUrl(PAGE_GDC_PROJECTS);
            waitForElementVisible(gpProject.getRoot());
            try {
                project.setProjectId(gpProject.createProject(project.getProjectName(),
                        project.getProjectName(), null, testParams.getAuthorizationToken(),
                        testParams.getDwhDriver(), testParams.getProjectEnvironment(),
                        projectCreateCheckIterations));
            } catch (JSONException e) {
                fail("There is problem when creating new project: " + e);
            } catch (InterruptedException e) {
                fail("There is problem when creating new project: " + e);
            }
        }
    }

    protected void deleteProjects(List<ProjectInfo> projectsToDelete) {
        for (ProjectInfo projectToDelete : projectsToDelete) {
            cleanProcessesInProject(projectToDelete.getProjectId());
            deleteProject(projectToDelete.getProjectId());
        }
    }

    protected void cleanProcessesInWorkingProject() {
        cleanProcessesInProject(getWorkingProject().getProjectId());
    }
    
    private void cleanProcessesInProject(String projectId) {
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
}
