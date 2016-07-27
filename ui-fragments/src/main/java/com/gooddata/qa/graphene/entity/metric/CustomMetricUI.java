package com.gooddata.qa.graphene.entity.metric;

import static java.lang.String.format;
import static java.util.Arrays.asList;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;

public class CustomMetricUI {

    private String name;
    private List<String> metrics;
    private List<String> attributes;
    private List<String> attributeValues;
    private List<String> facts;

    public CustomMetricUI() {
        metrics = Lists.newArrayList();
        attributes = Lists.newArrayList();
        attributeValues = Lists.newArrayList();
        facts = Lists.newArrayList();
    }

    public CustomMetricUI withName(String name) {
        this.name = name;
        return this;
    }

    public CustomMetricUI withAttributes(String... attributes) {
        this.attributes = Lists.newArrayList(attributes);
        return this;
    }

    public CustomMetricUI addMoreAttributes(String... attributes) {
        this.attributes.addAll(asList(attributes));
        return this;
    }

    public CustomMetricUI withAttributeValues(String... attributeValues) {
        this.attributeValues = Lists.newArrayList(attributeValues);
        return this;
    }

    public CustomMetricUI addMoreAttributeValues(String... attributeValues) {
        this.attributeValues.addAll(asList(attributeValues));
        return this;
    }

    public CustomMetricUI withMetrics(String... metrics) {
        this.metrics = Lists.newArrayList(metrics);
        return this;
    }

    public CustomMetricUI addMoreMetrics(String... metrics) {
        this.metrics.addAll(asList(metrics));
        return this;
    }

    public CustomMetricUI withFacts(String... facts) {
        this.facts = Lists.newArrayList(facts);
        return this;
    }

    public CustomMetricUI addMoreFacts(String... facts) {
        this.facts.addAll(asList(facts));
        return this;
    }

    public String getName() {
        return name;
    }

    public List<String> getFacts() {
        return Lists.newArrayList(facts);
    }

    public List<String> getAttributes() {
        return Lists.newArrayList(attributes);
    }

    public List<String> getAttributeValues() {
        return Lists.newArrayList(attributeValues);
    }

    public List<String> getMetrics() {
        return Lists.newArrayList(metrics);
    }

    public String buildMaql(String maqlTemplate) {
        for (String metric : metrics) {
            maqlTemplate = maqlTemplate.replaceFirst("__metric__", metric);
        }

        for (String fact : facts) {
            maqlTemplate = maqlTemplate.replaceFirst("__fact__", fact);
        }

        for(String attr : attributes) {
            maqlTemplate = maqlTemplate.replaceFirst("__attr__", attr);
        }

        for(String value : attributeValues) {
            maqlTemplate = maqlTemplate.replaceFirst("__attrValue__", extractAttributeValue(value).getRight());
        }
        return maqlTemplate;
    }

    public static String buildAttributeValue(String attribute, String value) {
        return format("%s > %s", attribute, value);
    }

    public static Pair<String, String> extractAttributeValue(String value) {
        String[] parts = value.split(">");
        return Pair.of(parts[0].trim(), parts[1].trim());
    }
}
