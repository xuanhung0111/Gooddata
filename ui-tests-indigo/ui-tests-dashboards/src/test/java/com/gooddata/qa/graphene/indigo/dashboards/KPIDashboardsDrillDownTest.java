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
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AVG_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_WON;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_REGION;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_ACTIVE;

import static java.util.stream.Collectors.toList;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class KPIDashboardsDrillDownTest extends AbstractDashboardTest {
    private final String TABLE_ONLY_MEASURES_COLUMNS = "Table only measures and columns";
    private final String DASHBOARD_TABLE_ONLY_MEASURES_COLUMNS = "Dashboard with table only measures and columns";
    private final String TABLE_ONLY_ROWS = "Table only rows";
    private final String DASHBOARD_TABLE_ONLY_ROWS = "Dashboard table only rows";
    private final String TABLE_ONLY_ROWS_MEASURES = "Table only measures and rows";
    private final String DASHBOARD_TABLE_ONLY_ROWS_MEASURES = "Dashboard table only rows and measures";
    private final String TABLE_ONLY_ROWS_COLUMNS = "Table only columns and rows";
    private final String DASHBOARD_TABLE_ONLY_ROWS_COLUMNS = "Dashboard table only rows and columns";
    private final String TABLE_HAS_MEASURES_ROWS_COLUMNS = "Table has measure rows and columns";
    private final String DASHBOARD_TABLE_HAS_MEASURES_ROWS_COLUMNS = "Dashboard table only measures rows and columns";
    private final String TABLE_TWO_DRILLABLED_ATTRIBUTES = "Table with one attribute and two drillable attributes";
    private final String DASHBOARD_TABLE_TWO_DRILLABLED_ATTRIBUTES = "Dashboard with table one attribute that has two drillable attributes";
    private final String TABLE_TWO_ATTRIBUTES_WITH_TWO_DRILLABLED_ATTRIBUTES = "Table with two attributes and two drillable attributes";
    private final String DASHBOARD_TABLE_TWO_ATTRIBUTES_TWO_DRILLABLED_ATTRIBUTES = "Dashboard with table two attributes that has two drillable attributes";
    private final String TABLE_TWO_ATTRIBUTES_WITH_TWO_DRILLABLED_ATTRIBUTES_SEPARATE = "Table with two attributes and two drillable separate attributes";
    private final String DASHBOARD_TABLE_TWO_ATTRIBUTES_TWO_DRILLABLED_ATTRIBUTES_SEPARATE = "Dashboard with table two attributes that has two drillable separate attributes";
    private final String COLUMN_CHART_ONLY_MEASURES = "Column chart only measures";
    private final String DASHBOARD_COLUMN_CHART_ONLY_MEASURES = "Dashboard drill down with column chart only measures";
    private final String COLUMN_CHART_ONLY_VIEWBY_MEASURES = "Column chart only measures and viewby";
    private final String DASHBOARD_COLUMN_CHART_ONLY_VIEWBY_MEASURES = "Dashboard column chart only measures and viewby";
    private final String COLUMN_CHART_ONLY_STACKBY_MEASURES = "Column chart only measures and stackby";
    private final String DASHBOARD_COLUMN_CHART_ONLY_STACKBY_MEASURES = "Dashboard column chart only measures and stackby";
    private final String COLUMN_CHART_MEASURES_STACKBY_VIEWBY = "Column chart only measures, stackby and viewby";
    private final String DASHBOARD_COLUMN_CHART_ONLY_MEASURES_STACKBY_VIEWBY = "Dashboard column chart only measures stackby, viewby";
    private final String COLUMN_CHART_TWO_DRILLABLED_ATTRIBUTES = "Column chart with two drillable attribute";
    private final String DASHBOARD_COLUMN_CHART_TWO_DRILLABLED_ATTRIBUTES = "Dashboard column chart with two drillable attribute";
    private final String COLUMN_CHART_TWO_ATTRIBUTES_WITH_TWO_DRILLABLED_ATTRIBUTES = "Column chart with two attributes and two drillable attributes";
    private final String DASHBOARD_COLUMN_CHART_TWO_ATTRIBUTES_WITH_TWO_DRILLABLED_ATTRIBUTES = "Dashboard column chart with two attributes and two drillable attributes";
    private final String COLUMN_CHART_TWO_ATTRIBUTES_WITH_TWO_DRILLABLED_ATTRIBUTES_SEPARATE = "Column chart with two attributes and two drillable separate attributes";
    private final String DASHBOARD_COLUMN_CHART_TWO_ATTRIBUTES_TWO_DRILLABLED_ATTRIBUTES_SEPARATE = "Dashboard with column chart two attributes that has two drillable separate attributes";
    private final String HEAT_MAP_ONLY_MEASURES = "Heat map only measures";
    private final String DASHBOARD_HEAT_MAP_ONLY_MEASURES = "Dashboard drill down with heat map only measures";

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
        createInsight(TABLE_TWO_ATTRIBUTES_WITH_TWO_DRILLABLED_ATTRIBUTES, asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT), asList(
            Pair.of(ATTR_IS_WON, CategoryBucket.Type.ATTRIBUTE), Pair.of(ATTR_DEPARTMENT, CategoryBucket.Type.ATTRIBUTE), Pair.of(ATTR_REGION, CategoryBucket.Type.COLUMNS)));
        createColumnChart(COLUMN_CHART_ONLY_MEASURES, asList(METRIC_AMOUNT, METRIC_AVG_AMOUNT), asList());
        createColumnChart(COLUMN_CHART_ONLY_VIEWBY_MEASURES, asList(METRIC_AMOUNT), asList(Pair.of(ATTR_IS_WON, CategoryBucket.Type.VIEW)));
        createColumnChart(COLUMN_CHART_ONLY_STACKBY_MEASURES, asList(METRIC_AMOUNT), asList(Pair.of(ATTR_IS_WON, CategoryBucket.Type.STACK)));
        createColumnChart(COLUMN_CHART_MEASURES_STACKBY_VIEWBY, asList(METRIC_AMOUNT), asList(Pair.of(ATTR_REGION, CategoryBucket.Type.VIEW), Pair.of(ATTR_IS_WON, CategoryBucket.Type.STACK)));
        createColumnChart(COLUMN_CHART_TWO_DRILLABLED_ATTRIBUTES, asList(METRIC_AMOUNT), asList(Pair.of(ATTR_IS_WON, CategoryBucket.Type.VIEW), Pair.of(ATTR_REGION, CategoryBucket.Type.STACK)));
        createColumnChart(COLUMN_CHART_TWO_ATTRIBUTES_WITH_TWO_DRILLABLED_ATTRIBUTES, asList(METRIC_AMOUNT), 
            asList(Pair.of(ATTR_IS_WON, CategoryBucket.Type.VIEW), Pair.of(ATTR_DEPARTMENT, CategoryBucket.Type.VIEW)));
        createHeatMap(HEAT_MAP_ONLY_MEASURES,asList(METRIC_AMOUNT), asList());
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
                asList("false","true"), asList("Direct Sales","Inside Sales"), asList()},
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

        assertFalse(pivotTableReport.isCellUnderlinedByElement(pivotTableReport.getHeaderElement(ATTRIBUTE_TITLE_TRUE,0)),"Cannot drilldown with table that only has measures and columns.");
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "tableData")
    public void drillDownOnTableWithOneDrillable(String dashboard, String insight, List<String> comparelist, List<String> compareDrilllist, List<String> compareBodyDrilllist) {

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
        assertEquals(drillContinueModalDialog.getTitleInsight(), TABLE_TWO_DRILLABLED_ATTRIBUTES + " › " + ATTRIBUTE_TITLE_TRUE + " › " + ATTRIBUTE_TITLE_DIRECT_SALES);
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
        assertEquals(drillContinueAgainModalDialog.getTitleInsight(), TABLE_TWO_DRILLABLED_ATTRIBUTES + " › " + ATTRIBUTE_TITLE_TRUE + " › " + ATTRIBUTE_TITLE_INSIDE_SALES);
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
        assertEquals(drillContinueAModalDialog.getTitleInsight(), TABLE_TWO_ATTRIBUTES_WITH_TWO_DRILLABLED_ATTRIBUTES + " › " + ATTRIBUTE_TITLE_TRUE + " › " + ATTRIBUTE_TITLE_DIRECT_SALES);
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
        assertEquals(drillContinueAgainModalDialog.getTitleInsight(), TABLE_TWO_ATTRIBUTES_WITH_TWO_DRILLABLED_ATTRIBUTES + " › " + ATTRIBUTE_TITLE_TRUE + " › " + ATTRIBUTE_TITLE_INSIDE_SALES);
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
        List<String> expectedValues = asList("2010", "false", "true", "2011", "false", "true", "2012", "false", "true", "2013", "false", "2014", "false", "2016", "false", "2017", "false");
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
        assertEquals(drillContinueAModalDialog.getTitleInsight(), TABLE_TWO_ATTRIBUTES_WITH_TWO_DRILLABLED_ATTRIBUTES_SEPARATE + " › " + ATTRIBUTE_TITLE_TRUE + " › " + ATTRIBUTE_TITLE_DIRECT_SALES);
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
            { DASHBOARD_COLUMN_CHART_ONLY_MEASURES,COLUMN_CHART_ONLY_MEASURES, asList("$116,625,456.54","$20,286.22")},
            { DASHBOARD_HEAT_MAP_ONLY_MEASURES,HEAT_MAP_ONLY_MEASURES, asList("$116,625,456.54")}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "chartOnlyMeasures")
    public void drillDownOnColumnChartOnlyMeasures(String dashboard, String insight, List<String> compareData) {
        initIndigoDashboardsPage().addDashboard().addInsight(insight);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.changeDashboardTitle(dashboard).waitForWidgetsLoading().saveEditModeWithWidgets();
        indigoDashboardsPage.waitForWidgetsLoading().getFirstWidget(Insight.class).getChartReport();
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, insight).getChartReport();
        assertThat(chartReport.getDataLabels(), equalTo(compareData));
        chartReport.clickOnElement(Pair.of(0, 0));
        chartReport.getTooltipTextOnTrackerByIndex(0,0);
        assertFalse(chartReport.isColumnHighlighted(Pair.of(0, 0)),"Cannot drilldown with chart that only has measures.");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void drillDownOnColumnChartOnlyMeasuresAndViewby() {
        initIndigoDashboardsPage().addDashboard().addInsight(COLUMN_CHART_ONLY_VIEWBY_MEASURES);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.changeDashboardTitle(DASHBOARD_COLUMN_CHART_ONLY_VIEWBY_MEASURES).waitForWidgetsLoading().saveEditModeWithWidgets();
        indigoDashboardsPage.waitForWidgetsLoading().getFirstWidget(Insight.class).getChartReport();
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, COLUMN_CHART_ONLY_VIEWBY_MEASURES).getChartReport();
        assertThat(chartReport.getDataLabels(), equalTo(asList("$78,314,703.09","$38,310,753.45")));

        assertTrue(chartReport.isColumnHighlighted(Pair.of(0, 0)),"Can drilldown with chart that has viewby.");
        chartReport.clickOnElement(Pair.of(0, 0));
        indigoDashboardsPage.waitForDrillModalDialogLoading();
        DrillModalDialog drillModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillModalDialog.getTitleInsight(), COLUMN_CHART_ONLY_VIEWBY_MEASURES + " › " + ATTRIBUTE_TITLE_FALSE);
        ChartReport targetReport = drillModalDialog.getChartReport();
        assertThat(targetReport.getDataLabels(), equalTo(asList("$53,901,464.88","$24,413,238.21")));
    }

    @DataProvider(name = "columnsChartStackData")
    public Object[][] getColumnChart() {
        return new Object[][]{
            {
                DASHBOARD_COLUMN_CHART_ONLY_STACKBY_MEASURES,COLUMN_CHART_ONLY_STACKBY_MEASURES, 
                asList("$116,625,456.54"), asList("$53,901,464.88","$24,413,238.21"), asList("$78,314,703.09")},
            {
                DASHBOARD_COLUMN_CHART_ONLY_MEASURES_STACKBY_VIEWBY,COLUMN_CHART_MEASURES_STACKBY_VIEWBY, 
                asList("$28,017,096.42","$88,608,360.12"), asList("$13,594,424.44","$3,879,912.33"), asList("$17,474,336.77")},
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "columnsChartStackData")
    public void drillDownOnColumnChartWithStackby(String dashboard, String insight, List<String> compareStackTotal, List<String> compareDrilllist, List<String> compareStackTotalDrill) {
        initIndigoDashboardsPage().addDashboard().addInsight(insight);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.changeDashboardTitle(dashboard).waitForWidgetsLoading().saveEditModeWithWidgets();
        indigoDashboardsPage.waitForWidgetsLoading().getFirstWidget(Insight.class).getChartReport();
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, insight).getChartReport();
        assertThat(chartReport.getTotalsStackedColumn(), equalTo(compareStackTotal));

        assertTrue(chartReport.isColumnHighlighted(Pair.of(0, 0)),"Can drilldown with chart that has stackby or viewby.");
        chartReport.clickOnElement(Pair.of(0, 0));
        indigoDashboardsPage.waitForDrillModalDialogLoading();
        DrillModalDialog drillModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillModalDialog.getTitleInsight(), insight + " › " + ATTRIBUTE_TITLE_FALSE);
        ChartReport targetReport = drillModalDialog.getChartReport();
        assertThat(targetReport.getDataLabels(), equalTo(compareDrilllist));
        assertThat(targetReport.getTotalsStackedColumn(), equalTo(compareStackTotalDrill));
    }

    @Test(dependsOnGroups = {"createProject"}, description = "This test case covered the drilling down continue on a column chart")
    public void drillDownOnColumnChartWithTwoDrillableAttributes() {
        initIndigoDashboardsPage().addDashboard().addInsight(COLUMN_CHART_TWO_DRILLABLED_ATTRIBUTES);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.changeDashboardTitle(DASHBOARD_COLUMN_CHART_TWO_DRILLABLED_ATTRIBUTES).waitForWidgetsLoading().saveEditModeWithWidgets();
        indigoDashboardsPage.waitForWidgetsLoading().getFirstWidget(Insight.class).getChartReport();
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, COLUMN_CHART_TWO_DRILLABLED_ATTRIBUTES).getChartReport();
        assertThat(chartReport.getTotalsStackedColumn(), equalTo(asList("$78,314,703.09","$38,310,753.45")));

        assertTrue(chartReport.isColumnHighlighted(Pair.of(0, 0)),"Can drilldown with chart that has stackby or viewby.");
        chartReport.clickOnElement(Pair.of(0, 0));
        indigoDashboardsPage.waitForDrillModalDialogLoading();
        DrillModalDialog drillModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillModalDialog.getTitleInsight(), COLUMN_CHART_TWO_DRILLABLED_ATTRIBUTES + " › " + ATTRIBUTE_TITLE_FALSE);
        ChartReport targetReport = drillModalDialog.getChartReport();
        assertThat(targetReport.getDataLabels(), equalTo(asList("$13,594,424.44","$3,879,912.33","$40,307,040.44","$20,533,325.88")));
        assertThat(targetReport.getTotalsStackedColumn(), equalTo(asList("$53,901,464.88","$24,413,238.21")));

        targetReport.clickOnElement(Pair.of(0, 0));
        indigoDashboardsPage.waitForDrillModalDialogLoading();
        DrillModalDialog drillContinueModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillContinueModalDialog.getTitleInsight(), COLUMN_CHART_TWO_DRILLABLED_ATTRIBUTES + " › " + ATTRIBUTE_TITLE_FALSE + " › " + ATTRIBUTE_TITLE_DIRECT_SALES);
        ChartReport targetContinueDrillReport = drillContinueModalDialog.getChartReport();
        List<String> expectedContinueDrillValuesModal = asList("$9,470,887.67", "$10,310,590.88", "$23,530,577.73", "$2,953,747.46","$3,470,688.72", "$4,164,972.42");
        assertThat(targetContinueDrillReport.getTotalsStackedColumn(), equalTo(expectedContinueDrillValuesModal));

        DrillModalDialog.getInstance(browser).reset();
        assertEquals(drillModalDialog.getTitleInsight(), COLUMN_CHART_TWO_DRILLABLED_ATTRIBUTES + " › " + ATTRIBUTE_TITLE_FALSE);
        assertThat(targetReport.getDataLabels(), equalTo(asList("$13,594,424.44","$3,879,912.33","$40,307,040.44","$20,533,325.88")));
        assertThat(targetReport.getTotalsStackedColumn(), equalTo(asList("$53,901,464.88","$24,413,238.21")));
    }

    @Test(dependsOnGroups = {"createProject"}, description = "This test case covered the drilling down on a column chart that has two drillable attributes")
    public void drillDownOnColumnChartTwoAttributesWithTwoDrillableAttributes() {
        initIndigoDashboardsPage().addDashboard().addInsight(COLUMN_CHART_TWO_ATTRIBUTES_WITH_TWO_DRILLABLED_ATTRIBUTES);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.changeDashboardTitle(DASHBOARD_COLUMN_CHART_TWO_ATTRIBUTES_WITH_TWO_DRILLABLED_ATTRIBUTES).waitForWidgetsLoading().saveEditModeWithWidgets();
        indigoDashboardsPage.waitForWidgetsLoading().getFirstWidget(Insight.class).getChartReport();
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, COLUMN_CHART_TWO_ATTRIBUTES_WITH_TWO_DRILLABLED_ATTRIBUTES).getChartReport();
        assertThat(chartReport.getDataLabels(), equalTo(asList("$53,901,464.88","$24,413,238.21","$26,504,860.08","$11,805,893.37")));

        assertTrue(chartReport.isColumnHighlighted(Pair.of(0, 0)),"Can drilldown with chart that has stackby or viewby.");
        chartReport.openDrillingPicker(Pair.of(0, 0)).drillDown(ATTRIBUTE_TITLE_FALSE);
        indigoDashboardsPage.waitForDrillModalDialogLoading();
        DrillModalDialog drillModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillModalDialog.getTitleInsight(), COLUMN_CHART_TWO_ATTRIBUTES_WITH_TWO_DRILLABLED_ATTRIBUTES + " › " + ATTRIBUTE_TITLE_FALSE);
        ChartReport targetReport = drillModalDialog.getChartReport();
        assertThat(targetReport.getDataLabels(), equalTo(asList("$53,901,464.88", "$24,413,238.21")));

        assertTrue(targetReport.isColumnHighlighted(Pair.of(0, 0)),"Can continue drilldown on modal window.");
        targetReport.clickOnElement(Pair.of(0, 0));
        indigoDashboardsPage.waitForDrillModalDialogLoading();
        DrillModalDialog drillContinueModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillContinueModalDialog.getTitleInsight(), COLUMN_CHART_TWO_ATTRIBUTES_WITH_TWO_DRILLABLED_ATTRIBUTES + " › " + ATTRIBUTE_TITLE_FALSE + " › " + ATTRIBUTE_TITLE_DIRECT_SALES);
        ChartReport targetContinueDrillReport = drillContinueModalDialog.getChartReport();
        List<String> expectedContinueDrillValuesModal = asList("$9,470,887.67", "$10,310,590.88", "$23,530,577.73", "$2,953,747.46","$3,470,688.72", "$4,164,972.42");
        assertThat(targetContinueDrillReport.getDataLabels(), equalTo(expectedContinueDrillValuesModal));
        assertFalse(targetContinueDrillReport.isColumnHighlighted(Pair.of(0, 0)),"Cannot drilldown continue.");
        
        DrillModalDialog.getInstance(browser).reset();
        DrillModalDialog resetDrillModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(resetDrillModalDialog.getTitleInsight(), COLUMN_CHART_TWO_ATTRIBUTES_WITH_TWO_DRILLABLED_ATTRIBUTES + " › " + ATTRIBUTE_TITLE_FALSE);
        ChartReport targetResetReport = resetDrillModalDialog.getChartReport();
        assertThat(targetResetReport.getDataLabels(), equalTo(asList("$53,901,464.88", "$24,413,238.21")));

        DrillModalDialog.getInstance(browser).close();

        chartReport.openDrillingPicker(Pair.of(0, 0)).drillDown(ATTRIBUTE_TITLE_DIRECT_SALES);
        indigoDashboardsPage.waitForDrillModalDialogLoading();
        DrillModalDialog drillBModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillBModalDialog.getTitleInsight(), COLUMN_CHART_TWO_ATTRIBUTES_WITH_TWO_DRILLABLED_ATTRIBUTES + " › " + ATTRIBUTE_TITLE_DIRECT_SALES);
        ChartReport targetReportB = drillBModalDialog.getChartReport();
        assertThat(targetReportB.getDataLabels(), equalTo(asList("$9,470,887.67", "$10,310,590.88", "$23,530,577.73", "$2,953,747.46","$3,470,688.72", "$4,164,972.42")));
        assertFalse(targetReportB.isColumnHighlighted(Pair.of(0, 0)),"Cannot drilldown continue.");
    }

    @Test(dependsOnGroups = {"createProject"}, description = "This test case covered the drilling down on a column chart that has two drillable attributes separaty")
    public void drillDownOnColumnChartTwoAttributesWithTwoDrillableAttributesSeparate() {
        AnalysisPage analysisPage = initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_AMOUNT).addDate().addAttribute(ATTR_IS_WON);
        analysisPage.saveInsight(COLUMN_CHART_TWO_ATTRIBUTES_WITH_TWO_DRILLABLED_ATTRIBUTES_SEPARATE).waitForReportComputing();
        initIndigoDashboardsPage().addDashboard().addInsight(COLUMN_CHART_TWO_ATTRIBUTES_WITH_TWO_DRILLABLED_ATTRIBUTES_SEPARATE);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectStaticPeriod("01/01/2011", "12/31/2012").apply();

        indigoDashboardsPage.changeDashboardTitle(DASHBOARD_COLUMN_CHART_TWO_ATTRIBUTES_TWO_DRILLABLED_ATTRIBUTES_SEPARATE).waitForWidgetsLoading().saveEditModeWithWidgets();
        indigoDashboardsPage.waitForWidgetsLoading().getFirstWidget(Insight.class).getChartReport();
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, COLUMN_CHART_TWO_ATTRIBUTES_WITH_TWO_DRILLABLED_ATTRIBUTES_SEPARATE).getChartReport();
        assertThat(chartReport.getDataLabels(), equalTo(asList("$33,160,441.09","$20,528,222.66","$31,325,066.13","$8,323,300.01")));
        
        assertTrue(chartReport.isColumnHighlighted(Pair.of(0, 0)),"Can drilldown with chart that has stackby or viewby.");
        chartReport.openDrillingPicker(Pair.of(0, 0)).drillDown(ATTRIBUTE_TITLE_FALSE);
        indigoDashboardsPage.waitForDrillModalDialogLoading();
        DrillModalDialog drillModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillModalDialog.getTitleInsight(), COLUMN_CHART_TWO_ATTRIBUTES_WITH_TWO_DRILLABLED_ATTRIBUTES_SEPARATE + " › " + ATTRIBUTE_TITLE_FALSE);
        ChartReport targetReport = drillModalDialog.getChartReport();
        assertThat(targetReport.getDataLabels(), equalTo(asList("$26,101,838.99", "$7,058,602.10")));

        DrillModalDialog.getInstance(browser).close();

        chartReport.openDrillingPicker(Pair.of(0, 0)).drillDown(ATTRIBUTE_TITLE_2011);
        indigoDashboardsPage.waitForDrillModalDialogLoading();
        DrillModalDialog drillBModalDialog = DrillModalDialog.getInstance(browser);
        assertEquals(drillBModalDialog.getTitleInsight(), COLUMN_CHART_TWO_ATTRIBUTES_WITH_TWO_DRILLABLED_ATTRIBUTES_SEPARATE + " › " + ATTRIBUTE_TITLE_2011);
    }

    private String createInsight(String insightTitle, List<String> metricsTitle,
                             List<Pair<String, CategoryBucket.Type>> attributeConfigurations) {
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

    private String createColumnChart(String insightTitle, List<String> metricsTitle,
                             List<Pair<String, CategoryBucket.Type>> attributeConfigurations) {
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

    private String createHeatMap(String insightTitle, List<String> metricsTitle,
                             List<Pair<String, CategoryBucket.Type>> attributeConfigurations) {
    return indigoRestRequest.createInsight(
        new InsightMDConfiguration(insightTitle, ReportType.HEAT_MAP)
            .setMeasureBucket(metricsTitle.stream()
                .map(metric -> MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric)))
                .collect(toList()))
            .setCategoryBucket(attributeConfigurations.stream()
                .map(attribute -> CategoryBucket.createCategoryBucket(
                    getAttributeByTitle(attribute.getKey()), attribute.getValue()))
                .collect(toList())));
    }
}
