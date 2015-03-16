package com.gooddata.qa.graphene.enums;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

public enum ADSTables {

    WITHOUT_ADDITIONAL_FIELDS("createTable.txt", "copyTable.txt", "Unknown data source"),
    WITH_ADDITIONAL_FIELDS(
            "createTableWithAdditionalFields.txt",
            "copyTableWithAdditionalFields.txt",
            "Unknown data source",
            AdditionalDatasets.PERSON_WITH_NEW_FIELDS,
            AdditionalDatasets.OPPORTUNITY_WITH_NEW_FIELDS),
    WITH_ADDITIONAL_DATE(
            "createTableWithAdditionalDate.txt",
            "copyTableWithAdditionalDate.txt",
            "Unknown data source",
            AdditionalDatasets.PERSON_WITH_NEW_DATE_FIELD,
            AdditionalDatasets.OPPORTUNITY_WITH_NEW_FIELDS),
    WITH_ERROR_MAPPING("createTableWithErrorMapping.txt", "copyTableWithErrorMapping.txt");

    private String createTableSqlFile;
    private String copyTableSqlFile;
    private String datasourceName;
    private List<AdditionalDatasets> additionalDatasets = Lists.newArrayList();

    private ADSTables(String createTableSqlFile, String copyTableSqlFile) {
        this(createTableSqlFile, copyTableSqlFile, "");
    }

    private ADSTables(String createTableSqlFile, String copyTableSqlFile, String datasourceName,
            AdditionalDatasets... datasets) {
        this.createTableSqlFile = createTableSqlFile;
        this.copyTableSqlFile = copyTableSqlFile;
        this.datasourceName = datasourceName;
        this.additionalDatasets = Arrays.asList(datasets);
    }

    public String getCreateTableSqlFile() {
        return this.createTableSqlFile;
    }

    public String getCopyTableSqlFile() {
        return this.copyTableSqlFile;
    }

    public String getDatasourceName() {
        return this.datasourceName;
    }

    public List<AdditionalDatasets> getAdditionalDatasets() {
        return Lists.newArrayList(this.additionalDatasets);
    }
}
