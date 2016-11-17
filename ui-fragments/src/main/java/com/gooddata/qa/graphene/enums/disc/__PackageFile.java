package com.gooddata.qa.graphene.enums.disc;

public enum __PackageFile {

    BASIC("Basic.zip");

    private String name;

    private __PackageFile(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
