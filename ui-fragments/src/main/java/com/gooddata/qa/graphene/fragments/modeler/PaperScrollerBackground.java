package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class PaperScrollerBackground extends AbstractFragment {
    private static final String PAPER_SCROLLER_BACKGROUND = "joint-paper-scroller";

    @FindBy(className = "joint-paper")
    private MainModelContent mainModelContent;

    public static PaperScrollerBackground getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                PaperScrollerBackground.class, waitForElementVisible(className(PAPER_SCROLLER_BACKGROUND), searchContext));
    }

    public MainModelContent getMainModelContent() {
        return mainModelContent;
    }
}
