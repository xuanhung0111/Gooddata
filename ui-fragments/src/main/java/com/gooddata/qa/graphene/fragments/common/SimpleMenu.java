package com.gooddata.qa.graphene.fragments.common;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class SimpleMenu extends AbstractFragment {

    protected static final By LOCATOR = By.cssSelector(".gdc-menu-simple:not(.yui3-overlay-hidden):not(.hidden)");

    @FindBy(css = "li:not(.separator)")
    protected List<WebElement> items;

    public static SimpleMenu getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(SimpleMenu.class, waitForElementVisible(LOCATOR, searchContext));
    }

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
        select(e -> label.equals(e.findElement(BY_LINK).getText().trim()));
        waitForElementNotVisible(this.getRoot());
    }

    public void select(Predicate<WebElement> filter) {
        waitForAllItemsVisible();
        items.stream()
            .filter(filter)
            .map(e -> e.findElement(BY_LINK))
            .findFirst()
            .get()
            .click();
    }

    public void openSubMenu(String label) {
        assertTrue(StringUtils.isNotBlank(label));
        waitForAllItemsVisible();
        WebElement menu = items.stream()
            .filter(e -> label.equals(e.findElement(BY_LINK).getText().trim()))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("Cannot find: " + label));

        new Actions(browser).moveToElement(menu).perform();
    }

    protected void waitForAllItemsVisible() {
        waitForElementVisible(this.getRoot());
        waitForCollectionIsNotEmpty(items);
    }
}
