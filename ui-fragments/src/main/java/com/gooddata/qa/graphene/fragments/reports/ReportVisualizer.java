package com.gooddata.qa.graphene.fragments.reports;

import java.util.List;

import com.gooddata.qa.graphene.enums.metrics.SimpleMetricTypes;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.ReportTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.support.ui.Select;

public class ReportVisualizer extends AbstractFragment {

    private static final By BY_WHAT_AREA_METRICS_HEADER = By.xpath("//div[contains(@class, 'sndPanel1')]//div[@title='All Metrics']");
    private static final By BY_HOW_AREA_ATTRIBUTES_HEADER = By.xpath("//div[contains(@class, 'sndPanel1')]//div[@title='All Attributes']");
    private static final By BY_FILTER_TAB = By.id("filterTab");

    private static final String XPATH_REPORT_VISUALIZATION_TYPE = "//div[contains(@class, 's-enabled')]/div[contains(@class, 'c-chartType') and ./span[@title='${type}']]";

    private static final String XPATH_METRIC_CHECKBOX = "//div[contains(@class, 'sndMetric') and contains(@title, '${metric}')]//input[@type='checkbox']";
    private static final String XPATH_METRIC_CHECKBOX_CHECKED = "//div[contains(@class, 'sndMetric') and contains(@title, '${metric}')]//input[@type='checkbox' and @checked='checked']";

    private static final String XPATH_ATTRIBUTE_CHECKBOX = "//div[contains(@class, 's-snd-AttributesContainer')]//div[contains(@class, 'element') and contains(@title, '${attribute}')]//input[@type='checkbox']";
    private static final String XPATH_ATTRIBUTE_CHECKBOX_CHECKED = "//div[contains(@class, 'AttributesContainer')]//div[contains(@class, 'element') and contains(@title, '${attribute}')]//input[@type='checkbox' and @checked='checked']";

    @FindBy(xpath = "//div[contains(@class, 'reportEditorWhatArea')]/button")
    private WebElement whatButton;

    @FindBy(xpath = "//div[contains(@class, 'reportEditorHowArea')]/button")
    private WebElement howButton;

    @FindBy(xpath = "//div[contains(@class, 'reportEditorFilterArea')]/button")
    private WebElement filterButton;

    @FindBy(xpath = "//form[@class='sndFooterForm']/button[text()='Done']")
    private WebElement doneButton;

    @FindBy(xpath = "//label[@class='sndMetricFilterLabel']/../input")
    private WebElement metricFilterInput;

    @FindBy(xpath = "//label[@class='sndAttributeFilterLabel']/../input")
    private WebElement attributeFilterInput;

    @FindBy
    private WebElement reportVisualizationContainer;

    @FindBy(xpath = "//button[contains(@class,'sndCreateMetric')]")
    private WebElement createMetricButton;

    @FindBy(xpath = "//select[contains(@class,'s-sme-fnSelect')]")
    private Select metricOperationSelect;

    @FindBy(xpath = "//select[contains(@class,'s-sme-objSelect')]")
    private Select performOperationSelect;

    @FindBy(xpath = "//input[contains(@class,'s-sme-global')]")
    private WebElement addToGlobalInput;

    @FindBy(xpath = "//input[contains(@class,'s-sme-title')]")
    private WebElement metricTitleInput;

    @FindBy(xpath = "//button[contains(@class,'s-sme-addButton')]")
    private WebElement addMetricButton;

    public void selectWhatArea(List<String> what) throws InterruptedException {
        waitForElementVisible(whatButton).click();
        waitForElementVisible(BY_WHAT_AREA_METRICS_HEADER);
        for (String metric : what) {
            waitForElementVisible(metricFilterInput).clear();
            metricFilterInput.sendKeys(metric);
            By metricCheckbox = By.xpath(XPATH_METRIC_CHECKBOX.replace("${metric}", metric));
            waitForElementVisible(metricCheckbox);
            Thread.sleep(2000);
            root.findElement(metricCheckbox).click();
            waitForElementVisible(By.xpath(XPATH_METRIC_CHECKBOX_CHECKED.replace("${metric}", metric)));
        }
    }

    public void selectHowArea(List<String> how) throws InterruptedException {
        waitForElementVisible(howButton).click();
        waitForElementVisible(BY_HOW_AREA_ATTRIBUTES_HEADER);
        if (how != null) {
            for (String attribute : how) {
                waitForElementVisible(attributeFilterInput).clear();
                attributeFilterInput.sendKeys(attribute);
                By attributeCheckbox = By.xpath(XPATH_ATTRIBUTE_CHECKBOX.replace("${attribute}", attribute));
                waitForElementVisible(attributeCheckbox);
                Thread.sleep(2000);
                root.findElement(attributeCheckbox).click();
                waitForElementVisible(By.xpath(XPATH_ATTRIBUTE_CHECKBOX_CHECKED.replace("${attribute}", attribute)));
            }
        }
    }

    public void addSimpleMetric(SimpleMetricTypes metricOperation, String metricOnFact, String metricName, boolean addToGlobal) {
        waitForElementVisible(whatButton).click();
        waitForElementVisible(BY_WHAT_AREA_METRICS_HEADER);

        waitForElementVisible(createMetricButton).click();
        waitForElementVisible(metricOperationSelect).selectByVisibleText(metricOperation.name());
        waitForElementVisible(performOperationSelect).selectByVisibleText(metricOnFact);

        if (metricName!=null) {
            waitForElementVisible(metricTitleInput).clear();
            metricTitleInput.sendKeys(metricName);
        }
        if (addToGlobal) waitForElementVisible(addToGlobalInput).click();
        waitForElementVisible(addMetricButton).click();
    }

    public void selectFilterArea() {
        waitForElementVisible(filterButton).click();
        waitForElementVisible(BY_FILTER_TAB);
    }

    public void finishReportChanges() {
        waitForElementVisible(doneButton).click();
        waitForElementNotVisible(doneButton);
    }

    public void selectReportVisualisation(ReportTypes reportVisualizationType) {
        By icon = By.xpath(XPATH_REPORT_VISUALIZATION_TYPE.replace("${type}", reportVisualizationType.getName()));
        waitForElementVisible(icon);
        reportVisualizationContainer.findElement(icon).click();
        waitForElementVisible(By.id(reportVisualizationType.getContainerTabId()));
    }

}
