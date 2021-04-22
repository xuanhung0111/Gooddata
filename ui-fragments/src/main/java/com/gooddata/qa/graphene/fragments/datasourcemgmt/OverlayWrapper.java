package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
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

    @FindBy(className = "s-create-datasource-dialog-item-postgres")
    private WebElement postgreResource;

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

    @FindBy(css = ".bubble-negative .content")
    private WebElement contentErrorMessage;

    @FindBy(css = ".bubble-warning-tooltip .content")
    private WebElement contentWarningMessage;

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

    public static OverlayWrapper getInstanceByIndex(SearchContext searchContext, int index) {
        List<WebElement> wrapperList = searchContext.findElements(className(OVERLAY_WRAPPER));
        return Graphene.createPageFragment(
                OverlayWrapper.class, wrapperList.get(index));
    }

    public void selectPostgreItem() {
        waitForElementVisible(popupResource);
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(postgreResource).click().build().perform();
    }

    public void selectSnowflakeItem() {
        waitForElementVisible(popupResource);
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(snowflakeResource).click().build().perform();
    }

    public String getTextErrorShareUser() {
        waitForElementVisible(errorAddUserFailPopUp);
        return errorAddUserFailPopUp.getText();
    }

    public void actionOnErrorShareUserDialog() {
        waitForElementVisible(errorAddUserFailPopUp);
        WebElement closeButton = errorAddUserFailPopUp.findElement(By.className("s-dialog-close-button"));
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(closeButton).click().build().perform();
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
        ImportBigQueryDialog.getInstance(browser).clickConnectManualLink();
    }

    public void selectS3Item() {
        waitForElementVisible(popupResource);
        getActions().moveToElement(s3Resource).click().build().perform();
    }

    public void selectGenericItem() {
        waitForElementVisible(popupResource);
        getActions().moveToElement(genericResource).click().build().perform();
    }

    public String getErrorAddUserMessage() {
        waitForElementVisible(contentErrorMessage);
        return contentErrorMessage.getText();
    }

    public String getWarningAddUserMessage() {
        waitForElementVisible(contentWarningMessage);
        return contentWarningMessage.getText();
    }
}
