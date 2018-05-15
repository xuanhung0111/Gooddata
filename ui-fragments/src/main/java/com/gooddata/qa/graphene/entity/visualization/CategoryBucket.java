package com.gooddata.qa.graphene.entity.visualization;

import com.gooddata.md.Attribute;

import java.util.UUID;

public class CategoryBucket {

    private final String localIdentifier = generateHashString();
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

    public String getLocalIdentifier() {
        return localIdentifier;
    }

    public static CategoryBucket createViewByBucket(Attribute attribute) {
        return new CategoryBucket(attribute.getUri(), attribute.getDefaultDisplayForm().getUri(), "view",
                "attribute");
    }

    public static CategoryBucket createStackByBucket(Attribute attribute) {
        return new CategoryBucket(attribute.getUri(), attribute.getDefaultDisplayForm().getUri(), "stack",
                "attribute");
    }

    public static CategoryBucket createAttributeByBucket(Attribute attribute) {
        return new CategoryBucket(attribute.getUri(), attribute.getDefaultDisplayForm().getUri(), "attribute",
                "attribute");
    }

    private String generateHashString() {
        return UUID.randomUUID().toString().substring(0, 5);
    }
}
