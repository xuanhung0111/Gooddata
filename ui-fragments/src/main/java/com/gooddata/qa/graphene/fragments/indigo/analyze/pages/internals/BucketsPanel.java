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
        waitForFragmentVisible(metricsBucket).addMetric(metric);
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

    public List<String> getAllCategoryNames() {
        return waitForFragmentVisible(categoriesBucket).getItemsName();
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

    public void changeGranularity(String time) {
        waitForFragmentVisible(categoriesBucket).changeGranularity(time);
    }

    public List<String> getAllGranularities() {
        return waitForFragmentVisible(categoriesBucket).getAllGranularities();
    }
}
