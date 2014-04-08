package com.gooddata.qa.graphene.fragments.manage;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.fragments.AbstractTable;

public class AttributesTable extends AbstractTable {

    public static final By BY_ATTRIBUTE_LINK = By.cssSelector("td.title a");
    public static final By BY_ATTRIBUTE_DETAIL_PAGE = By.xpath("//div[@id='p-objectPage' and contains(@class,'s-displayed')]");

    public boolean selectAttribute(String attributeName) {
        for (int i = 0; i < getNumberOfRows(); i++) {
            WebElement row = waitForElementVisible(rows.get(i));
            WebElement link = row.findElement(BY_ATTRIBUTE_LINK);
            if (link.getText().equals(attributeName)) {
                link.click();
                waitForElementVisible(BY_ATTRIBUTE_DETAIL_PAGE);
                return true;
            }
        }
        return false;
    }
}
