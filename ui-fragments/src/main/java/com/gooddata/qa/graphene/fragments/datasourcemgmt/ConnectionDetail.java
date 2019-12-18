package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class ConnectionDetail extends AbstractFragment {
    private static final By CONNECTION_DETAIL_CLASS = By.className("can-create-new-datasource");

    @FindBy(className = "title")
    private WebElement name;

    @FindBy(className = "edit-button")
    private WebElement editButton;

    @FindBy(className = "deleteButton")
    private WebElement deleteButton;

    @FindBy(className = "outputStageButton")
    private WebElement outputstagebutton;

    @FindBy(className = "modelButton")
    private WebElement modelbutton;

    @FindBy(className = "dataLoadButton")
    private WebElement dataloadbutton;

    @FindBy(className = "schema")
    private WebElement schema ;

    @FindBy(className = "prefix")
    private WebElement prefix ;

    public void addName( String value) {
        waitForElementVisible(name).sendKeys(value);
    }

    public void addSchema(String value) {
        waitForElementVisible(schema).sendKeys(value);
    }

    public void addPrefix(String value) {
        waitForElementVisible(prefix).sendKeys(value);
    }

}
