package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;

import com.gooddata.qa.graphene.fragments.indigo.insight.AbstractInsightSelectionPanel;

public class IndigoInsightSelectionPanel extends AbstractInsightSelectionPanel {

    private static final By ROOT_LOCATOR = className("gd-visualizations-list");

    public static IndigoInsightSelectionPanel getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(IndigoInsightSelectionPanel.class,
                waitForElementVisible(ROOT_LOCATOR, searchContext));
    }

    /**
     * Add an insight to last position in dashboard by double click
     * @param insight
     */
    public void addInsightUsingDoubleClick(final String insight) {
        getActions().doubleClick(waitForElementVisible(getInsightItem(insight).getRoot())).perform();
    }

    public static void waitForNotPresent() {
        waitForElementNotPresent(ROOT_LOCATOR);
    }

    public static boolean isPresent(final SearchContext searchContext) {
        return isElementPresent(ROOT_LOCATOR, searchContext);
    }
}
