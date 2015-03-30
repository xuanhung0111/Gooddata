package com.gooddata.qa.graphene.fragments.dashboards.widget.configuration;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForCollectionIsNotEmpty;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

public class FiltersConfigPanel extends AbstractFragment {
    @FindBys({
        @FindBy(css = ".filterList,.unavailableFilterList"),
        @FindBy(css = ".yui3-widget")
    })
    private List<WebElement> filters;

    private static final By BY_LABEL = By.cssSelector("label");
    private static final By BY_CHECKBOX = By.cssSelector("input");
    private static final String SELECTED = "yui3-c-checkbox-selected";
    private static final String DISABLED = "yui3-c-checkbox-disabled";

    public List<String> getAllFilters() {
        waitForCollectionIsNotEmpty(filters);
        return Lists.newArrayList(Collections2.transform(filters, new Function<WebElement, String>() {
            @Override
            public String apply(WebElement input) {
                return input.findElement(BY_LABEL).getText();
            }
        }));
    }

    public boolean areAllFiltersDisabled() {
        waitForCollectionIsNotEmpty(filters);
        return filters.get(0).getAttribute("class").contains(DISABLED);
    }

    public List<String> getAllAffectedFilters() {
        waitForCollectionIsNotEmpty(filters);
        return Lists.newArrayList(FluentIterable.from(filters).filter(new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement input) {
                return input.getAttribute("class").contains(SELECTED);
            }
        }).transform(new Function<WebElement, String>() {
            @Override
            public String apply(WebElement input) {
                return input.findElement(BY_LABEL).getText();
            }
        }));
    }

    public void removeFiltersFromAffectedList(String... filterNames) {
        waitForCollectionIsNotEmpty(filters);
        Collection<WebElement> affectedELements = Collections2.filter(filters, new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement input) {
                return input.getAttribute("class").contains(SELECTED);
            }
        });

        List<String> filterInputs = Arrays.asList(filterNames);
        for (WebElement e : affectedELements) {
            if (!filterInputs.contains(e.findElement(BY_LABEL).getText()))
                continue;
            assertTrue(e.getAttribute("class").contains(SELECTED));
            e.findElement(BY_CHECKBOX).click();
        }
    }

    public void addFiltersToAffectedList(String... filterNames) {
        waitForCollectionIsNotEmpty(filters);
        Collection<WebElement> unaffectedELements = Collections2.filter(filters, new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement input) {
                return !input.getAttribute("class").contains(SELECTED);
            }
        });

        List<String> filterInputs = Arrays.asList(filterNames);
        for (WebElement e : unaffectedELements) {
            if (!filterInputs.contains(e.findElement(BY_LABEL).getText()))
                continue;
            assertFalse(e.getAttribute("class").contains(SELECTED));
            e.findElement(BY_CHECKBOX).click();
        }
    }
}
