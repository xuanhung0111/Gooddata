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
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;

public class PublishModelDialog extends AbstractFragment {
    private static final By SIDEBAR = By.className("publish-model");

    @FindBy(css = ".gd-dialog-footer .s-publish")
    private WebElement btnPublish;

    @FindBy(css = ".gd-dialog-footer .s-force_publish")
    private WebElement btnForcePublish;

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

    @FindBy(className = "publish-option-dropdown")
    private WebElement publishOptionBtn;

    public static final PublishModelDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(PublishModelDialog.class, waitForElementVisible(SIDEBAR, searchContext));
    }

    public String getTextOption() {
        return publishOptionBtn.getText();
    }

    public void chooseOption(String option) {
        publishOptionBtn.click();
        WebElement dropDownlist = waitForElementVisible(By.className("dropdown-list"), browser);
        dropDownlist.findElement(By.className(option)).click();
        waitForElementNotVisible(dropDownlist);
    }

    public boolean isPreserveDataDisable() {
        publishOptionBtn.click();
        WebElement dropDownlist = waitForElementVisible(By.className("dropdown-list"), browser);
        return isElementPresent(By.cssSelector(".s-publish_optiondotpreserve.is-disabled"), dropDownlist);
    }

    public void choosePreserveData() {
        chooseOption("s-publish_optiondotpreserve");
    }

    public void chooseDropData() {
        chooseOption("s-publish_optiondotdrop");
    }

    public void publishModel() {
        if(isElementPresent(By.cssSelector(".gd-dialog-footer .s-publish"), browser)) {
            btnPublish.click();
        } else {
            btnForcePublish.click();
        }
    }

    public void publishSwitchToEditMode() {
        publishModel();
        waitForFragmentNotVisible(this);
        ToolBar.getInstance(browser).clickEditBtn();
    }

    public void publishInTableView() {
        publishModel();
        waitForFragmentNotVisible(this);
        ToolBar.getInstanceInTableView(browser, 1).clickEditBtn();
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
