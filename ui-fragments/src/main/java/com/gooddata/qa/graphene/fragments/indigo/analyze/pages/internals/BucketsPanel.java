package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;

import java.util.Collection;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class BucketsPanel extends AbstractFragment {

    private static final String STACKS_BUCKET_LOCATOR = "s-bucket-stacks";

    @FindBy(css = ".s-visualization-picker")
    private VisualizationReportTypePicker reportTypePicker;

    @FindBy(css = ".s-bucket-metrics")
    private MetricsBucket metricsBucket;

    @FindBy(css = ".s-bucket-categories")
    private CategoriesBucket categoriesBucket;

    @FindBy(className = STACKS_BUCKET_LOCATOR)
    private StacksBucket stacksBucket;

    public void addMetric(WebElement metric) {
        waitForFragmentVisible(metricsBucket).addMetric(metric);
    }

    public void addMetricFromFact(WebElement fact) {
        waitForFragmentVisible(metricsBucket).addMetricFromFact(fact);
    }

    public void addMetricFromAttribute(WebElement attribute) {
        waitForFragmentVisible(metricsBucket).addMetricFromAttribute(attribute);
    }

    public void removeMetric(String metric) {
        waitForFragmentVisible(metricsBucket).removeMetric(metric);
    }

    public void addCategory(WebElement category) {
        waitForFragmentVisible(categoriesBucket).addCategory(category);
    }

    public void removeCategory(String category) {
        waitForFragmentVisible(categoriesBucket).removeCategory(category);
    }

    public void setReportType(ReportType type) {
        waitForFragmentVisible(reportTypePicker).setReportType(type);
    }

    public boolean isReportTypeSelected(ReportType type) {
        return waitForFragmentVisible(reportTypePicker).isSelected(type);
    }

    public void turnOnShowInPercents() {
        waitForFragmentVisible(metricsBucket).turnOnShowInPercents();
    }

    public boolean isBlankState() {
        waitForFragmentVisible(categoriesBucket);
        waitForFragmentVisible(metricsBucket);
        return metricsBucket.isEmpty() && categoriesBucket.isEmpty();
    }

    public List<String> getAllAddedCategoryNames() {
        return waitForFragmentVisible(categoriesBucket).getItemNames();
    }

    public List<String> getAllAddedMetricNames() {
        return waitForFragmentVisible(metricsBucket).getItemNames();
    }

    public boolean isShowPercentConfigEnabled() {
        return waitForFragmentVisible(metricsBucket).isShowPercentConfigEnabled();
    }

    public boolean isCompareSamePeriodConfigEnabled() {
        return waitForFragmentVisible(metricsBucket).isCompareSamePeriodConfigEnabled();
    }

    public boolean isShowPercentConfigSelected() {
        return waitForFragmentVisible(metricsBucket).isShowPercentConfigSelected();
    }

    public boolean isCompareSamePeriodConfigSelected() {
        return waitForFragmentVisible(metricsBucket).isCompareSamePeriodConfigSelected();
    }

    public void changeGranularity(String time) {
        waitForFragmentVisible(categoriesBucket).changeGranularity(time);
    }

    public List<String> getAllGranularities() {
        return waitForFragmentVisible(categoriesBucket).getAllGranularities();
    }

    public boolean isMetricBucketEmpty() {
        return waitForFragmentVisible(metricsBucket).isEmpty();
    }

    public boolean isCategoryBucketEmpty() {
        return waitForFragmentVisible(categoriesBucket).isEmpty();
    }

    public String getSelectedDimensionSwitch() {
        return waitForFragmentVisible(categoriesBucket).getSelectedDimensionSwitch();
    }

    public void changeDimensionSwitchInBucket(String dimensionSwitch) {
        waitForFragmentVisible(categoriesBucket).changeDimensionSwitchInBucket(dimensionSwitch);
    }

    public void compareToSamePeriodOfYearBefore() {
        waitForFragmentVisible(metricsBucket).compareToSamePeriodOfYearBefore();
    }

    public void addStackBy(WebElement category) {
        waitForFragmentVisible(stacksBucket).addCategory(category);
    }

    public void replaceMetric(String oldMetric, WebElement newMetric) {
        waitForFragmentVisible(metricsBucket).replaceMetric(oldMetric, newMetric);
    }

    public void replaceCategory(String oldCategory, WebElement newCategory) {
        waitForFragmentVisible(categoriesBucket).replaceCategory(oldCategory, newCategory);
    }

    public void replaceStackBy(WebElement category) {
        waitForFragmentVisible(stacksBucket).replaceStackBy(category);
    }

    public boolean isStackByDisabled() {
        return waitForFragmentVisible(stacksBucket).isStackByDisabled();
    }

    public String getStackByMessage() {
        return waitForFragmentVisible(stacksBucket).getStackByMessage();
    }

    public String getMetricMessage() {
        return waitForFragmentVisible(metricsBucket).getMetricMessage();
    }

    public String getAddedStackByName() {
        return waitForFragmentVisible(stacksBucket).getAddedStackByName();
    }

    public boolean isStackByBucketEmpty() {
        if (browser.findElements(By.className(STACKS_BUCKET_LOCATOR)).isEmpty()) {
            return true;
        }
        return waitForFragmentVisible(stacksBucket).isEmpty();
    }

    public void switchAxisAndStackBy() {
        stacksBucket.replaceStackBy(categoriesBucket.getFirstItem());
    }

    public String getMetricAggregation(String metric) {
        return waitForFragmentVisible(metricsBucket).getMetricAggregation(metric);
    }

    public String getSelectedGranularity() {
        return waitForFragmentVisible(categoriesBucket).getSelectedGranularity();
    }

    public Collection<String> getAllMetricAggregations(String metric) {
        return waitForFragmentVisible(metricsBucket).getAllMetricAggregations(metric);
    }

    public void changeMetricAggregation(String metric, String newAggregation) {
        waitForFragmentVisible(metricsBucket).changeMetricAggregation(metric, newAggregation);
    }

    public boolean isMetricConfigurationCollapsed(String metric) {
        return waitForFragmentVisible(metricsBucket).isMetricConfigurationCollapsed(metric);
    }

    public void expandMetricConfiguration(String metric) {
        waitForFragmentVisible(metricsBucket).expandMetricConfiguration(metric);
    }

    public void collapseMetricConfiguration(String metric) {
        waitForFragmentVisible(metricsBucket).collapseMetricConfiguration(metric);
    }

    public void addFilterMetric(String metric, String attribute, String... values) {
        waitForFragmentVisible(metricsBucket).addFilterMetric(metric, attribute, values);
    }

    public void addFilterMetricBySelectOnly(String metric, String attribute, String value) {
        waitForFragmentVisible(metricsBucket).addFilterMetricBySelectOnly(metric, attribute, value);
    }

    public String getFilterMetricText(String metric) {
        return waitForFragmentVisible(metricsBucket).getFilterMetricText(metric);
    }

    public boolean canAddAnotherAttributeFilterToMetric(String metric) {
        return waitForFragmentVisible(metricsBucket).canAddAnotherAttributeFilterToMetric(metric);
    }

    public void removeAttributeFilterFromMetric(String metric) {
        waitForFragmentVisible(metricsBucket).removeAttributeFilterFromMetric(metric);
    }

    public String getAttributeDescription(String metric, String attribute) {
        return waitForFragmentVisible(metricsBucket).getAttributeDescription(metric, attribute);
    }
}
