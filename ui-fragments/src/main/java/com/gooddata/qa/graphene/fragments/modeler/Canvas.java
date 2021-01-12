package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.id;

public class Canvas extends AbstractFragment {
    private static final String CANVAS = "paper-container";

    @FindBy(className = "joint-paper-scroller")
    private PaperScrollerBackground scrollerBackground;

    @FindBy(className = "joint-paper")
    private MainModelContent mainModelContent;

    public static final Canvas getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(Canvas.class, waitForElementVisible(id(CANVAS), searchContext));
    }

    public PaperScrollerBackground getPaperScrollerBackground() {
        waitForElementVisible(scrollerBackground.getRoot());
        return scrollerBackground;
    }

    public MainModelContent getModelContent() {
        waitForElementVisible(mainModelContent.getRoot());
        return mainModelContent;
    }
}
