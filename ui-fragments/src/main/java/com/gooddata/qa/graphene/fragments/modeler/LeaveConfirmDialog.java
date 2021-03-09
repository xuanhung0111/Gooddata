package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;

public class LeaveConfirmDialog extends AbstractFragment {

    private static By LEAVE_CONFIRM_DIALOG = By.className("gd-dialog");

    @FindBy(className = "gd-dialog-header")
    private WebElement dialogHeader;

    @FindBy(className = "gd-dialog-content")
    private WebElement dialogContent;

    @FindBy(className = "s-dialog-close-button")
    private WebElement closeButton;

    @FindBy(className = "s-leave_anyway")
    private WebElement leaveButton;

    @FindBy(className = "s-publish")
    private WebElement publishButton;

    public static LeaveConfirmDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                LeaveConfirmDialog.class, waitForElementVisible(LEAVE_CONFIRM_DIALOG, searchContext));
    }

    public boolean isLeaveConfirmDialogDisplay() {
        return isElementVisible(LEAVE_CONFIRM_DIALOG, browser);
    }

    public String getDialogHeader() {
        return waitForElementVisible(dialogHeader).getText();
    }

    public String getDialogContent() {
        return waitForElementVisible(dialogContent).getText();
    }

    public void clickCloseDialog() {
        waitForElementVisible(closeButton).click();
        waitForFragmentNotVisible(this);
    }

    public void clickLeaveAnywayButton() {
        waitForElementVisible(leaveButton).click();
        waitForFragmentNotVisible(this);
    }

    public void clickPublishButton() {
        waitForElementVisible(publishButton).click();
        waitLoadingPublish();
    }

    public void waitForLeaveConfirmDialogHidden() {
        waitForFragmentNotVisible(this);
    }

    private void waitLoadingPublish() {
        if(isElementPresent(By.cssSelector(".gdc-ldm-waiting-dialog .waiting-dialog-content"), browser)) {
            waitForFragmentNotVisible(WaitingDialog.getInstance(browser));
        }
    }
}
