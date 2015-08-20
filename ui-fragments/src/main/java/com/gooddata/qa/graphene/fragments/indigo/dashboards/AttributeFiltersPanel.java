package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.Objects;

/**
 * Panel holding {@link AttributeFilter}s
 */
public class AttributeFiltersPanel extends AbstractFragment {

    @FindBy(css = ".s-attribute-filter")
    private List<AttributeFilter> attributeFilters;

    public List<AttributeFilter> getAttributeFilters() {
        return attributeFilters;
    }

    public AttributeFilter getAttributeFilter(String title) {
        return attributeFilters.stream().filter(s -> Objects.equals(title, s.getTitle())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Attribute filter button with title '" + title + "' not found!"));
    }
}
