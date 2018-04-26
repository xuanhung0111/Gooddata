package com.gooddata.qa.graphene.i18n;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import com.gooddata.qa.mdObjects.dashboard.Dashboard;
import com.gooddata.qa.mdObjects.dashboard.tab.Tab;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.java.Builder;
import org.jboss.arquillian.graphene.Graphene;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.fragments.dashboards.EmbeddedDashboard;

public abstract class AbstractEmbeddedDashboardTest extends GoodSalesAbstractTest {

    protected String embeddedUri;

    @Override
    protected void customizeProject() throws Throwable {
        // to open embedded setting dialog, a new dashboard is required
        new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId())
                .createDashboard(Builder.of(Dashboard::new).with(dashboard -> {
                    dashboard.setName("Test Dashboard");
                    dashboard.addTab(Builder.of(Tab::new).build());// empty tab
                }).build().getMdObject());
    }

    protected EmbeddedDashboard initEmbeddedDashboard() {
        browser.get(embeddedUri);
        EmbeddedDashboard page = Graphene.createPageFragment(EmbeddedDashboard.class,
                waitForElementVisible(EmbeddedDashboard.LOCATOR, browser));
        EmbeddedDashboard.waitForDashboardLoaded(browser);

        return page;
    }
}
