package com.gooddata.qa.graphene.entity.attribute;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;

public class ComputedAttributeDefinition {

    private String name;
    private String attribute;
    private String metric;
    private List<AttributeBucket> buckets = new ArrayList<>();

    public ComputedAttributeDefinition withName(String name) {
        this.name = name;
        return this;
    }

    public ComputedAttributeDefinition withAttribute(String attribute) {
        this.attribute = attribute;
        return this;
    }

    public ComputedAttributeDefinition withMetric(String metric) {
        this.metric = metric;
        return this;
    }

    public ComputedAttributeDefinition withBucket(AttributeBucket bucket) {
        this.buckets.add(bucket);
        return this;
    }

    public String getName() {
        return name;
    }

    public String getAttribute() {
        return attribute;
    }

    public String getMetric() {
        return metric;
    }

    public List<AttributeBucket> getBuckets() {
        return unmodifiableList(buckets);
    }

    public static class AttributeBucket {
        private int index;
        private String name;
        private String[] value;

        public AttributeBucket(int index, String name, String... value) {
            this.index = index;
            this.name = name;
            this.value = value;
        }

        public int getIndex() {
            return index;
        }

        public String getName() {
            return name;
        }

        public String[] getValue() {
            return value;
        }
    }
}
