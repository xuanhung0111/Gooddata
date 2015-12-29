package com.gooddata.qa.graphene.fragments.reports.filter;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.openqa.selenium.By;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class SubFilter extends AbstractFragment {

    private static final By DELETE_BUTTON_LOCATOR = By.cssSelector(".simpleBtnOrange.s-btn-delete");

    @FindBy(css = ".operator select")
    private Select operatorSelect;

    public void changeOperator(String operator) {
        waitForElementVisible(operatorSelect).selectByVisibleText(operator);
    }

    public void delete() {
        new Actions(browser).moveToElement(this.getRoot()).perform();
        waitForElementVisible(getRoot().findElement(DELETE_BUTTON_LOCATOR)).click();
    }
}
