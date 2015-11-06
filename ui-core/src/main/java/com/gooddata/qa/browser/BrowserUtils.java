package com.gooddata.qa.browser;

import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
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

    public static String getCurrentBrowserAgent(WebDriver browser) {
        Capabilities capabilities = ((RemoteWebDriver) browser).getCapabilities();
        String platform = capabilities.getCapability("platform").toString().toUpperCase();
        return capabilities.getBrowserName() + " - " + platform;
    }

    public static boolean canAccessGreyPage(WebDriver browser) {
        if (getCurrentBrowserAgent(browser).contains("internet explorer")) {
            return false;
        }

        if (getCurrentBrowserAgent(browser).contains("ANDROID")) {
            return false;
        }

        if (getCurrentBrowserAgent(browser).contains("safari")) {
            return false;
        }

        return true;
    }

    public static void maximize(WebDriver browser) {
        browser.manage().window().setPosition(new Point(0, 0));

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        int width = (int) toolkit.getScreenSize().getWidth();
        int height = (int) toolkit.getScreenSize().getHeight();
        browser.manage().window().setSize(new Dimension(width, height));
    }
}
