package com.gooddata.qa.graphene.fragments.manage;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDataPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementEnabled;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForObjectPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForUserProfilePageLoaded;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.tagName;

import java.util.List;

import com.gooddata.qa.graphene.entity.metric.CustomMetricUI;
import com.gooddata.qa.graphene.enums.metrics.MetricTypes;
import com.gooddata.qa.graphene.fragments.common.PermissionSettingDialog;
import com.gooddata.qa.graphene.fragments.common.PermissionSettingDialog.PermissionType;
import com.gooddata.qa.graphene.fragments.profile.UserProfilePage;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class MetricPage extends DataPage {

    @FindBy(className = "s-btn-create_metric")
    private WebElement createMetricButton;

    @FindBy(css = ".s-btn-edit")
    private WebElement editButton;

    @FindBy(xpath = "//a[@class='interpolateProject']")
    private WebElement dataLink;

    @FindBy(className = "dataPage-listRow")
    private List<WebElement> metrics;

    @FindBy(className = "s-btn-permissions___")
    private WebElement permissionButton;

    private static final By METRIC_EDITOR_LOCATOR = className("metricEditorFrame");
    private static final By TITLE_METRIC = cssSelector(".title span:not(.s-lockIcon)");

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

    public boolean isMetricLocked(String metricName) {
        return metrics.stream()
                .filter(metric -> metric.findElement(TITLE_METRIC).getText().equals(metricName))
                .anyMatch(metric -> isElementVisible(className("s-lockIcon"), metric));
    }

    public boolean isMetricEditable(String metricName) {
        return metrics.stream()
                .filter(metric -> metric.findElement(TITLE_METRIC).getText().equals(metricName))
                .anyMatch(metric -> isElementVisible(tagName("input"), metric));
    }

    public boolean isEmpty() {
        return !waitForElementPresent(By.id("metricsTable"), browser).isDisplayed();
    }

    public boolean isPermissionButtonEnabled() {
        return !waitForElementVisible(permissionButton).getAttribute("class").contains("disabled");
    }

    public MetricPage setEditingPermission(String metricName, PermissionType permissionType) {
        metrics.stream()
                .filter(metric -> metric.findElement(TITLE_METRIC).getText().equals(metricName))
                .map(metric -> metric.findElement(tagName("input")))
                .findFirst()
                .get()
                .click();
        waitForElementEnabled(permissionButton).click();
        PermissionSettingDialog.getInstance(browser).setEditingPermission(permissionType).save();
        Graphene.waitGui().until(browser -> permissionButton.getAttribute("class").contains("disabled"));
        return this;
    }

    public MetricEditorDialog openMetricEditor() {
        waitForElementVisible(createMetricButton).click();

        return MetricEditorDialog.getInstance(browser);
    }

    public void clickMetricOwner(String metricName) {
        metrics.stream()
                .filter(metric -> metric.findElement(TITLE_METRIC).getText().equals(metricName))
                .map(metric -> metric.findElement(cssSelector(".author a")))
                .findFirst()
                .get()
                .click();
    }

    public UserProfilePage openMetricOwnerProfilePage(String metricName) {
        clickMetricOwner(metricName);
        waitForUserProfilePageLoaded(browser);
        return UserProfilePage.getInstance(browser);
    }

    public String getTooltipFromLockIcon(String metricName) {
        WebElement lockIcon = metrics.stream()
                .filter(metric -> metric.findElement(TITLE_METRIC).getText().equals(metricName))
                .map(metric -> metric.findElement(className("s-lockIcon")))
                .findAny()
                .get();
        return waitForElementVisible(lockIcon).getAttribute("title");
    }

    private MetricPage backToMetricsTable() {
        browser.switchTo().defaultContent();
        waitForElementNotPresent(METRIC_EDITOR_LOCATOR);
        sleepTightInSeconds(2);
        MetricDetailsPage.getInstance(browser).clickDataPageLink();
        return waitForFragmentVisible(this);
    }
}
