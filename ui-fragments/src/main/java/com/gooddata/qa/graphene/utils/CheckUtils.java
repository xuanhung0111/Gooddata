package com.gooddata.qa.graphene.utils;

import static com.gooddata.qa.graphene.utils.WaitUtils.*;
import static java.lang.String.format;
import static org.testng.Assert.fail;

import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public final class CheckUtils {

    private static final Logger log = Logger.getLogger(CheckUtils.class.getName());

    private static final String STATUS_BAR_SELECTOR = "div#status > div.box-%s div.leftContainer";
    private static final By BY_GREEN_BAR = By.cssSelector(format(STATUS_BAR_SELECTOR, "success"));
    private static final By BY_REPORT_ERROR = By.cssSelector("div.error-container");

    public static final By BY_RED_BAR = By.cssSelector(format(STATUS_BAR_SELECTOR, "error"));
    public static final By BY_RED_BAR_WARNING = By.cssSelector(format(STATUS_BAR_SELECTOR, "warning"));
    public static final By BY_BLUE_BAR = By.cssSelector(format(STATUS_BAR_SELECTOR, "info"));
    public static final By BY_DISMISS_BUTTON = By.cssSelector("div#status .s-btn-dismiss");
    public static final By BY_ERROR_ID = By.cssSelector("div#status .c-error-errorId");
    public static final By BY_ERROR_MESSAGE = By.cssSelector("div#status .c-error-message");

    public static final By BY_INDIGO_MESSAGE = By.cssSelector(".gd-message:not(.progress)");
    public static final By BY_INDIGO_DISMISS_BUTTON = By.cssSelector(".gd-message-dismiss");

    private CheckUtils() {
    }

    public static void checkLocalization(WebDriver browser) {
        if (browser.getPageSource().contains("Missing translation")) {
            fail("MISSING TRANSLATION APPEARED");
        }
    }

    public static void checkRedBar(SearchContext searchContext) {
        if (ElementUtils.isElementPresent(BY_RED_BAR,searchContext)) {
            logRedBarMessageInfo(searchContext);
            fail("RED BAR APPEARED - " + searchContext.findElement(BY_RED_BAR).getText());
        }
        if (ElementUtils.isElementPresent(BY_RED_BAR_WARNING,searchContext)) {
            fail("RED BAR APPEARED - " + searchContext.findElement(BY_RED_BAR_WARNING).getText());
        }
        //this kind of error appeared for the first time in geo chart
        if (ElementUtils.isElementPresent(BY_REPORT_ERROR,searchContext) && searchContext.findElement(BY_REPORT_ERROR).isDisplayed()) {
            fail("Report error APPEARED - " + searchContext.findElement(BY_REPORT_ERROR).getText());
        }
    }
    
    public static void logRedBarMessageInfo(SearchContext searchContext){
        if(ElementUtils.isElementPresent(BY_ERROR_ID,searchContext)) {
            log.info("ErrorId APPEARED - " + searchContext.findElement(BY_ERROR_ID).getAttribute("value"));
        }

        if(ElementUtils.isElementPresent(BY_ERROR_MESSAGE,searchContext)) {
            log.info("STATUS BAR INFO MESSAGE APPEARED - " + searchContext.findElement(BY_ERROR_MESSAGE).getAttribute("value"));
        }
    }

    public static void checkGreenBar(SearchContext searchContext) {
        waitForElementVisible(BY_GREEN_BAR, searchContext);
    }

    public static void checkBlueBar(SearchContext searchContext) {
        waitForElementVisible(BY_BLUE_BAR, searchContext);
    }

    public static void checkGreenBar(SearchContext searchContext, String desiredMessage) {
        String greenBarMessage = waitForElementVisible(BY_GREEN_BAR, searchContext).getText();

        if (desiredMessage.length() != 0 && !greenBarMessage.equals(desiredMessage)) {
            fail("WRONG GREEN BAR MESSAGE - is: " + greenBarMessage + " expected: " + desiredMessage);
        }
    }

    public static void dismissSuccessMessage(WebDriver browser) {
        int timeoutInSeconds = 10;

        WebElement message = waitForElementVisible(BY_INDIGO_MESSAGE, browser, timeoutInSeconds);
        String nodeClass = message.getAttribute("class");

        if (nodeClass.contains("success")) {
            waitForElementVisible(BY_INDIGO_DISMISS_BUTTON, message).click();
            waitForElementNotPresent(message);
        } else if (nodeClass.contains("error")) {
            throw new RuntimeException("Indigo error message found");
        } else {
            throw new RuntimeException("Unknown indigo message found");
        }
    }
}
