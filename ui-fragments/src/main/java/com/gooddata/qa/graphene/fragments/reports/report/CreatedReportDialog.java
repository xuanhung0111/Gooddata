package com.gooddata.qa.graphene.fragments.reports.report;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.PermissionSettingDialog.PermissionType;
import com.gooddata.qa.graphene.utils.ElementUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.stream.Collectors;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static java.lang.String.format;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;

public class CreatedReportDialog extends AbstractFragment {

    private static final By ROOT_LOCATOR = By.className("s-saveReportDialog");

    @FindBy(className = "ember-text-field")
    private WebElement reportName;

    @FindBy(className = "s-btn-add_tags")
    private WebElement addTagButton;

    @FindBy(id = "settings-visibility")
    private WebElement visibilityCheckbox;

    @FindBy(css = ".s-btn-create, .s-btn-save, .s-btn-save_as")
    private WebElement createButton;

    @FindBy(className = "s-btn-cancel")
    private WebElement cancelButton;

    @FindBy(className = "input-radio")
    private List<WebElement> inputRadio;

    @FindBy(css = ".separated-top .value .value-part .row-info")
    private WebElement infoEditorPermission;

    public static CreatedReportDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(CreatedReportDialog.class,
                waitForElementVisible(ROOT_LOCATOR, searchContext));
    }

    public CreatedReportDialog setReportName(String name) {
        waitForElementVisible(reportName).clear();
        reportName.sendKeys(name);
        return this;
    }

    public CreatedReportDialog addTag(String tag) {
        waitForElementVisible(addTagButton).click();
        waitForElementNotVisible(addTagButton);

        WebElement input = waitForElementVisible(cssSelector(".c-ipeEditorIn input"), browser);
        input.clear();
        input.sendKeys(tag);
        waitForElementVisible(cssSelector(".c-ipeEditorControls .s-btn-add"), browser).click();
        waitForElementVisible(addTagButton);
        return this;
    }

    public CreatedReportDialog setReportVisibleSettings(boolean isVisible) {
        if (isVisible != waitForElementVisible(visibilityCheckbox).isSelected()) {
            visibilityCheckbox.click();
        }
        return this;
    }

    public CreatedReportDialog setPermissionSetting(PermissionType permission) {
        waitForElementVisible(cssSelector(permission.getCssSelector()), getRoot()).click();
        return this;
    }

    public List<PermissionType> getEnabledEditPermission() {
        return inputRadio.stream()
                .filter(element -> element.isEnabled())
                .map(element -> element.getAttribute("value"))
                .map(string -> string.equalsIgnoreCase(
                        PermissionType.ADMIN.toString()) ? PermissionType.ADMIN : PermissionType.ALL)
                .collect(Collectors.toList());
    }

    public String getRowInfoEditPermission() {
        return waitForElementVisible(infoEditorPermission).getText();
    }

    public List<String> getLockedAncestors() {
        //There is an element ".scrollableArea-shadow" overlap object which will be clicked
        //so that it is clicked at top-central instead of central.
        ElementUtils.scrollElementIntoView(infoEditorPermission, browser);
        getActions().moveToElement(waitForElementVisible(infoEditorPermission)
                .findElement(cssSelector("a:not(.inlineBubbleHelp)")), 0, 1).click().perform();
        return waitForElementVisible(className("lockedAncestors-list"), browser)
                .findElements(className("lockedAncestor-link"))
                .stream().map(WebElement::getText).collect(Collectors.toList());
    }

    public void saveReport() {
        waitForElementVisible(createButton).click();
        waitForFragmentNotVisible(this);
    }

    public void cancelCreateReport() {
        waitForElementVisible(cancelButton).click();
        waitForFragmentNotVisible(this);
    }

    public String getTextOnSaveButton() {
        return waitForElementVisible(createButton).getText();
    }
}
