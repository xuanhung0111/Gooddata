package com.gooddata.qa.graphene.fragments.manage;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractTable;
import com.gooddata.qa.utils.CssUtils;

public class ObjectsTable extends AbstractTable {

    public static final String SORT_ASC = "ASC";
    public static final String SORT_DESC = "DESC";
    private static final String CSS_DESC_SORT = "a.desc";
    private static final String CSS_ASC_SORT = "a.asc";
    public static final By BY_OBJECT_LINK = By.cssSelector("td.title a");
    public static final By BY_OBJECT_DETAIL_PAGE = By
            .xpath("//div[@id='p-objectPage' and contains(@class,'s-displayed')]");
    private static final By BY_ROW_CHECKBOX = By.cssSelector("input[name='objectSelection']");
    private static final By BY_ROW_CHECKBOX_IS_CHECKED = By.cssSelector("input:checked");
    private static final By BY_ROW_TITLE = By.cssSelector("td.title");
    private static final By BY_TABLE_HEADER_TITLE = By.cssSelector("th.col-title");

    @FindBy(css = "th.col-title")
    private WebElement tableHeaderTitle;

    @FindBy(css = "th.col-time")
    private WebElement tableHeaderTime;

    @FindBy(css = "th.col-author")
    private WebElement tableHeaderAuthor;

    public static final ObjectsTable getInstance(By rootLocator, SearchContext context) {
        return Graphene.createPageFragment(ObjectsTable.class, waitForElementVisible(rootLocator, context));
    }

    public void selectObject(String objectName) {
        List<WebElement> links = getRoot().findElements(
                By.cssSelector(".s-title-" + CssUtils.simplifyText(objectName) + " a"));
        
        if (links.size() > 0) {
            if (links.size() > 1) {
                log.warning("selectObject matched " + links.size() + " elements, selecting first one");
            }
            links.get(0).sendKeys(Keys.ENTER);
            waitForElementVisible(BY_OBJECT_DETAIL_PAGE, browser);
            return;
        }
        
        throw new NoSuchElementException("Unable to find object with title matching: " + objectName);
    }

    public List<String> getAllItems() {
        List<String> ret = new ArrayList<String>();
        for (int i = 0, n = getNumberOfRows(); i < n; i++) {
            ret.add(waitForElementVisible(rows.get(i)).findElement(BY_OBJECT_LINK).getText());
        }
        return ret;
    }

    public ObjectsTable assertCheckboxes(boolean checkSelection, boolean expectedSelection) {
        List<WebElement> tableRows = getRows();
        for (WebElement tableRow : tableRows) {
            assertTrue(tableRow.findElement(BY_ROW_CHECKBOX).isDisplayed());
            if (checkSelection) {
                assertEquals(tableRow.findElement(ObjectsTable.BY_ROW_CHECKBOX).isSelected(),
                        expectedSelection);
            }
        }
        return this;
    }

    public ObjectsTable checkOnCheckboxes(List<String> objectTitles) {
        List<WebElement> tableRows = getRows();
        for (String objectName : objectTitles) {
            for (int i = 0; i < tableRows.size(); i++) {
                if (tableRows.get(i).findElement(BY_ROW_TITLE).getText().equals(objectName)) {
                    tableRows.get(i).findElement(ObjectsTable.BY_ROW_CHECKBOX).sendKeys(Keys.SPACE);
                    waitForElementVisible(tableRows.get(i).findElement(
                            ObjectsTable.BY_ROW_CHECKBOX_IS_CHECKED));
                    assertTrue(tableRows.get(i).findElement(ObjectsTable.BY_ROW_CHECKBOX)
                            .isSelected());
                    continue;
                }
            }
        }
        return this;
    }

    public ObjectsTable assertTableHeader() {
        assertTrue(tableHeaderTitle.isDisplayed());
        assertTrue(tableHeaderTime.isDisplayed());
        assertTrue(tableHeaderAuthor.isDisplayed());
        return this;
    }

    public ObjectsTable sortObjectsTable(String sortType, List<String> defaultObjectsList) {
        WebElement table = getRoot();
        List<String> sortedObjectsList = new ArrayList<String>();
        sortedObjectsList.addAll(defaultObjectsList);
        String selector = CSS_ASC_SORT;
        if (sortType == SORT_ASC) {
            selector = CSS_DESC_SORT;
            Collections.reverse(sortedObjectsList);
        }
        table.findElement(BY_TABLE_HEADER_TITLE).findElement(By.cssSelector(selector)).click();
        waitForElementVisible(getRoot());
        int index = getNumberOfRows() - 1;
        for (WebElement row : getRows()) {
            assertTrue(row.getText().contains(sortedObjectsList.get(index)));
            index--;
        }
        return this;
    }
}
