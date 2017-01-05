package com.gooddata.qa.graphene.disc.schedule;

import static com.gooddata.qa.graphene.utils.ElementUtils.getBubbleMessage;
import static com.gooddata.qa.utils.http.process.ProcessRestUtils.deteleProcess;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.Schedule;
import com.gooddata.qa.graphene.disc.__AbstractDISCTest;
import com.gooddata.qa.graphene.enums.disc.__Executable;
import com.gooddata.qa.graphene.enums.disc.__ScheduleCronTime;
import com.gooddata.qa.graphene.fragments.disc.process.ProcessDetail.Tab;
import com.gooddata.qa.graphene.fragments.disc.schedule.CreateScheduleForm;
import com.gooddata.qa.graphene.fragments.disc.schedule.CronEditor;
import com.gooddata.qa.graphene.fragments.disc.schedule.__ScheduleDetailFragment;

public class CreateScheduleTest extends __AbstractDISCTest {

    @Test(dependsOnGroups = {"createProject"})
    public void createSchedule() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            ((CreateScheduleForm) __initDiscProjectDetailPage()
                    .openCreateScheduleForm()
                    .selectExecutable(__Executable.SUCCESSFUL_GRAPH))
                    .schedule();
            assertTrue(projectDetailPage.getProcess(process.getName())
                    .hasSchedule(__Executable.SUCCESSFUL_GRAPH.getName()),
                    "Schedule is not created");

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void cancelCreateSchedule() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            String scheduleName = "Schedule-" + generateHashString();
            __initDiscProjectDetailPage()
                    .openCreateScheduleForm()
                    .enterScheduleName(scheduleName)
                    .cancelSchedule();
            assertFalse(projectDetailPage.getProcess(process.getName()).hasSchedule(scheduleName),
                    "Schedule " + scheduleName + " is created");

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createScheduleWithCustomInput() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Map<String, String> params = createRandomParams();
            Map<String, String> secureParams = createRandomParams();

            ((CreateScheduleForm) __initDiscProjectDetailPage()
                    .openCreateScheduleForm()
                    .addParameters(params)
                    .addSecureParameters(secureParams))
                    .schedule();

            __ScheduleDetailFragment scheduleDetail = __ScheduleDetailFragment.getInstance(browser);
            assertEquals(scheduleDetail.getAllParametersInfo(), params);

            Map<String, String> actualSecureParams = scheduleDetail.getAllSecureParametersInfo();
            assertEquals(actualSecureParams.keySet(), secureParams.keySet());
            assertTrue(actualSecureParams.values().stream().allMatch(value -> value.equals("")),
                    "Secure value is not hidden!");

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createScheduleForSpecificExecutable() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            String scheduleName = "Schedule-" + generateHashString();

            __initDiscProjectDetailPage()
                    .getProcess(process.getName())
                    .openTab(Tab.EXECUTABLE)
                    .clickScheduleLinkFrom(__Executable.SUCCESSFUL_GRAPH)
                    .enterScheduleName(scheduleName)
                    .schedule();

            assertEquals(__ScheduleDetailFragment.getInstance(browser).getSelectedExecutable(),
                    __Executable.SUCCESSFUL_GRAPH);
            assertTrue(projectDetailPage.getProcess(process.getName()).hasSchedule(scheduleName),
                    "Schedule " + scheduleName + " is not created");

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createScheduleFromSchedulesList() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            String scheduleName = "Schedule-" + generateHashString();

            __initDiscProjectDetailPage()
                    .getProcess(process.getName())
                    .openTab(Tab.SCHEDULE)
                    .clickCreateScheduleLink()
                    .enterScheduleName(scheduleName)
                    .schedule();
            assertTrue(projectDetailPage.getProcess(process.getName()).hasSchedule(scheduleName),
                    "Schedule " + scheduleName + " is not created");

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createScheduleWithEveryWeekCronTime() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            LocalDateTime date = LocalDateTime.now();

            ((CreateScheduleForm) __initDiscProjectDetailPage()
                    .openCreateScheduleForm()
                    .selectRunTimeByEveryWeek(date.getDayOfWeek(), date.getHour(), date.getMinute()))
                    .schedule();

            CronEditor cronEditor = __ScheduleDetailFragment.getInstance(browser).getCronEditor();
            assertEquals(cronEditor.getSelectedCronType(), __ScheduleCronTime.EVERY_WEEK);
            assertEquals(cronEditor.getSelectedDayOfWeek(), date.getDayOfWeek());
            assertEquals(cronEditor.getSelectedHourOfDay(), date.getHour());
            assertEquals(cronEditor.getSelectedMinuteOfHour(), date.getMinute());

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createScheduleWithEveryDayCronTime() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            LocalTime time = LocalTime.now();

            ((CreateScheduleForm) __initDiscProjectDetailPage()
                    .openCreateScheduleForm()
                    .selectRunTimeByEveryDay(time.getHour(), time.getMinute()))
                    .schedule();

            CronEditor cronEditor = __ScheduleDetailFragment.getInstance(browser).getCronEditor();
            assertEquals(cronEditor.getSelectedCronType(), __ScheduleCronTime.EVERY_DAY);
            assertEquals(cronEditor.getSelectedHourOfDay(), time.getHour());
            assertEquals(cronEditor.getSelectedMinuteOfHour(), time.getMinute());

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createScheduleWithEveryHourCronTime() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            LocalTime time = LocalTime.now();

            ((CreateScheduleForm) __initDiscProjectDetailPage()
                    .openCreateScheduleForm()
                    .selectRunTimeByEveryHour(time.getMinute()))
                    .schedule();

            CronEditor cronEditor = __ScheduleDetailFragment.getInstance(browser).getCronEditor();
            assertEquals(cronEditor.getSelectedCronType(), __ScheduleCronTime.EVERY_HOUR);
            assertEquals(cronEditor.getSelectedMinuteOfHour(), time.getMinute());

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createScheduleWithCronExpression() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            String cronExpression = "*/20 * * * *";

            ((CreateScheduleForm) __initDiscProjectDetailPage()
                    .openCreateScheduleForm()
                    .selectRunTimeByCronExpression(cronExpression))
                    .schedule();

            CronEditor cronEditor = __ScheduleDetailFragment.getInstance(browser).getCronEditor();
            assertEquals(cronEditor.getSelectedCronType(), __ScheduleCronTime.CRON_EXPRESSION);
            assertEquals(cronEditor.getCronExpression(), cronExpression);

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createScheduleWithIncorrectCron() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            CreateScheduleForm scheduleForm = __initDiscProjectDetailPage().openCreateScheduleForm();
            scheduleForm.selectRunTimeByCronExpression("* * *");
            scheduleForm.schedule();

            assertTrue(scheduleForm.getCronEditor().isCronExpressionInputError(),
                    "Cron expression input not show error");
            assertEquals(getBubbleMessage(browser), "Inserted cron format is invalid. Please verify and try again.");

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createScheduleTriggeredByAnotherSchedule() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule triggeringSchedule = createSchedule(process, __Executable.SUCCESSFUL_GRAPH,
                    __ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            ((CreateScheduleForm) __initDiscProjectDetailPage()
                    .openCreateScheduleForm()
                    .selectRunTimeByTriggeringSchedule(triggeringSchedule.getId()))
                    .schedule();

            CronEditor cronEditor = __ScheduleDetailFragment.getInstance(browser).getCronEditor();
            assertEquals(cronEditor.getSelectedCronType(), __ScheduleCronTime.AFTER);
            assertEquals(cronEditor.getTriggeringSchedule(), triggeringSchedule.getName());

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createScheduleWithCustomName() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            String scheduleName = "Schedule-" + generateHashString();
            __initDiscProjectDetailPage()
                    .openCreateScheduleForm()
                    .enterScheduleName(scheduleName)
                    .schedule();
            assertTrue(projectDetailPage.getProcess(process.getName()).hasSchedule(scheduleName),
                    "Schedule is not created");

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createScheduleWithEmptyCustomName() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            CreateScheduleForm scheduleForm = __initDiscProjectDetailPage()
                    .openCreateScheduleForm()
                    .enterScheduleName("");
            assertTrue(scheduleForm.isScheduleNameInputError(), "Schedule name input not show error");
            assertEquals(getBubbleMessage(browser), "can't be blank");

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createScheduleNotUniqueName() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            createSchedule(process, __Executable.SUCCESSFUL_GRAPH, __ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            CreateScheduleForm scheduleForm = __initDiscProjectDetailPage().openCreateScheduleForm()
                    .enterScheduleName(__Executable.SUCCESSFUL_GRAPH.getName());
            assertTrue(scheduleForm.isScheduleNameInputError(), "Schedule name input not show error");
            assertEquals(getBubbleMessage(browser),
                    format("'%s' name already in use within the process. Change the name.",
                            __Executable.SUCCESSFUL_GRAPH.getName()));

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @SuppressWarnings("serial")
    private Map<String, String> createRandomParams() {
        return new HashMap<String, String>() {{
            put("param" + generateHashString(), "value" + generateHashString());
            put("param" + generateHashString(), "value" + generateHashString());
        }};
    }
}
