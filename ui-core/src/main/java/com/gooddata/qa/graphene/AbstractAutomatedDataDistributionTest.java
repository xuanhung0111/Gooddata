package com.gooddata.qa.graphene;

import com.gooddata.dataload.processes.Schedule;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.disc.projects.ProjectDetailPage;
import com.gooddata.qa.graphene.fragments.disc.schedule.add.DataloadScheduleDetail;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.schedule.ScheduleUtils;
import com.gooddata.qa.utils.snowflake.DataSourceUtils;
import com.gooddata.qa.utils.snowflake.ProcessUtils;

import static com.gooddata.qa.graphene.AbstractTest.Profile.DOMAIN;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static java.lang.String.format;

import java.io.IOException;

import org.json.JSONException;
/* Abstract class use for segment data distribution process 
 * interact with datasource , schedule 
 */
public class AbstractAutomatedDataDistributionTest extends AbstractDataIntegrationTest {
    protected RestClient domainRestClient;
    protected RestClient adminRestClient;
    protected ScheduleUtils domainScheduleUtils;
    protected ProcessUtils domainProcessUtils;
    protected DataSourceUtils datasourceUtils;
    protected boolean useK8sExecutor = false;

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        domainRestClient = new RestClient(getProfile(DOMAIN));
        adminRestClient = new RestClient(getProfile(Profile.ADMIN));
        domainScheduleUtils = new ScheduleUtils(testParams, domainRestClient);
        datasourceUtils = new DataSourceUtils(testParams);

    }

    protected ProjectDetailPage initDiscProjectDetailPage(String serviceprojectId) {
        openUrl(format(ProjectDetailPage.URI, serviceprojectId));
        return waitForFragmentVisible(projectDetailPage);
    }

    protected DataloadScheduleDetail initScheduleDetail(Schedule schedule, String processName, String serviceprojectId) {
        return initDiscProjectDetailPage(serviceprojectId).getDataDistributonProcess(processName).openSchedule(schedule.getName());
    }

    protected void addUsersToServiceProject() throws JSONException, IOException {
        addUserToProject(testParams.getUser(), UserRoles.ADMIN);
    }
}
