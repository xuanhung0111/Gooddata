package com.gooddata.qa.graphene.enums.disc;

import java.util.Calendar;

public enum ScheduleCronTimes {

    /*
     * The waitingAutoRunInMinutes value is set to Integer.MAX_VALUE when the cron times are not 
     * checked in auto run schedule tests, because it takes a too long time for waiting in reality.
     */
    CRON_EVERYWEEK("every week", "${day} at ${hour}:${minute} UTC", Integer.MAX_VALUE),
    CRON_EVERYDAY("every day", "daily at ${hour}:${minute} UTC", Integer.MAX_VALUE),
    CRON_EVERYHOUR("every hour", "every hour", 60),
    CRON_30_MINUTES("every 30 minutes", "every 30 minutes", 30),
    CRON_15_MINUTES("every 15 minutes", "every 15 minutes", 15),
    CRON_EXPRESSION("cron expression", "${cronExpression} UTC", Integer.MAX_VALUE),
    AFTER("after", "after ${triggerSchedule} completion", 2);

    private String cronTime;
    private String cronFormat;
    private int waitingAutoRunInMinutes;
    
    private ScheduleCronTimes(String cronTime, String cronFormat, int waitingAutoRunInMinutes) {
        this.cronTime = cronTime;
        this.cronFormat = cronFormat;
        this.waitingAutoRunInMinutes = waitingAutoRunInMinutes;
    }

    public String getCronTimeOption() {
        return cronTime;
    }

    public String getCronFormat() {
        return cronFormat;
    }

    public int getWaitingAutoRunInMinutes() {
        if (this != CRON_30_MINUTES && this != CRON_15_MINUTES)
            return waitingAutoRunInMinutes;
        Calendar startWaitingTime = Calendar.getInstance();
        int waitingTimeFromNow =
                waitingAutoRunInMinutes - startWaitingTime.get(Calendar.MINUTE)
                        % waitingAutoRunInMinutes;
        System.out.println("Waiting time in minutes from now: " + waitingTimeFromNow);
        return waitingTimeFromNow;
    }
}
