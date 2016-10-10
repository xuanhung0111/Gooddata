package com.gooddata.qa.graphene.disc;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
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
import com.gooddata.qa.graphene.fragments.login.LoginFragment;
import com.google.common.base.Predicate;

public class OverviewPageTest extends AbstractOverviewProjectsTest {

    @BeforeClass(alwaysRun = true)
    public void initProperties() {
        // Created time is used to identify the working project in case user has no admin role
        projectTitle = "Disc-test-overview-page-" + System.currentTimeMillis();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkOverviewPageShowAfterLogoutAndSignIn() throws ParseException, JSONException, IOException {
        initDISCOverviewPage();
        // Use this action to avoid navigating to projects.html before logout.
        // So when sign in, user will be redirected to DISC page again
        logoutInDiscPage();
        signIn(false, UserRoles.ADMIN);
        waitForFragmentVisible(discOverviewProjects);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"empty-state"})
    public void checkDefaultOverviewState() {
        initDISCOverviewPage();
        assertTrue(discOverview.isActive(OverviewProjectStates.FAILED));
        assertFalse(discOverview.isActive(OverviewProjectStates.RUNNING));
        assertFalse(discOverview.isActive(OverviewProjectStates.SCHEDULED));
        assertFalse(discOverview.isActive(OverviewProjectStates.SUCCESSFUL));
    }

    @DataProvider(name = "statesProvider")
    public Object[][] statesProvider() {
        return new Object[][] {
            { OverviewProjectStates.FAILED },
            { OverviewProjectStates.SUCCESSFUL },
            { OverviewProjectStates.RUNNING },
            { OverviewProjectStates.SCHEDULED }
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "statesProvider", groups = {"empty-state"})
    public void checkEmptyState(OverviewProjectStates state) {
        initDISCOverviewPage();
        discOverview.selectOverviewState(state);
        waitForFragmentVisible(discOverviewProjects);
        assertEquals(discOverview.getStateNumber(state), "0");
        assertEquals(discOverviewProjects.getOverviewEmptyStateMessage(), state.getOverviewEmptyState(),
                    "Incorrect overview empty state message!");
    }

    @Test(dependsOnGroups = {"empty-state"}, dataProvider = "statesProvider",
            description = "This test try to check the overview number of each state (except SCHEDULED) in case"
                    + "there is at least ONE schedule is run. Create and run schedule in case empty state")
    public void checkState(OverviewProjectStates state) {
        if (state == OverviewProjectStates.SCHEDULED) {
            log.info("Skip checking SCHEDULED state because it's very difficult to make a schedule in this mode!"
                + "Now we have to create upto 30 schedules to run parallel to hope that some will be in scheduled queue."
                + "Please feel free to fix it if there is another better for this situation!");
            return;
        }

        initDISCOverviewPage();
        discOverview.selectOverviewState(state);
        waitForFragmentVisible(discOverviewProjects);
        if (Integer.parseInt(discOverview.getStateNumber(state)) == 0) {
            OverviewProjectDetails overviewProject = new OverviewProjectDetails().setProjectId(testParams.getProjectId());
            OverviewProcess overviewProcess =
                    overviewProject.newProcess().setProcessName("Check Overview " + state.getOption() + " Project");
            OverviewSchedule overviewSchedule = overviewProcess.newSchedule().setScheduleName("Failed Schedule");
            overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule));
            prepareDataForCheckingOverviewState(state, overviewProject);
        }

        initDISCOverviewPage();
        discOverview.selectOverviewState(state);
        waitForFragmentVisible(discOverviewProjects);

        assertEquals(discOverview.getStateNumber(state), String.valueOf(discOverviewProjects.getOverviewProjectNumber()));
    }

    @Test(dependsOnGroups = {"empty-state"})
    public void accessProjectDetailFromOverviewPage() {
        List<Executables> graphs = new ArrayList<>();

        initDISCOverviewPage();
        discOverview.selectOverviewState(OverviewProjectStates.SUCCESSFUL);
        waitForFragmentVisible(discOverviewProjects);
        if (Integer.parseInt(discOverview.getStateNumber(OverviewProjectStates.SUCCESSFUL)) == 0) {
            graphs.add(Executables.SUCCESSFUL_GRAPH);
        }
        discOverview.selectOverviewState(OverviewProjectStates.FAILED);
        waitForFragmentVisible(discOverviewProjects);
        if (Integer.parseInt(discOverview.getStateNumber(OverviewProjectStates.FAILED)) == 0) {
            graphs.add(Executables.FAILED_GRAPH);
        }

        if (!graphs.isEmpty()) {
            String processName = "Check Access Project Detail Page";
            openProjectDetailPage(testParams.getProjectId());
            deployInProjectDetailPage(DeployPackages.BASIC, processName);

            for (Executables graph: graphs) {
                createSchedule(new ScheduleBuilder().setProcessName(processName).setExecutable(graph)
                        .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setHourInDay("23").setMinuteInHour("59"));
                scheduleDetail.manualRun();
                scheduleDetail.waitForExecutionFinish();
                scheduleDetail.clickOnCloseScheduleButton();
            }
        }

        accessWorkingProjectDetail(OverviewProjectStates.SUCCESSFUL);
        accessWorkingProjectDetail(OverviewProjectStates.FAILED);
    }

    @Test(dependsOnGroups = {"empty-state"})
    public void restartFailedProjects() {
        initDISCOverviewPage();
        discOverview.selectOverviewState(OverviewProjectStates.FAILED);
        waitForFragmentVisible(discOverviewProjects);
        OverviewProjectDetails overviewProject = new OverviewProjectDetails().setProjectId(testParams.getProjectId());
        if (Integer.parseInt(discOverview.getStateNumber(OverviewProjectStates.FAILED)) == 0) {
            OverviewProcess overviewProcess =
                    overviewProject.newProcess().setProcessName("Check Overview Failed Project");
            OverviewSchedule overviewSchedule = overviewProcess.newSchedule().setScheduleName("Failed Schedule");
            overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule));
            prepareDataForCheckingOverviewState(OverviewProjectStates.FAILED, overviewProject);
        }

        initDISCOverviewPage();
        waitForFragmentVisible(discOverview);
        discOverview.selectOverviewState(OverviewProjectStates.FAILED);
        waitForFragmentVisible(discOverviewProjects);
        discOverviewProjects.checkOnSelectedProjects(overviewProject);
        discOverviewProjects.bulkAction(OverviewProjectStates.FAILED);

        Predicate<WebDriver> restartFinished = browser -> {
            browser.navigate().refresh();
            return Integer.parseInt(discOverview.getStateNumber(OverviewProjectStates.FAILED)) > 0;
        };
        Graphene.waitGui().until(restartFinished);

        discOverviewProjects.openScheduleDetail(testParams.getProjectId());
        waitForElementVisible(scheduleDetail.getRoot());
        if (!scheduleDetail.isStarted()) {
            assertTrue(scheduleDetail.getExecutionItemsNumber() > 1);
        }
    }

    @Test(dependsOnGroups = {"empty-state"}, dataProvider = "statesProvider")
    public void disableFailedProjects(OverviewProjectStates state) {
        if (state == OverviewProjectStates.SCHEDULED) {
            log.info("Ignore SCHEDULED case!");
            return;
        }

        initDISCOverviewPage();
        discOverview.selectOverviewState(state);
        waitForFragmentVisible(discOverviewProjects);
        OverviewProjectDetails overviewProject = new OverviewProjectDetails().setProjectId(testParams.getProjectId());
        if (Integer.parseInt(discOverview.getStateNumber(state)) == 0) {
            OverviewProcess overviewProcess =
                    overviewProject.newProcess().setProcessName("Check Overview Failed Project");
            OverviewSchedule overviewSchedule = overviewProcess.newSchedule().setScheduleName("Failed Schedule");
            overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule));
            prepareDataForCheckingOverviewState(state, overviewProject);
        }

        initDISCOverviewPage();
        waitForFragmentVisible(discOverview);
        discOverview.selectOverviewState(state);
        waitForFragmentVisible(discOverviewProjects);
        discOverviewProjects.checkOnSelectedProjects(overviewProject);
        discOverviewProjects.getOverviewProjectExpandButton(
                discOverviewProjects.getOverviewProjectWithAdminRole(testParams.getProjectId())).click();
        String link = discOverviewProjects.getOverviewScheduleLink(state,
                discOverviewProjects.getOverviewSchedules(discOverviewProjects.getProcesses().get(0)).get(0));
        discOverviewProjects.disableAction();
        browser.navigate().refresh();
        waitForFragmentVisible(discOverviewProjects);
        assertEquals(discOverview.getStateNumber(state), "0");

        browser.get(link);
        waitForFragmentVisible(scheduleDetail);
        waitForElementVisible(scheduleDetail.getEnableButton());
    }

    @Test(dependsOnGroups = {"empty-state"})
    public void runSuccessfulProjects() {
        initDISCOverviewPage();
        discOverview.selectOverviewState(OverviewProjectStates.SUCCESSFUL);
        waitForFragmentVisible(discOverviewProjects);
        OverviewProjectDetails overviewProject = new OverviewProjectDetails().setProjectId(testParams.getProjectId());
        if (Integer.parseInt(discOverview.getStateNumber(OverviewProjectStates.SUCCESSFUL)) == 0) {
            OverviewProcess overviewProcess =
                    overviewProject.newProcess().setProcessName("Check Run Overview Successful Project");
            OverviewSchedule overviewSchedule = overviewProcess.newSchedule().setScheduleName("Successful Schedule");
            overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule));
            prepareDataForCheckingOverviewState(OverviewProjectStates.SUCCESSFUL, overviewProject);
        }

        initDISCOverviewPage();
        waitForFragmentVisible(discOverview);
        discOverview.selectOverviewState(OverviewProjectStates.SUCCESSFUL);
        waitForFragmentVisible(discOverviewProjects);
        discOverviewProjects.checkOnSelectedProjects(overviewProject);
        discOverviewProjects.getOverviewProjectExpandButton(
                discOverviewProjects.getOverviewProjectWithAdminRole(testParams.getProjectId())).click();
        String link = discOverviewProjects.getOverviewScheduleLink(OverviewProjectStates.SUCCESSFUL,
                discOverviewProjects.getOverviewSchedules(discOverviewProjects.getProcesses().get(0)).get(0));
        browser.get(link);
        waitForElementVisible(scheduleDetail.getRoot());
        if (scheduleDetail.isStarted()) {
            scheduleDetail.waitForExecutionFinish();
        }
        assertSuccessfulExecution();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void stopRunningProjects() {
        OverviewProjectDetails overviewProject = new OverviewProjectDetails().setProjectId(testParams.getProjectId());
        OverviewProcess overviewProcess =
                overviewProject.newProcess().setProcessName("Check Stop Overview Running Project");
        OverviewSchedule overviewSchedule = overviewProcess.newSchedule().setScheduleName("Running Schedule");
        overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule));
        prepareDataForCheckingOverviewState(OverviewProjectStates.RUNNING, overviewProject);

        initDISCOverviewPage();
        waitForFragmentVisible(discOverview);
        discOverview.selectOverviewState(OverviewProjectStates.RUNNING);
        waitForFragmentVisible(discOverviewProjects);
        discOverviewProjects.checkOnSelectedProjects(overviewProject);
        discOverviewProjects.bulkAction(OverviewProjectStates.RUNNING);
        browser.navigate().refresh();
        waitForFragmentVisible(discOverviewProjects);

        browser.get(overviewSchedule.getScheduleUrl());
        waitForElementVisible(scheduleDetail.getRoot());
        assertManualStoppedExecution();
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"project-overview"}, dataProvider = "statesProvider")
    public void checkProjectsNotAdminInState(OverviewProjectStates state)
            throws ParseException, JSONException, IOException {
        if (state == OverviewProjectStates.SCHEDULED) {
            log.info("Ignore SCHEDULED case!");
            return;
        }

        OverviewProjectDetails overviewProject = new OverviewProjectDetails()
            .setProjectId(testParams.getProjectId())
            .setProjectName(projectTitle);
        OverviewProcess overviewProcess =
                overviewProject.newProcess().setProcessName("Check Overview Project With Non-Admin Role " + state.getOption());
        OverviewSchedule overviewSchedule = overviewProcess.newSchedule().setScheduleName("Schedule");
        overviewProcess.addSchedule(overviewSchedule);
        overviewProject.addProcess(overviewProcess);
        prepareDataForCheckingOverviewState(state, overviewProject);

        try {
            openUrl(PAGE_PROJECTS);

            logoutAndLoginAs(false, UserRoles.VIEWER);
            openUrl(DISC_OVERVIEW_PAGE);
            discOverview.selectOverviewState(state);
            waitForFragmentVisible(discOverviewProjects);
            checkProjectNotAdmin(state, overviewProject);
            openUrl(PAGE_PROJECTS);

            logoutAndLoginAs(false, UserRoles.EDITOR);
            openUrl(DISC_OVERVIEW_PAGE);
            discOverview.selectOverviewState(state);
            waitForFragmentVisible(discOverviewProjects);
            checkProjectNotAdmin(state, overviewProject);
        } finally {
            openUrl(PAGE_PROJECTS);
            logoutAndLoginAs(false, UserRoles.ADMIN);
        }
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
        createAndAddUserToProject(UserRoles.VIEWER);
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
        LoginFragment.waitForPageLoaded(browser);
    }

    private void checkProjectNotAdmin(OverviewProjectStates projectState,
            OverviewProjectDetails expectedOverviewProject) {
        WebElement overviewProject =
                discOverviewProjects.getOverviewProjectWithoutAdminRole(expectedOverviewProject.getProjectName());
        assertNotNull(overviewProject, "Cannot find project without admin role!");
        try {
            discOverviewProjects.getOverviewProjectName(overviewProject).click();
            Graphene.waitGui().withTimeout(10, TimeUnit.SECONDS).until().element(projectDetailPage.getRoot()).is()
                    .visible();
        } catch (NoSuchElementException ex) {
            System.out.println("Non-admin user cannot access project detail page!");
        }
        waitForFragmentVisible(discOverviewProjects);
        assertOverviewProjectWithoutAdminRole(projectState, expectedOverviewProject);
    }

    private void assertOverviewProjectWithoutAdminRole(OverviewProjectStates projectState,
            OverviewProjectDetails expectedOverviewProject) {
        WebElement overviewProjectDetail =
                discOverviewProjects.getOverviewProjectWithoutAdminRole(expectedOverviewProject.getProjectName());
        assertNotNull(overviewProjectDetail, "Cannot find project without admin role!");
        WebElement overviewProjectLogLinkElement =
                discOverviewProjects.getOverviewProjectLog(overviewProjectDetail);
        int projectScheduleNumber = expectedOverviewProject.getProjectScheduleNumber();
        String overviewProjectRuntime =
                discOverviewProjects.getOverviewProjectRuntime(overviewProjectDetail).getText();
        String overviewProjectDate = discOverviewProjects.getOverviewProjectDate(overviewProjectDetail).getText();
        if (projectScheduleNumber == 1) {
            OverviewSchedule expectedOverviewSchedule =
                    expectedOverviewProject.getOverviewProcesses().get(0).getOverviewSchedules().get(0);
            if (projectState != OverviewProjectStates.SCHEDULED)
                assertTrue(overviewProjectLogLinkElement.getAttribute("class").contains("action-unavailable-icon"));
            assertProjectInfoWithOnlyOneSchedule(projectState, overviewProjectDetail, expectedOverviewSchedule);
        } else if (projectScheduleNumber > 1) {
            if (projectState != OverviewProjectStates.SCHEDULED) {
                assertTrue(overviewProjectRuntime.isEmpty(), "Overview project runtime is not empty!");
                assertTrue(overviewProjectDate.isEmpty(), "Overview project date is not empty!");
                if (projectState == OverviewProjectStates.FAILED) {
                    String errorMessage = String.format("%d schedules", projectScheduleNumber);
                    assertEquals(discOverviewProjects.getOverviewProjectErrorMessage(overviewProjectDetail)
                            .getText(), errorMessage, "Incorrect error message");
                } else if (projectState == OverviewProjectStates.SUCCESSFUL) {
                    String okInfo = String.format("%d schedules", projectScheduleNumber);
                    assertEquals(discOverviewProjects.getOverviewProjectOKInfo(overviewProjectDetail).getText(),
                            okInfo, "Incorrect OK info!");
                }
            }
        }
    }

    private void assertProjectInfoWithOnlyOneSchedule(OverviewProjectStates projectState,
            WebElement overviewProjectDetail, OverviewSchedule expectedOverviewSchedule) {
        if (projectState != OverviewProjectStates.SCHEDULED) {
            assertTrue(discOverviewProjects.getOverviewProjectLog(overviewProjectDetail).isEnabled(),
                    "Log link is not enabled!");
            assertFalse(discOverviewProjects.getOverviewProjectRuntime(overviewProjectDetail).getText().isEmpty(),
                    "Execution runtime is empty!");
            System.out.println("Project schedule runtime: "
                    + discOverviewProjects.getOverviewProjectRuntime(overviewProjectDetail).getText());
            if (projectState != OverviewProjectStates.RUNNING) {
                assertEquals(expectedOverviewSchedule.getLastExecutionRunTime(), discOverviewProjects
                        .getOverviewProjectRuntime(overviewProjectDetail).getText(),
                        "Incorrect execution runtime!");
                assertEquals(expectedOverviewSchedule.getOverviewExecutionDateTime(), discOverviewProjects
                        .getOverviewProjectDate(overviewProjectDetail).getText(), "Incorrect execution date!");
            } else
                assertTrue(
                        expectedOverviewSchedule.getOverviewExecutionDateTime().contains(
                                discOverviewProjects.getOverviewProjectDate(overviewProjectDetail).getText()),
                        "Incorrect execution date!");
            if (projectState == OverviewProjectStates.FAILED) {
                assertEquals(expectedOverviewSchedule.getExecutionDescription(), discOverviewProjects
                        .getOverviewProjectErrorMessage(overviewProjectDetail).getText(),
                        "Incorrect error message!");
            } else if (projectState == OverviewProjectStates.SUCCESSFUL) {
                assertEquals("1 schedule", discOverviewProjects.getOverviewProjectOKInfo(overviewProjectDetail)
                        .getText(), "Incorrect successful info!");
            }
        }
    }
}
