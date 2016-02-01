package com.gooddata.qa.graphene.entity.csvuploader;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.apache.commons.lang.WordUtils;

import com.gooddata.qa.graphene.fragments.csvuploader.DataPreviewTable.ColumnType;
import com.google.common.collect.Lists;

public class CsvFile {

    public static final List<String> PAYROLL_COLUMN_NAMES = asList("Lastname", "Firstname", "Education",
            "Position", "Department", "State", "County", "Paydate", "Amount");
    public static final List<String> PAYROLL_COLUMN_TYPES = asList("Attribute", "Attribute", "Attribute",
            "Attribute", "Attribute", "Attribute", "Attribute", "Date (Year-Month-Day)", "Measure");
    public static final long PAYROLL_DATA_ROW_COUNT = 3876;

    private final String fileName;
    private final List<String> columnNames;
    private final List<String> columnTypes;
    // number of rows with data (rows with facts)
    private final long dataRowCount;

    public CsvFile(final String fileName) {
        this(fileName, PAYROLL_COLUMN_NAMES, PAYROLL_COLUMN_TYPES, PAYROLL_DATA_ROW_COUNT);
    }

    public CsvFile(final String fileName, final List<String> columnNames, final List<String> columnTypes,
            final long dataRowCount) {
        this.fileName = fileName;
        this.columnNames = unmodifiableList(columnNames);
        this.columnTypes = unmodifiableList(columnTypes);
        this.dataRowCount = dataRowCount;
    }

    public String getFileName() {
        return this.fileName + ".csv";
    }

    public String getDatasetNameOfFirstUpload() {
        return WordUtils.capitalize(fileName.replace(".", " "));
    }

    public String getDatasetName(final long datasetIndex) {
        assertTrue(datasetIndex > 0);
        return format("%s (%s)", getDatasetNameOfFirstUpload(), datasetIndex);
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public List<String> changeColumnType(final String columnName, final ColumnType type) {
        final int columnIndex = getColumnNames().indexOf(columnName);
        final List<String> changedColumnTypes = Lists.newArrayList(columnTypes);
        changedColumnTypes.set(columnIndex, type.getVisibleText());
        return changedColumnTypes;
    }

    public List<String> getColumnTypes() {
        return columnTypes;
    }

    public long getDataRowCount() {
        return dataRowCount;
    }
}
