package com.gooddata.qa.browser;

import com.gooddata.qa.graphene.utils.ElementUtils;
import com.gooddata.qa.graphene.utils.WaitUtils;
import org.jboss.arquillian.drone.api.annotation.Default;
import org.jboss.arquillian.graphene.context.GrapheneContext;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class BrowserUtils {
    private static final String MAGIC = "MagicElementClassForPageReload";

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

    public static void addMagicElementToDOM(WebDriver browser) {
        // Use to avoid MAGIC element added in an embedded iframe and Selenium cannot identify it in default content
        switchToMainWindow(browser);

        String script = "var magic = document.createElement('div'); magic.className='" + MAGIC + "'; "
                + "document.getElementsByTagName('body')[0].appendChild(magic);";

        // When previous action causes page redirect, magic element can be put to DOM
        // and be immediately replaced with new DOM. Let's be safe and do silent retries.
        for (int attempts = 0; attempts < 3; attempts++) {
            ((JavascriptExecutor) browser).executeScript(script);

            if (ElementUtils.isElementPresent(By.className(MAGIC), browser)) {
                return;
            }
        }

        throw new NoSuchElementException("Cannot find magic element in DOM");
    }

    public static boolean isMagicElementPresentInDOM(WebDriver browser) {
        return browser.findElements(By.className(MAGIC)).size() > 0;
    }

    public static void refreshCurrentPage(WebDriver browser) {
        // Refresh the page and wait until the old content disappears.
        // In general, performing page refresh and locating a page fragment
        // can fail because old fragment is found just before the DOM refresh.

        // 1) Insert new element with unique class name
        // It is not possible to locate "body", then refresh and then wait for stale element exception,
        // because Graphene will automatically locate gone element - we must work with element that will disappear.
        addMagicElementToDOM(browser);

        // 2) Refresh and wait until magic element disappears
        browser.navigate().refresh();
        WaitUtils.waitForElementNotPresent(By.className(MAGIC), browser);
    }

    /**
     * Drag and drop for custom touch backend
     * Covers the scenario when dragging fromSelector to toSelector, then waiting
     * until the dropSelector element appears and then dropping.
     *
     * (If the element on which we should drop is already present,
     *  use the same dropSelector as toSelector)
     *
     * @param driver WebDriver instance
     * @param fromSelector css selector of an element to be dragged
     * @param toSelector css selector of an element to which we should drag
     * @param dropSelector css selector of an element onto which we should drop
     */
    public static void dragAndDropWithCustomBackend(WebDriver driver, String fromSelector, String toSelector, String dropSelector) {
        WebElement source = waitForElementVisible(By.cssSelector(fromSelector), driver);
        Actions driverActions = new Actions(driver);

        driverActions.clickAndHold(source).perform();

        try {
            WebElement target = waitForElementVisible(By.cssSelector(toSelector), driver);
            driverActions.moveToElement(target).perform();

            WebElement drop = waitForElementVisible(By.cssSelector(dropSelector), driver);
            driverActions.moveToElement(drop).perform();
        } finally {
            driverActions.release().perform();
        }
    }

    public static void dragAndDropWithCustomBackend(WebDriver driver, WebElement from, WebElement dropZone) {
        waitForElementVisible(from);
        Actions driverActions = new Actions(driver);
        driverActions.clickAndHold(from).perform();

        try {

            waitForElementVisible(dropZone);
            driverActions.moveToElement(dropZone).perform();
        } finally {
            driverActions.release().perform();
        }
    }

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
