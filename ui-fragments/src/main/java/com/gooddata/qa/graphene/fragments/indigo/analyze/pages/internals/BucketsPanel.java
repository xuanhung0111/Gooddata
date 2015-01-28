package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForFragmentVisible;

import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class BucketsPanel extends AbstractFragment {

    @FindBy(css = ".s-visualization-picker")
    private VisualizationReportTypePicker reportTypePicker;

    @FindBy(css = ".s-bucket-metrics")
    private MetricsBucket metricsBucket;

    @FindBy(css = ".s-bucket-category")
    private CategoriesBucket categoriesBucket;

    public void addMetric(WebElement metric) {
        waitForFragmentVisible(metricsBucket);
        metricsBucket.addMetric(metric);
    }

    public void addCategory(WebElement category) {
        waitForFragmentVisible(categoriesBucket);
        categoriesBucket.addCategory(category);
    }

    public void setReportType(ReportType type) {
        waitForFragmentVisible(reportTypePicker);
        reportTypePicker.setReportType(type);
    }

    public boolean isReportTypeSelected(ReportType type) {
        waitForFragmentVisible(reportTypePicker);
        return reportTypePicker.isSelected(type);
    }

    public void turnOnShowInPercents() {
        waitForFragmentVisible(metricsBucket);
        metricsBucket.turnOnShowInPercents();
    }

    public boolean isBlankState() {
        waitForFragmentVisible(categoriesBucket);
        waitForFragmentVisible(metricsBucket);
        return metricsBucket.isEmpty() && categoriesBucket.isEmpty();
    }

    public List<String> getAllCategoryNames() {
        return categoriesBucket.getItemsName();
    }

    public boolean isShowPercentConfigEnabled() {
        waitForFragmentVisible(metricsBucket);
        return metricsBucket.isShowPercentConfigEnabled();
    }

    public boolean isCompareSamePeriodConfigEnabled() {
        waitForFragmentVisible(metricsBucket);
        return metricsBucket.isCompareSamePeriodConfigEnabled();
    }

    public boolean isShowPercentConfigSelected() {
        waitForFragmentVisible(metricsBucket);
        return metricsBucket.isShowPercentConfigSelected();
    }

    public void changeGranularity(String time) {
        waitForFragmentVisible(categoriesBucket);
        categoriesBucket.changeGranularity(time);
    }

    public List<String> getAllGranularities() {
        waitForFragmentVisible(categoriesBucket);
        return categoriesBucket.getAllGranularities();
    }
}
