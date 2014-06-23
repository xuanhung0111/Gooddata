package com.gooddata.qa.graphene.enums;

import java.util.Arrays;
import java.util.List;

public enum ScheduleCronTimes {

	CRON_EVERYWEEK("every week", Arrays.asList("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"), "${day} at ${hour}:${minute} UTC"),
	CRON_EVERYDAY("every day", null, "daily at ${hour}:${minute} UTC"),
	CRON_EVERYHOUR("every hour", null, null),
	CRON_30_MINUTES("every 30 minutes", null, null),
	CRON_15_MINUTES("every 15 minutes", null, null),
	CRON_EXPRESSION("cron expression", null, null);
	
	private String cronTime;
	private List<String> daysInWeek;
	private String cronFormat;
	
	private ScheduleCronTimes(String cronTime, List<String> daysInWeek, String cronFormat) {
		this.cronTime = cronTime;
		this.daysInWeek = daysInWeek;
		this.cronFormat = cronFormat;
	}
	
	public String getCronTime() {
		return cronTime;
	}
	
	public List<String> getDaysInWeek() {
		return daysInWeek;
	}
	
	public String getCronFormat() {
		return cronFormat;
	}
}
