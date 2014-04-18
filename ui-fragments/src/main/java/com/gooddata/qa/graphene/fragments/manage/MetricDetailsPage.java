package com.gooddata.qa.graphene.fragments.manage;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class MetricDetailsPage extends AbstractFragment {

    @FindBy(xpath = "//div[contains(@class,'MAQLDocumentationContainer')]")
    private WebElement maql;

    @FindBy(xpath = "//span[contains(@class,'metric_format')]")
    private WebElement metricFormat;

    public String getMAQL(String metricName) {
	String maqlValue = waitForElementVisible(maql).getText();
	return maqlValue;
    }

    public String getMetricFormat(String metricName) {
	String format = waitForElementVisible(metricFormat).getText();
	return format;
    }
}
