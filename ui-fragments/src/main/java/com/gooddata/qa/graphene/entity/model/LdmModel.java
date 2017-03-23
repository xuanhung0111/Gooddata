package com.gooddata.qa.graphene.entity.model;

import static java.util.stream.Collectors.joining;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;

import java.util.ArrayList;
import java.util.List;

public class LdmModel {

    private List<Dataset> datasets;

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
}
