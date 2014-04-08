/**
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.utils.http;

/**
 * Exception indicating that we received status code other than expected.
 * You can get returned status code using {@link #getStatusCode()}
 */
public class InvalidStatusCodeException extends RuntimeException {

    private int statusCode;

    public InvalidStatusCodeException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
