package com.gooddata.qa.graphene.entity.model;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.math.NumberUtils.isNumber;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;

public class AdsTable {
    private String name;
    private List<String> attributes;
    private List<String> facts;
    private boolean hasTimeStamp;
    private CsvFile dataFile;

    public AdsTable(String name) {
        this.name = name;
        this.attributes = new ArrayList<>();
        this.facts = new ArrayList<>();
        this.hasTimeStamp = false;
        this.dataFile = null;
    }

    public AdsTable withAttributes(String... attributes) {
        this.attributes.addAll(asList(attributes));
        return this;
    }

    public AdsTable withFacts(String... facts) {
        this.facts.addAll(asList(facts));
        return this;
    }

    public AdsTable hasTimeStamp(boolean hasTimeStamp) {
        this.hasTimeStamp = hasTimeStamp;
        return this;
    }

    public AdsTable withDataFile(CsvFile dataFile) {
        this.dataFile = dataFile;
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
                .append(hasTimeStamp == true ? ", x__timestamp TIMESTAMP ENCODING RLE" : "")
                .append(");")
                .toString()
                .replace("${table}", getName());
    }

    private String insertData() {
        List<List<String>> data = new ArrayList<>();

        try (final CsvListReader reader = new CsvListReader(new FileReader(dataFile.getFilePath()),
                CsvPreference.STANDARD_PREFERENCE)) {
            reader.getHeader(true);
            List<String> row;
            while ((row = reader.read()) != null) {
                data.add(row);
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException("Csv file: " + dataFile.getFilePath() + " not found!");
        } catch (IOException e) {
            throw new RuntimeException("There has an error when reading file");
        }

        return data.stream()
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
