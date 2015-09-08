package com.gooddata.qa.graphene.fragments.csvuploader;

import static java.util.stream.Collectors.toList;

import com.gooddata.qa.graphene.fragments.AbstractTable;

import org.openqa.selenium.By;

import java.util.List;

public class DatasetColumnsTable extends AbstractTable {

    private static final By BY_DATASET_COLUMN_NAME = By.className("s-dataset-column-name");
    private static final By BY_DATASET_COLUMN_TYPE = By.className("s-dataset-column-type");

    public List<String> getColumnNames() {
        return getTableColumnValues(BY_DATASET_COLUMN_NAME);
    }

    public List<String> getColumnTypes() {
        return getTableColumnValues(BY_DATASET_COLUMN_TYPE);
    }

    private List<String> getTableColumnValues(By by) {
        return getRows().stream()
                .map(row -> row.findElement(by).getText())
                .collect(toList());
    }

}
