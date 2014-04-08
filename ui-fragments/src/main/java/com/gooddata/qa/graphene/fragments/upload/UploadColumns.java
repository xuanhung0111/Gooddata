package com.gooddata.qa.graphene.fragments.upload;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class UploadColumns extends AbstractFragment {

    public static enum OptionDataType {
        TEXT("Text"),
        NUMBER("Number"),
        DATE("Date");

        private String optionLabel;

        private OptionDataType(String optionLabel) {
            this.optionLabel = optionLabel;
        }

        public String getOptionLabel() {
            return optionLabel;
        }

        public By getOptionByLocator() {
            return By.xpath("option[text()='" + optionLabel + "']");
        }
    }

    private static final By BY_INPUT = By.xpath("td/div/input");
    private static final By BY_SELECT = By.xpath("td[3]/select");

    private static final By BY_SELECTED_OPTION = By.xpath("option[@selected='selected']");

    @FindBy(xpath = "tbody/tr")
    private List<WebElement> columns;

    public List<WebElement> getColumns() {
        return columns;
    }

    public int getNumberOfColumns() {
        return columns.size();
    }

    private WebElement getColumnWebElement(int i) {
        return columns.get(i);
    }

    public void setColumnName(int columnIndex, String columnName) {
        WebElement input = getColumnWebElement(columnIndex).findElement(BY_INPUT);
        input.clear();
        input.sendKeys(columnName);
    }

    public void setColumnType(int columnIndex, OptionDataType dataType) {
        WebElement select = getColumnWebElement(columnIndex).findElement(BY_SELECT);
        select.findElement(dataType.getOptionByLocator()).click();
    }

    public String getColumnName(int columnIndex) {
        return getColumnWebElement(columnIndex).findElement(BY_INPUT).getAttribute("value");
    }

    public String getColumnType(int columnIndex) {
        return getColumnWebElement(columnIndex).findElement(BY_SELECT).findElement(BY_SELECTED_OPTION).getText();
    }

    public List<String> getColumnNames() {
        List<String> columnNames = new ArrayList<String>();
        for (int i = 0; i < columns.size(); i++) {
            columnNames.add(getColumnName(i));
        }
        return columnNames;
    }

    public List<String> getColumnTypes() {
        List<String> columnTypes = new ArrayList<String>();
        for (int i = 0; i < columns.size(); i++) {
            columnTypes.add(getColumnType(i));
        }
        return columnTypes;
    }
}
