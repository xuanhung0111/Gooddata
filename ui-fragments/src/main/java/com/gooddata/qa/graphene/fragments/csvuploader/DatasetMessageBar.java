package com.gooddata.qa.graphene.fragments.csvuploader;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class DatasetMessageBar extends AbstractFragment{

    private static final By BY_ERROR_MESSAGE_BAR = cssSelector(".gd-message.error");
    private static final By BY_SUCCESS_MESSAGE_BAR = cssSelector(".gd-message.success");

    public static DatasetMessageBar getInstance(SearchContext context) {
        return Graphene.createPageFragment(DatasetMessageBar.class,
                waitForElementVisible(className("gd-messages"), context));
    }

    public WebElement waitForErrorMessageBar() {
        return waitForElementVisible(BY_ERROR_MESSAGE_BAR, getRoot());
    }

    public WebElement waitForSuccessMessageBar() {
        return waitForElementVisible(BY_SUCCESS_MESSAGE_BAR, getRoot());
    }
}
