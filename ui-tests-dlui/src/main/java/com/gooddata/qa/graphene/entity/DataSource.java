package com.gooddata.qa.graphene.entity;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.entity.Field.FieldTypes;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class DataSource {

    private static final By BY_DATASOURCE_TITLE = By.cssSelector(".source-title");

    private String name = "";
    private List<Dataset> datasets = Lists.newArrayList();

    public DataSource() {}

    public String getName() {
        return name;
    }

    public DataSource withName(String name) {
        this.name = name;
        return this;
    }

    public List<Dataset> getAvailableDatasets(FieldTypes fieldType) {
        List<Dataset> filteredDataset = Lists.newArrayList();
        for (Dataset dataset : datasets) {
            if (!dataset.getFieldsInSpecificFilter(fieldType).isEmpty())
                filteredDataset.add(dataset);
        }

        return filteredDataset;
    }

    public DataSource withDatasets(Dataset... datasets) {
        return withDatasets(Lists.newArrayList(datasets));
    }

    public DataSource withDatasets(List<Dataset> datasets) {
        this.datasets.addAll(datasets);
        return this;
    }

    public DataSource updateDatasetStatus(Dataset... customDatasets) {
        checkValidDatasets(customDatasets);

        for (final Dataset customDataset : customDatasets) {
            Iterables.find(Lists.newArrayList(datasets), new Predicate<Dataset>() {

                @Override
                public boolean apply(Dataset dataset) {
                    return customDataset.getName().equals(dataset.getName());
                }
            }).updateFieldStatus(customDataset.getAllFields());
        }

        return this;
    }

    public List<Dataset> getSelectedDataSets() {
        List<Dataset> selectedDatasets = Lists.newArrayList();
        for (Dataset dataset : datasets) {
            if (dataset.getSelectedFields().size() > 0)
                selectedDatasets.add(dataset);
        }

        return selectedDatasets;
    }

    public void applyAddSelectedFields() {
        addSelectedField(true);
    }

    public void cancelAddSelectedFields() {
        addSelectedField(false);
    }

    public boolean hasCorrespondingWebElement(Collection<WebElement> elements) {
        return Iterables.any(elements, findWebElementPredicate());
    }

    public WebElement getCorrespondingWebElement(Collection<WebElement> elements) {
        return Iterables.find(elements, findWebElementPredicate());
    }

    private Predicate<WebElement> findWebElementPredicate() {
        return new Predicate<WebElement>() {

            @Override
            public boolean apply(WebElement element) {
                return name.equals(waitForElementVisible(BY_DATASOURCE_TITLE, element).getText());
            }
        };
    }

    private void addSelectedField(boolean confirmed) {
        for (Dataset dataset : datasets) {
            if (dataset.getSelectedFields().size() > 0) {
                dataset.addSelectedFields(confirmed);
            }
        }
    }

    private void checkValidDatasets(Dataset... validatedDatasets) {
        for (final Dataset validatedDataset : validatedDatasets) {
            try {
                Iterables.find(getAvailableDatasets(FieldTypes.ALL), new Predicate<Dataset>() {

                    @Override
                    public boolean apply(Dataset dataset) {
                        return dataset.getName().equals(validatedDataset.getName());
                    }
                });
            } catch (NoSuchElementException e) {
                throw new IllegalStateException("Data source '" + this.name
                        + "' doesn't contain dataset '" + validatedDataset.getName() + "'", e);
            }
        }
    }
}
