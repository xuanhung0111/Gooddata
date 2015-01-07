package com.gooddata.qa.graphene.entity.dlui;

import java.util.Collection;
import java.util.List;

import com.gooddata.qa.graphene.entity.dlui.Field.FieldTypes;
import com.google.common.collect.Lists;

public class DataSource {

    private String datasourceName;
    private List<Dataset> datasets = Lists.newArrayList();

    public String getName() {
        return datasourceName;
    }

    public DataSource setName(String datasourceName) {
        this.datasourceName = datasourceName;
        return this;
    }

    public List<Dataset> getDatasetInSpecificFilter(FieldTypes fieldType) {
        if (fieldType == FieldTypes.ALL)
            return Lists.newArrayList(datasets);
        
        List<Dataset> filteredDataset = Lists.newArrayList();
        for (Dataset dataset : datasets) {
            if (!dataset.getFieldsInSpecificFilter(fieldType).isEmpty())
                filteredDataset.add(dataset);
        }

        return filteredDataset;
    }

    public DataSource setDatasets(Collection<Dataset> datasets) {
        this.datasets = Lists.newArrayList(datasets);
        return this;
    }
}
