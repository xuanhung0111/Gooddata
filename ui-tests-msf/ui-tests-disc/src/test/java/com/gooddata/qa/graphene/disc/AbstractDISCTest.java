package com.gooddata.qa.graphene.disc;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.ZIP_FILES;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.AbstractMSFTest;
import com.gooddata.qa.graphene.entity.disc.NotificationBuilder;
import com.gooddata.qa.graphene.entity.disc.ProjectInfo;
import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.enums.disc.DeployPackages;
import com.gooddata.qa.graphene.enums.disc.NotificationEvents;
import com.gooddata.qa.graphene.enums.disc.OverviewProjectStates;
import com.gooddata.qa.graphene.enums.disc.DeployPackages.Executables;
import com.gooddata.qa.graphene.fragments.disc.NotificationRule;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.google.common.base.Predicate;

public abstract class AbstractDISCTest extends AbstractMSFTest {

    private List<ProjectInfo> projects;

    private static final String OK_GROUP_DESCRIPTION_FORMAT = "OK %d×";

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
        waitForFragmentNotVisible(deployForm);
        assertDeployedProcessInProjects(processName, projects, deployPackage);
    }

    protected String deployInProjectDetailPage(DeployPackages deployPackage, String processName) {
        String processUrl = null;
        waitForElementVisible(projectDetailPage.getRoot());
        projectDetailPage.clickOnDeployProcessButton();

        String filePath = getFilePathFromResource("/" + ZIP_FILES + "/" + deployPackage.getPackageName());
        deployForm.deployProcess(filePath, deployPackage.getPackageType(), processName);
        waitForFragmentNotVisible(deployForm);
        assertFalse(projectDetailPage.isErrorDialogVisible(), "An error is shown!");
        processUrl = browser.getCurrentUrl();

        checkFocusedProcess(processName);
        assertActiveProcessInList(processName, deployPackage);
        Screenshots.takeScreenshot(browser, "assert-successful-deployed-process-" + processName, getClass());
        return processUrl;
    }

    protected void redeployProcess(String processName, DeployPackages redeployPackage, String redeployProcessName,
            ScheduleBuilder... schedules) {
        waitForElementVisible(projectDetailPage.getRoot());
        projectDetailPage.activeProcess(processName).clickOnRedeployButton();
        waitForElementVisible(deployForm.getRoot());

        String filePath = getFilePathFromResource("/" + ZIP_FILES + "/" + redeployPackage.getPackageName());
        deployForm.redeployProcess(filePath, redeployPackage.getPackageType(), redeployProcessName);
        waitForFragmentNotVisible(deployForm);
        assertFalse(projectDetailPage.isErrorDialogVisible(), "An error is shown!");
        checkFocusedProcess(redeployProcessName);
        assertActiveProcessInList(redeployProcessName, redeployPackage, schedules);
        Screenshots.takeScreenshot(browser, "assert-successful-deployed-process-" + redeployProcessName,
                getClass());
    }

    protected void assertOverviewCustomScheduleName(OverviewProjectStates overviewState, ProjectInfo projectInfo,
            ScheduleBuilder scheduleBuilder) {
        String scheduleUrl = scheduleBuilder.getScheduleUrl();
        String overviewScheduleLink = scheduleUrl.substring(scheduleUrl.indexOf("#"));

        WebElement scheduleWrapper =
                discOverviewProjects.getOverviewScheduleName(overviewState, projectInfo, overviewScheduleLink);

        String overviewScheduleName = discOverviewProjects.getOverviewScheduleName(scheduleWrapper);
        String overviewScheduleGraphName = discOverviewProjects.getOverviewScheduleGraphName(scheduleWrapper);

        assertEquals(overviewScheduleName, scheduleBuilder.getScheduleName(),
                "Incorrect schedule name on overview page!");
        if (scheduleBuilder.isDataloadProcess())
            assertTrue(overviewScheduleGraphName.isEmpty(), "Dataload schedule with invalid title: "
                    + overviewScheduleGraphName);
        else
            assertEquals(overviewScheduleGraphName, scheduleBuilder.getExecutable().getExecutablePath(),
                    "Incorrect schedule executable path on overview page!");
    }

    protected void assertActiveProcessInList(String processName, DeployPackages deployPackage,
            ScheduleBuilder... schedules) {
        projectDetailPage.activeProcess(processName);
        assertEquals(projectDetailPage.getProcessTitle(), processName, "Incorrect active process title!");
        projectDetailPage.clickOnScheduleTab();
        String scheduleTabTitle =
                String.format("%d schedule", schedules.length) + (schedules.length == 1 ? "" : "s");
        assertEquals(projectDetailPage.getScheduleTabTitle(), scheduleTabTitle,
                "Incorrect active schedule tab title!");
        projectDetailPage.clickOnExecutableTab();
        List<Executables> executableList = deployPackage.getExecutables();
        String executableTabTitle =
                String.format("%d %s total", executableList.size(), deployPackage.getPackageType()
                        .getProcessTypeExecutable() + (executableList.size() > 1 ? "s" : ""));
        assertEquals(projectDetailPage.getExecutableTabTitle(), executableTabTitle,
                "Incorrect active executable tab title!");
        assertTrue(projectDetailPage.isCorrectExecutableList(executableList), "Incorrect executable list!");
    }

    protected void createAndAssertSchedule(ScheduleBuilder scheduleBuilder) {
        createSchedule(scheduleBuilder);
        assertSchedule(scheduleBuilder);
    }

    protected void createSchedule(ScheduleBuilder scheduleBuilder) {
        projectDetailPage.clickOnNewScheduleButton();
        waitForFragmentVisible(scheduleForm);
        scheduleForm.createNewSchedule(scheduleBuilder);
        if (scheduleBuilder.isConfirmed())
            waitForFragmentVisible(scheduleDetail);
    }

    protected void openScheduleViaUrl(String scheduleUrl) {
        System.out.println("Loading schedule detail page ..." + scheduleUrl);
        // Use this step instead of OpenURL function
        // because schedule url contains many parts (project id, process id, schedule id)
        browser.get(scheduleUrl);
        waitForFragmentVisible(scheduleDetail);
    }

    protected void assertSchedule(ScheduleBuilder scheduleBuilder) {
        checkFocusedProcess(scheduleBuilder.getProcessName());
        waitForFragmentVisible(schedulesTable);
        assertTrue(projectDetailPage.isCorrectScheduleInList(schedulesTable, scheduleBuilder),
                "Incorrect schedule in list!");
        schedulesTable.getScheduleTitle(scheduleBuilder.getScheduleName()).click();
        waitForFragmentVisible(scheduleDetail);
        assertScheduleDetails(scheduleBuilder);
    }

    protected void assertScheduleDetails(ScheduleBuilder scheduleBuilder) {
        assertEquals(scheduleDetail.getScheduleTitle(), scheduleBuilder.getScheduleName(),
                "Incorrect schedule name oh schedule detail page!");

        if (!scheduleBuilder.isDataloadProcess()) {
            Graphene.waitGui().until(new Predicate<WebDriver>() {

                @Override
                public boolean apply(WebDriver arg0) {
                    return scheduleDetail.getSelectedExecutablePath().equals(
                            scheduleBuilder.getExecutable().getExecutablePath());
                }
            });
        }

        assertTrue(scheduleDetail.isCorrectCronTime(scheduleBuilder.getCronTimeBuilder()), "Incorrect cron time!");

        if (!scheduleBuilder.getParameters().isEmpty())
            assertTrue(scheduleDetail.isCorrectScheduleParameters(scheduleBuilder.getParameters()),
                    "Incorrect parameters!");

        if (scheduleBuilder.isDataloadProcess()) {
            assertTrue(scheduleDetail.isCorrectDataloadOption(), "Incorrect dataload options!");
            assertTrue(scheduleDetail.isCorrectInlineBubbleHelp(), "Incorrect inline bubble help!");
            if (scheduleBuilder.isSynchronizeAllDatasets()) {
                assertTrue(scheduleDetail.isCorrectAllDatasetSelected(), "All datasets are not selected!");
            } else {
                assertTrue(scheduleDetail.isCorrectDatasetsSelected(scheduleBuilder),
                        "Incorrect selected datasets!");
            }
        }
    }

    protected void assertLastExecutionDetail() {
        assertTrue(scheduleDetail.isLastExecutionLogDisplayed(), "Execution log link is not shown!");
        assertTrue(scheduleDetail.isLastExecutionLogLinkEnabled(), "Execution log link is not enabled!");
        assertTrue(scheduleDetail.isLastExecutionDateDisplayed(), "Execution date is not shown!");
        System.out.println("Last execution date: " + scheduleDetail.getLastExecutionDate());
        assertTrue(scheduleDetail.isLastExecutionTimeDisplayed(), "Execution time is not shown!");
        System.out.println("Last execution time: " + scheduleDetail.getLastExecutionTime());
        assertTrue(scheduleDetail.isLastExecutionRunTimeDisplayed(), "Execution runtime is not shown!");
        System.out.println("Last execution runtime: " + scheduleDetail.getLastExecutionRuntime());
    }

    protected void assertSuccessfulExecution() {
        scheduleDetail.waitForExecutionFinish();
        assertTrue(scheduleDetail.isOkStatusDisplayedForLastExecution(), "OK icon is not shown!");
        assertTrue(scheduleDetail.getLastExecutionDescription().contains("OK"),
                "Status 'OK' is not shown in description!");
        assertLastExecutionDetail();
    }

    protected void assertFailedExecution(Executables executable) {
        scheduleDetail.waitForExecutionFinish();
        assertTrue(scheduleDetail.isErrorIconVisible(), "Error icon is not shown!");
        assertThat(scheduleDetail.getLastExecutionDescription(), containsString(executable.getErrorMessage()));
        assertLastExecutionDetail();
    }

    protected void assertManualStoppedExecution() {
        scheduleDetail.waitForExecutionFinish();
        assertTrue(scheduleDetail.isErrorIconVisible(), "Error icon is not shown");
        assertThat(scheduleDetail.getLastExecutionDescription(), is("MANUALLY STOPPED"));
        assertLastExecutionDetail();
    }

    protected void repeatManualRunFailedSchedule(int executionTimes, Executables executable) {
        for (int i = 0; i < executionTimes; i++) {
            scheduleDetail.manualRun();
            assertFailedExecution(executable);
        }
    }

    protected void checkOkExecutionGroup(final int okExecutionNumber, int okGroupIndex) {
        List<WebElement> scheduleExecutionItems = scheduleDetail.getExecutionItems();
        final int scheduleExecutionNumber = scheduleExecutionItems.size();
        String groupDescription = String.format(OK_GROUP_DESCRIPTION_FORMAT, okExecutionNumber);
        WebElement okExecutionGroup = scheduleExecutionItems.get(okGroupIndex);

        assertOkExecutionGroupInfo(okExecutionGroup, groupDescription);
        scheduleDetail.getOkGroupExpandButton(okExecutionGroup).click();
        Graphene.waitGui().until(new Predicate<WebDriver>() {

            @Override
            public boolean apply(WebDriver arg0) {
                return scheduleExecutionItems.size() == scheduleExecutionNumber + okExecutionNumber;
            }
        });
        assertExecutionItemsInfo(okGroupIndex, okExecutionNumber, groupDescription);
    }

    protected void prepareScheduleWithBasicPackage(ScheduleBuilder scheduleBuilder) {
        assertFalse(scheduleBuilder.getExecutable() == null, "Executable is not shown!");
        deployInProjectDetailPage(scheduleBuilder.getExecutable().getExecutablePackage(),
                scheduleBuilder.getProcessName());
        createAndAssertSchedule(scheduleBuilder);
        scheduleBuilder.setScheduleUrl(browser.getCurrentUrl());
    }

    protected NotificationBuilder createAndAssertNotification(NotificationBuilder notificationBuilder) {
        createNotification(notificationBuilder);
        sleepTightInSeconds(2); // Wait for notification is completely saved!
        assertNotification(notificationBuilder);

        return notificationBuilder;
    }

    protected NotificationBuilder createNotification(NotificationBuilder notificationBuilder) {
        openProjectDetailPage(getWorkingProject());
        projectDetailPage.activeProcess(notificationBuilder.getProcessName()).clickOnNotificationRuleButton();
        waitForFragmentVisible(discNotificationRules);
        discNotificationRules.clickOnAddNotificationButton();
        int notificationIndex = discNotificationRules.getNotificationNumber() - 1;
        NotificationRule newNotification = discNotificationRules.getNotificationRule(notificationIndex);
        notificationBuilder.setIndex(notificationIndex);
        newNotification.setNotificationFields(notificationBuilder);
        if (notificationBuilder.isSaved())
            newNotification.saveNotification();
        else
            newNotification.cancelSaveNotification();

        return notificationBuilder;
    }

    protected void assertNotification(NotificationBuilder notificationBuilder) {
        openProjectDetailPage(getWorkingProject());
        projectDetailPage.activeProcess(notificationBuilder.getProcessName()).clickOnNotificationRuleButton();
        waitForFragmentVisible(discNotificationRules);
        NotificationRule notificationRule =
                discNotificationRules.getNotificationRule(notificationBuilder.getIndex());
        assertTrue(notificationRule.isNotExpanded(), "Notification rule is expanded!");
        notificationRule.expandNotificationRule();

        final NotificationRule notificationRuleItem =
                discNotificationRules.getNotificationRule(notificationBuilder.getIndex());
        Graphene.waitGui().until(new Predicate<WebDriver>() {

            @Override
            public boolean apply(WebDriver arg0) {
                return !notificationRuleItem.getEmail().isEmpty();
            }
        });
        assertEquals(notificationRuleItem.getEmail(), notificationBuilder.getEmail(), "Incorrect email!");
        assertEquals(notificationRuleItem.getSubject(), notificationBuilder.getSubject(), "Incorrect subject!");
        assertEquals(notificationRuleItem.getMessage(), notificationBuilder.getMessage(), "Incorrect message!");
        Select scheduleEvent = notificationRuleItem.getEventSelect();
        assertEquals(notificationBuilder.getEvent().getEventOption(), scheduleEvent.getFirstSelectedOption()
                .getText(), "Incorrect selected schedule event!");
        if (notificationBuilder.getEvent() == NotificationEvents.CUSTOM_EVENT)
            assertEquals(notificationRuleItem.getCustomEvent(), notificationBuilder.getCustomEventName(),
                    "Incorrect custom event name!");
    }

    protected void selectProjectsToDeployInProjectsPage(List<ProjectInfo> projects) {
        waitForFragmentVisible(discProjectsList);
        discProjectsList.checkOnProjects(projects);
        assertTrue(discProjectsList.getDeployProcessButton().isEnabled(),
                "Deploy process button is not enabled on projects page!");
        discProjectsList.clickOnDeployProcessButton();
        waitForFragmentVisible(deployForm);
    }

    protected void createMultipleProjects(List<ProjectInfo> additionalProjects) {
        for (ProjectInfo project : additionalProjects) {
            openUrl(PAGE_GDC_PROJECTS);
            waitForElementVisible(gpProject.getRoot());
            try {
                project.setProjectId(gpProject.createProject(project.getProjectName(), project.getProjectName(),
                        null, testParams.getAuthorizationToken(), testParams.getDwhDriver(),
                        testParams.getProjectEnvironment(), projectCreateCheckIterations));
            } catch (JSONException e) {
                fail("There is problem when creating new project: " + e);
            }
        }
    }

    protected void deleteProjects(List<ProjectInfo> projectsToDelete) {
        for (ProjectInfo projectToDelete : projectsToDelete) {
            cleanProcessesInProject(projectToDelete);
            deleteProject(projectToDelete.getProjectId());
        }
    }

    protected void cleanProcessesInWorkingProject() {
        cleanProcessesInProject(getWorkingProject());
    }

    private void assertOkExecutionGroupInfo(WebElement okExecutionGroup, String groupDescription) {
        assertTrue(scheduleDetail.getOkExecutionIcon(okExecutionGroup).isDisplayed(),
                "OK icon is not shown for OK execution group!");
        assertThat(scheduleDetail.getExecutionDescription(okExecutionGroup).getText(),
                containsString(groupDescription));
        assertTrue(scheduleDetail.getLastExecutionOfOkGroup(okExecutionGroup).isDisplayed(),
                "Last execution of OK group is not shown!");
        System.out.println("Execution runtime of ok execution group: "
                + scheduleDetail.getExecutionRunTime(okExecutionGroup).getText());
        assertTrue(scheduleDetail.getExecutionRunTime(okExecutionGroup).isDisplayed(),
                "Execution runtime is not shown!");
        System.out.println("Execution date of ok execution group: "
                + scheduleDetail.getExecutionDate(okExecutionGroup).getText());
        assertTrue(scheduleDetail.getExecutionDate(okExecutionGroup).isDisplayed(), "Execution date is not shown!");
        System.out.println("Execution time of ok execution group: "
                + scheduleDetail.getExecutionTime(okExecutionGroup).getText());
        assertTrue(scheduleDetail.getExecutionTime(okExecutionGroup).isDisplayed(), "Execution time is not shown!");
        assertTrue(scheduleDetail.getExecutionLog(okExecutionGroup).isDisplayed(), "Execution log is not shown!");
        assertTrue(scheduleDetail.getOkGroupExpandButton(okExecutionGroup).isDisplayed(),
                "OK group expand button is not shown!");
    }

    private void assertExecutionItemsInfo(int okGroupIndex, int okExecutionNumber, String groupDescription) {
        for (int i = okGroupIndex; i < okGroupIndex + okExecutionNumber + 1; i++) {
            if (i == okGroupIndex) {
                WebElement scheduleExecutionItem = scheduleDetail.getExecutionItem(i);
                WebElement executionDescription = scheduleDetail.getExecutionDescription(scheduleExecutionItem);
                assertTrue(scheduleDetail.getOkExecutionIcon(scheduleExecutionItem).isDisplayed(), "OK icon is not shown for OK execution group!");
                System.out.println("Execution description at " + i + " index: " + executionDescription.getText());
                assertThat(executionDescription.getText(), containsString(groupDescription));
                assertFalse(scheduleDetail.isExecutionLogPresent(scheduleExecutionItem),
                        "Execution log is present for expanded group!");
                assertFalse(scheduleDetail.isExecutionTimePresent(scheduleExecutionItem),
                        "Execution time is present for expanded group!");
                assertFalse(scheduleDetail.isExecutionRuntimePresent(scheduleExecutionItem),
                        "Execution runtime is present for expanded group!");
            } else {
                WebElement scheduleExecutionItem = scheduleDetail.getExecutionItem(i);
                WebElement executionTime = scheduleDetail.getExecutionTime(scheduleExecutionItem);
                WebElement executionRunTime = scheduleDetail.getExecutionRunTime(scheduleExecutionItem);
                WebElement executionDate = scheduleDetail.getExecutionDate(scheduleExecutionItem);
                WebElement executionDescription = scheduleDetail.getExecutionDescription(scheduleExecutionItem);

                assertFalse(scheduleDetail.getOkExecutionIcon(scheduleExecutionItem).isDisplayed(), "OK icon is shown for executions in group!");
                System.out.println("Execution description at " + i + " index: " + executionDescription.getText());
                assertTrue(executionDescription.isDisplayed(), "Execution description is not shown!");
                System.out.println("Execution description at " + i + " index: " + executionRunTime.getText());
                assertTrue(executionRunTime.isDisplayed(), "Execution runtime is not shown!");
                System.out.println("Execution description at " + i + " index: " + executionDate.getText());
                assertTrue(executionDate.isDisplayed(), "Execution date is not shown!");
                System.out.println("Execution description at " + i + " index: " + executionTime.getText());
                assertTrue(executionTime.isDisplayed(), "Execution time is not shown!");
                assertTrue(scheduleDetail.getExecutionLog(scheduleExecutionItem).isDisplayed(),
                        "Execution log is not shown!");
            }
        }
    }

    private void checkFocusedProcess(final String processName) {
        Predicate<WebDriver> processIsFocused = browser -> processName.equals(projectDetailPage.getProcessTitle());
        Graphene.waitGui().until(processIsFocused);
    }

    private void cleanProcessesInProject(ProjectInfo projectInfo) {
        openProjectDetailByUrl(projectInfo.getProjectId());
        projectDetailPage.deleteAllProcesses();
    }

    private void assertDeployedProcessInProjects(String processName, List<ProjectInfo> projects,
            DeployPackages deployPackage) {
        for (ProjectInfo project : projects) {
            waitForElementVisible(discProjectsList.getRoot());
            discProjectsList.clickOnProjectTitle(project);
            waitForElementVisible(projectDetailPage.getRoot());
            projectDetailPage.activeProcess(processName);
            projectDetailPage.clickOnScheduleTab();
            assertEquals("0 schedules", projectDetailPage.getScheduleTabTitle());
            projectDetailPage.clickOnExecutableTab();
            List<Executables> executables = deployPackage.getExecutables();
            String executableTitle =
                    deployPackage.getPackageType().getProcessTypeExecutable()
                            + (executables.size() > 1 ? "s" : "");
            assertEquals(String.format("%d %s total", executables.size(), executableTitle),
                    projectDetailPage.getExecutableTabTitle(), "Incorrect executable tab title!");
            assertTrue(projectDetailPage.isCorrectExecutableList(executables), "Incorrect executable list!");

            Screenshots.takeScreenshot(browser, "assert-deployed-process-" + processName, getClass());
            discNavigation.clickOnProjectsButton();
        }
    }
}
