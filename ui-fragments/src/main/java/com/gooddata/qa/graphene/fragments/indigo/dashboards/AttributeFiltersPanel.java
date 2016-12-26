package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import java.util.List;
import java.util.Objects;

import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import org.openqa.selenium.By;

/**
 * Panel holding {@link AttributeFilter}s
 */
public class AttributeFiltersPanel extends AbstractFragment {

    private static final String ATTRIBUTE_FILTER_SELECTOR = ".s-attribute-filter";
    private static final String ATTRIBUTE_FILTER_LOADING_SELECTOR = ATTRIBUTE_FILTER_SELECTOR + " .s-loading";

    @FindBy(css = ATTRIBUTE_FILTER_SELECTOR)
    private List<AttributeFilter> attributeFilters;

    public List<AttributeFilter> getAttributeFilters() {
        return attributeFilters;
    }

    public AttributeFiltersPanel waitForAttributeFiltersLoaded() {
        waitForElementNotPresent(By.cssSelector(ATTRIBUTE_FILTER_LOADING_SELECTOR));

        return this;
    }

    public AttributeFilter getAttributeFilter(String title) {
        return attributeFilters.stream().filter(s -> Objects.equals(title, s.getTitle())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Attribute filter button with title '" + title + "' not found!"));
    }

    public AttributeFilter getLastFilter() {
        return waitForAttributeFiltersLoaded().attributeFilters.get(attributeFilters.size() -1);
    }

    public boolean isFilterVisible(String title) {
        return waitForAttributeFiltersLoaded().attributeFilters.stream()
                .anyMatch(e -> title.equals(e.getTitle()));
    }
}
