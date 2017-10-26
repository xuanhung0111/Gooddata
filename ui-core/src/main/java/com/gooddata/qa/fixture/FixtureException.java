package com.gooddata.qa.fixture;

public class FixtureException extends RuntimeException {
    public FixtureException(String msg) {
        super(msg);
    }

    public FixtureException(String message, Throwable cause) {
        super(message, cause);
    }
}
