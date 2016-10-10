package com.gooddata.qa.graphene.entity.visualization;

import java.util.List;

import com.gooddata.qa.graphene.enums.indigo.ReportType;

public class InsightMDConfiguration {

    private String title;
    private ReportType type;
    private List<MeasureBucket> measureBuckets;
    private List<CategoryBucket> categoryBuckets;

    public InsightMDConfiguration(String title, ReportType type) {
        this.title = title;
        this.type = type;
    }

    public InsightMDConfiguration setMeasureBucket(List<MeasureBucket> measureBuckets) {
        this.measureBuckets = measureBuckets;
        return this;
    }

    public InsightMDConfiguration setCategoryBucket(List<CategoryBucket> categoryBuckets) {
        this.categoryBuckets = categoryBuckets;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public ReportType getType() {
        return type;
    }

    public List<CategoryBucket> getCategoryBuckets() {
        return categoryBuckets;
    }

    public List<MeasureBucket> getMeasureBuckets() {
        return measureBuckets;
    }
}
