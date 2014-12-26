package com.gooddata.qa.graphene.disc;

import org.json.JSONException;

import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.enums.disc.DeployPackages;
import com.gooddata.qa.graphene.enums.disc.DeployPackages.Executables;
import com.gooddata.qa.graphene.enums.disc.OverviewProjectStates;
import com.gooddata.qa.graphene.enums.disc.ScheduleCronTimes;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.assertTrue;

public abstract class AbstractSchedulesTests extends AbstractDeployProcesses {

    protected static final String DISC_OVERVIEW_PAGE = "admin/disc/#/overview";

    protected void assertBrokenSchedule(ScheduleBuilder scheduleBuilder) {
        waitForElementVisible(brokenSchedulesTable.getRoot());
        projectDetailPage.assertScheduleInList(brokenSchedulesTable, scheduleBuilder);
    }

    protected void createSchedule(ScheduleBuilder scheduleBuilder) {
        projectDetailPage.clickOnNewScheduleButton();
        waitForElementVisible(scheduleForm.getRoot());
        scheduleForm.createNewSchedule(scheduleBuilder);
    }

    protected void assertSchedule(ScheduleBuilder scheduleBuilder) {
        projectDetailPage.checkFocusedProcess(scheduleBuilder.getProcessName());
        waitForElementVisible(schedulesTable.getRoot());
        projectDetailPage.assertScheduleInList(schedulesTable, scheduleBuilder);
        schedulesTable.getScheduleTitle(scheduleBuilder.getScheduleName()).click();
        waitForElementVisible(scheduleDetail.getRoot());

        scheduleDetail.assertSchedule(scheduleBuilder);
    }

    protected void createAndAssertSchedule(ScheduleBuilder scheduleBuilder) {
        createSchedule(scheduleBuilder);
        assertSchedule(scheduleBuilder);
    }

    protected void prepareScheduleWithBasicPackage(ScheduleBuilder scheduleBuilder) {
        deployInProjectDetailPage(DeployPackages.BASIC, scheduleBuilder.getProcessName());
        createAndAssertSchedule(scheduleBuilder);
        scheduleBuilder.setScheduleUrl(browser.getCurrentUrl());
    }

    protected void checkScheduleNameInOverviewPage(OverviewProjectStates overviewState)
            throws JSONException {
        try {
            openProjectDetailByUrl(getWorkingProject().getProjectId());

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
                            .setExecutable(graph).setCronTime(ScheduleCronTimes.CRON_EVERYDAY)
                            .setHourInDay("23").setMinuteInHour("59").isConfirm();
            createAndAssertSchedule(scheduleBuilder);

            scheduleDetail.manualRun();

            switch (overviewState) {
                case RUNNING:
                    assertTrue(scheduleDetail.isInRunningState());
                    break;
                case FAILED:
                    scheduleDetail.assertFailedExecution(scheduleBuilder.getExecutable());
                    break;
                case SUCCESSFUL:
                    scheduleDetail.assertSuccessfulExecution();
                    break;
                default:
                    break;
            }
            scheduleBuilder.setScheduleUrl(browser.getCurrentUrl());

            openUrl(DISC_OVERVIEW_PAGE);
            waitForElementVisible(discOverview.getRoot());
            discOverview.selectOverviewState(overviewState);
            waitForElementVisible(discOverviewProjects.getRoot());
            discOverviewProjects.assertOverviewScheduleName(overviewState, getWorkingProject(),
                    scheduleBuilder.getScheduleUrl(), scheduleName);
        } finally {
            cleanProcessesInProjectDetail(testParams.getProjectId());
        }
    }

    protected void prepareDataForTriggerScheduleTest(String processName,
            ScheduleBuilder triggerScheduleBuilder, ScheduleBuilder dependentScheduleBuilder) {
        openProjectDetailByUrl(getWorkingProject().getProjectId());

        deployInProjectDetailPage(DeployPackages.BASIC, processName);

        triggerScheduleBuilder.setProcessName(processName)
                .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setHourInDay("23")
                .setMinuteInHour("59");
        createAndAssertSchedule(triggerScheduleBuilder);
        triggerScheduleBuilder.setScheduleUrl(browser.getCurrentUrl());

        dependentScheduleBuilder
                .setProcessName(processName)
                .setCronTime(ScheduleCronTimes.AFTER)
                .setTriggerScheduleGroup(processName)
                .setTriggerScheduleOption(
                        triggerScheduleBuilder.getExecutable().getExecutablePath());
        createAndAssertSchedule(dependentScheduleBuilder);
        dependentScheduleBuilder.setScheduleUrl(browser.getCurrentUrl());
    }

    protected void manualRunTriggerSchedule(String scheduleUrl) {
        browser.get(scheduleUrl);
        waitForElementVisible(scheduleDetail.getRoot());
        scheduleDetail.manualRun();
    }

    protected void runSuccessfulTriggerSchedule(String scheduleUrl) {
        manualRunTriggerSchedule(scheduleUrl);
        scheduleDetail.assertSuccessfulExecution();
    }

    protected int waitForAutoRunDependentSchedule(ScheduleBuilder scheduleBuilder) {
        browser.get(scheduleBuilder.getScheduleUrl());
        waitForElementVisible(scheduleDetail.getRoot());
        scheduleDetail.tryToWaitForAutoRun(scheduleBuilder.getCronTimeBuilder());
        return scheduleDetail.getExecutionItemsNumber();
    }
}
