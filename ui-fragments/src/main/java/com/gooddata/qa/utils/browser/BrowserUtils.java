package com.gooddata.qa.utils.browser;

import org.jboss.arquillian.drone.api.annotation.Default;
import org.jboss.arquillian.graphene.context.GrapheneContext;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public class BrowserUtils {

    public static WebDriver getBrowserContext() {
        return GrapheneContext.getContextFor(Default.class).getWebDriver(WebDriver.class);
    }

    public static Object runScript(WebDriver driver, String script, Object... args) {
        if (driver instanceof JavascriptExecutor) {
            return ((JavascriptExecutor) driver).executeScript(script, args);
        } else {
            throw new IllegalStateException("This driver does not support JavaScript!");
        }
    }
}
