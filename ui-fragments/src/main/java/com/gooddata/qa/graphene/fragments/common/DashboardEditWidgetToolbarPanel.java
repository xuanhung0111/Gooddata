package com.gooddata.qa.graphene.fragments.common;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class DashboardEditWidgetToolbarPanel extends AbstractFragment {

    @FindBy(className = "yui3-toolbar-icon-config")
    private WebElement configureButton;

    @FindBy(className = "yui3-toolbar-icon-edit")
    private WebElement editButton;

    @FindBy(className = "yui3-toolbar-icon-remove")
    private WebElement removeButton;

    public static final By LOCATOR = By.className("s-dashboardwidget-toolbar");

    public static void openEditPanelFor(WebElement element, SearchContext searchContext) {
        waitForElementVisible(getInstanceFor(element, searchContext).editButton).click();
    }

    public static void removeWidget(WebElement element, SearchContext searchContext) {
        waitForElementVisible(getInstanceFor(element, searchContext).removeButton).click();
    }

    public static void openConfigurationPanelFor(WebElement element, SearchContext searchContext) {
        waitForElementVisible(getInstanceFor(element, searchContext).configureButton).click();
    }

    private static DashboardEditWidgetToolbarPanel getInstanceFor(WebElement element,
            SearchContext searchContext) {
        waitForElementVisible(element).click();
        return Graphene.createPageFragment(DashboardEditWidgetToolbarPanel.class,
                waitForElementVisible(LOCATOR, searchContext));
    }
}
