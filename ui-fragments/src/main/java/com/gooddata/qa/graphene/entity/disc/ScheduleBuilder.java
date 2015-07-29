package com.gooddata.qa.graphene.entity.disc;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.gooddata.qa.graphene.enums.disc.ScheduleCronTimes;
import com.gooddata.qa.graphene.enums.disc.DeployPackages.Executables;
import com.google.common.base.CharMatcher;

public class ScheduleBuilder {

    private String processName;
    private Executables executable;
    private String scheduleName;
    private boolean dataloadDatasetsOverlap;
    private CronTimeBuilder cronTimeBuilder = new CronTimeBuilder();
    private List<Parameter> secureParameters = new ArrayList<Parameter>();
    private List<Parameter> parameters = new ArrayList<Parameter>();
    private String scheduleUrl;
    private int retryDelay = 0;
    private boolean isEnabled = true;
    private boolean confirmed = true;
    private boolean dataloadProcess;
    private boolean synchronizeAllDatasets;
    private List<String> datasetsToSynchronize;
    private List<String> allDatasets;

    public ScheduleBuilder setProcessName(String processName) {
        this.processName = processName;
        return this;
    }

    public ScheduleBuilder setExecutable(Executables executable) {
        this.executable = executable;
        if (this.scheduleName == null)
            this.setDefaultScheduleName();
        return this;
    }

    public ScheduleBuilder setCronTime(ScheduleCronTimes cronTime) {
        this.cronTimeBuilder.setCronTime(cronTime);
        return this;
    }

    public ScheduleBuilder setCronTimeExpression(String cronTimeExpression) {
        this.cronTimeBuilder.setCronTimeExpression(cronTimeExpression);
        return this;
    }

    public ScheduleBuilder setDayInWeek(String dayInWeek) {
        this.cronTimeBuilder.setDayInWeek(dayInWeek);
        return this;
    }

    public ScheduleBuilder setHourInDay(String hourInDay) {
        this.cronTimeBuilder.setHourInDay(hourInDay);
        return this;
    }

    public ScheduleBuilder setMinuteInHour(String minuteInHour) {
        this.cronTimeBuilder.setMinuteInHour(minuteInHour);
        return this;
    }

    public ScheduleBuilder setParameters(List<Parameter> parameters) {
        for (Parameter param : parameters) {
            if (param.isSecure)
                this.secureParameters.add(param);
            else
                this.parameters.add(param);
        }
        return this;
    }

    public ScheduleBuilder setScheduleUrl(String scheduleUrl) {
        this.scheduleUrl = scheduleUrl;
        return this;
    }

    public String getScheduleUrl() {
        return this.scheduleUrl;
    }

    public ScheduleBuilder editParam(Parameter existingParam, Parameter editedParam) {
        for (Parameter param : (existingParam.isSecureParam() ? this.secureParameters
                : this.parameters)) {
            if (param.getParamName().equals(existingParam.getParamName())) {
                param.setParamName(editedParam.getParamName());
                param.setParamValue(editedParam.getParamValue());
            }
        }
        return this;
    }

    public ScheduleBuilder removeParam(Parameter paramToRemove) {
        if (paramToRemove.isSecure)
            this.secureParameters.remove(paramToRemove);
        else
            this.parameters.remove(paramToRemove);
        return this;
    }

    public ScheduleBuilder setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
        return this;
    }

    public ScheduleBuilder setDataloadDatasetsOverlap(boolean dataloadDatasetsOverlap) {
        this.dataloadDatasetsOverlap = dataloadDatasetsOverlap;
        return this;
    }

    public boolean isDataloadDatasetsOverlap() {
        return this.dataloadDatasetsOverlap;
    }

    public ScheduleBuilder setTriggerScheduleGroup(String groupOption) {
        this.cronTimeBuilder.setTriggerScheduleGroup(groupOption);
        return this;
    }

    public ScheduleBuilder setTriggerScheduleOption(String scheduleOption) {
        this.cronTimeBuilder.setTriggerScheduleOption(scheduleOption);
        return this;
    }

    public ScheduleBuilder setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
        return this;
    }

    public ScheduleBuilder setHasDataloadProcess(boolean hasDataloadProcess) {
        this.dataloadProcess = hasDataloadProcess;
        return this;
    }

    public boolean isDataloadProcess() {
        return dataloadProcess;
    }

    public ScheduleBuilder setSynchronizeAllDatasets(boolean synchronizeAllDatasets) {
        this.synchronizeAllDatasets = synchronizeAllDatasets;
        return this;
    }


    public boolean isSynchronizeAllDatasets() {
        return synchronizeAllDatasets;
    }


    public ScheduleBuilder setDatasetsToSynchronize(List<String> datasetsToSynchronize) {
        this.datasetsToSynchronize = datasetsToSynchronize;
        if (datasetsToSynchronize != null) {
            this.synchronizeAllDatasets = false;
        }

        if (datasetsToSynchronize == null || datasetsToSynchronize.isEmpty()) {
            this.dataloadDatasetsOverlap = false;
        }
        return this;
    }


    public List<String> getDatasetsToSynchronize() {
        return datasetsToSynchronize;
    }

    public ScheduleBuilder setAllDatasets(List<String> allDatasets) {
        this.allDatasets = allDatasets;
        return this;
    }

    public List<String> getAllDatasets() {
        return allDatasets;
    }

    public ScheduleBuilder hasExecutableProcess() {
        this.dataloadProcess = false;
        return this;
    }

    public String getProcessName() {
        return this.processName;
    }

    public Executables getExecutable() {
        return executable;
    }

    public List<Parameter> getParameters() {
        List<Parameter> returnParamList = new ArrayList<Parameter>();
        returnParamList.addAll(this.parameters);
        returnParamList.addAll(this.secureParameters);
        return returnParamList;
    }

    public String getScheduleName() {
        return this.scheduleName;
    }

    public String getDayInWeek() {
        return this.cronTimeBuilder.getDayInWeek();
    }

    public String getHourInDay() {
        return this.cronTimeBuilder.getHourInDay();
    }

    public String getMinuteInHour() {
        return this.cronTimeBuilder.getMinuteInHour();
    }

    public ScheduleCronTimes getCronTime() {
        return this.cronTimeBuilder.getCronTime();
    }

    public ScheduleBuilder setDefaultScheduleName() {
        if (executable != null)
            this.scheduleName = executable.getExecutableName();
        return this;
    }

    public CronTimeBuilder getCronTimeBuilder() {
        return this.cronTimeBuilder;
    }

    public int getRetryDelay() {
        return retryDelay;
    }

    public ScheduleBuilder setRetryDelayInMinute(int retryDelay) {
        this.retryDelay = retryDelay;
        return this;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public ScheduleBuilder setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
        return this;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public static class CronTimeBuilder {
        private ScheduleCronTimes cronTime;
        private String dayInWeek;
        private String hourInDay;
        private String minuteInHour;
        private String cronTimeExpression;
        private String triggerScheduleGroup;
        private String triggerScheduleOption;
        private String cronFormatInProjectDetailPage;
        private int waitingAutoRunInMinutes;

        public CronTimeBuilder() {
            this.cronTime = null;
            this.dayInWeek = null;
            this.hourInDay = null;
            this.minuteInHour = null;
            this.cronTimeExpression = null;
            this.triggerScheduleGroup = null;
            this.triggerScheduleOption = null;
            this.cronFormatInProjectDetailPage = null;
            this.waitingAutoRunInMinutes = 0;
        }

        public ScheduleCronTimes getCronTime() {
            return cronTime;
        }

        public CronTimeBuilder setCronTime(ScheduleCronTimes cronTime) {
            this.cronTime = cronTime;
            this.cronFormatInProjectDetailPage = cronTime.getCronFormat();
            this.waitingAutoRunInMinutes = cronTime.getWaitingAutoRunInMinutes();
            return this;
        }

        public CronTimeBuilder setDefaultCronTime() {
            this.setCronTime(ScheduleCronTimes.CRON_EVERYHOUR);
            this.setDefaultMinuteInHour();
            return this;
        }

        public String getDayInWeek() {
            return dayInWeek;
        }

        public CronTimeBuilder setDayInWeek(String dayInWeek) {
            this.dayInWeek = dayInWeek;
            this.cronFormatInProjectDetailPage =
                    this.cronFormatInProjectDetailPage.replace("${day}", dayInWeek);
            return this;
        }

        public CronTimeBuilder setDefaultDayInWeek() {
            this.setDayInWeek("Sunday");
            return this;
        }

        public String getHourInDay() {
            return hourInDay;
        }

        public CronTimeBuilder setHourInDay(String hourInDay) {
            this.hourInDay = hourInDay;
            this.cronFormatInProjectDetailPage =
                    this.cronFormatInProjectDetailPage.replace("${hour}", "00".equals(hourInDay) ? "0"
                            : CharMatcher.anyOf("0").trimLeadingFrom(hourInDay));
            return this;
        }

        public CronTimeBuilder setDefaultHourInDay() {
            this.setHourInDay("00");
            return this;
        }

        public String getMinuteInHour() {
            return minuteInHour;
        }

        public CronTimeBuilder setMinuteInHour(String minuteInHour) {
            this.minuteInHour = minuteInHour;
            this.cronFormatInProjectDetailPage =
                    this.cronFormatInProjectDetailPage.replace("${minute}", minuteInHour);
            return this;
        }

        public CronTimeBuilder setDefaultMinuteInHour() {
            this.setMinuteInHour("00");
            return this;
        }

        public String getCronTimeExpression() {
            return cronTimeExpression;
        }

        public CronTimeBuilder setCronTimeExpression(String cronTimeExpression) {
            this.cronTimeExpression = cronTimeExpression;
            this.cronFormatInProjectDetailPage =
                    this.cronFormatInProjectDetailPage.replace("${cronExpression}",
                            cronTimeExpression);
            return this;
        }

        public String getTriggerScheduleGroup() {
            return triggerScheduleGroup;
        }

        public CronTimeBuilder setTriggerScheduleGroup(String triggerScheduleGroup) {
            this.triggerScheduleGroup = triggerScheduleGroup;
            return this;
        }

        public String getTriggerScheduleOption() {
            return triggerScheduleOption;
        }

        public CronTimeBuilder setTriggerScheduleOption(String triggerScheduleOption) {
            this.triggerScheduleOption = triggerScheduleOption;
            this.cronFormatInProjectDetailPage =
                    this.cronFormatInProjectDetailPage
                            .replace("${triggerSchedule}", triggerScheduleOption
                                    .substring(triggerScheduleOption.lastIndexOf("/") + 1));
            return this;
        }

        public String getCronFormatInProjectDetailPage() {
            return cronFormatInProjectDetailPage;
        }

        public int getWaitingAutoRunInMinutes() {
            if (this.getCronTime() != ScheduleCronTimes.CRON_30_MINUTES
                    && this.getCronTime() != ScheduleCronTimes.CRON_15_MINUTES)
                return waitingAutoRunInMinutes;
            Calendar startWaitingTime = Calendar.getInstance();
            int waitingTimeFromNow =
                    waitingAutoRunInMinutes - startWaitingTime.get(Calendar.MINUTE)
                            % waitingAutoRunInMinutes;
            return waitingTimeFromNow;
        }

        public CronTimeBuilder setWaitingAutoRunInMinutes(int waitingAutoRunInMinutes) {
            this.waitingAutoRunInMinutes = waitingAutoRunInMinutes;
            return this;
        }
    }

    public static class Parameter {

        private String paramName;
        private boolean isSecure;
        private String paramValue;

        public Parameter() {
            this.paramName = null;
            this.isSecure = false;
            this.paramValue = null;
        }

        public Parameter setSecureParam() {
            this.isSecure = true;
            return this;
        }

        public boolean isSecureParam() {
            return this.isSecure;
        }

        public Parameter setParamName(String paramName) {
            this.paramName = paramName;
            return this;
        }

        public String getParamName() {
            return this.paramName;
        }

        public Parameter setParamValue(String paramValue) {
            this.paramValue = paramValue;
            return this;
        }

        public String getParamValue() {
            return this.paramValue;
        }
    }
}
