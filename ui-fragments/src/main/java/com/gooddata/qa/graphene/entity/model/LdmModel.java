package com.gooddata.qa.graphene.entity.model;

import static java.util.stream.Collectors.joining;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LdmModel {

    private List<Dataset> datasets;
    private static final Logger logger = LoggerFactory.getLogger(LdmModel.class);

    public LdmModel() {
        this.datasets = new ArrayList<>();
    }

    public static String loadFromFile(String filePath) {
        return getResourceAsString(filePath);
    }

    public LdmModel withDataset(Dataset dataset) {
        this.datasets.add(dataset);
        return this;
    }

    public String buildMaql() {
        return datasets.stream().map(Dataset::buildMaql).collect(joining());
    }

    //Function use for create MAQL with Primary key 
    public String buildMaqlUsingPrimaryKey() {
        String maql = datasets.stream().map(Dataset::buildMaqlUsingPrimaryKey).collect(joining());
        logger.info("MAQL update Model has Primary key: " + maql);
        return maql;
    }

    public String buildMaqlUsingPrimaryKeyNoMainLabel() {
        String maql = datasets.stream().map(Dataset::buildMaqlUsingPrimaryKeyNoMainLabel).collect(joining());
        logger.info("MAQL update Model has Primary key: " + maql);
        return maql;
    }

    public String buildMaqlChangeDefaultLabel() {
        String maql = datasets.stream().map(Dataset::buildDefaultLabels).collect(joining());
        logger.info("MAQL update Model change default Label: " + maql);
        return maql;
    }

    public String buildMaqlFactTableGrain() {
        String maql = datasets.stream().map(Dataset::buildMaqlFactTableGrain).collect(joining());
        logger.info("MAQL update Model Fact Table Grain: " + maql);
        return maql;
    }
}
