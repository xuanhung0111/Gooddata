package com.gooddata.qa.utils.lcm;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.gooddata.dataload.processes.DataloadProcess;

/**
 * Wrapper class to support creating LCM type dataload process by rest api, this process expected to run on k8s executor
 */
public class LcmDataloadProcess extends DataloadProcess {

    @JsonProperty
    private Component component;

    @JsonCreator
    public LcmDataloadProcess(final String name, final Component component) {
        super(name, "LCM");
        this.component = component;
    }

    /**
     * Create a release process
     * @param name name of process
     * @param version version of LCm component
     * @return
     */
    public static LcmDataloadProcess createReleaseProcess(final String name, final String version) {
        return new LcmDataloadProcess(name, new Component("lcm-brick-release", version));
    }

    /**
     * Create a provision process
     * @param name name of process
     * @param version version of LCm component
     * @return
     */
    public static LcmDataloadProcess createProvisionProcess(final String name, final String version) {
        return new LcmDataloadProcess(name, new Component("lcm-brick-provision", version));
    }

    /**
     * Create a rollout process
     * @param name name of process
     * @param version version of LCm component
     * @return
     */
    public static LcmDataloadProcess createRolloutProcess(final String name, final String version) {
        return new LcmDataloadProcess(name, new Component("lcm-brick-rollout", version));
    }

    @JsonTypeName("component")
    static class Component {

        private String name;
        private String version;

        public Component(String name, String version) {
            this.name = name;
            this.version = version;
        }

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }
    }
}
