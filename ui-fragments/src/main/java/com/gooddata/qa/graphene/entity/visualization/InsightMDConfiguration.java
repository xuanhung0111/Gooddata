package com.gooddata.qa.graphene.entity.visualization;

import java.util.List;

import com.gooddata.qa.graphene.enums.indigo.ReportType;

import java.util.ArrayList;

public class InsightMDConfiguration {

    private String title;
    private ReportType type;
    private List<MeasureBucket> measureBuckets = new ArrayList<MeasureBucket>();
    private List<CategoryBucket> categoryBuckets = new ArrayList<CategoryBucket>();
    private List<TotalsBucket> totalsBuckets = new ArrayList<TotalsBucket>();

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

    public InsightMDConfiguration setTotalsBucket(List<TotalsBucket> totalsBuckets) {
        this.totalsBuckets = totalsBuckets;
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

    public List<TotalsBucket> getTotalsBuckets() {
        return totalsBuckets;
    }
}
