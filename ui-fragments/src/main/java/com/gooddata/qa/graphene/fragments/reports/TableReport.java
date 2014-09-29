package com.gooddata.qa.graphene.fragments.reports;

import static org.testng.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class TableReport extends AbstractReport {

    @FindBy(xpath = "//div[@class='containerBody']/div[4]/div[@class='gridTabPlate']/div[@class='gridTile']/div[contains(@class,'element')]/span")
    private List<WebElement> attributeElementInGrid;

    @FindBy(xpath = "//div[@class='containerBody']/div[5]/div[@class='gridTabPlate']/div[@class='gridTile']/div[contains(@class,'data')]")
    private List<WebElement> metricValuesInGrid;

    @FindBy(xpath = "//div[@id='gridContainerTab']//div[contains(@class,'drillable')]")
    private List<WebElement> drillableElements;

    @FindBy(xpath = "//div[@id='analysisAttributesContainer']//span[contains(@class,'captionWrapper')]")
    private List<WebElement> attributesHeader;

    public List<String> getAttributesHeader() {
        List<String> attributes = new ArrayList<String>();
        for (int i = 0; i < attributesHeader.size(); i++) {
            String tmp = attributesHeader.get(i).getText();
            attributes.add(tmp);
        }
        return attributes;
    }

    public List<String> getAttributeElements() {
	List<String> attributeElements = new ArrayList<String>();
	for (int i = 0; i < attributeElementInGrid.size(); i++) {
	    String tmp = attributeElementInGrid.get(i).getText();
	    attributeElements.add(tmp);
	}
	return attributeElements;
    }

    public List<Float> getMetricElements() {
	List<Float> metricValues = new ArrayList<Float>();
	for (int i = 0; i < metricValuesInGrid.size(); i++) {
	    float tmp = ReportPage.getNumber(metricValuesInGrid.get(i)
		    .getAttribute("title"));
	    metricValues.add(tmp);
	}
	return metricValues;
    }

    public void verifyAttributeIsHyperlinkInReport() {
	String drillToHyperLinkElement = "Open external link in a new window";
	assertTrue(drillableElements.size() > 0,
		"Attribute is NOT drillable");
	for (WebElement element : drillableElements) {
	    assertTrue(waitForElementVisible(element).getAttribute("title")
		    .indexOf(drillToHyperLinkElement) > -1,
		    "Some of elements are NOT drillable to external link!");
	}
    }
}
