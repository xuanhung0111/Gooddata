package com.gooddata.qa.utils.lcm;

import com.gooddata.sdk.model.dataload.processes.DataloadProcess;
import com.gooddata.sdk.model.dataload.processes.ProcessExecution;
import com.gooddata.sdk.model.dataload.processes.ProcessExecutionDetail;
import com.gooddata.sdk.model.project.Project;
import com.gooddata.qa.graphene.common.TestParameters;
import com.gooddata.qa.graphene.entity.disc.Parameters;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestClient.RestProfile;

import java.util.Objects;
import java.util.function.Supplier;

import static org.apache.commons.lang3.Validate.notNull;

public abstract class LcmProcess {
    protected TestParameters testParameters = TestParameters.getInstance();
    protected Brick brick;
    protected String projectId;
    protected String name;
    /**
     * LCM|RUBY
     */
    protected String type;
    protected String executable;
    protected RestClient restClient;
    protected DataloadProcess dataloadProcess;
    protected Supplier<Parameters> defaultParameters;

    /**
     * Init a lcm process base on generic ruby process
     *
     */
    protected LcmProcess(final String projectId, final Brick brick) {
        notNull(testParameters, "testParameters cannot be null");
        notNull(projectId, "projectId cannot be null");
        notNull(brick, "brick cannot be null");
        this.restClient = new RestClient(
                new RestProfile(testParameters.getHost(), testParameters.getDomainUser(), testParameters.getPassword(),
                        true));
        this.projectId = projectId;
        this.type = "RUBY";
        this.executable = "main.rb";
        this.brick = brick;
        this.name = "Generic ruby process of " + brick.getName();
        createGenericRubyProcess();
    }

    /**
     * Init a lcm process base on built-in LCM process - this process run on k8s executor
     *
     */
    protected LcmProcess(final String projectId, final String lcmProcessName) {
        notNull(testParameters, "goodDataClient cannot be null");
        notNull(projectId, "projectId cannot be null");
        notNull(lcmProcessName, "lcmProcessName cannot be null");
        this.restClient = new RestClient(
                new RestProfile(testParameters.getHost(), testParameters.getDomainUser(), testParameters.getPassword(),
                        true));
        this.projectId = projectId;
        this.type = "LCM";
        this.executable = "";
        this.name = "Built-int LCM process of " + lcmProcessName;
        createLcmProcess();
    }

    /**
     * Create lcm process using generic ruby
     */
    protected void createGenericRubyProcess() {
        dataloadProcess = restClient.getProcessService().createProcessFromAppstore(getProject(),
                new DataloadProcess(name, type, brick.getPath())).get();
    }

    /**
     * Create lcm process using a built-in LCM dataload process component
     */
    protected abstract void createLcmProcess();

    /**
     * Run process with stored params
     *
     * @return
     */
    protected ProcessExecutionDetail execute() {
        final Parameters parameters = defaultParameters.get();
        return restClient.getProcessService().executeProcess(
                new ProcessExecution(dataloadProcess, executable, parameters.getParameters(),
                        parameters.getSecureParameters())).get();
    }

    /**
     * Run process with params
     * params: json string of params
     *
     * @return
     */
    protected ProcessExecutionDetail execute(final Parameters params) {
        notNull(params, "Parameter cannot be null");
        return restClient.getProcessService().executeProcess(
                new ProcessExecution(dataloadProcess, executable, params.getParameters(), params.getSecureParameters())).get();
    }

    protected Project getProject() {
        return restClient.getProjectService().getProjectById(projectId);
    }

    static LcmProcess ofRelease(final String adsUri, String projectId, boolean useK8sExecutor) {
        if (useK8sExecutor) {
            return new ReleaseProcess(adsUri, projectId, "LCM Dataload Release Process V" + TestParameters.getInstance().getLcmDataloadProcessComponentVersion());
        }
        return new ReleaseProcess(adsUri, projectId);
    }

    static LcmProcess ofProvision(final String adsUri, String projectId, boolean useK8sExecutor) {
        if (useK8sExecutor) {
            return new ProvisionProcess(adsUri, projectId, "LCM Dataload provision Process V" + TestParameters.getInstance().getLcmDataloadProcessComponentVersion());
        }
        return new ProvisionProcess(adsUri, projectId);
    }

    static LcmProcess ofRollout(final String adsUri, String projectId, boolean useK8sExecutor) {
        if (useK8sExecutor) {
            return new RolloutProcess(adsUri, projectId, "LCM Dataload Rollout Process V" + TestParameters.getInstance().getLcmDataloadProcessComponentVersion());
        }
        return new RolloutProcess(adsUri, projectId);
    }

    static class Brick {
        private String name;
        private String path;

        public String getPath() {
            return path;
        }

        public String getName() {
            return name;
        }

        private Brick(final String name, final String path) {
            notNull(name, "Brick name cannot be null");
            notNull(path, "Brick path cannot be null");
            this.name = name;
            this.path = path;
        }

        public static Brick ofReleaseBrick(final String appstore) {
            final String releaseStore = String.format("${%s}:branch/lcm2:/apps/release_brick", appstore);
            return new Brick("release", releaseStore);
        }

        public static Brick ofRolloutBrick(final String appstore) {
            final String rolloutStore = String.format("${%s}:branch/lcm2:/apps/rollout_brick", appstore);
            return new Brick("rollout", rolloutStore);
        }

        public static Brick ofProvisionBrick(final String appstore) {
            final String provisionStore = String.format("${%s}:branch/lcm2:/apps/provisioning_brick", appstore);
            return new Brick("provision", provisionStore);
        }
    }

    public Parameters getDefaultParameters() {
        return defaultParameters.get();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof LcmProcess))
            return false;
        if (obj == this)
            return true;
        LcmProcess lcm = (LcmProcess) obj;
        return Objects.equals(this.projectId, lcm.projectId) &&
                Objects.equals(this.type, lcm.type) &&
                Objects.equals(this.name, lcm.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, type, name);
    }
}
