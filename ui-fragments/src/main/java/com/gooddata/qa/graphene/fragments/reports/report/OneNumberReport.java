package com.gooddata.qa.graphene.fragments.reports.report;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static org.openqa.selenium.By.className;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

public class OneNumberReport extends AbstractReport {

    @FindBy(className = "number")
    private WebElement number;

    @FindBy(className = "description")
    private WebElement description;

    public String getValue() {
        return number.getText();
    }

    public String getDescription() {
        try {
            return description.getText();
        } catch(NoSuchElementException e) {
            return "";
        }
    }

    public OneNumberReport focus() {
        getRoot().click();
        return this;
    }

    public OneNumberReport changeDescription(String newDescription) {
        new Actions(browser).moveToElement(waitForElementVisible(description)).doubleClick().perform();
        WebElement input = waitForElementVisible(className("ipeEditor"), browser);
        input.clear();
        input.sendKeys(newDescription);
        sleepTightInSeconds(2);
        waitForElementVisible(className("s-ipeSaveButton"), browser).click();
        return this;
    }
}
