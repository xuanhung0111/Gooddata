package com.gooddata.qa.graphene.entity.disc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class OverviewProjectDetails {

    private ProjectInfo projectInfo;
    private List<OverviewProcess> overviewProcesses;

    public OverviewProjectDetails() {
        this.overviewProcesses = new ArrayList<OverviewProcess>();
    }

    public OverviewProjectDetails setProjectInfo(ProjectInfo projectInfo) {
        this.projectInfo = projectInfo;
        return this;
    }

    public ProjectInfo getProjectInfo() {
        return this.projectInfo;
    }

    public String getProjectName() {
        if (projectInfo == null)
            return null;
        return projectInfo.getProjectName();
    }

    public List<OverviewProcess> getOverviewProcesses() {
        return overviewProcesses;
    }

    public OverviewProjectDetails addProcess(OverviewProcess process) {
        overviewProcesses.add(process);
        return this;
    }

    public OverviewProjectDetails removeProcess(OverviewProcess process) {
        overviewProcesses.remove(process);
        return this;
    }

    public OverviewProcess newProcess() {
        return new OverviewProcess();
    }

    public int getProjectScheduleNumber() {
        int projectScheduleNumber = 0;
        for (OverviewProcess process : this.overviewProcesses) {
            projectScheduleNumber += process.getProcessScheduleNumber();
        }
        return projectScheduleNumber;
    }

    public class OverviewProcess {
        private String processName;
        private String processUrl;
        private List<OverviewSchedule> overviewSchedules;

        public OverviewProcess() {
            this.processName = null;
            this.processUrl = null;
            this.overviewSchedules = new ArrayList<OverviewSchedule>();
        }

        public String getProcessName() {
            return processName;
        }

        public OverviewProcess setProcessName(String processName) {
            this.processName = processName;
            return this;
        }

        public String getProcessUrl() {
            return processUrl;
        }

        public OverviewProcess setProcessUrl(String processUrl) {
            this.processUrl = processUrl;
            return this;
        }

        public List<OverviewSchedule> getOverviewSchedules() {
            return overviewSchedules;
        }

        public int getProcessScheduleNumber() {
            return overviewSchedules.size();
        }

        public OverviewProcess addSchedule(OverviewSchedule schedule) {
            overviewSchedules.add(schedule);
            return this;
        }

        public OverviewSchedule newSchedule() {
            return new OverviewSchedule();
        }

        public class OverviewSchedule {
            private String scheduleName;
            private String scheduleUrl;
            private String lastExecutionDescription;
            private String lastExecutionRunTime;
            private String lastExecutionDate;
            private String lastExecutionTime;

            public String getScheduleName() {
                return scheduleName;
            }

            public OverviewSchedule setScheduleName(String scheduleName) {
                this.scheduleName = scheduleName;
                return this;
            }

            public String getScheduleUrl() {
                return scheduleUrl;
            }

            public OverviewSchedule setScheduleUrl(String scheduleUrl) {
                this.scheduleUrl = scheduleUrl;
                return this;
            }

            public String getExecutionDescription() {
                return lastExecutionDescription;
            }

            public OverviewSchedule setExecutionDescription(String lastExecutionDescription) {
                this.lastExecutionDescription = lastExecutionDescription;
                return this;
            }

            public String getLastExecutionRunTime() {
                return lastExecutionRunTime;
            }

            public OverviewSchedule setLastExecutionRunTime(String lastExecutionRunTime) {
                this.lastExecutionRunTime = lastExecutionRunTime;
                return this;
            }

            public OverviewSchedule setLastExecutionDate(String lastExecutionDate) {
                this.lastExecutionDate = lastExecutionDate;
                return this;
            }

            public OverviewSchedule setLastExecutionTime(String lastExecutionTime) {
                this.lastExecutionTime = lastExecutionTime;
                return this;
            }

            public String getOverviewStartTime() {
                String executionDateTime = getOverviewExecutionDateTime();
                return executionDateTime.substring(0, executionDateTime.indexOf("-") - 1);
            }

            public String getOverviewExecutionDateTime() {
                SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss aa");
                SimpleDateFormat format2 = new SimpleDateFormat("hh:mm aa");
                String executionStartedTime = "";
                String executionEndTime = "";
                try {
                    executionStartedTime = format2.format(format.parse(lastExecutionTime.substring(0,
                            lastExecutionTime.lastIndexOf("-") - 1)));
                    executionEndTime =
                            format2.format(format.parse(lastExecutionTime.substring(lastExecutionTime
                                    .lastIndexOf("-") + 1)));
                } catch (ParseException e) {
                    System.out.println("There is problem when parsing execution time: ");
                    e.printStackTrace();
                }
                return lastExecutionDate + " " + executionStartedTime + " - " + executionEndTime;
            }
        }
    }
}
