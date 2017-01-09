package com.gooddata.qa.graphene.fragments.csvuploader;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static java.util.stream.Collectors.toList;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;

import java.util.List;
import java.util.Objects;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.FixedDataTable;
import com.gooddata.qa.graphene.fragments.csvuploader.DataTypeSelect.ColumnType;
import com.gooddata.qa.graphene.fragments.csvuploader.DateFormatSelect.DateFormat;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

public class DataPreviewTable extends FixedDataTable {

    private static final By NON_SELECTABLE_PRE_HEADER_ROW = By.className("non-selectable-pre-header");
    private static final By PRE_HEADER_ROW = By.className("pre-header");
    private static final By SELECTED_HEADER_ROW = By.className("header");

    private static final By BY_VERTICAL_SCROLLBAR = By.className("ScrollbarLayout_faceVertical");
    private static final By BY_HORIZONTAL_SCROLLBAR = By.className("ScrollbarLayout_faceHorizontal");

    @FindBy(className = "input-text")
    private List<WebElement> columnNames;

    @FindBy(css = ".data-type-picker.s-data-type-picker")
    private List<DataTypePicker> columnTypes;

    public List<String> getColumnNames() {
        waitForCollectionIsNotEmpty(columnNames);
        return columnNames.stream()
                .map(name -> name.getAttribute("value"))
                .collect(toList());
    }

    public List<WebElement> getColumnNameInputs() {
        return waitForCollectionIsNotEmpty(columnNames);
    }

    public List<String> getColumnTypes() {
        return columnTypes.stream()
                .map(DataTypePicker::getDataTypeSelect)
                .map(DataTypeSelect::getTypeSelection)
                .collect(toList());
    }

    public void setColumnsName(final List<String> names) {
        names.stream().forEach(name -> columnNames.get(names.indexOf(name)).sendKeys(name));
    }

    public void changeColumnDateFormat(String columnName, DateFormat type) {
        int columnIndex = getColumnNames().indexOf(columnName);
        changeColumnDateFormat(columnIndex, type);
    }

    public void changeColumnDateFormat(int fieldIndex, DateFormat type) {
        final DateFormatSelect selectedColumnType = columnTypes.get(fieldIndex).getDateFormatSelect();
        selectedColumnType.selectFormat(type);
        final Predicate<WebDriver> formatIsSelected =
                input -> type.getVisibleText().equals(selectedColumnType.getFormatSelection());
        Graphene.waitGui(browser)
                .withMessage(
                        "Expected date format is not selected: " + selectedColumnType.getFormatSelection())
                .until(formatIsSelected);
    }

    public int getColumnDateFormatCount(int fieldIndex) {
        final DateFormatSelect selectedColumnType = columnTypes.get(fieldIndex).getDateFormatSelect();
        return selectedColumnType.getValues().size();
    }

    public void changeColumnType(String columnName, ColumnType type) {
        int columnIndex = getColumnNames().indexOf(columnName);
        changeColumnType(columnIndex, type);
    }

    public void changeColumnType(int fieldIndex, ColumnType type) {
        final DataTypeSelect selectedColumnType = columnTypes.get(fieldIndex).getDataTypeSelect();
        selectedColumnType.selectByName(type.getVisibleText());
        final Predicate<WebDriver> typeIsSelected =
                input -> type.getVisibleText().equals(selectedColumnType.getTypeSelection());
        Graphene.waitGui(browser)
                .withMessage(
                        "Expected type is not selected: " + selectedColumnType.getTypeSelection())
                .until(typeIsSelected);
    }
    
    public void changeColumnName(String oldName, String newName) {
        int columnIndex = getColumnNames().indexOf(oldName);
        changeColumnName(columnIndex, newName);
    }

    public void changeColumnName(int fieldIndex, String columnName) {
        final WebElement editedColumn = columnNames.get(fieldIndex);
        // editedColumn.clear() works very unstable with react.js. So use sendKeys to make this action more stable.
        editedColumn.sendKeys(Keys.chord(Keys.CONTROL, "a") + Keys.DELETE + columnName);
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

    public DataTypeSelect getColumnTypeDropdown(final ColumnType columnType) {
        return columnTypes.stream()
                .map(DataTypePicker::getDataTypeSelect)
                .filter(e -> e.getTypeSelection().contains(columnType.getVisibleText()))
                .findFirst()
                .get();
    }

    public boolean isVerticalScrollbarVisible() {
        return isElementVisible(BY_VERTICAL_SCROLLBAR, getRoot());
    }

    public boolean isHorizontalScrollbarVisible() {
        return isElementVisible(BY_HORIZONTAL_SCROLLBAR, getRoot());
    }

    private boolean isErrorInput(WebElement input) {
        return input.getAttribute("class").contains("is-error");
    }

    private List<String> getColumns(WebElement columnElement) {
        if (columnElement == null)
            return Lists.newArrayList();
        return getElementTexts(columnElement.findElements(By.tagName("td")));
    }
}
