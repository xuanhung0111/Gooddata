package com.gooddata.qa.browser;

import com.gooddata.qa.utils.io.ResourceUtils;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.arquillian.drone.browserstack.webdriver.BrowserStackDriver;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
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

        if (browser instanceof BrowserStackDriver) {
            capabilities = ((BrowserStackDriver) browser).getCapabilities();
            if (capabilities.getCapability("platformName") != null) {
                return capabilities.getCapability("browser") + " - " + capabilities.getCapability("platformName");
            }
        }

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

    public static Object runScript(WebDriver driver, String script, Object... args) {
        if (driver instanceof JavascriptExecutor) {
            return ((JavascriptExecutor) driver).executeScript(script, args);
        } else {
            throw new IllegalStateException("This driver does not support JavaScript!");
        }
    }

    public static void dragAndDrop(WebDriver driver, String fromSelector, String toSelector) {
        // just to be sure, load dragdrop simulation suppport every time dragdrop is needed
        String scriptDragDropSetup = "/scripts/setupDragDropSimulation.js";
        String scriptContent = ResourceUtils.getResourceAsString(scriptDragDropSetup);

        BrowserUtils.runScript(driver, scriptContent);

        String dragScript = String.format("jQuery('%1s').simulateDragDrop({ dropTarget: '%2s'});",
                fromSelector, toSelector);
        BrowserUtils.runScript(driver, dragScript);
    }
}
