package com.gooddata.qa.graphene.fragments.manage;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

import static org.testng.Assert.assertEquals;
import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class MetricDetailsPage extends AbstractFragment {

    @FindBy(xpath = "//div[contains(@class,'MAQLDocumentationContainer')]")
    private WebElement maql;

    @FindBy(className = "formatter")
    private WebElement metricFormat;

    @FindBy(css = "#p-objectPage .s-btn-delete")
    private WebElement deleteButton;
    
    @FindBy(css = ".s-btn-sharing__amp__permissions")
    private WebElement sharingAndPermissionsButton;

    @FindBy(css = "div.objectHeader table tbody tr td h2")
    private WebElement name;

    private static final By CONFIRM_DELETE_BUTTON_LOCATOR =
            By.cssSelector(".yui3-d-modaldialog:not(.gdc-hidden) .c-modalDialog .s-btn-delete");

    private static final By VISIBILITY_CHECKBOX = By.id("settings-visibility");
    private static final By SAVE_PERMISSION_SETTING_BUTTON =
            By.cssSelector(".s-permissionSettingsDialog .s-btn-save_permissions");

    private static final By RENAME_INPUT_LOCATOR = By.cssSelector(".c-ipeEditorIn input");
    private static final By OK_BUTTON_LOCATOR = By.cssSelector(".c-ipeEditorControls button");

    public String getMAQL(String metricName) {
        return waitForElementVisible(maql).getText();
    }

    public String getMetricFormat() {
        return waitForElementVisible(metricFormat).getText();
    }

    public boolean isMetricCreatedSuccessfully(String metricName, String expectedMaql, String expectedFormat) {
        if (!expectedMaql.equals(getMAQL(metricName))) {
            System.out.println("Metric is not created properly");
            return false;
        }

        if (!expectedFormat.equals(getMetricFormat())) {
            System.out.println("Metric format is not set properly");
            return false;
        }

        return true;
    }

    public void changeMetricFormat(MetricFormatterDialog.Formatter format) {
        waitForElementVisible(metricFormat).click();
        Graphene.createPageFragment(MetricFormatterDialog.class,
                waitForElementVisible(MetricFormatterDialog.LOCATOR, browser)).changeFormat(format);
    }

    public void changeMetricFormatButDiscard(MetricFormatterDialog.Formatter format) {
        waitForElementVisible(metricFormat).click();
        Graphene.createPageFragment(MetricFormatterDialog.class,
                waitForElementVisible(MetricFormatterDialog.LOCATOR, browser)).changeFormatButDiscard(format);
    }

    public void deleteMetric() {
        waitForElementVisible(deleteButton).click();
        waitForElementVisible(CONFIRM_DELETE_BUTTON_LOCATOR, browser).click();
        waitForDataPageLoaded(browser);
    }

    public void setMetricVisibleToAllUser() {
        waitForElementVisible(sharingAndPermissionsButton).click();
        final WebElement checkbox = waitForElementVisible(VISIBILITY_CHECKBOX, browser);
        if (!checkbox.isSelected())
            checkbox.click();
        Graphene.waitGui().until().element(checkbox).is().selected();
        waitForElementVisible(SAVE_PERMISSION_SETTING_BUTTON, browser).click();
        waitForElementNotVisible(checkbox);
    }

    public void renameMetric(String newName) {
        waitForElementVisible(name).click();
        WebElement input = waitForElementVisible(RENAME_INPUT_LOCATOR, browser);
        input.clear();
        input.sendKeys(newName);
        waitForElementVisible(OK_BUTTON_LOCATOR, browser).click();
        waitForElementVisible(name);
        assertEquals(getName(), newName, "new metric name is not updated!");
    }

    private String getName() {
        return waitForElementVisible(name).getText();
    }
}
