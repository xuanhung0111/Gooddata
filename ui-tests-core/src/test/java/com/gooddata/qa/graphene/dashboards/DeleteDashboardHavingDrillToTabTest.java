package com.gooddata.qa.graphene.dashboards;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.dashboard.drilling.DrillSetting;
import com.gooddata.qa.graphene.fragments.common.StatusBar;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardTabs;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.DrillingConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.DrillingConfigPanel.DrillingGroup;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport.CellType;
import com.gooddata.qa.mdObjects.dashboard.Dashboard;
import com.gooddata.qa.mdObjects.dashboard.tab.ReportItem;
import com.gooddata.qa.mdObjects.dashboard.tab.Tab;
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
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_SALES_REP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_AMOUNT_BY_PRODUCT;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class DeleteDashboardHavingDrillToTabTest extends GoodSalesAbstractTest {

    private final String SOURCE_TAB = "source tab";
    private final String TARGET_TAB = "target tab";
    private final String DASHBOARD_HAS_DRILL_SETTINGS = "Dashboard has drill settings";
    private final String ERROR_MSG = "The target dashboard tab has been deleted.";
    private final String SHOW_ME_DEFAULT_VALUE = "Select Attribute / Report / Dashboard";
    private final String SHOW_ME_ON_INNER_DRILL_DEFAULT_VALUE = "Select Dashboard";

    private String reportHavingOneMetric;
    private String reportHavingTwoMetrics;
    private DashboardRestRequest dashboardRequest;

    @Override
    protected void customizeProject() throws Throwable {
        reportHavingOneMetric = getReportCreator().createAmountByProductReport();
        reportHavingTwoMetrics = getReportCreator().createTopSalesRepsByWonAndLostReport();
        dashboardRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void deleteDashboard() throws IOException, JSONException {
        String dashUri = dashboardRequest.createDashboard(
                initDashboardHavingDrillSetting(DASHBOARD_HAS_DRILL_SETTINGS).getMdObject());

        try {
            initDashboardsPage().selectDashboard(DASHBOARD_HAS_DRILL_SETTINGS)
                    .getTabs().getTab(SOURCE_TAB).open();

            addDrillSettingsToLatestReport(
                    new DrillSetting(singletonList(ATTR_PRODUCT), TARGET_TAB, DrillingGroup.DASHBOARDS.getName()));

            dashboardsPage.editDashboard().deleteDashboard();

            assertFalse(dashboardsPage.getDashboardName().equals(DASHBOARD_HAS_DRILL_SETTINGS),
                    "The dashboard has not been deleted");
        } finally {
            new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .deleteObjectsUsingCascade(dashUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void deleteTab() throws IOException, JSONException {
        String dashUri = dashboardRequest.createDashboard(
                initDashboardHavingDrillSetting(DASHBOARD_HAS_DRILL_SETTINGS).getMdObject());
        try {
            initDashboardsPage().selectDashboard(DASHBOARD_HAS_DRILL_SETTINGS)
                    .getTabs().getTab(SOURCE_TAB).open();

            addDrillSettingsToLatestReport(
                    new DrillSetting(singletonList(ATTR_PRODUCT), TARGET_TAB, DrillingGroup.DASHBOARDS.getName()));

            dashboardsPage.getTabs().getTab(TARGET_TAB).open();
            deleteDashboardTab(TARGET_TAB);

            Screenshots.takeScreenshot(browser, "deleteTab", this.getClass());
            assertFalse(dashboardsPage.getTabs().getAllTabNames().stream().anyMatch(TARGET_TAB::equals),
                    "The dashboard tab has not been deleted");
        } finally {
            new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .deleteObjectsUsingCascade(dashUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillToDeletedTab() throws IOException, JSONException {
        String dashUri = dashboardRequest.createDashboard(
                initDashboardHavingDrillSetting(DASHBOARD_HAS_DRILL_SETTINGS).getMdObject());
        try {
            DashboardTabs tabs = initDashboardsPage().selectDashboard(DASHBOARD_HAS_DRILL_SETTINGS).getTabs();

            tabs.getTab(SOURCE_TAB).open();
            addDrillSettingsToLatestReport(
                    new DrillSetting(singletonList(ATTR_PRODUCT), TARGET_TAB, DrillingGroup.DASHBOARDS.getName()));

            tabs.getTab(TARGET_TAB).open();
            deleteDashboardTab(TARGET_TAB);

            tabs.getTab(SOURCE_TAB).open();

            dashboardsPage.getContent().getLatestReport(TableReport.class)
                    .drillOnFirstValue(CellType.ATTRIBUTE_VALUE);
            Screenshots.takeScreenshot(browser, "drillToDeletedTab", this.getClass());

            // get new instance of DashboardTabs object to ensure tabs are updated
            assertEquals(dashboardsPage.getTabs().getSelectedTab().getLabel(), SOURCE_TAB,
                    "Switching to another tab is not expected when users drill to deleted tab");

            assertEquals(StatusBar.getInstance(browser).getMessage(), ERROR_MSG);
        } finally {
            new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .deleteObjectsUsingCascade(dashUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testDrillSettingsAfterDeletingTab() throws IOException, JSONException {
        String dashUri = dashboardRequest.createDashboard(
                initDashboardHavingDrillSetting(DASHBOARD_HAS_DRILL_SETTINGS).getMdObject());
        try {
            DashboardTabs tabs = initDashboardsPage().selectDashboard(DASHBOARD_HAS_DRILL_SETTINGS).getTabs();

            tabs.getTab(SOURCE_TAB).open();
            addDrillSettingsToLatestReport(
                    new DrillSetting(singletonList(ATTR_PRODUCT), TARGET_TAB, DrillingGroup.DASHBOARDS.getName()));

            deleteDashboardTab(TARGET_TAB);
            tabs.getTab(SOURCE_TAB).open();

            dashboardsPage.editDashboard();
            Pair<String, String> drillingSettings = WidgetConfigPanel
                    .openConfigurationPanelFor(
                            dashboardsPage.getContent().getLatestReport(TableReport.class).getRoot(), browser)
                    .getTab(WidgetConfigPanel.Tab.DRILLING, DrillingConfigPanel.class)
                    .getSettingsOnLastItemPanel();

            assertEquals(drillingSettings.getRight(), SHOW_ME_DEFAULT_VALUE);
            assertEquals(drillingSettings.getLeft(), ATTR_PRODUCT);
        } finally {
            new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .deleteObjectsUsingCascade(dashUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testInnerDrillSettingsAfterDeletingTab() throws IOException, JSONException {
        String dashboardName = "Dashboard having further drill setting";

        Tab sourceTab = Builder.of(Tab::new).with(tab -> {
            tab.setTitle(SOURCE_TAB);
            tab.addItem(Builder.of(ReportItem::new)
                    .with(report -> report.setObjUri(reportHavingTwoMetrics))
                    .build());
        }).build();

        Tab targetTab = Builder.of(Tab::new).with(tab -> {
            tab.setTitle(TARGET_TAB);
            tab.addItem(Builder.of(ReportItem::new)
                    .with(report -> report.setObjUri(reportHavingOneMetric))
                    .build());
        }).build();

        Dashboard dashboard = Builder.of(Dashboard::new).with(dash -> {
            dash.setName(dashboardName);
            dash.addTab(sourceTab);
            dash.addTab(targetTab);
        }).build();

        String dashUri = dashboardRequest.createDashboard(dashboard.getMdObject());

        try {
            DashboardTabs tabs = initDashboardsPage().selectDashboard(dashboardName).getTabs();

            tabs.getTab(SOURCE_TAB).open();
            addDrillSettingsToLatestReport(
                    new DrillSetting(singletonList(ATTR_SALES_REP), REPORT_AMOUNT_BY_PRODUCT, "Reports")
                            .addInnerDrillSetting(new DrillSetting(singletonList(ATTR_PRODUCT), TARGET_TAB,
                                    DrillingGroup.DASHBOARDS.getName())));

            tabs.getTab(TARGET_TAB).open();
            deleteDashboardTab(TARGET_TAB);

            tabs.getTab(SOURCE_TAB).open();
            dashboardsPage.editDashboard();
            TableReport report = dashboardsPage.getContent().getLatestReport(TableReport.class);

            DrillingConfigPanel drillingConfigPanel = WidgetConfigPanel
                    .openConfigurationPanelFor(report.getRoot(), browser)
                    .getTab(WidgetConfigPanel.Tab.DRILLING, DrillingConfigPanel.class);

            Pair<String, String> drillingSettings = drillingConfigPanel.getSettingsOnLastItemPanel();
            List<Pair<String, String>> innerDrillSettings = drillingConfigPanel
                    .getAllInnerDrillSettingsOnLastPanel();

            assertEquals(innerDrillSettings, singletonList(Pair.of(ATTR_PRODUCT,
                    SHOW_ME_ON_INNER_DRILL_DEFAULT_VALUE)));
            assertEquals(drillingSettings.getLeft(), ATTR_SALES_REP);
            // somehow displayed text is camel case
            assertEquals(drillingSettings.getRight().toLowerCase(), REPORT_AMOUNT_BY_PRODUCT.toLowerCase());

        } finally {
            new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .deleteObjectsUsingCascade(dashUri);
        }
    }

    private void addDrillSettingsToLatestReport(DrillSetting setting) {
        TableReport report = dashboardsPage.getContent()
                .getLatestReport(TableReport.class);

        dashboardsPage.editDashboard();

        WidgetConfigPanel widgetConfigPanel = WidgetConfigPanel
                .openConfigurationPanelFor(report.getRoot(), browser);
        DrillingConfigPanel drillingConfigPanel = widgetConfigPanel
                .getTab(WidgetConfigPanel.Tab.DRILLING, DrillingConfigPanel.class);

        drillingConfigPanel.addDrilling(setting.getValuesAsPair(), setting.getGroup());
        if (!setting.getInnerDrillSetting().isEmpty()) {
            setting.getInnerDrillSetting().forEach(innerSetting ->
                    drillingConfigPanel.addInnerDrillToLastItemPanel(innerSetting.getValuesAsPair()));
        }

        widgetConfigPanel.saveConfiguration();
        dashboardsPage.saveDashboard();
    }

    private void deleteDashboardTab(String name) {
        DashboardTabs tabs = dashboardsPage.getTabs();
        tabs.getTab(name).open();
        dashboardsPage.deleteDashboardTab(tabs.getSelectedTabIndex());
    }

    private Tab initTabHavingReport(String name) {
        return Builder.of(Tab::new).with(tab -> {
            tab.setTitle(name);
            tab.addItem(Builder.of(ReportItem::new)
                    .with(item -> item.setObjUri(reportHavingOneMetric))
                    .build());
        }).build();
    }

    private Dashboard initDashboardHavingDrillSetting(String name) {
        return Builder.of(Dashboard::new).with(dash -> {
            dash.setName(name);
            dash.addTab(initTabHavingReport(SOURCE_TAB));
            dash.addTab(initTabHavingReport(TARGET_TAB));
        }).build();
    }
}
