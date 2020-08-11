package com.gooddata.qa.graphene.disc;

import com.gooddata.qa.graphene.AbstractUITest;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.utils.cloudresources.DataSourceRestRequest;
import com.gooddata.qa.utils.cloudresources.ProjectViewRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.sdk.model.dataload.processes.DataloadProcess;
import org.json.JSONException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;

public class CleanUpDatasourceTest extends AbstractUITest {

    private int databaseRetentionDays;
    private DataSourceRestRequest dataSourceRestRequest;
    private ProjectViewRestRequest projectViewRestRequest;
    private DataloadProcess dataloadProcess;
    private RestClient restClient;

    @Test
    protected void signIn() throws JSONException {
        signIn(true, UserRoles.ADMIN);
    }

    @Test (dependsOnMethods = "signIn")
    public void cleanUpOldDatasource() throws SQLException, IOException {
        restClient = new RestClient(getProfile(ADMIN));
        databaseRetentionDays = testParams.getDatabaseRetentionDays();
        dataSourceRestRequest = new DataSourceRestRequest(restClient, null);
        projectViewRestRequest = new ProjectViewRestRequest(restClient, null);
        List<String> listDatasources = dataSourceRestRequest.getOldAutoTeamDatasources(databaseRetentionDays);
        if (!listDatasources.isEmpty()) {
            List<String> listUriProcess = projectViewRestRequest.listProcessesUsingATTDatasources(listDatasources);
            if (!listUriProcess.isEmpty()) {
                for (String uri : listUriProcess) {
                    dataloadProcess = restClient.getProcessService().getProcessByUri(uri);
                    restClient.getProcessService().removeProcess(dataloadProcess);
                }
            }
            for (String dataSource : listDatasources) {
                dataSourceRestRequest.deleteDataSource(dataSource);
            }
        }
    }
}
