package com.gooddata.qa.graphene.disc.dto;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.springframework.web.util.UriTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * ETL Process
 *
 * copied from msf-dto: https://github.com/gooddata/msf-dto/blob/master/src/main/java/com/gooddata/service/model/dataload/process/Process.java
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("process")
public final class Process implements Comparable<Process> {

    public static final String PROCESS_URI = "/gdc/projects/{projectId}/dataload/processes/{processId}";
    public static final String SOURCE_URI = PROCESS_URI + "/source";
    public static final String EXECUTIONS_URI = "/gdc/projects/{projectId}/dataload/processes/{processId}/executions";
    public static final UriTemplate PROCESS_URI_TEMPLATE = new UriTemplate(PROCESS_URI);
    public static final UriTemplate SOURCE_URI_TEMPLATE = new UriTemplate(SOURCE_URI);
    public static final UriTemplate EXECUTIONS_URI_TEMPLATE = new UriTemplate(EXECUTIONS_URI);

    private final String type;
    private final String name;
    private final String path;

    @Deprecated
    private final List<String> graphs;

    private final List<String> executables;

    private final Links links;

    @JsonCreator
    private Process(@JsonProperty("type") String type, @JsonProperty("name") String name, @JsonProperty("path") String path,
                    @JsonProperty("graphs") List<String> graphs, @JsonProperty("executables") List<String> executables, @JsonProperty("links") Links links) {
        this(type, name, path, graphs, executables, links, false);
    }

    private Process(String type, String name, String path,
                    List<String> graphs, List<String> executables, Links links, boolean oldClient) {
        Validate.notEmpty(name, "name can't be empty");

        this.type = oldClient ? null : StringUtils.isEmpty(type) ? "GRAPH" : type;
        this.name = name;
        this.path = path;

        final List<String> listToUse = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(executables)) {
            listToUse.addAll(executables);
        } else if (CollectionUtils.isNotEmpty(graphs)) {
            listToUse.addAll(graphs);
        }

        this.graphs = listToUse;
        this.executables = oldClient ? null : listToUse;

        this.links = links;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public Links getLinks() {
        return links;
    }

    public List<String> getGraphs() {
        return new ArrayList<String>(graphs);
    }

    public List<String> getExecutables() {
        return executables;
    }

    public String getType() {
        return type;
    }

    @JsonIgnore
    public String getProcessId() {
        if (links == null || links.getSelf() == null) {
            throw new IllegalArgumentException("links.self is null");
        }
        return PROCESS_URI_TEMPLATE.match(links.getSelf()).get("processId");
    }


    private static Links createLinks(String projectId, String processId, boolean includeSourceLink) {
        Validate.notEmpty(projectId, "projectId can't be empty");
        Validate.notEmpty(processId, "processId can't be empty");
        String self = PROCESS_URI_TEMPLATE.expand(projectId, processId).toString();
        String executions = EXECUTIONS_URI_TEMPLATE.expand(projectId, processId).toString();
        String source = SOURCE_URI_TEMPLATE.expand(projectId, processId).toString();
        return new Links(self, executions, includeSourceLink ? source : null);
    }

    @Override
    public int compareTo(Process o) {
        if (this.getName() == null) {
            if (o.getName() != null) {
                return -1;
            } else {
                return 0;
            }
        }
        return this.getName().compareTo(o.getName());

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Process process = (Process) o;

        if (!name.equals(process.name)) return false;
        if (!type.equals(process.type)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    public static class Builder {
        private String type;
        private String name;
        private String path;
        private Links links;

        private List<String> executables;
        private List<String> graphs;

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder executables(List<String> executables) {
            this.executables = executables;
            return this;
        }

        public Builder graphs(List<String> graphs) {
            this.graphs = graphs;
            return this;
        }

        /**
         * @param projectId
         * @param processId
         * @param includeSourceLink - should be the 'source' link included to 'process.links'? This is here because of the backward compatibility.
         */
        public Builder links(String projectId, String processId, boolean includeSourceLink) {
            this.links = createLinks(projectId, processId, includeSourceLink);
            return this;
        }

        /**
         * @param projectId
         * @param processId
         */
        public Builder links(String projectId, String processId) {
            this.links = createLinks(projectId, processId, false);
            return this;
        }

        /**
         * Constructs new {@link Process} instance
         *
         * @return
         */
        public Process build() {
            return new Process(this.type, this.name, this.path, this.graphs, this.executables, this.links);
        }

        public Process build(boolean oldClient) {
            return new Process(this.type, this.name, this.path, this.graphs, this.executables, this.links, oldClient);
        }

    }


    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Links {

        private final String self;
        private final String executions;
        private final String source;

        @JsonCreator
        public Links(@JsonProperty("self") String self, @JsonProperty("executions") String executions, @JsonProperty("source") String source) {
            Validate.notEmpty(self, "self can't be empty");
            Validate.notEmpty(executions, "executions can't be empty");

            this.self = self;
            this.executions = executions;
            this.source = source;
        }

        public String getSelf() {
            return self;
        }

        public String getExecutions() {
            return executions;
        }

        public String getSource() {
            return source;
        }
    }
}
