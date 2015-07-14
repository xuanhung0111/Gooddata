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

    private static final By confirmDeleteButtonLocator = By.cssSelector(".yui3-d-modaldialog:not(.gdc-hidden) .c-modalDialog .s-btn-delete");

    private static final By visibilityCheckbox = By.id("settings-visibility");
    private static final By savePermissionSettingButton = By.cssSelector(".s-permissionSettingsDialog .s-btn-save_permissions");
    

    public String getMAQL(String metricName) {
        return waitForElementVisible(maql).getText();
    }

    public String getMetricFormat() {
        return waitForElementVisible(metricFormat).getText();
    }

    public void checkCreatedMetric(String metricName, String expectedMaql, String expectedFormat) {
        assertEquals(getMAQL(metricName), expectedMaql, "Metric is not created properly");
        assertEquals(getMetricFormat(), expectedFormat, "Metric format is not set properly");
    }

    public void changeMetricFormat(String newFormat) {
        waitForElementVisible(metricFormat).click();
        Graphene.createPageFragment(MetricFormatterDialog.class,
                waitForElementVisible(MetricFormatterDialog.LOCATOR, browser)).changeFormat(newFormat);
        assertEquals(getMetricFormat(), newFormat, "New format is not applied!");
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

    public void deleteMetric() throws InterruptedException {
        waitForElementVisible(deleteButton).click();
        waitForElementVisible(confirmDeleteButtonLocator, browser).click();
        waitForDataPageLoaded(browser);
    }
    
    public void setMetricVisibleToAllUser() {
        waitForElementVisible(sharingAndPermissionsButton).click();
        final WebElement checkbox = waitForElementVisible(visibilityCheckbox, browser);
        if (!checkbox.isSelected())
            checkbox.click();
        Graphene.waitGui().until().element(checkbox).is().selected();
        waitForElementVisible(savePermissionSettingButton, browser).click();
        waitForElementNotVisible(checkbox);
    }
}
