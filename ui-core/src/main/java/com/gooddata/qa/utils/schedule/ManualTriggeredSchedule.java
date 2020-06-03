package com.gooddata.qa.utils.schedule;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gooddata.sdk.model.dataload.processes.DataloadProcess;
import com.gooddata.sdk.model.dataload.processes.Schedule;

/**
 * This schedule using to create a 'manual' schedule for dataload process
 * Because we can not set a null/empty to Schedule so we need wrapper to ignore cron property
 */
@JsonIgnoreProperties({"cron"})
public class ManualTriggeredSchedule extends Schedule {

    public ManualTriggeredSchedule(DataloadProcess process, String executable) {
        super(process, executable, "a_dummy_cron");
    }
}
