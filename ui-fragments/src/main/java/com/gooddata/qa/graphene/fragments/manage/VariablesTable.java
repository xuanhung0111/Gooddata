package com.gooddata.qa.graphene.fragments.manage;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.fragments.AbstractTable;

public class VariablesTable extends AbstractTable {
	
	public static final By BY_VARIABLE_LINK = By.cssSelector("td.title a");
	public static final By BY_VARIABLE_DETAIL_PAGE = By.xpath("//div[@id='p-objectPage' and contains(@class,'s-displayed')]");
	
	public boolean selectVariable(String variableName) {
		for (int i = 0; i < getNumberOfRows(); i++) {
			WebElement row = waitForElementVisible(rows.get(i));
			WebElement link = row.findElement(BY_VARIABLE_LINK);
			if (link.getText().equals(variableName)) {
				link.click();
				waitForElementVisible(BY_VARIABLE_DETAIL_PAGE);
				return true;
			}
		}
		return false;
	}
}
