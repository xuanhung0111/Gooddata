package com.gooddata.qa.graphene.fragments.reports;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class ReportFilter extends AbstractFragment {

	private By addFilterButton = By.cssSelector(".s-btn-add_filter");

	@FindBy(xpath = "//div[contains(@class,'newFilterPicker')]")
	private WebElement filterPicker;

	@FindBy(css = ".s-attributeFilter")
	private WebElement attributeFilterLink;

	@FindBy(xpath = "//div[contains(@class,'yui3-c-simpleColumn-underlay')]/div[contains(@class,'c-checkBox')]")
	private WebElement listOfElementWithCheckbox;

	@FindBy(xpath = "//div[contains(@class,'c-AttributeFilterPicker afp-list')]")
	private List<WebElement> simpleColumnList;

	@FindBy(xpath = "//button[text()='Select']")
	private WebElement selectElementButtonDialog;

	@FindBy(xpath = "//button[text()='All']")
	private WebElement allElementButton;

	@FindBy(css = ".s-rankFilter")
	private WebElement rankFilterLink;

	@FindBy(xpath = "//input[@name='operatorChoice' and @value='top']")
	private WebElement topOption;

	@FindBy(xpath = "//input[@name='operatorChoice' and @value='bottom']")
	private WebElement bottomOption;

	@FindBy(xpath = "//div[@title='Slider']/div/select")
	private Select rankSizeSelect;

	@FindBy(xpath = "//div[21]/div/div[2]/div/div[6]/div/button[2]")
	private WebElement selectMetricButtonDialog;

	@FindBy(css = ".s-rangeFilter")
	private WebElement rangeFilterLink;

	@FindBy(css = ".s-btn-select_attribute")
	private WebElement selectAttributeButton;

	@FindBy(css = ".s-btn-select_metric")
	private WebElement selectMetricButton;

	@FindBy(css = ".s-input-number")
	private WebElement rangeNumberInput;

	@FindBy(css = ".s-confirmButton")
	private WebElement confirmApplyButton;

	@FindBy(css = ".s-promptFilter")
	private WebElement promptFilterLink;

	@FindBy(css = ".s-btn-select_variable")
	private WebElement selectVariableButton;

	@FindBy(css = ".s-btn-hide_filters")
	private WebElement hideFiltersButton;

	private String listOfElementLocator = "//div[contains(@class,'yui3-c-simpleColumn-underlay')]/div[contains(@class,'c-label') and contains(@class,'s-item-${label}')]";

	public void addFilterSelectList(Map<String, String> data)
			throws InterruptedException {
		System.out.println("Adding attribute filter ......");
		String attribute = data.get("attribute");
		if (browser.findElements(addFilterButton).size() > 0) {
			waitForElementVisible(addFilterButton).click();
		}// displayed if at least one filter added.
		waitForElementVisible(filterPicker);
		waitForElementVisible(attributeFilterLink).click();
		By listOfAttribute = By.xpath(listOfElementLocator.replace("${label}",
				attribute.trim().toLowerCase().replaceAll(" ", "_")));
		waitForElementVisible(listOfAttribute);
		selectElement(attribute);
		waitForElementVisible(selectElementButtonDialog).click();
		waitForElementVisible(listOfElementWithCheckbox);
		waitForElementVisible(allElementButton).click();
		waitForElementVisible(confirmApplyButton).click();
		waitForElementNotVisible(confirmApplyButton);
		waitForElementVisible(hideFiltersButton).click();
	}

	private void selectElement(String elementName) {
		if (simpleColumnList != null && simpleColumnList.size() > 0) {
			for (WebElement elem : simpleColumnList) {
				if (elem.findElement(By.tagName("span")).getText()
						.equals(elementName)) {
					elem.findElement(By.tagName("span")).click();
					break;
				}
			}
			//
		} else {
			Assert.fail("No attribute are available");
		}
	}

	public void addRankFilter(Map<String, String> data)
			throws InterruptedException {
		System.out.println("Adding Rank Filter ......");
		String attribute = data.get("attribute");
		String metric = data.get("metric");
		String type = data.get("type");
		String size = data.get("size");
		String[] array = { "1", "3", "5", "10" };
		if (browser.findElements(addFilterButton).size() > 0) {
			waitForElementVisible(addFilterButton).click();
		} // displayed if at least one filter added.
		waitForElementVisible(filterPicker);
		waitForElementVisible(rankFilterLink).click();
		if (type.equals("Bottom")) {
			waitForElementVisible(bottomOption).click();
		} else {
			waitForElementVisible(topOption).click();
		}
		Thread.sleep(2000);
		waitForElementVisible(rankSizeSelect);
		if (Arrays.asList(array).contains(size)) {
			rankSizeSelect.selectByValue(size);
		} else {
			rankSizeSelect.selectByValue("3");
		}
		waitForElementVisible(selectAttributeButton).click();
		By listOfAttribute = By.xpath(listOfElementLocator.replace("${label}",
				attribute.trim().toLowerCase().replaceAll(" ", "_")));
		waitForElementVisible(listOfAttribute);
		selectElement(attribute);
		waitForElementVisible(selectElementButtonDialog).click();
		waitForElementVisible(selectMetricButton).click();
		By listOfMetric = By.xpath(listOfElementLocator.replace("${label}",
				metric.trim().toLowerCase().replaceAll(" ", "_")));
		waitForElementVisible(listOfMetric);
		selectElement(metric);
		waitForElementVisible(selectMetricButtonDialog).click();
		waitForElementVisible(confirmApplyButton).click();
		waitForElementNotVisible(confirmApplyButton);
		waitForElementVisible(hideFiltersButton).click();

	}

	public void addRangeFilter(Map<String, String> data)
			throws InterruptedException {
		System.out.println("Adding Range Filter ......");
		String attribute = data.get("attribute");
		String metric = data.get("metric");
		String number = data.get("number");
		if (browser.findElements(addFilterButton).size() > 0) {
			waitForElementVisible(addFilterButton).click();
		}// displayed if at least one filter added.
		waitForElementVisible(rangeFilterLink).click();
		waitForElementVisible(selectAttributeButton).click();
		By listOfAttribute = By.xpath(listOfElementLocator.replace("${label}",
				attribute.trim().toLowerCase().replaceAll(" ", "_")));
		waitForElementVisible(listOfAttribute);
		selectElement(attribute);
		waitForElementVisible(selectElementButtonDialog).click();
		waitForElementVisible(selectMetricButton).click();
		By listOfMetric = By.xpath(listOfElementLocator.replace("${label}",
				metric.trim().toLowerCase().replaceAll(" ", "_")));
		waitForElementVisible(listOfMetric);
		selectElement(metric);
		waitForElementVisible(selectMetricButtonDialog).click();
		waitForElementVisible(rangeNumberInput).clear();
		rangeNumberInput.sendKeys(number);
		waitForElementVisible(confirmApplyButton).click();
		waitForElementNotVisible(confirmApplyButton);
		waitForElementVisible(hideFiltersButton).click();
	}

	public void addPromtFiter(Map<String, String> data)
			throws InterruptedException {
		System.out.println("Adding Prompt Filter ......");
		String variable = data.get("variable");
		if (waitForElementVisible(addFilterButton).isDisplayed()) {
			waitForElementVisible(addFilterButton).click();
		}// displayed if at least one filter added.
		waitForElementVisible(promptFilterLink).click();
		waitForElementVisible(selectVariableButton).click();
		By listOfPrompt = By.xpath(listOfElementLocator.replace("${label}",
				variable.trim().toLowerCase().replaceAll(" ", "_")));
		waitForElementVisible(listOfPrompt);
		selectElement(variable);
		waitForElementVisible(selectElementButtonDialog).click();
		waitForElementVisible(confirmApplyButton).click();
		waitForElementNotVisible(confirmApplyButton);
		waitForElementVisible(hideFiltersButton).click();
	}
}
