package com.gooddata.qa.graphene.fragments.dashboards;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class DashboardScatterExplorer extends AbstractFragment {

	public static final By BY_IFRAME_SCATTER = By.xpath("//iframe[contains(@src,'iaa/scatter')]");
	private static final By searchPropertyText = By
			.xpath("//div[contains(@class, 'searchfield')]//input[contains(@class, 'searchfield-input')]");
	private static final By propertySelectionDialogPanel = By
			.xpath("//div[contains(@class,'gd-dropdown') and not(contains(@class,'searchfield'))]");
	private static final By scatterContainer = By
			.xpath("//div[contains(@class,'scatter-container') and @data-highcharts-chart>=0]");
	private static final By tableContainer = By
			.xpath("//div[contains(@class,'dda-table-view')]/div[contains(@class, 'ember-table-tables-container')]");
	private static final By embeddedWidgetConfigAttributeButton = By
			.xpath("//div[contains(@class, 'edit-panel-block')]//button[contains(@class, 's-btn-select_attribute____')]");
	private static final By embeddedWidgetConfigXAxisButton = By
			.xpath("//div[contains(@class, 'edit-panel-block')]/label[contains(text(), 'X Axis')]/..//button");
	private static final By embeddedWidgetConfigYAxisButton = By
			.xpath("//div[contains(@class, 'edit-panel-block')]/label[contains(text(), 'Y Axis')]/..//button");
	private static final By embeddedWidgetConfigAttributeMetricButton = By
			.xpath("//div[contains(@class, 'edit-panel-block')]//button[contains(@class, 's-btn-select_attr__metric____')]");
	private static final By enableColorLink = By
			.xpath("//div[contains(@class,'edit-panel-block')]/a[@href='#' and text()='enable color']");
	private static final By addAdditionalColumnLink = By
			.xpath("//div[contains(@class,'edit-panel-block')]/a[@href='#' and text()='add additional column']");
	private static final By disableColorLink = By
			.xpath("//div[contains(@class,'edit-panel-block')]/a[@href='#' and text()='disable color']");
	private static final By removeAdditionalColumnLink = By
			.xpath("//div[contains(@class,'edit-panel-block')]//a[@href='#' and contains(@class,'dda-remove-icon-link')]");
	private static final By selectedDataPointInput = By
			.xpath("//div[contains(@class, 'edit-panel-block')][1]//button[not (contains(@class,'s-btn-select_attribute____'))]");

	private static final String widgetPropertyFolderLocator = "//div[contains(@class, 'is-collapsible')]/span[text()='${propertyFolderLabel}']";
	private static final String widgetAttributeLocator = "//div[contains(@class, 'type-attribute') and not(contains(@class, 'item-disabled'))]/span[text()='${propertyLabel}']";
	private static final String widgetMetricLocator = "//div[contains(@class, 'type-metric')]/span[text()='${propertyLabel}']";

	public void initScatterWidget(Map<String, String> data) throws InterruptedException {
		waitForElementVisible(BY_IFRAME_SCATTER, browser);
		browser.switchTo().frame(browser.findElement(BY_IFRAME_SCATTER));
		waitForElementVisible(embeddedWidgetConfigAttributeButton, browser).click();
		WebElement propertySelectionDialogWebELement = waitForElementVisible(
				propertySelectionDialogPanel, browser);
		waitForElementVisible(searchPropertyText, browser).sendKeys(data.get("dataPoint"));
		By attributeFolderWidget = By.xpath(widgetPropertyFolderLocator.replace(
				"${propertyFolderLabel}", data.get("dataPointFolder")));
		waitForElementPresent(attributeFolderWidget, browser).click();
		By attributeWidget = By.xpath(widgetAttributeLocator.replace("${propertyLabel}",
				data.get("dataPoint")));
		waitForElementVisible(attributeWidget, browser).click();
		waitForElementNotVisible(propertySelectionDialogWebELement);
		waitForElementVisible(embeddedWidgetConfigXAxisButton, browser).click();
		waitForElementVisible(searchPropertyText, browser).sendKeys(data.get("xAxisMetric"));
		Thread.sleep(1000);
		By xAxisFolderWidget = By.xpath(widgetPropertyFolderLocator.replace(
				"${propertyFolderLabel}", data.get("xAxisMetricFolder")));
		waitForElementVisible(xAxisFolderWidget, browser).click();
		By xAxisWidget = By.xpath(widgetMetricLocator.replace("${propertyLabel}",
				data.get("xAxisMetric")));
		waitForElementVisible(xAxisWidget, browser).click();
		waitForElementNotVisible(propertySelectionDialogWebELement);
		waitForElementVisible(embeddedWidgetConfigYAxisButton, browser).click();
		waitForElementVisible(searchPropertyText, browser).sendKeys(data.get("yAxisMetric"));
		By yAxisFolderWidget = By.xpath(widgetPropertyFolderLocator.replace(
				"${propertyFolderLabel}", data.get("yAxisMetricFolder")));
		waitForElementVisible(yAxisFolderWidget, browser).click();
		By yAxisWidget = By.xpath(widgetMetricLocator.replace("${propertyLabel}",
				data.get("yAxisMetric")));
		waitForElementVisible(yAxisWidget, browser).click();
		waitForElementNotVisible(propertySelectionDialogWebELement);
		Thread.sleep(3000);
	}

	public void addScatterWidget(Map<String, String> data, boolean invalidConfiguration)
			throws InterruptedException {
		String parentWindowHandle = browser.getWindowHandle();
		initScatterWidget(data);
		if (!invalidConfiguration) {
			waitForElementVisible(scatterContainer, browser);
			waitForElementVisible(tableContainer, browser);
		}
		browser.switchTo().window(parentWindowHandle);
	}

	public void addColorToScatterWidget(Map<String, String> data) throws InterruptedException {
		String parentWindowHandle = browser.getWindowHandle();
		waitForElementVisible(BY_IFRAME_SCATTER, browser);
		browser.switchTo().frame(browser.findElement(BY_IFRAME_SCATTER));
		int chartVersion = Integer.parseInt(waitForElementVisible(scatterContainer, browser)
				.getAttribute("data-highcharts-chart"));
		// wait for attribute name loaded in Data Point selection.
		waitForElementVisible(selectedDataPointInput, browser);
		waitForElementVisible(enableColorLink, browser).click();
		waitForElementVisible(embeddedWidgetConfigAttributeButton, browser).click();
		WebElement propertySelectionDialogWebELement = waitForElementVisible(
				propertySelectionDialogPanel, browser);
		waitForElementVisible(searchPropertyText, browser).sendKeys(data.get("colorAttribute"));
		Thread.sleep(2000); // wait for illegal attribute disabled.
		By attributeFolderWidget = By.xpath(widgetPropertyFolderLocator.replace(
				"${propertyFolderLabel}", data.get("colorAttributeFolder")));
		waitForElementPresent(attributeFolderWidget, browser).click();
		By attributeWidget = By.xpath(widgetAttributeLocator.replace("${propertyLabel}",
				data.get("colorAttribute")));
		waitForElementVisible(attributeWidget, browser).click();
		waitForElementNotVisible(propertySelectionDialogWebELement);
		// wait for a while for ScatterContainer updating version of chart
		Thread.sleep(3000);
		Assert.assertEquals(
				Integer.parseInt(waitForElementVisible(scatterContainer, browser).getAttribute(
						"data-highcharts-chart")), chartVersion + 1);
		browser.switchTo().window(parentWindowHandle);
	}

	public void disableColorToScatterWidget() throws InterruptedException {
		String parentWindowHandle = browser.getWindowHandle();
		waitForElementVisible(BY_IFRAME_SCATTER, browser);
		browser.switchTo().frame(browser.findElement(BY_IFRAME_SCATTER));
		int chartVersion = Integer.parseInt(waitForElementVisible(scatterContainer, browser)
				.getAttribute("data-highcharts-chart"));
		waitForElementVisible(disableColorLink, browser).click();
		Thread.sleep(2000);
		Assert.assertEquals(
				Integer.parseInt(waitForElementVisible(scatterContainer, browser).getAttribute(
						"data-highcharts-chart")), chartVersion + 1);
		browser.switchTo().window(parentWindowHandle);
	}

	public void addColumnsToScatterWidget(Map<String, ArrayList<HashMap<String, String>>> data)
			throws InterruptedException {
		String parentWindowHandle = browser.getWindowHandle();
		waitForElementVisible(BY_IFRAME_SCATTER, browser);
		browser.switchTo().frame(browser.findElement(BY_IFRAME_SCATTER));
		ArrayList<HashMap<String, String>> attributeList = data.get("attributeList");
		if (attributeList != null && attributeList.size() > 0) {
			for (Iterator<HashMap<String, String>> iterator = attributeList.iterator(); iterator
					.hasNext();) {
				HashMap<String, String> attribute = iterator.next();
				addOneColumnToScatterWidget(attribute, "attribute");
			}
		}
		ArrayList<HashMap<String, String>> metricList = data.get("metricList");
		if (metricList != null && metricList.size() > 0) {
			for (Iterator<HashMap<String, String>> iterator = metricList.iterator(); iterator
					.hasNext();) {
				HashMap<String, String> metric = iterator.next();
				addOneColumnToScatterWidget(metric, "metric");
			}
		}
		Thread.sleep(3000);
		browser.switchTo().window(parentWindowHandle);
	}

	private void addOneColumnToScatterWidget(HashMap<String, String> data, String type)
			throws InterruptedException {
		waitForElementVisible(addAdditionalColumnLink, browser).click();
		waitForElementVisible(embeddedWidgetConfigAttributeMetricButton, browser).click();
		WebElement propertySelectionDialogWebELement = waitForElementVisible(
				propertySelectionDialogPanel, browser);
		waitForElementVisible(searchPropertyText, browser).sendKeys(data.get("property"));
		Thread.sleep(2000); // wait for illegal attribute/metric disabled.
		By folderWiget = By.xpath(widgetPropertyFolderLocator.replace("${propertyFolderLabel}",
				data.get("propertyFolder")));
		waitForElementPresent(folderWiget, browser).click();
		if (type.equals("attribute")) {
			By attributeWidget = By.xpath(widgetAttributeLocator.replace("${propertyLabel}",
					data.get("property")));
			waitForElementVisible(attributeWidget, browser).click();
		} else if (type.equals("metric")) {
			By metricWidget = By.xpath(widgetMetricLocator.replace("${propertyLabel}",
					data.get("property")));
			waitForElementVisible(metricWidget, browser).click();
		}
		waitForElementNotVisible(propertySelectionDialogWebELement);
	}

	public void removeColumnsFromScatterWidget() throws InterruptedException {
		String parentWindowHandle = browser.getWindowHandle();
		waitForElementVisible(BY_IFRAME_SCATTER, browser);
		browser.switchTo().frame(browser.findElement(BY_IFRAME_SCATTER));
		List<WebElement> removeIcons = browser.findElements(removeAdditionalColumnLink);
		while (removeIcons.size() > 0) {
			WebElement removeIcon = removeIcons.get(0);
			waitForElementVisible(removeIcon).click();
			Thread.sleep(2000);
		}
		Thread.sleep(2000);
		browser.switchTo().window(parentWindowHandle);
	}
}
