package com.gooddata.qa.utils.lcm;

import com.gooddata.qa.graphene.common.TestParameters;
import org.json.JSONArray;
import org.json.JSONObject;

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

    private String segmentId;
    private String clientId;

    public LcmBrickFlowBuilder(final TestParameters testParameters) {
        this.testParams = testParameters;
        this.devProjectId = testParams.getProjectId();
        this.lcmServiceProject = LCMServiceProject.newWorkFlow(testParams);
    }

    public LcmBrickFlowBuilder setDevelopProject(final String developProject) {
        this.devProjectId = developProject;
        return this;
    }

    public LcmBrickFlowBuilder setClientProjects(final String... clientId) {
        this.clientProjectIds = clientId;
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
        datasource = lcmServiceProject.createProvisionDatasource(segmentId, clientId, clientProjectIds);
        segmentFilters = new JSONArray() {{
            put(segmentId);
        }};
        return this;
    }

    public LcmBrickFlowBuilder release() {
        log.info("----Start releasing--------------");
        lcmServiceProject.release(releaseSegments);
        log.info("----Finished releasing--------------");
        return this;
    }

    public LcmBrickFlowBuilder provision() {
        log.info("----Start provisioning--------------");
        lcmServiceProject.provision(segmentFilters, datasource);
        log.info("----Finished provisioning--------------");
        return this;
    }

    public LcmBrickFlowBuilder rollout() {
        log.info("----Start rolling--------------");
        lcmServiceProject.rollout(segmentFilters);
        log.info("----Finished rolling--------------");
        return this;
    }

    public void runLcmFlow() {
        release();
        provision();
        rollout();
    }

    public void destroy() {
        log.info("--------------Start cleanup lcm service stuff");
        lcmServiceProject.cleanUp(testParams.getUserDomain());
        log.info("--------------Finished cleanup lcm service stuff");
    }
}
