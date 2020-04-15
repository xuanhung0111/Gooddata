package com.gooddata.qa.graphene.fragments.modeler;


import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;

public class PublishModelDialog extends AbstractFragment {
    private static final By SIDEBAR = By.className("publish-model");

    @FindBy(css = ".gd-dialog-content .preserveData")
    private WebElement btnPreserveData;

    @FindBy(css = ".gd-dialog-content .overwrite")
    private WebElement btnOverwrite;

    @FindBy(css = ".gd-dialog-footer .s-publish")
    private WebElement btnPublish;

    @FindBy(css = ".gd-dialog-footer .s-cancel-file")
    private WebElement btnCancel;

    @FindBy(css = ".gd-dialog-content .result-success-message")
    private WebElement textResultSuccess;

    @FindBy(css = ".icon-publishing")
    private WebElement publishingIcon;

    @FindBy(css = ".error-boundary .error-detail")
    private WebElement textResultError;

    @FindBy(css = ".error-boundary .nav")
    private WebElement btnCancelError;

    @FindBy(css = ".gd-dialog-close .s-dialog-close-button")
    private WebElement btnCancelSuccess;

    public static final PublishModelDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(PublishModelDialog.class, waitForElementVisible(SIDEBAR, searchContext));
    }

    public void preserveData() {
        btnPreserveData.click();
        btnPublish.click();
    }

    public void overwriteData() {
        btnOverwrite.click();
        btnPublish.click();
    }

    public void clickCancel() {
        btnCancel.click();
    }

    public String getTextSuccess() {
        waitForElementNotVisible(publishingIcon);
        waitForElementVisible(textResultSuccess);
        return textResultSuccess.getText();
    }

    public String getTextError() {
        waitForElementNotVisible(publishingIcon);
        waitForElementVisible(textResultError);
        return textResultError.getText();
    }

    public void clickButtonCancelErrorPopUp() {
        btnCancelError.click();
    }

    public void clickButtonCancelSuccessPopUp() {
        btnCancelSuccess.click();
    }
}
