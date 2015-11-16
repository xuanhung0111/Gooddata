package com.gooddata.qa.graphene.disc;

import static com.gooddata.qa.graphene.utils.CheckUtils.*;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.*;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.disc.ProjectInfo;
import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.enums.disc.DeployPackages;
import com.gooddata.qa.graphene.enums.disc.ScheduleCronTimes;
import com.gooddata.qa.graphene.enums.disc.DeployPackages.Executables;
import com.gooddata.qa.graphene.enums.disc.ProjectStateFilters;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class ProjectsPageTest extends AbstractOverviewProjectsTest {

    private static final String DISC_HEADER_TITLE = "Data Integration Console";
    private static final String PROJECTS_BUTTON_TITLE = "Projects";
    private static final String OVERVIEW_BUTTON_TITLE = "Overview";
    private static final int MINIMUM_NUMBER_OF_PROJECTS = 25;

    @BeforeClass
    public void initProperties() {
        // Created time is used to identify the working project in case user has no admin role
        projectTitle = "Disc-test-projects-page-" + System.currentTimeMillis();
    }

    @AfterMethod
    public void afterTest(Method m) {
        cleanWorkingProjectAfterTest(m);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkProjectFilterOptions() {
        initDISCProjectsPage();
        Select select = discProjectsPage.getProjectFilterSelect();
        List<String> options = select.getOptions().stream().map(option -> option.getText()).collect(toList());
        System.out.println("Check filter options list...");
        assertThat(
                options,
                hasItems(ProjectStateFilters.ALL.getOption(), ProjectStateFilters.FAILED.getOption(),
                        ProjectStateFilters.RUNNING.getOption(), ProjectStateFilters.SCHEDULED.getOption(),
                        ProjectStateFilters.SUCCESSFUL.getOption(), ProjectStateFilters.UNSCHEDULED.getOption(),
                        ProjectStateFilters.DISABLED.getOption()));
        assertEquals(ProjectStateFilters.ALL.getOption(), discProjectsPage.getSelectedFilterOption().getText());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkFailedProjectsFilterOption() {
        checkProjectsFilter(ProjectStateFilters.FAILED);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkSuccessfulProjectsFilterOptions() {
        checkProjectsFilter(ProjectStateFilters.SUCCESSFUL);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkRunningProjectsFilterOptions() {
        checkProjectsFilter(ProjectStateFilters.RUNNING);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkScheduledProjectsFilterOptions() {
        ProjectInfo projectInfo = new ProjectInfo().setProjectName("Disc-test-scheduled-filter-option");
        List<ProjectInfo> additionalProjects = Arrays.asList(projectInfo);
        createMultipleProjects(additionalProjects);
        try {
            prepareDataForAdditionalProjects(additionalProjects);
            prepareDataForProjectsPageTest(ProjectStateFilters.SCHEDULED, getWorkingProject());

            initDISCProjectsPage();
            checkProjectFilter(ProjectStateFilters.SCHEDULED, getProjects());
        } finally {
            deleteProjects(additionalProjects);
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkUnscheduledProjectsFilterOptions() {
        checkProjectsFilter(ProjectStateFilters.UNSCHEDULED);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkDisabledProjectsFilterOptions() {
        checkProjectsFilter(ProjectStateFilters.DISABLED);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkDataLoadingProcess() {
        String processName1 = "Check Data Loading Processes 1";
        String processName2 = "Check Data Loading Processes 2";
        openProjectDetailPage(getWorkingProject());
        deployInProjectDetailPage(DeployPackages.BASIC, processName1);
        deployInProjectDetailPage(DeployPackages.BASIC, processName2);

        ScheduleBuilder scheduleBuilder1 =
                new ScheduleBuilder().setProcessName(processName1).setExecutable(
                        Executables.LONG_TIME_RUNNING_GRAPH);
        createSchedule(scheduleBuilder1);
        ScheduleBuilder scheduleBuilder2 =
                new ScheduleBuilder().setProcessName(processName1).setExecutable(Executables.FAILED_GRAPH);
        createSchedule(scheduleBuilder2);
        ScheduleBuilder scheduleBuilder3 =
                new ScheduleBuilder().setProcessName(processName2).setExecutable(Executables.FAILED_GRAPH);
        createSchedule(scheduleBuilder3);

        initDISCProjectsPage();
        waitForFragmentVisible(discProjectsList);
        final String expectedDataLoadingProcess = String.format("%d processes, %d schedules", 2, 3);
        assertThat(discProjectsList.getProcessesLabel(getWorkingProject()), is(expectedDataLoadingProcess));
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkLastSuccessfulExecution() {
        String processName = "Check Last Successful Execution";
        openProjectDetailByUrl(testParams.getProjectId());
        deployInProjectDetailPage(DeployPackages.BASIC, processName);

        initDISCProjectsPage();
        waitForElementVisible(discProjectsList.getRoot());
        assertTrue(discProjectsList.getLastSuccessfulExecutionInfo(getWorkingProject()).isEmpty(),
                "Last successful execution info is not empty for new deployed process!");

        ScheduleBuilder scheduleBuilder1 =
                new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.SUCCESSFUL_GRAPH);
        openProjectDetailByUrl(testParams.getProjectId());
        createSchedule(scheduleBuilder1);
        scheduleDetail.manualRun();
        assertSuccessfulExecution();
        String lastSuccessfulExecutionDate = scheduleDetail.getLastExecutionDate();
        String lastSuccessfulExecutionTime = scheduleDetail.getLastExecutionTime();
        scheduleDetail.clickOnCloseScheduleButton();

        ScheduleBuilder scheduleBuilder2 =
                new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.FAILED_GRAPH);
        createSchedule(scheduleBuilder2);
        scheduleDetail.manualRun();
        assertFailedExecution(scheduleBuilder2.getExecutable());

        initDISCProjectsPage();
        waitForFragmentVisible(discProjectsList);
        assertEquals(discProjectsList.getLastSuccessfulExecutionInfo(getWorkingProject()),
                Joiner.on(" ").join(lastSuccessfulExecutionDate, lastSuccessfulExecutionTime.substring(14)));
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkLastSuccessfulAutoExecution() {
        String processName = "Check Last Successful Auto Execution";
        openProjectDetailByUrl(testParams.getProjectId());
        deployInProjectDetailPage(DeployPackages.BASIC, processName);

        ScheduleBuilder scheduleBuilder =
                new ScheduleBuilder().setProcessName(processName).setExecutable(Executables.SUCCESSFUL_GRAPH)
                        .setCronTime(ScheduleCronTimes.CRON_EVERYHOUR).setMinuteInHour("${minute}");
        createSchedule(scheduleBuilder);

        assertTrue(scheduleDetail.waitForAutoRunSchedule(scheduleBuilder.getCronTimeBuilder()),
                "Schedule is not run automatically well!");
        assertSuccessfulExecution();
        String lastSuccessfulExecutionDate = scheduleDetail.getLastExecutionDate();
        String lastSuccessfulExecutionTime = scheduleDetail.getLastExecutionTime();

        initDISCProjectsPage();
        waitForFragmentVisible(discProjectsList);
        assertEquals(discProjectsList.getLastSuccessfulExecutionInfo(getWorkingProject()),
                Joiner.on(" ").join(lastSuccessfulExecutionDate, lastSuccessfulExecutionTime.substring(14)));
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkProjectsNotAdmin() {
        try {
            addUsersWithOtherRolesToProject();

            openUrl(PAGE_PROJECTS);
            logout();
            signIn(false, UserRoles.VIEWER);

            initDISCProjectsPage();
            assertProjectNotAdmin(getWorkingProject().getProjectName());

            openUrl(PAGE_PROJECTS);
            logout();
            signIn(false, UserRoles.EDITOR);
            initDISCProjectsPage();
            assertProjectNotAdmin(getWorkingProject().getProjectName());
        } catch (ParseException e) {
            System.out.println("There is problem during adding user to project: ");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("There is problem during adding user to project: ");
            e.printStackTrace();
        } catch (JSONException e) {
            System.out.println("There is problem during adding user to project or signIn: ");
            e.printStackTrace();
        } finally {
            openUrl(PAGE_PROJECTS);
            logout();
            try {
                signIn(false, UserRoles.ADMIN);
            } catch (JSONException e) {
                System.out.println("There is problem when signIn: ");
                e.printStackTrace();
            }
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkPagingOptions() {
        initDISCProjectsPage();
        Select select = discProjectsPage.getProjectsPerPageSelect();
        List<String> pagingOptions =
                select.getOptions().stream().map(option -> option.getText()).collect(toList());
        System.out.println("Check paging options list...");
        assertThat(pagingOptions, hasItems("20", "50", "100", "200", "500", "1000", "2000", "5000"));
        assertEquals(select.getFirstSelectedOption().getText(), "20");
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkPagingProjectsPage() {
        openUrl(PAGE_PROJECTS);
        waitForElementVisible(projectsPage.getRoot());
        waitForCollectionIsNotEmpty(projectsPage.getProjectsElements());
        int projectsNumber =
                projectsPage.getProjectsElements().size() + projectsPage.getDemoProjectsElements().size();
        List<ProjectInfo> additionalProjects = new ArrayList<ProjectInfo>();
        if (projectsNumber <= MINIMUM_NUMBER_OF_PROJECTS) {
            for (int i = 0; i < 25 - projectsNumber + 1; i++) {
                ProjectInfo projectInfo = new ProjectInfo().setProjectName("Disc-test-paging-projects-page-" + i);
                additionalProjects.add(projectInfo);
            }
            createMultipleProjects(additionalProjects);
        }

        try {
            initDISCProjectsPage();
            Select select = discProjectsPage.getProjectsPerPageSelect();
            select.selectByVisibleText("20");
            assertEquals("20", select.getFirstSelectedOption().getText());
            waitForFragmentVisible(discProjectsList);

            int projectsPerPageNumber = Integer.parseInt("20");
            assertEquals(projectsPerPageNumber, discProjectsList.getNumberOfRows());
            String pagingBarLabelSubString = String.format("Showing 1-%d", projectsPerPageNumber);
            System.out.println("Paging bar label: " + pagingBarLabelSubString);
            assertThat(discProjectsPage.getPagingBarLabel(), containsString(pagingBarLabelSubString));

            WebElement prevPageButton = discProjectsPage.getPrevPageButton();
            WebElement nextPageButton = discProjectsPage.getNextPageButton();
            List<WebElement> projectPageNumbers = discProjectsPage.getProjectPageNumber();

            assertTrue(prevPageButton.getAttribute("class").contains("disabled"),
                    "Prev button is not disabled when first page is selected!");
            assertFalse(nextPageButton.getAttribute("class").contains("disabled"),
                    "Next button is disabled when first page is selected!");
            assertTrue(projectPageNumbers.get(0).getAttribute("class").contains("active-cell"),
                    "First page is not selected by default!");

            System.out.println("Click on Next button...");
            nextPageButton.click();
            waitForElementVisible(discProjectsList.getRoot());
            assertFalse(projectPageNumbers.get(0).getAttribute("class").contains("active-cell"),
                    "Non-selected page number is active!");
            assertTrue(projectPageNumbers.get(1).getAttribute("class").contains("active-cell"),
                    "Selected page number is not active!");
            assertFalse(prevPageButton.getAttribute("class").contains("disabled"),
                    "Prev button is disabled when selected page is not the first page!");

            System.out.println("Click on Prev button...");
            prevPageButton.click();
            waitForElementVisible(discProjectsList.getRoot());
            assertFalse(projectPageNumbers.get(1).getAttribute("class").contains("active-cell"),
                    "Non-selected page number is active!");
            assertTrue(projectPageNumbers.get(0).getAttribute("class").contains("active-cell"),
                    "Selected page number is not active!");
            assertTrue(prevPageButton.getAttribute("class").contains("disabled"),
                    "Prev button is not disabled when first page is selected!");

            for (int i = 1; i < projectPageNumbers.size() && i < 4; i++) {
                System.out.println("Click on page " + i);
                projectPageNumbers.get(i).click();
                waitForElementVisible(discProjectsList.getRoot());
                assertTrue(projectPageNumbers.get(i).getAttribute("class").contains("active-cell"),
                        "Projects page number is not enabled!");
                if (i != projectPageNumbers.size() - 1)
                    assertEquals(projectsPerPageNumber, discProjectsList.getNumberOfRows(),
                            "Incorrect projects per page number!");
                else
                    assertFalse(discProjectsList.getNumberOfRows() > projectsPerPageNumber,
                            "The project number in the last page: " + discProjectsList.getNumberOfRows());
            }
        } finally {
            deleteProjects(additionalProjects);
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkEmptySearchResult() {
        initDISCProjectsPage();
        for (ProjectStateFilters projectFilter : ProjectStateFilters.values()) {
            discProjectsPage.selectFilterOption(projectFilter);
            String expectedEmptySearchResultMessage =
                    projectFilter.getEmptySearchResultMessage().replace("${searchKey}", "no search result");
            discProjectsPage.enterSearchKey("no search result");
            System.out.println("Empty Search Result Message: " + discProjectsList.getEmptyStateMessage().trim());
            String actualEmptySearchResultMessage = discProjectsList.getEmptyStateMessage();
            if (projectFilter.equals(ProjectStateFilters.ALL))
                assertEquals(actualEmptySearchResultMessage.trim(), expectedEmptySearchResultMessage,
                        "Incorrect search result message!");
            else {
                assertThat(actualEmptySearchResultMessage, containsString(expectedEmptySearchResultMessage));
                String emtySearchResultMessageInSpecificState = "Search in all projects";
                assertTrue(actualEmptySearchResultMessage.contains(emtySearchResultMessageInSpecificState));
                discProjectsPage.getAllProjectLinkInEmptyState().click();
                assertEquals(ProjectStateFilters.ALL.getOption(), discProjectsPage.getSelectedFilterOption()
                        .getText());
            }
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkSearchProjectByName() {
        checkSearchWorkingProjectByName();
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkSearchProjectById() {
        checkSearchWorkingProjectById();
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkSearchProjectInSuccessfulState() {
        checkSearchProjectInSpecificState(ProjectStateFilters.SUCCESSFUL);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkSearchProjectInFailedState() {
        checkSearchProjectInSpecificState(ProjectStateFilters.FAILED);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkSearchProjectInRunningState() {
        checkSearchProjectInSpecificState(ProjectStateFilters.RUNNING);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkSearchProjectInDisabledState() {
        checkSearchProjectInSpecificState(ProjectStateFilters.DISABLED);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkSearchProjectInUnscheduledState() {
        initDISCProjectsPage();
        searchProjectInSpecificState(ProjectStateFilters.UNSCHEDULED, getWorkingProject());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkSearchUnicodeProjectName() {
        String unicodeProjectName = "Tiếng Việt ພາສາລາວ  résumé";
        ProjectInfo projectInfo = new ProjectInfo().setProjectName(unicodeProjectName);
        List<ProjectInfo> additionalProjects = Arrays.asList(projectInfo);
        createMultipleProjects(additionalProjects);
        try {
            initDISCProjectsPage();
            discProjectsPage.enterSearchKey(unicodeProjectName);
            discProjectsPage.waitForSearchingProgress();
            waitForFragmentVisible(discProjectsList);
            assertTrue(discProjectsList.isCorrectSearchedProjectByUnicodeName(unicodeProjectName),
                    "Incorrect search result by unicode name!");
        } finally {
            deleteProjects(additionalProjects);
        }

    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkDefaultSearchBox() {
        initDISCProjectsPage();
        WebElement searchBox = discProjectsPage.getSearchBox();
        assertTrue(searchBox.getAttribute("value").isEmpty(), "Default search box is not empty!");
        assertEquals(searchBox.getAttribute("placeholder"), "Search in project names and ids ...",
                "Incorrect placeholder in search box!");
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkDeleteSearchKey() {
        initDISCProjectsPage();
        discProjectsPage.enterSearchKey("no search result");
        System.out.println("Empty state message: " + discProjectsList.getEmptyStateMessage());
        discProjectsPage.deleteSearchKey();
        assertTrue(discProjectsPage.getSearchBox().getAttribute("value").isEmpty(),
                "Search box is not empty after deleting search key!");
        waitForFragmentVisible(discProjectsList);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkProjectListAfterLeaveAProject() {
        List<ProjectInfo> additionalProjects = Lists.newArrayList();
        String secondAdmin = testParams.getEditorUser();
        String secondAdminPassword = testParams.getEditorPassword();
        try {
            addUserToProject(secondAdmin, UserRoles.ADMIN);
            logout();
            signInAtUI(secondAdmin, secondAdminPassword);

            additionalProjects.add(new ProjectInfo().setProjectName("Additional Project"));
            createMultipleProjects(additionalProjects);

            initProjectsAndUsersPage();
            projectAndUsersPage.leaveProject();
            waitForProjectsPageLoaded(browser);

            initDISCProjectsPage();
            waitForElementVisible(discProjectsList.getRoot());
            for (ProjectInfo project : additionalProjects) {
                assertNotNull(discProjectsList.selectProjectWithAdminRole(project), "Cannot find project "
                        + project.getProjectId());
            }
            assertNull(discProjectsList.selectProjectWithAdminRole(getWorkingProject()),
                    "Project is still shown on DIC projects page after user leave it!!");
        } catch (Exception e) {
            throw new IllegalStateException("There is exeception when adding user to project!", e);
        } finally {
            deleteProjects(additionalProjects);
            logout();
            signInAtUI(testParams.getUser(), testParams.getPassword());
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkProjectsPageHeader() {
        initDISCProjectsPage();
        waitForFragmentVisible(discNavigation);
        assertEquals(discNavigation.getHeaderTitle(), DISC_HEADER_TITLE, "Incorrect projects page header!");
        assertEquals(discNavigation.getOverviewButtonTitle(), OVERVIEW_BUTTON_TITLE,
                "Incorrect Overview button title!");
        assertEquals(discNavigation.getProjectsButtonTitle(), PROJECTS_BUTTON_TITLE,
                "Incorrect Projects button tilte!");
        discNavigation.clickOnOverviewButton();
        waitForFragmentVisible(discOverview);
        discNavigation.clickOnProjectsButton();
        waitForFragmentVisible(discProjectsPage);
    }

    private void assertProjectNotAdmin(String projectName) {
        WebElement selectedProject =
                discProjectsList.selectProjectWithNonAdminRole(getWorkingProject().getProjectName());
        assertNotNull(selectedProject, "Project is not found!");
        try {
            discProjectsList.clickOnProjectWithNonAdminRole(selectedProject);
            Graphene.waitGui().withTimeout(10, TimeUnit.SECONDS).until().element(projectDetailPage.getRoot()).is()
                    .visible();
        } catch (NoSuchElementException ex) {
            System.out.println("Non-admin user cannot access project detail page!");
        }
    }
}
