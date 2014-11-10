package com.gooddata.qa.graphene.fragments.reports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;

import com.gooddata.qa.graphene.entity.filter.NumericRangeFilterItem;
import com.gooddata.qa.graphene.entity.filter.RankingFilterItem;
import com.gooddata.qa.graphene.entity.filter.SelectFromListValuesFilterItem;
import com.gooddata.qa.graphene.entity.filter.VariableFilterItem;
import com.gooddata.qa.graphene.entity.filter.RankingFilterItem.ResultSize;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class ReportFilter extends AbstractFragment {

    private static final String ATTRIBUTE_IN_HOW_FROM_LIST = 
            "//div[contains(@class,'s-item-${label}') and contains(@class,'s-enabled')]//span";
    
    private static final By BY_ADD_FILTER_BUTTON = By.cssSelector(".s-btn-add_filter");

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

    @FindBy(xpath = "//div[@id='reportContainerTab' and contains(@class, 'processingReport')]")
    private WebElement reportProcessing;

    private String listOfElementLocator = 
            "//div[contains(@class,'yui3-c-simpleColumn-underlay')]/div[contains(@class,'c-label') and contains(@class,'s-item-${label}')]";

    public void addFilterSelectList(SelectFromListValuesFilterItem filterItem) {
        System.out.println("Adding attribute filter ......");
        WebElement addFilterButton = waitForElementVisible(BY_ADD_FILTER_BUTTON, browser);
        
        if (!addFilterButton.getAttribute("class").contains("disabled")) {
            addFilterButton.click();
        }
        waitForElementVisible(filterPicker);
        waitForElementVisible(attributeFilterLink).click();
        
        Graphene.createPageFragment(SelectItemPopupPanel.class,
                waitForElementVisible(SelectItemPopupPanel.LOCATOR, browser))
                .searchAndSelectItem(filterItem.getAttribute());

        SelectItemPopupPanel panel =
                Graphene.createPageFragment(SelectItemPopupPanel.class,
                        waitForElementVisible(By.cssSelector(".listContainer"), browser));
        for (String e : filterItem.getValues()) {
            panel.searchAndSelectEmbedItem(e);
        }
        waitForElementVisible(confirmApplyButton).click();
        waitForElementNotVisible(confirmApplyButton);
        waitForElementVisible(hideFiltersButton).click();
    }

    private String createAttributeXPath(String locator, String placeHolder, String attributeName) {
        return locator.replace(placeHolder, attributeName.trim().toLowerCase().replaceAll("\\W", "_"));
    }

    private void selectElement(String elementName) {
        waitForElementVisible(By.xpath(createAttributeXPath(listOfElementLocator, "${label}", elementName)), browser);
        waitForElementVisible(By.xpath(createAttributeXPath(ATTRIBUTE_IN_HOW_FROM_LIST, "${label}", elementName)), browser).click();
    }

    public void addRankFilter(RankingFilterItem filterItem) throws InterruptedException {
        System.out.println("Adding Rank Filter ......");
        String attribute = filterItem.getAttribute();
        String metric = filterItem.getMetric();
        RankingFilterItem.ResultSize resultSize = filterItem.getSize();
        waitForElementVisible(report.getRoot());
        List<Float> metricValuesinGrid = report.getMetricElements();
        Collections.sort(metricValuesinGrid);
        if (resultSize == ResultSize.TOP) {
            Collections.reverse(metricValuesinGrid);
        }
        int rankSize = resultSize.getSize();
        List<Float> rankedMetric = new ArrayList<Float>();
        for (int i = 0; i < rankSize; i++) {
            rankedMetric.add(metricValuesinGrid.get(i));
        }
        Collections.sort(rankedMetric);
        if (browser.findElements(BY_ADD_FILTER_BUTTON).size() > 0) {
            waitForElementVisible(BY_ADD_FILTER_BUTTON, browser).click();
        } // displayed if at least one filter added.
        waitForElementVisible(filterPicker);
        waitForElementVisible(rankFilterLink).click();
        if (resultSize == ResultSize.BOTTOM) {
            waitForElementVisible(bottomOption).click();
        } else {
            waitForElementVisible(topOption).click();
        }
        Thread.sleep(2000);
        waitForElementVisible(rankSizeSelect);
        if (Arrays.asList(1, 3, 5, 10).contains(rankSize)) {
            rankSizeSelect.selectByValue(String.valueOf(rankSize));
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
        waitForReportRendered();
        waitForElementVisible(hideFiltersButton).click();
        metricValuesinGrid = report.getMetricElements();
        Collections.sort(metricValuesinGrid);
        Assert.assertEquals(metricValuesinGrid, rankedMetric, "Report isn't applied filter correctly");
    }

    public void addRangeFilter(NumericRangeFilterItem filterItem) {
        System.out.println("Adding Range Filter ......");
        String attribute = filterItem.getAttribute();
        String metric = filterItem.getMetric();
        int rangeNumber = filterItem.getRange().getNumber();
        if (browser.findElements(BY_ADD_FILTER_BUTTON).size() > 0) {
            waitForElementVisible(BY_ADD_FILTER_BUTTON, browser).click();
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
        rangeNumberInput.sendKeys(String.valueOf(rangeNumber));
        waitForElementVisible(confirmApplyButton).click();
        waitForElementNotVisible(confirmApplyButton);
        waitForReportRendered();
        waitForElementVisible(hideFiltersButton).click();
        waitForElementVisible(report.getRoot());
        List<Float> metricValuesInGrid = report.getMetricElements();
        for (int i = 0; i < metricValuesInGrid.size(); i++) {
            Assert.assertTrue(metricValuesInGrid.get(i) >= rangeNumber, "Report isn't applied filter correctly");
        }
    }

    public void addPromtFiter(VariableFilterItem filterItem) {
        System.out.println("Adding Prompt Filter ......");
        String variable = filterItem.getVariable();
        List<String> lsPromptElements = filterItem.getPromptElements();
        waitForElementVisible(report.getRoot());
        List<String> attrElementInGrid = report.getAttributeElements();
        lsPromptElements.retainAll(attrElementInGrid);
        if (waitForElementVisible(BY_ADD_FILTER_BUTTON, browser).isDisplayed()) {
            waitForElementVisible(BY_ADD_FILTER_BUTTON, browser).click();
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
        waitForReportRendered();
        waitForElementVisible(hideFiltersButton).click();
        attrElementInGrid = report.getAttributeElements();
        Assert.assertEquals(attrElementInGrid, lsPromptElements, "Report isn't applied filter correctly");
    }

    public void waitForReportRendered() {
        waitForElementVisible(reportProcessing);
        waitForElementNotVisible(reportProcessing);
    }
}
