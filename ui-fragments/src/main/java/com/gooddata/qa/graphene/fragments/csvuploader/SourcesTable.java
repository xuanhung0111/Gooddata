package com.gooddata.qa.graphene.fragments.csvuploader;

import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.fragments.AbstractTable;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class SourcesTable extends AbstractTable {

    public WebElement getSource(final String sourceName) {
        return Iterables.tryFind(getRows(), new Predicate<WebElement>() {

            @Override
            public boolean apply(WebElement row) {
                return row.getText().contains(sourceName);
            }
        }).orNull();
    }
    
    public int getNumberOfSources() {
        return getNumberOfRows();
    }
}
