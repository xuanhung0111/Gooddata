package com.gooddata.qa.graphene.fragments.manage;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class MetricDetailsPage extends ObjectPropertiesPage {

    @FindBy(xpath = "//div[contains(@class,'MAQLDocumentationContainer')]")
    private WebElement maql;

    @FindBy(className = "formatter")
    private WebElement metricFormat;

    @FindBy(css = ".s-btn-sharing__amp__permissions")
    private WebElement sharingAndPermissionsButton;

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

    public void setMetricVisibleToAllUser() {
        waitForElementVisible(sharingAndPermissionsButton).click();
        final WebElement checkbox = waitForElementVisible(VISIBILITY_CHECKBOX, browser);
        if (!checkbox.isSelected())
            checkbox.click();
        Graphene.waitGui().until().element(checkbox).is().selected();
        waitForElementVisible(SAVE_PERMISSION_SETTING_BUTTON, browser).click();
        waitForElementNotVisible(checkbox);
    }

    public MetricEditorDialog openMetricEditor() {
        waitForElementVisible(By.className("metric_editMetric"), getRoot()).click();

        return MetricEditorDialog.getInstance(browser);
    }
}
