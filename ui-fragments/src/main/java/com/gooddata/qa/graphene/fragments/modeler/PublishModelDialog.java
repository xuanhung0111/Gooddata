package com.gooddata.qa.graphene.fragments.modeler;


import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;

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

    @FindBy(css = ".icon-publishing")
    private WebElement publishingIcon;

    @FindBy(css = ".error-boundary .error-detail")
    private WebElement textResultError;

    @FindBy(css = ".error-boundary .nav")
    private WebElement btnCancelError;

    @FindBy(css = ".gd-dialog-close .s-dialog-close-button")
    private WebElement btnCancelSuccess;

    @FindBy(className = "sub-error")
    private WebElement errorPublish;

    @FindBy(css = ".gd-dialog-content .input-checkbox")
    private WebElement csvCheckbox;

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

    public void overwriteDataSwitchToEditMode() {
        overwriteData();
        waitForFragmentNotVisible(this);
        ToolBar.getInstance(browser).clickEditBtn();
    }

    public void overwriteInTableView() {
        overwriteData();
        waitForFragmentNotVisible(this);
        ToolBar.getInstanceInTableView(browser, 1).clickEditBtn();
    }

    public void overwriteDataAcceptError() {
        btnOverwrite.click();
        btnPublish.click();
    }

    public void clickCancel() {
        btnCancel.click();
    }

    public String getTextError() {
        waitForElementNotVisible(publishingIcon);
        waitForElementVisible(textResultError);
        return textResultError.getText();
    }

    public String getErrorPublish() {
        return waitForElementVisible(errorPublish).getText();
    }

    public boolean isPublishModelDialogDisplay() {
        return isElementVisible(SIDEBAR, getRoot());
    }

    public void clickButtonCancelErrorPopUp() {
        btnCancelError.click();
    }

    public boolean isUploadCsvChecked () {
        return waitForElementPresent(csvCheckbox).isSelected();
    }
}
