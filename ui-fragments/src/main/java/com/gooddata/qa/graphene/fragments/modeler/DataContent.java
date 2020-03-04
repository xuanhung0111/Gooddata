package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class DataContent extends AbstractFragment {
    private static final String DATA_CONTENT = "gdc-data-content";

    @FindBy(className = "gd-header-container")
    private HeaderContainer headerContainer;

    @FindBy(className = "gdc-modeler")
    private Modeler modeler;

    public static DataContent getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                DataContent.class, waitForElementVisible(className(DATA_CONTENT), searchContext));
    }

    public HeaderContainer getHeaderContainer() {
        return headerContainer;
    }

    public Modeler getModeler() {
        return modeler;
    }
}
