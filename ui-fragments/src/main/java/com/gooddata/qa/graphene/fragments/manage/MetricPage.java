package com.gooddata.qa.graphene.fragments.manage;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDataPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForObjectPageLoaded;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static org.openqa.selenium.By.className;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.metric.CustomMetricUI;
import com.gooddata.qa.graphene.enums.metrics.MetricTypes;

public class MetricPage extends DataPage {

    @FindBy(className = "s-btn-create_metric")
    private WebElement createMetricButton;

    @FindBy(css = ".s-btn-edit")
    private WebElement editButton;

    @FindBy(xpath = "//a[@class='interpolateProject']")
    private WebElement dataLink;

    private static final By METRIC_EDITOR_LOCATOR = className("metricEditorFrame");

    public static MetricPage getInstance(SearchContext context) {
        return Graphene.createPageFragment(MetricPage.class, waitForElementVisible(ROOT_LOCATOR, context));
    }

    public MetricPage createShareMetric(String metricName, String usedMetric, String attr) {
        openMetricEditor().createShareMetric(metricName, usedMetric, attr);
        return backToMetricsTable();
    }

    public MetricPage createDifferentMetric(String metricName, String usedMetric, String attr, String attrValue) {
        openMetricEditor().createDifferentMetric(metricName, usedMetric, attr, attrValue);
        return backToMetricsTable();
    }

    public MetricPage createRatioMetric(String metricName, String usedMetric1, String usedMetric2) {
        openMetricEditor().createRatioMetric(metricName, usedMetric1, usedMetric2);
        return backToMetricsTable();
    }

    public MetricPage createAggregationMetric(MetricTypes metricType, CustomMetricUI metricUI) {
        openMetricEditor().createAggregationMetric(metricType, metricUI);
        return backToMetricsTable();
    }

    public MetricPage createNumericMetric(MetricTypes metricType, CustomMetricUI metricUI) {
        openMetricEditor().createNumericMetric(metricType, metricUI);
        return backToMetricsTable();
    }

    public MetricPage createGranularityMetric(MetricTypes metricType, CustomMetricUI metricUI) {
        openMetricEditor().createGranularityMetric(metricType, metricUI);
        return backToMetricsTable();
    }

    public MetricPage createLogicalMetric(MetricTypes metricType, CustomMetricUI metricUI) {
        openMetricEditor().createLogicalMetric(metricType, metricUI);
        return backToMetricsTable();
    }

    public MetricPage createFilterMetric(MetricTypes metricType, CustomMetricUI metricUI) {
        openMetricEditor().createFilterMetric(metricType, metricUI);
        return backToMetricsTable();
    }

    public boolean isMetricCreatedSuccessfully(String metricName, String expectedMaql, String expectedFormat) {
        openMetricDetailPage(metricName);
        return Graphene.createPageFragment(MetricDetailsPage.class, waitForElementVisible(By.id("p-objectPage"),
                browser)).isMetricCreatedSuccessfully(expectedMaql, expectedFormat);
    }

    public MetricDetailsPage openMetricDetailPage(String metric) {
        ObjectsTable metricsTable = Graphene.createPageFragment(ObjectsTable.class,
                waitForElementVisible(By.id("metricsTable"), browser));
        waitForDataPageLoaded(browser);
        metricsTable.selectObject(metric);
        waitForObjectPageLoaded(browser);
        return MetricDetailsPage.getInstance(browser);
    }

    public boolean isMetricVisible(String metric) {
        if (isEmpty()) return false;
        ObjectsTable metricsTable = Graphene.createPageFragment(ObjectsTable.class,
                waitForElementVisible(By.id("metricsTable"), browser));
        waitForDataPageLoaded(browser);
        return metricsTable.getAllItems().contains(metric);
    }

    public boolean isEmpty() {
        return !waitForElementPresent(By.id("metricsTable"), browser).isDisplayed();
    }

    public MetricEditorDialog openMetricEditor() {
        waitForElementVisible(createMetricButton).click();

        return MetricEditorDialog.getInstance(browser);
    }

    private MetricPage backToMetricsTable() {
        browser.switchTo().defaultContent();
        waitForElementNotPresent(METRIC_EDITOR_LOCATOR);
        sleepTightInSeconds(2);
        MetricDetailsPage.getInstance(browser).clickDataPageLink();
        return waitForFragmentVisible(this);
    }
}
