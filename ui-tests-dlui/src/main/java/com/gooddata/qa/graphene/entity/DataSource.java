package com.gooddata.qa.graphene.entity;

import java.util.List;
import java.util.NoSuchElementException;

import com.gooddata.qa.graphene.entity.Field.FieldTypes;
import com.gooddata.qa.graphene.enums.ADSTables;
import com.gooddata.qa.graphene.enums.AdditionalDatasets;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import static org.testng.Assert.*;

public class DataSource {

    private String name;
    private List<Dataset> datasets = Lists.newArrayList();
    private List<Dataset> selectedDatasets = Lists.newArrayList();

    public DataSource() {}

    public DataSource(ADSTables ADSTable) {
        this.name = ADSTable.getDatasourceName();
        for (AdditionalDatasets additionalDataset : ADSTable.getAdditionalDatasets()) {
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

    public DataSource withSelectedDatasets(Dataset... datasets) {
        checkValidSelectedDatasets(datasets);

        this.selectedDatasets = Lists.newArrayList(datasets);

        return this;
    }

    public List<Dataset> getSelectedDataSets() {
        return Lists.newArrayList(selectedDatasets);
    }

    public void removeAddedDataset() {
        for (final Dataset addedDataset : selectedDatasets) {
            Predicate<Dataset> selectedPredicate = new Predicate<Dataset>() {

                @Override
                public boolean apply(Dataset dataset) {
                    return dataset.getName().equals(addedDataset.getName());
                }
            };

            if (addedDataset.getAllFields().size() == addedDataset.getSelectedFields().size())
                assertTrue(Iterables.removeIf(datasets, selectedPredicate));
            else {
                Iterables.find(datasets, selectedPredicate).removeAddedField(
                        addedDataset.getSelectedFields());
            }
        }
    }

    private void checkValidSelectedDatasets(Dataset... selectedDatasets) {
        for (final Dataset selectedDataset : selectedDatasets) {
            try {
                Iterables.find(getDatasetInSpecificFilter(FieldTypes.ALL),
                        new Predicate<Dataset>() {

                            @Override
                            public boolean apply(Dataset dataset) {
                                return dataset.getName().equals(selectedDataset.getName());
                            }
                        });
            } catch (NoSuchElementException e) {
                throw new IllegalStateException("Data source '" + this.name
                        + "' doesn't contain dataset '" + selectedDataset.getName() + "'", e);
            }
        }
    }
}
