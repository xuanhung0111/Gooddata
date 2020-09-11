package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import java.util.List;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.lang.String.format;
import static org.openqa.selenium.By.cssSelector;

public class ModelItems extends AbstractFragment {
    private static final String MODEL_ITEMS = ".ds-items";

    public static ModelItems getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                ModelItems.class, waitForElementVisible(cssSelector(MODEL_ITEMS), searchContext));
    }

    public WebElement getAttribute(String dataset, String name) {
        WebElement attribute = this.getRoot().findElement(By.cssSelector(format(".ds-item-attr%s%s", dataset, name)));
        return attribute;
    }

    public boolean isAttributeExist(String dataset, String name) {
        List<WebElement> attributes = this.getRoot().findElements(By.cssSelector(format(".ds-item-attr%s%s", dataset, name)));
        return attributes.size() == 0 ? false : true;
    }

    public WebElement getReference(String dataset) {
        WebElement reference = this.getRoot().findElement(By.cssSelector(format(".ds-item-reference_dataset%s", dataset)));
        return reference;
    }


    public WebElement getFact(String dataset, String name) {
        WebElement fact = this.getRoot().findElement(By.cssSelector(format(".ds-item-fact%s%s", dataset, name)));
        return fact;
    }

    public WebElement getDate() {
        WebElement date = this.getRoot().findElement(By.cssSelector(format(".ds-item-reference_date")));
        return date;
    }
}
