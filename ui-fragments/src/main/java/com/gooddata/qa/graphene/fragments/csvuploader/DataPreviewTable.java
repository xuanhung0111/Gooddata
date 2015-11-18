package com.gooddata.qa.graphene.fragments.csvuploader;

import static com.gooddata.qa.graphene.utils.CheckUtils.*;

import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

import com.gooddata.qa.graphene.fragments.FixedDataTable;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

public class DataPreviewTable extends FixedDataTable {

    private static final By NON_SELECTABLE_PRE_HEADER_ROW = By.className("non-selectable-pre-header");
    private static final By PRE_HEADER_ROW = By.className("pre-header");
    private static final By SELECTED_HEADER_ROW = By.className("header");
    
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

    public List<WebElement> getColumnNameInputs() {
        return waitForCollectionIsNotEmpty(columnNames).stream().collect(toList());
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
    
    public void changeColumnName(String oldName, String newName) {
        int columnIndex = getColumnNames().indexOf(oldName);
        changeColumnName(columnIndex, newName);
    }

    public void changeColumnName(int fieldIndex, String columnName) {
        final WebElement editedColumn = columnNames.get(fieldIndex);
        editedColumn.clear();
        Predicate<WebDriver> columnNameClear = webDriver -> editedColumn.getAttribute("value").equals("");
        Graphene.waitGui(browser).until(columnNameClear);
        editedColumn.sendKeys(columnName);
        final Predicate<WebDriver> columnNameUpdated =
                input -> columnName.equals(editedColumn.getAttribute("value"));
        Graphene.waitGui(browser)
                .withMessage(String.format("Expected name '%s' is not updated: '%s'", columnName, 
                        editedColumn.getAttribute("value")))
                .until(columnNameUpdated);
    }

    public boolean isEmptyColumnNameError(int index) {
        waitForCollectionIsNotEmpty(columnNames);
        return isErrorInput(columnNames.get(index));
    }

    public boolean isColumnNameError(String columnName) {
        return isErrorInput(getColumnNameInput(columnName));
    }
    
    public WebElement getColumnNameInput(String columnName) {
        return columnNames.stream()
                .filter(column -> Objects.equals(columnName, column.getAttribute("value")))
                .findFirst()
                .get();
    }

    public List<WebElement> getNonSelectablePreHeaderRows() {
        return getRoot().findElements(NON_SELECTABLE_PRE_HEADER_ROW);
    }

    public List<String> getNonSelectablePreHeaderRowCells(int index) {
        return getColumns(getNonSelectablePreHeaderRows().get(index));
    }

    public List<WebElement> getPreHeaderRows() {
        return getRoot().findElements(PRE_HEADER_ROW);
    }

    public List<String> getPreHeaderRowCells(int index) {
        return getColumns(getPreHeaderRows().get(index));
    }

    public WebElement getHeaderRow() {
        return getRoot().findElement(SELECTED_HEADER_ROW);
    }

    public List<String> getHeaderColumns() {
        return getColumns(getHeaderRow());
    }

    private boolean isErrorInput(WebElement input) {
        return input.getAttribute("class").contains("is-error");
    }

    private List<String> getColumns(WebElement columnElement) {
        if (columnElement == null)
            return Lists.newArrayList();
        return columnElement.findElements(By.tagName("td")).stream()
                .map(WebElement::getText)
                .collect(toList());
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
