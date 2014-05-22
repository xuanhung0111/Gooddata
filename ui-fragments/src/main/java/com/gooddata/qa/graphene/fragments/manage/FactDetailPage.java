package com.gooddata.qa.graphene.fragments.manage;

import static org.testng.Assert.*;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.metrics.SimpleMetricTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class FactDetailPage extends AbstractFragment {
    @FindBy(xpath = "//table[@class = 'factAggregations']")
    private WebElement factAggregationTable;

    @FindBy(id = "objectUsedInContainer")
    private WebElement objectUsedInContainer;

    @FindBy(xpath = "//div[@id = 'objectUsedInContainer']/div[@class = 'mezzo']/a")
    private List<WebElement> usedInMetricList;

    @FindBy(xpath = "//div[contains(@class,'MAQLDocumentationContainer')]/a")
    private WebElement factLink;

    @FindBy(id = "p-objectPage")
    protected ObjectPropertiesPage objectPropertiesPage;

    private final String metricButtonLocator = "//td[@class='fceName' and text()='${metricType}']/../td[@class='fceList']/button";
    private final String createdMetricLocator = "//td[@class='fceName' and text()='${metricType}']/../td[@class='fceList']/a";
    private final String metricLinkLocator = "//div[@id = 'objectUsedInContainer']/a[text() = '${metricName}']";

    @FindBy(id = "p-objectPage")
    private MetricDetailsPage metricDetailPage;

    public void createSimpleMetric(SimpleMetricTypes metricType, String factName) {
	waitForObjectPageLoaded();
	String operation = metricType.getLabel();
	By metricButton = By.xpath(metricButtonLocator.replace("${metricType}",
		operation));
	waitForElementVisible(metricButton).click();
	waitForElementNotPresent(metricButton);
	String operationInElement = operation.substring(0, 1).toUpperCase()
		+ operation.substring(1).toLowerCase();
	String expectedMetricName = String.format("%s [%s]", factName,
		operationInElement);
	By createdMetric = By.xpath(createdMetricLocator.replace(
		"${metricType}", operation));
	waitForElementVisible(createdMetric);
	assertEquals(browser.findElement(createdMetric).getText(),
		expectedMetricName, "Metric is not created properly");
	verifyUsedInMetric(metricType, factName, expectedMetricName);
	waitForElementVisible(factLink).click();
    }

    public void verifyUsedInMetric(SimpleMetricTypes metricType,
	    String factName, String metricName) {
	waitForElementVisible(objectUsedInContainer);
	for (WebElement elem : usedInMetricList) {
	    assertTrue(elem.getText() == metricName,
		    "Metric is not created properly and not listed on Used in Metrics");
	}
	By metricLink = By.xpath(metricLinkLocator.replace("${metricName}",
		metricName));
	waitForElementVisible(metricLink).click();
	String expectedMaql = String.format("SELECT %s(%s)", metricType,
		factName);
	String expectedFormat = "#,##0.00";
	metricDetailPage.checkCreatedMetric(metricName, expectedMaql,
		expectedFormat);
    }

    public void changeFactFolder(String newFolderName) {
	waitForObjectPageLoaded();
	waitForElementVisible(factAggregationTable);
	objectPropertiesPage.changeObjectFolder(newFolderName);
    }
}
