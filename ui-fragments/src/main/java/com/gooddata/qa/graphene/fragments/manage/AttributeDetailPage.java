package com.gooddata.qa.graphene.fragments.manage;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDataPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static org.openqa.selenium.By.xpath;
import static org.testng.Assert.assertEquals;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;
import com.gooddata.qa.utils.CssUtils;

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

    @FindBy(css = ".specialAction .info")
    private WebElement deleteButtonInfo;

    @FindBy(css = "button.pickAttribute")
    private WebElement selectDrillAttributeButton;

    @FindBy(css = "#p-objectPage .s-btn-delete")
    private WebElement deleteButton;

    private static final By confirmDeleteButtonLocator = By.cssSelector(".yui3-d-modaldialog:not(.gdc-hidden) .c-modalDialog .s-btn-delete");

    private static final By BY_EXTERNAL_PAGE_LINK = By.cssSelector("button.s-btn-external_page");

    // ************** Dialog rename attribute ************** //
    // can not declare these attributes as web element 
    // because they do not belong to AttributeDetailPage fragment 
    private static final By ATTRIBUTE_RENAME_INPUT_LOCATOR = By.cssSelector(".c-ipeEditorIn input");
    private static final By OK_BUTTON_LOCATOR              = By.cssSelector(".c-ipeEditorControls button");
    // ********************************************************

    public static final String ROOT_XPATH_LOCATOR = "//div[@id='p-objectPage' and contains(@class,'s-displayed')]";

    public static final AttributeDetailPage getInstance(SearchContext context) {
        return Graphene.createPageFragment(AttributeDetailPage.class,
                waitForElementVisible(xpath(ROOT_XPATH_LOCATOR), context));
    }

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

    public void selectLabelType(String labelType) {
        waitForElementVisible(labelEditButton).click();
        waitForElementVisible(labelTypeSelect);
        sleepTightInSeconds(2);
        labelTypeSelect.selectByVisibleText(labelType);
        sleepTightInSeconds(2);
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

    public AttributeDetailPage clearDrillingSetting() {
        waitForElementVisible(clearExternalPageButton).click();
        waitForElementNotVisible(clearExternalPageButton);
        return this;
    }

    public AttributeDetailPage setDrillToAttribute(String attribute) {
        waitForElementPresent(clearExternalPageButton);
        if (!clearExternalPageButton.getAttribute("class").contains("gdc-hidden")) {
            clearDrillingSetting();
        }
        waitForElementVisible(selectDrillAttributeButton).click();
        SelectItemPopupPanel popup = SelectItemPopupPanel.getInstance(browser);
        popup.searchAndSelectItem(attribute).submitPanel();
        waitForElementNotVisible(popup.getRoot());
        waitForElementVisible(By.cssSelector(".attr.option"), browser);
        waitForElementVisible(By.cssSelector(String.format("button.s-btn-%s",
                CssUtils.simplifyText(attribute.toLowerCase()))), browser);
        return this;
    }

    public boolean isDeleteButtonDisabled() {
        waitForElementVisible(deleteButton);

        // check that the button is truly non-clickable, i.e. that the delete
        // confirmation dialog does not appear
        deleteButton.click();
        waitForElementNotVisible(By.cssSelector(".t-confirmDelete"));

        return deleteButton.getAttribute("class").contains("disabled");
    }
    
    public void deleteAttribute() {
        waitForElementVisible(deleteButton).click();
        waitForElementVisible(confirmDeleteButtonLocator, browser).click();
        waitForDataPageLoaded(browser);
    }

    public String getDeleteButtonDescription() {
        return deleteButtonInfo.getText();
    }
}
