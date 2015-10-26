package com.gooddata.qa.graphene.fragments;

import static java.util.Objects.requireNonNull;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

/**
 * Graphene fragment representing react FixedDataTable component by Facebook.
 */
public class FixedDataTable extends AbstractFragment {

    @FindBy(className = "fixedDataTableRowLayout_rowWrapper")
    protected List<WebElement> rows;

    public List<WebElement> getRows() {
        return rows;
    }

    public int getNumberOfRows() {
        requireNonNull(rows, "table rows cannot be null");
        return rows.size();
    }

    public WebElement getRow(int rowIndex) {
        requireNonNull(rows, "table rows cannot be null");
        return rows.get(rowIndex);
    }
}
