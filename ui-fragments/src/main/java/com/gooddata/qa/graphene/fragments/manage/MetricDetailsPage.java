package com.gooddata.qa.graphene.fragments.manage;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

import static org.testng.Assert.assertEquals;
import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class MetricDetailsPage extends AbstractFragment {

    @FindBy(xpath = "//div[contains(@class,'MAQLDocumentationContainer')]")
    private WebElement maql;

    @FindBy(xpath = "//span[contains(@class,'metric_format')]")
    private WebElement metricFormat;

    @FindBy(css = "#p-objectPage .s-btn-delete")
    private WebElement deleteButton;

    private static final By confirmDeleteButtonLocator = By.cssSelector(".yui3-d-modaldialog:not(.gdc-hidden) .c-modalDialog .s-btn-delete");

    private static final By inputEditor = By.className("ipeEditor");

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
        WebElement input = waitForElementVisible(inputEditor, browser);
        input.clear();
        input.sendKeys(newFormat);
        waitForElementVisible(By.className("s-ipeSaveButton"), browser).click();
        waitForElementNotVisible(input);
        assertEquals(getMetricFormat(), newFormat, "New format is not applied!");
    }

    public void deleteMetric() throws InterruptedException {
        waitForElementVisible(deleteButton).click();
        waitForElementVisible(confirmDeleteButtonLocator, browser).click();
        waitForDataPageLoaded(browser);
    }
}
