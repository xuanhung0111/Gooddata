package com.gooddata.qa.graphene.entity.visualization;

import com.gooddata.md.Attribute;

public class CategoryBucket {

    private String attribute;
    private String displayForm;
    private String collection;
    private String type;

    private CategoryBucket(String attribute, String displayForm, String collection, String type) {
        this.attribute = attribute;
        this.displayForm = displayForm;
        this.collection = collection;
        this.type = type;
    }

    public String getAttribute() {
        return attribute;
    }

    public String getDisplayForm() {
        return displayForm;
    }

    public String getCollection() {
        return collection;
    }

    public String getType() {
        return type;
    }

    public static CategoryBucket createViewByBucket(Attribute attribute) {
        return new CategoryBucket(attribute.getUri(), attribute.getDefaultDisplayForm().getUri(), "view",
                "attribute");
    }

    public static CategoryBucket createStackByBucket(Attribute attribute) {
        return new CategoryBucket(attribute.getUri(), attribute.getDefaultDisplayForm().getUri(), "stack",
                "attribute");
    }
}
