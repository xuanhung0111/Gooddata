package com.gooddata.qa.graphene.dashboards;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.dashboard.drilling.DrillSetting;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardDrillDialog;

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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils.deleteAllDashboards;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_AMOUNT_BY_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_AMOUNT_BY_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.mdObjects.dashboard.tab.TabItem.ItemPosition.RIGHT;
import static com.gooddata.qa.mdObjects.dashboard.tab.TabItem.ItemPosition.TOP;
import static com.gooddata.qa.mdObjects.dashboard.tab.TabItem.ItemPosition.TOP_RIGHT;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport.CellType;

public class DrillToDashboardFromDrilledReportTest extends GoodSalesAbstractTest {

    private static final String DASHBOAD_1_NAME = "dashboard 1";
    private static final String DASHBOAD_2_NAME = "dashboard 2";
    private FilterItemContent stageNameAsMultipleFilter;
    private FilterItemContent productAsMultipleFilter;
    private FilterItemContent yearSnapshotFilter;

    private static final String REPORT_DRILLING_GROUP = "Reports";
    private static final String GRAMMAR_PLUS = "Grammar Plus";
    private static final String SHORT_LIST = "Short List";
    private static final String DATE_DIMENSION_VALUE_2011 = "2011";
    private static final String DATE_DIMENSION_SNAPSHOT = "Date dimension (Snapshot)";

    private static final String ERROR_MSG = "The target dashboard tab has been deleted.";

    private static final String TAB1_NAME = "Tab1";
    private static final String TAB2_NAME = "Tab2";
    private static final String TAB3_NAME = "Tab3";
    private static final String TAB4_NAME = "Tab4";

    private Dashboard dashboard1, dashboard2;

    private String amountByProductReportUri;
    private String amountByStageNameReportUri;
    private final static int year = 2011 - LocalDate.now().getYear();

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle = "GoodSales-test-drill-to-dashboard-tab-from-drilled-report";
    }

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();

        amountByProductReportUri = createAmountByProductReport();
        amountByStageNameReportUri = createAmountByStageNameReport();

        //init Dashboard Filter Objects
        stageNameAsMultipleFilter = createMultipleValuesFilter(getAttributeByTitle(ATTR_STAGE_NAME));
        productAsMultipleFilter = createMultipleValuesFilter(getAttributeByTitle(ATTR_PRODUCT));
        yearSnapshotFilter = createDateFilter(getAttributeByIdentifier("snapshot.year"), year, year);
        prepareDashboards();
    }

    @DataProvider
    public Object[][] drillToDashboardData() {
        List<Pair<String, List<String>>> expectedFilterValues1 = asList(
                Pair.of(ATTR_STAGE_NAME, singletonList(SHORT_LIST)),
                Pair.of(ATTR_PRODUCT, singletonList(GRAMMAR_PLUS))
        );

        List<Pair<String, List<String>>> expectedFilterValues2 = asList(
                Pair.of(ATTR_STAGE_NAME, singletonList(SHORT_LIST)),
                Pair.of(ATTR_PRODUCT, singletonList(GRAMMAR_PLUS)),
                Pair.of(DATE_DIMENSION_SNAPSHOT, singletonList(DATE_DIMENSION_VALUE_2011))
        );

        return new Object[][]{
                {DASHBOAD_1_NAME, TAB1_NAME, TAB1_NAME, REPORT_AMOUNT_BY_PRODUCT, expectedFilterValues1},
                {DASHBOAD_1_NAME, TAB1_NAME, TAB2_NAME, REPORT_AMOUNT_BY_STAGE_NAME, expectedFilterValues2}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "drillToDashboardData")
    public void drillReportToTabsInSameDashboard(final String dashboard, final String initTab, final String expectedTab,
                                                 final String expectedReport,
                                                 final List<Pair<String, List<String>>> expectedFilterValues)
            throws JSONException, IOException {
        DashboardsRestUtils.createDashboard(getRestApiClient(),
                testParams.getProjectId(), dashboard1.getMdObject());
        try {
            initDashboardsPage().selectDashboard(dashboard);

            dashboardsPage.getTabs().getTab(initTab).open();

            addDrillSettingsToLatestReport(
                    new DrillSetting(singletonList(ATTR_PRODUCT), REPORT_AMOUNT_BY_STAGE_NAME, REPORT_DRILLING_GROUP)
                            .addInnerDrillSetting(new DrillSetting(singletonList(ATTR_STAGE_NAME), expectedTab,
                                    DrillingConfigPanel.DrillingGroup.DASHBOARDS.getName()))
            );

            performDrillHasInnerDrill(dashboard, expectedTab);

            assertEquals(dashboardsPage.getTabs().getSelectedTab().getLabel(), expectedTab);

            TableReport tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class).waitForLoaded();

            assertEquals(tableReport.getReportTiTle(), expectedReport);
            if (!expectedFilterValues.isEmpty()) {
                for (Pair<String, List<String>> expectedFilterValue : expectedFilterValues) {
                    checkSelectedFilterValues(expectedFilterValue.getLeft(), expectedFilterValue.getRight());
                }
            }
        } finally {
            deleteAllDashboards(getRestApiClient(), testParams.getProjectId());
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillReportToTabInDefferentDashboard() throws JSONException, IOException {
        DashboardsRestUtils.createDashboard(getRestApiClient(),
                testParams.getProjectId(), dashboard1.getMdObject());

        DashboardsRestUtils.createDashboard(getRestApiClient(),
                testParams.getProjectId(), dashboard2.getMdObject());

        try {
            initDashboardsPage().selectDashboard(DASHBOAD_1_NAME).editDashboard();

            addDrillSettingsToLatestReport(
                    new DrillSetting(singletonList(ATTR_PRODUCT), REPORT_AMOUNT_BY_STAGE_NAME, REPORT_DRILLING_GROUP)
                            .addInnerDrillSetting(new DrillSetting(singletonList(ATTR_STAGE_NAME),
                                    TAB3_NAME, DrillingConfigPanel.DrillingGroup.DASHBOARDS.getName()))
            );

            performDrillHasInnerDrill(DASHBOAD_2_NAME, TAB3_NAME);

            assertEquals(dashboardsPage.getDashboardName(), DASHBOAD_2_NAME);
            assertEquals(dashboardsPage.getTabs().getSelectedTab().getLabel(), TAB3_NAME);

            TableReport tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class).waitForLoaded();

            assertEquals(tableReport.getReportTiTle(), REPORT_AMOUNT_BY_PRODUCT);
            assertEquals(dashboardsPage.getContent().getFilterWidgetByName(ATTR_STAGE_NAME).getCurrentValue(),
                    SHORT_LIST);
            assertEquals(dashboardsPage.getContent().getFilterWidgetByName(ATTR_PRODUCT).getCurrentValue(),
                    GRAMMAR_PLUS);
        } finally {
            deleteAllDashboards(getRestApiClient(), testParams.getProjectId());
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

    private void performDrillHasInnerDrill(final String expectedDashboard, String expectedTab) {
        TableReport tableReport = dashboardsPage.getReport(REPORT_AMOUNT_BY_PRODUCT, TableReport.class);
        tableReport.drillOn(GRAMMAR_PLUS, CellType.ATTRIBUTE_VALUE);

        DashboardDrillDialog drillDialog = Graphene.createPageFragment(DashboardDrillDialog.class,
                waitForElementVisible(DashboardDrillDialog.LOCATOR, browser));

        TableReport reportAfterDrillingToReport = drillDialog.getReport(TableReport.class);
        reportAfterDrillingToReport.drillOn(SHORT_LIST, CellType.ATTRIBUTE_VALUE);

        waitForFragmentNotVisible(reportAfterDrillingToReport);

        final Predicate<WebDriver> targetTabIsLoaded =
                browser -> dashboardsPage.getDashboardName().equals(expectedDashboard) &&
                        dashboardsPage.getTabs().getSelectedTab().getLabel().equals(expectedTab);
        Graphene.waitGui().until(targetTabIsLoaded);
    }

    private void checkSelectedFilterValues(String filterName, List<String> expectedValues) {
        assertEquals(dashboardsPage.getContent().getFilterWidgetByName(filterName).getCurrentValue(),
                expectedValues.stream().collect(Collectors.joining(", ")));
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
            dash.addTab(initTab(TAB1_NAME, asList(
                    Pair.of(stageNameAsMultipleFilter, TOP),
                    Pair.of(productAsMultipleFilter, TOP_RIGHT)),
                    amountByProductReportUri));
            dash.addTab(initTab(TAB2_NAME, asList(
                    Pair.of(productAsMultipleFilter, RIGHT),
                    Pair.of(stageNameAsMultipleFilter, RIGHT),
                    Pair.of(yearSnapshotFilter, TOP_RIGHT)),
                    amountByStageNameReportUri
            ));
            dash.addFilter(stageNameAsMultipleFilter);
            dash.addFilter(productAsMultipleFilter);
            dash.addFilter(yearSnapshotFilter);
        }).build();

        dashboard2 = Builder.of(Dashboard::new).with(dash -> {
            dash.setName(DASHBOAD_2_NAME);
            dash.addTab(initTab(TAB3_NAME, asList(
                    Pair.of(stageNameAsMultipleFilter, TOP),
                    Pair.of(productAsMultipleFilter, TOP_RIGHT)),
                    amountByProductReportUri));
            dash.addTab(initTab(TAB4_NAME, asList(
                    Pair.of(productAsMultipleFilter, RIGHT),
                    Pair.of(stageNameAsMultipleFilter, RIGHT),
                    Pair.of(yearSnapshotFilter, TOP_RIGHT)),
                    amountByStageNameReportUri
            ));
            dash.addFilter(stageNameAsMultipleFilter);
            dash.addFilter(productAsMultipleFilter);
            dash.addFilter(yearSnapshotFilter);
        }).build();
    }
}
