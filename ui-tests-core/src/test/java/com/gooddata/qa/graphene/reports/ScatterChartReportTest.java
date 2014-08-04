package com.gooddata.qa.graphene.reports;

import static com.gooddata.qa.graphene.common.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.DashFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEmbedDialog;
import com.gooddata.qa.graphene.fragments.dashboards.FilterWidget;
import com.gooddata.qa.graphene.fragments.dashboards.FilterWidget.FilterPanel;

public class ScatterChartReportTest extends GoodSalesAbstractTest {

	private static final By by_iframe = By.xpath("//iframe[contains(@src,'scatter')]");
	private static final long expectedDashboardExportSize = 42000;

	@Test(dependsOnMethods = { "createProject" }, groups = { "addAndEditScatterWidgetTest" })
	public void addScatterWidgetTest() throws InterruptedException {
		Map<String, String> data = new HashMap<String, String>();
		data.put("dataPointFolder", "Stage");
		data.put("dataPoint", "Stage Name");
		data.put("xAxisMetricFolder", "Sales Figures");
		data.put("xAxisMetric", "Amount");
		data.put("yAxisMetricFolder", "Sales Figures");
		data.put("yAxisMetric", "Avg. Amount");
		initDashboardsPage();
		DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
		dashboardsPage.addNewDashboard("Scatter Explorer");
		Thread.sleep(2000);
		dashboardsPage.editDashboard();
		dashboardEditBar.addScatterWidgetToDashboard(data);
		dashboardEditBar.saveDashboard();
		testScatterWidgetDisplaying();
	}

	private void testScatterWidgetDisplaying() throws InterruptedException {
		Thread.sleep(2000);
		// hover on data point
		waitForElementVisible(by_iframe, browser);
		browser.switchTo().frame(browser.findElement(by_iframe));
		By dataPoint = By
				.xpath("//div[@class='highcharts-container']/*[local-name()='svg' and namespace-uri()='http://www.w3.org/2000/svg']/*[name()='g' and @class='highcharts-series-group']//*[name() = 'path' and position() = 8]");
		WebElement selectedDataPointElement = waitForElementVisible(dataPoint, browser);
		Actions builder = new Actions(browser);
		Actions hoverOverDataPoint = builder.moveToElement(selectedDataPointElement);
		hoverOverDataPoint.perform();
		By nameTooltip = By
				.xpath("//div[@class='highcharts-container']/div[@class='highcharts-tooltip']//div[@class='content']/div[@class='tt-name']");
		By contentTooltip1 = By
				.xpath("//div[@class='highcharts-container']/div[@class='highcharts-tooltip']//div[@class='content']/table[@class='tt-values']//tr[1]/td[@class='title']");
		By contentTooltip2 = By
				.xpath("//div[@class='highcharts-container']/div[@class='highcharts-tooltip']//div[@class='content']/table[@class='tt-values']//tr[2]/td[@class='title']");
		Assert.assertTrue(waitForElementVisible(nameTooltip, browser).isDisplayed(),
				"Attribute name is not displayed");
		Assert.assertEquals(waitForElementVisible(nameTooltip, browser).getText(), "STAGE NAME");
		Assert.assertTrue(waitForElementVisible(contentTooltip1, browser).isDisplayed(),
				"X Axis metric name is not displayed");
		Assert.assertEquals(waitForElementVisible(contentTooltip1, browser).getText(), "Amount");
		Assert.assertTrue(waitForElementVisible(contentTooltip2, browser).isDisplayed(),
				"Y Axis metric is not displayed");
		Assert.assertEquals(waitForElementVisible(contentTooltip2, browser).getText(),
				"Avg. Amount");
		// click on one data point to make other data points become gray and
		// explorer table is selected correspondingly
		By tableStatus = By
				.xpath("//div[contains(@class,'dda-table-view')]//div[contains(@class,'ember-table-fixed-table-container') and contains(@class,'ember-table-footer-container')]//div[contains(@class,'ember-table-right-table-block')]//div[contains(@class,'text-align-left')]/span");
		WebElement tableStatusElement = browser.findElement(tableStatus);
		By dataPoints = By
				.xpath("//div[@class='highcharts-container']/*[local-name()='svg' and namespace-uri()='http://www.w3.org/2000/svg']/*[name()='g' and @class='highcharts-series-group']//*[name() = 'path']");
		List<WebElement> dataPointElements = browser.findElements(dataPoints);
		for (WebElement dataPointElement : dataPointElements) {
			Assert.assertEquals(dataPointElement.getAttribute("fill"), "rgb(77,133,255)");
			System.out.println(dataPointElement.getLocation() + " is blue");
		}
		Assert.assertEquals(tableStatusElement.getText(), "8 total",
				"Table does not contain report data");
		selectedDataPointElement.click();
		Assert.assertEquals(selectedDataPointElement.getAttribute("fill"), "rgb(77,133,255)");
		for (WebElement dataPointElement : dataPointElements) {
			if (!dataPointElement.equals(selectedDataPointElement)) {
				Assert.assertEquals(dataPointElement.getAttribute("fill"), "rgb(194,194,194)");
				System.out.println(dataPointElement.getLocation() + " is gray");
			}
		}
		Assert.assertEquals(tableStatusElement.getText(), "1 selected out of 8 total",
				"Table does not contain data of the selected data point");
		// re-click on the data point to make all points back to normal
		selectedDataPointElement.click();
		for (WebElement dataPointElement : dataPointElements) {
			Assert.assertEquals(dataPointElement.getAttribute("fill"), "rgb(77,133,255)");
			System.out.println(dataPointElement.getLocation() + " is blue");
		}
		Assert.assertEquals(tableStatusElement.getText(), "8 total",
				"Table does not contain report data");
	}

	@Test(dependsOnMethods = { "addScatterWidgetTest" }, priority = 2, groups = { "addAndEditScatterWidgetTest" })
	public void addColorSeriesTest() throws InterruptedException {
		Map<String, String> data = new HashMap<String, String>();
		data.put("colorAttributeFolder", "Stage");
		data.put("colorAttribute", "Status");
		initDashboardsPage();
		dashboardsPage.editDashboard();
		DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
		dashboardEditBar.addColorToScatterWidget(data);
		String parentWindowHandle = browser.getWindowHandle();
		waitForElementVisible(by_iframe, browser);
		browser.switchTo().frame(browser.findElement(by_iframe));
		// check whether color legend is added to scatter widget
		By legendSeries = By
				.xpath("//div[@class='highcharts-container']/*[local-name()='svg' and namespace-uri()='http://www.w3.org/2000/svg']/*[name()='g' and @class='highcharts-legend']//*[name()='g' and @class='highcharts-legend-item']");
		List<WebElement> series = browser.findElements(legendSeries);
		Assert.assertEquals(series.size(), 3, "Color legend is not added to Scatter chart");
		// check the second column header is STATUS
		By tableHeaderColumn2 = By
				.xpath("//div[contains(@class,'dda-table-view')]//div[contains(@class,'ember-table-header-row')]/div/div[2]//span");
		Assert.assertEquals(browser.findElement(tableHeaderColumn2).getText(), "STATUS",
				"STATUS column is not added to table");
		browser.switchTo().window(parentWindowHandle);
		dashboardEditBar.saveDashboard();
		waitForElementVisible(by_iframe, browser);
		browser.switchTo().frame(browser.findElement(by_iframe));
		// hover on data point
		By dataPoint = By
				.xpath("//div[@class='highcharts-container']/*[local-name()='svg' and namespace-uri()='http://www.w3.org/2000/svg']/*[name()='g' and @class='highcharts-series-group']//*[name() = 'path' and position() = 6]");
		WebElement selectedDataPointElement = waitForElementVisible(dataPoint, browser);
		Actions builder = new Actions(browser);
		Actions hoverOverDataPoint = builder.moveToElement(selectedDataPointElement);
		hoverOverDataPoint.perform();
		By nameTooltip = By
				.xpath("//div[@class='highcharts-container']/div[@class='highcharts-tooltip']//div[@class='content']/div[@class='tt-name']");
		By contentTooltip1 = By
				.xpath("//div[@class='highcharts-container']/div[@class='highcharts-tooltip']//div[@class='content']/table[@class='tt-values']//tr[1]/td[@class='title']");
		By contentTooltip2 = By
				.xpath("//div[@class='highcharts-container']/div[@class='highcharts-tooltip']//div[@class='content']/table[@class='tt-values']//tr[2]/td[@class='title']");
		By contentTooltip3 = By
				.xpath("//div[@class='highcharts-container']/div[@class='highcharts-tooltip']//div[@class='content']/table[@class='tt-values']//tr[3]/td[@class='title']");
		Assert.assertTrue(waitForElementVisible(nameTooltip, browser).isDisplayed(),
				"Attribute name is not displayed");
		Assert.assertEquals(waitForElementVisible(nameTooltip, browser).getText(), "STAGE NAME");
		Assert.assertTrue(waitForElementVisible(contentTooltip1, browser).isDisplayed(),
				"Color legend name is not displayed");
		Assert.assertEquals(waitForElementVisible(contentTooltip1, browser).getText(), "Status");
		Assert.assertTrue(waitForElementVisible(contentTooltip2, browser).isDisplayed(),
				"X Axis metric name is not displayed");
		Assert.assertEquals(waitForElementVisible(contentTooltip2, browser).getText(), "Amount");
		Assert.assertTrue(waitForElementVisible(contentTooltip3, browser).isDisplayed(),
				"Y Axis metric is not displayed");
		Assert.assertEquals(waitForElementVisible(contentTooltip3, browser).getText(),
				"Avg. Amount");
		// click on one data point to make other data points become gray and
		// explorer table is selected correspondingly
		By tableStatus = By
				.xpath("//div[contains(@class,'dda-table-view')]//div[contains(@class,'ember-table-fixed-table-container') and contains(@class,'ember-table-footer-container')]//div[contains(@class,'ember-table-right-table-block')]//div[contains(@class,'text-align-left')][1]/span");
		WebElement tableStatusElement = browser.findElement(tableStatus);
		By dataPoints = By
				.xpath("//div[@class='highcharts-container']/*[local-name()='svg' and namespace-uri()='http://www.w3.org/2000/svg']/*[name()='g' and @class='highcharts-series-group']//*[name() = 'path']");
		List<WebElement> dataPointElements = browser.findElements(dataPoints);
		selectedDataPointElement.click();
		Assert.assertEquals(selectedDataPointElement.getAttribute("fill"), "rgb(77,133,255)");
		for (Iterator<WebElement> iterator = dataPointElements.iterator(); iterator.hasNext();) {
			WebElement dataPointElement = (WebElement) iterator.next();
			if (!dataPointElement.equals(selectedDataPointElement)) {
				Assert.assertEquals(dataPointElement.getAttribute("fill"), "rgb(194,194,194)");
				System.out.println(dataPointElement.getLocation() + " is gray");
			}
		}
		Assert.assertEquals(tableStatusElement.getText(), "1 selected out of 8 total",
				"Table does not contain data of the selected data point");
		browser.switchTo().window(parentWindowHandle);
		// disable color
		dashboardsPage.editDashboard();
		dashboardEditBar.disableColorInScatterWidget();
		waitForElementVisible(by_iframe, browser);
		browser.switchTo().frame(browser.findElement(by_iframe));
		// check whether color legend is removed from scatter widget
		series = browser.findElements(legendSeries);
		Assert.assertEquals(series.size(), 1, "Color legend is not removed from Scatter chart");
		// check the second column header is no longer STATUS
		By.xpath("//div[contains(@class,'dda-table-view')]//div[contains(@class,'ember-table-header-row')]/div/div[2]//span");
		Assert.assertNotEquals(browser.findElement(tableHeaderColumn2).getText(), "STATUS",
				"STATUS column header is not removed from table");
		browser.switchTo().window(parentWindowHandle);
		dashboardEditBar.saveDashboard();
	}

	@Test(dependsOnMethods = { "addScatterWidgetTest" }, priority = 3, groups = { "addAndEditScatterWidgetTest" })
	public void addAdditionalColumnTest() throws InterruptedException {
		Map<String, ArrayList<HashMap<String, String>>> data = new HashMap<String, ArrayList<HashMap<String, String>>>();
		ArrayList<HashMap<String, String>> attributeList = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> attribute = new HashMap<String, String>();
		attribute.put("propertyFolder", "Product");
		attribute.put("property", "Product");
		attributeList.add(attribute);
		data.put("attributeList", attributeList);
		ArrayList<HashMap<String, String>> metricList = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> metric = new HashMap<String, String>();
		metric.put("propertyFolder", "Sales Figures");
		metric.put("property", "Expected");
		metricList.add(metric);
		data.put("metricList", metricList);
		initDashboardsPage();
		dashboardsPage.editDashboard();
		DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
		dashboardEditBar.addColumnsToScatterWidget(data);
		dashboardEditBar.saveDashboard();
		Thread.sleep(3000);
		String parentWindowHandle = browser.getWindowHandle();
		waitForElementVisible(by_iframe, browser);
		browser.switchTo().frame(browser.findElement(by_iframe));
		By tableHeaderColumn4 = By
				.xpath("//div[contains(@class,'dda-table-view')]//div[contains(@class,'ember-table-header-row')]/div/div[4]//span");
		By tableHeaderColumn5 = By
				.xpath("//div[contains(@class,'dda-table-view')]//div[contains(@class,'ember-table-header-row')]/div/div[5]//span");
		Assert.assertEquals(browser.findElement(tableHeaderColumn4).getText(), "PRODUCT NAME",
				"PRODUCT NAME column is not added to table");
		Assert.assertEquals(browser.findElement(tableHeaderColumn5).getText(), "EXPECTED",
				"EXPECTED column is not added to table");
		// remove additional columns
		browser.switchTo().window(parentWindowHandle);
		dashboardsPage.editDashboard();
		dashboardEditBar.removeColumnsFromScatterWidget();
		// check whether Product and Expected removed from table or not
		waitForElementVisible(by_iframe, browser);
		browser.switchTo().frame(browser.findElement(by_iframe));
		By tableHeaders = By
				.xpath("//div[contains(@class,'dda-table-view')]//div[contains(@class,'ember-table-header-row')]/div/div//span");
		Assert.assertEquals(browser.findElements(tableHeaders).size(), 3);
		browser.switchTo().window(parentWindowHandle);
		dashboardEditBar.saveDashboard();
	}

	@Test(dependsOnMethods = { "addScatterWidgetTest" }, priority = 4, groups = { "addAndEditScatterWidgetTest" })
	public void changeScatterExplorerTitle() throws InterruptedException {
		String scatterTitleText = new String("New Scatter Explorer");
		String scatterSubtitleText = new String("this is a test for scatter explorer");
		String fakeScatterTitleText = new String("New Scatter Explorer 2");
		String fakeScatterSubtitleText = new String("this is a test for scatter explorer 2");
		initDashboardsPage();
		dashboardsPage.editDashboard();
		checkRedBar(browser);
		Thread.sleep(3000);
		String parentWindowHandle = browser.getWindowHandle();
		waitForElementVisible(by_iframe, browser).click(); // click on the
															// scatter widget in
															// EDIT mode to make
															// it enable (it's a
															// kind of error).
		browser.switchTo().frame(browser.findElement(by_iframe));
		// hover on scatter title
		By scatterTitleInEditMode = By
				.xpath("//div[contains(@class,'dda-editable-text-view')]/div[@class='title']//label[contains(@class,'editable-label')]");
		WebElement scatterTitleInEditModeElement = waitForElementVisible(scatterTitleInEditMode,
				browser);
		Actions builder = new Actions(browser);
		Actions hoverAction = builder.moveToElement(scatterTitleInEditModeElement);
		hoverAction.perform();
		Assert.assertEquals(scatterTitleInEditModeElement.getCssValue("background-color"),
				"rgba(255, 253, 198, 1)", "Scatter title is not highlighted when being hovered on");
		// hover on scatter subtitle
		By scatterSubtitleInEditMode = By
				.xpath("//div[contains(@class,'dda-editable-text-view')]/div[@class='subtitle']//label[contains(@class,'editable-label')]");
		WebElement scatterSubtitleInEditModeElement = waitForElementVisible(
				scatterSubtitleInEditMode, browser);
		hoverAction = builder.moveToElement(scatterSubtitleInEditModeElement);
		hoverAction.perform();
		Assert.assertEquals(scatterSubtitleInEditModeElement.getCssValue("background-color"),
				"rgba(255, 253, 198, 1)",
				"Scatter subtitle is not highlighted when being hovered on");
		scatterTitleInEditModeElement.click();
		WebElement scatterTitleInput = waitForElementVisible(
				By.xpath("//div[contains(@class,'dda-editable-text-view')]/div[@class='title']//input"),
				browser);
		scatterTitleInput.clear();
		scatterTitleInput.sendKeys(scatterTitleText);
		scatterSubtitleInEditModeElement.click();
		WebElement scatterSubtitleInput = waitForElementVisible(
				By.xpath("//div[contains(@class,'dda-editable-text-view')]/div[@class='subtitle']//input"),
				browser);
		scatterSubtitleInput.clear();
		scatterSubtitleInput.sendKeys(scatterSubtitleText);
		browser.switchTo().window(parentWindowHandle);
		dashboardsPage.getDashboardEditBar().saveDashboard();
		Thread.sleep(3000);
		browser.switchTo().frame(browser.findElement(by_iframe));
		By scatterTitleInViewMode = By
				.xpath("//div[contains(@class,'dda-editable-text-view')]//div[@class='title']");
		By scatterSubTitleInViewMode = By
				.xpath("//div[contains(@class,'dda-editable-text-view')]//div[@class='subtitle']");
		WebElement scatterTitleInViewModeElement = waitForElementVisible(scatterTitleInViewMode,
				browser);
		WebElement scatterSubtitleInViewModeElement = waitForElementVisible(
				scatterSubTitleInViewMode, browser);
		Assert.assertEquals(scatterTitleInViewModeElement.getText(), scatterTitleText,
				"Scatter Title is not updated");
		Assert.assertEquals(scatterSubtitleInViewModeElement.getText(), scatterSubtitleText,
				"Scatter Subtitle is not updated");
		browser.switchTo().window(parentWindowHandle);
		// check editing scatter title/subtitle then cancel
		dashboardsPage.editDashboard();
		waitForElementVisible(by_iframe, browser).click(); // click on the
															// scatter widget in
															// EDIT mode to make
															// it enable (it's a
															// kind of error).
		browser.switchTo().frame(browser.findElement(by_iframe));
		scatterTitleInEditModeElement = waitForElementVisible(scatterTitleInEditMode, browser);
		scatterSubtitleInEditModeElement = waitForElementVisible(scatterSubtitleInEditMode, browser);
		scatterTitleInEditModeElement.click();
		scatterTitleInput = waitForElementVisible(
				By.xpath("//div[contains(@class,'dda-editable-text-view')]//div[@class='title']//input"),
				browser);
		scatterTitleInput.clear();
		scatterTitleInput.sendKeys(fakeScatterTitleText);
		scatterSubtitleInEditModeElement.click();
		scatterSubtitleInput = waitForElementVisible(
				By.xpath("//div[contains(@class,'dda-editable-text-view')]//div[@class='subtitle']//input"),
				browser);
		scatterSubtitleInput.clear();
		scatterSubtitleInput.sendKeys(fakeScatterSubtitleText);
		browser.switchTo().window(parentWindowHandle);
		dashboardsPage.getDashboardEditBar().cancelDashboard();
		browser.switchTo().frame(browser.findElement(by_iframe));
		scatterTitleInViewModeElement = waitForElementVisible(scatterTitleInViewMode, browser);
		scatterSubtitleInViewModeElement = waitForElementVisible(scatterSubTitleInViewMode, browser);
		Assert.assertEquals(scatterTitleInViewModeElement.getText(), scatterTitleText,
				"Scatter Title is changed in CANCEL action");
		Assert.assertEquals(scatterSubtitleInViewModeElement.getText(), scatterSubtitleText,
				"Scatter Subtitle is changed in CANCEL action");
	}

	@Test(dependsOnMethods = { "addScatterWidgetTest" }, priority = 5, alwaysRun = true, groups = { "addAndEditScatterWidgetTest" })
	public void shareScatterExplorerTest() throws InterruptedException {
		initDashboardsPage();
		DashboardEmbedDialog dialog = dashboardsPage.embedDashboard();
		String uri = dialog.getPreviewURI();
		browser.navigate().to(uri);
		testScatterWidgetDisplaying();
	}

	@Test(dependsOnMethods = { "addScatterWidgetTest" }, priority = 6, alwaysRun = true, groups = { "addAndEditScatterWidgetTest" })
	public void exportDashboardContainingScatterExplorerTest() throws InterruptedException {
		initDashboardsPage();
		String exportedDashboardName = dashboardsPage.exportDashboardTab(0);
		verifyDashboardExport(exportedDashboardName, expectedDashboardExportSize);
	}

	@Test(dependsOnGroups = { "addAndEditScatterWidgetTest" }, priority = 1, alwaysRun = true, groups = { "advancedScatterWidgetTest" })
	public void addScatterWidgetWithTooManyDataPointsTest() throws InterruptedException {
		Map<String, String> data = new HashMap<String, String>();
		data.put("dataPointFolder", "Account");
		data.put("dataPoint", "Account");
		data.put("xAxisMetricFolder", "Sales Figures");
		data.put("xAxisMetric", "Amount");
		data.put("yAxisMetricFolder", "Sales Figures");
		data.put("yAxisMetric", "Avg. Amount");
		String parentWindowHandle = browser.getWindowHandle();
		initDashboardsPage();
		DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
		dashboardsPage.addNewDashboard("Scatter Explorer 2");
		browser.navigate().refresh(); // after creating the new dashboard, the
										// browser still keeps the iframe of the
										// previous scatter widget
										// refresh browser to remove the iframe
										// of the old scatter widget
		Thread.sleep(2000);
		dashboardsPage.editDashboard();
		dashboardEditBar.addScatterWidgetToDashboard(data);
		// check "too many data points" message
		waitForElementVisible(by_iframe, browser);
		browser.switchTo().frame(browser.findElement(by_iframe));
		By alertMessage = By
				.xpath("//div[contains(@class,'scatter-component')]//div[@class='alert']//div[@class='alert-title']");
		WebElement alertMessageElement = waitForElementVisible(alertMessage, browser);
		Assert.assertEquals(alertMessageElement.getText().trim(), "Too many data points");
		browser.switchTo().window(parentWindowHandle);
		dashboardEditBar.saveDashboard();
		// check this alert message still show after saving dashboard
		waitForElementVisible(by_iframe, browser);
		browser.switchTo().frame(browser.findElement(by_iframe));
		alertMessageElement = waitForElementVisible(alertMessage, browser);
		Assert.assertEquals(alertMessageElement.getText().trim(), "Too many data points");
		browser.switchTo().window(parentWindowHandle);
		dashboardsPage.deleteDashboard();
	}

	@Test(dependsOnGroups = { "addAndEditScatterWidgetTest" }, priority = 2, alwaysRun = true, groups = { "advancedScatterWidgetTest" })
	public void addScatterWidgetWithInvalidConfigurationTest() throws InterruptedException {
		Map<String, String> data = new HashMap<String, String>();
		data.put("dataPointFolder", "Stage History");
		data.put("dataPoint", "Stage History");
		data.put("xAxisMetricFolder", "Sales Figures");
		data.put("xAxisMetric", "Amount");
		data.put("yAxisMetricFolder", "Sales Figures");
		data.put("yAxisMetric", "Avg. Amount");
		String parentWindowHandle = browser.getWindowHandle();
		initDashboardsPage();
		DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
		dashboardsPage.addNewDashboard("Scatter Explorer 3");
		browser.navigate().refresh(); // after creating the new dashboard, the
										// browser still keeps the iframe of the
										// previous scatter widget
										// refresh browser to remove the iframe
										// of the old scatter widget
		Thread.sleep(2000);
		dashboardsPage.editDashboard();
		dashboardEditBar.addScatterWidgetToDashboard(data, true);
		// check "invalid configuration" message
		waitForElementVisible(by_iframe, browser);
		browser.switchTo().frame(browser.findElement(by_iframe));
		By invalidConfigurationAlert = By
				.xpath("//div[contains(@class,'gd-dashboard')]/div[contains(@class,'explorer-message')]/div[@class='explorer-message-title']");
		WebElement invalidConfigurationAlertElement = waitForElementVisible(
				invalidConfigurationAlert, browser);
		Assert.assertEquals(invalidConfigurationAlertElement.getText().trim(),
				"Invalid configuration");
		browser.switchTo().window(parentWindowHandle);
		dashboardEditBar.saveDashboard();
		// check this alert message still show after saving dashboard
		waitForElementVisible(by_iframe, browser);
		browser.switchTo().frame(browser.findElement(by_iframe));
		invalidConfigurationAlertElement = waitForElementVisible(invalidConfigurationAlert, browser);
		Assert.assertEquals(invalidConfigurationAlertElement.getText().trim(),
				"Invalid configuration");
		browser.switchTo().window(parentWindowHandle);
		dashboardsPage.deleteDashboard();
	}

	@Test(dependsOnGroups = { "addAndEditScatterWidgetTest" }, priority = 3, alwaysRun = true, groups = { "advancedScatterWidgetTest" })
	public void applyFilterOnScatterWidgetTest() throws InterruptedException {
		Map<String, String> data = new HashMap<String, String>();
		data.put("dataPointFolder", "Stage");
		data.put("dataPoint", "Stage Name");
		data.put("xAxisMetricFolder", "Sales Figures");
		data.put("xAxisMetric", "Amount");
		data.put("yAxisMetricFolder", "Sales Figures");
		data.put("yAxisMetric", "Avg. Amount");
		data.put("colorAttributeFolder", "Sales Rep");
		data.put("colorAttribute", "Sales Rep");
		String parentWindowHandle = browser.getWindowHandle();
		initDashboardsPage();
		DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
		dashboardsPage.addNewDashboard("Scatter Explorer 4");
		browser.navigate().refresh(); // after creating the new dashboard, the
										// browser still keeps the iframe of the
										// previous scatter widget
										// refresh browser to remove the iframe
										// of the old scatter widget
		Thread.sleep(2000);
		dashboardsPage.editDashboard();
		dashboardEditBar.addScatterWidgetToDashboard(data);
		dashboardEditBar.addColorToScatterWidget(data);
		dashboardEditBar.addTimeFilterToDashboard(1, "last");
		dashboardEditBar.addListFilterToDashboard(DashFilterTypes.ATTRIBUTE, "Sales Rep");
		// check scatter widget is rendered well
		checkRedBar(browser);
		waitForElementVisible(by_iframe, browser);
		browser.switchTo().frame(browser.findElement(by_iframe));
		// check whether color legend is added to scatter widget
		By legendSeries = By
				.xpath("//div[@class='highcharts-container']/*[local-name()='svg' and namespace-uri()='http://www.w3.org/2000/svg']/*[name()='g' and @class='highcharts-legend']//*[name()='g' and @class='highcharts-legend-item']");
		List<WebElement> series = browser.findElements(legendSeries);
		Assert.assertEquals(series.size(), 19, "Color legend is not added to Scatter chart");
		// check the second column header is OWNER NAME
		By tableHeaderColumn2 = By
				.xpath("//div[contains(@class,'dda-table-view')]//div[contains(@class,'ember-table-header-row')]/div/div[2]//span");
		Assert.assertEquals(browser.findElement(tableHeaderColumn2).getText(), "OWNER NAME",
				"OWNER NAME column is not added to table");
		browser.switchTo().window(parentWindowHandle);
		dashboardEditBar.saveDashboard();
		FilterWidget filter = null;
		// check applying filter into scatter widget
		List<FilterWidget> filters = dashboardsPage.getFilters();
		for (Iterator<FilterWidget> iterator = filters.iterator(); iterator.hasNext();) {
			filter = iterator.next();
			if (filter.getRoot().getAttribute("class").contains("s-filter-list")) {
				break;
			}
		}
		// FilterWidget filter = filters.get(1);
		filter.openPanel();
		FilterPanel panel = filter.getPanel();
		panel.waitForValuesToLoad();
		List<FilterWidget.FilterPanel.FilterPanelRow> rows = panel.getRows();
		Actions actions = new Actions(browser);
		actions.moveToElement(rows.get(0).getRoot()).build().perform();
		// Select first value
		// Due to some weird black magic link does not react to clicks until it
		// is typed to
		rows.get(0).getSelectOnly().sendKeys("something");
		rows.get(0).getSelectOnly().click();
		waitForElementVisible(panel.getApply()).click();
		Thread.sleep(4000);
		waitForElementVisible(by_iframe, browser);
		browser.switchTo().frame(browser.findElement(by_iframe));
		series = browser.findElements(legendSeries);
		Assert.assertEquals(series.size(), 1, "Attribute filter is not applied to scatter");
		browser.switchTo().window(parentWindowHandle);
		// check filtering out all values on scatter widget
		By timeFilterButton = By
				.xpath("//div[contains(@class,'yui3-c-tabfilteritem')]//span[text()='Date dimension (Closed)']/../../../button");
		By timeLineLocator = By.xpath("//div[text()='2014']");
		By applyButton = By
				.xpath("//div[contains(@class,'bottomButtons')]//button[text()='Apply']");
		waitForElementVisible(timeFilterButton, browser).click();
		waitForElementVisible(timeLineLocator, browser).click();
		waitForElementVisible(applyButton, browser).click();
		Thread.sleep(3000);
		checkRedBar(browser);
		waitForElementVisible(by_iframe, browser);
		browser.switchTo().frame(browser.findElement(by_iframe));
		By noDataMessage = By.xpath("//div[@class='alert-title']");
		WebElement noDataMessageElement = waitForElementVisible(noDataMessage, browser);
		Assert.assertEquals(noDataMessageElement.getText().trim(), "No data",
				"Scatter widget is not filtered out values");
		browser.switchTo().window(parentWindowHandle);
		dashboardsPage.deleteDashboard();
	}

	@Test(dependsOnGroups = { "addAndEditScatterWidgetTest", "advancedScatterWidgetTest" }, groups = { "tests" })
	public void endOfTests() {
		successfulTest = true;
	}

}