package com.gooddata.qa.graphene.fragments.common;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;

import java.util.List;
import java.util.Objects;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class DropDown extends AbstractFragment {

    private static final String WEIRD_STRING_TO_CLEAR_ALL_ITEMS = "!@#$%^";

    private static final By BY_SEARCH_FIELD_INPUT = By.cssSelector(".searchfield input");

    @FindBy(css = ".gd-list-view-item span")
    private List<WebElement> items;

    @FindBy(className = "gd-list-view-item")
    private List<WebElement> entries;

    public static final DropDown getInstance(By locator, SearchContext searchContext) {
        WebElement root = waitForElementVisible(locator, searchContext);
        waitForElementNotPresent(By.cssSelector("[class*='loadingWheel'],.gd-spinner"), root);

        return Graphene.createPageFragment(DropDown.class, root);
    }

    public void searchAndSelectItem(String name) {
        if (isElementPresent(BY_SEARCH_FIELD_INPUT, browser)) {
            searchItem(name);
        }

        selectItem(name);
    }

    public void selectItem(final String name) {
        waitForCollectionIsNotEmpty(items)
                .stream()
                .filter(e -> Objects.equals(name, e.getText()))
                .findFirst()
                .get()
                .click();

        waitForElementNotVisible(this.getRoot());
    }

    public List<String> listTitlesOfItems() {
        return getElementTexts(items);
    }

    public boolean isPrivateItem(String name) {
        searchItem(name);
        return isElementVisible(By.className("is-unlisted"), waitForCollectionIsNotEmpty(entries).get(0));
    }

    private DropDown searchItem(String name) {
        WebElement searchFieldInput = waitForElementVisible(BY_SEARCH_FIELD_INPUT, browser);

        searchFieldInput.clear();
        searchFieldInput.sendKeys(WEIRD_STRING_TO_CLEAR_ALL_ITEMS);
        waitForCollectionIsEmpty(items);

        searchFieldInput.clear();
        searchFieldInput.sendKeys(name);
        waitForCollectionIsNotEmpty(items);

        return this;
    }
}
