package com.gooddata.qa.graphene.entity.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AttributeFilterItem extends FilterItem {

    private String attribute;
    private List<String> values;

    AttributeFilterItem(String attribute, String... values) {
        this.attribute = attribute;
        this.values = new ArrayList<String>(Arrays.asList(values));
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public List<String> getValues() {
        return values;
    }

    public void addValue(String value) {
        values.add(value);
    }
}
