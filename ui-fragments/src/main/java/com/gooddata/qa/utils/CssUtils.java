/**
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.utils;

import org.apache.commons.lang.StringEscapeUtils;
import org.openqa.selenium.WebElement;

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
        return StringEscapeUtils.escapeHtml(text)
                .replaceAll("[^a-zA-Z0-9]", "_")
                .toLowerCase();
    }

    public static String convertCSSClassTojQuerySelector(String cssClass) {
        return "." + cssClass.replaceAll(" ", ".");
    }

    /**
     * Some cases have shorten text with CSS rules text-overflow set ellipse,
     * Method support to detect above cases.
     * @param title Web element of title
     * @param width The width of title after being shorten (pixel)
     * @return boolean
     */
    public static boolean isShortendTilteDesignByCss(WebElement title, int width) {
        int actualWidth = Integer.parseInt(title.getCssValue("width").replace("px", ""));

        return title.getCssValue("text-overflow").equals("ellipsis") &&
                Math.abs(actualWidth - width) <= 20;
    }
}
