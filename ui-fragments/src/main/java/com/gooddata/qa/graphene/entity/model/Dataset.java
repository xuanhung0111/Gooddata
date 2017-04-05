package com.gooddata.qa.graphene.entity.model;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.List;

public class Dataset {

    private String name;

    private List<String> attributes;
    private List<String> facts;

    public Dataset(String name) {
        this.name = name;
        this.attributes = new ArrayList<>();
        this.facts = new ArrayList<>();
    }

    public Dataset withAttributes(String... attributes) {
        this.attributes.addAll(asList(attributes));
        return this;
    }

    public Dataset withFacts(String... facts) {
        this.facts.addAll(asList(facts));
        return this;
    }

    public String buildMaql() {
        return new StringBuilder()
                .append("CREATE FOLDER {dim.${dataset}} VISUAL(TITLE \"${dataset}\") TYPE ATTRIBUTE;")
                .append("CREATE FOLDER {ffld.${dataset}} VISUAL(TITLE \"${dataset}\") TYPE FACT;")
                .append("CREATE DATASET {dataset.${dataset}} VISUAL(TITLE \"${dataset}\");")
                .append("CREATE ATTRIBUTE {attr.${dataset}.factsof} VISUAL(TITLE \"Records of ${dataset}\", "
                        + "FOLDER {dim.${dataset}}) AS KEYS {f_${dataset}.id} FULLSET;")
                .append("ALTER DATASET {dataset.${dataset}} ADD {attr.${dataset}.factsof};")
                .append(buildAttributes())
                .append(buildFacts())
                .append("SYNCHRONIZE {dataset.${dataset}};")
                .toString()
                .replace("${dataset}", getName());
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
}
