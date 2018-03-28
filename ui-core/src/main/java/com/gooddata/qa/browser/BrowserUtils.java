package com.gooddata.qa.browser;

import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import com.gooddata.qa.graphene.utils.WaitUtils;

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

    public static void closeCurrentTab(WebDriver browser) {
        browser.close();
    }

    public static String getCurrentBrowserAgent(WebDriver browser) {
        Capabilities capabilities = ((RemoteWebDriver) browser).getCapabilities();

        return capabilities.getBrowserName() + " " + capabilities.getVersion() + " - "
                + capabilities.getCapability("platform");
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

    public static void switchToMainWindow(WebDriver browser) {
        browser.switchTo().defaultContent();
    }
    
    public static void refreshCurrentPage(WebDriver browser) {
        // Refresh the page and wait until the old content disappears.
        // In general, performing page refresh and locating a page fragment
        // can fail because old fragment is found just before the DOM refresh.
        
        // 1) Insert new element with unique class name
        // It is not possible to locate "body", then refresh and then wait for stale element exception,
        // because Graphene will automatically locate gone element - we must work with element that will disappear.
        String MAGIC = "MAGICrefreshCurrentPage";
        String script = "var magic = document.createElement('div'); magic.className='" + MAGIC + "'; "
                + "document.getElementsByTagName('body')[0].appendChild(magic);";
        ((JavascriptExecutor) browser).executeScript(script);
        
        // 2) Locate magic element, refresh and wait until magic element disappears
        WebElement magic = browser.findElement(By.className(MAGIC));
        browser.navigate().refresh();
        WaitUtils.waitForElementNotPresent(magic);
    }
}
