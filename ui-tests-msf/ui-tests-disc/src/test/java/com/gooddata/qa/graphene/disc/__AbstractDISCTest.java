package com.gooddata.qa.graphene.disc;

import static com.gooddata.qa.graphene.enums.ResourceDirectory.ZIP_FILES;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsFile;
import static java.lang.String.format;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;

import java.io.File;

import org.openqa.selenium.support.FindBy;

import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.ProcessType;
import com.gooddata.dataload.processes.Schedule;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.disc.__PackageFile;
import com.gooddata.qa.graphene.enums.disc.__Executable;
import com.gooddata.qa.graphene.fragments.disc.overview.__DiscOverviewPage;
import com.gooddata.qa.graphene.fragments.disc.schedule.__ScheduleDetailFragment;

public class __AbstractDISCTest extends AbstractProjectTest {

    private static final String SCHEDULT_DETAIL_URL = "admin/disc/#/projects/%s/processes/%s/schedules/%s";

    @FindBy(className = "l-page")
    protected __DiscOverviewPage overviewPage;

    protected __DiscOverviewPage __initDiscOverviewPage() {
        openUrl(DISC_OVERVIEW_PAGE);
        return waitForFragmentVisible(overviewPage).waitForPageLoaded();
    }

    protected __ScheduleDetailFragment initScheduleDetail(Schedule schedule) {
        openUrl(format(SCHEDULT_DETAIL_URL, testParams.getProjectId(), schedule.getProcessId(), schedule.getId()));
        return __ScheduleDetailFragment.getInstance(browser);
    }

    protected DataloadProcess createProcessWithBasicPackage(String processName) {
        return getGoodDataClient().getProcessService()
                .createProcess(getProject(), new DataloadProcess(processName, ProcessType.GRAPH),
                        loadPackage(__PackageFile.BASIC));
    }

    protected Schedule createSchedule(DataloadProcess process, __Executable executable, String crontimeExpression) {
        return getGoodDataClient().getProcessService()
                .createSchedule(getProject(), new Schedule(process, executable.getValue(), crontimeExpression));
    }

    private File loadPackage(__PackageFile packageFile) {
        return getResourceAsFile("/" + ZIP_FILES + "/" + packageFile.getName());
    }
}
