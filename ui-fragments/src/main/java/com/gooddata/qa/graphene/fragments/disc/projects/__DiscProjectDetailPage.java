package com.gooddata.qa.graphene.fragments.disc.projects;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class __DiscProjectDetailPage extends AbstractFragment {

    private static final By LOCATOR = By.className("ait-project-detail-fragment");

    public static final __DiscProjectDetailPage getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(__DiscProjectDetailPage.class, waitForElementVisible(LOCATOR, searchContext));
    }
}
