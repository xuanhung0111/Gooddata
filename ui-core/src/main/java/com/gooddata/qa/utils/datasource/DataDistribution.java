package com.gooddata.qa.utils.datasource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "dataSource", "target" })
public class DataDistribution {

    public DataDistribution(String dataSource, Target target) {
        this.dataSource = dataSource;
        this.target = target;
    }

    public DataDistribution(String dataSource) {
        this.dataSource = dataSource;
    }

    @JsonProperty("dataSource")
    private String dataSource;
    @JsonProperty("target")
    private Target target;

}
