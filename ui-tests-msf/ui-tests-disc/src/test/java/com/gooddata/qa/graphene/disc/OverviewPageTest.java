package com.gooddata.qa.graphene.disc;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.browser.BrowserUtils.canAccessGreyPage;

import java.io.IOException;
import java.lang.reflect.Method;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.disc.OverviewProjectDetails;
import com.gooddata.qa.graphene.entity.disc.OverviewProjectDetails.OverviewProcess;
import com.gooddata.qa.graphene.entity.disc.OverviewProjectDetails.OverviewProcess.OverviewSchedule;
import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.enums.disc.DeployPackages;
import com.gooddata.qa.graphene.enums.disc.DeployPackages.Executables;
import com.gooddata.qa.graphene.enums.disc.OverviewProjectStates;
import com.gooddata.qa.graphene.enums.disc.ScheduleCronTimes;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils;

public class OverviewPageTest extends AbstractOverviewProjectsTest {

    @BeforeClass
    public void initProperties() {
        // Created time is used to identify the working project in case user has no admin role
        projectTitle = "Disc-test-overview-page-" + System.currentTimeMillis();
    }

    @AfterMethod(alwaysRun = true)
    public void afterTest(Method m) {
        cleanWorkingProjectAfterTest(m);
    }

    // Due to bootstrap just save the last visited page, and account qa+test is used in many job at the same time,
    // the expected page will be incorrect in this case after logout and sign in again.
    // So we should use a separate user for this test case
    @Test(dependsOnMethods = {"createProject"})
    public void checkOverviewPageShowAfterLogoutAndSignIn() throws ParseException, JSONException, IOException {
        String newUser = generateEmail(testParams.getUser());

        UserManagementRestUtils.createUser(getRestApiClient(), testParams.getUserDomain(), newUser, testParams.getPassword());
        addUserToProject(newUser, UserRoles.ADMIN);

        try {
            logout();
            signInAtGreyPages(newUser, testParams.getPassword());

            initDISCOverviewPage();

            // Use this action to avoid navigating to projects.html before logout.
            // So when sign in, user will be redirected to DISC page again
            logoutInDiscPage();
            signInAtUI(newUser, testParams.getPassword());
            waitForFragmentVisible(discOverviewProjects);

        } finally {
            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.ADMIN);
            UserManagementRestUtils.deleteUserByEmail(getRestApiClient(), testParams.getUserDomain(), newUser);
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkDefaultOverviewState() {
        initDISCOverviewPage();
        assertTrue(discOverview.isActive(OverviewProjectStates.FAILED));
        assertFalse(discOverview.isActive(OverviewProjectStates.RUNNING));
        assertFalse(discOverview.isActive(OverviewProjectStates.SCHEDULED));
        assertFalse(discOverview.isActive(OverviewProjectStates.SUCCESSFUL));
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkEmptyFailedState() {
        initDISCOverviewPage();
        checkFilteredOutOverviewProject(OverviewProjectStates.FAILED, testParams.getProjectId());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkEmptyRunningState() {
        initDISCOverviewPage();
        checkFilteredOutOverviewProject(OverviewProjectStates.RUNNING, testParams.getProjectId());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkEmptySucessfulState() {
        initDISCOverviewPage();
        checkFilteredOutOverviewProject(OverviewProjectStates.SUCCESSFUL, testParams.getProjectId());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkScheduledStateNumber() {
        OverviewProjectDetails overviewProject = new OverviewProjectDetails();
        OverviewProcess overviewProcess =
                overviewProject.newProcess().setProcessName("Check Scheduled State Number");
        OverviewSchedule overviewSchedule = overviewProcess.newSchedule().setScheduleName("Scheduled schedule");
        prepareDataForProject(testParams.getProjectId());
        prepareDataForScheduledProject(overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule)));

        initDISCOverviewPage();
        discOverview.selectOverviewState(OverviewProjectStates.SCHEDULED);
        assertOverviewStateNumber(OverviewProjectStates.SCHEDULED, discOverviewProjects.getOverviewProjectNumber());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkCombinedStatesNumber() {
        String processName = "Check Combined States Number";
        openProjectDetailPage(testParams.getProjectId());
        deployInProjectDetailPage(DeployPackages.BASIC, processName);

        createSchedule(new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.FAILED_GRAPH)
                .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setHourInDay("23").setMinuteInHour("59"));
        scheduleDetail.manualRun();
        assertFailedExecution(Executables.FAILED_GRAPH);
        scheduleDetail.clickOnCloseScheduleButton();

        createSchedule(new ScheduleBuilder().setProcessName(processName)
                .setExecutable(Executables.SUCCESSFUL_GRAPH).setCronTime(ScheduleCronTimes.CRON_EVERYDAY)
                .setHourInDay("23").setMinuteInHour("59"));
        scheduleDetail.manualRun();
        assertSuccessfulExecution();

        initDISCOverviewPage();
        discOverview.selectOverviewState(OverviewProjectStates.FAILED);
        assertOverviewStateNumber(OverviewProjectStates.FAILED, discOverviewProjects.getOverviewProjectNumber());
        discOverview.selectOverviewState(OverviewProjectStates.SUCCESSFUL);
        assertOverviewStateNumber(OverviewProjectStates.SUCCESSFUL,
                discOverviewProjects.getOverviewProjectNumber());
        checkFilteredOutOverviewProject(OverviewProjectStates.RUNNING, testParams.getProjectId());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkOverviewFailedProjects() {
        OverviewProjectDetails overviewProject = new OverviewProjectDetails().setProjectId(testParams.getProjectId());
        OverviewProcess overviewProcess =
                overviewProject.newProcess().setProcessName("Check Overview Failed Project");
        OverviewSchedule overviewSchedule = overviewProcess.newSchedule().setScheduleName("Failed Schedule");
        overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule));

        prepareDataForCheckingOverviewState(OverviewProjectStates.FAILED, overviewProject);
        initDISCOverviewPage();
        waitForElementVisible(discOverviewProjects.getRoot());
        assertOverviewProject(OverviewProjectStates.FAILED, overviewProject);

        checkOtherOverviewStates(OverviewProjectStates.FAILED, testParams.getProjectId());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkOverviewSuccessfulProject() {
        OverviewProjectDetails overviewProject = new OverviewProjectDetails().setProjectId(testParams.getProjectId());
        OverviewProcess overviewProcess =
                overviewProject.newProcess().setProcessName("Check Overview Successful Project");
        OverviewSchedule overviewSchedule = overviewProcess.newSchedule().setScheduleName("Successful Schedule");
        overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule));

        prepareDataForCheckingOverviewState(OverviewProjectStates.SUCCESSFUL, overviewProject);
        initDISCOverviewPage();
        waitForElementVisible(discOverview.getRoot());
        discOverview.selectOverviewState(OverviewProjectStates.SUCCESSFUL);
        waitForElementVisible(discOverviewProjects.getRoot());
        assertOverviewProject(OverviewProjectStates.SUCCESSFUL, overviewProject);

        checkOtherOverviewStates(OverviewProjectStates.SUCCESSFUL, testParams.getProjectId());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkOverviewRunningProject() {
        OverviewProjectDetails overviewProject = new OverviewProjectDetails().setProjectId(testParams.getProjectId());
        OverviewProcess overviewProcess =
                overviewProject.newProcess().setProcessName("Check Overview Running Project");
        OverviewSchedule overviewSchedule = overviewProcess.newSchedule().setScheduleName("Running Schedule");
        overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule));

        prepareDataForCheckingOverviewState(OverviewProjectStates.RUNNING, overviewProject);
        initDISCOverviewPage();
        waitForElementVisible(discOverview.getRoot());
        discOverview.selectOverviewState(OverviewProjectStates.RUNNING);
        waitForElementVisible(discOverviewProjects.getRoot());
        assertOverviewProject(OverviewProjectStates.RUNNING, overviewProject);

        checkOtherOverviewStates(OverviewProjectStates.RUNNING, testParams.getProjectId());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void accessProjectDetailFromOverviewPage() {
        String processName = "Check Access Project Detail Page";
        openProjectDetailPage(testParams.getProjectId());
        deployInProjectDetailPage(DeployPackages.BASIC, processName);

        createSchedule(new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.FAILED_GRAPH)
                .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setHourInDay("23").setMinuteInHour("59"));
        scheduleDetail.manualRun();
        assertFailedExecution(Executables.FAILED_GRAPH);
        scheduleDetail.clickOnCloseScheduleButton();

        createSchedule(new ScheduleBuilder().setProcessName(processName)
                .setExecutable(Executables.SUCCESSFUL_GRAPH).setCronTime(ScheduleCronTimes.CRON_EVERYDAY)
                .setHourInDay("23").setMinuteInHour("59"));
        scheduleDetail.manualRun();
        assertSuccessfulExecution();

        accessWorkingProjectDetail(OverviewProjectStates.SUCCESSFUL);
        accessWorkingProjectDetail(OverviewProjectStates.FAILED);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void restartFailedProjects() {
        OverviewProjectDetails overviewProject = new OverviewProjectDetails().setProjectId(testParams.getProjectId());
        OverviewProcess overviewProcess =
                overviewProject.newProcess().setProcessName("Check Overview Failed Project");
        OverviewSchedule overviewSchedule = overviewProcess.newSchedule().setScheduleName("Failed Schedule");
        overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule));
        bulkActionsProjectInOverviewPage(OverviewProjectStates.FAILED, overviewProject);

        browser.get(overviewSchedule.getScheduleUrl());
        waitForElementVisible(scheduleDetail.getRoot());
        if (!scheduleDetail.isStarted()) {
            assertEquals(scheduleDetail.getExecutionItemsNumber(), 2);
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void restartFailedSchedule() {
        bulkActionsScheduleInOverviewPage(OverviewProjectStates.FAILED);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void disableFailedProjects() {
        disableProjectInOverviewPage(OverviewProjectStates.FAILED);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void disableFailedSchedule() {
        disableScheduleInOverviewPage(OverviewProjectStates.FAILED);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void runSuccessfulProjects() {
        OverviewProjectDetails overviewProject = new OverviewProjectDetails().setProjectId(testParams.getProjectId());
        OverviewProcess overviewProcess =
                overviewProject.newProcess().setProcessName("Check Run Overview Successful Project");
        OverviewSchedule overviewSchedule = overviewProcess.newSchedule().setScheduleName("Successful Schedule");
        overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule));
        bulkActionsProjectInOverviewPage(OverviewProjectStates.SUCCESSFUL, overviewProject);

        browser.get(overviewSchedule.getScheduleUrl());
        waitForElementVisible(scheduleDetail.getRoot());
        assertTrue(scheduleDetail.isStarted());
        assertSuccessfulExecution();
    }

    @Test(dependsOnMethods = {"createProject"})
    public void runSuccessfulSchedule() {
        bulkActionsScheduleInOverviewPage(OverviewProjectStates.SUCCESSFUL);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void disableSuccessfulProjects() {
        disableProjectInOverviewPage(OverviewProjectStates.SUCCESSFUL);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void disableSuccessfulSchedule() {
        disableScheduleInOverviewPage(OverviewProjectStates.SUCCESSFUL);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void stopRunningProjects() {
        OverviewProjectDetails overviewProject = new OverviewProjectDetails().setProjectId(testParams.getProjectId());
        OverviewProcess overviewProcess =
                overviewProject.newProcess().setProcessName("Check Stop Overview Running Project");
        OverviewSchedule overviewSchedule = overviewProcess.newSchedule().setScheduleName("Running Schedule");
        overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule));
        bulkActionsProjectInOverviewPage(OverviewProjectStates.RUNNING, overviewProject);

        browser.get(overviewSchedule.getScheduleUrl());
        waitForElementVisible(scheduleDetail.getRoot());
        assertManualStoppedExecution();
    }

    @Test(dependsOnMethods = {"createProject"})
    public void stopRunningSchedule() {
        bulkActionsScheduleInOverviewPage(OverviewProjectStates.RUNNING);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void disableRunningProjects() {
        disableProjectInOverviewPage(OverviewProjectStates.RUNNING);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void disableRunningSchedule() {
        disableScheduleInOverviewPage(OverviewProjectStates.RUNNING);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"project-overview"})
    public void checkProjectsNotAdminInFailedState() throws ParseException, JSONException, IOException {
        checkOverviewProjectWithoutAdminRole(OverviewProjectStates.FAILED);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"project-overview"})
    public void checkProjectsNotAdminInSucessfulState() throws ParseException, JSONException, IOException {
        checkOverviewProjectWithoutAdminRole(OverviewProjectStates.SUCCESSFUL);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"project-overview"})
    public void checkProjectsNotAdminInRunningState() throws ParseException, JSONException, IOException {
        checkOverviewProjectWithoutAdminRole(OverviewProjectStates.RUNNING);
    }

    private void accessWorkingProjectDetail(OverviewProjectStates state) {
        initDISCOverviewPage();
        discOverview.selectOverviewState(state);
        waitForFragmentVisible(discOverviewProjects).accessProjectDetailPage(testParams.getProjectId());
        waitForFragmentVisible(projectDetailPage);
    }

    private void logoutInDiscPage() {
        waitForElementVisible(BY_LOGGED_USER_BUTTON, browser).click();
        waitForElementVisible(BY_LOGOUT_LINK, browser).click();
        waitForFragmentVisible(loginFragment);
    }

    private void prepareDataForScheduledProject(OverviewProjectDetails overviewProject) {
        for (OverviewProcess overviewProcess : overviewProject.getOverviewProcesses()) {
            openProjectDetailPage(testParams.getProjectId());
            String processUrl = deployInProjectDetailPage(DeployPackages.BASIC, overviewProcess.getProcessName());
            overviewProcess.setProcessUrl(processUrl);
            for (OverviewSchedule overviewSchedule : overviewProcess.getOverviewSchedules()) {
                createSchedule(new ScheduleBuilder().setProcessName(overviewProcess.getProcessName())
                        .setExecutable(Executables.LONG_TIME_RUNNING_GRAPH)
                        .setScheduleName(overviewSchedule.getScheduleName())
                        .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setHourInDay("23").setMinuteInHour("59"));
                overviewSchedule.setScheduleUrl(browser.getCurrentUrl());
                scheduleDetail.manualRun();
            }
        }
    }
}
