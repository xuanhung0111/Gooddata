package com.gooddata.qa.graphene.utils;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.lang.String.format;
import static org.testng.Assert.fail;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;

public final class CheckUtils {

    private static final String STATUS_BAR_SELECTOR = "div#status > div.box-%s div.leftContainer";
    private static final By BY_GREEN_BAR = By.cssSelector(format(STATUS_BAR_SELECTOR, "success"));
    private static final By BY_REPORT_ERROR = By.cssSelector("div.error-container");

    public static final By BY_RED_BAR = By.cssSelector(format(STATUS_BAR_SELECTOR, "error"));
    public static final By BY_RED_BAR_WARNING = By.cssSelector(format(STATUS_BAR_SELECTOR, "warning"));
    public static final By BY_BLUE_BAR = By.cssSelector(format(STATUS_BAR_SELECTOR, "info"));
    public static final By BY_DISMISS_BUTTON = By.cssSelector("div#status .s-btn-dismiss");

    private CheckUtils() {
    }

    public static void checkRedBar(SearchContext searchContext) {
        if (searchContext.findElements(BY_RED_BAR).size() != 0) {
            fail("RED BAR APPEARED - " + searchContext.findElement(BY_RED_BAR).getText());
        }
        if (searchContext.findElements(BY_RED_BAR_WARNING).size() != 0) {
            fail("RED BAR APPEARED - " + searchContext.findElement(BY_RED_BAR_WARNING).getText());
        }
        //this kind of error appeared for the first time in geo chart
        if (searchContext.findElements(BY_REPORT_ERROR).size() != 0 && searchContext.findElement(BY_REPORT_ERROR).isDisplayed()) {
            fail("Report error APPEARED - " + searchContext.findElement(BY_REPORT_ERROR).getText());
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
}
