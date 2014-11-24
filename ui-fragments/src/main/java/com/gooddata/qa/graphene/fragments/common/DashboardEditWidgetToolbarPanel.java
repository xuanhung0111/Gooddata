package com.gooddata.qa.graphene.fragments.common;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class DashboardEditWidgetToolbarPanel extends AbstractFragment {

    @FindBy(xpath = "//div[contains(@class,'yui3-toolbar-icon-config')]")
    private WebElement configureButton;

    @FindBy(xpath = "//div[contains(@class,'yui3-toolbar-icon-edit')]")
    private WebElement editButton;

    @FindBy(xpath = "//div[contains(@class,'yui3-toolbar-icon-remove')]")
    private WebElement removeButton;

    public static final By LOCATOR = By.cssSelector(".s-dashboardwidget-toolbar");

    public void openEditPanel() {
        waitForElementVisible(editButton).click();
    }

    public void removeWidget() {
        waitForElementVisible(removeButton).click();
    }

    public void openConfigurationPanel() {
        waitForElementVisible(configureButton).click();
    }
}
