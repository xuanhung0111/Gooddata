package com.gooddata.qa.graphene.disc.schedule;

import static com.gooddata.qa.graphene.utils.ElementUtils.getBubbleMessage;
import static com.gooddata.qa.utils.http.process.ProcessRestUtils.deteleProcess;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.testng.annotations.Test;

import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.Schedule;
import com.gooddata.qa.graphene.disc.__AbstractDISCTest;
import com.gooddata.qa.graphene.enums.disc.__Executable;
import com.gooddata.qa.graphene.enums.disc.__ScheduleCronTime;
import com.gooddata.qa.graphene.fragments.disc.schedule.CreateScheduleForm;
import com.gooddata.qa.graphene.fragments.disc.schedule.__ScheduleDetailFragment;

public class EditScheduleTest extends __AbstractDISCTest {

    @Test(dependsOnGroups = {"createProject"})
    public void editScheduleWithCustomName() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, __Executable.ERROR_GRAPH,
                    __ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            String customName = "Schedule-" + generateHashString();
            __ScheduleDetailFragment scheduleDetail = initScheduleDetail(schedule)
                    .editNameByClickOnTitle(customName).cancelChanges();

            assertEquals(scheduleDetail.getName(), __Executable.ERROR_GRAPH.getName());
            assertTrue(projectDetailPage.getProcess(process.getName()).hasSchedule(__Executable.ERROR_GRAPH.getName()),
                    "Schedule name is edited!");

            scheduleDetail.editNameByClickOnTitle(customName).saveChanges();
            assertEquals(scheduleDetail.getName(), customName);
            assertTrue(projectDetailPage.getProcess(process.getName()).hasSchedule(customName),
                    "Schedule is not edited");

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void editScheduleNameWithPencilIcon() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, __Executable.SUCCESSFUL_GRAPH,
                    __ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            String customName = "Schedule-" + generateHashString();
            __ScheduleDetailFragment scheduleDetail = initScheduleDetail(schedule)
                    .editNameByClickOnEditIcon(customName)
                    .saveChanges();
            assertEquals(scheduleDetail.getName(), customName);

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void editScheduleWithEmptyCustomName() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, __Executable.SUCCESSFUL_GRAPH,
                    __ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            __ScheduleDetailFragment scheduleDetail = initScheduleDetail(schedule).editNameByClickOnTitle("");
            assertTrue(scheduleDetail.isNameInputError(), "Schedule name input not show error");
            assertEquals(getBubbleMessage(browser), "can't be blank");

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void editScheduleWithNotUniqueName() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            createSchedule(process, __Executable.SUCCESSFUL_GRAPH, __ScheduleCronTime.EVERY_30_MINUTES.getExpression());
            Schedule schedule = createSchedule(process, __Executable.ERROR_GRAPH,
                    __ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            __ScheduleDetailFragment scheduleDetail = initScheduleDetail(schedule)
                    .editNameByClickOnTitle(__Executable.SUCCESSFUL_GRAPH.getName());

            assertTrue(scheduleDetail.isNameInputError(), "Schedule name input not show error");
            assertEquals(getBubbleMessage(browser),
                    format("'%s' name already in use within the process. Change the name.",
                            __Executable.SUCCESSFUL_GRAPH.getName()));

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void editScheduleExecutable() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, __Executable.SUCCESSFUL_GRAPH,
                    __ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            __ScheduleDetailFragment scheduleDetail = initScheduleDetail(schedule);
            scheduleDetail.selectExecutable(__Executable.ERROR_GRAPH);
            scheduleDetail.cancelChanges();
            assertEquals(scheduleDetail.getSelectedExecutable(), __Executable.SUCCESSFUL_GRAPH);

            scheduleDetail.selectExecutable(__Executable.ERROR_GRAPH);
            scheduleDetail.saveChanges();
            assertEquals(scheduleDetail.getSelectedExecutable(), __Executable.ERROR_GRAPH);

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void editScheduleRunTime() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, __Executable.SUCCESSFUL_GRAPH,
                    __ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            __ScheduleDetailFragment scheduleDetail = initScheduleDetail(schedule);
            scheduleDetail.selectRunTime(__ScheduleCronTime.EVERY_15_MINUTES);
            scheduleDetail.cancelChanges();
            assertEquals(scheduleDetail.getCronEditor().getSelectedCronType(), __ScheduleCronTime.EVERY_30_MINUTES);

            scheduleDetail.selectRunTime(__ScheduleCronTime.EVERY_15_MINUTES);
            scheduleDetail.saveChanges();
            assertEquals(scheduleDetail.getCronEditor().getSelectedCronType(), __ScheduleCronTime.EVERY_15_MINUTES);

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void editScheduleWithIncorrectCronExpression() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, __Executable.SUCCESSFUL_GRAPH,
                    __ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            __ScheduleDetailFragment scheduleDetail = initScheduleDetail(schedule);
            scheduleDetail.selectRunTimeByCronExpression("* * *");
            scheduleDetail.clickSaveButton();

            assertTrue(scheduleDetail.getCronEditor().isCronExpressionInputError(),
                    "Cron expression input not show error");
            assertEquals(getBubbleMessage(browser), "Inserted cron format is invalid. Please verify and try again.");

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void editScheduleParameters() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Pair<String, String> param = createRandomParam();
            Pair<String, String> secureParam = createRandomParam();

            ((CreateScheduleForm) __initDiscProjectDetailPage()
                    .openCreateScheduleForm()
                    .addParameter(param.getKey(), param.getValue())
                    .addSecureParameter(secureParam.getKey(), secureParam.getValue()))
                    .schedule();

            Pair<String, String> editedParam = createRandomParam();
            Pair<String, String> editedSecureParam = createRandomParam();

            __ScheduleDetailFragment scheduleDetail = __ScheduleDetailFragment.getInstance(browser);
            scheduleDetail.getParameter(param.getKey()).editNameValuePair(editedParam.getKey(), editedParam.getValue());
            scheduleDetail.getParameter(secureParam.getKey()).editNameValuePair(editedSecureParam.getKey(), editedSecureParam.getValue());
            scheduleDetail.saveChanges();

            Map<String, String> actualParam = scheduleDetail.getAllParametersInfo();
            assertEquals(actualParam.size(), 1);
            assertThat(scheduleDetail.getAllParametersInfo().entrySet(), hasItem(editedParam));

            Map<String, String> actualSecureParam = scheduleDetail.getAllSecureParametersInfo();
            assertEquals(actualSecureParam.size(), 1);
            assertEquals(actualSecureParam.get(editedSecureParam.getKey()), "");

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void cancelEditScheduleParameters() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Pair<String, String> param = createRandomParam();
            Pair<String, String> secureParam = createRandomParam();

            ((CreateScheduleForm) __initDiscProjectDetailPage()
                    .openCreateScheduleForm()
                    .addParameter(param.getKey(), param.getValue())
                    .addSecureParameter(secureParam.getKey(), secureParam.getValue()))
                    .schedule();

            Pair<String, String> editedParam = createRandomParam();
            Pair<String, String> editedSecureParam = createRandomParam();

            __ScheduleDetailFragment scheduleDetail = __ScheduleDetailFragment.getInstance(browser);
            scheduleDetail.getParameter(param.getKey()).editNameValuePair(editedParam.getKey(), editedParam.getValue());
            scheduleDetail.getParameter(secureParam.getKey()).editNameValuePair(editedSecureParam.getKey(), editedSecureParam.getValue());
            scheduleDetail.cancelChanges();

            assertFalse(scheduleDetail.hasParameter(editedParam.getKey()), "Param is edited after cancel");
            assertFalse(scheduleDetail.hasParameter(editedSecureParam.getKey()), "Param is edited after cancel");

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @SuppressWarnings("unchecked")
    @Test(dependsOnGroups = {"createProject"})
    public void addNewParametersForSchedule() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Pair<String, String> param = createRandomParam();
            Pair<String, String> secureParam = createRandomParam();

            ((CreateScheduleForm) __initDiscProjectDetailPage()
                    .openCreateScheduleForm()
                    .addParameter(param.getKey(), param.getValue())
                    .addSecureParameter(secureParam.getKey(), secureParam.getValue()))
                    .schedule();

            Pair<String, String> additionalParam = createRandomParam();
            Pair<String, String> additionalSecureParam = createRandomParam();

            __ScheduleDetailFragment scheduleDetail = __ScheduleDetailFragment.getInstance(browser);
            scheduleDetail.addParameter(additionalParam.getKey(), additionalParam.getValue())
                    .addSecureParameter(additionalSecureParam.getKey(), additionalSecureParam.getValue());
            scheduleDetail.saveChanges();

            Map<String, String> actualParams = scheduleDetail.getAllParametersInfo();
            assertEquals(actualParams.size(), 2);
            assertThat(actualParams.entrySet(), hasItems(param, additionalParam));

            Map<String, String> actualSecureParams = scheduleDetail.getAllSecureParametersInfo();
            assertEquals(actualSecureParams.size(), 2);
            assertTrue(actualSecureParams.containsKey(secureParam.getKey()) &&
                    actualSecureParams.containsKey(additionalSecureParam.getKey()),
                    "Secure param not added correctly");
            assertTrue(actualSecureParams.values().stream().allMatch(value -> value.equals("")),
                    "Secure value is not hidden!");

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void deleteScheduleParams() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Pair<String, String> param = createRandomParam();

            ((CreateScheduleForm) __initDiscProjectDetailPage()
                    .openCreateScheduleForm()
                    .addParameter(param.getKey(), param.getValue()))
                    .schedule();

            __ScheduleDetailFragment scheduleDetail = __ScheduleDetailFragment.getInstance(browser);
            scheduleDetail.deleteParameter(param.getKey());
            scheduleDetail.cancelChanges();
            assertTrue(scheduleDetail.hasParameter(param.getKey()), "Parameter is deleted after cancel");

            scheduleDetail.deleteParameter(param.getKey());
            scheduleDetail.saveChanges();
            assertFalse(scheduleDetail.hasParameter(param.getKey()), "Parameter is not deleted");

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void addRetryDelay() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, __Executable.SUCCESSFUL_GRAPH,
                    __ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            int retryDelayInMinute = 60;
            __ScheduleDetailFragment scheduleDetail = initScheduleDetail(schedule)
                    .addRetryDelay(retryDelayInMinute).cancelChanges();
            assertFalse(scheduleDetail.hasRetryDelay(), "Retry delay is added");

            scheduleDetail.addRetryDelay(retryDelayInMinute).saveChanges();
            assertEquals(scheduleDetail.getRetryDelayValue(), retryDelayInMinute);

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void addIncorrectRetryDelay() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, __Executable.SUCCESSFUL_GRAPH,
                    __ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            __ScheduleDetailFragment scheduleDetail = initScheduleDetail(schedule)
                    .addRetryDelay(5)
                    .clickSaveButton();
            assertTrue(scheduleDetail.isRetryDelayInputError(), "Retry delay input not show error");
            assertEquals(getBubbleMessage(browser), "The minimal delay is every 15 minutes.\nUse numbers only.");

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void deleteRetryDelay() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, __Executable.SUCCESSFUL_GRAPH,
                    __ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            __ScheduleDetailFragment scheduleDetail = initScheduleDetail(schedule)
                    .addRetryDelay(15).saveChanges();

            scheduleDetail.clickDeleteRetryDelay().discard();
            assertTrue(scheduleDetail.hasRetryDelay(), "Retry delay is deleted");

            scheduleDetail.deleteRetryDelay();
            assertFalse(scheduleDetail.hasRetryDelay(), "Retry delay is not deleted");

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    private Pair<String, String> createRandomParam() {
        return Pair.of("param" + generateHashString(), "value" + generateHashString());
    }
}
