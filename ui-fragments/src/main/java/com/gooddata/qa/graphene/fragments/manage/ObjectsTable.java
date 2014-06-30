package com.gooddata.qa.graphene.fragments.manage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

import com.gooddata.qa.graphene.fragments.AbstractTable;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class ObjectsTable extends AbstractTable {

    public static final String SORT_ASC = "ASC";
    public static final String SORT_DESC = "DESC";
	private static final String CSS_DESC_SORT = "a.desc";
	private static final String CSS_ASC_SORT = "a.asc";
	public static final By BY_OBJECT_LINK = By.cssSelector("td.title a");
    public static final By BY_OBJECT_DETAIL_PAGE =
            By.xpath("//div[@id='p-objectPage' and contains(@class,'s-displayed')]");
	private static final By BY_ROW_CHECKBOX = By
	.cssSelector("input[name='objectSelection']");
	private static final By BY_ROW_CHECKBOX_IS_CHECKED = By
			.cssSelector("input:checked");
	private static final By BY_ROW_TITLE = By
			.cssSelector("td.title");
	private static final By BY_TABLE_HEADER_TITLE = By
	.cssSelector("th.column-title");
	
	@FindBy(css = "th.column-title")
    private WebElement tableHeaderTitle;
	
	@FindBy(css = "th.column-time")
    private WebElement tableHeaderTime;
	
	@FindBy(css = "th.column-author")
    private WebElement tableHeaderAuthor;
	
    public boolean selectObject(String objectName) {
        for (int i = 0; i < getNumberOfRows(); i++) {
            WebElement row = waitForElementVisible(rows.get(i));
            WebElement link = row.findElement(BY_OBJECT_LINK);
            if (link.getText().equals(objectName)) {
                link.click();
                waitForElementVisible(BY_OBJECT_DETAIL_PAGE, browser);
                return true;
            }
        }
        return false;
    }
	
	public void assertCheckboxes(boolean checkSelection, boolean expectedSelection) {
		List<WebElement> tableRows = getRows();
		for (WebElement tableRow : tableRows) {
			Assert.assertTrue(tableRow.findElement(BY_ROW_CHECKBOX).isDisplayed());
			if (checkSelection) {
				Assert.assertEquals(tableRow.findElement(ObjectsTable.BY_ROW_CHECKBOX).isSelected(),
						expectedSelection);
			}
		}
	}

	public void checkOnCheckboxes(List<String> objectTitles) throws InterruptedException {
		List<WebElement> tableRows = getRows();
		for (String objectName : objectTitles) {
			for (int i = 0; i < tableRows.size(); i++) {
				if (tableRows.get(i).findElement(BY_ROW_TITLE).getText().equals(objectName)) {
					tableRows.get(i).findElement(ObjectsTable.BY_ROW_CHECKBOX).click();
					waitForElementVisible(tableRows.get(i).findElement(ObjectsTable.BY_ROW_CHECKBOX_IS_CHECKED));
					Assert.assertTrue(tableRows.get(i).findElement(ObjectsTable.BY_ROW_CHECKBOX).isSelected());
					continue;
				}
			}
		}
	}

	public void assertTableHeader() {
		Assert.assertTrue(tableHeaderTitle.isDisplayed());
		Assert.assertTrue(tableHeaderTime.isDisplayed());
		Assert.assertTrue(tableHeaderAuthor.isDisplayed());
	}

	public void sortObjectsTable(String sortType, List<String> defaultObjectsList) throws InterruptedException {
		WebElement table = getRoot();
		List<String> sortedObjectsList = new ArrayList<String>();
		sortedObjectsList.addAll(defaultObjectsList);
		String selector = CSS_ASC_SORT;
		if (sortType == SORT_ASC) {
			selector = CSS_DESC_SORT;
			Collections.reverse(sortedObjectsList);
		}
		table.findElement(BY_TABLE_HEADER_TITLE)
				.findElement(By.cssSelector(selector)).click();
		waitForElementVisible(getRoot());
		int index = getNumberOfRows() - 1;
		for (WebElement row : getRows()) {
			Assert.assertTrue(row.getText().contains(sortedObjectsList.get(index)));
			index--;
		}
	}
}
