package com.gooddata.qa.graphene.disc;

import static java.lang.String.format;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;

import java.time.LocalTime;

import org.openqa.selenium.support.FindBy;

import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.Schedule;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.disc.__Executable;
import com.gooddata.qa.graphene.fragments.disc.overview.__DiscOverviewPage;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.ProcessType;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.PackageFile;
import com.gooddata.qa.graphene.fragments.disc.projects.__ProjectDetailPage;
import com.gooddata.qa.graphene.fragments.disc.projects.__ProjectsPage;
import com.gooddata.qa.graphene.fragments.disc.schedule.__ScheduleDetailFragment;

public class __AbstractDISCTest extends AbstractProjectTest {

    public static final String PROJECT_DETAIL_PAGE_URL = "admin/disc/#/projects/%s";
    private static final String SCHEDULT_DETAIL_URL = "admin/disc/#/projects/%s/processes/%s/schedules/%s";

    @FindBy(className = "l-page")
    protected __DiscOverviewPage overviewPage;

    @FindBy(className = "ait-projects-fragment")
    protected __ProjectsPage projectsPage;

    @FindBy(className = "ait-project-detail-fragment")
    protected __ProjectDetailPage projectDetailPage;

    protected __DiscOverviewPage __initDiscOverviewPage() {
        openUrl(DISC_OVERVIEW_PAGE);
        return waitForFragmentVisible(overviewPage).waitForPageLoaded();
    }

    protected __ProjectsPage __initDiscProjectsPage() {
        openUrl(DISC_PROJECTS_PAGE_URL);
        return waitForFragmentVisible(projectsPage).waitForPageLoaded();
    }

    protected __ProjectDetailPage __initDiscProjectDetailPage() {
        openUrl(format(PROJECT_DETAIL_PAGE_URL, testParams.getProjectId()));
        return waitForFragmentVisible(projectDetailPage);
    }

    protected __ScheduleDetailFragment initScheduleDetail(Schedule schedule) {
        openUrl(format(SCHEDULT_DETAIL_URL, testParams.getProjectId(), schedule.getProcessId(), schedule.getId()));
        return __ScheduleDetailFragment.getInstance(browser);
    }

    protected DataloadProcess createProcessWithBasicPackage(String processName) {
        log.info("Create process: " + processName);
        return getGoodDataClient().getProcessService().createProcess(getProject(),
                new DataloadProcess(processName, ProcessType.CLOUD_CONNECT.getValue()), PackageFile.BASIC.loadFile());
    }

    protected Schedule createSchedule(DataloadProcess process, __Executable executable, String crontimeExpression) {
        return createScheduleWithTriggerType(process, executable, crontimeExpression);
    }

    protected Schedule createSchedule(DataloadProcess process, __Executable executable, Schedule triggeringSchedule) {
        return createScheduleWithTriggerType(process, executable, triggeringSchedule);
    }

    protected String parseTimeToCronExpression(LocalTime time) {
        return format("%d * * * *", time.getMinute());
    }

    protected String generateProcessName() {
        return "Process-" + generateHashString();
    }

    private Schedule createScheduleWithTriggerType(DataloadProcess process, __Executable executable,
            Object triggerType) {
        String expectedExecutable = process.getExecutables()
                .stream().filter(e -> e.contains(executable.getPath())).findFirst().get();

        Schedule schedule = null;

        if (triggerType instanceof String) {
            schedule = new Schedule(process, expectedExecutable, (String) triggerType);
        } else {
            schedule = new Schedule(process, expectedExecutable, (Schedule) triggerType);
        }

        return getGoodDataClient().getProcessService().createSchedule(getProject(), schedule);
    }
}
