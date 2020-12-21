package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class OverlayWrapper extends AbstractFragment {
    private static final String OVERLAY_WRAPPER = "target-bl";

    @FindBy(className = "s-create-datasource-dialog-item-snowflake")
    private WebElement snowflakeResource;

    @FindBy(className = "s-create-datasource-dialog-item-redshift")
    private WebElement redshiftResource;

    @FindBy(className = "s-create-datasource-dialog-item-bigquery")
    private WebElement bigqueryResource;

    @FindBy(css = ".create-datasource-dialog")
    private WebElement popupResource;

    public static OverlayWrapper getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                OverlayWrapper.class, waitForElementVisible(className(OVERLAY_WRAPPER), searchContext));
    }

    public void selectSnowflakeItem() {
        waitForElementVisible(popupResource);
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(snowflakeResource).click().build().perform();
    }

    public void selectRedshiftItem() {
        waitForElementVisible(popupResource);
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(redshiftResource).click().build().perform();
    }

    public void selectBigqueryItem() {
        waitForElementVisible(popupResource);
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(bigqueryResource).click().build().perform();
    }
}
