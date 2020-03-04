package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class ComponentContainer extends AbstractFragment {
    private static final String COMPONENT_CONTAINER = "gdc-ldm-component-container";

    public static ComponentContainer getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                ComponentContainer.class, waitForElementVisible(className(COMPONENT_CONTAINER), searchContext));
    }
}
