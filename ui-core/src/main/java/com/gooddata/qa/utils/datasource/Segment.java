package com.gooddata.qa.utils.datasource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Segment {

    public Segment(String uri) {
        this.uri = uri;
    }

    @JsonProperty("uri")
    private String uri;

    public Segment withUri(String uri) {
        this.uri = uri;
        return this;
    }

}
