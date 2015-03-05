package com.gooddata.qa.graphene.enums.dlui;

import java.util.Arrays;
import java.util.List;

import com.gooddata.qa.graphene.entity.dlui.Field;
import com.gooddata.qa.graphene.entity.dlui.Field.FieldTypes;
import com.google.common.collect.Lists;

public enum AdditionalDatasets {

    PERSON_WITH_NEW_FIELDS("person", new Field("Position",
            FieldTypes.ATTRIBUTE)),
    PERSON_WITH_NEW_DATE_FIELD("person", new Field("Position",
            FieldTypes.ATTRIBUTE), new Field("Date", FieldTypes.DATE)),
    OPPORTUNITY_WITH_NEW_FIELDS("opportunity", new Field("Title2",
            FieldTypes.ATTRIBUTE), new Field("Label",
            FieldTypes.LABLE_HYPERLINK), new Field("Totalprice2",
            FieldTypes.FACT)),
    OPPORTUNITY_WITH_NEW_DATE_FIELD("opportunity", new Field("Title2",
            FieldTypes.ATTRIBUTE), new Field("Label",
            FieldTypes.LABLE_HYPERLINK), new Field("Totalprice2",
            FieldTypes.FACT), new Field("Date", FieldTypes.DATE));

    private String name;
    private List<Field> fields;

    private AdditionalDatasets(String name, Field... fields) {
        this.name = name;
        this.fields = Arrays.asList(fields);
    }

    public List<Field> getFields() {
        return Lists.newArrayList(fields);
    }

    public String getName() {
        return this.name;
    }
}
