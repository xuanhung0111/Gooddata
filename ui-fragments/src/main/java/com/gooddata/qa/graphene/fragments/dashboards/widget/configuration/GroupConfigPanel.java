package com.gooddata.qa.graphene.fragments.dashboards.widget.configuration;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class GroupConfigPanel extends AbstractFragment {

    @FindBy(css = ".filterList .yui3-widget")
    private List<WebElement> filters;

    private static final By BY_UNAVAILABLE_EXPLANATION = By.cssSelector(".unavailableExplanation > span");

    public GroupConfigPanel selectFilters(String... filters) {
        WebElement checkbox;

        outer:
        for (String filter : filters) {
            for (WebElement filterEle : this.filters) {
                if (filter.equals(filterEle.findElement(By.tagName("label")).getAttribute("title"))) {
                    checkbox = filterEle.findElement(By.tagName("input"));
                    if (!checkbox.isSelected()) {
                        checkbox.click();
                    }
                    continue outer;
                }
            }
            throw new NoSuchElementException("Cannot find filter: " + filter);
        }
        return this;
    }

    public String getUnavailableExplanationMessage() {
        return waitForElementVisible(BY_UNAVAILABLE_EXPLANATION, getRoot()).getText().trim();
    }
}
