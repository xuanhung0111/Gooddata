package com.gooddata.qa.graphene.fragments.manage;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

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

    private static final By BY_EXTERNAL_PAGE_LINK = By.cssSelector("button.s-btn-external_page");

    @FindBy(css = "button.s-btn-clear")
    private WebElement clearExternalPageButton;

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
}
