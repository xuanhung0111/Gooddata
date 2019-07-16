package com.gooddata.qa.utils.datasource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Config {

    public Config(DataDistribution dataDistribution) {
        this.dataDistribution = dataDistribution;
    }

    @JsonProperty("dataDistribution")
    private DataDistribution dataDistribution;

}
