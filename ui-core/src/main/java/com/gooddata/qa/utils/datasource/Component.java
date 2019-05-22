package com.gooddata.qa.utils.datasource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "version", "config" })
public class Component {

    public Component(String name, String version, Config config) {
        this.name = name;
        this.version = version;
        this.config = config;
    }

    @JsonProperty("name")
    private String name;
    @JsonProperty("version")
    private String version;
    @JsonProperty("config")
    private Config config;

}
