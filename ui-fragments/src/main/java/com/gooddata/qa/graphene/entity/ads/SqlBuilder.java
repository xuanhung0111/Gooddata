package com.gooddata.qa.graphene.entity.ads;

import static java.util.stream.Collectors.joining;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;

import java.util.ArrayList;
import java.util.List;

public class SqlBuilder {

    private List<AdsTable> adsTable;

    public SqlBuilder() {
        this.adsTable = new ArrayList<>();
    }

    public static String loadFromFile(String filePath) {
        return getResourceAsString(filePath);
    }

    public SqlBuilder withAdsTable(AdsTable table) {
        this.adsTable.add(table);
        return this;
    }

    public String build() {
        return adsTable.stream().map(AdsTable::buildSql).collect(joining());
    }
}
