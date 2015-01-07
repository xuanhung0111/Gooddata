package com.gooddata.qa.graphene.enums.dlui;

import java.util.Arrays;
import java.util.List;

import com.gooddata.qa.graphene.entity.dlui.Field;
import com.gooddata.qa.graphene.entity.dlui.Field.FieldTypes;
import com.google.common.collect.Lists;

public enum AdditionalDatasets {

    PERSON_WITH_NEW_FIELDS("person", new Field().setFieldName("Position").setFieldType(FieldTypes.ATTRIBUTE)),
    PERSON_WITH_NEW_DATE_FIELD("person", new Field().setFieldName("Position").setFieldType(FieldTypes.ATTRIBUTE),
            new Field().setFieldName("Date").setFieldType(FieldTypes.DATE)),
    OPPORTUNITY_WITH_NEW_FIELDS("opportunity", new Field().setFieldName("Title2").setFieldType(FieldTypes.ATTRIBUTE),
            new Field().setFieldName("Label").setFieldType(FieldTypes.LABLE_HYPERLINK),
            new Field().setFieldName("Totalprice2").setFieldType(FieldTypes.FACT)),
    OPPORTUNITY_WITH_NEW_DATE_FIELD("opportunity", new Field().setFieldName("Title2").setFieldType(FieldTypes.ATTRIBUTE),
            new Field().setFieldName("Label").setFieldType(FieldTypes.LABLE_HYPERLINK),
            new Field().setFieldName("Totalprice2").setFieldType(FieldTypes.FACT),
            new Field().setFieldName("Date").setFieldType(FieldTypes.DATE));
    
    private String datasetName;
    private List<Field> fields;
    
    private AdditionalDatasets(String datasetName, Field... fields) {
        this.datasetName = datasetName;
        this.fields = Arrays.asList(fields);
    }
    
    public List<Field> getFields() {
        return Lists.newArrayList(fields);
    }
    
    public String getName() {
        return this.datasetName;
    }
}
