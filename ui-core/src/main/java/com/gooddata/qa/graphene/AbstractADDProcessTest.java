package com.gooddata.qa.graphene;

import static com.gooddata.qa.graphene.AbstractTest.Profile.DOMAIN;
import static java.lang.String.format;

import com.gooddata.qa.graphene.enums.project.DeleteMode;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestRequest;
import com.gooddata.qa.utils.lcm.LcmBrickFlowBuilder;
import org.testng.annotations.AfterClass;

import java.io.IOException;
import java.time.LocalTime;

public class AbstractADDProcessTest extends AbstractDataIntegrationTest {

    protected RestClient domainRestClient;
    protected LcmBrickFlowBuilder lcmBrickFlowBuilder;
    protected boolean useK8sExecutor = false;

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        lcmBrickFlowBuilder = new LcmBrickFlowBuilder(testParams, useK8sExecutor);
        domainRestClient = new RestClient(getProfile(DOMAIN));
    }

    protected String generateScheduleName() {
        return "schedule-" + generateHashString();
    }

    protected String generateDataSourceTitle() {
        return "data-source-" + generateHashString();
    }

    protected String parseTimeToCronExpression(LocalTime time) {
        return format("%d * * * *", time.getMinute());
    }

    protected String createSegment(String clientID, String clientProjectId) {
        String segmentID = "att_segment_" + generateHashString();
        lcmBrickFlowBuilder.setSegmentId(segmentID).setClientId(clientID)
                .setDevelopProject(testParams.getProjectId()).setClientProjects(clientProjectId).buildLcmProjectParameters();
        lcmBrickFlowBuilder.runLcmFlow();
        return segmentID;
    }

    protected void addUserToSpecificProject(String email, UserRoles userRole, String projectId) throws IOException {
        final String domainUser = testParams.getDomainUser() != null ? testParams.getDomainUser() : testParams.getUser();
        final UserManagementRestRequest userManagementRestRequest = new UserManagementRestRequest(new RestClient(
                new RestClient.RestProfile(testParams.getHost(), domainUser, testParams.getPassword(), true)),
                projectId);
        userManagementRestRequest.addUserToProject(email, userRole);
    }

    @AfterClass(alwaysRun = true)
    public void cleanUpLCM() {
        if (testParams.getDeleteMode() == DeleteMode.DELETE_NEVER || lcmBrickFlowBuilder == null) {
            return;
        }
        lcmBrickFlowBuilder.destroy();
    }
}
