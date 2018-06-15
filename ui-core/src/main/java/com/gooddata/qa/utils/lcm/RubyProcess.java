package com.gooddata.qa.utils.lcm;

import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.ProcessExecution;
import com.gooddata.dataload.processes.ProcessExecutionDetail;
import com.gooddata.project.Project;
import com.gooddata.qa.graphene.common.TestParameters;
import com.gooddata.qa.graphene.entity.disc.Parameters;
import com.gooddata.qa.utils.http.RestClient;

import java.util.Objects;
import java.util.function.Supplier;

import static org.apache.commons.lang.Validate.notNull;

public abstract class RubyProcess {
    protected Brick brick;
    protected String projectId;
    protected String name;
    protected RestClient restClient;
    private DataloadProcess rubyProcess;
    protected Supplier<Parameters> defaultParameters;

    protected RubyProcess(final RestClient restClient, final String projectId, final Brick brick) {
        notNull(restClient, "goodDataClient cannot be null");
        notNull(projectId, "projectId cannot be null");
        notNull(brick, "brick cannot be null");
        this.restClient = restClient;
        this.projectId = projectId;
        this.brick = brick;
        this.name = "Process of " + brick.getName();
        createProcess();
    }

    protected void createProcess() {
        rubyProcess = restClient.getProcessService().createProcessFromAppstore(getProject(),
                new DataloadProcess(name, "RUBY", brick.getPath())).get();
    }

    /**
     * Run process with stored params
     *
     * @return
     */
    protected ProcessExecutionDetail execute() {
        final Parameters parameters = defaultParameters.get();
        return restClient.getProcessService().executeProcess(
                new ProcessExecution(rubyProcess, "main.rb", parameters.getParameters(),
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
                new ProcessExecution(rubyProcess, "main.rb", params.getParameters(), params.getSecureParameters())).get();
    }

    protected Project getProject() {
        return restClient.getProjectService().getProjectById(projectId);
    }

    static RubyProcess ofRelease(final TestParameters testParameters, final String adsUri, String projectId) {
        return new ReleaseProcess(testParameters, adsUri, projectId);
    }

    static RubyProcess ofProvision(final TestParameters testParameters, final String adsUri, String projectId) {
        return new ProvisionProcess(testParameters, adsUri, projectId);
    }

    static RubyProcess ofRollout(final TestParameters testParameters, final String adsUri, String projectId) {
        return new RolloutProcess(testParameters, adsUri, projectId);
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

        public static Brick ofReleaseBrick() {
            return new Brick("release", "${PRODUCTION_APPSTORE}:branch/lcm2:/apps/release_brick");
        }

        public static Brick ofRolloutBrick() {
            return new Brick("rollout", "${PRODUCTION_APPSTORE}:branch/lcm2:/apps/rollout_brick");
        }

        public static Brick ofProvisionBrick() {
            return new Brick("provision", "${PRODUCTION_APPSTORE}:branch/lcm2:/apps/provisioning_brick");
        }
    }

    public Parameters getDefaultParameters() {
        return defaultParameters.get();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof RubyProcess))
            return false;
        if (obj == this)
            return true;
        return this.brick.getPath().equals(((RubyProcess) obj).brick.getPath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, brick.getPath());
    }
}
