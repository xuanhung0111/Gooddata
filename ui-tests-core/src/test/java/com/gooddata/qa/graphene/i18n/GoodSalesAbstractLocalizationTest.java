package com.gooddata.qa.graphene.i18n;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import com.gooddata.qa.graphene.TemplateAbstractTest;
import org.jboss.arquillian.graphene.Graphene;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.fragments.dashboards.EmbeddedDashboard;

public abstract class GoodSalesAbstractLocalizationTest extends TemplateAbstractTest {

    protected String embeddedUri;

    public EmbeddedDashboard initEmbeddedDashboard() {
        browser.get(embeddedUri);
        EmbeddedDashboard page = Graphene.createPageFragment(EmbeddedDashboard.class,
                waitForElementVisible(EmbeddedDashboard.LOCATOR, browser));
        EmbeddedDashboard.waitForDashboardLoaded(browser);

        return page;
    }
}
