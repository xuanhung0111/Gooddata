package com.gooddata.qa.graphene.entity.ads;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.math.NumberUtils.isNumber;

import java.util.ArrayList;
import java.util.List;

public class AdsTable {

    private String name;
    private List<String> attributes;
    private List<String> facts;
    private boolean hasClientId;
    private boolean hasTimeStamp;
    private List<List<String>> dataToInsert;

    public AdsTable(String name) {
        this.name = name;
        this.attributes = new ArrayList<>();
        this.facts = new ArrayList<>();
        this.hasClientId = false;
        this.hasTimeStamp = false;
        this.dataToInsert = null;
    }

    public AdsTable withAttributes(String... attributes) {
        this.attributes.addAll(asList(attributes));
        return this;
    }

    public AdsTable withFacts(String... facts) {
        this.facts.addAll(asList(facts));
        return this;
    }

    public AdsTable hasClientId(boolean hasClientId) {
        this.hasClientId = hasClientId;
        return this;
    }

    public AdsTable hasTimeStamp(boolean hasTimeStamp) {
        this.hasTimeStamp = hasTimeStamp;
        return this;
    }

    public AdsTable withData(List<List<String>> dataToInsert) {
        this.dataToInsert = dataToInsert;
        return this;
    }

    public String buildSql() {
        return new StringBuilder()
                .append(createTable())
                .append(insertData())
                .toString();
    }

    private String getName() {
        return name;
    }

    private String createTable() {
        return new StringBuilder()
                .append("DROP TABLE IF EXISTS ${table};")
                .append("CREATE TABLE ${table}(")
                .append(attributes.stream().map(attr -> attr.replace(attr, "a__" + attr + " VARCHAR(128)"))
                        .collect(joining(", ")))
                .append(", " + facts.stream().map(fact -> fact.replace(fact, "f__" + fact + " NUMERIC(12,2)"))
                        .collect(joining(", ")))
                .append(hasClientId == true ? ", x__client_id VARCHAR(128) ENCODING RLE" : "")
                .append(hasTimeStamp == true ? ", x__timestamp TIMESTAMP ENCODING RLE" : "")
                .append(");")
                .toString()
                .replace("${table}", getName());
    }

    private String insertData() {
        System.out.println("Data of Ads table: " + getName());
        dataToInsert.stream().forEach(System.out::println);

        return dataToInsert.stream()
                .map(this::parseDataInRowAsCorrectType)
                .map(row -> "INSERT into ${table} values (" + row.stream().collect(joining(", ")) + ");")
                .collect(joining())
                .replace("${table}", getName());
    }

    private List<String> parseDataInRowAsCorrectType(List<String> dataRow) {
        return dataRow.stream().map(data -> {
            if (isNumber(data)) return data;
            return "'" + data + "'";
        }).collect(toList());
    }
}
