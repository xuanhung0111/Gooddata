package com.gooddata.qa.graphene.fragments.csvuploader;

import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.fragments.AbstractTable;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class DatasetTable extends AbstractTable {

    public WebElement getDataset(final String datasetName) {
        WebElement dataset = Iterables.find(getRows(), new Predicate<WebElement>() {

            @Override
            public boolean apply(WebElement row) {
                return row.getText().contains(datasetName);
            }
        });
        return dataset;
    }
    
    public int getNumberOfDatasets() {
        return getNumberOfRows();
    }
}
