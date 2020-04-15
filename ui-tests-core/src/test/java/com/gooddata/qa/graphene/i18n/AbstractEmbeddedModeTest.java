package com.gooddata.qa.graphene.i18n;

import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import com.gooddata.qa.graphene.AbstractDashboardWidgetTest;
import com.gooddata.qa.graphene.fragments.common.FilterContextHerokuAppPage;
import com.gooddata.qa.graphene.fragments.dashboards.EmbeddedDashboard;
import com.gooddata.qa.graphene.fragments.reports.report.EmbeddedReportContainer;
import com.gooddata.qa.mdObjects.dashboard.Dashboard;
import com.gooddata.qa.mdObjects.dashboard.tab.Tab;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.java.Builder;
import org.jboss.arquillian.graphene.Graphene;

public abstract class AbstractEmbeddedModeTest extends AbstractDashboardWidgetTest {

    protected static final String HEROKU_APP_LINK = "https://test-filter-context.herokuapp.com";
    protected String embeddedUri;
    protected String embeddedReportUri;

    @Override
    protected void customizeProject() throws Throwable {
        // to open embedded setting dialog, a new dashboard is required
        new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId())
                .createDashboard(Builder.of(Dashboard::new).with(dashboard -> {
                    dashboard.setName("Test Dashboard");
                    dashboard.addTab(Builder.of(Tab::new).build());// empty tab
                }).build().getMdObject());
    }

    //--------------------------Embedded Dashboard - Start -------------------------------------
    protected EmbeddedDashboard initEmbeddedDashboard() {
        // Using work around to avoid user staying at embedded dashboard and refresh page when there are unsaved tasks.
        // It makes the embedded dashboard malfunction and reset stage to the normal dashboard.
        openUrl(PAGE_GDC);

        browser.get(embeddedUri);
        EmbeddedDashboard.waitForDashboardLoaded(browser);
        return EmbeddedDashboard.getInstance(browser);
    }
    //--------------------------Embedded Dashboard - Start -------------------------------------

    //--------------------------Embedded Report - Start ----------------------------------------
    protected EmbeddedReportContainer initEmbeddedReport() {
        browser.get(embeddedReportUri);

        //the previous page is using the same widget so should sleep in two seconds before getting widget
        //otherwise, it could get the old widget
        sleepTightInSeconds(2);

        return Graphene.createPageFragment(EmbeddedReportContainer.class,
                waitForElementVisible(EmbeddedReportContainer.LOCATOR, browser));
    }

    protected void switchToPopUpWindow(String reportTitle) {
        for (String window : browser.getWindowHandles()) {
            String windowTitle = browser.switchTo().window(window).getTitle();
            if (windowTitle.contains(reportTitle)) {
                return;
            }
        }
    }
    //--------------------------Embedded Report - Finish ---------------------------------------

    public FilterContextHerokuAppPage initFilterContextHerokuAppPage() {
        browser.get(HEROKU_APP_LINK);
        return FilterContextHerokuAppPage.getInstance(browser);
    }
}
