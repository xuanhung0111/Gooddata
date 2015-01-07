package com.gooddata.qa.graphene.entity.dlui;

import java.util.List;

import com.gooddata.qa.graphene.entity.dlui.Field.FieldTypes;
import com.google.common.collect.Lists;

public class Dataset {

    private String datasetName;
    private List<Field> fields = Lists.newArrayList();

    public String getName() {
        return datasetName;
    }

    public Dataset setName(String datasetName) {
        this.datasetName = datasetName;
        return this;
    }

    public List<Field> getFieldsInSpecificFilter(FieldTypes fieldType) {
        if (fieldType == FieldTypes.ALL)
            return Lists.newArrayList(fields);

        List<Field> filteredFields = Lists.newArrayList();
        for (Field field : this.fields) {
            if (field.getFieldType() == fieldType)
                filteredFields.add(field);
        }
        return filteredFields;
    }

    public Dataset setFields(List<Field> fields) {
        this.fields.addAll(fields);
        return this;
    }
}
