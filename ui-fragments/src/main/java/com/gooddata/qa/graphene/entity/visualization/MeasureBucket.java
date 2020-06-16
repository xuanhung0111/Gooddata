package com.gooddata.qa.graphene.entity.visualization;

import com.gooddata.sdk.model.md.Metric;

import java.util.UUID;

public class MeasureBucket {

    private final String localIdentifier = generateHashString();
    private String measureFilters;
    private String title;
    private Type type;
    private String objectUri;
    private boolean showInPercent;

    private MeasureBucket(String measureFilters, String title, boolean showInPercent, Type type, String objectUri) {
        this.measureFilters = measureFilters;
        this.title = title;
        this.showInPercent = showInPercent;
        this.type = type;
        this.objectUri = objectUri;
    }

    public static MeasureBucket createSimpleMeasureBucket(Metric metric) {
        return createMeasureBucket(metric, Type.MEASURES);
    }

    public static MeasureBucket createMeasureBucket(Metric metric, Type type) {
        return new MeasureBucket("", metric.getTitle(), false, type, metric.getUri());
    }

    public enum Type {
        MEASURES, SECONDARY_MEASURES, TERTIARY_MEASURES
    }

    public static MeasureBucket createMeasureBucketWithShowInPercent (Metric metric, boolean hasShowInPercent) {
        return new MeasureBucket("", metric.getTitle(),  hasShowInPercent, Type.MEASURES, metric.getUri());
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

    public boolean hasShowInPercent() {
        return showInPercent;
    }

    public Type getType() {
        return type;
    }

    public String getObjectUri() {
        return objectUri;
    }

    private String generateHashString() {
        return UUID.randomUUID().toString().substring(0, 5);
    }
}
