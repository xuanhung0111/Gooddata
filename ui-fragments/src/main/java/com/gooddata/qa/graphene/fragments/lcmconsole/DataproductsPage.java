package com.gooddata.qa.graphene.fragments.lcmconsole;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.utils.ElementUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.lang.String.*;

public class DataproductsPage extends AbstractFragment {

    private static final By DATA_PRODUCTS_ID = By.id("app-admin");
    public static final String URI = "/lcmconsole";

    @FindBy(className = "s-dialog-submit-button")
    private WebElement createDataproductButton;

    public static final DataproductsPage getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(DataproductsPage.class, waitForElementVisible(DATA_PRODUCTS_ID, searchContext));
    }

    public CreateDataproductDialog openCreateDataProductDialog() {
        waitForElementVisible(createDataproductButton).click();
        return CreateDataproductDialog.getInstance(browser);
    }

    public boolean isDataproductPresent(String dataproductId) {
        return ElementUtils.isElementPresent(getCssSelectorDataproduct(dataproductId), browser);
    }

    private By getCssSelectorDataproduct(String dataproductId) {
        return By.cssSelector(format("[href = '#/dataproducts/%s']", dataproductId));
    }
}
