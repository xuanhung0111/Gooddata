package com.gooddata.qa.graphene.entity.variable;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttributeVariable extends AbstractVariable {

    private String attribute;
    private List<String> attributeValues = new ArrayList<>();
    private Map<String, Collection<String>> userSpecificValues = new HashMap<>();

    public AttributeVariable(String name) {
        super(name);
    }

    public AttributeVariable withAttribute(String attribute) {
        this.attribute = attribute;
        return this;
    }

    public AttributeVariable withAttributeValues(String... values) {
        this.attributeValues.addAll(asList(values));
        return this;
    }

    public AttributeVariable withAttributeValues(Collection<String> values) {
        this.attributeValues.addAll(values);
        return this;
    }

    public AttributeVariable withUserSpecificValues(String userProfileUri, Collection<String> specificValues) {
        this.userSpecificValues.put(userProfileUri, specificValues);
        return this;
    }

    public String getAttribute() {
        return attribute;
    }

    public List<String> getAttributeValues() {
        return unmodifiableList(attributeValues);
    }

    public Map<String, Collection<String>> getUserSpecificValues() {
        return unmodifiableMap(userSpecificValues);
    }
}
