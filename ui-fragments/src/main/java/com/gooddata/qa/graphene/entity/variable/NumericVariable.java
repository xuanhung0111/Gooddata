package com.gooddata.qa.graphene.entity.variable;

public class NumericVariable extends AbstractVariable {

    private int defaultNumber;
    private int userNumber;

    public NumericVariable(String name) {
        super(name);
    }

    public NumericVariable withDefaultNumber(int number) {
        defaultNumber = number;
        return this;
    }

    public NumericVariable withUserNumber(int number) {
        userNumber = number;
        return this;
    }

    public int getDefaultNumber() {
        return defaultNumber;
    }

    public int getUserNumber() {
        return userNumber;
    }
}
