package com.gooddata.qa.graphene.fragments.lcmconsole;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.utils.ElementUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class DataproductsPage extends AbstractFragment {

    public static String URI = "/lcmconsole";

    @FindBy(className = "s-dialog-submit-button")
    private WebElement createDataproductButton;

    public void clickCreateDataproductButton() {
        waitForElementVisible(createDataproductButton).click();
    }

    public boolean isDataproductPresent(String name) {
        return ElementUtils.isElementPresent(By.cssSelector("[href = '#/dataproducts/" + name + "' ]"), browser);
    }
}
