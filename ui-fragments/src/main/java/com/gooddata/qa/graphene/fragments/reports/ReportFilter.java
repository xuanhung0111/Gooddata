package com.gooddata.qa.graphene.fragments.reports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

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

    @FindBy(xpath = "//div[contains(@class,'attributes')]//input[contains(@class,'gdc-input')]")
    private WebElement searchAttributeInput;

    @FindBy(xpath = "//input[contains(@class,'gdc-input')]")
    private WebElement searchValueInput;

    @FindBy(xpath = "//button[(text()='Select') and not (contains(@class,'yui3-c-button-disabled'))]")
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

    @FindBy(xpath = "//div[@id='gridContainerTab']")
    private TableReport report;

    private String listOfElementLocator = "//div[contains(@class,'yui3-c-simpleColumn-underlay')]/div[contains(@class,'c-label') and contains(@class,'s-item-${label}')]";

    private String attributeElementLocator = "//div[contains(@class,'yui3-c-simpleColumn-underlay')]/div[contains(@class,'s-item-${element}')]/input";

    public void addFilterSelectList(Map<String, String> data)
            throws InterruptedException {
        System.out.println("Adding attribute filter ......");
        String attribute = data.get("attribute");
        String attributeElements = data.get("attributeElements");
        List<String> attributeHearderInGrid = report.getAttributesHeader();
        List<String> lsAttributeElements = Arrays.asList(attributeElements.split(", "));
        Boolean attributeInHow = attributeHearderInGrid.contains(attribute);
        Collections.sort(lsAttributeElements);
        if (browser.findElements(addFilterButton).size() > 0) {
            waitForElementVisible(addFilterButton, browser).click();
        }// displayed if at least one filter added.
        waitForElementVisible(filterPicker);
        waitForElementVisible(attributeFilterLink).click();
        By listOfAttribute = By.xpath(listOfElementLocator.replace("${label}",
                attribute.trim().toLowerCase().replaceAll("\\W", "_")));
        if (attributeInHow) {
            waitForElementVisible(listOfAttribute, browser);
            selectElement(attribute);
        } else {
            waitForElementVisible(searchAttributeInput).sendKeys(attribute);
            waitForElementVisible(listOfAttribute, browser);
            selectElementNotInHow(attribute);
        }
        waitForElementVisible(selectElementButtonDialog).click();
        waitForElementVisible(listOfElementWithCheckbox);
        for (int i = 0; i < lsAttributeElements.size(); i++) {
            if (!attributeInHow) {
                waitForElementVisible(searchValueInput).clear();
                searchValueInput.sendKeys(lsAttributeElements.get(i));
            }
            By attributeElement = By.xpath(attributeElementLocator.replace("${element}",
                    lsAttributeElements.get(i).trim().toLowerCase().replaceAll("\\W", "_")));
            waitForElementVisible(attributeElement, browser).click();
        }
        waitForElementVisible(confirmApplyButton).click();
        waitForElementNotVisible(confirmApplyButton);
        waitForTableReportRendered();
        waitForElementVisible(hideFiltersButton).click();
        waitForElementVisible(report.getRoot());        
        if (attributeInHow) {
            List<String> attributeElementsInGrid = report.getAttributeElements();
            Collections.sort(attributeElementsInGrid);
            Assert.assertEquals(attributeElementsInGrid, lsAttributeElements, "Report isn't applied filter correctly");
        }
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

    private void selectElementNotInHow(String elementName) {
        By listOfAttribute = By.xpath(listOfElementLocator.replace("${label}", elementName.trim().toLowerCase().replaceAll("\\W", "_")));
        if (listOfAttribute != null) {
            for (WebElement elem : root.findElements(listOfAttribute)) {
                String ele = elem.findElement(By.tagName("span")).getText();
                if (ele.equals(elementName)) {
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
        String[] array = {"1", "3", "5", "10"};
        waitForElementVisible(report.getRoot());
        List<Float> metricValuesinGrid = report.getMetricElements();
        Collections.sort(metricValuesinGrid);
        if (type == "Top") {
            Collections.reverse(metricValuesinGrid);
        }
        int rankSize = Integer.parseInt(size);
        List<Float> rankedMetric = new ArrayList<Float>();
        for (int i = 0; i < rankSize; i++) {
            rankedMetric.add(metricValuesinGrid.get(i));
        }
        Collections.sort(rankedMetric);
        if (browser.findElements(addFilterButton).size() > 0) {
            waitForElementVisible(addFilterButton, browser).click();
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
        waitForElementVisible(listOfAttribute, browser);
        selectElement(attribute);
        waitForElementVisible(selectElementButtonDialog).click();
        waitForElementVisible(selectMetricButton).click();
        By listOfMetric = By.xpath(listOfElementLocator.replace("${label}",
                metric.trim().toLowerCase().replaceAll(" ", "_")));
        waitForElementVisible(listOfMetric, browser);
        selectElement(metric);
        waitForElementVisible(selectElementButtonDialog).click();
        waitForElementVisible(confirmApplyButton).click();
        waitForElementNotVisible(confirmApplyButton);
        waitForTableReportRendered();
        waitForElementVisible(hideFiltersButton).click();
        metricValuesinGrid = report.getMetricElements();
        Collections.sort(metricValuesinGrid);
        Assert.assertEquals(metricValuesinGrid, rankedMetric, "Report isn't applied filter correctly");

    }

    public void addRangeFilter(Map<String, String> data)
            throws InterruptedException {
        System.out.println("Adding Range Filter ......");
        String attribute = data.get("attribute");
        String metric = data.get("metric");
        String number = data.get("number");
        if (browser.findElements(addFilterButton).size() > 0) {
            waitForElementVisible(addFilterButton, browser).click();
        }// displayed if at least one filter added.
        waitForElementVisible(rangeFilterLink).click();
        waitForElementVisible(selectAttributeButton).click();
        By listOfAttribute = By.xpath(listOfElementLocator.replace("${label}",
                attribute.trim().toLowerCase().replaceAll(" ", "_")));
        waitForElementVisible(listOfAttribute, browser);
        selectElement(attribute);
        waitForElementVisible(selectElementButtonDialog).click();
        waitForElementVisible(selectMetricButton).click();
        By listOfMetric = By.xpath(listOfElementLocator.replace("${label}",
                metric.trim().toLowerCase().replaceAll(" ", "_")));
        waitForElementVisible(listOfMetric, browser);
        selectElement(metric);
        waitForElementVisible(selectElementButtonDialog).click();
        waitForElementVisible(rangeNumberInput).clear();
        rangeNumberInput.sendKeys(number);
        waitForElementVisible(confirmApplyButton).click();
        waitForElementNotVisible(confirmApplyButton);
        waitForTableReportRendered();
        waitForElementVisible(hideFiltersButton).click();
        waitForElementVisible(report.getRoot());
        List<Float> metricValuesInGrid = report.getMetricElements();
        int rangeNumber = Integer.parseInt(number);
        for (int i = 0; i < metricValuesInGrid.size(); i++) {
            Assert.assertTrue(metricValuesInGrid.get(i) >= rangeNumber, "Report isn't applied filter correctly");
        }
    }

    public void addPromtFiter(Map<String, String> data)
            throws InterruptedException {
        System.out.println("Adding Prompt Filter ......");
        String variable = data.get("variable");
        String promptElements = data.get("promptElements");
        List<String> lsPromptElements = Arrays.asList(promptElements.split(", "));
        waitForElementVisible(report.getRoot());
        List<String> attrElementInGrid = report.getAttributeElements();
        lsPromptElements.retainAll(attrElementInGrid);
        if (waitForElementVisible(addFilterButton, browser).isDisplayed()) {
            waitForElementVisible(addFilterButton, browser).click();
        }// displayed if at least one filter added.
        waitForElementVisible(promptFilterLink).click();
        waitForElementVisible(selectVariableButton).click();
        By listOfPrompt = By.xpath(listOfElementLocator.replace("${label}",
                variable.trim().toLowerCase().replaceAll(" ", "_")));
        waitForElementVisible(listOfPrompt, browser);
        selectElement(variable);
        waitForElementVisible(selectElementButtonDialog).click();
        waitForElementVisible(confirmApplyButton).click();
        waitForElementNotVisible(confirmApplyButton);
        waitForTableReportRendered();
        waitForElementVisible(hideFiltersButton).click();
        attrElementInGrid = report.getAttributeElements();
        Assert.assertEquals(attrElementInGrid, lsPromptElements, "Report isn't applied filter correctly");
    }

    public void waitForTableReportRendered() {
        By gridContainerBody = By.xpath("//div[@id='gridContainerTab']/div[@id='gridContainer']/div[@class='containerBody']/div[contains(@class,'gridTab')]");
        if (browser.findElements(gridContainerBody).size() > 0) {
            waitForElementNotPresent(gridContainerBody);
        }
        waitForElementVisible(gridContainerBody, browser);
    }
}
