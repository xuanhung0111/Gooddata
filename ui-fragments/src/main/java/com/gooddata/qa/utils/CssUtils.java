/**
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.utils;

public final class CssUtils {

    private CssUtils() {
    }

    /**
     * Helper method to convert name of object to derived
     * name used in CSS fragments. Similar function exists
     * on our UI and is used to generate selenium friendly CSS
     * classes.
     */
    public static String simplifyText(String text) {
        String replacedText = text.replaceAll("[^a-zA-Z0-9]", "_");
        return replacedText.toLowerCase();
    }
}
