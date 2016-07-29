package com.gooddata.qa.graphene.fragments.manage;

import static com.gooddata.qa.graphene.entity.metric.CustomMetricUI.extractAttributeValue;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.lang.String.format;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.metric.CustomMetricUI;
import com.gooddata.qa.graphene.enums.metrics.MetricTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;

public class MetricEditorDialog extends AbstractFragment {

    private static final By BY_SAVE_BUTTON = By.cssSelector("button.save:not([style^='display: none'])");
    private static final By BY_BACK_BUTTON = By.cssSelector(".button.back:not([style^='display: none'])");
    private static final By BY_CANCEL_BUTTON = By.cssSelector(".button.cancel:not([style^='display: none'])");

    @FindBy(css = ".metricEditor > div:not([style^='display: none']) input.text.metricName")
    private WebElement metricInput;

    @FindBy(css = "div.maql-editor")
    private WebElement maqlEditor;

    @FindBy(css = ".primary.controls button.add:not([style^='display: none'])")
    private WebElement addButton;

    @FindBy(xpath =
            "//div[contains(@class,'MAQLEditorElementsMenu')]/div[@class='header']/button[@class='confirm']")
    private WebElement addSelectedButton;

    @FindBy(xpath = "//div[@class='listContainer']/div/ul[@class='elementList']/li[@class='category']")
    private WebElement customCategoryList;
    
    @FindBy(css = ".elementList .c-label:not(.gdc-hidden) span")
    private List<WebElement> attributeElelements;

    private static final String METRIC_LINK_LOCATOR = "${metricType}";
    private static final String METRIC_TEMPLATE_TAB_LOCATOR = "//ul[@role='tablist']//em[text()='${tab}']";

    public static final By LOCATOR = By.className("s-metricEditor");

    private static final String SELECTION_LOCATOR = "//a[contains(@class,'%s') and text()='...']";
    private static final By ATTRIBUTE_SELECTION_LOCATOR = By.xpath(format(SELECTION_LOCATOR, "attributes"));
    private static final By ATTRIBUTE_ELEMENT_SELECTION_LOCATOR =
            By.xpath(format(SELECTION_LOCATOR, "attributeElements"));
    private static final By FACT_SELECTION_LOCATOR = By.xpath(format(SELECTION_LOCATOR, "facts"));
    private static final By METRIC_SELECTION_LOCATOR = By.xpath(format(SELECTION_LOCATOR, "metrics"));

    public void clickShareMetricLink() {
        waitForElementVisible(By.cssSelector("div.shareTemplate"), browser).click();
    }

    public void clickDifferentMetricLink() {
        waitForElementVisible(By.cssSelector("div.differenceTemplate"), browser).click();
    }

    public void clickRatioMetricLink() {
        waitForElementVisible(By.cssSelector("div.ratioTemplate"), browser).click();
    }

    public void clickCustomMetricLink() {
        waitForElementVisible(By.cssSelector("div.customMetric"), browser).click();
    }

    public void createShareMetric(String metricName, String usedMetric, String attr) {
        clickShareMetricLink();
        selectElement(usedMetric);
        selectElement(attr);
        enterMetricNameAndSubmit(metricName);
    }

    public void createDifferentMetric(String metricName, String usedMetric, String attr, String attrValue) {
        clickDifferentMetricLink();
        selectElement(usedMetric);
        selectElement(attr);
        selectElement(attrValue);
        enterMetricNameAndSubmit(metricName);
    }

    public void createRatioMetric(String metricName, String usedMetric1, String usedMetric2) {
        clickRatioMetricLink();
        selectElement(usedMetric1);
        selectElement(usedMetric2);
        enterMetricNameAndSubmit(metricName);
    }

    public void createAggregationMetric(MetricTypes metricType, CustomMetricUI metricUI) {
        createTemplateMetricTab(metricType);
        if (metricType == MetricTypes.COUNT) {
            selectAttributes(metricUI);
        } else if (metricType == MetricTypes.FORECAST) {
            selectMetrics(metricUI);
        } else {
            selectFacts(metricUI);
        }
        enterMetricNameAndSubmit(metricUI.getName());
    }

    public void createNumericMetric(MetricTypes metricType, CustomMetricUI metricUI) {
        createTemplateMetricTab(metricType);
        selectMetrics(metricUI);
        enterMetricNameAndSubmit(metricUI.getName());
    }

    public void createGranularityMetric(MetricTypes metricType, CustomMetricUI metricUI) {
        createTemplateMetricTab(metricType);
        selectMetrics(metricUI);
        if (metricType != MetricTypes.BY_ALL) {
            selectAttributes(metricUI);
        }
        enterMetricNameAndSubmit(metricUI.getName());
    }

    public void createLogicalMetric(MetricTypes metricType, CustomMetricUI metricUI) {
        createTemplateMetricTab(metricType);
        selectMetrics(metricUI);
        if (metricType != MetricTypes.CASE && metricType != MetricTypes.IF) {
            selectAttributes(metricUI);
            selectAttrElements(metricUI);
        }
        enterMetricNameAndSubmit(metricUI.getName());
    }

    public void createFilterMetric(MetricTypes metricType, CustomMetricUI metricUI) {
        createTemplateMetricTab(metricType);
        selectMetrics(metricUI);
        selectAttributes(metricUI);
        switch (metricType) {
            case EQUAL:
            case DOES_NOT_EQUAL:
            case GREATER:
            case GREATER_OR_EQUAL:
            case LESS:
            case LESS_OR_EQUAL:
            case BETWEEN:
            case NOT_BETWEEN:
            case IN:
            case NOT_IN:
                selectAttrElements(metricUI);
                break;
            default:
                break;
        }
        enterMetricNameAndSubmit(metricUI.getName());
    }

    public MetricEditorDialog enterMetricName(String name) {
        waitForElementVisible(metricInput).clear();
        metricInput.sendKeys(name);

        return this;
    }

    public void save() {
        waitForElementVisible(BY_SAVE_BUTTON, getRoot()).click();
    }

    public void back() {
        waitForElementVisible(BY_BACK_BUTTON, getRoot()).click();
    }

    public void cancel() {
        waitForElementVisible(BY_CANCEL_BUTTON, getRoot()).click();
    }

    private void selectAttributes(CustomMetricUI metricUI) {
        for (String attribute : metricUI.getAttributes()) {
            waitForElementVisible(ATTRIBUTE_SELECTION_LOCATOR, browser)
                .click();
            selectElement(attribute);
            waitForElementVisible(addSelectedButton).click();
            waitForElementVisible(customCategoryList);
        }
    }

    private void selectAttrElements(CustomMetricUI metricUI) {
        Pair<String, String> attributeValueInfo;
        for (String value : metricUI.getAttributeValues()) {
            attributeValueInfo = extractAttributeValue(value);
            waitForElementVisible(ATTRIBUTE_ELEMENT_SELECTION_LOCATOR, browser)
                .click();
            selectElement(attributeValueInfo.getLeft());
            selectElement(attributeValueInfo.getRight());
            waitForElementVisible(addSelectedButton).click();
            waitForElementVisible(customCategoryList);
        }
    }

    private void selectFacts(CustomMetricUI metricUI) {
        for (String fact : metricUI.getFacts()) {
            waitForElementVisible(FACT_SELECTION_LOCATOR, browser).click();
            selectElement(fact);
            waitForElementVisible(addSelectedButton).click();
            waitForElementVisible(customCategoryList);
        }
    }

    private void selectMetrics(CustomMetricUI metricUI) {
        for (String metric : metricUI.getMetrics()) {
            waitForElementVisible(METRIC_SELECTION_LOCATOR, browser).click();
            selectElement(metric);
            waitForElementVisible(addSelectedButton).click();
            waitForElementVisible(customCategoryList);
        }
    }

    private void selectElement(String element) {
        SelectItemPopupPanel panel = Graphene.createPageFragment(SelectItemPopupPanel.class, 
                waitForElementVisible(By.cssSelector(".s-metricEditor > [style='display: block;'] .listContainer"),
                        browser));
        panel.searchAndSelectItem(element);
    }

    private void createTemplateMetricTab(MetricTypes metric) {
        clickCustomMetricLink();
        waitForElementVisible(maqlEditor);
        waitForElementVisible(By.xpath(METRIC_TEMPLATE_TAB_LOCATOR.replace("${tab}", metric.getType())), browser).click();
        waitForElementVisible(By.linkText(METRIC_LINK_LOCATOR.replace("${metricType}", metric.getLabel())),
                browser).click();
    }

    private void enterMetricNameAndSubmit(String name) {
        enterMetricName(name).submit();
    }

    private void submit() {
        waitForElementVisible(addButton).click();
    }
}
