package com.gooddata.qa.browser;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

public class BrowserUtils {
    public static List<String> getWindowHandles(WebDriver browser) {
        //http://stackoverflow.com/questions/12729265/switch-tabs-using-selenium-webdriver
        return new ArrayList<> (browser.getWindowHandles());
    }

    public static void switchToLastTab(WebDriver browser) {
        List<String> windowHandles = BrowserUtils.getWindowHandles(browser);
        browser.switchTo().window(windowHandles.get(windowHandles.size() - 1));
    }

    public static void switchToFirstTab(WebDriver browser) {
        List<String> windowHandles = BrowserUtils.getWindowHandles(browser);
        browser.switchTo().window(windowHandles.get(0));
    }

    public static boolean isIE(WebDriver browser) {
        return "internet explorer".equals(getCurrentBrowserAgent(browser));
    }

    public static String getCurrentBrowserAgent(WebDriver browser) {
        System.out.println(((RemoteWebDriver) browser).getCapabilities().getCapability("browserName"));
        return ((RemoteWebDriver) browser).getCapabilities().getBrowserName();
    }
}
