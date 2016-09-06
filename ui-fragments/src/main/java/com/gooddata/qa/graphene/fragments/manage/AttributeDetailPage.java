package com.gooddata.qa.graphene.fragments.manage;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.getTooltipFromElement;
import static com.gooddata.qa.graphene.utils.ElementUtils.BY_BUBBLE_CONTENT;
import static com.gooddata.qa.graphene.utils.ElementUtils.makeSureNoPopupVisible;
import static java.util.Objects.isNull;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;

import java.util.Collection;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.enums.AttributeLabelTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;
import com.gooddata.qa.utils.CssUtils;

public class AttributeDetailPage extends ObjectPropertiesPage {

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

    @FindBy(css = ".labels-editor p")
    private WebElement labelSectionInfo;

    @FindBy(className = "inlineBubbleHelp")
    private WebElement labelTypeHelpIcon;

    @FindBy(className = "s-labelRow")
    private Collection<AttributeLabel> attributeLabels;

    private static final By BY_EXTERNAL_PAGE_LINK = By.cssSelector("button.s-btn-external_page");

    public static final AttributeDetailPage getInstance(SearchContext context) {
        return Graphene.createPageFragment(AttributeDetailPage.class, waitForElementVisible(LOCATOR, context));
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

    public String getDeleteButtonDescription() {
        return deleteButtonInfo.getText();
    }

    public AttributeLabel getLabel(String label) {
        return waitForCollectionIsNotEmpty(attributeLabels)
                .stream()
                .filter(e -> label.equals(e.getTitle()))
                .findFirst()
                .get();
    }

    public String getLabelSectionInfo() {
        return waitForElementVisible(labelSectionInfo).getText();
    }

    public String getLabelTypeTooltip() {
        return getTooltipFromElement(waitForElementVisible(labelTypeHelpIcon), browser);
    }

    public String getLabelTypeInfoLink() {
        makeSureNoPopupVisible();
        getActions().moveToElement(waitForElementVisible(labelTypeHelpIcon)).perform();

        return waitForElementVisible(BY_BUBBLE_CONTENT, browser).findElement(BY_LINK).getAttribute("href");
    }

    public String getAttributeUri() {
        return browser.getCurrentUrl().split("objectPage\\|")[1];
    }

    public static class AttributeLabel extends AbstractFragment {

        private static final By BY_EDIT_BUTTON = By.className("s-labelEditButton");
        private static final By BY_TYPE_SELECT = By.className("s-labelTypeSelect");

        @FindBy(className = "title-cell")
        private WebElement titleField;

        @FindBy(className = "type-cell")
        private WebElement typeField;

        public AttributeLabel changeTitle(String title) {
            clickEditButton();

            WebElement titleInput = waitForElementVisible(By.className("s-titleInput"), getRoot());
            titleInput.clear();
            titleInput.sendKeys(title);

            saveChanges();
            return this;
        }

        public AttributeLabel selectType(AttributeLabelTypes type) {
            clickEditButton();

            new Select(waitForElementVisible(BY_TYPE_SELECT, getRoot()))
                    .selectByVisibleText(type.getlabel());

            saveChanges();
            return this;
        }

        public boolean canSelectType() {
            return isNull(waitForElementVisible(BY_TYPE_SELECT, getRoot()).getAttribute("disabled"));
        }

        public boolean canEdit() {
            return isElementPresent(BY_EDIT_BUTTON, getRoot());
        }

        public String getTitle() {
            return waitForElementVisible(titleField).getText();
        }

        public String getType() {
            return waitForElementVisible(typeField).getText();
        }

        public AttributeLabel clickEditButton() {
            waitForElementVisible(BY_EDIT_BUTTON, getRoot()).click();
            return this;
        }

        private AttributeLabel saveChanges() {
            By saveButton = By.className("s-labelSaveButton");
            waitForElementVisible(saveButton, getRoot()).click();
            waitForElementNotPresent(saveButton);
            return this;
        }
    }
}
