package com.gooddata.qa.browser;

import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;

import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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
        //Wait for loading browser of new tab
        sleepTightInSeconds(1);
        List<String> windowHandles = BrowserUtils.getWindowHandles(browser);
        browser.switchTo().window(windowHandles.get(windowHandles.size() - 1));
    }

    public static void switchToFirstTab(WebDriver browser) {
        List<String> windowHandles = BrowserUtils.getWindowHandles(browser);
        browser.switchTo().window(windowHandles.get(0));
    }

    public static String getCurrentBrowserAgent(WebDriver browser) {
        Capabilities capabilities = ((RemoteWebDriver) browser).getCapabilities();

        return capabilities.getBrowserName() + " - " + capabilities.getCapability("platform");
    }

    public static boolean canAccessGreyPage(WebDriver browser) {
        final String currentBrowserAgent = getCurrentBrowserAgent(browser);

        if (currentBrowserAgent.contains("iOS")) {
            return true;
        }

        if (Stream.of("internet explorer", "ANDROID", "safari")
                .anyMatch(currentBrowserAgent::contains)) {
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

    public static void switchToMainWindow(WebDriver browser) {
        browser.switchTo().defaultContent();
    }
}
