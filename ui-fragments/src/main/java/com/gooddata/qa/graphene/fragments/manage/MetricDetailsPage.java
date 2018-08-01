package com.gooddata.qa.graphene.fragments.manage;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;

import com.gooddata.qa.graphene.fragments.common.PermissionSettingDialog;
import com.gooddata.qa.graphene.fragments.common.PermissionSettingDialog.PermissionType;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.stream.Collectors;

public class MetricDetailsPage extends ObjectPropertiesPage {

    @FindBy(xpath = "//div[contains(@class,'MAQLDocumentationContainer')]")
    private WebElement maql;

    @FindBy(className = "formatter")
    private WebElement metricFormat;

    @FindBy(css = ".s-btn-sharing__amp__permissions")
    private WebElement sharingAndPermissionsButton;

    @FindBy(className = "s-lockIcon")
    private WebElement lockIcon;

    @FindBy(className = "s-unlistedIcon")
    private WebElement eyeIcon;

    private static final By VISIBILITY_CHECKBOX = By.id("settings-visibility");
    private static final By SAVE_PERMISSION_SETTING_BUTTON =
            By.cssSelector(".s-permissionSettingsDialog .s-btn-save_permissions");

    public static final MetricDetailsPage getInstance(SearchContext context) {
        return Graphene.createPageFragment(MetricDetailsPage.class, waitForElementVisible(LOCATOR, context));
    }

    public String getMAQL() {
        return waitForElementVisible(maql).getText();
    }

    public String getMetricFormat() {
        return waitForElementVisible(metricFormat).getText();
    }

    public boolean isMetricCreatedSuccessfully(String expectedMaql, String expectedFormat) {
        if (!expectedMaql.equals(getMAQL())) {
            System.out.println("Metric is not created properly");
            return false;
        }

        if (!expectedFormat.equals(getMetricFormat())) {
            System.out.println("Metric format is not set properly");
            return false;
        }

        return true;
    }

    public MetricDetailsPage changeMetricFormat(MetricFormatterDialog.Formatter format) {
        waitForElementVisible(metricFormat).click();
        Graphene.createPageFragment(MetricFormatterDialog.class,
                waitForElementVisible(MetricFormatterDialog.LOCATOR, browser)).changeFormat(format);
        return this;
    }

    public MetricDetailsPage changeMetricFormatButDiscard(MetricFormatterDialog.Formatter format) {
        waitForElementVisible(metricFormat).click();
        Graphene.createPageFragment(MetricFormatterDialog.class,
                waitForElementVisible(MetricFormatterDialog.LOCATOR, browser)).changeFormatButDiscard(format);
        return this;
    }

    public MetricDetailsPage setMetricVisibleToAllUser() {
        openPermissionSettingDialog().setVisibility(true).save();
        return this;
    }

    public MetricDetailsPage setEditingPermission(PermissionType permissionType) {
        waitForElementVisible(sharingAndPermissionsButton).click();
        PermissionSettingDialog permissionSettingDialog = PermissionSettingDialog.getInstance(browser);
        permissionSettingDialog.setEditingPermission(permissionType).save();
        waitForFragmentNotVisible(permissionSettingDialog);
        if (permissionType == PermissionType.ADMIN) {
            waitForElementVisible(lockIcon);
        }
        return this;
    }

    public MetricDetailsPage setVisibility(boolean visible) {
        PermissionSettingDialog permissionSettingDialog = openPermissionSettingDialog();
        permissionSettingDialog.setVisibility(visible).save();
        waitForFragmentNotVisible(permissionSettingDialog);
        if (visible == false) {
            waitForElementVisible(eyeIcon);
        }
        return this;
    }

    public PermissionSettingDialog clickLockIcon() {
        waitForElementVisible(lockIcon).click();
        return PermissionSettingDialog.getInstance(browser);
    }

    public PermissionSettingDialog clickEyeIcon() {
        waitForElementVisible(eyeIcon).click();
        return PermissionSettingDialog.getInstance(browser);
    }

    public PermissionSettingDialog openPermissionSettingDialog() {
        waitForElementVisible(sharingAndPermissionsButton).click();
        return PermissionSettingDialog.getInstance(browser);
    }

    public boolean canOpenSettingDialogByClickLockIcon() {
        waitForElementVisible(lockIcon).click();
        return isElementPresent(className("s-btn-save_permissions"), browser);
    }

    public boolean isLockedMetric() {
        return isElementVisible(lockIcon);
    }

    public boolean isPrivateMetric() {
        return isElementVisible(eyeIcon);
    }

    public String getTooltipFromLockIcon() {
        new Actions(browser).moveToElement(lockIcon).moveByOffset(1, 1).perform();
        return waitForElementVisible(cssSelector(".bubble-overlay .content"), browser).getText();
    }

    public String getTooltipFromEyeIcon() {
        new Actions(browser).moveToElement(eyeIcon).moveByOffset(1, 1).perform();
        return waitForElementVisible(cssSelector(".bubble-overlay .content"), browser).getText();
    }

    public MetricEditorDialog openMetricEditor() {
        waitForElementVisible(className("metric_editMetric"), getRoot()).click();

        return MetricEditorDialog.getInstance(browser);
    }

    public List<String> getTitlesOfButtonsOnEditPanel() {
        return getRoot().findElements(cssSelector(".editPanel .btn:not(.gdc-hidden)")).stream()
                .map(button -> button.getText()).collect(Collectors.toList());
    }
}
