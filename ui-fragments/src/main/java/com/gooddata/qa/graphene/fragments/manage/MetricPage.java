package com.gooddata.qa.graphene.fragments.manage;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForDataPageLoaded;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForObjectPageLoaded;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.metric.CustomMetricUI;
import com.gooddata.qa.graphene.enums.metrics.MetricTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class MetricPage extends AbstractFragment {

    @FindBy(className = "s-btn-create_metric")
    private WebElement createMetricButton;

    @FindBy(xpath = "//iframe[@class='metricEditorFrame']")
    private WebElement metricEditorPopup;

    @FindBy(css = ".s-btn-edit")
    private WebElement editButton;

    @FindBy(xpath = "//a[@class='interpolateProject']")
    private WebElement dataLink;

    public void createShareMetric(String metricName, String usedMetric, String attrFolder, String attr) {
        openMetricEditor();
        Graphene.createPageFragment(MetricEditorDialog.class,
                waitForElementVisible(MetricEditorDialog.LOCATOR, browser))
            .createShareMetric(metricName, usedMetric, attrFolder, attr);
        backToMetricsTable();
    }

    public void createDifferentMetric(String metricName, String usedMetric, String attrFolder, String attr,
            String attrValue) {
        openMetricEditor();
        Graphene.createPageFragment(MetricEditorDialog.class,
                waitForElementVisible(MetricEditorDialog.LOCATOR, browser))
            .createDifferentMetric(metricName, usedMetric, attrFolder, attr, attrValue);
        backToMetricsTable();
    }

    public void createRatioMetric(String metricName, String usedMetric1, String usedMetric2) {
        openMetricEditor();
        Graphene.createPageFragment(MetricEditorDialog.class,
                waitForElementVisible(MetricEditorDialog.LOCATOR, browser))
            .createRatioMetric(metricName, usedMetric1, usedMetric2);
        backToMetricsTable();
    }

    public void createAggregationMetric(MetricTypes metricType, CustomMetricUI metricUI) {
        openMetricEditor();
        Graphene.createPageFragment(MetricEditorDialog.class,
                waitForElementVisible(MetricEditorDialog.LOCATOR, browser))
            .createAggregationMetric(metricType, metricUI);
        backToMetricsTable();
    }

    public void createNumericMetric(MetricTypes metricType, CustomMetricUI metricUI) {
        openMetricEditor();
        Graphene.createPageFragment(MetricEditorDialog.class,
                waitForElementVisible(MetricEditorDialog.LOCATOR, browser))
            .createNumericMetric(metricType, metricUI);
        backToMetricsTable();
    }

    public void createGranularityMetric(MetricTypes metricType, CustomMetricUI metricUI) {
        openMetricEditor();
        Graphene.createPageFragment(MetricEditorDialog.class,
                waitForElementVisible(MetricEditorDialog.LOCATOR, browser))
            .createGranularityMetric(metricType, metricUI);
        backToMetricsTable();
    }

    public void createLogicalMetric(MetricTypes metricType, CustomMetricUI metricUI) {
        openMetricEditor();
        Graphene.createPageFragment(MetricEditorDialog.class,
                waitForElementVisible(MetricEditorDialog.LOCATOR, browser))
            .createLogicalMetric(metricType, metricUI);
        backToMetricsTable();
    }

    public void createFilterMetric(MetricTypes metricType, CustomMetricUI metricUI) {
        openMetricEditor();
        Graphene.createPageFragment(MetricEditorDialog.class,
                waitForElementVisible(MetricEditorDialog.LOCATOR, browser))
            .createFilterMetric(metricType, metricUI);
        backToMetricsTable();
    }

    public boolean isMetricCreatedSuccessfully(String metricName, String expectedMaql, String expectedFormat) {
        openMetricDetailPage(metricName);
        return Graphene.createPageFragment(MetricDetailsPage.class, waitForElementVisible(By.id("p-objectPage"),
                browser)).isMetricCreatedSuccessfully(metricName, expectedMaql, expectedFormat);
    }

    public void openMetricDetailPage(String metric) {
        ObjectsTable metricsTable = Graphene.createPageFragment(ObjectsTable.class,
                waitForElementVisible(By.id("metricsTable"), browser));
        waitForDataPageLoaded(browser);
        metricsTable.selectObject(metric);
        waitForObjectPageLoaded(browser);
    }

    public boolean isMetricVisible(String metric) {
        ObjectsTable metricsTable = Graphene.createPageFragment(ObjectsTable.class,
                waitForElementVisible(By.id("metricsTable"), browser));
        waitForDataPageLoaded(browser);
        return metricsTable.getAllItems().contains(metric);
    }

    private void openMetricEditor() {
        waitForElementVisible(createMetricButton).click();
        browser.switchTo().frame(waitForElementVisible(metricEditorPopup));
    }

    private void backToMetricsTable() {
        browser.switchTo().defaultContent();
        waitForElementNotPresent(metricEditorPopup);
        sleepTightInSeconds(2);
        waitForElementVisible(editButton);
        waitForElementVisible(dataLink).click();
    }
}
