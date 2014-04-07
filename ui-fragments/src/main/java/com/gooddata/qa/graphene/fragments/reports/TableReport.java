package com.gooddata.qa.graphene.fragments.reports;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class TableReport extends AbstractReport {
	
	@FindBy(xpath = "//div[@class='containerBody']/div[4]/div[@class='gridTabPlate']/div[@class='gridTile']/div[contains(@class,'element')]")
	private List<WebElement> attributeElementInGrid;
	
	@FindBy(xpath = "//div[@class='containerBody']/div[5]/div[@class='gridTabPlate']/div[@class='gridTile']/div[contains(@class,'data')]")
	private List<WebElement> metricValuesInGrid;
	
	public List<String> getAttributeInGrid(){
		int length =  attributeElementInGrid.size();
		List<String> attributeElements = new ArrayList<String>();
		for (int i = 0; i< length; i++){
			String tmp = attributeElementInGrid.get(i).getAttribute("title");
			attributeElements.add(tmp);
		}
		return attributeElements;
	}
	
	public List<Float> getMetricInGrid(){
		int length =  metricValuesInGrid.size();
		System.out.println("Metric values in grid = " + length);
		List<Float> metricValues = new ArrayList<Float>();
		for (int i = 0; i< length; i++){
			float tmp = ReportPage.getNumber(metricValuesInGrid.get(i).getAttribute("title"));
			metricValues.add(tmp);
		}
		return metricValues;
	}
}
