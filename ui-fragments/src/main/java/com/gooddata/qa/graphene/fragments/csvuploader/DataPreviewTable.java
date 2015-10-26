package com.gooddata.qa.graphene.fragments.csvuploader;

import static com.gooddata.qa.graphene.utils.CheckUtils.*;

import java.util.List;

import static java.util.stream.Collectors.toList;

import com.gooddata.qa.graphene.fragments.FixedDataTable;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.google.common.base.Predicate;

public class DataPreviewTable extends FixedDataTable {

    @FindBy(className = "input-text")
    private List<WebElement> columnNames;

    @FindBy(css = ".s-data-type-picker select")
    private List<Select> columnTypes;

    public List<String> getColumnNames() {
        waitForCollectionIsNotEmpty(columnNames);
        return columnNames.stream()
                .map(name -> name.getAttribute("value"))
                .collect(toList());
    }

    public List<String> getColumnTypes() {
        return columnTypes.stream()
                .map(type -> type.getFirstSelectedOption().getText())
                .collect(toList());
    }

    public void setColumnsName(final List<String> names) {
        names.stream().forEach(name -> columnNames.get(names.indexOf(name)).sendKeys(name));
    }
    
    public void changeColumnType(String columnName, ColumnType type) {
        int columnIndex = getColumnNames().indexOf(columnName);
        changeColumnType(columnIndex, type);
    }

    public void changeColumnType(int fieldIndex, ColumnType type) {
        final Select selectedColumnType = columnTypes.get(fieldIndex);
        selectedColumnType.selectByValue(type.getValue());
        final Predicate<WebDriver> typeIsSelected =
                input -> type.getValue().equals(selectedColumnType.getFirstSelectedOption().getAttribute("value"));
        Graphene.waitGui(browser)
                .withMessage(
                        "Expected type is not selected: " + selectedColumnType.getFirstSelectedOption().getText())
                .until(typeIsSelected);
    }

    public void changeColumnName(int fieldIndex, String columnName) {
        final WebElement editedColumn = columnNames.get(fieldIndex);
        editedColumn.clear();
        editedColumn.sendKeys(columnName);
        final Predicate<WebDriver> columnNameUpdated =
                input -> columnName.equals(editedColumn.getAttribute("value"));
        Graphene.waitGui(browser)
                .withMessage(String.format("Expected name '%s' is not updated: '%s'", columnName, editedColumn.getAttribute("value")))
                .until(columnNameUpdated);
    }

    public enum ColumnType {
        ATTRIBUTE("Attribute"),
        FACT("Measure"),
        DATE("Date");
        
        private String typeByVisibleText;
        
        private ColumnType(String typeByVisibleText) {
            this.typeByVisibleText = typeByVisibleText;
        }
        
        public String getVisibleText() {
            return typeByVisibleText;
        }
        
        public String getValue() {
            return this.name();
        }
    }
}
