package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class OverlayWrapper extends AbstractFragment {
    private static final String OVERLAY_WRAPPER = "overlay-wrapper";

    @FindBy(className = "s-create-datasource-dialog-item-snowflake")
    private WebElement snowflakeResource;

    @FindBy(className = "s-create-datasource-dialog-item-redshift")
    private WebElement redshiftResource;

    @FindBy(className = "s-create-datasource-dialog-item-bigquery")
    private WebElement bigqueryResource;

    @FindBy(css = ".create-datasource-dialog")
    private WebElement popupResource;

    @FindBy(className = "s-create-datasource-dialog-item-s3-data-source")
    private WebElement s3Resource;

    @FindBy(className = "s-create-datasource-dialog-item-generic-data-source")
    private WebElement genericResource;

    //The username was not recognized and will be skipped    --[2]
    @FindBy(css = ".bubble-negative .content")
    private WebElement contentErrorMessage;

    //The Data Source is already shared with this username. Its settings will be overriden --[2]
    @FindBy(css = ".bubble-warning-tooltip .content")
    private WebElement contentWarningMessage;

    //child : s-dialog-close-button, error-detail: Data Source could not be shared with any of the provided usernames.
    @FindBy(className = "data-source-user-add-error-dialog")
    private WebElement errorAddUserFailPopUp;

    @FindBy(className = "s-delete-datasource-user-dialog")
    private DeleteUserDialog deleteUserDialog;

    @FindBy(className = "data-source-user-edit-dialog")
    private EditUserDialog editUserDialog;

    public static OverlayWrapper getInstance(SearchContext searchContext) {
        List<WebElement> wrapperList = searchContext.findElements(className(OVERLAY_WRAPPER));
        return Graphene.createPageFragment(OverlayWrapper.class, wrapperList.get(1));
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

    public void selectS3Item() {
        waitForElementVisible(popupResource);
        getActions().moveToElement(s3Resource).click().build().perform();
    }

    public void selectGenericItem() {
        waitForElementVisible(popupResource);
        getActions().moveToElement(genericResource).click().build().perform();
    }
}
