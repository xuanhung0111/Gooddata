package com.gooddata.qa.graphene.utils;

import com.gooddata.qa.browser.BrowserUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.stream.Collectors.toList;

public final class ElementUtils {

    public static final By BY_BUBBLE_CONTENT = By.cssSelector(".bubble-content .content");

    private ElementUtils() {
    }

    /**
     * Check if element is currently present in DOM
     * @see <a href="http://stackoverflow.com/questions/7991522/selenium-webdriver-test-if-element-is-present">SO: selenium-webdriver-test-if-element-is-present</a>
     * @param locatorKey By element for location
     * @param context context to search
     * @return
     */
    public static boolean isElementPresent(By locatorKey, SearchContext context) {
        return context.findElements(locatorKey).size() > 0;
    }

    public static boolean isElementVisible(By locatorKey, SearchContext context) {
        if (!isElementPresent(locatorKey, context)) return false;

        return context.findElement(locatorKey).isDisplayed();
    }

    public static boolean isElementVisible(WebElement element) {
        return element.isDisplayed();
    }

    public static void scrollElementIntoView(WebElement element, WebDriver browser) {
        BrowserUtils.runScript(browser, "arguments[0].scrollIntoView(true);", element);
    }

    /**
     * Get texts of all elements matching given By selector under specified context
     * @param selector selector to match items
     * @param context search context
     * @return array of strings with element texts
     */
    public static List<String> getElementTexts(By selector, SearchContext context) {
        return getElementTexts(context.findElements(selector));
    }

    public static List<String> getElementTexts(Collection<WebElement> elements) {
        return elements.stream()
            .map(WebElement::getText)
            .collect(toList());
    }

    public static List<String> getElementTexts(Collection<WebElement> elements, Function<WebElement, WebElement> func) {
        return elements.stream()
            .map(func::apply)
            .map(WebElement::getText)
            .collect(toList());
    }

    public static String getTooltipFromElement(By locator, WebDriver browser) {
        return getTooltipFromElement(waitForElementVisible(locator, browser), browser);
    }

    public static String getTooltipFromElement(WebElement element, WebDriver browser) {
        makeSureNoPopupVisible();
        // Selenium 3.8.1 + gecko 0.20.0 has problem with action #moveToElement when working inside docker container.
        // The element hovered by this action will not trigger the event so it's meaningless when need to check a
        // tooltip show from that. This additional #moveByOffset is a work around to make it work properly.
        new Actions(browser).moveToElement(element).moveByOffset(1, 1).perform();

        return getBubbleMessage(browser);
    }

    public static String getBubbleMessage(WebDriver browser) {
        return browser.findElements(BY_BUBBLE_CONTENT).stream()
                .filter(element -> isElementVisible(element))
                .findFirst()
                .get()
                .getText();
    }

    public static void clickElementByVisibleLocator(SearchContext searchContext, By... bySelectors) {
        Function<WebDriver, Boolean> visibilityOfElement = browser ->
                Stream.of(bySelectors).anyMatch(by -> isElementVisible(by, searchContext));
        Graphene.waitGui().until(visibilityOfElement);

        Stream.of(bySelectors)
                .filter(by -> isElementVisible(by, searchContext))
                .map(by -> waitForElementVisible(by, searchContext))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No visible element found"))
                .click();
    }

    public static void makeSureNoPopupVisible() {
        makeSureNoPopupVisible(BY_BUBBLE_CONTENT);
    }

    public static void makeSureNoPopupVisible(By popupElement) {
        WebDriver browser = BrowserUtils.getBrowserContext();

        // Move to top left corner of HTML body to make sure no popup is displayed
        moveToElementActions(browser.findElement(By.tagName("body")), 0, 0).perform();

        Function<WebDriver, Boolean> isDismissed = context -> !isElementVisible(popupElement, context);
        Graphene.waitGui().until(isDismissed);
    }
    
    // helper method to cover different implementation in webdrivers
    // - geckodriver follows W3C and counts offset from element's center
    // - chromedriver & others follow Selenium API and counts offset from top-left corner 
    public static Actions moveToElementActions(WebElement target, int xOffset, int yOffset) {
        WebDriver browser = BrowserUtils.getBrowserContext();
        String browserName = ((RemoteWebDriver) browser).getCapabilities().getBrowserName().toLowerCase();
        Actions actions = new Actions(browser);
        
        // 
        if ("firefox".equals(browserName)) {
            // TODO: possible rounding issue, how exactly is center of element counted in Geckodriver?
            // If width is 7, is clicking point 3 (assumed) or 4?
            return actions.moveToElement(target,
                    -target.getSize().width / 2 + xOffset,
                    -target.getSize().height / 2 + yOffset);
        } else {
            return actions.moveToElement(target, xOffset, yOffset);
        }
    }

    // Selenium action clear() not work properly with some kind of inputs (calendar picker input, ...).
    // Use this method as a replacement.
    public static void clear(WebElement element) {
        WebDriver browser = BrowserUtils.getBrowserContext();
        new Actions(browser).click(element)
                .keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL).sendKeys(Keys.DELETE)
                .perform();
    }
}
