package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import com.gooddata.qa.graphene.fragments.common.AbstractPicker;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import java.util.List;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.stream.Collectors.toList;
import static org.openqa.selenium.By.className;

public class FilterBarPicker extends AbstractPicker {

    public static final By ROOT_LOCATOR = className("s-filter-dropdown");
    public final String S_ACTIVE = "s-active";

    @Override
    protected String getListItemsCssSelector() {
        return ".s-filter-dropdown-item";
    }

    @Override
    protected void waitForPickerLoaded() {}

    @Override
    protected WebElement getElement(String nameItem) {
        return getElements().stream().filter(e -> e.getText().equals(nameItem)).findFirst().get();
    }

    public static FilterBarPicker getInstance(SearchContext context) {
        return Graphene.createPageFragment(FilterBarPicker.class, waitForElementVisible(ROOT_LOCATOR, context));
    }

    public FilterBarPicker uncheckItem(String nameItem) {
        return toggle(nameItem, false);
    }

    public FilterBarPicker checkItem(String nameItem) {
        return toggle(nameItem, true);
    }

    public FilterBarPicker checkItem(String name, int index) {
        return checkItem(name + "\nM" + index);
    }

    public FilterBarPicker uncheckItem(String name, int index) {
        return uncheckItem(name + "\nM" + index);
    }

    public boolean isItemChecked(String name, int index) {
        return isItemCheck(name + "\nM" + index);
    }

    public boolean isItemCheck(String nameItem) {
        return isItemCheck(getElement(nameItem));
    }

    public List<String> getValuesText() {
        return getElements().stream().map(e -> e.getText()).collect(toList());
    }

    public void apply() { waitForElementVisible(className("s-filter-dropdown-apply"), browser).click(); }

    private FilterBarPicker toggle(String nameItem, boolean isChecked) {
        WebElement webElement = getElement(nameItem);
        if (isItemCheck(webElement) != isChecked) {
            webElement.click();
        }
        return this;
    }

    private Boolean isItemCheck(WebElement webElement) {
        return webElement.getAttribute("class").contains(S_ACTIVE);
    }
}
