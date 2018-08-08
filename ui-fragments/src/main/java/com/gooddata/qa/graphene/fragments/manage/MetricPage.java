package com.gooddata.qa.graphene.fragments.manage;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDataPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementEnabled;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForObjectPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForUserProfilePageLoaded;
import static java.lang.String.format;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.tagName;
import static org.openqa.selenium.By.xpath;

import com.gooddata.qa.graphene.entity.metric.CustomMetricUI;
import com.gooddata.qa.graphene.enums.metrics.MetricTypes;
import com.gooddata.qa.graphene.fragments.common.PermissionSettingDialog;
import com.gooddata.qa.graphene.fragments.common.PermissionSettingDialog.PermissionType;
import com.gooddata.qa.graphene.fragments.profile.UserProfilePage;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class MetricPage extends DataPage {

    @FindBy(className = "s-btn-create_metric")
    private WebElement createMetricButton;

    @FindBy(css = ".s-btn-edit")
    private WebElement editButton;

    @FindBy(xpath = "//a[@class='interpolateProject']")
    private WebElement dataLink;

    @FindBy(className = "s-btn-permissions___")
    private WebElement permissionButton;

    @FindBy(className = "none")
    private WebElement noneButton;

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

    public boolean isMetricLocked(String metricName) {
        return isElementVisible(className("s-lockIcon"), getMetricEntry(metricName));
    }

    public boolean isPrivateMetric(String metricName) {
        return isElementVisible(className("s-unlistedIcon"), getMetricEntry(metricName));
    }

    public boolean isMetricEditable(String metricName) {
        return isElementVisible(tagName("input"), getMetricEntry(metricName));
    }

    public boolean isEmpty() {
        return !waitForElementPresent(By.id("metricsTable"), browser).isDisplayed();
    }

    public boolean isPermissionButtonEnabled() {
        return !waitForElementVisible(permissionButton).getAttribute("class").contains("disabled");
    }

    public MetricPage setEditingPermission(String metricName, PermissionType permissionType) {
        selectMetricsAndOpenPermissionDialog(metricName).setEditingPermission(permissionType).save();
        Graphene.waitGui().until(browser -> permissionButton.getAttribute("class").contains("disabled"));
        return this;
    }

    public MetricPage setVisibility(boolean visible, String... metricNames) {
        selectMetricsAndOpenPermissionDialog(metricNames).setVisibility(visible).save();
        Graphene.waitGui().until(browser -> permissionButton.getAttribute("class").contains("disabled"));
        return this;
    }

    public PermissionSettingDialog selectMetricsAndOpenPermissionDialog(String... metricNames) {
        waitForElementVisible(noneButton).click();
        Graphene.waitGui().until(browser -> permissionButton.getAttribute("class").contains("disabled"));
        for (String metricName: metricNames) {
            getMetricEntry(metricName).findElement(tagName("input")).click();
        }
        waitForElementEnabled(permissionButton).click();
        return PermissionSettingDialog.getInstance(browser);
    }

    public MetricPage waitForListMetricLoading() {
        By loadingElementLocator = cssSelector("#metricsTable .loadingWheel");
        try {
            waitForElementVisible(loadingElementLocator, getRoot(), 3);
            waitForElementNotVisible(loadingElementLocator);
        } catch (TimeoutException e) {
            log.info("List metric already loaded so WebDriver unable to catch the loading indicator");
        }
        return this;
    }

    public MetricEditorDialog openMetricEditor() {
        waitForElementVisible(createMetricButton).click();
        return MetricEditorDialog.getInstance(browser);
    }

    public void clickMetricOwner(String metricName) {
        getMetricEntry(metricName).findElement(cssSelector(".author a")).click();
    }

    public UserProfilePage openMetricOwnerProfilePage(String metricName) {
        clickMetricOwner(metricName);
        waitForUserProfilePageLoaded(browser);
        return UserProfilePage.getInstance(browser);
    }

    public String getTooltipFromLockIcon(String metricName) {
        WebElement lockIcon = getMetricEntry(metricName).findElement(className("s-lockIcon"));
        return waitForElementVisible(lockIcon).getAttribute("title");
    }

    private WebElement getMetricEntry(String metricName) {
        return waitForElementVisible(xpath(format("//span[@title = '%s']/../../..", metricName)), getRoot());
    }

    private MetricPage backToMetricsTable() {
        browser.switchTo().defaultContent();
        waitForElementNotPresent(METRIC_EDITOR_LOCATOR);
        sleepTightInSeconds(2);
        MetricDetailsPage.getInstance(browser).clickDataPageLink();
        return waitForFragmentVisible(this);
    }
}
