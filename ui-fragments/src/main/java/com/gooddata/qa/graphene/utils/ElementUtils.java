package com.gooddata.qa.graphene.utils;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

public final class ElementUtils {

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
}
