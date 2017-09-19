package com.gooddata.qa.graphene.dashboards;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.mdObjects.dashboard.Dashboard;
import com.gooddata.qa.mdObjects.dashboard.filter.FilterItemContent;
import com.gooddata.qa.mdObjects.dashboard.tab.FilterItem;
import com.gooddata.qa.mdObjects.dashboard.tab.ReportItem;
import com.gooddata.qa.mdObjects.dashboard.tab.Tab;
import com.gooddata.qa.mdObjects.dashboard.tab.TabItem;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils;
import com.gooddata.qa.utils.java.Builder;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.threeten.extra.YearQuarter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.*;

import static com.gooddata.qa.graphene.enums.DateRange.ZONE_ID;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DIMENSION_CLOSED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DIMENSION_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_AMOUNT_BY_PRODUCT;
import static com.gooddata.qa.mdObjects.dashboard.tab.TabItem.ItemPosition.TOP_RIGHT;
import static com.gooddata.qa.utils.http.RestUtils.deleteObjectsUsingCascade;
import static java.util.Arrays.*;
import static java.util.Collections.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class DrillToDashBoardTabApplyingDateFilterTest extends GoodSalesAbstractTest {

    private final String DASHBOARD_HAVING_SAME_DATE_DIMENSION_AND_RANGE = "Dashboard having SAME date dimension and " +
            "SAME range";
    private final String DASHBOARD_HAVING_SAME_DATE_DIMENSION_AND_DIFF_RANGE = "Dashboard having SAME date dimension " +
            "and DIFFERENT range";
    private final String DASHBOARD_HAVING_DIFF_DATE_DIMENSION_AND_SAME_RANGE = "Dashboard having DIFFERENT date " +
            "dimension and SAME range";
    private final String DASHBOARD_HAVING_DIFF_DATE_DIMENSION_AND_DIFF_RANGE = "Dashboard having DIFFERENT date " +
            "dimension and DIFFERENT range";

    private final String SOURCE_TAB = "Source Tab";
    private final String TARGET_TAB = "Target Tab";

    private final String DASHBOARD_DRILLING_GROUP = "Dashboards";

    private final String DATA_FILTERED_BY_YEAR_2014 = "2014";

    private String testReportUri;

    @Test(dependsOnGroups = {"createProject"})
    public void initData() {
        testReportUri = createAmountByProductReport();
    }

    @DataProvider
    public Object[][] drillToDashboardData() {
        YearQuarter now = YearQuarter.now(ZONE_ID);

        Pair<String, String> changeValuesOnSameDateDimensionAndRangeDash = Pair.of(DATE_DIMENSION_CLOSED,
                DATA_FILTERED_BY_YEAR_2014);

        Pair<String, String> expectedValuesOnSameDateDimensionAndRangeDash = Pair.of(DATE_DIMENSION_CLOSED,
                DATA_FILTERED_BY_YEAR_2014);

        Pair<String, String> changeValuesOnSameDateDimensionAndDiffRangeDash = Pair.of(DATE_DIMENSION_CLOSED,
                DATA_FILTERED_BY_YEAR_2014);

        Pair<String, String> expectedValuesOnSameDateDimensionAndDiffRangeDash = Pair.of(DATE_DIMENSION_CLOSED,
                now.getQuarter() + " " + now.getYear());

        Pair<String, String> changeValuesOnDiffDateDimensionAndSameRangeDash = Pair.of(DATE_DIMENSION_CLOSED,
                DATA_FILTERED_BY_YEAR_2014);

        Pair<String, String> expectedValuesOnDiffDateDimensionAndSameRangeDash = Pair.of
                (DATE_DIMENSION_SNAPSHOT, "2017");

        Pair<String, String> changeValuesOnDiffDateDimensionAndDiffRangeDash = Pair.of(DATE_DIMENSION_CLOSED,
                DATA_FILTERED_BY_YEAR_2014);

        Pair<String, String> expectedValuesOnDiffDateDimensionAndDiffRangeDash = Pair.of
                (DATE_DIMENSION_SNAPSHOT, now.getQuarter() + " " + now.getYear());

        return new Object[][]{
                {DASHBOARD_HAVING_SAME_DATE_DIMENSION_AND_RANGE, changeValuesOnSameDateDimensionAndRangeDash,
                        expectedValuesOnSameDateDimensionAndRangeDash, 3},
                {DASHBOARD_HAVING_SAME_DATE_DIMENSION_AND_DIFF_RANGE, changeValuesOnSameDateDimensionAndDiffRangeDash,
                        expectedValuesOnSameDateDimensionAndDiffRangeDash, 0},
                {DASHBOARD_HAVING_DIFF_DATE_DIMENSION_AND_SAME_RANGE, changeValuesOnDiffDateDimensionAndSameRangeDash,
                        expectedValuesOnDiffDateDimensionAndSameRangeDash, 0},
                {DASHBOARD_HAVING_DIFF_DATE_DIMENSION_AND_DIFF_RANGE, changeValuesOnDiffDateDimensionAndDiffRangeDash,
                        expectedValuesOnDiffDateDimensionAndDiffRangeDash, 0}
        };
    }

    @Test(dependsOnMethods = {"initData"}, dataProvider = "drillToDashboardData")
    public void drillToTabHavingDateFilter(String dashboard, Pair<String, String> filterValuesToChange,
                                           Pair<String, String> expectedFilterValue,
                                           int expectedReportRowCount) throws IOException, JSONException {

        String dashboardUri = DashboardsRestUtils.createDashboard(getRestApiClient(),
                testParams.getProjectId(), getDashboardByName(dashboard).getMdObject());

        try {
            initDashboardsPage().selectDashboard(dashboard).editDashboard();

            TableReport reportOnSourceTab = dashboardsPage.getContent().getReport(REPORT_AMOUNT_BY_PRODUCT, TableReport.class);

            reportOnSourceTab.addDrilling(Pair.of(singletonList(ATTR_PRODUCT), TARGET_TAB), DASHBOARD_DRILLING_GROUP);
            dashboardsPage.saveDashboard();

            changeFilterValues(filterValuesToChange.getLeft(), filterValuesToChange.getRight());

            // att to drill to is CompuSci
            reportOnSourceTab.drillOnAttributeValue();
            reportOnSourceTab.waitForTableReportExecutionProgress();

            assertTrue(dashboardsPage.getTabs().getTab(TARGET_TAB).isSelected(),
                    TARGET_TAB + "is not selected after drill action");

            TableReport reportOnTargetTab = dashboardsPage.getContent()
                    .getReport(REPORT_AMOUNT_BY_PRODUCT, TableReport.class).waitForTableReportExecutionProgress();
            Screenshots.takeScreenshot(browser, "testDrillToDashboardTab-" + dashboard, getClass());
            checkSelectedFilterValues(expectedFilterValue.getLeft(), expectedFilterValue.getRight());
            assertEquals(reportOnTargetTab.getAttributeElements().size(), expectedReportRowCount,
                    "Report is not applied date filter");
        } finally {
            deleteObjectsUsingCascade(getRestApiClient(), testParams.getProjectId(), dashboardUri);
        }
    }

    @Test(dependsOnMethods = {"initData"})
    public void drillToTabOnAnotherDashboard() throws IOException, JSONException {
        String firstDash = "Dashboard 1";
        String secondDash = "Dashboard 2";

        // init dashboard 1
        FilterItemContent sourceFilterContent = createDateFilter(getAttributeByIdentifier("closed.year"), 0, 0);

        Tab sourceTab = initTab(SOURCE_TAB, Pair.of(sourceFilterContent, TOP_RIGHT));

        Dashboard dashboard_1 = Builder.of(Dashboard::new).with(dashboard -> {
            dashboard.setName(firstDash);
            dashboard.addTab(sourceTab);
            dashboard.addFilter(sourceFilterContent);
        }).build();

        // init dashboard 2
        FilterItemContent targetFilterContent = createDateFilter(getAttributeByIdentifier("closed.year"), -1, -1);

        Tab targetTab = initTab(TARGET_TAB, Pair.of(targetFilterContent, TOP_RIGHT));

        Dashboard dashboard2 = Builder.of(Dashboard::new).with(dashboard -> {
            dashboard.setName(secondDash);
            dashboard.addTab(targetTab);
            dashboard.addFilter(targetFilterContent);
        }).build();

        List<String> dashboardUris = new ArrayList<>();
        for (Dashboard dashboard : asList(dashboard_1, dashboard2)) {
            dashboardUris.add(DashboardsRestUtils
                    .createDashboard(getRestApiClient(), testParams.getProjectId(), dashboard.getMdObject()));
        }

        try {
            initDashboardsPage().selectDashboard(firstDash).editDashboard();

            TableReport reportOnFirstDash = dashboardsPage.getContent()
                    .getLatestReport(TableReport.class);

            reportOnFirstDash.addDrilling(Pair.of(singletonList(ATTR_PRODUCT), TARGET_TAB), DASHBOARD_DRILLING_GROUP);
            dashboardsPage.saveDashboard();

            changeFilterValues(DATE_DIMENSION_CLOSED, DATA_FILTERED_BY_YEAR_2014);
            reportOnFirstDash.waitForTableReportExecutionProgress().drillOnAttributeValue();

            TableReport reportOnSecondDash = dashboardsPage.getContent()
                    .getLatestReport(TableReport.class).waitForTableReportExecutionProgress();

            assertEquals(reportOnSecondDash.getAttributeElements().size(), 3);
            checkSelectedFilterValues(DATE_DIMENSION_CLOSED, DATA_FILTERED_BY_YEAR_2014);
        } finally {
            for (String dashboardUri : dashboardUris) {
                deleteObjectsUsingCascade(getRestApiClient(), testParams.getProjectId(), dashboardUri);
            }
        }
    }

    private void changeFilterValues(String filterName, String valueOnTimeline) {
        dashboardsPage.getContent().getFilterWidgetByName(filterName)
                .changeTimeFilterValueByClickInTimeLine(valueOnTimeline);
    }

    private void checkSelectedFilterValues(String filterName, String expectedValue) {
        assertEquals(dashboardsPage.getContent().getFilterWidgetByName(filterName).getCurrentValue(), expectedValue);
    }

    private Dashboard initDashboardHavingSameDimensionAndRange() {
        FilterItemContent sourceFilterContent = createDateFilter(getAttributeByIdentifier("closed.year"), 0, 0);
        Tab sourceTab = initTab(SOURCE_TAB, Pair.of(sourceFilterContent, TOP_RIGHT));

        FilterItemContent targetFilterContent = createDateFilter(getAttributeByIdentifier("closed.year"), -1, -1);
        Tab targetTab = initTab(TARGET_TAB, Pair.of(targetFilterContent, TOP_RIGHT));

        return Builder.of(Dashboard::new).with(dashboard -> {
            dashboard.setName(DASHBOARD_HAVING_SAME_DATE_DIMENSION_AND_RANGE);
            dashboard.addTab(sourceTab);
            dashboard.addTab(targetTab);
            dashboard.addFilter(sourceFilterContent);
            dashboard.addFilter(targetFilterContent);
        }).build();
    }

    private Dashboard initDashboardHavingSameDimensionAndDiffRange() {
        FilterItemContent sourceFilterContent = createDateFilter(getAttributeByIdentifier("closed.year"), 0, 0);
        Tab sourceTab = initTab(SOURCE_TAB, Pair.of(sourceFilterContent, TOP_RIGHT));

        FilterItemContent targetFilterContent = createDateFilter(getAttributeByIdentifier("closed.quarter"), 0, 0);
        Tab targetTab = initTab(TARGET_TAB, Pair.of(targetFilterContent, TOP_RIGHT));

        return Builder.of(Dashboard::new).with(dashboard -> {
            dashboard.setName(DASHBOARD_HAVING_SAME_DATE_DIMENSION_AND_DIFF_RANGE);
            dashboard.addTab(sourceTab);
            dashboard.addTab(targetTab);
            dashboard.addFilter(sourceFilterContent);
            dashboard.addFilter(targetFilterContent);
        }).build();
    }

    private Dashboard initDashboardHavingDiffDimensionAndSameRange() {
        FilterItemContent sourceFilterContent = createDateFilter(getAttributeByIdentifier("closed.year"), 0, 0);
        Tab sourceTab = initTab(SOURCE_TAB, Pair.of(sourceFilterContent, TOP_RIGHT));

        FilterItemContent targetFilterContent = createDateFilter(getAttributeByIdentifier("snapshot.year"), 0, 0);
        Tab targetTab = initTab(TARGET_TAB, Pair.of(targetFilterContent, TOP_RIGHT));

        return Builder.of(Dashboard::new).with(dashboard -> {
            dashboard.setName(DASHBOARD_HAVING_DIFF_DATE_DIMENSION_AND_SAME_RANGE);
            dashboard.addTab(sourceTab);
            dashboard.addTab(targetTab);
            dashboard.addFilter(sourceFilterContent);
            dashboard.addFilter(targetFilterContent);
        }).build();
    }

    private Dashboard initDashboardHavingDiffDimensionAndDiffRange() {
        FilterItemContent sourceFilterContent = createDateFilter(getAttributeByIdentifier("closed.year"), 0, 0);
        Tab sourceTab = initTab(SOURCE_TAB, Pair.of(sourceFilterContent, TOP_RIGHT));

        FilterItemContent targetFilterContent = createDateFilter(getAttributeByIdentifier("snapshot.quarter"), 0, 0);
        Tab targetTab = initTab(TARGET_TAB, Pair.of(targetFilterContent, TOP_RIGHT));

        return Builder.of(Dashboard::new).with(dashboard -> {
            dashboard.setName(DASHBOARD_HAVING_DIFF_DATE_DIMENSION_AND_DIFF_RANGE);
            dashboard.addTab(sourceTab);
            dashboard.addTab(targetTab);
            dashboard.addFilter(sourceFilterContent);
            dashboard.addFilter(targetFilterContent);
        }).build();
    }

    private Dashboard getDashboardByName(String name) {
        List<Dashboard> dashboards = asList(
                initDashboardHavingSameDimensionAndRange(),
                initDashboardHavingSameDimensionAndDiffRange(),
                initDashboardHavingDiffDimensionAndSameRange(),
                initDashboardHavingDiffDimensionAndDiffRange());

        return dashboards.stream()
                .filter(dashboard -> dashboard.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Can't find given dashboard"));
    }

    private Tab initTab(String name, Pair<FilterItemContent, TabItem.ItemPosition> appliedFilter) {
        FilterItem filterItem = Builder.of(FilterItem::new)
                .with(item -> item.setContentId(appliedFilter.getLeft().getId()))
                .with(item -> item.setPosition(appliedFilter.getRight())).build();

        ReportItem reportItem = createReportItem(testReportUri, singletonList(filterItem.getId()));

        return Builder.of(Tab::new)
                .with(tab -> tab.setTitle(name))
                .with(tab -> tab.addItems(asList(reportItem, filterItem)))
                .build();
    }
}
