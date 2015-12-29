package com.gooddata.qa.graphene.dto;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang.Validate;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.springframework.web.util.UriTemplate;

import com.google.common.base.Objects;

/**
 * ETL Processes
 * <p/>
 * copied from msf-dto: https://github.com/gooddata/msf-dto/blob/master/src/main/java/com/gooddata/service/model/dataload/process/Processes.java
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("processes")
public class Processes {

    public static final String PROCESSES_URI = "/gdc/projects/{projectId}/dataload/processes";
    public static final UriTemplate PROCESSES_URI_TEMPLATE = new UriTemplate(PROCESSES_URI);

    private final List<Process> items;
    private final Links links;

    @JsonCreator
    public Processes(@JsonProperty("items") List<Process> items, @JsonProperty("links") Links links) {
        Validate.notNull(items, "Items can't be null");
        this.items = new ArrayList<Process>(items);
        Collections.sort(this.items);
        this.links = links;
    }

    public Processes(List<Process> items) {
        this(items, null);
    }

    public List<Process> getItems() {
        return new ArrayList<Process>(items);
    }

    public Links getLinks() {
        return links;
    }

    public static class Links {

        private final String self;

        @JsonCreator
        public Links(@JsonProperty("self") String self) {
            Validate.notEmpty(self, "self can't be empty");
            this.self = self;
        }

        public String getSelf() {
            return self;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(items, links);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Processes)) {
            return false;
        }
        final Processes other = (Processes) obj;

        return Objects.equal(other.items, this.items) &&
                Objects.equal(other.links, this.links);
    }

    public Process getDataloadProcess() {
        for (Process process : getItems()) {
            if ("DATALOAD".equals(process.getType())) {
                return process;
            }
        }
        throw new NoSuchElementException("No dataload process in the project!");
    }

    public int getDataloadProcessCount() {
        int count = 0;
        for (Process process : getItems()) {
            if ("DATALOAD".equals(process.getType())) {
                count++;
            }
        }
        return count;
    }
}
