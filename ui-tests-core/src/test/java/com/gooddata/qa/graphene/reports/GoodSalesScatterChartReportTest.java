package com.gooddata.qa.graphene.reports;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTight;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.dashboard.DashFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEmbedDialog;
import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.AttributeFilterPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.FilterPanelRow;
import com.gooddata.qa.graphene.utils.Sleeper;
import com.gooddata.qa.utils.CssUtils;
import com.gooddata.qa.utils.graphene.Screenshots;

public class GoodSalesScatterChartReportTest extends GoodSalesAbstractTest {

    private static final By BY_IFRAME_SCATTER = By.xpath("//iframe[contains(@src,'iaa/scatter')]");
    private static final long expectedDashboardExportSize = 42000;

    private static final By dataPoints =
            By.xpath("//*[local-name()='svg' and namespace-uri()='http://www.w3.org/2000/svg']/*[name()='g' and @class='highcharts-series-group']//*[name() = 'path']");
    private static final By nameTooltip = By
            .xpath("//div[@class='highcharts-tooltip']//div[@class='tt-name']");
    private static final By contentTooltip =
            By.xpath("//div[@class='highcharts-tooltip']//table[@class='tt-values']//tr/td[@class='title']");
    private static final By tableStatus =
            By.xpath("//div[contains(@class,'ember-table-footer-container')]//div[contains(@class,'text-align-left')][1]/span");
    private static final By legendSeries =
            By.xpath("//*[local-name()='svg' and namespace-uri()='http://www.w3.org/2000/svg']/*[name()='g' and @class='highcharts-legend']//*[name()='g' and @class='highcharts-legend-item']");
    private static final By tableHeaderColumns =
            By.xpath("//div[contains(@class,'dda-table-view')]//div[contains(@class,'ember-table-header-row')]/div//span");
    private static final By scatterTitleInEditMode = By
            .xpath("//div[@class='title']//label[contains(@class,'editable-label')]");
    private static final By scatterSubtitleInEditMode = By
            .xpath("//div[@class='subtitle']//label[contains(@class,'editable-label')]");
    private static final By scatterTitleInput = By.xpath("//div[@class='title']//input");
    private static final By scatterSubtitleInput = By.xpath("//div[@class='subtitle']//input");
    private static final By scatterTitleInViewMode = By.xpath("//div[@class='title']");
    private static final By scatterSubTitleInViewMode = By.xpath("//div[@class='subtitle']");
    private static final By alertMessage = By
            .xpath("//div[contains(@class,'scatter-component')]//div[@class='alert-title']");
    private static final By invalidConfigurationAlert = By
            .xpath("//div[contains(@class,'gd-dashboard')]//div[@class='explorer-message-title']");
    private static final By timeFilterButton =
            By.xpath("//div[contains(@class,'yui3-c-tabfilteritem')]//span[text()='Date dimension (Closed)']/../../../button");
    private static final By timeLineLocator = By.xpath("//div[text()='2014']");
    private static final By applyButton = By
            .xpath("//div[contains(@class,'bottomButtons')]//button[text()='Apply']");
    private static final By noDataMessage = By.xpath("//div[@class='alert-title']");

    private static final String SCATTER_DASHBOARD = "Scatter Explorer"; 

    @Test(dependsOnMethods = {"createProject"}, groups = {"addAndEditScatterWidgetTest"})
    public void addScatterWidgetTest() {
        Map<String, String> data = new HashMap<String, String>();
        data.put("dataPointFolder", "Stage");
        data.put("dataPoint", "Stage Name");
        data.put("xAxisMetricFolder", "Sales Figures");
        data.put("xAxisMetric", "Amount");
        data.put("yAxisMetricFolder", "Sales Figures");
        data.put("yAxisMetric", "Avg. Amount");
        initDashboardsPage();
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        dashboardsPage.addNewDashboard(SCATTER_DASHBOARD);
        sleepTight(2000);
        dashboardsPage.editDashboard();
        sleepTight(2000);
        dashboardEditBar.addScatterWidgetToDashboard(data);
        sleepTight(2000);
        Screenshots.takeScreenshot(browser, "scatter-explorer", this.getClass());
        dashboardEditBar.saveDashboard();
        sleepTight(2000);
        testScatterWidgetDisplaying();
    }

    @Test(dependsOnMethods = {"addScatterWidgetTest"}, priority = 2,
            groups = {"addAndEditScatterWidgetTest"})
    public void addColorSeriesTest() {
        Map<String, String> data = new HashMap<String, String>();
        data.put("colorAttributeFolder", "Sales Rep");
        data.put("colorAttribute", "Department");
        sleepTight(2000);
        initDashboardsPage();
        dashboardsPage.selectDashboard(SCATTER_DASHBOARD);
        dashboardsPage.editDashboard();
        sleepTight(2000);
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        dashboardEditBar.addColorToScatterWidget(data);
        sleepTight(2000);
        Screenshots.takeScreenshot(browser, "scatter-explorer-added-color-series", this.getClass());
        String parentWindowHandle = browser.getWindowHandle();
        switchToScatterFrame();
        // check whether color legend is added to scatter widget
        List<WebElement> series = browser.findElements(legendSeries);
        assertEquals(series.size(), 2, "Color legend is not added to Scatter chart");
        // check the second column header is STATUS
        List<WebElement> tableHeaderColumnElements = browser.findElements(tableHeaderColumns);
        assertEquals(tableHeaderColumnElements.get(1).getText(), "DEPARTMENT",
                "Color column is not added to table");
        browser.switchTo().window(parentWindowHandle);
        dashboardEditBar.saveDashboard();
        // hover on data point
        switchToScatterFrame();
        // check if dataPoints has been rendered
        waitForElementVisible(dataPoints, browser);
        List<WebElement> dataPointElements = browser.findElements(dataPoints);
        WebElement selectedDataPointElement = dataPointElements.get(7);
        Actions builder = new Actions(browser);
        Actions hoverOverDataPoint = builder.moveToElement(selectedDataPointElement);
        hoverOverDataPoint.perform();
        assertTrue(waitForElementVisible(nameTooltip, browser).isDisplayed(),
                "Attribute name is not displayed");
        assertEquals(waitForElementVisible(nameTooltip, browser).getText(), "STAGE NAME");
        assertTrue(waitForElementVisible(contentTooltip, browser).isDisplayed(),
                "Tooltip name is not displayed");
        assertEquals(browser.findElements(contentTooltip).get(0).getText(), "Department");
        assertEquals(browser.findElements(contentTooltip).get(1).getText(), "Amount");
        assertEquals(browser.findElements(contentTooltip).get(2).getText(), "Avg. Amount");
        // click on one data point to make other data points become gray and
        // explorer table is selected correspondingly
        WebElement tableStatusElement = browser.findElement(tableStatus);
        selectedDataPointElement.click();
        sleepTight(2000);
        assertEquals(selectedDataPointElement.getAttribute("fill"), "rgb(77,133,255)");
        for (Iterator<WebElement> iterator = dataPointElements.iterator(); iterator.hasNext();) {
            WebElement dataPointElement = (WebElement) iterator.next();
            if (!dataPointElement.equals(selectedDataPointElement)) {
                assertEquals(dataPointElement.getAttribute("fill"), "rgb(194,194,194)");
                System.out.println(dataPointElement.getLocation() + " is gray");
            }
        }
        assertEquals(tableStatusElement.getText(), "1 selected out of 16 total",
                "Table does not contain data of the selected data point");
        browser.switchTo().window(parentWindowHandle);
        // disable color
        dashboardsPage.editDashboard();
        sleepTight(2000);
        dashboardEditBar.disableColorInScatterWidget();
        sleepTight(2000);
        switchToScatterFrame();
        // check whether color legend is removed from scatter widget
        series = browser.findElements(legendSeries);
        assertEquals(series.size(), 1, "Color legend is not removed from Scatter chart");
        // check the second column header is no longer STATUS
        tableHeaderColumnElements = browser.findElements(tableHeaderColumns);
        assertNotEquals(tableHeaderColumnElements.get(1).getText(), "DEPARTMENT",
                "Color column header is not removed from table");
        browser.switchTo().window(parentWindowHandle);
        dashboardEditBar.saveDashboard();
        sleepTight(2000);
    }

    @Test(dependsOnMethods = {"addScatterWidgetTest"}, priority = 3,
            groups = {"addAndEditScatterWidgetTest"})
    public void addAdditionalColumnTest() {
        Map<String, ArrayList<HashMap<String, String>>> data =
                new HashMap<String, ArrayList<HashMap<String, String>>>();
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

        sleepTight(2000);
        initDashboardsPage();
        dashboardsPage.selectDashboard(SCATTER_DASHBOARD);
        dashboardsPage.editDashboard();
        sleepTight(1000);
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        dashboardEditBar.addColumnsToScatterWidget(data);
        Screenshots.takeScreenshot(browser, "scatter-explorer-added-additional-column",
                this.getClass());
        dashboardEditBar.saveDashboard();
        sleepTight(3000);
        String parentWindowHandle = browser.getWindowHandle();
        switchToScatterFrame();
        List<WebElement> tableHeaderColumnElements = browser.findElements(tableHeaderColumns);
        assertEquals(tableHeaderColumnElements.get(3).getText(), "PRODUCT NAME",
                "PRODUCT NAME column is not added to table");
        assertEquals(tableHeaderColumnElements.get(4).getText(), "EXPECTED",
                "EXPECTED column is not added to table");
        // remove additional columns
        browser.switchTo().window(parentWindowHandle);
        dashboardsPage.editDashboard();
        sleepTight(2000);
        dashboardEditBar.removeColumnsFromScatterWidget();
        sleepTight(2000);
        // check whether Product and Expected removed from table or not
        switchToScatterFrame();
        assertEquals(browser.findElements(tableHeaderColumns).size(), 3);
        browser.switchTo().window(parentWindowHandle);
        dashboardEditBar.saveDashboard();
        sleepTight(2000);
    }

    @Test(dependsOnMethods = {"addScatterWidgetTest"}, priority = 4,
            groups = {"addAndEditScatterWidgetTest"})
    public void changeScatterExplorerTitle() {
        String scatterTitleText = new String("New Scatter Explorer");
        String scatterSubtitleText = new String("this is a test for scatter explorer");
        String fakeScatterTitleText = new String("New Scatter Explorer 2");
        String fakeScatterSubtitleText = new String("this is a test for scatter explorer 2");

        sleepTight(2000);
        initDashboardsPage();
        dashboardsPage.selectDashboard(SCATTER_DASHBOARD);
        sleepTight(2000);
        dashboardsPage.editDashboard();
        sleepTight(3000);
        checkRedBar(browser);
        String parentWindowHandle = browser.getWindowHandle();
        // click on the scatter widget in EDIT mode to make it enable
        waitForElementVisible(BY_IFRAME_SCATTER, browser).click();
        browser.switchTo().frame(browser.findElement(BY_IFRAME_SCATTER));
        // hover on scatter title
        WebElement scatterTitleInEditModeElement =
                waitForElementVisible(scatterTitleInEditMode, browser);
        Actions builder = new Actions(browser);
        Actions hoverAction = builder.moveToElement(scatterTitleInEditModeElement);
        hoverAction.perform();
        assertEquals(scatterTitleInEditModeElement.getCssValue("background-color"),
                "rgba(255, 253, 198, 1)", "Scatter title is not highlighted when being hovered on");
        // hover on scatter subtitle
        WebElement scatterSubtitleInEditModeElement =
                waitForElementVisible(scatterSubtitleInEditMode, browser);
        hoverAction = builder.moveToElement(scatterSubtitleInEditModeElement);
        hoverAction.perform();
        assertEquals(scatterSubtitleInEditModeElement.getCssValue("background-color"),
                "rgba(255, 253, 198, 1)",
                "Scatter subtitle is not highlighted when being hovered on");
        scatterTitleInEditModeElement.click();
        WebElement scatterTitleInputElement = waitForElementVisible(scatterTitleInput, browser);
        scatterTitleInputElement.clear();
        scatterTitleInputElement.sendKeys(scatterTitleText);
        scatterSubtitleInEditModeElement.click();
        WebElement scatterSubtitleInputElement =
                waitForElementVisible(scatterSubtitleInput, browser);
        scatterSubtitleInputElement.clear();
        scatterSubtitleInputElement.sendKeys(scatterSubtitleText);
        browser.switchTo().window(parentWindowHandle);
        dashboardsPage.getDashboardEditBar().saveDashboard();
        Screenshots.takeScreenshot(browser, "change-scatter-explorer-name-and-description",
                this.getClass());
        sleepTight(3000);
        switchToScatterFrame();
        WebElement scatterTitleInViewModeElement =
                waitForElementVisible(scatterTitleInViewMode, browser);
        WebElement scatterSubtitleInViewModeElement =
                waitForElementVisible(scatterSubTitleInViewMode, browser);
        assertEquals(scatterTitleInViewModeElement.getText(), scatterTitleText,
                "Scatter Title is not updated");
        assertEquals(scatterSubtitleInViewModeElement.getText(), scatterSubtitleText,
                "Scatter Subtitle is not updated");
        browser.switchTo().window(parentWindowHandle);
        // check editing scatter title/subtitle then cancel
        dashboardsPage.editDashboard();
        sleepTight(2000);
        // click on the scatter widget in EDIT mode to make it enable
        waitForElementVisible(BY_IFRAME_SCATTER, browser).click();
        browser.switchTo().frame(browser.findElement(BY_IFRAME_SCATTER));
        scatterTitleInEditModeElement = waitForElementVisible(scatterTitleInEditMode, browser);
        scatterSubtitleInEditModeElement =
                waitForElementVisible(scatterSubtitleInEditMode, browser);
        scatterTitleInEditModeElement.click();
        scatterTitleInputElement = waitForElementVisible(scatterTitleInput, browser);
        scatterTitleInputElement.clear();
        scatterTitleInputElement.sendKeys(fakeScatterTitleText);
        scatterSubtitleInEditModeElement.click();
        scatterSubtitleInputElement = waitForElementVisible(scatterSubtitleInput, browser);
        scatterSubtitleInputElement.clear();
        scatterSubtitleInputElement.sendKeys(fakeScatterSubtitleText);
        browser.switchTo().window(parentWindowHandle);
        dashboardsPage.getDashboardEditBar().cancelDashboard();
        sleepTight(2000);
        switchToScatterFrame();
        scatterTitleInViewModeElement = waitForElementVisible(scatterTitleInViewMode, browser);
        scatterSubtitleInViewModeElement =
                waitForElementVisible(scatterSubTitleInViewMode, browser);
        Screenshots.takeScreenshot(browser,
                "cancel-changing-scatter-explorer-name-and-description", this.getClass());
        assertEquals(scatterTitleInViewModeElement.getText(), scatterTitleText,
                "Scatter Title is changed in CANCEL action");
        assertEquals(scatterSubtitleInViewModeElement.getText(), scatterSubtitleText,
                "Scatter Subtitle is changed in CANCEL action");
    }

    @Test(dependsOnMethods = {"addScatterWidgetTest"}, priority = 5,
            groups = {"addAndEditScatterWidgetTest"})
    public void shareScatterExplorerTest() {
        sleepTight(2000);
        initDashboardsPage();
        DashboardEmbedDialog dialog = dashboardsPage.embedDashboard();
        String uri = dialog.getPreviewURI();
        browser.navigate().to(uri);
        waitForElementVisible(BY_IFRAME_SCATTER, browser);
        sleepTight(2000);
        Screenshots.takeScreenshot(browser, "share-scatter-explorer", this.getClass());
        testScatterWidgetDisplaying();
    }

    @Test(dependsOnMethods = {"addScatterWidgetTest"}, priority = 6,
            groups = {"addAndEditScatterWidgetTest"})
    public void exportDashboardContainingScatterExplorerTest() {
        sleepTight(2000);
        initDashboardsPage();
        String exportedDashboardName = dashboardsPage.exportDashboardTab(0);
        verifyDashboardExport(exportedDashboardName, expectedDashboardExportSize);
    }

    @Test(dependsOnGroups = {"addAndEditScatterWidgetTest"}, priority = 1,
            groups = {"advancedScatterWidgetTest"})
    public void addScatterWidgetWithTooManyDataPointsTest() {
        Map<String, String> data = new HashMap<String, String>();
        data.put("dataPointFolder", "Account");
        data.put("dataPoint", "Account");
        data.put("xAxisMetricFolder", "Sales Figures");
        data.put("xAxisMetric", "Amount");
        data.put("yAxisMetricFolder", "Sales Figures");
        data.put("yAxisMetric", "Avg. Amount");
        String parentWindowHandle = browser.getWindowHandle();
        sleepTight(2000);
        initDashboardsPage();
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        dashboardsPage.addNewDashboard("Scatter Explorer 2");
        // after creating the new dashboard, the browser still keeps the iframe of the previous
        // scatter widget
        // refresh browser to remove the iframe of the old scatter widget
        browser.navigate().refresh();
        sleepTight(2000);
        dashboardsPage.editDashboard();
        sleepTight(2000);
        dashboardEditBar.addScatterWidgetToDashboard(data);
        sleepTight(2000);
        Screenshots.takeScreenshot(browser, "scatter-explorer-with-too-many-data-points",
                this.getClass());
        // check "too many data points" message
        switchToScatterFrame();
        WebElement alertMessageElement = waitForElementVisible(alertMessage, browser);
        assertEquals(alertMessageElement.getText().trim(), "Too many data points");
        browser.switchTo().window(parentWindowHandle);
        dashboardEditBar.saveDashboard();
        sleepTight(2000);
        // check this alert message still show after saving dashboard
        switchToScatterFrame();
        alertMessageElement = waitForElementVisible(alertMessage, browser);
        assertEquals(alertMessageElement.getText().trim(), "Too many data points");
    }

    @Test(dependsOnGroups = {"addAndEditScatterWidgetTest"}, priority = 2,
            groups = {"advancedScatterWidgetTest"})
    public void addScatterWidgetWithInvalidConfigurationTest() {
        Map<String, String> data = new HashMap<String, String>();
        data.put("dataPointFolder", "Stage History");
        data.put("dataPoint", "Stage History");
        data.put("xAxisMetricFolder", "Sales Figures");
        data.put("xAxisMetric", "Amount");
        data.put("yAxisMetricFolder", "Sales Figures");
        data.put("yAxisMetric", "Avg. Amount");
        String parentWindowHandle = browser.getWindowHandle();

        initDashboardsPage();
        sleepTight(2000);
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        dashboardsPage.addNewDashboard("Scatter Explorer 3");
        // after creating the new dashboard, the browser still keeps the iframe of the previous
        // scatter widget
        // refresh browser to remove the iframe of the old scatter widget
        browser.navigate().refresh();
        sleepTight(2000);
        dashboardsPage.editDashboard();
        sleepTight(2000);
        dashboardEditBar.addScatterWidgetToDashboard(data, true);
        sleepTight(2000);
        Screenshots.takeScreenshot(browser, "scatter-explorer-with-invalid-configuration",
                this.getClass());
        // check "invalid configuration" message
        switchToScatterFrame();
        WebElement invalidConfigurationAlertElement =
                waitForElementVisible(invalidConfigurationAlert, browser);
        assertEquals(invalidConfigurationAlertElement.getText().trim(), "Invalid configuration");
        browser.switchTo().window(parentWindowHandle);
        dashboardEditBar.saveDashboard();
        sleepTight(2000);
        // check this alert message still show after saving dashboard
        switchToScatterFrame();
        invalidConfigurationAlertElement =
                waitForElementVisible(invalidConfigurationAlert, browser);
        assertEquals(invalidConfigurationAlertElement.getText().trim(), "Invalid configuration");
    }

    @Test(dependsOnGroups = {"addAndEditScatterWidgetTest"}, priority = 3,
            groups = {"advancedScatterWidgetTest"})
    public void applyFilterOnScatterWidgetTest() {
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

        sleepTight(2000);
        initDashboardsPage();
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        dashboardsPage.addNewDashboard("Scatter Explorer 4");
        // after creating the new dashboard, the browser still keeps the iframe of the previous
        // scatter widget
        // refresh browser to remove the iframe of the old scatter widget
        browser.navigate().refresh();
        sleepTight(2000);
        dashboardsPage.editDashboard();
        sleepTight(2000);
        dashboardEditBar.addScatterWidgetToDashboard(data);
        sleepTight(2000);
        dashboardEditBar.addColorToScatterWidget(data);
        sleepTight(2000);
        dashboardEditBar.addTimeFilterToDashboard(1, "last");
        sleepTight(2000);

        String fromDate = "01/01/2013";
        String toDate = "12/30/2013";
        dashboardEditBar.getDashboardEditFilter().changeTimeFilterByEnterFromDateToDate(fromDate, toDate);

        sleepTight(2000);
        dashboardEditBar.addListFilterToDashboard(DashFilterTypes.ATTRIBUTE, "Sales Rep");
        sleepTight(2000);
        // check scatter widget is rendered well
        checkRedBar(browser);
        switchToScatterFrame();
        // check whether color legend is added to scatter widget
        List<WebElement> series = browser.findElements(legendSeries);
        assertEquals(series.size(), 19, "Color legend is not added to Scatter chart");
        // check the second column header is OWNER NAME
        List<WebElement> tableHeaderColumnElements = browser.findElements(tableHeaderColumns);
        assertEquals(tableHeaderColumnElements.get(1).getText(), "OWNER NAME",
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
        filter.openPanel();
        AttributeFilterPanel panel = filter.getPanel(AttributeFilterPanel.class);
        panel.waitForValuesToLoad();
        List<FilterPanelRow> rows = panel.getRows();
        Actions actions = new Actions(browser);
        actions.moveToElement(rows.get(0).getRoot()).build().perform();
        sleepTight(1000);
        // Select first value
        // Due to some weird black magic link does not react to clicks until it is typed to
        WebElement selectedOnly = rows.get(0).getSelectOnly();
        assertEquals("only", selectedOnly.getText());
        selectedOnly.sendKeys("something");
        selectedOnly.click();
        panel.submit();
        sleepTight(4000);
        
        dashboardsPage.getContent().getFilterWidget(CssUtils.simplifyText("Date dimension (Closed)"))
                .changeTimeFilterByEnterFromAndToDate(fromDate, toDate);
        Sleeper.sleepTight(1000);

        Screenshots.takeScreenshot(browser, "scatter-explorer-applied-filter", this.getClass());
        switchToScatterFrame();
        series = browser.findElements(legendSeries);
        assertEquals(series.size(), 1, "Attribute filter is not applied to scatter");
        browser.switchTo().window(parentWindowHandle);
        // check filtering out all values on scatter widget
        waitForElementVisible(timeFilterButton, browser).click();
        waitForElementVisible(timeLineLocator, browser).click();
        waitForElementVisible(applyButton, browser).click();
        sleepTight(3000);
        checkRedBar(browser);
        Screenshots
                .takeScreenshot(browser, "scatter-explorer-filtered-out-values", this.getClass());
        switchToScatterFrame();
        WebElement noDataMessageElement = waitForElementVisible(noDataMessage, browser);
        assertEquals(noDataMessageElement.getText().trim(), "No data",
                "Scatter widget is not filtered out values");
    }

    private void testScatterWidgetDisplaying() {
        sleepTight(2000);
        // hover on data point
        switchToScatterFrame();
        waitForElementVisible(dataPoints, browser);
        List<WebElement> dataPointElements = browser.findElements(dataPoints);
        WebElement selectedDataPointElement = dataPointElements.get(7);
        Actions builder = new Actions(browser);
        Actions hoverOverDataPoint = builder.moveToElement(selectedDataPointElement);
        hoverOverDataPoint.perform();
        Screenshots.takeScreenshot(browser, "hover-on-one-data-point", this.getClass());
        assertTrue(waitForElementVisible(nameTooltip, browser).isDisplayed(),
                "Attribute name is not displayed");
        assertEquals(waitForElementVisible(nameTooltip, browser).getText(), "STAGE NAME");
        assertTrue(waitForElementVisible(contentTooltip, browser).isDisplayed(),
                "Axis metric name is not displayed");
        assertEquals(browser.findElements(contentTooltip).get(0).getText(), "Amount");
        assertEquals(browser.findElements(contentTooltip).get(1).getText(), "Avg. Amount");
        // click on one data point to make other data points become gray and
        // explorer table is selected correspondingly
        WebElement tableStatusElement = browser.findElement(tableStatus);
        for (WebElement dataPointElement : dataPointElements) {
            assertEquals(dataPointElement.getAttribute("fill"), "rgb(77,133,255)");
            System.out.println(dataPointElement.getLocation() + " is blue");
        }
        assertEquals(tableStatusElement.getText(), "8 total", "Table does not contain report data");
        selectedDataPointElement.click();
        assertEquals(selectedDataPointElement.getAttribute("fill"), "rgb(77,133,255)");
        for (WebElement dataPointElement : dataPointElements) {
            if (!dataPointElement.equals(selectedDataPointElement)) {
                assertEquals(dataPointElement.getAttribute("fill"), "rgb(194,194,194)");
                System.out.println(dataPointElement.getLocation() + " is gray");
            }
        }
        assertEquals(tableStatusElement.getText(), "1 selected out of 8 total",
                "Table does not contain data of the selected data point");
        // re-click on the data point to make all points back to normal
        selectedDataPointElement.click();
        for (WebElement dataPointElement : dataPointElements) {
            assertEquals(dataPointElement.getAttribute("fill"), "rgb(77,133,255)");
            System.out.println(dataPointElement.getLocation() + " is blue");
        }
        assertEquals(tableStatusElement.getText(), "8 total", "Table does not contain report data");
    }

    private void switchToScatterFrame() {
        waitForElementVisible(BY_IFRAME_SCATTER, browser);
        browser.switchTo().frame(browser.findElement(BY_IFRAME_SCATTER));
    }

}
