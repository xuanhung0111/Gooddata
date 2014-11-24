package com.gooddata.qa.graphene.fragments.common;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class SimpleMenu extends AbstractFragment {

    public static final By LOCATOR = By.cssSelector(".gdc-menu-simple:not(.yui3-overlay-hidden):not(.hidden)");

    @FindBy(css = "li:not(.separator)")
    protected List<WebElement> items;

    public int getItemsCount() {
        waitForAllItemsVisible();
        return items.size();
    }

    public boolean contains(String label) {
        assertTrue(StringUtils.isNotBlank(label));
        waitForAllItemsVisible();

        for (WebElement e : items) {
            if (label.equals(e.findElement(BY_LINK).getText().trim()))
                return true;
        }
        return false;
    }

    public void select(String label) {
        assertTrue(StringUtils.isNotBlank(label));
        waitForAllItemsVisible();

        for (WebElement e : items) {
            if (!label.equals(e.findElement(BY_LINK).getText().trim())) continue;

            e.click();
            waitForElementNotVisible(this.getRoot());
            return;
        }
        throw new IllegalArgumentException(String.format("Item '%s' is not in menu!", label));
    }

    protected void waitForAllItemsVisible() {
        waitForElementVisible(this.getRoot());
        waitForCollectionIsNotEmpty(items);
    }
}
