package com.gooddata.qa.utils.browser;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

public class BrowserUtils {
    public static Object runScript(WebDriver driver, String script, Object... args) {
        if (driver instanceof JavascriptExecutor) {
            return ((JavascriptExecutor) driver).executeScript(script, args);
        } else {
            throw new IllegalStateException("This driver does not support JavaScript!");
        }
    }
}
