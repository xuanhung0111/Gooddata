package com.gooddata.qa.graphene.fragments;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class AbstractTable extends AbstractFragment {

    @FindBy(css = "tbody tr")
    protected List<WebElement> rows;

    public List<WebElement> getRows() {
        return rows;
    }

    public int getNumberOfRows() {
        if (rows == null) {
            throw new NullPointerException();
        }
        return rows.size();
    }

    public WebElement getRow(int rowIndex) {
        if (rowIndex < 0 || rowIndex > getNumberOfRows()) {
            throw new IndexOutOfBoundsException();
        }
        return rows.get(rowIndex);
    }
    
    public List<WebElement> getCells(int rowIndex) {
        return getRow(rowIndex).findElements(By.cssSelector("td"));
    }
    
    public WebElement getCell(int rowIndex, int cellIndex) {
        return getCells(rowIndex).get(cellIndex);
    }
}
