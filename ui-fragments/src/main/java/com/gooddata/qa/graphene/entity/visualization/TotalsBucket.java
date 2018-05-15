package com.gooddata.qa.graphene.entity.visualization;

import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport.AggregationItem;

public class TotalsBucket {

    private String measureIdentifier;
    private String attributeIdentifier;
    private AggregationItem type;

    private TotalsBucket(String measureIdentifier, String attributeIdentifier, AggregationItem type) {
        this.measureIdentifier = measureIdentifier;
        this.attributeIdentifier = attributeIdentifier;
        this.type = type;
    }

    public String getMeasureIdentifier() {
        return measureIdentifier;
    }

    public String getAttributeIdentifier() {
        return attributeIdentifier;
    }

    public String getType() {
        return type.getShortenedName();
    }

    public static TotalsBucket createTotals(MeasureBucket measureBuckets, CategoryBucket categoryBucket, AggregationItem type) {
        return new TotalsBucket(measureBuckets.getLocalIdentifier(), categoryBucket.getLocalIdentifier(), type);
    }
}
