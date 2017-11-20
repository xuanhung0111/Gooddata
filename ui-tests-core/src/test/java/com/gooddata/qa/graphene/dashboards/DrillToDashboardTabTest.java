package com.gooddata.qa.graphene.dashboards;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport.CellType;
import com.gooddata.qa.mdObjects.dashboard.Dashboard;
import com.gooddata.qa.mdObjects.dashboard.filter.FilterItemContent;
import com.gooddata.qa.mdObjects.dashboard.tab.FilterItem;
import com.gooddata.qa.mdObjects.dashboard.tab.ReportItem;
import com.gooddata.qa.mdObjects.dashboard.tab.Tab;
import com.gooddata.qa.mdObjects.dashboard.tab.TabItem;
import com.gooddata.qa.mdObjects.dashboard.tab.TabItem.ItemPosition;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils;
import com.gooddata.qa.utils.java.Builder;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_AMOUNT_BY_PRODUCT;
import static com.gooddata.qa.mdObjects.dashboard.tab.TabItem.ItemPosition.RIGHT;
import static com.gooddata.qa.mdObjects.dashboard.tab.TabItem.ItemPosition.TOP;
import static com.gooddata.qa.mdObjects.dashboard.tab.TabItem.ItemPosition.TOP_RIGHT;
import static com.gooddata.qa.utils.http.RestUtils.deleteObjectsUsingCascade;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class DrillToDashboardTabTest extends GoodSalesAbstractTest {

    private final String DASHBOARD_HAVING_SAME_FILTER_MODE = "Dashboard having same filter mode";
    private final String DASHBOARD_HAVING_DIFF_FILTER_MODE = "Dashboard having different filter mode";
    private final String DASHBOARD_HAVING_NO_FILTER_ON_SOURCE_TAB = "Dashboard having no filter on source tab";
    private final String DASHBOARD_HAVING_NO_FILTER_ON_TARGET_TAB = "Dashboard having no filter on target tab";
    private final String SOURCE_TAB = "Source Tab";
    private final String TARGET_TAB = "Target Tab";

    private final String DISCOVERY = "Discovery";
    private final String CLOSED_WON = "Closed Won";
    private final String WEST_14 = "14 West";
    private final String BULBS_1000 = "1000Bulbs.com";
    private final String COMPUSCI = "CompuSci";
    private final String EXPLORER = "Explorer";
    private final String ALL = "All";


    private FilterItemContent stageNameAsSingleFilter;
    private FilterItemContent accountAsMultipleFilter;
    private FilterItemContent stageNameAsMultipleFilter;
    private FilterItemContent accountAsSingleFilter;
    private FilterItemContent productAsSingleFilter;

    private String amountByProductReportUri;

    @Override
    protected void customizeProject() throws Throwable {
        amountByProductReportUri = createAmountByProductReport();

        //init Dashboard Filter Objects
        stageNameAsSingleFilter = createSingleValueFilter(getAttributeByTitle(ATTR_STAGE_NAME));
        accountAsMultipleFilter = createMultipleValuesFilter(getAttributeByTitle(ATTR_ACCOUNT));
        stageNameAsMultipleFilter = createMultipleValuesFilter(getAttributeByTitle(ATTR_STAGE_NAME));
        accountAsSingleFilter = createSingleValueFilter(getAttributeByTitle(ATTR_ACCOUNT));
        productAsSingleFilter = createSingleValueFilter(getAttributeByTitle(ATTR_PRODUCT));
    }

    @DataProvider
    public Object[][] drillToDashboardData() {
        List<Pair<String, List<String>>> changeValuesInSameFilterModeDash = asList(
                Pair.of(ATTR_ACCOUNT, asList(BULBS_1000, WEST_14)),
                Pair.of(ATTR_STAGE_NAME, singletonList(CLOSED_WON)));

        List<Pair<String, List<String>>> expectedValuesInSameFilterModeDash = asList(
                Pair.of(ATTR_ACCOUNT, asList(BULBS_1000, WEST_14)),
                Pair.of(ATTR_PRODUCT, singletonList(COMPUSCI)),
                Pair.of(ATTR_STAGE_NAME, singletonList(CLOSED_WON)));

        List<Pair<String, List<String>>> changeValuesInDiffFilterModeDash = asList(
                Pair.of(ATTR_ACCOUNT, asList(BULBS_1000, WEST_14)),
                Pair.of(ATTR_STAGE_NAME, singletonList(CLOSED_WON)));

        List<Pair<String, List<String>>> expectedValuesInDiffFilterModeDash = asList(
                Pair.of(ATTR_ACCOUNT, singletonList(BULBS_1000)),
                Pair.of(ATTR_STAGE_NAME, singletonList(ALL)),
                Pair.of(ATTR_PRODUCT, singletonList(COMPUSCI)));

        List<Pair<String, List<String>>> changeValuesInNoFilterInSourceDash = new ArrayList<>();

        List<Pair<String, List<String>>> expectedValuesInNoFilterInSourceDash = singletonList(
                Pair.of(ATTR_PRODUCT, singletonList(COMPUSCI)));

        List<Pair<String, List<String>>> changeValuesInNoFilterInTargetDash = asList(
                Pair.of(ATTR_STAGE_NAME, singletonList(DISCOVERY)),
                Pair.of(ATTR_PRODUCT, singletonList(EXPLORER)));

        List<Pair<String, List<String>>> expectedValuesInNoFilterInTargetDash = new ArrayList<>();

        return new Object[][]{
                {DASHBOARD_HAVING_SAME_FILTER_MODE, changeValuesInSameFilterModeDash,
                        expectedValuesInSameFilterModeDash, 1},
                {DASHBOARD_HAVING_DIFF_FILTER_MODE, changeValuesInDiffFilterModeDash,
                        expectedValuesInDiffFilterModeDash, 0},
                {DASHBOARD_HAVING_NO_FILTER_ON_SOURCE_TAB, changeValuesInNoFilterInSourceDash,
                        expectedValuesInNoFilterInSourceDash, 1},
                {DASHBOARD_HAVING_NO_FILTER_ON_TARGET_TAB, changeValuesInNoFilterInTargetDash,
                        expectedValuesInNoFilterInTargetDash, 6}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "drillToDashboardData")
    public void testDrillToDashboardTab(String dashboard, List<Pair<String, List<String>>> filterValuesToChange,
                                        List<Pair<String, List<String>>> expectedFilterValues,
                                        int expectedReportSize) throws IOException, JSONException {

        String dashboardUri = DashboardsRestUtils.createDashboard(getRestApiClient(),
                testParams.getProjectId(), getDashboardByName(dashboard).getMdObject());

        try {
            initDashboardsPage().selectDashboard(dashboard);

            dashboardsPage.getTabs().getTab(TARGET_TAB).open();
            int reportRowCountBeforeDrilling = dashboardsPage.getContent()
                    .getLatestReport(TableReport.class)
                    .getAttributeValues()
                    .size();

            dashboardsPage.getTabs().getTab(SOURCE_TAB).open();
            dashboardsPage.editDashboard();

            TableReport reportOnSourceTab = dashboardsPage.getContent().getReport(REPORT_AMOUNT_BY_PRODUCT, TableReport.class);

            reportOnSourceTab.addDrilling(Pair.of(singletonList(ATTR_PRODUCT), TARGET_TAB), "Dashboards");
            dashboardsPage.saveDashboard();


            if (!filterValuesToChange.isEmpty()) {
                for (Pair<String, List<String>> entry : filterValuesToChange) {
                    changeFilterValues(entry.getLeft(), entry.getRight());
                }
            }

            // att to drill to is CompuSci
            reportOnSourceTab.drillOnFirstValue(CellType.ATTRIBUTE_VALUE);
            reportOnSourceTab.waitForLoaded();

            assertTrue(dashboardsPage.getTabs().getTab(TARGET_TAB).isSelected(),
                    TARGET_TAB + "is not selected after drill action");

            TableReport reportOnTargetTab = dashboardsPage.getContent()
                    .getReport(REPORT_AMOUNT_BY_PRODUCT, TableReport.class).waitForLoaded();
            Screenshots.takeScreenshot(browser, "testDrillToDashboardTab-" + dashboard, getClass());
            if (!expectedFilterValues.isEmpty()) {
                for (Pair<String, List<String>> expectedFilterValue : expectedFilterValues) {
                    checkSelectedFilterValues(expectedFilterValue.getLeft(), expectedFilterValue.getRight());
                }
            }

            assertEquals(reportOnTargetTab.getAttributeValues().size(), expectedReportSize);

            browser.navigate().refresh();
            assertEquals(dashboardsPage.getContent().getLatestReport(TableReport.class).getAttributeValues().size(),
                    reportRowCountBeforeDrilling, "Target tab items are not back to original values");

        } finally {
            deleteObjectsUsingCascade(getRestApiClient(), testParams.getProjectId(), dashboardUri);
        }
    }

    private Tab initDashboardTab(String name, List<TabItem> items) {
        return Builder.of(Tab::new)
                .with(tab -> tab.setTitle(name))
                .with(tab -> tab.addItems(items))
                .build();
    }

    private Dashboard getDashboardByName(String name) {
        List<Dashboard> dashboards = asList(initDashboardHavingDiffFilterMode(), initDashboardHavingSameFilterMode(),
                initDashboardHavingNoFilterInSourceTab(), initDashboardHavingNoFilterInTargetTab());

        return dashboards.stream()
                .filter(dashboard -> dashboard.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Can't find given dashboard"));
    }

    private void changeFilterValues(String filterName, List<String> values) {
        dashboardsPage.getContent().getFilterWidgetByName(filterName)
                .changeAttributeFilterValues(values.toArray(new String[values.size()]));
    }

    private void checkSelectedFilterValues(String filterName, List<String> expectedValues) {
        assertEquals(dashboardsPage.getContent().getFilterWidgetByName(filterName).getCurrentValue(),
                expectedValues.stream().collect(Collectors.joining(", ")));
    }

    private Dashboard initDashboardHavingDiffFilterMode() {
        Tab sourceTab = initTab(SOURCE_TAB, asList(
                Pair.of(stageNameAsSingleFilter, TOP),
                Pair.of(accountAsMultipleFilter, TOP_RIGHT)));

        Tab targetTab = initTab(TARGET_TAB, asList(
                Pair.of(productAsSingleFilter, RIGHT),
                Pair.of(stageNameAsMultipleFilter, TOP),
                Pair.of(accountAsSingleFilter, TOP_RIGHT)
        ));

        return Builder.of(Dashboard::new).with(dash -> {
            dash.setName(DASHBOARD_HAVING_DIFF_FILTER_MODE);
            dash.addTab(sourceTab);
            dash.addTab(targetTab);
            dash.addFilter(stageNameAsSingleFilter);
            dash.addFilter(stageNameAsMultipleFilter);
            dash.addFilter(accountAsSingleFilter);
            dash.addFilter(accountAsMultipleFilter);
            dash.addFilter(productAsSingleFilter);
        }).build();
    }

    private Tab initTab(String name, List<Pair<FilterItemContent, ItemPosition>> appliedFilters) {
        List<FilterItem> filterItems = appliedFilters.stream().map(pair -> Builder.of(FilterItem::new)
                .with(item -> item.setContentId(pair.getLeft().getId()))
                .with(item -> item.setPosition(pair.getRight())).build()).collect(Collectors.toList());

        ReportItem reportItem = createReportItem(amountByProductReportUri,
                filterItems.stream().map(FilterItem::getId).collect(Collectors.toList()));

        return initDashboardTab(name,
                Stream.of(singletonList(reportItem), filterItems).flatMap(List::stream).collect
                        (Collectors.toList()));
    }

    private Dashboard initDashboardHavingSameFilterMode() {
        Tab sourceTab = initTab(SOURCE_TAB, asList(
                Pair.of(stageNameAsSingleFilter, TOP),
                Pair.of(accountAsMultipleFilter, TOP_RIGHT)));

        Tab targetTab = initTab(TARGET_TAB, asList(
                Pair.of(productAsSingleFilter, RIGHT),
                Pair.of(stageNameAsSingleFilter, TOP),
                Pair.of(accountAsMultipleFilter, TOP_RIGHT)
        ));

        return Builder.of(Dashboard::new).with(dash -> {
            dash.setName(DASHBOARD_HAVING_SAME_FILTER_MODE);
            dash.addTab(sourceTab);
            dash.addTab(targetTab);
            dash.addFilter(stageNameAsSingleFilter);
            dash.addFilter(accountAsMultipleFilter);
            dash.addFilter(productAsSingleFilter);
        }).build();
    }

    private Dashboard initDashboardHavingNoFilterInSourceTab() {
        Tab sourceTab = initDashboardTab(SOURCE_TAB, singletonList(createReportItem(amountByProductReportUri)));

        Tab targetTab = initTab(TARGET_TAB, asList(
                Pair.of(stageNameAsMultipleFilter, TOP),
                Pair.of(productAsSingleFilter, RIGHT)
        ));

        return Builder.of(Dashboard::new).with(dash -> {
            dash.setName(DASHBOARD_HAVING_NO_FILTER_ON_SOURCE_TAB);
            dash.addTab(sourceTab);
            dash.addTab(targetTab);
            dash.addFilter(stageNameAsMultipleFilter);
            dash.addFilter(productAsSingleFilter);
        }).build();
    }

    private Dashboard initDashboardHavingNoFilterInTargetTab() {
        Tab sourceTab = initTab(SOURCE_TAB, asList(
                Pair.of(stageNameAsMultipleFilter, TOP),
                Pair.of(productAsSingleFilter, TOP_RIGHT)));

        Tab targetTab = initDashboardTab(TARGET_TAB, singletonList(createReportItem(amountByProductReportUri)));

        return Builder.of(Dashboard::new).with(dash -> {
            dash.setName(DASHBOARD_HAVING_NO_FILTER_ON_TARGET_TAB);
            dash.addTab(sourceTab);
            dash.addTab(targetTab);
            dash.addFilter(stageNameAsMultipleFilter);
            dash.addFilter(productAsSingleFilter);
        }).build();
    }

}
