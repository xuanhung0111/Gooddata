package com.gooddata.qa.graphene.entity.ads;

import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;
import static java.util.stream.Collectors.joining;

import java.util.stream.Stream;

import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;

public class SqlBuilder {

    private SqlBuilder() {
    }

    /*
     * This method will read and identify data type in CsvFile (attributes, facts, timestamp, ...) 
     * then load to an Ads table object automatically. More effective to decrease redundant steps as we must 
     * define Ads table in test repeatedly. 
     */
    public static String build(CsvFile... csvFiles) {
        return Stream.of(csvFiles).map(file -> parseResource(file)).map(AdsTable::buildSql).collect(joining());
    }

    /*
     * In some specific case, the Algorithm to parse resource from CsvFile does not work
     * properly (user wants to test some attributes with fact values or otherwise). So this method is a good 
     * helper as user can define ads table object and load data to it directly.
     */
    public static String build(AdsTable... adsTables) {
        return Stream.of(adsTables).map(AdsTable::buildSql).collect(joining());
    }

    public static String loadFromFile(String filePath) {
        return getResourceAsString(filePath);
    }

    public static String dropTables(String... tables) {
        return Stream.of(tables).map(table -> "DROP TABLE IF EXISTS " + table).collect(joining(";"));
    }

    private static AdsTable parseResource(CsvFile csvFile) {
        return new AdsTable(csvFile.getFileName().replace(".csv", ""))
                .withAttributes(csvFile.getAttributeColumns())
                .withFacts(csvFile.getFactColumns())
                .hasClientId(csvFile.hasClientIdColumn())
                .hasTimeStamp(csvFile.hasTimeStampColumn())
                .withData(csvFile.getDataRows());
    }
}
