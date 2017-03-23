package com.gooddata.qa.graphene.entity.csvuploader;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import com.gooddata.qa.graphene.fragments.csvuploader.DataTypeSelect.ColumnType;
import com.google.common.collect.Lists;

public class CsvFile {

    private String fileName;
    private List<Column> columns;
    private List<List<String>> data;
    private String filePath;
    private long dataRowCount;

    public CsvFile(final String fileName) {
        this.fileName = fileName;
        columns = Collections.emptyList();
        data = new ArrayList<>();
        filePath = "";
        dataRowCount = 0L;
    }

    public static CsvFile loadFile(final String filePath) {
        final File rootCsvFile = new File(filePath);
        if (!rootCsvFile.exists()) {
            throw new IllegalArgumentException("Csv file with path: [" + filePath + "] does not exist!");
        }

        final CsvFile csv = new CsvFile(rootCsvFile.getName().split("\\.csv")[0]);
        csv.filePath = filePath;

        long totalDataRow = 0;
        try (final CsvListReader reader = new CsvListReader(new FileReader(rootCsvFile),
                CsvPreference.STANDARD_PREFERENCE)) {
            final List<String> headers = asList(reader.getHeader(true));
            if (!headers.stream().anyMatch(NumberUtils::isParsable)) {
                csv.columns = headers.stream().map(Column::new).collect(toList());
            } else {
                totalDataRow++;
            }

            // filter multiple headers
            List<String> row; 
            while ((row = reader.read()) != null && !row.stream().anyMatch(NumberUtils::isParsable)) {
                csv.columns = row.stream().map(Column::new).collect(toList());
            }

            totalDataRow++;
            while (reader.read() != null) {
                totalDataRow++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        csv.dataRowCount = totalDataRow;
        return csv;
    }

    public CsvFile setColumnTypes(final String... types) {
        final Iterator<String> typesIterator = asList(types).iterator();
        final Iterator<Column> columnsIterator = columns.iterator();

        while (columnsIterator.hasNext() && typesIterator.hasNext()) {
            columnsIterator.next().type = typesIterator.next();
        }
        return this;
    }

    public CsvFile columns(final Column... cols) {
        columns = asList(cols);
        return this;
    }

    public CsvFile rows(final String... data) {
        this.data.add(asList(data));
        dataRowCount++;
        return this;
    }

    public String saveToDisc(final String path) throws IOException {
        final File file = new File(path, getFileName());
        final File parent = file.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            throw new IOException("Cannot create new folder in: " + path);
        }

        if (file.exists()) {
            file.delete();
        }

        if (!file.createNewFile()) {
            throw new IOException("Cannot create new file in: " + path);
        }

        try (final CsvListWriter writer = new CsvListWriter(new FileWriter(file),
                CsvPreference.STANDARD_PREFERENCE)) {
            final List<String> columnNames = getColumnNames();
            if (!columnNames.stream().allMatch(String::isEmpty)) {
                writer.write(columnNames);
            }

            for (final List<String> row : data) {
                writer.write(row);
            }
        }
        filePath = file.getAbsolutePath();
        return filePath;
    }

    public String getFileName() {
        return fileName + ".csv";
    }

    public List<String> getColumnNames() {
        return columns.stream().map(Column::getTitle).map(WordUtils::capitalize).collect(toList());
    }

    public List<String> getColumnTypes() {
        return columns.stream().map(Column::getType).collect(toList());
    }

    public long getDataRowCount() {
        return dataRowCount;
    }

    public String getDatasetNameOfFirstUpload() {
        return WordUtils.capitalize(fileName.replace(".", " "));
    }

    public String getDatasetName(final long datasetIndex) {
        return format("%s (%s)", getDatasetNameOfFirstUpload(), datasetIndex);
    }

    public String getFilePath() {
        return filePath;
    }

    public List<String> changeColumnType(final String columnName, final ColumnType type) {
        final int columnIndex = getColumnNames().indexOf(columnName);
        final List<String> changedColumnTypes = Lists.newArrayList(getColumnTypes());
        changedColumnTypes.set(columnIndex, type.getVisibleText());
        return changedColumnTypes;
    }

    public static class Column {
        private String title;
        private String type;

        public Column(final String title) {
            this(title, "UNKNOWN TYPE");
        }

        public Column(final String title, final String type) {
            this.title = title;
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public String getType() {
            return type;
        }
    }
}
