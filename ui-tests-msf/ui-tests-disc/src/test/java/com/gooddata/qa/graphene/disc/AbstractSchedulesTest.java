package com.gooddata.qa.graphene.disc;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.openqa.selenium.By;

import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.enums.disc.DeployPackages;
import com.gooddata.qa.graphene.enums.disc.DeployPackages.Executables;
import com.gooddata.qa.graphene.enums.disc.OverviewProjectStates;
import com.gooddata.qa.graphene.enums.disc.ScheduleCronTimes;

public abstract class AbstractSchedulesTest extends AbstractDISCTest {

    private static final String EMPTY_SCHEDULE_NAME_ERROR = "can't be blank";
    private static final String INVALID_SCHEDULE_NAME_ERROR =
            "\'${scheduleName}\' name already in use within the process. Change the name.";
    protected static final String EMPTY_SCHEDULE_TRIGGER_MESSAGE = "Schedules cannot be scheduled in a loop";
    private static final String INVALID_SCHEDULE_TITLE_ERROR =
            "\'${scheduleName}\' name already in use within the process. Change the name.";
    private static final String EMPTY_SCHEDULE_TITLE_ERROR = "can't be blank";

    protected static By BY_ERROR_BUBBLE = By.cssSelector(".bubble.isActive");

    protected void createScheduleWithInvalidScheduleName(ScheduleBuilder scheduleBuilder, String validScheduleName) {
        waitForFragmentVisible(scheduleForm).createNewSchedule(scheduleBuilder);
        String errorBubbleMessage = waitForElementVisible(BY_ERROR_BUBBLE, browser).getText();
        if (scheduleBuilder.getScheduleName().isEmpty()) {
            assertTrue(scheduleForm.isScheduleNameErrorShown(), "Error is not shown for empty schedule name!");
            assertEquals(errorBubbleMessage, EMPTY_SCHEDULE_NAME_ERROR,
                    "Incorrect error message for empty schedule name!");
        } else {
            assertTrue(scheduleForm.isScheduleNameErrorShown(), "Error is not shown for invalid schedule name!");
            assertEquals(errorBubbleMessage,
                    INVALID_SCHEDULE_NAME_ERROR.replace("${scheduleName}", scheduleBuilder.getScheduleName()),
                    "Incorrect error message for invalid schedule name!");
        }
        scheduleForm.setCustomScheduleName(scheduleBuilder.setScheduleName(validScheduleName));
        scheduleForm.clickOnSaveScheduleButton();
    }

    protected void checkNoTriggerScheduleOptions() {
        waitForFragmentVisible(scheduleForm);
        scheduleForm.selectCronType(ScheduleCronTimes.AFTER);
        assertEquals(scheduleForm.getTriggerScheduleMessage(), EMPTY_SCHEDULE_TRIGGER_MESSAGE);
        System.out.println("No trigger schedule message: " + scheduleForm.getTriggerScheduleMessage());
        browser.navigate().refresh();
    }

    protected void changeInvalidScheduleName(String invalidScheduleName) {
        scheduleDetail.changeScheduleName(invalidScheduleName);
        scheduleDetail.saveEditedScheduleTitle();
        assertTrue(scheduleDetail.isErrorScheduleTitle(), "Error is not shown for invalid schedule title!");
        String errorBubbleMessage = waitForElementVisible(BY_ERROR_BUBBLE, browser).getText();
        if (invalidScheduleName.isEmpty())
            assertEquals(errorBubbleMessage, EMPTY_SCHEDULE_TITLE_ERROR,
                    "Incorrect error message for empty schedule name!");
        else
            assertEquals(errorBubbleMessage,
                    INVALID_SCHEDULE_TITLE_ERROR.replace("${scheduleName}", invalidScheduleName),
                    "Incorrect error message for invalid schedule name!");

        scheduleDetail.clickOnCloseScheduleButton();
    }

    protected void assertBrokenSchedule(ScheduleBuilder scheduleBuilder) {
        waitForElementVisible(brokenSchedulesTable.getRoot());
        assertTrue(projectDetailPage.isCorrectScheduleInList(brokenSchedulesTable, scheduleBuilder),
                "Incorrect schedule in list!");
    }

    protected void checkScheduleNameInOverviewPage(OverviewProjectStates overviewState) {
        try {
            openProjectDetailPage(testParams.getProjectId());

            String processName = "Check Custom Schedule Name In Overview Page";
            String scheduleName = "Custom Schedule Name";
            deployInProjectDetailPage(DeployPackages.BASIC, processName);

            Executables graph = null;
            switch (overviewState) {
                case FAILED:
                    graph = Executables.FAILED_GRAPH;
                    break;
                case SUCCESSFUL:
                    graph = Executables.SUCCESSFUL_GRAPH;
                    break;
                case RUNNING:
                    graph = Executables.LONG_TIME_RUNNING_GRAPH;
                    break;
                default:
                    graph = Executables.SUCCESSFUL_GRAPH;
                    break;
            }
            ScheduleBuilder scheduleBuilder =
                    new ScheduleBuilder().setProcessName(processName).setScheduleName(scheduleName)
                            .setExecutable(graph).setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setHourInDay("23")
                            .setMinuteInHour("59").setConfirmed(true);
            createAndAssertSchedule(scheduleBuilder);

            scheduleDetail.manualRun();

            switch (overviewState) {
                case RUNNING:
                    assertTrue(scheduleDetail.isInRunningState());
                    break;
                case FAILED:
                    assertFailedExecution(scheduleBuilder.getExecutable());
                    break;
                case SUCCESSFUL:
                    assertSuccessfulExecution();
                    break;
                default:
                    break;
            }
            scheduleBuilder.setScheduleUrl(browser.getCurrentUrl());

            openUrl(DISC_OVERVIEW_PAGE);
            waitForFragmentVisible(discOverview);
            discOverview.selectOverviewState(overviewState);
            waitForFragmentVisible(discOverviewProjects);
            assertOverviewCustomScheduleName(overviewState, testParams.getProjectId(), scheduleBuilder);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    protected void prepareDataForTriggerScheduleTest(ScheduleBuilder triggerScheduleBuilder,
            ScheduleBuilder dependentScheduleBuilder) {
        verifyScheduleForTriggerScheduleTest(triggerScheduleBuilder);
        verifyScheduleForTriggerScheduleTest(dependentScheduleBuilder);

        openProjectDetailPage(testParams.getProjectId());

        String triggerScheduleProcess = triggerScheduleBuilder.getProcessName();
        deployInProjectDetailPage(triggerScheduleBuilder.getExecutable().getExecutablePackage(),
                triggerScheduleProcess);
        String dependentScheduleProcess = dependentScheduleBuilder.getProcessName();
        if (!triggerScheduleProcess.equals(dependentScheduleProcess))
            deployInProjectDetailPage(dependentScheduleBuilder.getExecutable().getExecutablePackage(),
                    dependentScheduleProcess);

        triggerScheduleBuilder.setCronTime(ScheduleCronTimes.CRON_EVERYDAY);
        createAndAssertSchedule(triggerScheduleBuilder);
        triggerScheduleBuilder.setScheduleUrl(browser.getCurrentUrl());

        dependentScheduleBuilder.setCronTime(ScheduleCronTimes.AFTER)
                .setTriggerScheduleGroup(triggerScheduleProcess)
                .setTriggerScheduleOption(triggerScheduleBuilder.getScheduleName());
        createAndAssertSchedule(dependentScheduleBuilder);
        dependentScheduleBuilder.setScheduleUrl(browser.getCurrentUrl());
    }

    protected void manualRunTriggerSchedule(String scheduleUrl) {
        browser.get(scheduleUrl);
        waitForFragmentVisible(scheduleDetail);
        scheduleDetail.manualRun();
    }

    protected void runSuccessfulTriggerSchedule(String scheduleUrl) {
        manualRunTriggerSchedule(scheduleUrl);
        assertSuccessfulExecution();
    }

    protected int waitForAutoRunDependentSchedule(ScheduleBuilder scheduleBuilder) {
        browser.get(scheduleBuilder.getScheduleUrl());
        waitForFragmentVisible(scheduleDetail);
        scheduleDetail.tryToWaitForAutoRun(scheduleBuilder.getCronTimeBuilder());
        return scheduleDetail.getExecutionItemsNumber();
    }

    private void verifyScheduleForTriggerScheduleTest(ScheduleBuilder scheduleBuilder) {
        assertFalse(scheduleBuilder == null, "Schedule builder is null!");
        assertFalse(scheduleBuilder.getProcessName() == null, "Process name is null!");
        assertFalse(scheduleBuilder.getExecutable() == null, "Executable is null!");
    }
}
