package com.gooddata.qa.graphene.entity.variable;

import static java.util.Collections.unmodifiableMap;

import java.util.HashMap;
import java.util.Map;

public class NumericVariable extends AbstractVariable {

    private int defaultNumber;
    private Map<String, Integer> userSpecificNumber = new HashMap<>();

    public NumericVariable(String name) {
        super(name);
    }

    public NumericVariable withDefaultNumber(int number) {
        defaultNumber = number;
        return this;
    }

    public NumericVariable withUserSpecificNumber(String userProfileUri, int specificNumber) {
        this.userSpecificNumber.put(userProfileUri, specificNumber);
        return this;
    }

    public int getDefaultNumber() {
        return defaultNumber;
    }

    public Map<String, Integer> getUserSpecificNumber() {
        return unmodifiableMap(userSpecificNumber);
    }
}
