package com.gooddata.qa.graphene.entity;

import java.util.List;
import java.util.NoSuchElementException;

import com.gooddata.qa.graphene.entity.Field.FieldTypes;
import com.gooddata.qa.graphene.enums.AdditionalDatasets;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import static org.testng.Assert.*;

public class Dataset {

    private String name;
    private List<Field> fields = Lists.newArrayList();
    private List<Field> selectedFields = Lists.newArrayList();

    public Dataset() {}

    public Dataset(AdditionalDatasets dataset) {
        this.name = dataset.getName();
        this.fields.addAll(dataset.getFields());
    }

    public String getName() {
        return name;
    }

    public Dataset withName(String name) {
        this.name = name;
        return this;
    }

    public List<Field> getAllFields() {
        return Lists.newArrayList(fields);
    }

    public List<Field> getFieldsInSpecificFilter(FieldTypes fieldType) {
        if (fieldType == FieldTypes.ALL)
            return Lists.newArrayList(fields);

        List<Field> filteredFields = Lists.newArrayList();
        for (Field field : this.fields) {
            if (field.getType() == fieldType)
                filteredFields.add(field);
        }
        return filteredFields;
    }

    public Dataset withFields(Field... fields) {
        this.fields = Lists.newArrayList(fields);
        return this;
    }

    public Dataset withSelectedFields(Field... fields) {
        checkValidSelectedFileds(fields);

        this.selectedFields = Lists.newArrayList(fields);

        return this;
    }

    public List<Field> getSelectedFields() {
        return Lists.newArrayList(selectedFields);
    }

    public void removeAddedField(List<Field> addedfields) {
        for (final Field addedField : addedfields) {
            assertTrue(Iterables.removeIf(fields, new Predicate<Field>() {

                @Override
                public boolean apply(Field field) {
                    return field.getName().equals(addedField.getName());
                }
            }));
        }
    }

    private void checkValidSelectedFileds(Field... selectedfields) {
        for (final Field selectedField : selectedfields) {
            try {
                Iterables.find(getAllFields(), new Predicate<Field>() {

                    @Override
                    public boolean apply(Field field) {
                        return field.getName().equals(selectedField.getName());
                    }
                });
            } catch (NoSuchElementException e) {
                throw new IllegalStateException("Dataset '" + this.name
                        + "' doesn't contain field '" + selectedField.getName() + "'", e);
            }
        }
    }
}
