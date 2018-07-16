package com.gooddata.qa.graphene.fragments.common;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.stream.Collectors;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;

public class PermissionSettingDialog extends AbstractFragment {

    private static final By LOCATOR = className("s-permissionSettingsDialog");

    @FindBy(className = "submit-button")
    private WebElement saveButton;

    @FindBy(className = "s-btn-cancel")
    private WebElement cancelButton;

    @FindBy(id = "settings-visibility")
    private WebElement visibilityCheckBox;

    @FindBy(css = ".separated-top .value .value-part .row-info")
    private WebElement infoEditorPermission;

    @FindBy(css = "div > .row .row-info")
    private WebElement infoVisibility;

    @FindBy(css = "header > h4")
    private WebElement header;

    public static PermissionSettingDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(PermissionSettingDialog.class, waitForElementVisible(LOCATOR, searchContext));
    }

    public PermissionSettingDialog setVisibility(boolean visible) {
        WebElement visibleCheckbox = waitForElementVisible(visibilityCheckBox);
        if (visible != visibleCheckbox.isSelected()) {
            visibleCheckbox.click();
        }
        return this;
    }

    public PermissionSettingDialog setEditingPermission(PermissionType permissionType) {
        waitForElementVisible(cssSelector(permissionType.getCssSelector()), getRoot()).click();
        return this;
    }

    public String getRowInfoEditPermission() {
        return waitForElementVisible(infoEditorPermission).getText();
    }

    public List<String> getLockedAncestors() {
        //There is an element ".scrollableArea-shadow" overlap object which will be clicked
        //so that it is clicked at top-central instead of central.
        getActions().moveToElement(waitForElementVisible(infoEditorPermission)
                .findElement(cssSelector("a:not(.inlineBubbleHelp)")), 0, 2).click().perform();
        return waitForElementVisible(className("lockedAncestors-list"), browser)
                .findElements(className("lockedAncestor-link"))
                .stream().map(WebElement::getText).collect(Collectors.toList());
    }

    public String getToolTipFromVisibilityQuestionIcon() {
        new Actions(browser).moveToElement(waitForElementVisible(cssSelector("div > .row .bubbleHelpView"), browser))
                .moveByOffset(1, 1).perform();
        Graphene.waitGui().until(screen -> browser.findElements(cssSelector(".arrow-cl .content")).size() > 1);
        return browser.findElements(className("bubble-overlay")).stream()
                .filter(element -> !element.getAttribute("style").contains("display"))
                .findFirst()
                .get()
                .getText();
    }

    public String getRowInfoVisibility() {
        return waitForElementVisible(infoVisibility).getText();
    }

    public String getHeaderTitle() {
        return waitForElementVisible(header).getText();
    }

    public String getAffectedElementInfo() {
        return waitForElementVisible(cssSelector(".buttons strong"), getRoot()).getText();
    }

    public boolean isEditPermissionSectionVisible() {
        return isElementVisible(className("separated-top"), getRoot());
    }

    public void save() {
        waitForElementVisible(saveButton).click();
        waitForFragmentNotVisible(this);
    }

    public void cancel() {
        waitForElementVisible(cancelButton).click();
        waitForFragmentNotVisible(this);
    }

    public enum PermissionType {
        ALL("input[value='all']"),
        ADMIN("input[value='admin']");

        private String cssSelector;

        PermissionType(String cssSelector) {
            this.cssSelector = cssSelector;
        }

        public String getCssSelector() {
            return this.cssSelector;
        }
    }
}
