package com.gooddata.qa.graphene.entity.dlui;

import java.util.List;

import com.gooddata.qa.graphene.entity.dlui.Field.FieldTypes;
import com.gooddata.qa.graphene.enums.dlui.AdditionalDatasets;
import com.google.common.collect.Lists;

public class Dataset {

    private String name;
    private List<Field> fields = Lists.newArrayList();

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
    
    public void removeFields(Field... fields) {
        this.fields.removeAll(Lists.newArrayList(fields));
    }
}
