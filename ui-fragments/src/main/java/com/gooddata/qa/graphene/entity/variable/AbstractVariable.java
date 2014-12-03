package com.gooddata.qa.graphene.entity.variable;

public class AbstractVariable {

    protected String name;

    public AbstractVariable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
