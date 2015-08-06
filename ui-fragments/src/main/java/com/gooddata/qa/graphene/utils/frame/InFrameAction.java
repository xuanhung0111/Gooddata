package com.gooddata.qa.graphene.utils.frame;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;

import com.gooddata.qa.graphene.utils.CheckUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public interface InFrameAction<T> {
    public T doAction();

    public static final class Utils {
        public static final <T> T doActionInFrame(WebElement frame, InFrameAction<T> action, WebDriver browser) {
            try {
                browser.switchTo().frame(CheckUtils.waitForElementVisible(frame));
                return action.doAction();
            } finally {
                browser.switchTo().defaultContent();
            }
        }
    } 
}
