package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class MappedTo extends AbstractFragment {
    private static final String MAPPED_TO = "mapped-to";

    @FindBy(className = "source-name")
    private WebElement sourceName;

    public static MappedTo getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                MappedTo.class, waitForElementVisible(className(MAPPED_TO), searchContext));
    }

    public String getSourceName() {
        return sourceName.getText();
    }
}
