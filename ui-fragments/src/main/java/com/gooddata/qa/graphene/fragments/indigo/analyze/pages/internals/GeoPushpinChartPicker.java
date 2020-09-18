package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class GeoPushpinChartPicker extends AbstractFragment {

    private static final By GEO_PUSHPIN_CHART = className("s-gd-geo-component");
    private static final String GEO_TOOLTIP = "mapboxgl-popup-content";
    private static final String GEO_PUSHPIN_SIZE = "s-pushpin-size-legend";
    private static final String GEO_PUSHPIN_COLOR = "color-legend";
    private static final String GEO_PUSHPIN_CATEGORY = "s-geo-category-legend";
    private static final String ZOOM_IN_BTN = "mapboxgl-ctrl-zoom-in";
    private static final String ZOOM_OUT_BTN = "mapboxgl-ctrl-zoom-out";

    @FindBy(className = GEO_TOOLTIP)
    private WebElement geoPopupTooltip;

    @FindBy(className = "mapboxgl-canvas")
    private WebElement canvasGeoMap;

    @FindBy(className = ZOOM_IN_BTN)
    private WebElement zoomInBtn;

    @FindBy(className = ZOOM_OUT_BTN)
    private WebElement zoomOutBtn;

    @FindBy(className = GEO_PUSHPIN_SIZE)
    private WebElement pushpinSizeLegend;

    @FindBy(className = GEO_PUSHPIN_COLOR)
    private WebElement pushpinColorLegend;

    @FindBy(className = GEO_PUSHPIN_CATEGORY)
    private WebElement pushpinCategoryLegend;

    @FindBy(className = "s-dash-item-action-placeholder")
    private WebElement exportCsvAndXlsxIcon;

    @FindBy(className = "s-options-menu-export-xlsx")
    private WebElement exportXlsxFile;

    @FindBy(className = "s-options-menu-export-csv")
    private WebElement exportCsvFile;

    @FindBy(className = "s-geo-legend")
    private WebElement geoLegend;

    @FindBy(className = "circle-max-icon")
    private WebElement maxCircleIcon;

    public boolean isPushpinSizeLegendVisible() {
        return isElementVisible(className(GEO_PUSHPIN_SIZE), browser);
    }

    public boolean isPushpinColorLegendVisible() {
        return isElementVisible(className(GEO_PUSHPIN_COLOR), browser);
    }

    public boolean isPushpinCategoryLegendVisible() {
        return isElementVisible(className(GEO_PUSHPIN_CATEGORY), browser);
    }

    public boolean isGeoRenderChartDisplayed() {
        return isElementVisible(className("s-gd-geo-chart-renderer"), browser);
    }

    public boolean isGeoLegendDisplayed() {
        return isElementVisible(className("s-geo-legend"), browser);
    }

    public String getAttributeGeoLegend() {
        return  waitForElementPresent(geoLegend).getAttribute("class");
    }

    public static GeoPushpinChartPicker getInstance(SearchContext context) {
        return Graphene.createPageFragment(GeoPushpinChartPicker.class,
                waitForElementVisible(GEO_PUSHPIN_CHART, context));
    }

    public boolean isZoomInAndZoomOutDisplayed() {
        return isElementVisible(className(ZOOM_IN_BTN), browser) && isElementVisible(className(ZOOM_OUT_BTN), browser);
    }

    public List<String> getGeoPopupTooltip() {
        return waitForElementPresent(geoPopupTooltip)
                .findElements(By.className("gd-viz-tooltip-title")).stream().map(el -> el.getText())
                .collect(Collectors.toList());
    }

    public List<String> getGeoPopupTooltipValue() {
        return waitForElementPresent(geoPopupTooltip)
                .findElements(By.className("gd-viz-tooltip-value")).stream().map(el -> el.getText())
                .collect(Collectors.toList());
    }

    public Map<String, String> getPopupTooltipDetails() {
        Map<String, String> detailTooltip = new HashMap<>();
        waitForElementPresent(geoPopupTooltip)
                .findElements(By.className("gd-viz-tooltip-item")).stream().forEach(el -> {
            String title = el.findElement(By.className("gd-viz-tooltip-title")).getText();
            String value = el.findElement(By.className("gd-viz-tooltip-value")).getText();
            detailTooltip.put(title, value);
        });
        return detailTooltip;
    }

    public List<String> getListLablesColorPushpinLegend() {
        return waitForElementPresent(pushpinColorLegend)
                .findElements(By.cssSelector(".color-legend .labels span")).stream()
                .filter(el -> !el.getAttribute("style").contains("width: 10px;"))
                .map(el -> el.getText())
                .collect(Collectors.toList());
    }

    public List<String> getListBackgroundColorPushpinLegend() {
        return waitForElementPresent(pushpinColorLegend)
                .findElements(By.cssSelector(".color-legend .boxes span")).stream()
                .map(el -> el.getAttribute("style").replace("background-color: rgba", "")
                        .replace("; border: none;", ""))
                .collect(Collectors.toList());
    }

    public GeoPushpinChartPicker clickFilterSegmentOnPushpinLegend(String segment) {
        getSegmentFilterOnCategoryLegend().stream().filter(el -> el.getText().equals(segment)).findFirst().get().click();
        return this;
    }

    public String getColorFilterSegmentOnPushpinLegend(String segment) {
        WebElement element = getSegmentFilterOnCategoryLegend().stream().filter(el -> el.getText().equals(segment))
                .findFirst().get();
        return element.getAttribute("style").replace(";", "");
    }

    public List<String> getListIconPushpinSizeLegend() {
        return waitForElementPresent(pushpinSizeLegend)
                .findElements(By.cssSelector(".pushpin-size-legend-circle span")).stream()
                .map(el -> el.getAttribute("class")).collect(Collectors.toList());
    }

    public List<String> getAttrFilterOnPushpinLegend() {
        return getSegmentFilterOnCategoryLegend().stream().map(el -> el.getText()).collect(Collectors.toList());
    }

    public List<WebElement> getSegmentFilterOnCategoryLegend() {
        return waitForElementPresent(pushpinCategoryLegend)
                .findElements(By.cssSelector(".series .series-name"));
    }

    public boolean isGeoPopupTooltipDisplayed() {
        return isElementVisible(className(GEO_TOOLTIP), browser);
    }

    public WebElement returnCanvasChart() {
        return canvasGeoMap;
    }

    public GeoPushpinChartPicker hoverOnGeoPushpin(int xOffset, int yOffset) {
        getActions().moveToElement(canvasGeoMap, xOffset, yOffset).perform();
        return this;
    }

    public GeoPushpinChartPicker doubleClickOnZoomInBtn() {
        waitForElementVisible(zoomInBtn).click();
        waitForElementVisible(zoomInBtn).click();
        return this;
    }

    public GeoPushpinChartPicker hoverOnCanvasMapToExportFile() {
        getActions().moveToElement(canvasGeoMap).build().perform();
        return this;
    }

    public GeoPushpinChartPicker doubleClickOnZoomOutBtn() {
        waitForElementVisible(zoomOutBtn).click();
        waitForElementVisible(zoomOutBtn).click();
        return this;
    }

    public GeoPushpinChartPicker hoverAndClickOnGeoPushpin(int xOffset, int yOffset) {
        getActions().moveToElement(canvasGeoMap, xOffset, yOffset).click().perform();
        return this;
    }
}
