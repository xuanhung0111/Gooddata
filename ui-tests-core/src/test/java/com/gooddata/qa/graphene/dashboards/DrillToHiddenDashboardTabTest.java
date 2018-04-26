package com.gooddata.qa.graphene.dashboards;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.DrillingConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.DrillingConfigPanel.DrillingGroup;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.mdObjects.dashboard.Dashboard;
import com.gooddata.qa.mdObjects.dashboard.tab.Tab;
import com.gooddata.qa.mdObjects.dashboard.tab.TabItem;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.java.Builder;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport.CellType;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_TOP_SALES_REPS_BY_WON_AND_LOST;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class DrillToHiddenDashboardTabTest extends GoodSalesAbstractTest {

    private final String PRIVATE_DASHBOARD = "Private Dashboard";
    private final String PUBLIC_DASHBOARD = "Public Dashboard";

    private final String TAB_ON_PRIVATE_DASHBOARD = "Tab On Private Dashboard";
    private final String TAB_ON_PUBLIC_DASHBOARD = "Tab On Public Dashboard";

    private DashboardRestRequest dashboardRequest;

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    @Override
    protected void customizeProject() throws Throwable {
        dashboardRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void initDashboardDrillingToHiddenTab() throws IOException, JSONException {
        String reportOnPublicDash = getReportCreator().createAmountByProductReport();
        String reportOnPrivateDash = getReportCreator().createTopSalesRepsByWonAndLostReport();

        Dashboard privateDash = Builder.of(Dashboard::new).with(dashboard -> {
            dashboard.setName(PRIVATE_DASHBOARD);
            dashboard.addTab(initDashboardTab(TAB_ON_PRIVATE_DASHBOARD,
                    singletonList(createReportItem(reportOnPrivateDash))));
        }).build();

        Dashboard publicDash = Builder.of(Dashboard::new).with(dashboard -> {
            dashboard.setName(PUBLIC_DASHBOARD);
            dashboard.addTab(initDashboardTab(TAB_ON_PUBLIC_DASHBOARD,
                    singletonList(createReportItem(reportOnPublicDash))));
        }).build();

        for (Dashboard dashboard : asList(privateDash, publicDash)) {
            dashboardRequest.createDashboard(dashboard.getMdObject());
        }

        initDashboardsPage().selectDashboard(PUBLIC_DASHBOARD).publishDashboard(true);
        dashboardsPage.editDashboard();
        dashboardsPage.getContent().getLatestReport(TableReport.class).addDrilling(
                Pair.of(singletonList(ATTR_PRODUCT), TAB_ON_PRIVATE_DASHBOARD), DrillingGroup.DASHBOARDS.getName());
        dashboardsPage.saveDashboard();

        dashboardsPage.selectDashboard(PRIVATE_DASHBOARD).publishDashboard(false);
    }

    @Test(dependsOnMethods = {"initDashboardDrillingToHiddenTab"})
    public void testDrillReportToHiddenTab() throws JSONException {
        signIn(true, UserRoles.EDITOR);
        try {
            initDashboardsPage().selectDashboard(PUBLIC_DASHBOARD).getContent()
                    .getLatestReport(TableReport.class)
                    .drillOnFirstValue(CellType.ATTRIBUTE_VALUE);

            final Function<WebDriver, Boolean> targetTabIsLoaded =
                    browser -> dashboardsPage.getDashboardName().equals(PRIVATE_DASHBOARD) &&
                            dashboardsPage.getTabs().getSelectedTab().getLabel().equals(TAB_ON_PRIVATE_DASHBOARD);
            Graphene.waitGui().until(targetTabIsLoaded);

            assertEquals(dashboardsPage.getContent().getLatestReport(TableReport.class).getReportTiTle(),
                    REPORT_TOP_SALES_REPS_BY_WON_AND_LOST);
        } finally {
            logout();
            signIn(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = {"initDashboardDrillingToHiddenTab"})
    public void testHiddenDashboardTabOnDrillingDialog() throws JSONException {
        signIn(true, UserRoles.EDITOR);
        try {
            initDashboardsPage().selectDashboard(PUBLIC_DASHBOARD).editDashboard();

            DrillingConfigPanel drillingConfigPanel =
                    WidgetConfigPanel
                            .openConfigurationPanelFor(
                                    dashboardsPage.getContent().getLatestReport(TableReport.class).getRoot(),
                                    browser)
                            .getTab(WidgetConfigPanel.Tab.DRILLING, DrillingConfigPanel.class);

            assertFalse(drillingConfigPanel.isValueOnRightButton(PRIVATE_DASHBOARD, DrillingGroup.DASHBOARDS.getName()),
                    PRIVATE_DASHBOARD + " is on setting panel");
        } finally {
            logout();
            signIn(true, UserRoles.ADMIN);
        }
    }

    private Tab initDashboardTab(String name, List<TabItem> items) {
        return Builder.of(Tab::new)
                .with(tab -> tab.setTitle(name))
                .with(tab -> tab.addItems(items))
                .build();
    }
}
