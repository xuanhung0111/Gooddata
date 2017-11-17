package com.gooddata.qa.graphene.dashboards;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.dashboard.drilling.DrillSetting;
import com.gooddata.qa.graphene.fragments.common.StatusBar;
import com.gooddata.qa.graphene.fragments.dashboards.SaveAsDialog;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.DrillingConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.mdObjects.dashboard.Dashboard;
import com.gooddata.qa.mdObjects.dashboard.filter.FilterItemContent;
import com.gooddata.qa.mdObjects.dashboard.tab.FilterItem;
import com.gooddata.qa.mdObjects.dashboard.tab.ReportItem;
import com.gooddata.qa.mdObjects.dashboard.tab.Tab;
import com.gooddata.qa.mdObjects.dashboard.tab.TabItem;
import com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils;
import com.gooddata.qa.utils.java.Builder;
import com.google.common.base.Predicate;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_AMOUNT_BY_PRODUCT;
import static com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils.deleteAllDashboards;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;

public class DrillFromCopiedDashboardTest extends GoodSalesAbstractTest {
    private static final String DASHBOAD_1_NAME = "dashboard 1";
    private static final String DASHBOAD_2_NAME = "dashboard 2";

    private static final String DASHBOARD_DRILLING_GROUP = "Dashboards";
    private static final String GRAMMAR_PLUS = "Grammar Plus";

    private static final String ERROR_MSG = "The target dashboard tab has been deleted.";

    private static final String TAB1_NAME = "Tab1";
    private static final String TAB2_NAME = "Tab2";
    private static final String TAB3_NAME = "Tab3";
    private static final String TAB4_NAME = "Tab4";

    private Dashboard dashboard1, dashboard2;

    private String amountByProductReportUri;
    private String amountByStageNameReportUri;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle = "GoodSales-test-drill-to-dashboard-tab-from-drilled-report";
    }

    @Override
    protected void customizeProject() throws Throwable {
        amountByProductReportUri = createAmountByProductReport();
        amountByStageNameReportUri = createAmountByStageNameReport();

        prepareDashboards();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillReportToTabFromCopiedDashboard()
            throws JSONException, IOException {
        // test drill to existing tab of target dashboard
        checkDrillReportToDeletedTabFromCopiedDashboard(DASHBOAD_1_NAME,
                TAB1_NAME,
                TAB2_NAME,
                DASHBOAD_1_NAME + " Copied1");
        // test drill to newly created tab of target dashboard
        checkDrillReportToDeletedTabFromCopiedDashboard(DASHBOAD_1_NAME,
                TAB1_NAME,
                "New Tab",
                DASHBOAD_1_NAME + " Copied2");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillReportToDeletedTabFromCopiedDashboard()
            throws JSONException, IOException {
        DashboardsRestUtils.createDashboard(getRestApiClient(),
                testParams.getProjectId(), dashboard1.getMdObject());
        try {
            initDashboardsPage().selectDashboard(DASHBOAD_1_NAME);
            dashboardsPage.getTabs().getTab(TAB1_NAME).open();

            addDrillSettingsToLatestReport(
                    new DrillSetting(singletonList(ATTR_PRODUCT), TAB2_NAME, DASHBOARD_DRILLING_GROUP)
            );
            dashboardsPage.deleteDashboardTab(1);
            dashboardsPage.saveAsDashboard(DASHBOAD_1_NAME + " Copied",
                    SaveAsDialog.PermissionType.USE_EXISTING_PERMISSIONS);

            assertEquals(dashboardsPage.getDashboardName(), DASHBOAD_1_NAME + " Copied");

            TableReport tableReport = dashboardsPage.getReport(REPORT_AMOUNT_BY_PRODUCT, TableReport.class);
            tableReport.drillOn(GRAMMAR_PLUS, TableReport.CellType.ATTRIBUTE_VALUE);

            assertEquals(StatusBar.getInstance(browser).getMessage(), ERROR_MSG);
        } finally {
            deleteAllDashboards(getRestApiClient(), testParams.getProjectId());
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillCopiedBetweenDashboards() throws JSONException, IOException {
        DashboardsRestUtils.createDashboard(getRestApiClient(),
                testParams.getProjectId(), dashboard1.getMdObject());

        DashboardsRestUtils.createDashboard(getRestApiClient(),
                testParams.getProjectId(), dashboard2.getMdObject());
        try {
            initDashboardsPage().selectDashboard(DASHBOAD_1_NAME).editDashboard();
            addDrillSettingsToLatestReport(
                    new DrillSetting(singletonList(ATTR_PRODUCT), TAB3_NAME, DASHBOARD_DRILLING_GROUP)
            );
            dashboardsPage.saveAsDashboard(DASHBOAD_1_NAME + " Copied",
                    SaveAsDialog.PermissionType.USE_EXISTING_PERMISSIONS);

            assertEquals(dashboardsPage.getDashboardName(), DASHBOAD_1_NAME + " Copied");
            performDrill(DASHBOAD_2_NAME, TAB3_NAME);
            assertEquals(dashboardsPage.getDashboardName(), DASHBOAD_2_NAME);
            assertEquals(dashboardsPage.getTabs().getSelectedTab().getLabel(), TAB3_NAME);
        } finally {
            deleteAllDashboards(getRestApiClient(), testParams.getProjectId());
        }
    }

    private void checkDrillReportToDeletedTabFromCopiedDashboard(final String sourceDashboard,
                                                                    final String sourceTab,
                                                                    final String targetTab,
                                                                    final String saveAsDashboard)
            throws JSONException, IOException {
        DashboardsRestUtils.createDashboard(getRestApiClient(),
                testParams.getProjectId(), dashboard1.getMdObject());
        try {
            initDashboardsPage().selectDashboard(sourceDashboard);
            if (!dashboardsPage.getTabs().getAllTabNames().contains(targetTab)) {
                dashboardsPage.addNewTab(targetTab);
                dashboardsPage.saveDashboard();
            }
            dashboardsPage.getTabs().getTab(sourceTab).open();

            addDrillSettingsToLatestReport(
                    new DrillSetting(singletonList(ATTR_PRODUCT), targetTab, DASHBOARD_DRILLING_GROUP)
            );
            dashboardsPage.editDashboard().saveAsDashboard(saveAsDashboard, false,
                    SaveAsDialog.PermissionType.USE_EXISTING_PERMISSIONS);

            performDrill(saveAsDashboard, targetTab);

            assertEquals(dashboardsPage.getDashboardName(), saveAsDashboard);
            assertEquals(dashboardsPage.getTabs().getSelectedTab().getLabel(), targetTab);
        } finally {
            deleteAllDashboards(getRestApiClient(), testParams.getProjectId());
        }
    }

    private void performDrill(final String expectedDashboard, final String expectedTab) {
        TableReport tableReport = dashboardsPage.getReport(REPORT_AMOUNT_BY_PRODUCT, TableReport.class);
        tableReport.drillOn(GRAMMAR_PLUS, TableReport.CellType.ATTRIBUTE_VALUE);

        final Predicate<WebDriver> targetTabIsLoaded =
                browser -> dashboardsPage.getDashboardName().equals(expectedDashboard) &&
                        dashboardsPage.getTabs().getSelectedTab().getLabel().equals(expectedTab);
        Graphene.waitGui().until(targetTabIsLoaded);
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

    private Tab initTab(String name, List<Pair<FilterItemContent, TabItem.ItemPosition>> appliedFilters,
                        String reportName) {
        List<FilterItem> filterItems = appliedFilters.stream().map(pair -> Builder.of(FilterItem::new)
                .with(item -> item.setContentId(pair.getLeft().getId()))
                .with(item -> item.setPosition(pair.getRight())).build()).collect(Collectors.toList());

        ReportItem reportItem = createReportItem(reportName,
                filterItems.stream().map(FilterItem::getId).collect(Collectors.toList()));

        return initDashboardTab(name,
                Stream.of(singletonList(reportItem), filterItems).flatMap(List::stream).collect
                        (Collectors.toList()));
    }

    private Tab initDashboardTab(String name, List<TabItem> items) {
        return Builder.of(Tab::new)
                .with(tab -> tab.setTitle(name))
                .with(tab -> tab.addItems(items))
                .build();
    }

    private void prepareDashboards() {
        dashboard1 = Builder.of(Dashboard::new).with(dash -> {
            dash.setName(DASHBOAD_1_NAME);
            dash.addTab(initTab(TAB1_NAME, Collections.emptyList(),
                    amountByProductReportUri));
            dash.addTab(initTab(TAB2_NAME, Collections.emptyList(),
                    amountByStageNameReportUri
            ));
        }).build();

        dashboard2 = Builder.of(Dashboard::new).with(dash -> {
            dash.setName(DASHBOAD_2_NAME);
            dash.addTab(initTab(TAB3_NAME, Collections.emptyList(),
                    amountByProductReportUri));
            dash.addTab(initTab(TAB4_NAME, Collections.emptyList(),
                    amountByStageNameReportUri
            ));
        }).build();
    }
}
