package com.gooddata.qa.graphene.entity.model;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

public class Dataset {

    private String name;

    private String primarykey ;
    private List<String> attributes;
    private List<String> facts;
    private List<Pair<String, String>> labelOfAttributes;
    private List<Pair<String, String>> defaultLabelOfAttributes;
    private List<String> grains;

    public Dataset(String name) {
        this.name = name;
        this.grains = new ArrayList<>();
        this.primarykey = null;
        this.attributes = new ArrayList<>();
        this.labelOfAttributes = new ArrayList<>();
        this.defaultLabelOfAttributes = new ArrayList<>();
        this.facts = new ArrayList<>();
    }

    public Dataset withAttributes(String... attributes) {
        this.attributes.addAll(asList(attributes));
        return this;
    }

    public Dataset withPrimaryKey(String primarykey) {
        this.primarykey = primarykey;
        return this;
    }

    public Dataset withFacts(String... facts) {
        this.facts.addAll(asList(facts));
        return this;
    }

    public Dataset withGrain(String... grains) {
        this.grains.addAll(asList(grains));
        return this;
    }

    //add label with format Pair.of(attribute,label)
    public Dataset withLabelOfAtrribute(Pair<String, String> labelOfAttribute) {
        this.labelOfAttributes.add(labelOfAttribute);
        return this;
    }

    //add label with format Pair.of(attribute,defaultLabel)
    public Dataset withDefaultLabelOfAtrribute(Pair<String, String> defaultLabelOfAttribute) {
        this.defaultLabelOfAttributes.add(defaultLabelOfAttribute);
        return this;
    }

    public String buildMaql() {
        return new StringBuilder()
                .append("CREATE FOLDER {dim.${dataset}} VISUAL(TITLE \"${dataset}\") TYPE ATTRIBUTE;")
                .append("CREATE FOLDER {ffld.${dataset}} VISUAL(TITLE \"${dataset}\") TYPE FACT;")
                .append("CREATE DATASET {dataset.${dataset}} VISUAL(TITLE \"${dataset}\");")
                .append("CREATE ATTRIBUTE {attr.${dataset}.factsof} VISUAL(TITLE \"Records of ${dataset}\","
                        + "FOLDER {dim.${dataset}}) AS KEYS {f_${dataset}.id} FULLSET;")
                .append("ALTER DATASET {dataset.${dataset}} ADD {attr.${dataset}.factsof};")
                .append(buildAttributes())
                .append(buildFacts())
                .append("SYNCHRONIZE {dataset.${dataset}};")
                .toString()
                .replace("${dataset}", getName());
    }

    public String buildMaqlUsingPrimaryKey() {
        return new StringBuilder()
                .append("CREATE FOLDER {dim.${dataset}} VISUAL(TITLE \"${dataset}\") TYPE ATTRIBUTE;")
                .append("CREATE FOLDER {ffld.${dataset}} VISUAL(TITLE \"${dataset}\") TYPE FACT;")
                .append("CREATE DATASET {dataset.${dataset}} VISUAL(TITLE \"${dataset}\");")
                .append(buildAttributes())
                .append(buildFacts())
                .append(primarykey == null ? "" : buildPrimaryKey())
                .append(labelOfAttributes.isEmpty() ? "" : buildLabels())
                .append(defaultLabelOfAttributes.isEmpty() ? "" : buildDefaultLabels())
                .append("SYNCHRONIZE {dataset.${dataset}};")
                .toString()
                .replace("${dataset}", getName());
    }

    public String buildMaqlUsingPrimaryKeyNoMainLabel() {
        return new StringBuilder()
                .append("CREATE FOLDER {dim.${dataset}} VISUAL(TITLE \"${dataset}\") TYPE ATTRIBUTE;")
                .append("CREATE FOLDER {ffld.${dataset}} VISUAL(TITLE \"${dataset}\") TYPE FACT;")
                .append("CREATE DATASET {dataset.${dataset}} VISUAL(TITLE \"${dataset}\");")
                .append(buildAttributes())
                .append(buildFacts())
                .append(primarykey == null ? "" : buildPrimaryKeyNoMainLabel())
                .append(labelOfAttributes.isEmpty() ? "" : buildLabels())
                .append(defaultLabelOfAttributes.isEmpty() ? "" : buildDefaultLabels())
                .append("SYNCHRONIZE {dataset.${dataset}};")
                .toString()
                .replace("${dataset}", getName());
    }

    public String buildMaqlFactTableGrain() {
        return new StringBuilder()
                .append("CREATE FOLDER {dim.${dataset}} VISUAL(TITLE \"${dataset}\") TYPE ATTRIBUTE;")
                .append("CREATE FOLDER {ffld.${dataset}} VISUAL(TITLE \"${dataset}\") TYPE FACT;")
                .append("CREATE DATASET {dataset.${dataset}} VISUAL(TITLE \"${dataset}\");")
                .append(buildAttributes())
                .append("CREATE ATTRIBUTE {attr.${dataset}.factsof} VISUAL(TITLE \"Records of ${dataset}\","
                        + "FOLDER {dim.${dataset}}) AS KEYS {f_${dataset}.id} PRIMARY SET GRAIN")
                .append(grains == null ? ";" : buildGrains() + ";")
                .append("ALTER DATASET {dataset.${dataset}} ADD {attr.${dataset}.factsof};")
                .append(buildFacts())
                .append("SYNCHRONIZE {dataset.${dataset}};")
                .toString()
                .replace("${dataset}", getName());
    }

    public String buildPrimaryKey() {
        return new StringBuilder()
                .append("CREATE ATTRIBUTE {attr.${dataset}.${primarykey}} VISUAL(TITLE \"${primarykey}\", FOLDER {dim.${dataset}})"
                        + " AS KEYS {f_${dataset}.id} FULLSET;")
                .append("ALTER DATASET {dataset.${dataset}} ADD {attr.${dataset}.${primarykey}};")
                .append("ALTER ATTRIBUTE {attr.${dataset}.${primarykey}} ADD LABELS {label.${dataset}.${primarykey}}"
                        + "VISUAL(TITLE \"${primarykey}\") AS {f_${dataset}.nm_${primarykey}};")
                .toString()
                .replace("${dataset}", getName())
                .replace("${primarykey}", primarykey);
    }

    private String getName() {
        return name;
    }

    private String buildAttribute(String attribute) {
        return new StringBuilder()
                .append("CREATE ATTRIBUTE {attr.${dataset}.${attribute}} VISUAL(TITLE \"${attribute}\", "
                        + "FOLDER {dim.${dataset}}) AS KEYS {d_${dataset}_${attribute}.id} FULLSET, "
                        + "{f_${dataset}.${attribute}_id};")
                .append("ALTER DATASET {dataset.${dataset}} ADD {attr.${dataset}.${attribute}};")
                .append("ALTER ATTRIBUTE {attr.${dataset}.${attribute}} ADD LABELS {label.${dataset}.${attribute}} "
                        + "VISUAL(TITLE \"${attribute}\") AS {d_${dataset}_${attribute}.nm_${attribute}};")
                .toString()
                .replace("${dataset}", getName())
                .replace("${attribute}", attribute);
    }

    public String buildDefaultLabels() {
        return defaultLabelOfAttributes.stream().map(this::buildDefaultLabel).collect(joining());
    }

    private String buildAttributes() {
        return attributes.stream().map(this::buildAttribute).collect(joining());
    }

    private String buildFact(String fact) {
        return new StringBuilder()
                .append("CREATE FACT {fact.${dataset}.${fact}} VISUAL(TITLE \"${fact}\", FOLDER {ffld.${dataset}}) "
                        + "AS {f_${dataset}.f_${fact}};")
                .append("ALTER DATASET {dataset.${dataset}} ADD {fact.${dataset}.${fact}};")
                .toString()
                .replace("${dataset}", getName())
                .replace("${fact}", fact);
    }

    private String buildFacts() {
        return facts.stream().map(this::buildFact).collect(joining());
    }

    private String buildGrains() {
        return grains.stream().map(this::buildGrain).collect(joining(", "));
    }

    private String buildGrain(String grain) {
        return String.format("{attr.%s.%s}",getName(), grain);
    }

    private String buildLabel(Pair<String, String> label) {
        return new StringBuilder().append(String.format(
                "ALTER ATTRIBUTE {attr.%s.%s} ADD LABELS {label.%s.%s.%s} VISUAL(TITLE \"%s\") AS {f_%s.nm_%s};", getName(),
                label.getLeft(), getName(), label.getLeft(), label.getRight(), label.getRight(), getName(), label.getRight()))
                .toString();
    }

    private String buildLabels() {
        return labelOfAttributes.stream().map(this::buildLabel).collect(joining());
    }

    private String buildDefaultLabel(Pair<String, String> label) {
        return new StringBuilder().append(String.format(
                "ALTER ATTRIBUTE {attr.%s.%s} DEFAULT LABEL {label.%s.%s.%s};", getName(),
                label.getLeft(), getName(), label.getLeft(), label.getRight())).toString();
    }

    private String buildPrimaryKeyNoMainLabel() {
        return new StringBuilder()
                .append("CREATE ATTRIBUTE {attr.${dataset}.${primarykey}} VISUAL(TITLE \"${primarykey}\", FOLDER {dim.${dataset}})"
                        + " AS KEYS {f_${dataset}.id} FULLSET;")
                .append("ALTER DATASET {dataset.${dataset}} ADD {attr.${dataset}.${primarykey}};")
                .toString()
                .replace("${dataset}", getName())
                .replace("${primarykey}", primarykey);
    }
}
