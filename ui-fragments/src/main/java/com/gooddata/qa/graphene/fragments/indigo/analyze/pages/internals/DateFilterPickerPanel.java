package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class DateFilterPickerPanel extends AbstractFragment {

    @FindBy(css = ".filter-picker-text")
    private List<WebElement> periods;

    public static final By LOCATOR = By.cssSelector(".adi-date-filter-picker");

    public void select(final String period) {
        waitForCollectionIsNotEmpty(periods);
        Iterables.find(periods, new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement input) {
                return period.equals(input.getText());
            }
        }).click();
        waitForFragmentNotVisible(this);
    }

    public List<String> getAllPeriods() {
        waitForCollectionIsNotEmpty(periods);
        return Lists.newArrayList(Collections2.transform(periods,
                new Function<WebElement, String>() {
            @Override
            public String apply(WebElement input) {
                return input.getText();
            }
        }));
    }
}
