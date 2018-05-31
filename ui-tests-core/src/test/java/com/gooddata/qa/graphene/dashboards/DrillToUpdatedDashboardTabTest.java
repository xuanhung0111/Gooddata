package com.gooddata.qa.graphene.dashboards;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.fragments.common.StatusBar;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardTabs;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.DrillingConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.DrillingConfigPanel.DrillingGroup;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport.CellType;
import com.gooddata.qa.mdObjects.dashboard.Dashboard;
import com.gooddata.qa.mdObjects.dashboard.tab.Tab;
import com.gooddata.qa.mdObjects.dashboard.tab.TabItem;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.java.Builder;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static java.util.Collections.singletonList;
import static org.testng.Assert.*;

public class DrillToUpdatedDashboardTabTest extends GoodSalesAbstractTest {

    private final String FIRST_TAB = "First Tab";
    private final String SECOND_TAB = "Second Tab";
    private final String THIRD_TAB = "Third Tab";

    private String reportUri;
    private DashboardRestRequest dashboardRequest;

    @Override
    protected void customizeProject() throws Throwable {
        reportUri = getReportCreator().createAmountByProductReport();
        dashboardRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillToDeletedTab() throws IOException, JSONException {
        Dashboard dash = initDashboardHavingManyTabs();
        String dashUri = dashboardRequest.createDashboard(dash.getMdObject());
        try {
            initDashboardsPage().selectDashboard(dash.getName());
            dashboardsPage.getTabs().getTab(FIRST_TAB).open();
            dashboardsPage.editDashboard();

            TableReport reportOnFirstTab = dashboardsPage.getContent().getLatestReport(TableReport.class);
            reportOnFirstTab.addDrilling(Pair.of(singletonList(ATTR_PRODUCT), SECOND_TAB),
                    DrillingGroup.DASHBOARDS.getName());
            dashboardsPage.saveDashboard();

            deleteDashboardTab(SECOND_TAB);

            dashboardsPage.editDashboard();
            WidgetConfigPanel widgetConfigPanel = WidgetConfigPanel
                    .openConfigurationPanelFor(reportOnFirstTab.getRoot(), browser);
            Screenshots.takeScreenshot(browser, "drillToDeletedTab", getClass());

            assertTrue(widgetConfigPanel
                    .getTab(WidgetConfigPanel.Tab.DRILLING, DrillingConfigPanel.class).getSettingsOnLastItemPanel()
                    .equals(Pair.of(ATTR_PRODUCT, "Select Attribute / Report / Dashboard")),
                    "Show me is not reset");

            assertFalse(widgetConfigPanel.getTab(WidgetConfigPanel.Tab.DRILLING, DrillingConfigPanel.class)
                    .getAllValueLists(0).getRight().contains(SECOND_TAB), SECOND_TAB + "is not removed out list tabs");
            widgetConfigPanel.discardConfiguration();

            reportOnFirstTab.drillOnFirstValue(CellType.ATTRIBUTE_VALUE);
            StatusBar statusBar = StatusBar.getInstance(browser);
            assertEquals(statusBar.getMessage(), "The target dashboard tab has been deleted.");
            statusBar.dismiss();
        } finally {
            new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .deleteObjectsUsingCascade(dashUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillToRenameTab() throws IOException, JSONException {
        final String RENAME_TAB = "Rename Tab";
        Dashboard dash = initDashboardHavingManyTabs();
        String dashUri = dashboardRequest.createDashboard(dash.getMdObject());
        try {
            initDashboardsPage().selectDashboard(dash.getName());
            dashboardsPage.getTabs().getTab(FIRST_TAB).open();
            dashboardsPage.editDashboard();

            TableReport reportOnFirstTab = dashboardsPage.getContent().getLatestReport(TableReport.class);
            reportOnFirstTab.addDrilling(Pair.of(singletonList(ATTR_PRODUCT), SECOND_TAB),
                    DrillingGroup.DASHBOARDS.getName());
            dashboardsPage.saveDashboard();

            renameDashboardTab(SECOND_TAB, RENAME_TAB);

            dashboardsPage.getTabs().getTab(FIRST_TAB).open();
            dashboardsPage.editDashboard();
            WidgetConfigPanel widgetConfigPanel = WidgetConfigPanel
                    .openConfigurationPanelFor(reportOnFirstTab.getRoot(), browser);
            Screenshots.takeScreenshot(browser, "drillToRenameTab", getClass());

            assertTrue(widgetConfigPanel
                    .getTab(WidgetConfigPanel.Tab.DRILLING, DrillingConfigPanel.class)
                    .getSettingsOnLastItemPanel().equals(Pair.of(ATTR_PRODUCT, RENAME_TAB)),
                    "Show me is not updated");

            assertTrue(widgetConfigPanel.getTab(WidgetConfigPanel.Tab.DRILLING, DrillingConfigPanel.class)
                    .getAllValueLists(0).getRight().contains(RENAME_TAB), RENAME_TAB + " is not added to list tabs");

            widgetConfigPanel.discardConfiguration();

            reportOnFirstTab.drillOnFirstValue(CellType.ATTRIBUTE_VALUE).waitForLoaded();
            assertTrue(dashboardsPage.getTabs().getTab(RENAME_TAB).isSelected(),
                    RENAME_TAB + " is not selected after drill action");
        } finally {
            new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .deleteObjectsUsingCascade(dashUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillToCreatedDashboardTab() throws IOException, JSONException {
        final String FOURTH_TAB = "Fourth Tab";
        Dashboard dash = initDashboardHavingManyTabs();
        String dashUri = dashboardRequest.createDashboard(dash.getMdObject());
        try {
            initDashboardsPage().selectDashboard(dash.getName());
            dashboardsPage.addNewTab(FOURTH_TAB);

            dashboardsPage.getTabs().getTab(FIRST_TAB).open();

            TableReport reportOnFirstTab = dashboardsPage.getContent().getLatestReport(TableReport.class);
            reportOnFirstTab.addDrilling(Pair.of(singletonList(ATTR_PRODUCT), FOURTH_TAB),
                    DrillingGroup.DASHBOARDS.getName());
            reportOnFirstTab.drillOnFirstValue(CellType.ATTRIBUTE_VALUE).waitForLoaded();
            Screenshots.takeScreenshot(browser, "drillToCreatedDashboardTab", getClass());

            assertTrue(dashboardsPage.getTabs().getTab(FOURTH_TAB).isSelected(),
                    FOURTH_TAB + " is not select after drilling action");
        } finally {
            new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .deleteObjectsUsingCascade(dashUri);
        }
    }

    private Tab initDashboardTab(String name, List<TabItem> items) {
        return Builder.of(Tab::new).with(tab -> {
            tab.setTitle(name);
            tab.addItems(items);
        }).build();
    }

    private Dashboard initDashboardHavingManyTabs() {
        Tab firstTab = initDashboardTab(FIRST_TAB, singletonList(createReportItem(reportUri)));
        Tab scecondTab = initDashboardTab(SECOND_TAB, singletonList(createReportItem(reportUri)));
        Tab thirdTab = initDashboardTab(THIRD_TAB, singletonList(createReportItem(reportUri)));

        return  Builder.of(Dashboard::new).with(dash -> {
            dash.setName("Dashboard " + generateHashString());
            dash.addTab(firstTab);
            dash.addTab(scecondTab);
            dash.addTab(thirdTab);
        }).build();
    }

    private void deleteDashboardTab(String name) {
        DashboardTabs tabs = dashboardsPage.getTabs();
        tabs.getTab(name).open();
        dashboardsPage.deleteDashboardTab(tabs.getSelectedTabIndex());
    }

    private void renameDashboardTab(String name, String newName) {
        DashboardTabs tabs = dashboardsPage.getTabs();
        tabs.getTab(name).open();
        dashboardsPage.renameTab(tabs.getSelectedTabIndex(), newName);
    }
}
