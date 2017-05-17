package com.gooddata.qa.fixture;

import java.nio.file.Path;
import java.nio.file.Paths;

public enum Fixture {
    GOODSALES("goodsales");

    private static final String FIXTURE_HOME = "/fixtures";

    private String name;

    private Fixture(String name) {
        this.name = name;
    }

    public Path getPath() {
        return Paths.get(FIXTURE_HOME, name);
    }

    public String getName() {
        return name;
    }
}
