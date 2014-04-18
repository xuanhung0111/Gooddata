package com.gooddata.qa.graphene.fragments.manage;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.fragments.AbstractTable;

public class MetricsTable extends AbstractTable {

    public static final By BY_METRIC_LINK = By.cssSelector("td.title a");
    public static final By BY_METRIC_DETAIL_PAGE = By
	    .xpath("//div[@id='p-objectPage' and contains(@class,'s-displayed')]");

    public boolean selectMetric(String metricName) {
	for (int i = 0; i < getNumberOfRows(); i++) {
	    WebElement row = waitForElementVisible(rows.get(i));
	    WebElement link = row.findElement(BY_METRIC_LINK);
	    if (link.getText().equals(metricName)) {
		link.click();
		waitForElementVisible(BY_METRIC_DETAIL_PAGE);
		return true;
	    }
	}
	return false;
    }
}
