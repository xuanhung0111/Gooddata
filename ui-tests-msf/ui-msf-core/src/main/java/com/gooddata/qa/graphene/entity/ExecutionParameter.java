package com.gooddata.qa.graphene.entity;

public class ExecutionParameter {

    private String name;
    private Object value;

    public ExecutionParameter(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public Object getValue() {
        return this.value;
    }
}
