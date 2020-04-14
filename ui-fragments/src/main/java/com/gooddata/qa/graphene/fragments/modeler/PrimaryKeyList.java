package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;

import static java.lang.String.format;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class PrimaryKeyList extends AbstractFragment {

    private static final String PRIMARY_KEY_LIST = "primary-key-list";
    private static final String SEARCH_TEXTBOX = "gd-input-field";
    private static final String ATTRIBUTE_CLASS_NAME = "s-attr.%s.%s";

    public static PrimaryKeyList getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                PrimaryKeyList.class, waitForElementVisible(className(PRIMARY_KEY_LIST), searchContext));
    }

    public void searchAttribute(String attributeName) {
        this.getRoot().findElement(By.className(SEARCH_TEXTBOX)).sendKeys(attributeName);
    }

    public void clickAttribute(String datasetName, String attributeName) {
        this.getRoot().findElement(className(format("s-attr.%s.%s", datasetName, attributeName))).click();
    }
}
