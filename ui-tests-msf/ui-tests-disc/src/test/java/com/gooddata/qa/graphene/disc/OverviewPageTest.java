package com.gooddata.qa.graphene.disc;

import static com.gooddata.qa.browser.BrowserUtils.canAccessGreyPage;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.Schedule;
import com.gooddata.qa.graphene.enums.disc.schedule.ScheduleStatus;
import com.gooddata.qa.graphene.common.AbstractProcessTest;
import com.gooddata.qa.graphene.enums.disc.schedule.Executable;
import com.gooddata.qa.graphene.enums.disc.schedule.ScheduleCronTime;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.disc.overview.OverviewPage.OverviewState;
import com.gooddata.qa.graphene.fragments.disc.overview.OverviewProjects.OverviewProjectItem;
import com.gooddata.qa.graphene.fragments.disc.schedule.ScheduleDetail;
import com.gooddata.qa.graphene.fragments.login.LoginFragment;

public class OverviewPageTest extends AbstractProcessTest {

    private static final String FAILED_EMPTY_STATE_MESSAGE = "No failed data loading processes. Good job!";
    private static final String RUNNING_EMPTY_STATE_MESSAGE = "No data loading processes are running right now.";
    private static final String SCHEDULED_EMPTY_STATE_MESSAGE = "No data loading processes are scheduled to run.";
    private static final String SUCCESSFUL_EMPTY_STATE_MESSAGE = "No data loading processes have successfully finished.";
    private static final String NO_PROJECTS_TITLE = "You do not have any projects.";
    private static final String NO_PROJECTS_MESSAGE = "Do you want to build your own project with your own data? Let us know!";

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
        createAndAddUserToProject(UserRoles.VIEWER);
    }
    
    @Test(dependsOnGroups = {"createProject"})
    public void checkOverviewPageShowAfterLogoutAndSignIn() throws ParseException, JSONException, IOException {
        initDiscOverviewPage();
        // Use this action to avoid navigating to projects.html before logout.
        // So when sign in, user will be redirected to DISC page again
        logoutInDiscPage();
        signIn(false, UserRoles.ADMIN);
        waitForFragmentVisible(overviewPage).waitForPageLoaded();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkDefaultOverviewState() {
        initDiscOverviewPage();

        takeScreenshot(browser, "Default-overview-stages", getClass());
        assertTrue(overviewPage.isStateActive(OverviewState.FAILED));
        assertFalse(overviewPage.isStateActive(OverviewState.RUNNING));
        assertFalse(overviewPage.isStateActive(OverviewState.SCHEDULED));
        assertFalse(overviewPage.isStateActive(OverviewState.SUCCESSFUL));
    }

    @DataProvider(name = "emptyStatesProvider")
    public Object[][] getEmptyStatesProvider() {
        return new Object[][] {
            {OverviewState.FAILED, FAILED_EMPTY_STATE_MESSAGE},
            {OverviewState.SUCCESSFUL, SUCCESSFUL_EMPTY_STATE_MESSAGE},
            {OverviewState.RUNNING, RUNNING_EMPTY_STATE_MESSAGE},
            {OverviewState.SCHEDULED, SCHEDULED_EMPTY_STATE_MESSAGE}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "emptyStatesProvider")
    public void checkEmptyState(OverviewState state, String message) {
        initDiscOverviewPage().selectState(state);

        takeScreenshot(browser, "Empty-message-for-" + state + "-shows", getClass());
        assertEquals(overviewPage.getStateNumber(state), 0);
        assertEquals(overviewPage.getEmptyStateMessage(), message);
    }

    @DataProvider(name = "specificStatesProvider")
    public Object[][] getSpecificStatesProvider() {
        return new Object[][] {
            {OverviewState.FAILED, Executable.ERROR_GRAPH},
            {OverviewState.SUCCESSFUL, Executable.SUCCESSFUL_GRAPH},
            {OverviewState.RUNNING, Executable.LONG_TIME_RUNNING_GRAPH},
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "specificStatesProvider")
    public void checkSpecificState(OverviewState state, Executable executable) {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, executable, ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            ScheduleDetail scheduleDetailFragment = initScheduleDetail(schedule).executeSchedule();
            if (state == OverviewState.RUNNING) {
                scheduleDetailFragment.waitForStatus(ScheduleStatus.RUNNING);
            } else {
                scheduleDetailFragment.waitForExecutionFinish();
            }

            initDiscOverviewPage().selectState(state);
            takeScreenshot(browser, "State-" + state + "-shows-correctly", getClass());
            assertEquals(overviewPage.getStateNumber(state), 1);

        } finally {
            getProcessService().removeProcess(process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void accessProjectDetailFromOverviewPage() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, Executable.SUCCESSFUL_GRAPH,
                    ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            initScheduleDetail(schedule).executeSchedule().waitForExecutionFinish();

            initDiscOverviewPage()
                    .selectState(OverviewState.SUCCESSFUL)
                    .getOverviewProject(projectTitle)
                    .openDetailPage();
            waitForFragmentVisible(projectDetailPage);

            takeScreenshot(browser, "Project-detail-page-shows", getClass());
            assertThat(browser.getCurrentUrl(), containsString(testParams.getProjectId()));

        } finally {
            getProcessService().removeProcess(process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void restartFailedScheduleFromProject() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, Executable.ERROR_GRAPH,
                    ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            assertEquals(initScheduleDetail(schedule)
                    .executeSchedule()
                    .waitForExecutionFinish()
                    .getExecutionHistoryItemNumber(), 1);

            initDiscOverviewPage()
                    .selectState(OverviewState.FAILED)
                    .markOnProject(projectTitle)
                    .restartScheduleExecution()
                    .waitForExecutionFinish();

            takeScreenshot(browser, "Restart-failed-schedule-from-overview-page-successfully", getClass());
            assertEquals(overviewPage.getStateNumber(OverviewState.FAILED), 1);
            assertEquals(initScheduleDetail(schedule).getExecutionHistoryItemNumber(), 2);

        } finally {
            getProcessService().removeProcess(process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void disableFailedScheduleFromProject() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, Executable.ERROR_GRAPH,
                    ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            initScheduleDetail(schedule).executeSchedule().waitForExecutionFinish();

            initDiscOverviewPage()
                    .selectState(OverviewState.FAILED)
                    .markOnProject(projectTitle)
                    .disableScheduleExecution()
                    .waitForPageLoaded();

            takeScreenshot(browser, "Disable-failed-schedule-from-project-successfully", getClass());
            assertEquals(overviewPage.getStateNumber(OverviewState.FAILED), 0);
            assertTrue(initScheduleDetail(schedule).isDisabled(),
                    "Schedule is not disabled after being disabled from overview page");

        } finally {
            getProcessService().removeProcess(process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void runSuccessfulScheduleFromProject() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, Executable.SUCCESSFUL_GRAPH,
                    ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            assertEquals(initScheduleDetail(schedule)
                    .executeSchedule()
                    .waitForExecutionFinish()
                    .getExecutionHistoryItemNumber(), 1);

            initDiscOverviewPage()
                    .selectState(OverviewState.SUCCESSFUL)
                    .markOnProject(projectTitle)
                    .executeSchedule()
                    .waitForExecutionFinish();

            takeScreenshot(browser, "Run-successful-schedule-from-overview-page-successfully", getClass());
            assertEquals(overviewPage.getStateNumber(OverviewState.SUCCESSFUL), 1);
            assertEquals(initScheduleDetail(schedule).getExecutionHistoryItemNumber(), 2);

        } finally {
            getProcessService().removeProcess(process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void stopRunningScheduleFromProject() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, Executable.LONG_TIME_RUNNING_GRAPH,
                    ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            initScheduleDetail(schedule).executeSchedule().waitForStatus(ScheduleStatus.RUNNING);

            initDiscOverviewPage()
                    .selectState(OverviewState.RUNNING)
                    .markOnProject(projectTitle)
                    .stopScheduleExecution()
                    .waitForPageLoaded()
                    .selectState(OverviewState.FAILED);

            takeScreenshot(browser, "Stop-schedule-excution-from-overview-page", getClass());
            assertEquals(overviewPage.getStateNumber(OverviewState.FAILED), 1);
            assertEquals(initScheduleDetail(schedule)
                    .getLastExecutionHistoryItem()
                    .getStatusDescription(), "MANUALLY STOPPED");

        } finally {
            getProcessService().removeProcess(process);
        }
    }

    @DataProvider(name = "userProvider")
    public Object[][] getUserProvider() {
        return new Object[][] {
            {UserRoles.EDITOR},
            {UserRoles.VIEWER}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "userProvider")
    public void checkProjectDisabledWithNonAdminRole(UserRoles role) throws JSONException {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, Executable.SUCCESSFUL_GRAPH,
                    ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            initScheduleDetail(schedule).executeSchedule().waitForExecutionFinish();

            logoutAndLoginAs(canAccessGreyPage(browser), role);

            OverviewProjectItem project = initDiscOverviewPage()
                    .selectState(OverviewState.SUCCESSFUL)
                    .getOverviewProject(projectTitle);

            takeScreenshot(browser, role + "cannot-access-project-detail-from-overview-page", getClass());
            assertTrue(project.isDisabled(), role + " can access project detail page");

        } finally {
            getProcessService().removeProcess(process);
            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkHeaderBar() {
        initDiscOverviewPage();

        assertEquals(getHeaderTitle(), "Data Integration Console");
        assertEquals(getOverviewButton().getText(), "Overview");
        assertEquals(getProjectsButton().getText(), "Projects");

        getProjectsButton().click();
        waitForFragmentVisible(projectsPage).waitForPageLoaded();

        getOverviewButton().click();
        waitForFragmentVisible(overviewPage).waitForPageLoaded();
    }

    @DataProvider(name = "scheduleProvider")
    public Object[][] getScheduleProvider() {
        return new Object[][] {
            {Executable.SUCCESSFUL_GRAPH, OverviewState.SUCCESSFUL},
            {Executable.ERROR_GRAPH, OverviewState.FAILED},
            {Executable.LONG_TIME_RUNNING_GRAPH, OverviewState.RUNNING}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "scheduleProvider")
    public void checkCustomScheduleNameInOverviewPage(Executable executable, OverviewState state) {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, executable, ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            String customScheduleName = "Schedule-" + generateHashString();
            ScheduleDetail scheduleDetail = initScheduleDetail(schedule);

            scheduleDetail.editNameByClickOnTitle(customScheduleName).saveChanges();
            scheduleDetail.executeSchedule();

            if (executable == Executable.LONG_TIME_RUNNING_GRAPH) {
                scheduleDetail.waitForStatus(ScheduleStatus.RUNNING);
            } else {
                scheduleDetail.waitForExecutionFinish();
            }

            OverviewProjectItem project = initDiscOverviewPage()
                    .selectState(state).getOverviewProject(projectTitle);
            assertTrue(project.expand().hasSchedule(customScheduleName), "Schedule " + customScheduleName + " not show");
            assertEquals(project.getScheduleExecutable(customScheduleName), executable.getPath());

        } finally {
            getProcessService().removeProcess(process);
        }
    }

    @Test
    public void messageIsShownForUserWithoutProjects() throws IOException, JSONException {
        String username = createDynamicUserFrom(testParams.getUser());
        logout();
        signInAtUI(username, testParams.getPassword());

        initDiscOverviewPage();
        takeScreenshot(browser, "overview-page-without-projects", this.getClass());
        assertThat(overviewPage.getOverviewEmptyStateTitle(), equalTo(NO_PROJECTS_TITLE));
        assertThat(overviewPage.getOverviewEmptyStateMessage(), equalTo(NO_PROJECTS_MESSAGE));
        assertThat(overviewPage.containsAnyOverviewState(), is(false));

        initDiscProjectsPage();
        takeScreenshot(browser, "projects-page-without-projects", this.getClass());
        assertThat(projectsPage.getEmptyStateTitle(), equalTo(NO_PROJECTS_TITLE));
        assertThat(projectsPage.getEmptyStateMessage(), equalTo(NO_PROJECTS_MESSAGE));
    }

    private void logoutInDiscPage() {
        waitForElementVisible(BY_LOGGED_USER_BUTTON, browser).click();
        waitForElementVisible(BY_LOGOUT_LINK, browser).click();
        LoginFragment.waitForPageLoaded(browser);
    }

    private WebElement getOverviewButton() {
        return waitForElementVisible(By.className("ait-header-overview-btn"), browser);
    }

    private WebElement getProjectsButton() {
        return waitForElementVisible(By.className("ait-header-projects-btn"), browser);
    }

    private String getHeaderTitle() {
        return waitForElementVisible(By.className("app-title"), browser).getText();
    }
}
