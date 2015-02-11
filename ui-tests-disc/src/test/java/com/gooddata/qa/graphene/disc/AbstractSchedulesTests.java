package com.gooddata.qa.graphene.disc;

import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.enums.disc.DeployPackages;
import com.gooddata.qa.graphene.enums.disc.DeployPackages.Executables;
import com.gooddata.qa.graphene.enums.disc.OverviewProjectStates;
import com.gooddata.qa.graphene.enums.disc.ScheduleCronTimes;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

public abstract class AbstractSchedulesTests extends AbstractDISCTest {

    protected void assertBrokenSchedule(ScheduleBuilder scheduleBuilder) {
        waitForElementVisible(brokenSchedulesTable.getRoot());
        projectDetailPage.assertScheduleInList(brokenSchedulesTable, scheduleBuilder);
    }
    
    protected void checkScheduleNameInOverviewPage(OverviewProjectStates overviewState) {
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
                            .setHourInDay("23").setMinuteInHour("59").setConfirmed(true);
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
            discOverviewProjects.assertOverviewCustomScheduleName(overviewState, getWorkingProject(), scheduleBuilder);
        } finally {
            cleanProcessesInWorkingProject();
        }
    }

    protected void prepareDataForTriggerScheduleTest(ScheduleBuilder triggerScheduleBuilder,
            ScheduleBuilder dependentScheduleBuilder) {
        verifyScheduleForTriggerScheduleTest(triggerScheduleBuilder);
        verifyScheduleForTriggerScheduleTest(dependentScheduleBuilder);

        openProjectDetailByUrl(getWorkingProject().getProjectId());
        
        String triggerScheduleProcess = triggerScheduleBuilder.getProcessName();
        deployInProjectDetailPage(triggerScheduleBuilder.getExecutable().getExecutablePackage(),
                triggerScheduleProcess);
        String dependentScheduleProcess = dependentScheduleBuilder.getProcessName();
        if (!triggerScheduleProcess.equals(dependentScheduleProcess))
            deployInProjectDetailPage(dependentScheduleBuilder.getExecutable()
                    .getExecutablePackage(), dependentScheduleProcess);

        triggerScheduleBuilder.setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setHourInDay("23")
                .setMinuteInHour("59");
        createAndAssertSchedule(triggerScheduleBuilder);
        triggerScheduleBuilder.setScheduleUrl(browser.getCurrentUrl());

        dependentScheduleBuilder
                .setCronTime(ScheduleCronTimes.AFTER)
                .setTriggerScheduleGroup(triggerScheduleProcess)
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

    private void verifyScheduleForTriggerScheduleTest(ScheduleBuilder scheduleBuilder) {
        assertFalse(scheduleBuilder == null);
        assertFalse(scheduleBuilder.getProcessName() == null);
        assertFalse(scheduleBuilder.getExecutable() == null);
    }
}
