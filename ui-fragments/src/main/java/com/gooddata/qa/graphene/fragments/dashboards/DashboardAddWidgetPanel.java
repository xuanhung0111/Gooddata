package com.gooddata.qa.graphene.fragments.dashboards;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.className;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

import com.gooddata.qa.graphene.enums.dashboard.WidgetTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class DashboardAddWidgetPanel extends AbstractFragment {

    public static final By LOCATOR = className("yui3-c-adddashboardwidgetpickerpanel-content");

    @FindBy(xpath = "//div[contains(@class, 'yui3-c-dashboardwidgetconfigpanel')]")
    private WebElement widgetConfigPanel;

    @FindBy(xpath = "//div[contains(@class, 'yui3-c-dashboardwidgetconfigpanel')]//div[contains(@class,'configPanel-views')]//button")
    private WebElement widgetConfigMetricButton;

    @FindBy(xpath = "//div[contains(@class, 'yui3-c-dashboardwidgetconfigpanel')]//button[contains(@class,'s-btn-apply')]")
    private WebElement widgetConfigApplyButton;

    @FindBy(xpath = "//div[contains(@class, 'attrs-container')]")
    private WebElement layers;

    @FindBy(xpath = "//div[contains(@class, 'c-geoConfiguration')]//span[@class='attr-inside']")
    private List<WebElement> listGeoLayer;

    @FindBy(css = ".c-geoConfiguration input")
    private List<WebElement> listGeoLayerCheckbox;

    private static final String widgetLocator =
            "//div[contains(@class,'yui3-c-adddashboardwidgetpickerpanel')]//div[contains(@class,'add-dashboard-item')]/div[contains(text(), '${widgetLabel}')]/../button";
    private static final String widgetMetricLocator =
            "//div[contains(@class,'yui3-widget-stacked shelterPlugin-plugged')]//div[contains(@class,'yui3-c-picker-content')]//div[contains(@class,'yui3-c-simplecolumn')]//div[contains(@class,'c-label') and contains(@class,'s-enabled')]/span[text()='${metricLabel}']";

    public void initWidget(WidgetTypes type) {
        By widgetType = By.xpath(widgetLocator.replace("${widgetLabel}", type.getLabel()));
        waitForElementVisible(widgetType, browser).click();
    }

    public void initWidget(WidgetTypes type, String metricLabel) {
        initWidget(type);
        // TODO fragments for widget config panel + metric selection can be used
        // - but better IDs and UI organization is required
        waitForElementVisible(widgetConfigPanel);
        waitForElementVisible(widgetConfigMetricButton).click();
        By metricInWidget = By.xpath(widgetMetricLocator.replace("${metricLabel}", metricLabel));
        waitForElementVisible(metricInWidget, browser).click();
        sleepTightInSeconds(3);
    }

    public void addWidget(WidgetTypes type, String metricLabel) {
        initWidget(type, metricLabel);
        waitForElementVisible(widgetConfigApplyButton).click();
        waitForElementNotVisible(widgetConfigPanel);
    }

    public List<String> getGeoLayersList() {
        List<String> layersList = new ArrayList<String>();
        for (WebElement element : listGeoLayer) {
            layersList.add(element.getText());
        }
        return layersList;
    }

    public void verifyLayersList(String metricLabel, List<String> layersList) {
        initWidget(WidgetTypes.GEO_CHART, metricLabel);
        waitForElementVisible(layers);

        if (layersList.isEmpty()) {
            WebElement noLayerElement = waitForElementVisible(cssSelector(".geo-list .no-attrs"), browser);
            assertTrue(noLayerElement.getText().startsWith("No layers available for selected metric."));
            assertEquals(noLayerElement.findElement(cssSelector("a[href]")).getAttribute("href"),
                    "https://help.gooddata.com/display/doc/Setting+up+Data+for+Geo+Charts");
            return;
        }

        List<String> actualLayersList = getGeoLayersList();
        for (String layer : layersList) {
            int matchingLayer = 0;
            for (String actualLayer : actualLayersList) {
                if (layer.equalsIgnoreCase(actualLayer)) {
                    matchingLayer++;
                }
            }
            Assert.assertTrue(matchingLayer > 0,
                    String.format("Layer %s is NOT visible on the list", layer));
        }
    }

    public void addGeoChart(String metricLabel, String attributeLayer) {
        initWidget(WidgetTypes.GEO_CHART, metricLabel);
        waitForElementVisible(layers);

        if (!listGeoLayerCheckbox.isEmpty()) {
            int i = 0;
            listGeoLayerCheckbox.get(0).click();
            sleepTightInSeconds(3);
            for (WebElement element : listGeoLayerCheckbox) {
                if (listGeoLayer.get(i).getText().equalsIgnoreCase(attributeLayer)) {
                    element.click();
                    break;
                }
                i++;
            }
        }

        waitForElementVisible(widgetConfigApplyButton).click();
        waitForElementNotVisible(widgetConfigPanel);
    }
}
