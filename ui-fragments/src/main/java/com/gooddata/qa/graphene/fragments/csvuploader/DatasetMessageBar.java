package com.gooddata.qa.graphene.fragments.csvuploader;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class DatasetMessageBar extends AbstractFragment{
    
    private static final By BY_PROGRESS_MESSAGE_BAR = By.cssSelector(".gd-message.progress");
    private static final By BY_ERROR_MESSAGE_BAR = By.cssSelector(".gd-message.error");
    private static final By BY_SUCCESS_MESSAGE_BAR = By.cssSelector(".gd-message.success");
    
    public WebElement waitForProgressMessageBar() {
        return waitForElementVisible(BY_PROGRESS_MESSAGE_BAR, getRoot());
    }
    
    public WebElement waitForErrorMessageBar() {
        return waitForElementVisible(BY_ERROR_MESSAGE_BAR, getRoot());
    }
    
    public WebElement waitForSuccessMessageBar() {
        return waitForElementVisible(BY_SUCCESS_MESSAGE_BAR, getRoot());
    }
}
