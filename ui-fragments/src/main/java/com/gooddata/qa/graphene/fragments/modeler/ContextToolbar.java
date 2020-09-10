package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class ContextToolbar extends AbstractFragment {
    private static final String CONTEXT_TOOLBAR = "joint-context-toolbar";

    @FindBy(css = ".move-delete-menu .btn-delete")
    private WebElement deleteButton;

    public static ContextToolbar getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                ContextToolbar.class, waitForElementVisible(className(CONTEXT_TOOLBAR), searchContext));
    }

    public ContextToolbar deleteElement() {
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(deleteButton).click().build().perform();
        return this;
    }
}
