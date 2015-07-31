package com.gooddata.qa.graphene.fragments.csvuploader;

import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.fragments.AbstractTable;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class SourcesTable extends AbstractTable {

    public WebElement getSource(final String sourceName) {
        return getRows().stream()
                .filter(row -> row.getText().contains(sourceName))
                .findFirst()
                .orElse(null);
    }
    
    public int getNumberOfSources() {
        return getNumberOfRows();
    }
}
