package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.PivotTableReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.DrillModalDialog;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.attribute.AttributeRestRequest;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.*;
import static java.util.stream.Collectors.toList;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class KPIDashboardsDrillDownTest extends AbstractDashboardTest {
    private final String TABLE_ONLY_MEASURES_COLUMNS = "Table only measures and columns";
    private final String TABLE_ONLY_ROWS = "Table only rows";
    private final String TABLE_ONLY_ROWS_MEASURES = "Table only measures and rows";
    private final String TABLE_ONLY_ROWS_COLUMNS = "Table only columns and rows";
    private final String TABLE_HAS_MEASURES_ROWS_COLUMNS = "Table has measure rows and columns";
    private final String TABLE_TWO_DRILLABLED_ATTRIBUTES = "Table with one attribute and two drillable attributes";
    private final String TABLE_TWO_ATTRIBUTES_WITH_TWO_DRILLABLED_ATTRIBUTES = "Table with two attributes and two drillable attributes";
    private final String TABLE_TWO_ATTRIBUTES_WITH_TWO_DRILLABLED_ATTRIBUTES_SEPARATE = "Table with two attributes and two drillable separate attributes";
    private final String COLUMN_CHART_ONLY_MEASURES = "Column chart only measures";
    private final String HEAT_MAP_ONLY_MEASURES = "Heat map only measures";
    private final String LINE_CHART_ONLY_MEASURES = "Line chart only measures";
    private final String BULLET_CHART_ONLY_MEASURES = "Bullet chart only measures";
    private final String COLUMN_CHART_ONLY_VIEWBY_MEASURES = "Column chart only measures and viewby";
    private final String HEAT_MAP_ONLY_ROWS_MEASURES = "Heat map only measures and rows";
    private final String LINE_CHART_ONLY_TRENDBY_MEASURES = "Line chart only measures and trendby";
    private final String BULLET_CHART_ONLY_VIEWBY_MEASURES = "Bullet chart only measures and viewby";
    private final String COLUMN_CHART_ONLY_STACKBY_MEASURES = "Column chart only measures and stackby";
    private final String LINE_CHART_ONLY_SEGMENTBY_MEASURES = "Line chart only measures and segmentby";
    private final String HEAT_MAP_ONLY_COLUMN_MEASURES = "Heat map only measures and column";
    private final String COLUMN_CHART_MEASURES_STACKBY_VIEWBY = "Column chart only measures, stackby and viewby";
    private final String HEAT_MAP_MEASURES_ROW_COLUMN = "Heat map only measures, row and column";
    private final String LINE_CHART_MEASURES_TRENDBY_SEGMENTBY = "Line chart only measures, trendby and segmentby";
    private final String BULLET_CHART_MEASURES_VIEWBYS = "Bullet chart only measures and two viewbys";
    private final String COLUMN_CHART_TWO_DRILLABLED_ATTRIBUTES = "Column chart with two drillable attribute";
    private final String HEAT_MAP_TWO_DRILLABLED_ATTRIBUTES = "Heat map with two drillable attribute";
    private final String LINE_CHART_TWO_DRILLABLED_ATTRIBUTES = "Line chart with two drillable attribute";
    private final String BULLET_CHART_TWO_DRILLABLED_ATTRIBUTES = "Bullet chart with two drillable attribute";
    private final String COLUMN_CHART_TWO_ATTRIBUTES_DIFFERENCE_BUCKET = "Column chart with two attributes on difference bucket and two drillable attributes";
    private final String HEAT_MAP_TWO_ATTRIBUTES_DIFFERENCE_BUCKET = "Heat map with two attributes on difference bucket and two drillable attributes";
    private final String LINE_CHART_TWO_ATTRIBUTES_DIFFERENCE_BUCKET = "Line chart with two attributes on difference bucket and two drillable attributes";
    private final String COLUMN_CHART_TWO_ATTRIBUTES_SAME_BUCKET = "Column chart with two attributes on same bucket and two drillable attributes";
    private final String BULLET_CHART_TWO_ATTRIBUTES_SAME_BUCKET = "Bullet chart with two attributes on same bucket and two drillable attributes";
    private final String COLUMN_CHART_TWO_DRILLABLED_ATTRIBUTES_SEPARATE = "Column chart with two attributes and two drillable separate attributes";
    private final String BULLET_CHART_TWO_DRILLABLED_ATTRIBUTES_SEPARATE = "Bullet chart with two attributes and two drillable separate attributes";

    private final String DASHBOARD_TABLE_ONLY_MEASURES_COLUMNS = "Dashboard with table only measures and columns";
    private final String DASHBOARD_TABLE_ONLY_ROWS = "Dashboard table only rows";
    private final String DASHBOARD_TABLE_ONLY_ROWS_MEASURES = "Dashboard table only rows and measures";
    private final String DASHBOARD_TABLE_ONLY_ROWS_COLUMNS = "Dashboard table only rows and columns";
    private final String DASHBOARD_TABLE_HAS_MEASURES_ROWS_COLUMNS = "Dashboard table only measures rows and columns";
    private final String DASHBOARD_TABLE_TWO_DRILLABLED_ATTRIBUTES = "Dashboard with table one attribute that has two drillable attributes";
    private final String DASHBOARD_TABLE_TWO_ATTRIBUTES_TWO_DRILLABLED_ATTRIBUTES = "Dashboard with table two attributes that has two drillable attributes";
    private final String DASHBOARD_TABLE_TWO_ATTRIBUTES_TWO_DRILLABLED_ATTRIBUTES_SEPARATE = "Dashboard with table two attributes that has two drillable separate attributes";
    private final String DASHBOARD_COLUMN_CHART_ONLY_MEASURES = "Dashboard drill down with column chart only measures";
    private final String DASHBOARD_HEAT_MAP_ONLY_MEASURES = "Dashboard drill down with heat map only measures";
    private final String DASHBOARD_LINE_CHART_ONLY_MEASURES = "Dashboard drill down with line chart only measures";
    private final String DASHBOARD_BULLET_CHART_ONLY_MEASURES = "Dashboard drill down with bullet chart only measures";
    private final String DASHBOARD_COLUMN_CHART_ONLY_VIEWBY_MEASURES = "Dashboard column chart only measures and viewby";
    private final String DASHBOARD_HEAT_MAP_ONLY_ROWS_MEASURES = "Dashboard heat map only measures and rows";
    private final String DASHBOARD_LINE_CHART_ONLY_TRENDBY_MEASURES = "Dashboard line chart only measures and trendby";
    private final String DASHBOARD_BULLET_CHART_ONLY_VIEWBY_MEASURES = "Dashboard bullet chart only measures and viewby";
    private final String DASHBOARD_COLUMN_CHART_ONLY_STACKBY_MEASURES = "Dashboard column chart only measures and stackby";
    private final String DASHBOARD_LINE_CHART_ONLY_SEGMENTBY_MEASURES = "Dashboard line chart only measures and segmentby";
    private final String DASHBOARD_HEAT_MAP_ONLY_COLUMN_MEASURES = "Dashboard heat map only measures and column";
    private final String DASHBOARD_COLUMN_CHART_MEASURES_STACKBY_VIEWBY = "Dashboard column chart has measures stackby, viewby";
    private final String DASHBOARD_HEAT_MAP_MEASURES_ROW_COLUMN = "Dashboard Heat map only measures, row and column";
    private final String DASHBOARD_LINE_CHART_MEASURES_TRENDBY_SEGMENTBY = "Dashboard Line chart only measures, trendby and segmentby";
    private final String DASHBOARD_BULLET_CHART_MEASURES_VIEWBYS = "Dashboard Bullet chart only measures ands two viewbys";
    private final String DASHBOARD_COLUMN_CHART_TWO_DRILLABLED_ATTRIBUTES = "Dashboard column chart with two drillable attribute";
    private final String DASHBOARD_HEAT_MAP_TWO_DRILLABLED_ATTRIBUTES = "Dashboard heat map with two drillable attribute";
    private final String DASHBOARD_LINE_CHART_TWO_DRILLABLED_ATTRIBUTES = "Dashboard line chart with two drillable attribute";
    private final String DASHBOARD_BULLET_CHART_TWO_DRILLABLED_ATTRIBUTES = "Dashboard bullet chart with two drillable attribute";
    private final String DASHBOARD_COLUMN_CHART_TWO_ATTRIBUTES_DIFFERENCE_BUCKET = "Dashboard column chart with two attributes on difference bucket";
    private final String DASHBOARD_HEAT_MAP_TWO_ATTRIBUTES_DIFFERENCE_BUCKET = "Dashboard heat map with two attributes on difference bucket";
    private final String DASHBOARD_LINE_CHART_TWO_ATTRIBUTES_DIFFERENCE_BUCKET = "Dashboard line chart with two attributes on difference bucket";
    private final String DASHBOARD_COLUMN_CHART_TWO_ATTRIBUTES_SAME_BUCKET = "Dashboard column chart with two attributes on same bucket";
    private final String DASHBOARD_BULLET_CHART_TWO_ATTRIBUTES_SAME_BUCKET = "Dashboard bullet chart with two attributes on same bucket";
    private final String DASHBOARD_COLUMN_CHART_TWO_DRILLABLED_ATTRIBUTES_SEPARATE = "Dashboard with column chart two attributes that has two drillable separate attributes";
    private final String DASHBOARD_BULLET_CHART_TWO_DRILLABLED_ATTRIBUTES_SEPARATE = "Dashboard with bullet chart two attributes that has two drillable separate attributes";

    private final String ATTRIBUTE_TITLE_TRUE = "true";
    private final String ATTRIBUTE_TITLE_FALSE = "false";
    private final String ATTRIBUTE_TITLE_DIRECT_SALES = "Direct Sales";
    private final String ATTRIBUTE_TITLE_INSIDE_SALES = "Inside Sales";
    private final String ATTRIBUTE_TITLE_2011 = "2011";

    private IndigoRestRequest indigoRestRequest;
    ProjectRestRequest projectRestRequest;
    AttributeRestRequest attributeRestRequest;

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createSnapshotBOPMetric();
        metrics.createSnapshotEOPMetric();
        metrics.createTimelineEOPMetric();
        metrics.createTimelineBOPMetric();
        metrics.createAmountMetric();
        metrics.createAvgAmountMetric();
        getMetricCreator().createBestCaseMetric();
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_KPI_DASHBOARD_DRILL_TO_INSIGHT, true);
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_KPI_DASHBOARD_DRILL_TO_DASHBOARD, true);
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_KPI_DASHBOARD_DRILL_DOWN, true);

        attributeRestRequest = new AttributeRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        attributeRestRequest.setDrillDown(ATTR_IS_WON, getAttributeDisplayFormUri(ATTR_DEPARTMENT));
        attributeRestRequest.setDrillDown(ATTR_DEPARTMENT, getAttributeDisplayFormUri(ATTR_PRODUCT));

        createInsight(TABLE_ONLY_MEASURES_COLUMNS, asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT), asList(
            Pair.of(ATTR_IS_WON, CategoryBucket.Type.COLUMNS), Pair.of(ATTR_REGION, CategoryBucket.Type.COLUMNS)));
        createInsight(TABLE_ONLY_ROWS, asList(), asList( Pair.of(ATTR_IS_WON, CategoryBucket.Type.ATTRIBUTE)));
        createInsight(TABLE_ONLY_ROWS_MEASURES, asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT), asList(
            Pair.of(ATTR_IS_WON, CategoryBucket.Type.ATTRIBUTE), Pair.of(ATTR_REGION, CategoryBucket.Type.ATTRIBUTE)));
        createInsight(TABLE_ONLY_ROWS_COLUMNS, asList(), asList(
            Pair.of(ATTR_IS_WON, CategoryBucket.Type.ATTRIBUTE), Pair.of(ATTR_REGION, CategoryBucket.Type.COLUMNS), Pair.of(ATTR_IS_ACTIVE, CategoryBucket.Type.COLUMNS)));
        createInsight(TABLE_HAS_MEASURES_ROWS_COLUMNS, asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT), asList(
            Pair.of(ATTR_IS_WON, CategoryBucket.Type.ATTRIBUTE), Pair.of(ATTR_REGION, CategoryBucket.Type.COLUMNS), Pair.of(ATTR_IS_ACTIVE, CategoryBucket.Type.COLUMNS)));
        createInsight(TABLE_TWO_DRILLABLED_ATTRIBUTES, asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT), asList(
            Pair.of(ATTR_IS_WON, CategoryBucket.Type.ATTRIBUTE), Pair.of(ATTR_REGION, CategoryBucket.Type.COLUMNS)));
        createInsight(TABLE_TWO_ATTRIBUTES_WITH_TWO_DRILLABLED_ATTRIBUTES, asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT),
            asList(Pair.of(ATTR_IS_WON, CategoryBucket.Type.ATTRIBUTE), Pair.of(ATTR_DEPARTMENT, CategoryBucket.Type.ATTRIBUTE),
            Pair.of(ATTR_REGION, CategoryBucket.Type.COLUMNS)));
        createColumnChart(COLUMN_CHART_ONLY_MEASURES, asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT), asList());
        createHeatMap(HEAT_MAP_ONLY_MEASURES,METRIC_AMOUNT, asList());
        createLineChart(LINE_CHART_ONLY_MEASURES,asList(METRIC_AMOUNT), asList());
        createBulletChart(BULLET_CHART_ONLY_MEASURES,METRIC_AMOUNT, METRIC_AVG_AMOUNT, METRIC_BEST_CASE, asList());
        createColumnChart(COLUMN_CHART_ONLY_VIEWBY_MEASURES, asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT), asList(Pair.of(ATTR_IS_WON, CategoryBucket.Type.VIEW)));
        createHeatMap(HEAT_MAP_ONLY_ROWS_MEASURES, METRIC_AMOUNT, asList(Pair.of(ATTR_IS_WON, CategoryBucket.Type.VIEW)));
        createLineChart(LINE_CHART_ONLY_TRENDBY_MEASURES, asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT), asList(Pair.of(ATTR_IS_WON, CategoryBucket.Type.TREND)));
        createBulletChart(BULLET_CHART_ONLY_VIEWBY_MEASURES, METRIC_AMOUNT, METRIC_AVG_AMOUNT, METRIC_BEST_CASE, asList(Pair.of(ATTR_IS_WON, CategoryBucket.Type.VIEW)));
        createColumnChart(COLUMN_CHART_ONLY_STACKBY_MEASURES, asList(METRIC_AMOUNT), asList(Pair.of(ATTR_IS_WON, CategoryBucket.Type.STACK)));
        createHeatMap(HEAT_MAP_ONLY_COLUMN_MEASURES, METRIC_AMOUNT, asList(Pair.of(ATTR_IS_WON, CategoryBucket.Type.STACK)));
        createLineChart(LINE_CHART_ONLY_SEGMENTBY_MEASURES, asList(METRIC_AMOUNT), asList(Pair.of(ATTR_IS_WON, CategoryBucket.Type.SEGMENT)));
        createColumnChart(COLUMN_CHART_MEASURES_STACKBY_VIEWBY, asList(METRIC_AMOUNT), asList(Pair.of(ATTR_REGION, CategoryBucket.Type.VIEW),
            Pair.of(ATTR_IS_WON, CategoryBucket.Type.STACK)));
        createHeatMap(HEAT_MAP_MEASURES_ROW_COLUMN, METRIC_AMOUNT, asList(Pair.of(ATTR_REGION, CategoryBucket.Type.VIEW),
            Pair.of(ATTR_IS_WON, CategoryBucket.Type.STACK)));
        createLineChart(LINE_CHART_MEASURES_TRENDBY_SEGMENTBY, asList(METRIC_AMOUNT), asList(Pair.of(ATTR_REGION, CategoryBucket.Type.TREND),
            Pair.of(ATTR_IS_WON, CategoryBucket.Type.SEGMENT)));
        createBulletChart(BULLET_CHART_MEASURES_VIEWBYS, METRIC_AMOUNT, METRIC_AVG_AMOUNT, METRIC_BEST_CASE,
            asList(Pair.of(ATTR_REGION, CategoryBucket.Type.VIEW), Pair.of(ATTR_IS_WON, CategoryBucket.Type.VIEW)));
        createColumnChart(COLUMN_CHART_TWO_DRILLABLED_ATTRIBUTES, asList(METRIC_AMOUNT), asList(Pair.of(ATTR_IS_WON, CategoryBucket.Type.VIEW),
            Pair.of(ATTR_REGION, CategoryBucket.Type.STACK)));
        createHeatMap(HEAT_MAP_TWO_DRILLABLED_ATTRIBUTES, METRIC_AMOUNT, asList(Pair.of(ATTR_IS_WON, CategoryBucket.Type.VIEW),
            Pair.of(ATTR_REGION, CategoryBucket.Type.STACK)));
        createLineChart(LINE_CHART_TWO_DRILLABLED_ATTRIBUTES, asList(METRIC_AMOUNT), asList(Pair.of(ATTR_IS_WON, CategoryBucket.Type.TREND),
            Pair.of(ATTR_REGION, CategoryBucket.Type.SEGMENT)));
        createBulletChart(BULLET_CHART_TWO_DRILLABLED_ATTRIBUTES, METRIC_AMOUNT, METRIC_AVG_AMOUNT, METRIC_BEST_CASE,
            asList(Pair.of(ATTR_IS_WON, CategoryBucket.Type.VIEW), Pair.of(ATTR_REGION, CategoryBucket.Type.VIEW)));
        createColumnChart(COLUMN_CHART_TWO_ATTRIBUTES_DIFFERENCE_BUCKET, asList(METRIC_AMOUNT),
            asList(Pair.of(ATTR_IS_WON, CategoryBucket.Type.VIEW), Pair.of(ATTR_DEPARTMENT, CategoryBucket.Type.STACK)));
        createHeatMap(HEAT_MAP_TWO_ATTRIBUTES_DIFFERENCE_BUCKET, METRIC_AMOUNT,
            asList(Pair.of(ATTR_IS_WON, CategoryBucket.Type.VIEW), Pair.of(ATTR_DEPARTMENT, CategoryBucket.Type.STACK)));
        createLineChart(LINE_CHART_TWO_ATTRIBUTES_DIFFERENCE_BUCKET, asList(METRIC_AMOUNT),
            asList(Pair.of(ATTR_IS_WON, CategoryBucket.Type.TREND), Pair.of(ATTR_DEPARTMENT, CategoryBucket.Type.SEGMENT)));
        createColumnChart(COLUMN_CHART_TWO_ATTRIBUTES_SAME_BUCKET, asList(METRIC_AMOUNT),
            asList(Pair.of(ATTR_IS_WON, CategoryBucket.Type.VIEW), Pair.of(ATTR_DEPARTMENT, CategoryBucket.Type.VIEW)));
        createBulletChart(BULLET_CHART_TWO_ATTRIBUTES_SAME_BUCKET, METRIC_AMOUNT, METRIC_AVG_AMOUNT, METRIC_BEST_CASE,
            asList(Pair.of(ATTR_IS_WON, CategoryBucket.Type.VIEW), Pair.of(ATTR_DEPARTMENT, CategoryBucket.Type.VIEW)));
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    @DataProvider(name = "tableData")
    public Object[][] getPivotTable() {
        return new Object[][]{
            {
                DASHBOARD_TABLE_ONLY_ROWS,TABLE_ONLY_ROWS,
                asList("false", "true"), asList("Direct Sales", "Inside Sales"), asList()},
            {
                DASHBOARD_TABLE_ONLY_ROWS_MEASURES,TABLE_ONLY_ROWS_MEASURES,
                asList("false", "East Coast", "West Coast", "true", "East Coast", "West Coast"),
                asList("Direct Sales", "East Coast", "West Coast", "Inside Sales", "East Coast", "West Coast"),
                asList("$17,474,336.77", "$23,361.41","$60,840,366.32","$34,411.97", "$10,542,759.65","$10,196.09", "$27,767,993.80","$12,627.56")},
            {
                DASHBOARD_TABLE_ONLY_ROWS_COLUMNS,TABLE_ONLY_ROWS_COLUMNS,
                asList("false","true"), asList("Direct Sales", "Inside Sales"), asList()},
            {
                DASHBOARD_TABLE_HAS_MEASURES_ROWS_COLUMNS,TABLE_HAS_MEASURES_ROWS_COLUMNS,
                asList("false","true"), asList("Direct Sales", "Inside Sales"),
                asList("$17,474,336.77", "$23,361.41","$60,840,366.32","$34,411.97", "$10,542,759.65","$10,196.09", "$27,767,993.80","$12,627.56")}
        };
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillDownOnTableOnlyMeasuresAndColumns() {
        initIndigoDashboardsPage().addDashboard().addInsight(TABLE_ONLY_MEASURES_COLUMNS);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.changeDashboardTitle(DASHBOARD_TABLE_ONLY_MEASURES_COLUMNS).waitForWidgetsLoading().saveEditModeWithWidgets();

        PivotTableReport pivotTableReport = indigoDashboardsPage.waitForWidgetsLoading().getFirstWidget(Insight.class).getPivotTableReport();
        List<String> expectedValues = asList("$17,474,336.77");
        assertThat(pivotTableReport.getBodyContentColumn(0).stream().flatMap(List::stream).collect(toList()), equalTo(expectedValues));

        assertFalse(pivotTableReport.isCellUnderlinedByElement(pivotTableReport.getHeaderElement(ATTRIBUTE_TITLE_TRUE,0)),
            "Cannot drilldown with table that only has measures and columns.");
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "tableData",
        description = "This test case covered the drilling down continue on table that has one drillable attribute")
    public void drillDownOnTableWithOneDrillable(String dashboard, String insight, List<String> comparelist, List<String> compareDrilllist,
        List<String> compareBodyDrilllist) {
        initIndigoDashboardsPage().addDashboard().addInsight(insight);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.changeDashboardTitle(dashboard).waitForWidgetsLoading().saveEditModeWithWidgets();

        PivotTableReport pivotTableReport = indigoDashboardsPage.waitForWidgetsLoading().getFirstWidget(Insight.class).getPivotTableReport();
        assertThat(pivotTableReport.getRowAttributeColumns(), equalTo(comparelist));
        assertThat(pivotTableReport.getValueMeasuresPresent(), equalTo(compareBodyDrilllist));

        DrillModalDialog drillModalDialog = pivotTableReport.drillOnCellRowAttributeElement(ATTRIBUTE_TITLE_TRUE);
        assertEquals(drillModalDialog.getTitleInsight(), insight + " › " + ATTRIBUTE_TITLE_TRUE);

        PivotTableReport targetReport = drillModalDialog.getPivotTableReport();
        List<String> expectedValuesModal = compareDrilllist;
        assertThat(targetReport.getRowAttributeColumns(), equalTo(expectedValuesModal));
    }

    @Test(dependsOnGroups = {"createProject"}, description = "This test case covered the drilling down continue on a table")
    public void drillDownOnTableWithTwoDrillableAttributes() {
        initIndigoDashboardsPage().addDashboard().addInsight(TABLE_TWO_DRILLABLED_ATTRIBUTES);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.changeDashboardTitle(DASHBOARD_TABLE_TWO_DRILLABLED_ATTRIBUTES).waitForWidgetsLoading().saveEditModeWithWidgets();
        PivotTableReport pivotTableReport = indigoDashboardsPage.waitForWidgetsLoading().getFirstWidget(Insight.class).getPivotTableReport();
        List<String> expectedValues = asList("false", "true");
        assertThat(pivotTableReport.getRowAttributeColumns(), equalTo(expectedValues));

        DrillModalDialog drillModalDialog = pivotTableReport.drillOnCellRowAttributeElement(ATTRIBUTE_TITLE_TRUE);
        assertEquals(drillModalDialog.getTitleInsight(), TABLE_TWO_DRILLABLED_ATTRIBUTES + " › " + ATTRIBUTE_TITLE_TRUE);

        PivotTableReport targetReport = drillModalDialog.getPivotTableReport();
        List<String> expectedValuesModal = asList("Direct Sales", "Inside Sales");
        assertThat(targetReport.getRowAttributeColumns(), equalTo(expectedValuesModal));

        DrillModalDialog drillContinueModalDialog = targetReport.drillOnCellRowAttributeElement(ATTRIBUTE_TITLE_DIRECT_SALES);
        assertEquals(drillContinueModalDialog.getTitleInsight(),
            TABLE_TWO_DRILLABLED_ATTRIBUTES + " › " + ATTRIBUTE_TITLE_TRUE + " › " + ATTRIBUTE_TITLE_DIRECT_SALES);
        PivotTableReport targetContinueDrillReport = drillContinueModalDialog.getPivotTableReport();
        List<String> expectedContinueDrillValuesModal = asList("CompuSci", "Educationly", "Explorer", "Grammar Plus","PhoenixSoft", "WonderKid");
        assertThat(targetContinueDrillReport.getRowAttributeColumns(), equalTo(expectedContinueDrillValuesModal));

        DrillModalDialog.getInstance(browser).reset();
        DrillModalDialog drillResetModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillResetModalDialog.getTitleInsight(), TABLE_TWO_DRILLABLED_ATTRIBUTES + " › " + ATTRIBUTE_TITLE_TRUE);
        PivotTableReport targetResetDrillReport = drillResetModalDialog.getPivotTableReport();
        List<String> expectedResetDrillValuesModal = asList("Direct Sales", "Inside Sales");
        assertThat(targetResetDrillReport.getRowAttributeColumns(), equalTo(expectedResetDrillValuesModal));

        DrillModalDialog drillContinueAgainModalDialog = targetResetDrillReport.drillOnCellRowAttributeElement(ATTRIBUTE_TITLE_INSIDE_SALES);
        assertEquals(drillContinueAgainModalDialog.getTitleInsight(),
            TABLE_TWO_DRILLABLED_ATTRIBUTES + " › " + ATTRIBUTE_TITLE_TRUE + " › " + ATTRIBUTE_TITLE_INSIDE_SALES);
        PivotTableReport targetContinueAgainDrillReport = drillContinueModalDialog.getPivotTableReport();
        List<String> expectedContinueAgainDrillValuesModal = asList("CompuSci", "Educationly", "Explorer", "Grammar Plus","PhoenixSoft", "WonderKid");
        assertThat(targetContinueAgainDrillReport.getRowAttributeColumns(), equalTo(expectedContinueAgainDrillValuesModal));
    }

    @Test(dependsOnGroups = {"createProject"}, description = "This test case covered the drilling down on a table that has two drillable attributes")
    public void drillDownOnTableTwoAttributesWithTwoDrillableAttributes() {
        initIndigoDashboardsPage().addDashboard().addInsight(TABLE_TWO_ATTRIBUTES_WITH_TWO_DRILLABLED_ATTRIBUTES);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.changeDashboardTitle(DASHBOARD_TABLE_TWO_ATTRIBUTES_TWO_DRILLABLED_ATTRIBUTES).waitForWidgetsLoading().saveEditModeWithWidgets();
        PivotTableReport pivotTableReport = indigoDashboardsPage.waitForWidgetsLoading().getFirstWidget(Insight.class).getPivotTableReport();
        List<String> expectedValues = asList("false", "Direct Sales", "Inside Sales", "true", "Direct Sales", "Inside Sales");
        assertThat(pivotTableReport.getRowAttributeColumns(), equalTo(expectedValues));

        DrillModalDialog drillBModalDialog = pivotTableReport.drillOnCellRowAttributeElement(ATTRIBUTE_TITLE_DIRECT_SALES);
        assertEquals(drillBModalDialog.getTitleInsight(), TABLE_TWO_ATTRIBUTES_WITH_TWO_DRILLABLED_ATTRIBUTES + " › " + ATTRIBUTE_TITLE_DIRECT_SALES);
        PivotTableReport targetReportB = drillBModalDialog.getPivotTableReport();
        List<String> expectedValuesBModal = asList("CompuSci", "Educationly", "Explorer", "Grammar Plus","PhoenixSoft", "WonderKid");
        assertThat(targetReportB.getRowAttributeColumns(), equalTo(expectedValuesBModal));

        DrillModalDialog.getInstance(browser).close();

        DrillModalDialog drillAModalDialog = pivotTableReport.drillOnCellRowAttributeElement(ATTRIBUTE_TITLE_TRUE);
        assertEquals(drillAModalDialog.getTitleInsight(), TABLE_TWO_ATTRIBUTES_WITH_TWO_DRILLABLED_ATTRIBUTES + " › " + ATTRIBUTE_TITLE_TRUE);
        PivotTableReport targetReportA = drillAModalDialog.getPivotTableReport();
        List<String> expectedValuesAModal = asList("Direct Sales", "Inside Sales");
        assertThat(targetReportA.getRowAttributeColumns(), equalTo(expectedValuesAModal));

        DrillModalDialog drillContinueAModalDialog = targetReportA.drillOnCellRowAttributeElement(ATTRIBUTE_TITLE_DIRECT_SALES);
        assertEquals(drillContinueAModalDialog.getTitleInsight(),
            TABLE_TWO_ATTRIBUTES_WITH_TWO_DRILLABLED_ATTRIBUTES + " › " + ATTRIBUTE_TITLE_TRUE + " › " + ATTRIBUTE_TITLE_DIRECT_SALES);
        PivotTableReport targetContinueDrillAReport = drillContinueAModalDialog.getPivotTableReport();
        List<String> expectedContinueDrillValuesAModal = asList("CompuSci", "Educationly", "Explorer", "Grammar Plus","PhoenixSoft", "WonderKid");
        assertThat(targetContinueDrillAReport.getRowAttributeColumns(), equalTo(expectedContinueDrillValuesAModal));

        DrillModalDialog.getInstance(browser).reset();
        DrillModalDialog resetDrillAModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(resetDrillAModalDialog.getTitleInsight(), TABLE_TWO_ATTRIBUTES_WITH_TWO_DRILLABLED_ATTRIBUTES + " › " + ATTRIBUTE_TITLE_TRUE);
        PivotTableReport targetResetReportA = drillAModalDialog.getPivotTableReport();
        List<String> expectedValuesAResetModal = asList("Direct Sales", "Inside Sales");
        assertThat(targetResetReportA.getRowAttributeColumns(), equalTo(expectedValuesAResetModal));

        DrillModalDialog drillContinueAgainModalDialog = targetContinueDrillAReport.drillOnCellRowAttributeElement(ATTRIBUTE_TITLE_INSIDE_SALES);
        assertEquals(drillContinueAgainModalDialog.getTitleInsight(),
            TABLE_TWO_ATTRIBUTES_WITH_TWO_DRILLABLED_ATTRIBUTES + " › " + ATTRIBUTE_TITLE_TRUE + " › " + ATTRIBUTE_TITLE_INSIDE_SALES);
        PivotTableReport targetContinueAgainDrillReport = drillContinueAgainModalDialog.getPivotTableReport();
        List<String> expectedContinueAgainDrillValuesModal = asList("CompuSci", "Educationly", "Explorer", "Grammar Plus","PhoenixSoft", "WonderKid");
        assertThat(targetContinueAgainDrillReport.getRowAttributeColumns(), equalTo(expectedContinueAgainDrillValuesModal));
    }

    @Test(dependsOnGroups = {"createProject"}, description = "This test case covered the drilling down on a table that has two drillable attributes separate")
    public void drillDownOnTableTwoAttributesWithTwoDrillableAttributesSeparate() {
        AnalysisPage analysisPage = initAnalysePage().changeReportType(ReportType.TABLE)
            .addMetric(METRIC_AMOUNT)
            .addMetric(METRIC_AVG_AMOUNT)
            .addDate()
            .addAttribute(ATTR_IS_WON);
        analysisPage.saveInsight(TABLE_TWO_ATTRIBUTES_WITH_TWO_DRILLABLED_ATTRIBUTES_SEPARATE).waitForReportComputing();
        initIndigoDashboardsPage().addDashboard().addInsight(TABLE_TWO_ATTRIBUTES_WITH_TWO_DRILLABLED_ATTRIBUTES_SEPARATE);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();

        indigoDashboardsPage.changeDashboardTitle(DASHBOARD_TABLE_TWO_ATTRIBUTES_TWO_DRILLABLED_ATTRIBUTES_SEPARATE).waitForWidgetsLoading().saveEditModeWithWidgets();
        PivotTableReport pivotTableReport = indigoDashboardsPage.waitForWidgetsLoading().getFirstWidget(Insight.class).getPivotTableReport();
        List<String> expectedValues = asList("2010", "false", "true", "2011", "false", "true", "2012",
            "false", "true", "2013", "false", "2014", "false", "2016", "false", "2017", "false");
        assertThat(pivotTableReport.getRowAttributeColumns(), equalTo(expectedValues));

        DrillModalDialog drillBModalDialog = pivotTableReport.drillOnCellRowAttributeElement(ATTRIBUTE_TITLE_2011);
        assertEquals(drillBModalDialog.getTitleInsight(), TABLE_TWO_ATTRIBUTES_WITH_TWO_DRILLABLED_ATTRIBUTES_SEPARATE + " › " + ATTRIBUTE_TITLE_2011);
        PivotTableReport targetReportB = drillBModalDialog.getPivotTableReport();
        List<String> expectedValuesBModal = asList("Q1/2011", "false", "true", "Q2/2011", "false", "true", "Q3/2011", "false", "true", "Q4/2011", "false", "true");
        assertThat(targetReportB.getRowAttributeColumns(), equalTo(expectedValuesBModal));

        DrillModalDialog.getInstance(browser).close();

        DrillModalDialog drillAModalDialog = pivotTableReport.drillOnCellRowAttributeElement(ATTRIBUTE_TITLE_TRUE);
        assertEquals(drillAModalDialog.getTitleInsight(), TABLE_TWO_ATTRIBUTES_WITH_TWO_DRILLABLED_ATTRIBUTES_SEPARATE + " › " + ATTRIBUTE_TITLE_TRUE);

        PivotTableReport targetReportA = drillAModalDialog.getPivotTableReport();
        List<String> expectedValuesAModal = asList("Direct Sales", "Inside Sales");
        assertThat(targetReportA.getRowAttributeColumns(), equalTo(expectedValuesAModal));

        DrillModalDialog drillContinueAModalDialog = targetReportA.drillOnCellRowAttributeElement(ATTRIBUTE_TITLE_DIRECT_SALES);
        assertEquals(drillContinueAModalDialog.getTitleInsight(),
            TABLE_TWO_ATTRIBUTES_WITH_TWO_DRILLABLED_ATTRIBUTES_SEPARATE + " › " + ATTRIBUTE_TITLE_TRUE + " › " + ATTRIBUTE_TITLE_DIRECT_SALES);
        PivotTableReport targetContinueDrillAReport = drillContinueAModalDialog.getPivotTableReport();
        List<String> expectedContinueDrillValuesAModal = asList("CompuSci", "Educationly", "Explorer", "Grammar Plus", "PhoenixSoft", "WonderKid");
        assertThat(targetContinueDrillAReport.getRowAttributeColumns(), equalTo(expectedContinueDrillValuesAModal));

        DrillModalDialog.getInstance(browser).reset();
        DrillModalDialog resetDrillAModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(resetDrillAModalDialog.getTitleInsight(), TABLE_TWO_ATTRIBUTES_WITH_TWO_DRILLABLED_ATTRIBUTES_SEPARATE + " › " + ATTRIBUTE_TITLE_TRUE);
        PivotTableReport targetReportAReset = drillAModalDialog.getPivotTableReport();
        List<String> expectedValuesAResetModal = asList("Direct Sales", "Inside Sales");
        assertThat(targetReportAReset.getRowAttributeColumns(), equalTo(expectedValuesAResetModal));
    }

    @DataProvider(name = "chartOnlyMeasures")
    public Object[][] getChartOnlyMeasures() {
        return new Object[][]{
            { DASHBOARD_COLUMN_CHART_ONLY_MEASURES, COLUMN_CHART_ONLY_MEASURES, "column chart"},
            { DASHBOARD_HEAT_MAP_ONLY_MEASURES, HEAT_MAP_ONLY_MEASURES, "heat map"},
            { DASHBOARD_LINE_CHART_ONLY_MEASURES, LINE_CHART_ONLY_MEASURES, "line chart"},
            { DASHBOARD_BULLET_CHART_ONLY_MEASURES, BULLET_CHART_ONLY_MEASURES, "bullet chart"}
        };
    }

    @Test(dependsOnGroups = "createProject", dataProvider = "chartOnlyMeasures",
        description = "This test case covered the drilling down continue on charts that have only measures")
    public void drillDownOnChartOnlyMeasures(String dashboard, String insight, String chartName) {
        initIndigoDashboardsPage().addDashboard().addInsight(insight);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.changeDashboardTitle(dashboard).waitForWidgetsLoading().saveEditModeWithWidgets();
        indigoDashboardsPage.waitForWidgetsLoading().getFirstWidget(Insight.class).getChartReport();

        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, insight).getChartReport();
        chartReport.getTooltipTextOnTrackerByIndex(0,0);
        assertThat(chartReport.getTooltipTextOnTrackerByIndex(0,0), equalTo(asList(asList("Amount", "$116,625,456.54"))));

        chartReport.clickOnElement(Pair.of(0, 0));
        assertFalse(chartReport.isColumnHighlighted(Pair.of(0, 0)),"Should not allow to drill down on " + chartName + " that only has measures.");
    }

    @DataProvider(name = "chartWithStackData")
    public Object[][] getChartWithStackData() {
        return new Object[][]{
            {
                DASHBOARD_COLUMN_CHART_ONLY_VIEWBY_MEASURES, COLUMN_CHART_ONLY_VIEWBY_MEASURES, ATTRIBUTE_TITLE_FALSE,
                asList(asList("Is Won?", "false"), asList("Amount", "$78,314,703.09")), asList(asList("Department", "Direct Sales"), asList("Amount", "$53,901,464.88"))},
            {
                DASHBOARD_LINE_CHART_ONLY_TRENDBY_MEASURES, LINE_CHART_ONLY_TRENDBY_MEASURES, ATTRIBUTE_TITLE_FALSE,
                asList(asList("Is Won?", "false"), asList("Amount", "$78,314,703.09")), asList(asList("Department", "Direct Sales"), asList("Amount", "$53,901,464.88"))},
            {
                DASHBOARD_BULLET_CHART_ONLY_VIEWBY_MEASURES, BULLET_CHART_ONLY_VIEWBY_MEASURES, ATTRIBUTE_TITLE_FALSE,
                asList(asList("Is Won?", "false"), asList("Amount", "$78,314,703.09")), asList(asList("Department", "Direct Sales"), asList("Amount", "$53,901,464.88"))},
            {
                DASHBOARD_COLUMN_CHART_ONLY_STACKBY_MEASURES, COLUMN_CHART_ONLY_STACKBY_MEASURES, ATTRIBUTE_TITLE_FALSE,
                asList(asList("false","$78,314,703.09")), asList(asList("Direct Sales", "$53,901,464.88"))},
            {
                DASHBOARD_LINE_CHART_ONLY_SEGMENTBY_MEASURES, LINE_CHART_ONLY_SEGMENTBY_MEASURES, ATTRIBUTE_TITLE_FALSE,
                asList(asList("false","$78,314,703.09")), asList(asList("Direct Sales", "$53,901,464.88"))},
            {
                DASHBOARD_COLUMN_CHART_MEASURES_STACKBY_VIEWBY, COLUMN_CHART_MEASURES_STACKBY_VIEWBY, ATTRIBUTE_TITLE_FALSE,
                asList(asList("Region", "East Coast"), asList("false", "$17,474,336.77")),
                asList(asList("Region", "East Coast"), asList("Direct Sales", "$13,594,424.44"))},
            {
                DASHBOARD_LINE_CHART_MEASURES_TRENDBY_SEGMENTBY, LINE_CHART_MEASURES_TRENDBY_SEGMENTBY, ATTRIBUTE_TITLE_FALSE,
                asList(asList("Region", "East Coast"), asList("false", "$17,474,336.77")),
                asList(asList("Region", "East Coast"), asList("Direct Sales", "$13,594,424.44"))},
            {
                DASHBOARD_BULLET_CHART_MEASURES_VIEWBYS, BULLET_CHART_MEASURES_VIEWBYS, ATTRIBUTE_TITLE_FALSE,
                asList(asList("Region", "East Coast"), asList("Is Won?", "false"), asList("Amount", "$17,474,336.77")),
                asList(asList("Department", "Direct Sales"), asList("Amount", "$13,594,424.44"))},
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "chartWithStackData",
        description = "This test case covered the drilling down continue on charts")
    public void drillDownOnChart(String dashboard, String insight, String attributeDrill, List<List<String>> tooltipCompare, List<String> resultCompare) {
        initIndigoDashboardsPage().addDashboard().addInsight(insight);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.changeDashboardTitle(dashboard).waitForWidgetsLoading().saveEditModeWithWidgets();
        indigoDashboardsPage.waitForWidgetsLoading().getFirstWidget(Insight.class).getChartReport();
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, insight).getChartReport();
        chartReport.getTooltipTextOnTrackerByIndex(0,0);
        assertThat(chartReport.getTooltipTextOnTrackerByIndex(0,0), equalTo(tooltipCompare));
        chartReport.getTooltipInteractionOnTrackerByIndex(0,0);
        assertEquals(chartReport.getTooltipInteractionOnTrackerByIndex(0,0), "Click chart to drill");

        chartReport.clickOnElement(Pair.of(0, 0));
        indigoDashboardsPage.waitForDrillModalDialogLoading();
        DrillModalDialog drillModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillModalDialog.getTitleInsight(), insight + " › " + attributeDrill);
        ChartReport targetReport = drillModalDialog.getChartReport();
        assertThat(targetReport.getCurrentHighChartsTooltip(Pair.of(0, 0)), equalTo(resultCompare));
    }

    @DataProvider(name = "heatMapChartWithStackData")
    public Object[][] getHeatMapChartWithStackData() {
        return new Object[][]{
            {
                DASHBOARD_HEAT_MAP_ONLY_ROWS_MEASURES, HEAT_MAP_ONLY_ROWS_MEASURES, ATTRIBUTE_TITLE_FALSE, "$78,314,703.09", "$53,901,464.88",
                asList(asList("Is Won?", "false"), asList("Amount", "$78,314,703.09")), asList(asList("Department", "Direct Sales"), asList("Amount", "$53,901,464.88"))},
            {
                DASHBOARD_HEAT_MAP_ONLY_COLUMN_MEASURES, HEAT_MAP_ONLY_COLUMN_MEASURES, ATTRIBUTE_TITLE_FALSE, "$78,314,703.09", "$53,901,464.88",
                asList(asList("Is Won?", "false"), asList("Amount", "$78,314,703.09")), asList(asList("Department", "Direct Sales"), asList("Amount", "$53,901,464.88"))},
            {
                DASHBOARD_HEAT_MAP_MEASURES_ROW_COLUMN, HEAT_MAP_MEASURES_ROW_COLUMN, ATTRIBUTE_TITLE_FALSE, "$17,474,336.77", "$13,594,424.44",
                asList(asList("Region", "East Coast"), asList("Is Won?", "false"), asList("Amount", "$17,474,336.77")),
                asList(asList("Region", "East Coast"), asList("Department", "Direct Sales"), asList("Amount", "$13,594,424.44"))},
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "heatMapChartWithStackData", description = "This test case covered the drilling down continue on heat map charts")
    public void drillDownOnHeatMapChart(String dashboard, String insight, String attributeDrill, String titleDrill, String titleDrillResult, List<List<String>> tooltipCompare, List<String> resultCompare) {
        initIndigoDashboardsPage().addDashboard().addInsight(insight);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.changeDashboardTitle(dashboard).waitForWidgetsLoading().saveEditModeWithWidgets();
        indigoDashboardsPage.waitForWidgetsLoading().getFirstWidget(Insight.class).getChartReport();
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, insight).getChartReport();
        assertThat(chartReport.getTooltipTextOnTrackerByTitle(titleDrill), equalTo(tooltipCompare));
        assertEquals(chartReport.getTooltipInteractionOnTrackerByTitle(titleDrill), "Click chart to drill");

        chartReport.clickOnElementByTitle(titleDrill);
        indigoDashboardsPage.waitForDrillModalDialogLoading();
        DrillModalDialog drillModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillModalDialog.getTitleInsight(), insight + " › " + attributeDrill);
        ChartReport targetReport = drillModalDialog.getChartReport();
        assertThat(targetReport.getCurrentHighChartsTooltipByTitle(titleDrillResult), equalTo(resultCompare));
    }

    @DataProvider(name = "chartWithTwoDrillableAttributes")
    public Object[][] getChartWithTwoDrillableAttributes() {
        return new Object[][]{
            {
                DASHBOARD_COLUMN_CHART_TWO_DRILLABLED_ATTRIBUTES, COLUMN_CHART_TWO_DRILLABLED_ATTRIBUTES, ATTRIBUTE_TITLE_FALSE, ATTRIBUTE_TITLE_DIRECT_SALES,
                asList(asList("Is Won?", "false"), asList("East Coast", "$17,474,336.77")),
                asList(asList("Department", "Direct Sales"), asList("East Coast", "$13,594,424.44")),
                asList(asList("Product", "CompuSci"), asList("East Coast", "$2,462,322.50"))},
            {
                DASHBOARD_LINE_CHART_TWO_DRILLABLED_ATTRIBUTES, LINE_CHART_TWO_DRILLABLED_ATTRIBUTES, ATTRIBUTE_TITLE_FALSE, ATTRIBUTE_TITLE_DIRECT_SALES,
                asList(asList("Is Won?", "false"), asList("East Coast", "$17,474,336.77")),
                asList(asList("Department", "Direct Sales"), asList("East Coast", "$13,594,424.44")),
                asList(asList("Product", "CompuSci"), asList("East Coast", "$2,462,322.50"))},
            {
                DASHBOARD_BULLET_CHART_TWO_DRILLABLED_ATTRIBUTES, BULLET_CHART_TWO_DRILLABLED_ATTRIBUTES, ATTRIBUTE_TITLE_FALSE, ATTRIBUTE_TITLE_DIRECT_SALES,
                asList(asList("Is Won?", "false"), asList("Region", "East Coast"), asList("Amount", "$17,474,336.77")),
                asList(asList("Department", "Direct Sales"), asList("Region", "East Coast"), asList("Amount", "$13,594,424.44")),
                asList(asList("Product", "CompuSci"), asList("Region", "East Coast"), asList("Amount", "$2,462,322.50"))},
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "chartWithTwoDrillableAttributes",
        description = "This test case covered the drilling down continue on charts that have two drillable attributes")
    public void drillDownOnChartWithTwoDrillableAttributes(String dashboard, String insight, String attributeDrill, String attributeContinueDrill,
        List<List<String>> tooltipCompare, List<String> resultCompare, List<String> resultDrillContinueCompare) {
        initIndigoDashboardsPage().addDashboard().addInsight(insight);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.changeDashboardTitle(dashboard).waitForWidgetsLoading().saveEditModeWithWidgets();
        indigoDashboardsPage.waitForWidgetsLoading().getFirstWidget(Insight.class).getChartReport();
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, insight).getChartReport();
        chartReport.getTooltipTextOnTrackerByIndex(0,0);
        assertThat(chartReport.getTooltipTextOnTrackerByIndex(0,0), equalTo(tooltipCompare));
        chartReport.getTooltipInteractionOnTrackerByIndex(0,0);
        assertEquals(chartReport.getTooltipInteractionOnTrackerByIndex(0,0), "Click chart to drill");

        chartReport.clickOnElement(Pair.of(0, 0));
        indigoDashboardsPage.waitForDrillModalDialogLoading();
        DrillModalDialog drillModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillModalDialog.getTitleInsight(), insight + " › " + attributeDrill);
        ChartReport targetReport = drillModalDialog.getChartReport();
        assertThat(targetReport.getCurrentHighChartsTooltip(Pair.of(0, 0)), equalTo(resultCompare));

        targetReport.clickOnElement(Pair.of(0, 0));
        indigoDashboardsPage.waitForDrillModalDialogLoading();
        DrillModalDialog drillContinueModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillContinueModalDialog.getTitleInsight(), insight + " › " + attributeDrill + " › " + attributeContinueDrill);
        ChartReport targetContinueReport = drillModalDialog.getChartReport();
        assertThat(targetContinueReport.getCurrentHighChartsTooltip(Pair.of(0, 0)), equalTo(resultDrillContinueCompare));

        DrillModalDialog.getInstance(browser).reset();
        assertEquals(drillModalDialog.getTitleInsight(), insight + " › " + attributeDrill);
        assertThat(targetReport.getCurrentHighChartsTooltip(Pair.of(0, 0)), equalTo(resultCompare));
    }

    @Test(dependsOnGroups = {"createProject"}, description = "This test case covered the drilling down continue on heat map that have two drillable attributes")
    public void drillDownOnHeatMapChartWithTwoDrillableAttributes() {
        initIndigoDashboardsPage().addDashboard().addInsight(HEAT_MAP_TWO_DRILLABLED_ATTRIBUTES);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.changeDashboardTitle(DASHBOARD_HEAT_MAP_TWO_DRILLABLED_ATTRIBUTES).waitForWidgetsLoading().saveEditModeWithWidgets();
        indigoDashboardsPage.waitForWidgetsLoading().getFirstWidget(Insight.class).getChartReport();
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, HEAT_MAP_TWO_DRILLABLED_ATTRIBUTES).getChartReport();
        List<List<String>> tooltipCompare = asList(asList("Is Won?", "false"), asList("Region", "East Coast"), asList("Amount", "$17,474,336.77"));
        assertThat(chartReport.getTooltipTextOnTrackerByTitle("$17,474,336.77"), equalTo(tooltipCompare));
        assertEquals(chartReport.getTooltipInteractionOnTrackerByTitle("$17,474,336.77"), "Click chart to drill");

        chartReport.clickOnElementByTitle("$17,474,336.77");
        indigoDashboardsPage.waitForDrillModalDialogLoading();
        DrillModalDialog drillModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillModalDialog.getTitleInsight(), HEAT_MAP_TWO_DRILLABLED_ATTRIBUTES + " › " + ATTRIBUTE_TITLE_FALSE);
        ChartReport targetReport = drillModalDialog.getChartReport();
        List<List<String>> resultCompare = asList(asList("Department", "Direct Sales"), asList("Region", "East Coast"), asList("Amount", "$13,594,424.44"));
        assertThat(targetReport.getCurrentHighChartsTooltipByTitle("$13,594,424.44"), equalTo(resultCompare));

        targetReport.clickOnElementByTitle("$13,594,424.44");
        indigoDashboardsPage.waitForDrillModalDialogLoading();
        DrillModalDialog drillContinueModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillContinueModalDialog.getTitleInsight(), HEAT_MAP_TWO_DRILLABLED_ATTRIBUTES + " › " + ATTRIBUTE_TITLE_FALSE + " › " + ATTRIBUTE_TITLE_DIRECT_SALES);
        ChartReport targetContinueReport = drillModalDialog.getChartReport();
        List<List<String>> resultDrillContinueCompare = asList(asList("Product", "CompuSci"), asList("Region", "East Coast"), asList("Amount", "$2,462,322.50"));
        assertThat(targetContinueReport.getCurrentHighChartsTooltipByTitle("$2,462,322.50"), equalTo(resultDrillContinueCompare));

        DrillModalDialog.getInstance(browser).reset();
        assertEquals(drillModalDialog.getTitleInsight(), HEAT_MAP_TWO_DRILLABLED_ATTRIBUTES + " › " + ATTRIBUTE_TITLE_FALSE);
        assertThat(targetReport.getCurrentHighChartsTooltipByTitle("$13,594,424.44"), equalTo(resultCompare));
    }

    @DataProvider(name = "chartHasTwoAttributesDifferenceBucket")
    public Object[][] getChartHasTwoAttributesDifferenceBucket() {
        return new Object[][]{
            {
                DASHBOARD_COLUMN_CHART_TWO_ATTRIBUTES_DIFFERENCE_BUCKET, COLUMN_CHART_TWO_ATTRIBUTES_DIFFERENCE_BUCKET, ATTRIBUTE_TITLE_FALSE,
                ATTRIBUTE_TITLE_DIRECT_SALES, asList(asList("Is Won?", "false"), asList("Direct Sales", "$53,901,464.88")),
                asList(asList("Department", "Direct Sales"), asList("Direct Sales", "$53,901,464.88")),
                asList(asList("Department", "Direct Sales"), asList("CompuSci", "$9,470,887.67")),
                asList(asList("Is Won?", "false"), asList("CompuSci", "$9,470,887.67"))},
            {
                DASHBOARD_LINE_CHART_TWO_ATTRIBUTES_DIFFERENCE_BUCKET, LINE_CHART_TWO_ATTRIBUTES_DIFFERENCE_BUCKET, ATTRIBUTE_TITLE_FALSE, ATTRIBUTE_TITLE_DIRECT_SALES,
                asList(asList("Is Won?", "false"), asList("Direct Sales", "$53,901,464.88")),
                asList(asList("Department", "Direct Sales"), asList("Direct Sales", "$53,901,464.88")),
                asList(asList("Department", "Direct Sales"), asList("CompuSci", "$9,470,887.67")),
                asList(asList("Is Won?", "false"), asList("CompuSci", "$9,470,887.67"))},
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "chartHasTwoAttributesDifferenceBucket",
        description = "This test case covered the drilling down on charts that have two drillable attributes on the difference bucket")
    public void drillDownOnChartTwoDrillableAttributesDifferenceBucket(String dashboard, String insight, String attributeDrill, String attributeContinueDrill,
        List<List<String>> tooltipCompare, List<String> resultCompare, List<String> resultDrillContinueCompare, List<String> resultDrillBCompare) {
        initIndigoDashboardsPage().addDashboard().addInsight(insight);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.changeDashboardTitle(dashboard).waitForWidgetsLoading().saveEditModeWithWidgets();
        indigoDashboardsPage.waitForWidgetsLoading().getFirstWidget(Insight.class).getChartReport();
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, insight).getChartReport();
        chartReport.getTooltipTextOnTrackerByIndex(0,0);
        assertThat(chartReport.getTooltipTextOnTrackerByIndex(0,0), equalTo(tooltipCompare));
        chartReport.getTooltipInteractionOnTrackerByIndex(0,0);
        assertEquals(chartReport.getTooltipInteractionOnTrackerByIndex(0,0), "Click chart to drill");

        chartReport.openDrillingPicker(Pair.of(0, 0)).drillDown(attributeDrill);
        indigoDashboardsPage.waitForDrillModalDialogLoading();
        DrillModalDialog drillModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillModalDialog.getTitleInsight(), insight + " › " + attributeDrill);
        ChartReport targetReport = drillModalDialog.getChartReport();
        assertThat(targetReport.getCurrentHighChartsTooltip(Pair.of(0, 0)), equalTo(resultCompare));

        targetReport.openDrillingPicker(Pair.of(0, 0)).drillDown(attributeContinueDrill);
        indigoDashboardsPage.waitForDrillModalDialogLoading();
        DrillModalDialog drillContinueModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillContinueModalDialog.getTitleInsight(), insight + " › " + attributeDrill + " › " + attributeContinueDrill);
        ChartReport targetContinueReport = drillModalDialog.getChartReport();
        assertThat(targetContinueReport.getCurrentHighChartsTooltip(Pair.of(0, 0)), equalTo(resultDrillContinueCompare));

        DrillModalDialog.getInstance(browser).reset();
        DrillModalDialog resetDrillModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(resetDrillModalDialog.getTitleInsight(), insight + " › " + attributeDrill);
        ChartReport targetResetReport = resetDrillModalDialog.getChartReport();
        assertThat(targetResetReport.getCurrentHighChartsTooltip(Pair.of(0, 0)), equalTo(resultCompare));

        DrillModalDialog.getInstance(browser).close();

        chartReport.openDrillingPicker(Pair.of(0, 0)).drillDown(attributeContinueDrill);
        indigoDashboardsPage.waitForDrillModalDialogLoading();
        DrillModalDialog drillBModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillBModalDialog.getTitleInsight(), insight + " › " + attributeContinueDrill);
        ChartReport targetReportB = drillBModalDialog.getChartReport();
        assertThat(targetReportB.getCurrentHighChartsTooltip(Pair.of(0, 0)), equalTo(resultDrillBCompare));
    }

    @Test(dependsOnGroups = {"createProject"}, description = "This test case covered the drilling down on heat map that have two drillable attributes on the difference bucket")
    public void drillDownOnHeatMapChartTwoDrillableAttributesDifferenceBucket() {
        initIndigoDashboardsPage().addDashboard().addInsight(HEAT_MAP_TWO_ATTRIBUTES_DIFFERENCE_BUCKET);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.changeDashboardTitle(DASHBOARD_HEAT_MAP_TWO_ATTRIBUTES_DIFFERENCE_BUCKET).waitForWidgetsLoading().saveEditModeWithWidgets();
        indigoDashboardsPage.waitForWidgetsLoading().getFirstWidget(Insight.class).getChartReport();
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, HEAT_MAP_TWO_ATTRIBUTES_DIFFERENCE_BUCKET).getChartReport();
        List<List<String>> tooltipCompare = asList(asList("Is Won?", "false"), asList("Department", "Direct Sales"), asList("Amount", "$53,901,464.88"));
        assertThat(chartReport.getTooltipTextOnTrackerByTitle("$53,901,464.88"), equalTo(tooltipCompare));

        chartReport.openDrillingPickerByTitle("$53,901,464.88").drillDown(ATTRIBUTE_TITLE_FALSE);
        indigoDashboardsPage.waitForDrillModalDialogLoading();
        DrillModalDialog drillModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillModalDialog.getTitleInsight(), HEAT_MAP_TWO_ATTRIBUTES_DIFFERENCE_BUCKET + " › " + ATTRIBUTE_TITLE_FALSE);
        ChartReport targetReport = drillModalDialog.getChartReport();
        List<List<String>> resultCompare = asList(asList("Department", "Direct Sales"), asList("Department", "Direct Sales"), asList("Amount", "$53,901,464.88"));
        assertThat(targetReport.getCurrentHighChartsTooltipByTitle("$53,901,464.88"), equalTo(resultCompare));

        targetReport.openDrillingPickerByTitle("$53,901,464.88").drillDown(ATTRIBUTE_TITLE_DIRECT_SALES);
        indigoDashboardsPage.waitForDrillModalDialogLoading();
        DrillModalDialog drillContinueModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillContinueModalDialog.getTitleInsight(), HEAT_MAP_TWO_ATTRIBUTES_DIFFERENCE_BUCKET + " › " + ATTRIBUTE_TITLE_FALSE + " › " + ATTRIBUTE_TITLE_DIRECT_SALES);
        ChartReport targetContinueReport = drillModalDialog.getChartReport();
        List<List<String>> resultDrillContinueCompare = asList(asList("Product", "CompuSci"), asList("Department", "Direct Sales"), asList("Amount", "$9,470,887.67"));
        assertThat(targetContinueReport.getCurrentHighChartsTooltipByTitle("$9,470,887.67"), equalTo(resultDrillContinueCompare));

        DrillModalDialog.getInstance(browser).reset();
        DrillModalDialog resetDrillModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(resetDrillModalDialog.getTitleInsight(), HEAT_MAP_TWO_ATTRIBUTES_DIFFERENCE_BUCKET + " › " + ATTRIBUTE_TITLE_FALSE);
        ChartReport targetResetReport = resetDrillModalDialog.getChartReport();
        assertThat(targetResetReport.getCurrentHighChartsTooltipByTitle("$53,901,464.88"), equalTo(resultCompare));

        DrillModalDialog.getInstance(browser).close();

        chartReport.openDrillingPickerByTitle("$53,901,464.88").drillDown(ATTRIBUTE_TITLE_DIRECT_SALES);
        indigoDashboardsPage.waitForDrillModalDialogLoading();
        DrillModalDialog drillBModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillBModalDialog.getTitleInsight(), HEAT_MAP_TWO_ATTRIBUTES_DIFFERENCE_BUCKET + " › " + ATTRIBUTE_TITLE_DIRECT_SALES);
        ChartReport targetReportB = drillBModalDialog.getChartReport();
        List<List<String>> resultDrillBCompare = asList(asList("Is Won?", "false"), asList("Product", "CompuSci"), asList("Amount", "$9,470,887.67"));
        assertThat(targetReportB.getCurrentHighChartsTooltipByTitle("$9,470,887.67"), equalTo(resultDrillBCompare));
    }

    @DataProvider(name = "chartHasTwoAttributesSameBucket")
    public Object[][] getChartHasTwoAttributesSameBucket() {
        return new Object[][]{
            {
                DASHBOARD_COLUMN_CHART_TWO_ATTRIBUTES_SAME_BUCKET, COLUMN_CHART_TWO_ATTRIBUTES_SAME_BUCKET, ATTRIBUTE_TITLE_FALSE, ATTRIBUTE_TITLE_DIRECT_SALES,
                asList(asList("Is Won?", "false"), asList("Department", "Direct Sales"), asList("Amount", "$53,901,464.88")),
                asList(asList("Department", "Direct Sales"), asList("Amount", "$53,901,464.88")),
                asList(asList("Product", "CompuSci"), asList("Amount", "$9,470,887.67"))},
            {
                DASHBOARD_BULLET_CHART_TWO_ATTRIBUTES_SAME_BUCKET, BULLET_CHART_TWO_ATTRIBUTES_SAME_BUCKET, ATTRIBUTE_TITLE_FALSE, ATTRIBUTE_TITLE_DIRECT_SALES,
                asList(asList("Is Won?", "false"), asList("Department", "Direct Sales"), asList("Amount", "$53,901,464.88")),
                asList(asList("Department", "Direct Sales"), asList("Amount", "$53,901,464.88")),
                asList(asList("Product", "CompuSci"), asList("Amount", "$9,470,887.67"))},
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "chartHasTwoAttributesSameBucket",
        description = "This test case covered the drilling down on charts that have two drillable attributes on the same bucket")
    public void drillDownOnChartTwoAttributesSameBucketWithTwoDrillableAttributes(String dashboard, String insight, String attributeDrill, String attributeContinueDrill,
        List<List<String>> tooltipCompare, List<String> resultCompare, List<String> resultDrillContinueCompare) {
        initIndigoDashboardsPage().addDashboard().addInsight(insight);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.changeDashboardTitle(dashboard).waitForWidgetsLoading().saveEditModeWithWidgets();
        indigoDashboardsPage.waitForWidgetsLoading().getFirstWidget(Insight.class).getChartReport();
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, insight).getChartReport();
        chartReport.getTooltipTextOnTrackerByIndex(0,0);
        assertThat(chartReport.getTooltipTextOnTrackerByIndex(0,0), equalTo(tooltipCompare));
        chartReport.getTooltipInteractionOnTrackerByIndex(0,0);
        assertEquals(chartReport.getTooltipInteractionOnTrackerByIndex(0,0), "Click chart to drill");

        chartReport.openDrillingPicker(Pair.of(0, 0)).drillDown(attributeDrill);
        indigoDashboardsPage.waitForDrillModalDialogLoading();
        DrillModalDialog drillModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillModalDialog.getTitleInsight(), insight + " › " + attributeDrill);
        ChartReport targetReport = drillModalDialog.getChartReport();
        assertThat(targetReport.getCurrentHighChartsTooltip(Pair.of(0,0)), equalTo(resultCompare));

        targetReport.clickOnElement(Pair.of(0, 0));
        indigoDashboardsPage.waitForDrillModalDialogLoading();
        DrillModalDialog drillContinueModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillContinueModalDialog.getTitleInsight(), insight + " › " + attributeDrill + " › " + attributeContinueDrill);
        ChartReport targetContinueReport = drillModalDialog.getChartReport();
        assertThat(targetContinueReport.getCurrentHighChartsTooltip(Pair.of(0,0)), equalTo(resultDrillContinueCompare));

        DrillModalDialog.getInstance(browser).reset();
        DrillModalDialog resetDrillModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(resetDrillModalDialog.getTitleInsight(), insight + " › " + attributeDrill);
        ChartReport targetResetReport = resetDrillModalDialog.getChartReport();
        assertThat(targetResetReport.getCurrentHighChartsTooltip(Pair.of(0,0)), equalTo(resultCompare));

        DrillModalDialog.getInstance(browser).close();

        chartReport.openDrillingPicker(Pair.of(0, 0)).drillDown(attributeContinueDrill);
        indigoDashboardsPage.waitForDrillModalDialogLoading();
        DrillModalDialog drillBModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillBModalDialog.getTitleInsight(), insight + " › " + attributeContinueDrill);
        ChartReport targetReportB = drillBModalDialog.getChartReport();
        assertThat(targetReportB.getCurrentHighChartsTooltip(Pair.of(0,0)), equalTo(resultDrillContinueCompare));
    }

    @DataProvider(name = "chartHasTwoAttributesSeparate")
    public Object[][] getChartHasTwoAttributesSeparate() {
        return new Object[][]{
            {
                ReportType.COLUMN_CHART, DASHBOARD_COLUMN_CHART_TWO_DRILLABLED_ATTRIBUTES_SEPARATE, COLUMN_CHART_TWO_DRILLABLED_ATTRIBUTES_SEPARATE,
                ATTRIBUTE_TITLE_FALSE, ATTRIBUTE_TITLE_2011, asList(asList("Year (Closed)", "2011"), asList("Is Won?", "false"), asList("Amount", "$33,160,441.09")),
                asList(asList("Department", "Direct Sales"), asList("Amount", "$26,101,838.99")),
                asList(asList("Quarter/Year (Closed)", "Q1/2011"), asList("Is Won?", "false"), asList("Amount", "$3,839,753.28"))},
            {
                ReportType.BULLET_CHART, DASHBOARD_BULLET_CHART_TWO_DRILLABLED_ATTRIBUTES_SEPARATE, BULLET_CHART_TWO_DRILLABLED_ATTRIBUTES_SEPARATE,
                ATTRIBUTE_TITLE_FALSE, ATTRIBUTE_TITLE_2011, asList(asList("Year (Closed)", "2011"), asList("Is Won?", "false"), asList("Amount", "$33,160,441.09")),
                asList(asList("Department", "Direct Sales"), asList("Amount", "$26,101,838.99")),
                asList(asList("Quarter/Year (Closed)", "Q1/2011"), asList("Is Won?", "false"), asList("Amount", "$3,839,753.28"))},
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "chartHasTwoAttributesSeparate",
        description = "This test case covered the drilling down on charts that have two drillable attributes separaty")
    public void drillDownOnChartTwoDrillableAttributesSeparate(ReportType reportType, String dashboard, String insight, String attributeDrill, String dateDrill,
        List<String> tooltipCompare, List<List<String>> resultACompare, List<List<String>> resultBCompare) {
        AnalysisPage analysisPage = initAnalysePage().changeReportType(reportType).addMetric(METRIC_AMOUNT).addDate().addAttribute(ATTR_IS_WON);
        analysisPage.saveInsight(insight).waitForReportComputing();
        initIndigoDashboardsPage().addDashboard().addInsight(insight);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectStaticPeriod("01/01/2011", "12/31/2012").apply();

        indigoDashboardsPage.changeDashboardTitle(dashboard).waitForWidgetsLoading().saveEditModeWithWidgets();
        indigoDashboardsPage.waitForWidgetsLoading().getFirstWidget(Insight.class).getChartReport();
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, insight).getChartReport();
        chartReport.getTooltipTextOnTrackerByIndex(0,0);
        assertThat(chartReport.getTooltipTextOnTrackerByIndex(0,0), equalTo(tooltipCompare));
        chartReport.getTooltipInteractionOnTrackerByIndex(0,0);
        assertEquals(chartReport.getTooltipInteractionOnTrackerByIndex(0,0), "Click chart to drill");

        chartReport.openDrillingPicker(Pair.of(0, 0)).drillDown(attributeDrill);
        indigoDashboardsPage.waitForDrillModalDialogLoading();
        DrillModalDialog drillModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillModalDialog.getTitleInsight(), insight + " › " + attributeDrill);
        ChartReport targetAReport = drillModalDialog.getChartReport();
        assertThat(targetAReport.getCurrentHighChartsTooltip(Pair.of(0,0)), equalTo(resultACompare));

        DrillModalDialog.getInstance(browser).close();

        chartReport.openDrillingPicker(Pair.of(0, 0)).drillDown(dateDrill);
        indigoDashboardsPage.waitForDrillModalDialogLoading();
        DrillModalDialog drillBModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillBModalDialog.getTitleInsight(), insight + " › " + dateDrill);
        ChartReport targetBReport = drillBModalDialog.getChartReport();
        assertThat(targetBReport.getCurrentHighChartsTooltip(Pair.of(0,0)), equalTo(resultBCompare));
    }

    private String createInsight(String insightTitle, List<String> metricsTitle, List<Pair<String, CategoryBucket.Type>> attributeConfigurations) {
        return indigoRestRequest.createInsight(
            new InsightMDConfiguration(insightTitle, ReportType.TABLE)
                .setMeasureBucket(metricsTitle.stream()
                    .map(metric -> MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric)))
                    .collect(toList()))
                .setCategoryBucket(attributeConfigurations.stream()
                    .map(attribute -> CategoryBucket.createCategoryBucket(
                        getAttributeByTitle(attribute.getKey()), attribute.getValue()))
                    .collect(toList())));
    }

    private String createColumnChart(String insightTitle, List<String> metricsTitle, List<Pair<String, CategoryBucket.Type>> attributeConfigurations) {
        return indigoRestRequest.createInsight(
            new InsightMDConfiguration(insightTitle, ReportType.COLUMN_CHART)
                .setMeasureBucket(metricsTitle.stream()
                    .map(metric -> MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric)))
                    .collect(toList()))
                .setCategoryBucket(attributeConfigurations.stream()
                    .map(attribute -> CategoryBucket.createCategoryBucket(
                        getAttributeByTitle(attribute.getKey()), attribute.getValue()))
                    .collect(toList())));
    }

    private String createHeatMap(String insightTitle, String metricsTitle, List<Pair<String, CategoryBucket.Type>> attributeConfigurations) {
        return indigoRestRequest.createInsight(
            new InsightMDConfiguration(insightTitle, ReportType.HEAT_MAP)
                .setMeasureBucket(asList(
                    MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metricsTitle))))
                .setCategoryBucket(attributeConfigurations.stream()
                    .map(attribute -> CategoryBucket.createCategoryBucket(
                        getAttributeByTitle(attribute.getKey()), attribute.getValue()))
                    .collect(toList())));
    }

    private String createLineChart(String insightTitle, List<String> metricsTitle, List<Pair<String, CategoryBucket.Type>> attributeConfigurations) {
        return indigoRestRequest.createInsight(
            new InsightMDConfiguration(insightTitle, ReportType.LINE_CHART)
                .setMeasureBucket(metricsTitle.stream()
                    .map(metric -> MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric)))
                    .collect(toList()))
                .setCategoryBucket(attributeConfigurations.stream()
                    .map(attribute -> CategoryBucket.createCategoryBucket(
                        getAttributeByTitle(attribute.getKey()), attribute.getValue()))
                    .collect(toList())));
    }

    private String createBulletChart(String insightTitle, String metricsTitle, String metricsSecondaryTitle, String metricTertiaryTitle, List<Pair<String,
        CategoryBucket.Type>> attributeConfigurations) {
        return indigoRestRequest.createInsight(
            new InsightMDConfiguration(insightTitle, ReportType.BULLET_CHART)
                .setMeasureBucket(asList(
                    MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metricsTitle)),
                    MeasureBucket.createMeasureBucket(getMetricByTitle(metricsSecondaryTitle), MeasureBucket.Type.SECONDARY_MEASURES),
                    MeasureBucket.createMeasureBucket(getMetricByTitle(metricTertiaryTitle), MeasureBucket.Type.TERTIARY_MEASURES)))
                .setCategoryBucket(attributeConfigurations.stream()
                .map(attribute -> CategoryBucket.createCategoryBucket(
                        getAttributeByTitle(attribute.getKey()), attribute.getValue()))
                .collect(toList())));
    }
}
