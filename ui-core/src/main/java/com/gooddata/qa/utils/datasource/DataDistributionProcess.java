package com.gooddata.qa.utils.datasource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gooddata.dataload.processes.DataloadProcess;

public class DataDistributionProcess extends DataloadProcess {

    private Segment segment;
    private Target target;
    private DataDistribution dataDistribution;
    private Config config;
    @JsonProperty
    private Component component;

    public DataDistributionProcess(final String name , final String dataSource , final String uri, final String version) {
        super(name, "ETL");
        this.segment = new Segment(uri);
        this.target = new Target(segment);
        this.dataDistribution = new DataDistribution(dataSource, target);
        this.config = new Config(dataDistribution) ;
        this.component = new Component("gdc-data-distribution", version, config);
    }

    public DataDistributionProcess(final String name, final String dataSource, final String version) {
        super(name, "ETL");
        this.dataDistribution = new DataDistribution(dataSource);
        this.config = new Config(dataDistribution);
        this.component = new Component("gdc-data-distribution", version, config);
    }

}
