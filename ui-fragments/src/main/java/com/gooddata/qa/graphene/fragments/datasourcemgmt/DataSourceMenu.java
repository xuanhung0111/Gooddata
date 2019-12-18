package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class DataSourceMenu extends AbstractFragment {
    private static final By MENU_CLASS = By.className("menu-class");

    @FindBy(className = "add-button")
    private WebElement addButton;

    @FindBy(className = "pop-up-resource")
    private WebElement popupresource;

    @FindBy(className = "resource1")
    private WebElement resource1;

    @FindBy(className = "resource2")
    private WebElement resource2;

    @FindBy(className = "resource3")
    private WebElement resource3;

    public static final DataSourceMenu getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(DataSourceMenu.class, waitForElementVisible(MENU_CLASS, searchContext));
    }

    public SnowflakeDetail selectSnowflakeResource() {
        waitForElementVisible(addButton).click();
        waitForElementVisible(popupresource);
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(resource1).click().build().perform();
        return SnowflakeDetail.getInstance(browser);
    }
}
