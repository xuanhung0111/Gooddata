package com.gooddata.qa.graphene.entity.variable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AttributeVariable extends AbstractVariable {

    private String attribute;
    private List<String> elements;
    private boolean userSpecificValues;

    public AttributeVariable(String name) {
        super(name);
        elements = new ArrayList<String>();
        userSpecificValues = false;
    }

    public AttributeVariable withUserSpecificValues() {
        userSpecificValues = true;
        return this;
    }

    public AttributeVariable withAttributeElements(String... elements) {
        this.elements.addAll(Arrays.asList(elements));
        return this;
    }

    public AttributeVariable withAttribute(String attribute) {
        this.attribute = attribute;
        return this;
    }

    public String getAttribute() {
        return attribute;
    }

    public List<String> getAttributeElements() {
        return elements;
    }

    public boolean isUserSpecificValues() {
        return userSpecificValues;
    }
}
