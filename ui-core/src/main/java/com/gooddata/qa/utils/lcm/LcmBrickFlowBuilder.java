package com.gooddata.qa.utils.lcm;

import com.gooddata.sdk.model.dataload.processes.ProcessExecutionDetail;
import com.gooddata.qa.graphene.common.TestParameters;

import org.json.JSONArray;
import org.json.JSONObject;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Handy class for build simple lcm with ruby bricks
 */
final public class LcmBrickFlowBuilder {

    private static final Logger log = Logger.getLogger(LcmBrickFlowBuilder.class.getName());
    private LCMServiceProject lcmServiceProject;
    private TestParameters testParams;
    private String devProjectId;
    private String[] clientProjectIds;
    private JSONArray releaseSegments;
    private JSONObject datasource;
    private JSONArray segmentFilters;
    private Map<String, String> clients;

    private String segmentId;
    private String clientId;

    public LcmBrickFlowBuilder(final TestParameters testParameters, boolean useK8sExecutor) {
        this.testParams = testParameters;
        this.devProjectId = testParams.getProjectId();
        this.clients = new HashMap<>();
        this.lcmServiceProject = LCMServiceProject.newWorkFlow(testParams, useK8sExecutor);
    }

    public LcmBrickFlowBuilder setDevelopProject(final String developProject) {
        this.devProjectId = developProject;
        return this;
    }

    public LcmBrickFlowBuilder setClientProjects(final String... clientId) {
        this.clientProjectIds = clientId;
        return this;
    }

    //this test use for set clientId match with clientProjectId
    public LcmBrickFlowBuilder setClient(final String clientId, String clientProjectId) {
        this.clients.put(clientId, clientProjectId);
        return this;
    }

    public LcmBrickFlowBuilder setSegmentId(final String segmentId) {
        this.segmentId = segmentId;
        return this;
    }

    public LcmBrickFlowBuilder setClientId(final String clientId) {
        this.clientId = clientId;
        return this;
    }

    public LcmBrickFlowBuilder buildLcmProjectParameters() {
        releaseSegments = new JSONArray() {{
            put(new JSONObject() {{
                put("segment_id", segmentId);
                put("development_pid", devProjectId);
                put("driver", testParams.getProjectDriver().getValue());
                put("master_name", "Master of " + segmentId);
            }});
        }};
        // when the map<String, String> clients is null , 
        // create provision datasource base on  clientId,clientProjectIds
        if (clients.isEmpty()) {
            datasource = lcmServiceProject.createProvisionDatasource(segmentId, clientId, clientProjectIds);
        } else {
            datasource = lcmServiceProject.createProvisionDatasource(segmentId, clients);
        }
        segmentFilters = new JSONArray() {{
            put(segmentId);
        }};
        return this;
    }

    public void deleteMasterProject() {
        String masterProject = lcmServiceProject.getMasterProject(testParams.getUserDomain(), segmentId);
        log.info("Domain: " + testParams.getUserDomain());
        log.info("Master project: " + masterProject);
        lcmServiceProject.deleteProject(masterProject);
    }

    public LcmBrickFlowBuilder release() {
        log.info("----Start releasing--------------");
        ProcessExecutionDetail detail = lcmServiceProject.release(releaseSegments);
        verifyExecutionLog(detail);
        log.info("----Finished releasing--------------");
        return this;
    }

    public LcmBrickFlowBuilder provision() {
        log.info("----Start provisioning--------------");
        ProcessExecutionDetail detail = lcmServiceProject.provision(segmentFilters, datasource);
        verifyExecutionLog(detail);
        log.info("----Finished provisioning--------------");
        return this;
    }

    public LcmBrickFlowBuilder rollout() {
        log.info("----Start rolling--------------");
        ProcessExecutionDetail detail = lcmServiceProject.rollout(segmentFilters);
        verifyExecutionLog(detail);
        log.info("----Finished rolling--------------");
        final String executionLog = lcmServiceProject.getExecutionLog(detail.getLogUri());
        log.info("----executionLog------");
        log.info(executionLog);
        return this;
    }

    public void runLcmFlow() {
        release();
        provision();
        rollout();
    }

    public LCMServiceProject getLCMServiceProject() {
        return lcmServiceProject;
    }

    public void destroy() {
        log.info("--------------Start cleanup lcm service stuff");
        lcmServiceProject.cleanUp(testParams.getUserDomain());
        log.info("--------------Finished cleanup lcm service stuff");
    }

    private void verifyExecutionLog(final ProcessExecutionDetail detail) {
        final String executionLog = lcmServiceProject.getExecutionLog(detail.getLogUri());
        assertTrue(executionLog.contains("INFO -- : Pipeline ending"), "execution log does not contain valid ending message");
        assertTrue(executionLog.contains("GoodData::LCM2"), "execution log does not contain LCM2");
        assertTrue(executionLog.contains(LcmRestUtils.ATT_LCM_DATA_PRODUCT), "execution log does not contain a expected data product");
    }
}
