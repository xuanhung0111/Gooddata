package com.gooddata.qa.graphene.fragments.dashboards;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.reports.ReportPage;

import static com.gooddata.qa.graphene.utils.CheckUtils.*;

public class DashboardGeoChart extends AbstractFragment {

    @FindBy(css="div.geolegend-metricName")
    private WebElement metricName;

    @FindBy(css = "div.geolegend-start")
    private WebElement startValueDiv;

    @FindBy(css = "div.geolegend-stop")
    private WebElement stopValueDiv;

    @FindBy(css = "div.gradientView-inner")
    private WebElement colorGradientView;

    @FindBy(css = "path.leaflet-clickable")
    private List<WebElement> svgPathList;

    public void verifyGeoChart(String layerName, String metricLabel, float expectedStartValue,
            float expectedStopValue, List<Integer> indexList,
            List<String> expectedColorList, List<String> expectedSvgDataList,
            List<String> expectedMetricValuesList, List<String> expectedAttrValuesList) throws InterruptedException {
        // Verify color rank based on metric values
        browser.navigate().refresh();
        waitForElementVisible(metricName); 
        Assert.assertEquals(metricName.getText(), metricLabel, "Metric label of GEO is not properly in Geo.");
        waitForElementVisible(startValueDiv);
        Assert.assertEquals(ReportPage.getNumber(startValueDiv.getText()), expectedStartValue, "Start metric value of GEO is not properly in Geo.");
        waitForElementVisible(stopValueDiv);
        Assert.assertEquals(ReportPage.getNumber(stopValueDiv.getText()), expectedStopValue, "Stop metric value of GEO is not properly in Geo.");
        waitForElementVisible(colorGradientView);
        Assert.assertEquals(colorGradientView.getAttribute("style"), "background: -moz-linear-gradient(0px 50% , rgb(230, 230, 230), rgb(43, 107, 174)) repeat scroll 0% 0% transparent;");
        // Verify map area on GEO
        List<String> actualColorList = new ArrayList<String>();
        for (int i : indexList) {
            waitForElementVisible(svgPathList.get(i));
            actualColorList.add(svgPathList.get(i).getCssValue("fill"));
        }
        Assert.assertEquals(actualColorList, expectedColorList, "Color of all layers is not properly.");
        List<String> actualSvgDataList = new ArrayList<String>();
        for (int i : indexList) {
            waitForElementVisible(svgPathList.get(i));
            actualSvgDataList.add(svgPathList.get(i).getAttribute("d"));
        }
        Assert.assertEquals(actualSvgDataList, expectedSvgDataList, "Layers on GEO are not properly.");
        // Verify label (name + metric value) in tool-tip
        int j = 0;
        for (int i : indexList) {
            Actions action = new Actions(browser);
            action.moveToElement(svgPathList.get(i)).build().perform();
            String script = "return document.getElementsByClassName('yui3-bubble-content')[0].getElementsByClassName('content')[0].innerHTML";
            String actualTooltipValues = (String) ((JavascriptExecutor) browser).executeScript(script);
            String expectedTooltipValues = "Sum of Amount:&nbsp;<b>" + expectedMetricValuesList.get(j) + "</b><br>" + layerName + ":&nbsp;<b>" + expectedAttrValuesList.get(j) + "</b><br>";            
            Assert.assertEquals(actualTooltipValues, expectedTooltipValues, "Tooltip values on GEO is not properly.");
            j++;
        }
    }
}
