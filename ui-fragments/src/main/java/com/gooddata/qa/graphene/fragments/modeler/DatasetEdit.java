package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementContainDisabledAttribute;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import java.util.ArrayList;
import java.util.List;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;
import static com.gooddata.qa.graphene.utils.ElementUtils.scrollElementIntoView;

public class DatasetEdit extends AbstractFragment {
    private static String DATASET_EDIT = "dataset-edit";
    private static final By BY_DATASET_INPUT= By.cssSelector(".dataset-edit-column-input .input-text");

    @FindBy(css = ".dataset-column-wrapper")
    private List<WebElement> listColumns;

    @FindBy(css = ".dataset-column-wrapper.is-disabled")
    private List<WebElement> listDisabledColumns;

    @FindBy(css = ".data-type-picker")
    private List<WebElement> dataTypePickers;

    public static DatasetEdit getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                DatasetEdit.class, waitForElementVisible(className(DATASET_EDIT), searchContext));
    }

    public List<String> getListColumns () {
        List<String> columns = new ArrayList<String>();
        for (WebElement column : listColumns) {
            columns.add(column.findElement(BY_DATASET_INPUT).getAttribute("value"));
        }
        return columns;
    }

    public int getNumberOfDisabledColumns() {
        return listDisabledColumns.size();
    }

    public WebElement getColumnByName(String columnName) {
        WebElement column = listColumns.stream()
                .filter(input -> input.findElement(BY_DATASET_INPUT).getAttribute("value").equals(columnName))
                .findFirst()
                .get();
        return column;
    }

    public WebElement getInputColumnByName(String columnName) {
        WebElement column = listColumns.stream()
                .filter(input -> input.findElement(BY_DATASET_INPUT).getAttribute("value").equals(columnName))
                .findFirst()
                .get();
        return column.findElement(By.cssSelector(".dataset-edit-column-input .input-text"));
    }

    public WebElement getDatatypeByName(String columnName) {
        WebElement column = getColumnByName(columnName);
        int index = listColumns.indexOf(column);
        WebElement datatype =  dataTypePickers.get(index);
        return datatype;
    }

    public String getTextDatatypeByName(String columnName) {
        WebElement column = getColumnByName(columnName);
        int index = listColumns.indexOf(column);
        WebElement datatype =  dataTypePickers.get(index);
        scrollElementIntoView(datatype, browser);
        return datatype.getText();
    }

    public GenericList clickOnDatatypeByName(String columnName) {
        WebElement datatype =  getDatatypeByName(columnName);
        scrollElementIntoView(datatype, browser);
        datatype.click();
        return GenericList.getInstance(browser);
    }

    public String getTextImbigousDateByName(String columnName) {
        WebElement datatype =  getDatatypeByName(columnName);
        WebElement date = datatype.findElement(className("type-dropdown"));
        scrollElementIntoView(date, browser);
        return date.getText();
    }

    public GenericList clickOnImbigousDateByName(String columnName) {
        WebElement datatype =  getDatatypeByName(columnName);
        WebElement date = datatype.findElement(className("type-dropdown"));
        scrollElementIntoView(date, browser);
        date.click();
        return GenericList.getInstance(browser);
    }

    public GenericList clickOnImbigousDateFormatByName(String columnName) {
        WebElement datatype =  getDatatypeByName(columnName);
        WebElement format = datatype.findElement(className("format-dropdown"));
        scrollElementIntoView(format, browser);
        format.click();
        return GenericList.getInstance(browser);
    }

    public DatasetEdit editColumnByName(String columnName, String newColumnName) {
        WebElement input = getInputColumnByName(columnName);
        scrollElementIntoView(input, browser);
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(input).click().keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL).sendKeys(Keys.DELETE)
                .sendKeys(newColumnName).build().perform();
        return this;
    }

    public boolean isColumnDisabled(String columnName) {
        WebElement input = getInputColumnByName(columnName);
        return isElementContainDisabledAttribute(input);
    }

    public ChooseReferencePopUp getChooseReferencePopUp(String columnName) {
        WebElement column = getColumnByName(columnName);
        return ChooseReferencePopUp.getInstance(column);
    }
}
