package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class SnowflakeDetail extends ConnectionDetail {
    private static final By SNOWFLAKE_DETAIL_CLASS = By.className("can-create-new-datasource");
    public static final SnowflakeDetail getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(SnowflakeDetail.class, waitForElementVisible(SNOWFLAKE_DETAIL_CLASS, searchContext));
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
}
