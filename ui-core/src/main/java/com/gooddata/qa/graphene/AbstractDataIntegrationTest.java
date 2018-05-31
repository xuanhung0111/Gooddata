package com.gooddata.qa.graphene;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static java.lang.String.format;

import java.time.LocalTime;

import org.openqa.selenium.support.FindBy;

import com.gooddata.GoodData;
import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.ProcessExecution;
import com.gooddata.dataload.processes.ProcessExecutionDetail;
import com.gooddata.dataload.processes.ProcessService;
import com.gooddata.dataload.processes.Schedule;
import com.gooddata.qa.graphene.entity.disc.Parameters;
import com.gooddata.qa.graphene.fragments.disc.overview.OverviewPage;
import com.gooddata.qa.graphene.fragments.disc.projects.ProjectDetailPage;
import com.gooddata.qa.graphene.fragments.disc.projects.ProjectsPage;

public class AbstractDataIntegrationTest extends AbstractProjectTest {

    @FindBy(className = "l-page")
    protected OverviewPage overviewPage;

    @FindBy(className = "ait-projects-fragment")
    protected ProjectsPage projectsPage;

    @FindBy(className = "ait-project-detail-fragment")
    protected ProjectDetailPage projectDetailPage;

    protected OverviewPage initDiscOverviewPage() {
        openUrl(OverviewPage.URI);
        return waitForFragmentVisible(overviewPage).waitForPageLoaded();
    }

    protected ProjectsPage initDiscProjectsPage() {
        openUrl(ProjectsPage.URI);
        return waitForFragmentVisible(projectsPage).waitForPageLoaded();
    }

    protected ProjectDetailPage initDiscProjectDetailPage() {
        openUrl(format(ProjectDetailPage.URI, testParams.getProjectId()));
        return waitForFragmentVisible(projectDetailPage);
    }

    protected ProcessExecutionDetail executeProcess(DataloadProcess process, Parameters parameters) {
        return executeProcess(process, "", parameters);
    }

    protected ProcessExecutionDetail executeProcess(DataloadProcess process, String executable,
            Parameters parameters) {
        return executeProcess(getGoodDataClient(), process, executable, parameters);
    }

    protected ProcessExecutionDetail executeProcess(GoodData goodData, DataloadProcess process, String executable,
            Parameters parameters) {
        return goodData.getProcessService()
                .executeProcess(new ProcessExecution(process, executable,
                        parameters.getParameters(), parameters.getSecureParameters()))
                .get();
    }

    protected void deleteScheduleByName(DataloadProcess process, String scheduleName) {
        getProcessService().removeSchedule(getScheduleByName(process, scheduleName));
    }

    protected String getScheduleId(DataloadProcess process, String scheduleName) {
        return getScheduleByName(process, scheduleName).getId();
    }

    protected String parseTimeToCronExpression(LocalTime time) {
        return format("%d * * * *", time.getMinute());
    }

    protected String generateProcessName() {
        return "Process-" + generateHashString();
    }

    protected String generateScheduleName() {
        return "Schedule-" + generateHashString();
    }

    protected ProcessService getProcessService() {
        return getGoodDataClient().getProcessService();
    }

    private Schedule getScheduleByName(DataloadProcess process, String scheduleName) {
        return getProcessService().listSchedules(getProject()).stream()
                .filter(s -> s.getProcessId().equals(process.getId()))
                .filter(s -> scheduleName.equals(s.getName()))
                .findFirst()
                .get();
    }
}
