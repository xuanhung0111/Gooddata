package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.scrollElementIntoView;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class RowHeader extends AbstractFragment {

    @FindBy(css = ROW_TITLE_CLASS_NAME_EDIT_MODE)
    private WebElement rowTitle;

    @FindBy(css = ROW_TITLE_DESCRIPTION_NAME_EDIT_MODE)
    private WebElement rowDescription;

    private static final String ROW_TITLE_CLASS_NAME_EDIT_MODE = ".s-fluid-layout-row-title-input";
    private static final String ROW_TITLE_DESCRIPTION_NAME_EDIT_MODE = ".s-fluid-layout-row-description-input";
    private static final String ROW_TITLE_CLASS_NAME = ".s-fluid-layout-row-title";
    private static final String ROW_TITLE_DESCRIPTION_NAME = ".s-fluid-layout-row-description";
    private static final String MORE_BUTTON = ".more-link .underline";

    public String getHeaderRowInEditMode() {
        return getRoot().findElement(By.cssSelector(ROW_TITLE_CLASS_NAME_EDIT_MODE)).getText();
    }

    public String getDescriptionRowInEditMode() {
        return getRoot().findElement(By.cssSelector(ROW_TITLE_DESCRIPTION_NAME_EDIT_MODE)).getText();
    }

    public String getHeaderRowInViewMode() {
        return getRoot().findElement(By.cssSelector(ROW_TITLE_CLASS_NAME)).getText();
    }

    public String getDescriptionRowInViewMode() {
        return getRoot().findElement(By.cssSelector(ROW_TITLE_DESCRIPTION_NAME)).getText();
    }

    public void clickOnTitle() {
        //TODO: use click function get error element is not clickable, so use Action click as work around
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(rowTitle).click().build().perform();
    }

    public void clickOnDescription() {
        //TODO: use click function get error element is not clickable, so use Action click as work around
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(rowDescription).click().build().perform();
    }

    public RowHeader changeDashboardRowTitle(String newTitle, boolean scroll) {
        waitForElementVisible(rowTitle);
        sleepTightInSeconds(1);
        if(scroll == true) scrollElementIntoView(rowTitle, browser);
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(rowTitle).click().sendKeys(newTitle, Keys.ENTER).build().perform();
        return this;
    }

    public RowHeader changeDashboardRowDescription(String newTitle, boolean scroll) {
        waitForElementVisible(rowDescription);
        sleepTightInSeconds(1);
        Actions driverActions = new Actions(browser);
        if(scroll == true) scrollElementIntoView(rowDescription, browser);
        driverActions.moveToElement(rowDescription).click().sendKeys(newTitle, Keys.ENTER).build().perform();
        return this;
    }

    public WebElement getMoreDescription() {
        return waitForElementVisible(By.cssSelector(MORE_BUTTON), getRoot());
    }

    public WebElement getRowDescription() {
        return rowDescription;
    }

    public WebElement getRowTitle() {
        return rowTitle;
    }

    public boolean hasHeader() {
        return isElementVisible(By.className(ROW_TITLE_CLASS_NAME),browser);
    }

    public boolean hasDescription() {
        return isElementVisible(By.className(ROW_TITLE_DESCRIPTION_NAME),browser);
    }
}
