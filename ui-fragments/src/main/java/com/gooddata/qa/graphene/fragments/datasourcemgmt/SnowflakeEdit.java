package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class SnowflakeEdit extends ConnectionEdit {
    private static final By SNOWFLAKE_DETAIL_CLASS = By.className("can-create-new-datasource");
    public static final SnowflakeEdit getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(SnowflakeEdit.class, waitForElementVisible(SNOWFLAKE_DETAIL_CLASS, searchContext));
    }
    @FindBy(className = "url")
    private WebElement url;

    @FindBy(className = "warehouse")
    private WebElement warehouse;

    @FindBy(className = "username")
    private WebElement username;

    @FindBy(className = "password")
    private WebElement password;

    @FindBy(className = "database")
    private WebElement database;

    public SnowflakeEdit addUrl( String value) {
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(url).click().sendKeys(value).build().perform();
        return this;
    }

    public SnowflakeEdit addWarehouse(String value) {
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(warehouse).click().sendKeys(value).build().perform();
        return this;
    }

    public SnowflakeEdit addUsername(String value) {
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(username).click().sendKeys(value).build().perform();
        return this;
    }

    public SnowflakeEdit addPassword(String value){
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(password).click().sendKeys(value).build().perform();
        return this;
    }


    public SnowflakeEdit addDatabase(String value) {
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(database).click().sendKeys(value).build().perform();
        return this;
    }


    public  SnowflakeEdit addSnowflakeInfo(String name, String url, String warehouse, String username, String password,
                                             String database, String prefix, String schema) {
        addName(name);
        addUrl(url);
        addUsername(username);
        addPassword(password);
        addDatabase(database);
        addWarehouse(warehouse);
        addPrefix(prefix);
        addSchema(schema);
        return this;
    }
}
