package com.gooddata.qa.graphene.entity.visualization;

import com.gooddata.md.Attribute;

import java.util.UUID;

public class CategoryBucket {

    private final String localIdentifier = generateHashString();
    private String attribute;
    private String displayForm;
    private Type type;

    private CategoryBucket(String attribute, String displayForm, Type type) {
        this.attribute = attribute;
        this.displayForm = displayForm;
        this.type = type;
    }

    public String getAttribute() {
        return attribute;
    }

    public String getDisplayForm() {
        return displayForm;
    }

    public Type getType() {
        return type;
    }

    public String getLocalIdentifier() {
        return localIdentifier;
    }

    public static CategoryBucket createCategoryBucket(Attribute attribute, Type type) {
        return new CategoryBucket(attribute.getUri(), attribute.getDefaultDisplayForm().getUri(), type);
    }

    private String generateHashString() {
        return UUID.randomUUID().toString().substring(0, 5);
    }

    public enum Type {
        ATTRIBUTE, VIEW, TREND, STACK, SEGMENT
    }
}
