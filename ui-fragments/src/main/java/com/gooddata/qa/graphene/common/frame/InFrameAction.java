package com.gooddata.qa.graphene.common.frame;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public interface InFrameAction<T> {
    public T doAction();

    public static final class Utils {
        public static final <T> T doActionInFrame(By byFrame, InFrameAction<T> action, WebDriver browser) {
            try {
                browser.switchTo().frame(waitForElementVisible(byFrame, browser));
                return action.doAction();
            } finally {
                browser.switchTo().defaultContent();
            }
        }

        public static final <T> T doActionInFrame(WebElement frame, InFrameAction<T> action, WebDriver browser) {
            try {
                browser.switchTo().frame(waitForElementVisible(frame));
                return action.doAction();
            } finally {
                browser.switchTo().defaultContent();
            }
        }
    } 
}
