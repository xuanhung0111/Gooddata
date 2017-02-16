package com.gooddata.qa.graphene.fragments.dashboards.widget;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

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

    @FindBy(className = "yui3-toolbar-icon-addLink")
    private WebElement addLinkButton;

    public static final By LOCATOR = By.className("s-dashboardwidget-toolbar");

    public static boolean isVisible(SearchContext searchContext) {
        return isElementVisible(LOCATOR, searchContext);
    }

    public static void openEditPanelFor(WebElement element, SearchContext searchContext) {
        waitForElementVisible(getInstanceFor(element, searchContext).editButton).click();
    }

    public static void removeWidget(WebElement element, SearchContext searchContext) {
        waitForElementVisible(getInstanceFor(element, searchContext).removeButton).click();
    }

    public static void openConfigurationPanelFor(WebElement element, SearchContext searchContext) {
        waitForElementVisible(getInstanceFor(element, searchContext).configureButton).click();
    }

    public static void openAddLinkPanelFor(WebElement element, SearchContext searchContext) {
        waitForElementVisible(getInstanceFor(element, searchContext).addLinkButton).click();
    }

    private static DashboardEditWidgetToolbarPanel getInstanceFor(WebElement element,
            SearchContext searchContext) {
        waitForElementVisible(element).click();
        return Graphene.createPageFragment(DashboardEditWidgetToolbarPanel.class,
                waitForElementVisible(LOCATOR, searchContext));
    }
}
