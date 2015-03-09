package com.gooddata.qa.graphene.entity.dlui;

import java.util.List;

import com.gooddata.qa.graphene.entity.dlui.Field.FieldTypes;
import com.gooddata.qa.graphene.enums.dlui.ADSTables;
import com.gooddata.qa.graphene.enums.dlui.AdditionalDatasets;
import com.google.common.collect.Lists;

public class DataSource {

    private String name;
    private List<Dataset> datasets = Lists.newArrayList();
    
    public DataSource() {}
    
    public DataSource(ADSTables adsTable) {
        this.name = adsTable.getDatasourceName();
        for(AdditionalDatasets additionalDataset : adsTable.getAdditionalDatasets()) {
            this.datasets.add(new Dataset(additionalDataset));
        }
    }

    public String getName() {
        return name;
    }

    public DataSource withName(String name) {
        this.name = name;
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

    public DataSource withDatasets(Dataset... datasets) {
        this.datasets = Lists.newArrayList(datasets);
        return this;
    }
}
