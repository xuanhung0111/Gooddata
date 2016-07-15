package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;

import com.gooddata.qa.graphene.fragments.indigo.insight.AbstractInsightSelectionPanel;

public class AnalysisInsightSelectionPanel extends AbstractInsightSelectionPanel {

    private static final By ROOT_LOCATOR = className("open-visualizations");

    public static AnalysisInsightSelectionPanel getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(AnalysisInsightSelectionPanel.class,
                waitForElementVisible(ROOT_LOCATOR, searchContext));
    }

    public void openInsight(final String insight) {
        if (!getRoot().isDisplayed())
            throw new RuntimeException("The insight selection panel is collapsed");
        getInsightItem(insight).open();
    }

}
