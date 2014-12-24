package com.gooddata.qa.graphene.disc;

import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.enums.disc.DeployPackages;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public abstract class AbstractSchedulesTests extends AbstractDeployProcesses {

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
}
