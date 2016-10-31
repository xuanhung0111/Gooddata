package com.gooddata.qa.graphene;

import java.util.List;

import com.gooddata.qa.graphene.entity.dlui.Dataset;
import com.gooddata.qa.graphene.entity.dlui.Field;
import com.gooddata.qa.graphene.entity.dlui.Field.FieldTypes;
import com.google.common.collect.Lists;

public enum AdditionalDatasets {

    PERSON_WITH_NEW_FIELDS("person", new Field("Position", FieldTypes.ATTRIBUTE)),
    PERSON_WITH_NEW_DATE_FIELD(
            "person",
            new Field("Position", FieldTypes.ATTRIBUTE),
            new Field("Date", FieldTypes.DATE)),
    OPPORTUNITY_WITH_NEW_FIELDS(
            "opportunity",
            new Field("Title2", FieldTypes.ATTRIBUTE),
            new Field("Label", FieldTypes.LABEL_HYPERLINK),
            new Field("Totalprice2", FieldTypes.FACT)),
    OPPORTUNITY_WITH_NEW_DATE_FIELD(
            "opportunity",
            new Field("Title2", FieldTypes.ATTRIBUTE),
            new Field("Label", FieldTypes.LABEL_HYPERLINK),
            new Field("Totalprice2", FieldTypes.FACT),
            new Field("Date", FieldTypes.DATE)),
    ARTIST_WITH_NEW_FIELD("artist", new Field("Artisttitle", FieldTypes.ATTRIBUTE)),
    AUTHOR_WITH_NEW_FIELD("author", new Field("Authorname", FieldTypes.ATTRIBUTE)),
    TRACK_WITH_NEW_FIELD("track", new Field("Trackname", FieldTypes.ATTRIBUTE));

    private String name;
    private List<Field> additionalFields;

    private AdditionalDatasets(String name, Field... additionalFields) {
        this.name = name;
        this.additionalFields = Lists.newArrayList(additionalFields);
    }

    public Dataset getDataset() {
        List<Field> fields = Lists.newArrayList();
        for (Field additionalField : additionalFields) {
            fields.add(additionalField.clone());
        }
        return new Dataset().withName(name).withFields(fields);
    }
}
