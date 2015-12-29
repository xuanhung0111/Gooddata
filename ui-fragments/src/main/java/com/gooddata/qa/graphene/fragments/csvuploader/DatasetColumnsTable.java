package com.gooddata.qa.graphene.fragments.csvuploader;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;

import java.util.List;

import org.openqa.selenium.By;

import com.gooddata.qa.graphene.fragments.AbstractTable;

public class DatasetColumnsTable extends AbstractTable {

    private static final By BY_DATASET_COLUMN_NAME = By.className("s-dataset-column-name");
    private static final By BY_DATASET_COLUMN_TYPE = By.className("s-dataset-column-type");

    public List<String> getColumnNames() {
        return getTableColumnValues(BY_DATASET_COLUMN_NAME);
    }

    public List<String> getColumnTypes() {
        return getTableColumnValues(BY_DATASET_COLUMN_TYPE);
    }

    private List<String> getTableColumnValues(final By by) {
        return getElementTexts(getRows(), row -> row.findElement(by));
    }

}
