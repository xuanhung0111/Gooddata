package com.gooddata.qa.graphene.entity.visualization;

import com.gooddata.md.Metric;

import java.util.UUID;

public class MeasureBucket {

    private final String localIdentifier = generateHashString();
    private String measureFilters;
    private String title;
    private String type;
    private String objectUri;
    private boolean showPoP;
    private boolean showInPercent;

    private MeasureBucket(String measureFilters, String title, boolean showPoP, boolean showInPercent, String type,
            String objectUri) {
        this.measureFilters = measureFilters;
        this.title = title;
        this.showPoP = showPoP;
        this.showInPercent = showInPercent;
        this.type = type;
        this.objectUri = objectUri;
    }

    public static MeasureBucket createSimpleMeasureBucket(Metric metric) {
        return new MeasureBucket("", metric.getTitle(), false, false, "metric", metric.getUri());
    }

    public static MeasureBucket createMeasureBucketWithShowInPercent (Metric metric, boolean hasShowInPercent) {
        return new MeasureBucket("", metric.getTitle(), false, hasShowInPercent, "metric", metric.getUri());
    }

    public String getLocalIdentifier() {
        return localIdentifier;
    }

    public String getMeasureFilters() {
        return measureFilters;
    }

    public String getTitle() {
        return title;
    }

    public boolean hasShowPoP() {
        return showPoP;
    }

    public boolean hasShowInPercent() {
        return showInPercent;
    }

    public String getType() {
        return type;
    }

    public String getObjectUri() {
        return objectUri;
    }

    private String generateHashString() {
        return UUID.randomUUID().toString().substring(0, 5);
    }
}
