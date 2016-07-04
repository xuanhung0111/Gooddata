package com.gooddata.qa.utils.browser;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public class BrowserUtils {
    public static Object runScript(WebDriver driver, String script, Object... args) {
        if (driver instanceof JavascriptExecutor) {
            return ((JavascriptExecutor) driver).executeScript(script, args);
        } else {
            throw new IllegalStateException("This driver does not support JavaScript!");
        }
    }

    public static void contextClick(WebDriver driver, WebElement onElement) {
        new Actions(driver).moveToElement(onElement).perform();
        StringBuilder script = new StringBuilder()
            .append("var evt = arguments[0].ownerDocument.createEvent('MouseEvents');")
            .append("evt.initMouseEvent('contextmenu', true, true,")
            .append("window, 1, 0, 0, 0, 0, false,")
            .append("false, false, false, 2, null);")
            .append("!arguments[0].dispatchEvent(evt); // dispatch for firefox + others");
        runScript(driver, script.toString(), onElement);
    }
}
