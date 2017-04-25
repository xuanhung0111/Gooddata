package com.gooddata.qa.graphene;

import com.gooddata.dataload.processes.Schedule;
import com.gooddata.qa.graphene.fragments.disc.schedule.ScheduleDetail;

public class AbstractDiscTest extends AbstractProcessTest {

    protected ScheduleDetail initScheduleDetail(Schedule schedule) {
        return initDiscProjectDetailPage().getProcessById(schedule.getProcessId()).openSchedule(schedule.getName());
    }

    protected void executeScheduleWithSpecificTimes(ScheduleDetail scheduleDetail, int times) {
        for (int i = 1; i <= times; i++) {
            scheduleDetail.executeSchedule().waitForExecutionFinish();
        }
    }
}
