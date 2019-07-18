package com.gooddata.qa.utils.datasource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Target {

    public Target(Segment segment) {
        this.segment = segment;
    }

    @JsonProperty("segment")
    private Segment segment;

    public Target withSegment(Segment segment) {
        this.segment = segment;
        return this;
    }
}
