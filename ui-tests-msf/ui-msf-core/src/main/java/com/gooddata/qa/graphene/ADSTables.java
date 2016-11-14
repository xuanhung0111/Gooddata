package com.gooddata.qa.graphene;

import java.util.Arrays;
import java.util.List;

import com.gooddata.qa.graphene.entity.dlui.Dataset;
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
    WITH_ERROR_MAPPING("createTableWithErrorMapping.txt", "copyTableWithErrorMapping.txt"),
    WITH_ADDITIONAL_FIELDS_LARGE_DATA(
            "createTableWithAdditionalFields.txt",
            "copyTableWithAdditionalFieldsLargeData.txt",
            "Unknown data source",
            AdditionalDatasets.PERSON_WITH_NEW_FIELDS,
            AdditionalDatasets.OPPORTUNITY_WITH_NEW_FIELDS),
    WITH_ADDITIONAL_FIELDS_AND_REFERECES(
            "createTableWithReferences.txt",
            "copyTableWithReferences.txt",
            "Unknown data source",
            AdditionalDatasets.ARTIST_WITH_NEW_FIELD,
            AdditionalDatasets.TRACK_WITH_NEW_FIELD),
    WITH_ADDITIONAL_FIELDS_AND_MULTI_REFERECES(
            "createTableWithMultiReferences.txt",
            "copyTableWithMultiReferences.txt",
            "Unknown data source",
            AdditionalDatasets.TRACK_WITH_NEW_FIELD,
            AdditionalDatasets.ARTIST_WITH_NEW_FIELD,
            AdditionalDatasets.AUTHOR_WITH_NEW_FIELD),
    WITH_ADDITIONAL_CONNECTION_POINT(
            "createTableWithAdditionalConnectionPoint.txt",
            "copyTableWithAdditionalConnectionPoint.txt",
            "Unknown data source",
            AdditionalDatasets.PERSON_WITH_NEW_FIELDS,
            AdditionalDatasets.OPPORTUNITY_WITH_NEW_FIELDS),
    WITH_ADDITIONAL_LABEL_OF_NEW_FIELD(
            "createTableWithAdditionalLabelOfNewField.txt",
            "copyTableWithAdditionalLabelOfNewField.txt",
            "Unknown data source",
            AdditionalDatasets.PERSON_WITH_NEW_FIELDS,
            AdditionalDatasets.OPPORTUNITY_WITH_NEW_FIELDS);

    private String createTableSqlFile;
    private String copyTableSqlFile;
    private String datasourceName;
    private List<AdditionalDatasets> additionalDatasets = Lists.newArrayList();

    private ADSTables(String createTableSqlFile, String copyTableSqlFile) {
        this(createTableSqlFile, copyTableSqlFile, "");
    }

    private ADSTables(String createTableSqlFile, String copyTableSqlFile,
            String datasourceName, AdditionalDatasets... datasets) {
        this.createTableSqlFile = createTableSqlFile;
        this.copyTableSqlFile = copyTableSqlFile;
        this.datasourceName = datasourceName;
        this.additionalDatasets = Arrays.asList(datasets);
    }

    public List<Dataset> getDatasets() {
        List<Dataset> datasets = Lists.newArrayList();
        for (AdditionalDatasets additionalDataset : this.additionalDatasets) {
            datasets.add(additionalDataset.getDataset());
        }
        return datasets;
    }

    public String getCreateTableSqlFile() {
        return createTableSqlFile;
    }

    public String getCopyTableSqlFile() {
        return copyTableSqlFile;
    }

    public String getDatasourceName() {
        return datasourceName;
    }
}
