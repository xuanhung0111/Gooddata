package com.gooddata.qa.graphene.fragments.manage;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import static org.testng.Assert.assertEquals;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.CssUtils;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;

public class AttributeDetailPage extends AbstractFragment {

    @FindBy(css = "div.objectHeader table tbody tr td h2")
    private WebElement attributeName;

    @FindBy(css = ".s-attributeLabels td.type-cell")
    private WebElement labelType;

    @FindBy(css = "div.labels-editor button.s-labelEditButton")
    private WebElement labelEditButton;

    @FindBy(css = "select.s-labelTypeSelect")
    private Select labelTypeSelect;

    @FindBy(css = "input.s-titleInput")
    private WebElement labelTitleInput;

    @FindBy(css = "button.s-labelSaveButton")
    private WebElement labelSaveButton;

    @FindBy(css = "button.s-btn-clear")
    private WebElement clearExternalPageButton;

    @FindBy(css = ".specialButton .s-btn-delete")
    private WebElement deleteButton;

    @FindBy(css = ".specialAction .info")
    private WebElement deleteButtonInfo;

    @FindBy(css = "button.pickAttribute")
    private WebElement selectDrillAttributeButton;

    private static final By BY_EXTERNAL_PAGE_LINK = By.cssSelector("button.s-btn-external_page");

    // ************** Dialog rename attribute ************** //
    // can not declare these attributes as web element 
    // because they do not belong to AttributeDetailPage fragment 
    private static final By ATTRIBUTE_RENAME_INPUT_LOCATOR = By.cssSelector(".c-ipeEditorIn input");
    private static final By OK_BUTTON_LOCATOR              = By.cssSelector(".c-ipeEditorControls button");
    // ********************************************************

    public String getAttributeName() {
        return waitForElementVisible(attributeName).getText();
    }

    public String getAttributeLabelType() {
        return waitForElementVisible(labelType).getText();
    }

    public boolean isHyperLink() {
        return browser.findElements(BY_EXTERNAL_PAGE_LINK).size() > 0;
    }

    public void setDrillToExternalPage() {
        waitForElementVisible(BY_EXTERNAL_PAGE_LINK, browser).click();
        waitForElementVisible(clearExternalPageButton);
    }

    public void selectLabelType(String labelType) throws InterruptedException {
        waitForElementVisible(labelEditButton).click();
        waitForElementVisible(labelTypeSelect);
        Thread.sleep(2000);
        labelTypeSelect.selectByVisibleText(labelType);
        Thread.sleep(2000);
        waitForElementVisible(labelSaveButton).click();
        waitForElementNotVisible(labelTitleInput);
    }

    public void renameAttribute(String newName) {
        waitForElementVisible(attributeName).click();
        WebElement input = waitForElementVisible(ATTRIBUTE_RENAME_INPUT_LOCATOR, browser);
        input.clear();
        input.sendKeys(newName);
        waitForElementVisible(OK_BUTTON_LOCATOR, browser).click();
        waitForElementVisible(attributeName);
        assertEquals(getAttributeName(), newName, "new attribute name is not updated!");
    }

    public boolean isDrillToExternalPage() {
        return waitForElementPresent(By.cssSelector(".link.option"), browser).getAttribute("style").contains("display: inline");
    }

    public void clearDrillingSetting() {
        waitForElementVisible(clearExternalPageButton).click();
        waitForElementNotVisible(clearExternalPageButton);
    }

    public void setDrillToAttribute(String attribute) {
        waitForElementPresent(clearExternalPageButton);
        if (!clearExternalPageButton.getAttribute("class").contains("gdc-hidden")) {
            clearDrillingSetting();
        }
        waitForElementVisible(selectDrillAttributeButton).click();
        SelectItemPopupPanel popup = Graphene.createPageFragment(SelectItemPopupPanel.class,
                waitForElementVisible(SelectItemPopupPanel.LOCATOR, browser));
        popup.searchAndSelectItem(attribute);
        waitForElementNotVisible(popup.getRoot());
        waitForElementVisible(By.cssSelector(".attr.option"), browser);
        waitForElementVisible(By.cssSelector(String.format("button.s-btn-%s",
                CssUtils.simplifyText(attribute.toLowerCase()))), browser);
    }

    public boolean isDeleteButtonDisabled() {
        waitForElementVisible(deleteButton);
        return deleteButton.getAttribute("class").contains("disabled");
    }

    public String getDeleteButtonDescription() {
        return deleteButtonInfo.getText();
    }
}
